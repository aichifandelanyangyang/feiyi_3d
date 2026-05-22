<template>
  <div class="study-layout">
    <!-- 书斋侧边栏 -->
    <aside class="study-sidebar" :class="{ collapsed: isCollapsed }">
      <div class="sidebar-header">
        <img class="header-logo" src="/imgs/logo1.jpg" alt="文房" />
        <h1 v-if="!isCollapsed" class="header-title">文房</h1>
      </div>

      <div class="sidebar-divider">
        <span class="divider-dot"></span>
      </div>

      <el-menu
        :default-active="$route.path"
        :collapse="isCollapsed"
        router
        class="study-menu"
        background-color="transparent"
        text-color="rgba(231, 211, 150, 0.6)"
        active-text-color="#e7d396"
      >
        <el-menu-item index="/dashboard" class="menu-item">
          <el-icon class="menu-icon"><DataBoard /></el-icon>
          <span class="menu-text">案台</span>
        </el-menu-item>
        <el-menu-item index="/exhibit" class="menu-item">
          <el-icon class="menu-icon"><Picture /></el-icon>
          <span class="menu-text">展品</span>
        </el-menu-item>
        <el-menu-item index="/heritage" class="menu-item">
          <el-icon class="menu-icon"><Collection /></el-icon>
          <span class="menu-text">典藏</span>
        </el-menu-item>
        <el-menu-item index="/heritage-category" class="menu-item">
          <el-icon class="menu-icon"><Menu /></el-icon>
          <span class="menu-text">品类</span>
        </el-menu-item>
        <el-menu-item index="/knowledge" class="menu-item">
          <el-icon class="menu-icon"><Reading /></el-icon>
          <span class="menu-text">智库</span>
        </el-menu-item>
        <el-menu-item index="/community" class="menu-item">
          <el-icon class="menu-icon"><ChatDotRound /></el-icon>
          <span class="menu-text">雅集</span>
        </el-menu-item>
        <el-menu-item v-if="userInfo.roleType === 1" index="/user" class="menu-item">
          <el-icon class="menu-icon"><User /></el-icon>
          <span class="menu-text">阁员</span>
        </el-menu-item>
        <el-menu-item index="/profile" class="menu-item">
          <el-icon class="menu-icon"><Edit /></el-icon>
          <span class="menu-text">我的</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer" v-if="!isCollapsed">
        <div class="footer-line"></div>
        <span class="footer-mark">非遗数字化</span>
      </div>
    </aside>

    <!-- 主内容区 -->
    <div class="study-main">
      <header class="study-header">
        <div class="header-left">
          <button class="collapse-btn" @click="isCollapsed = !isCollapsed">
            <el-icon>
              <Fold v-if="!isCollapsed" />
              <Expand v-else />
            </el-icon>
          </button>
          <div class="breadcrumb">
            <span class="crumb-home">文房</span>
            <span class="crumb-separator">›</span>
            <span class="crumb-current">{{ routeName }}</span>
          </div>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand" class="user-dropdown">
            <span class="user-info">
              <el-avatar :size="32" :src="userInfo.avatar || undefined">{{ (userInfo.username || '?').charAt(0).toUpperCase() }}</el-avatar>
              <span class="username">{{ userInfo.realName || userInfo.username }}</span>
              <span class="role-badge" :class="roleClass">{{ userInfo.roleName }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu class="study-dropdown">
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="study-content">
        <div class="content-paper">
          <router-view />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DataBoard, Picture, Collection, Menu, Reading, ChatDotRound, User, Edit, Fold, Expand } from '@element-plus/icons-vue'

const router = useRouter()
const currentRoute = useRoute()
const isCollapsed = ref(false)
const userInfo = ref({})

const routeName = computed(() => {
  const names = {
    'dashboard': '案台',
    'exhibit': '展品',
    'heritage': '典藏',
    'heritage-category': '品类',
    'knowledge': '智库',
    'community': '雅集',
    'user': '阁员',
    'profile': '我的'
  }
  const path = currentRoute.path.split('/')[1]
  return names[path] || ''
})

const roleClass = computed(() => {
  const classes = { 1: 'role-admin', 2: 'role-manager', 3: 'role-staff' }
  return classes[userInfo.value.roleType] || 'role-staff'
})

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_user')
    router.push('/login')
    ElMessage.success('已退出登录')
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

onMounted(() => {
  const stored = localStorage.getItem('admin_user')
  if (stored) {
    userInfo.value = JSON.parse(stored)
  }
})
</script>

<style scoped lang="scss">
.study-layout {
  display: flex;
  height: 100vh;
  background: #1a1614;
}

/* 书斋侧边栏 */
.study-sidebar {
  width: 200px;
  background: linear-gradient(180deg, #24201c 0%, #1a1614 100%);
  border-right: 1px solid rgba(201, 168, 76, 0.15);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;

  &.collapsed {
    width: 64px;

    .sidebar-header {
      padding: 16px 0;

      .header-logo {
        margin: 0 auto;
      }
    }
  }

  .sidebar-header {
    padding: 20px;
    display: flex;
    align-items: center;
    gap: 12px;

    .header-logo {
      width: 36px;
      height: 36px;
      border-radius: 4px;
      object-fit: cover;
      flex-shrink: 0;
    }

    .header-title {
      font-family: var(--font-serif);
      font-size: 20px;
      color: rgba(231, 211, 150, 0.95);
      letter-spacing: 4px;
      margin: 0;
    }
  }

  .sidebar-divider {
    padding: 0 20px 16px;
    text-align: center;

    .divider-dot {
      display: inline-block;
      width: 4px;
      height: 4px;
      background: rgba(201, 168, 76, 0.4);
      border-radius: 50%;
    }
  }

  .study-menu {
    flex: 1;
    border-right: none;

    :deep(.el-menu-item) {
      height: 48px;
      line-height: 48px;
      margin: 4px 12px;
      border-radius: 4px;
      transition: all 0.3s;

      &:hover {
        background: rgba(201, 168, 76, 0.08);
      }

      &.is-active {
        background: rgba(201, 168, 76, 0.15);
        border-left: 2px solid rgba(201, 168, 76, 0.6);
      }
    }

    .menu-icon {
      font-size: 18px;
      margin-right: 12px;
    }

    .menu-text {
      font-family: var(--font-serif);
      font-size: 14px;
      letter-spacing: 2px;
    }
  }

  .sidebar-footer {
    padding: 16px 20px;
    text-align: center;

    .footer-line {
      height: 1px;
      background: linear-gradient(90deg, transparent, rgba(201, 168, 76, 0.3), transparent);
      margin-bottom: 10px;
    }

    .footer-mark {
      font-size: 11px;
      color: rgba(201, 168, 76, 0.4);
      letter-spacing: 1px;
    }
  }
}

/* 主内容区 */
.study-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f5efe4;
  overflow: hidden;

  /* 顶部栏 */
  .study-header {
    height: 56px;
    background: linear-gradient(180deg, #fffdf8 0%, #faf8f3 100%);
    border-bottom: 1px solid rgba(212, 201, 184, 0.4);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24px;

    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;

      .collapse-btn {
        width: 32px;
        height: 32px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: transparent;
        border: 1px solid rgba(212, 201, 184, 0.5);
        border-radius: 4px;
        cursor: pointer;
        transition: all var(--transition);

        &:hover {
          border-color: var(--gold);
          background: rgba(201, 168, 76, 0.05);
        }

        .el-icon {
          font-size: 18px;
          color: var(--text-light);
        }
      }

      .breadcrumb {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 13px;

        .crumb-home {
          color: var(--text-light);
          letter-spacing: 1px;
        }

        .crumb-separator {
          color: rgba(201, 168, 76, 0.5);
        }

        .crumb-current {
          font-family: var(--font-serif);
          color: var(--text-color);
          letter-spacing: 2px;
        }
      }
    }

    .header-right {
      .user-dropdown {
        .user-info {
          display: flex;
          align-items: center;
          gap: 10px;
          cursor: pointer;
          padding: 6px 12px;
          border-radius: 4px;
          transition: background var(--transition);

          &:hover {
            background: rgba(201, 168, 76, 0.05);
          }

          .el-avatar {
            border: 1px solid rgba(212, 201, 184, 0.4);
          }

          .username {
            font-size: 13px;
            color: var(--text-color);
            letter-spacing: 1px;
          }

          .role-badge {
            padding: 2px 8px;
            border-radius: 2px;
            font-size: 11px;
            letter-spacing: 1px;

            &.role-admin {
              background: rgba(166, 64, 41, 0.1);
              color: rgba(166, 64, 41, 0.8);
              border: 1px solid rgba(166, 64, 41, 0.2);
            }

            &.role-manager {
              background: rgba(201, 168, 76, 0.1);
              color: rgba(180, 148, 66, 0.8);
              border: 1px solid rgba(201, 168, 76, 0.2);
            }

            &.role-staff {
              background: rgba(140, 126, 116, 0.1);
              color: rgba(140, 126, 116, 0.8);
              border: 1px solid rgba(140, 126, 116, 0.2);
            }
          }
        }
      }
    }
  }

  /* 内容区 */
  .study-content {
    flex: 1;
    padding: 20px;
    overflow-y: auto;
    background:
      repeating-linear-gradient(
        0deg,
        transparent,
        transparent 24px,
        rgba(140, 126, 116, 0.02) 24px,
        rgba(140, 126, 116, 0.02) 25px
      );

    .content-paper {
      min-height: 100%;
    }
  }
}

/* 下拉菜单样式 */
:deep(.study-dropdown) {
  border: 1px solid rgba(212, 201, 184, 0.4);
  border-radius: 4px;
  box-shadow: 0 4px 16px rgba(44, 36, 32, 0.1);

  .el-dropdown-menu__item {
    font-size: 13px;
    color: var(--text-color);
    letter-spacing: 1px;

    &:hover {
      background: rgba(201, 168, 76, 0.08);
      color: var(--text-color);
    }
  }
}
</style>
