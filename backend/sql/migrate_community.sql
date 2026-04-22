-- ============================================
-- 社区帖子表迁移脚本
-- 执行此脚本为现有数据库添加社区功能表
-- ============================================

USE feiyi_3d;

-- 创建社区帖子表（如果不存在）
CREATE TABLE IF NOT EXISTS t_community_post (
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

-- 插入示例帖子数据（假设user用户ID为2，如果不存在请先确认）
-- 先检查是否有普通用户
INSERT INTO t_community_post (user_id, title, content, images, status, view_count, like_count) VALUES
(2, '探访景德镇：千年瓷都的匠心传承', '近日有幸前往景德镇，亲眼见证了手工制瓷的全过程。从拉坯、利坯到施釉、画坯，每一道工序都凝聚着匠人的心血与智慧。青花瓷上那淡雅的蓝色花纹，不仅是装饰，更是一种文化的延续。非遗传承，需要我们每一个人的关注与支持。', NULL, 1, 128, 45),
(2, '苏绣之美：指尖上的江南风情', '苏州刺绣，一针一线皆是诗意。有幸拜访了苏绣传承人工作室，亲眼目睹了双面绣"猫"的绣制过程。绣娘们用丝线在绸缎上绘出栩栩如生的画面，那细腻的针法、雅致的配色，令人叹为观止。苏绣不仅是技艺，更是一种对美的追求和对生活的热爱。', NULL, 1, 256, 89),
(2, '昆曲初体验：一曲牡丹亭，千古传唱情', '第一次走进昆曲剧场，观看了经典剧目《牡丹亭》。那婉转的唱腔、优雅的身段，将杜丽娘与柳梦梅的爱情故事演绎得动人心弦。昆曲不愧为"百戏之祖"，六百年的传承，依然焕发着独特的艺术魅力。作为年轻人，我们应该多了解这门古老的艺术。', NULL, 1, 189, 67),
(2, '剪纸艺术的魅力：纸上的万千世界', '春节期间，有幸参加了社区组织的剪纸活动。一把剪刀、一张红纸，在传承人的指导下，我剪出了一只简单的蝴蝶。剪纸看似简单，实则需要极大的耐心和技巧。每一幅剪纸作品，都是劳动人民智慧的结晶，承载着丰富的民俗文化内涵。', NULL, 1, 312, 112),
(2, '古琴感悟：弦上流淌的千年雅韵', '有幸聆听了一场古琴演奏会，那深沉悠远的琴音，仿佛穿越时空而来。《高山流水》《阳关三叠》等经典曲目，展现了古琴艺术的深邃意境。古琴不仅是乐器，更是中国文人精神的象征。在快节奏的现代生活中，偶尔静下心来聆听古琴，能让心灵得到净化。', NULL, 1, 145, 38);

-- 验证
SELECT COUNT(*) AS '帖子总数' FROM t_community_post;
SELECT '社区帖子表迁移完成！' AS message;