-- AI知识库表
SET NAMES utf8mb4;
CREATE TABLE IF NOT EXISTS `t_knowledge` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(200) NOT NULL COMMENT '知识标题',
    `content` TEXT NOT NULL COMMENT '知识内容',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '分类',
    `deleted_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标记 0未删除 1已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI知识库';

-- 默认知识条目
INSERT INTO `t_knowledge` (`title`, `content`, `category`) VALUES
('非物质文化遗产概述', '非物质文化遗产是指各族人民世代相传并视为其文化遗产组成部分的各种传统文化表现形式，以及与传统文化表现形式相关的实物和场所。包括：传统口头文学以及作为其载体的语言；传统美术、书法、音乐、舞蹈、戏剧、曲艺和杂技；传统技艺、医药和历法；传统礼仪、节庆等民俗；传统体育和游艺等。', '基础知识'),
('非遗保护的意义', '保护非物质文化遗产对于维护文化多样性、促进文化创新、增强民族凝聚力具有重要意义。非遗是中华民族智慧与文明的结晶，是连接民族情感的纽带和维系国家统一的基础。通过保护和传承非遗，可以让优秀传统文化在现代社会中焕发新的生机与活力。', '基础知识'),
('国家级非遗代表性项目', '国家级非物质文化遗产代表性项目名录由国务院批准公布。截至目前，国务院已公布五批国家级非遗代表性项目名录，共计1557个国家级非遗代表性项目。涵盖民间文学、传统音乐、传统舞蹈、传统戏剧、曲艺、传统体育游艺与杂技、传统美术、传统技艺、传统医药、民俗十大类别。', '基础知识'),
('非遗传承人制度', '非物质文化遗产代表性传承人是非遗的重要承载者和传递者，掌握着非遗的丰富知识和精湛技艺。国家建立了国家级、省级、市级、县级四级非遗代表性传承人认定制度，通过认定和支持传承人，推动非遗的传承与发展。传承人有义务开展传承活动，培养后继人才。', '基础知识'),
('中国非遗十大门类', '中国非物质文化遗产分为十大门类：1.民间文学；2.传统音乐；3.传统舞蹈；4.传统戏剧；5.曲艺；6.传统体育、游艺与杂技；7.传统美术；8.传统技艺；9.传统医药；10.民俗。每个门类下包含众多具体项目，体现了中华文化的博大精深和丰富多彩。', '基础知识');
