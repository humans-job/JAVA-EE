<template>
  <el-card class="card" shadow="never">
    <div class="row">
      <div>
        <div class="h1">{{ title }}</div>
        <div class="sub">{{ hint }}</div>
      </div>
      <el-button type="primary" @click="$router.push('/app/notices')">回到通知教育</el-button>
    </div>

    <el-divider />

    <el-empty description="模块开发中" />
    <div style="margin-top: 8px; text-align: center">
      <el-button @click="$router.push('/app/profile')"></el-button>
    </div>
  </el-card>
</template>

<script setup lang="ts">
defineProps<{ title: string; hint: string }>()
</script>

<style scoped>
.card { border-radius: 14px; }
.row { display: flex; align-items: center; justify-content: space-between; gap: 10px; }
.h1 { font-size: 18px; font-weight: 700; }
.sub { margin-top: 4px; color: var(--el-text-color-secondary); font-size: 13px; }
</style>
