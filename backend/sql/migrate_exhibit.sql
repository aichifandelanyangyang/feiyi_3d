-- ============================================
-- 展品表结构迁移：合并t_exhibit_detail到t_exhibit
-- ============================================

USE feiyi_3d;

-- 1. 为t_exhibit添加新字段
ALTER TABLE t_exhibit
    ADD COLUMN category VARCHAR(100) DEFAULT NULL COMMENT '展品类别' AFTER model_path,
    ADD COLUMN era VARCHAR(100) DEFAULT NULL COMMENT '年代' AFTER category,
    ADD COLUMN origin VARCHAR(200) DEFAULT NULL COMMENT '产地' AFTER era,
    ADD COLUMN material VARCHAR(200) DEFAULT NULL COMMENT '材质' AFTER origin,
    ADD COLUMN history TEXT DEFAULT NULL COMMENT '历史背景' AFTER material,
    ADD COLUMN craft TEXT DEFAULT NULL COMMENT '工艺特点' AFTER history,
    ADD COLUMN cultural_value TEXT DEFAULT NULL COMMENT '文化价值' AFTER craft;

-- 2. 迁移数据（从t_exhibit_detail复制到t_exhibit）
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

-- 3. 可选：删除t_exhibit_detail表（确认数据迁移成功后执行）
-- DROP TABLE IF EXISTS t_exhibit_detail;
