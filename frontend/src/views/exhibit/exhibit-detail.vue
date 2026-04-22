<template>
  <!-- 展品详情大屏容器 start -->
  <div class="exhibit-screen">
    <!-- 背景装饰元素 start -->
    <div class="bg-decorations">
      <div class="bg-grain"></div>
      <div class="bg-vignette"></div>
      <div class="bg-glow"></div>
    </div>
    <!-- 背景装饰元素 end -->

    <!-- 顶部标题栏 start -->
    <header class="screen-header">
      <div class="back-btn" @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回展厅</span>
      </div>
      <div class="header-center">
        <div class="header-ornament-line"></div>
        <h1 class="main-title">非遗展品鉴赏</h1>
        <div class="header-ornament-line"></div>
      </div>
    </header>
    <!-- 顶部标题栏 end -->

    <!-- 主内容区域 - 三栏布局 start -->
    <main class="screen-content">
      <!-- 左侧信息卡片区 start -->
      <aside class="left-panel">
        <!-- 展品名称卡片 -->
        <div class="info-card card-small stagger-1">
          <div class="card-accent"></div>
          <div class="card-inner">
            <div class="card-header">
              <span class="header-label">展品名称</span>
            </div>
            <div class="card-body title-body">
              <h2 class="exhibit-name">{{ exhibitInfo.name || '加载中...' }}</h2>
              <div class="exhibit-tags">
                <span class="tag" v-if="exhibitInfo.category">{{ exhibitInfo.category }}</span>
                <span class="tag" v-if="exhibitInfo.era">{{ exhibitInfo.era }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 展品简介卡片 -->
        <div class="info-card card-large stagger-2">
          <div class="card-accent"></div>
          <div class="card-inner">
            <div class="card-header">
              <span class="header-label">展品简介</span>
              <button class="voice-btn" :class="{ playing: currentSpeaking === 'description' }" @click="toggleSpeech('description', exhibitInfo.description || '暂无简介信息')" :title="currentSpeaking === 'description' ? '停止朗读' : '语音朗读'">
                <span class="voice-icon">{{ currentSpeaking === 'description' ? '■' : '▶' }}</span>
              </button>
            </div>
            <div class="card-body">
              <p class="card-text">{{ exhibitInfo.description || '暂无简介信息' }}</p>
            </div>
          </div>
        </div>

        <!-- 历史渊源卡片 -->
        <div class="info-card card-large stagger-3">
          <div class="card-accent"></div>
          <div class="card-inner">
            <div class="card-header">
              <span class="header-label">历史渊源</span>
              <button class="voice-btn" :class="{ playing: currentSpeaking === 'history' }" @click="toggleSpeech('history', exhibitInfo.history || '暂无历史信息')" :title="currentSpeaking === 'history' ? '停止朗读' : '语音朗读'">
                <span class="voice-icon">{{ currentSpeaking === 'history' ? '■' : '▶' }}</span>
              </button>
            </div>
            <div class="card-body">
              <p class="card-text">{{ exhibitInfo.history || '暂无历史信息' }}</p>
            </div>
          </div>
        </div>
      </aside>
      <!-- 左侧信息卡片区 end -->

      <!-- 中间3D模型展示区 start -->
      <section class="center-panel">
        <div class="model-stage">
          <!-- 装饰环 -->
          <div class="stage-ring ring-outer"></div>
          <div class="stage-ring ring-inner"></div>
          <!-- 底座光晕 -->
          <div class="stage-glow"></div>
          
          <!-- 3D模型容器 -->
          <div class="model-area">
            <div v-if="isLoading" class="loading-overlay">
              <div class="loading-orbit"></div>
              <div class="loading-percent">{{ Math.floor(loadingProgress) }}%</div>
              <p class="loading-hint">模型加载中</p>
            </div>
            <div ref="modelContainer" class="model-container"></div>
          </div>
          
          <!-- 控制按钮 -->
          <div class="model-controls">
            <button class="ctrl-btn" :class="{ active: autoRotate }" @click="toggleAutoRotate" title="自动旋转">⟳</button>
            <button class="ctrl-btn" @click="handleResetView" title="重置视角">⊙</button>
          </div>
        </div>
        
        <!-- 底部操作提示 -->
        <div class="tips-bar">
          <kbd>拖拽</kbd><span class="tip-desc">旋转</span>
          <span class="tip-sep"></span>
          <kbd>滚轮</kbd><span class="tip-desc">缩放</span>
        </div>
      </section>
      <!-- 中间3D模型展示区 end -->

      <!-- 右侧信息卡片区 start -->
      <aside class="right-panel">
        <!-- 展品数据卡片 -->
        <div class="info-card card-small stagger-1">
          <div class="card-accent"></div>
          <div class="card-inner">
            <div class="card-header">
              <span class="header-label">展品数据</span>
              <button class="voice-btn" :class="{ playing: currentSpeaking === 'data' }" @click="toggleSpeech('data', dataCardText)" :title="currentSpeaking === 'data' ? '停止朗读' : '语音朗读'">
                <span class="voice-icon">{{ currentSpeaking === 'data' ? '■' : '▶' }}</span>
              </button>
            </div>
            <div class="card-body">
              <div class="data-list">
                <div class="data-row">
                  <span class="label">分类</span>
                  <span class="value">{{ exhibitInfo.category || '-' }}</span>
                </div>
                <div class="data-row">
                  <span class="label">年代</span>
                  <span class="value">{{ exhibitInfo.era || '-' }}</span>
                </div>
                <div class="data-row">
                  <span class="label">产地</span>
                  <span class="value">{{ exhibitInfo.origin || '-' }}</span>
                </div>
                <div class="data-row">
                  <span class="label">编号</span>
                  <span class="value">{{ exhibitInfo.id || '-' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 制作工艺卡片 -->
        <div class="info-card card-large stagger-2">
          <div class="card-accent"></div>
          <div class="card-inner">
            <div class="card-header">
              <span class="header-label">制作工艺</span>
              <button class="voice-btn" :class="{ playing: currentSpeaking === 'craft' }" @click="toggleSpeech('craft', exhibitInfo.craft || '暂无工艺信息')" :title="currentSpeaking === 'craft' ? '停止朗读' : '语音朗读'">
                <span class="voice-icon">{{ currentSpeaking === 'craft' ? '■' : '▶' }}</span>
              </button>
            </div>
            <div class="card-body">
              <p class="card-text">{{ exhibitInfo.craft || '暂无工艺信息' }}</p>
            </div>
          </div>
        </div>

        <!-- 文化价值卡片 -->
        <div class="info-card card-large stagger-3">
          <div class="card-accent"></div>
          <div class="card-inner">
            <div class="card-header">
              <span class="header-label">文化价值</span>
              <button class="voice-btn" :class="{ playing: currentSpeaking === 'value' }" @click="toggleSpeech('value', exhibitInfo.value || '暂无价值信息')" :title="currentSpeaking === 'value' ? '停止朗读' : '语音朗读'">
                <span class="voice-icon">{{ currentSpeaking === 'value' ? '■' : '▶' }}</span>
              </button>
            </div>
            <div class="card-body">
              <p class="card-text">{{ exhibitInfo.value || '暂无价值信息' }}</p>
            </div>
          </div>
        </div>
      </aside>
      <!-- 右侧信息卡片区 end -->
    </main>
    <!-- 主内容区域 end -->

    <!-- 底部装饰 start -->
    <footer class="screen-footer">
      <span class="footer-text">非物质文化遗产数字化保护与传承</span>
    </footer>
    <!-- 底部装饰 end -->
  </div>
  <!-- 展品详情大屏容器 end -->
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Refresh, Aim } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { ModelViewer } from '@/utils/three/model-viewer'
import { getExhibitByName } from '@/api/exhibit'

const router = useRouter()
const route = useRoute()

// DOM引用
const modelContainer = ref(null)

// 状态
const isLoading = ref(true)
const loadingProgress = ref(0)
const autoRotate = ref(true)

// 模型查看器实例
let modelViewer = null

// 语音朗读
const currentSpeaking = ref('')  // 当前正在朗读的卡片key
let speechUtterance = null

// 展品数据卡片的文本内容
const dataCardText = computed(() => {
  const parts = []
  if (exhibitInfo.category) parts.push(`分类：${exhibitInfo.category}`)
  if (exhibitInfo.era) parts.push(`年代：${exhibitInfo.era}`)
  if (exhibitInfo.origin) parts.push(`产地：${exhibitInfo.origin}`)
  if (exhibitInfo.id) parts.push(`编号：${exhibitInfo.id}`)
  return parts.length > 0 ? parts.join('，') : '暂无展品数据'
})

// 展品信息
const exhibitInfo = reactive({
  id: '',
  name: '',
  category: '',
  era: '',
  origin: '',
  modelPath: '',
  description: '',
  history: '',
  craft: '',
  value: ''
})

// 加载展品数据（通过名称查询）
const loadExhibitData = async (exhibitName) => {
  try {
    const res = await getExhibitByName(exhibitName)
    if (res.data) {
      exhibitInfo.id = res.data.id
      exhibitInfo.name = res.data.name || ''
      exhibitInfo.category = res.data.category || ''
      exhibitInfo.era = res.data.era || ''
      exhibitInfo.origin = res.data.origin || ''
      exhibitInfo.modelPath = res.data.modelPath || ''
      exhibitInfo.description = res.data.description || ''
      exhibitInfo.history = res.data.history || ''
      exhibitInfo.craft = res.data.craft || ''
      exhibitInfo.value = res.data.culturalValue || ''
    }
  } catch (error) {
    console.error('获取展品信息失败:', error)
    ElMessage.error('获取展品信息失败')
  }
}

// 初始化
onMounted(async () => {
  // 获取路由中的展品名称并解码
  const exhibitName = decodeURIComponent(route.params.name)
  
  // 从后端加载展品数据
  await loadExhibitData(exhibitName)
  
  // 获取模型路径
  const modelPath = exhibitInfo.modelPath
  console.log('加载模型:', modelPath)
  
  // 初始化3D模型查看器
  if (modelContainer.value && modelPath) {
    modelViewer = new ModelViewer({
      container: modelContainer.value,
      autoRotate: true,
      onProgress: (progress) => {
        loadingProgress.value = progress
      },
      onComplete: () => {
        isLoading.value = false
      },
      onError: (error) => {
        console.error('模型加载失败:', error)
        isLoading.value = false
        ElMessage.warning('3D模型加载失败，请检查模型文件')
      }
    })
    
    await modelViewer.init()
    await modelViewer.loadModel(modelPath)
  } else if (!modelPath) {
    isLoading.value = false
    ElMessage.warning('该展品暂无3D模型')
  }
})

// 销毁
onBeforeUnmount(() => {
  stopSpeech()
  if (modelViewer) {
    modelViewer.dispose()
    modelViewer = null
  }
})

// 返回展厅
const handleBack = () => {
  router.push('/exhibition-hall')
}

// 切换自动旋转
const toggleAutoRotate = () => {
  autoRotate.value = !autoRotate.value
  if (modelViewer) {
    modelViewer.setAutoRotate(autoRotate.value)
  }
}

// 重置视角
const handleResetView = () => {
  if (modelViewer) {
    modelViewer.resetView()
  }
}

// 语音朗读切换
const toggleSpeech = (key, text) => {
  // 如果当前正在朗读同一个卡片，停止
  if (currentSpeaking.value === key) {
    stopSpeech()
    return
  }
  
  // 停止之前的朗读
  stopSpeech()
  
  // 开始新的朗读
  if (!window.speechSynthesis) {
    ElMessage.warning('您的浏览器不支持语音朗读功能')
    return
  }
  
  speechUtterance = new SpeechSynthesisUtterance(text)
  speechUtterance.lang = 'zh-CN'
  speechUtterance.rate = 0.9
  speechUtterance.pitch = 1
  
  speechUtterance.onend = () => {
    currentSpeaking.value = ''
    speechUtterance = null
  }
  speechUtterance.onerror = () => {
    currentSpeaking.value = ''
    speechUtterance = null
  }
  
  currentSpeaking.value = key
  window.speechSynthesis.speak(speechUtterance)
}

// 停止朗读
const stopSpeech = () => {
  if (window.speechSynthesis) {
    window.speechSynthesis.cancel()
  }
  currentSpeaking.value = ''
  speechUtterance = null
}
</script>

<style scoped lang="scss">
/* ═══════════════════════════════════════════
   展品详情 — 典雅博物馆图录风格
   ═══════════════════════════════════════════ */

$gold: #c9a84c;
$gold-dim: rgba(201, 168, 76, 0.35);
$gold-glow: rgba(201, 168, 76, 0.5);
$ivory: #f5f0e8;
$ivory-dim: rgba(245, 240, 232, 0.06);
$cinnabar: #a64029;
$bg-deep: #100e0b;
$bg-warm: #1a1714;
$card-bg: rgba(26, 23, 20, 0.8);
$card-border: rgba(201, 168, 76, 0.2);
$text-bright: rgba(245, 240, 232, 0.92);
$text-muted: rgba(245, 240, 232, 0.5);
$glass-blur: blur(16px);
$font-display: 'Noto Serif SC', '宋体', serif;

/* ── 容器 ── */
.exhibit-screen {
  width: 100%;
  height: 100vh;
  background: $bg-deep;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  padding: 10px 16px;
  box-sizing: border-box;
}

/* ── 背景装饰 ── */
.bg-decorations {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;

  .bg-grain {
    position: absolute;
    inset: 0;
    background:
      repeating-linear-gradient(0deg, transparent 0px, transparent 59px, rgba(255,255,255,0.012) 60px),
      repeating-linear-gradient(90deg, transparent 0px, transparent 59px, rgba(255,255,255,0.012) 60px);
  }

  .bg-vignette {
    position: absolute;
    inset: 0;
    background: radial-gradient(ellipse 70% 60% at 50% 50%, transparent 30%, rgba(0,0,0,0.5) 100%);
  }

  .bg-glow {
    position: absolute;
    top: 45%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 600px;
    height: 600px;
    background: radial-gradient(circle, rgba($gold, 0.06) 0%, transparent 65%);
    border-radius: 50%;
  }
}

/* ── 顶部标题栏 ── */
.screen-header {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 6px 0 10px;
  z-index: 10;

  .back-btn {
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 7px 16px;
    background: $card-bg;
    backdrop-filter: $glass-blur;
    border: 1px solid $card-border;
    border-radius: 4px;
    color: $gold;
    cursor: pointer;
    transition: all 0.3s;
    font-size: 13px;
    letter-spacing: 1px;

    &:hover {
      border-color: $gold-glow;
      background: rgba($gold, 0.08);
      box-shadow: 0 0 16px rgba($gold, 0.15);
    }
  }

  .header-center {
    display: flex;
    align-items: center;
    gap: 24px;
  }

  .header-ornament-line {
    width: 80px;
    height: 1px;
    background: linear-gradient(90deg, transparent, $gold-dim, transparent);
  }

  .main-title {
    font-family: $font-display;
    font-size: 24px;
    font-weight: 600;
    color: $gold;
    letter-spacing: 10px;
    margin: 0;
    text-shadow: 0 0 20px rgba($gold, 0.25);
  }
}

/* ── 主内容区域 ── */
.screen-content {
  flex: 1;
  display: grid;
  grid-template-columns: 340px 1fr 340px;
  gap: 12px;
  min-height: 0;
  z-index: 10;
}

/* ── 侧边面板 ── */
.left-panel, .right-panel {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* ── 信息卡片 ── */
.info-card {
  display: flex;
  border-radius: 6px;
  overflow: hidden;
  flex: 1;
  background: $card-bg;
  backdrop-filter: $glass-blur;
  border: 1px solid $card-border;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.3);
  opacity: 0;
  animation: cardEnter 0.5s ease-out forwards;

  &.stagger-1 { animation-delay: 0.1s; }
  &.stagger-2 { animation-delay: 0.25s; }
  &.stagger-3 { animation-delay: 0.4s; }

  &.card-small {
    flex: 0 0 auto;
  }

  &.card-large {
    flex: 1;
  }

  &:hover {
    border-color: $gold-glow;
    box-shadow: 0 4px 32px rgba($gold, 0.08);
  }

  transition: border-color 0.3s, box-shadow 0.3s;
}

@keyframes cardEnter {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 卡片左侧金色竖条 */
.card-accent {
  width: 3px;
  background: linear-gradient(180deg, $gold, rgba($gold, 0.2));
  flex-shrink: 0;
}

.card-inner {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);

  .header-label {
    font-family: $font-display;
    font-size: 13px;
    font-weight: 500;
    color: $gold;
    letter-spacing: 3px;
  }

  .voice-btn {
    margin-left: auto;
    width: 28px;
    height: 28px;
    border-radius: 4px;
    border: 1px solid rgba($gold, 0.25);
    background: rgba($gold, 0.05);
    cursor: pointer;
    transition: all 0.25s;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0;
    flex-shrink: 0;

    .voice-icon {
      font-size: 10px;
      color: $gold;
      line-height: 1;
    }

    &:hover {
      border-color: $gold-glow;
      background: rgba($gold, 0.1);
    }

    &.playing {
      border-color: $gold;
      background: rgba($gold, 0.15);
      box-shadow: 0 0 10px rgba($gold, 0.2);
    }
  }
}

.card-body {
  padding: 12px 14px;
  flex: 1;
  overflow-y: auto;

  &::-webkit-scrollbar { width: 3px; }
  &::-webkit-scrollbar-thumb { background: rgba($gold, 0.15); border-radius: 2px; }

  &.title-body {
    display: flex;
    flex-direction: column;
    justify-content: center;
    padding: 12px 14px;

    .exhibit-name {
      font-family: $font-display;
      color: $text-bright;
      font-size: 20px;
      font-weight: 600;
      margin: 0 0 8px;
      letter-spacing: 3px;
    }

    .exhibit-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;

      .tag {
        padding: 3px 12px;
        background: rgba($gold, 0.08);
        border: 1px solid $gold-dim;
        border-radius: 3px;
        color: $gold;
        font-size: 12px;
        letter-spacing: 1px;
      }
    }
  }
}

.card-text {
  color: $text-muted;
  font-size: 13.5px;
  line-height: 1.85;
  margin: 0;
}

.data-list {
  .data-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 7px 0;
    border-bottom: 1px solid rgba(255, 255, 255, 0.04);

    &:last-child { border-bottom: none; }

    .label {
      color: $text-muted;
      font-size: 13px;
      letter-spacing: 1px;
    }

    .value {
      color: $text-bright;
      font-family: $font-display;
      font-size: 14px;
      font-weight: 500;
    }
  }
}

/* ── 中间展示区 ── */
.center-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  animation: centerFadeIn 0.6s ease-out 0.15s both;
}

@keyframes centerFadeIn {
  from { opacity: 0; transform: scale(0.97); }
  to { opacity: 1; transform: scale(1); }
}

.model-stage {
  position: relative;
  width: 100%;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;

  .stage-ring {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    border-radius: 50%;
    pointer-events: none;

    &.ring-outer {
      width: 100%;
      height: 0;
      padding-bottom: 100%;
      max-width: 640px;
      max-height: 640px;
      border: 1.5px solid rgba($gold, 0.12);
      animation: ringRotate 60s linear infinite;
    }

    &.ring-inner {
      width: 96%;
      height: 0;
      padding-bottom: 96%;
      max-width: 610px;
      max-height: 610px;
      border: 1px dashed rgba($gold, 0.18);
      animation: ringRotate 45s linear infinite reverse;
    }
  }

  .stage-glow {
    position: absolute;
    bottom: 5%;
    width: 90%;
    max-width: 540px;
    height: 50px;
    background: radial-gradient(ellipse, rgba($gold, 0.25) 0%, rgba($gold, 0.1) 40%, transparent 70%);
    border-radius: 50%;
    filter: blur(20px);
  }

  .model-area {
    position: relative;
    width: 92%;
    max-width: 560px;
    aspect-ratio: 1;
    border-radius: 50%;
    overflow: hidden;
    background: radial-gradient(circle, rgba(20, 18, 14, 0.95) 0%, $bg-deep 100%);
    border: 2px solid rgba($gold, 0.25);
    box-shadow:
      0 0 60px rgba($gold, 0.1),
      0 0 120px rgba($gold, 0.05),
      inset 0 0 80px rgba($gold, 0.04);

    .model-container {
      width: 100%;
      height: 100%;
      border-radius: 50%;
    }

    .loading-overlay {
      position: absolute;
      inset: 0;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background: rgba($bg-deep, 0.92);
      border-radius: 50%;
      z-index: 10;

      .loading-orbit {
        width: 64px;
        height: 64px;
        border: 2px solid rgba($gold, 0.15);
        border-top-color: $gold;
        border-radius: 50%;
        animation: spin 1.2s linear infinite;
      }

      .loading-percent {
        margin-top: 16px;
        font-family: $font-display;
        font-size: 22px;
        font-weight: 600;
        color: $gold;
        letter-spacing: 2px;
        text-shadow: 0 0 12px rgba($gold, 0.3);
      }

      .loading-hint {
        margin-top: 8px;
        color: $text-muted;
        font-size: 13px;
        letter-spacing: 2px;
      }
    }
  }

  .model-controls {
    position: absolute;
    bottom: 24px;
    right: 24px;
    display: flex;
    gap: 8px;
    z-index: 15;

    .ctrl-btn {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      border: 1px solid $card-border;
      background: $card-bg;
      backdrop-filter: $glass-blur;
      color: $gold;
      cursor: pointer;
      transition: all 0.25s;
      font-size: 18px;
      display: flex;
      align-items: center;
      justify-content: center;

      &:hover {
        border-color: $gold-glow;
        background: rgba($gold, 0.1);
      }

      &.active {
        border-color: $gold;
        background: rgba($gold, 0.12);
        box-shadow: 0 0 12px rgba($gold, 0.2);
      }
    }
  }
}

@keyframes ringRotate {
  from { transform: translate(-50%, -50%) rotate(0deg); }
  to { transform: translate(-50%, -50%) rotate(360deg); }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ── 操作提示 ── */
.tips-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 8px 0 0;

  kbd {
    display: inline-flex;
    align-items: center;
    padding: 3px 10px;
    background: rgba(255, 255, 255, 0.04);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-bottom-width: 2px;
    border-radius: 4px;
    font-family: $font-display;
    font-size: 11px;
    color: rgba(255, 255, 255, 0.5);
  }

  .tip-desc {
    font-size: 11px;
    color: $text-muted;
    letter-spacing: 2px;
    margin-right: 6px;
  }

  .tip-sep {
    width: 1px;
    height: 14px;
    background: rgba(255, 255, 255, 0.08);
    margin: 0 4px;
  }
}

/* ── 底部 ── */
.screen-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 6px 0 3px;
  z-index: 10;

  .footer-text {
    color: rgba(255, 255, 255, 0.2);
    font-size: 11px;
    letter-spacing: 6px;
  }
}

/* ── 响应式 ── */
@media (max-width: 1400px) {
  .screen-content {
    grid-template-columns: 280px 1fr 280px;
  }
}

@media (max-width: 1200px) {
  .screen-content {
    grid-template-columns: 240px 1fr 240px;
    gap: 10px;
  }

  .card-header .header-label { font-size: 12px; }
  .card-text { font-size: 13px; }
}

@media (max-width: 900px) {
  .screen-content {
    grid-template-columns: 1fr;
    grid-template-rows: auto 1fr auto;
  }

  .left-panel, .right-panel {
    flex-direction: row;
    overflow-x: auto;
    gap: 10px;

    .info-card {
      min-width: 220px;
      flex: none;
    }
  }

  .center-panel { min-height: 400px; }

  .screen-header {
    .header-ornament-line { display: none; }
    .main-title { font-size: 20px; letter-spacing: 5px; }
    .back-btn { position: relative; left: 0; transform: none; margin-right: auto; }
  }
}
</style>
