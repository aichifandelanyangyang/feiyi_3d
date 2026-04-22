import request from './request'

// 获取展厅信息
export const getExhibitionInfo = () => {
  return request({
    url: '/exhibition/info',
    method: 'get'
  })
}

// 获取展品列表
export const getExhibitList = (exhibitionId) => {
  return request({
    url: '/exhibition/exhibit/list',
    method: 'get',
    params: { exhibitionId }
  })
}

// 获取展品详情
export const getExhibitDetail = (id) => {
  return request({
    url: `/exhibition/exhibit/get/${id}`,
    method: 'get'
  })
}

// 记录用户访问
export const recordVisit = (data) => {
  return request({
    url: '/exhibition/visit/add',
    method: 'post',
    data
  })
}

export default {
  getExhibitionInfo,
  getExhibitList,
  getExhibitDetail,
  recordVisit
}
