/**
 * @author: giscafer ,https://github.com/giscafer
 * @date: 2020-05-21 17:21:29
 * @description: 用一个Vue实例封装事件常用的方法并赋值给全局的变量，以便在任何一个组件都可调用这些方法来实现全局事件管理
 *
 * 使用如下：
 * mounted(){
    this.$jsEditorEvent.on('change_value',id);
    this.$jsEditorEvent.emit('change_value',1);
    ...
  }
 */

import Vue from 'vue';

const eventHub = new Vue({
  methods: {
    on(...args) {
      this.$on.apply(this, args);
    },
    emit(...args) {
      this.$emit.apply(this, args);
    },
    off(...args) {
      this.$off.apply(this, args);
    },
    once(...args) {
      this.$once.apply(this, args);
    },
  },
});

/* const CustomEventPlugin = V =>
  Object.defineProperty(V.prototype, '$event', {
    value: eventHub,
    writable: true
  }); */

const CustomEventPlugin = {
  install: function(V) {
    Object.defineProperty(V.prototype, '$jsEditorEvent', {
      value: eventHub,
      writable: true,
    });
  },
};

export default CustomEventPlugin;
