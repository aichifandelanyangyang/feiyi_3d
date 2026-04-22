<template>
  <div class="ledger-manage">
    <!-- 页面头部 -->
    <div class="ledger-header">
      <div class="header-seal">社</div>
      <h2 class="header-title">社区管理</h2>
      <div class="header-divider">
        <span class="divider-line"></span>
        <span class="divider-dot"></span>
        <span class="divider-line"></span>
      </div>
    </div>

    <!-- 检索栏 -->
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
              <el-input v-model="searchForm.keyword" placeholder="标题/内容" clearable class="search-input" />
            </el-form-item>
            <el-form-item>
              <template #label>
                <span class="form-label">状态</span>
              </template>
              <el-select v-model="searchForm.status" placeholder="全部状态" clearable class="search-select">
                <el-option label="待审核" :value="0" />
                <el-option label="已通过" :value="1" />
                <el-option label="已拒绝" :value="2" />
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
          >
            <el-table-column prop="username" label="用户" width="110" align="center">
              <template #default="{ row }">
                <div class="user-cell">
                  <el-avatar :size="28" :src="row.avatar || undefined">{{ (row.username || '?').charAt(0).toUpperCase() }}</el-avatar>
                  <span>{{ row.username }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="title" label="标题" width="180" show-overflow-tooltip />
            <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
            <el-table-column label="图片" width="80" align="center">
              <template #default="{ row }">
                <template v-if="row.images">
                  <el-image
                    :src="row.images.split(',')[0]"
                    style="width: 44px; height: 44px; border-radius: 2px; border: 1px solid rgba(212, 201, 184, 0.4);"
                    fit="cover"
                    :preview-src-list="row.images.split(',')"
                  />
                </template>
                <span v-else class="no-image">—</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80" align="center">
              <template #default="{ row }">
                <span class="status-tag" :class="statusClass(row.status)">{{ statusText(row.status) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="viewCount" label="浏览" width="60" align="center" />
            <el-table-column prop="likeCount" label="点赞" width="60" align="center" />
            <el-table-column prop="createTime" label="发布时间" width="150" align="center">
              <template #default="{ row }">
                {{ row.createTime ? row.createTime.replace('T', ' ').slice(0, 16) : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="260" align="center" fixed="right">
              <template #default="{ row }">
                <div class="action-btns">
                  <button v-if="row.status === 0" class="action-btn success" @click="handleApprove(row)">通过</button>
                  <button v-if="row.status === 0" class="action-btn warning" @click="handleReject(row)">拒绝</button>
                  <button class="action-btn primary" @click="handleView(row)">查看</button>
                  <button class="action-btn danger" @click="handleDelete(row)">删除</button>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <div class="ledger-footer">
            <div class="footer-info">
              <span class="info-text">共 {{ total }} 条帖子</span>
            </div>
            <el-pagination
              v-model:current-page="searchForm.pageNum"
              v-model:page-size="searchForm.pageSize"
              :total="total"
              :page-sizes="[10, 20, 50]"
              layout="sizes, prev, pager, next"
              @size-change="loadData"
              @current-change="loadData"
              class="study-pagination"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 详情册页弹窗 -->
    <el-dialog v-model="detailVisible" title="" width="600px" class="ledger-dialog" :show-close="false">
      <template #header>
        <div class="dialog-header">
          <span class="header-seal">阅</span>
          <h3 class="header-title">帖子详情</h3>
        </div>
      </template>
      
      <div class="dialog-content">
        <div class="content-section">
          <span class="section-label">用户</span>
          <span class="section-value">{{ currentPost.username }}</span>
        </div>
        <div class="content-section">
          <span class="section-label">标题</span>
          <span class="section-value highlight">{{ currentPost.title }}</span>
        </div>
        <div class="content-section block">
          <span class="section-label">内容</span>
          <div class="section-text">{{ currentPost.content }}</div>
        </div>
        <div class="content-section block" v-if="currentPost.images">
          <span class="section-label">图片</span>
          <div class="image-list">
            <el-image
              v-for="(img, idx) in currentPost.images.split(',')"
              :key="idx"
              :src="img"
              style="width: 80px; height: 80px; border-radius: 2px; border: 1px solid rgba(212, 201, 184, 0.4);"
              fit="cover"
              :preview-src-list="currentPost.images.split(',')"
              :initial-index="idx"
            />
          </div>
        </div>
        <div class="content-section">
          <span class="section-label">状态</span>
          <span class="status-tag" :class="statusClass(currentPost.status)">{{ statusText(currentPost.status) }}</span>
        </div>
        <div class="content-section" v-if="currentPost.status === 2">
          <span class="section-label">拒绝原因</span>
          <span class="section-value reject-reason">{{ currentPost.rejectReason }}</span>
        </div>
        <div class="content-section">
          <span class="section-label">发布时间</span>
          <span class="section-value">{{ currentPost.createTime?.replace('T', ' ') }}</span>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <button v-if="currentPost.status === 0" class="btn-success" @click="handleApprove(currentPost); detailVisible = false">通过</button>
          <button v-if="currentPost.status === 0" class="btn-warning" @click="handleReject(currentPost)">拒绝</button>
          <button class="btn-default" @click="detailVisible = false">关闭</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getCommunityList, approveCommunityPost, rejectCommunityPost, deleteCommunityPost } from '@/api/community'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const detailVisible = ref(false)
const currentPost = ref({})

const searchForm = reactive({
  keyword: '',
  status: null,
  pageNum: 1,
  pageSize: 10
})

const statusText = (s) => ({ 0: '待审核', 1: '已通过', 2: '已拒绝' }[s] || '未知')
const statusClass = (s) => ({ 0: 'pending', 1: 'approved', 2: 'rejected' }[s] || '')
const statusType = (s) => ({ 0: 'warning', 1: 'success', 2: 'danger' }[s] || 'info')

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

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCommunityList(searchForm)
    tableData.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('加载失败', e)
  } finally {
    loading.value = false
  }
}

const handleApprove = async (row) => {
  await ElMessageBox.confirm('确定审核通过该帖子？', '提示')
  await approveCommunityPost(row.id)
  ElMessage.success('已通过')
  loadData()
}

const handleReject = (row) => {
  ElMessageBox.prompt('请输入拒绝原因', '审核拒绝', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValidator: (val) => !!val?.trim() || '请输入拒绝原因'
  }).then(async ({ value }) => {
    await rejectCommunityPost(row.id, value)
    ElMessage.success('已拒绝')
    detailVisible.value = false
    loadData()
  }).catch(() => {})
}

const handleView = (row) => {
  currentPost.value = { ...row }
  detailVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该帖子？删除后不可恢复', '警告', { type: 'warning' })
    .then(async () => {
      await deleteCommunityPost(row.id)
      ElMessage.success('删除成功')
      loadData()
    }).catch(() => {})
}

onMounted(() => loadData())
</script>

<style scoped lang="scss">
.ledger-manage {
  padding: 4px;
}

/* 册页头 */
.ledger-header {
  text-align: center;
  margin-bottom: 24px;

  .header-seal {
    width: 44px;
    height: 44px;
    margin: 0 auto 12px;
    border: 2px solid rgba(166, 64, 41, 0.3);
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-family: var(--font-serif);
    font-size: 20px;
    color: rgba(166, 64, 41, 0.6);
    transform: rotate(-3deg);
  }

  .header-title {
    font-family: var(--font-serif);
    font-size: 24px;
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
  border-color: rgba(166, 64, 41, 0.2);
  border-style: solid;

  &.corner-tl { top: 8px; left: 8px; border-width: 1px 0 0 1px; }
  &.corner-tr { top: 8px; right: 8px; border-width: 1px 1px 0 0; }
  &.corner-bl { bottom: 8px; left: 8px; border-width: 0 0 1px 1px; }
  &.corner-br { bottom: 8px; right: 8px; border-width: 0 1px 1px 0; }
}

/* 检索栏 */
.search-panel {
  margin-bottom: 20px;

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
      width: 180px;
    }

    .search-select {
      width: 120px;
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
      padding: 8px 20px;
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

/* 数据册页 */
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

  .user-cell {
    display: flex;
    align-items: center;
    gap: 8px;
    justify-content: center;

    .el-avatar {
      border: 1px solid rgba(212, 201, 184, 0.4);
    }
  }

  .no-image {
    color: var(--text-light);
    font-size: 12px;
  }

  .status-tag {
    display: inline-block;
    padding: 2px 8px;
    border-radius: 2px;
    font-size: 11px;
    letter-spacing: 1px;

    &.pending {
      background: rgba(230, 162, 60, 0.1);
      color: rgba(230, 162, 60, 0.9);
      border: 1px solid rgba(230, 162, 60, 0.2);
    }

    &.approved {
      background: rgba(103, 194, 58, 0.1);
      color: rgba(103, 194, 58, 0.9);
      border: 1px solid rgba(103, 194, 58, 0.2);
    }

    &.rejected {
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

      &.success {
        color: rgba(103, 194, 58, 0.9);
        border-color: rgba(103, 194, 58, 0.3);

        &:hover {
          background: rgba(103, 194, 58, 0.08);
        }
      }

      &.warning {
        color: rgba(230, 162, 60, 0.9);
        border-color: rgba(230, 162, 60, 0.3);

        &:hover {
          background: rgba(230, 162, 60, 0.08);
        }
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
  :deep(.el-pagination__sizes) {
    .el-select__wrapper {
      background: rgba(255, 253, 248, 0.8);
      border: 1px solid rgba(212, 201, 184, 0.4);
      border-radius: 4px;
      box-shadow: none !important;
    }
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

/* 弹窗样式 */
:deep(.ledger-dialog) {
  .el-dialog {
    border-radius: 6px;
    background: linear-gradient(145deg, #fffdf8 0%, #faf8f3 100%);
    border: 1px solid rgba(212, 201, 184, 0.4);
  }

  .el-dialog__header {
    padding: 0;
    margin: 0;
  }

  .el-dialog__body {
    padding: 20px 24px;
  }

  .el-dialog__footer {
    padding: 16px 24px 20px;
    border-top: 1px solid rgba(212, 201, 184, 0.25);
  }
}

.dialog-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 24px 16px;
  border-bottom: 1px solid rgba(212, 201, 184, 0.25);

  .header-seal {
    width: 32px;
    height: 32px;
    border: 1px solid rgba(166, 64, 41, 0.3);
    border-radius: 3px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-family: var(--font-serif);
    font-size: 14px;
    color: rgba(166, 64, 41, 0.6);
  }

  .header-title {
    font-family: var(--font-serif);
    font-size: 18px;
    font-weight: 600;
    color: var(--text-color);
    letter-spacing: 3px;
    margin: 0;
  }
}

.dialog-content {
  .content-section {
    display: flex;
    align-items: baseline;
    gap: 16px;
    padding: 12px 0;
    border-bottom: 1px solid rgba(212, 201, 184, 0.2);

    &.block {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;
    }

    &:last-child {
      border-bottom: none;
    }

    .section-label {
      width: 50px;
      font-size: 12px;
      color: var(--text-light);
      letter-spacing: 2px;
    }

    .section-value {
      flex: 1;
      font-size: 14px;
      color: var(--text-color);
      letter-spacing: 0.5px;

      &.highlight {
        font-family: var(--font-serif);
        font-weight: 500;
      }

      &.reject-reason {
        color: rgba(245, 108, 108, 0.8);
      }
    }

    .section-text {
      font-size: 14px;
      color: var(--text-secondary);
      line-height: 1.8;
      white-space: pre-wrap;
    }

    .image-list {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;

  button {
    padding: 8px 20px;
    border-radius: 4px;
    font-family: var(--font-serif);
    font-size: 13px;
    letter-spacing: 2px;
    cursor: pointer;
    transition: all var(--transition);
  }

  .btn-success {
    background: rgba(103, 194, 58, 0.9);
    border: none;
    color: #fff;

    &:hover {
      background: rgba(103, 194, 58, 1);
    }
  }

  .btn-warning {
    background: rgba(230, 162, 60, 0.9);
    border: none;
    color: #fff;

    &:hover {
      background: rgba(230, 162, 60, 1);
    }
  }

  .btn-default {
    background: transparent;
    border: 1px solid rgba(212, 201, 184, 0.5);
    color: var(--text-color);

    &:hover {
      border-color: var(--gold);
      background: rgba(201, 168, 76, 0.05);
    }
  }
}
</style>
