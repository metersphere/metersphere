<template>
  <div>
    <MsCard simple :special-height="119" no-content-padding>
      <modelConfig model-key="system" :card-min-width="320" />
    </MsCard>
  </div>
</template>

<script setup lang="ts">
  import MsCard from '@/components/pure/ms-card/index.vue';
  import modelConfig from '@/components/business/ms-personal-drawer/components/modelConfig.vue';
</script>

<style scoped lang="less"></style>
