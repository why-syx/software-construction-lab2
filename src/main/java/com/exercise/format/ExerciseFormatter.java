package com.exercise.format;

import com.exercise.equation.Equation;

import java.util.List;

/**
 * 练习题排版器的抽象接口（依赖倒转原则中的"抽象"）。
 *
 * <p>交互层只依赖本接口完成输出排版，因此可以替换为控制台等宽排版、HTML 排版等
 * 不同实现，而不影响生成与导出逻辑。</p>
 */
public interface ExerciseFormatter {

    /**
     * 把题目列表排版成按列对齐的多行文本。
     *
     * @param equations     题目列表，不能为空引用
     * @param columns       每行列数
     * @param includeAnswer 是否在每道题后显示答案
     * @return 排版后的文本；题目列表为空时返回空字符串
     * @throws NullPointerException 题目列表为空引用时抛出
     * @throws IllegalArgumentException 列数越界时抛出
     */
    String format(List<Equation> equations, int columns, boolean includeAnswer);
}
