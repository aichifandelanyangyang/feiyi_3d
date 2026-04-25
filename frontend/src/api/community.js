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

// 帖子收藏相关
export function addPostFavorite(postId) {
  return request({ url: `/community/favorite/${postId}`, method: 'post' })
}

export function removePostFavorite(postId) {
  return request({ url: `/community/unfavorite/${postId}`, method: 'post' })
}

export function checkPostFavorite(postId) {
  return request({ url: `/community/favorite/check/${postId}`, method: 'get' })
}

export function getPostFavoriteList(params) {
  return request({ url: '/community/favorite/list', method: 'get', params })
}

// 帖子点赞相关
export function addPostLike(postId) {
  return request({ url: `/community/like/${postId}`, method: 'post' })
}

export function removePostLike(postId) {
  return request({ url: `/community/unlike/${postId}`, method: 'post' })
}

export function checkPostLike(postId) {
  return request({ url: `/community/like/check/${postId}`, method: 'get' })
}

export function getPostLikeList(params) {
  return request({ url: '/community/like/list', method: 'get', params })
}
