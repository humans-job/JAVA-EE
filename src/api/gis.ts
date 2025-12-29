import http, { type ApiResp } from './http'

// ======================== GeoJSON 类型定义 ========================

export type GeoJSONPoint = {
    type: 'Point'
    coordinates: [number, number]
}

export type GeoJSONPolygon = {
    type: 'Polygon'
    coordinates: number[][][]
}

export type GeoJSONMultiPolygon = {
    type: 'MultiPolygon'
    coordinates: number[][][][]
}

export type GeoJSONLineString = {
    type: 'LineString'
    coordinates: number[][]
}

export type GeoJSONGeometry =
    | GeoJSONPoint
    | GeoJSONPolygon
    | GeoJSONMultiPolygon
    | GeoJSONLineString
    | {
    type: 'MultiPoint' | 'MultiLineString' | 'GeometryCollection'
    coordinates?: any
    geometries?: GeoJSONGeometry[]
}

// ======================== DTO 类型定义 ========================

export type DeptGisDTO = {
    deptId: number
    deptName: string
    deptType: number
    regionShape: GeoJSONGeometry | null
    regionCenter: GeoJSONPoint | null
}

export type UpdateRegionPayload = {
    regionShape: GeoJSONGeometry
    regionCenter?: GeoJSONPoint
}

export const DeptTypeEnum = {
    CORPS: 5,
    DIVISION: 4,
    REGIMENT: 3,
    COMPANY: 2,
    MILITIA: 1
} as const

export type DeptType = (typeof DeptTypeEnum)[keyof typeof DeptTypeEnum]

// ======================== API 接口 ========================

/** 更新本级管辖区域 */
export function apiUpdateRegion(payload: UpdateRegionPayload) {
    return http.put<ApiResp<string>>('/api/gis/region', payload)
}

/** 获取当前部门的区域数据 */
export function apiGetRegion() {
    return http.get<ApiResp<DeptGisDTO>>('/api/gis/region')
}

/** 查询直属下级单位的 GIS 态势 */
export function apiGetSubordinates() {
    return http.get<ApiResp<DeptGisDTO[]>>('/api/gis/situation/subordinates')
}

/** 分层加载全疆地图数据（兵团级） */
export function apiGetLayerData(deptType: DeptType | number) {
    return http.get<ApiResp<DeptGisDTO[]>>('/api/gis/situation/layer', {
        params: { type: deptType }
    })
}
