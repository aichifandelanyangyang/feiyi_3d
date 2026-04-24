<template>
  <div class="roster-ledger">
    <!-- 页面头部 -->
    <div class="ledger-header">
      <div class="header-seal">用</div>
      <h2 class="header-title">用户管理</h2>
      <div class="header-divider">
        <span class="divider-line"></span>
        <span class="divider-dot"></span>
        <span class="divider-line"></span>
      </div>
    </div>

    <!-- 遴选栏 -->
    <div class="search-panel">
      <div class="panel-frame">
        <div class="frame-corner corner-tl"></div>
        <div class="frame-corner corner-tr"></div>
        <div class="frame-corner corner-bl"></div>
        <div class="frame-corner corner-br"></div>

        <div class="panel-content">
          <el-form :inline="true" :model="searchForm" class="search-form">
            <el-form-item>
              <template #label>
                <span class="form-label">关键词</span>
              </template>
              <el-input v-model="searchForm.keyword" placeholder="用户名/姓名" clearable class="search-input" />
            </el-form-item>
            <el-form-item>
              <template #label>
                <span class="form-label">角色</span>
              </template>
              <el-select v-model="searchForm.roleType" placeholder="全部角色" clearable class="search-select">
                <el-option label="管理员" :value="1" />
                <el-option label="普通用户" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <button type="button" class="search-btn" @click="loadData">
                <el-icon><Search /></el-icon>
                <span>搜索</span>
              </button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>

    <!-- 添加按钮 -->
    <div class="action-panel">
      <button class="add-btn" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        <span>添加用户</span>
      </button>
    </div>

    <!-- 数据列表 -->
    <div class="data-ledger">
      <div class="ledger-frame">
        <div class="frame-corner corner-tl"></div>
        <div class="frame-corner corner-tr"></div>
        <div class="frame-corner corner-bl"></div>
        <div class="frame-corner corner-br"></div>

        <div class="ledger-content">
          <el-table
            :data="tableData"
            v-loading="loading"
            class="study-table"
            :header-cell-style="headerStyle"
            :cell-style="cellStyle"
            height="400"
          >
            <el-table-column label="头像" width="100" align="center">
              <template #default="{ row }">
                <el-avatar :size="36" :src="row.avatar || undefined">
                  {{ (row.username || '?').charAt(0).toUpperCase() }}
                </el-avatar>
              </template>
            </el-table-column>
            <el-table-column prop="username" label="用户名" min-width="100" show-overflow-tooltip />
            <el-table-column prop="realName" label="姓名" min-width="80" show-overflow-tooltip />
            <el-table-column prop="phone" label="手机号" width="150" show-overflow-tooltip />
            <el-table-column prop="roleName" label="角色" width="150" align="center">
              <template #default="{ row }">
                <span :class="['role-badge', row.roleType === 1 ? 'admin' : 'visitor']">
                  {{ row.roleType === 1 ? '管理员' : '普通用户' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="150" align="center">
              <template #default="{ row }">
                <span :class="['status-badge', row.status === 1 ? 'active' : 'disabled']">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="注册时间" width="150" align="center" />
            <el-table-column label="操作" width="390" align="center" fixed="right">
              <template #default="{ row }">
                <div class="action-btns">
                  <button class="action-btn primary" @click="handleEdit(row)">编辑</button>
                  <button v-if="row.roleType !== 1" :class="['action-btn', row.status === 1 ? 'warning' : 'success']" @click="handleToggleStatus(row)">
                    {{ row.status === 1 ? '禁用' : '启用' }}
                  </button>
                  <button class="action-btn warning" @click="handleResetPwd(row)">重置密码</button>
                  <button v-if="row.roleType !== 1" class="action-btn danger" @click="handleDelete(row)">删除</button>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <div class="ledger-footer">
            <div class="footer-info">
              <span class="info-text">共 {{ page.total }} 个用户</span>
            </div>
            <el-pagination
              v-model:current-page="page.pageNum"
              v-model:page-size="page.pageSize"
              :total="page.total"
              :page-sizes="[5, 10, 20]"
              layout="total, prev, pager, next"
              @current-change="loadData"
              class="study-pagination"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '添加用户'" width="500px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="头像">
          <ImageUpload v-model="form.avatar" />
        </el-form-item>
        <el-form-item label="角色" prop="roleType">
          <el-select v-model="form.roleType" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" :value="1" />
            <el-option label="普通用户" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { getUserList, getUserDetail, addUser, updateUser, deleteUser, resetPassword, toggleUserStatus } from '@/api/user'
import ImageUpload from '@/components/ImageUpload.vue'

const headerStyle = () => ({
  background: 'rgba(140, 126, 116, 0.04)',
  color: 'var(--text-color)',
  fontFamily: 'var(--font-serif)',
  fontSize: '13px',
  letterSpacing: '2px',
  fontWeight: 500
})

const cellStyle = () => ({
  fontSize: '13px',
  color: 'var(--text-secondary)'
})

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const tableData = ref([])

const searchForm = reactive({ keyword: '', roleType: null })
const page = reactive({ pageNum: 1, pageSize: 5, total: 0 })

const defaultForm = {
  id: null,
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  avatar: '',
  roleType: 3
}
const form = reactive({ ...defaultForm })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  roleType: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserList({ ...searchForm, ...page })
    if (res.code === 200 && res.data) {
      tableData.value = res.data.list || []
      page.total = res.data.total || 0
    }
  } catch (e) {
    console.error('获取用户列表失败:', e)
    tableData.value = []
    page.total = 0
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  Object.assign(form, { ...defaultForm })
}

const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  resetForm()
  try {
    const res = await getUserDetail(row.id)
    if (res.code === 200 && res.data) {
      const detail = res.data
      form.id = detail.id
      form.username = detail.username || ''
      form.realName = detail.realName || ''
      form.phone = detail.phone || ''
      form.email = detail.email || ''
      form.avatar = detail.avatar || ''
      form.roleType = detail.roleType || 3
      dialogVisible.value = true
    }
  } catch (e) {
    ElMessage.error('获取用户详情失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定删除用户「${row.username}」吗？删除后数据将无法恢复。`,
    '删除确认',
    { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' }
  )
    .then(async () => {
      try {
        const res = await deleteUser(row.id)
        if (res.code === 200) {
          ElMessage.success('删除成功')
          loadData()
        } else {
          ElMessage.error(res.msg || '删除失败')
        }
      } catch (e) {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

const handleResetPwd = (row) => {
  ElMessageBox.prompt('请输入新密码', '重置密码', {
    inputPattern: /^.{6,20}$/,
    inputErrorMessage: '密码长度需为6-20个字符',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })
    .then(async ({ value }) => {
      try {
        const res = await resetPassword(row.id, value)
        if (res.code === 200) {
          ElMessage.success('密码重置成功')
        } else {
          ElMessage.error(res.msg || '重置失败')
        }
      } catch (e) {
        ElMessage.error('重置密码失败')
      }
    })
    .catch(() => {})
}

const handleToggleStatus = async (row) => {
  const action = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定${action}用户「${row.username}」？`, '提示', { type: 'warning' })
    const res = await toggleUserStatus(row.id)
    if (res.code === 200) {
      ElMessage.success(`已${action}`)
      loadData()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (e) {
    // 用户取消或请求失败
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      let res
      if (form.id) {
        res = await updateUser(form)
      } else {
        res = await addUser(form)
      }
      if (res.code === 200) {
        ElMessage.success(form.id ? '更新成功' : '添加成功')
        dialogVisible.value = false
        loadData()
      } else {
        ElMessage.error(res.msg || '操作失败')
      }
    } catch (e) {
      console.error('操作失败:', e)
      ElMessage.error('操作失败，请稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.roster-ledger {
  padding: 4px;
}

/* 名册头部 */
.ledger-header {
  text-align: center;
  margin-bottom: 24px;

  .header-seal {
    width: 44px;
    height: 44px;
    margin: 0 auto 12px;
    border: 2px solid rgba(166, 64, 41, 0.35);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-family: var(--font-serif);
    font-size: 18px;
    color: rgba(166, 64, 41, 0.65);
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

/* 遴选栏 */
.search-panel {
  margin-bottom: 16px;

  .panel-frame {
    background: linear-gradient(145deg, #fffdf8 0%, #faf8f3 100%);
    border: 1px solid rgba(212, 201, 184, 0.35);
    border-radius: 4px;
    padding: 8px;
    position: relative;
  }

  .panel-content {
    padding: 16px 20px;
    border: 1px solid rgba(212, 201, 184, 0.2);
    border-radius: 2px;
  }

  .search-form {
    display: flex;
    align-items: center;
    gap: 20px;
    flex-wrap: wrap;

    :deep(.el-form-item) {
      margin-bottom: 0;
    }

    .form-label {
      font-family: var(--font-serif);
      font-size: 13px;
      color: var(--text-light);
      letter-spacing: 2px;
      margin-right: 8px;
    }

    .search-input {
      width: 160px;
    }

    .search-select {
      width: 110px;
    }

    :deep(.el-input__wrapper),
    :deep(.el-select__wrapper) {
      background: rgba(255, 253, 248, 0.8);
      border: 1px solid rgba(212, 201, 184, 0.4);
      border-radius: 4px;
      box-shadow: none !important;
    }

    .search-btn {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 8px 18px;
      background: linear-gradient(135deg, rgba(166, 64, 41, 0.9) 0%, rgba(140, 50, 30, 0.95) 100%);
      border: none;
      border-radius: 4px;
      color: #fff;
      font-family: var(--font-serif);
      font-size: 13px;
      letter-spacing: 2px;
      cursor: pointer;
      transition: all var(--transition);
      box-shadow: 0 4px 12px rgba(166, 64, 41, 0.25);

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(166, 64, 41, 0.35);
      }

      .el-icon {
        font-size: 14px;
      }
    }
  }
}

/* 操作面板 */
.action-panel {
  margin-bottom: 16px;

  .add-btn {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 24px;
    background: linear-gradient(145deg, #fffdf8 0%, #faf8f3 100%);
    border: 1px solid rgba(166, 64, 41, 0.35);
    border-radius: 4px;
    font-family: var(--font-serif);
    font-size: 14px;
    color: var(--text-color);
    letter-spacing: 3px;
    cursor: pointer;
    transition: all var(--transition);

    &:hover {
      border-color: rgba(166, 64, 41, 0.5);
      background: rgba(166, 64, 41, 0.04);
      transform: translateY(-2px);
    }

    .el-icon {
      font-size: 16px;
      color: rgba(166, 64, 41, 0.6);
    }
  }
}

/* 名册册页 */
.data-ledger {
  .ledger-frame {
    background: linear-gradient(145deg, #fffdf8 0%, #faf8f3 100%);
    border: 1px solid rgba(212, 201, 184, 0.35);
    border-radius: 4px;
    padding: 8px;
    position: relative;
  }

  .ledger-content {
    border: 1px solid rgba(212, 201, 184, 0.2);
    border-radius: 2px;
    overflow: hidden;
  }
}

/* 表格样式 */
.study-table {
  :deep(.el-table__header-wrapper) {
    th {
      border-bottom: 1px solid rgba(212, 201, 184, 0.3) !important;
    }
  }

  :deep(.el-table__body-wrapper) {
    td {
      border-bottom: 1px solid rgba(212, 201, 184, 0.15) !important;
    }
  }

  :deep(.el-table__row:hover) {
    background: rgba(201, 168, 76, 0.04) !important;
  }

  .role-badge {
    display: inline-block;
    padding: 2px 8px;
    border-radius: 2px;
    font-size: 12px;
    letter-spacing: 1px;

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

  .status-badge {
    display: inline-block;
    padding: 2px 6px;
    border-radius: 2px;
    font-size: 12px;
    letter-spacing: 1px;

    &.active {
      background: rgba(103, 194, 58, 0.1);
      color: rgba(103, 194, 58, 0.9);
      border: 1px solid rgba(103, 194, 58, 0.2);
    }

    &.disabled {
      background: rgba(140, 126, 116, 0.1);
      color: rgba(140, 126, 116, 0.6);
      border: 1px solid rgba(140, 126, 116, 0.2);
    }
  }

  .action-btns {
    display: flex;
    gap: 4px;
    justify-content: center;
    flex-wrap: wrap;

    .action-btn {
      padding: 4px 8px;
      background: transparent;
      border: 1px solid rgba(212, 201, 184, 0.4);
      border-radius: 2px;
      font-size: 12px;
      letter-spacing: 1px;
      cursor: pointer;
      transition: all var(--transition);

      &:hover {
        border-color: var(--gold);
        background: rgba(201, 168, 76, 0.05);
      }

      &.primary {
        color: rgba(64, 158, 255, 0.9);
        border-color: rgba(64, 158, 255, 0.3);

        &:hover {
          background: rgba(64, 158, 255, 0.08);
        }
      }

      &.warning {
        color: rgba(230, 162, 60, 0.9);
        border-color: rgba(230, 162, 60, 0.3);

        &:hover {
          background: rgba(230, 162, 60, 0.08);
        }
      }

      &.success {
        color: rgba(103, 194, 58, 0.9);
        border-color: rgba(103, 194, 58, 0.3);

        &:hover {
          background: rgba(103, 194, 58, 0.08);
        }
      }

      &.danger {
        color: rgba(245, 108, 108, 0.9);
        border-color: rgba(245, 108, 108, 0.3);

        &:hover {
          background: rgba(245, 108, 108, 0.08);
        }
      }
    }
  }
}

/* 册页底 */
.ledger-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-top: 1px solid rgba(212, 201, 184, 0.25);

  .footer-info {
    .info-text {
      font-size: 13px;
      color: var(--text-light);
      letter-spacing: 1px;
    }
  }
}

/* 分页样式 */
.study-pagination {
  :deep(.el-pagination__total) {
    font-size: 13px;
    color: var(--text-light);
  }

  :deep(.el-pager li) {
    background: transparent;
    border: 1px solid rgba(212, 201, 184, 0.3);
    border-radius: 4px;
    margin: 0 4px;

    &:hover, &.is-active {
      border-color: rgba(201, 168, 76, 0.5);
      background: rgba(201, 168, 76, 0.08);
      color: var(--text-color);
    }
  }

  :deep(.btn-prev), :deep(.btn-next) {
    background: transparent;
    border: 1px solid rgba(212, 201, 184, 0.3);
    border-radius: 4px;

    &:hover {
      border-color: rgba(201, 168, 76, 0.5);
      background: rgba(201, 168, 76, 0.08);
    }
  }
}
</style>