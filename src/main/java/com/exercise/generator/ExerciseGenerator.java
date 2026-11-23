package com.exercise.generator;

import com.exercise.equation.Equation;

import java.util.List;

/**
 * 练习题生成器的抽象接口（依赖倒转原则中的"抽象"）。
 *
 * <p>交互层 {@code ConsoleApp} 只持有本接口的引用，因此可以在不修改交互代码的前提下
 * 替换为随机生成器、固定题库生成器或测试用的桩实现。</p>
 */
public interface ExerciseGenerator {

    /**
     * 批量生成互不重复的练习题。
     *
     * <p>返回列表中的每一道题都满足：三个操作数、所有中间结果与最终结果均为
     * {@code 0 < 值 < 100} 的正整数，且除法严格整除。</p>
     *
     * @param count 题目数量
     * @return 去重后的题目列表，长度等于 {@code count}
     * @throws IllegalArgumentException 数量不在允许范围内时抛出
     * @throws ExerciseGenerationException 在尝试次数上限内无法生成足够的不重复题目时抛出
     */
    List<Equation> generate(int count);
}
