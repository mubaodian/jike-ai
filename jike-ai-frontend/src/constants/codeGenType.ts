/**
 * 代码生成类型枚举
 * 对应后端 CodeGenTypeEnum
 */
export const CODE_GEN_TYPE_ENUM = {
  HTML: 'html',
  MULTI_FILE: 'multi_file',
  VUE_PROJECT:'vue_project'
} as const

export type CodeGenType = (typeof CODE_GEN_TYPE_ENUM)[keyof typeof CODE_GEN_TYPE_ENUM]

/**
 * 代码生成类型选项列表（用于下拉选择）
 */
export const CODE_GEN_TYPE_OPTIONS = [
  { label: '原生 HTML 模式', value: CODE_GEN_TYPE_ENUM.HTML },
  { label: '原生多文件模式', value: CODE_GEN_TYPE_ENUM.MULTI_FILE },
  { label: 'Vue 项目模式', value: CODE_GEN_TYPE_ENUM.VUE_PROJECT },
]

/**
 * 代码生成类型显示文本映射（用于 Tag 标签展示）
 */
export const CODE_GEN_TYPE_DISPLAY = {
  [CODE_GEN_TYPE_ENUM.HTML]: 'HTML 模式',
  [CODE_GEN_TYPE_ENUM.MULTI_FILE]: '多文件模式',
  [CODE_GEN_TYPE_ENUM.VUE_PROJECT]: 'Vue 项目模式',
} as const

/**
 * 根据类型值获取显示文本
 */
export const getCodeGenTypeDisplay = (type: string): string => {
  return CODE_GEN_TYPE_DISPLAY[type as CodeGenType] || type
}
