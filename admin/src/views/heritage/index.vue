<template>
  <div class="collection-ledger">
    <!-- 页面头部 -->
    <div class="ledger-header">
      <div class="header-seal">遗</div>
      <h2 class="header-title">非遗管理</h2>
      <div class="header-divider">
        <span class="divider-line"></span>
        <span class="divider-dot"></span>
        <span class="divider-line"></span>
      </div>
    </div>

    <!-- 鉴选栏 -->
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
                <span class="form-label">名称</span>
              </template>
              <el-input v-model="searchForm.keyword" placeholder="项目名称" clearable class="search-input" />
            </el-form-item>
            <el-form-item>
              <template #label>
                <span class="form-label">分类</span>
              </template>
              <el-select v-model="searchForm.categoryId" placeholder="全部分类" clearable class="search-select">
                <el-option v-for="c in categoryList" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <template #label>
                <span class="form-label">级别</span>
              </template>
              <el-select v-model="searchForm.level" placeholder="全部级别" clearable class="search-select small">
                <el-option label="国家级" value="国家级" />
                <el-option label="省级" value="省级" />
                <el-option label="市级" value="市级" />
                <el-option label="县级" value="县级" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <button type="button" class="search-btn" @click="loadData">
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

    <!-- 入藏按钮 -->
    <div class="action-panel">
      <button class="add-btn" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        <span>添加项目</span>
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
            <el-table-column prop="name" label="项目名称" min-width="100" fixed="left" show-overflow-tooltip />
            <el-table-column label="图片" width="80" align="center">
              <template #default="{ row }">
                <el-image v-if="row.coverImage" :src="row.coverImage" style="width: 48px; height: 48px; border-radius: 2px; border: 1px solid rgba(212, 201, 184, 0.4);" fit="cover" :preview-src-list="[row.coverImage]" />
                <span v-else style="color: var(--text-light); font-size: 12px;">—</span>
              </template>
            </el-table-column>
            <el-table-column prop="categoryName" label="项目分类" width="80" align="center" />
            <el-table-column prop="level" label="遗产级别" width="75" align="center" />
            <el-table-column prop="region" label="申报地区" width="90" show-overflow-tooltip />
            <el-table-column prop="protectionUnit" label="保护单位" width="100" show-overflow-tooltip />
            <el-table-column prop="publishTime" label="公布时间" width="75" align="center" />
            <el-table-column prop="description" label="项目简介" min-width="140" show-overflow-tooltip />
            <el-table-column prop="history" label="历史渊源" min-width="140" show-overflow-tooltip />
            <el-table-column prop="viewCount" label="浏览量" width="60" align="center" />
            <el-table-column prop="favoriteCount" label="收藏数" width="60" align="center" />
            <el-table-column label="操作" width="150" align="center" fixed="right">
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
              <span class="info-text">共 {{ page.total }} 个项目</span>
            </div>
            <el-pagination
              v-model:current-page="page.pageNum"
              v-model:page-size="page.pageSize"
              :total="page.total"
              layout="total, prev, pager, next"
              @current-change="loadData"
              class="study-pagination"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑项目' : '添加项目'" width="700px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="项目名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入项目名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
                <el-option v-for="c in categoryList" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="遗产级别">
              <el-select v-model="form.level" placeholder="请选择" style="width: 100%">
                <el-option label="国家级" value="国家级" />
                <el-option label="省级" value="省级" />
                <el-option label="市级" value="市级" />
                <el-option label="县级" value="县级" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="申报地区">
              <el-input v-model="form.region" placeholder="如：江西景德镇" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="公布时间">
              <el-input v-model="form.publishTime" placeholder="如：2006年" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="保护单位">
              <el-input v-model="form.protectionUnit" placeholder="保护单位名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="封面图片">
          <ImageUpload v-model="form.coverImage" />
        </el-form-item>
        <el-form-item label="项目简介">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="项目简介" />
        </el-form-item>
        <el-form-item label="历史渊源">
          <el-input v-model="form.history" type="textarea" :rows="2" placeholder="历史渊源" />
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
import { getHeritageList, getHeritageDetail, addHeritage, updateHeritage, deleteHeritage, getCategoryList } from '@/api/heritage'
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
const categoryList = ref([])

const searchForm = reactive({ keyword: '', categoryId: null, level: '' })
const page = reactive({ pageNum: 1, pageSize: 5, total: 0 })

const defaultForm = {
  id: null, name: '', categoryId: null, level: '', region: '',
  publishTime: '', protectionUnit: '', description: '',
  history: '', coverImage: '', sort: 0
}
const form = reactive({ ...defaultForm })

const rules = {
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    categoryList.value = res.data || []
  } catch (e) {
    categoryList.value = []
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getHeritageList({ ...searchForm, ...page })
    tableData.value = res.data?.list || []
    page.total = res.data?.total || 0
  } catch (e) {
    console.error('列表请求失败:', e)
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.categoryId = null
  searchForm.level = ''
  page.pageNum = 1
  loadData()
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
    // 获取完整详情数据
    const res = await getHeritageDetail(row.id)
    if (res.code === 200 && res.data) {
      const detail = res.data
      form.id = detail.id
      form.name = detail.name || ''
      form.categoryId = detail.categoryId || null
      form.level = detail.level || ''
      form.region = detail.region || ''
      form.publishTime = detail.publishTime || ''
      form.protectionUnit = detail.protectionUnit || ''
      form.description = detail.description || ''
      form.history = detail.history || ''
      form.coverImage = detail.coverImage || ''
      dialogVisible.value = true
    }
  } catch (e) {
    ElMessage.error('获取详情失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定删除项目「${row.name}」吗？删除后数据将无法恢复。`,
    '删除确认',
    {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    }
  )
    .then(async () => {
      try {
        const res = await deleteHeritage(row.id)
        if (res.code === 200) {
          ElMessage.success('删除成功')
          loadData()
        } else {
          ElMessage.error(res.msg || '删除失败')
        }
      } catch (e) {
        ElMessage.error('删除失败，请稍后重试')
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
      let res
      if (form.id) {
        res = await updateHeritage(form.id, form)
      } else {
        res = await addHeritage(form)
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
  loadCategories()
  loadData()
})
</script>

<style scoped lang="scss">
.collection-ledger {
  padding: 4px;
}

/* 典藏册头部 */
.ledger-header {
  text-align: center;
  margin-bottom: 24px;

  .header-seal {
    width: 48px;
    height: 48px;
    margin: 0 auto 12px;
    border: 2px solid rgba(201, 168, 76, 0.4);
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-family: var(--font-serif);
    font-size: 22px;
    color: rgba(201, 168, 76, 0.7);
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

/* 四角框通用 */
.frame-corner {
  position: absolute;
  width: 10px;
  height: 10px;
  border-color: rgba(166, 64, 41, 0.2);
  border-style: solid;

  &.corner-tl { top: 8px; left: 8px; border-width: 1px 0 0 1px; }
  &.corner-tr { top: 8px; right: 8px; border-width: 1px 1px 0 0; }
  &.corner-bl { bottom: 8px; left: 8px; border-width: 0 0 1px 1px; }
  &.corner-br { bottom: 8px; right: 8px; border-width: 0 1px 1px 0; }
}

/* 鉴选栏 */
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
      width: 120px;

      &.small {
        width: 100px;
      }
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
      transition: all var(--transition);
      margin-left: 8px;

      &:hover {
        border-color: var(--gold);
        background: rgba(201, 168, 76, 0.05);
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
    border: 1px solid rgba(201, 168, 76, 0.4);
    border-radius: 4px;
    font-family: var(--font-serif);
    font-size: 14px;
    color: var(--text-color);
    letter-spacing: 3px;
    cursor: pointer;
    transition: all var(--transition);

    &:hover {
      border-color: rgba(166, 64, 41, 0.4);
      background: rgba(166, 64, 41, 0.03);
      transform: translateY(-2px);
    }

    .el-icon {
      font-size: 16px;
      color: rgba(166, 64, 41, 0.6);
    }
  }
}

/* 典藏册页 */
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

  .cell-scroll {
    max-height: 60px;
    overflow-y: auto;
    font-size: 13px;
    line-height: 1.6;
    padding-right: 4px;

    &::-webkit-scrollbar { width: 4px; }
    &::-webkit-scrollbar-thumb { background: rgba(212, 201, 184, 0.5); border-radius: 2px; }
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
