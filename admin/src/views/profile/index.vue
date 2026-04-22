<template>
  <div class="name-card">
    <!-- 页面头部 -->
    <div class="card-header">
      <div class="header-seal">我</div>
      <h2 class="header-title">个人中心</h2>
      <div class="header-divider">
        <span class="divider-line"></span>
        <span class="divider-dot"></span>
        <span class="divider-line"></span>
      </div>
    </div>

    <div class="card-content">
      <el-row :gutter="28" class="card-row">
        <!-- 左侧：头像与信息 -->
        <el-col :span="10" class="left-col">
          <div class="portrait-frame">
            <div class="frame-corner corner-tl"></div>
            <div class="frame-corner corner-tr"></div>
            <div class="frame-corner corner-bl"></div>
            <div class="frame-corner corner-br"></div>

            <div class="portrait-content">
              <div class="portrait-section">
                <el-upload
                  class="portrait-uploader"
                  :show-file-list="false"
                  :http-request="handleAvatarUpload"
                  accept="image/*"
                >
                  <div class="portrait-display">
                    <el-avatar :size="96" :src="userInfo.avatar || undefined">
                      {{ (userInfo.username || '?').charAt(0).toUpperCase() }}
                    </el-avatar>
                  </div>
                  <div class="portrait-mask">
                    <el-icon :size="22"><Camera /></el-icon>
                    <span>更换头像</span>
                  </div>
                </el-upload>
                <h3 class="portrait-username">{{ userInfo.username }}</h3>
                <span :class="['role-badge', userInfo.roleType === 1 ? 'admin' : 'visitor']">
                  {{ userInfo.roleType === 1 ? '管理员' : '普通用户' }}
                </span>
              </div>

              <div class="info-list">
                <div class="info-item">
                  <span class="info-label">用户ID</span>
                  <span class="info-value">{{ userInfo.userId || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">用户名</span>
                  <span class="info-value">{{ userInfo.username || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">姓名</span>
                  <span class="info-value">{{ userInfo.realName || '未设置' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">电话</span>
                  <span class="info-value">{{ userInfo.phone || '未设置' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">邮箱</span>
                  <span class="info-value">{{ userInfo.email || '未设置' }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-col>

        <!-- 右侧：编辑个人信息 -->
        <el-col :span="14" class="right-col">
            <!-- 修改信息 -->
            <div class="edit-frame main-edit">
            <div class="frame-corner corner-tl"></div>
            <div class="frame-corner corner-tr"></div>
            <div class="frame-corner corner-bl"></div>
            <div class="frame-corner corner-br"></div>

            <div class="edit-content">
              <h4 class="section-title">修改信息</h4>
              <el-form
                ref="formRef"
                :model="profileForm"
                :rules="formRules"
                label-width="80px"
                class="profile-form"
              >
                <el-form-item label="姓名" prop="realName">
                  <el-input v-model="profileForm.realName" placeholder="请输入姓名" clearable class="form-input" />
                </el-form-item>
                <el-form-item label="电话" prop="phone">
                  <el-input v-model="profileForm.phone" placeholder="请输入电话" clearable class="form-input" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="profileForm.email" placeholder="请输入邮箱" clearable class="form-input" />
                </el-form-item>
                <el-form-item>
                  <button type="button" class="submit-btn" :class="{ loading: saveLoading }" @click="handleSave">
                    <span>{{ saveLoading ? '保存中...' : '保存' }}</span>
                  </button>
                  <button type="button" class="reset-btn" @click="resetForm">重置</button>
                </el-form-item>
              </el-form>
            </div>
          </div>

            <!-- 修改密码 -->
            <div class="edit-frame pwd-edit">
            <div class="frame-corner corner-tl"></div>
            <div class="frame-corner corner-tr"></div>
            <div class="frame-corner corner-bl"></div>
            <div class="frame-corner corner-br"></div>

            <div class="edit-content">
              <h4 class="section-title">修改密码</h4>
              <el-form
                ref="pwdFormRef"
                :model="pwdForm"
                :rules="pwdRules"
                label-width="80px"
                class="profile-form"
              >
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" class="form-input" />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请确认新密码" class="form-input" />
                </el-form-item>
                <el-form-item>
                  <button type="button" class="submit-btn warning" :class="{ loading: pwdLoading }" @click="handleChangePwd">
                    <span>{{ pwdLoading ? '修改中...' : '修改密码' }}</span>
                  </button>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Camera } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getMyInfo, updateProfile, uploadImage, resetPassword } from '@/api/user'

const userInfo = ref({})
const saveLoading = ref(false)
const pwdLoading = ref(false)
const formRef = ref(null)
const pwdFormRef = ref(null)

const profileForm = reactive({
  realName: '',
  phone: '',
  email: ''
})

const pwdForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

const roleTagType = computed(() => {
  const types = { 1: 'danger', 2: 'warning', 3: 'info' }
  return types[userInfo.value.roleType] || 'info'
})

const formRules = {
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

const pwdRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const fetchInfo = async () => {
  try {
    const res = await getMyInfo()
    if (res.data) {
      userInfo.value = res.data
      profileForm.realName = res.data.realName || ''
      profileForm.phone = res.data.phone || ''
      profileForm.email = res.data.email || ''
      // 同步localStorage
      const stored = JSON.parse(localStorage.getItem('admin_user') || '{}')
      Object.assign(stored, res.data)
      localStorage.setItem('admin_user', JSON.stringify(stored))
    }
  } catch (e) {
    console.error('获取个人信息失败', e)
  }
}

const handleSave = async () => {
  await formRef.value?.validate()
  saveLoading.value = true
  try {
    await updateProfile(profileForm)
    ElMessage.success('保存成功')
    await fetchInfo()
  } catch (e) {
    // handled by interceptor
  } finally {
    saveLoading.value = false
  }
}

const resetForm = () => {
  profileForm.realName = userInfo.value.realName || ''
  profileForm.phone = userInfo.value.phone || ''
  profileForm.email = userInfo.value.email || ''
}

const handleAvatarUpload = async (options) => {
  try {
    const res = await uploadImage(options.file)
    const url = res.data.url
    await updateProfile({ avatar: url })
    ElMessage.success('头像更新成功')
    await fetchInfo()
  } catch (e) {
    ElMessage.error('头像上传失败')
  }
}

const handleChangePwd = async () => {
  await pwdFormRef.value?.validate()
  pwdLoading.value = true
  try {
    await resetPassword(userInfo.value.userId, pwdForm.newPassword)
    ElMessage.success('密码修改成功，请重新登录')
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } catch (e) {
    // handled by interceptor
  } finally {
    pwdLoading.value = false
  }
}

onMounted(() => fetchInfo())
</script>

<style scoped lang="scss">
.name-card {
  padding: 8px;
  max-width: 1200px;
  margin: 0 auto;
}

.card-row {
  display: flex;
  align-items: stretch;

  .left-col, .right-col {
    display: flex;
    flex-direction: column;
  }

  .right-col {
    gap: 16px;
  }
}

/* 名帖头部 */
.card-header {
  text-align: center;
  margin-bottom: 28px;

  .header-seal {
    width: 48px;
    height: 48px;
    margin: 0 auto 14px;
    border: 2px solid rgba(201, 168, 76, 0.45);
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-family: var(--font-serif);
    font-size: 22px;
    color: rgba(201, 168, 76, 0.75);
    box-shadow: 0 2px 8px rgba(201, 168, 76, 0.15);
  }

  .header-title {
    font-family: var(--font-serif);
    font-size: 26px;
    font-weight: 600;
    color: var(--text-color);
    letter-spacing: 6px;
    margin-bottom: 12px;
  }

  .header-divider {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;

    .divider-line {
      width: 60px;
      height: 1px;
      background: linear-gradient(90deg, transparent, rgba(201, 168, 76, 0.4), transparent);
    }

    .divider-dot {
      width: 4px;
      height: 4px;
      background: rgba(201, 168, 76, 0.5);
      border-radius: 50%;
    }
  }
}

/* 四角框通用 */
.frame-corner {
  position: absolute;
  width: 10px;
  height: 10px;
  border-color: rgba(140, 126, 116, 0.25);
  border-style: solid;

  &.corner-tl { top: 8px; left: 8px; border-width: 1px 0 0 1px; }
  &.corner-tr { top: 8px; right: 8px; border-width: 1px 1px 0 0; }
  &.corner-bl { bottom: 8px; left: 8px; border-width: 0 0 1px 1px; }
  &.corner-br { bottom: 8px; right: 8px; border-width: 0 1px 1px 0; }
}

/* 像框 */
.portrait-frame {
  background: linear-gradient(165deg, #fffefb 0%, #faf7f0 50%, #f5f0e6 100%);
  border: 1px solid rgba(201, 168, 76, 0.25);
  border-radius: 6px;
  padding: 10px;
  position: relative;
  height: 100%;
  box-shadow:
    0 4px 20px rgba(140, 126, 116, 0.08),
    0 1px 3px rgba(140, 126, 116, 0.05);
  transition: box-shadow 0.3s ease;

  &:hover {
    box-shadow:
      0 8px 30px rgba(140, 126, 116, 0.12),
      0 2px 6px rgba(140, 126, 116, 0.08);
  }

  .portrait-content {
    border: 1px solid rgba(201, 168, 76, 0.15);
    border-radius: 3px;
    padding: 24px;
    height: 100%;
    display: flex;
    flex-direction: column;
  }
}

.portrait-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0 24px;
  gap: 14px;

  .portrait-uploader {
    position: relative;
    cursor: pointer;
    border-radius: 50%;

    .portrait-display {
      :deep(.el-avatar) {
        border: 2px solid rgba(201, 168, 76, 0.4);
        box-shadow: 0 4px 12px rgba(140, 126, 116, 0.15);
        transition: transform 0.3s ease, box-shadow 0.3s ease;
      }

      &:hover :deep(.el-avatar) {
        transform: scale(1.02);
        box-shadow: 0 6px 16px rgba(140, 126, 116, 0.2);
      }
    }

    .portrait-mask {
      position: absolute;
      top: 0;
      left: 0;
      width: 96px;
      height: 96px;
      border-radius: 50%;
      background: rgba(0, 0, 0, 0.45);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 12px;
      gap: 4px;
      opacity: 0;
      transition: opacity 0.3s;
      font-family: var(--font-serif);
      letter-spacing: 1px;
    }

    &:hover .portrait-mask {
      opacity: 1;
    }
  }

  .portrait-username {
    font-family: var(--font-serif);
    font-size: 20px;
    font-weight: 600;
    color: var(--text-color);
    margin: 0;
    letter-spacing: 2px;
  }

  .role-badge {
    display: inline-block;
    padding: 4px 12px;
    border-radius: 2px;
    font-size: 13px;
    letter-spacing: 2px;
    font-family: var(--font-serif);

    &.admin {
      background: rgba(166, 64, 41, 0.1);
      color: rgba(166, 64, 41, 0.9);
      border: 1px solid rgba(166, 64, 41, 0.2);
    }

    &.visitor {
      background: rgba(140, 126, 116, 0.1);
      color: rgba(140, 126, 116, 0.8);
      border: 1px solid rgba(140, 126, 116, 0.2);
    }
  }
}

.info-list {
  margin-top: auto;
  border-top: 1px solid rgba(201, 168, 76, 0.2);
  padding-top: 20px;

  .info-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px dashed rgba(201, 168, 76, 0.2);
    position: relative;

    &:last-child {
      border-bottom: none;
    }

    &::before {
      content: '';
      position: absolute;
      left: -8px;
      width: 3px;
      height: 3px;
      background: rgba(201, 168, 76, 0.4);
      border-radius: 50%;
    }

    .info-label {
      font-family: var(--font-serif);
      font-size: 13px;
      color: var(--text-light);
      letter-spacing: 3px;
      min-width: 50px;
    }

    .info-value {
      font-size: 14px;
      color: var(--text-secondary);
      text-align: right;
    }
  }
}

/* 编辑框 */
.edit-frame {
  background: linear-gradient(165deg, #fffefb 0%, #faf7f0 50%, #f5f0e6 100%);
  border: 1px solid rgba(201, 168, 76, 0.25);
  border-radius: 6px;
  padding: 10px;
  position: relative;
  flex: 1;
  display: flex;
  flex-direction: column;
  box-shadow:
    0 4px 20px rgba(140, 126, 116, 0.08),
    0 1px 3px rgba(140, 126, 116, 0.05);
  transition: box-shadow 0.3s ease;

  &:hover {
    box-shadow:
      0 8px 30px rgba(140, 126, 116, 0.12),
      0 2px 6px rgba(140, 126, 116, 0.08);
  }

  &.main-edit {
    flex: 1.4;
  }

  &.pwd-edit {
    flex: 1;
  }

  .edit-content {
    border: 1px solid rgba(201, 168, 76, 0.15);
    border-radius: 3px;
    padding: 22px 28px;
    flex: 1;
    display: flex;
    flex-direction: column;
  }
}

.section-title {
  font-family: var(--font-serif);
  font-size: 17px;
  font-weight: 600;
  color: var(--text-color);
  letter-spacing: 5px;
  margin: 0 0 22px 0;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(201, 168, 76, 0.25);
  display: flex;
  align-items: center;
  gap: 10px;

  &::before {
    content: '';
    width: 4px;
    height: 16px;
    background: linear-gradient(180deg, rgba(201, 168, 76, 0.6), rgba(201, 168, 76, 0.2));
    border-radius: 2px;
  }
}

.profile-form {
  flex: 1;
  display: flex;
  flex-direction: column;

  :deep(.el-form-item) {
    margin-bottom: 18px;

    &:last-of-type {
      margin-top: auto;
      margin-bottom: 0;
      padding-top: 8px;
    }
  }

  :deep(.el-form-item__label) {
    font-family: var(--font-serif);
    font-size: 14px;
    color: var(--text-light);
    letter-spacing: 2px;
  }

  .form-input {
    :deep(.el-input__wrapper) {
      background: rgba(255, 253, 248, 0.8);
      border: 1px solid rgba(212, 201, 184, 0.4);
      border-radius: 4px;
      box-shadow: none !important;
    }
  }

  .submit-btn {
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

    &:hover:not(.loading) {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba(166, 64, 41, 0.35);
    }

    &.loading {
      opacity: 0.7;
      cursor: not-allowed;
    }

    &.warning {
      background: linear-gradient(135deg, rgba(201, 168, 76, 0.9) 0%, rgba(166, 140, 50, 0.95) 100%);
      box-shadow: 0 4px 12px rgba(201, 168, 76, 0.25);

      &:hover:not(.loading) {
        box-shadow: 0 6px 16px rgba(201, 168, 76, 0.35);
      }
    }
  }

  .reset-btn {
    padding: 10px 20px;
    background: transparent;
    border: 1px solid rgba(212, 201, 184, 0.5);
    border-radius: 4px;
    font-family: var(--font-serif);
    font-size: 14px;
    color: var(--text-light);
    letter-spacing: 2px;
    cursor: pointer;
    transition: all var(--transition);
    margin-left: 12px;

    &:hover {
      border-color: var(--gold);
      background: rgba(201, 168, 76, 0.05);
    }
  }
}
</style>
