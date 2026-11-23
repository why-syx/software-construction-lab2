package com.exercise.generator;

/**
 * 题目生成失败时抛出的运行时异常。
 *
 * <p>典型场景：请求的数量超过了可选题目的总容量，或在尝试次数上限内始终无法
 * 抽到新的不重复题目。抛出明确异常而不是无限循环，是防止生成器"卡死"的最后一道防线。</p>
 */
public class ExerciseGenerationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造异常。
     *
     * @param message 面向用户的说明
     */
    public ExerciseGenerationException(String message) {
        super(message);
    }

    /**
     * 构造异常。
     *
     * @param message 面向用户的说明
     * @param cause   原始异常
     */
    public ExerciseGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
