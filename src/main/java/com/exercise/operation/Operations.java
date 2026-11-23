package com.exercise.operation;

import java.util.List;

/**
 * 运算符注册表：集中暴露四种二元运算的唯一实例。
 *
 * <p>把"可用的运算集合"收敛到一处，生成器只需依赖 {@link #all()}，新增运算符时
 * 只需在此登记，不必修改生成器、算式等调用方（开闭原则）。</p>
 */
public final class Operations {

    /** 加法。 */
    public static final Operation ADD = new Addition();

    /** 减法。 */
    public static final Operation SUBTRACT = new Subtraction();

    /** 乘法。 */
    public static final Operation MULTIPLY = new Multiplication();

    /** 除法。 */
    public static final Operation DIVIDE = new Division();

    private static final List<Operation> ALL = List.of(ADD, SUBTRACT, MULTIPLY, DIVIDE);

    private Operations() {
        // 工具类禁止实例化
    }

    /**
     * 返回全部四种运算的不可变列表，顺序固定为 +、-、*、/。
     *
     * @return 不可变的运算符列表
     */
    public static List<Operation> all() {
        return ALL;
    }

    /**
     * 根据符号查找运算符。
     *
     * @param symbol 运算符符号，例如 {@code '+'}
     * @return 对应的运算符实例
     * @throws IllegalArgumentException 符号未知时抛出
     */
    public static Operation of(char symbol) {
        return switch (symbol) {
            case '+' -> ADD;
            case '-' -> SUBTRACT;
            case '*' -> MULTIPLY;
            case '/' -> DIVIDE;
            default -> throw new IllegalArgumentException("未知运算符：" + symbol);
        };
    }
}
