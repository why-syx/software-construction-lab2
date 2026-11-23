package com.exercise.operation;

/**
 * 减法运算。
 *
 * <p>减法不可交换、不可结合，因此题目去重时不能对其操作数排序。</p>
 */
public final class Subtraction extends AbstractBinaryOperation {

    /** 减法是低优先级运算。 */
    public Subtraction() {
        super("-", 1, false, false);
    }

    @Override
    protected int doApply(int left, int right) {
        return Math.subtractExact(left, right);
    }
}
