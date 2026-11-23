package com.exercise.operation;

import java.util.Objects;

/**
 * 二元运算的抽象基类，使用"模板方法"统一所有子类对外的行为契约。
 *
 * <p>子类只实现真正的算术逻辑 {@link #doApply(int, int)}，而符号、优先级、
 * 交换律/结合律等元数据以及对外入口 {@link #apply(int, int)} 都由本类统一持有。
 * 由于 {@code apply} 被声明为 {@code final}，任何子类都无法改变父类承诺的语义，
 * 这正是里氏替换原则（LSP）要求的"子类必须能够替换父类而不破坏程序正确性"。</p>
 */
public abstract class AbstractBinaryOperation implements Operation {

    private final String symbol;
    private final int precedence;
    private final boolean commutative;
    private final boolean associative;

    /**
     * 构造一个二元运算。
     *
     * @param symbol      运算符符号，不能为空
     * @param precedence  优先级，必须为正数
     * @param commutative 是否满足交换律
     * @param associative 是否满足结合律
     */
    protected AbstractBinaryOperation(String symbol, int precedence, boolean commutative, boolean associative) {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("运算符符号不能为空");
        }
        if (precedence <= 0) {
            throw new IllegalArgumentException("运算符优先级必须为正数");
        }
        this.symbol = symbol;
        this.precedence = precedence;
        this.commutative = commutative;
        this.associative = associative;
    }

    /**
     * {@inheritDoc}
     *
     * <p>本方法被声明为 {@code final}：子类只能通过 {@link #doApply(int, int)}
     * 提供具体算法，不能绕过或修改统一入口，从而保证所有实现的行为契约一致。</p>
     */
    @Override
    public final int apply(int left, int right) {
        return doApply(left, right);
    }

    /**
     * 子类实现的具体算术逻辑。
     *
     * @param left  左操作数
     * @param right 右操作数
     * @return 运算结果
     * @throws ArithmeticException 运算无定义时抛出
     */
    protected abstract int doApply(int left, int right);

    @Override
    public final String symbol() {
        return symbol;
    }

    @Override
    public final int precedence() {
        return precedence;
    }

    @Override
    public final boolean isCommutative() {
        return commutative;
    }

    @Override
    public final boolean isAssociative() {
        return associative;
    }

    @Override
    public final String toString() {
        return symbol;
    }

    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AbstractBinaryOperation that)) {
            return false;
        }
        return symbol.equals(that.symbol);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(symbol);
    }
}
