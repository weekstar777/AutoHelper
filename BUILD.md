# AutoHelper 构建说明

## 环境要求

- Java 21
- Gradle 8.8
- IntelliJ IDEA（推荐）或其他IDE

## 构建方法

### 方法1：使用命令行

```bash
# Windows
gradlew.bat build

# Linux/Mac
./gradlew build
```

### 方法2：使用IDE

1. 用IntelliJ IDEA打开项目
2. 右键点击`build.gradle`
3. 选择"Run Gradle Tasks" -> "build"

## 构建产物

构建成功后，jar文件位于：
```
build/libs/AutoHelper-1.0.0.jar
```

文件大小约：31KB

## 开发环境

### 运行客户端测试

```bash
gradlew.bat runClient
```

### 生成源码

```bash
gradlew.bat genSources
```

## 清理构建

```bash
gradlew.bat clean
```

## 发布流程

1. 确保所有功能测试通过
2. 更新版本号（gradle.properties）
3. 运行构建：`gradlew.bat build`
4. 复制jar到releases目录
5. 编写RELEASE_NOTES.md
6. 创建Git tag
7. 推送到GitHub

## 常见问题

### Q: 构建失败？
A: 
- 检查Java版本是否为21
- 运行`gradlew.bat clean`清理缓存
- 删除`.gradle`文件夹重新构建

### Q: 如何修改版本号？
A: 编辑`gradle.properties`中的`mod_version`

### Q: 如何添加依赖？
A: 在`build.gradle`的`dependencies`块中添加
