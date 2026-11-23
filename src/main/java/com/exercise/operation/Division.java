package com.exercise.operation;

/**
 * 除法运算。
 *
 * <p>本实验要求除法必须"严格整除"：若不能整除，则直接抛出
 * {@link ArithmeticException}，绝不静默截断为整数商。这一约定由本类统一实现，
 * 调用方无需在每一处重复判断余数。</p>
 */
public final class Division extends AbstractBinaryOperation {

    /** 除法是高优先级、不可交换、不可结合的运算。 */
    public Division() {
        super("/", 2, false, false);
    }

    @Override
    protected int doApply(int left, int right) {
        if (right == 0) {
            throw new ArithmeticException("除数不能为 0");
        }
        if (left % right != 0) {
            throw new ArithmeticException("除法不能整除：" + left + " ÷ " + right + " 的余数为 " + (left % right));
        }
        return left / right;
    }
}
