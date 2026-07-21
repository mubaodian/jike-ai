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
