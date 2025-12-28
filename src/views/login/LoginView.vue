<template>
  <div class="wrap">
    <el-card class="card" shadow="always">
      <template #header>
        <div class="title">系统登录</div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px" @keyup.enter="onSubmit">
        <el-form-item label="认证方式" prop="authType">
          <el-radio-group v-model="form.authType">
            <el-radio-button :label="1">账号密码</el-radio-button>
            <el-radio-button :label="2">USB Key</el-radio-button>
            <el-radio-button :label="3">证书</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <!-- 只有账号密码才需要用户名 -->
        <el-form-item v-if="form.authType === 1" label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item v-if="form.authType === 1" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>

        <el-form-item v-if="form.authType === 2" label="USB Key" prop="usbKey">
          <el-input v-model="form.usbKey" placeholder="请输入 USB Key 序列号" />
        </el-form-item>

        <el-form-item v-if="form.authType === 3" label="证书SN" prop="certSn">
          <el-input v-model="form.certSn" placeholder="请输入证书序列号" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSubmit" style="width: 100%">登录</el-button>
        </el-form-item>
      </el-form>

      <el-alert type="info" :closable="false" show-icon>
        <template #title>
          后端登录接口：POST /api/auth/login（authType: 1=账号密码 / 2=USB Key / 3=证书）
        </template>
      </el-alert>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { apiLogin } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { HOME_PATH_BY_USER_TYPE } from '@/utils/constants'
import { useRouter } from 'vue-router'

const router = useRouter()
const auth = useAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  authType: 1 as number,
  username: '',
  password: '',
  usbKey: '',
  certSn: ''
})

const rules: FormRules = {
  authType: [{ required: true, message: '请选择认证方式', trigger: 'change' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  usbKey: [{ required: true, message: '请输入 USB Key', trigger: 'blur' }],
  certSn: [{ required: true, message: '请输入证书序列号', trigger: 'blur' }]
}

watch(
    () => form.authType,
    () => {
      // 切换认证方式：清掉所有输入，避免残留导致误提交/误校验
      form.username = ''
      form.password = ''
      form.usbKey = ''
      form.certSn = ''
      formRef.value?.clearValidate()
    }
)

async function onSubmit() {
  if (!formRef.value) return

  // 只校验当前 authType 需要的字段
  const props: string[] = ['authType']
  if (form.authType === 1) props.push('username', 'password')
  if (form.authType === 2) props.push('usbKey')
  if (form.authType === 3) props.push('certSn')

  try {
    await formRef.value.validateField(props as any)
  } catch {
    return
  }

  loading.value = true
  try {
    // 按类型组织 payload：USBKey/证书不传 username/password
    const payload =
        form.authType === 1
            ? { authType: 1, username: form.username, password: form.password }
            : form.authType === 2
                ? { authType: 2, username: '', usbKey: form.usbKey }   // ✅ 关键：username 给空串
                : { authType: 3, username: '', certSn: form.certSn }    // ✅ 关键：username 给空串


    const r = await apiLogin(payload as any)

    if (!r.data?.token) {
      ElMessage.error(r.msg || '登录失败')
      return
    }

    // username：USBKey/证书登录后，后端可能返回 username；不返回就给一个兜底展示名
    const displayName =
        r.data.username ||
        (form.authType === 1 ? form.username : form.authType === 2 ? `USB:${form.usbKey}` : `CERT:${form.certSn}`)

    auth.setAuth({
      token: r.data.token,
      username: displayName,
      userType: r.data.userType
    })

    ElMessage.success(r.msg || '登录成功')

    const target = HOME_PATH_BY_USER_TYPE[r.data.userType] || '/app/notices'
    router.replace(target)
  } catch (e: any) {
    ElMessage.error(e?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: linear-gradient(180deg, var(--el-color-primary-light-9), var(--el-bg-color-page));
}
.card {
  width: 420px;
  border-radius: 14px;
}
.title {
  font-size: 18px;
  font-weight: 700;
}
</style>
