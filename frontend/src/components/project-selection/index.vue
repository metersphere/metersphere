<template>
  <div class="project-selection">
    <a-select v-model="value" placeholder="Please select ...">
      <a-option v-for="item of data" :key="item.key" :value="item" :label="item.label" />
    </a-select>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';

  const value = ref('默认工作空间');
  const data = [
    {
      value: '测试项目1',
      label: '测试项目1',
      key: '1',
    },
    {
      value: '测试项目2',
      label: '测试项目2',
      key: '2',
    },
    {
      value: '默认工作空间',
      label: '默认工作空间',
      key: '2',
    },
  ];
</script>

<style lang="less" scoped>
  .project-selection {
    & .arco-select-view-single {
      @apply border-none;

      color: #323233;
      background-color: var(--color-bg-1);
    }
  }
</style>
