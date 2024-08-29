<template>
  <div>
    <slot></slot>
  </div>
</template>

<script lang="ts">
  // 注意: vue3不支持使用 defineOptions() 来动态设置组件名称，由于不能访问 setup() 中的响应式变量
  export default {
    name: '',
    props: {
      cacheName: {
        type: String,
        required: true,
      },
    },
    created() {
      this.$options.name = this.cacheName;
    },
  };
</script>

<style scoped></style>
