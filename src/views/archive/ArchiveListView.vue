<template>
  <el-card class="card" shadow="never">
    <template #header>
      <div class="head">
        <div>
          <div class="h1">民兵档案管理</div>
          <div class="sub">支持：查询、Excel 批量导入、团机关提交师部审核、师部审核（通过/驳回）、编辑、删除。</div>
        </div>
        <div class="actions">
          <el-button v-if="canImport" type="primary" @click="openImport">Excel 导入</el-button>
          <el-button v-if="canSubmit" type="success" :disabled="selectedIds.length === 0" @click="submitAudit">
            提交师部审核
          </el-button>
          <el-button v-if="canAudit" type="warning" :disabled="selectedIds.length !== 1" @click="openAudit">
            审核
          </el-button>
          <el-button @click="load(1)">刷新</el-button>
        </div>
      </div>
    </template>

    <el-form :inline="true" :model="query" class="filters">
      <el-form-item label="审核状态">
        <el-select v-model="query.auditStatus" clearable placeholder="全部" style="width: 160px">
          <el-option :value="0" label="草稿" />
          <el-option :value="1" label="待审核" />
          <el-option :value="2" label="已归档" />
          <el-option :value="3" label="驳回" />
        </el-select>
      </el-form-item>
      <el-form-item label="身份证">
        <el-input v-model="query.idCard" clearable placeholder="模糊查询" style="width: 220px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load(1)">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table
      :data="rows"
      border
      row-key="id"
      v-loading="loading"
      @selection-change="onSelection"
    >
      <el-table-column type="selection" width="44" />
      <el-table-column prop="name" label="姓名" width="110" />
      <el-table-column prop="idCard" label="身份证" min-width="180" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="politicStatus" label="政治面貌" width="120" />
      <el-table-column prop="joinTime" label="入队时间" width="160">
        <template #default="{ row }">
          {{ row.joinTime ? fmt(row.joinTime) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="auditStatus" label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="tagType(row.auditStatus)">{{ statusText(row.auditStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="auditFeedback" label="审核意见" min-width="160" show-overflow-tooltip />

      <el-table-column fixed="right" label="操作" width="170">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="onDelete(row.id)">删除</el-button>
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

  <!-- 编辑（后端 /update 只能编辑已存在记录，新增请用 Excel 导入） -->
  <el-dialog v-model="editVisible" title="编辑档案" width="620px">
    <el-form :model="editForm" label-width="90px">
      <el-form-item label="ID">
        <el-input :model-value="String(editForm.id || '')" disabled />
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="editForm.name" />
      </el-form-item>
      <el-form-item label="身份证">
        <el-input v-model="editForm.idCard" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="editForm.phone" />
      </el-form-item>
      <el-form-item label="地址">
        <el-input v-model="editForm.address" />
      </el-form-item>
      <el-form-item label="政治面貌">
        <el-input v-model="editForm.politicStatus" />
      </el-form-item>
      <el-form-item label="入队时间">
        <el-date-picker
          v-model="editForm.joinTime"
          type="date"
          placeholder="选择时间"
          value-format="YYYY-MM-DD 00:00"
          style="width: 100%"
        />
      </el-form-item>

      <el-alert
        type="info"
        show-icon
        :closable="false"
        title="提示：如果该记录处于“驳回”，后端会在保存后自动回到“草稿”，允许重新提交。"
      />
    </el-form>

    <template #footer>
      <el-button @click="editVisible=false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="saveEdit">保存</el-button>
    </template>
  </el-dialog>

  <!-- 审核 -->
  <el-dialog v-model="auditVisible" title="师部审核" width="540px">
    <el-form :model="auditForm" label-width="90px">
      <el-form-item label="档案ID">
        <el-input :model-value="String(auditForm.id || '')" disabled />
      </el-form-item>
      <el-form-item label="审核结果">
        <el-radio-group v-model="auditForm.auditStatus">
          <el-radio :value="2">通过</el-radio>
          <el-radio :value="3">驳回</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="auditForm.auditStatus===3" label="驳回原因" required>
        <el-input v-model="auditForm.auditFeedback" type="textarea" :rows="3" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="auditVisible=false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="doAudit">提交</el-button>
    </template>
  </el-dialog>

  <!-- Excel 导入 -->
  <el-drawer v-model="importVisible" title="Excel 导入（前端解析后调用 /api/militia/archive/import）" size="560px">
    <el-upload
      drag
      :auto-upload="false"
      accept=".xlsx,.xls"
      :on-change="onFileChange"
    >
      <el-icon><UploadFilled /></el-icon>
      <div class="el-upload__text">把 Excel 拖到这里，或 <em>点击上传</em></div>
      <template #tip>
        <div class="el-upload__tip">
          建议列名（支持中文表头）：姓名/身份证/手机号/地址/政治面貌/入队时间
          （或 name/idCard/phone/address/politicStatus/joinTime）
        </div>
      </template>
    </el-upload>

    <el-divider />

    <el-table v-if="importPreview.length" :data="importPreview.slice(0, 8)" border>
      <el-table-column prop="name" label="姓名" width="110" />
      <el-table-column prop="idCard" label="身份证" min-width="180" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="joinTime" label="入队时间" width="160" />
    </el-table>

    <el-alert
      v-if="importPreview.length"
      type="info"
      show-icon
      :closable="false"
      style="margin-top:12px"
      :title="`已解析 ${importPreview.length} 条，点击“提交导入”写入后端`"
    />

    <div style="margin-top: 14px; display:flex; gap:10px;">
      <el-button :disabled="!importPreview.length" type="primary" :loading="saving" @click="submitImport">提交导入</el-button>
      <el-button @click="clearImport">清空</el-button>
    </div>

    <el-divider />

    <el-descriptions v-if="importResult" title="导入结果" :column="1" border>
      <el-descriptions-item label="总条数">{{ importResult.total }}</el-descriptions-item>
      <el-descriptions-item label="成功">{{ importResult.success }}</el-descriptions-item>
      <el-descriptions-item label="失败">{{ importResult.fail }}</el-descriptions-item>
    </el-descriptions>

    <el-table v-if="importResult?.failList?.length" :data="importResult.failList" border style="margin-top: 10px;">
      <el-table-column prop="idCard" label="身份证" width="200" />
      <el-table-column prop="reason" label="失败原因" />
    </el-table>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'
import { useAuthStore } from '@/stores/auth'
import {
  apiArchiveAudit,
  apiArchiveDelete,
  apiArchiveImport,
  apiArchiveList,
  apiArchiveSubmit,
  apiArchiveUpdate,
  type ImportItem,
  type ImportResp,
  type MilitiaInfo
} from '@/api/archive'

const auth = useAuthStore()
auth.initFromStorage()

// 前端 UI 控制：按 userType 决定显示哪些动作按钮
const canImport = computed(() => [3, 4, 5].includes(auth.userType))  // 团/师/兵团
const canSubmit = computed(() => auth.userType === 3)               // 团机关提交
const canAudit  = computed(() => [4, 5].includes(auth.userType))     // 师/兵团审核

const loading = ref(false)
const saving = ref(false)

const rows = ref<MilitiaInfo[]>([])
const total = ref(0)
const selectedIds = ref<number[]>([])

const query = reactive({
  auditStatus: undefined as number | undefined,
  idCard: '',
  pageNum: 1,
  pageSize: 10
})

function fmt(v: string) {
  return dayjs(v).format('YYYY-MM-DD')
}

function statusText(v?: number | null) {
  return v === 0 ? '草稿' : v === 1 ? '待审核' : v === 2 ? '已归档' : v === 3 ? '驳回' : '-'
}
function tagType(v?: number | null) {
  return v === 2 ? 'success' : v === 3 ? 'danger' : v === 1 ? 'warning' : 'info'
}

function onSelection(r: MilitiaInfo[]) {
  selectedIds.value = r.map((x) => Number(x.id))
}

async function load(page?: number) {
  if (page) query.pageNum = page
  loading.value = true
  const r = await apiArchiveList({
    auditStatus: query.auditStatus,
    idCard: query.idCard || undefined,
    pageNum: query.pageNum,
    pageSize: query.pageSize
  })
  rows.value = r.data?.list || []
  total.value = Number(r.data?.total || 0)
  if(r.code==500){
    ElMessage.error(r.msg)
  }
  loading.value = false
}

function reset() {
  query.auditStatus = undefined
  query.idCard = ''
  load(1)
}

async function submitAudit() {
  try {
    await ElMessageBox.confirm(`确认提交 ${selectedIds.value.length} 条档案进入师部审核？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  saving.value = true
  let data = await apiArchiveSubmit(selectedIds.value)
  if (data.code == 200) {
    ElMessage.success('提交成功')
  }else if (data.code == 500){
    ElMessage.error(data.msg)
  }
  saving.value = false
}

/** 编辑 */
const editVisible = ref(false)
const editForm = reactive<Partial<MilitiaInfo>>({
  id: 0,
  name: '',
  idCard: '',
  phone: '',
  address: '',
  politicStatus: '',
  joinTime: ''
})

function openEdit(row: MilitiaInfo) {
  editForm.id = row.id
  editForm.name = row.name || ''
  editForm.idCard = row.idCard || ''
  editForm.phone = row.phone || ''
  editForm.address = row.address || ''
  editForm.politicStatus = row.politicStatus || ''
  editForm.joinTime = row.joinTime || ''
  editVisible.value = true
}

async function saveEdit() {
  if (!editForm.id) return
  saving.value = true
  try {
    let data = await apiArchiveUpdate({
      id: editForm.id,
      name: editForm.name,
      idCard: editForm.idCard,
      phone: editForm.phone,
      address: editForm.address,
      politicStatus: editForm.politicStatus,
      joinTime: editForm.joinTime || null
    })
    if (data.code == 500){
      ElMessage.error(data.msg)
      editVisible.value = false
      await load()
    }else if (data.code == 200){
      ElMessage.success('保存成功')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

/** 删除 */
async function onDelete(id: number) {
  try {
    await ElMessageBox.confirm('确认删除该档案？', '提示', { type: 'warning' })
  } catch {
    return
  }
  saving.value = true
  let data;
  data = await apiArchiveDelete(id)
  if(data.code === 200){
    ElMessage.success('删除成功')
    await load()
  }else if (data.code === 500){
    ElMessage.error(data.msg)
  }
  saving.value = false
}

/** 审核 */
const auditVisible = ref(false)
const auditForm = reactive({
  id: 0,
  auditStatus: 2 as 2 | 3,
  auditFeedback: ''
})

function openAudit() {
  if (selectedIds.value.length !== 1) return
  auditForm.id = selectedIds.value[0]
  auditForm.auditStatus = 2
  auditForm.auditFeedback = ''
  auditVisible.value = true
}

async function doAudit() {
  if (auditForm.auditStatus === 3 && !auditForm.auditFeedback.trim()) {
    ElMessage.warning('驳回原因必填')
    return
  }
  saving.value = true
  try {
    let data = await apiArchiveAudit({
      id: auditForm.id,
      auditStatus: auditForm.auditStatus,
      auditFeedback: auditForm.auditStatus === 3 ? auditForm.auditFeedback : undefined
    })
    if (data.code == 500){
      ElMessage.error(data.msg)
    }else if (data.code == 200){
      ElMessage.success('审核完成')
      auditVisible.value = false
      await load()
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '审核失败')
  } finally {
    saving.value = false
  }
}

/** Excel 导入 */
const importVisible = ref(false)
const importPreview = ref<ImportItem[]>([])
const importResult = ref<ImportResp | null>(null)

function openImport() {
  importPreview.value = []
  importResult.value = null
  importVisible.value = true
}

function normalizeHeader(h: string) {
  return (h || '').toString().trim().toLowerCase()
}

function toIsoDate(v: any): string | null {
  if (v == null || v === '') return null
  // xlsx 可能给 Date / number(ExcelDate) / string
  if (v instanceof Date) return dayjs(v).format('YYYY-MM-DDT00:00:00')
  if (typeof v === 'number') {
    // Excel date serial -> JS Date
    const date = XLSX.SSF.parse_date_code(v)
    if (date) {
      const d = new Date(Date.UTC(date.y, date.m - 1, date.d))
      return dayjs(d).format('YYYY-MM-DDT00:00:00')
    }
  }
  const s = String(v).trim()
  if (!s) return null
  // 支持 YYYY-MM-DD 或 YYYY/MM/DD
  const d = dayjs(s.replace(/\//g, '-'))
  return d.isValid() ? d.format('YYYY-MM-DDT00:00:00') : null
}

function onFileChange(file: any) {
  const raw = file?.raw as File | undefined
  if (!raw) return
  const reader = new FileReader()
  reader.onload = (e) => {
    const data = e.target?.result
    if (!data) return
    const wb = XLSX.read(data, { type: 'array' })
    const sheetName = wb.SheetNames[0]
    const ws = wb.Sheets[sheetName]
    const json = XLSX.utils.sheet_to_json<Record<string, any>>(ws, { defval: '' })

    // 支持中英文表头
    const mapped: ImportItem[] = json.map((row) => {
      const out: any = {}
      for (const [k, v] of Object.entries(row)) {
        const key = normalizeHeader(k)
        if (['name', '姓名'].includes(k) || key === 'name' || key === '姓名') out.name = String(v || '').trim()
        if (['idcard', '身份证', '身份证号'].includes(key) || k === '身份证' || k === '身份证号') out.idCard = String(v || '').trim()
        if (['phone', '手机号', '电话'].includes(key) || k === '手机号' || k === '电话') out.phone = String(v || '').trim()
        if (['address', '地址'].includes(key) || k === '地址') out.address = String(v || '').trim()
        if (['politicstatus', '政治面貌'].includes(key) || k === '政治面貌') out.politicStatus = String(v || '').trim()
        if (['jointime', '入队时间', '入伍时间', '加入时间'].includes(key) || ['入队时间','入伍时间','加入时间'].includes(k)) out.joinTime = toIsoDate(v)
      }
      // 兜底：常用英文列
      out.idCard = out.idCard || String(row['idCard'] || row['IDCard'] || '').trim()
      out.name = out.name || String(row['name'] || '').trim()
      out.phone = out.phone || String(row['phone'] || '').trim()
      out.address = out.address || String(row['address'] || '').trim()
      out.politicStatus = out.politicStatus || String(row['politicStatus'] || '').trim()
      out.joinTime = out.joinTime || toIsoDate(row['joinTime'])
      return out as ImportItem
    }).filter((x) => x.idCard && String(x.idCard).trim())

    importPreview.value = mapped
  }
  reader.readAsArrayBuffer(raw)
}

async function submitImport() {
  if (!importPreview.value.length) return
  saving.value = true
  try {
    const r = await apiArchiveImport(importPreview.value)
    importResult.value = r.data
    ElMessage.success(r.msg || '导入完成')
    await load(1)
  } catch (e: any) {
    ElMessage.error(e?.message || '导入失败')
  } finally {
    saving.value = false
  }
}

function clearImport() {
  importPreview.value = []
  importResult.value = null
}

onMounted(() => load(1))
</script>

<style scoped>
.card { border-radius: 16px; }
.head {
  display:flex;
  align-items:center;
  justify-content:space-between;
  gap: 16px;
}
.h1 { font-size: 20px; font-weight: 800; }
.sub { color: var(--el-text-color-secondary); margin-top: 4px; font-size: 13px; }
.actions { display:flex; gap: 10px; flex-wrap: wrap; }
.filters { margin-bottom: 10px; }
.pager { margin-top: 14px; display:flex; justify-content:flex-end; }
</style>
