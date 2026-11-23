# 软件构造实验 2：三个数四则混合运算练习题生成器

> 学号：542413320648　姓名：吴昊阳　专业班级：工业软件 24-06　课程：软件构造

本项目用一个可测试、可扩展的 Java 控制台程序，完成"三个数参与的加减乘除混合运算练习题"
的批量生成、去重、排版与 CSV 导出。题目严格遵循"先乘除、后加减"的数学优先级，并保证
**参与运算的数字、每一步中间结果和最终结果都满足 `0 < 值 < 100` 的正整数**，除法必须整除。

## 一、功能特性

- 三个数 + 两个运算符的混合运算，自动按优先级组织表达式树并求值；
- 构造式随机采样，100% 生成合法题目，不依赖"生成后拒绝"的低效重试；
- 基于规范化键自动去重（加法、乘法按交换律与结合律归一化，减法/除法保持顺序）；
- 用户可自定义题目数量（1~500）、每行列数（1~10）、导出文件名（默认 `exercises.csv`）
  以及是否显示答案；
- 控制台按指定列数整齐排版，同时导出 UTF-8 带 BOM 的标准 CSV，Excel 打开中文不乱码；
- 所有输入统一校验：非数字、负数、0、超大数值、非法路径、系统保留文件名均会友好提示并重试；
- JUnit 5 全量自动化测试，共 **111** 个用例，覆盖运算、表达式、算式、生成、排版、导出与交互。

## 二、环境与运行

| 项目 | 要求 |
| --- | --- |
| JDK | 17 及以上（开发环境为 JDK 21） |
| Maven | 3.8 及以上 |
| 测试框架 | JUnit 5（`junit-jupiter 5.10.2`） |
| 源码编码 | UTF-8 |

```bash
# 编译并运行全部单元测试
mvn clean test

# 启动控制台程序
mvn compile exec:java

# 或者直接运行编译产物
mvn -q compile
java -cp target/classes com.exercise.ui.Main
```

控制台主菜单：

```
1. 设置题目数量        2. 设置每行列数
3. 设置导出文件名      4. 生成练习题
5. 显示当前题目        6. 导出为 CSV 文件
7. 切换是否包含答案    8. 一键生成并导出
0. 退出
```

## 三、目录结构

```
2/
├── pom.xml                          Maven 构建配置（JDK 17、JUnit 5、UTF-8）
├── README.md
├── src/main/java/com/exercise/
│   ├── common/       Limits            全局数值红线与范围常量
│   ├── operation/    Operation, AbstractBinaryOperation, Addition, Subtraction,
│   │                 Multiplication, Division, Operations
│   ├── expression/   Expression, ConstantExpression, BinaryExpression,
│   │                 ExpressionCanonicalizer
│   ├── equation/     Equation, TwoOperandEquation, ThreeOperandEquation
│   ├── generator/    ExerciseGenerator, RandomExerciseGenerator,
│   │                 ExerciseGenerationException
│   ├── format/       ExerciseFormatter, ConsoleTableFormatter
│   ├── export/       ExerciseExporter, CsvExporter, ExportConfig
│   ├── validation/   InputValidator, InputValidationException
│   └── ui/           Main, ConsoleApp, InputReader, InputClosedException
├── src/test/java/com/exercise/      JUnit 5 测试（9 个测试类，111 个用例）
└── docs/
    ├── class_diagram.puml           完整 UML 类图源文件
    ├── uml_model.puml               模型层分图源文件
    ├── uml_service.puml             服务与交互层分图源文件
    ├── class_diagram.mmd            Mermaid 版本
    ├── uml_class_diagram.png        UML 类图（报告用，两幅分图拼接）
    ├── uml_class_diagram_full.png   完整类图（高清参考）
    ├── logs/                        控制台会话、JUnit 日志、样例 CSV
    └── screenshots/                 运行、CSV、JUnit 截图素材
```

## 四、设计要点

### 4.1 分层与依赖方向

```
入口    Main（组合根）──创建并注入──▶ ConsoleApp
交互层  ConsoleApp ──依赖──▶ ExerciseGenerator / ExerciseFormatter / ExerciseExporter（接口）
                     │
生成层  RandomExerciseGenerator ──▶ Equation（抽象）
                     │
模型层  Equation ──▶ Expression（抽象）──▶ Operation（抽象）──▶ 四个具体运算符
```

依赖只指向抽象：`ConsoleApp` 不知道题目是随机生成的，也不知道导出的是 CSV；具体实现由
独立的 `Main` 组合根统一装配。

### 4.2 单一职责原则（SRP）

- `Operation` 及其子类只负责算术，不关心题目范围；
- `Expression` 只负责组合与求值，`ExpressionCanonicalizer` 只负责生成去重键；
- `Equation` 只负责"一道题是否合法"以及对外展示；
- `RandomExerciseGenerator` 只负责采样，`ConsoleTableFormatter` 只负责排版，
  `CsvExporter` 只负责持久化，`InputValidator` 只负责把字符串变成合法值；
- `Limits` 是全部阈值（1~99、100 上界、列数、数量）的唯一来源。

### 4.3 里氏替换原则（LSP）

- `AbstractBinaryOperation.apply` 是 `final` 模板方法，子类只能实现 `doApply`，
  无法改变"运算无定义时抛 `ArithmeticException`"的父类契约；
- 所有 `Equation` 子类在构造阶段完成同一套校验，只要对象构造成功就一定合法，
  调用方可以放心地按父类使用；
- 测试中通过 `Operation` 接口统一驱动四种运算，并用反射断言 `apply` 为 `final`。

### 4.4 依赖倒转原则（DIP）

- `ConsoleApp` 的字段类型是 `ExerciseGenerator`、`ExerciseExporter`、`ExerciseFormatter`
  三个接口，而不是具体类；
- `ConsoleAppTest` 用桩实现（Stub）注入固定的题目、假的排版器和记录调用的导出器，
  在完全不启动真实 I/O 的情况下验证交互逻辑，这从测试层面证明了依赖确实被反转。

## 五、核心算法

### 5.1 构造式采样（避免死循环与低合法率）

若三个数各自独立取 1~99 再判断整题是否合法，三数题的合法率很低；请求数量接近题库容量时，
重复碰撞还会按"生日问题"急剧上升。本项目反向构造：

1. 先确定按优先级"先算"的那一对数，按运算符反推出使该步结果落在 (0, 100) 内的取值范围；
2. 再由该中间结果反推剩余一个数的合法范围；
3. 除法通过"除数 × 商"直接构造被除数，从源头上保证整除；
4. 为避免出现 `62 * 1 * 1` 这类无练习价值的题目，操作数统一从 2 开始取值；
5. 去重循环设置尝试次数上限，极端情况下抛出带说明的 `ExerciseGenerationException`，
   绝不会无限循环。

### 5.2 统一的数值红线

`Equation` 构造时一次性取出表达式树的求值轨迹（全部叶子、全部中间结果、最终结果），
逐项用 `Limits.isInRange` 校验；任一项越界或除法不能整除，立即抛出
`IllegalArgumentException`。因此"构造即合法"，生成器、排版器和导出器都无需重复判断。

### 5.3 去重键

`ExpressionCanonicalizer` 对表达式生成与书写顺序无关的键：

- 加法/乘法链被摊平、排序后拼接，因此 `1 + 2 + 3` 与 `3 + 1 + 2` 是同一道题；
- 减法/除法不可交换，保持左右顺序并加括号，避免误判；
- 不同运算符的链不会互相合并（例如 `2 * 3 + 4` 与 `2 + 3 * 4` 仍然是两道不同的题）。

`Equation` 的 `equals`/`hashCode` 都基于这个不可变键，`LinkedHashSet` 可正确去重。

## 六、CSV 导出

- 编码：UTF-8；文件头写入 BOM（`EF BB BF`），Excel 打开中文不乱码；
- 行分隔符：CRLF，兼容 Excel 与 Windows 记事本；
- 每行按用户指定列数用英文逗号分隔，最后一列不足时自然留空；
- 单元格按 RFC 4180 转义（逗号、双引号、换行）；
- 目标目录不存在时自动创建，写入失败由交互层捕获并提示重试。

## 七、测试与素材

```text
Tests run: 111, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

测试类与用例数：

| 测试类 | 用例数 | 覆盖内容 |
| --- | ---: | --- |
| `OperationTest` | 17 | 四则运算、溢出、除零、不能整除、优先级、LSP |
| `ExpressionTest` | 11 | 求值、求值轨迹、中缀文本与括号规则 |
| `ExpressionCanonicalizerTest` | 9 | 交换律/结合律归一化、减法除法不误判 |
| `EquationTest` | 23 | 三数优先级、中间/最终结果边界、整除、相等性 |
| `RandomExerciseGeneratorTest` | 9 | 数量、去重、独立求值交叉验证、16 种运算符组合 |
| `ConsoleTableFormatterTest` | 9 | 行数、列对齐、答案开关、参数校验 |
| `CsvExporterTest` | 11 | BOM、UTF-8、CRLF、列数、转义、目录创建 |
| `InputValidatorTest` | 13 | 非数字、0、负数、超大值、非法文件名、保留名 |
| `ConsoleAppTest` | 9 | 正常流程、非法输入、EOF、导出失败容错、依赖注入 |

运行素材见 `docs/logs/` 与 `docs/screenshots/`。

## 八、实验报告与提交物

- 实验报告：`542413320648吴昊阳/542413320648 吴昊阳-实验 2.docx`（Word 版）
  与 `542413320648 吴昊阳-实验 2.doc`（按课程模板转换的 .doc 版）
- 源码压缩包：`542413320648吴昊阳/542413320648 吴昊阳-实验 2.rar`（仅含 `src/` 与 `pom.xml`）
- 完整 UML：`docs/uml_class_diagram.png`、`docs/uml_class_diagram_full.png`

---

**实验日期：2026.11.23**
