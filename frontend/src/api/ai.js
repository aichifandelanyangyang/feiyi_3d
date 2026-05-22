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

/**
 * AI对话 - SSE流式
 * @param {Object} data - {message, history}
 * @param {Object} callbacks - {onMetadata, onContent, onDone, onError}
 */
export function aiChatStream(data, callbacks) {
  const { onMetadata, onContent, onDone, onError } = callbacks
  const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
  const token = localStorage.getItem('token')

  let currentEvent = ''

  fetch(`${baseURL}/ai/chat/stream`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    },
    body: JSON.stringify(data)
  })
    .then(response => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`)
      }
      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      function read() {
        reader.read().then(({ done, value }) => {
          if (done) {
            onDone?.()
            return
          }

          buffer += decoder.decode(value, { stream: true })
          const lines = buffer.split('\n')
          buffer = lines.pop() || ''

          for (const line of lines) {
            if (!line.trim()) continue

            if (line.startsWith('event:')) {
              currentEvent = line.substring(6).trim()
            } else if (line.startsWith('data:')) {
              const dataStr = line.substring(5).trim()
              handleSseEvent(currentEvent, dataStr, callbacks)
            }
          }

          read()
        }).catch(err => {
          onError?.(err.message || '读取流失败')
        })
      }

      read()
    })
    .catch(err => {
      onError?.(err.message || '请求失败')
    })
}

function handleSseEvent(event, dataStr, callbacks) {
  const { onMetadata, onContent, onDone, onError } = callbacks

  switch (event) {
    case 'metadata':
      try {
        const data = JSON.parse(dataStr)
        onMetadata?.(data)
      } catch (e) {
        console.warn('解析metadata失败:', dataStr)
      }
      break
    case 'content':
      try {
        const data = JSON.parse(dataStr)
        onContent?.(data.text || '')
      } catch (e) {
        // 直接作为文本处理
        onContent?.(dataStr)
      }
      break
    case 'done':
      onDone?.()
      break
    case 'error':
      try {
        const data = JSON.parse(dataStr)
        onError?.(data.message || '服务异常')
      } catch (e) {
        onError?.(dataStr || '服务异常')
      }
      break
  }
}
