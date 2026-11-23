package com.exercise.validation;

/**
 * 输入校验异常：携带一条可直接展示给用户的中文说明。
 *
 * <p>交互层捕获本异常后打印提示并重新询问，因此校验逻辑本身不需要关心
 * 输入流的重试细节。</p>
 */
public class InputValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造异常。
     *
     * @param message 面向用户的说明
     */
    public InputValidationException(String message) {
        super(message);
    }
}
