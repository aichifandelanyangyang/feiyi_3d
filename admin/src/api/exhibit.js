import request from '@/utils/request'

export function getExhibitList(params) {
  return request({ url: '/exhibit/list', method: 'get', params })
}

export function getExhibitDetail(id) {
  return request({ url: `/exhibit/get/${id}`, method: 'get' })
}

export function addExhibit(data) {
  return request({ url: '/admin/exhibit/add', method: 'post', data })
}

export function updateExhibit(id, data) {
  return request({ url: `/admin/exhibit/update/${id}`, method: 'post', data })
}

export function deleteExhibit(id) {
  return request({ url: `/admin/exhibit/delete/${id}`, method: 'post' })
}
