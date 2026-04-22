import request from '@/utils/request'

// ========== 非遗项目 ==========

export function getHeritageList(params) {
  return request({ url: '/heritage/list', method: 'get', params })
}

export function getHeritageDetail(id) {
  return request({ url: `/heritage/get/${id}`, method: 'get' })
}

export function addHeritage(data) {
  return request({ url: '/admin/heritage/add', method: 'post', data })
}

export function updateHeritage(id, data) {
  return request({ url: `/admin/heritage/update/${id}`, method: 'post', data })
}

export function deleteHeritage(id) {
  return request({ url: `/admin/heritage/delete/${id}`, method: 'post' })
}

// ========== 非遗分类 ==========

export function getCategoryList() {
  return request({ url: '/heritage/category/list', method: 'get' })
}

export function addCategory(data) {
  return request({ url: '/admin/heritage/category/add', method: 'post', data })
}

export function updateCategory(id, data) {
  return request({ url: `/admin/heritage/category/update/${id}`, method: 'post', data })
}

export function deleteCategory(id) {
  return request({ url: `/admin/heritage/category/delete/${id}`, method: 'post' })
}
