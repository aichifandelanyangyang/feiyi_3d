import request from '@/utils/request'

export function getCommunityList(params) {
  return request({ url: '/community/list', method: 'get', params })
}

export function getCommunityDetail(id) {
  return request({ url: `/community/get/${id}`, method: 'get' })
}

export function createPost(data) {
  return request({ url: '/community/post', method: 'post', data })
}

export function getMyPosts(params) {
  return request({ url: '/community/my', method: 'get', params })
}
