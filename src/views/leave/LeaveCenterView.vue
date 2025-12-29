<template>
  <el-card class="card" shadow="never">
    <template #header>
      <div class="head">
        <div>
          <div class="h1">请销假管理</div>
          <div class="sub">
            {{ isMilitia
              ? '提交请假申请、查看审批状态、销假打卡。'
              : '审批下属请假申请、确认销假归档。' }}
          </div>
        </div>

        <div class="actions">
          <!-- 民兵可以申请请假 -->
          <el-button v-if="isMilitia" type="primary" @click="openApply">申请请假</el-button>
          <el-button @click="load(1)">刷新</el-button>
        </div>
      </div>
    </template>

    <!-- 统计卡片 -->
    <div class="stats-row" v-if="stats">
      <el-card class="stat-card" shadow="hover">
        <el-statistic title="待审批" :value="stats.pendingCount || 0" />
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <el-statistic title="已通过" :value="stats.approvedCount || 0" />
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <el-statistic title="已驳回" :value="stats.rejectedCount || 0" />
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <el-statistic title="待确认" :value="stats.reportedCount || 0" />
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <el-statistic title="已归档" :value="stats.confirmedCount || 0" />
      </el-card>
    </div>

    <!-- 筛选条件 -->
    <el-form :inline="true" :model="query" class="filters">
      <el-form-item label="月份">
        <el-date-picker
            v-model="query.month"
            type="month"
            placeholder="选择月份"
            format="YYYY-MM"
            value-format="YYYY-MM"
            style="width: 160px"
            clearable
        />
      </el-form-item>

      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" style="width: 160px">
          <el-option :value="0" label="待审批" />
          <el-option :value="1" label="已通过" />
          <el-option :value="2" label="已驳回" />
          <el-option :value="3" label="待确认" />
          <el-option :value="4" label="已归档" />
        </el-select>
      </el-form-item>

      <!-- 管理员可筛选待确认 -->
      <el-form-item v-if="!isMilitia" label="快捷筛选">
        <el-checkbox v-model="query.onlyWaitConfirm">只看待确认</el-checkbox>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="load(1)">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 数据表格 -->
    <el-table :data="rows" border row-key="leaveId" v-loading="loading">
      <el-table-column prop="leaveId" label="ID" width="80" />

      <!-- 管理员视角显示申请人 -->
      <el-table-column v-if="!isMilitia" prop="username" label="申请人" width="120" show-overflow-tooltip />

      <el-table-column prop="leaveReason" label="请假事由" min-width="180" show-overflow-tooltip />

      <el-table-column prop="startTime" label="开始时间" width="160">
        <template #default="{ row }">
          {{ fmt(row.startTime) }}
        </template>
      </el-table-column>

      <el-table-column prop="endTime" label="结束时间" width="160">
        <template #default="{ row }">
          {{ fmt(row.endTime) }}
        </template>
      </el-table-column>

      <el-table-column prop="applyTime" label="申请时间" width="160">
        <template #default="{ row }">
          {{ fmt(row.applyTime) }}
        </template>
      </el-table-column>

      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">
            {{ statusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column fixed="right" label="操作" width="240">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>

          <!-- 民兵：驳回后可重新提交 -->
          <el-button
              v-if="isMilitia && row.status === 2"
              link
              type="warning"
              @click="openResubmit(row)"
          >
            重新提交
          </el-button>

          <!-- 民兵：已通过可销假打卡 -->
          <el-button
              v-if="isMilitia && row.status === 1"
              link
              type="success"
              @click="openReportBack(row)"
          >
            销假打卡
          </el-button>

          <!-- 管理员：待审批可审批 -->
          <el-button
              v-if="!isMilitia && row.status === 0"
              link
              type="warning"
              @click="openApprove(row)"
          >
            审批
          </el-button>

          <!-- 管理员：待确认可确认归档 -->
          <el-button
              v-if="!isMilitia && row.status === 3"
              link
              type="success"
              :loading="confirmingId === row.leaveId"
              @click="confirmLeave(row.leaveId)"
          >
            确认归档
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pager">
      <el-pagination
          background
          layout="total, prev, pager, next, sizes"
          :total="total"
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          @current-change="load"
          @size-change="load(1)"
      />
    </div>
  </el-card>

  <!-- 详情弹窗 -->
  <el-dialog v-model="detailVisible" title="请假详情" width="640px">
    <el-descriptions v-if="detailRow" :column="2" border>
      <el-descriptions-item label="请假ID">{{ detailRow.leaveId }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="statusTag(detailRow.status)">{{ statusText(detailRow.status) }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="请假事由" :span="2">{{ detailRow.leaveReason }}</el-descriptions-item>
      <el-descriptions-item label="开始时间">{{ fmt(detailRow.startTime) }}</el-descriptions-item>
      <el-descriptions-item label="结束时间">{{ fmt(detailRow.endTime) }}</el-descriptions-item>
      <el-descriptions-item label="申请时间">{{ fmt(detailRow.applyTime) }}</el-descriptions-item>
      <el-descriptions-item label="审批意见">{{ detailRow.approveOpinion || '-' }}</el-descriptions-item>
      <el-descriptions-item label="销假时间">{{ detailRow.reportBackTime ? fmt(detailRow.reportBackTime) : '-' }}</el-descriptions-item>
      <el-descriptions-item label="销假定位">{{ detailRow.reportBackLocation || '-' }}</el-descriptions-item>
    </el-descriptions>

    <template #footer>
      <el-button @click="detailVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 申请请假弹窗 -->
  <el-dialog v-model="applyVisible" title="申请请假" width="560px">
    <el-form :model="applyForm" label-width="90px">
      <el-form-item label="请假事由" required>
        <el-input
            v-model.trim="applyForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入请假事由"
        />
      </el-form-item>

      <el-form-item label="开始时间" required>
        <el-date-picker
            v-model="applyForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="结束时间" required>
        <el-date-picker
            v-model="applyForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="applyVisible = false">取消</el-button>
      <el-button type="primary" :loading="applying" @click="submitApply">提交申请</el-button>
    </template>
  </el-dialog>

  <!-- 重新提交弹窗 -->
  <el-dialog v-model="resubmitVisible" title="重新提交请假" width="560px">
    <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px">
      <template #title>您的请假申请已被驳回，请修改后重新提交。</template>
    </el-alert>

    <el-form :model="resubmitForm" label-width="90px">
      <el-form-item label="请假事由" required>
        <el-input
            v-model.trim="resubmitForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入请假事由"
        />
      </el-form-item>

      <el-form-item label="开始时间" required>
        <el-date-picker
            v-model="resubmitForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="结束时间" required>
        <el-date-picker
            v-model="resubmitForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="resubmitVisible = false">取消</el-button>
      <el-button type="primary" :loading="resubmitting" @click="submitResubmit">重新提交</el-button>
    </template>
  </el-dialog>

  <!-- 销假打卡弹窗 -->
  <el-dialog v-model="reportBackVisible" title="销假打卡" width="480px">
    <el-alert type="success" :closable="false" show-icon style="margin-bottom: 16px">
      <template #title>请确认您已归队，填写当前位置完成销假。</template>
    </el-alert>

    <el-form :model="reportBackForm" label-width="90px">
      <el-form-item label="当前位置" required>
        <el-input
            v-model.trim="reportBackForm.reportBackLocation"
            placeholder="请输入当前位置（如：XX营区）"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="reportBackVisible = false">取消</el-button>
      <el-button type="primary" :loading="reportingBack" @click="submitReportBack">确认打卡</el-button>
    </template>
  </el-dialog>

  <!-- 审批弹窗 -->
  <el-dialog v-model="approveVisible" title="审批请假" width="560px">
    <el-descriptions v-if="approveRow" :column="1" border style="margin-bottom: 16px">
      <el-descriptions-item label="申请人">{{ approveRow.username || `用户${approveRow.userId}` }}</el-descriptions-item>
      <el-descriptions-item label="请假事由">{{ approveRow.leaveReason }}</el-descriptions-item>
      <el-descriptions-item label="请假时间">
        {{ fmt(approveRow.startTime) }} ~ {{ fmt(approveRow.endTime) }}
      </el-descriptions-item>
    </el-descriptions>

    <el-form :model="approveForm" label-width="90px">
      <el-form-item label="审批结果" required>
        <el-radio-group v-model="approveForm.status">
          <el-radio :value="1">通过</el-radio>
          <el-radio :value="2">驳回</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="审批意见">
        <el-input
            v-model.trim="approveForm.approveOpinion"
            type="textarea"
            :rows="3"
            placeholder="请输入审批意见（可选）"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="approveVisible = false">取消</el-button>
      <el-button type="primary" :loading="approving" @click="submitApprove">提交审批</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import {
  apiLeaveList,
  apiLeaveSubmit,
  apiLeaveResubmit,
  apiLeaveApprove,
  apiLeaveReportBack,
  apiLeaveConfirm,
  type LeaveRecord,
  type LeaveStatsResponse
} from '@/api/leave'

const auth = useAuthStore()
auth.initFromStorage()

// 民兵 userType=1
const isMilitia = computed(() => auth.userType === 1)

const loading = ref(false)
const rows = ref<LeaveRecord[]>([])
const total = ref(0)
const stats = ref<Partial<LeaveStatsResponse> | null>(null)

const query = reactive({
  month: undefined as string | undefined,
  status: undefined as number | undefined,
  pageNum: 1,
  pageSize: 10,
  onlyWaitConfirm: false
})

// 格式化时间
function fmt(v?: string) {
  if (!v) return '-'
  return dayjs(v).format('YYYY-MM-DD HH:mm')
}

// 状态文本
function statusText(s: number) {
  const map: Record<number, string> = {
    0: '待审批',
    1: '已通过',
    2: '已驳回',
    3: '待确认',
    4: '已归档'
  }
  return map[s] || `状态${s}`
}

// 状态标签颜色
function statusTag(s: number) {
  const map: Record<number, string> = {
    0: 'warning',
    1: 'success',
    2: 'danger',
    3: 'info',
    4: ''
  }
  return map[s] || ''
}

// 加载数据
async function load(page?: number) {
  if (page) query.pageNum = page
  loading.value = true
  try {
    const r = await apiLeaveList({
      month: query.month,
      status: query.status,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      queryType: isMilitia.value ? 2 : 1,
      onlyWaitConfirm: query.onlyWaitConfirm || undefined
    })

    const data = r.data.data
    rows.value = data?.records || []
    total.value = Number(data?.total || 0)

    // 保存统计数据
    stats.value = {
      pendingCount: data?.pendingCount,
      approvedCount: data?.approvedCount,
      rejectedCount: data?.rejectedCount,
      reportedCount: data?.reportedCount,
      confirmedCount: data?.confirmedCount
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function reset() {
  query.month = undefined
  query.status = undefined
  query.onlyWaitConfirm = false
  load(1)
}

// ========== 详情 ==========
const detailVisible = ref(false)
const detailRow = ref<LeaveRecord | null>(null)

function openDetail(row: LeaveRecord) {
  detailRow.value = row
  detailVisible.value = true
}

// ========== 申请请假 ==========
const applyVisible = ref(false)
const applying = ref(false)
const applyForm = reactive({
  reason: '',
  startTime: '',
  endTime: ''
})

function openApply() {
  applyForm.reason = ''
  applyForm.startTime = ''
  applyForm.endTime = ''
  applyVisible.value = true
}

async function submitApply() {
  if (!applyForm.reason.trim()) return ElMessage.warning('请输入请假事由')
  if (!applyForm.startTime) return ElMessage.warning('请选择开始时间')
  if (!applyForm.endTime) return ElMessage.warning('请选择结束时间')

  if (new Date(applyForm.endTime) <= new Date(applyForm.startTime)) {
    return ElMessage.warning('结束时间必须晚于开始时间')
  }

  applying.value = true
  try {
    await apiLeaveSubmit({
      reason: applyForm.reason,
      startTime: applyForm.startTime,
      endTime: applyForm.endTime
    })
    ElMessage.success('申请提交成功')
    applyVisible.value = false
    await load(1)
  } catch (e: any) {
    ElMessage.error(e?.message || '提交失败')
  } finally {
    applying.value = false
  }
}

// ========== 重新提交 ==========
const resubmitVisible = ref(false)
const resubmitting = ref(false)
const resubmitForm = reactive({
  leaveId: 0,
  reason: '',
  startTime: '',
  endTime: ''
})

function openResubmit(row: LeaveRecord) {
  resubmitForm.leaveId = row.leaveId
  resubmitForm.reason = row.leaveReason
  resubmitForm.startTime = row.startTime
  resubmitForm.endTime = row.endTime
  resubmitVisible.value = true
}

async function submitResubmit() {
  if (!resubmitForm.reason.trim()) return ElMessage.warning('请输入请假事由')
  if (!resubmitForm.startTime) return ElMessage.warning('请选择开始时间')
  if (!resubmitForm.endTime) return ElMessage.warning('请选择结束时间')

  resubmitting.value = true
  try {
    await apiLeaveResubmit({
      leaveId: resubmitForm.leaveId,
      reason: resubmitForm.reason,
      startTime: resubmitForm.startTime,
      endTime: resubmitForm.endTime
    })
    ElMessage.success('重新提交成功')
    resubmitVisible.value = false
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message || '提交失败')
  } finally {
    resubmitting.value = false
  }
}

// ========== 销假打卡 ==========
const reportBackVisible = ref(false)
const reportingBack = ref(false)
const reportBackForm = reactive({
  leaveId: 0,
  reportBackLocation: ''
})

function openReportBack(row: LeaveRecord) {
  reportBackForm.leaveId = row.leaveId
  reportBackForm.reportBackLocation = ''
  reportBackVisible.value = true
}

async function submitReportBack() {
  if (!reportBackForm.reportBackLocation.trim()) {
    return ElMessage.warning('请输入当前位置')
  }

  reportingBack.value = true
  try {
    await apiLeaveReportBack({
      leaveId: reportBackForm.leaveId,
      reportBackLocation: reportBackForm.reportBackLocation
    })
    ElMessage.success('销假打卡成功')
    reportBackVisible.value = false
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message || '打卡失败')
  } finally {
    reportingBack.value = false
  }
}

// ========== 审批 ==========
const approveVisible = ref(false)
const approving = ref(false)
const approveRow = ref<LeaveRecord | null>(null)
const approveForm = reactive({
  leaveId: 0,
  status: 1 as 1 | 2,
  approveOpinion: ''
})

function openApprove(row: LeaveRecord) {
  approveRow.value = row
  approveForm.leaveId = row.leaveId
  approveForm.status = 1
  approveForm.approveOpinion = ''
  approveVisible.value = true
}

async function submitApprove() {
  approving.value = true
  try {
    await apiLeaveApprove({
      leaveId: approveForm.leaveId,
      status: approveForm.status,
      approveOpinion: approveForm.approveOpinion || undefined
    })
    ElMessage.success(approveForm.status === 1 ? '已通过' : '已驳回')
    approveVisible.value = false
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message || '审批失败')
  } finally {
    approving.value = false
  }
}

// ========== 确认归档 ==========
const confirmingId = ref<number | null>(null)

async function confirmLeave(leaveId: number) {
  confirmingId.value = leaveId
  try {
    await apiLeaveConfirm({ leaveId })
    ElMessage.success('确认归档成功')
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message || '确认失败')
  } finally {
    confirmingId.value = null
  }
}

onMounted(() => {
  load(1)
})
</script>

<style scoped>
.card {
  border-radius: 16px;
}
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
.h1 {
  font-size: 20px;
  font-weight: 800;
}
.sub {
  color: var(--el-text-color-secondary);
  margin-top: 4px;
  font-size: 13px;
}
.actions {
  display: flex;
  gap: 10px;
}
.stats-row {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}
.stat-card {
  border-radius: 12px;
  text-align: center;
}
.filters {
  margin-bottom: 10px;
}
.pager {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}
</style>
