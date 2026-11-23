package com.exercise.export;

import com.exercise.equation.Equation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;

/**
 * 把练习题导出为标准 CSV 文件。
 *
 * <p>针对"用 Excel 打开中文 CSV 出现乱码"这一常见坑，本实现做了三件事：</p>
 * <ol>
 *   <li>文件以 UTF-8 编码写入；</li>
 *   <li>在文件最前面写入 BOM（字节序列 {@code EF BB BF}）。Excel 依靠它识别
 *       UTF-8 编码，缺少 BOM 时会按本地代码页（中文 Windows 为 GBK）解析，导致乱码；</li>
 *   <li>使用 CRLF（{@code \r\n}）作为行分隔符，兼容 Excel 与 Windows 记事本。</li>
 * </ol>
 *
 * <p>单元格内容按 RFC 4180 规则转义：包含逗号、双引号或换行时整体加双引号，
 * 内部双引号写成两个双引号。</p>
 */
public final class CsvExporter implements ExerciseExporter {

    /** UTF-8 BOM，告知 Excel 本文件使用 UTF-8 编码。 */
    private static final String UTF8_BOM = "﻿";

    /** CSV 标准行分隔符。 */
    private static final String LINE_SEPARATOR = "\r\n";

    @Override
    public Path export(List<Equation> equations, Path target, ExportConfig config) throws IOException {
        Objects.requireNonNull(equations, "题目列表不能为空");
        Objects.requireNonNull(target, "导出路径不能为空");
        Objects.requireNonNull(config, "导出参数不能为空");
        if (equations.isEmpty()) {
            throw new IllegalArgumentException("没有可导出的题目");
        }

        Path absolute = target.toAbsolutePath().normalize();
        Path parent = absolute.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                Files.newOutputStream(absolute,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE),
                StandardCharsets.UTF_8))) {
            writer.write(UTF8_BOM);
            int columns = config.columns();
            for (int index = 0; index < equations.size(); index++) {
                if (index % columns != 0) {
                    writer.write(',');
                }
                writer.write(escape(buildCell(equations.get(index), config.includeAnswer())));
                boolean endOfRow = (index + 1) % columns == 0 || index == equations.size() - 1;
                if (endOfRow) {
                    writer.write(LINE_SEPARATOR);
                }
            }
        }
        return absolute;
    }

    /**
     * 生成单元格文本。
     *
     * @param equation      算式
     * @param includeAnswer 是否附答案
     * @return 单元格文本
     */
    private static String buildCell(Equation equation, boolean includeAnswer) {
        if (includeAnswer) {
            return equation.getText() + " = " + equation.getAnswer();
        }
        return equation.getText() + " =";
    }

    /**
     * 按 RFC 4180 规则转义单元格。
     *
     * @param value 原始文本
     * @return 转义后的文本
     */
    static String escape(String value) {
        if (value.indexOf(',') >= 0 || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }
}
