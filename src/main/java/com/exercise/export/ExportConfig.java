package com.exercise.export;

import com.exercise.common.Limits;

/**
 * CSV 导出参数。
 *
 * @param columns       每行写入的题目列数
 * @param includeAnswer 是否在单元格中附带答案
 */
public record ExportConfig(int columns, boolean includeAnswer) {

    /**
     * 校验参数合法性。
     *
     * @throws IllegalArgumentException 列数越界时抛出
     */
    public ExportConfig {
        if (columns < Limits.MIN_COLUMNS || columns > Limits.MAX_COLUMNS) {
            throw new IllegalArgumentException(
                    "每行列数必须在 " + Limits.MIN_COLUMNS + "~" + Limits.MAX_COLUMNS + " 之间，当前为 " + columns);
        }
    }
}
