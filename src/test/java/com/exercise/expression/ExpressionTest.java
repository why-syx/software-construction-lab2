package com.exercise.expression;

import com.exercise.operation.Operation;
import com.exercise.operation.Operations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 表达式层单元测试：求值、求值轨迹、中缀文本与括号规则。
 */
@DisplayName("表达式层测试")
class ExpressionTest {

    private static final Operation ADD = Operations.ADD;
    private static final Operation SUBTRACT = Operations.SUBTRACT;
    private static final Operation MULTIPLY = Operations.MULTIPLY;
    private static final Operation DIVIDE = Operations.DIVIDE;

    @Test
    @DisplayName("常量表达式")
    void constantExpressionWorks() {
        ConstantExpression constant = new ConstantExpression(42);
        assertEquals(42, constant.evaluate());
        assertEquals(List.of(42), constant.evaluationTrace());
        assertEquals("42", constant.toInfix());
        assertEquals(42, constant.value());
    }

    @Test
    @DisplayName("二元表达式求值")
    void binaryExpressionEvaluates() {
        Expression expression = new BinaryExpression(ADD, new ConstantExpression(2), new ConstantExpression(3));
        assertEquals(5, expression.evaluate());
        assertEquals(List.of(2, 3, 5), expression.evaluationTrace());
        assertEquals("2 + 3", expression.toInfix());
    }

    @Test
    @DisplayName("先乘除后加减：求值轨迹按后序记录")
    void traceFollowsPrecedence() {
        Expression expression = new BinaryExpression(ADD,
                new ConstantExpression(2),
                new BinaryExpression(MULTIPLY, new ConstantExpression(3), new ConstantExpression(4)));
        assertEquals(14, expression.evaluate());
        assertEquals(List.of(2, 3, 4, 12, 14), expression.evaluationTrace());
        assertEquals("2 + 3 * 4", expression.toInfix());
    }

    @Test
    @DisplayName("先乘除后加减：乘法在左侧")
    void traceFollowsPrecedenceWhenMultiplicationIsLeft() {
        Expression expression = new BinaryExpression(ADD,
                new BinaryExpression(MULTIPLY, new ConstantExpression(2), new ConstantExpression(3)),
                new ConstantExpression(4));
        assertEquals(10, expression.evaluate());
        assertEquals(List.of(2, 3, 6, 4, 10), expression.evaluationTrace());
        assertEquals("2 * 3 + 4", expression.toInfix());
    }

    @Test
    @DisplayName("中缀文本：左侧低优先级必须加括号")
    void leftLowPrecedenceNeedsParentheses() {
        Expression expression = new BinaryExpression(MULTIPLY,
                new BinaryExpression(ADD, new ConstantExpression(2), new ConstantExpression(3)),
                new ConstantExpression(4));
        assertEquals("(2 + 3) * 4", expression.toInfix());
        assertEquals(20, expression.evaluate());
    }

    @Test
    @DisplayName("中缀文本：右侧低优先级必须加括号")
    void rightLowPrecedenceNeedsParentheses() {
        Expression expression = new BinaryExpression(SUBTRACT,
                new ConstantExpression(10),
                new BinaryExpression(ADD, new ConstantExpression(2), new ConstantExpression(3)));
        assertEquals("10 - (2 + 3)", expression.toInfix());
    }

    @Test
    @DisplayName("中缀文本：同级右子节点只有结合律成立时才省略括号")
    void rightSamePrecedenceParentheses() {
        Expression subtract = new BinaryExpression(SUBTRACT,
                new ConstantExpression(10),
                new BinaryExpression(SUBTRACT, new ConstantExpression(3), new ConstantExpression(2)));
        assertEquals("10 - (3 - 2)", subtract.toInfix());

        Expression add = new BinaryExpression(ADD,
                new ConstantExpression(1),
                new BinaryExpression(ADD, new ConstantExpression(2), new ConstantExpression(3)));
        assertEquals("1 + 2 + 3", add.toInfix());
    }

    @Test
    @DisplayName("中缀文本：乘除混合的括号")
    void mixedMultiplicationDivisionParentheses() {
        Expression divideByProduct = new BinaryExpression(DIVIDE,
                new ConstantExpression(12),
                new BinaryExpression(MULTIPLY, new ConstantExpression(2), new ConstantExpression(3)));
        assertEquals("12 / (2 * 3)", divideByProduct.toInfix());

        Expression multiplyByQuotient = new BinaryExpression(MULTIPLY,
                new ConstantExpression(2),
                new BinaryExpression(DIVIDE, new ConstantExpression(6), new ConstantExpression(3)));
        assertEquals("2 * (6 / 3)", multiplyByQuotient.toInfix());
    }

    @Test
    @DisplayName("除法不能整除时求值与求轨迹都抛出异常")
    void divisionFailurePropagates() {
        Expression expression = new BinaryExpression(DIVIDE,
                new ConstantExpression(7), new ConstantExpression(2));
        assertThrows(ArithmeticException.class, expression::evaluate);
        assertThrows(ArithmeticException.class, expression::evaluationTrace);
    }

    @Test
    @DisplayName("子表达式与运算符不能为空")
    void nullArgumentsRejected() {
        assertThrows(NullPointerException.class,
                () -> new BinaryExpression(null, new ConstantExpression(1), new ConstantExpression(2)));
        assertThrows(NullPointerException.class,
                () -> new BinaryExpression(ADD, null, new ConstantExpression(2)));
        assertThrows(NullPointerException.class,
                () -> new BinaryExpression(ADD, new ConstantExpression(1), null));
    }

    @Test
    @DisplayName("访问器返回构造时传入的对象")
    void accessorsReturnComponents() {
        ConstantExpression left = new ConstantExpression(3);
        ConstantExpression right = new ConstantExpression(4);
        BinaryExpression expression = new BinaryExpression(MULTIPLY, left, right);
        assertEquals(MULTIPLY, expression.operation());
        assertEquals(left, expression.left());
        assertEquals(right, expression.right());
        assertEquals("3 * 4", expression.toString());
    }
}
