<template>
  <memberTable v-if="isMounted" />
</template>

<script setup lang="ts">
  /**
   * @description 项目管理-项目与权限-成员
   */
  const memberTable = defineAsyncComponent(() => import('./components/memberTable.vue'));

  const isMounted = ref(false);

  onMounted(() => {
    isMounted.value = true; // 解决memberTable因为异步初始化列导致的路由组件渲染两次的问题
  });
</script>

<style lang="less" scoped>
  div {
    color: var(--color-text-1);
  }
</style>
