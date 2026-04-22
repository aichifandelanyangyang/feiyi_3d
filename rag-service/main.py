"""
非遗智识 RAG 语义检索服务
FastAPI + ChromaDB + sentence-transformers
"""

import logging
from contextlib import asynccontextmanager
from typing import List, Optional

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

from config import settings
from vector_store import sync_all, search, get_model

# ───────── 日志 ─────────
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(name)s] %(levelname)s - %(message)s",
)
logger = logging.getLogger("rag-service")


# ───────── 定时同步 ─────────
scheduler = None


def _start_scheduler():
    global scheduler
    if settings.sync_interval_minutes > 0:
        from apscheduler.schedulers.background import BackgroundScheduler
        scheduler = BackgroundScheduler()
        scheduler.add_job(sync_all, "interval", minutes=settings.sync_interval_minutes)
        scheduler.start()
        logger.info("定时同步已启动，间隔 %d 分钟", settings.sync_interval_minutes)


# ───────── 生命周期 ─────────
@asynccontextmanager
async def lifespan(app: FastAPI):
    # 启动时: 预加载模型 + 全量同步
    get_model()
    sync_all()
    _start_scheduler()
    yield
    # 关闭时
    if scheduler:
        scheduler.shutdown(wait=False)


# ───────── FastAPI App ─────────
app = FastAPI(
    title="非遗智识 RAG 服务",
    description="基于 ChromaDB 向量数据库的语义检索微服务",
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


# ───────── 请求/响应模型 ─────────
class SearchRequest(BaseModel):
    query: str
    top_k: Optional[int] = None
    collections: Optional[List[str]] = None  # knowledge / heritage / exhibit


class SearchResultItem(BaseModel):
    id: str
    source_type: str
    document: str
    metadata: dict
    distance: float


class SearchResponse(BaseModel):
    results: List[SearchResultItem]


class SyncResponse(BaseModel):
    message: str


# ───────── 接口 ─────────
@app.post("/api/rag/search", response_model=SearchResponse)
def rag_search(req: SearchRequest):
    """语义检索：接收自然语言查询，返回 Top-K 相关文档"""
    if not req.query or not req.query.strip():
        raise HTTPException(status_code=400, detail="query 不能为空")

    results = search(
        query=req.query.strip(),
        top_k=req.top_k,
        collections=req.collections,
    )
    return SearchResponse(results=[SearchResultItem(**r) for r in results])


@app.post("/api/rag/sync", response_model=SyncResponse)
def rag_sync():
    """手动触发全量数据同步"""
    sync_all()
    return SyncResponse(message="同步完成")


@app.get("/api/rag/health")
def health():
    return {"status": "ok", "model": settings.embedding_model}


# ───────── 入口 ─────────
if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=settings.port, reload=False)
