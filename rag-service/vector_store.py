"""
ChromaDB 向量存储模块 —— 管理 embedding 索引和语义检索
"""

import logging
import chromadb
from chromadb.config import Settings as ChromaSettings
from sentence_transformers import SentenceTransformer
from typing import List, Dict, Any, Optional

from config import settings
from db import fetch_knowledge, fetch_heritage, fetch_exhibits

logger = logging.getLogger("rag-service")

# 全局单例
_model: Optional[SentenceTransformer] = None
_client: Optional[chromadb.ClientAPI] = None

# Collection 名称
COL_KNOWLEDGE = "knowledge"
COL_HERITAGE = "heritage"
COL_EXHIBIT = "exhibit"


def get_model() -> SentenceTransformer:
    global _model
    if _model is None:
        logger.info("加载 Embedding 模型: %s ...", settings.embedding_model)
        _model = SentenceTransformer(settings.embedding_model)
        logger.info("Embedding 模型加载完成")
    return _model


def get_client() -> chromadb.ClientAPI:
    global _client
    if _client is None:
        _client = chromadb.PersistentClient(
            path=settings.chroma_persist_dir,
            settings=ChromaSettings(anonymized_telemetry=False),
        )
    return _client


def _embed(texts: List[str]) -> List[List[float]]:
    """批量生成 embedding 向量"""
    model = get_model()
    embeddings = model.encode(texts, show_progress_bar=False, normalize_embeddings=True)
    return embeddings.tolist()


# ───────── 数据同步 ─────────

def sync_all():
    """从 MySQL 读取全量数据，写入 ChromaDB"""
    logger.info("开始同步数据到 ChromaDB ...")
    _sync_knowledge()
    _sync_heritage()
    _sync_exhibits()
    logger.info("数据同步完成")


def _sync_knowledge():
    rows = fetch_knowledge()
    if not rows:
        logger.info("知识库无数据，跳过")
        return
    client = get_client()
    col = client.get_or_create_collection(COL_KNOWLEDGE)

    ids, documents, metadatas = [], [], []
    for r in rows:
        doc_id = f"knowledge_{r['id']}"
        text = f"{r['title'] or ''}。{r['content'] or ''}"
        ids.append(doc_id)
        documents.append(text)
        metadatas.append({
            "source_id": int(r["id"]),
            "source_type": "knowledge",
            "title": r["title"] or "",
            "category": r["category"] or "",
        })

    embeddings = _embed(documents)
    col.upsert(ids=ids, documents=documents, embeddings=embeddings, metadatas=metadatas)
    logger.info("知识库同步完成: %d 条", len(ids))


def _sync_heritage():
    rows = fetch_heritage()
    if not rows:
        logger.info("非遗项目无数据，跳过")
        return
    client = get_client()
    col = client.get_or_create_collection(COL_HERITAGE)

    ids, documents, metadatas = [], [], []
    for r in rows:
        doc_id = f"heritage_{r['id']}"
        parts = [
            r["name"] or "",
            f"级别：{r['level']}" if r.get("level") else "",
            f"地区：{r['region']}" if r.get("region") else "",
            f"传承人：{r['inheritor']}" if r.get("inheritor") else "",
            r["description"] or "",
            r["history"] or "",
            r["feature"] or "",
        ]
        text = "。".join(p for p in parts if p)
        ids.append(doc_id)
        documents.append(text)
        metadatas.append({
            "source_id": int(r["id"]),
            "source_type": "heritage",
            "name": r["name"] or "",
            "level": r["level"] or "",
            "region": r["region"] or "",
            "inheritor": r["inheritor"] or "",
        })

    embeddings = _embed(documents)
    col.upsert(ids=ids, documents=documents, embeddings=embeddings, metadatas=metadatas)
    logger.info("非遗项目同步完成: %d 条", len(ids))


def _sync_exhibits():
    rows = fetch_exhibits()
    if not rows:
        logger.info("展品无数据，跳过")
        return
    client = get_client()
    col = client.get_or_create_collection(COL_EXHIBIT)

    ids, documents, metadatas = [], [], []
    for r in rows:
        doc_id = f"exhibit_{r['id']}"
        parts = [
            r["name"] or "",
            f"类别：{r['category']}" if r.get("category") else "",
            f"年代：{r['era']}" if r.get("era") else "",
            f"产地：{r['origin']}" if r.get("origin") else "",
            f"材质：{r['material']}" if r.get("material") else "",
            r["description"] or "",
            r["history"] or "",
            r["craft"] or "",
            r["cultural_value"] or "",
        ]
        text = "。".join(p for p in parts if p)
        ids.append(doc_id)
        documents.append(text)
        metadatas.append({
            "source_id": int(r["id"]),
            "source_type": "exhibit",
            "name": r["name"] or "",
            "category": r["category"] or "",
            "era": r["era"] or "",
            "origin": r["origin"] or "",
            "image": r["image"] or "",
            "exhibition_id": int(r["exhibition_id"]) if r.get("exhibition_id") else 0,
        })

    embeddings = _embed(documents)
    col.upsert(ids=ids, documents=documents, embeddings=embeddings, metadatas=metadatas)
    logger.info("展品同步完成: %d 条", len(ids))


# ───────── 语义检索 ─────────

def search(query: str, top_k: int = None, collections: List[str] = None) -> List[Dict[str, Any]]:
    """
    对指定 collection（默认全部）做语义检索，返回 Top-K 结果。
    每个结果包含: source_type, document, metadata, distance
    """
    if top_k is None:
        top_k = settings.search_top_k
    if collections is None:
        collections = [COL_KNOWLEDGE, COL_HERITAGE, COL_EXHIBIT]

    query_emb = _embed([query])[0]
    client = get_client()
    results = []

    for col_name in collections:
        try:
            col = client.get_collection(col_name)
        except Exception:
            continue

        count = col.count()
        if count == 0:
            continue

        n = min(top_k, count)
        res = col.query(query_embeddings=[query_emb], n_results=n)

        if res and res["ids"] and res["ids"][0]:
            for i, doc_id in enumerate(res["ids"][0]):
                results.append({
                    "id": doc_id,
                    "source_type": col_name,
                    "document": res["documents"][0][i] if res["documents"] else "",
                    "metadata": res["metadatas"][0][i] if res["metadatas"] else {},
                    "distance": res["distances"][0][i] if res["distances"] else 1.0,
                })

    # 按 distance 升序排序（越小越相关），取 Top-K
    results.sort(key=lambda x: x["distance"])
    return results[:top_k]
