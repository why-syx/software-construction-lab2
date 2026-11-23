package com.exercise.ui;

import com.exercise.export.CsvExporter;
import com.exercise.export.ExerciseExporter;
import com.exercise.format.ConsoleTableFormatter;
import com.exercise.format.ExerciseFormatter;
import com.exercise.generator.ExerciseGenerator;
import com.exercise.generator.RandomExerciseGenerator;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * 组合根（Composition Root）：整个程序中唯一知道具体实现的地方。
 *
 * <p>把对象装配从 {@link ConsoleApp} 中剥离出来，使交互层可以做到<b>零具体实现依赖</b>：
 * ConsoleApp 的字段与构造器只出现 ExerciseGenerator、ExerciseExporter、
 * ExerciseFormatter 三个接口。依赖倒转原则的"高层模块不依赖低层模块，二者都依赖抽象"
 * 在这里得到严格落实；需要替换生成算法或导出格式时，只改本类一行装配代码即可。</p>
 */
public final class Main {

    private Main() {
        // 入口类禁止实例化
    }

    /**
     * 程序入口：创建具体实现并注入控制台应用。
     *
     * @param args 命令行参数（本程序不使用）
     */
    public static void main(String[] args) {
        PrintStream utf8Out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        ExerciseGenerator generator = new RandomExerciseGenerator();
        ExerciseExporter exporter = new CsvExporter();
        ExerciseFormatter formatter = new ConsoleTableFormatter();
        new ConsoleApp(generator, exporter, formatter, System.in, utf8Out).run();
    }
}
