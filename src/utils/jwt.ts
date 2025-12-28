// 简单 JWT 解析（不做签名验证）：仅用于从 token payload 里取 userId 等字段。
// 后端 Auth0JwtUtil: claim key 为 "data"，其中包含 { userId: number }。

export type JwtPayload = {
  exp?: number
  iat?: number
  data?: Record<string, any>
}

function base64UrlDecode(str: string) {
  // base64url -> base64
  const base64 = str.replace(/-/g, '+').replace(/_/g, '/')
  // 补齐 padding
  const pad = base64.length % 4
  const padded = pad ? base64 + '='.repeat(4 - pad) : base64
  try {
    return decodeURIComponent(
      atob(padded)
        .split('')
        .map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
        .join('')
    )
  } catch {
    // fallback（非 UTF-8）
    return atob(padded)
  }
}

export function parseJwt(token: string): JwtPayload | null {
  if (!token) return null
  const parts = token.split('.')
  if (parts.length < 2) return null
  try {
    const json = base64UrlDecode(parts[1])
    return JSON.parse(json) as JwtPayload
  } catch {
    return null
  }
}

export function getUserIdFromToken(token: string): number | null {
  const payload = parseJwt(token)
  const v = payload?.data?.userId
  const n = typeof v === 'string' ? Number(v) : typeof v === 'number' ? v : null
  return Number.isFinite(n as number) ? (n as number) : null
}
