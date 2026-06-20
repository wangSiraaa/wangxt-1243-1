import { defineStore } from 'pinia'
import { authApi } from '@/api'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null'),
    role: localStorage.getItem('role') || ''
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    isAdmin: (state) => state.role === 'ADMIN',
    isManager: (state) => state.role === 'MANAGER',
    isPersonnel: (state) => state.role === 'PERSONNEL',
    isProjectManager: (state) => state.role === 'PROJECT_MANAGER',
    isTeamLeader: (state) => state.role === 'TEAM_LEADER',
    isCustomer: (state) => state.role === 'CUSTOMER',
    roleName: (state) => {
      const roleMap = {
        PROJECT_MANAGER: '项目经理',
        TEAM_LEADER: '队长',
        CUSTOMER: '客户方',
        ADMIN: '管理员',
        MANAGER: '经理',
        PERSONNEL: '人员'
      }
      return roleMap[state.role] || state.role
    }
  },

  actions: {
    async login(loginData) {
      try {
        const response = await authApi.login(loginData)
        const data = response.data
        this.token = data.token
        this.role = data.role
        this.userInfo = data.userInfo || null
        localStorage.setItem('token', this.token)
        localStorage.setItem('role', this.role)
        if (this.userInfo) {
          localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
        }
        return data
      } catch (error) {
        throw error
      }
    },

    async getCurrentUser() {
      try {
        const response = await authApi.getCurrentUser()
        const data = response.data
        this.userInfo = data
        this.role = data.role || this.role
        localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
        localStorage.setItem('role', this.role)
        return data
      } catch (error) {
        throw error
      }
    },

    logout() {
      this.token = ''
      this.userInfo = null
      this.role = ''
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('role')
    }
  }
})
