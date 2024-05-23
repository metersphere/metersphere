<template>
  <FeatureCaseMinder :module-id="props.moduleId" :module-name="props.moduleName" :modules-count="props.modulesCount" />
</template>

<script setup lang="ts">
  import FeatureCaseMinder from './featureCaseMinder/index.vue';

  const props = defineProps<{
    minderType: 'FeatureCase';
    moduleId: string;
    moduleName: string;
    modulesCount: Record<string, number>; // 模块数量
  }>();
</script>

<style lang="less" scoped></style>
