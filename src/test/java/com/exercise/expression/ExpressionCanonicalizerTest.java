package com.exercise.expression;

import com.exercise.operation.Operation;
import com.exercise.operation.Operations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 规范化器测试：交换律、结合律的归一化，以及减法/除法不被误判。
 */
@DisplayName("表达式规范化测试")
class ExpressionCanonicalizerTest {

    private static final Operation ADD = Operations.ADD;
    private static final Operation SUBTRACT = Operations.SUBTRACT;
    private static final Operation MULTIPLY = Operations.MULTIPLY;
    private static final Operation DIVIDE = Operations.DIVIDE;

    private static Expression binary(Operation operation, int left, int right) {
        return new BinaryExpression(operation, new ConstantExpression(left), new ConstantExpression(right));
    }

    private static Expression binary(Operation operation, Expression left, Expression right) {
        return new BinaryExpression(operation, left, right);
    }

    @Test
    @DisplayName("常量规范化")
    void constantCanonicalization() {
        assertEquals("42", ExpressionCanonicalizer.canonicalize(new ConstantExpression(42)));
    }

    @Test
    @DisplayName("加法交换律：1 + 2 与 2 + 1 相同")
    void additionIsCommutative() {
        assertEquals(
                ExpressionCanonicalizer.canonicalize(binary(ADD, 1, 2)),
                ExpressionCanonicalizer.canonicalize(binary(ADD, 2, 1)));
    }

    @Test
    @DisplayName("加法结合律：1 + 2 + 3 的六种写法相同")
    void additionIsAssociative() {
        String base = ExpressionCanonicalizer.canonicalize(
                binary(ADD, binary(ADD, 1, 2), new ConstantExpression(3)));
        assertEquals(base, ExpressionCanonicalizer.canonicalize(
                binary(ADD, binary(ADD, 3, 2), new ConstantExpression(1))));
        assertEquals(base, ExpressionCanonicalizer.canonicalize(
                binary(ADD, new ConstantExpression(3),
                        binary(ADD, new ConstantExpression(1), new ConstantExpression(2)))));
    }

    @Test
    @DisplayName("乘法交换律与加法交换律的组合")
    void multiplicationAndAdditionCombine() {
        Expression first = binary(ADD, binary(MULTIPLY, 2, 3), new ConstantExpression(1));
        Expression second = binary(ADD, new ConstantExpression(1), binary(MULTIPLY, 3, 2));
        assertEquals(
                ExpressionCanonicalizer.canonicalize(first),
                ExpressionCanonicalizer.canonicalize(second));
    }

    @Test
    @DisplayName("不同运算符的链不会被错误合并")
    void differentOperatorsAreNotMerged() {
        Expression productThenAdd = binary(ADD, binary(MULTIPLY, 2, 3), new ConstantExpression(4));
        Expression addThenMultiply = binary(ADD, new ConstantExpression(2), binary(MULTIPLY, 3, 4));
        assertNotEquals(
                ExpressionCanonicalizer.canonicalize(productThenAdd),
                ExpressionCanonicalizer.canonicalize(addThenMultiply));
    }

    @Test
    @DisplayName("减法不可交换：10 - 2 与 2 - 10 不同")
    void subtractionIsNotCommutative() {
        assertNotEquals(
                ExpressionCanonicalizer.canonicalize(binary(SUBTRACT, 10, 2)),
                ExpressionCanonicalizer.canonicalize(binary(SUBTRACT, 2, 10)));
    }

    @Test
    @DisplayName("除法不可交换：12 / 3 与 3 / 12 不同")
    void divisionIsNotCommutative() {
        assertNotEquals(
                ExpressionCanonicalizer.canonicalize(binary(DIVIDE, 12, 3)),
                ExpressionCanonicalizer.canonicalize(binary(DIVIDE, 3, 12)));
    }

    @Test
    @DisplayName("减法与加法的结构不会被错误归一化")
    void subtractionStructurePreserved() {
        Expression subtractedSum = binary(SUBTRACT, new ConstantExpression(10),
                binary(ADD, new ConstantExpression(2), new ConstantExpression(3)));
        Expression leftAssociative = binary(SUBTRACT,
                binary(SUBTRACT, new ConstantExpression(10), new ConstantExpression(2)),
                new ConstantExpression(3));
        assertNotEquals(
                ExpressionCanonicalizer.canonicalize(subtractedSum),
                ExpressionCanonicalizer.canonicalize(leftAssociative));
    }

    @Test
    @DisplayName("空表达式抛出异常")
    void nullExpressionRejected() {
        assertThrows(IllegalArgumentException.class, () -> ExpressionCanonicalizer.canonicalize(null));
    }
}
