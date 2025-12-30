<template>
  <el-card class="card" shadow="never">
    <template #header>
      <div class="head">
        <div>
          <div class="h1">报表服务</div>
          <div class="sub">
            {{ activeTab === 'pending'
              ? '查看待审批的下级报表；可进行审批操作。'
              : '查看我提交的报表；可新建报表。' }}
          </div>
        </div>

        <div class="actions">
          <!-- 切换视图 -->
          <el-radio-group v-model="activeTab" @change="handleTabChange">
            <el-radio-button value="my">我的报表</el-radio-button>
            <el-radio-button v-if="canApprove" value="pending">待审批</el-radio-button>
          </el-radio-group>
          <el-button type="primary" @click="openSubmit">提交报表</el-button>
          <el-button @click="load(1)">刷新</el-button>
        </div>
      </div>
    </template>

    <!-- 筛选条件 -->
    <el-form :inline="true" :model="query" class="filters">
      <el-form-item label="类型">
        <el-select v-model="query.reportType" clearable placeholder="全部" style="width: 140px">
          <el-option :value="1" label="月计划" />
          <el-option :value="2" label="月总结" />
          <el-option :value="3" label="专项报告" />
        </el-select>
      </el-form-item>

      <el-form-item label="月份">
        <el-date-picker
            v-model="query.reportMonth"
            type="month"
            placeholder="选择月份"
            format="YYYY-MM"
            value-format="YYYY-MM"
            style="width: 140px"
            clearable
        />
      </el-form-item>

      <el-form-item v-if="activeTab === 'my'" label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" style="width: 120px">
          <el-option :value="0" label="待审批" />
          <el-option :value="1" label="已通过" />
          <el-option :value="2" label="已驳回" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="load(1)">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 数据表格 -->
    <el-table :data="rows" border row-key="id" v-loading="loading">
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />

      <el-table-column prop="reportType" label="类型" width="110">
        <template #default="{ row }">
          <el-tag effect="plain">{{ ReportTypeMap[row.reportType] || '未知' }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="reportMonth" label="报表月份" width="120" />

      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">
            {{ ReportStatusMap[row.status] || '未知' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="提交时间" width="170">
        <template #default="{ row }">
          {{ fmt(row.createTime) }}
        </template>
      </el-table-column>

      <el-table-column prop="approveTime" label="审批时间" width="170">
        <template #default="{ row }">
          {{ row.approveTime ? fmt(row.approveTime) : '-' }}
        </template>
      </el-table-column>

      <el-table-column fixed="right" label="操作" width="200">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>

          <!-- 待审批视图：显示审批按钮 -->
          <template v-if="activeTab === 'pending' && row.status === 0">
            <el-button link type="success" @click="handleApprove(row.reportId, 1)">通过</el-button>
            <el-button link type="danger" @click="handleApprove(row.reportId, 2)">驳回</el-button>
          </template>
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
  <el-dialog v-model="detailVisible" title="报表详情" width="720px">
    <el-descriptions v-if="detailRow" :column="1" border>
      <el-descriptions-item label="标题">{{ detailRow.title }}</el-descriptions-item>
      <el-descriptions-item label="类型">{{ ReportTypeMap[detailRow.reportType] }}</el-descriptions-item>
      <el-descriptions-item label="报表月份">{{ detailRow.reportMonth }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="statusTagType(detailRow.status)">
          {{ ReportStatusMap[detailRow.status] }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="提交时间">{{ fmt(detailRow.createTime) }}</el-descriptions-item>
      <el-descriptions-item label="审批时间">
        {{ detailRow.approveTime ? fmt(detailRow.approveTime) : '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="附件">
        <template v-if="detailRow.filePath">
          <el-link type="primary" :href="detailRow.filePath" target="_blank">查看附件</el-link>
        </template>
        <template v-else>无</template>
      </el-descriptions-item>
      <el-descriptions-item label="内容">
        <div class="content">{{ detailRow.content || '（无内容）' }}</div>
      </el-descriptions-item>
    </el-descriptions>

    <template #footer>
      <!-- 详情弹窗中也可审批 -->
      <template v-if="activeTab === 'pending' && detailRow?.status === 0">
        <el-button type="success" @click="handleApprove(detailRow!.reportId, 1); detailVisible = false">
          通过
        </el-button>
        <el-button type="danger" @click="handleApprove(detailRow!.reportId, 2); detailVisible = false">
          驳回
        </el-button>
      </template>
      <el-button @click="detailVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 提交报表弹窗 -->
  <el-dialog v-model="submitVisible" title="提交报表" width="640px">
    <el-form :model="submitForm" label-width="90px" :rules="submitRules" ref="submitFormRef">
      <el-form-item label="标题" prop="title">
        <el-input v-model.trim="submitForm.title" placeholder="请输入报表标题" maxlength="100" />
      </el-form-item>

      <el-form-item label="类型" prop="reportType">
        <el-radio-group v-model="submitForm.reportType">
          <el-radio :value="1">月计划</el-radio>
          <el-radio :value="2">月总结</el-radio>
          <el-radio :value="3">专项报告</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="报表月份" prop="reportMonth">
        <el-date-picker
            v-model="submitForm.reportMonth"
            type="month"
            placeholder="选择月份"
            format="YYYY-MM"
            value-format="YYYY-MM"
            style="width: 200px"
        />
      </el-form-item>

      <el-form-item label="附件路径">
        <el-input v-model.trim="submitForm.filePath" placeholder="可选，填写附件URL或路径" />
        <div class="form-tip">提示：如需上传文件，请先通过文件上传接口获取路径</div>
      </el-form-item>

      <el-form-item label="内容" prop="content">
        <el-input
            v-model="submitForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入报表内容"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="submitVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="doSubmit">提交</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import {
  apiReportList,
  apiReportSubmit,
  apiReportApprove,
  apiReportDetail,
  ReportStatus,
  ReportStatusMap,
  ReportTypeMap,
  type WorkReport,
  type ReportQueryParams
} from '@/api/report'

const auth = useAuthStore()
auth.initFromStorage()

// 权限：userType 3(团机关) 及以上可以审批下级
const canApprove = computed(() => [3, 4, 5].includes(auth.userType))

// 当前视图：my=我的报表，pending=待审批
const activeTab = ref<'my' | 'pending'>('my')

const loading = ref(false)
const rows = ref<WorkReport[]>([])
const total = ref(0)

const query = reactive<ReportQueryParams>({
  reportType: undefined,
  reportMonth: undefined,
  status: undefined,
  pageNum: 1,
  pageSize: 10
})

// 格式化时间
function fmt(v?: string) {
  if (!v) return '-'
  return dayjs(v).format('YYYY-MM-DD HH:mm')
}

// 状态标签颜色
function statusTagType(status: number) {
  switch (status) {
    case ReportStatus.APPROVED:
      return 'success'
    case ReportStatus.REJECTED:
      return 'danger'
    default:
      return 'warning'
  }
}

// 切换Tab
function handleTabChange() {
  query.status = activeTab.value === 'pending' ? 0 : undefined
  load(1)
}

// 加载数据
async function load(page?: number) {
  if (page) query.pageNum = page
  loading.value = true
  try {
    // 待审批视图强制 status=0
    const params: ReportQueryParams = {
      ...query,
      status: activeTab.value === 'pending' ? 0 : query.status
    }
    const res = await apiReportList(params)
    rows.value = res.data?.list || []
    total.value = Number(res.data?.total || 0)
  } catch (e: any) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 重置筛选
function reset() {
  query.reportType = undefined
  query.reportMonth = undefined
  query.status = undefined
  load(1)
}

// ==================== 详情 ====================
const detailVisible = ref(false)
const detailRow = ref<WorkReport | null>(null)

async function openDetail(row: WorkReport) {
  try {
    const res = await apiReportDetail(row.reportId)
    detailRow.value = res.data || row
    detailVisible.value = true
  } catch (e: any) {
    ElMessage.error(e?.message || '获取详情失败')
  }
}

// ==================== 审批 ====================
async function handleApprove(reportId: number, status: number) {
  const actionText = status === 1 ? '通过' : '驳回'
  try {
    await ElMessageBox.confirm(`确认${actionText}该报表吗？`, '审批确认', {
      type: 'warning',
      confirmButtonText: actionText,
      cancelButtonText: '取消'
    })

    await apiReportApprove({ reportId, status })
    ElMessage.success(`${actionText}成功`)
    await load()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e?.message || '操作失败')
    }
  }
}

// ==================== 提交报表 ====================
const submitVisible = ref(false)
const submitting = ref(false)
const submitFormRef = ref<FormInstance>()

const submitForm = reactive({
  title: '',
  content: '',
  filePath: '',
  reportType: 1,
  reportMonth: ''
})

const submitRules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  reportType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  reportMonth: [{ required: true, message: '请选择月份', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

function openSubmit() {
  submitForm.title = ''
  submitForm.content = ''
  submitForm.filePath = ''
  submitForm.reportType = 1
  submitForm.reportMonth = dayjs().format('YYYY-MM')
  submitVisible.value = true
}

async function doSubmit() {
  if (!submitFormRef.value) return
  try {
    await submitFormRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    await apiReportSubmit({
      title: submitForm.title,
      content: submitForm.content,
      filePath: submitForm.filePath || undefined,
      reportType: submitForm.reportType,
      reportMonth: submitForm.reportMonth
    })
    ElMessage.success('提交成功')
    submitVisible.value = false
    await load(1)
  } catch (e: any) {
    ElMessage.error(e?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

// 初始化
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
  flex-wrap: wrap;
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
  align-items: center;
  flex-wrap: wrap;
}
.filters {
  margin-bottom: 10px;
}
.pager {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}
.content {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.75;
  padding: 6px 0;
}
.form-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}
</style>
