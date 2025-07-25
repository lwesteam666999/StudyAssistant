# StudyAssistant - 学习辅助工具

一个基于Java Swing开发的智能学习辅助工具，帮助您建立良好的学习习惯，通过科学的时间管理和休息提醒来提高学习效率。

## 🌟 主要特性

### 📚 智能学习管理
- **自动计时**: 精确记录学习时间，自动管理学习周期
- **智能提醒**: 在学习过程中随机时间播放提示音，提醒适当休息
- **双模式支持**: 
  - **正常模式**: 90分钟学习周期，每5分钟内随机提醒
  - **测试模式**: 60秒学习周期，便于快速测试功能

### 🎵 音频提醒系统
- **声音提醒**: 内置音频文件，休息时间到达时自动播放
- **音量控制**: 可调节音量滑块，适应不同环境需求
- **多场景提醒**: 短休息和长休息不同的提醒方式

### 🖥️ 用户界面
- **现代化界面**: 使用FlatLaf主题，提供清爽的现代化界面
- **系统托盘**: 支持最小化到系统托盘，不占用任务栏空间
- **进度显示**: 实时显示当前学习进度和状态
- **便捷操作**: 简洁的按钮布局，一键开始/暂停/退出

### ⚙️ 个性化设置
- **休息时间配置**: 可自定义短休息(10-60秒)和长休息(15-30分钟)时间
- **状态管理**: 智能状态切换，支持学习、暂停、休息、空闲四种状态
- **测试模式**: 开发者友好的快速测试模式

## 🚀 快速开始

### 系统要求
- Java 21 或更高版本
- Windows/macOS/Linux 操作系统
- 支持系统托盘的桌面环境

### 安装运行

#### 方式一：直接运行JAR文件
1. 从 [Releases](https://github.com/lwesteam666999/StudyAssistant/releases) 页面下载最新版本的 `myapp-1.0-SNAPSHOT-shaded.jar`
2. 确保您的系统已安装Java 21或更高版本
3. 双击JAR文件运行，或使用命令行：
```bash
java -jar myapp-1.0-SNAPSHOT-shaded.jar
```

#### 方式二：从源码构建
1. 克隆项目到本地：
```bash
git clone https://github.com/lwesteam666999/StudyAssistant.git
cd StudyAssistant
```

2. 使用Maven构建项目：
```bash
mvn clean package
```

3. 运行生成的JAR文件：
```bash
java -jar target/myapp-1.0-SNAPSHOT-shaded.jar
```

## 📖 使用指南

### 基本操作
1. **开始学习**: 点击"开始学习"按钮开始计时
2. **暂停学习**: 点击"暂停学习"按钮暂停计时，再次点击恢复
3. **退出程序**: 点击"退出程序"按钮完全退出应用

### 高级功能
- **系统托盘**: 关闭窗口时程序会最小化到系统托盘，右键托盘图标可进行快速操作
- **休息时间设置**: 点击"休息时间设置"按钮自定义休息时长
- **测试模式**: 点击"测试模式"按钮切换到快速测试模式
- **音量调节**: 使用界面底部的音量滑块调节提醒音量

### 学习周期说明
- **正常模式**: 90分钟为一个完整学习周期，每5分钟内会有随机时间的休息提醒
- **短休息**: 默认20秒，可在设置中调整(10-60秒)
- **长休息**: 默认20分钟，可在设置中调整(15-30分钟)
- **测试模式**: 60秒为一个小周期，3个小周期后进入长休息，便于功能测试

## 🛠️ 技术架构

### 核心技术栈
- **Java 21**: 现代Java特性支持
- **Swing + FlatLaf**: 现代化桌面GUI框架
- **Maven**: 项目构建和依赖管理
- **Java Sound API**: 音频播放功能

### 项目结构
```
src/main/java/
├── app/           # 应用程序入口
├── ui/            # 用户界面组件
├── core/          # 核心业务逻辑
├── actions/       # 用户交互处理
└── resources/     # 资源文件(图标、音频)
```

### 核心组件
- **MainApp**: 应用程序主入口
- **MainWindow**: 主窗口界面管理
- **TimerService**: 计时服务和学习周期管理
- **StateManager**: 应用状态管理
- **SoundPlayer**: 音频播放服务

## 🤝 贡献指南

欢迎提交Issue和Pull Request来帮助改进这个项目！

### 开发环境设置
1. 确保安装Java 21 SDK
2. 安装Maven 3.6+
3. 克隆项目并导入IDE
4. 运行 `mvn compile` 确保项目可以正常编译

### 提交规范
- 提交前请确保代码通过编译
- 遵循现有的代码风格
- 为新功能添加适当的注释

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 联系方式

- GitHub: [lwesteam666999](https://github.com/lwesteam666999)
- 项目地址: [StudyAssistant](https://github.com/lwesteam666999/StudyAssistant)

## 🔄 更新日志

### v1.0-SNAPSHOT
- ✨ 初始版本发布
- 🎯 基础学习计时功能
- 🔊 音频提醒系统
- 🖥️ 系统托盘支持
- ⚙️ 个性化设置选项
- 🧪 测试模式支持

---

**让学习更高效，让休息更科学！** 🎓✨
