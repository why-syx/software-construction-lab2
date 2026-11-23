package com.exercise.generator;

import com.exercise.common.Limits;
import com.exercise.equation.Equation;
import com.exercise.equation.ThreeOperandEquation;
import com.exercise.operation.Operation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 生成器单元测试。
 *
 * <p>除了常规的数量与去重断言外，本测试还实现了一个"独立的求值器"：它只读取算式
 * 的三个操作数与两个运算符，按数学优先级重新计算一遍，并与被测对象的答案、求值
 * 轨迹逐项比对。这样可以避免"测试和实现共用同一段有 bug 的逻辑"而互相掩护。</p>
 */
@DisplayName("练习题生成器测试")
class RandomExerciseGeneratorTest {

    @Test
    @DisplayName("生成指定数量且自动去重")
    void generatesRequestedCountWithoutDuplicates() {
        RandomExerciseGenerator generator = new RandomExerciseGenerator(new Random(2026));
        List<Equation> equations = generator.generate(30);
        assertEquals(30, equations.size());
        assertEquals(30, new HashSet<>(equations).size());
        assertEquals(30, new LinkedHashSet<>(equations).size());
    }

    @Test
    @DisplayName("一万道题中每一道都满足全部约束")
    void everyEquationSatisfiesAllConstraints() {
        for (long seed = 1; seed <= 10; seed++) {
            RandomExerciseGenerator generator = new RandomExerciseGenerator(new Random(seed));
            List<Equation> equations = generator.generate(1000);
            for (Equation equation : equations) {
                assertThreeOperandConstraints(equation);
            }
        }
    }

    @Test
    @DisplayName("16 种运算符组合都能被生成到")
    void allOperatorCombinationsAppear() {
        RandomExerciseGenerator generator = new RandomExerciseGenerator(new Random(42));
        List<Equation> equations = generator.generate(1000);
        Set<String> combinations = new HashSet<>();
        for (Equation equation : equations) {
            List<Operation> operations = equation.getOperations();
            combinations.add(operations.get(0).symbol() + "" + operations.get(1).symbol());
        }
        assertEquals(16, combinations.size());
    }

    @Test
    @DisplayName("同一个随机种子生成完全相同的题目序列")
    void sameSeedProducesSameSequence() {
        RandomExerciseGenerator first = new RandomExerciseGenerator(new Random(123456789L));
        RandomExerciseGenerator second = new RandomExerciseGenerator(new Random(123456789L));
        List<Equation> firstList = first.generate(50);
        List<Equation> secondList = second.generate(50);
        assertEquals(firstList, secondList);
        for (int index = 0; index < firstList.size(); index++) {
            assertEquals(firstList.get(index).getText(), secondList.get(index).getText());
        }
    }

    @Test
    @DisplayName("不同随机种子通常生成不同题目")
    void differentSeedsUsuallyDiffer() {
        List<Equation> first = new RandomExerciseGenerator(new Random(1)).generate(50);
        List<Equation> second = new RandomExerciseGenerator(new Random(2)).generate(50);
        assertNotEquals(first, second);
    }

    @Test
    @DisplayName("支持 1 道与 500 道两种边界数量")
    void supportsBoundaryCounts() {
        RandomExerciseGenerator generator = new RandomExerciseGenerator(new Random(7));
        assertEquals(1, generator.generate(1).size());
        assertEquals(500, generator.generate(500).size());
    }

    @Test
    @DisplayName("大批量生成依然保持唯一")
    void largeBatchRemainsUnique() {
        RandomExerciseGenerator generator = new RandomExerciseGenerator(new Random(99));
        List<Equation> equations = generator.generate(3000);
        assertEquals(3000, equations.size());
        assertEquals(3000, new HashSet<>(equations).size());
        for (Equation equation : equations) {
            assertThreeOperandConstraints(equation);
        }
    }

    @Test
    @DisplayName("非法数量被拒绝")
    void invalidCountRejected() {
        RandomExerciseGenerator generator = new RandomExerciseGenerator(new Random(1));
        assertThrows(IllegalArgumentException.class, () -> generator.generate(0));
        assertThrows(IllegalArgumentException.class, () -> generator.generate(-1));
        assertThrows(IllegalArgumentException.class, () -> generator.generate(Limits.MAX_GENERATE_COUNT + 1));
    }

    @Test
    @DisplayName("随机源不能为空")
    void nullRandomRejected() {
        assertThrows(NullPointerException.class, () -> new RandomExerciseGenerator(null));
    }

    /**
     * 断言一道题满足全部业务约束，并用独立求值器交叉验证。
     *
     * @param equation 待检查的算式
     */
    private static void assertThreeOperandConstraints(Equation equation) {
        ThreeOperandEquation three = assertInstanceOf(ThreeOperandEquation.class, equation);

        int first = three.getFirst();
        int second = three.getSecond();
        int third = three.getThird();
        for (int operand : new int[]{first, second, third}) {
            assertTrue(Limits.isInRange(operand), "操作数越界：" + operand);
        }
        assertEquals(2, three.getOperations().size(), "必须包含两个运算符");

        // 独立求值：不依赖被测对象的表达式树，只按数学优先级重新计算。
        List<Integer> steps = new ArrayList<>();
        int expectedAnswer = evaluateIndependently(three, steps);
        int expectedStep = steps.get(0);
        int expectedFinal = steps.get(1);
        assertTrue(Limits.isInRange(expectedStep), "中间结果越界：" + expectedStep);
        assertTrue(Limits.isInRange(expectedFinal), "最终结果越界：" + expectedFinal);
        assertEquals(expectedAnswer, three.getAnswer());
        assertEquals(expectedFinal, three.getAnswer());

        // 求值轨迹必须与独立计算结果逐项一致（含后序顺序）。
        List<Integer> expectedTrace = new ArrayList<>();
        expectedTrace.add(first);
        expectedTrace.add(second);
        if (three.getFirstOperation().precedence() >= three.getSecondOperation().precedence()) {
            expectedTrace.add(expectedStep);
            expectedTrace.add(third);
        } else {
            expectedTrace.add(third);
            expectedTrace.add(expectedStep);
        }
        expectedTrace.add(expectedFinal);
        assertEquals(expectedTrace, three.getTrace());

        // 生成的算式按优先级组织，展示文本永远不需要括号。
        String expectedText = first + " " + three.getFirstOperation().symbol() + " " + second
                + " " + three.getSecondOperation().symbol() + " " + third;
        assertEquals(expectedText, three.getText());
        assertEquals(5, three.getTrace().size());
    }

    /**
     * 独立求值器：根据两个运算符的优先级决定先算哪一对数。
     *
     * @param equation 算式
     * @param steps    输出参数，依次写入中间结果与最终结果
     * @return 最终结果
     */
    private static int evaluateIndependently(ThreeOperandEquation equation, List<Integer> steps) {
        int first = equation.getFirst();
        int second = equation.getSecond();
        int third = equation.getThird();
        Operation firstOperation = equation.getFirstOperation();
        Operation secondOperation = equation.getSecondOperation();

        if (firstOperation.precedence() >= secondOperation.precedence()) {
            int step = firstOperation.apply(first, second);
            steps.add(step);
            int result = secondOperation.apply(step, third);
            steps.add(result);
            return result;
        }
        int step = secondOperation.apply(second, third);
        steps.add(step);
        int result = firstOperation.apply(first, step);
        steps.add(result);
        return result;
    }
}
