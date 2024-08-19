<template>
  <div v-if="caseLevel" class="flex items-center">
    <div
      class="mr-[4px] h-[8px] w-[8px] rounded-full"
      :style="{
        backgroundColor: caseLevel.bgColor,
        border: `1px solid ${caseLevel.borderColor}`,
      }"
    ></div>
    {{ caseLevel.label }}
  </div>
  <span v-else class="text-[var(--color-text-2)]"> - </span>
</template>

<script setup lang="ts">
  import { CaseLevel } from './types';

  const props = defineProps<{
    caseLevel?: CaseLevel;
  }>();

  const caseLevelMap = {
    P0: {
      label: 'P0',
      bgColor: 'rgb(var(--danger-2))',
      borderColor: 'rgb(var(--danger-6))',
    },
    P1: {
      label: 'P1',
      bgColor: 'rgb(var(--warning-2))',
      borderColor: 'rgb(var(--warning-6))',
    },
    P2: {
      label: 'P2',
      bgColor: 'rgb(var(--link-2))',
      borderColor: 'rgb(var(--link-5))',
    },
    P3: {
      label: 'P3',
      bgColor: 'var(--color-text-n8)',
      borderColor: 'var(--color-text-brand)',
    },
  };

  const caseLevel = computed(() => (props.caseLevel ? caseLevelMap[props.caseLevel] : undefined));
</script>

<style lang="less" scoped></style>
