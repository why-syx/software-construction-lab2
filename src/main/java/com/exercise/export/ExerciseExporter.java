package com.exercise.export;

import com.exercise.equation.Equation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * 练习题导出器的抽象接口（依赖倒转原则中的"抽象"）。
 */
public interface ExerciseExporter {

    /**
     * 把题目列表导出到目标文件。
     *
     * @param equations 题目列表，不能为空
     * @param target    目标文件路径
     * @param config    导出参数
     * @return 实际写入的绝对路径
     * @throws IOException 文件无法创建或写入时抛出
     */
    Path export(List<Equation> equations, Path target, ExportConfig config) throws IOException;
}
