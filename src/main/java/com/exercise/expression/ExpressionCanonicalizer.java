package com.exercise.expression;

import com.exercise.operation.Operation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 表达式规范化器：为一棵表达式生成与书写顺序无关的规范化键，供题目去重使用。
 *
 * <p>规范化规则：</p>
 * <ol>
 *   <li>常量保留原值；</li>
 *   <li>加法与乘法满足交换律和结合律，因此先把连续的同类运算"摊平"成若干个
 *       子键，再按字典序排序，最后用运算符连接，例如 {@code 1 + 2 + 3} 与
 *       {@code 3 + 1 + 2} 得到相同的键；</li>
 *   <li>减法与除法不满足交换律/结合律，保持原有左右顺序并加括号，避免误判相等。</li>
 * </ol>
 *
 * <p>等式性与哈希完全由该规范化键决定，从机制上保证 {@code equals} 与
 * {@code hashCode} 的一致性。</p>
 */
public final class ExpressionCanonicalizer {

    private ExpressionCanonicalizer() {
        // 工具类禁止实例化
    }

    /**
     * 生成表达式的规范化键。
     *
     * @param expression 表达式，不能为空
     * @return 规范化键
     */
    public static String canonicalize(Expression expression) {
        if (expression == null) {
            throw new IllegalArgumentException("表达式不能为空");
        }
        if (expression instanceof ConstantExpression constant) {
            return Integer.toString(constant.value());
        }
        BinaryExpression binary = (BinaryExpression) expression;
        if (binary.operation().isCommutative() && binary.operation().isAssociative()) {
            List<String> parts = new ArrayList<>();
            flattenCommutativeChain(binary, binary.operation(), parts);
            Collections.sort(parts);
            return "(" + String.join(binary.operation().symbol(), parts) + ")";
        }
        return "(" + canonicalize(binary.left()) + binary.operation().symbol() + canonicalize(binary.right()) + ")";
    }

    /**
     * 把连续的同类可交换运算摊平。只有子节点使用的是与父节点完全相同的运算符时
     * 才继续摊平，否则把子节点整体作为一个规范化子键——这一步至关重要，
     * 否则 {@code 2 * 3 + 4} 会把乘号链与加号链错误地合并。
     *
     * @param expression    当前子表达式
     * @param rootOperation 当前链的运算符
     * @param parts         收集到的规范化子键
     */
    private static void flattenCommutativeChain(Expression expression, Operation rootOperation, List<String> parts) {
        if (expression instanceof BinaryExpression binary && binary.operation().equals(rootOperation)) {
            flattenCommutativeChain(binary.left(), rootOperation, parts);
            flattenCommutativeChain(binary.right(), rootOperation, parts);
        } else {
            parts.add(canonicalize(expression));
        }
    }
}
