package com.exercise.operation;

/**
 * 加法运算。
 */
public final class Addition extends AbstractBinaryOperation {

    /** 加法是可交换、可结合的低优先级运算。 */
    public Addition() {
        super("+", 1, true, true);
    }

    @Override
    protected int doApply(int left, int right) {
        return Math.addExact(left, right);
    }
}
