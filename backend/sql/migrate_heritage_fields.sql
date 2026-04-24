-- ============================================
-- 非遗项目表字段统一迁移脚本
-- ============================================

USE feiyi_3d;

-- 1. 将 declare_year 改名为 publish_time (公布时间)
ALTER TABLE t_heritage
    CHANGE COLUMN declare_year publish_time VARCHAR(50) DEFAULT NULL COMMENT '公布时间';

-- 2. 新增 protection_unit 字段 (保护单位)
ALTER TABLE t_heritage
    ADD COLUMN protection_unit VARCHAR(200) DEFAULT NULL COMMENT '保护单位' AFTER region;

-- 3. 新增 favorite_count 字段 (收藏数，用于缓存优化)
ALTER TABLE t_heritage
    ADD COLUMN favorite_count INT(11) DEFAULT 0 COMMENT '收藏数' AFTER view_count;

-- 4. 删除 content 字段 (详细内容)
ALTER TABLE t_heritage DROP COLUMN IF EXISTS content;

-- 5. 删除 feature 字段 (工艺特点)
ALTER TABLE t_heritage DROP COLUMN IF EXISTS feature;

-- 初始化收藏数（从收藏表统计）
UPDATE t_heritage h
    SET h.favorite_count = (
        SELECT COUNT(*) FROM t_favorite f WHERE f.heritage_id = h.id
    );

-- 完成
SELECT '非遗项目表字段迁移完成！' AS message;