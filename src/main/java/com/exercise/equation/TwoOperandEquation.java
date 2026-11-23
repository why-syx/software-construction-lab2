package com.exercise.equation;

import com.exercise.expression.BinaryExpression;
import com.exercise.expression.ConstantExpression;
import com.exercise.expression.Expression;
import com.exercise.operation.Operation;

import java.util.List;
import java.util.Objects;

/**
 * 两个数的算式（兼容题型）。
 *
 * <p>除三数混合运算外，本实验同时保留两数算式，用于验证运算层与校验层的通用性。</p>
 */
public final class TwoOperandEquation extends Equation {

    private final int left;
    private final int right;
    private final Operation operation;

    /**
     * 构造两数算式，例如 {@code 12 + 7}。
     *
     * @param left      左操作数，必须为 1~99
     * @param right     右操作数，必须为 1~99
     * @param operation 运算符，不能为空
     * @throws IllegalArgumentException 操作数越界、运算符为空或运算结果不合法时抛出
     */
    public TwoOperandEquation(int left, int right, Operation operation) {
        super(buildExpression(left, right, operation));
        this.left = left;
        this.right = right;
        this.operation = operation;
    }

    /**
     * 根据操作数与运算符构造表达式。
     *
     * @param left      左操作数
     * @param right     右操作数
     * @param operation 运算符
     * @return 二元表达式
     */
    private static Expression buildExpression(int left, int right, Operation operation) {
        requireOperandInRange(left, "左操作数");
        requireOperandInRange(right, "右操作数");
        Objects.requireNonNull(operation, "运算符不能为空");
        return new BinaryExpression(operation, new ConstantExpression(left), new ConstantExpression(right));
    }

    /**
     * 返回左操作数。
     *
     * @return 左操作数
     */
    public int getLeft() {
        return left;
    }

    /**
     * 返回右操作数。
     *
     * @return 右操作数
     */
    public int getRight() {
        return right;
    }

    /**
     * 返回运算符。
     *
     * @return 运算符
     */
    public Operation getOperation() {
        return operation;
    }

    @Override
    public List<Operation> getOperations() {
        return List.of(operation);
    }
}
