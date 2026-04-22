"""
RAG 服务配置 —— 通过环境变量或 .env 文件加载
环境变量前缀: RAG_
"""

from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    # MySQL
    mysql_host: str = "127.0.0.1"
    mysql_port: int = 3306
    mysql_user: str = "root"
    mysql_password: str = "AAbb1234!"
    mysql_db: str = "feiyi_3d"

    # ChromaDB
    chroma_persist_dir: str = "./chroma_data"

    # Embedding 模型
    embedding_model: str = "shibing624/text2vec-base-chinese"

    # 检索
    search_top_k: int = 8

    # 数据同步间隔（分钟），0 = 不自动同步
    sync_interval_minutes: int = 30

    # 服务端口
    port: int = 8100

    model_config = {
        "env_prefix": "RAG_",
        "env_file": ".env",
        "env_file_encoding": "utf-8",
        "extra": "ignore",
    }


settings = Settings()
