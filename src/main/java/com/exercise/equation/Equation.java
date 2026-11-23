package com.exercise.equation;

import com.exercise.common.Limits;
import com.exercise.expression.Expression;
import com.exercise.expression.ExpressionCanonicalizer;
import com.exercise.operation.Operation;

import java.util.List;
import java.util.Objects;

/**
 * 算式的抽象基类（领域模型的根）。
 *
 * <p>一道"合法"的算式必须同时满足：</p>
 * <ol>
 *   <li>参与运算的数字均满足 {@code 0 < 值 < 100} 的正整数；</li>
 *   <li>运算过程中的每一个中间结果也满足该范围；</li>
 *   <li>最终结果同样满足该范围；</li>
 *   <li>除法必须严格整除，除数为 0 视为非法。</li>
 * </ol>
 *
 * <p>以上四条在构造阶段由 {@link Expression#evaluationTrace()} 一次性校验：轨迹里
 * 既有全部叶子操作数，也有全部中间结果与最终结果。任何一条不满足，构造函数立即
 * 抛出 {@link IllegalArgumentException}（fail-fast），因此只要拿到了算式对象，
 * 就一定是合法题目——这正是里氏替换原则的体现：所有子类对外都保持"构造即合法"
 * 的同一契约。</p>
 *
 * <p>算式对象构造完成后不可变，{@code equals}/{@code hashCode} 基于不可变的
 * 规范化键，可以被 {@code HashSet}/{@code LinkedHashSet} 安全地去重。</p>
 */
public abstract class Equation {

    private final Expression expression;
    private final String canonicalKey;

    /**
     * 构造算式并立即校验数值红线。
     *
     * @param expression 算式表达式，不能为空
     * @throws NullPointerException 表达式为空引用时抛出
     * @throws IllegalArgumentException 存在无定义运算、出现越界值或表达式类型不受支持时抛出；
     *                                  运算层因除零/不能整除抛出的 ArithmeticException 会作为 cause 保留
     */
    protected Equation(Expression expression) {
        this.expression = Objects.requireNonNull(expression, "算式表达式不能为空");

        List<Integer> trace;
        try {
            trace = expression.evaluationTrace();
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("算式不合法：" + exception.getMessage(), exception);
        }
        for (int value : trace) {
            if (!Limits.isInRange(value)) {
                throw new IllegalArgumentException(
                        "算式不合法：数值 " + value + " 超出 (0, 100) 范围（参与运算的数字、中间结果和最终结果都必须为正整数）："
                                + expression.toInfix());
            }
        }
        this.canonicalKey = ExpressionCanonicalizer.canonicalize(expression);
    }

    /**
     * 校验一个操作数是否位于 {@code [1, 99]}。
     *
     * @param value 操作数
     * @param name  操作数名称，用于异常信息
     * @throws IllegalArgumentException 越界时抛出
     */
    protected static void requireOperandInRange(int value, String name) {
        if (!Limits.isInRange(value)) {
            throw new IllegalArgumentException(name + "必须是 " + Limits.MIN_OPERAND + "~" + Limits.MAX_OPERAND
                    + " 之间的正整数，当前为 " + value);
        }
    }

    /**
     * 返回底层表达式。
     *
     * @return 表达式
     */
    public final Expression getExpression() {
        return expression;
    }

    /**
     * 返回题目的最终结果。
     *
     * @return 最终结果
     */
    public final int getAnswer() {
        return expression.evaluate();
    }

    /**
     * 返回不带等号的中缀表达式文本，例如 {@code "2 + 3 * 4"}。
     *
     * @return 中缀表达式
     */
    public final String getText() {
        return expression.toInfix();
    }

    /**
     * 返回求值轨迹（全部操作数、中间结果与最终结果）。
     *
     * @return 不可变的求值轨迹
     */
    public final List<Integer> getTrace() {
        return expression.evaluationTrace();
    }

    /**
     * 返回规范化键，用于去重。
     *
     * @return 规范化键
     */
    public final String getCanonicalKey() {
        return canonicalKey;
    }

    /**
     * 返回本题包含的运算符，按从左到右的书写顺序排列。
     *
     * @return 不可变的运算符列表
     */
    public abstract List<Operation> getOperations();

    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Equation that)) {
            return false;
        }
        return canonicalKey.equals(that.canonicalKey);
    }

    @Override
    public final int hashCode() {
        return canonicalKey.hashCode();
    }

    @Override
    public String toString() {
        return getText() + " = " + getAnswer();
    }
}
