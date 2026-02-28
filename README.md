# Claude Persona - IDEA 插件

为 Claude Code 添加趣味交互人设，让 AI 编程更加有趣！

## 功能特性

- 🎭 **31种文学CP人设**：来自《小王子》《红楼梦》《傲慢与偏见》等经典作品
- 🎲 **随机切换**：一键随机选择人设
- 📅 **定时切换**：支持每日/每月自动随机
- ✏️ **自动生成CLAUDE.md**：一键将人设应用到项目

## 安装要求

### 系统环境

| 要求项 | 版本要求 | 说明 |
|--------|----------|------|
| **JDK** | 17+ | 编译和运行都需要 JDK 17 或更高版本 |
| **IntelliJ IDEA** | 2023.2+ (Build 232.*) | 社区版(IC)或旗舰版(Ultimate)均可 |
| **操作系统** | Windows / macOS / Linux | 跨平台支持 |
| **Gradle** | 8.5 (wrapper已包含) | 无需手动安装，使用项目自带的gradlew |

### 兼容性说明

- 插件基于 IntelliJ Platform SDK 2023.2 开发
- 兼容 IntelliJ IDEA 2023.2 及以上版本（until-build: 999.*）
- 依赖 Kotlin 1.9.21 运行时
- 依赖 Gson 2.10.1（已内嵌，无需额外安装）

## 安装

### 方式一：从源码构建安装（推荐）

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd x\ for\ idea
   ```

2. **构建插件**
   ```bash
   # macOS / Linux
   ./gradlew buildPlugin

   # Windows
   gradlew.bat buildPlugin
   ```

3. **在 IDEA 中安装**
   - 打开 IDEA → `Settings`（macOS: `Preferences`）
   - 进入 `Plugins` → 点击齿轮图标 ⚙️
   - 选择 `Install Plugin from Disk...`
   - 选择 `build/distributions/Claude Persona-1.0.0.zip`
   - 重启 IDEA

### 方式二：开发模式运行

适合开发者调试：

```bash
# 启动带插件的沙箱 IDEA
./gradlew runIde
```

### 常见问题

<details>
<summary><b>❓ Gradle 构建失败</b></summary>

确保已安装 JDK 17 并设置 `JAVA_HOME` 环境变量：
```bash
# 检查 Java 版本
java -version

# 设置 JAVA_HOME (macOS 示例)
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```
</details>

<details>
<summary><b>❓ 插件安装后不显示</b></summary>

1. 确认 IDEA 版本 >= 2023.2
2. 检查 `Settings` → `Plugins` 中插件是否已启用
3. 尝试 `File` → `Invalidate Caches / Restart`
</details>

<details>
<summary><b>❓ 找不到 Claude Persona 工具窗口</b></summary>

- 在 IDEA 右侧边栏查找 "Claude Persona" 图标
- 或通过 `View` → `Tool Windows` → `Claude Persona` 打开
</details>

## 使用方法

### 1. 打开 Claude Persona 工具窗口

在 IDEA 右侧边栏点击 "Claude Persona" 图标

### 2. 选择人设

- **按分类筛选**：使用顶部下拉框筛选分类
- **随机选择**：点击 "🎲 随机选择" 按钮
- **手动选择**：在列表中点击选择

### 3. 应用人设

点击 "✅ 应用人设" 按钮，插件会自动在项目根目录的 `CLAUDE.md` 文件中添加人设配置。

### 4. 重启 Claude Code

修改 `CLAUDE.md` 后需要重启 Claude Code 使人设生效。

## 切换模式

| 模式 | 说明 |
|------|------|
| MANUAL | 手动切换人设 |
| DAILY | 每天自动随机一个新人设 |
| MONTHLY | 每月自动随机一个新人设 |

可在 `Settings` → `Tools` → `Claude Persona` 中设置默认模式。

## 人设列表

- 中国古典文学：刘备-诸葛亮、林黛玉-贾宝玉、孙悟空-唐僧...
- 西方古典文学：达西-伊丽莎白、罗密欧-朱丽叶...
- 武侠小说：黄蓉-郭靖、杨过-小龙女、令狐冲-任盈盈...
- 影视/动漫：杰克-露丝、工藤新一-毛利兰、赫敏-哈利...

完整列表请查看 `src/main/resources/personas.json`

## 开发

### 环境要求

- JDK 17+
- IntelliJ IDEA 2023.2+

### 构建

```bash
./gradlew buildPlugin
```

### 运行测试IDE

```bash
./gradlew runIde
```

## 许可证

MIT License