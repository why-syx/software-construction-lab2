# 远程仓库关联与推送指南

本地仓库已完成初始化与首次提交。推送前，请先在 GitHub 或 Gitee 上创建一个**空仓库**
（不要勾选“初始化 README / .gitignore / License”，否则会产生无关的初始提交）。

## 1. 检查提交身份

本地提交使用的身份是占位值：

```bash
git config user.name
git config user.email
```

如需改成自己的邮箱（不影响已有提交内容）：

```bash
git config user.name "吴昊阳"
git config user.email "你的邮箱@example.com"
```

> 注意：如果执行 `git commit --amend --reset-author`，提交哈希会变化；
> 实验报告中引用的首次提交哈希 2a9c572 将不再对应，建议保留原提交或同步修改报告。

## 2. GitHub（HTTPS）

```bash
cd "D:/ASUS/桌面/rjgz-sy/2"
git remote add origin https://github.com/<你的GitHub用户名>/<仓库名>.git
git branch -M main
git push -u origin main
```

如果已经添加过 origin，用下面这条替换地址：

```bash
git remote set-url origin https://github.com/<你的GitHub用户名>/<仓库名>.git
```

## 3. Gitee（HTTPS）

```bash
cd "D:/ASUS/桌面/rjgz-sy/2"
git remote add origin https://gitee.com/<你的Gitee用户名>/<仓库名>.git
git branch -M main
git push -u origin main
```

## 4. SSH 方式（可选）

```bash
# GitHub
git remote add origin git@github.com:<你的GitHub用户名>/<仓库名>.git

# Gitee
git remote add origin git@gitee.com:<你的Gitee用户名>/<仓库名>.git
```

使用 SSH 前需要先在本地生成密钥并把公钥添加到对应平台。

## 5. 推送后核对

```bash
git remote -v
git log --oneline -3
git status
```

确认远程地址正确、工作区干净后，把仓库网页地址填入实验报告第七节
`[请粘贴 GitHub/Gitee 仓库链接]` 的位置。

## 6. 提交物打包（WSL + rar）

本项目已生成 `542413320648吴昊阳/542413320648 吴昊阳-实验 2.rar`。如需重新打包：

```bash
cd "/mnt/d/ASUS/桌面/rjgz-sy/2"
rar a -r "542413320648吴昊阳/542413320648 吴昊阳-实验 2.rar" pom.xml src \
    -x"*.class" -x"*/target/*"
```

压缩包中只包含 `pom.xml` 与 `src/`，不包含 `target/`、`.idea/`、报告或截图。
