<template>
  <div class="community-detail-container">
    <!-- 头部导航 -->
    <header class="page-header">
      <div class="logo" @click="router.push('/')">
        <img src="@/assets/images/logo.svg" alt="logo" class="logo-img" />
        <span class="logo-text">非遗3D数字化交互平台</span>
      </div>
      <nav class="nav-menu">
        <router-link to="/" class="nav-item">首页</router-link>
        <router-link to="/exhibition-hall" class="nav-item">虚拟展厅</router-link>
        <router-link to="/heritage-list" class="nav-item">非遗典藏</router-link>
        <router-link to="/community" class="nav-item active">社区互动</router-link>
        <router-link to="/ai-assistant" class="nav-item">非遗智识</router-link>
      </nav>
      <div class="user-area">
        <template v-if="userStore.isLoggedIn">
          <el-dropdown trigger="click" @command="handleUserCommand">
            <div class="user-info">
              <el-avatar :size="36" :src="userStore.avatar || undefined">
                {{ userStore.username?.charAt(0)?.toUpperCase() }}
              </el-avatar>
              <span class="user-name">{{ userStore.username }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" @click="router.push('/login')">登录</el-button>
        </template>
      </div>
    </header>

    <!-- 主体 -->
    <main class="page-main">
      <!-- 卷轴导航 -->
      <div class="scroll-navigator">
        <button class="back-brush" @click="router.push('/community')">
          <span class="brush-icon">‹</span>
          <span class="brush-text">返归雅集</span>
        </button>
      </div>

      <!-- 书画长卷 -->
      <div v-loading="loading" class="manuscript-scroll">
        <!-- 卷首 -->
        <div class="scroll-header">
          <div class="header-ornament">
            <div class="ornament-pattern"></div>
          </div>
        </div>

        <!-- 帖心 -->
        <div class="scroll-body">
          <!-- 作者印 -->
          <div class="author-seal">
            <div class="seal-ring">
              <el-avatar :size="64" :src="post.avatar || undefined" class="author-avatar">
                {{ (post.username || '?').charAt(0).toUpperCase() }}
              </el-avatar>
            </div>
            <div class="author-credentials">
              <span class="credential-name">{{ post.username }}</span>
              <span class="credential-time">{{ formatTime(post.createTime) }} 挥毫</span>
            </div>
            <div class="authenticity-stamp">
              <div class="stamp-mark">雅<br>集</div>
            </div>
          </div>

          <!-- 正文 -->
          <div class="manuscript-content">
            <h1 class="scroll-title">{{ post.title }}</h1>
            <div class="scroll-text">{{ post.content }}</div>
          </div>

          <!-- 图卷 -->
          <div v-if="post.images" class="illustration-scroll">
            <div class="scroll-divider">
              <span class="divider-line"></span>
              <span class="divider-icon">图</span>
              <span class="divider-line"></span>
            </div>
            <div class="scroll-gallery">
              <div
                v-for="(img, idx) in post.images.split(',')"
                :key="idx"
                class="gallery-scroll"
              >
                <el-image
                  :src="img"
                  fit="cover"
                  class="scroll-image"
                  :preview-src-list="post.images.split(',')"
                  :initial-index="idx"
                />
                <div class="image-caption">图 {{ ['一', '二', '三', '四', '五', '六', '七', '八', '九'][idx] || (idx + 1) }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 卷尾 -->
        <div class="scroll-footer">
          <div class="footer-ornament">
            <div class="ornament-pattern"></div>
          </div>
          <div class="scroll-colophon">
            <div class="colophon-stats">
              <div class="stat-mark">
                <el-icon><View /></el-icon>
                <span class="mark-count">{{ post.viewCount || 0 }}</span>
                <span class="mark-label">览</span>
              </div>
              <button
                class="stat-mark like-btn"
                :class="{ active: isLiked }"
                @click="handleLike"
              >
                <span class="heart-icon">
                  <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                    <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
                  </svg>
                </span>
                <span class="mark-count">{{ post.likeCount || 0 }}</span>
                <span class="mark-label">赞</span>
              </button>
            </div>
            <button
              class="collector-seal"
              :class="{ active: isFavorited }"
              @click="handleFavorite"
            >
              {{ isFavorited ? '已藏' : '入藏' }}
            </button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, View, Star } from '@element-plus/icons-vue'
import { useUserStore } from '@/store'
import { getCommunityDetail, addPostFavorite, removePostFavorite, checkPostFavorite, addPostLike, removePostLike, checkPostLike } from '@/api/community'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const post = ref({})
const isFavorited = ref(false)
const isLiked = ref(false)

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await getCommunityDetail(route.params.id)
    if (res.data) {
      post.value = res.data
    }
    // 检查收藏和点赞状态
    if (userStore.isLoggedIn) {
      const favRes = await checkPostFavorite(route.params.id)
      isFavorited.value = favRes.data || false
      const likeRes = await checkPostLike(route.params.id)
      isLiked.value = likeRes.data || false
    }
  } catch (e) {
    console.error('获取详情失败', e)
  } finally {
    loading.value = false
  }
}

const handleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    if (isFavorited.value) {
      await removePostFavorite(route.params.id)
      isFavorited.value = false
      ElMessage.success('已取消收藏')
    } else {
      await addPostFavorite(route.params.id)
      isFavorited.value = true
      ElMessage.success('收藏成功')
    }
  } catch (e) {
    // handled by interceptor
  }
}

const handleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    if (isLiked.value) {
      await removePostLike(route.params.id)
      isLiked.value = false
      post.value.likeCount = Math.max(0, post.value.likeCount - 1)
      ElMessage.success('已取消点赞')
    } else {
      await addPostLike(route.params.id)
      isLiked.value = true
      post.value.likeCount = post.value.likeCount + 1
      ElMessage.success('点赞成功')
    }
  } catch (e) {
    // handled by interceptor
  }
}

const handleUserCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

onMounted(() => fetchDetail())
</script>

<style scoped lang="scss">
.community-detail-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #f0ebe0 0%, #e8e2d4 100%);
  position: relative;

  /* 古纸纹理 */
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background:
      repeating-linear-gradient(
        0deg,
        transparent,
        transparent 4px,
        rgba(140, 126, 116, 0.02) 4px,
        rgba(140, 126, 116, 0.02) 5px
      ),
      repeating-linear-gradient(
        90deg,
        transparent,
        transparent 100px,
        rgba(201, 168, 76, 0.01) 100px,
        rgba(201, 168, 76, 0.01) 101px
      );
    pointer-events: none;
  }
}

.page-main {
  max-width: 720px;
  margin: 0 auto;
  padding: 32px 24px 60px;
  position: relative;
  z-index: 1;
}

/* ---- 卷轴导航 ---- */
.scroll-navigator {
  margin-bottom: 24px;

  .back-brush {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    background: rgba(255, 253, 248, 0.8);
    border: 1px solid rgba(201, 168, 76, 0.3);
    border-radius: 24px;
    cursor: pointer;
    transition: all var(--transition);

    &:hover {
      background: #fff;
      border-color: var(--gold);
    }

    .brush-icon {
      font-size: 20px;
      color: var(--text-light);
    }

    .brush-text {
      font-family: var(--font-serif);
      font-size: 14px;
      color: var(--text-color);
      letter-spacing: 2px;
    }
  }
}

/* ---- 书画长卷 ---- */
.manuscript-scroll {
  background: linear-gradient(145deg, #fffdf8 0%, #faf8f3 50%, #f5f1e8 100%);
  border-radius: 12px;
  box-shadow:
    0 8px 40px rgba(44, 36, 32, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  position: relative;
  overflow: hidden;

  /* 卷轴装饰边 */
  &::before,
  &::after {
    content: '';
    position: absolute;
    left: 0;
    right: 0;
    height: 8px;
    background: linear-gradient(90deg,
      rgba(201, 168, 76, 0.3) 0%,
      rgba(166, 64, 41, 0.2) 50%,
      rgba(201, 168, 76, 0.3) 100%
    );
  }

  &::before { top: 0; }
  &::after { bottom: 0; }
}

/* 卷首卷尾装饰 */
.scroll-header,
.scroll-footer {
  padding: 20px 40px;

  .header-ornament,
  .footer-ornament {
    display: flex;
    justify-content: center;

    .ornament-pattern {
      width: 60px;
      height: 4px;
      background: linear-gradient(90deg,
        transparent,
        rgba(201, 168, 76, 0.4),
        transparent
      );
      position: relative;

      &::before,
      &::after {
        content: '';
        position: absolute;
        top: 50%;
        transform: translateY(-50%);
        width: 6px;
        height: 6px;
        background: rgba(201, 168, 76, 0.4);
        border-radius: 50%;
      }

      &::before { left: -12px; }
      &::after { right: -12px; }
    }
  }
}

/* 帖心 */
.scroll-body {
  padding: 20px 48px 40px;
}

/* 作者印 */
.author-seal {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid rgba(212, 201, 184, 0.4);

  .seal-ring {
    position: relative;
    padding: 4px;
    background: linear-gradient(135deg, rgba(166, 64, 41, 0.15) 0%, rgba(201, 168, 76, 0.25) 100%);
    border-radius: 50%;

    .author-avatar {
      border: 2px solid #fff;
    }
  }

  .author-credentials {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;

    .credential-name {
      font-family: var(--font-serif);
      font-size: 18px;
      color: var(--text-color);
      letter-spacing: 2px;
    }

    .credential-time {
      font-size: 13px;
      color: var(--text-light);
      letter-spacing: 1px;
    }
  }

  .authenticity-stamp {
    .stamp-mark {
      width: 48px;
      height: 48px;
      border: 2px solid rgba(166, 64, 41, 0.3);
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-family: var(--font-serif);
      font-size: 14px;
      line-height: 1.2;
      color: rgba(166, 64, 41, 0.6);
      text-align: center;
      transform: rotate(-6deg);
      background: rgba(166, 64, 41, 0.04);
    }
  }
}

/* 正文内容 */
.manuscript-content {
  margin-bottom: 36px;

  .scroll-title {
    font-family: var(--font-serif);
    font-size: 28px;
    font-weight: 700;
    color: var(--text-color);
    line-height: 1.6;
    letter-spacing: 3px;
    margin-bottom: 28px;
    text-align: center;
    word-break: break-word;
  }

  .scroll-text {
    font-size: 16px;
    color: var(--text-secondary);
    line-height: 2.2;
    white-space: pre-wrap;
    word-break: break-word;
    text-align: justify;
    padding: 0 16px;
  }
}

/* 图卷 */
.illustration-scroll {
  margin-bottom: 32px;

  .scroll-divider {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16px;
    margin-bottom: 24px;

    .divider-line {
      flex: 1;
      max-width: 120px;
      height: 1px;
      background: linear-gradient(90deg, transparent, rgba(201, 168, 76, 0.4), transparent);
    }

    .divider-icon {
      width: 32px;
      height: 32px;
      border: 1px solid rgba(201, 168, 76, 0.4);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-family: var(--font-serif);
      font-size: 14px;
      color: rgba(201, 168, 76, 0.7);
    }
  }

  .scroll-gallery {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 16px;

    .gallery-scroll {
      position: relative;

      .scroll-image {
        width: 100%;
        aspect-ratio: 4/3;
        border-radius: 4px;
        border: 1px solid rgba(212, 201, 184, 0.5);
        object-fit: cover;
      }

      .image-caption {
        position: absolute;
        bottom: 8px;
        right: 8px;
        padding: 4px 10px;
        background: rgba(255, 255, 255, 0.9);
        border: 1px solid rgba(212, 201, 184, 0.4);
        border-radius: 2px;
        font-family: var(--font-serif);
        font-size: 11px;
        color: var(--text-light);
        letter-spacing: 1px;
      }
    }
  }
}

/* 卷尾题跋 */
.scroll-colophon {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 40px;
  border-top: 1px solid rgba(212, 201, 184, 0.4);
  background: linear-gradient(180deg, transparent 0%, rgba(201, 168, 76, 0.03) 100%);

  .colophon-stats {
    display: flex;
    gap: 24px;

    .stat-mark {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 14px;
      color: var(--text-light);

      .el-icon {
        font-size: 18px;
        color: rgba(201, 168, 76, 0.7);
      }

      .mark-count {
        font-family: var(--font-serif);
        font-weight: 500;
      }

      .mark-label {
        font-size: 12px;
        letter-spacing: 1px;
      }
    }

    .like-btn {
      padding: 4px 8px;
      border: 1px solid transparent;
      border-radius: 4px;
      background: transparent;
      cursor: pointer;
      transition: all var(--transition);

      .heart-icon {
        display: flex;
        align-items: center;
        color: rgba(201, 168, 76, 0.7);
        transition: color var(--transition);
      }

      &:hover {
        background: rgba(201, 168, 76, 0.08);
        border-color: rgba(201, 168, 76, 0.3);

        .heart-icon {
          color: var(--gold);
        }
      }

      &.active {
        .heart-icon {
          color: var(--primary-color);
        }

        .mark-count {
          color: var(--primary-color);
        }
      }
    }
  }

  .collector-seal {
    padding: 6px 14px;
    border: 1px solid rgba(166, 64, 41, 0.25);
    border-radius: 3px;
    font-family: var(--font-serif);
    font-size: 12px;
    color: rgba(166, 64, 41, 0.6);
    letter-spacing: 3px;
    transform: rotate(-2deg);
    background: rgba(166, 64, 41, 0.04);
    cursor: pointer;
    transition: all var(--transition);

    &:hover {
      background: rgba(166, 64, 41, 0.08);
      border-color: rgba(166, 64, 41, 0.4);
    }

    &.active {
      background: rgba(166, 64, 41, 0.12);
      border-color: rgba(166, 64, 41, 0.5);
      color: rgba(166, 64, 41, 0.8);
    }
  }
}
</style>
