<template>
  <FeatureCaseMinder :module-id="props.moduleId" :module-name="props.moduleName" />
</template>

<script setup lang="ts">
  import FeatureCaseMinder from '@/components/business/ms-minders/featureCaseMinder.vue';

  const props = defineProps<{
    minderType: 'FeatureCase';
    moduleId: string;
    moduleName: string;
  }>();
</script>

<style lang="less" scoped></style>
