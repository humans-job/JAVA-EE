export const USER_TYPE_LABEL: Record<number, string> = {
  1: '民兵',
  2: '营/连/分队',
  3: '团机关',
  4: '师机关',
  5: '兵团机关'
}

export const HOME_PATH_BY_USER_TYPE: Record<number, string> = {
  1: '/app/militia/home',
  2: '/app/company/home',
  3: '/app/regiment/home',
  4: '/app/division/home',
  5: '/app/corps/home'
}

export type UserType = 1 | 2 | 3 | 4 | 5
