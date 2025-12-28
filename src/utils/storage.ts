export type StoredAuth = {
  token: string
  username: string
  userType: number
  userId: number
}

const KEY = 'militia_auth'

export function readAuth(): StoredAuth | null {
  const raw = localStorage.getItem(KEY)
  if (!raw) return null
  try {
    const obj = JSON.parse(raw) as StoredAuth
    if (!obj?.token || !obj?.username || !obj?.userType || !obj?.userId) return null
    return obj
  } catch {
    return null
  }
}

export function writeAuth(auth: StoredAuth) {
  localStorage.setItem(KEY, JSON.stringify(auth))
}

export function clearAuth() {
  localStorage.removeItem(KEY)
}
