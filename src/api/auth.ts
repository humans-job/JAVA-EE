import http, { assertOk, type ApiResp } from './http'

export type LoginReq = {
  authType: number
  username?: string
  password?: string
  usbKey?: string
  certSn?: string
  loginIp?: string
}

export type LoginResp = {
  token: string
  username: string
  userType: number
}

export async function apiLogin(req: LoginReq) {
  const { data } = await http.post<ApiResp<LoginResp>>('/api/auth/login', req)
  return assertOk(data)
}
