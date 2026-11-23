package com.exercise.operation;

/**
 * 二元运算符的抽象接口（依赖倒转原则中的"抽象"）。
 *
 * <p>高层模块（表达式、算式、生成器）只依赖本接口，而不依赖加、减、乘、除任何一个
 * 具体实现类，因此新增运算符时不需要修改任何调用方代码。</p>
 *
 * <p>实现类的共同契约（里氏替换原则）：</p>
 * <ul>
 *   <li>{@link #apply(int, int)} 返回整数运算结果；</li>
 *   <li>当运算在数学上无定义时（如除数为 0、除法不能整除、整数溢出），
 *       统一抛出 {@link ArithmeticException}，不允许静默截断或返回特殊值；</li>
 *   <li>{@link #symbol()} 返回的符号可唯一标识运算类型。</li>
 * </ul>
 */
public interface Operation {

    /**
     * 返回运算符的数学符号，例如 {@code "+"}、{@code "-"}、{@code "*"}、{@code "/"}。
     *
     * @return 运算符符号
     */
    String symbol();

    /**
     * 返回运算符的优先级：加、减为 1，乘、除为 2。数值越大优先级越高。
     *
     * @return 优先级
     */
    int precedence();

    /**
     * 判断该运算是否满足交换律（用于题目去重时的规范化）。
     *
     * @return 满足交换律返回 {@code true}
     */
    boolean isCommutative();

    /**
     * 判断该运算是否满足结合律（用于题目去重时的规范化）。
     *
     * @return 满足结合律返回 {@code true}
     */
    boolean isAssociative();

    /**
     * 对两个整数执行运算。
     *
     * @param left  左操作数
     * @param right 右操作数
     * @return 运算结果
     * @throws ArithmeticException 运算无定义（除数为 0、不能整除或整数溢出）时抛出
     */
    int apply(int left, int right);
}
