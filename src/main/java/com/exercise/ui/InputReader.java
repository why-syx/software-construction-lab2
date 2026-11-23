package com.exercise.ui;

import com.exercise.validation.InputValidationException;
import com.exercise.validation.InputValidator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * 控制台输入助手：把"提示 → 读取整行 → 校验 → 失败重试"的循环封装在一处。
 *
 * <p>所有输入都通过 {@link BufferedReader#readLine()} 读取整行，再交给
 * {@link InputValidator} 解析。这样做有两个好处：</p>
 * <ol>
 *   <li>{@code readLine()} 一定会消费掉整行，非法记号不会残留在输入流中，
 *       从根源上避免 {@code Scanner.nextInt()} 失败后反复读取同一记号导致的死循环；</li>
 *   <li>读到的字符串先 trim 再解析，用户输入前后的空格不会造成误判。</li>
 * </ol>
 *
 * <p>输入流结束（EOF）时抛出 {@link InputClosedException}，由交互层统一优雅退出。</p>
 */
public final class InputReader {

    private final BufferedReader reader;
    private final PrintStream out;

    /**
     * 构造输入助手。
     *
     * @param input 输入流，不能为空
     * @param out   用于打印提示的输出流，不能为空
     */
    public InputReader(InputStream input, PrintStream out) {
        this.reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        this.out = out;
    }

    /**
     * 读取一个满足区间约束的整数，非法输入会提示并重新询问。
     *
     * @param prompt 提示语
     * @param min    允许的最小值（含）
     * @param max    允许的最大值（含）
     * @return 合法整数
     * @throws InputClosedException 输入流结束时抛出
     */
    public int readInt(String prompt, int min, int max) {
        while (true) {
            out.print(prompt);
            out.flush();
            String line = nextLine();
            try {
                return InputValidator.parseBoundedInt(line, min, max);
            } catch (InputValidationException exception) {
                out.println("  [输入有误] " + exception.getMessage());
            }
        }
    }

    /**
     * 读取一个是否需要回答的布尔值。
     *
     * @param prompt       提示语
     * @param defaultValue 直接回车时采用的默认值
     * @return 用户选择
     * @throws InputClosedException 输入流结束时抛出
     */
    public boolean readYesNo(String prompt, boolean defaultValue) {
        while (true) {
            out.print(prompt + "（是/否，直接回车默认" + (defaultValue ? "是" : "否") + "）：");
            out.flush();
            String line = nextLine().trim().toLowerCase();
            if (line.isEmpty()) {
                return defaultValue;
            }
            switch (line) {
                case "是", "y", "yes", "1" -> {
                    return true;
                }
                case "否", "n", "no", "0" -> {
                    return false;
                }
                default -> out.println("  [输入有误] 请输入“是”或“否”。");
            }
        }
    }

    /**
     * 读取导出文件名，直接回车返回默认文件名。
     *
     * @param prompt      提示语
     * @param defaultName 默认文件名
     * @return 规范化后的文件名
     * @throws InputClosedException 输入流结束时抛出
     */
    public String readFileName(String prompt, String defaultName) {
        while (true) {
            out.print(prompt + "（直接回车使用默认值 " + defaultName + "）：");
            out.flush();
            String line = nextLine();
            if (line.isBlank()) {
                return defaultName;
            }
            try {
                return InputValidator.normalizeCsvFileName(line);
            } catch (InputValidationException exception) {
                out.println("  [输入有误] " + exception.getMessage());
            }
        }
    }

    /**
     * 读取一行文本。
     *
     * @return 读取到的行
     * @throws InputClosedException 输入流结束或读取失败时抛出
     */
    private String nextLine() {
        try {
            String line = reader.readLine();
            if (line == null) {
                throw new InputClosedException();
            }
            return line;
        } catch (IOException exception) {
            throw new InputClosedException();
        }
    }
}
