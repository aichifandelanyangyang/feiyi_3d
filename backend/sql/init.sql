-- ============================================
-- 非遗3D数字化交互平台 数据库初始化脚本
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS feiyi_3d DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE feiyi_3d;

-- 确保初始化脚本按 UTF8MB4 解析（避免中文插入乱码）
SET NAMES utf8mb4;

-- ============================================
-- 非遗分类表
-- ============================================
DROP TABLE IF EXISTS t_heritage_category;
CREATE TABLE t_heritage_category (
    id BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '分类描述',
    deleted_flag TINYINT(1) DEFAULT 0 COMMENT '删除标记 0未删除 1已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='非遗分类表';

-- ============================================
-- 非遗项目表
-- ============================================
DROP TABLE IF EXISTS t_heritage;
CREATE TABLE t_heritage (
    id BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(200) NOT NULL COMMENT '项目名称',
    category_id BIGINT(20) UNSIGNED DEFAULT NULL COMMENT '分类ID',
    level VARCHAR(50) DEFAULT NULL COMMENT '级别：国家级、省级、市级、县级',
    region VARCHAR(200) DEFAULT NULL COMMENT '所属地区',
    declare_year VARCHAR(20) DEFAULT NULL COMMENT '申报年份',
    inheritor VARCHAR(500) DEFAULT NULL COMMENT '传承人',
    description TEXT DEFAULT NULL COMMENT '简介',
    content LONGTEXT DEFAULT NULL COMMENT '详细内容',
    history TEXT DEFAULT NULL COMMENT '历史渊源',
    feature TEXT DEFAULT NULL COMMENT '工艺特点',
    cover_image VARCHAR(500) DEFAULT NULL COMMENT '封面图片',
    view_count INT(11) DEFAULT 0 COMMENT '浏览次数',
    sort INT(11) DEFAULT 0 COMMENT '排序',
    deleted_flag TINYINT(1) DEFAULT 0 COMMENT '删除标记 0未删除 1已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_category_id (category_id),
    KEY idx_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='非遗项目表';

-- ============================================
-- 展厅表
-- ============================================
DROP TABLE IF EXISTS t_exhibition;
CREATE TABLE t_exhibition (
    id BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(200) NOT NULL COMMENT '展厅名称',
    description TEXT DEFAULT NULL COMMENT '展厅描述',
    cover_image VARCHAR(500) DEFAULT NULL COMMENT '展厅封面图片',
    model_path VARCHAR(500) DEFAULT NULL COMMENT '3D模型文件路径',
    visit_count INT(11) DEFAULT 0 COMMENT '访问量',
    status TINYINT(1) DEFAULT 1 COMMENT '状态 0禁用 1启用',
    deleted_flag TINYINT(1) DEFAULT 0 COMMENT '删除标记 0未删除 1已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='展厅表';

-- ============================================
-- 展品表
-- ============================================
DROP TABLE IF EXISTS t_exhibit;
CREATE TABLE t_exhibit (
    id BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    exhibition_id BIGINT(20) UNSIGNED NOT NULL COMMENT '展厅ID',
    heritage_id BIGINT(20) UNSIGNED DEFAULT NULL COMMENT '关联非遗项目ID',
    name VARCHAR(200) NOT NULL COMMENT '展品名称',
    description TEXT DEFAULT NULL COMMENT '展品描述',
    image VARCHAR(500) DEFAULT NULL COMMENT '展品图片',
    model_path VARCHAR(500) DEFAULT NULL COMMENT '3D模型路径',
    position_x DECIMAL(10, 4) DEFAULT 0 COMMENT 'X坐标',
    position_y DECIMAL(10, 4) DEFAULT 0 COMMENT 'Y坐标',
    position_z DECIMAL(10, 4) DEFAULT 0 COMMENT 'Z坐标',
    sort INT(11) DEFAULT 0 COMMENT '排序',
    deleted_flag TINYINT(1) DEFAULT 0 COMMENT '删除标记 0未删除 1已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_exhibition_id (exhibition_id),
    KEY idx_heritage_id (heritage_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='展品表';

-- ============================================
-- 访问记录表
-- ============================================
DROP TABLE IF EXISTS t_visit_record;
CREATE TABLE t_visit_record (
    id BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    exhibition_id BIGINT(20) UNSIGNED DEFAULT NULL COMMENT '展厅ID',
    visitor_ip VARCHAR(50) DEFAULT NULL COMMENT '访客IP',
    user_agent VARCHAR(500) DEFAULT NULL COMMENT '浏览器UA',
    visit_duration INT(11) DEFAULT 0 COMMENT '访问时长(秒)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_exhibition_id (exhibition_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访问记录表';

-- ============================================
-- 用户表
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
    role_type TINYINT(1) NOT NULL DEFAULT 3 COMMENT '角色类型 1管理员 3普通用户',
    status TINYINT(1) DEFAULT 1 COMMENT '状态 0禁用 1启用',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    deleted_flag TINYINT(1) DEFAULT 0 COMMENT '删除标记 0未删除 1已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_role_type (role_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 展品详情表（扩展展品信息）
-- ============================================
DROP TABLE IF EXISTS t_exhibit_detail;
CREATE TABLE t_exhibit_detail (
    id BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    exhibit_id BIGINT(20) UNSIGNED NOT NULL COMMENT '展品ID',
    category VARCHAR(100) DEFAULT NULL COMMENT '展品类别',
    era VARCHAR(100) DEFAULT NULL COMMENT '年代',
    origin VARCHAR(200) DEFAULT NULL COMMENT '产地',
    material VARCHAR(200) DEFAULT NULL COMMENT '材质',
    size VARCHAR(200) DEFAULT NULL COMMENT '尺寸',
    history TEXT DEFAULT NULL COMMENT '历史背景',
    craft TEXT DEFAULT NULL COMMENT '工艺特点',
    cultural_value TEXT DEFAULT NULL COMMENT '文化价值',
    audio_path VARCHAR(500) DEFAULT NULL COMMENT '语音讲解路径',
    video_path VARCHAR(500) DEFAULT NULL COMMENT '视频路径',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_exhibit_id (exhibit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='展品详情表';

-- ============================================
-- 社区帖子表
-- ============================================
DROP TABLE IF EXISTS t_community_post;
CREATE TABLE t_community_post (
    id BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT(20) UNSIGNED NOT NULL COMMENT '用户ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    images VARCHAR(2000) DEFAULT NULL COMMENT '图片（多张逗号分隔）',
    status TINYINT(1) DEFAULT 0 COMMENT '审核状态 0待审核 1已通过 2已拒绝',
    reject_reason VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因',
    view_count INT(11) DEFAULT 0 COMMENT '浏览次数',
    like_count INT(11) DEFAULT 0 COMMENT '点赞次数',
    deleted_flag TINYINT(1) DEFAULT 0 COMMENT '删除标记 0未删除 1已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_status (status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区帖子表';

-- ============================================
-- 初始化数据
-- ============================================

-- 插入非遗分类
INSERT INTO t_heritage_category (name, description) VALUES
('传统技艺', '传统手工技艺类非物质文化遗产'),
('传统美术', '传统美术类非物质文化遗产'),
('传统音乐', '传统音乐类非物质文化遗产'),
('传统舞蹈', '传统舞蹈类非物质文化遗产'),
('传统戏剧', '传统戏剧类非物质文化遗产'),
('民俗', '民俗类非物质文化遗产');

-- 插入非遗项目示例数据
INSERT INTO t_heritage (name, category_id, level, region, declare_year, inheritor, description, history, feature, view_count, sort) VALUES
('景德镇手工制瓷技艺', 1, '国家级', '江西省景德镇市', '2006年', '王锡良、秦锡麟等', '景德镇手工制瓷技艺是中国传统陶瓷文化的杰出代表，具有悠久的历史和独特的艺术价值。', '景德镇制瓷始于汉代，兴于唐宋，盛于明清。', '以"白如玉、明如镜、薄如纸、声如磬"著称于世。', 1234, 1),
('苏州刺绣', 2, '国家级', '江苏省苏州市', '2006年', '姚建萍等', '苏绣是中国四大名绣之一，以针法精细、色彩雅致著称。', '苏绣起源于三国时期，发展于宋代。', '具有"平、齐、细、密、和、光、顺、匀"的特点。', 2345, 2),
('昆曲', 5, '国家级', '江苏省昆山市', '2001年', '蔡正仁、张继青等', '昆曲是中国最古老的戏曲剧种之一，被誉为"百戏之祖"。', '昆曲起源于元末明初的昆山腔。', '唱腔婉转细腻，表演优雅精致。', 3456, 3),
('中国剪纸', 2, '国家级', '全国', '2009年', '多位传承人', '剪纸是中国最普及的民间传统装饰艺术之一，有着悠久的历史。', '剪纸艺术起源于汉代，至今已有两千多年历史。', '以剪刀或刻刀在纸上剪刻花纹，用于装点生活。', 4567, 4),
('古琴艺术', 3, '国家级', '全国', '2003年', '龚一、李祥霆等', '古琴是中国最古老的弹拨乐器之一，具有三千年以上的历史。', '古琴相传为伏羲所创，是中国文人的必修乐器。', '音色深沉，余音悠远，具有深厚的文化内涵。', 2234, 5),
('龙舞', 4, '国家级', '全国', '2006年', '多位传承人', '龙舞是中国传统民间舞蹈，以舞龙表演祈求风调雨顺。', '龙舞起源于汉代祭祀活动。', '表演形式多样，有布龙、草龙、火龙等。', 1890, 6);

-- 插入展厅数据
INSERT INTO t_exhibition (name, description, model_path, status) VALUES
('非遗文化展厅', '非物质文化遗产3D数字展厅，汇集中华优秀传统文化精华', '/models/exhibition-hall.glb', 1);

-- 插入展品数据
INSERT INTO t_exhibit (exhibition_id, heritage_id, name, description, position_x, position_y, position_z, sort) VALUES
(1, 1, '青花瓷瓶', '景德镇青花瓷代表作品', -5, 1, -8, 1),
(1, 1, '粉彩花卉盘', '清代粉彩瓷器精品', -3, 1, -8, 2),
(1, 2, '苏绣双面绣', '苏州刺绣双面绣屏风', 0, 1, -8, 3),
(1, 3, '昆曲戏服', '传统昆曲表演服饰', 3, 1, -8, 4),
(1, 4, '剪纸作品', '中国传统剪纸艺术作品', 5, 1, -8, 5);

-- 插入初始用户（密码都是123456，使用BCrypt加密）
INSERT INTO t_user (username, password, real_name, role_type, status) VALUES
('admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36PfTbBF9g0LPWmmdrjJaJO', '系统管理员', 1, 1),
('user', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36PfTbBF9g0LPWmmdrjJaJO', '普通用户', 3, 1);

-- 插入社区帖子示例数据（user用户发布的帖子）
INSERT INTO t_community_post (user_id, title, content, images, status, view_count, like_count) VALUES
(2, '探访景德镇：千年瓷都的匠心传承', '近日有幸前往景德镇，亲眼见证了手工制瓷的全过程。从拉坯、利坯到施釉、画坯，每一道工序都凝聚着匠人的心血与智慧。青花瓷上那淡雅的蓝色花纹，不仅是装饰，更是一种文化的延续。非遗传承，需要我们每一个人的关注与支持。', NULL, 1, 128, 45),
(2, '苏绣之美：指尖上的江南风情', '苏州刺绣，一针一线皆是诗意。有幸拜访了苏绣传承人工作室，亲眼目睹了双面绣"猫"的绣制过程。绣娘们用丝线在绸缎上绘出栩栩如生的画面，那细腻的针法、雅致的配色，令人叹为观止。苏绣不仅是技艺，更是一种对美的追求和对生活的热爱。', NULL, 1, 256, 89),
(2, '昆曲初体验：一曲牡丹亭，千古传唱情', '第一次走进昆曲剧场，观看了经典剧目《牡丹亭》。那婉转的唱腔、优雅的身段，将杜丽娘与柳梦梅的爱情故事演绎得动人心弦。昆曲不愧为"百戏之祖"，六百年的传承，依然焕发着独特的艺术魅力。作为年轻人，我们应该多了解这门古老的艺术。', NULL, 1, 189, 67),
(2, '剪纸艺术的魅力：纸上的万千世界', '春节期间，有幸参加了社区组织的剪纸活动。一把剪刀、一张红纸，在传承人的指导下，我剪出了一只简单的蝴蝶。剪纸看似简单，实则需要极大的耐心和技巧。每一幅剪纸作品，都是劳动人民智慧的结晶，承载着丰富的民俗文化内涵。', NULL, 1, 312, 112),
(2, '古琴感悟：弦上流淌的千年雅韵', '有幸聆听了一场古琴演奏会，那深沉悠远的琴音，仿佛穿越时空而来。《高山流水》《阳关三叠》等经典曲目，展现了古琴艺术的深邃意境。古琴不仅是乐器，更是中国文人精神的象征。在快节奏的现代生活中，偶尔静下心来聆听古琴，能让心灵得到净化。', NULL, 1, 145, 38);

-- 插入展品详情
INSERT INTO t_exhibit_detail (exhibit_id, category, era, origin, material, history, craft, cultural_value) VALUES
(1, '瓷器', '清代', '江西景德镇', '高岭土', '青花瓷是中国瓷器的珍品，始创于唐代，成熟于元代，盛于明清。', '采用钴料在瓷坯上绑画，施透明釉后高温烧制而成。', '青花瓷是中国陶瓷文化的瑰宝，被誉为"瓷国明珠"。'),
(2, '瓷器', '清代', '江西景德镇', '瓷土', '粉彩瓷是清代康熙晚期创烧的釉上彩新品种。', '在烧好的白瓷上用玻璃白打底，再用各种彩料绑画，低温烧制。', '粉彩瓷色彩柔和淡雅，具有极高的艺术价值。'),
(3, '刺绣', '现代', '江苏苏州', '丝线、绸缎', '苏绣起源于三国时期，至今已有两千多年历史。', '以针代笔，以线代墨，采用齐针、套针等技法。', '苏绣是中国四大名绣之一，代表了中国传统刺绣工艺的最高水平。'),
(4, '服饰', '清代', '江苏昆山', '丝绸、金线', '昆曲戏服是昆曲表演的重要组成部分。', '采用刺绣、织锦等多种工艺，配以金银线装饰。', '昆曲戏服体现了中国传统服饰文化的精华。'),
(5, '剪纸', '现代', '陕西', '宣纸', '剪纸艺术起源于汉代，有两千多年历史。', '以剪刀或刻刀在纸上剪刻出各种图案。', '剪纸是中国最普及的民间艺术之一，体现了劳动人民的智慧。');

-- 完成
SELECT '数据库初始化完成！' AS message;



-- 位置: backend/sql/migrate_exhibit.sql

USE feiyi_3d;

-- 为t_exhibit添加新字段
ALTER TABLE t_exhibit
    ADD COLUMN category VARCHAR(100) DEFAULT NULL COMMENT '展品类别' AFTER model_path,
    ADD COLUMN era VARCHAR(100) DEFAULT NULL COMMENT '年代' AFTER category,
    ADD COLUMN origin VARCHAR(200) DEFAULT NULL COMMENT '产地' AFTER era,
    ADD COLUMN material VARCHAR(200) DEFAULT NULL COMMENT '材质' AFTER origin,
    ADD COLUMN history TEXT DEFAULT NULL COMMENT '历史背景' AFTER material,
    ADD COLUMN craft TEXT DEFAULT NULL COMMENT '工艺特点' AFTER history,
    ADD COLUMN cultural_value TEXT DEFAULT NULL COMMENT '文化价值' AFTER craft;

-- 迁移旧数据
UPDATE t_exhibit e
    LEFT JOIN t_exhibit_detail ed ON e.id = ed.exhibit_id
SET e.category = ed.category,
    e.era = ed.era,
    e.origin = ed.origin,
    e.material = ed.material,
    e.history = ed.history,
    e.craft = ed.craft,
    e.cultural_value = ed.cultural_value
WHERE ed.exhibit_id IS NOT NULL;