package com.exercise.expression;

import java.util.List;

/**
 * 叶子表达式：一个整数常量。
 */
public final class ConstantExpression implements Expression {

    private final int value;

    /**
     * 构造常量表达式。
     *
     * <p>本类只负责"表示一个数"，数值是否符合 (0, 100) 的题目约束由算式层统一校验，
     * 以保持单一职责。</p>
     *
     * @param value 常量值
     */
    public ConstantExpression(int value) {
        this.value = value;
    }

    /**
     * 返回常量值。
     *
     * @return 常量值
     */
    public int value() {
        return value;
    }

    @Override
    public int evaluate() {
        return value;
    }

    @Override
    public List<Integer> evaluationTrace() {
        return List.of(value);
    }

    @Override
    public String toInfix() {
        return Integer.toString(value);
    }

    @Override
    public String toString() {
        return toInfix();
    }
}
