# YOKONEX-Link

一个 Minecraft Forge 模组，通过 WebSocket 实现游戏与外部服务的实时通信。

## 功能特性

- **WebSocket 通信**：提供完整的 WebSocket API，支持实时双向通信
- **IM 集成**：集成役次元 IM 服务，支持消息收发和状态同步
- **实时推送**：服务器可主动推送游戏事件和 IM 消息
- **配置界面**：提供图形化配置界面，支持快捷键打开
- **心跳检测**：自动心跳机制保持连接活跃

## 系统要求

- **Minecraft 版本**：1.20.1
- **Forge 版本**：47.3.0
- **Java 版本**：17 或更高

## 安装方法

1. 下载最新版本的 `yokonex-link-x.x.x.jar` 文件
2. 将文件放入 Minecraft 的 `mods` 文件夹
3. 启动 Minecraft（确保已安装 Forge）
4. 模组将自动加载

## 构建方法

### 前置要求

- JDK 17 或更高版本
- Gradle 8.8+

### 构建步骤

```bash
# 克隆仓库
git clone https://github.com/tzwgoo/Minecraft-YCY-Link.git
cd Minecraft-YCY-Link

# Windows
gradlew.bat build

# Linux/Mac
./gradlew build
```

构建完成后，JAR 文件将位于 `build/libs/` 目录下。

### 开发环境设置

```bash
# 生成 IDE 运行配置
gradlew.bat genIntellijRuns  # IntelliJ IDEA
gradlew.bat genEclipseRuns   # Eclipse

# 运行客户端
gradlew.bat runClient

# 运行服务端
gradlew.bat runServer
```

## 使用说明

### 配置

首次运行后，配置文件将生成在 `run/config/yokonex-link.json`。

在游戏中按配置的快捷键（默认：未设置）打开配置界面，可以设置：
- WebSocket 服务器地址
- IM 服务配置（UID、Token、AppID）
- 其他连接参数

### WebSocket API

本模组提供完整的 WebSocket API，详见 [WebSocket API 文档](docs/WEBSOCKET_API.md)。

#### 连接地址

```
ws://localhost:3001
```

#### 主要功能

- **心跳检测**：`type: "ping"`
- **获取状态**：`type: "getStatus"`
- **登录 IM**：`type: "login"`
- **发送指令**：`type: "sendCommand"`
- **重新初始化**：`type: "reinit"`

#### 服务器推送

- 连接状态通知
- IM 消息实时推送
- 网络状态变化
- 定时心跳

### 示例代码

#### JavaScript 客户端

```javascript
const ws = new WebSocket('ws://localhost:3001');

ws.onopen = () => {
  // 发送心跳
  ws.send(JSON.stringify({ type: 'ping' }));
  
  // 获取状态
  ws.send(JSON.stringify({ type: 'getStatus' }));
};

ws.onmessage = (event) => {
  const data = JSON.parse(event.data);
  console.log('收到消息:', data);
};
```

更多示例请参考 [WebSocket API 文档](docs/WEBSOCKET_API.md)。

## 项目结构

```
src/main/java/cn/cinian/
├── YOKONEXLink.java          # 主模组类
├── YOKONEXLinkClient.java    # 客户端入口
├── config/                   # 配置相关
├── client/                   # 客户端功能
├── event/                    # 事件处理
├── integration/              # 外部服务集成
├── mixin/                    # Mixin 注入
├── screen/                   # GUI 界面
└── websocket/                # WebSocket 通信
```

## 技术栈

- **构建工具**：Gradle 8.8
- **模组加载器**：Forge 47.3.0
- **映射**：ParchmentMC 2023.09.03
- **WebSocket 库**：Java-WebSocket 1.5.4 (使用 Shadow 打包)
- **Java 版本**：17

## 依赖项

- [Java-WebSocket](https://github.com/TooTallNate/Java-WebSocket) - WebSocket 客户端库（已打包）

## 许可证

本项目采用 [CC0 1.0 Universal](LICENSE) 许可证。

## 贡献

欢迎提交 Issue 和 Pull Request！

## 更新日志

### v1.0.0
- 初始版本发布
- WebSocket 通信功能
- IM 服务集成
- 配置界面支持

## 联系方式

- 作者：cinian
- 项目主页：[GitHub](https://github.com/tzwgoo/Minecraft-YCY-Link.git)

## 致谢

感谢以下开源项目：
- [Minecraft Forge](https://github.com/MinecraftForge/MinecraftForge)
- [ParchmentMC](https://github.com/ParchmentMC/Parchment)
- [Java-WebSocket](https://github.com/TooTallNate/Java-WebSocket)