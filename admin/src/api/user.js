import request from '@/utils/request'

export function login(data) {
  return request({ url: '/login', method: 'post', data })
}

export function getUserList(params) {
  return request({ url: '/admin/user/list', method: 'get', params })
}

export function getUserDetail(id) {
  return request({ url: `/admin/user/get/${id}`, method: 'get' })
}

export function addUser(data) {
  return request({ url: '/admin/user/add', method: 'post', data })
}

export function updateUser(data) {
  return request({ url: '/admin/user/update', method: 'post', data })
}

export function deleteUser(id) {
  return request({ url: `/admin/user/delete/${id}`, method: 'post' })
}

export function resetPassword(id, newPassword) {
  return request({ url: '/admin/user/resetPassword', method: 'post', params: { id, newPassword } })
}

export function toggleUserStatus(id) {
  return request({ url: `/admin/user/toggleStatus/${id}`, method: 'post' })
}

export function getMyInfo() {
  return request({ url: '/user/info', method: 'get' })
}

export function updateProfile(data) {
  return request({ url: '/user/profile', method: 'post', data })
}

export function updateAvatar(avatar) {
  return request({ url: '/user/avatar', method: 'post', params: { avatar } })
}

export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({ url: '/admin/file/upload/image', method: 'post', data: formData, headers: { 'Content-Type': 'multipart/form-data' } })
}
