CREATE TABLE IF NOT EXISTS `t_community_post` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `content` text NOT NULL COMMENT '内容',
  `images` varchar(2000) DEFAULT NULL COMMENT '图片（多张逗号分隔）',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '审核状态 0待审核 1已通过 2已拒绝',
  `reject_reason` varchar(500) DEFAULT NULL COMMENT '拒绝原因',
  `view_count` int NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞次数',
  `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记 0正常 1已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区帖子表';
