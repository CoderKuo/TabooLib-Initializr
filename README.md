# TabooLib-模板生成器

快捷生成TabooLib项目解决方案,本项目使用`TabooLib`开发

## 构建

```shell
./gradlew build
```

## 打开build/libs/文件夹

```shell
java -jar TabooLib-Initializr-1.1.jar
```

## 使用方法

### 命令

```shell
# 主命令
taboolib # 别名 taboo tb

tb create 项目名 # 创建项目
tb create 项目名 模板 # 使用模板创建项目

tb edit 项目名 # 编辑项目
tb edit 项目名 group xxx.xxx # 编辑项目组名
tb edit 项目名 env list # 查看所有模块及索引
tb edit 项目名 env add 1 2 3 # 给项目中加入索引为1 2 3 的模块

tb write 项目名 路径 # 写出项目 路径为空时自动为当前目录下out文件夹
```

![](https://souts.cn/upload/iShot_2024-03-14_02.06.05.png)

### 图片展示

![](https://souts.cn/upload/展示图片1.png)

## 相关链接

[TabooLib](https://github.com/TabooLib/taboolib)

[TabooLib-Initializr-Template](https://github.com/CoderKuo/TabooLib-Initializr-Template)
