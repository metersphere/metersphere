<template>
  <MsCard simple no-content-padding>
    <MsSplitBox :size="0.25" :max="0.5">
      <template #first>
        <div class="p-[24px]">
          <moduleTree @init="(val) => (folderTree = val)" @new-api="newApi" @change="(val) => (activeModule = val)" />
        </div>
      </template>
      <template #second>
        <div class="flex h-full flex-col">
          <debug ref="debugRef" :module="activeModule" :module-tree="folderTree" />
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

  import { ModuleTreeNode } from '@/models/projectManagement/file';

  const debugRef = ref<InstanceType<typeof debug>>();
  const activeModule = ref<string>('root');
  const folderTree = ref<ModuleTreeNode[]>([]);

  function newApi() {
    debugRef.value?.addDebugTab();
  }
</script>

<style lang="less" scoped></style>
