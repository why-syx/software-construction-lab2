package com.exercise.common;

/**
 * 全局业务阈值。
 *
 * <p>本实验最核心的"数值红线"——参与运算的数字、中间结果与最终结果都必须满足
 * {@code 0 < 值 < 100}——只在这里定义一次，模型层、生成层、交互层都引用同一份常量，
 * 从机制上避免出现"某处写 &lt;= 100、另一处写 &lt; 100"的不一致。</p>
 *
 * <p>该类是纯常量工具类，不允许实例化。</p>
 */
public final class Limits {

    /** 操作数的最小值（正整数）。 */
    public static final int MIN_OPERAND = 1;

    /** 操作数的最大值（严格小于 100）。 */
    public static final int MAX_OPERAND = 99;

    /** 数值上界（开区间上界，即 100 本身不合法）。 */
    public static final int UPPER_BOUND = 100;

    /** 排版时每行允许的最小列数。 */
    public static final int MIN_COLUMNS = 1;

    /** 排版时每行允许的最大列数。 */
    public static final int MAX_COLUMNS = 10;

    /** 控制台允许用户设置的最小题目数量。 */
    public static final int MIN_UI_COUNT = 1;

    /** 控制台允许用户设置的最大题目数量。 */
    public static final int MAX_UI_COUNT = 500;

    /** 生成器内部允许的最大批量（供自动化测试一次生成大量题目使用）。 */
    public static final int MAX_GENERATE_COUNT = 5000;

    private Limits() {
        // 工具类禁止实例化
    }

    /**
     * 判断一个整数是否满足本实验的"数值红线"：严格大于 0 且严格小于 100。
     *
     * @param value 待判定的整数
     * @return {@code true} 表示合法
     */
    public static boolean isInRange(int value) {
        return value > 0 && value < UPPER_BOUND;
    }
}
