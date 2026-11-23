# docs 目录说明

本目录保存实验 2 的 UML 源文件、运行日志与截图素材。

## UML

| 文件 | 说明 |
| --- | --- |
| `class_diagram.puml` | 完整 UML 类图源文件（PlantUML，含全部 28 个类/接口/异常） |
| `uml_model.puml` | 模型层分图源文件（运算符、表达式、算式） |
| `uml_service.puml` | 服务与交互层分图源文件（生成、排版、导出、界面） |
| `class_diagram.mmd` | Mermaid 版本，便于在 GitHub/Gitee 直接预览 |
| `uml_class_diagram.png` | **报告用图**：两幅分图（模型层 + 服务层）纵向拼接，200 DPI |
| `uml_class_diagram_full.png` | 完整类图的高清版本，适合放大查看全部关系 |

`uml_class_diagram.png` 的生成方式：

```bash
# 1. 渲染两幅分图
java -jar plantuml.jar -tpng -charset UTF-8 uml_model.puml uml_service.puml
# 2. 裁剪空白并纵向拼接（工具源码见提交说明，或使用任意图像工具）
```

PlantUML 使用纯 Java 的 Smetana 布局引擎（`!pragma layout smetana`），无需安装 Graphviz。

## 日志（logs/）

| 文件 | 说明 |
| --- | --- |
| `console-demo.txt` | 设置 30 道题、每行 5 列并一键生成导出的完整会话 |
| `console-invalid-input.txt` | 非数字、负数、0、超大值、非法文件名等容错演示 |
| `junit-test.txt` | `mvn clean test` 完整输出（111 个用例全部通过） |
| `exercises.csv` | 样例导出文件（UTF-8 带 BOM，5 列） |
| `csv-preview.pdf` | 用 Excel 打开 `exercises.csv` 后导出的预览 PDF |

## 截图（screenshots/）

| 文件 | 用途 |
| --- | --- |
| `run.png` | 程序运行截图（生成 30 道题并导出 CSV） |
| `csv.png` | Excel 打开 CSV 的效果（含行号、列标，验证无乱码） |
| `junit-green.png` | JUnit 全绿结果（111 tests, 0 failures, BUILD SUCCESS） |

截图由 `docs/logs/` 中的真实运行日志渲染而来，可直接插入实验报告；如果希望展示自己电脑上的
运行界面，替换同名文件即可。
