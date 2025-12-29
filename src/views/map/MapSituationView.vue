<template>
  <el-card class="card" shadow="never">
    <!-- 头部 -->
    <template #header>
      <div class="head">
        <div>
          <div class="h1">地图态势</div>
          <div class="sub">{{ subDescription }}</div>
        </div>
        <div class="actions">
          <el-button
              v-if="canEditRegion"
              type="primary"
              :loading="saving"
              @click="saveRegion"
          >
            保存区域
          </el-button>
          <el-button
              v-if="canEditRegion"
              type="warning"
              plain
              @click="clearDrawing"
          >
            清除绘制
          </el-button>
          <el-button @click="refreshData">刷新</el-button>
        </div>
      </div>
    </template>

    <!-- 筛选条件 -->
    <el-form :inline="true" class="filters">
      <!-- 兵团级：选择查看的层级 -->
      <template v-if="isCorps">
        <el-form-item label="查看层级">
          <el-select
              v-model="selectedLayer"
              placeholder="选择层级"
              style="width: 160px"
              @change="handleLayerChange"
          >
            <el-option :value="4" label="师级单位" />
            <el-option :value="3" label="团级单位" />
            <el-option :value="2" label="连级单位" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="layerLoading" @click="loadLayerData">
            加载态势
          </el-button>
        </el-form-item>
      </template>

      <!-- 团/师级：查看下属单位 -->
      <template v-if="canViewSubordinates">
        <el-form-item>
          <el-button
              type="primary"
              :loading="subLoading"
              @click="loadSubordinates"
          >
            查看下属单位
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button @click="clearSubordinates">清除下属图层</el-button>
        </el-form-item>
      </template>

      <!-- 连级/团级/师级：加载本级区域 -->
      <template v-if="canEditRegion">
        <el-form-item>
          <el-button @click="loadMyRegion">加载本级区域</el-button>
        </el-form-item>
      </template>
    </el-form>

    <!-- 提示信息 -->
    <el-alert
        v-if="canEditRegion"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 12px"
    >
      <template #title>
        使用左侧工具栏绘制管辖区域（支持多边形、矩形），绘制完成后点击「保存区域」提交。
      </template>
    </el-alert>

    <!-- 主体区域：左侧列表 + 右侧地图 -->
    <div class="main-content">
      <!-- 左侧列表（团/师级显示下属单位） -->
      <div v-if="showSubordinateList" class="list-panel">
        <div class="list-header">
          <span class="list-title">下属单位列表</span>
          <el-tag type="info" size="small">共 {{ subordinateList.length }} 个</el-tag>
        </div>
        <el-table
            :data="subordinateList"
            border
            size="small"
            height="calc(100% - 40px)"
            highlight-current-row
            @row-click="focusOnDept"
        >
          <el-table-column prop="deptName" label="单位名称" min-width="140" show-overflow-tooltip />
          <el-table-column prop="deptType" label="级别" width="70" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="getDeptTagType(row.deptType)">
                {{ getDeptTypeName(row.deptType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="区域" width="70" align="center">
            <template #default="{ row }">
              <el-tag
                  size="small"
                  :type="row.regionShape ? 'success' : 'info'"
              >
                {{ row.regionShape ? '已划' : '未划' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ row }">
              <el-button
                  link
                  type="primary"
                  size="small"
                  :disabled="!row.regionShape && !row.regionCenter"
                  @click.stop="focusOnDept(row)"
              >
                定位
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 兵团级列表面板 -->
      <div v-if="isCorps && layerList.length > 0" class="list-panel">
        <div class="list-header">
          <span class="list-title">{{ getLayerName(selectedLayer) }}单位列表</span>
          <el-tag type="info" size="small">共 {{ layerList.length }} 个</el-tag>
        </div>
        <el-table
            :data="layerList"
            border
            size="small"
            height="calc(100% - 40px)"
            highlight-current-row
            @row-click="focusOnDept"
        >
          <el-table-column prop="deptName" label="单位名称" min-width="140" show-overflow-tooltip />
          <el-table-column label="区域" width="70" align="center">
            <template #default="{ row }">
              <el-tag
                  size="small"
                  :type="row.regionShape ? 'success' : 'info'"
              >
                {{ row.regionShape ? '已划' : '未划' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ row }">
              <el-button
                  link
                  type="primary"
                  size="small"
                  :disabled="!row.regionShape && !row.regionCenter"
                  @click.stop="focusOnDept(row)"
              >
                定位
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 右侧地图 -->
      <div class="map-panel" :class="{ 'full-width': isFullWidthMap }">
        <div ref="mapRef" class="map-container"></div>

        <!-- 地图图例 -->
        <div class="map-legend">
          <div class="legend-title">图例</div>
          <div class="legend-item">
            <span class="legend-color" style="background: #e74c3c;"></span>
            <span>兵团</span>
          </div>
          <div class="legend-item">
            <span class="legend-color" style="background: #3498db;"></span>
            <span>师</span>
          </div>
          <div class="legend-item">
            <span class="legend-color" style="background: #2ecc71;"></span>
            <span>团</span>
          </div>
          <div class="legend-item">
            <span class="legend-color" style="background: #f39c12;"></span>
            <span>连</span>
          </div>
          <div class="legend-item">
            <span class="legend-color" style="background: #9b59b6;"></span>
            <span>民兵</span>
          </div>
        </div>

        <!-- 加载遮罩 -->
        <div v-if="mapLoading" class="map-loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载中...</span>
        </div>
      </div>
    </div>

    <!-- 统计信息 -->
    <div class="stats-bar" v-if="hasStats">
      <el-statistic title="已划定区域" :value="statsDrawn" />
      <el-statistic title="未划定区域" :value="statsUndrawn" />
      <el-statistic title="单位总数" :value="statsTotal" />
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import '@geoman-io/leaflet-geoman-free'
import '@geoman-io/leaflet-geoman-free/dist/leaflet-geoman.css'
import {
  apiGetRegion,
  apiUpdateRegion,
  apiGetSubordinates,
  apiGetLayerData,
  type DeptGisDTO,
  type UpdateRegionPayload,
  type GeoJSONGeometry,
  type GeoJSONPoint
} from '@/api/gis'

// ======================== 状态定义 ========================

const auth = useAuthStore()
auth.initFromStorage()

const mapRef = ref<HTMLElement>()
let map: L.Map | null = null
let drawnItems: L.FeatureGroup   // 用户绑定图层（可编辑）
let subordinateLayer: L.FeatureGroup  // 下属单位图层
let layerDataGroup: L.FeatureGroup    // 兵团级态势图层

const saving = ref(false)
const subLoading = ref(false)
const layerLoading = ref(false)
const mapLoading = ref(false)

const selectedLayer = ref<number>(20) // 兵团级选择的层级
const subordinateList = ref<DeptGisDTO[]>([])
const layerList = ref<DeptGisDTO[]>([])
const myRegionData = ref<DeptGisDTO | null>(null)

// ======================== 权限计算 ========================

const userType = computed(() => auth.userType)

// 用户类型判断
const isCompany = computed(() => userType.value === 2)   // 连级
const isRegiment = computed(() => userType.value === 3)  // 团级
const isDivision = computed(() => userType.value === 4)  // 师级
const isCorps = computed(() => userType.value === 5)     // 兵团级

// 功能权限
const canEditRegion = computed(() => [2, 3, 4].includes(userType.value)) // 连/团/师可编辑区域
const canViewSubordinates = computed(() => [3, 4].includes(userType.value)) // 团/师可查看下属

// 是否显示下属列表
const showSubordinateList = computed(() =>
    canViewSubordinates.value && subordinateList.value.length > 0
)

// 地图是否全宽
const isFullWidthMap = computed(() => {
  if (isCorps.value && layerList.value.length === 0) return true
  if (isCompany.value) return true
  if ((isRegiment.value || isDivision.value) && subordinateList.value.length === 0) return true
  return false
})

// 描述文字
const subDescription = computed(() => {
  if (isCorps.value) {
    return '兵团级视角：可按层级查看全疆所有单位的管辖区域分布。'
  }
  if (isDivision.value) {
    return '师级视角：可绘制本级管辖区域，并查看下属团级单位的区域分布。'
  }
  if (isRegiment.value) {
    return '团级视角：可绘制本级管辖区域，并查看下属连级单位的区域分布。'
  }
  if (isCompany.value) {
    return '连级视角：可绘制本单位的管辖区域范围。'
  }
  return '地图态势查看与区域管理'
})

// ======================== 统计信息 ========================

const hasStats = computed(() => {
  return (isCorps.value && layerList.value.length > 0) ||
      (canViewSubordinates.value && subordinateList.value.length > 0)
})

const statsTotal = computed(() => {
  if (isCorps.value) return layerList.value.length
  return subordinateList.value.length
})

const statsDrawn = computed(() => {
  const list = isCorps.value ? layerList.value : subordinateList.value
  return list.filter(d => d.regionShape).length
})

const statsUndrawn = computed(() => statsTotal.value - statsDrawn.value)

// ======================== 工具函数 ========================

function getDeptTypeName(deptType: number): string {
  const map: Record<number, string> = {
    5: '兵团',
    4: '师',
    3: '团',
    2: '连',
    1: '民兵'
  }
  return map[deptType] || '未知'
}

function getDeptTagType(deptType: number): string {
  const map: Record<number, string> = {
    5: 'danger',
    4: 'primary',
    3: 'success',
    2: 'warning',
    1: ''
  }
  return map[deptType] || 'info'
}

function getLayerName(layer: number): string {
  const map: Record<number, string> = {
    4: '师级',
    3: '团级',
    2: '连级'
  }
  return map[layer] || ''
}

function getStyleByDeptType(deptType: number): L.PathOptions {
  const styleMap: Record<number, L.PathOptions> = {
    5: { color: '#e74c3c', weight: 3, fillOpacity: 0.2, fillColor: '#e74c3c' },
    4: { color: '#3498db', weight: 2.5, fillOpacity: 0.25, fillColor: '#3498db' },
    3: { color: '#2ecc71', weight: 2, fillOpacity: 0.3, fillColor: '#2ecc71' },
    2: { color: '#f39c12', weight: 1.5, fillOpacity: 0.35, fillColor: '#f39c12' },
    1: { color: '#9b59b6', weight: 1, fillOpacity: 0.4, fillColor: '#9b59b6' }
  }
  return styleMap[deptType] || { color: '#95a5a6', weight: 1, fillOpacity: 0.3 }
}

// ======================== 地图初始化 ========================

function initMap() {
  if (!mapRef.value || map) return

  // 初始化地图 - 以新疆为中心
  map = L.map(mapRef.value, {
    center: [41.5, 86.0],  // 新疆中心大致坐标
    zoom: 6,
    minZoom: 4,
    maxZoom: 18
  })

  // 添加 OpenStreetMap 底图
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
  }).addTo(map)

  // 初始化图层组
  drawnItems = new L.FeatureGroup()
  subordinateLayer = new L.FeatureGroup()
  layerDataGroup = new L.FeatureGroup()

  map.addLayer(drawnItems)
  map.addLayer(subordinateLayer)
  map.addLayer(layerDataGroup)

  // 仅对可编辑用户启用 Geoman
  if (canEditRegion.value) {
    initGeoman()
  }
}

function initGeoman() {
  if (!map) return

  // 添加 Geoman 控件
  map.pm.addControls({
    position: 'topleft',
    drawMarker: false,
    drawPolyline: false,
    drawCircle: false,
    drawCircleMarker: false,
    drawPolygon: true,
    drawRectangle: true,
    drawText: false,
    editMode: true,
    dragMode: true,
    cutPolygon: false,
    removalMode: true,
    rotateMode: false
  })

  // 设置 Geoman 语言为中文
  map.pm.setLang('zh')

  // 监听绘制完成事件
  map.on('pm:create', (e: any) => {
    // 清除之前绑定的图形（只保留一个）
    drawnItems.clearLayers()
    drawnItems.addLayer(e.layer)

    // 绑定弹窗
    e.layer.bindPopup('我的管辖区域（未保存）')

    ElMessage.success('区域绑定成功，请点击「保存区域」提交')
  })

  // 监听编辑完成
  map.on('pm:edit', () => {
    ElMessage.info('区域已修改，请点击「保存区域」提交')
  })

  // 监听删除
  map.on('pm:remove', (e: any) => {
    drawnItems.removeLayer(e.layer)
  })
}

// ======================== 数据加载 ========================

async function loadMyRegion() {
  if (!map) return
  mapLoading.value = true

  try {
    const { data } = await apiGetRegion()
    myRegionData.value = data.data

    if (data.data?.regionShape) {
      drawnItems.clearLayers()

      const geoJsonLayer = L.geoJSON(data.data.regionShape as any, {
        style: () => ({
          color: '#409EFF',
          weight: 3,
          fillOpacity: 0.3,
          fillColor: '#409EFF'
        })
      })

      geoJsonLayer.eachLayer(layer => {
        drawnItems.addLayer(layer)
        ;(layer as L.Layer).bindPopup(`我的管辖区域：${data.data?.deptName || ''}`)
      })

      // 缩放到区域
      const bounds = drawnItems.getBounds()
      if (bounds.isValid()) {
        map.fitBounds(bounds, { padding: [50, 50] })
      }

      ElMessage.success('区域加载成功')
    } else {
      ElMessage.info('当前单位暂未划定区域')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '加载区域失败')
  } finally {
    mapLoading.value = false
  }
}

async function loadSubordinates() {
  if (!map) return
  subLoading.value = true
  mapLoading.value = true

  try {
    const { data } = await apiGetSubordinates()
    subordinateList.value = data.data || []

    // 清除并重新渲染
    subordinateLayer.clearLayers()

    subordinateList.value.forEach(dept => {
      if (dept.regionShape) {
        const layer = L.geoJSON(dept.regionShape as any, {
          style: () => getStyleByDeptType(dept.deptType)
        })

        layer.eachLayer(l => {
          ;(l as L.Layer).bindPopup(`
            <strong>${dept.deptName}</strong><br/>
            级别：${getDeptTypeName(dept.deptType)}
          `)
          ;(l as any)._deptData = dept  // 存储关联数据
        })

        subordinateLayer.addLayer(layer)
      }
    })

    // 如果有数据，缩放到全部范围
    const bounds = subordinateLayer.getBounds()
    if (bounds.isValid()) {
      map.fitBounds(bounds, { padding: [50, 50] })
    }

    ElMessage.success(`加载完成，共 ${subordinateList.value.length} 个下属单位`)
  } catch (e: any) {
    ElMessage.error(e?.message || '加载下属单位失败')
  } finally {
    subLoading.value = false
    mapLoading.value = false
  }
}

async function loadLayerData() {
  if (!map || !isCorps.value) return
  layerLoading.value = true
  mapLoading.value = true

  try {
    const { data } = await apiGetLayerData(selectedLayer.value)
    layerList.value = data.data || []

    // 清除并重新渲染
    layerDataGroup.clearLayers()

    layerList.value.forEach(dept => {
      if (dept.regionShape) {
        const layer = L.geoJSON(dept.regionShape as any, {
          style: () => getStyleByDeptType(dept.deptType)
        })

        layer.eachLayer(l => {
          ;(l as L.Layer).bindPopup(`
            <strong>${dept.deptName}</strong><br/>
            级别：${getDeptTypeName(dept.deptType)}
          `)
          ;(l as any)._deptData = dept
        })

        layerDataGroup.addLayer(layer)
      }
    })

    // 缩放到全部范围
    const bounds = layerDataGroup.getBounds()
    if (bounds.isValid()) {
      map.fitBounds(bounds, { padding: [50, 50] })
    }

    ElMessage.success(`加载完成，共 ${layerList.value.length} 个${getLayerName(selectedLayer.value)}单位`)
  } catch (e: any) {
    ElMessage.error(e?.message || '加载态势数据失败')
  } finally {
    layerLoading.value = false
    mapLoading.value = false
  }
}

// ======================== 操作方法 ========================

async function saveRegion() {
  if (!drawnItems) return

  const layers = drawnItems.getLayers()
  if (layers.length === 0) {
    ElMessage.warning('请先绑定管辖区域')
    return
  }

  const layer = layers[0]
  const geometry = layerToGeoJSON(layer)
  const center = getLayerCenter(layer)

  if (!geometry) {
    ElMessage.error('无法解析区域数据')
    return
  }

  try {
    await ElMessageBox.confirm('确认保存当前绑定的管辖区域？', '保存确认', {
      type: 'info'
    })
  } catch {
    return
  }

  saving.value = true
  try {
    const payload: UpdateRegionPayload = {
      regionShape: geometry,
      regionCenter: center || undefined
    }
    await apiUpdateRegion(payload)
    ElMessage.success('区域保存成功')

    // 更新弹窗内容
    ;(layer as L.Layer).bindPopup(`我的管辖区域（已保存）`)
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function clearDrawing() {
  if (!drawnItems) return

  ElMessageBox.confirm('确认清除当前绑定的区域？此操作不会删除已保存的数据。', '清除确认', {
    type: 'warning'
  }).then(() => {
    drawnItems.clearLayers()
    ElMessage.info('已清除绑定图层')
  }).catch(() => {})
}

function clearSubordinates() {
  subordinateLayer?.clearLayers()
  subordinateList.value = []
  ElMessage.info('已清除下属单位图层')
}

function handleLayerChange() {
  // 切换层级时清除当前数据
  layerDataGroup?.clearLayers()
  layerList.value = []
}

function focusOnDept(dept: DeptGisDTO) {
  if (!map) return

  if (dept.regionShape) {
    const layer = L.geoJSON(dept.regionShape as any)
    const bounds = layer.getBounds()
    if (bounds.isValid()) {
      map.fitBounds(bounds, { padding: [100, 100], maxZoom: 12 })
    }
  } else if (dept.regionCenter) {
    const coords = dept.regionCenter.coordinates
    map.setView([coords[1], coords[0]], 10)
  } else {
    ElMessage.info('该单位暂无区域数据')
  }
}

function refreshData() {
  if (canEditRegion.value) {
    loadMyRegion()
  }
  if (canViewSubordinates.value && subordinateList.value.length > 0) {
    loadSubordinates()
  }
  if (isCorps.value && layerList.value.length > 0) {
    loadLayerData()
  }
}

// ======================== 工具函数 ========================

function layerToGeoJSON(layer: L.Layer): GeoJSONGeometry | null {
  if (!layer) return null

  if ('toGeoJSON' in layer && typeof layer.toGeoJSON === 'function') {
    const geoJson = (layer as L.Polygon).toGeoJSON()
    return geoJson.geometry as GeoJSONGeometry
  }
  return null
}

function getLayerCenter(layer: L.Layer): GeoJSONPoint | null {
  if (!layer) return null

  if ('getBounds' in layer && typeof layer.getBounds === 'function') {
    const bounds = (layer as L.Polygon).getBounds()
    const center = bounds.getCenter()
    return {
      type: 'Point',
      coordinates: [center.lng, center.lat]
    }
  }
  return null
}

// ======================== 生命周期 ========================

onMounted(async () => {
  await nextTick()
  initMap()

  // 自动加载本级区域
  if (canEditRegion.value) {
    loadMyRegion()
  }
})

onUnmounted(() => {
  if (map) {
    map.remove()
    map = null
  }
})

// 监听 mapRef 变化（防止初始化时机问题）
watch(mapRef, (el) => {
  if (el && !map) {
    initMap()
  }
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

.filters {
  margin-bottom: 12px;
}

.main-content {
  display: flex;
  gap: 16px;
  height: 500px;
}

.list-panel {
  width: 320px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  overflow: hidden;
}

.list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  background: var(--el-fill-color-light);
  border-bottom: 1px solid var(--el-border-color-light);
}

.list-title {
  font-weight: 600;
  font-size: 14px;
}

.map-panel {
  flex: 1;
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--el-border-color-light);
}

.map-panel.full-width {
  width: 100%;
}

.map-container {
  width: 100%;
  height: 100%;
  background: #f5f7fa;
}

.map-legend {
  position: absolute;
  bottom: 20px;
  right: 20px;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.95);
  padding: 12px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  min-width: 80px;
}

.legend-title {
  font-weight: 600;
  font-size: 12px;
  margin-bottom: 8px;
  color: var(--el-text-color-primary);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  margin-bottom: 4px;
  color: var(--el-text-color-regular);
}

.legend-item:last-child {
  margin-bottom: 0;
}

.legend-color {
  width: 16px;
  height: 12px;
  border-radius: 2px;
  opacity: 0.8;
}

.map-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  z-index: 1001;
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.map-loading .is-loading {
  font-size: 24px;
  color: var(--el-color-primary);
}

.stats-bar {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  padding: 16px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
}

/* Leaflet 覆盖样式 */
:deep(.leaflet-control-container) {
  .leaflet-top.leaflet-left {
    top: 10px;
    left: 10px;
  }
}

:deep(.leaflet-popup-content-wrapper) {
  border-radius: 8px;
}

:deep(.leaflet-popup-content) {
  margin: 12px 16px;
  font-size: 13px;
  line-height: 1.5;
}

/* Geoman 控件样式覆盖 */
:deep(.leaflet-pm-toolbar) {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

:deep(.leaflet-pm-actions-container) {
  border-radius: 4px;
}
</style>
