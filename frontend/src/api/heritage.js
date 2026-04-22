import request from '@/utils/request'

// 获取非遗项目列表
export const getHeritageList = (params) => {
  return request({
    url: '/heritage/list',
    method: 'get',
    params
  })
}

// 获取非遗项目详情
export const getHeritageDetail = (id) => {
  return request({
    url: `/heritage/get/${id}`,
    method: 'get'
  })
}

// 获取非遗分类列表
export const getCategoryList = () => {
  return request({
    url: '/heritage/category/list',
    method: 'get'
  })
}

// 搜索非遗项目
export const searchHeritage = (keyword) => {
  return request({
    url: '/heritage/search',
    method: 'get',
    params: { keyword }
  })
}

export default {
  getHeritageList,
  getHeritageDetail,
  getCategoryList,
  searchHeritage
}
