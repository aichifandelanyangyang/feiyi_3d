# 非遗3D数字化交互平台

## 项目简介

非遗3D数字化交互平台是一个基于现代Web技术构建的非物质文化遗产数字化展示系统。通过3D虚拟展厅技术，让用户能够身临其境地体验和了解中华优秀传统文化。

## 技术栈

### 前端
- **Vue 3** - 渐进式JavaScript框架
- **Three.js** - 3D图形渲染引擎
- **Vite** - 下一代前端构建工具
- **Vue Router** - 官方路由管理器
- **Pinia** - 状态管理库
- **Element Plus** - Vue 3 UI组件库
- **Axios** - HTTP客户端

### 后端
- **Java 17** - 编程语言
- **Spring Boot 3.2** - 应用框架
- **MyBatis Plus** - ORM框架
- **MySQL** - 关系型数据库
- **Druid** - 数据库连接池
- **Knife4j** - API文档工具

## 功能模块

### 🏛️ 虚拟展厅
- 3D展厅模型加载与渲染
- 第一人称漫游模式
- 自由视角浏览模式
- 碰撞检测与物理交互
- 实时小地图导航
- 展品信息展示

### 📚 非遗项目
- 非遗项目分类浏览
- 项目搜索功能
- 项目详情展示
- 历史渊源介绍
- 工艺特点说明

## 项目结构

```
feiyi_3d/
├── frontend/                # 前端项目
│   ├── public/              # 静态资源
│   │   └── models/          # 3D模型文件
│   ├── src/
│   │   ├── api/             # API接口
│   │   ├── assets/          # 资源文件
│   │   ├── components/      # 公共组件
│   │   ├── router/          # 路由配置
│   │   ├── store/           # 状态管理
│   │   ├── utils/           # 工具类
│   │   │   └── three/       # Three.js相关
│   │   └── views/           # 页面视图
│   ├── package.json
│   └── vite.config.js
│
├── backend/                 # 后端项目
│   ├── src/main/java/com/feiyi/
│   │   ├── common/          # 公共模块
│   │   ├── config/          # 配置类
│   │   ├── handler/         # 全局处理器
│   │   └── module/          # 业务模块
│   │       ├── heritage/    # 非遗模块
│   │       └── exhibition/  # 展厅模块
│   ├── src/main/resources/
│   │   ├── mapper/          # MyBatis映射文件
│   │   └── application.yml  # 应用配置
│   ├── sql/                 # 数据库脚本
│   └── pom.xml
│
└── README.md
```

## 快速开始

### 环境要求

- Node.js >= 18.0
- Java >= 17
- MySQL >= 8.0
- Maven >= 3.8

### 数据库初始化

```bash
# 登录MySQL执行初始化脚本
mysql -u root -p < backend/sql/init.sql
```

### 后端启动

```bash
cd backend

# 安装依赖并启动
mvn clean install
mvn spring-boot:run
```

后端启动后访问: http://localhost:8080/api/doc.html 查看API文档

### 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端启动后访问: http://localhost:3000

### 模型文件配置

将3D展厅模型文件 `3d展厅稿11.glb` 复制到 `frontend/public/models/` 目录下。

## 展厅操作说明

| 按键/操作 | 功能 |
|---------|------|
| W / ↑ | 向前移动 |
| S / ↓ | 向后移动 |
| A / ← | 向左移动 |
| D / → | 向右移动 |
| 鼠标移动 | 调整视角 |
| 鼠标滚轮 | 缩放视野 |
| 点击画面 | 进入第一人称模式 |
| ESC | 退出第一人称模式 |

## 开发规范

- 前端代码遵循 [Vue开发规范](./Vue开发规范.md)
- 后端代码遵循 [Java开发规范](./Java开发规范.md)

## 许可证

MIT License
