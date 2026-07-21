/**
 * 环境变量配置
 */

import { CODE_GEN_TYPE_ENUM } from './constants/codeGenType'

// 应用部署域名
export const DEPLOY_BASE_URL = import.meta.env.VITE_DEPLOY_BASE_URL || 'http://localhost'

// API 基础地址
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8123/api'

// 静态资源地址
export const STATIC_BASE_URL = `${API_BASE_URL}/static`

// 获取部署应用的完整URL
export const getDeployUrl = (deployKey: string) => {
  return `${DEPLOY_BASE_URL}/${deployKey}`
}

// 获取
export const getStaticPreviewUrl = (codeGenType: string, appId: string) => {
  const baseUrl = `${STATIC_BASE_URL}/${codeGenType}_${appId}/`
  if(codeGenType === CODE_GEN_TYPE_ENUM.VUE_PROJECT) {
    return `${baseUrl}dist/index.html`
  }
  return baseUrl
}
