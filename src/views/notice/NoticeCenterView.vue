<template>
  <el-card class="card" shadow="never">
    <template #header>
      <div class="head">
        <div>
          <div class="h1">通知教育</div>
          <div class="sub">
            团机关：仅查看收到的通知并标记已读；
            师机关：可发布通知，并查看“我发布的通知”和阅读反馈。
          </div>
        </div>
        <div class="actions">
          <el-button v-if="canPublish" type="primary" @click="openPublish">发布通知</el-button>
          <el-button @click="load(1)">刷新</el-button>
        </div>
      </div>
    </template>

    <!-- ✅ Tab：只有师机关才能看到“我发布的” -->
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane name="my" label="我收到的" />
      <el-tab-pane v-if="canViewSent" name="sent" label="我发布的" />
    </el-tabs>

    <!-- ✅ 筛选：根据 Tab 显示不同筛选项 -->
    <el-form :inline="true" :model="query" class="filters">
      <el-form-item label="类型">
        <el-select v-model="query.noticeType" clearable placeholder="全部" style="width: 160px">
          <el-option :value="1" label="通知公告" />
          <el-option :value="2" label="教育学习" />
        </el-select>
      </el-form-item>

      <!-- 我收到的：阅读状态 -->
      <el-form-item v-if="activeTab === 'my'" label="阅读状态">
        <el-select v-model="query.readStatus" clearable placeholder="全部" style="width: 160px">
          <el-option :value="0" label="未读" />
          <el-option :value="1" label="已读" />
        </el-select>
      </el-form-item>

      <!-- 我发布的：完成状态（status） -->
      <el-form-item v-if="activeTab === 'sent'" label="完成状态">
        <el-select v-model="query.status" clearable placeholder="全部" style="width: 160px">
          <el-option :value="0" label="未完成" />
          <el-option :value="1" label="已完成" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="load(1)">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- ✅ 表格：两种 Tab 显示不同列 -->
    <el-table :data="rows" border row-key="noticeId" v-loading="loading">
      <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
      <el-table-column prop="noticeType" label="类型" width="120">
        <template #default="{ row }">
          <el-tag effect="plain">{{ typeText(row.noticeType) }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="发布时间" width="180">
        <template #default="{ row }">
          {{ fmt(row.createTime) }}
        </template>
      </el-table-column>

      <!-- 我发布的：完成状态 -->
      <el-table-column v-if="activeTab === 'sent'" prop="status" label="完成状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '已完成' : '未完成' }}
          </el-tag>
        </template>
      </el-table-column>

      <!-- 我收到的：阅读状态 + 阅读时间 -->
      <el-table-column v-if="activeTab === 'my'" prop="isRead" label="阅读" width="110">
        <template #default="{ row }">
          <el-tag :type="row.isRead ? 'success' : 'warning'">{{ row.isRead ? '已读' : '未读' }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column v-if="activeTab === 'my'" prop="readTime" label="阅读时间" width="180">
        <template #default="{ row }">
          {{ row.readTime ? fmt(row.readTime) : '-' }}
        </template>
      </el-table-column>

      <el-table-column fixed="right" label="操作" width="240">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>

          <!-- 我收到的：标记已读 -->
          <el-button
              v-if="activeTab === 'my' && !row.isRead"
              link
              type="success"
              :loading="markingId === row.noticeId"
              @click="markRead(row.noticeId)"
          >
            标记已读
          </el-button>

          <!-- 我发布的：阅读反馈（只有师机关可见） -->
          <el-button
              v-if="activeTab === 'sent' && canFeedback"
              link
              type="warning"
              @click="openFeedback(row.noticeId)"
          >
            阅读反馈
          </el-button>
        </template>
      </el-table-column>
    </el-table>

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

  <!-- 详情（后端暂无按 noticeId 获取 content 的接口，这里仅展示标题/基本信息） -->
  <el-dialog v-model="detailVisible" title="通知详情" width="560px">
    <el-descriptions v-if="detailRow" :column="1" border>
      <el-descriptions-item label="标题">{{ detailRow.title }}</el-descriptions-item>
      <el-descriptions-item label="类型">{{ typeText(detailRow.noticeType) }}</el-descriptions-item>
      <el-descriptions-item label="发布时间">{{ fmt(detailRow.createTime) }}</el-descriptions-item>

      <el-descriptions-item v-if="activeTab === 'my'" label="阅读状态">
        {{ detailRow.isRead ? '已读' : '未读' }}
      </el-descriptions-item>

      <el-descriptions-item v-if="activeTab === 'sent'" label="完成状态">
        {{ detailRow.status === 1 ? '已完成' : '未完成' }}
      </el-descriptions-item>
    </el-descriptions>

    <el-alert
        style="margin-top: 12px"
        type="info"
        :closable="false"
        show-icon
        title="提示：后端当前未提供“按 noticeId 查询 content”的接口，所以前端暂不展示正文。你后续补接口后，把这里换成请求即可。"
    />
    <template #footer>
      <el-button @click="detailVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 发布（仅师机关可见按钮触发） -->
  <el-dialog v-model="publishVisible" title="发布通知" width="640px">
    <el-form :model="publishForm" label-width="90px">
      <el-form-item label="标题" required>
        <el-input v-model.trim="publishForm.title" placeholder="请输入标题" />
      </el-form-item>
      <el-form-item label="类型" required>
        <el-radio-group v-model="publishForm.noticeType">
          <el-radio :value="1">通知公告</el-radio>
          <el-radio :value="2">教育学习</el-radio>
          <el-radio :value="3">团场内部通知</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="内容" required>
        <el-input
            v-model="publishForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入内容（可先用纯文本，后续可换富文本编辑器）"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="publishVisible=false">取消</el-button>
      <el-button type="primary" :loading="publishing" @click="submitPublish">发布</el-button>
    </template>
  </el-dialog>

  <!-- 阅读反馈（仅师机关在“我发布的”里点开） -->
  <el-dialog v-model="feedbackVisible" title="阅读反馈" width="720px">
    <div class="fb-top" v-if="feedback">
      <el-statistic title="总数" :value="feedback.total" />
      <el-statistic title="已读" :value="feedback.readCount" />
      <el-statistic title="未读" :value="feedback.unreadCount" />
    </div>

    <el-form :inline="true" :model="fbQuery" class="filters" style="margin-top: 10px">
      <el-form-item label="筛选">
        <el-select v-model="fbQuery.readStatus" clearable placeholder="全部" style="width: 160px">
          <el-option :value="0" label="未读" />
          <el-option :value="1" label="已读" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="fbLoading" @click="loadFeedback(1)">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="feedback?.deptList || []" border v-loading="fbLoading" row-key="userId">
      <el-table-column prop="userId" label="用户ID" width="110" />
      <el-table-column prop="username" label="用户名/部门名" min-width="160" />
      <el-table-column prop="deptId" label="部门ID" width="110" />
      <el-table-column prop="isRead" label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="row.isRead ? 'success' : 'warning'">{{ row.isRead ? '已读' : '未读' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="readTime" label="阅读时间" width="180">
        <template #default="{ row }">
          {{ row.readTime ? fmt(row.readTime) : '-' }}
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
          background
          layout="total, prev, pager, next, sizes"
          :total="fbTotal"
          v-model:current-page="fbQuery.pageNum"
          v-model:page-size="fbQuery.pageSize"
          @current-change="loadFeedback"
          @size-change="loadFeedback(1)"
      />
    </div>

    <template #footer>
      <el-button @click="feedbackVisible=false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import {
  apiNoticeFeedback,
  apiNoticeRead,
  apiNoticeMyList,
  apiNoticePublish,
  apiNoticeSentList,
  type NoticeFeedback,
  type NoticeMyListItem
} from '@/api/notice'

const auth = useAuthStore()
auth.initFromStorage()

// ✅ 只有师机关 userType=4
const isTeacher = computed(() => auth.userType === 4)
const canPublish = isTeacher
const canFeedback = isTeacher
const canViewSent = isTeacher

// ✅ Tab：默认我收到的
const activeTab = ref<'my' | 'sent'>('my')

// ✅ 防止篡改 tab 绕过
watch(
    () => auth.userType,
    () => {
      if (!canViewSent.value && activeTab.value === 'sent') activeTab.value = 'my'
    },
    { immediate: true }
)

function onTabChange() {
  if (!canViewSent.value && activeTab.value === 'sent') {
    activeTab.value = 'my'
    ElMessage.warning('无权限查看“我发布的通知”')
    return
  }
  reset()
}

const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)

const query = reactive({
  noticeType: undefined as number | undefined,
  // my 专用
  readStatus: undefined as number | undefined,
  // sent 专用
  status: undefined as number | undefined,
  pageNum: 1,
  pageSize: 10
})

function fmt(v?: string) {
  if (!v) return '-'
  return dayjs(v).format('YYYY-MM-DD HH:mm')
}
function typeText(t: number) {
  return t === 1 ? '通知公告' : t === 2 ? '教育学习' : t === 3 ? '团场内部' : `类型${t}`
}

async function load(page?: number) {
  if (page) query.pageNum = page
  loading.value = true
  try {
    if (activeTab.value === 'sent') {
      // ✅ 兜底：团机关强制回 my
      if (!canViewSent.value) {
        activeTab.value = 'my'
        ElMessage.warning('无权限查看“我发布的通知”')
        return
      }

      const r = await apiNoticeSentList({
        noticeType: query.noticeType,
        readStatus: query.status,
        pageNum: query.pageNum,
        pageSize: query.pageSize
      })

      const pageData = r.data.data
      // sent 接口返回 deptNotices IPage：records 里字段一般是 noticeId/title/type/status/sendTime/senderId...
      rows.value = (pageData?.records || []).map((n: any) => ({
        noticeId: n.noticeId,
        title: n.title,
        noticeType: n.type,
        status: n.status,
        createTime: n.sendTime
      }))
      total.value = Number(pageData?.total || 0)
      return
    }

    // ✅ 我收到的
    const r = await apiNoticeMyList({
      noticeType: query.noticeType,
      readStatus: query.readStatus,
      pageNum: query.pageNum,
      pageSize: query.pageSize
    })
    const pageData = r.data.data
    rows.value = pageData?.records || []
    total.value = Number(pageData?.total || 0)
  } catch (e: any) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function reset() {
  query.noticeType = undefined
  query.readStatus = undefined
  query.status = undefined
  query.pageNum = 1
  load(1)
}

const markingId = ref<number | null>(null)
async function markRead(id: number) {
  markingId.value = id
  try {
    await apiNoticeRead(id)
    ElMessage.success('已标记')
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  } finally {
    markingId.value = null
  }
}

/** 详情 */
const detailVisible = ref(false)
const detailRow = ref<any | null>(null)
function openDetail(row: NoticeMyListItem | any) {
  detailRow.value = row
  detailVisible.value = true
}

/** 发布 */
const publishVisible = ref(false)
const publishing = ref(false)
const publishForm = reactive({
  title: '',
  content: '',
  noticeType: 1
})

function openPublish() {
  if (!canPublish.value) return ElMessage.warning('无权限发布通知')
  publishForm.title = ''
  publishForm.content = ''
  publishForm.noticeType = 1
  publishVisible.value = true
}

async function submitPublish() {
  if (!canPublish.value) return ElMessage.warning('无权限发布通知')
  if (!publishForm.title.trim()) return ElMessage.warning('标题必填')
  if (!publishForm.content.trim()) return ElMessage.warning('内容必填')

  publishing.value = true
  try {
    const r = await apiNoticePublish({
      title: publishForm.title,
      content: publishForm.content,
      noticeType: publishForm.noticeType
    })

    // 后端 publish 可能返回 -1（无下级单位）
    if (r.data.data === -1) {
      ElMessage.error('发布失败：当前单位无下级单位（未生成接收记录）')
      return
    }

    ElMessage.success('发布成功')
    publishVisible.value = false

    // ✅ 发布后切到“我发布的”并刷新（只有师机关才存在 sent tab）
    activeTab.value = 'sent'
    await load(1)
  } catch (e: any) {
    ElMessage.error(e?.message || '发布失败')
  } finally {
    publishing.value = false
  }
}

/** 阅读反馈 */
const feedbackVisible = ref(false)
const fbLoading = ref(false)
const feedback = ref<NoticeFeedback | null>(null)
const fbTotal = ref(0)
const fbQuery = reactive({
  noticeId: 0,
  readStatus: undefined as number | undefined,
  pageNum: 1,
  pageSize: 10
})

function openFeedback(noticeId: number) {
  if (!canFeedback.value || activeTab.value !== 'sent') return ElMessage.warning('无权限查看反馈')
  fbQuery.noticeId = noticeId
  fbQuery.readStatus = undefined
  fbQuery.pageNum = 1
  fbQuery.pageSize = 10
  feedbackVisible.value = true
  loadFeedback(1)
}

async function loadFeedback(page?: number) {
  if (!fbQuery.noticeId) return
  if (page) fbQuery.pageNum = page
  fbLoading.value = true
  try {
    const r = await apiNoticeFeedback(fbQuery.noticeId, {
      readStatus: fbQuery.readStatus,
      pageNum: fbQuery.pageNum,
      pageSize: fbQuery.pageSize
    })
    feedback.value = r.data.data
    fbTotal.value = Number(r.data.data?.total || 0)
  } catch (e: any) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    fbLoading.value = false
  }
}

onMounted(() => load(1))
</script>

<style scoped>
.card { border-radius: 16px; }
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
.h1 { font-size: 20px; font-weight: 800; }
.sub { color: var(--el-text-color-secondary); margin-top: 4px; font-size: 13px; }
.actions { display: flex; gap: 10px; }
.filters { margin-bottom: 10px; }
.pager { margin-top: 14px; display:flex; justify-content:flex-end; }
.fb-top {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
</style>
