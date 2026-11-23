package com.exercise.ui;

import com.exercise.common.Limits;
import com.exercise.equation.Equation;
import com.exercise.export.CsvExporter;
import com.exercise.export.ExerciseExporter;
import com.exercise.export.ExportConfig;
import com.exercise.format.ConsoleTableFormatter;
import com.exercise.format.ExerciseFormatter;
import com.exercise.generator.ExerciseGenerationException;
import com.exercise.generator.ExerciseGenerator;
import com.exercise.generator.RandomExerciseGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

/**
 * 控制台交互层：菜单展示、参数设置、生成/导出调度与异常兜底。
 *
 * <p>本类只依赖 {@link ExerciseGenerator}、{@link ExerciseExporter}、
 * {@link ExerciseFormatter} 三个抽象接口，而不依赖任何具体实现，体现了依赖倒转原则：
 * {@link #main(String[])} 负责把具体实现注入进来，业务代码完全不知道
 * "题目是随机生成的"还是"导出的是 CSV"。</p>
 *
 * <p>所有用户输入都经过 {@link InputReader} 的统一校验与重试，因此本类收到的
 * 永远是合法值；文件 I/O 失败也会被就地捕获并给出可操作的中文提示，程序不会崩溃。</p>
 */
public final class ConsoleApp {

    /** 默认题目数量。 */
    private static final int DEFAULT_COUNT = 30;

    /** 默认每行列数。 */
    private static final int DEFAULT_COLUMNS = 5;

    /** 默认导出文件名。 */
    private static final String DEFAULT_FILE_NAME = "exercises.csv";

    private final ExerciseGenerator generator;
    private final ExerciseExporter exporter;
    private final ExerciseFormatter formatter;
    private final InputReader input;
    private final PrintStream out;

    private int count = DEFAULT_COUNT;
    private int columns = DEFAULT_COLUMNS;
    private String fileName = DEFAULT_FILE_NAME;
    private boolean includeAnswer;
    private List<Equation> current = List.of();

    /**
     * 构造控制台应用。
     *
     * @param generator 题目生成器，不能为空
     * @param exporter  题目导出器，不能为空
     * @param formatter 题目排版器，不能为空
     * @param in        输入流，不能为空
     * @param out       输出流，不能为空
     */
    public ConsoleApp(ExerciseGenerator generator,
                      ExerciseExporter exporter,
                      ExerciseFormatter formatter,
                      InputStream in,
                      PrintStream out) {
        this.generator = Objects.requireNonNull(generator, "生成器不能为空");
        this.exporter = Objects.requireNonNull(exporter, "导出器不能为空");
        this.formatter = Objects.requireNonNull(formatter, "排版器不能为空");
        Objects.requireNonNull(in, "输入流不能为空");
        this.out = Objects.requireNonNull(out, "输出流不能为空");
        this.input = new InputReader(in, out);
    }

    /**
     * 程序入口：装配具体实现并启动交互循环。
     *
     * @param args 命令行参数（本程序不使用）
     */
    public static void main(String[] args) {
        PrintStream utf8Out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        ConsoleApp app = new ConsoleApp(
                new RandomExerciseGenerator(),
                new CsvExporter(),
                new ConsoleTableFormatter(),
                System.in,
                utf8Out);
        app.run();
    }

    /**
     * 启动交互循环，直到用户选择退出或输入流结束。
     */
    public void run() {
        printBanner();
        try {
            boolean running = true;
            while (running) {
                printSettings();
                printMenu();
                int choice = input.readInt("请输入操作编号 [0-8]：", 0, 8);
                switch (choice) {
                    case 1 -> changeCount();
                    case 2 -> changeColumns();
                    case 3 -> changeFileName();
                    case 4 -> {
                        if (generateExercises()) {
                            showExercises();
                        }
                    }
                    case 5 -> showExercises();
                    case 6 -> exportExercises();
                    case 7 -> toggleAnswer();
                    case 8 -> generateAndExport();
                    case 0 -> running = false;
                    default -> out.println("[提示] 未知操作，请重新选择。");
                }
            }
            out.println();
            out.println("感谢使用，再见！");
        } catch (InputClosedException exception) {
            out.println();
            out.println("输入已结束，程序退出。");
        }
    }

    /**
     * 设置题目数量。
     */
    private void changeCount() {
        count = input.readInt(
                "请输入题目数量（" + Limits.MIN_UI_COUNT + "~" + Limits.MAX_UI_COUNT + "）：",
                Limits.MIN_UI_COUNT, Limits.MAX_UI_COUNT);
        out.println("[成功] 题目数量已设置为 " + count + " 道。");
    }

    /**
     * 设置每行列数。
     */
    private void changeColumns() {
        columns = input.readInt(
                "请输入每行列数（" + Limits.MIN_COLUMNS + "~" + Limits.MAX_COLUMNS + "）：",
                Limits.MIN_COLUMNS, Limits.MAX_COLUMNS);
        out.println("[成功] 每行列数已设置为 " + columns + " 列。");
    }

    /**
     * 设置导出文件名。
     */
    private void changeFileName() {
        fileName = input.readFileName("请输入导出文件名", DEFAULT_FILE_NAME);
        out.println("[成功] 导出文件名已设置为 " + fileName + "。");
    }

    /**
     * 切换是否在题目后显示答案。
     */
    private void toggleAnswer() {
        includeAnswer = !includeAnswer;
        out.println("[成功] 已" + (includeAnswer ? "开启" : "关闭") + "答案显示。");
    }

    /**
     * 按当前设置生成题目。
     *
     * @return 生成成功返回 {@code true}
     */
    private boolean generateExercises() {
        try {
            current = generator.generate(count);
            out.println("[成功] 已生成 " + current.size() + " 道不重复的练习题。");
            return true;
        } catch (ExerciseGenerationException | IllegalArgumentException exception) {
            out.println("[失败] 生成练习题失败：" + exception.getMessage());
            return false;
        }
    }

    /**
     * 在控制台按当前列数展示题目。
     */
    private void showExercises() {
        if (current.isEmpty()) {
            out.println("[提示] 当前还没有题目，请先执行“4. 生成练习题”。");
            return;
        }
        out.println();
        out.println("---------------- 题目列表（共 " + current.size() + " 道，每行 " + columns + " 列）----------------");
        out.print(formatter.format(current, columns, includeAnswer));
        out.println("------------------------------------------------------------");
    }

    /**
     * 把当前题目导出为 CSV；若尚未生成题目则先自动生成。
     */
    private void exportExercises() {
        if (current.isEmpty()) {
            out.println("[提示] 当前还没有题目，先按当前设置自动生成……");
            if (!generateExercises()) {
                return;
            }
        }
        Path target;
        try {
            target = Paths.get(fileName);
        } catch (InvalidPathException exception) {
            out.println("[失败] 文件名不合法：" + exception.getReason());
            return;
        }
        try {
            Path written = exporter.export(current, target, new ExportConfig(columns, includeAnswer));
            out.println("[成功] 已导出 " + current.size() + " 道题到：" + written);
            out.println("        文件使用 UTF-8（带 BOM）编码，可直接用 Excel 打开。");
        } catch (IOException exception) {
            out.println("[失败] 导出失败：" + exception.getMessage());
            out.println("        请检查文件名是否合法、目标目录是否可写，然后重试。");
        } catch (InvalidPathException | SecurityException exception) {
            out.println("[失败] 路径不可用：" + exception.getMessage());
            out.println("        请修改导出文件名后重试。");
        }
    }

    /**
     * 一键完成"生成 + 展示 + 导出"。
     */
    private void generateAndExport() {
        if (!generateExercises()) {
            return;
        }
        showExercises();
        exportExercises();
    }

    /**
     * 打印程序标题。
     */
    private void printBanner() {
        out.println("============================================================");
        out.println("                  小学四则运算练习题生成器");
        out.println("          —— 软件构造实验 2：三个数四则混合运算 ——");
        out.println("============================================================");
        out.println("说明：每道题由三个数、两个运算符组成，严格按先乘除后加减计算；");
        out.println("      参与运算的数字、中间结果与最终结果均为 1~99 的正整数，除法必须整除。");
    }

    /**
     * 打印当前参数。
     */
    private void printSettings() {
        out.println();
        out.println("当前设置：题目数量 " + count + " 道 | 每行列数 " + columns + " 列"
                + " | 包含答案 " + (includeAnswer ? "是" : "否")
                + " | 导出文件 " + fileName);
    }

    /**
     * 打印操作菜单。
     */
    private void printMenu() {
        out.println("------------------------------------------------------------");
        out.println("  1. 设置题目数量        2. 设置每行列数");
        out.println("  3. 设置导出文件名      4. 生成练习题");
        out.println("  5. 显示当前题目        6. 导出为 CSV 文件");
        out.println("  7. 切换是否包含答案    8. 一键生成并导出");
        out.println("  0. 退出");
        out.println("------------------------------------------------------------");
    }
}
