const value = {
  maxLength: null,
  minLength: null,
  default: null,
  enum: null,
  pattern: null,
  format: null,
  description: null,
}
const attr = {
  maxLength: {
    name: '最大字符数',
    type: 'integer'
  },
  minLength: {
    name: '最小字符数',
    type: 'integer'
  },
  default: {
    name: '默认值',
    type: 'string',
  },
  enum: {
    name: '枚举值',
    type: 'textarea',
    description:"一行一个枚举值"
  },
  pattern: {
    name: '正则表达式',
    type: 'string'
  },
  format: {
    name: '格式',
    type: 'array',
    enums: ['date', 'date-time', 'email', 'hostname', 'ipv4', 'ipv6', 'uri']
  },
  description: {
    name: '描述',
    type: 'string',
  }
}
const wrapper = {value, attr}
export default wrapper
