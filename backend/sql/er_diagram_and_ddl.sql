
## 二、实体关系说明

### 2.1 关系类型总览

| 关系名称 | 源实体 | 目标实体 | 关系类型 | 外键字段 |
|---------|--------|----------|----------|----------|
| 用户-帖子 | t_user | t_community_post | 一对多 | user_id |
| 用户-点赞 | t_user | t_community_like | 一对多 | user_id |
| 用户-帖子收藏 | t_user | t_community_favorite | 一对多 | user_id |
| 用户-非遗收藏 | t_user | t_favorite | 一对多 | user_id |
| 分类-非遗 | t_heritage_category | t_heritage | 一对多 | category_id |
| 非遗-展品 | t_heritage | t_exhibit | 一对多(可选) | heritage_id |
| 非遗-收藏记录 | t_heritage | t_favorite | 一对多 | heritage_id |
| 展厅-展品 | t_exhibition | t_exhibit | 一对多 | exhibition_id |
| 展厅-访问记录 | t_exhibition | t_visit_record | 一对多 | exhibition_id |
| 展品-详情 | t_exhibit | t_exhibit_detail | 一对一 | exhibit_id |
| 帖子-点赞 | t_community_post | t_community_like | 一对多 | post_id |
| 帖子-收藏 | t_community_post | t_community_favorite | 一对多 | post_id |

### 2.2 实体分组

**用户模块**
- `t_user` - 用户核心信息表

**非遗模块**
- `t_heritage_category` - 非遗分类
- `t_heritage` - 非遗项目核心表
- `t_favorite` - 用户收藏非遗项目的关联表

**展览模块**
- `t_exhibition` - 3D展厅
- `t_exhibit` - 展品核心表
- `t_exhibit_detail` - 展品详情扩展表
- `t_visit_record` - 访问记录统计表

**社区模块**
- `t_community_post` - 帖子表
- `t_community_like` - 点赞关联表
- `t_community_favorite` - 收藏关联表

**AI模块**
- `t_knowledge` - AI知识库（独立实体）

---

## 三、完整SQL建表语句

```sql
-- ============================================
-- 非遗3D数字化交互平台 完整数据库建表语句
-- 数据库: MySQL 8.0+
-- 字符集: UTF8MB4
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS feiyi_3d
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE feiyi_3d;
SET NAMES utf8mb4;

-- ============================================
-- 1. 用户表 (t_user)
-- ============================================
DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
    id BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    avatar VARCHAR(500) DEFAULT NULL COMMENT '头像',
    role_type TINYINT(1) NOT NULL DEFAULT 3 COMMENT '角色类型',
    status TINYINT(1) DEFAULT 1 COMMENT '状态',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    deleted_flag TINYINT(1) DEFAULT 0 COMMENT '删除标记',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_role_type (role_type),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

-- ============================================
-- 2. 非遗分类表 (t_heritage_category)
-- ============================================
DROP TABLE IF EXISTS t_heritage_category;
CREATE TABLE t_heritage_category (
    id BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '分类描述',
    deleted_flag TINYINT(1) DEFAULT 0 COMMENT '删除标记',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='非遗分类';

-- ============================================
-- 3. 非遗项目表 (t_heritage)
-- ============================================
DROP TABLE IF EXISTS t_heritage;
CREATE TABLE t_heritage (
    id BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(200) NOT NULL COMMENT '项目名称',
    level VARCHAR(50) DEFAULT NULL COMMENT '遗产级别',
    region VARCHAR(200) DEFAULT NULL COMMENT '申报地区',
    protection_unit VARCHAR(200) DEFAULT NULL COMMENT '保护单位',
    publish_time VARCHAR(50) DEFAULT NULL COMMENT '公布时间',
    description TEXT DEFAULT NULL COMMENT '项目简介',
    history TEXT DEFAULT NULL COMMENT '历史渊源',
    cover_image VARCHAR(500) DEFAULT NULL COMMENT '封面图片',
    view_count INT(11) DEFAULT 0 COMMENT '浏览量',
    favorite_count INT(11) DEFAULT 0 COMMENT '收藏数',
    deleted_flag TINYINT(1) DEFAULT 0 COMMENT '删除标记',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_category_id (category_id),
    KEY idx_level (level),
    KEY idx_region (region),
    KEY idx_name (name),
    CONSTRAINT fk_heritage_category FOREIGN KEY (category_id)
        REFERENCES t_heritage_category(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='非遗项目';

-- ============================================
-- 4. 用户收藏表 (t_favorite) - 非遗项目收藏
-- ============================================
DROP TABLE IF EXISTS t_favorite;
CREATE TABLE t_favorite (
    id BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT(20) UNSIGNED NOT NULL COMMENT '用户ID',
    heritage_id BIGINT(20) UNSIGNED NOT NULL COMMENT '非遗项目ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_heritage (user_id, heritage_id),
    KEY idx_user_id (user_id),
    KEY idx_heritage_id (heritage_id),
    CONSTRAINT fk_favorite_user FOREIGN KEY (user_id)
        REFERENCES t_user(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_favorite_heritage FOREIGN KEY (heritage_id)
        REFERENCES t_heritage(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='非遗项目收藏';


-- ============================================
-- 6. 展品表 (t_exhibit)
-- ============================================
DROP TABLE IF EXISTS t_exhibit;
CREATE TABLE t_exhibit (
    id BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(200) NOT NULL COMMENT '展品名称',
    description TEXT DEFAULT NULL COMMENT '展品描述',
    image VARCHAR(500) DEFAULT NULL COMMENT '展品图片',
    model_path VARCHAR(500) DEFAULT NULL COMMENT '3D模型路径',
    category VARCHAR(100) DEFAULT NULL COMMENT '展品类别',
    era VARCHAR(100) DEFAULT NULL COMMENT '年代',
    origin VARCHAR(200) DEFAULT NULL COMMENT '产地',
    material VARCHAR(200) DEFAULT NULL COMMENT '材质',
    history TEXT DEFAULT NULL COMMENT '历史背景',
    craft TEXT DEFAULT NULL COMMENT '制作工艺',
    cultural_value TEXT DEFAULT NULL COMMENT '文化价值', 
    deleted_flag TINYINT(1) DEFAULT 0 COMMENT '删除标记',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_heritage_id (heritage_id),
    KEY idx_name (name),
    CONSTRAINT fk_exhibit_heritage FOREIGN KEY (heritage_category_id)
        REFERENCES t_heritage_category(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='展品';


-- ============================================
-- 9. 社区帖子表 (t_community_post)
-- ============================================
DROP TABLE IF EXISTS t_community_post;
CREATE TABLE t_community_post (
    id BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT(20) UNSIGNED NOT NULL COMMENT '用户ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    images VARCHAR(2000) DEFAULT NULL COMMENT '图片',
    status TINYINT(1) DEFAULT 0 COMMENT '审核状态',
    reject_reason VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因',
    view_count INT(11) DEFAULT 0 COMMENT '浏览次数',
    like_count INT(11) DEFAULT 0 COMMENT '点赞次数',
    favorite_count INT(11) DEFAULT 0 COMMENT '收藏次数',
    deleted_flag TINYINT(1) DEFAULT 0 COMMENT '删除标记',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_status (status),
    KEY idx_create_time (create_time),
    KEY idx_title (title),
    CONSTRAINT fk_post_user FOREIGN KEY (user_id)
        REFERENCES t_user(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区帖子';

-- ============================================
-- 10. 社区点赞表 (t_community_like)
-- ============================================
DROP TABLE IF EXISTS t_community_like;
CREATE TABLE t_community_like (
    id BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT(20) UNSIGNED NOT NULL COMMENT '用户ID',
    post_id BIGINT(20) UNSIGNED NOT NULL COMMENT '帖子ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_post (user_id, post_id),
    KEY idx_user_id (user_id),
    KEY idx_post_id (post_id),
    CONSTRAINT fk_like_user FOREIGN KEY (user_id)
        REFERENCES t_user(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_like_post FOREIGN KEY (post_id)
        REFERENCES t_community_post(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区帖子点赞';

-- ============================================
-- 11. 社区收藏表 (t_community_favorite)
-- ============================================
DROP TABLE IF EXISTS t_community_favorite;
CREATE TABLE t_community_favorite (
    id BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT(20) UNSIGNED NOT NULL COMMENT '用户ID',
    post_id BIGINT(20) UNSIGNED NOT NULL COMMENT '帖子ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_post (user_id, post_id),
    KEY idx_user_id (user_id),
    KEY idx_post_id (post_id),
    CONSTRAINT fk_comm_favorite_user FOREIGN KEY (user_id)
        REFERENCES t_user(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_comm_favorite_post FOREIGN KEY (post_id)
        REFERENCES t_community_post(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区帖子收藏';



-- ============================================
-- 12. AI知识库表 (t_knowledge)
-- ============================================
DROP TABLE IF EXISTS t_knowledge;
CREATE TABLE t_knowledge (
    id BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    title VARCHAR(200) NOT NULL COMMENT '知识标题',
    content TEXT DEFAULT NULL COMMENT '知识内容',
    category VARCHAR(100) DEFAULT NULL COMMENT '知识分类',
    deleted_flag TINYINT(1) DEFAULT 0 COMMENT '删除标记',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_title (title),
    KEY idx_category (category),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI知识库';

---

## 四、表统计信息

| 序号 | 表名 | 中文名 | 字段数 | 紫引数 | 外键数 |
|------|------|--------|--------|--------|--------|
| 1 | t_user | 用户表 | 13 | 4 | 0 |
| 2 | t_heritage_category | 非遗分类表 | 6 | 2 | 0 |
| 3 | t_heritage | 非遗项目表 | 17 | 5 | 1 |
| 4 | t_favorite | 用户收藏表 | 4 | 4 | 2 |
| 5 | t_exhibition | 展厅表 | 10 | 3 | 0 |
| 6 | t_exhibit | 展品表 | 22 | 4 | 2 |
| 7 | t_exhibit_detail | 展品详情表 | 14 | 2 | 1 |
| 8 | t_visit_record | 访问记录表 | 6 | 4 | 1 |
| 9 | t_community_post | 社区帖子表 | 13 | 5 | 1 |
| 10 | t_community_like | 社区点赞表 | 4 | 4 | 2 |
| 11 | t_community_favorite | 社区收藏表 | 4 | 4 | 2 |
| 12 | t_knowledge | AI知识库表 | 7 | 3 | 0 |

**总计：12张表，约100+字段，完整的外键约束关系。**