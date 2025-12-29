<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="brand">
        <div class="brand__title">民兵系统</div>
        <div class="brand__sub">Vue3 + Element Plus</div>
      </div>

      <el-menu :default-active="active" class="menu" router>
        <el-menu-item :index="homePath">
          <el-icon><HomeFilled /></el-icon>
          <span>我的主页</span>
        </el-menu-item>

        <el-menu-item index="/app/notices">
          <el-icon><BellFilled /></el-icon>
          <span>通知教育</span>
        </el-menu-item>

        <el-menu-item v-if="canArchive" index="/app/archives">
          <el-icon><Collection /></el-icon>
          <span>档案管理</span>
        </el-menu-item>

        <el-menu-item v-if="canMap" index="/app/map">
          <el-icon><MapLocation /></el-icon>
          <span>地图态势</span>
        </el-menu-item>

        <el-menu-item v-if="canLeave" index="/app/leave">
          <el-icon><Timer /></el-icon>
          <span>请销假</span>
        </el-menu-item>

        <el-menu-item v-if="canReports" index="/app/reports">
          <el-icon><DataAnalysis /></el-icon>
          <span>报表服务</span>
        </el-menu-item>

        <el-menu-item index="/app/profile">
          <el-icon><UserFilled /></el-icon>
          <span>个人信息</span>
        </el-menu-item>
      </el-menu>

      <div class="aside__footer">
        <div class="user">
          <div class="user__name">{{ auth.username }}</div>
          <div class="user__role">{{ roleLabel }}</div>
        </div>
        <el-button type="danger" plain size="small" @click="onLogout">退出登录</el-button>
      </div>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header__title">{{ pageTitle }}</div>
      </el-header>
      <el-main class="main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { HOME_PATH_BY_USER_TYPE, USER_TYPE_LABEL } from '@/utils/constants'
import {
  BellFilled,
  Collection,
  HomeFilled,
  MapLocation,
  Timer,
  DataAnalysis,
  UserFilled
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
auth.initFromStorage()

const active = computed(() => route.path)
const homePath = computed(() => HOME_PATH_BY_USER_TYPE[auth.userType] || '/app/notices')
const roleLabel = computed(() => USER_TYPE_LABEL[auth.userType] || `userType=${auth.userType}`)

const canArchive = computed(() => [3, 4, 5].includes(auth.userType))
const canMap = computed(() => [2, 3, 4, 5].includes(auth.userType))
const canReports = computed(() => [2, 3].includes(auth.userType))
const canLeave = computed(() => [1,2].includes(auth.userType))
const pageTitle = computed(() => (route.meta?.title as string) || '控制台')

async function onLogout() {
  try {
    await ElMessageBox.confirm('确认退出登录吗？', '提示', { type: 'warning' })
    auth.logout()
    router.replace('/login')
  } catch {
    // cancelled
  }
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
}
.aside {
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--el-border-color-light);
  background: var(--el-bg-color);
}
.brand {
  padding: 16px 14px 10px;
  border-bottom: 1px solid var(--el-border-color-light);
}
.brand__title {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.5px;
}
.brand__sub {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.menu {
  border-right: none;
  flex: 1;
}
.aside__footer {
  padding: 12px 14px;
  border-top: 1px solid var(--el-border-color-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}
.user {
  min-width: 0;
}
.user__name {
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.user__role {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.header {
  display: flex;
  align-items: center;
  border-bottom: 1px solid var(--el-border-color-light);
  background: var(--el-bg-color);
}
.header__title {
  font-size: 14px;
  font-weight: 600;
}
.main {
  background: var(--el-bg-color-page);
  padding: 16px;
}
</style>
