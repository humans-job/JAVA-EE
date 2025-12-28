<template>
  <el-card class="card" shadow="never">
    <div class="row">
      <div>
        <div class="h1">欢迎，{{ auth.username }}</div>
        <div class="sub">当前身份：{{ roleLabel }}（userType={{ auth.userType }}，userId={{ auth.userId }}）</div>
      </div>
      <div class="actions">
        <el-button type="primary" @click="$router.push('/app/notices')">通知教育</el-button>
        <el-button v-if="canArchive" @click="$router.push('/app/archives')">档案管理</el-button>
      </div>
    </div>

    <el-divider />

    <el-alert type="info" :closable="false" show-icon>
      <template #title>
        说明：路由守卫会根据 userType 限制访问；越权访问会被重定向到登录页并弹窗。
      </template>
    </el-alert>

    <div class="grid">
      <el-card class="mini" shadow="hover">
        <div class="mini__title">通知教育</div>
        <div class="mini__desc">查看我收到的通知/教育学习；支持标记已读、查看阅读反馈。</div>
        <el-button type="primary" plain @click="$router.push('/app/notices')">进入</el-button>
      </el-card>

      <el-card class="mini" shadow="hover" :class="{ disabled: !canArchive }">
        <div class="mini__title">档案管理</div>
        <div class="mini__desc">民兵档案：导入、编辑、提交审核、师部审核、归档。</div>
        <el-button :disabled="!canArchive" type="primary" plain @click="$router.push('/app/archives')">进入</el-button>
      </el-card>

      <el-card class="mini" shadow="hover">
        <div class="mini__title">地图态势</div>
        <div class="mini__desc">模块暂未完成：已预留页面与路由。</div>
        <el-button type="primary" plain @click="$router.push('/app/map')">查看</el-button>
      </el-card>

      <el-card class="mini" shadow="hover">
        <div class="mini__title">请销假</div>
        <div class="mini__desc">模块暂未完成：已预留页面与路由。</div>
        <el-button type="primary" plain @click="$router.push('/app/leave')">查看</el-button>
      </el-card>

      <el-card class="mini" shadow="hover">
        <div class="mini__title">报表服务</div>
        <div class="mini__desc">模块暂未完成：已预留页面与路由。</div>
        <el-button type="primary" plain @click="$router.push('/app/reports')">查看</el-button>
      </el-card>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { USER_TYPE_LABEL } from '@/utils/constants'

defineProps<{ roleKey: string }>()

const auth = useAuthStore()
auth.initFromStorage()

const roleLabel = computed(() => USER_TYPE_LABEL[auth.userType] || `userType=${auth.userType}`)
const canArchive = computed(() => [3, 4, 5].includes(auth.userType))
</script>

<style scoped>
.card {
  border-radius: 14px;
}
.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}
.actions {
  display: flex;
  gap: 10px;
}
.h1 {
  font-size: 18px;
  font-weight: 700;
}
.sub {
  margin-top: 4px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
.grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 12px;
}
.mini {
  border-radius: 14px;
}
.mini__title {
  font-weight: 700;
  font-size: 14px;
}
.mini__desc {
  margin: 8px 0 12px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  min-height: 40px;
}
.disabled {
  opacity: 0.75;
}
</style>
