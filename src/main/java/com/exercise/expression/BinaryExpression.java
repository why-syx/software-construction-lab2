package com.exercise.expression;

import com.exercise.operation.Operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 内部节点表达式：由"左子表达式 + 运算符 + 右子表达式"构成。
 *
 * <p>求值时先求左右子树，再套用运算符；求值轨迹按后序拼接，因此天然记录了
 * 每一个中间结果。中缀文本会依据运算符优先级自动补充必要括号，
 * 保证文本与语义严格一致。</p>
 */
public final class BinaryExpression implements Expression {

    private final Operation operation;
    private final Expression left;
    private final Expression right;

    /**
     * 构造二元表达式。
     *
     * @param operation 运算符，不能为空
     * @param left      左子表达式，不能为空
     * @param right     右子表达式，不能为空
     */
    public BinaryExpression(Operation operation, Expression left, Expression right) {
        this.operation = Objects.requireNonNull(operation, "运算符不能为空");
        this.left = Objects.requireNonNull(left, "左子表达式不能为空");
        this.right = Objects.requireNonNull(right, "右子表达式不能为空");
    }

    /**
     * 返回运算符。
     *
     * @return 运算符
     */
    public Operation operation() {
        return operation;
    }

    /**
     * 返回左子表达式。
     *
     * @return 左子表达式
     */
    public Expression left() {
        return left;
    }

    /**
     * 返回右子表达式。
     *
     * @return 右子表达式
     */
    public Expression right() {
        return right;
    }

    @Override
    public int evaluate() {
        int leftValue = left.evaluate();
        int rightValue = right.evaluate();
        return operation.apply(leftValue, rightValue);
    }

    @Override
    public List<Integer> evaluationTrace() {
        List<Integer> trace = new ArrayList<>();
        trace.addAll(left.evaluationTrace());
        trace.addAll(right.evaluationTrace());
        trace.add(evaluate());
        return List.copyOf(trace);
    }

    @Override
    public String toInfix() {
        StringBuilder builder = new StringBuilder();
        appendChild(builder, left, false);
        builder.append(' ').append(operation.symbol()).append(' ');
        appendChild(builder, right, true);
        return builder.toString();
    }

    /**
     * 拼接子表达式，必要时加括号。
     *
     * @param builder 目标缓冲区
     * @param child   子表达式
     * @param isRight 是否为右子节点
     */
    private void appendChild(StringBuilder builder, Expression child, boolean isRight) {
        if (needsParentheses(child, isRight)) {
            builder.append('(').append(child.toInfix()).append(')');
        } else {
            builder.append(child.toInfix());
        }
    }

    /**
     * 判断子表达式是否需要括号。
     *
     * <p>规则：子表达式优先级低于当前运算时必须加括号；高于时不需要；相等时，
     * 左子节点按左结合直接拼接，右子节点只有在"父运算与子运算都满足结合律"时
     * 才可以省略括号（例如 {@code 1 + (2 + 3)} 可写作 {@code 1 + 2 + 3}，
     * 但 {@code 10 - (3 - 2)} 必须保留括号）。</p>
     *
     * @param child   子表达式
     * @param isRight 是否为右子节点
     * @return 需要括号返回 {@code true}
     */
    private boolean needsParentheses(Expression child, boolean isRight) {
        if (!(child instanceof BinaryExpression childBinary)) {
            return false;
        }
        int parentPrecedence = operation.precedence();
        int childPrecedence = childBinary.operation.precedence();
        if (childPrecedence < parentPrecedence) {
            return true;
        }
        if (childPrecedence > parentPrecedence) {
            return false;
        }
        if (!isRight) {
            return false;
        }
        return !(operation.isAssociative() && childBinary.operation.isAssociative());
    }

    @Override
    public String toString() {
        return toInfix();
    }
}
