<template>
  <MsCard simple no-content-padding>
    <MsSplitBox :size="0.25" :max="0.5">
      <template #first>
        <div class="p-[24px]">
          <moduleTree @new-api="newApi" />
        </div>
      </template>
      <template #second>
        <div class="flex h-full flex-col">
          <debug ref="debugRef" />
        </div>
      </template>
    </MsSplitBox>
  </MsCard>
</template>

<script lang="ts" setup>
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import debug from './components/debug/index.vue';
  import moduleTree from './components/moduleTree.vue';

  const debugRef = ref<InstanceType<typeof debug>>();

  function newApi() {
    debugRef.value?.addDebugTab();
  }
</script>

<style lang="less" scoped></style>
