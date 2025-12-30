import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { HOME_PATH_BY_USER_TYPE } from '@/utils/constants'

const LoginView = () => import('@/views/login/LoginView.vue')
const MainLayout = () => import('@/layouts/MainLayout.vue')
const RoleHome = () => import('@/views/home/RoleHome.vue')
const NoticeCenterView = () => import('@/views/notice/NoticeCenterView.vue')
const ArchiveListView = () => import('@/views/archive/ArchiveListView.vue')
const PlaceholderModule = () => import('@/views/placeholder/PlaceholderModule.vue')
const ProfileView = () => import('@/views/profile/ProfileView.vue')
const ReportCenterView = () => import('@/views/report/ReportCenterView.vue')
const LeaveCenterView = () => import('@/views/leave/LeaveCenterView.vue')
const MapSituationView = () => import('@/views/map/MapSituationView.vue')
declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    allowedUserTypes?: number[]
    title?: string
  }
}

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { title: '登录' }
  },
  {
    path: '/',
    redirect: '/app'
  },
  {
    path: '/app',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/app/notices' },

      { path: 'militia/home', component: RoleHome, meta: { requiresAuth: true, allowedUserTypes: [1], title: '主页' }, props: { roleKey: 'militia' } },
      { path: 'company/home', component: RoleHome, meta: { requiresAuth: true, allowedUserTypes: [2], title: '主页' }, props: { roleKey: 'company' } },
      { path: 'regiment/home', component: RoleHome, meta: { requiresAuth: true, allowedUserTypes: [3], title: '主页' }, props: { roleKey: 'regiment' } },
      { path: 'division/home', component: RoleHome, meta: { requiresAuth: true, allowedUserTypes: [4], title: '主页' }, props: { roleKey: 'division' } },
      { path: 'corps/home', component: RoleHome, meta: { requiresAuth: true, allowedUserTypes: [5], title: '主页' }, props: { roleKey: 'corps' } },

      { path: 'notices', component: NoticeCenterView, meta: { requiresAuth: true, allowedUserTypes: [3,4], title: '通知教育' } },
      { path: 'archives', component: ArchiveListView, meta: { requiresAuth: true, allowedUserTypes: [3,4,5], title: '档案管理' } },

      { path: 'map', component: MapSituationView, meta: { requiresAuth: true, allowedUserTypes: [2,3,4,5], title: '地图态势' }},
      { path: 'leave', component: LeaveCenterView, meta: { requiresAuth: true, allowedUserTypes: [1,2], title: '请销假' } },
      { path: 'reports', component: ReportCenterView, meta: { requiresAuth: true, allowedUserTypes: [2,3], title: '报表服务' } },

      { path: 'profile', component: ProfileView, meta: { requiresAuth: true, allowedUserTypes: [1,2,3,4,5], title: '个人信息' } },
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

let showingBlocked = false
router.beforeEach(async (to) => {
  const auth = useAuthStore()
  auth.initFromStorage()

  if (to.path === '/login' && auth.isLoggedIn) {
    return HOME_PATH_BY_USER_TYPE[auth.userType] || '/app/notices'
  }

  if (!to.meta.requiresAuth) return true

  if (!auth.isLoggedIn) {
    if (!showingBlocked) {
      showingBlocked = true
      ElMessageBox.alert('请先登录后再访问该页面。', '未登录', { type: 'warning' }).finally(() => (showingBlocked = false))
    }
    return { path: '/login' }
  }

  const allow = to.meta.allowedUserTypes
  if (allow && !allow.includes(auth.userType)) {
    // 按需求：访问不能访问的页面 -> 导到登录并弹窗
    auth.logout()
    if (!showingBlocked) {
      showingBlocked = true
      ElMessageBox.alert('你没有权限访问该页面，请重新登录。', '权限不足', { type: 'error' }).finally(() => (showingBlocked = false))
    }
    return { path: '/login' }
  }

  // /app 空路径时：按 userType 自动导到各自主页（可选）
  if (to.path === '/app') {
    return HOME_PATH_BY_USER_TYPE[auth.userType] || '/app/notices'
  }

  return true
})

router.afterEach((to) => {
  if (to.meta?.title) document.title = `民兵系统 - ${to.meta.title}`
})

export default router
