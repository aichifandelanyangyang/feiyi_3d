import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/login',
    method: 'post',
    data
  })
}

export function register(data) {
  return request({
    url: '/register',
    method: 'post',
    data
  })
}

export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

export function updateAvatar(avatar) {
  return request({
    url: '/user/avatar',
    method: 'post',
    params: { avatar }
  })
}

export function updateProfile(data) {
  return request({
    url: '/user/profile',
    method: 'post',
    data
  })
}

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

// 收藏相关
export function addFavorite(heritageId) {
  return request({ url: `/favorite/add/${heritageId}`, method: 'post' })
}

export function removeFavorite(heritageId) {
  return request({ url: `/favorite/remove/${heritageId}`, method: 'post' })
}

export function checkFavorite(heritageId) {
  return request({ url: `/favorite/check/${heritageId}`, method: 'get' })
}

export function getFavoriteList(params) {
  return request({ url: '/favorite/list', method: 'get', params })
}
