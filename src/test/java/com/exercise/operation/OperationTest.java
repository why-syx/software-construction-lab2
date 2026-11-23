package com.exercise.operation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 二元运算层单元测试：覆盖四种运算的正确性、边界、异常契约与元数据。
 */
@DisplayName("二元运算层测试")
class OperationTest {

    @Test
    @DisplayName("加法：普通值与边界值")
    void additionComputesCorrectly() {
        assertEquals(5, Operations.ADD.apply(2, 3));
        assertEquals(2, Operations.ADD.apply(1, 1));
        assertEquals(99, Operations.ADD.apply(98, 1));
        assertEquals(100, Operations.ADD.apply(99, 1));
    }

    @Test
    @DisplayName("加法：整数溢出抛出 ArithmeticException")
    void additionOverflowThrows() {
        assertThrows(ArithmeticException.class, () -> Operations.ADD.apply(Integer.MAX_VALUE, 1));
    }

    @Test
    @DisplayName("减法：普通值与负数结果")
    void subtractionComputesCorrectly() {
        assertEquals(2, Operations.SUBTRACT.apply(5, 3));
        assertEquals(98, Operations.SUBTRACT.apply(99, 1));
        assertEquals(0, Operations.SUBTRACT.apply(1, 1));
        assertEquals(-4, Operations.SUBTRACT.apply(2, 6));
    }

    @Test
    @DisplayName("乘法：普通值与边界值")
    void multiplicationComputesCorrectly() {
        assertEquals(99, Operations.MULTIPLY.apply(9, 11));
        assertEquals(99, Operations.MULTIPLY.apply(1, 99));
        assertEquals(1, Operations.MULTIPLY.apply(1, 1));
    }

    @Test
    @DisplayName("乘法：整数溢出抛出 ArithmeticException")
    void multiplicationOverflowThrows() {
        assertThrows(ArithmeticException.class, () -> Operations.MULTIPLY.apply(Integer.MAX_VALUE, 2));
    }

    @Test
    @DisplayName("除法：严格整除")
    void divisionComputesExactly() {
        assertEquals(4, Operations.DIVIDE.apply(12, 3));
        assertEquals(1, Operations.DIVIDE.apply(99, 99));
        assertEquals(99, Operations.DIVIDE.apply(99, 1));
    }

    @Test
    @DisplayName("除法：除数为 0 抛出 ArithmeticException")
    void divisionByZeroThrows() {
        ArithmeticException exception = assertThrows(ArithmeticException.class,
                () -> Operations.DIVIDE.apply(12, 0));
        assertTrue(exception.getMessage().contains("除数不能为 0"));
    }

    @Test
    @DisplayName("除法：不能整除时抛出 ArithmeticException，不允许静默截断")
    void nonExactDivisionThrows() {
        ArithmeticException exception = assertThrows(ArithmeticException.class,
                () -> Operations.DIVIDE.apply(7, 2));
        assertTrue(exception.getMessage().contains("不能整除"));
    }

    @Test
    @DisplayName("优先级：加减为 1，乘除为 2")
    void precedenceIsCorrect() {
        assertEquals(1, Operations.ADD.precedence());
        assertEquals(1, Operations.SUBTRACT.precedence());
        assertEquals(2, Operations.MULTIPLY.precedence());
        assertEquals(2, Operations.DIVIDE.precedence());
    }

    @Test
    @DisplayName("交换律与结合律元数据")
    void algebraicFlagsAreCorrect() {
        assertTrue(Operations.ADD.isCommutative());
        assertTrue(Operations.MULTIPLY.isCommutative());
        assertFalse(Operations.SUBTRACT.isCommutative());
        assertFalse(Operations.DIVIDE.isCommutative());

        assertTrue(Operations.ADD.isAssociative());
        assertTrue(Operations.MULTIPLY.isAssociative());
        assertFalse(Operations.SUBTRACT.isAssociative());
        assertFalse(Operations.DIVIDE.isAssociative());
    }

    @Test
    @DisplayName("符号与 toString")
    void symbolsAreCorrect() {
        assertEquals("+", Operations.ADD.symbol());
        assertEquals("-", Operations.SUBTRACT.symbol());
        assertEquals("*", Operations.MULTIPLY.symbol());
        assertEquals("/", Operations.DIVIDE.symbol());
        assertEquals("+", Operations.ADD.toString());
    }

    @Test
    @DisplayName("运算注册表：数量、顺序与不可变性")
    void registryContainsAllOperations() {
        List<Operation> all = Operations.all();
        assertEquals(4, all.size());
        assertSame(Operations.ADD, all.get(0));
        assertSame(Operations.SUBTRACT, all.get(1));
        assertSame(Operations.MULTIPLY, all.get(2));
        assertSame(Operations.DIVIDE, all.get(3));
        assertThrows(UnsupportedOperationException.class, () -> all.add(Operations.ADD));
    }

    @Test
    @DisplayName("按符号查找运算符")
    void lookupBySymbol() {
        assertSame(Operations.ADD, Operations.of('+'));
        assertSame(Operations.SUBTRACT, Operations.of('-'));
        assertSame(Operations.MULTIPLY, Operations.of('*'));
        assertSame(Operations.DIVIDE, Operations.of('/'));
        assertThrows(IllegalArgumentException.class, () -> Operations.of('%'));
    }

    @Test
    @DisplayName("相等性基于符号")
    void equalityIsBasedOnSymbol() {
        assertEquals(new Addition(), Operations.ADD);
        assertEquals(new Addition().hashCode(), Operations.ADD.hashCode());
        assertNotEquals(Operations.ADD, Operations.SUBTRACT);
    }

    @Test
    @DisplayName("里氏替换：只依赖 Operation 接口即可完成四则运算")
    void polymorphismWorksThroughInterface() {
        for (Operation operation : Operations.all()) {
            int result;
            switch (operation.symbol()) {
                case "+" -> result = operation.apply(20, 4);
                case "-" -> result = operation.apply(20, 4);
                case "*" -> result = operation.apply(20, 4);
                case "/" -> result = operation.apply(20, 4);
                default -> throw new IllegalStateException("未知运算符");
            }
            int expected = switch (operation.symbol()) {
                case "+" -> 24;
                case "-" -> 16;
                case "*" -> 80;
                case "/" -> 5;
                default -> throw new IllegalStateException("未知运算符");
            };
            assertEquals(expected, result);
        }
    }

    @Test
    @DisplayName("里氏替换：apply 是 final 方法，子类无法改变父类契约")
    void applyIsFinal() throws NoSuchMethodException {
        int modifiers = AbstractBinaryOperation.class
                .getMethod("apply", int.class, int.class)
                .getModifiers();
        assertTrue(Modifier.isFinal(modifiers));
    }

    @Test
    @DisplayName("抽象基类：构造参数校验")
    void abstractBaseValidatesConstructorArguments() {
        assertThrows(IllegalArgumentException.class, () -> new TestOperation(null, 1));
        assertThrows(IllegalArgumentException.class, () -> new TestOperation(" ", 1));
        assertThrows(IllegalArgumentException.class, () -> new TestOperation("+", 0));
    }

    /** 测试用最小子类，用于验证抽象基类的构造校验。 */
    private static final class TestOperation extends AbstractBinaryOperation {
        private TestOperation(String symbol, int precedence) {
            super(symbol, precedence, true, true);
        }

        @Override
        protected int doApply(int left, int right) {
            return left + right;
        }
    }
}
