package com.exercise.ui;

/**
 * 输入流结束（EOF）时抛出的内部异常。
 *
 * <p>交互层捕获后打印告别语并正常退出，而不是让程序因 {@code NoSuchElementException}
 * 或空指针而崩溃。</p>
 */
public class InputClosedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造异常。
     */
    public InputClosedException() {
        super("输入流已结束");
    }
}
