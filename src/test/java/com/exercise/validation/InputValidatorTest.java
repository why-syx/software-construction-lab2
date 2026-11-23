package com.exercise.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 输入校验测试：非数字、负数、0、超大数值、非法文件名与保留名称。
 */
@DisplayName("输入校验测试")
class InputValidatorTest {

    @Test
    @DisplayName("合法整数解析（含首尾空格）")
    void parsesValidIntegers() {
        assertEquals(30, InputValidator.parseBoundedInt("30", 1, 500));
        assertEquals(30, InputValidator.parseBoundedInt("  30  ", 1, 500));
        assertEquals(1, InputValidator.parseBoundedInt("1", 1, 500));
        assertEquals(500, InputValidator.parseBoundedInt("500", 1, 500));
    }

    @Test
    @DisplayName("非数字输入给出明确提示")
    void rejectsNonNumericInput() {
        InputValidationException exception = assertThrows(InputValidationException.class,
                () -> InputValidator.parseBoundedInt("abc", 1, 500));
        assertTrue(exception.getMessage().contains("不是有效的整数"));

        assertThrows(InputValidationException.class,
                () -> InputValidator.parseBoundedInt("3.5", 1, 500));
        assertThrows(InputValidationException.class,
                () -> InputValidator.parseBoundedInt("1e3", 1, 500));
    }

    @Test
    @DisplayName("空输入与 null 被拒绝")
    void rejectsEmptyInput() {
        assertTrue(assertThrows(InputValidationException.class,
                () -> InputValidator.parseBoundedInt("", 1, 500)).getMessage().contains("不能为空"));
        assertTrue(assertThrows(InputValidationException.class,
                () -> InputValidator.parseBoundedInt("   ", 1, 500)).getMessage().contains("不能为空"));
        assertTrue(assertThrows(InputValidationException.class,
                () -> InputValidator.parseBoundedInt(null, 1, 500)).getMessage().contains("不能为空"));
    }

    @Test
    @DisplayName("0 与负数被拒绝")
    void rejectsZeroAndNegative() {
        assertTrue(assertThrows(InputValidationException.class,
                () -> InputValidator.parseBoundedInt("0", 1, 500)).getMessage().contains("大于 0"));
        assertTrue(assertThrows(InputValidationException.class,
                () -> InputValidator.parseBoundedInt("-5", 1, 500)).getMessage().contains("大于 0"));
    }

    @Test
    @DisplayName("超大数值被拒绝")
    void rejectsTooLargeValues() {
        assertTrue(assertThrows(InputValidationException.class,
                () -> InputValidator.parseBoundedInt("501", 1, 500)).getMessage().contains("数值过大"));
        assertTrue(assertThrows(InputValidationException.class,
                () -> InputValidator.parseBoundedInt("99999999999999999999", 1, 500))
                .getMessage().contains("不是有效的整数"));
    }

    @Test
    @DisplayName("区间包含 0 时允许输入 0（用于菜单退出项）")
    void allowsZeroWhenRangeIncludesZero() {
        assertEquals(0, InputValidator.parseBoundedInt("0", 0, 8));
        assertEquals(8, InputValidator.parseBoundedInt("8", 0, 8));
        assertTrue(assertThrows(InputValidationException.class,
                () -> InputValidator.parseBoundedInt("-1", 0, 8)).getMessage().contains("不能小于 0"));
    }

    @Test
    @DisplayName("低于下限的正数被拒绝")
    void rejectsBelowMinimum() {
        assertTrue(assertThrows(InputValidationException.class,
                () -> InputValidator.parseBoundedInt("1", 2, 10)).getMessage().contains("不能小于 2"));
    }

    @Test
    @DisplayName("校验器自身的区间参数非法时抛出 IllegalArgumentException")
    void rejectsInvalidRange() {
        assertThrows(IllegalArgumentException.class,
                () -> InputValidator.parseBoundedInt("5", 10, 2));
    }

    @Test
    @DisplayName("缺少扩展名时自动补 .csv")
    void appendsCsvSuffix() {
        assertEquals("exercises.csv", InputValidator.normalizeCsvFileName("exercises"));
        assertEquals(Paths.get("sub", "dir", "file.csv").toString(),
                InputValidator.normalizeCsvFileName("sub/dir/file"));
        assertEquals(Paths.get("C:\\temp\\report.csv").toString(),
                InputValidator.normalizeCsvFileName("C:\\temp\\report"));
    }

    @Test
    @DisplayName("已有扩展名时保持不变")
    void keepsExistingSuffix() {
        assertEquals("a.csv", InputValidator.normalizeCsvFileName("a.csv"));
        assertEquals("data.CSV", InputValidator.normalizeCsvFileName(" data.CSV "));
    }

    @Test
    @DisplayName("非法路径字符被拒绝")
    void rejectsIllegalPathCharacters() {
        InputValidationException exception = assertThrows(InputValidationException.class,
                () -> InputValidator.normalizeCsvFileName("a?b.csv"));
        assertTrue(exception.getMessage().contains("不合法"));
    }

    @Test
    @DisplayName("系统保留名称被拒绝")
    void rejectsReservedNames() {
        assertTrue(assertThrows(InputValidationException.class,
                () -> InputValidator.normalizeCsvFileName("CON")).getMessage().contains("保留"));
        assertTrue(assertThrows(InputValidationException.class,
                () -> InputValidator.normalizeCsvFileName("con.csv")).getMessage().contains("保留"));
    }

    @Test
    @DisplayName("空文件名、目录路径、点结尾被拒绝")
    void rejectsInvalidFileNames() {
        assertThrows(InputValidationException.class, () -> InputValidator.normalizeCsvFileName(""));
        assertThrows(InputValidationException.class, () -> InputValidator.normalizeCsvFileName("   "));
        assertThrows(InputValidationException.class, () -> InputValidator.normalizeCsvFileName("D:\\"));
        assertThrows(InputValidationException.class, () -> InputValidator.normalizeCsvFileName(".."));
        assertTrue(assertThrows(InputValidationException.class,
                () -> InputValidator.normalizeCsvFileName("abc.")).getMessage().contains("不能以点或空格结尾"));
    }
}
