package com.exercise.format;

import com.exercise.common.Limits;
import com.exercise.equation.Equation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 控制台等宽排版器：把题目按用户指定的列数整齐排列。
 *
 * <p>实现思路：先计算全部单元格文本的最大长度，据此确定统一列宽；再逐行拼接并
 * 去掉行尾多余空格。由于算式文本只包含数字、运算符和空格，字符宽度一致，
 * 因此控制台中天然对齐。</p>
 */
public final class ConsoleTableFormatter implements ExerciseFormatter {

    /** 单元格之间的最小间隔。 */
    private static final int CELL_GAP = 3;

    @Override
    public String format(List<Equation> equations, int columns, boolean includeAnswer) {
        Objects.requireNonNull(equations, "题目列表不能为空");
        if (columns < Limits.MIN_COLUMNS || columns > Limits.MAX_COLUMNS) {
            throw new IllegalArgumentException(
                    "每行列数必须在 " + Limits.MIN_COLUMNS + "~" + Limits.MAX_COLUMNS + " 之间，当前为 " + columns);
        }
        if (equations.isEmpty()) {
            return "";
        }

        List<String> cells = new ArrayList<>(equations.size());
        int maxLength = 0;
        for (Equation equation : equations) {
            String cell = buildCell(equation, includeAnswer);
            cells.add(cell);
            maxLength = Math.max(maxLength, cell.length());
        }
        int width = maxLength + CELL_GAP;

        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < cells.size(); index++) {
            String cell = cells.get(index);
            builder.append(cell);
            boolean endOfRow = (index + 1) % columns == 0 || index == cells.size() - 1;
            if (endOfRow) {
                while (!builder.isEmpty() && builder.charAt(builder.length() - 1) == ' ') {
                    builder.deleteCharAt(builder.length() - 1);
                }
                builder.append('\n');
            } else {
                builder.append(" ".repeat(Math.max(1, width - cell.length())));
            }
        }
        return builder.toString();
    }

    /**
     * 生成单个单元格文本。
     *
     * @param equation      算式
     * @param includeAnswer 是否附答案
     * @return 单元格文本
     */
    private String buildCell(Equation equation, boolean includeAnswer) {
        if (includeAnswer) {
            return equation.getText() + " = " + equation.getAnswer();
        }
        return equation.getText() + " =";
    }
}
