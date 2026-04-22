import request from '@/utils/request'

/**
 * AI对话
 */
export function aiChat(data) {
  return request({
    url: '/ai/chat',
    method: 'post',
    data,
    timeout: 60000
  })
}
