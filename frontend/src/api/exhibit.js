import request from '@/utils/request'

/**
 * 获取展品列表
 */
export function getExhibitList(params) {
  return request({
    url: '/exhibit/list',
    method: 'get',
    params
  })
}

/**
 * 获取展品详情（按ID）
 */
export function getExhibitDetail(id) {
  return request({
    url: `/exhibit/get/${id}`,
    method: 'get'
  })
}

/**
 * 获取展品详情（按名称模糊匹配）
 */
export function getExhibitByName(name) {
  return request({
    url: '/exhibit/getByName',
    method: 'get',
    params: { name }
  })
}

/**
 * 根据展厅ID获取展品列表
 */
export function getExhibitsByExhibition(exhibitionId) {
  return request({
    url: `/exhibit/listByExhibition/${exhibitionId}`,
    method: 'get'
  })
}
