# 构建说明

由于Gradle wrapper需要从网络下载，请按以下步骤手动初始化：

## 方法1：使用已有的Gradle

如果你电脑上已安装Gradle 8.0+：

```bash
cd D:\project_collection\code_project\AutoHelper
gradle wrapper --gradle-version 8.8
./gradlew build
```

## 方法2：从Fabric模板复制

从任何现有的Fabric模组项目复制以下文件到本项目：
- `gradle/wrapper/gradle-wrapper.jar`
- `gradlew` (Linux/Mac)
- `gradlew.bat` (Windows)

然后运行：
```bash
./gradlew build
```

## 方法3：使用IDE

在IntelliJ IDEA中：
1. 打开项目文件夹
2. IDEA会自动识别为Gradle项目
3. 等待依赖下载完成
4. 右键`build.gradle` -> Run Gradle Tasks -> build

## 构建输出

成功后，模组jar文件位于：
`build/libs/autohelper-1.0.0.jar`

将此文件放入`.minecraft/mods`文件夹即可使用。
