export default {
  id: 'counter',
  // 状态值定义
  state: () => {
    return {count: 0}
  },
  // 状态更改方法定义
  actions: {
    increment() {
      this.count++
    },
  },
}
