<template>
  <!-- 非遗详情页 -->
  <div class="heritage-detail-container">
    <!-- 头部导航 start -->
    <header class="page-header">
      <div class="logo" @click="goHome">
        <img src="@/assets/images/logo.svg" alt="logo" class="logo-img" />
        <span class="logo-text">非遗3D数字化交互平台</span>
      </div>
      <nav class="nav-menu">
        <router-link to="/" class="nav-item">首页</router-link>
        <router-link to="/exhibition-hall" class="nav-item">虚拟展厅</router-link>
        <router-link to="/heritage-list" class="nav-item">非遗典藏</router-link>
        <router-link to="/community" class="nav-item">社区互动</router-link>
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
    <!-- 头部导航 end -->

    <!-- 主体内容 start -->
    <main class="page-main">
      <!-- 返回导览 -->
      <div class="navigation-bar">
        <button class="back-guide" @click="goBack">
          <span class="guide-icon">‹</span>
          <span class="guide-text">返归典藏</span>
        </button>
      </div>

      <!-- 藏品鉴赏卷 -->
      <div class="appreciation-scroll">
        <!-- 卷首 -->
        <div class="scroll-prelude">
          <div class="prelude-ornament"></div>
        </div>

        <!-- 品赏区 -->
        <div class="appreciation-body">
          <!-- 左卷：影像 -->
          <div class="visual-scroll">
            <div class="visual-frame">
              <div class="frame-corner corner-tl"></div>
              <div class="frame-corner corner-tr"></div>
              <div class="frame-corner corner-bl"></div>
              <div class="frame-corner corner-br"></div>
              <div class="visual-image">
                <img :src="heritageDetail.coverImage || defaultImage" :alt="heritageDetail.name" />
              </div>
            </div>
            <div class="visual-caption">
              <span class="caption-label">藏品影像</span>
              <span class="caption-seal">鉴</span>
            </div>
          </div>

          <!-- 右卷：铭识 -->
          <div class="inscription-scroll">
            <div class="inscription-header">
              <div class="header-category">{{ heritageDetail.categoryName }}</div>
              <h1 class="header-title">{{ heritageDetail.name }}</h1>
              <div class="header-level" v-if="heritageDetail.level">
                <span class="level-badge">{{ heritageDetail.level }}</span>
              </div>
            </div>

            <div class="inscription-records">
              <div class="record-item">
                <span class="record-label">申报地区</span>
                <span class="record-value">{{ heritageDetail.region }}</span>
              </div>
              <div class="record-item">
                <span class="record-label">公布时间</span>
                <span class="record-value">{{ heritageDetail.publishTime || '不详' }}</span>
              </div>
              <div class="record-item">
                <span class="record-label">保护单位</span>
                <span class="record-value">{{ heritageDetail.protectionUnit || '待考' }}</span>
              </div>
            </div>

            <div class="inscription-abstract">
              <div class="abstract-title">题要</div>
              <p class="abstract-text">{{ heritageDetail.description }}</p>
            </div>

            <div class="inscription-actions">
              <button v-if="heritageDetail.hasExhibit" class="action-primary" @click="goExhibition">
                <span>展厅观摩</span>
              </button>
              <button class="action-secondary" @click="handleFavorite">
                <el-icon><Star /></el-icon>
                <span>{{ isFavorited ? '已藏' : '入藏' }}</span>
              </button>
            </div>
          </div>
        </div>

        <!-- 详述卷 -->
        <div class="detail-sections">
          <section class="detail-section" v-if="heritageDetail.history">
            <div class="section-header">
              <span class="header-icon">源</span>
              <h2 class="header-name">渊源</h2>
            </div>
            <div class="section-body">
              <p>{{ heritageDetail.history }}</p>
            </div>
          </section>
        </div>

        <!-- 卷尾 -->
        <div class="scroll-finale">
          <div class="finale-ornament">
            <div class="ornament-line"></div>
            <div class="ornament-seal">非遗典藏</div>
            <div class="ornament-line"></div>
          </div>
        </div>
      </div>
    </main>
    <!-- 主体内容 end -->
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Star, Share } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getHeritageDetail } from '@/api/heritage'
import { checkFavorite, addFavorite, removeFavorite } from '@/api/user'
import { useUserStore } from '@/store'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const handleUserCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

// 默认图片
const defaultImage = 'https://via.placeholder.com/800x500?text=非遗典藏'

// 非遗详情数据
const heritageDetail = ref({})

// 相关展品列表
const exhibitList = ref([])

// 返回首页
const goHome = () => {
  router.push('/')
}

// 返回列表
const goBack = () => {
  router.push('/heritage-list')
}

// 跳转展厅
const goExhibition = () => {
  router.push('/exhibition-hall')
}

// 获取详情数据方法
const fetchDetail = async () => {
  const id = route.params.id
  try {
    const res = await getHeritageDetail(id)
    if (res.code === 200 && res.data) {
      heritageDetail.value = res.data
    }
  } catch (e) {
    console.error('获取详情失败:', e)
  }
}

// 收藏状态
const isFavorited = ref(false)

const checkFav = async () => {
  if (!userStore.isLoggedIn) return
  try {
    const res = await checkFavorite(route.params.id)
    isFavorited.value = !!res.data
  } catch (e) {
    // ignore
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
      await removeFavorite(route.params.id)
      isFavorited.value = false
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite(route.params.id)
      isFavorited.value = true
      ElMessage.success('收藏成功')
    }
  } catch (e) {
    // handled by interceptor
  }
}

// 生命周期
onMounted(() => {
  fetchDetail()
  checkFav()
})
</script>

<style scoped lang="scss">
.heritage-detail-container {
  min-height: 100vh;
  background:
    /* 左侧暖光 */
    radial-gradient(ellipse 60% 100% at 0% 50%, rgba(166, 64, 41, 0.04) 0%, transparent 50%),
    /* 顶部柔光 */
    radial-gradient(ellipse 80% 40% at 50% 0%, rgba(201, 168, 76, 0.1) 0%, transparent 60%),
    /* 底色 */
    linear-gradient(180deg, #faf6ed 0%, #f5efe4 100%);
  position: relative;

  /* 绢本纹理 */
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background:
      repeating-linear-gradient(
        0deg,
        transparent,
        transparent 28px,
        rgba(140, 126, 116, 0.02) 28px,
        rgba(140, 126, 116, 0.02) 29px
      );
    pointer-events: none;
  }
}

/* 导航栏使用全局 .page-header 规则 */

/* ---- 主体 ---- */
.page-main {
  padding: 28px 60px 60px;
  max-width: 1080px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

/* ---- 返回导览 ---- */
.navigation-bar {
  margin-bottom: 20px;

  .back-guide {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 18px;
    background: rgba(255, 253, 248, 0.9);
    border: 1px solid rgba(201, 168, 76, 0.3);
    border-radius: 24px;
    cursor: pointer;
    transition: all var(--transition);

    &:hover {
      background: #fff;
      border-color: var(--gold);
    }

    .guide-icon {
      font-size: 20px;
      color: var(--text-light);
    }

    .guide-text {
      font-family: var(--font-serif);
      font-size: 13px;
      color: var(--text-color);
      letter-spacing: 2px;
    }
  }
}

/* ---- 藏品鉴赏卷 ---- */
.appreciation-scroll {
  background: linear-gradient(145deg, #fffdf8 0%, #faf8f3 100%);
  border-radius: 8px;
  border: 1px solid rgba(201, 168, 76, 0.25);
  box-shadow:
    0 8px 32px rgba(44, 36, 32, 0.08),
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
    height: 6px;
    background: linear-gradient(90deg,
      rgba(201, 168, 76, 0.4) 0%,
      rgba(166, 64, 41, 0.2) 50%,
      rgba(201, 168, 76, 0.4) 100%
    );
  }

  &::before { top: 0; }
  &::after { bottom: 0; }
}

/* 卷首 */
.scroll-prelude {
  padding: 24px 40px 16px;

  .prelude-ornament {
    width: 40px;
    height: 4px;
    margin: 0 auto;
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
      width: 5px;
      height: 5px;
      background: rgba(201, 168, 76, 0.4);
      border-radius: 50%;
    }

    &::before { left: -10px; }
    &::after { right: -10px; }
  }
}

/* 品赏区 */
.appreciation-body {
  display: grid;
  grid-template-columns: 1fr 1.1fr;
  gap: 44px;
  padding: 20px 48px 40px;
}

/* 左卷：影像 */
.visual-scroll {
  .visual-frame {
    position: relative;
    padding: 10px;
    background: linear-gradient(145deg, #fff 0%, #faf8f3 100%);
    border: 1px solid rgba(212, 201, 184, 0.4);
    border-radius: 4px;

    .frame-corner {
      position: absolute;
      width: 14px;
      height: 14px;
      border-color: rgba(166, 64, 41, 0.2);
      border-style: solid;

      &.corner-tl { top: 8px; left: 8px; border-width: 1px 0 0 1px; }
      &.corner-tr { top: 8px; right: 8px; border-width: 1px 1px 0 0; }
      &.corner-bl { bottom: 8px; left: 8px; border-width: 0 0 1px 1px; }
      &.corner-br { bottom: 8px; right: 8px; border-width: 0 1px 1px 0; }
    }
  }

  .visual-image {
    aspect-ratio: 4/3;
    border-radius: 2px;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.6s ease;
    }

    &:hover img {
      transform: scale(1.03);
    }
  }

  .visual-caption {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    margin-top: 16px;

    .caption-label {
      font-size: 12px;
      color: var(--text-light);
      letter-spacing: 2px;
    }

    .caption-seal {
      width: 22px;
      height: 22px;
      border: 1px solid rgba(166, 64, 41, 0.3);
      border-radius: 2px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-family: var(--font-serif);
      font-size: 11px;
      color: rgba(166, 64, 41, 0.5);
      transform: rotate(-5deg);
    }
  }
}

/* 右卷：铭识 */
.inscription-scroll {
  padding-top: 8px;

  .inscription-header {
    margin-bottom: 24px;
    padding-bottom: 20px;
    border-bottom: 1px solid rgba(212, 201, 184, 0.3);

    .header-category {
      font-size: 12px;
      color: var(--text-light);
      letter-spacing: 3px;
      margin-bottom: 8px;
      text-transform: uppercase;
    }

    .header-title {
      font-family: var(--font-serif);
      font-size: 28px;
      font-weight: 700;
      color: var(--text-color);
      letter-spacing: 3px;
      margin-bottom: 12px;
      line-height: 1.4;
    }

    .header-level {
      .level-badge {
        display: inline-block;
        padding: 4px 12px;
        background: rgba(201, 168, 76, 0.1);
        border: 1px solid rgba(201, 168, 76, 0.25);
        border-radius: 2px;
        font-size: 11px;
        color: rgba(180, 148, 66, 0.8);
        letter-spacing: 2px;
      }
    }
  }

  .inscription-records {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
    margin-bottom: 24px;
    padding: 16px;
    background: rgba(140, 126, 116, 0.03);
    border-radius: 4px;

    .record-item {
      text-align: center;

      .record-label {
        display: block;
        font-size: 11px;
        color: var(--text-light);
        letter-spacing: 2px;
        margin-bottom: 6px;
      }

      .record-value {
        font-family: var(--font-serif);
        font-size: 14px;
        color: var(--text-color);
        letter-spacing: 1px;
      }
    }
  }

  .inscription-abstract {
    margin-bottom: 28px;

    .abstract-title {
      font-family: var(--font-serif);
      font-size: 13px;
      color: var(--text-light);
      letter-spacing: 3px;
      margin-bottom: 12px;
      padding-left: 12px;
      border-left: 2px solid rgba(201, 168, 76, 0.4);
    }

    .abstract-text {
      font-size: 14px;
      color: var(--text-secondary);
      line-height: 1.9;
      text-align: justify;
    }
  }

  .inscription-actions {
    display: flex;
    gap: 12px;

    .action-primary {
      padding: 12px 28px;
      background: linear-gradient(135deg, rgba(166, 64, 41, 0.9) 0%, rgba(140, 50, 30, 0.95) 100%);
      border: none;
      border-radius: 4px;
      color: #fff;
      font-family: var(--font-serif);
      font-size: 14px;
      letter-spacing: 3px;
      cursor: pointer;
      transition: all var(--transition);
      box-shadow: 0 4px 12px rgba(166, 64, 41, 0.25);

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(166, 64, 41, 0.35);
      }
    }

    .action-secondary {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px 24px;
      background: transparent;
      border: 1px solid rgba(212, 201, 184, 0.5);
      border-radius: 4px;
      font-family: var(--font-serif);
      font-size: 14px;
      color: var(--text-color);
      letter-spacing: 2px;
      cursor: pointer;
      transition: all var(--transition);

      &:hover {
        border-color: var(--gold);
        background: rgba(201, 168, 76, 0.05);
      }

      .el-icon {
        font-size: 16px;
        color: rgba(201, 168, 76, 0.7);
      }
    }
  }
}

/* 详述卷 */
.detail-sections {
  padding: 0 48px 40px;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.detail-section {
  .section-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid rgba(212, 201, 184, 0.25);

    .header-icon {
      width: 28px;
      height: 28px;
      border: 1px solid rgba(166, 64, 41, 0.25);
      border-radius: 3px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-family: var(--font-serif);
      font-size: 13px;
      color: rgba(166, 64, 41, 0.6);
    }

    .header-name {
      font-family: var(--font-serif);
      font-size: 18px;
      font-weight: 600;
      color: var(--text-color);
      letter-spacing: 3px;
    }
  }

  .section-body {
    font-size: 15px;
    color: var(--text-secondary);
    line-height: 2;
    padding-left: 40px;

    :deep(p) {
      margin-bottom: 16px;
    }
  }
}

/* 卷尾 */
.scroll-finale {
  padding: 32px 48px 40px;

  .finale-ornament {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16px;

    .ornament-line {
      width: 60px;
      height: 1px;
      background: linear-gradient(90deg, transparent, rgba(201, 168, 76, 0.3), transparent);
    }

    .ornament-seal {
      padding: 6px 16px;
      border: 1px solid rgba(166, 64, 41, 0.2);
      border-radius: 3px;
      font-family: var(--font-serif);
      font-size: 12px;
      color: rgba(166, 64, 41, 0.5);
      letter-spacing: 3px;
      transform: rotate(-2deg);
    }
  }
}
</style>
