<template>
  <div
    class="mr-[4px] h-[8px] w-[8px] rounded-full"
    :style="{
      backgroundColor: caseLevelMap[props.caseLevel].bgColor,
      border: `1px solid ${caseLevelMap[props.caseLevel].borderColor}`,
    }"
  ></div>
  {{ caseLevelMap[props.caseLevel].label }}
</template>

<script setup lang="ts">
  import { CaseLevel } from './types';

  const props = defineProps<{
    caseLevel: CaseLevel;
  }>();

  const caseLevelMap = {
    P1: {
      label: 'P1',
      bgColor: 'rgb(var(--danger-2))',
      borderColor: 'rgb(var(--danger-6))',
    },
    P2: {
      label: 'P2',
      bgColor: 'rgb(var(--warning-2))',
      borderColor: 'rgb(var(--warning-6))',
    },
    P3: {
      label: 'P3',
      bgColor: 'rgb(var(--link-2))',
      borderColor: 'rgb(var(--link-5))',
    },
    P4: {
      label: 'P4',
      bgColor: 'var(--color-text-n8)',
      borderColor: 'var(--color-text-brand)',
    },
  } as const;
</script>

<style lang="less" scoped></style>
