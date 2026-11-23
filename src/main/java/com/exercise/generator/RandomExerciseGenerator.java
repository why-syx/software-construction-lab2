package com.exercise.generator;

import com.exercise.common.Limits;
import com.exercise.equation.Equation;
import com.exercise.equation.ThreeOperandEquation;
import com.exercise.operation.Operation;
import com.exercise.operation.Operations;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * 随机练习题生成器。
 *
 * <p><b>核心思想是"构造式采样"而非"生成后拒绝"。</b>如果先让三个数各自独立地取
 * 1~99，再判断整道题是否合法，三数题的合法率只有百分之十几；当请求数量接近题库
 * 容量时，碰撞概率还会按"生日问题"规律急剧上升，最终可能长时间抽不到新题。</p>
 *
 * <p>本实现反过来做：先确定"先算的那一对数"，按运算符反推出使该步结果落在
 * (0, 100) 内的取值范围；再由该中间结果反推第三个数（或第一个数）的合法范围。
 * 这样每一次采样都 100% 合法，不需要任何"生成后拒绝"的重试。</p>
 *
 * <p>去重使用 {@link LinkedHashSet}，依赖 {@link Equation} 基于规范化键重写的
 * {@code equals}/{@code hashCode}；同时设置尝试次数上限，保证在极端情况下
 * 抛出明确异常而不是死循环。</p>
 */
public final class RandomExerciseGenerator implements ExerciseGenerator {

    /** 生成循环的最小尝试次数。 */
    private static final int MIN_ATTEMPTS = 10_000;

    /** 每道题平均允许的尝试次数（用于计算总尝试上限）。 */
    private static final int ATTEMPTS_PER_QUESTION = 200;

    /** 为固定运算符组合构造一道题时允许的最大重采样次数。 */
    private static final int MAX_CONSTRUCTION_ATTEMPTS = 1_000;

    private final Random random;

    /**
     * 使用系统默认随机源构造生成器。
     */
    public RandomExerciseGenerator() {
        this(new Random());
    }

    /**
     * 使用指定的随机源构造生成器，便于单元测试复现同一批题目。
     *
     * @param random 随机源，不能为空
     */
    public RandomExerciseGenerator(Random random) {
        this.random = Objects.requireNonNull(random, "随机源不能为空");
    }

    @Override
    public List<Equation> generate(int count) {
        if (count < Limits.MIN_UI_COUNT || count > Limits.MAX_GENERATE_COUNT) {
            throw new IllegalArgumentException(
                    "题目数量必须在 " + Limits.MIN_UI_COUNT + "~" + Limits.MAX_GENERATE_COUNT + " 之间，当前为 " + count);
        }

        Set<Equation> unique = new LinkedHashSet<>();
        int maxAttempts = Math.max(MIN_ATTEMPTS, count * ATTEMPTS_PER_QUESTION);
        int attempts = 0;
        while (unique.size() < count && attempts < maxAttempts) {
            attempts++;
            unique.add(nextEquation());
        }
        if (unique.size() < count) {
            throw new ExerciseGenerationException(
                    "尝试 " + attempts + " 次后只生成了 " + unique.size() + " 道不重复题目，"
                            + "无法满足 " + count + " 道的要求，请减少题目数量后重试。");
        }
        return List.copyOf(unique);
    }

    /**
     * 随机选择两个运算符并构造一道合法题目。
     *
     * @return 合法且不重复校验由上层集合负责的算式
     */
    private Equation nextEquation() {
        List<Operation> operations = Operations.all();
        Operation first = operations.get(random.nextInt(operations.size()));
        Operation second = operations.get(random.nextInt(operations.size()));

        for (int attempt = 0; attempt < MAX_CONSTRUCTION_ATTEMPTS; attempt++) {
            Equation equation = tryConstruct(first, second);
            if (equation != null) {
                return equation;
            }
        }
        throw new ExerciseGenerationException("运算符组合 " + first + " / " + second + " 无法构造出合法算式");
    }

    /**
     * 尝试用给定运算符构造一道题；若当前抽样无法满足外层约束则返回 {@code null}，
     * 由调用方重新抽样。
     *
     * @param first  第一个运算符
     * @param second 第二个运算符
     * @return 构造成功的算式，或 {@code null}
     */
    private Equation tryConstruct(Operation first, Operation second) {
        if (first.precedence() >= second.precedence()) {
            // 先算前两个数：(a op1 b) op2 c
            int[] inner = sampleInnerPair(first);
            int innerValue = first.apply(inner[0], inner[1]);
            Integer third = sampleRightOperand(second, innerValue);
            if (third == null) {
                return null;
            }
            return new ThreeOperandEquation(inner[0], inner[1], third, first, second);
        }
        // 先算后两个数：a op1 (b op2 c)
        int[] inner = sampleInnerPair(second);
        int innerValue = second.apply(inner[0], inner[1]);
        Integer firstOperand = sampleLeftOperand(first, innerValue);
        if (firstOperand == null) {
            return null;
        }
        return new ThreeOperandEquation(firstOperand, inner[0], inner[1], first, second);
    }

    /**
     * 构造一对操作数，使 {@code x op y} 的结果严格落在 (0, 100) 内。
     *
     * <p>为避免出现 {@code 62 * 1 * 1}、{@code 35 / 1} 这类过于平凡、缺乏练习价值的题目，
     * 所有操作数一律从 2 开始取值；除法与乘法的另一个因子也至少为 2。</p>
     *
     * @param operation 内层运算符
     * @return 两个操作数组成的数组 {@code {x, y}}
     */
    private int[] sampleInnerPair(Operation operation) {
        switch (operation.symbol()) {
            case "+": {
                int x = 2 + random.nextInt(96);           // 2~97
                int y = 2 + random.nextInt(98 - x);       // 2~(99-x)
                return new int[]{x, y};
            }
            case "-": {
                int x = 4 + random.nextInt(96);           // 4~99
                int y = 2 + random.nextInt(x - 3);        // 2~(x-2)，保证结果 >= 2
                return new int[]{x, y};
            }
            case "*": {
                int x = 2 + random.nextInt(48);           // 2~49
                int y = 2 + random.nextInt(99 / x - 1);   // 2~(99/x)
                return new int[]{x, y};
            }
            case "/": {
                int divisor = 2 + random.nextInt(48);     // 2~49
                int quotient = 2 + random.nextInt(99 / divisor - 1); // 2~(99/divisor)
                return new int[]{divisor * quotient, divisor};
            }
            default:
                throw new IllegalArgumentException("未知运算符：" + operation.symbol());
        }
    }

    /**
     * 已知左操作数 {@code inner}，构造右操作数 {@code c}，使 {@code inner op c} 合法。
     *
     * @param operation 外层运算符
     * @param inner     已知的左值（中间结果）
     * @return 合法的右操作数；不存在时返回 {@code null}
     */
    private Integer sampleRightOperand(Operation operation, int inner) {
        switch (operation.symbol()) {
            case "+": {
                int limit = 99 - inner;
                return limit >= 2 ? 2 + random.nextInt(limit - 1) : null;
            }
            case "-": {
                return inner >= 3 ? 2 + random.nextInt(inner - 2) : null;
            }
            case "*": {
                int limit = 99 / inner;
                return limit >= 2 ? 2 + random.nextInt(limit - 1) : null;
            }
            case "/": {
                List<Integer> divisors = collectProperDivisors(inner);
                return divisors.isEmpty() ? null : divisors.get(random.nextInt(divisors.size()));
            }
            default:
                throw new IllegalArgumentException("未知运算符：" + operation.symbol());
        }
    }

    /**
     * 已知右操作数 {@code inner}，构造左操作数 {@code a}，使 {@code a op inner} 合法。
     *
     * @param operation 外层运算符
     * @param inner     已知的右值（中间结果）
     * @return 合法的左操作数；不存在时返回 {@code null}
     */
    private Integer sampleLeftOperand(Operation operation, int inner) {
        switch (operation.symbol()) {
            case "+": {
                int limit = 99 - inner;
                return limit >= 2 ? 2 + random.nextInt(limit - 1) : null;
            }
            case "-": {
                // 需要 inner + 2 <= a <= 99，保证结果至少为 2
                if (inner > 97) {
                    return null;
                }
                return inner + 2 + random.nextInt(98 - inner);
            }
            case "*": {
                int limit = 99 / inner;
                return limit >= 2 ? 2 + random.nextInt(limit - 1) : null;
            }
            case "/": {
                // 需要 a 是 inner 的 integer 倍，且 a <= 99
                int limit = 99 / inner;
                return limit >= 2 ? inner * (2 + random.nextInt(limit - 1)) : null;
            }
            default:
                throw new IllegalArgumentException("未知运算符：" + operation.symbol());
        }
    }

    /**
     * 收集一个正整数除 1 和自身以外的全部正因数。
     *
     * @param value 正整数
     * @return 真因数列表；质数返回空列表
     */
    private static List<Integer> collectProperDivisors(int value) {
        List<Integer> divisors = new ArrayList<>();
        for (int candidate = 2; candidate <= value / 2; candidate++) {
            if (value % candidate == 0) {
                divisors.add(candidate);
            }
        }
        return divisors;
    }
}
