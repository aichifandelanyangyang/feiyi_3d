<template>
  <div class="gallery-ledger">
    <!-- 页面头部 -->
    <div class="ledger-header">
      <div class="header-seal">展</div>
      <h2 class="header-title">展品管理</h2>
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
                <span class="form-label">名称</span>
              </template>
              <el-input v-model="searchForm.keyword" placeholder="展品名称" clearable class="search-input" />
            </el-form-item>
            <el-form-item>
              <template #label>
                <span class="form-label">描述</span>
              </template>
              <el-input v-model="searchForm.description" placeholder="展品描述" clearable class="search-input" />
            </el-form-item>
            <el-form-item>
              <template #label>
                <span class="form-label">分类</span>
              </template>
              <el-select v-model="searchForm.category" placeholder="全部分类" clearable class="search-select">
                <el-option v-for="c in categoryList" :key="c.id" :label="c.name" :value="c.name" />
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

    <!-- 入展按钮 -->
    <div class="action-panel">
      <button class="add-btn" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        <span>添加展品</span>
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
            <el-table-column prop="name" label="展品名称" min-width="100" fixed="left" show-overflow-tooltip />
            <el-table-column label="图片" width="80" align="center">
              <template #default="{ row }">
                <el-image v-if="row.image" :src="row.image" style="width: 48px; height: 48px; border-radius: 2px; border: 1px solid rgba(212, 201, 184, 0.4);" fit="cover" :preview-src-list="[row.image]" />
                <span v-else style="color: var(--text-light); font-size: 12px;">—</span>
              </template>
            </el-table-column>
            <el-table-column prop="category" label="分类" width="80" align="center" />
            <el-table-column prop="era" label="年代" width="80" align="center" />
            <el-table-column prop="origin" label="产地" width="90" show-overflow-tooltip />
            <el-table-column prop="material" label="材质" width="100" />
            <el-table-column prop="modelPath" label="3D模型" width="80">
              <template #default="{ row }">
                <span v-if="row.modelPath" class="model-badge yes">有</span>
                <span v-else class="model-badge no">无</span>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
            <el-table-column prop="history" label="历史背景" min-width="140" show-overflow-tooltip />
            <el-table-column prop="craft" label="制作工艺" min-width="140" show-overflow-tooltip />
            <el-table-column prop="culturalValue" label="文化价值" min-width="120" show-overflow-tooltip />
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
              <span class="info-text">共 {{ page.total }} 件展品</span>
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
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑展品' : '添加展品'" width="1000px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="展品名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入展品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类别" prop="category">
              <el-select v-model="form.category" placeholder="请选择" style="width: 100%">
                <el-option v-for="c in categoryList" :key="c.id" :label="c.name" :value="c.name" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="年代">
              <el-input v-model="form.era" placeholder="如：明代、清代" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产地">
              <el-input v-model="form.origin" placeholder="如：景德镇、苏州" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="材质">
              <el-input v-model="form.material" placeholder="如：陶土、丝绸" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联展厅">
              <el-select v-model="form.exhibitionId" placeholder="请选择" style="width: 100%">
                <el-option label="非遗文化展厅" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="展品图片">
          <ImageUpload v-model="form.image" />
        </el-form-item>
        <el-form-item label="3D模型路径">
          <el-input v-model="form.modelPath" placeholder="/models/exhibits/exhibit1.glb">
            <template #prepend>路径</template>
          </el-input>
          <div class="form-tip">模型文件需放在 public/models/exhibits/ 目录下</div>
        </el-form-item>
        <el-form-item label="展品描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="简要描述展品特点" />
        </el-form-item>
        <el-form-item label="历史背景">
          <el-input v-model="form.history" type="textarea" :rows="2" placeholder="展品的历史渊源" />
        </el-form-item>
        <el-form-item label="制作工艺">
          <el-input v-model="form.craft" type="textarea" :rows="2" placeholder="制作工艺流程" />
        </el-form-item>
        <el-form-item label="文化价值">
          <el-input v-model="form.culturalValue" type="textarea" :rows="2" placeholder="展品的文化意义" />
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
import { getExhibitList, addExhibit, updateExhibit, deleteExhibit } from '@/api/exhibit'
import { getCategoryList } from '@/api/heritage'
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

const searchForm = reactive({ keyword: '', description: '', category: '' })
const page = reactive({ pageNum: 1, pageSize: 5, total: 0 })

const defaultForm = {
  id: null,
  exhibitionId: 1,
  name: '',
  category: '',
  era: '',
  origin: '',
  image: '',
  modelPath: '',
  description: '',
  history: '',
  craft: '',
  culturalValue: ''
}
const form = reactive({ ...defaultForm })

const rules = {
  name: [{ required: true, message: '请输入展品名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择类别', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getExhibitList({ ...searchForm, ...page })
    console.log('列表响应:', res)
    if (res.code === 200 && res.data) {
      tableData.value = res.data.list || []
      page.total = res.data.total || 0
    } else {
      throw new Error('获取列表失败')
    }
  } catch (e) {
    console.error('列表请求失败:', e)
    // 后端未连接时使用模拟数据
    tableData.value = [
      { id: 1, name: '青花瓷瓶', category: '瓷器', era: '明代', origin: '景德镇', material: '高岭土', modelPath: '/models/exhibits/exhibit1.glb', description: '这是一件精美的明代青花瓷瓶，采用传统的青花釉下彩工艺制作而成。', history: '青花瓷起源于唐代，在元代开始成熟，到明代达到鼎盛。', craft: '青花瓷的制作工艺包括选料、制坯、绑画、上釉、烧制等多个步骤。', culturalValue: '青花瓷是中国传统文化的重要载体。' },
      { id: 2, name: '苏绣屏风', category: '刺绣', era: '清代', origin: '苏州', material: '丝绸', modelPath: '/models/exhibits/exhibit2.glb', description: '苏绣精品，采用传统双面绣技法。', history: '苏绣是中国四大名绣之一。', craft: '以针法细腻、色彩雅致著称。', culturalValue: '代表中国传统刺绣工艺的精华。' },
      { id: 3, name: '紫砂壶', category: '紫砂', era: '清代', origin: '宜兴', material: '紫砂泥', modelPath: '/models/exhibits/exhibit3.glb', description: '宜兴紫砂壶，造型古朴典雅。', history: '紫砂壶起源于明代。', craft: '经过选泥、制坯、雕刻、烧制等工序。', culturalValue: '中国茶文化的重要载体。' },
      { id: 4, name: '皮影戏偶', category: '戏曲', era: '现代', origin: '陕西', material: '牛皮', modelPath: '', description: '传统皮影戏角色造型。', history: '皮影戏有两千多年历史。', craft: '采用牛皮雕刻上色。', culturalValue: '民间艺术瑰宝。' },
      { id: 5, name: '剪纸作品', category: '剪纸', era: '现代', origin: '河北', material: '宣纸', modelPath: '', description: '传统剪纸艺术作品。', history: '剪纸起源于汉代。', craft: '以剪刀或刻刀剪刻图案。', culturalValue: '最普及的民间艺术。' }
    ]
    page.total = tableData.value.length
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.description = ''
  searchForm.category = ''
  loadData()
}

const resetForm = () => {
  form.id = null
  form.exhibitionId = 1
  form.name = ''
  form.category = ''
  form.era = ''
  form.origin = ''
  form.material = ''
  form.image = ''
  form.modelPath = ''
  form.description = ''
  form.history = ''
  form.craft = ''
  form.culturalValue = ''
}

const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  form.id = row.id
  form.exhibitionId = row.exhibitionId || 1
  form.name = row.name || ''
  form.category = row.category || ''
  form.era = row.era || ''
  form.origin = row.origin || ''
  form.material = row.material || ''
  form.image = row.image || ''
  form.modelPath = row.modelPath || ''
  form.description = row.description || ''
  form.history = row.history || ''
  form.craft = row.craft || ''
  form.culturalValue = row.culturalValue || ''
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除展品「${row.name}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await deleteExhibit(row.id)
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
      console.log('提交数据:', JSON.stringify(form))
      let res
      if (form.id) {
        res = await updateExhibit(form.id, form)
      } else {
        res = await addExhibit(form)
      }
      console.log('响应:', res)
      if (res.code === 200) {
        ElMessage.success(form.id ? '更新成功' : '添加成功')
        dialogVisible.value = false
        loadData()
      } else {
        ElMessage.error(res.msg || '操作失败')
      }
    } catch (e) {
      console.error('请求失败:', e)
      ElMessage.error('操作失败')
    } finally {
      submitting.value = false
    }
  })
}

const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    categoryList.value = res.data || []
  } catch (e) {
    categoryList.value = []
  }
}

onMounted(() => {
  loadCategories()
  loadData()
})
</script>

<style scoped lang="scss">
.gallery-ledger {
  padding: 4px;
}

/* 陈列册头部 */
.ledger-header {
  text-align: center;
  margin-bottom: 24px;

  .header-seal {
    width: 48px;
    height: 48px;
    margin: 0 auto 12px;
    border: 2px solid rgba(166, 64, 41, 0.35);
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-family: var(--font-serif);
    font-size: 22px;
    color: rgba(166, 64, 41, 0.65);
    transform: rotate(-2deg);
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

/* 陈列册页 */
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

  .model-badge {
    display: inline-block;
    padding: 2px 8px;
    border-radius: 2px;
    font-size: 11px;
    letter-spacing: 1px;

    &.yes {
      background: rgba(103, 194, 58, 0.1);
      color: rgba(103, 194, 58, 0.9);
      border: 1px solid rgba(103, 194, 58, 0.2);
    }

    &.no {
      background: rgba(245, 108, 108, 0.1);
      color: rgba(245, 108, 108, 0.9);
      border: 1px solid rgba(245, 108, 108, 0.2);
    }
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

.form-tip {
  font-size: 12px;
  color: var(--text-light);
  margin-top: 4px;
  letter-spacing: 0.5px;
}
</style>
