import { defineStore } from 'pinia'
import { clearAuth, readAuth, writeAuth, type StoredAuth } from '@/utils/storage'
import { getUserIdFromToken } from '@/utils/jwt'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: '' as string,
    username: '' as string,
    userType: 0 as number,
    userId: 0 as number,
    _inited: false as boolean
  }),
  getters: {
    isLoggedIn: (s) => Boolean(s.token && s.userType && s.userId)
  },
  actions: {
    initFromStorage() {
      if (this._inited) return
      const cached = readAuth()
      if (cached) {
        this.token = cached.token
        this.username = cached.username
        this.userType = cached.userType
        this.userId = cached.userId
      }
      this._inited = true
    },
    setAuth(partial: Omit<StoredAuth, 'userId'> & { userId?: number | null }) {
      const uid = partial.userId ?? getUserIdFromToken(partial.token) ?? 0
      const auth: StoredAuth = {
        token: partial.token,
        username: partial.username,
        userType: partial.userType,
        userId: uid
      }
      this.token = auth.token
      this.username = auth.username
      this.userType = auth.userType
      this.userId = auth.userId
      writeAuth(auth)
    },
    logout() {
      this.token = ''
      this.username = ''
      this.userType = 0
      this.userId = 0
      this._inited = true
      clearAuth()
    }
  }
})
