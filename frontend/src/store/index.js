import { defineStore } from 'pinia'

// 用户状态管理
export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null'),
    token: localStorage.getItem('token') || ''
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token,
    avatar: (state) => state.userInfo?.avatar || '',
    username: (state) => state.userInfo?.username || ''
  },
  
  actions: {
    // 设置用户信息
    setUserInfo(info) {
      this.userInfo = info
      localStorage.setItem('userInfo', JSON.stringify(info))
    },
    
    // 设置token
    setToken(token) {
      this.token = token
      localStorage.setItem('token', token)
    },

    // 更新头像
    updateAvatar(avatar) {
      if (this.userInfo) {
        this.userInfo.avatar = avatar
        localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
      }
    },
    
    // 登出
    logout() {
      this.userInfo = null
      this.token = ''
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
})

// 展厅状态管理
export const useExhibitionStore = defineStore('exhibition', {
  state: () => ({
    // 当前展厅信息
    currentExhibition: null,
    // 当前选中的展品
    selectedExhibit: null,
    // 漫游模式
    walkMode: true,
    // 移动速度
    moveSpeed: 5
  }),
  
  actions: {
    // 设置当前展厅
    setExhibition(exhibition) {
      this.currentExhibition = exhibition
    },
    
    // 设置选中展品
    setSelectedExhibit(exhibit) {
      this.selectedExhibit = exhibit
    },
    
    // 切换漫游模式
    toggleWalkMode() {
      this.walkMode = !this.walkMode
    },
    
    // 设置移动速度
    setMoveSpeed(speed) {
      this.moveSpeed = speed
    }
  }
})
