<template>
  <div class="study-login">
    <!-- 背景纹理 -->
    <div class="paper-texture"></div>
    
    <!-- 入阁门 -->
    <div class="entrance-gate">
      <!-- 门楣 -->
      <div class="gate-header">
        <div class="header-ornament">
          <span class="ornament-line"></span>
          <span class="ornament-square">文</span>
          <span class="ornament-line"></span>
        </div>
        <h1 class="gate-title">文房</h1>
        <p class="gate-subtitle">非遗数字化管理平台</p>
      </div>

      <!-- 门签 -->
      <div class="gate-panel">
        <div class="panel-frame">
          <div class="frame-corner corner-tl"></div>
          <div class="frame-corner corner-tr"></div>
          <div class="frame-corner corner-bl"></div>
          <div class="frame-corner corner-br"></div>
          
          <div class="panel-content">
            <div class="seal-mark">入阁</div>
            
            <el-form ref="formRef" :model="form" :rules="rules" class="entry-form">
              <el-form-item prop="username">
                <div class="form-field">
                  <span class="field-label">阁员</span>
                  <el-input 
                    v-model="form.username" 
                    placeholder="请输入用户名" 
                    size="large"
                    :prefix-icon="User"
                  />
                </div>
              </el-form-item>
              
              <el-form-item prop="password">
                <div class="form-field">
                  <span class="field-label">密钥</span>
                  <el-input 
                    v-model="form.password" 
                    type="password" 
                    placeholder="请输入密码"
                    size="large"
                    :prefix-icon="Lock"
                    show-password 
                    @keyup.enter="handleLogin" 
                  />
                </div>
              </el-form-item>
              
              <el-form-item>
                <button 
                  type="button" 
                  class="entry-btn" 
                  :disabled="loading"
                  @click="handleLogin"
                >
                  <span v-if="loading">请稍候...</span>
                  <span v-else>登阁</span>
                </button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>

      
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '@/api/user'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await login(form)
      localStorage.setItem('admin_token', res.data.token)
      localStorage.setItem('admin_user', JSON.stringify(res.data))
      ElMessage.success('登阁成功')
      router.push('/')
    } catch (e) {
      console.error(e)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped lang="scss">
.study-login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    /* 顶部微光 */
    radial-gradient(ellipse 80% 50% at 50% 0%, rgba(201, 168, 76, 0.1) 0%, transparent 50%),
    /* 底色 */
    linear-gradient(180deg, #2c2420 0%, #1a1614 100%);
  position: relative;

  /* 绢本纹理 */
  .paper-texture {
    position: absolute;
    inset: 0;
    background:
      repeating-linear-gradient(
        0deg,
        transparent,
        transparent 40px,
        rgba(140, 126, 116, 0.03) 40px,
        rgba(140, 126, 116, 0.03) 41px
      );
    pointer-events: none;
  }
}

/* 入阁门 */
.entrance-gate {
  width: 420px;
  position: relative;
  z-index: 1;
}

/* 门楣 */
.gate-header {
  text-align: center;
  margin-bottom: 32px;

  .header-ornament {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16px;
    margin-bottom: 16px;

    .ornament-line {
      width: 60px;
      height: 1px;
      background: linear-gradient(90deg, transparent, rgba(201, 168, 76, 0.4), transparent);
    }

    .ornament-square {
      width: 36px;
      height: 36px;
      border: 1px solid rgba(201, 168, 76, 0.4);
      display: flex;
      align-items: center;
      justify-content: center;
      font-family: var(--font-serif);
      font-size: 18px;
      color: rgba(201, 168, 76, 0.7);
    }
  }

  .gate-title {
    font-family: var(--font-serif);
    font-size: 42px;
    font-weight: 700;
    color: rgba(231, 211, 150, 0.95);
    letter-spacing: 12px;
    margin-bottom: 8px;
  }

  .gate-subtitle {
    font-size: 13px;
    color: rgba(231, 211, 150, 0.5);
    letter-spacing: 3px;
  }
}

/* 门签 */
.gate-panel {
  .panel-frame {
    background: linear-gradient(145deg, #fffdf8 0%, #f5f1e8 100%);
    border-radius: 6px;
    padding: 10px;
    position: relative;
    box-shadow: 0 16px 48px rgba(0, 0, 0, 0.3);
  }

  .frame-corner {
    position: absolute;
    width: 16px;
    height: 16px;
    border-color: rgba(166, 64, 41, 0.25);
    border-style: solid;

    &.corner-tl { top: 10px; left: 10px; border-width: 1px 0 0 1px; }
    &.corner-tr { top: 10px; right: 10px; border-width: 1px 1px 0 0; }
    &.corner-bl { bottom: 10px; left: 10px; border-width: 0 0 1px 1px; }
    &.corner-br { bottom: 10px; right: 10px; border-width: 0 1px 1px 0; }
  }

  .panel-content {
    background: rgba(255, 253, 248, 0.95);
    border: 1px solid rgba(212, 201, 184, 0.4);
    border-radius: 3px;
    padding: 36px 32px 32px;
    position: relative;
  }

  .seal-mark {
    position: absolute;
    top: -12px;
    right: 24px;
    padding: 6px 14px;
    background: rgba(166, 64, 41, 0.9);
    color: #fff;
    font-family: var(--font-serif);
    font-size: 13px;
    letter-spacing: 3px;
    border-radius: 2px;
    box-shadow: 0 4px 12px rgba(166, 64, 41, 0.3);
  }
}

/* 表单 */
.entry-form {
  .form-field {
    margin-bottom: 8px;

    .field-label {
      display: block;
      font-size: 12px;
      color: var(--text-light);
      letter-spacing: 2px;
      margin-bottom: 8px;
    }
  }

  :deep(.el-input__wrapper) {
    background: rgba(255, 253, 248, 0.8);
    border: 1px solid rgba(212, 201, 184, 0.5);
    border-radius: 4px;
    box-shadow: none !important;
    padding: 8px 12px;

    &:hover, &.is-focus {
      border-color: var(--gold);
    }
  }

  :deep(.el-input__inner) {
    font-family: var(--font-sans);
    font-size: 15px;
    color: var(--text-color);

    &::placeholder {
      color: var(--text-light);
    }
  }

  :deep(.el-input__prefix) {
    color: var(--text-light);
  }
}

.entry-btn {
  width: 100%;
  padding: 14px;
  margin-top: 8px;
  background: linear-gradient(135deg, rgba(166, 64, 41, 0.9) 0%, rgba(140, 50, 30, 0.95) 100%);
  border: none;
  border-radius: 4px;
  color: #fff;
  font-family: var(--font-serif);
  font-size: 16px;
  letter-spacing: 4px;
  cursor: pointer;
  transition: all var(--transition);
  box-shadow: 0 4px 16px rgba(166, 64, 41, 0.25);

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(166, 64, 41, 0.35);
  }

  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }
}

/* 门贴士 */
.gate-note {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 24px;
  font-size: 12px;
  color: rgba(231, 211, 150, 0.4);

  .note-icon {
    font-size: 14px;
  }
}
</style>
