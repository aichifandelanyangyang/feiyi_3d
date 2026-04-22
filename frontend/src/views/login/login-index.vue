<template>
  <div class="login-container">
    <!-- 背景装饰纹样 -->
    <div class="bg-decoration">
      <div class="bg-pattern"></div>
      <div class="bg-ornament bg-ornament--tl"></div>
      <div class="bg-ornament bg-ornament--br"></div>
    </div>

    <!-- 登录卡片 -->
    <div class="login-card">
      <!-- 左侧 — 文化氛围区 -->
      <div class="card-left">
        <div class="card-left-overlay"></div>
        <div class="brand-info">
          <div class="brand-ornament"></div>
          <h1 class="brand-title">非遗3D数字化<br/>交互平台</h1>
          <p class="brand-desc">沉浸式3D体验，感受千年传统文化魅力</p>
          <div class="brand-tags">
            <span class="brand-tag">3D展示</span>
            <span class="brand-tag">数字传承</span>
            <span class="brand-tag">文化探索</span>
          </div>
        </div>
      </div>
      <!-- 右侧 — 表单区 -->
      <div class="card-right">
        <div class="form-wrapper">
          <h2 class="form-title">{{ isLogin ? '欢迎回来' : '注册账号' }}</h2>
          <p class="form-subtitle">{{ isLogin ? '登录以探索非遗文化之美' : '创建账号，开启文化之旅' }}</p>

          <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="handleSubmit">
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                placeholder="请输入用户名"
                prefix-icon="User"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>
            <el-form-item v-if="!isLogin" prop="confirmPassword">
              <el-input
                v-model="form.confirmPassword"
                type="password"
                placeholder="请确认密码"
                prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>

            <el-form-item>
              <button
                type="button"
                class="submit-btn"
                :disabled="loading"
                @click="handleSubmit"
              >
                <span v-if="loading" class="btn-loading"></span>
                <span>{{ isLogin ? '登 录' : '注 册' }}</span>
              </button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            <span class="footer-text">
              {{ isLogin ? '还没有账号？' : '已有账号？' }}
            </span>
            <button class="toggle-btn" @click="toggleMode">
              {{ isLogin ? '立即注册' : '去登录' }}
            </button>
          </div>

          <div class="back-home">
            <button class="home-btn" @click="goHome">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
              <span>返回首页</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '@/api/user'
import { useUserStore } from '@/store'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const isLogin = ref(true)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (!isLogin.value && value !== form.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = computed(() => ({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度2-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: isLogin.value ? [] : [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}))

const toggleMode = () => {
  isLogin.value = !isLogin.value
  form.username = ''
  form.password = ''
  form.confirmPassword = ''
  formRef.value?.clearValidate()
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const data = { username: form.username, password: form.password }
      const res = isLogin.value ? await login(data) : await register(data)
      
      userStore.setToken(res.data.token)
      userStore.setUserInfo(res.data)
      
      ElMessage.success(isLogin.value ? '登录成功' : '注册成功')
      router.push('/')
    } catch (e) {
      // 错误由拦截器处理
    } finally {
      loading.value = false
    }
  })
}

const goHome = () => {
  router.push('/')
}
</script>

<style scoped lang="scss">
/* ---- 登录容器 — 深色水墨背景 ---- */
.login-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-dark);
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;

  .bg-pattern {
    position: absolute;
    inset: 0;
    background:
      radial-gradient(ellipse at 20% 50%, rgba(166, 64, 41, 0.08) 0%, transparent 60%),
      radial-gradient(ellipse at 80% 20%, rgba(201, 168, 76, 0.06) 0%, transparent 50%),
      radial-gradient(ellipse at 60% 80%, rgba(91, 138, 114, 0.05) 0%, transparent 50%);
  }

  .bg-ornament {
    position: absolute;
    border: 1px solid rgba(201, 168, 76, 0.08);
    border-radius: 50%;

    &--tl {
      width: 500px;
      height: 500px;
      top: -200px;
      left: -150px;
      animation: floatSlow 10s ease-in-out infinite;
    }

    &--br {
      width: 350px;
      height: 350px;
      bottom: -120px;
      right: -80px;
      animation: floatSlow 8s ease-in-out infinite reverse;
    }
  }
}

/* ---- 登录卡片 ---- */
.login-card {
  display: flex;
  width: 900px;
  min-height: 520px;
  border-radius: var(--radius-lg);
  overflow: hidden;
  position: relative;
  z-index: 1;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.4);
  animation: fadeInUp 0.8s ease-out;
}

/* 左侧 — 深色文化氛围区 */
.card-left {
  width: 380px;
  background: linear-gradient(160deg, #3a2a22 0%, #2c2420 50%, #1e1815 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 40px;
  position: relative;
  overflow: hidden;

  .card-left-overlay {
    position: absolute;
    inset: 0;
    background:
      radial-gradient(ellipse at 30% 70%, rgba(166, 64, 41, 0.15) 0%, transparent 60%),
      radial-gradient(ellipse at 70% 30%, rgba(201, 168, 76, 0.08) 0%, transparent 50%);
  }

  .brand-info {
    text-align: center;
    color: var(--text-on-dark);
    position: relative;
    z-index: 1;
  }

  .brand-ornament {
    width: 40px;
    height: 2px;
    background: var(--gold);
    margin: 0 auto 28px;
    opacity: 0.6;
  }

  .brand-title {
    font-family: var(--font-serif);
    font-size: 28px;
    font-weight: 700;
    line-height: 1.5;
    letter-spacing: 3px;
    margin-bottom: 16px;
    color: var(--gold-light);
  }

  .brand-desc {
    font-size: 14px;
    color: rgba(231, 211, 150, 0.55);
    line-height: 1.8;
    letter-spacing: 1px;
    margin-bottom: 32px;
  }

  .brand-tags {
    display: flex;
    gap: 10px;
    justify-content: center;
    flex-wrap: wrap;

    .brand-tag {
      padding: 4px 14px;
      border: 1px solid rgba(201, 168, 76, 0.2);
      border-radius: 2px;
      font-size: 12px;
      color: rgba(201, 168, 76, 0.5);
      letter-spacing: 2px;
    }
  }
}

/* 右侧 — 表单区 */
.card-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 44px;
  background: var(--bg-card);
}

.form-wrapper {
  width: 100%;
  max-width: 340px;

  .form-title {
    font-family: var(--font-serif);
    font-size: 26px;
    font-weight: 700;
    color: var(--text-color);
    letter-spacing: 3px;
    margin-bottom: 8px;
  }

  .form-subtitle {
    font-size: 13px;
    color: var(--text-light);
    margin-bottom: 36px;
    letter-spacing: 1px;
  }

  /* 提交按钮 — 印章式 */
  .submit-btn {
    width: 100%;
    height: 46px;
    background: var(--primary-color);
    color: #fff;
    border: none;
    border-radius: var(--radius-sm);
    font-size: 16px;
    font-family: var(--font-sans);
    letter-spacing: 6px;
    cursor: pointer;
    transition: all var(--transition);
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    position: relative;
    overflow: hidden;

    &:hover {
      background: var(--primary-light);
      transform: translateY(-1px);
      box-shadow: 0 6px 24px rgba(166, 64, 41, 0.3);
    }

    &:active {
      transform: translateY(0);
    }

    &:disabled {
      opacity: 0.7;
      cursor: not-allowed;
      transform: none;
    }

    .btn-loading {
      width: 18px;
      height: 18px;
      border: 2px solid rgba(255, 255, 255, 0.3);
      border-top-color: #fff;
      border-radius: 50%;
      animation: spin 0.6s linear infinite;
    }
  }

  .form-footer {
    text-align: center;
    margin-top: 20px;

    .footer-text {
      font-size: 13px;
      color: var(--text-light);
    }

    .toggle-btn {
      background: none;
      border: none;
      color: var(--primary-color);
      font-size: 13px;
      cursor: pointer;
      font-family: var(--font-sans);
      letter-spacing: 1px;
      transition: color var(--transition);

      &:hover {
        color: var(--primary-light);
      }
    }
  }

  .back-home {
    text-align: center;
    margin-top: 28px;

    .home-btn {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      background: none;
      border: none;
      color: var(--text-light);
      font-size: 13px;
      cursor: pointer;
      font-family: var(--font-sans);
      letter-spacing: 1px;
      transition: color var(--transition);

      &:hover {
        color: var(--primary-color);
      }
    }
  }
}

/* Element Plus 输入框覆写 */
:deep(.el-input__wrapper) {
  border-radius: var(--radius-sm);
  background: var(--bg-warm);
  box-shadow: 0 0 0 1px var(--border-light) inset !important;
  transition: all var(--transition) !important;

  &:hover {
    box-shadow: 0 0 0 1px var(--border-color) inset !important;
  }

  &.is-focus {
    box-shadow: 0 0 0 1px var(--primary-color) inset !important;
    background: #fff;
  }
}

:deep(.el-input__inner) {
  color: var(--text-color);
  font-family: var(--font-sans);

  &::placeholder {
    color: var(--text-light);
    letter-spacing: 1px;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
