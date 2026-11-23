package com.exercise.expression;

import java.util.List;

/**
 * 算术表达式的抽象（组合模式中的 Component）。
 *
 * <p>表达式既可以是单个数字（{@link ConstantExpression}），也可以是由运算符把两个
 * 子表达式组合起来的结果（{@link BinaryExpression}）。由于二者实现同一接口，
 * 上层代码可以用统一的方式求值、取中缀文本和获取求值轨迹，无需关心它究竟是
 * 叶子节点还是内部节点。</p>
 */
public interface Expression {

    /**
     * 计算表达式的值。
     *
     * @return 运算结果
     * @throws ArithmeticException 存在无定义的运算（除数为 0 或不能整除）时抛出
     */
    int evaluate();

    /**
     * 按后序（先子后父）返回求值过程中出现的全部数值：所有叶子操作数、每个内部节点的
     * 中间结果，以及最后的最终结果。
     *
     * <p>该轨迹是边界校验的唯一数据来源：只要轨迹中的每个值都落在 (0, 100) 内，
     * 就同时保证了"参与运算的数字""中间结果""最终结果"三者全部合法。</p>
     *
     * @return 不可变的求值轨迹
     * @throws ArithmeticException 存在无定义的运算时抛出
     */
    List<Integer> evaluationTrace();

    /**
     * 返回带必要括号的中缀表达式文本，例如 {@code "2 + 3 * 4"}。
     *
     * @return 中缀表达式
     */
    String toInfix();
}
