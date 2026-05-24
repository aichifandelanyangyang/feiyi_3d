<template>
  <div class="ai-assistant">
    <!-- 浮动按钮 -->
    <button class="ai-fab" :class="{ active: isOpen }" @click="toggleChat">
      <span class="fab-icon" v-if="!isOpen">智</span>
      <span class="fab-close" v-else>✕</span>
    </button>

    <!-- 聊天面板 -->
    <transition name="chat-slide">
      <div v-if="isOpen" class="chat-panel">
        <!-- 头部 -->
        <div class="chat-header">
          <div class="header-seal">智</div>
          <div class="header-info">
            <h3 class="header-title">非遗智识</h3>
            <p class="header-sub">AI知识助手</p>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="chat-messages" ref="messagesRef">
          <div
            v-for="(msg, index) in messages"
            :key="index"
            class="message-item"
            :class="msg.role"
          >
            <div class="message-avatar">
              <span v-if="msg.role === 'assistant'">智</span>
              <span v-else>我</span>
            </div>
            <div class="message-content">
              <div class="message-bubble" v-html="formatMessage(msg.content)"></div>
              <div class="message-sources" v-if="msg.sources && msg.sources.length">
                <span class="sources-label">参考：</span>
                <span class="source-tag" v-for="(s, i) in msg.sources" :key="i">{{ s }}</span>
              </div>
            </div>
          </div>

          <!-- 加载动画 -->
          <div v-if="loading" class="message-item assistant">
            <div class="message-avatar"><span>智</span></div>
            <div class="message-content">
              <div class="message-bubble loading-bubble">
                <span class="dot-typing">
                  <span></span><span></span><span></span>
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区 -->
        <div class="chat-input">
          <input
            v-model="inputText"
            placeholder="请输入您想了解的非遗知识..."
            @keyup.enter="sendMessage"
            :disabled="loading"
          />
          <button class="send-btn" @click="sendMessage" :disabled="loading || !inputText.trim()">
            <span>发送</span>
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { aiChatStream } from '@/api/ai'

const isOpen = ref(false)
const inputText = ref('')
const loading = ref(false)
const messagesRef = ref(null)

const messages = ref([
  {
    role: 'assistant',
    content: '您好！我是「非遗智识」——您的非物质文化遗产知识助手。\n\n您可以向我询问关于非遗项目、传统技艺、文化传承等方面的问题，我将竭诚为您解答。'
  }
])

const toggleChat = () => {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    nextTick(() => scrollToBottom())
  }
}

const scrollToBottom = () => {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
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
      html += `<h4 class="md-h4">${formatInline(match[1])}</h4>`
      continue
    }
    if ((match = h3Re.exec(line))) {
      if (inList) { html += '</ul>'; inList = false }
      if (inOl) { html += '</ol>'; inOl = false }
      html += `<h3 class="md-h3">${formatInline(match[1])}</h3>`
      continue
    }
    if ((match = h2Re.exec(line))) {
      if (inList) { html += '</ul>'; inList = false }
      if (inOl) { html += '</ol>'; inOl = false }
      html += `<h2 class="md-h2">${formatInline(match[1])}</h2>`
      continue
    }
    if ((match = h1Re.exec(line))) {
      if (inList) { html += '</ul>'; inList = false }
      if (inOl) { html += '</ol>'; inOl = false }
      html += `<h1 class="md-h1">${formatInline(match[1])}</h1>`
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

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  loading.value = true
  await nextTick()
  scrollToBottom()

  // 先添加空的 AI 消息占位
  const msgIndex = messages.value.length
  messages.value.push({ role: 'assistant', content: '', sources: [] })

  // 构建历史消息
  const history = messages.value
    .slice(-11, -1)
    .filter(m => m.role === 'user' || m.role === 'assistant')
    .map(m => ({ role: m.role, content: m.content }))

  // 调用 SSE 流式 API
  aiChatStream(
    { message: text, history },
    {
      onMetadata: (data) => {
        messages.value[msgIndex].sources = data.sources || []
        loading.value = false
      },
      onContent: (textChunk) => {
        messages.value[msgIndex].content += textChunk
        scrollToBottom()
      },
      onDone: () => {
        // 流式结束
      },
      onError: (errMsg) => {
        if (!messages.value[msgIndex].content) {
          messages.value[msgIndex].content = errMsg || '服务异常，请稍后再试'
        }
        loading.value = false
      }
    }
  )
}
</script>

<style scoped>
.ai-assistant {
  position: fixed;
  bottom: 32px;
  right: 32px;
  z-index: 9999;
  font-family: 'Noto Serif SC', 'Source Han Serif CN', serif;
}

/* 浮动按钮 */
.ai-fab {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: 2px solid rgba(201, 168, 76, 0.6);
  background: linear-gradient(145deg, #3a2e24, #2a2018);
  color: #e7d396;
  font-size: 22px;
  cursor: pointer;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.3), 0 0 20px rgba(201, 168, 76, 0.15);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  position: absolute;
  bottom: 0;
  right: 0;
}

.ai-fab:hover {
  transform: scale(1.1);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4), 0 0 30px rgba(201, 168, 76, 0.25);
}

.ai-fab.active {
  background: linear-gradient(145deg, #4a3e34, #3a2e24);
}

.fab-icon {
  font-family: 'Noto Serif SC', serif;
  font-weight: 700;
}

.fab-close {
  font-size: 18px;
}

/* 聊天面板 */
.chat-panel {
  position: absolute;
  bottom: 72px;
  right: 0;
  width: 400px;
  height: 560px;
  background: linear-gradient(170deg, #fefcf7 0%, #f8f4eb 100%);
  border: 1px solid rgba(201, 168, 76, 0.3);
  border-radius: 12px;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.15), 0 0 1px rgba(201, 168, 76, 0.3);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 头部 */
.chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #3a2e24 0%, #2a2018 100%);
  border-bottom: 2px solid rgba(201, 168, 76, 0.4);
}

.header-seal {
  width: 38px;
  height: 38px;
  border: 1.5px solid rgba(201, 168, 76, 0.6);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #e7d396;
  font-weight: 700;
}

.header-title {
  font-size: 16px;
  color: #e7d396;
  letter-spacing: 4px;
  margin: 0;
  font-weight: 600;
}

.header-sub {
  font-size: 11px;
  color: rgba(231, 211, 150, 0.5);
  letter-spacing: 2px;
  margin: 2px 0 0;
}

/* 消息列表 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chat-messages::-webkit-scrollbar {
  width: 4px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: rgba(201, 168, 76, 0.25);
  border-radius: 2px;
}

/* 消息项 */
.message-item {
  display: flex;
  gap: 10px;
  max-width: 90%;
}

.message-item.user {
  flex-direction: row-reverse;
  align-self: flex-end;
}

.message-item.assistant {
  align-self: flex-start;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.message-item.assistant .message-avatar {
  background: linear-gradient(135deg, #3a2e24, #2a2018);
  color: #e7d396;
  border: 1px solid rgba(201, 168, 76, 0.4);
}

.message-item.user .message-avatar {
  background: linear-gradient(135deg, rgba(166, 64, 41, 0.85), rgba(140, 50, 30, 0.9));
  color: #fff;
}

.message-bubble {
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 13.5px;
  line-height: 1.7;
  letter-spacing: 0.5px;

  /* Markdown 样式 */
  h1, h2, h3, h4, h5, h6 {
    font-family: 'Noto Serif SC', 'Source Han Serif CN', serif;
    margin: 0;
    padding: 0;
    font-weight: inherit;
    line-height: 1.5;
  }

  .md-h1, .md-h2, .md-h3, .md-h4 {
    font-family: 'Noto Serif SC', 'Source Han Serif CN', serif;
    color: #3d3429;
    margin: 10px 0 8px 0;
    letter-spacing: 1px;
    clear: both;
  }
  .md-h1 { font-size: 17px; font-weight: 700; border-bottom: 1px solid rgba(201, 168, 76, 0.25); padding-bottom: 5px; margin-top: 12px; }
  .md-h2 { font-size: 16px; font-weight: 700; margin-top: 10px; }
  .md-h3 { font-size: 14.5px; font-weight: 600; color: rgba(166, 64, 41, 0.85); margin-top: 8px; }
  .md-h4 { font-size: 14px; font-weight: 600; margin-top: 6px; }

  .md-p {
    margin: 4px 0;
  }

  .md-ul, .md-ol {
    margin: 6px 0;
    padding-left: 16px;
  }
  .md-ul { list-style-type: none; }
  .md-ul li {
    position: relative;
    margin: 3px 0;
    &:before {
      content: '•';
      position: absolute;
      left: -12px;
      color: rgba(201, 168, 76, 0.7);
      font-size: 10px;
    }
  }
  .md-ol {
    list-style-type: decimal;
    li { margin: 3px 0; }
  }

  .md-code {
    font-family: 'Consolas', 'Monaco', monospace;
    font-size: 12px;
    background: rgba(201, 168, 76, 0.08);
    border: 1px solid rgba(201, 168, 76, 0.15);
    padding: 1px 4px;
    border-radius: 2px;
    color: rgba(166, 64, 41, 0.85);
  }

  strong {
    font-weight: 600;
    color: rgba(166, 64, 41, 0.85);
  }

  em {
    font-style: italic;
  }
}

.message-item.assistant .message-bubble {
  background: #fff;
  color: #3d3429;
  border: 1px solid rgba(212, 201, 184, 0.35);
  border-top-left-radius: 2px;
}

.message-item.user .message-bubble {
  background: linear-gradient(135deg, #3a2e24, #2a2018);
  color: #e7d396;
  border-top-right-radius: 2px;
}

/* 引用来源 */
.message-sources {
  margin-top: 6px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
}

.sources-label {
  font-size: 11px;
  color: rgba(140, 126, 116, 0.6);
}

.source-tag {
  font-size: 11px;
  color: rgba(166, 64, 41, 0.7);
  background: rgba(166, 64, 41, 0.06);
  border: 1px solid rgba(166, 64, 41, 0.15);
  padding: 1px 8px;
  border-radius: 10px;
}

/* 加载动画 */
.loading-bubble {
  padding: 14px 20px !important;
}

.dot-typing {
  display: flex;
  gap: 6px;
}

.dot-typing span {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: rgba(201, 168, 76, 0.5);
  animation: dotPulse 1.2s infinite ease-in-out;
}

.dot-typing span:nth-child(2) {
  animation-delay: 0.2s;
}

.dot-typing span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes dotPulse {
  0%, 80%, 100% {
    opacity: 0.3;
    transform: scale(0.8);
  }
  40% {
    opacity: 1;
    transform: scale(1);
  }
}

/* 输入区 */
.chat-input {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid rgba(212, 201, 184, 0.3);
  background: rgba(255, 253, 248, 0.9);
}

.chat-input input {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid rgba(212, 201, 184, 0.4);
  border-radius: 8px;
  background: #fff;
  font-size: 13px;
  color: #3d3429;
  outline: none;
  font-family: inherit;
  letter-spacing: 0.5px;
  transition: border-color 0.2s;
}

.chat-input input:focus {
  border-color: rgba(201, 168, 76, 0.5);
}

.chat-input input::placeholder {
  color: rgba(140, 126, 116, 0.4);
}

.send-btn {
  padding: 10px 20px;
  background: linear-gradient(135deg, rgba(166, 64, 41, 0.9), rgba(140, 50, 30, 0.95));
  border: none;
  border-radius: 8px;
  color: #fff;
  font-family: inherit;
  font-size: 13px;
  letter-spacing: 2px;
  cursor: pointer;
  transition: all 0.2s;
}

.send-btn:hover:not(:disabled) {
  box-shadow: 0 4px 12px rgba(166, 64, 41, 0.3);
  transform: translateY(-1px);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 过渡动画 */
.chat-slide-enter-active,
.chat-slide-leave-active {
  transition: all 0.3s ease;
}

.chat-slide-enter-from,
.chat-slide-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}
</style>
