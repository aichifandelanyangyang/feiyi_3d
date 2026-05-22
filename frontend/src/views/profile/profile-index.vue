<template>
  <div class="profile-container">
    <!-- 头部导航 -->
    <header class="page-header">
      <div class="logo" @click="router.push('/')">
        <img src="../../../public/imgs/logo1.jpg" alt="logo" class="logo-img" />
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
            <div class="user-info-btn">
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
      <!-- 左侧：文牒目录 -->
      <aside class="archive-sidebar">
        <div class="sidebar-header">
          <div class="archive-seal">文牒</div>
          <div class="archive-title">我的数字档案</div>
          <div class="archive-subtitle">Digital Archive</div>
        </div>

        <nav class="archive-nav">
          <div
            v-for="item in archiveMenu"
            :key="item.key"
            class="nav-item"
            :class="{ active: activeSection === item.key }"
            @click="activeSection = item.key"
          >
            <div class="nav-icon"><el-icon><component :is="item.icon" /></el-icon></div>
            <div class="nav-content">
              <span class="nav-label">{{ item.label }}</span>
              <span class="nav-desc">{{ item.desc }}</span>
            </div>
            <div class="nav-index">{{ item.index }}</div>
          </div>
        </nav>

        <div class="sidebar-footer">
          <div class="footer-line"></div>
          <div class="footer-text">非遗3D数字化平台</div>
        </div>
      </aside>

      <!-- 右侧：内容卷轴 -->
      <div class="content-scroll">
        <!-- 身份印鉴区 -->
        <div class="identity-seal">
          <div class="seal-left">
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :http-request="handleAvatarUpload"
              accept="image/*"
            >
              <div class="avatar-ring">
                <el-avatar :size="72" :src="userStore.avatar || undefined" class="user-avatar">
                  {{ userStore.username?.charAt(0)?.toUpperCase() }}
                </el-avatar>
                <div class="avatar-hint"><el-icon><Camera /></el-icon></div>
              </div>
            </el-upload>
            <div class="identity-info">
              <div class="info-row">
                <span class="info-label">持牒人</span>
                <span class="info-value">{{ userStore.username }}</span>
              </div>
              <div class="info-row" v-if="profileInfo.realName">
                <span class="info-label">实名</span>
                <span class="info-value">{{ profileInfo.realName }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">身份</span>
                <span class="info-badge">{{ profileInfo.roleName || '普通访客' }}</span>
              </div>
            </div>
          </div>
          <div class="seal-right">
            <div class="stamp-mark">
              <div class="stamp-inner">已认证</div>
            </div>
          </div>
        </div>

        <!-- 内容笺纸 -->
        <div class="content-paper">
          <div class="paper-corner paper-corner-tl"></div>
          <div class="paper-corner paper-corner-tr"></div>
          <div class="paper-corner paper-corner-bl"></div>
          <div class="paper-corner paper-corner-br"></div>

          <!-- 个人信息 -->
          <div v-show="activeSection === 'info'" class="section-content">
            <div class="section-header">
              <h3 class="section-name">个人笺档</h3>
              <p class="section-note">完善档案信息，留存数字足迹</p>
            </div>
            <div class="form-container">
              <el-form :model="profileForm" label-position="top">
                <div class="form-row">
                  <el-form-item label="档案编号（用户名）" class="form-item-half">
                    <el-input :value="profileInfo.username" disabled>
                      <template #prefix><el-icon><Document /></el-icon></template>
                    </el-input>
                  </el-form-item>
                  <el-form-item label="真实姓名" class="form-item-half">
                    <el-input v-model="profileForm.realName" placeholder="请输入真实姓名">
                      <template #prefix><el-icon><User /></el-icon></template>
                    </el-input>
                  </el-form-item>
                </div>
                <div class="form-row">
                  <el-form-item label="联系电话" class="form-item-half">
                    <el-input v-model="profileForm.phone" placeholder="请输入手机号">
                      <template #prefix><el-icon><Phone /></el-icon></template>
                    </el-input>
                  </el-form-item>
                  <el-form-item label="电子邮箱" class="form-item-half">
                    <el-input v-model="profileForm.email" placeholder="请输入邮箱">
                      <template #prefix><el-icon><Message /></el-icon></template>
                    </el-input>
                  </el-form-item>
                </div>
                <el-form-item>
                  <el-button type="primary" :loading="saveLoading" @click="handleSaveProfile" class="save-btn">
                    <el-icon><Check /></el-icon> 存档更新
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>

          <!-- 我的收藏 -->
          <div v-show="activeSection === 'favorites'" class="section-content">
            <div class="section-header">
              <h3 class="section-name">藏品目录</h3>
              <p class="section-note">收藏的非遗典藏，共 {{ favTotal }} 件</p>
            </div>
            <div v-loading="favLoading" class="collection-grid">
              <div v-if="favoriteList.length === 0 && !favLoading" class="empty-state">
                <div class="empty-illustration">
                  <div class="empty-scroll"></div>
                </div>
                <p class="empty-text">暂无藏品</p>
                <p class="empty-hint">浏览非遗典藏，收藏感兴趣的珍品</p>
                <el-button type="primary" text @click="router.push('/heritage-list')">去探索 →</el-button>
              </div>
              <div
                v-for="item in favoriteList"
                :key="item.id"
                class="collection-item"
                @click="router.push(`/heritage-detail/${item.heritageId}`)"
              >
                <div class="item-frame">
                  <div class="item-image">
                    <el-image v-if="item.coverImage" :src="item.coverImage" fit="cover" />
                    <div v-else class="image-placeholder">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </div>
                  <div class="item-meta">
                    <h4>{{ item.heritageName }}</h4>
                    <div class="meta-tags">
                      <span v-if="item.categoryName" class="meta-tag">{{ item.categoryName }}</span>
                      <span v-if="item.level" class="meta-tag level">{{ item.level }}</span>
                    </div>
                    <p v-if="item.region" class="meta-region">{{ item.region }}</p>
                  </div>
                  <button class="remove-btn" @click.stop="handleRemoveFav(item)">
                    <el-icon><Close /></el-icon>
                  </button>
                </div>
              </div>
            </div>
            <div v-if="favTotal > favPageSize" class="page-turn">
              <el-pagination
                v-model:current-page="favPageNum"
                :page-size="favPageSize"
                :total="favTotal"
                layout="prev, pager, next"
                @current-change="fetchFavorites"
              />
            </div>
          </div>

          <!-- 我的帖子 -->
          <div v-show="activeSection === 'posts'" class="section-content">
            <div class="section-header">
              <h3 class="section-name">言论辑录</h3>
              <p class="section-note">发表的帖子与讨论，共 {{ postTotal }} 篇</p>
            </div>
            <div v-loading="postLoading" class="manuscript-list">
              <div v-if="myPosts.length === 0 && !postLoading" class="empty-state">
                <div class="empty-illustration">
                  <div class="empty-brush"></div>
                </div>
                <p class="empty-text">暂无言论</p>
                <p class="empty-hint">参与社区讨论，分享非遗见解</p>
                <el-button type="primary" text @click="router.push('/community')">去交流 →</el-button>
              </div>
              <div
                v-for="post in myPosts"
                :key="post.id"
                class="manuscript-item"
                @click="router.push(`/community/${post.id}`)"
              >
                <div class="item-ribbon">
                  <el-tag :type="statusType(post.status)" size="small" effect="plain">{{ statusText(post.status) }}</el-tag>
                </div>
                <div class="item-body">
                  <h4>{{ post.title }}</h4>
                  <p class="item-excerpt">{{ post.content }}</p>
                  <div class="item-footer">
                    <span class="item-date">{{ formatTime(post.createTime) }}</span>
                    <div class="item-stats">
                      <span><el-icon><View /></el-icon>{{ post.viewCount || 0 }}</span>
                      <span><span class="heart-icon"><svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg></span>{{ post.likeCount || 0 }}</span>
                    </div>
                  </div>
                </div>
                <div v-if="post.status === 2 && post.rejectReason" class="item-reject">
                  <el-alert :title="`拒绝原因：${post.rejectReason}`" type="error" :closable="false" show-icon />
                </div>
              </div>
            </div>
            <div v-if="postTotal > postPageSize" class="page-turn">
              <el-pagination
                v-model:current-page="postPageNum"
                :page-size="postPageSize"
                :total="postTotal"
                layout="prev, pager, next"
                @current-change="fetchMyPosts"
              />
            </div>
          </div>

          <!-- 收藏帖子 -->
          <div v-show="activeSection === 'postFavorites'" class="section-content">
            <div class="section-header">
              <h3 class="section-name">藏帖雅集</h3>
              <p class="section-note">收藏的社区帖子，共 {{ postFavTotal }} 篇</p>
            </div>
            <div v-loading="postFavLoading" class="manuscript-list">
              <div v-if="postFavList.length === 0 && !postFavLoading" class="empty-state">
                <div class="empty-illustration">
                  <div class="empty-scroll"></div>
                </div>
                <p class="empty-text">暂无藏帖</p>
                <p class="empty-hint">浏览社区讨论，收藏感兴趣的帖子</p>
                <el-button type="primary" text @click="router.push('/community')">去探索 →</el-button>
              </div>
              <div
                v-for="item in postFavList"
                :key="item.id"
                class="manuscript-item"
                @click="router.push(`/community/${item.postId}`)"
              >
                <div class="item-body">
                  <h4>{{ item.postTitle }}</h4>
                  <p class="item-excerpt">{{ item.postContent }}</p>
                  <div class="item-footer">
                    <span class="item-author">
                      <el-avatar :size="20" :src="item.authorAvatar || undefined">
                        {{ (item.authorName || '?').charAt(0).toUpperCase() }}
                      </el-avatar>
                      {{ item.authorName }}
                    </span>
                    <div class="item-stats">
                      <span><el-icon><View /></el-icon>{{ item.viewCount || 0 }}</span>
                      <span><span class="heart-icon"><svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg></span>{{ item.likeCount || 0 }}</span>
                    </div>
                  </div>
                </div>
                <button class="remove-btn" @click.stop="handleRemovePostFav(item)">
                  <el-icon><Close /></el-icon>
                </button>
              </div>
            </div>
            <div v-if="postFavTotal > postFavPageSize" class="page-turn">
              <el-pagination
                v-model:current-page="postFavPageNum"
                :page-size="postFavPageSize"
                :total="postFavTotal"
                layout="prev, pager, next"
                @current-change="fetchPostFavorites"
              />
            </div>
          </div>

          <!-- 点赞帖子 -->
          <div v-show="activeSection === 'postLikes'" class="section-content">
            <div class="section-header">
              <h3 class="section-name">点赞留痕</h3>
              <p class="section-note">点赞的社区帖子，共 {{ postLikeTotal }} 篇</p>
            </div>
            <div v-loading="postLikeLoading" class="manuscript-list">
              <div v-if="postLikeList.length === 0 && !postLikeLoading" class="empty-state">
                <div class="empty-illustration">
                  <div class="empty-scroll"></div>
                </div>
                <p class="empty-text">暂无点赞</p>
                <p class="empty-hint">浏览社区讨论，点赞感兴趣的帖子</p>
                <el-button type="primary" text @click="router.push('/community')">去探索 →</el-button>
              </div>
              <div
                v-for="item in postLikeList"
                :key="item.id"
                class="manuscript-item"
                @click="router.push(`/community/${item.postId}`)"
              >
                <div class="item-body">
                  <h4>{{ item.postTitle }}</h4>
                  <p class="item-excerpt">{{ item.postContent }}</p>
                  <div class="item-footer">
                    <span class="item-author">
                      <el-avatar :size="20" :src="item.authorAvatar || undefined">
                        {{ (item.authorName || '?').charAt(0).toUpperCase() }}
                      </el-avatar>
                      {{ item.authorName }}
                    </span>
                    <div class="item-stats">
                      <span><el-icon><View /></el-icon>{{ item.viewCount || 0 }}</span>
                      <span><span class="heart-icon"><svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg></span>{{ item.likeCount || 0 }}</span>
                    </div>
                  </div>
                </div>
                <button class="remove-btn" @click.stop="handleRemovePostLike(item)">
                  <el-icon><Close /></el-icon>
                </button>
              </div>
            </div>
            <div v-if="postLikeTotal > postLikePageSize" class="page-turn">
              <el-pagination
                v-model:current-page="postLikePageNum"
                :page-size="postLikePageSize"
                :total="postLikeTotal"
                layout="prev, pager, next"
                @current-change="fetchPostLikes"
              />
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Camera, View, Star, Document, User, Phone, Message, Check,
  Picture, Close, Collection, ChatLineRound, EditPen, Promotion
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store'
import { getUserInfo, updateProfile, uploadImage, getFavoriteList, removeFavorite } from '@/api/user'
import { getMyPosts, getPostFavoriteList, removePostFavorite, getPostLikeList, removePostLike } from '@/api/community'

const router = useRouter()
const userStore = useUserStore()

// === 文牒导航菜单 ===
const activeSection = ref('info')
const archiveMenu = [
  { key: 'info', label: '个人笺档', desc: '档案信息管理', icon: 'Document', index: '壹' },
  { key: 'favorites', label: '藏品目录', desc: '收藏的非遗珍品', icon: 'Collection', index: '贰' },
  { key: 'posts', label: '言论辑录', desc: '发表的讨论帖子', icon: 'ChatLineRound', index: '叁' },
  { key: 'postFavorites', label: '藏帖雅集', desc: '收藏的社区帖子', icon: 'EditPen', index: '肆' },
  { key: 'postLikes', label: '点赞留痕', desc: '点赞的社区帖子', icon: 'Promotion', index: '伍' }
]

// === 个人信息 ===
const profileInfo = ref({})
const profileForm = reactive({ realName: '', phone: '', email: '' })
const saveLoading = ref(false)

const fetchProfile = async () => {
  try {
    const res = await getUserInfo()
    if (res.data) {
      profileInfo.value = res.data
      profileForm.realName = res.data.realName || ''
      profileForm.phone = res.data.phone || ''
      profileForm.email = res.data.email || ''
      // 同步store
      userStore.setUserInfo(res.data)
    }
  } catch (e) {
    console.error('获取个人信息失败', e)
  }
}

const handleSaveProfile = async () => {
  saveLoading.value = true
  try {
    await updateProfile(profileForm)
    ElMessage.success('保存成功')
    await fetchProfile()
  } catch (e) {
    // handled by interceptor
  } finally {
    saveLoading.value = false
  }
}

const handleAvatarUpload = async (options) => {
  try {
    const res = await uploadImage(options.file)
    const url = res.data.url
    await updateProfile({ avatar: url })
    userStore.updateAvatar(url)
    profileInfo.value.avatar = url
    ElMessage.success('头像更新成功')
  } catch (e) {
    ElMessage.error('头像上传失败')
  }
}

// === 我的收藏 ===
const favLoading = ref(false)
const favoriteList = ref([])
const favPageNum = ref(1)
const favPageSize = ref(12)
const favTotal = ref(0)

const fetchFavorites = async () => {
  favLoading.value = true
  try {
    const res = await getFavoriteList({ pageNum: favPageNum.value, pageSize: favPageSize.value })
    favoriteList.value = res.data?.list || []
    favTotal.value = res.data?.total || 0
  } catch (e) {
    console.error('获取收藏失败', e)
  } finally {
    favLoading.value = false
  }
}

const handleRemoveFav = (item) => {
  ElMessageBox.confirm(`确定取消收藏「${item.heritageName}」？`, '提示').then(async () => {
    await removeFavorite(item.heritageId)
    ElMessage.success('已取消收藏')
    fetchFavorites()
  }).catch(() => {})
}

// === 我的帖子 ===
const postLoading = ref(false)
const myPosts = ref([])
const postPageNum = ref(1)
const postPageSize = ref(10)
const postTotal = ref(0)

const statusText = (s) => ({ 0: '待审核', 1: '已通过', 2: '已拒绝' }[s] || '未知')
const statusType = (s) => ({ 0: 'warning', 1: 'success', 2: 'danger' }[s] || 'info')
const formatTime = (t) => t ? t.replace('T', ' ').substring(0, 16) : ''

const fetchMyPosts = async () => {
  postLoading.value = true
  try {
    const res = await getMyPosts({ pageNum: postPageNum.value, pageSize: postPageSize.value })
    myPosts.value = res.data?.list || []
    postTotal.value = res.data?.total || 0
  } catch (e) {
    console.error('获取帖子失败', e)
  } finally {
    postLoading.value = false
  }
}

const handleUserCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/')
  } else if (command === 'profile') {
    // already on this page
  }
}

// === 收藏帖子 ===
const postFavLoading = ref(false)
const postFavList = ref([])
const postFavPageNum = ref(1)
const postFavPageSize = ref(10)
const postFavTotal = ref(0)

const fetchPostFavorites = async () => {
  postFavLoading.value = true
  try {
    const res = await getPostFavoriteList({ pageNum: postFavPageNum.value, pageSize: postFavPageSize.value })
    postFavList.value = res.data?.list || []
    postFavTotal.value = res.data?.total || 0
  } catch (e) {
    console.error('获取收藏帖子失败', e)
  } finally {
    postFavLoading.value = false
  }
}

const handleRemovePostFav = (item) => {
  ElMessageBox.confirm(`确定取消收藏「${item.postTitle}」？`, '提示').then(async () => {
    await removePostFavorite(item.postId)
    ElMessage.success('已取消收藏')
    fetchPostFavorites()
  }).catch(() => {})
}

// === 点赞帖子 ===
const postLikeLoading = ref(false)
const postLikeList = ref([])
const postLikePageNum = ref(1)
const postLikePageSize = ref(10)
const postLikeTotal = ref(0)

const fetchPostLikes = async () => {
  postLikeLoading.value = true
  try {
    const res = await getPostLikeList({ pageNum: postLikePageNum.value, pageSize: postLikePageSize.value })
    postLikeList.value = res.data?.list || []
    postLikeTotal.value = res.data?.total || 0
  } catch (e) {
    console.error('获取点赞帖子失败', e)
  } finally {
    postLikeLoading.value = false
  }
}

const handleRemovePostLike = (item) => {
  ElMessageBox.confirm(`确定取消点赞「${item.postTitle}」？`, '提示').then(async () => {
    await removePostLike(item.postId)
    ElMessage.success('已取消点赞')
    fetchPostLikes()
  }).catch(() => {})
}

onMounted(() => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  fetchProfile()
  fetchFavorites()
  fetchMyPosts()
  fetchPostFavorites()
  fetchPostLikes()
})
</script>

<style scoped lang="scss">
.profile-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f7f3ec 0%, #ede8dc 50%, #e8e2d4 100%);
  position: relative;

  /* 宣纸底纹 */
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background:
      repeating-linear-gradient(
        0deg,
        transparent,
        transparent 3px,
        rgba(140, 126, 116, 0.02) 3px,
        rgba(140, 126, 116, 0.02) 6px
      );
    pointer-events: none;
  }
}

/* 导航栏使用全局 .page-header 规则 */

/* ---- 主体布局：档案室式 ---- */
.page-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 40px 60px;
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 32px;
  position: relative;
  z-index: 1;
}

/* ---- 左侧：文牒目录（深色档案柜） ---- */
.archive-sidebar {
  background: linear-gradient(145deg, #3a332a 0%, #2c2420 100%);
  border-radius: 8px;
  padding: 28px 20px;
  position: relative;
  overflow: hidden;
  box-shadow:
    0 8px 32px rgba(44, 36, 32, 0.18),
    inset 0 1px 0 rgba(255, 255, 255, 0.06);

  /* 木纹质感暗示 */
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background:
      repeating-linear-gradient(
        90deg,
        transparent,
        transparent 2px,
        rgba(201, 168, 76, 0.02) 2px,
        rgba(201, 168, 76, 0.02) 3px
      );
    pointer-events: none;
  }

  .sidebar-header {
    text-align: center;
    padding-bottom: 24px;
    border-bottom: 1px solid rgba(201, 168, 76, 0.15);
    margin-bottom: 24px;
    position: relative;
    z-index: 1;

    .archive-seal {
      width: 56px;
      height: 56px;
      margin: 0 auto 14px;
      border: 2px solid rgba(201, 168, 76, 0.4);
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-family: var(--font-serif);
      font-size: 22px;
      color: var(--gold);
      letter-spacing: 2px;
      background: rgba(201, 168, 76, 0.08);
      box-shadow: 0 0 0 4px rgba(201, 168, 76, 0.04);
    }

    .archive-title {
      font-family: var(--font-serif);
      font-size: 18px;
      color: var(--gold-light);
      letter-spacing: 3px;
      margin-bottom: 6px;
    }

    .archive-subtitle {
      font-size: 11px;
      color: rgba(231, 211, 150, 0.4);
      letter-spacing: 2px;
      text-transform: uppercase;
    }
  }

  .archive-nav {
    display: flex;
    flex-direction: column;
    gap: 10px;
    position: relative;
    z-index: 1;

    .nav-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 16px 14px;
      border-radius: 6px;
      cursor: pointer;
      transition: all var(--transition);
      border: 1px solid transparent;
      position: relative;

      &:hover {
        background: rgba(255, 255, 255, 0.04);
        border-color: rgba(201, 168, 76, 0.15);
      }

      &.active {
        background: rgba(201, 168, 76, 0.1);
        border-color: rgba(201, 168, 76, 0.3);

        .nav-index {
          color: var(--gold);
          border-color: var(--gold);
        }

        .nav-label {
          color: var(--gold-light);
        }

        .nav-icon {
          color: var(--gold);
        }
      }

      .nav-icon {
        width: 36px;
        height: 36px;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.04);
        display: flex;
        align-items: center;
        justify-content: center;
        color: rgba(231, 211, 150, 0.5);
        font-size: 16px;
        transition: all var(--transition);
      }

      .nav-content {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 2px;

        .nav-label {
          font-family: var(--font-serif);
          font-size: 15px;
          color: rgba(255, 253, 248, 0.85);
          letter-spacing: 1px;
          transition: color var(--transition);
        }

        .nav-desc {
          font-size: 11px;
          color: rgba(231, 211, 150, 0.4);
          letter-spacing: 0.5px;
        }
      }

      .nav-index {
        font-family: var(--font-serif);
        font-size: 14px;
        width: 28px;
        height: 28px;
        border: 1px solid rgba(201, 168, 76, 0.2);
        border-radius: 4px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: rgba(231, 211, 150, 0.5);
        transition: all var(--transition);
      }
    }
  }

  .sidebar-footer {
    margin-top: auto;
    padding-top: 24px;
    text-align: center;
    position: relative;
    z-index: 1;

    .footer-line {
      width: 40px;
      height: 1px;
      background: rgba(201, 168, 76, 0.25);
      margin: 0 auto 12px;
    }

    .footer-text {
      font-size: 10px;
      color: rgba(231, 211, 150, 0.3);
      letter-spacing: 2px;
    }
  }
}

/* ---- 右侧：内容卷轴 ---- */
.content-scroll {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 身份印鉴区 */
.identity-seal {
  background: linear-gradient(145deg, #fffdf8 0%, #f5f1e8 100%);
  border-radius: 8px;
  padding: 28px 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow:
    0 4px 20px rgba(44, 36, 32, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(201, 168, 76, 0.2);
  position: relative;

  /* 四角装饰 */
  &::before,
  &::after {
    content: '';
    position: absolute;
    width: 12px;
    height: 12px;
    border-color: rgba(166, 64, 41, 0.2);
    border-style: solid;
  }

  &::before {
    top: 10px;
    left: 10px;
    border-width: 1px 0 0 1px;
  }

  &::after {
    top: 10px;
    right: 10px;
    border-width: 1px 1px 0 0;
  }

  .seal-left {
    display: flex;
    align-items: center;
    gap: 20px;

    .avatar-uploader {
      cursor: pointer;

      .avatar-ring {
        position: relative;
        width: 84px;
        height: 84px;
        border-radius: 50%;
        padding: 4px;
        background: linear-gradient(135deg, rgba(166, 64, 41, 0.15) 0%, rgba(201, 168, 76, 0.25) 100%);

        .user-avatar {
          width: 100%;
          height: 100%;
          border: 2px solid #fff;
        }

        .avatar-hint {
          position: absolute;
          bottom: 0;
          right: 0;
          width: 26px;
          height: 26px;
          background: var(--primary-color);
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #fff;
          font-size: 12px;
          border: 2px solid #fff;
          box-shadow: 0 2px 6px rgba(166, 64, 41, 0.3);
        }
      }
    }

    .identity-info {
      display: flex;
      flex-direction: column;
      gap: 6px;

      .info-row {
        display: flex;
        align-items: center;
        gap: 12px;

        .info-label {
          font-size: 12px;
          color: var(--text-light);
          letter-spacing: 1px;
          min-width: 48px;
        }

        .info-value {
          font-family: var(--font-serif);
          font-size: 16px;
          color: var(--text-color);
          letter-spacing: 1px;
        }

        .info-badge {
          padding: 3px 10px;
          background: rgba(166, 64, 41, 0.08);
          border: 1px solid rgba(166, 64, 41, 0.2);
          border-radius: 3px;
          font-size: 12px;
          color: var(--primary-color);
          letter-spacing: 1px;
        }
      }
    }
  }

  .seal-right {
    .stamp-mark {
      width: 64px;
      height: 64px;
      border: 2px solid rgba(166, 64, 41, 0.25);
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      transform: rotate(-8deg);
      background: rgba(166, 64, 41, 0.04);

      .stamp-inner {
        font-family: var(--font-serif);
        font-size: 14px;
        color: rgba(166, 64, 41, 0.6);
        letter-spacing: 2px;
        writing-mode: vertical-rl;
        text-orientation: upright;
      }
    }
  }
}

/* 内容笺纸 */
.content-paper {
  background: linear-gradient(145deg, #fffdf8 0%, #faf8f3 100%);
  border-radius: 8px;
  border: 1px solid rgba(212, 201, 184, 0.5);
  padding: 36px 40px;
  position: relative;
  box-shadow:
    0 4px 24px rgba(44, 36, 32, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  min-height: 480px;

  /* 纸纹 */
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background:
      repeating-linear-gradient(
        0deg,
        transparent,
        transparent 24px,
        rgba(140, 126, 116, 0.03) 24px,
        rgba(140, 126, 116, 0.03) 25px
      );
    pointer-events: none;
    border-radius: 8px;
  }

  /* 四角装饰 */
  .paper-corner {
    position: absolute;
    width: 16px;
    height: 16px;
    border-color: rgba(166, 64, 41, 0.15);
    border-style: solid;

    &.paper-corner-tl {
      top: 14px;
      left: 14px;
      border-width: 1px 0 0 1px;
    }

    &.paper-corner-tr {
      top: 14px;
      right: 14px;
      border-width: 1px 1px 0 0;
    }

    &.paper-corner-bl {
      bottom: 14px;
      left: 14px;
      border-width: 0 0 1px 1px;
    }

    &.paper-corner-br {
      bottom: 14px;
      right: 14px;
      border-width: 0 1px 1px 0;
    }
  }

  .section-content {
    position: relative;
    z-index: 1;
  }

  .section-header {
    text-align: center;
    margin-bottom: 32px;
    padding-bottom: 24px;
    border-bottom: 1px solid rgba(212, 201, 184, 0.4);

    .section-name {
      font-family: var(--font-serif);
      font-size: 24px;
      color: var(--text-color);
      letter-spacing: 4px;
      margin-bottom: 8px;
    }

    .section-note {
      font-size: 13px;
      color: var(--text-light);
      letter-spacing: 1px;
    }
  }
}

/* ---- 表单 ---- */
.form-container {
  max-width: 600px;
  margin: 0 auto;

  .form-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
    margin-bottom: 8px;
  }

  :deep(.el-form-item__label) {
    font-family: var(--font-serif);
    font-size: 13px;
    color: var(--text-secondary);
    letter-spacing: 1px;
    padding-bottom: 8px;
  }

  :deep(.el-input__wrapper) {
    background: rgba(255, 253, 248, 0.8);
    border: 1px solid rgba(212, 201, 184, 0.4);
    border-radius: 4px;
    box-shadow: none !important;
    padding: 8px 12px;

    &:hover, &.is-focus {
      border-color: var(--gold);
    }
  }

  :deep(.el-input__inner) {
    font-family: var(--font-sans);
    font-size: 14px;
    color: var(--text-color);

    &::placeholder {
      color: var(--text-light);
    }
  }

  :deep(.el-input__prefix) {
    color: var(--text-light);
    margin-right: 8px;
  }

  :deep(.el-input.is-disabled .el-input__inner) {
    color: var(--text-light);
    background: transparent;
  }

  .save-btn {
    padding: 10px 28px;
    font-family: var(--font-serif);
    font-size: 14px;
    letter-spacing: 2px;
    border-radius: 4px;
    margin-top: 8px;
  }
}

/* ---- 藏品目录 ---- */
.collection-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.collection-item {
  cursor: pointer;

  .item-frame {
    background: linear-gradient(145deg, #fff 0%, #faf8f3 100%);
    border: 1px solid rgba(212, 201, 184, 0.5);
    border-radius: 4px;
    padding: 6px;
    position: relative;
    transition: all var(--transition);
    box-shadow: 0 2px 12px rgba(44, 36, 32, 0.04);

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(44, 36, 32, 0.1);
      border-color: rgba(201, 168, 76, 0.4);

      .remove-btn {
        opacity: 1;
      }
    }

    .item-image {
      aspect-ratio: 4/3;
      border-radius: 2px;
      overflow: hidden;
      background: var(--bg-warm);

      .el-image {
        width: 100%;
        height: 100%;
      }

      .image-placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: var(--text-light);
        font-size: 24px;
      }
    }

    .item-meta {
      padding: 12px 8px 8px;

      h4 {
        font-family: var(--font-serif);
        font-size: 14px;
        color: var(--text-color);
        letter-spacing: 1px;
        margin-bottom: 8px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .meta-tags {
        display: flex;
        gap: 6px;
        margin-bottom: 6px;

        .meta-tag {
          font-size: 11px;
          padding: 2px 6px;
          background: rgba(140, 126, 116, 0.08);
          border: 1px solid rgba(140, 126, 116, 0.15);
          border-radius: 2px;
          color: var(--text-light);

          &.level {
            background: rgba(201, 168, 76, 0.1);
            border-color: rgba(201, 168, 76, 0.25);
            color: rgba(180, 148, 66, 0.8);
          }
        }
      }

      .meta-region {
        font-size: 12px;
        color: var(--text-light);
      }
    }

    .remove-btn {
      position: absolute;
      top: 10px;
      right: 10px;
      width: 26px;
      height: 26px;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.9);
      border: 1px solid rgba(212, 201, 184, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      color: var(--text-light);
      font-size: 12px;
      cursor: pointer;
      opacity: 0;
      transition: all var(--transition);

      &:hover {
        background: var(--primary-color);
        border-color: var(--primary-color);
        color: #fff;
      }
    }
  }
}

/* ---- 言论辑录 ---- */
.manuscript-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.manuscript-item {
  background: linear-gradient(145deg, #fff 0%, #faf8f3 100%);
  border: 1px solid rgba(212, 201, 184, 0.4);
  border-radius: 6px;
  padding: 20px 24px;
  position: relative;
  cursor: pointer;
  transition: all var(--transition);

  &:hover {
    border-color: rgba(201, 168, 76, 0.4);
    box-shadow: 0 4px 16px rgba(44, 36, 32, 0.06);
  }

  .item-ribbon {
    position: absolute;
    top: 16px;
    right: 16px;
  }

  .item-body {
    padding-right: 80px;

    h4 {
      font-family: var(--font-serif);
      font-size: 17px;
      color: var(--text-color);
      letter-spacing: 1px;
      margin-bottom: 10px;
      line-height: 1.4;
    }

    .item-excerpt {
      font-size: 14px;
      color: var(--text-secondary);
      line-height: 1.8;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      word-break: break-word;
      margin-bottom: 12px;
    }

    .item-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .item-date {
        font-size: 12px;
        color: var(--text-light);
        letter-spacing: 0.5px;
      }

      .item-author {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 13px;
        color: var(--text-secondary);

        .el-avatar {
          border: 1px solid rgba(212, 201, 184, 0.3);
        }
      }

      .item-stats {
        display: flex;
        gap: 16px;

        span {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 12px;
          color: var(--text-light);

          .heart-icon {
            display: flex;
            align-items: center;
          }
        }
      }
    }
  }

  .item-reject {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px dashed rgba(212, 201, 184, 0.4);
  }

  .remove-btn {
    position: absolute;
    top: 50%;
    right: 16px;
    transform: translateY(-50%);
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.9);
    border: 1px solid rgba(212, 201, 184, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--text-light);
    font-size: 14px;
    cursor: pointer;
    opacity: 0;
    transition: all var(--transition);

    &:hover {
      background: var(--primary-color);
      border-color: var(--primary-color);
      color: #fff;
    }
  }

  &:hover .remove-btn {
    opacity: 1;
  }
}

/* ---- 空状态 ---- */
.empty-state {
  text-align: center;
  padding: 60px 20px;

  .empty-illustration {
    width: 80px;
    height: 80px;
    margin: 0 auto 20px;
    position: relative;

    .empty-scroll {
      width: 100%;
      height: 100%;
      border: 2px solid rgba(212, 201, 184, 0.3);
      border-radius: 4px;
      position: relative;
      background: linear-gradient(145deg, #faf8f3 0%, #f5f1e8 100%);

      &::before {
        content: '';
        position: absolute;
        top: 20%;
        left: 15%;
        right: 15%;
        height: 2px;
        background: rgba(212, 201, 184, 0.3);
        box-shadow:
          0 12px 0 rgba(212, 201, 184, 0.3),
          0 24px 0 rgba(212, 201, 184, 0.3);
      }
    }

    .empty-brush {
      width: 100%;
      height: 100%;
      border: 2px solid rgba(212, 201, 184, 0.3);
      border-radius: 50%;
      position: relative;

      &::before {
        content: '';
        position: absolute;
        top: 30%;
        left: 50%;
        transform: translateX(-50%);
        width: 2px;
        height: 40%;
        background: rgba(212, 201, 184, 0.4);
      }

      &::after {
        content: '';
        position: absolute;
        top: 50%;
        left: 30%;
        width: 40%;
        height: 2px;
        background: rgba(212, 201, 184, 0.4);
      }
    }
  }

  .empty-text {
    font-family: var(--font-serif);
    font-size: 18px;
    color: var(--text-color);
    letter-spacing: 2px;
    margin-bottom: 8px;
  }

  .empty-hint {
    font-size: 13px;
    color: var(--text-light);
    margin-bottom: 16px;
  }
}

/* ---- 分页 ---- */
.page-turn {
  display: flex;
  justify-content: center;
  padding-top: 16px;
  border-top: 1px solid rgba(212, 201, 184, 0.3);
}
</style>
