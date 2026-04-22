"""
MySQL 数据读取模块 —— 从 feiyi_3d 数据库读取知识库、非遗、展品数据
"""

import pymysql
from typing import List, Dict, Any
from config import settings


def _get_conn():
    return pymysql.connect(
        host=settings.mysql_host,
        port=settings.mysql_port,
        user=settings.mysql_user,
        password=settings.mysql_password,
        database=settings.mysql_db,
        charset="utf8mb4",
        cursorclass=pymysql.cursors.DictCursor,
    )


def fetch_knowledge() -> List[Dict[str, Any]]:
    """读取知识库条目"""
    conn = _get_conn()
    try:
        with conn.cursor() as cur:
            cur.execute(
                "SELECT id, title, content, category FROM t_knowledge WHERE deleted_flag = 0"
            )
            return cur.fetchall()
    finally:
        conn.close()


def fetch_heritage() -> List[Dict[str, Any]]:
    """读取非遗项目"""
    conn = _get_conn()
    try:
        with conn.cursor() as cur:
            cur.execute(
                """SELECT id, name, level, region, inheritor, description, 
                          content, history, feature, declare_year
                   FROM t_heritage WHERE deleted_flag = 0"""
            )
            return cur.fetchall()
    finally:
        conn.close()


def fetch_exhibits() -> List[Dict[str, Any]]:
    """读取展品"""
    conn = _get_conn()
    try:
        with conn.cursor() as cur:
            cur.execute(
                """SELECT id, name, description, category, era, origin, 
                          material, history, craft, cultural_value,
                          image, exhibition_id
                   FROM t_exhibit WHERE deleted_flag = 0"""
            )
            return cur.fetchall()
    finally:
        conn.close()
