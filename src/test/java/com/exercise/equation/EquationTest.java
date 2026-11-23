package com.exercise.equation;

import com.exercise.common.Limits;
import com.exercise.operation.Operation;
import com.exercise.operation.Operations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 算式层单元测试：优先级、中间结果范围、整除校验、相等性与去重键。
 */
@DisplayName("算式层测试")
class EquationTest {

    private static final Operation ADD = Operations.ADD;
    private static final Operation SUBTRACT = Operations.SUBTRACT;
    private static final Operation MULTIPLY = Operations.MULTIPLY;
    private static final Operation DIVIDE = Operations.DIVIDE;

    @Test
    @DisplayName("三数混合：先乘除后加减（乘法在右侧）")
    void multiplicationOnRightIsEvaluatedFirst() {
        ThreeOperandEquation equation = new ThreeOperandEquation(2, 3, 4, ADD, MULTIPLY);
        assertEquals("2 + 3 * 4", equation.getText());
        assertEquals(14, equation.getAnswer());
        assertEquals(List.of(2, 3, 4, 12, 14), equation.getTrace());
        assertEquals(List.of(ADD, MULTIPLY), equation.getOperations());
    }

    @Test
    @DisplayName("三数混合：先乘除后加减（乘法在左侧）")
    void multiplicationOnLeftIsEvaluatedFirst() {
        ThreeOperandEquation equation = new ThreeOperandEquation(2, 3, 4, MULTIPLY, ADD);
        assertEquals("2 * 3 + 4", equation.getText());
        assertEquals(10, equation.getAnswer());
        assertEquals(List.of(2, 3, 6, 4, 10), equation.getTrace());
    }

    @Test
    @DisplayName("三数混合：除法在左侧且整除")
    void divisionOnLeftIsExact() {
        ThreeOperandEquation equation = new ThreeOperandEquation(12, 4, 2, DIVIDE, ADD);
        assertEquals("12 / 4 + 2", equation.getText());
        assertEquals(5, equation.getAnswer());
        assertEquals(List.of(12, 4, 3, 2, 5), equation.getTrace());
    }

    @Test
    @DisplayName("三数混合：除法在右侧且整除")
    void divisionOnRightIsExact() {
        ThreeOperandEquation equation = new ThreeOperandEquation(2, 12, 4, ADD, DIVIDE);
        assertEquals("2 + 12 / 4", equation.getText());
        assertEquals(5, equation.getAnswer());
        assertEquals(List.of(2, 12, 4, 3, 5), equation.getTrace());
    }

    @Test
    @DisplayName("三数混合：乘法与除法混合")
    void multiplicationAndDivisionMix() {
        assertEquals(6, new ThreeOperandEquation(9, 3, 2, DIVIDE, MULTIPLY).getAnswer());
        assertEquals(6, new ThreeOperandEquation(9, 2, 3, MULTIPLY, DIVIDE).getAnswer());
        assertEquals(8, new ThreeOperandEquation(10, 6, 3, SUBTRACT, DIVIDE).getAnswer());
    }

    @Test
    @DisplayName("中间结果为 0 时构造函数抛出异常")
    void zeroIntermediateResultRejected() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new ThreeOperandEquation(1, 1, 1, SUBTRACT, ADD));
        assertTrue(exception.getMessage().contains("超出 (0, 100)"));
    }

    @Test
    @DisplayName("中间结果为负数时构造函数抛出异常")
    void negativeIntermediateResultRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new ThreeOperandEquation(1, 2, 1, SUBTRACT, ADD));
    }

    @Test
    @DisplayName("最终结果为 0 时构造函数抛出异常")
    void zeroFinalResultRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new ThreeOperandEquation(9, 3, 3, DIVIDE, SUBTRACT));
    }

    @Test
    @DisplayName("最终结果为负数时构造函数抛出异常")
    void negativeFinalResultRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new ThreeOperandEquation(9, 3, 4, DIVIDE, SUBTRACT));
    }

    @Test
    @DisplayName("中间结果达到 100 时构造函数抛出异常")
    void upperBoundIntermediateRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new ThreeOperandEquation(50, 49, 1, ADD, ADD));
    }

    @Test
    @DisplayName("最终结果达到 100 时构造函数抛出异常")
    void upperBoundFinalRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new ThreeOperandEquation(99, 1, 1, ADD, ADD));
    }

    @Test
    @DisplayName("边界内的最大结果 99 合法")
    void upperBoundMinusOneAccepted() {
        ThreeOperandEquation equation = new ThreeOperandEquation(49, 49, 1, ADD, ADD);
        assertEquals(99, equation.getAnswer());
    }

    @Test
    @DisplayName("除法不能整除时构造函数抛出异常")
    void nonExactDivisionRejected() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new ThreeOperandEquation(2, 3, 4, SUBTRACT, DIVIDE));
        assertTrue(exception.getMessage().contains("不能整除"));
    }

    @Test
    @DisplayName("操作数越界（0、100、负数）时构造函数抛出异常")
    void operandsOutOfRangeRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new ThreeOperandEquation(0, 3, 4, ADD, ADD));
        assertThrows(IllegalArgumentException.class,
                () -> new ThreeOperandEquation(1, 100, 4, ADD, ADD));
        assertThrows(IllegalArgumentException.class,
                () -> new ThreeOperandEquation(1, 2, -3, ADD, ADD));
    }

    @Test
    @DisplayName("运算符为空时抛出异常")
    void nullOperationsRejected() {
        assertThrows(NullPointerException.class,
                () -> new ThreeOperandEquation(1, 2, 3, null, ADD));
        assertThrows(NullPointerException.class,
                () -> new ThreeOperandEquation(1, 2, 3, ADD, null));
    }

    @Test
    @DisplayName("两数算式：四则运算与边界")
    void twoOperandEquationWorks() {
        TwoOperandEquation addition = new TwoOperandEquation(12, 7, ADD);
        assertEquals("12 + 7", addition.getText());
        assertEquals(19, addition.getAnswer());
        assertEquals(List.of(ADD), addition.getOperations());
        assertEquals(12, addition.getLeft());
        assertEquals(7, addition.getRight());
        assertEquals(ADD, addition.getOperation());

        assertEquals(5, new TwoOperandEquation(12, 7, SUBTRACT).getAnswer());
        assertEquals(84, new TwoOperandEquation(12, 7, MULTIPLY).getAnswer());
        assertEquals(4, new TwoOperandEquation(12, 3, DIVIDE).getAnswer());
    }

    @Test
    @DisplayName("两数算式：非法结果与非法操作数")
    void twoOperandValidation() {
        assertThrows(IllegalArgumentException.class, () -> new TwoOperandEquation(1, 1, SUBTRACT));
        assertThrows(IllegalArgumentException.class, () -> new TwoOperandEquation(99, 1, ADD));
        assertThrows(IllegalArgumentException.class, () -> new TwoOperandEquation(0, 1, ADD));
        assertThrows(IllegalArgumentException.class, () -> new TwoOperandEquation(1, 0, DIVIDE));
        assertThrows(IllegalArgumentException.class, () -> new TwoOperandEquation(12, 5, DIVIDE));
    }

    @Test
    @DisplayName("相等性：交换律题目视为同一道")
    void commutativeEquationsAreEqual() {
        ThreeOperandEquation first = new ThreeOperandEquation(1, 2, 3, ADD, ADD);
        ThreeOperandEquation second = new ThreeOperandEquation(3, 2, 1, ADD, ADD);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertEquals(first.getCanonicalKey(), second.getCanonicalKey());

        ThreeOperandEquation productRight = new ThreeOperandEquation(2, 3, 4, ADD, MULTIPLY);
        ThreeOperandEquation productLeft = new ThreeOperandEquation(4, 3, 2, MULTIPLY, ADD);
        assertEquals(productRight, productLeft);
    }

    @Test
    @DisplayName("相等性：不同题目不相等")
    void differentEquationsAreNotEqual() {
        ThreeOperandEquation first = new ThreeOperandEquation(2, 3, 4, ADD, MULTIPLY);
        ThreeOperandEquation second = new ThreeOperandEquation(2, 3, 4, MULTIPLY, ADD);
        assertNotEquals(first, second);
        assertNotEquals(first, null);
        assertNotEquals(first, "2 + 3 * 4");

        TwoOperandEquation two = new TwoOperandEquation(2, 3, ADD);
        assertNotEquals(first, two);
    }

    @Test
    @DisplayName("相等性：自反性与哈希稳定性")
    void equalityIsReflexive() {
        ThreeOperandEquation equation = new ThreeOperandEquation(5, 6, 7, ADD, ADD);
        assertEquals(equation, equation);
        assertEquals(equation.hashCode(), equation.hashCode());
        assertFalse(equation.equals(null));
    }

    @Test
    @DisplayName("求值轨迹不可修改")
    void traceIsUnmodifiable() {
        ThreeOperandEquation equation = new ThreeOperandEquation(2, 3, 4, ADD, MULTIPLY);
        List<Integer> trace = equation.getTrace();
        assertThrows(UnsupportedOperationException.class, () -> trace.add(1));
    }

    @Test
    @DisplayName("toString 输出完整等式")
    void toStringContainsAnswer() {
        ThreeOperandEquation equation = new ThreeOperandEquation(2, 3, 4, ADD, MULTIPLY);
        assertEquals("2 + 3 * 4 = 14", equation.toString());
        assertNotNull(equation.getExpression());
    }

    @Test
    @DisplayName("数值红线：边界值判定")
    void numericLimitBoundaries() {
        assertFalse(Limits.isInRange(0));
        assertTrue(Limits.isInRange(1));
        assertTrue(Limits.isInRange(99));
        assertFalse(Limits.isInRange(100));
        assertFalse(Limits.isInRange(-1));
    }
}
