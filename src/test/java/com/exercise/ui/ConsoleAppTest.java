package com.exercise.ui;

import com.exercise.equation.Equation;
import com.exercise.equation.ThreeOperandEquation;
import com.exercise.export.ExerciseExporter;
import com.exercise.export.ExportConfig;
import com.exercise.format.ExerciseFormatter;
import com.exercise.generator.ExerciseGenerator;
import com.exercise.generator.RandomExerciseGenerator;
import com.exercise.operation.Operations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 控制台交互测试：正常流程、非法输入、EOF、导出失败容错，以及依赖注入验证。
 */
@DisplayName("控制台交互测试")
class ConsoleAppTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("正常流程：设置数量、列数、文件名并生成导出")
    void normalFlowGeneratesAndExports() throws IOException {
        Path target = tempDir.resolve("exercises.csv");
        String script = "1\n7\n2\n3\n3\n" + target + "\n4\n6\n0\n";
        String output = run(script, new RandomExerciseGenerator(new Random(2026)));

        assertTrue(output.contains("题目数量已设置为 7 道"));
        assertTrue(output.contains("每行列数已设置为 3 列"));
        assertTrue(output.contains("已生成 7 道不重复的练习题"));
        assertTrue(output.contains("已导出 7 道题"));
        assertTrue(output.contains("感谢使用，再见！"));
        assertTrue(Files.exists(target));

        byte[] bytes = Files.readAllBytes(target);
        assertEquals((byte) 0xEF, bytes[0]);
        assertEquals((byte) 0xBB, bytes[1]);
        assertEquals((byte) 0xBF, bytes[2]);

        // 7 道题、每行 3 列 => 3 行，最后一列不足；借此验证列数确实生效
        String content = Files.readString(target, StandardCharsets.UTF_8);
        if (content.startsWith("﻿")) {
            content = content.substring(1);
        }
        String[] lines = content.split("\\r\\n", -1);
        assertEquals(4, lines.length, "3 行内容加一个结尾空串");
        assertEquals(3, lines[0].split(",", -1).length);
        assertEquals(3, lines[1].split(",", -1).length);
        assertEquals(1, lines[2].split(",", -1).length);
    }

    @Test
    @DisplayName("非法输入不会导致崩溃，提示后重新询问")
    void invalidInputsAreHandled() {
        String script = "1\nabc\n-5\n0\n999999\n30\n0\n";
        String output = run(script, new RandomExerciseGenerator(new Random(1)));

        assertTrue(output.contains("[输入有误]"));
        assertTrue(output.contains("不是有效的整数"));
        assertTrue(output.contains("必须输入大于 0 的正整数"));
        assertTrue(output.contains("数值过大"));
        assertTrue(output.contains("题目数量已设置为 30 道"));
        assertTrue(output.contains("感谢使用，再见！"));
    }

    @Test
    @DisplayName("非法菜单编号被提示后重新询问")
    void invalidMenuChoiceIsHandled() {
        String output = run("9\nx\n0\n", new RandomExerciseGenerator(new Random(1)));
        assertTrue(output.contains("数值过大"));
        assertTrue(output.contains("不是有效的整数"));
        assertTrue(output.contains("感谢使用，再见！"));
    }

    @Test
    @DisplayName("输入流结束时优雅退出")
    void endOfInputExitsGracefully() {
        String output = run("", new RandomExerciseGenerator(new Random(1)));
        assertTrue(output.contains("输入已结束，程序退出。"));
    }

    @Test
    @DisplayName("非法文件名被拒绝后可使用默认文件名")
    void invalidFileNameFallsBackToDefault() {
        String invalidName = "bad" + (char) 0 + "name.csv";
        String output = run("3\n" + invalidName + "\n\n0\n", new RandomExerciseGenerator(new Random(1)));
        assertTrue(output.contains("[输入有误]"));
        assertTrue(output.contains("文件名或路径不合法"));
        assertTrue(output.contains("导出文件名已设置为 exercises.csv"));
    }

    @Test
    @DisplayName("导出到目录等不可写路径时给出失败提示而不崩溃")
    void exportFailureIsHandled() {
        // 目录名带 .csv 后缀，避免被文件名规范化逻辑补扩展名后变成可写文件
        Path directory = tempDir.resolve("a-directory.csv");
        assertTrue(directory.toFile().mkdirs());
        String script = "1\n5\n3\n" + directory + "\n6\n0\n";
        String output = run(script, new RandomExerciseGenerator(new Random(1)));

        assertTrue(output.contains("[失败] 导出失败"));
        assertTrue(output.contains("感谢使用，再见！"));
    }

    @Test
    @DisplayName("尚未生成题目时显示题目会给出提示")
    void showWithoutGeneration() {
        String output = run("5\n0\n", new RandomExerciseGenerator(new Random(1)));
        assertTrue(output.contains("当前还没有题目"));
    }

    @Test
    @DisplayName("答案开关生效")
    void toggleAnswer() {
        String output = run("7\n0\n", new RandomExerciseGenerator(new Random(1)));
        assertTrue(output.contains("已开启答案显示"));
        assertTrue(output.contains("包含答案 是"));
    }

    @Test
    @DisplayName("依赖倒转：交互层只通过接口调用生成器、排版器与导出器")
    void dependenciesAreInjectedThroughInterfaces() {
        Equation fixed = new ThreeOperandEquation(2, 3, 4, Operations.ADD, Operations.MULTIPLY);
        StubGenerator generator = new StubGenerator(List.of(fixed));
        StubFormatter formatter = new StubFormatter();
        RecordingExporter exporter = new RecordingExporter(tempDir.resolve("stub.csv"));

        String output = runWith("1\n7\n2\n3\n7\n4\n5\n6\n0\n", generator, exporter, formatter);

        assertEquals(1, generator.calls, "生成器应只被调用一次");
        assertEquals(7, generator.lastCount, "交互层应把用户设置的题量传给生成器");
        assertTrue(output.contains("FAKE_TABLE"));
        assertEquals(1, exporter.calls, "导出器应被调用一次");
        assertEquals(List.of(fixed), exporter.exported);
        assertEquals(3, exporter.config.columns());
        assertTrue(exporter.config.includeAnswer(), "切换答案后导出参数应为 true");
    }

    /**
     * 使用真实实现运行交互脚本。
     *
     * @param script 脚本输入
     * @param generator 生成器
     * @return 控制台输出
     */
    private String run(String script, ExerciseGenerator generator) {
        return runWith(script, generator, new com.exercise.export.CsvExporter(),
                new com.exercise.format.ConsoleTableFormatter());
    }

    /**
     * 使用任意实现运行交互脚本。
     *
     * @param script    脚本输入
     * @param generator 生成器
     * @param exporter  导出器
     * @param formatter 排版器
     * @return 控制台输出
     */
    private static String runWith(String script, ExerciseGenerator generator,
                                  ExerciseExporter exporter, ExerciseFormatter formatter) {
        ByteArrayInputStream input = new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(output, true, StandardCharsets.UTF_8);
        new ConsoleApp(generator, exporter, formatter, input, out).run();
        return output.toString(StandardCharsets.UTF_8);
    }

    /** 固定返回一组题目的桩生成器。 */
    private static final class StubGenerator implements ExerciseGenerator {
        private final List<Equation> equations;
        private int calls;
        private int lastCount;

        private StubGenerator(List<Equation> equations) {
            this.equations = new ArrayList<>(equations);
        }

        @Override
        public List<Equation> generate(int count) {
            calls++;
            lastCount = count;
            return List.copyOf(equations);
        }
    }

    /** 记录调用参数的桩导出器。 */
    private static final class RecordingExporter implements ExerciseExporter {
        private final Path result;
        private List<Equation> exported;
        private ExportConfig config;
        private int calls;

        private RecordingExporter(Path result) {
            this.result = result;
        }

        @Override
        public Path export(List<Equation> equations, Path target, ExportConfig config) throws IOException {
            calls++;
            this.exported = List.copyOf(equations);
            this.config = config;
            return result;
        }
    }

    /** 输出固定文本的桩排版器。 */
    private static final class StubFormatter implements ExerciseFormatter {
        @Override
        public String format(List<Equation> equations, int columns, boolean includeAnswer) {
            return "FAKE_TABLE\n";
        }
    }
}
