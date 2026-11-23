package com.exercise.operation;

/**
 * 乘法运算。
 */
public final class Multiplication extends AbstractBinaryOperation {

    /** 乘法是可交换、可结合的高优先级运算。 */
    public Multiplication() {
        super("*", 2, true, true);
    }

    @Override
    protected int doApply(int left, int right) {
        return Math.multiplyExact(left, right);
    }
}
