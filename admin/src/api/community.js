import request from '@/utils/request'

export function getCommunityList(params) {
  return request({ url: '/admin/community/list', method: 'get', params })
}

export function approveCommunityPost(id) {
  return request({ url: `/admin/community/approve/${id}`, method: 'post' })
}

export function rejectCommunityPost(id, reason) {
  return request({ url: `/admin/community/reject/${id}`, method: 'post', params: { reason } })
}

export function deleteCommunityPost(id) {
  return request({ url: `/admin/community/delete/${id}`, method: 'post' })
}
