const value = {
  maximum: null,
  minimum: null,
  default: null,
  enum: null,
  description: null,
}
const attr = {
  description: {
    name: '描述',
    type: 'string',
  },
  maximum: {
    name: '最大值',
    type: 'number'
  },
  minimum: {
    name: '最小值',
    type: 'number'
  },
  default: {
    name: '默认值',
    type: 'string',
  },
  enum: {
    name: '枚举值',
    type: 'textarea',
    description: "一行一个枚举值"
  },
}
const wrapper = {value, attr}
export default wrapper
