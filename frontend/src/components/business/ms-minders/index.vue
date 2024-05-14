<template>
  <FeatureCaseMinder :import-json="props.importJson" />
</template>

<script setup lang="ts">
  import type { MinderJson } from '@/components/pure/ms-minder-editor/props';
  import FeatureCaseMinder from '@/components/business/ms-minders/featureCaseMinder.vue';

  const props = defineProps<{
    minderType: 'FeatureCase';
    importJson: MinderJson;
  }>();
</script>

<style lang="less" scoped></style>
