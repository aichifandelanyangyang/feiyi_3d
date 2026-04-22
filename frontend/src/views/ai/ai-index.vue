<template>
  <div class="ai-container">
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
        <router-link to="/community" class="nav-item">社区互动</router-link>
        <router-link to="/ai-assistant" class="nav-item active">非遗智识</router-link>
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
      <div class="ai-layout">
        <!-- 历史对话侧边栏 -->
        <aside class="history-sidebar" :class="{ collapsed: sidebarCollapsed }">
          <div class="sidebar-header">
            <span class="sidebar-title">历史对话</span>
            <button class="sidebar-toggle" @click="sidebarCollapsed = !sidebarCollapsed" title="收起">
              <span>«</span>
            </button>
          </div>
          <button class="new-chat-btn" @click="newConversation">
            <span>+ 新对话</span>
          </button>
          <div class="history-list">
            <div
              v-for="conv in conversations"
              :key="conv.id"
              class="history-item"
              :class="{ active: conv.id === currentConvId }"
              @click="switchConversation(conv.id)"
            >
              <span class="history-title">{{ conv.title }}</span>
              <span class="history-time">{{ conv.date }}</span>
              <button class="history-delete" @click.stop="deleteConversation(conv.id)" title="删除">
                ×
              </button>
            </div>
            <div v-if="!conversations.length" class="history-empty">暂无历史对话</div>
          </div>
        </aside>

        <!-- 侧栏收起时的展开按钮 -->
        <button v-if="sidebarCollapsed" class="sidebar-expand" @click="sidebarCollapsed = false" title="展开历史对话">
          <span>»</span>
        </button>

        <!-- 右侧对话区 -->
        <div class="chat-main">
          <!-- 页头 -->
          <div class="ai-header">
            <div class="header-ornament">
              <span class="ornament-line"></span>
              <span class="ornament-seal">智</span>
              <span class="ornament-line"></span>
            </div>
            <h1 class="ai-title">非遗智识</h1>
            <p class="ai-subtitle">AI智能助手 · 博古通今，问道非遗</p>
          </div>

      <!-- 对话区 -->
      <div class="chat-scroll">
        <div class="scroll-frame">
          <div class="frame-corner corner-tl"></div>
          <div class="frame-corner corner-tr"></div>
          <div class="frame-corner corner-bl"></div>
          <div class="frame-corner corner-br"></div>

          <!-- 消息列表 -->
          <div class="chat-messages" ref="messagesRef">
            <div
              v-for="(msg, index) in messages"
              :key="index"
              class="message-item"
              :class="msg.role"
            >
              <div class="message-avatar">
                <span v-if="msg.role === 'assistant'" class="avatar-ai">智</span>
                <el-avatar v-else :size="40" :src="userStore.avatar || undefined">
                  {{ (userStore.username || '我').charAt(0).toUpperCase() }}
                </el-avatar>
              </div>
              <div class="message-body">
                <div class="message-meta">
                  <span class="meta-name">{{ msg.role === 'assistant' ? '非遗智识' : (userStore.username || '我') }}</span>
                  <span class="meta-time">{{ msg.time }}</span>
                </div>
                <div class="message-bubble" v-html="formatMessage(msg.content)"></div>
                <div class="message-sources" v-if="msg.sources && msg.sources.length">
                  <span class="sources-label">引经据典：</span>
                  <span class="source-tag" v-for="(s, i) in msg.sources" :key="i">{{ s }}</span>
                </div>
                <div class="message-exhibits" v-if="msg.exhibits && msg.exhibits.length && msg.showExhibits">
                  <span class="exhibits-label">相关展品：</span>
                  <div class="exhibit-link-list">
                    <a
                      v-for="ex in msg.exhibits.slice(0, 3)"
                      :key="ex.id"
                      class="exhibit-link-card"
                      @click="goToExhibit(ex)"
                    >
                      <img v-if="ex.image" :src="ex.image" class="link-thumb" alt="" />
                      <span v-else class="link-thumb-placeholder">3D</span>
                      <span class="link-name">{{ ex.name }}</span>
                      <span class="link-arrow">查看3D展品 →</span>
                    </a>
                  </div>
                  <a class="hall-link" @click="goToHall">
                    <span class="hall-icon">🏛️</span>
                    <span>进入虚拟展厅，浏览更多3D展品</span>
                    <span class="link-arrow">→</span>
                  </a>
                </div>
              </div>
            </div>

            <!-- 加载态 -->
            <div v-if="loading" class="message-item assistant">
              <div class="message-avatar"><span class="avatar-ai">智</span></div>
              <div class="message-body">
                <div class="message-meta"><span class="meta-name">非遗智识</span></div>
                <div class="message-bubble">
                  <span class="dot-typing"><span></span><span></span><span></span></span>
                </div>
              </div>
            </div>
          </div>

          <!-- 输入区 -->
          <div class="chat-input-area">
            <div class="input-frame">
              <input
                v-model="inputText"
                placeholder="请输入您想了解的非遗知识..."
                @keyup.enter="sendMessage"
                :disabled="loading"
                class="chat-input"
              />
              <button class="send-btn" @click="sendMessage" :disabled="loading || !inputText.trim()">
                <span class="btn-text">发送</span>
              </button>
            </div>
            <p class="input-hint">可以询问非遗项目、传统技艺、文化传承等相关问题</p>
          </div>
        </div>
      </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { aiChat } from '@/api/ai'
import { useUserStore } from '@/store'

const router = useRouter()
const userStore = useUserStore()

const inputText = ref('')
const loading = ref(false)
const messagesRef = ref(null)
const sidebarCollapsed = ref(false)
const initialized = ref(false)

const WELCOME_MSG = {
  role: 'assistant',
  content: '您好！我是「非遗智识」—— 一位基于人工智能技术的非物质文化遗产知识助手。\n\n我接入了平台的非遗知识库与项目数据，能够为您智能检索并解答关于非遗项目、传统技艺、文化传承等方面的问题。\n\n请随时向我提问，我将竭诚为您服务！',
  time: ''
}

const STORAGE_KEY = 'ai_chat_history'

const getNow = () => {
  const d = new Date()
  return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

const getToday = () => {
  const d = new Date()
  return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

// ===== 历史对话管理 =====
const conversations = ref([])  // { id, title, date, messages }
const currentConvId = ref(null)
const messages = ref([])

const loadConversations = () => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) {
      conversations.value = JSON.parse(raw)
    }
  } catch (e) {
    conversations.value = []
  }
}

const saveConversations = () => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(conversations.value))
  } catch (e) { /* ignore */ }
}

const createConvId = () => Date.now().toString(36) + Math.random().toString(36).slice(2, 6)

const extractTitle = (msgs) => {
  const userMsg = msgs.find(m => m.role === 'user')
  if (!userMsg) return '新对话'
  const text = userMsg.content || ''
  return text.length > 20 ? text.slice(0, 20) + '...' : text
}

const newConversation = () => {
  // 保存当前对话
  saveCurrentConversation()
  // 创建新对话
  const id = createConvId()
  currentConvId.value = id
  WELCOME_MSG.time = getNow()
  messages.value = [{ ...WELCOME_MSG }]
}

const saveCurrentConversation = () => {
  if (!currentConvId.value) return
  // 只有用户发过消息才保存
  const hasUserMsg = messages.value.some(m => m.role === 'user')
  if (!hasUserMsg) {
    // 没发过消息，移除空对话记录
    conversations.value = conversations.value.filter(c => c.id !== currentConvId.value)
    saveConversations()
    return
  }
  const idx = conversations.value.findIndex(c => c.id === currentConvId.value)
  const convData = {
    id: currentConvId.value,
    title: extractTitle(messages.value),
    date: getToday(),
    messages: messages.value.map(m => ({ role: m.role, content: m.content, time: m.time, sources: m.sources, exhibits: m.exhibits, showExhibits: m.showExhibits }))
  }
  if (idx >= 0) {
    conversations.value[idx] = convData
  } else {
    conversations.value.unshift(convData)
  }
  saveConversations()
}

const switchConversation = (id) => {
  if (id === currentConvId.value) return
  saveCurrentConversation()
  const conv = conversations.value.find(c => c.id === id)
  if (conv) {
    currentConvId.value = id
    messages.value = conv.messages.map(m => ({ ...m }))
    nextTick(() => scrollToBottom())
  }
}

const deleteConversation = (id) => {
  ElMessageBox.confirm('确定删除该对话记录吗？', '提示', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
    .then(() => {
      conversations.value = conversations.value.filter(c => c.id !== id)
      saveConversations()
      if (id === currentConvId.value) {
        // 先清除当前ID，避免 saveCurrentConversation 把已删除的对话又加回来
        currentConvId.value = null
        const newId = createConvId()
        currentConvId.value = newId
        WELCOME_MSG.time = getNow()
        messages.value = [{ ...WELCOME_MSG }]
      }
      ElMessage.success('已删除')
    })
    .catch(() => {})
}

const handleUserCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

const scrollToBottom = () => {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

// 消息变化时自动保存（初始化完成后才开始保存，避免覆盖历史记录）
watch(messages, () => {
  if (initialized.value) saveCurrentConversation()
}, { deep: true })

const goToExhibit = (exhibit) => {
  router.push(`/exhibit/${encodeURIComponent(exhibit.name)}`)
}

const goToHall = () => {
  router.push('/exhibition-hall')
}

const formatMessage = (text) => {
  if (!text) return ''
  const lines = text.split('\n')
  let html = ''
  let inList = false
  let inOl = false

  // 标题正则
  const h4Re = /^####\s+(.+)/
  const h3Re = /^###\s+(.+)/
  const h2Re = /^##\s+(.+)/
  const h1Re = /^#\s+(.+)/
  // 列表正则
  const ulRe = /^[-*]\s+(.+)/
  const olRe = /^\d+\.\s+(.+)/

  for (let i = 0; i < lines.length; i++) {
    let line = lines[i]
    let match

    // 标题 #### > ### > ## > #
    if ((match = h4Re.exec(line))) {
      if (inList) { html += '</ul>'; inList = false }
      if (inOl) { html += '</ol>'; inOl = false }
      html += `<h6 class="md-h4">${formatInline(match[1])}</h6>`
      continue
    }
    if ((match = h3Re.exec(line))) {
      if (inList) { html += '</ul>'; inList = false }
      if (inOl) { html += '</ol>'; inOl = false }
      html += `<h5 class="md-h3">${formatInline(match[1])}</h5>`
      continue
    }
    if ((match = h2Re.exec(line))) {
      if (inList) { html += '</ul>'; inList = false }
      if (inOl) { html += '</ol>'; inOl = false }
      html += `<h4 class="md-h2">${formatInline(match[1])}</h4>`
      continue
    }
    if ((match = h1Re.exec(line))) {
      if (inList) { html += '</ul>'; inList = false }
      if (inOl) { html += '</ol>'; inOl = false }
      html += `<h3 class="md-h1">${formatInline(match[1])}</h3>`
      continue
    }

    // 无序列表
    if ((match = ulRe.exec(line))) {
      if (inOl) { html += '</ol>'; inOl = false }
      if (!inList) { html += '<ul class="md-ul">'; inList = true }
      html += `<li>${formatInline(match[1])}</li>`
      continue
    }

    // 有序列表
    if ((match = olRe.exec(line))) {
      if (inList) { html += '</ul>'; inList = false }
      if (!inOl) { html += '<ol class="md-ol">'; inOl = true }
      html += `<li>${formatInline(match[1])}</li>`
      continue
    }

    // 普通段落
    if (inList) { html += '</ul>'; inList = false }
    if (inOl) { html += '</ol>'; inOl = false }

    if (line.trim() === '') {
      html += '<br>'
    } else {
      html += `<p class="md-p">${formatInline(line)}</p>`
    }
  }

  if (inList) html += '</ul>'
  if (inOl) html += '</ol>'
  return html
}

const formatInline = (text) => {
  return text
    .replace(/`([^`]+)`/g, '<code class="md-code">$1</code>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
}

const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: text, time: getNow() })
  inputText.value = ''
  loading.value = true
  await nextTick()
  scrollToBottom()

  try {
    const history = messages.value
      .slice(-11, -1)
      .filter(m => m.role === 'user' || m.role === 'assistant')
      .map(m => ({ role: m.role, content: m.content }))

    const res = await aiChat({ message: text, history })

    if (res.code === 200 && res.data) {
      const reply = res.data.reply || '抱歉，暂时无法回答。'
      const msgIndex = messages.value.length
      messages.value.push({ role: 'assistant', content: '', sources: res.data.sources || [], exhibits: res.data.exhibits || [], showExhibits: false, time: getNow() })
      loading.value = false

      let charIndex = 0
      const typeInterval = setInterval(() => {
        if (charIndex < reply.length) {
          messages.value[msgIndex].content += reply[charIndex]
          charIndex++
          scrollToBottom()
        } else {
          clearInterval(typeInterval)
          // 打字结束后延迟显示展品推荐
          if (messages.value[msgIndex].exhibits && messages.value[msgIndex].exhibits.length) {
            setTimeout(() => {
              messages.value[msgIndex].showExhibits = true
              nextTick(() => scrollToBottom())
            }, 400)
          }
        }
      }, 20)
    } else {
      messages.value.push({ role: 'assistant', content: res.msg || '请求失败，请稍后再试。', time: getNow() })
      loading.value = false
    }
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '网络异常，请稍后再试。', time: getNow() })
    loading.value = false
  }

  await nextTick()
  scrollToBottom()
}

onMounted(() => {
  loadConversations()
  if (conversations.value.length) {
    const latest = conversations.value[0]
    currentConvId.value = latest.id
    messages.value = latest.messages.map(m => ({ ...m }))
  } else {
    const id = createConvId()
    currentConvId.value = id
    WELCOME_MSG.time = getNow()
    messages.value = [{ ...WELCOME_MSG }]
  }
  nextTick(() => {
    scrollToBottom()
    initialized.value = true
  })
})
</script>

<style scoped lang="scss">
.ai-container {
  min-height: 100vh;
  background:
    radial-gradient(ellipse 80% 50% at 50% 0%, rgba(201, 168, 76, 0.12) 0%, transparent 70%),
    radial-gradient(ellipse 50% 80% at 0% 50%, rgba(166, 64, 41, 0.06) 0%, transparent 60%),
    linear-gradient(180deg, #faf6ed 0%, #f2ece0 50%, #e8e2d4 100%);
  position: relative;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background:
      repeating-linear-gradient(0deg, transparent, transparent 24px, rgba(140, 126, 116, 0.03) 24px, rgba(140, 126, 116, 0.03) 25px),
      url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)' opacity='0.03'/%3E%3C/svg%3E");
    background-size: 100% 100%, 200px 200px;
    pointer-events: none;
    opacity: 0.8;
  }

  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 10%;
    right: 10%;
    height: 1px;
    background: linear-gradient(90deg, transparent 0%, rgba(201, 168, 76, 0.3) 20%, rgba(201, 168, 76, 0.5) 50%, rgba(201, 168, 76, 0.3) 80%, transparent 100%);
  }
}

.page-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px 60px;
  position: relative;
  z-index: 1;
}

/* 布局 */
.ai-layout {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.chat-main {
  flex: 1;
  min-width: 0;
}

/* 历史对话侧边栏 */
.history-sidebar {
  width: 220px;
  flex-shrink: 0;
  background: linear-gradient(170deg, rgba(255, 253, 248, 0.95), rgba(250, 246, 237, 0.9));
  border: 1px solid rgba(201, 168, 76, 0.25);
  border-radius: 8px;
  padding: 12px;
  transition: all 0.3s ease;
  position: sticky;
  top: 100px;
  max-height: calc(100vh - 140px);
  display: flex;
  flex-direction: column;

  &.collapsed { display: none; }

  .sidebar-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 10px;
  }

  .sidebar-title {
    font-family: var(--font-serif);
    font-size: 14px;
    font-weight: 600;
    color: var(--text-color);
    letter-spacing: 2px;
  }

  .sidebar-toggle {
    background: none;
    border: none;
    font-size: 16px;
    color: var(--text-light);
    cursor: pointer;
    padding: 2px 6px;
    border-radius: 4px;
    transition: background 0.2s;
    &:hover { background: rgba(201, 168, 76, 0.1); }
  }
}

.sidebar-expand {
  position: sticky;
  top: 100px;
  flex-shrink: 0;
  width: 28px;
  height: 48px;
  background: linear-gradient(170deg, rgba(255, 253, 248, 0.95), rgba(250, 246, 237, 0.9));
  border: 1px solid rgba(201, 168, 76, 0.25);
  border-radius: 0 8px 8px 0;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: var(--text-light);
  transition: all 0.2s;
  &:hover { background: rgba(201, 168, 76, 0.1); }
}

.new-chat-btn {
  width: 100%;
  padding: 8px 0;
  border: 1px dashed rgba(201, 168, 76, 0.4);
  border-radius: 6px;
  background: transparent;
  font-family: var(--font-serif);
  font-size: 13px;
  color: rgba(166, 64, 41, 0.8);
  letter-spacing: 2px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 10px;
  &:hover {
    background: rgba(201, 168, 76, 0.08);
    border-color: rgba(201, 168, 76, 0.6);
  }
}

.history-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 4px;

  &::-webkit-scrollbar { width: 3px; }
  &::-webkit-scrollbar-thumb { background: rgba(201, 168, 76, 0.2); border-radius: 2px; }
}

.history-item {
  padding: 8px 10px;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  transition: all 0.2s;

  &:hover { background: rgba(201, 168, 76, 0.08); }
  &.active {
    background: rgba(201, 168, 76, 0.12);
    border-left: 2px solid rgba(201, 168, 76, 0.6);
  }

  .history-title {
    display: block;
    font-size: 13px;
    color: var(--text-color);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    padding-right: 18px;
  }

  .history-time {
    display: block;
    font-size: 11px;
    color: var(--text-light);
    margin-top: 2px;
  }

  .history-delete {
    position: absolute;
    top: 6px;
    right: 6px;
    background: none;
    border: none;
    font-size: 14px;
    color: var(--text-light);
    cursor: pointer;
    opacity: 0;
    transition: opacity 0.2s;
    padding: 0 4px;
    border-radius: 3px;
    &:hover { color: rgba(166, 64, 41, 0.8); background: rgba(166, 64, 41, 0.08); }
  }

  &:hover .history-delete { opacity: 1; }
}

.history-empty {
  text-align: center;
  padding: 20px 0;
  font-size: 12px;
  color: var(--text-light);
  letter-spacing: 1px;
}

/* 页头 */
.ai-header {
  text-align: center;
  margin-bottom: 36px;

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

    .ornament-seal {
      width: 42px;
      height: 42px;
      border: 2px solid rgba(201, 168, 76, 0.5);
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-family: var(--font-serif);
      font-size: 20px;
      font-weight: 700;
      color: rgba(201, 168, 76, 0.7);
    }
  }

  .ai-title {
    font-family: var(--font-serif);
    font-size: 42px;
    font-weight: 700;
    color: var(--text-color);
    letter-spacing: 12px;
    margin-bottom: 12px;
  }

  .ai-subtitle {
    font-size: 15px;
    color: var(--text-light);
    letter-spacing: 4px;
  }
}

/* 对话卷轴 */
.chat-scroll {
  .scroll-frame {
    background: linear-gradient(170deg, rgba(255, 253, 248, 0.95) 0%, rgba(250, 246, 237, 0.9) 100%);
    border: 1px solid rgba(201, 168, 76, 0.25);
    border-radius: 8px;
    padding: 12px;
    position: relative;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
  }
}

/* 四角框 */
.frame-corner {
  position: absolute;
  width: 12px;
  height: 12px;
  border-color: rgba(201, 168, 76, 0.35);
  border-style: solid;

  &.corner-tl { top: 10px; left: 10px; border-width: 1.5px 0 0 1.5px; }
  &.corner-tr { top: 10px; right: 10px; border-width: 1.5px 1.5px 0 0; }
  &.corner-bl { bottom: 10px; left: 10px; border-width: 0 0 1.5px 1.5px; }
  &.corner-br { bottom: 10px; right: 10px; border-width: 0 1.5px 1.5px 0; }
}

/* 消息列表 */
.chat-messages {
  height: 520px;
  overflow-y: auto;
  padding: 28px 28px;
  display: flex;
  flex-direction: column;
  gap: 24px;

  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-thumb { background: rgba(201, 168, 76, 0.2); border-radius: 2px; }
}

/* 消息项 */
.message-item {
  display: flex;
  gap: 14px;
  animation: fadeSlideIn 0.3s ease;

  &.user { flex-direction: row-reverse; }
  &.user .message-body { align-items: flex-end; }
  &.user .message-bubble {
    background: linear-gradient(135deg, #3a2e24, #2a2018);
    color: #e7d396;
    border-top-right-radius: 2px;
  }
}

@keyframes fadeSlideIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.message-avatar {
  flex-shrink: 0;

  .avatar-ai {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: linear-gradient(135deg, #3a2e24, #2a2018);
    border: 1.5px solid rgba(201, 168, 76, 0.5);
    color: #e7d396;
    font-family: var(--font-serif);
    font-size: 16px;
    font-weight: 700;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.message-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-width: 80%;
}

.message-meta {
  display: flex;
  align-items: center;
  gap: 10px;

  .meta-name {
    font-family: var(--font-serif);
    font-size: 13px;
    color: var(--text-color);
    letter-spacing: 1px;
    font-weight: 500;
  }

  .meta-time {
    font-size: 11px;
    color: var(--text-light);
  }
}

.message-bubble {
  padding: 14px 18px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.8;
  letter-spacing: 0.5px;
  background: #fff;
  color: var(--text-color);
  border: 1px solid rgba(212, 201, 184, 0.3);
  border-top-left-radius: 2px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);

  /* Markdown 样式 */
  .md-h1, .md-h2, .md-h3, .md-h4 {
    font-family: var(--font-serif);
    color: var(--text-color);
    margin: 12px 0 8px;
    letter-spacing: 2px;
  }
  .md-h1 { font-size: 18px; font-weight: 700; border-bottom: 1px solid rgba(201, 168, 76, 0.2); padding-bottom: 6px; }
  .md-h2 { font-size: 16px; font-weight: 600; }
  .md-h3 { font-size: 15px; font-weight: 600; color: rgba(166, 64, 41, 0.8); }
  .md-h4 { font-size: 14px; font-weight: 500; }

  .md-p {
    margin: 6px 0;
    text-align: justify;
  }

  .md-ul, .md-ol {
    margin: 8px 0;
    padding-left: 20px;
  }
  .md-ul { list-style-type: none; }
  .md-ul li {
    position: relative;
    margin: 4px 0;
    &:before {
      content: '•';
      position: absolute;
      left: -14px;
      color: rgba(201, 168, 76, 0.7);
      font-size: 12px;
    }
  }
  .md-ol {
    list-style-type: decimal;
    li { margin: 4px 0; }
  }

  .md-code {
    font-family: 'Consolas', 'Monaco', monospace;
    font-size: 13px;
    background: rgba(201, 168, 76, 0.08);
    border: 1px solid rgba(201, 168, 76, 0.15);
    padding: 2px 6px;
    border-radius: 3px;
    color: rgba(166, 64, 41, 0.85);
  }

  strong {
    font-weight: 600;
    color: rgba(166, 64, 41, 0.85);
  }

  em {
    font-style: italic;
    color: var(--text-color);
  }
}

/* 引用来源 */
.message-sources {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  margin-top: 2px;

  .sources-label {
    font-size: 12px;
    color: var(--text-light);
    letter-spacing: 1px;
  }

  .source-tag {
    font-size: 11px;
    color: rgba(166, 64, 41, 0.75);
    background: rgba(166, 64, 41, 0.06);
    border: 1px solid rgba(166, 64, 41, 0.15);
    padding: 2px 10px;
    border-radius: 12px;
    letter-spacing: 0.5px;
  }
}

/* 相关展品链接 */
.message-exhibits {
  margin-top: 10px;
  animation: exhibitFadeIn 0.5s ease both;

  @keyframes exhibitFadeIn {
    from {
      opacity: 0;
      transform: translateY(8px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  .exhibits-label {
    display: block;
    font-size: 12px;
    color: var(--text-light);
    letter-spacing: 1px;
    margin-bottom: 8px;
  }

  .exhibit-link-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .exhibit-link-card {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 8px 14px 8px 8px;
    background: linear-gradient(135deg, rgba(255, 253, 248, 0.95), rgba(250, 246, 237, 0.9));
    border: 1px solid rgba(201, 168, 76, 0.25);
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.25s ease;
    text-decoration: none;

    &:hover {
      border-color: rgba(201, 168, 76, 0.5);
      transform: translateY(-2px);
      box-shadow: 0 4px 16px rgba(201, 168, 76, 0.12);
    }

    .link-thumb {
      width: 36px;
      height: 36px;
      border-radius: 4px;
      object-fit: cover;
      border: 1px solid rgba(212, 201, 184, 0.3);
    }

    .link-thumb-placeholder {
      width: 36px;
      height: 36px;
      border-radius: 4px;
      background: linear-gradient(135deg, #3a2e24, #2a2018);
      border: 1px solid rgba(201, 168, 76, 0.3);
      display: flex;
      align-items: center;
      justify-content: center;
      font-family: var(--font-serif);
      font-size: 11px;
      font-weight: 700;
      color: rgba(201, 168, 76, 0.7);
      letter-spacing: 1px;
    }

    .link-name {
      font-size: 13px;
      color: var(--text-color);
      letter-spacing: 0.5px;
      font-weight: 500;
    }

    .link-arrow {
      font-size: 11px;
      color: rgba(166, 64, 41, 0.7);
      letter-spacing: 0.5px;
      white-space: nowrap;
      transition: color 0.2s;
    }

    &:hover .link-arrow {
      color: rgba(166, 64, 41, 0.9);
    }
  }

  .hall-link {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 6px;
    padding: 8px 16px;
    background: linear-gradient(135deg, rgba(44, 36, 32, 0.04), rgba(201, 168, 76, 0.08));
    border: 1px dashed rgba(201, 168, 76, 0.35);
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.25s ease;
    font-size: 13px;
    color: var(--text-color);
    letter-spacing: 0.5px;

    .hall-icon {
      font-size: 16px;
    }

    .link-arrow {
      margin-left: auto;
      font-size: 12px;
      color: rgba(166, 64, 41, 0.7);
      transition: color 0.2s;
    }

    &:hover {
      border-color: rgba(201, 168, 76, 0.6);
      background: linear-gradient(135deg, rgba(44, 36, 32, 0.06), rgba(201, 168, 76, 0.12));

      .link-arrow {
        color: rgba(166, 64, 41, 0.9);
      }
    }
  }
}

/* 加载动画 */
.dot-typing {
  display: flex;
  gap: 6px;
  padding: 4px 0;

  span {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: rgba(201, 168, 76, 0.5);
    animation: dotPulse 1.2s infinite ease-in-out;

    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }
}

@keyframes dotPulse {
  0%, 80%, 100% { opacity: 0.3; transform: scale(0.8); }
  40% { opacity: 1; transform: scale(1); }
}

/* 输入区 */
.chat-input-area {
  padding: 16px 20px 20px;
  border-top: 1px solid rgba(201, 168, 76, 0.2);

  .input-frame {
    display: flex;
    gap: 12px;
    align-items: center;
  }

  .chat-input {
    flex: 1;
    padding: 12px 18px;
    border: 1px solid rgba(212, 201, 184, 0.4);
    border-radius: 8px;
    background: #fff;
    font-size: 14px;
    font-family: var(--font-serif);
    color: var(--text-color);
    letter-spacing: 0.5px;
    outline: none;
    transition: border-color 0.2s;

    &:focus { border-color: rgba(201, 168, 76, 0.5); }
    &::placeholder { color: rgba(140, 126, 116, 0.4); }
  }

  .send-btn {
    padding: 12px 28px;
    background: linear-gradient(135deg, rgba(166, 64, 41, 0.9), rgba(140, 50, 30, 0.95));
    border: none;
    border-radius: 8px;
    color: #fff;
    font-family: var(--font-serif);
    font-size: 14px;
    letter-spacing: 3px;
    cursor: pointer;
    transition: all 0.2s;
    box-shadow: 0 4px 16px rgba(166, 64, 41, 0.25);

    &:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(166, 64, 41, 0.35);
    }

    &:disabled { opacity: 0.5; cursor: not-allowed; }
  }

  .input-hint {
    margin-top: 10px;
    font-size: 12px;
    color: var(--text-light);
    letter-spacing: 1px;
    text-align: center;
  }
}
</style>
