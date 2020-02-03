const options = function (value, array) {
  if (!value) return '';
  if (array) {
    for (let i = 0; i < array.length; i++) {
      if (value === array[i].key) {
        return array[i].value
      }
    }
  }
  return value;
};

const filters = {
  "options": options,
};

export default {
  install(Vue) {
    // 注册公用过滤器
    Object.keys(filters).forEach(key => {
      Vue.filter(key, filters[key])
    });
  }
}
