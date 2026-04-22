<template>
  <!-- 虚拟展厅容器 -->
  <div class="exhibition-hall-container">
    <!-- Three.js 渲染容器 -->
    <div ref="threeContainer" class="three-container"></div>

    <!-- 加载进度 start -->
    <transition name="loading-fade">
      <div v-if="isLoading" class="loading-overlay">
        <div class="loading-bg-pattern"></div>
        <div class="loading-content">
          <div class="loading-seal">
            <div class="seal-frame">
              <span class="seal-char">展</span>
            </div>
            <div class="seal-orbit" :style="{ '--progress': loadingProgress / 100 }"></div>
          </div>
          <div class="loading-info">
            <h2 class="loading-title">虚拟展厅</h2>
            <p class="loading-subtitle">{{ loadingTip }}</p>
            <div class="loading-bar-track">
              <div class="loading-bar-fill" :style="{ width: loadingProgress + '%' }"></div>
              <div class="loading-bar-glow" :style="{ left: loadingProgress + '%' }"></div>
            </div>
            <span class="loading-percent">{{ Math.floor(loadingProgress) }}%</span>
          </div>
        </div>
      </div>
    </transition>
    <!-- 加载进度 end -->

    <!-- 控制面板 start -->
    <div class="control-panel" :class="{ collapsed: isPanelCollapsed }">
      <div class="panel-corner corner-tl"></div>
      <div class="panel-corner corner-tr"></div>
      <div class="panel-corner corner-bl"></div>
      <div class="panel-corner corner-br"></div>
      <div class="panel-header" @click="togglePanel">
        <span class="panel-title">展厅控制</span>
        <span class="panel-toggle">{{ isPanelCollapsed ? '▶' : '◀' }}</span>
      </div>
      <transition name="panel-slide">
        <div v-show="!isPanelCollapsed" class="panel-body">
          <!-- 漫游模式切换 -->
          <div class="control-item">
            <span class="control-label">漫游模式</span>
            <el-switch 
              v-model="isWalkMode" 
              active-text="第一人称"
              inactive-text="自由视角"
              @change="handleModeChange"
            />
          </div>
          <!-- 移动速度 -->
          <div class="control-item">
            <span class="control-label">移动速度</span>
            <el-slider 
              v-model="moveSpeed" 
              :min="1" 
              :max="10"
              :step="1"
              @change="handleSpeedChange"
            />
          </div>
          <!-- 重置视角 -->
          <div class="control-item actions">
            <button class="hud-btn primary" @click="handleResetCamera">重置视角</button>
            <button class="hud-btn" @click="handleExitHall">退出展厅</button>
          </div>
        </div>
      </transition>
    </div>
    <!-- 控制面板 end -->

    <!-- 背景音乐控制 -->
    <div class="bgm-control" :class="{ playing: isBgmPlaying }" @click="toggleBgm" :title="isBgmPlaying ? '关闭背景音乐' : '开启背景音乐'">
      <div class="bgm-bars">
        <span class="bar"></span><span class="bar"></span><span class="bar"></span><span class="bar"></span>
      </div>
      <span class="bgm-label">{{ isBgmPlaying ? '音乐' : '静音' }}</span>
    </div>

    <!-- 十字准星（第一人称模式） -->
    <div v-show="isWalkMode && !isLoading" class="crosshair">
      <div class="crosshair-h"></div>
      <div class="crosshair-v"></div>
      <div class="crosshair-dot"></div>
      <div class="crosshair-ring" :class="{ active: hoveredExhibitName || hoveredVideoName }"></div>
    </div>
    
    <!-- 展品悬停提示 -->
    <transition name="tooltip-pop">
      <div v-if="hoveredExhibitName" class="exhibit-tooltip">
        <div class="tooltip-accent"></div>
        <div class="tooltip-body">
          <span class="tooltip-label">展品</span>
          <span class="tooltip-name">{{ hoveredExhibitName }}</span>
        </div>
        <span class="tooltip-action">点击查看 →</span>
      </div>
    </transition>
    
    <!-- 视频悬停提示 -->
    <transition name="tooltip-pop">
      <div v-if="hoveredVideoName && !hoveredExhibitName" class="exhibit-tooltip video-tooltip">
        <div class="tooltip-accent"></div>
        <div class="tooltip-body">
          <span class="tooltip-label">影像</span>
          <span class="tooltip-name">{{ hoveredVideoName }}</span>
        </div>
        <span class="tooltip-action">点击播放 →</span>
      </div>
    </transition>

    <!-- 视频播放对话框 -->
    <div v-if="videoDialogVisible" class="video-dialog-overlay" @click.self="closeVideoDialog">
      <div class="video-dialog">
        <div class="video-dialog-header">
          <span class="video-dialog-title">{{ videoDialogTitle }}</span>
          <button class="video-dialog-close" @click="closeVideoDialog">✕</button>
        </div>
        <div class="video-dialog-body">
          <video
            ref="dialogVideoRef"
            :src="videoDialogSrc"
            class="video-player"
            @timeupdate="onVideoTimeUpdate"
            @loadedmetadata="onVideoLoaded"
          ></video>
        </div>
        <div class="video-dialog-controls">
          <button class="control-btn" @click="toggleDialogVideo">
            {{ isDialogPlaying ? '⏸' : '▶' }}
          </button>
          <div class="progress-bar" @click="seekVideo">
            <div class="progress-fill" :style="{ width: videoProgress + '%' }"></div>
          </div>
          <span class="time-display">{{ formatTime(videoCurrentTime) }} / {{ formatTime(videoDuration) }}</span>
          <div class="volume-control">
            <button class="control-btn" @click="toggleMute">
              {{ isDialogMuted ? '🔇' : '🔊' }}
            </button>
            <input
              type="range"
              min="0"
              max="100"
              :value="dialogVolume"
              class="volume-slider"
              @input="changeVolume"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 操作提示 start -->
    <div class="tips-panel" :class="{ 'tips-hidden': tipsHidden }">
      <div class="tips-row">
        <div class="tip-item">
          <kbd>W</kbd><kbd>A</kbd><kbd>S</kbd><kbd>D</kbd>
          <span class="tip-desc">移动</span>
        </div>
        <div class="tip-divider"></div>
        <div class="tip-item">
          <kbd>鼠标</kbd>
          <span class="tip-desc">视角</span>
        </div>
        <div class="tip-divider"></div>
        <div class="tip-item">
          <kbd>滚轮</kbd>
          <span class="tip-desc">缩放</span>
        </div>
      </div>
    </div>
    <!-- 操作提示 end -->

  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ExhibitionScene } from '@/utils/three/exhibition-scene'

const router = useRouter()

// DOM引用
const threeContainer = ref(null)

// 状态变量
const isLoading = ref(true)
const loadingProgress = ref(0)
const loadingTip = ref('初始化场景...')
const isPanelCollapsed = ref(false)
const isWalkMode = ref(true)
const moveSpeed = ref(5)
const hoveredExhibitName = ref('')  // 悬停的展品名称
const hoveredVideoName = ref('')  // 悬停的视频名称
const tipsHidden = ref(false)  // 操作提示自动隐藏

// 背景音乐
const isBgmPlaying = ref(false)
let bgmAudio = null

// 视频对话框状态
const videoDialogVisible = ref(false)
const videoDialogSrc = ref('')
const videoDialogTitle = ref('')
const dialogVideoRef = ref(null)
const isDialogPlaying = ref(false)
const isDialogMuted = ref(false)
const dialogVolume = ref(80)
const videoProgress = ref(0)
const videoCurrentTime = ref(0)
const videoDuration = ref(0)

// Three.js场景实例
let exhibitionScene = null
let tipsTimer = null

// 初始化场景方法
const initScene = async () => {
  if (!threeContainer.value) return

  exhibitionScene = new ExhibitionScene({
    container: threeContainer.value,
    onProgress: (progress, tip) => {
      loadingProgress.value = progress
      loadingTip.value = tip
    },
    onComplete: () => {
      isLoading.value = false
      
      // 模型加载完成后，可以获取模型结构
      console.log('可点击物体列表:', exhibitionScene.getClickableObjectNames())
      
      // 8秒后自动隐藏操作提示
      tipsTimer = setTimeout(() => { tipsHidden.value = true }, 8000)
    },
    // 物体点击回调
    onObjectClick: (objectInfo) => {
      handleObjectClick(objectInfo)
    }
  })

  await exhibitionScene.init()
  
  // 设置展品悬停回调
  exhibitionScene.onExhibitHover = (name) => {
    hoveredExhibitName.value = name || ''
  }
  
  // 设置视频悬停回调
  exhibitionScene.onVideoHover = (name) => {
    if (name) {
      const nameMap = {
        '视频屏幕皮影': '皮影戏',
        '视频屏幕瓷器': '瓷器'
      }
      hoveredVideoName.value = nameMap[name] || name
    } else {
      hoveredVideoName.value = ''
    }
  }
  
  // 设置点击处理器映射（根据实际模型中的物体名称配置）
  // 示例：根据物体名称跳转到对应页面
  // exhibitionScene.setClickHandlers({
  //   '展品1': () => router.push('/heritage-detail/1'),
  //   '展品2': () => router.push('/heritage-detail/2'),
  // })
}

// 处理物体点击事件
const handleObjectClick = (objectInfo) => {
  console.log('点击了物体:', objectInfo.name, '是否为展品:', objectInfo.isExhibit)
  
  // 检查是否点击了视频屏幕
  if (objectInfo.isVideoScreen) {
    openVideoDialog(objectInfo.videoSrc, objectInfo.name)
    return
  }
  
  // 只处理标记为展品的点击（名称包含3d的结构）
  if (!objectInfo.isExhibit) {
    return
  }
  
  // 直接使用展品名称跳转，后端会通过名称模糊匹配查询
  const exhibitName = encodeURIComponent(objectInfo.name)
  router.push(`/exhibit/${exhibitName}`)
}

// 打开视频对话框
const openVideoDialog = (src, name) => {
  // 视频名称映射
  const nameMap = {
    '视频屏幕皮影': '皮影戏',
    '视频屏幕瓷器': '瓷器'
  }
  videoDialogTitle.value = nameMap[name] || name
  videoDialogSrc.value = src
  videoDialogVisible.value = true
  isDialogPlaying.value = false
  videoProgress.value = 0
  videoCurrentTime.value = 0
  
  setTimeout(() => {
    if (dialogVideoRef.value) {
      dialogVideoRef.value.volume = dialogVolume.value / 100
      dialogVideoRef.value.muted = false
      isDialogMuted.value = false
      dialogVideoRef.value.play()
      isDialogPlaying.value = true
    }
  }, 100)
}

// 关闭视频对话框
const closeVideoDialog = () => {
  if (dialogVideoRef.value) {
    dialogVideoRef.value.pause()
  }
  videoDialogVisible.value = false
  isDialogPlaying.value = false
}

// 播放/暂停对话框视频
const toggleDialogVideo = () => {
  if (!dialogVideoRef.value) return
  if (dialogVideoRef.value.paused) {
    dialogVideoRef.value.play()
    isDialogPlaying.value = true
  } else {
    dialogVideoRef.value.pause()
    isDialogPlaying.value = false
  }
}

// 视频时间更新
const onVideoTimeUpdate = () => {
  if (!dialogVideoRef.value) return
  videoCurrentTime.value = dialogVideoRef.value.currentTime
  if (videoDuration.value > 0) {
    videoProgress.value = (videoCurrentTime.value / videoDuration.value) * 100
  }
}

// 视频加载完成
const onVideoLoaded = () => {
  if (dialogVideoRef.value) {
    videoDuration.value = dialogVideoRef.value.duration
  }
}

// 跳转视频进度
const seekVideo = (e) => {
  if (!dialogVideoRef.value) return
  const rect = e.currentTarget.getBoundingClientRect()
  const percent = (e.clientX - rect.left) / rect.width
  dialogVideoRef.value.currentTime = percent * videoDuration.value
}

// 切换静音
const toggleMute = () => {
  if (!dialogVideoRef.value) return
  isDialogMuted.value = !isDialogMuted.value
  dialogVideoRef.value.muted = isDialogMuted.value
}

// 调节音量
const changeVolume = (e) => {
  dialogVolume.value = Number(e.target.value)
  if (dialogVideoRef.value) {
    dialogVideoRef.value.volume = dialogVolume.value / 100
    isDialogMuted.value = dialogVolume.value === 0
    dialogVideoRef.value.muted = isDialogMuted.value
  }
}

// 格式化时间
const formatTime = (seconds) => {
  if (!seconds || isNaN(seconds)) return '0:00'
  const m = Math.floor(seconds / 60)
  const s = Math.floor(seconds % 60)
  return `${m}:${s.toString().padStart(2, '0')}`
}

// 切换面板展开状态
const togglePanel = () => {
  isPanelCollapsed.value = !isPanelCollapsed.value
}

// 切换漫游模式方法
const handleModeChange = (value) => {
  if (exhibitionScene) {
    exhibitionScene.setWalkMode(value)
  }
}

// 调整移动速度方法
const handleSpeedChange = (value) => {
  if (exhibitionScene) {
    exhibitionScene.setMoveSpeed(value)
  }
}

// 重置相机视角方法
const handleResetCamera = () => {
  if (exhibitionScene) {
    exhibitionScene.resetCamera()
  }
}

// 退出展厅方法
const handleExitHall = () => {
  router.push('/')
}

// 背景音乐控制
const initBgm = () => {
  bgmAudio = new Audio('/audio/bgm.mp3')
  bgmAudio.loop = true
  bgmAudio.volume = 0.3
}

const toggleBgm = () => {
  if (!bgmAudio) initBgm()
  if (isBgmPlaying.value) {
    bgmAudio.pause()
    isBgmPlaying.value = false
  } else {
    bgmAudio.play().then(() => {
      isBgmPlaying.value = true
    }).catch(() => {
      isBgmPlaying.value = false
    })
  }
}

// 生命周期 - 挂载
onMounted(() => {
  initScene()
})

// 生命周期 - 卸载前
onBeforeUnmount(() => {
  if (tipsTimer) clearTimeout(tipsTimer)
  if (bgmAudio) {
    bgmAudio.pause()
    bgmAudio = null
  }
  if (exhibitionScene) {
    exhibitionScene.dispose()
    exhibitionScene = null
  }
})
</script>

<style scoped lang="scss">
/* ═══════════════════════════════════════════
   虚拟展厅 — 高级博物馆 HUD 风格
   ═══════════════════════════════════════════ */

$gold: #c9a84c;
$gold-dim: rgba(201, 168, 76, 0.35);
$gold-glow: rgba(201, 168, 76, 0.6);
$cinnabar: #a64029;
$glass-bg: rgba(12, 10, 8, 0.55);
$glass-border: rgba(255, 255, 255, 0.12);
$glass-blur: blur(16px);
$hud-radius: 6px;
$font-display: 'Noto Serif SC', '宋体', serif;

.exhibition-hall-container {
  width: 100%;
  height: 100vh;
  position: relative;
  overflow: hidden;
  background: #0c0a08;
}

.three-container {
  width: 100%;
  height: 100%;
}

/* ── 加载画面 ── */
.loading-overlay {
  position: absolute;
  inset: 0;
  background: #0c0a08;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.loading-bg-pattern {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse 60% 40% at 50% 45%, rgba($gold, 0.06) 0%, transparent 70%),
    repeating-linear-gradient(0deg, transparent 0px, transparent 59px, rgba(255,255,255,0.015) 60px),
    repeating-linear-gradient(90deg, transparent 0px, transparent 59px, rgba(255,255,255,0.015) 60px);
  pointer-events: none;
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 36px;
  position: relative;
  z-index: 1;
  animation: loadingEntrance 0.8s ease-out;
}

@keyframes loadingEntrance {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.loading-seal {
  position: relative;
  width: 100px;
  height: 100px;

  .seal-frame {
    width: 100%;
    height: 100%;
    border: 2px solid $gold-dim;
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;

    &::before, &::after {
      content: '';
      position: absolute;
      width: 8px;
      height: 8px;
    }
    &::before { top: -1px; left: -1px; border-top: 2px solid $gold; border-left: 2px solid $gold; }
    &::after { bottom: -1px; right: -1px; border-bottom: 2px solid $gold; border-right: 2px solid $gold; }
  }

  .seal-char {
    font-family: $font-display;
    font-size: 40px;
    font-weight: 700;
    color: $gold;
    letter-spacing: 2px;
    text-shadow: 0 0 20px rgba($gold, 0.3);
  }

  .seal-orbit {
    position: absolute;
    inset: -8px;
    border-radius: 50%;
    border: 1.5px solid transparent;
    border-top-color: $gold;
    animation: sealSpin 2s linear infinite;
    opacity: 0.6;
  }
}

@keyframes sealSpin {
  to { transform: rotate(360deg); }
}

.loading-info {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;

  .loading-title {
    font-family: $font-display;
    font-size: 28px;
    font-weight: 600;
    color: rgba(255, 255, 255, 0.9);
    letter-spacing: 10px;
    margin: 0;
  }

  .loading-subtitle {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.4);
    letter-spacing: 2px;
    margin: 0;
    min-height: 20px;
  }
}

.loading-bar-track {
  width: 240px;
  height: 2px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 1px;
  position: relative;
  overflow: visible;

  .loading-bar-fill {
    height: 100%;
    background: linear-gradient(90deg, transparent, $gold);
    border-radius: 1px;
    transition: width 0.3s ease;
  }

  .loading-bar-glow {
    position: absolute;
    top: 50%;
    transform: translate(-50%, -50%);
    width: 6px;
    height: 6px;
    background: $gold;
    border-radius: 50%;
    box-shadow: 0 0 12px $gold-glow, 0 0 4px $gold;
    transition: left 0.3s ease;
  }
}

.loading-percent {
  font-family: $font-display;
  font-size: 14px;
  color: rgba($gold, 0.7);
  letter-spacing: 3px;
  font-variant-numeric: tabular-nums;
}

/* 加载退出动画 */
.loading-fade-leave-active {
  transition: opacity 0.6s ease;
}
.loading-fade-leave-to {
  opacity: 0;
}

/* ── 控制面板 ── */
.control-panel {
  position: absolute;
  top: 24px;
  right: 24px;
  width: 260px;
  background: $glass-bg;
  backdrop-filter: $glass-blur;
  -webkit-backdrop-filter: $glass-blur;
  border: 1px solid $glass-border;
  border-radius: $hud-radius;
  z-index: 100;
  transition: width 0.3s ease;

  &.collapsed { width: auto; }
}

/* 面板四角金色点缀 */
.panel-corner {
  position: absolute;
  width: 8px;
  height: 8px;
  border-color: $gold-dim;
  border-style: solid;
  pointer-events: none;
  transition: border-color 0.3s;

  &.corner-tl { top: -1px; left: -1px; border-width: 1.5px 0 0 1.5px; }
  &.corner-tr { top: -1px; right: -1px; border-width: 1.5px 1.5px 0 0; }
  &.corner-bl { bottom: -1px; left: -1px; border-width: 0 0 1.5px 1.5px; }
  &.corner-br { bottom: -1px; right: -1px; border-width: 0 1.5px 1.5px 0; }
}

.control-panel:hover .panel-corner { border-color: $gold-glow; }

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  cursor: pointer;
  user-select: none;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);

  .panel-title {
    font-family: $font-display;
    font-size: 14px;
    font-weight: 500;
    color: rgba(255, 255, 255, 0.85);
    letter-spacing: 3px;
  }

  .panel-toggle {
    font-size: 10px;
    color: rgba(255, 255, 255, 0.35);
    transition: color 0.2s;
  }

  &:hover .panel-toggle { color: $gold; }
}

.panel-body {
  padding: 18px;

  .control-item {
    margin-bottom: 18px;

    &:last-child { margin-bottom: 0; }

    &.actions {
      display: flex;
      gap: 10px;
      padding-top: 4px;
    }

    .control-label {
      display: block;
      margin-bottom: 10px;
      font-size: 12px;
      color: rgba(255, 255, 255, 0.45);
      letter-spacing: 2px;
    }
  }
}

/* 面板展开收起动画 */
.panel-slide-enter-active, .panel-slide-leave-active {
  transition: all 0.25s ease;
  overflow: hidden;
}
.panel-slide-enter-from, .panel-slide-leave-to {
  opacity: 0;
  max-height: 0;
  padding-top: 0;
  padding-bottom: 0;
}

/* HUD按钮 */
.hud-btn {
  flex: 1;
  padding: 9px 0;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.04);
  color: rgba(255, 255, 255, 0.7);
  font-family: $font-display;
  font-size: 13px;
  letter-spacing: 2px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: rgba(255, 255, 255, 0.3);
    background: rgba(255, 255, 255, 0.08);
    color: #fff;
  }

  &.primary {
    border-color: $gold-dim;
    color: $gold;

    &:hover {
      border-color: $gold-glow;
      background: rgba($gold, 0.08);
      box-shadow: 0 0 12px rgba($gold, 0.15);
    }
  }
}

/* ── 背景音乐 ── */
.bgm-control {
  position: absolute;
  top: 24px;
  left: 24px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  background: $glass-bg;
  backdrop-filter: $glass-blur;
  -webkit-backdrop-filter: $glass-blur;
  border: 1px solid $glass-border;
  border-radius: 20px;
  cursor: pointer;
  z-index: 100;
  transition: all 0.3s;
  user-select: none;

  &:hover {
    border-color: rgba(255, 255, 255, 0.25);
  }

  &.playing {
    border-color: $gold-dim;

    .bgm-bars .bar {
      animation-play-state: running;
    }
  }

  .bgm-label {
    color: rgba(255, 255, 255, 0.6);
    font-size: 12px;
    letter-spacing: 2px;
  }
}

.bgm-bars {
  display: flex;
  align-items: flex-end;
  gap: 2px;
  height: 16px;

  .bar {
    width: 3px;
    background: $gold;
    border-radius: 1px;
    animation: barBounce 1.2s ease-in-out infinite;
    animation-play-state: paused;

    &:nth-child(1) { height: 40%; animation-delay: 0s; }
    &:nth-child(2) { height: 70%; animation-delay: 0.15s; }
    &:nth-child(3) { height: 50%; animation-delay: 0.3s; }
    &:nth-child(4) { height: 30%; animation-delay: 0.45s; }
  }
}

@keyframes barBounce {
  0%, 100% { height: 30%; }
  50% { height: 100%; }
}

/* ── 十字准星 ── */
.crosshair {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 200;
  pointer-events: none;
  width: 40px;
  height: 40px;

  .crosshair-h, .crosshair-v {
    position: absolute;
    background: rgba(255, 255, 255, 0.5);
  }

  .crosshair-h {
    width: 16px;
    height: 1px;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
  }

  .crosshair-v {
    width: 1px;
    height: 16px;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
  }

  .crosshair-dot {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 3px;
    height: 3px;
    background: #fff;
    border-radius: 50%;
  }

  .crosshair-ring {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 28px;
    height: 28px;
    border: 1.5px solid rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);

    &.active {
      width: 36px;
      height: 36px;
      border-color: $gold;
      box-shadow: 0 0 16px rgba($gold, 0.4), inset 0 0 8px rgba($gold, 0.1);
      animation: ringPulse 1.5s ease-in-out infinite;
    }
  }
}

@keyframes ringPulse {
  0%, 100% { box-shadow: 0 0 16px rgba($gold, 0.4), inset 0 0 8px rgba($gold, 0.1); }
  50% { box-shadow: 0 0 24px rgba($gold, 0.6), inset 0 0 12px rgba($gold, 0.15); }
}

/* ── 展品/视频悬停提示 ── */
.exhibit-tooltip {
  position: absolute;
  top: calc(50% + 44px);
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 22px 12px 0;
  background: $glass-bg;
  backdrop-filter: $glass-blur;
  -webkit-backdrop-filter: $glass-blur;
  border: 1px solid $gold-dim;
  border-radius: $hud-radius;
  z-index: 200;
  pointer-events: none;
  white-space: nowrap;

  .tooltip-accent {
    width: 3px;
    height: 100%;
    min-height: 32px;
    background: $gold;
    border-radius: 0 2px 2px 0;
    flex-shrink: 0;
  }

  .tooltip-body {
    display: flex;
    flex-direction: column;
    gap: 2px;

    .tooltip-label {
      font-size: 10px;
      color: $gold;
      letter-spacing: 3px;
      text-transform: uppercase;
    }

    .tooltip-name {
      color: #fff;
      font-family: $font-display;
      font-size: 16px;
      font-weight: 500;
      letter-spacing: 1px;
    }
  }

  .tooltip-action {
    font-size: 11px;
    color: rgba(255, 255, 255, 0.35);
    letter-spacing: 1px;
  }

  &.video-tooltip {
    border-color: rgba($cinnabar, 0.4);

    .tooltip-accent { background: $cinnabar; }
    .tooltip-label { color: $cinnabar; }
  }
}

/* 提示弹出动画 */
.tooltip-pop-enter-active {
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.tooltip-pop-leave-active {
  transition: all 0.15s ease;
}
.tooltip-pop-enter-from {
  opacity: 0;
  transform: translateX(-50%) translateY(8px) scale(0.95);
}
.tooltip-pop-leave-to {
  opacity: 0;
  transform: translateX(-50%) scale(0.95);
}

/* ── 操作提示 ── */
.tips-panel {
  position: absolute;
  bottom: 28px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 100;
  transition: opacity 0.6s ease, transform 0.6s ease;

  &.tips-hidden {
    opacity: 0;
    transform: translateX(-50%) translateY(10px);
    pointer-events: none;
  }
}

.tips-row {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 12px 24px;
  background: $glass-bg;
  backdrop-filter: $glass-blur;
  -webkit-backdrop-filter: $glass-blur;
  border: 1px solid $glass-border;
  border-radius: $hud-radius;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 6px;

  kbd {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 26px;
    height: 26px;
    padding: 0 6px;
    background: rgba(255, 255, 255, 0.06);
    border: 1px solid rgba(255, 255, 255, 0.15);
    border-bottom-width: 2px;
    border-radius: 4px;
    font-family: $font-display;
    font-size: 11px;
    color: rgba(255, 255, 255, 0.7);
    letter-spacing: 0.5px;
  }

  .tip-desc {
    font-size: 11px;
    color: rgba(255, 255, 255, 0.35);
    letter-spacing: 2px;
    margin-left: 2px;
  }
}

.tip-divider {
  width: 1px;
  height: 16px;
  background: rgba(255, 255, 255, 0.1);
}

/* ── 视频对话框 ── */
.video-dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.88);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  animation: overlayIn 0.3s ease;
}

@keyframes overlayIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.video-dialog {
  width: 80vw;
  max-width: 960px;
  background: #111;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.8);
  animation: dialogSlideIn 0.35s cubic-bezier(0.16, 1, 0.3, 1);

  .video-dialog-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 24px;
    background: rgba(255, 255, 255, 0.03);
    border-bottom: 1px solid rgba(255, 255, 255, 0.06);

    .video-dialog-title {
      font-family: $font-display;
      color: rgba(255, 255, 255, 0.9);
      font-size: 18px;
      font-weight: 500;
      letter-spacing: 3px;
    }

    .video-dialog-close {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: none;
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 4px;
      color: rgba(255, 255, 255, 0.4);
      font-size: 14px;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        color: #fff;
        border-color: rgba(255, 255, 255, 0.3);
        background: rgba(255, 255, 255, 0.05);
      }
    }
  }

  .video-dialog-body {
    position: relative;
    background: #000;

    .video-player {
      width: 100%;
      display: block;
      max-height: 60vh;
    }
  }

  .video-dialog-controls {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 14px 24px;
    background: rgba(255, 255, 255, 0.03);

    .control-btn {
      width: 36px;
      height: 36px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: none;
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 4px;
      color: rgba(255, 255, 255, 0.8);
      font-size: 16px;
      cursor: pointer;
      transition: all 0.2s;
      flex-shrink: 0;

      &:hover {
        border-color: $gold-dim;
        color: $gold;
        background: rgba($gold, 0.05);
      }
    }

    .progress-bar {
      flex: 1;
      height: 4px;
      background: rgba(255, 255, 255, 0.1);
      border-radius: 2px;
      cursor: pointer;
      position: relative;
      transition: height 0.15s;

      &:hover { height: 6px; }

      .progress-fill {
        height: 100%;
        background: linear-gradient(90deg, $gold-dim, $gold);
        border-radius: 2px;
        transition: width 0.1s linear;
      }
    }

    .time-display {
      color: rgba(255, 255, 255, 0.4);
      font-size: 12px;
      min-width: 85px;
      text-align: center;
      font-variant-numeric: tabular-nums;
      letter-spacing: 0.5px;
    }

    .volume-control {
      display: flex;
      align-items: center;
      gap: 6px;

      .volume-slider {
        width: 70px;
        height: 3px;
        -webkit-appearance: none;
        appearance: none;
        background: rgba(255, 255, 255, 0.12);
        border-radius: 2px;
        outline: none;
        cursor: pointer;

        &::-webkit-slider-thumb {
          -webkit-appearance: none;
          appearance: none;
          width: 12px;
          height: 12px;
          border-radius: 50%;
          background: $gold;
          cursor: pointer;
          box-shadow: 0 0 6px rgba($gold, 0.4);
        }
      }
    }
  }
}

@keyframes dialogSlideIn {
  from { opacity: 0; transform: scale(0.96) translateY(12px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}

/* ── Element Plus 深色覆写 ── */
:deep(.el-switch) {
  --el-switch-on-color: #{$gold};
}

:deep(.el-switch__label) {
  color: rgba(255, 255, 255, 0.4) !important;

  &.is-active {
    color: rgba(255, 255, 255, 0.75) !important;
  }
}

:deep(.el-slider) {
  --el-slider-main-bg-color: #{$gold};
  --el-slider-runway-bg-color: rgba(255, 255, 255, 0.1);
}
</style>
