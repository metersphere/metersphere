<template>
  <a-steps v-bind="attrs" @change="changeCurrent">
    <a-step v-for="item of stepList" :key="item.title">{{ t(item.title) }}</a-step>
  </a-steps>
</template>

<script setup lang="ts">
  import { ref, useAttrs } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import type { StepList } from '@/models/system/plugin';

  const { t } = useI18n();
  const props = defineProps<{
    current: number;
    stepList: StepList;
    setCurrent: (step: number) => void;
  }>();
  const attrs = useAttrs();
  const changeCurrent = (step: number) => {
    props.setCurrent(step);
  };
</script>

<style scoped lang="less">
  :deep(.arco-steps-icon) {
    width: 20px;
    height: 20px;
    line-height: 19px;
  }
  :deep(.arco-modal-body) {
    padding: 10px;
  }
</style>
