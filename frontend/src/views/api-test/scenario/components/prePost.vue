<template>
  <div class="condition">
    <div>
      <precondition v-model:config="preProcessorConfig" :is-definition="false" />
    </div>
    <a-divider class="my-[8px]" type="dashed" />
    <div>
      <postcondition v-model:config="postProcessorConfig" :is-definition="false" :layout="activeLayout" />
    </div>
  </div>
</template>

<script setup lang="ts">
  import postcondition from '@/views/api-test/components/requestComposition/postcondition.vue';
  import precondition from '@/views/api-test/components/requestComposition/precondition.vue';

  const activeLayout = ref<'horizontal' | 'vertical'>('vertical');
  const preProcessorConfig = ref({
    enableGlobal: false,
    processors: [],
  });
  const postProcessorConfig = ref({
    enableGlobal: false,
    processors: [],
  });
</script>

<style lang="less" scoped>
  .condition {
    flex-shrink: 0;
    width: 100%;
    height: 700px;
    overflow: overlay;
  }
</style>
