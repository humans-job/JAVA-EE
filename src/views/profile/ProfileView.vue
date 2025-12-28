<template>
  <el-card class="card" shadow="never">
    <div class="h1">个人信息</div>
    <div class="sub">这些信息来自登录接口并存储在 localStorage。</div>

    <el-divider />

    <el-descriptions :column="2" border>
      <el-descriptions-item label="用户ID">{{ auth.userId }}</el-descriptions-item>
      <el-descriptions-item label="用户名">{{ auth.username }}</el-descriptions-item>
      <el-descriptions-item label="用户类型">{{ roleLabel }}</el-descriptions-item>
      <el-descriptions-item label="Token" :span="2">
        <el-input :model-value="auth.token" type="textarea" :rows="3" readonly />
      </el-descriptions-item>
    </el-descriptions>

    <el-divider />
    <el-alert type="warning" :closable="false" show-icon>
      <template #title>
        提示：后端鉴权依赖请求头 Authorization: Bearer &lt;token&gt;（或 token 头）。
      </template>
    </el-alert>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { USER_TYPE_LABEL } from '@/utils/constants'

const auth = useAuthStore()
auth.initFromStorage()

const roleLabel = computed(() => USER_TYPE_LABEL[auth.userType] || `userType=${auth.userType}`)
</script>

<style scoped>
.card { border-radius: 14px; }
.h1 { font-size: 18px; font-weight: 700; }
.sub { margin-top: 4px; color: var(--el-text-color-secondary); font-size: 13px; }
</style>
