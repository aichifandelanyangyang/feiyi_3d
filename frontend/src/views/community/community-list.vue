<template>
  <div class="community-container">
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
      <!-- 雅集堂Header -->
      <div class="salon-header">
        <div class="header-ornament">
          <span class="ornament-line"></span>
          <span class="ornament-cloud">☁</span>
          <span class="ornament-line"></span>
        </div>
        <h1 class="salon-title">雅集清谈</h1>
        <p class="salon-subtitle">以文会友，论道非遗</p>
        <div class="header-actions">
          <div class="search-pavilion">
            <el-input
              v-model="keyword"
              placeholder="寻寻觅觅..."
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
          <button class="compose-btn" @click="handlePublish">
            <span class="btn-icon">✦</span>
            <span class="btn-text">发表高论</span>
          </button>
        </div>
      </div>

      <!-- 流觞曲水 - 帖子列表 -->
      <div v-loading="loading" class="flowing-stream">
        <div v-if="postList.length === 0 && !loading" class="empty-garden">
          <div class="empty-scroll-visual"></div>
          <p class="empty-title">虚席以待</p>
          <p class="empty-desc">暂无高论，君可开先河</p>
        </div>

        <div
          v-for="(post, index) in postList"
          :key="post.id"
          class="dialogue-card"
          :class="{ 'even': index % 2 === 1 }"
          :style="{ animationDelay: index * 0.1 + 's' }"
          @click="goDetail(post.id)"
        >
          <div class="card-ripple"></div>
          <div class="dialogue-content">
            <div class="speaker-row">
              <div class="speaker-avatar">
                <el-avatar :size="44" :src="post.avatar || undefined">
                  {{ (post.username || '?').charAt(0).toUpperCase() }}
                </el-avatar>
                <div class="avatar-halo"></div>
              </div>
              <div class="speaker-info">
                <span class="speaker-name">{{ post.username }}</span>
                <span class="speech-time">{{ formatTime(post.createTime) }}</span>
              </div>
              <div v-if="activeTab === 'my'" class="speech-status">
                <span :class="['status-dot', statusType(post.status)]"></span>
                <span class="status-text">{{ statusText(post.status) }}</span>
              </div>
            </div>

            <div class="speech-body">
              <h3 class="speech-title">{{ post.title }}</h3>
              <p class="speech-text">{{ post.content }}</p>
            </div>

            <div v-if="post.images" class="speech-illustrations">
              <div class="illustration-grid">
                <div
                  v-for="(img, idx) in post.images.split(',').slice(0, 4)"
                  :key="idx"
                  class="illustration-frame"
                  :class="{ 'more': idx === 3 && post.images.split(',').length > 4 }"
                  @click.stop
                >
                  <el-image :src="img" fit="cover" class="illustration-img" />
                  <div v-if="idx === 3 && post.images.split(',').length > 4" class="more-overlay">
                    <span>+{{ post.images.split(',').length - 4 }}</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="dialogue-footer">
              <div class="engagement">
                <span class="engagement-item">
                  <el-icon><View /></el-icon>
                  <span class="count">{{ post.viewCount || 0 }}</span>
                  <span class="label">览</span>
                </span>
                <span class="engagement-item">
                  <el-icon><Star /></el-icon>
                  <span class="count">{{ post.likeCount || 0 }}</span>
                  <span class="label">赞</span>
                </span>
              </div>
              <div class="card-seal">{{ formatTime(post.createTime).split(' ')[0] }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="total > pageSize" class="page-navigator">
        <el-pagination
          v-model:current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="fetchList"
        />
      </div>
    </main>

    <!-- 挥毫弹窗 - 发表帖子 -->
    <el-dialog
      v-model="publishVisible"
      title=""
      width="620px"
      destroy-on-close
      class="compose-dialog"
      :show-close="false"
    >
      <template #header>
        <div class="dialog-header">
          <div class="header-seal">挥毫</div>
          <h3 class="header-title">发表高论</h3>
        </div>
      </template>

      <div class="compose-paper">
        <div class="paper-ruling"></div>
        <el-form ref="formRef" :model="postForm" :rules="postRules">
          <el-form-item prop="title" class="title-item">
            <el-input
              v-model="postForm.title"
              placeholder="题跋..."
              maxlength="100"
              show-word-limit
              class="title-input"
            >
              <template #prefix><span class="input-prefix">题名</span></template>
            </el-input>
          </el-form-item>
          <el-form-item prop="content" class="content-item">
            <el-input
              v-model="postForm.content"
              type="textarea"
              :rows="8"
              placeholder="畅叙幽情，分享你对非遗文化的见解与感悟..."
              maxlength="2000"
              show-word-limit
              class="content-input"
            />
          </el-form-item>
          <el-form-item class="image-item">
            <div class="upload-label">附图</div>
            <el-upload
              :file-list="imageList"
              :http-request="handleImageUpload"
              :on-remove="handleImageRemove"
              accept="image/*"
              list-type="picture-card"
              :limit="9"
              class="elegant-upload"
            >
              <div class="upload-trigger">
                <el-icon><Plus /></el-icon>
                <span>添图</span>
              </div>
            </el-upload>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <div class="dialogue-footer-actions">
          <button class="btn-cancel" @click="publishVisible = false">作罢</button>
          <button class="btn-submit" :loading="submitLoading" @click="handleSubmitPost">
            <span>付梓</span>
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Edit, View, Star, Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/store'
import { getCommunityList, getMyPosts, createPost } from '@/api/community'
import { uploadImage } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const postList = ref([])
const keyword = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const activeTab = ref('all')

const publishVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const postForm = ref({ title: '', content: '', images: '' })
const imageList = ref([])
const uploadedUrls = ref([])

const postRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const statusText = (s) => ({ 0: '待审核', 1: '已通过', 2: '已拒绝' }[s] || '未知')
const statusType = (s) => ({ 0: 'warning', 1: 'success', 2: 'danger' }[s] || 'info')

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

const fetchList = async () => {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value }
    let res
    if (activeTab.value === 'my') {
      res = await getMyPosts(params)
    } else {
      res = await getCommunityList(params)
    }
    postList.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('加载失败', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  fetchList()
}

const handleTabChange = () => {
  pageNum.value = 1
  fetchList()
}

const handlePublish = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  postForm.value = { title: '', content: '', images: '' }
  imageList.value = []
  uploadedUrls.value = []
  publishVisible.value = true
}

const handleImageUpload = async (options) => {
  try {
    const res = await uploadImage(options.file)
    const url = res.data.url
    uploadedUrls.value.push(url)
    imageList.value.push({ name: options.file.name, url })
  } catch (e) {
    ElMessage.error('图片上传失败')
  }
}

const handleImageRemove = (file) => {
  const idx = uploadedUrls.value.indexOf(file.url)
  if (idx > -1) uploadedUrls.value.splice(idx, 1)
}

const handleSubmitPost = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const data = {
        title: postForm.value.title,
        content: postForm.value.content,
        images: uploadedUrls.value.join(',')
      }
      await createPost(data)
      ElMessage.success('发布成功，等待审核')
      publishVisible.value = false
      activeTab.value = 'my'
      fetchList()
    } catch (e) {
      // error handled by interceptor
    } finally {
      submitLoading.value = false
    }
  })
}

const goDetail = (id) => {
  router.push(`/community/${id}`)
}

const handleUserCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

onMounted(() => fetchList())
</script>

<style scoped lang="scss">
.community-container {
  min-height: 100vh;
  background:
    /* 顶部柔和光晕 */
    radial-gradient(ellipse 80% 50% at 50% 0%, rgba(201, 168, 76, 0.12) 0%, transparent 70%),
    /* 左侧微暖光 */
    radial-gradient(ellipse 50% 80% at 0% 50%, rgba(166, 64, 41, 0.06) 0%, transparent 60%),
    /* 底色 */
    linear-gradient(180deg, #faf6ed 0%, #f2ece0 50%, #e8e2d4 100%);
  position: relative;

  /* 宣纸纹理 + 细微颗粒 */
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background:
      /* 水平行线 */
      repeating-linear-gradient(
        0deg,
        transparent,
        transparent 24px,
        rgba(140, 126, 116, 0.03) 24px,
        rgba(140, 126, 116, 0.03) 25px
      ),
      /* 极细颗粒感 */
      url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)' opacity='0.03'/%3E%3C/svg%3E");
    background-size: 100% 100%, 200px 200px;
    pointer-events: none;
    opacity: 0.8;
  }

  /* 顶部装饰线 */
  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 10%;
    right: 10%;
    height: 1px;
    background: linear-gradient(90deg,
      transparent 0%,
      rgba(201, 168, 76, 0.3) 20%,
      rgba(201, 168, 76, 0.5) 50%,
      rgba(201, 168, 76, 0.3) 80%,
      transparent 100%
    );
  }
}

/* 导航栏使用全局 .page-header 规则 */

/* ---- 主体 ---- */
.page-main {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 24px 60px;
  position: relative;
  z-index: 1;
}

/* ---- 雅集堂Header ---- */
.salon-header {
  text-align: center;
  margin-bottom: 40px;
  padding-bottom: 32px;
  border-bottom: 1px solid rgba(201, 168, 76, 0.25);

  .header-ornament {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16px;
    margin-bottom: 20px;

    .ornament-line {
      width: 80px;
      height: 1px;
      background: linear-gradient(90deg, transparent, rgba(201, 168, 76, 0.5), transparent);
    }

    .ornament-cloud {
      font-size: 20px;
      color: rgba(201, 168, 76, 0.6);
    }
  }

  .salon-title {
    font-family: var(--font-serif);
    font-size: 42px;
    font-weight: 700;
    color: var(--text-color);
    letter-spacing: 12px;
    margin-bottom: 12px;
  }

  .salon-subtitle {
    font-size: 15px;
    color: var(--text-light);
    letter-spacing: 4px;
    margin-bottom: 28px;
  }

  .header-actions {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16px;

    .search-pavilion {
      :deep(.el-input__wrapper) {
        background: rgba(255, 253, 248, 0.9);
        border: 1px solid rgba(201, 168, 76, 0.3);
        border-radius: 24px;
        box-shadow: none !important;
        padding: 8px 16px;

        &:hover, &.is-focus {
          border-color: var(--gold);
        }
      }

      :deep(.el-input__inner) {
        font-family: var(--font-serif);
        font-size: 14px;
        color: var(--text-color);

        &::placeholder {
          color: var(--text-light);
        }
      }
    }

    .compose-btn {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 10px 24px;
      background: linear-gradient(135deg, rgba(166, 64, 41, 0.9) 0%, rgba(140, 50, 30, 0.95) 100%);
      border: none;
      border-radius: 24px;
      color: #fff;
      font-family: var(--font-serif);
      font-size: 14px;
      letter-spacing: 2px;
      cursor: pointer;
      transition: all var(--transition);
      box-shadow: 0 4px 16px rgba(166, 64, 41, 0.25);

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(166, 64, 41, 0.35);
      }

      .btn-icon {
        font-size: 16px;
      }
    }
  }
}

/* ---- 流觞曲水 - 帖子列表 ---- */
.flowing-stream {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.empty-garden {
  text-align: center;
  padding: 80px 20px;

  .empty-scroll-visual {
    width: 100px;
    height: 100px;
    margin: 0 auto 24px;
    border: 2px solid rgba(212, 201, 184, 0.4);
    border-radius: 6px;
    position: relative;
    background: linear-gradient(145deg, #faf8f3 0%, #f5f1e8 100%);

    &::before {
      content: '雅';
      position: absolute;
      inset: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      font-family: var(--font-serif);
      font-size: 36px;
      color: rgba(201, 168, 76, 0.3);
    }
  }

  .empty-title {
    font-family: var(--font-serif);
    font-size: 22px;
    color: var(--text-color);
    letter-spacing: 4px;
    margin-bottom: 8px;
  }

  .empty-desc {
    font-size: 14px;
    color: var(--text-light);
    letter-spacing: 1px;
  }
}

/* 对话卡片 - 诗笺式 */
.dialogue-card {
  position: relative;
  background: linear-gradient(145deg, #fffdf8 0%, #faf8f3 100%);
  border-radius: 6px;
  cursor: pointer;
  border: 1px solid rgba(212, 201, 184, 0.35);
  transition: all var(--transition);
  animation: fadeInUp 0.6s ease-out both;
  overflow: hidden;
  /* 诗笺抬格式内边距 */
  padding: 32px 36px 28px;
  /* 微妙投影 */
  box-shadow:
    0 2px 8px rgba(44, 36, 32, 0.04),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);

  &:hover {
    transform: translateY(-3px);
    box-shadow:
      0 8px 24px rgba(44, 36, 32, 0.08),
      inset 0 1px 0 rgba(255, 255, 255, 0.9);
    border-color: rgba(201, 168, 76, 0.45);
  }

  &.even {
    background: linear-gradient(145deg, #faf8f3 0%, #f5f1e8 100%);
  }

  /* 诗笺抬头线 */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 36px;
    right: 36px;
    height: 1px;
    background: linear-gradient(90deg,
      transparent,
      rgba(201, 168, 76, 0.2) 20%,
      rgba(201, 168, 76, 0.3) 50%,
      rgba(201, 168, 76, 0.2) 80%,
      transparent
    );
  }

  /* 右侧朱栏 */
  &::after {
    content: '';
    position: absolute;
    top: 60px;
    right: 36px;
    width: 1px;
    height: calc(100% - 120px);
    background: linear-gradient(180deg,
      transparent,
      rgba(166, 64, 41, 0.1) 20%,
      rgba(166, 64, 41, 0.15) 50%,
      rgba(166, 64, 41, 0.1) 80%,
      transparent
    );
  }
}

.dialogue-content {
  position: relative;
  z-index: 1;
}

.speaker-row {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(212, 201, 184, 0.2);

  .speaker-avatar {
    position: relative;

    /* 诗笺抬头的姓名印效果 */
    &::before {
      content: '';
      position: absolute;
      inset: -3px;
      border: 1px solid rgba(166, 64, 41, 0.15);
      border-radius: 50%;
      transition: all var(--transition);
    }

    &:hover::before {
      border-color: rgba(166, 64, 41, 0.25);
    }
  }

  .speaker-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;

    .speaker-name {
      font-family: var(--font-serif);
      font-size: 16px;
      color: var(--text-color);
      letter-spacing: 2px;
    }

    .speech-time {
      font-size: 12px;
      color: var(--text-light);
      letter-spacing: 0.5px;
    }
  }

  .speech-status {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 4px 10px;
    background: rgba(140, 126, 116, 0.04);
    border-radius: 3px;

    .status-dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;

      &.warning { background: #e6a23c; }
      &.success { background: #67c23a; }
      &.danger { background: #f56c6c; }
    }

    .status-text {
      font-size: 11px;
      color: var(--text-light);
      letter-spacing: 1px;
    }
  }
}

.speech-body {
  margin-bottom: 16px;

  .speech-title {
    font-family: var(--font-serif);
    font-size: 19px;
    font-weight: 600;
    color: var(--text-color);
    line-height: 1.5;
    letter-spacing: 1px;
    margin-bottom: 10px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    word-break: break-word;
  }

  .speech-text {
    font-size: 14px;
    color: var(--text-secondary);
    line-height: 1.85;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
    word-break: break-word;
  }
}

.speech-illustrations {
  margin-bottom: 18px;

  .illustration-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 10px;

    .illustration-frame {
      aspect-ratio: 1;
      border-radius: 4px;
      overflow: hidden;
      border: 1px solid rgba(212, 201, 184, 0.4);
      position: relative;

      &.more::after {
        content: '';
        position: absolute;
        inset: 0;
        background: rgba(44, 36, 32, 0.5);
      }

      .illustration-img {
        width: 100%;
        height: 100%;
      }

      .more-overlay {
        position: absolute;
        inset: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        font-family: var(--font-serif);
        font-size: 18px;
        z-index: 1;
      }
    }
  }
}

.dialogue-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 20px;
  margin-top: 4px;
  /* 落款线 */
  border-top: 1px solid rgba(212, 201, 184, 0.25);
  position: relative;

  /* 落款点 */
  &::before {
    content: '';
    position: absolute;
    top: -2px;
    left: 50%;
    transform: translateX(-50%);
    width: 4px;
    height: 4px;
    background: rgba(201, 168, 76, 0.4);
    border-radius: 50%;
  }

  .engagement {
    display: flex;
    gap: 24px;

    .engagement-item {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      color: var(--text-light);

      .el-icon {
        font-size: 15px;
        color: rgba(140, 126, 116, 0.6);
      }

      .count {
        font-family: var(--font-serif);
        font-weight: 500;
      }

      .label {
        font-size: 12px;
        letter-spacing: 1px;
        opacity: 0.8;
      }
    }
  }

  /* 日期落款 */
  .card-seal {
    font-family: var(--font-serif);
    font-size: 12px;
    color: rgba(140, 126, 116, 0.5);
    letter-spacing: 1px;
  }
}

/* 分页导航 */
.page-navigator {
  display: flex;
  justify-content: center;
  padding-top: 24px;
  border-top: 1px solid rgba(212, 201, 184, 0.25);
}

/* ---- 挥毫弹窗 ---- */
.compose-dialog {
  :deep(.el-dialog) {
    border-radius: 12px;
    background: linear-gradient(145deg, #fffdf8 0%, #f5f1e8 100%);
    box-shadow: 0 24px 60px rgba(44, 36, 32, 0.2);
  }

  :deep(.el-dialog__header) {
    padding: 0;
    margin: 0;
  }

  :deep(.el-dialog__body) {
    padding: 0;
  }

  :deep(.el-dialog__footer) {
    padding: 20px 32px 28px;
    border-top: 1px solid rgba(212, 201, 184, 0.3);
  }

  .dialog-header {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    padding: 28px 32px 20px;
    border-bottom: 1px solid rgba(212, 201, 184, 0.3);

    .header-seal {
      width: 40px;
      height: 40px;
      border: 2px solid rgba(166, 64, 41, 0.4);
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-family: var(--font-serif);
      font-size: 16px;
      color: var(--primary-color);
      transform: rotate(-3deg);
    }

    .header-title {
      font-family: var(--font-serif);
      font-size: 22px;
      color: var(--text-color);
      letter-spacing: 4px;
    }
  }

  .compose-paper {
    position: relative;
    padding: 28px 32px;
    background:
      repeating-linear-gradient(
        0deg,
        transparent,
        transparent 31px,
        rgba(140, 126, 116, 0.06) 31px,
        rgba(140, 126, 116, 0.06) 32px
      );

    .paper-ruling {
      position: absolute;
      top: 28px;
      left: 80px;
      bottom: 28px;
      width: 1px;
      background: rgba(166, 64, 41, 0.1);
    }

    :deep(.el-form-item) {
      margin-bottom: 20px;

      &.title-item {
        margin-bottom: 16px;
      }

      &.content-item {
        margin-bottom: 24px;
      }
    }

    :deep(.el-input__wrapper) {
      background: transparent;
      border: none;
      border-bottom: 1px solid rgba(212, 201, 184, 0.5);
      border-radius: 0;
      box-shadow: none !important;
      padding: 8px 0;

      &:hover, &.is-focus {
        border-color: var(--gold);
      }
    }

    :deep(.el-input__inner) {
      font-family: var(--font-serif);
      font-size: 18px;
      color: var(--text-color);

      &::placeholder {
        color: var(--text-light);
        font-size: 16px;
      }
    }

    :deep(.el-textarea__inner) {
      background: transparent;
      border: none;
      box-shadow: none !important;
      font-family: var(--font-sans);
      font-size: 15px;
      line-height: 32px;
      color: var(--text-color);
      padding: 0;
      resize: none;

      &::placeholder {
        color: var(--text-light);
      }
    }

    .input-prefix {
      font-family: var(--font-serif);
      font-size: 13px;
      color: var(--text-light);
      letter-spacing: 2px;
      margin-right: 12px;
    }

    .upload-label {
      font-family: var(--font-serif);
      font-size: 13px;
      color: var(--text-light);
      letter-spacing: 2px;
      margin-bottom: 10px;
      padding-left: 10px;
    }

    .elegant-upload {
      :deep(.el-upload--picture-card) {
        width: 80px;
        height: 80px;
        border: 1px dashed rgba(212, 201, 184, 0.6);
        border-radius: 4px;
        background: transparent;

        &:hover {
          border-color: var(--gold);
        }
      }

      :deep(.el-upload-list__item) {
        width: 80px;
        height: 80px;
        border-radius: 4px;
        border: 1px solid rgba(212, 201, 184, 0.4);
      }

      .upload-trigger {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 4px;
        color: var(--text-light);
        font-size: 12px;

        .el-icon {
          font-size: 20px;
        }
      }
    }
  }

  .dialogue-footer-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;

    .btn-cancel {
      padding: 10px 24px;
      background: transparent;
      border: 1px solid rgba(212, 201, 184, 0.5);
      border-radius: 4px;
      font-family: var(--font-serif);
      font-size: 14px;
      color: var(--text-light);
      letter-spacing: 2px;
      cursor: pointer;
      transition: all var(--transition);

      &:hover {
        border-color: var(--text-light);
        color: var(--text-color);
      }
    }

    .btn-submit {
      padding: 10px 32px;
      background: linear-gradient(135deg, rgba(166, 64, 41, 0.9) 0%, rgba(140, 50, 30, 0.95) 100%);
      border: none;
      border-radius: 4px;
      font-family: var(--font-serif);
      font-size: 14px;
      color: #fff;
      letter-spacing: 3px;
      cursor: pointer;
      transition: all var(--transition);
      box-shadow: 0 4px 16px rgba(166, 64, 41, 0.25);

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(166, 64, 41, 0.35);
      }
    }
  }
}

@keyframes pulse {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(1.05); }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
