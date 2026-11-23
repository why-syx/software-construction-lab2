package com.exercise.validation;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;

/**
 * 纯函数式的输入校验工具。
 *
 * <p>把"字符串 → 合法整数/合法文件名"的判定集中到一处，交互层只负责循环与提示。
 * 该校验器没有 I/O 副作用，便于单元测试穷举各种非法输入。</p>
 */
public final class InputValidator {

    /** Windows 保留设备名，不能用作文件名主干。 */
    private static final Set<String> RESERVED_NAMES = Set.of(
            "CON", "PRN", "AUX", "NUL",
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9");

    /** CSV 扩展名。 */
    private static final String CSV_SUFFIX = ".csv";

    private InputValidator() {
        // 工具类禁止实例化
    }

    /**
     * 把字符串解析为指定区间内的整数。
     *
     * @param raw 用户原始输入
     * @param min 允许的最小值（含）
     * @param max 允许的最大值（含）
     * @return 解析后的整数
     * @throws InputValidationException 输入为空、不是整数、非正数或超出区间时抛出
     */
    public static int parseBoundedInt(String raw, int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("最小值不能大于最大值");
        }
        String text = raw == null ? "" : raw.trim();
        if (text.isEmpty()) {
            throw new InputValidationException("输入不能为空，请输入一个整数。");
        }
        int value;
        try {
            value = Integer.parseInt(text);
        } catch (NumberFormatException exception) {
            throw new InputValidationException("“" + abbreviate(text) + "”不是有效的整数，请重新输入。");
        }
        if (value <= 0 && min > 0) {
            // 题目数量、列数等"必须为正"的场景，0 与负数统一给出更直观的提示
            throw new InputValidationException("必须输入大于 0 的正整数。");
        }
        if (value < min) {
            throw new InputValidationException("数值不能小于 " + min + "。");
        }
        if (value > max) {
            throw new InputValidationException("数值过大，最大允许 " + max + "。");
        }
        return value;
    }

    /**
     * 规范化用户输入的 CSV 文件名。
     *
     * <p>规则：去掉首尾空白；用 {@link Paths#get} 校验路径语法；拒绝保留设备名；
     * 缺少扩展名时自动补 {@code .csv}；允许用户填写相对路径或绝对路径。</p>
     *
     * @param raw 用户原始输入
     * @return 规范化后的路径字符串
     * @throws InputValidationException 文件名为空或路径非法时抛出
     */
    public static String normalizeCsvFileName(String raw) {
        String text = raw == null ? "" : raw.trim();
        if (text.isEmpty()) {
            throw new InputValidationException("文件名不能为空。");
        }
        Path path;
        try {
            path = Paths.get(text);
        } catch (InvalidPathException exception) {
            throw new InputValidationException("文件名或路径不合法：" + exception.getReason() + "。");
        }
        Path fileNamePath = path.getFileName();
        if (fileNamePath == null) {
            throw new InputValidationException("请输入一个文件路径，而不是目录。");
        }
        String fileName = fileNamePath.toString();
        if (fileName.isBlank() || fileName.equals(".") || fileName.equals("..")) {
            throw new InputValidationException("文件名不合法。");
        }
        if (fileName.endsWith(".") || fileName.endsWith(" ")) {
            throw new InputValidationException("文件名不能以点或空格结尾。");
        }
        String baseName = fileName;
        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = baseName.substring(0, dotIndex);
        }
        if (RESERVED_NAMES.contains(baseName.toUpperCase(Locale.ROOT))) {
            throw new InputValidationException("“" + baseName + "”是系统保留名称，请换一个文件名。");
        }
        if (!fileName.toLowerCase(Locale.ROOT).endsWith(CSV_SUFFIX)) {
            path = path.resolveSibling(fileName + CSV_SUFFIX);
        }
        return path.toString();
    }

    /**
     * 截断过长的输入，避免异常信息刷屏。
     *
     * @param text 原始文本
     * @return 最长 20 个字符的摘要
     */
    private static String abbreviate(String text) {
        return text.length() <= 20 ? text : text.substring(0, 20) + "…";
    }
}
