package com.exercise.format;

import com.exercise.equation.Equation;
import com.exercise.equation.ThreeOperandEquation;
import com.exercise.operation.Operations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 控制台排版器测试：行数、列对齐、答案开关与参数校验。
 */
@DisplayName("控制台排版器测试")
class ConsoleTableFormatterTest {

    private final ConsoleTableFormatter formatter = new ConsoleTableFormatter();

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
    @DisplayName("按指定列数分行")
    void formatsIntoRows() {
        String text = formatter.format(sampleEquations(), 2, false);
        String[] lines = text.split("\n", -1);
        assertEquals(4, lines.length, "5 道题按每行 2 列应产生 3 行加一个结尾空串");
        assertEquals(3, text.lines().count());
        assertTrue(lines[0].contains("2 + 3 * 4 ="));
        assertTrue(lines[0].contains("2 * 3 + 4 ="));
        assertTrue(lines[1].contains("12 / 4 + 2 ="));
        assertTrue(lines[2].contains("49 + 49 + 1 ="));
    }

    @Test
    @DisplayName("每行列数生效：5 列时只有一行")
    void singleRowWhenColumnsMatchSize() {
        String text = formatter.format(sampleEquations(), 5, false);
        assertEquals(1, text.lines().count());
    }

    @Test
    @DisplayName("单列时每题一行")
    void singleColumn() {
        String text = formatter.format(sampleEquations(), 1, false);
        assertEquals(5, text.lines().count());
    }

    @Test
    @DisplayName("同一列的起始位置对齐")
    void columnsAreAligned() {
        String text = formatter.format(sampleEquations(), 2, false);
        List<String> lines = text.lines().toList();
        int firstRowSecondCell = lines.get(0).indexOf("2 * 3 + 4 =");
        int secondRowSecondCell = lines.get(1).indexOf("2 + 12 / 4 =");
        assertEquals(firstRowSecondCell, secondRowSecondCell);
        assertTrue(firstRowSecondCell > 0);
    }

    @Test
    @DisplayName("行尾不保留多余空格")
    void noTrailingSpaces() {
        String text = formatter.format(sampleEquations(), 2, false);
        for (String line : text.lines().toList()) {
            assertFalse(line.endsWith(" "), "行尾不应有空格：" + line);
        }
    }

    @Test
    @DisplayName("答案开关生效")
    void includeAnswer() {
        String withAnswer = formatter.format(sampleEquations(), 2, true);
        assertTrue(withAnswer.contains("2 + 3 * 4 = 14"));
        String withoutAnswer = formatter.format(sampleEquations(), 2, false);
        assertFalse(withoutAnswer.contains("= 14"));
        assertTrue(withoutAnswer.contains("2 + 3 * 4 ="));
    }

    @Test
    @DisplayName("空列表返回空字符串")
    void emptyList() {
        assertEquals("", formatter.format(List.of(), 5, false));
    }

    @Test
    @DisplayName("空列表引用抛出 NullPointerException")
    void nullListRejected() {
        assertThrows(NullPointerException.class, () -> formatter.format(null, 5, false));
    }

    @Test
    @DisplayName("列数越界被拒绝")
    void invalidColumnsRejected() {
        assertThrows(IllegalArgumentException.class, () -> formatter.format(sampleEquations(), 0, false));
        assertThrows(IllegalArgumentException.class, () -> formatter.format(sampleEquations(), 11, false));
    }
}
