import request from '@/utils/request'

export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/admin/file/upload/image',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function uploadFile(file, folder = 'file') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('folder', folder)
  return request({
    url: '/admin/file/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
