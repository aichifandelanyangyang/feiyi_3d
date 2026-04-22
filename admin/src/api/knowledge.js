import request from '@/utils/request'

export function getKnowledgeList() {
  return request({ url: '/admin/knowledge/list', method: 'get' })
}

export function addKnowledge(data) {
  return request({ url: '/admin/knowledge/add', method: 'post', data })
}

export function updateKnowledge(id, data) {
  return request({ url: `/admin/knowledge/update/${id}`, method: 'post', data })
}

export function deleteKnowledge(id) {
  return request({ url: `/admin/knowledge/delete/${id}`, method: 'post' })
}
