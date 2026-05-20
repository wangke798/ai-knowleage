/** 统一响应结构 */
export interface Result<T = unknown> {
  code: number
  message: string
  data: T
  traceId?: string
}

/** 分页请求参数 */
export interface PageQuery {
  page?: number
  size?: number
}

/** 分页响应结构 */
export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}
