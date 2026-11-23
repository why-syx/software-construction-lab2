package com.exercise.export;

import com.exercise.equation.Equation;
import com.exercise.equation.ThreeOperandEquation;
import com.exercise.operation.Operations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CSV 导出器测试：BOM、UTF-8 编码、列数、换行符、转义与异常处理。
 */
@DisplayName("CSV 导出器测试")
class CsvExporterTest {

    @TempDir
    Path tempDir;

    private final CsvExporter exporter = new CsvExporter();

    private static List<Equation> sampleEquations() {
        List<Equation> equations = new ArrayList<>();
        equations.add(new ThreeOperandEquation(2, 3, 4, Operations.ADD, Operations.MULTIPLY));
        equations.add(new ThreeOperandEquation(2, 3, 4, Operations.MULTIPLY, Operations.ADD));
        equations.add(new ThreeOperandEquation(12, 4, 2, Operations.DIVIDE, Operations.ADD));
        equations.add(new ThreeOperandEquation(2, 12, 4, Operations.ADD, Operations.DIVIDE));
        equations.add(new ThreeOperandEquation(49, 49, 1, Operations.ADD, Operations.ADD));
        return equations;
    }

    @Test
    @DisplayName("文件以 UTF-8 BOM 开头")
    void writesUtf8Bom() throws IOException {
        Path target = tempDir.resolve("exercises.csv");
        exporter.export(sampleEquations(), target, new ExportConfig(2, false));
        byte[] bytes = Files.readAllBytes(target);
        assertTrue(bytes.length >= 3);
        assertArrayEquals(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF},
                new byte[]{bytes[0], bytes[1], bytes[2]});
    }

    @Test
    @DisplayName("按指定列数写入，行数与逗号数正确")
    void writesExpectedRowsAndColumns() throws IOException {
        Path target = tempDir.resolve("rows.csv");
        exporter.export(sampleEquations(), target, new ExportConfig(2, false));
        String content = readWithoutBom(target);
        String[] lines = content.split("\\r\\n", -1);
        assertEquals(4, lines.length, "5 道题按 2 列应写 3 行，末尾留一个空串");
        assertEquals(3, content.lines().count());
        assertEquals(1, countChar(lines[0], ','));
        assertEquals(1, countChar(lines[1], ','));
        assertEquals(0, countChar(lines[2], ','));
    }

    @Test
    @DisplayName("使用 CRLF 作为行分隔符")
    void usesCrlf() throws IOException {
        Path target = tempDir.resolve("crlf.csv");
        exporter.export(sampleEquations(), target, new ExportConfig(5, false));
        String content = readWithoutBom(target);
        assertTrue(content.contains("\r\n"));
        assertFalse(content.replace("\r\n", "").contains("\n"));
    }

    @Test
    @DisplayName("单元格内容为题目表达式并可附带答案")
    void cellContent() throws IOException {
        Path target = tempDir.resolve("content.csv");
        exporter.export(sampleEquations(), target, new ExportConfig(5, false));
        String content = readWithoutBom(target);
        assertTrue(content.contains("2 + 3 * 4 ="));
        assertTrue(content.contains("49 + 49 + 1 ="));
        assertFalse(content.contains("= 14"));

        Path withAnswer = tempDir.resolve("answer.csv");
        exporter.export(sampleEquations(), withAnswer, new ExportConfig(5, true));
        String answerContent = readWithoutBom(withAnswer);
        assertTrue(answerContent.contains("2 + 3 * 4 = 14"));
        assertTrue(answerContent.contains("49 + 49 + 1 = 99"));
    }

    @Test
    @DisplayName("多列时每题之间用英文逗号分隔")
    void multipleColumnsUseCommas() throws IOException {
        Path target = tempDir.resolve("comma.csv");
        exporter.export(sampleEquations().subList(0, 3), target, new ExportConfig(5, false));
        String content = readWithoutBom(target);
        assertEquals(1, content.lines().count());
        assertEquals(2, countChar(content, ','));
    }

    @Test
    @DisplayName("RFC 4180 转义")
    void escapesSpecialCharacters() {
        assertEquals("plain", CsvExporter.escape("plain"));
        assertEquals("\"a,b\"", CsvExporter.escape("a,b"));
        assertEquals("\"a\"\"b\"", CsvExporter.escape("a\"b"));
        assertEquals("\"a\nb\"", CsvExporter.escape("a\nb"));
        assertEquals("\"a\rb\"", CsvExporter.escape("a\rb"));
    }

    @Test
    @DisplayName("目标目录不存在时自动创建")
    void createsParentDirectories() throws IOException {
        Path target = tempDir.resolve("a").resolve("b").resolve("c.csv");
        Path written = exporter.export(sampleEquations(), target, new ExportConfig(2, false));
        assertTrue(Files.exists(written));
        assertEquals(target.toAbsolutePath().normalize(), written);
    }

    @Test
    @DisplayName("已存在的文件会被覆盖")
    void overwritesExistingFile() throws IOException {
        Path target = tempDir.resolve("overwrite.csv");
        Files.writeString(target, "old-content", StandardCharsets.UTF_8);
        exporter.export(sampleEquations(), target, new ExportConfig(2, false));
        String content = readWithoutBom(target);
        assertFalse(content.contains("old-content"));
        assertTrue(content.contains("2 + 3 * 4 ="));
    }

    @Test
    @DisplayName("空题目列表被拒绝")
    void emptyListRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> exporter.export(List.of(), tempDir.resolve("empty.csv"), new ExportConfig(2, false)));
    }

    @Test
    @DisplayName("空参数被拒绝")
    void nullArgumentsRejected() {
        Path target = tempDir.resolve("null.csv");
        assertThrows(NullPointerException.class,
                () -> exporter.export(null, target, new ExportConfig(2, false)));
        assertThrows(NullPointerException.class,
                () -> exporter.export(sampleEquations(), null, new ExportConfig(2, false)));
        assertThrows(NullPointerException.class,
                () -> exporter.export(sampleEquations(), target, null));
    }

    @Test
    @DisplayName("导出参数列数越界被拒绝")
    void invalidConfigRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ExportConfig(0, false));
        assertThrows(IllegalArgumentException.class, () -> new ExportConfig(11, true));
    }

    /**
     * 读取文件并去掉 UTF-8 BOM。
     *
     * @param path 文件路径
     * @return 去掉 BOM 后的文本
     * @throws IOException 读取失败
     */
    private static String readWithoutBom(Path path) throws IOException {
        String content = Files.readString(path, StandardCharsets.UTF_8);
        return content.startsWith("﻿") ? content.substring(1) : content;
    }

    /**
     * 统计字符出现次数。
     *
     * @param text  文本
     * @param target 目标字符
     * @return 出现次数
     */
    private static int countChar(String text, char target) {
        int count = 0;
        for (int index = 0; index < text.length(); index++) {
            if (text.charAt(index) == target) {
                count++;
            }
        }
        return count;
    }
}
