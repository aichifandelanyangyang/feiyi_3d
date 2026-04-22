<template>
  <!-- 首页容器 -->
  <div class="home-container">
    <!-- 头部导航 start -->
    <header class="home-header" :class="{ 'header-scrolled': isScrolled }">
      <div class="logo" @click="router.push('/')">
        <img src="@/assets/images/logo.svg" alt="logo" class="logo-img" />
        <span class="logo-text">非遗3D数字化交互平台</span>
      </div>
      <nav class="nav-menu">
        <router-link to="/" class="nav-item active">首页</router-link>
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
                <el-dropdown-item command="avatar">修改头像</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button class="login-btn" @click="router.push('/login')">登录</el-button>
        </template>
      </div>

      <!-- 头像上传弹窗 -->
      <el-dialog v-model="avatarDialogVisible" title="修改头像" width="400px" destroy-on-close>
        <div class="avatar-upload-wrapper">
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :http-request="handleAvatarUpload"
            accept="image/*"
          >
            <el-avatar :size="120" :src="previewAvatar || userStore.avatar || undefined" class="avatar-preview">
              {{ userStore.username?.charAt(0)?.toUpperCase() }}
            </el-avatar>
            <div class="upload-tip">点击上传新头像</div>
          </el-upload>
        </div>
        <template #footer>
          <el-button @click="avatarDialogVisible = false">关闭</el-button>
        </template>
      </el-dialog>
    </header>
    <!-- 头部导航 end -->

    <!-- 主体内容 start -->
    <main class="home-main">
      <!-- 英雄区域 start -->
      <section class="hero-section">
        <!-- 装饰纹样 -->
        <div class="hero-ornament hero-ornament--left"></div>
        <div class="hero-ornament hero-ornament--right"></div>
        <div class="hero-overlay"></div>
        <div class="hero-content">
          <div class="hero-badge">非物质文化遗产 · 数字传承</div>
          <h1 class="hero-title">探索非物质文化遗产</h1>
          <p class="hero-subtitle">沉浸式3D体验，感受千年传统文化魅力</p>
          <div class="hero-actions">
            <button class="hero-btn hero-btn--primary" @click="handleEnterHall">
              <span>进入虚拟展厅</span>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
            </button>
            <button class="hero-btn hero-btn--ghost" @click="router.push('/heritage-list')">
              <span>浏览非遗典藏</span>
            </button>
          </div>
        </div>
      </section>
      <!-- 英雄区域 end -->

      <!-- 功能模块 start -->
      <section class="feature-section">
        <div class="section-header">
          <div class="section-ornament">
            <span class="ornament-line"></span>
            <span class="ornament-dot"></span>
            <span class="ornament-line"></span>
          </div>
          <h2 class="section-title">数字展卷</h2>
          <p class="section-subtitle">四重维度，层层深入非遗之美</p>
        </div>
        <div class="feature-scroll">
          <div class="feature-ribbon">
            <div 
              v-for="(item, index) in featureList" 
              :key="item.id" 
              class="feature-scroll-card"
              :style="{ animationDelay: index * 0.12 + 's' }"
            >
              <div class="card-frame">
                <div class="frame-corner frame-corner-tl"></div>
                <div class="frame-corner frame-corner-tr"></div>
                <div class="frame-corner frame-corner-bl"></div>
                <div class="frame-corner frame-corner-br"></div>
                <div class="card-content">
                  <div class="card-header">
                    <span class="card-index">{{ ['壹', '贰', '叁', '肆'][index] }}</span>
                    <div class="card-icon">
                      <component :is="item.icon" />
                    </div>
                  </div>
                  <div class="card-body">
                    <h3 class="card-title">{{ item.title }}</h3>
                    <p class="card-desc">{{ item.desc }}</p>
                  </div>
                  <div class="card-footer">
                    <div class="footer-seal">
                      <span class="seal-inner">数字非遗</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
      <!-- 功能模块 end -->

      <!-- 文化理念区 start -->
      <section class="culture-section">
        <div class="culture-inner">
          <div class="culture-text">
            <h2 class="culture-title">数字化保护<br/>让非遗活起来</h2>
            <p class="culture-desc">运用3D数字化技术，让传统文化跨越时空，走向未来。在这里，每一件非遗作品都以全新的方式呈现其独特的匠心之美。</p>
            <div class="culture-stats">
              <div class="stat-item">
                <span class="stat-num">3D</span>
                <span class="stat-label">沉浸展示</span>
              </div>
              <div class="stat-item">
                <span class="stat-num">360°</span>
                <span class="stat-label">全景漫游</span>
              </div>
              <div class="stat-item">
                <span class="stat-num">∞</span>
                <span class="stat-label">数字传承</span>
              </div>
            </div>
          </div>
        </div>
      </section>
      <!-- 文化理念区 end -->
    </main>
    <!-- 主体内容 end -->

    <!-- 底部 start -->
    <footer class="home-footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <img src="@/assets/images/logo.svg" alt="logo" class="footer-logo" />
          <span class="footer-name">非遗3D数字化交互平台</span>
        </div>
        <div class="footer-divider"></div>
        <p class="footer-copy">以数字之力 · 守护传统之美</p>
      </div>
    </footer>
    <!-- 底部 end -->
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store'
import { uploadImage, updateAvatar } from '@/api/user'
import { View, Collection, Pointer, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const avatarDialogVisible = ref(false)
const previewAvatar = ref('')
const isScrolled = ref(false)

const onScroll = () => {
  isScrolled.value = window.scrollY > 60
}

onMounted(() => window.addEventListener('scroll', onScroll))
onBeforeUnmount(() => window.removeEventListener('scroll', onScroll))

const handleUserCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
  } else if (command === 'avatar') {
    previewAvatar.value = ''
    avatarDialogVisible.value = true
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

const handleAvatarUpload = async (options) => {
  try {
    const res = await uploadImage(options.file)
    const url = res.data.url
    previewAvatar.value = url
    await updateAvatar(url)
    userStore.updateAvatar(url)
    ElMessage.success('头像更新成功')
  } catch (e) {
    ElMessage.error('上传失败')
  }
}

// 功能列表数据
const featureList = ref([
  {
    id: 1,
    title: '虚拟展厅',
    desc: '身临其境的3D展厅漫游体验',
    icon: 'House'
  },
  {
    id: 2,
    title: '非遗展示',
    desc: '丰富的非物质文化遗产项目展示',
    icon: 'Collection'
  },
  {
    id: 3,
    title: '互动体验',
    desc: '多维度交互，深入了解非遗文化',
    icon: 'Pointer'
  },
  {
    id: 4,
    title: '数字保护',
    desc: '运用数字化技术保护传承非遗',
    icon: 'Lock'
  }
])

// 进入展厅方法
const handleEnterHall = () => {
  router.push('/exhibition-hall')
}
</script>

<style scoped lang="scss">
/* ---- 首页容器 ---- */
.home-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-color);
}

/* ---- 头部导航 — 透明 → 滚动后实底 ---- */
.home-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 60px;
  height: 72px;
  background: transparent;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  transition: background var(--transition), box-shadow var(--transition);

  &.header-scrolled {
    background: rgba(255, 253, 248, 0.95);
    backdrop-filter: blur(12px);
    box-shadow: 0 1px 12px rgba(44, 36, 32, 0.08);

    .nav-item { color: var(--text-color); }
    .logo-text { color: var(--primary-color); }
  }

  .logo {
    display: flex;
    align-items: center;
    gap: 12px;
    cursor: pointer;

    .logo-img {
      width: 38px;
      height: 38px;
      filter: drop-shadow(0 1px 3px rgba(0,0,0,0.15));
    }

    .logo-text {
      font-family: var(--font-serif);
      font-size: 19px;
      font-weight: 700;
      color: #fff;
      letter-spacing: 2px;
      transition: color var(--transition);
    }
  }

  .nav-menu {
    display: flex;
    gap: 36px;

    .nav-item {
      font-size: 15px;
      color: rgba(255, 255, 255, 0.85);
      text-decoration: none;
      transition: color var(--transition);
      position: relative;
      letter-spacing: 1px;

      &::after {
        content: '';
        position: absolute;
        bottom: -4px;
        left: 50%;
        transform: translateX(-50%) scaleX(0);
        width: 20px;
        height: 2px;
        background: var(--gold);
        border-radius: 1px;
        transition: transform var(--transition);
      }

      &:hover,
      &.active {
        color: var(--gold-light);

        &::after {
          transform: translateX(-50%) scaleX(1);
        }
      }
    }
  }

  .login-btn {
    background: transparent;
    border: 1px solid rgba(255, 255, 255, 0.5);
    color: #fff;
    border-radius: var(--radius-sm);
    padding: 6px 24px;
    letter-spacing: 2px;
    transition: all var(--transition);

    &:hover {
      background: rgba(255, 255, 255, 0.15);
      border-color: #fff;
    }
  }

  .user-area {
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 4px 10px;
      border-radius: var(--radius-sm);
      transition: background var(--transition);

      &:hover { background: rgba(255, 255, 255, 0.12); }

      .user-name {
        font-size: 14px;
        color: rgba(255, 255, 255, 0.9);
        max-width: 100px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }

  &.header-scrolled .user-area .user-info {
    &:hover { background: rgba(44, 36, 32, 0.05); }
    .user-name { color: var(--text-color); }
  }

  &.header-scrolled .login-btn {
    color: var(--primary-color);
    border-color: var(--primary-color);

    &:hover {
      background: var(--primary-color);
      color: #fff;
    }
  }
}

.avatar-upload-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;

  .avatar-uploader {
    cursor: pointer;
    text-align: center;
  }

  .avatar-preview {
    border: 3px dashed var(--border-color);
    transition: border-color var(--transition);

    &:hover { border-color: var(--primary-color); }
  }

  .upload-tip {
    margin-top: 12px;
    font-size: 13px;
    color: var(--text-light);
  }
}

/* ---- 主体区域 ---- */
.home-main {
  flex: 1;
}

/* ---- 英雄区域 ---- */
.hero-section {
  height: 100vh;
  min-height: 660px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background: url('/imgs/首页背景1.png') center/cover no-repeat;
  overflow: hidden;

  .hero-overlay {
    position: absolute;
    inset: 0;
    background: linear-gradient(
      160deg,
      rgba(44, 36, 32, 0.75) 0%,
      rgba(122, 46, 28, 0.55) 50%,
      rgba(44, 36, 32, 0.7) 100%
    );
    z-index: 1;
  }

  .hero-ornament {
    position: absolute;
    width: 280px;
    height: 280px;
    border: 1px solid rgba(201, 168, 76, 0.15);
    border-radius: 50%;
    z-index: 2;
    pointer-events: none;

    &--left {
      left: -80px;
      bottom: -60px;
      animation: floatSlow 8s ease-in-out infinite;
    }

    &--right {
      right: -60px;
      top: -40px;
      width: 200px;
      height: 200px;
      animation: floatSlow 6s ease-in-out infinite reverse;
    }
  }

  .hero-content {
    position: relative;
    z-index: 3;
    text-align: center;
    max-width: 720px;
    padding: 0 24px;
    animation: fadeInUp 1s ease-out;

    .hero-badge {
      display: inline-block;
      padding: 8px 28px;
      border: 1px solid var(--gold);
      color: var(--gold-light);
      font-size: 13px;
      letter-spacing: 4px;
      border-radius: 2px;
      margin-bottom: 32px;
      animation: fadeIn 1.2s ease-out;
    }

    .hero-title {
      font-family: var(--font-serif);
      font-size: 52px;
      font-weight: 900;
      color: #fff;
      line-height: 1.3;
      margin-bottom: 20px;
      letter-spacing: 6px;
      text-shadow: 0 2px 20px rgba(0, 0, 0, 0.3);
    }

    .hero-subtitle {
      font-size: 18px;
      color: rgba(255, 255, 255, 0.8);
      margin-bottom: 48px;
      letter-spacing: 3px;
      line-height: 1.6;
    }

    .hero-actions {
      display: flex;
      gap: 20px;
      justify-content: center;
    }
  }
}

/* 英雄区域按钮 */
.hero-btn {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 16px 40px;
  font-size: 16px;
  font-family: var(--font-sans);
  letter-spacing: 2px;
  border: none;
  cursor: pointer;
  transition: all var(--transition);
  position: relative;
  overflow: hidden;

  &--primary {
    background: var(--primary-color);
    color: #fff;
    border-radius: var(--radius-sm);

    &:hover {
      background: var(--primary-light);
      transform: translateY(-2px);
      box-shadow: 0 8px 32px rgba(166, 64, 41, 0.35);
    }

    svg {
      transition: transform var(--transition);
    }

    &:hover svg {
      transform: translateX(4px);
    }
  }

  &--ghost {
    background: transparent;
    color: rgba(255, 255, 255, 0.85);
    border: 1px solid rgba(255, 255, 255, 0.35);
    border-radius: var(--radius-sm);

    &:hover {
      background: rgba(255, 255, 255, 0.1);
      border-color: rgba(255, 255, 255, 0.6);
      color: #fff;
    }
  }
}

/* ---- 功能模块区域 — 数字展卷 ---- */
.feature-section {
  padding: 90px 0 100px;
  background: linear-gradient(180deg, #f7f3ec 0%, #ede8dc 100%);
  position: relative;
  overflow: hidden;

  /* 宣纸纹理感背景 */
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background:
      repeating-linear-gradient(
        0deg,
        transparent,
        transparent 2px,
        rgba(140, 126, 116, 0.015) 2px,
        rgba(140, 126, 116, 0.015) 4px
      );
    pointer-events: none;
  }

  /* 顶部装饰边 */
  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(90deg,
      transparent 0%,
      rgba(166, 64, 41, 0.3) 20%,
      rgba(201, 168, 76, 0.5) 50%,
      rgba(166, 64, 41, 0.3) 80%,
      transparent 100%
    );
  }

  .section-header {
    text-align: center;
    margin-bottom: 48px;
    position: relative;
    z-index: 1;

    .section-ornament {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12px;
      margin-bottom: 22px;

      .ornament-line {
        width: 60px;
        height: 1px;
        background: linear-gradient(90deg, transparent, rgba(201, 168, 76, 0.6), transparent);
      }

      .ornament-dot {
        width: 6px;
        height: 6px;
        background: var(--gold);
        border-radius: 50%;
        box-shadow: 0 0 0 3px rgba(201, 168, 76, 0.15);
      }
    }

    .section-title {
      font-family: var(--font-serif);
      font-size: 38px;
      font-weight: 700;
      color: var(--text-color);
      letter-spacing: 8px;
      margin-bottom: 14px;
    }

    .section-subtitle {
      font-size: 15px;
      color: var(--text-light);
      letter-spacing: 3px;
    }
  }

  .feature-scroll {
    position: relative;
    z-index: 1;

    .scroll-hint {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12px;
      margin-bottom: 28px;

      .hint-text {
        font-size: 12px;
        color: var(--text-light);
        letter-spacing: 4px;
      }

      .hint-line {
        width: 40px;
        height: 1px;
        background: linear-gradient(90deg, transparent, var(--divider), transparent);
      }
    }
  }

  .feature-ribbon {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 24px;
    max-width: 1240px;
    margin: 0 auto;
    padding: 0 40px;
  }

  /* 展卷式卡片 — 像器物说明牌 */
  .feature-scroll-card {
    animation: fadeInUp 0.8s ease-out both;

    .card-frame {
      background: linear-gradient(145deg, #fffdf8 0%, #f5f1e8 100%);
      border: 1px solid rgba(201, 168, 76, 0.25);
      border-radius: 4px;
      padding: 8px;
      position: relative;
      box-shadow:
        0 4px 20px rgba(44, 36, 32, 0.06),
        inset 0 1px 0 rgba(255, 255, 255, 0.8);
      transition: all var(--transition);

      /* 四角装饰 — 像装裱 */
      .frame-corner {
        position: absolute;
        width: 16px;
        height: 16px;
        border-color: rgba(166, 64, 41, 0.25);
        border-style: solid;
        transition: all var(--transition);

        &.frame-corner-tl {
          top: 6px;
          left: 6px;
          border-width: 1px 0 0 1px;
        }

        &.frame-corner-tr {
          top: 6px;
          right: 6px;
          border-width: 1px 1px 0 0;
        }

        &.frame-corner-bl {
          bottom: 6px;
          left: 6px;
          border-width: 0 0 1px 1px;
        }

        &.frame-corner-br {
          bottom: 6px;
          right: 6px;
          border-width: 0 1px 1px 0;
        }
      }

      &:hover {
        transform: translateY(-6px);
        box-shadow:
          0 12px 32px rgba(44, 36, 32, 0.12),
          inset 0 1px 0 rgba(255, 255, 255, 0.9);
        border-color: rgba(201, 168, 76, 0.45);

        .frame-corner {
          border-color: rgba(166, 64, 41, 0.45);
          width: 20px;
          height: 20px;
        }

        .card-icon {
          background: var(--primary-color);
          color: #fff;
          border-color: var(--primary-color);
        }

        .card-index {
          color: var(--primary-color);
        }
      }
    }

    .card-content {
      background: rgba(255, 253, 248, 0.95);
      border: 1px solid rgba(212, 201, 184, 0.4);
      border-radius: 2px;
      padding: 28px 24px 24px;
      min-height: 320px;
      display: flex;
      flex-direction: column;
    }

    .card-header {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      margin-bottom: 24px;

      .card-index {
        font-family: var(--font-serif);
        font-size: 42px;
        line-height: 1;
        color: rgba(201, 168, 76, 0.65);
        font-weight: 700;
        letter-spacing: 2px;
        transition: color var(--transition);
      }

      .card-icon {
        width: 48px;
        height: 48px;
        background: rgba(166, 64, 41, 0.06);
        border: 1px solid rgba(166, 64, 41, 0.15);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 22px;
        color: var(--primary-color);
        transition: all var(--transition);
      }
    }

    .card-body {
      flex: 1;

      .card-title {
        font-family: var(--font-serif);
        font-size: 22px;
        color: var(--text-color);
        margin-bottom: 14px;
        letter-spacing: 2px;
        line-height: 1.4;
      }

      .card-desc {
        font-size: 14px;
        color: var(--text-secondary);
        line-height: 1.95;
      }
    }

    .card-footer {
      margin-top: 20px;
      padding-top: 16px;
      border-top: 1px solid rgba(212, 201, 184, 0.4);
      display: flex;
      justify-content: flex-end;

      .footer-seal {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        padding: 4px 10px;
        border: 1px solid rgba(166, 64, 41, 0.25);
        border-radius: 2px;
        background: rgba(166, 64, 41, 0.04);

        .seal-inner {
          font-size: 11px;
          color: rgba(166, 64, 41, 0.7);
          letter-spacing: 2px;
        }
      }
    }
  }
}

/* ---- 文化理念区 ---- */
.culture-section {
  background: var(--bg-dark);
  padding: 100px 60px;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -1px;
    left: 0;
    right: 0;
    height: 60px;
    background: linear-gradient(to bottom, var(--bg-color), transparent);
  }

  .culture-inner {
    max-width: 800px;
    margin: 0 auto;
    text-align: center;
  }

  .culture-title {
    font-family: var(--font-serif);
    font-size: 36px;
    font-weight: 700;
    color: var(--text-on-dark);
    line-height: 1.5;
    letter-spacing: 4px;
    margin-bottom: 24px;
  }

  .culture-desc {
    font-size: 15px;
    color: rgba(231, 211, 150, 0.65);
    line-height: 2;
    letter-spacing: 1px;
    margin-bottom: 56px;
  }

  .culture-stats {
    display: flex;
    justify-content: center;
    gap: 80px;

    .stat-item {
      text-align: center;

      .stat-num {
        display: block;
        font-family: var(--font-serif);
        font-size: 36px;
        font-weight: 700;
        color: var(--gold);
        margin-bottom: 8px;
        letter-spacing: 2px;
      }

      .stat-label {
        font-size: 13px;
        color: rgba(231, 211, 150, 0.5);
        letter-spacing: 3px;
      }
    }
  }
}

/* ---- 页脚 ---- */
.home-footer {
  background: var(--bg-dark-soft);
  padding: 40px 60px;

  .footer-inner {
    max-width: 1200px;
    margin: 0 auto;
    text-align: center;
  }

  .footer-brand {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    margin-bottom: 16px;

    .footer-logo {
      width: 28px;
      height: 28px;
      opacity: 0.7;
    }

    .footer-name {
      font-family: var(--font-serif);
      font-size: 15px;
      color: rgba(231, 211, 150, 0.6);
      letter-spacing: 3px;
    }
  }

  .footer-divider {
    width: 40px;
    height: 1px;
    background: rgba(201, 168, 76, 0.25);
    margin: 0 auto 16px;
  }

  .footer-copy {
    font-size: 12px;
    color: rgba(231, 211, 150, 0.35);
    letter-spacing: 4px;
  }
}
</style>
