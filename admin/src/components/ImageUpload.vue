<template>
  <div class="image-upload">
    <el-upload
      class="image-uploader"
      :show-file-list="false"
      :http-request="handleUpload"
      accept="image/*"
      :before-upload="beforeUpload"
    >
      <img v-if="imageUrl" :src="imageUrl" class="preview-image" />
      <div v-else class="upload-placeholder">
        <el-icon class="upload-icon"><Plus /></el-icon>
        <span class="upload-text">上传图片</span>
      </div>
    </el-upload>
    <div v-if="imageUrl" class="image-actions">
      <el-button type="danger" link size="small" @click.stop="handleRemove">删除</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadImage } from '@/api/file'

const props = defineProps({
  modelValue: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])

const imageUrl = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }
  return true
}

const handleUpload = async (options) => {
  try {
    const res = await uploadImage(options.file)
    imageUrl.value = res.data.url
    ElMessage.success('上传成功')
  } catch (e) {
    ElMessage.error('上传失败')
  }
}

const handleRemove = () => {
  imageUrl.value = ''
}
</script>

<style scoped lang="scss">
.image-upload {
  display: inline-block;
}

.image-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 8px;
    cursor: pointer;
    overflow: hidden;
    transition: border-color 0.3s;

    &:hover {
      border-color: #409eff;
    }
  }
}

.preview-image {
  width: 148px;
  height: 148px;
  object-fit: cover;
  display: block;
}

.upload-placeholder {
  width: 148px;
  height: 148px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c939d;

  .upload-icon {
    font-size: 28px;
    margin-bottom: 8px;
  }

  .upload-text {
    font-size: 12px;
  }
}

.image-actions {
  text-align: center;
  margin-top: 4px;
}
</style>
