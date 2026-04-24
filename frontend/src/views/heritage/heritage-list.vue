<template>
  <!-- 非遗项目列表页 -->
  <div class="heritage-list-container">
    <!-- 头部导航 start -->
    <header class="page-header">
      <div class="logo" @click="goHome">
        <img src="@/assets/images/logo.svg" alt="logo" class="logo-img" />
        <span class="logo-text">非遗3D数字化交互平台</span>
      </div>
      <nav class="nav-menu">
        <router-link to="/" class="nav-item">首页</router-link>
        <router-link to="/exhibition-hall" class="nav-item">虚拟展厅</router-link>
        <router-link to="/heritage-list" class="nav-item active">非遗典藏</router-link>
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
      <!-- 典藏阁Header -->
      <div class="archive-header">
        <div class="header-ornament">
          <div class="ornament-square"></div>
        </div>
        <h1 class="archive-title">非遗典藏</h1>
        <p class="archive-subtitle">守护传承，珍视每一件文化瑰宝</p>
        <div class="header-divider">
          <span class="divider-line"></span>
          <span class="divider-jewel">◆</span>
          <span class="divider-line"></span>
        </div>
      </div>

      <!-- 鉴选栏 -->
      <div class="selection-bar">
        <div class="selection-inner">
          <div class="search-catalog">
            <el-icon class="search-icon"><Search /></el-icon>
            <el-input 
              v-model="searchKeyword" 
              placeholder="鉴寻珍品..."
              clearable
              @keyup.enter="handleSearch"
            />
          </div>
          <div class="category-catalog">
            <el-select 
              v-model="selectedCategory" 
              placeholder="品类筛选"
              clearable
              @change="handleCategoryChange"
            >
              <el-option 
                v-for="item in categoryList" 
                :key="item.id" 
                :label="item.name" 
                :value="item.id"
              />
            </el-select>
          </div>
          <button class="search-btn" @click="handleSearch">
            <span>鉴选</span>
          </button>
        </div>
      </div>

      <!-- 藏品陈列 -->
      <section class="collection-gallery">
        <div v-if="heritageList.length === 0" class="empty-vault">
          <div class="vault-ornament">珍</div>
          <p class="empty-title">暂无藏品</p>
          <p class="empty-hint">期待新的文化瑰宝入库</p>
        </div>

        <div class="gallery-grid">
          <div 
            v-for="(item, index) in heritageList" 
            :key="item.id" 
            class="artifact-card"
            :class="{ 'featured': index < 3 }"
            :style="{ animationDelay: index * 0.08 + 's' }"
            @click="goDetail(item.id)"
          >
            <div class="artifact-frame">
              <div class="frame-corner frame-tl"></div>
              <div class="frame-corner frame-tr"></div>
              <div class="frame-corner frame-bl"></div>
              <div class="frame-corner frame-br"></div>
              
              <div class="artifact-image">
                <img :src="item.coverImage || defaultImage" :alt="item.name" />
                <div class="image-veil"></div>
              </div>
              
              <div class="artifact-badge">{{ item.categoryName }}</div>
              
              <div class="artifact-content">
                <h3 class="artifact-name">{{ item.name }}</h3>
                <p class="artifact-origin">
                  <el-icon><Location /></el-icon>
                  <span>{{ item.region }}</span>
                </p>
                <div class="artifact-stats">
                  <span class="stat-item">
                    <el-icon><View /></el-icon>
                    <span>{{ item.viewCount || 0 }}</span>
                  </span>
                  <span class="stat-item">
                    <el-icon><Star /></el-icon>
                    <span>{{ item.favoriteCount || 0 }}</span>
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="total > 0" class="pagination-vault">
          <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[8, 16, 24]"
            layout="total, prev, pager, next"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </section>
    </main>
    <!-- 主体内容 end -->
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Location, View, Star } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getHeritageList, getCategoryList } from '@/api/heritage'
import { useUserStore } from '@/store'

const router = useRouter()
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
const defaultImage = 'https://via.placeholder.com/400x300?text=非遗典藏'

// 搜索关键词
const searchKeyword = ref('')
// 选中分类
const selectedCategory = ref('')
// 分页参数
const pageNum = ref(1)
const pageSize = ref(8)
const total = ref(0)

// 分类列表
const categoryList = ref([])

// 非遗项目列表
const heritageList = ref([])

// 返回首页
const goHome = () => {
  router.push('/')
}

// 跳转详情页
const goDetail = (id) => {
  router.push(`/heritage-detail/${id}`)
}

// 搜索方法
const handleSearch = () => {
  pageNum.value = 1
  fetchList()
}

// 分类变更方法
const handleCategoryChange = () => {
  pageNum.value = 1
  fetchList()
}

// 分页大小变更
const handleSizeChange = () => {
  pageNum.value = 1
  fetchList()
}

// 页码变更
const handlePageChange = () => {
  fetchList()
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    const res = await getCategoryList()
    if (res.code === 200) {
      categoryList.value = res.data || []
    }
  } catch (e) {
    console.error('获取分类失败:', e)
  }
}

// 获取列表数据方法
const fetchList = async () => {
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (selectedCategory.value) params.categoryId = selectedCategory.value

    const res = await getHeritageList(params)
    if (res.code === 200 && res.data) {
      heritageList.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    console.error('获取列表失败:', e)
  }
}

// 生命周期
onMounted(() => {
  fetchCategories()
  fetchList()
})
</script>

<style scoped lang="scss">
.heritage-list-container {
  min-height: 100vh;
  background:
    /* 顶部珍贵感金色光晕 */
    radial-gradient(ellipse 100% 60% at 50% 0%, rgba(201, 168, 76, 0.15) 0%, transparent 60%),
    /* 整体暖调底色 */
    linear-gradient(180deg, #faf6ed 0%, #f5efe4 50%, #ede6d8 100%);
  position: relative;

  /* 绢本质感 */
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background:
      /* 极细横线纹理 */
      repeating-linear-gradient(
        0deg,
        transparent,
        transparent 32px,
        rgba(140, 126, 116, 0.025) 32px,
        rgba(140, 126, 116, 0.025) 33px
      );
    pointer-events: none;
  }
}

/* 导航栏使用全局 .page-header 规则 */

/* ---- 主体 ---- */
.page-main {
  padding: 36px 60px 60px;
  max-width: 1280px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

/* ---- 典藏阁Header ---- */
.archive-header {
  text-align: center;
  margin-bottom: 36px;
  padding-bottom: 24px;

  .header-ornament {
    margin-bottom: 16px;

    .ornament-square {
      width: 48px;
      height: 48px;
      margin: 0 auto;
      border: 2px solid rgba(201, 168, 76, 0.4);
      position: relative;

      &::before {
        content: '';
        position: absolute;
        inset: 6px;
        border: 1px solid rgba(166, 64, 41, 0.2);
      }

      &::after {
        content: '藏';
        position: absolute;
        inset: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        font-family: var(--font-serif);
        font-size: 20px;
        color: rgba(201, 168, 76, 0.6);
      }
    }
  }

  .archive-title {
    font-family: var(--font-serif);
    font-size: 40px;
    font-weight: 700;
    color: var(--text-color);
    letter-spacing: 10px;
    margin-bottom: 10px;
  }

  .archive-subtitle {
    font-size: 14px;
    color: var(--text-light);
    letter-spacing: 3px;
    margin-bottom: 20px;
  }

  .header-divider {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16px;

    .divider-line {
      width: 80px;
      height: 1px;
      background: linear-gradient(90deg, transparent, rgba(201, 168, 76, 0.4), transparent);
    }

    .divider-jewel {
      font-size: 10px;
      color: rgba(201, 168, 76, 0.5);
    }
  }
}

/* ---- 鉴选栏 ---- */
.selection-bar {
  background: linear-gradient(145deg, #fffdf8 0%, #faf8f3 100%);
  border: 1px solid rgba(201, 168, 76, 0.25);
  border-radius: 6px;
  padding: 20px 28px;
  margin-bottom: 32px;
  box-shadow: 0 4px 16px rgba(44, 36, 32, 0.04);

  .selection-inner {
    display: flex;
    gap: 16px;
    align-items: center;
  }

  .search-catalog {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 16px;
    background: rgba(255, 253, 248, 0.8);
    border: 1px solid rgba(212, 201, 184, 0.4);
    border-radius: 4px;

    .search-icon {
      font-size: 18px;
      color: var(--text-light);
    }

    :deep(.el-input__wrapper) {
      box-shadow: none !important;
      background: transparent;
      padding: 0;
    }

    :deep(.el-input__inner) {
      font-family: var(--font-serif);
      font-size: 15px;
      color: var(--text-color);

      &::placeholder {
        color: var(--text-light);
      }
    }
  }

  .category-catalog {
    width: 160px;

    :deep(.el-select__wrapper) {
      background: rgba(255, 253, 248, 0.8);
      border: 1px solid rgba(212, 201, 184, 0.4);
      border-radius: 4px;
      box-shadow: none !important;
    }
  }

  .search-btn {
    padding: 10px 28px;
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
}

/* ---- 藏品陈列 ---- */
.collection-gallery {
  .empty-vault {
    text-align: center;
    padding: 100px 20px;

    .vault-ornament {
      width: 80px;
      height: 80px;
      margin: 0 auto 24px;
      border: 2px solid rgba(201, 168, 76, 0.3);
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-family: var(--font-serif);
      font-size: 32px;
      color: rgba(201, 168, 76, 0.4);
    }

    .empty-title {
      font-family: var(--font-serif);
      font-size: 20px;
      color: var(--text-color);
      letter-spacing: 4px;
      margin-bottom: 8px;
    }

    .empty-hint {
      font-size: 13px;
      color: var(--text-light);
    }
  }

  .gallery-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 28px;
    margin-bottom: 40px;
  }

  /* 藏品卡片 - 如博古架陈列 */
  .artifact-card {
    animation: fadeInUp 0.6s ease-out both;

    &.featured .artifact-frame {
      border-color: rgba(201, 168, 76, 0.4);
    }

    .artifact-frame {
      background: linear-gradient(145deg, #fffdf8 0%, #faf8f3 100%);
      border: 1px solid rgba(212, 201, 184, 0.35);
      border-radius: 4px;
      padding: 8px;
      position: relative;
      transition: all var(--transition);
      box-shadow: 0 2px 8px rgba(44, 36, 32, 0.04);

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 12px 28px rgba(44, 36, 32, 0.1);
        border-color: rgba(201, 168, 76, 0.5);

        .frame-corner {
          opacity: 0.6;
        }

        .image-veil {
          opacity: 0;
        }
      }

      /* 四角装饰 */
      .frame-corner {
        position: absolute;
        width: 12px;
        height: 12px;
        border-color: rgba(166, 64, 41, 0.25);
        border-style: solid;
        opacity: 0.4;
        transition: opacity var(--transition);

        &.frame-tl { top: 6px; left: 6px; border-width: 1px 0 0 1px; }
        &.frame-tr { top: 6px; right: 6px; border-width: 1px 1px 0 0; }
        &.frame-bl { bottom: 6px; left: 6px; border-width: 0 0 1px 1px; }
        &.frame-br { bottom: 6px; right: 6px; border-width: 0 1px 1px 0; }
      }
    }

    .artifact-image {
      position: relative;
      aspect-ratio: 4/3;
      border-radius: 2px;
      overflow: hidden;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 0.6s ease;
      }

      .image-veil {
        position: absolute;
        inset: 0;
        background: linear-gradient(180deg,
          transparent 60%,
          rgba(44, 36, 32, 0.15) 100%
        );
        transition: opacity var(--transition);
      }
    }

    .artifact-badge {
      position: absolute;
      top: 16px;
      left: 16px;
      padding: 4px 12px;
      background: rgba(44, 36, 32, 0.8);
      color: var(--gold-light);
      font-size: 11px;
      letter-spacing: 2px;
      border-radius: 2px;
      border: 1px solid rgba(201, 168, 76, 0.25);
    }

    .artifact-content {
      padding: 16px 12px 12px;

      .artifact-name {
        font-family: var(--font-serif);
        font-size: 16px;
        font-weight: 600;
        color: var(--text-color);
        letter-spacing: 1px;
        margin-bottom: 10px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .artifact-origin {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 12px;
        color: var(--text-light);
        margin-bottom: 10px;

        .el-icon {
          font-size: 14px;
          color: rgba(166, 64, 41, 0.5);
        }
      }

      .artifact-stats {
        padding-top: 10px;
        border-top: 1px solid rgba(212, 201, 184, 0.25);

        .stat-item {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 12px;
          color: var(--text-light);

          .el-icon {
            font-size: 14px;
            color: rgba(201, 168, 76, 0.6);
          }
        }
      }
    }
  }

  .pagination-vault {
    display: flex;
    justify-content: center;
    padding-top: 24px;
    border-top: 1px solid rgba(212, 201, 184, 0.25);
  }
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
