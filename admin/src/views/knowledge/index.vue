<template>
  <div class="knowledge-ledger">
    <!-- 页面头部 -->
    <div class="ledger-header">
      <div class="header-seal">智</div>
      <h2 class="header-title">知识库管理</h2>
      <div class="header-divider">
        <span class="divider-line"></span>
        <span class="divider-dot"></span>
        <span class="divider-line"></span>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-panel">
      <div class="panel-frame">
        <div class="frame-corner corner-tl"></div>
        <div class="frame-corner corner-tr"></div>
        <div class="frame-corner corner-bl"></div>
        <div class="frame-corner corner-br"></div>
        <div class="panel-content">
          <el-form :inline="true" class="search-form">
            <el-form-item>
              <template #label><span class="form-label">标题</span></template>
              <el-input v-model="searchKeyword" placeholder="知识标题" clearable class="search-input" />
            </el-form-item>
            <el-form-item>
              <button type="button" class="search-btn" @click="applySearch">
                <el-icon><Search /></el-icon>
                <span>搜索</span>
              </button>
              <button type="button" class="reset-btn" @click="resetSearch">
                <span>重置</span>
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
        <span>添加知识</span>
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
            :data="filteredData"
            v-loading="loading"
            class="study-table"
            :header-cell-style="headerStyle"
            :cell-style="cellStyle"
          >
            <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
            <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.content ? row.content.slice(0, 80) + (row.content.length > 80 ? '...' : '') : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="category" label="分类" width="100" align="center">
              <template #default="{ row }">{{ row.category || '-' }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="160" align="center">
              <template #default="{ row }">
                {{ row.createTime ? row.createTime.replace('T', ' ').slice(0, 16) : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" align="center" fixed="right">
              <template #default="{ row }">
                <div class="action-btns">
                  <button class="action-btn primary" @click="handleEdit(row)">编辑</button>
                  <button class="action-btn danger" @click="handleDelete(row)">删除</button>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <div class="ledger-footer">
            <div class="footer-info">
              <span class="info-text">共 {{ filteredData.length }} 条知识</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑知识' : '添加知识'" width="640px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="如：昆曲的历史渊源" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="form.category" placeholder="如：基础知识、传统技艺" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="请输入知识内容，AI助手将基于这些内容回答用户问题" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { getKnowledgeList, addKnowledge, updateKnowledge, deleteKnowledge } from '@/api/knowledge'

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
const searchKeyword = ref('')
const activeKeyword = ref('')

const filteredData = computed(() => {
  if (!activeKeyword.value) return tableData.value
  const kw = activeKeyword.value.toLowerCase()
  return tableData.value.filter(item => (item.title || '').toLowerCase().includes(kw))
})

const applySearch = () => { activeKeyword.value = searchKeyword.value }
const resetSearch = () => { searchKeyword.value = ''; activeKeyword.value = '' }

const defaultForm = { id: null, title: '', content: '', category: '' }
const form = reactive({ ...defaultForm })

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getKnowledgeList()
    tableData.value = res.data || []
  } catch (e) {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  Object.assign(form, { id: null, title: '', content: '', category: '' })
}

const handleAdd = () => { resetForm(); dialogVisible.value = true }

const handleEdit = (row) => {
  resetForm()
  form.id = row.id
  form.title = row.title || ''
  form.content = row.content || ''
  form.category = row.category || ''
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除知识「${row.title}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await deleteKnowledge(row.id)
        ElMessage.success('删除成功')
        loadData()
      } catch (e) {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (form.id) {
        await updateKnowledge(form.id, form)
      } else {
        await addKnowledge(form)
      }
      ElMessage.success(form.id ? '更新成功' : '添加成功')
      dialogVisible.value = false
      loadData()
    } catch (e) {
      console.error('操作失败:', e)
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => loadData())
</script>

<style scoped lang="scss">
.knowledge-ledger {
  padding: 4px;
}

.ledger-header {
  text-align: center;
  margin-bottom: 24px;

  .header-seal {
    width: 44px;
    height: 44px;
    margin: 0 auto 12px;
    border: 2px solid rgba(140, 126, 116, 0.4);
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-family: var(--font-serif);
    font-size: 20px;
    color: rgba(140, 126, 116, 0.7);
  }

  .header-title {
    font-family: var(--font-serif);
    font-size: 26px;
    font-weight: 600;
    color: var(--text-color);
    letter-spacing: 8px;
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

    :deep(.el-form-item) { margin-bottom: 0; }

    .form-label {
      font-family: var(--font-serif);
      font-size: 13px;
      color: var(--text-light);
      letter-spacing: 2px;
      margin-right: 8px;
    }

    .search-input { width: 200px; }

    :deep(.el-input__wrapper) {
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
      transition: all 0.2s;
      box-shadow: 0 4px 12px rgba(166, 64, 41, 0.25);

      &:hover { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(166, 64, 41, 0.35); }
      .el-icon { font-size: 14px; }
    }

    .reset-btn {
      padding: 8px 16px;
      background: transparent;
      border: 1px solid rgba(212, 201, 184, 0.5);
      border-radius: 4px;
      font-family: var(--font-serif);
      font-size: 13px;
      color: var(--text-light);
      letter-spacing: 2px;
      cursor: pointer;
      transition: all 0.2s;
      margin-left: 8px;

      &:hover { border-color: var(--gold); background: rgba(201, 168, 76, 0.05); }
    }
  }
}

.action-panel {
  margin-bottom: 16px;

  .add-btn {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 24px;
    background: linear-gradient(145deg, #fffdf8 0%, #faf8f3 100%);
    border: 1px solid rgba(140, 126, 116, 0.4);
    border-radius: 4px;
    font-family: var(--font-serif);
    font-size: 14px;
    color: var(--text-color);
    letter-spacing: 3px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      border-color: rgba(166, 64, 41, 0.4);
      background: rgba(166, 64, 41, 0.03);
      transform: translateY(-2px);
    }

    .el-icon { font-size: 16px; color: rgba(140, 126, 116, 0.7); }
  }
}

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

.study-table {
  :deep(.el-table__header-wrapper) {
    th { border-bottom: 1px solid rgba(212, 201, 184, 0.3) !important; }
  }

  :deep(.el-table__body-wrapper) {
    td { border-bottom: 1px solid rgba(212, 201, 184, 0.15) !important; }
  }

  :deep(.el-table__row:hover) {
    background: rgba(201, 168, 76, 0.04) !important;
  }

  .action-btns {
    display: flex;
    gap: 6px;
    justify-content: center;

    .action-btn {
      padding: 4px 10px;
      background: transparent;
      border: 1px solid rgba(212, 201, 184, 0.4);
      border-radius: 2px;
      font-size: 12px;
      letter-spacing: 1px;
      cursor: pointer;
      transition: all 0.2s;

      &.primary {
        color: rgba(64, 158, 255, 0.9);
        border-color: rgba(64, 158, 255, 0.3);
        &:hover { background: rgba(64, 158, 255, 0.08); }
      }

      &.danger {
        color: rgba(245, 108, 108, 0.9);
        border-color: rgba(245, 108, 108, 0.3);
        &:hover { background: rgba(245, 108, 108, 0.08); }
      }
    }
  }
}

.ledger-footer {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border-top: 1px solid rgba(212, 201, 184, 0.25);

  .info-text {
    font-size: 13px;
    color: var(--text-light);
    letter-spacing: 1px;
  }
}
</style>
