<template>
  <div class="study-desk">
    <!-- 问候横幅 -->
    <header class="desk-header">
      <div class="header-greeting">
        <h2 class="greeting-text">{{ greeting }}，<em>{{ displayName }}</em></h2>
        <p class="greeting-date">{{ todayStr }}</p>
      </div>
      <div class="header-stamp">
        <span class="stamp-text">案台</span>
      </div>
    </header>

    <!-- 统计卡片 -->
    <div class="desk-stats">
      <div
        v-for="(s, i) in statList"
        :key="i"
        class="stat-card"
        :class="s.type"
        :style="{ animationDelay: `${i * 0.07}s` }"
        @click="$router.push(s.route)"
      >
        <div class="stat-stripe"></div>
        <div class="stat-body">
          <el-icon class="stat-icon"><component :is="s.icon" /></el-icon>
          <div class="stat-text">
            <span class="stat-number">{{ s.value }}</span>
            <span class="stat-label">{{ s.label }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 主体区 -->
    <div class="desk-body">
      <!-- 左列 -->
      <div class="body-col">
        <!-- 快捷操作 -->
        <section class="desk-panel">
          <div class="panel-head">
            <span class="head-seal">捷</span>
            <h3>快捷操作</h3>
          </div>
          <div class="panel-content">
            <div class="action-grid">
              <button
                v-for="a in actions"
                :key="a.label"
                class="action-btn"
                :class="a.cls"
                @click="$router.push(a.route)"
              >
                <el-icon><component :is="a.icon" /></el-icon>
                <span>{{ a.label }}</span>
              </button>
            </div>
          </div>
        </section>

        <!-- 近期展品 -->
        <section class="desk-panel">
          <div class="panel-head">
            <span class="head-seal">品</span>
            <h3>近期展品</h3>
            <a class="head-link" @click="$router.push('/exhibit')">全部 →</a>
          </div>
          <div class="panel-content">
            <div class="exhibit-list" v-if="recentExhibits.length">
              <div class="exhibit-row" v-for="(item, i) in recentExhibits" :key="item.id">
                <span class="row-idx">{{ String(i + 1).padStart(2, '0') }}</span>
                <div class="row-thumb">
                  <img v-if="item.coverImage" :src="item.coverImage" alt="" />
                  <el-icon v-else><Picture /></el-icon>
                </div>
                <div class="row-info">
                  <span class="row-name">{{ item.name }}</span>
                  <span class="row-meta">{{ item.category || '未分类' }}　·　{{ item.era || '—' }}</span>
                </div>
              </div>
            </div>
            <p class="panel-empty" v-else>暂无展品数据</p>
          </div>
        </section>
      </div>

      <!-- 右列 -->
      <div class="body-col">
        <!-- 待审雅集 -->
        <section class="desk-panel">
          <div class="panel-head">
            <span class="head-seal seal-alert">审</span>
            <h3>待审雅集</h3>
            <span class="head-badge" v-if="pendingPosts.length">{{ pendingPosts.length }}</span>
            <a class="head-link" @click="$router.push('/community')">审核 →</a>
          </div>
          <div class="panel-content">
            <div class="pending-list" v-if="pendingPosts.length">
              <div class="pending-row" v-for="item in pendingPosts" :key="item.id">
                <div class="pending-avatar">{{ (item.authorName || '匿')[0] }}</div>
                <div class="pending-body">
                  <span class="pending-title">{{ item.title || '无题' }}</span>
                  <span class="pending-meta">{{ item.authorName || '匿名' }}　·　{{ fmtTime(item.createTime) }}</span>
                </div>
              </div>
            </div>
            <p class="panel-empty" v-else>无待审内容，诸事安好</p>
          </div>
        </section>

        <!-- 阁籍信息 -->
        <section class="desk-panel">
          <div class="panel-head">
            <span class="head-seal">籍</span>
            <h3>阁籍</h3>
          </div>
          <div class="panel-content">
            <dl class="info-dl">
              <div class="info-row">
                <dt>版本</dt>
                <dd>v1.0.0</dd>
              </div>
              <div class="info-row">
                <dt>阁员</dt>
                <dd>{{ displayName }}</dd>
              </div>
              <div class="info-row">
                <dt>职司</dt>
                <dd><span class="role-chip" :class="roleClass">{{ userInfo.roleName || '—' }}</span></dd>
              </div>
              <div class="info-row">
                <dt>展品总数</dt>
                <dd>{{ stats.exhibitCount }} 件</dd>
              </div>
              <div class="info-row">
                <dt>典藏总数</dt>
                <dd>{{ stats.heritageCount }} 项</dd>
              </div>
              <div class="info-row">
                <dt>阁员总数</dt>
                <dd>{{ stats.userCount }} 人</dd>
              </div>
            </dl>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Picture, Collection, User, View, Plus, ChatDotRound, Reading, Folder, DataBoard } from '@element-plus/icons-vue'
import { getExhibitList } from '@/api/exhibit'
import { getHeritageList } from '@/api/heritage'
import { getUserList } from '@/api/user'
import { getCommunityList } from '@/api/community'

const stats = reactive({ exhibitCount: 0, heritageCount: 0, userCount: 0, postCount: 0 })
const userInfo = ref({})
const recentExhibits = ref([])
const pendingPosts = ref([])

const displayName = computed(() => userInfo.value.realName || userInfo.value.username || '管理员')

const statList = computed(() => [
  { icon: 'Picture', value: stats.exhibitCount, label: '展品', type: 'exhibit', route: '/exhibit' },
  { icon: 'Collection', value: stats.heritageCount, label: '典藏', type: 'heritage', route: '/heritage' },
  { icon: 'User', value: stats.userCount, label: '阁员', type: 'user', route: '/user' },
  { icon: 'ChatDotRound', value: stats.postCount, label: '雅集', type: 'post', route: '/community' }
])

const actions = [
  { icon: 'Plus', label: '添展品', route: '/exhibit', cls: 'accent-cinnabar' },
  { icon: 'Collection', label: '典藏', route: '/heritage', cls: '' },
  { icon: 'ChatDotRound', label: '雅集', route: '/community', cls: '' },
  { icon: 'User', label: '阁员', route: '/user', cls: '' },
  { icon: 'Reading', label: '智库', route: '/knowledge', cls: '' },
  { icon: 'Folder', label: '品类', route: '/heritage-category', cls: '' },
]

const roleClass = computed(() => {
  const m = { 1: 'role-admin', 2: 'role-manager', 3: 'role-staff' }
  return m[userInfo.value.roleType] || 'role-staff'
})

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 9) return '早安'
  if (h < 12) return '上午好'
  if (h < 14) return '午安'
  if (h < 18) return '下午好'
  return '晚上好'
})

const todayStr = computed(() => {
  const d = new Date()
  const w = ['日', '一', '二', '三', '四', '五', '六']
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日　星期${w[d.getDay()]}`
})

const fmtTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const loadData = async () => {
  const [exRes, htRes, usRes, cmRes, cmAllRes] = await Promise.allSettled([
    getExhibitList({ page: 1, pageSize: 5 }),
    getHeritageList({ page: 1, pageSize: 1 }),
    getUserList({ page: 1, pageSize: 1 }),
    getCommunityList({ page: 1, pageSize: 10, status: 0 }),
    getCommunityList({ page: 1, pageSize: 1 })
  ])

  if (exRes.status === 'fulfilled' && exRes.value?.data) {
    const d = exRes.value.data
    stats.exhibitCount = d.total ?? (d.records || d.list || []).length
    recentExhibits.value = (d.records || d.list || []).slice(0, 5)
  }
  if (htRes.status === 'fulfilled' && htRes.value?.data) {
    const d = htRes.value.data
    stats.heritageCount = d.total ?? (d.records || d.list || []).length
  }
  if (usRes.status === 'fulfilled' && usRes.value?.data) {
    const d = usRes.value.data
    stats.userCount = d.total ?? (d.records || d.list || []).length
  }
  if (cmRes.status === 'fulfilled' && cmRes.value?.data) {
    const d = cmRes.value.data
    pendingPosts.value = (d.records || d.list || []).slice(0, 5)
  }
  if (cmAllRes.status === 'fulfilled' && cmAllRes.value?.data) {
    const d = cmAllRes.value.data
    stats.postCount = d.total ?? (d.records || d.list || []).length
  }
}

onMounted(() => {
  const stored = localStorage.getItem('admin_user')
  if (stored) userInfo.value = JSON.parse(stored)
  loadData()
})
</script>

<style scoped lang="scss">
/* ═══ Design Tokens ═══ */
$gold:      #c9a84c;
$cinnabar:  #a64029;
$ink:       #2c2420;
$ink-light: #8c7e74;
$parchment: #fffdf8;
$paper:     #faf8f3;
$border:    rgba(212, 201, 184, 0.35);
$serif:     'Noto Serif SC', 'Source Han Serif SC', 'Songti SC', Georgia, serif;

/* ═══ Entrance Animation ═══ */
@keyframes slideUp {
  from { opacity: 0; transform: translateY(12px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* ═══ Container ═══ */
.study-desk {
  padding: 2px 0;
}

/* ═══ Header / Greeting ═══ */
.desk-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  margin-bottom: 22px;
  background: linear-gradient(135deg, $parchment 0%, $paper 60%, rgba($gold, 0.04) 100%);
  border: 1px solid $border;
  border-left: 3px solid rgba($gold, 0.45);
  border-radius: 4px;
  animation: slideUp 0.4s ease both;
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    right: 0;
    top: 0;
    bottom: 0;
    width: 120px;
    background: linear-gradient(90deg, transparent, rgba($gold, 0.02));
    pointer-events: none;
  }

  .header-greeting {
    .greeting-text {
      font-family: $serif;
      font-size: 19px;
      font-weight: 600;
      color: $ink;
      letter-spacing: 1.5px;
      margin: 0 0 6px;
      line-height: 1.4;

      em {
        font-style: normal;
        color: rgba($cinnabar, 0.85);
      }
    }

    .greeting-date {
      font-size: 12.5px;
      color: $ink-light;
      letter-spacing: 1px;
      margin: 0;
    }
  }

  .header-stamp {
    width: 48px;
    height: 48px;
    border: 2px solid rgba($cinnabar, 0.25);
    border-radius: 2px;
    display: flex;
    align-items: center;
    justify-content: center;
    transform: rotate(-6deg);
    flex-shrink: 0;

    .stamp-text {
      font-family: $serif;
      font-size: 16px;
      font-weight: 700;
      color: rgba($cinnabar, 0.5);
      letter-spacing: 2px;
      writing-mode: vertical-rl;
    }
  }
}

/* ═══ Stat Cards ═══ */
.desk-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 22px;
}

.stat-card {
  position: relative;
  background: linear-gradient(145deg, $parchment, $paper);
  border: 1px solid $border;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;
  animation: slideUp 0.4s ease both;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 6px 20px rgba($ink, 0.06);
    border-color: rgba($gold, 0.4);
  }

  .stat-stripe {
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 3px;
    background: rgba($gold, 0.4);
    transition: width 0.2s ease;
  }

  &:hover .stat-stripe { width: 4px; }

  .stat-body {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 20px 18px;
  }

  .stat-icon {
    font-size: 28px;
    opacity: 0.55;
    transition: opacity 0.2s;
  }

  &:hover .stat-icon { opacity: 0.8; }

  .stat-text {
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  .stat-number {
    font-family: $serif;
    font-size: 26px;
    font-weight: 700;
    color: $ink;
    letter-spacing: 0.5px;
    line-height: 1.15;
  }

  .stat-label {
    font-size: 12px;
    color: $ink-light;
    letter-spacing: 2.5px;
  }

  /* Type accents */
  &.exhibit {
    .stat-stripe { background: rgba($gold, 0.5); }
    .stat-icon { color: $gold; }
  }
  &.heritage {
    .stat-stripe { background: rgba($cinnabar, 0.45); }
    .stat-icon { color: $cinnabar; }
  }
  &.user {
    .stat-stripe { background: rgba($ink-light, 0.45); }
    .stat-icon { color: $ink-light; }
  }
  &.post {
    .stat-stripe { background: rgba(107, 142, 35, 0.45); }
    .stat-icon { color: rgba(107, 142, 35, 0.7); }
  }
}

/* ═══ Body Layout ═══ */
.desk-body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  align-items: start;
}

.body-col {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ═══ Panel (Section Card) ═══ */
.desk-panel {
  background: linear-gradient(150deg, $parchment 0%, $paper 100%);
  border: 1px solid $border;
  border-radius: 4px;
  overflow: hidden;
  animation: slideUp 0.45s ease both;
  animation-delay: 0.25s;

  .panel-head {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 14px 18px;
    border-bottom: 1px solid rgba(212, 201, 184, 0.22);

    h3 {
      font-family: $serif;
      font-size: 15px;
      font-weight: 600;
      color: $ink;
      letter-spacing: 3px;
      margin: 0;
    }

    .head-seal {
      width: 24px;
      height: 24px;
      border: 1px solid rgba($cinnabar, 0.22);
      border-radius: 2px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      font-family: $serif;
      font-size: 11px;
      color: rgba($cinnabar, 0.55);
      flex-shrink: 0;

      &.seal-alert {
        border-color: rgba($cinnabar, 0.35);
        color: rgba($cinnabar, 0.7);
      }
    }

    .head-badge {
      min-width: 20px;
      height: 20px;
      padding: 0 6px;
      background: rgba($cinnabar, 0.1);
      border: 1px solid rgba($cinnabar, 0.22);
      border-radius: 10px;
      font-size: 11px;
      font-weight: 600;
      color: rgba($cinnabar, 0.75);
      display: inline-flex;
      align-items: center;
      justify-content: center;
      line-height: 1;
    }

    .head-link {
      margin-left: auto;
      font-size: 12px;
      color: $ink-light;
      letter-spacing: 0.5px;
      cursor: pointer;
      transition: color 0.2s;
      user-select: none;

      &:hover { color: rgba($cinnabar, 0.7); }
    }
  }

  .panel-content {
    padding: 14px 18px;
  }

  .panel-empty {
    text-align: center;
    padding: 20px 0;
    font-size: 13px;
    color: $ink-light;
    letter-spacing: 2px;
    margin: 0;
  }
}

/* ═══ Quick Actions ═══ */
.action-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;

  .action-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 7px;
    padding: 18px 8px;
    background: rgba($parchment, 0.8);
    border: 1px solid rgba(212, 201, 184, 0.38);
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.22s ease;

    &:hover {
      border-color: rgba($gold, 0.45);
      background: #fff;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba($ink, 0.04);
    }

    .el-icon {
      font-size: 22px;
      color: rgba($gold, 0.65);
      transition: color 0.2s;
    }

    span {
      font-size: 12px;
      color: $ink;
      letter-spacing: 1.5px;
    }

    &.accent-cinnabar {
      background: rgba($cinnabar, 0.04);
      border-color: rgba($cinnabar, 0.25);

      .el-icon { color: rgba($cinnabar, 0.6); }

      &:hover {
        background: rgba($cinnabar, 0.07);
        border-color: rgba($cinnabar, 0.4);
      }
    }
  }
}

/* ═══ Exhibit List ═══ */
.exhibit-list {
  display: flex;
  flex-direction: column;
}

.exhibit-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 4px;
  border-bottom: 1px solid rgba(212, 201, 184, 0.12);
  transition: background 0.15s;

  &:last-child { border-bottom: none; }
  &:hover { background: rgba($gold, 0.03); }

  .row-idx {
    width: 22px;
    font-family: $serif;
    font-size: 12px;
    color: rgba($ink-light, 0.5);
    text-align: center;
    flex-shrink: 0;
  }

  .row-thumb {
    width: 38px;
    height: 38px;
    border-radius: 3px;
    border: 1px solid rgba(212, 201, 184, 0.3);
    overflow: hidden;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background: $paper;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .el-icon {
      font-size: 16px;
      color: rgba($gold, 0.35);
    }
  }

  .row-info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 2px;

    .row-name {
      font-size: 13.5px;
      color: $ink;
      letter-spacing: 0.5px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .row-meta {
      font-size: 11px;
      color: $ink-light;
      letter-spacing: 1px;
    }
  }
}

/* ═══ Pending List ═══ */
.pending-list {
  display: flex;
  flex-direction: column;
}

.pending-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 4px;
  border-bottom: 1px solid rgba(212, 201, 184, 0.12);
  transition: background 0.15s;

  &:last-child { border-bottom: none; }
  &:hover { background: rgba($cinnabar, 0.02); }

  .pending-avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: rgba($cinnabar, 0.07);
    border: 1px solid rgba($cinnabar, 0.14);
    display: flex;
    align-items: center;
    justify-content: center;
    font-family: $serif;
    font-size: 12px;
    color: rgba($cinnabar, 0.55);
    flex-shrink: 0;
  }

  .pending-body {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 2px;

    .pending-title {
      font-size: 13.5px;
      color: $ink;
      letter-spacing: 0.5px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .pending-meta {
      font-size: 11px;
      color: $ink-light;
      letter-spacing: 1px;
    }
  }
}

/* ═══ Info DL ═══ */
.info-dl {
  margin: 0;

  .info-row {
    display: flex;
    align-items: center;
    padding: 11px 0;
    border-bottom: 1px solid rgba(212, 201, 184, 0.15);

    &:last-child { border-bottom: none; }

    dt {
      width: 68px;
      font-size: 12px;
      color: $ink-light;
      letter-spacing: 2px;
      flex-shrink: 0;
    }

    dd {
      flex: 1;
      font-size: 13.5px;
      color: $ink;
      letter-spacing: 0.5px;
      margin: 0;
    }
  }
}

.role-chip {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 2px;
  font-size: 12px;
  letter-spacing: 1px;

  &.role-admin {
    background: rgba($cinnabar, 0.09);
    color: rgba($cinnabar, 0.8);
    border: 1px solid rgba($cinnabar, 0.18);
  }

  &.role-manager {
    background: rgba($gold, 0.09);
    color: rgba(180, 148, 66, 0.8);
    border: 1px solid rgba($gold, 0.18);
  }

  &.role-staff {
    background: rgba($ink-light, 0.09);
    color: rgba($ink-light, 0.8);
    border: 1px solid rgba($ink-light, 0.18);
  }
}
</style>
