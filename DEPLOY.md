# 非遗3D数字化平台 — 服务器部署指南

> 适用于火山引擎 / 阿里云 / 腾讯云等 Linux 云服务器（Ubuntu/CentOS）

## 一、服务器要求

| 项目 | 最低配置                  | 推荐配置     |
| ---- | ------------------------- | ------------ |
| CPU  | 2核                       | 4核          |
| 内存 | 4GB                       | 8GB          |
| 磁盘 | 40GB                      | 80GB         |
| 系统 | Ubuntu 20.04+ / CentOS 7+ | Ubuntu 22.04 |
| 带宽 | 3Mbps                     | 5Mbps+       |

**需要开放的端口**（在安全组/防火墙中配置）：

- `80` — Nginx（前端 + 管理后台）
- `8080` — Java 后端 API
- `9090` — MinIO 对象存储
- `8100` — RAG 向量检索服务（可选，仅内网访问也可）

---

## 二、安装基础环境

以 **Ubuntu 22.04** 为例（CentOS 请将 `apt` 替换为 `yum`）：

### 2.1 更新系统

```bash
sudo apt update && sudo apt upgrade -y
```

### 2.2 安装 JDK 17

```bash
sudo apt install -y openjdk-17-jdk
java -version   # 确认输出 17.x
```

### 2.3 安装 Maven

```bash
sudo apt install -y maven
mvn -version
```

### 2.4 安装 Node.js 18+

```bash
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs
node -v   # 确认 18.x+
npm -v
```

### 2.5 安装 MySQL 8.0

```bash
sudo apt install -y mysql-server
sudo systemctl start mysql
sudo systemctl enable mysql

# 设置root密码
sudo mysql
> ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'AAbb1234!';
> FLUSH PRIVILEGES;
> EXIT;
```

### 2.6 安装 Nginx

```bash
sudo apt install -y nginx
sudo systemctl start nginx
sudo systemctl enable nginx
```

### 2.7 安装 Python 3.10+（RAG 服务需要）

```bash
sudo apt install -y python3 python3-pip python3-venv
python3 --version
```

### 2.8 安装 MinIO

```bash
wget https://dl.min.io/server/minio/release/linux-amd64/minio
chmod +x minio
sudo mv minio /usr/local/bin/

# 创建存储目录
sudo mkdir -p /data/minio
```

---

## 三、上传代码到服务器

在**本地电脑**上执行（将 `YOUR_SERVER_IP` 替换为服务器公网IP）：

```bash
# 方法1：使用 scp 直接上传（排除 node_modules 等）
cd d:\code\3D\code\feiyi_3d

# 先打包（Windows 上可用 7-Zip 或 Git Bash）
tar --exclude='node_modules' --exclude='.git' --exclude='target' --exclude='chroma_data' --exclude='__pycache__' -czf feiyi_3d.tar.gz .

scp feiyi_3d.tar.gz root@YOUR_SERVER_IP:/opt/
```

在**服务器**上解压：

```bash
mkdir -p /opt/feiyi_3d
cd /opt/feiyi_3d
tar -xzf /opt/feiyi_3d.tar.gz
```

也可以用 **Git** 方式：

```bash
cd /opt
git clone <你的仓库地址> feiyi_3d
```

---

## 四、初始化数据库

```bash
mysql -u root -p'AAbb1234!' < /opt/feiyi_3d/backend/sql/init.sql
```

验证：

```bash
mysql -u root -p'AAbb1234!' -e "USE feiyi_3d; SHOW TABLES;"
```

---

## 五、部署后端（Java Spring Boot）

### 5.1 修改配置

编辑 `backend/src/main/resources/application.yml`，确认数据库密码、MinIO 地址等正确。

如果服务器上 MinIO/MySQL 地址不同于 `localhost`，需要修改：

```bash
cd /opt/feiyi_3d/backend
vim src/main/resources/application.yml
```

### 5.2 打包

```bash
cd /opt/feiyi_3d/backend
mvn clean package -DskipTests
```

打包完成后生成 `target/feiyi-3d-platform-1.0.0.jar`

### 5.3 后台启动

```bash
# 创建日志目录
mkdir -p /opt/feiyi_3d/logs

# 后台运行
nohup java -jar target/feiyi-3d-platform-1.0.0.jar \
  --server.port=8080 \
  > /opt/feiyi_3d/logs/backend.log 2>&1 &

# 查看启动日志
tail -f /opt/feiyi_3d/logs/backend.log
```

验证：`curl http://localhost:8080/api/doc.html`

### 5.4 创建 Systemd 服务（开机自启）

```bash
sudo tee /etc/systemd/system/feiyi-backend.service << 'EOF'
[Unit]
Description=Feiyi 3D Backend
After=network.target mysql.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/feiyi_3d/backend
ExecStart=/usr/bin/java -jar /opt/feiyi_3d/backend/target/feiyi-3d-platform-1.0.0.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable feiyi-backend
sudo systemctl start feiyi-backend
sudo systemctl status feiyi-backend
```

---

## 六、部署 RAG 向量检索服务（Python）

```bash
cd /opt/feiyi_3d/rag-service

# 创建虚拟环境
python3 -m venv venv
source venv/bin/activate

# 安装依赖
pip install -r requirements.txt

# 后台运行
nohup python3 main.py > /opt/feiyi_3d/logs/rag.log 2>&1 &
```

验证：`curl http://localhost:8100/health`

Systemd 服务：

```bash
sudo tee /etc/systemd/system/feiyi-rag.service << 'EOF'
[Unit]
Description=Feiyi RAG Service
After=network.target mysql.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/feiyi_3d/rag-service
ExecStart=/opt/feiyi_3d/rag-service/venv/bin/python main.py
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable feiyi-rag
sudo systemctl start feiyi-rag
```

---

## 七、部署 MinIO 对象存储

```bash
# 启动 MinIO
MINIO_ROOT_USER=root MINIO_ROOT_PASSWORD=12345678 \
nohup minio server /data/minio --console-address ":9001" --address ":9090" \
  > /opt/feiyi_3d/logs/minio.log 2>&1 &
```

访问 `http://YOUR_SERVER_IP:9001` 登录 MinIO 控制台：

- 用户名：`root`
- 密码：`12345678`
- 创建 Bucket 名为 `feiyi`，设置访问策略为 `public`

---

## 八、构建前端静态文件

### 8.1 前端（用户端）

```bash
cd /opt/feiyi_3d/frontend
npm install

# !!重要!! 构建前需要创建生产环境配置
cat > .env.production << 'EOF'
VITE_API_BASE_URL=/api
EOF

npm run build
# 产出目录：dist/
```

### 8.2 管理后台

```bash
cd /opt/feiyi_3d/admin
npm install

cat > .env.production << 'EOF'
VITE_API_BASE_URL=/api
EOF

npm run build
# 产出目录：dist/
```

---

## 九、配置 Nginx 反向代理

```bash
sudo tee /etc/nginx/sites-available/feiyi << 'EOF'
# ===== 用户前端 =====
server {
    listen 80;
    server_name YOUR_SERVER_IP;  # 替换为你的域名或公网IP

    # 3D模型等大文件
    client_max_body_size 200M;

    # Gzip 压缩（大幅加速 JS/CSS/GLB 加载）
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml image/svg+xml model/gltf-binary;
    gzip_min_length 1024;
    gzip_comp_level 6;

    # 前端静态文件
    root /opt/feiyi_3d/frontend/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    # 后端 API 反向代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_read_timeout 120s;
    }

    # MinIO 文件访问代理（可选）
    location /minio/ {
        proxy_pass http://127.0.0.1:9090/;
    }
}

# ===== 管理后台（端口 81）=====
server {
    listen 81;
    server_name YOUR_SERVER_IP;

    client_max_body_size 200M;

    root /opt/feiyi_3d/admin/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
EOF

# 启用配置
sudo ln -sf /etc/nginx/sites-available/feiyi /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default

# 测试配置 & 重载
sudo nginx -t
sudo systemctl reload nginx
```

---

## 十、验证部署

| 服务         | 地址                                 | 说明             |
| ------------ | ------------------------------------ | ---------------- |
| 用户前端     | `http://YOUR_SERVER_IP`              | 主站，含虚拟展厅 |
| 管理后台     | `http://YOUR_SERVER_IP:81`           | 后台管理         |
| 后端 API     | `http://YOUR_SERVER_IP/api/doc.html` | Swagger 文档     |
| MinIO 控制台 | `http://YOUR_SERVER_IP:9001`         | 文件管理         |

---

## 十一、常用运维命令

```bash
# 查看各服务状态
sudo systemctl status feiyi-backend
sudo systemctl status feiyi-rag
sudo systemctl status nginx
sudo systemctl status mysql

# 重启服务
sudo systemctl restart feiyi-backend
sudo systemctl restart feiyi-rag

# 查看日志
tail -100f /opt/feiyi_3d/logs/backend.log
tail -100f /opt/feiyi_3d/logs/rag.log
journalctl -u feiyi-backend -f

# 前端更新部署（代码更新后）
cd /opt/feiyi_3d/frontend && npm run build
cd /opt/feiyi_3d/admin && npm run build
# 无需重启 Nginx

# 后端更新部署
cd /opt/feiyi_3d/backend && mvn clean package -DskipTests
sudo systemctl restart feiyi-backend
```

---

## 十二、安全组/防火墙

在火山引擎控制台的 **安全组** 中，确保开放以下入站端口：

| 端口 | 用途                              |
| ---- | --------------------------------- |
| 22   | SSH 远程登录                      |
| 80   | 用户前端                          |
| 81   | 管理后台                          |
| 9001 | MinIO 控制台（建议仅允许你的 IP） |

> `8080`、`8100`、`9090` 端口**不需要**对外开放，通过 Nginx 反向代理访问即可。

---

## 快速部署一键脚本参考

将以下脚本保存为 `deploy.sh` 放在 `/opt/feiyi_3d/` 下，以后更新代码后执行即可：

```bash
#!/bin/bash
set -e
cd /opt/feiyi_3d

echo "=== 构建前端 ==="
cd frontend && npm install && npm run build && cd ..
cd admin && npm install && npm run build && cd ..

echo "=== 构建后端 ==="
cd backend && mvn clean package -DskipTests && cd ..

echo "=== 重启服务 ==="
sudo systemctl restart feiyi-backend
sudo systemctl restart feiyi-rag
sudo systemctl reload nginx

echo "=== 部署完成 ==="
```
