<template>
  <MsCard has-breadcrumb simple no-content-padding>
    <MsSplitBox :size="300" :max="0.5">
      <template #first>
        <div class="p-[24px]">
          <moduleTree
            ref="moduleTreeRef"
            :active-node-id="activeApi?.id"
            :trash="true"
            @init="handleModuleInit"
            @folder-node-select="handleNodeSelect"
            @change-protocol="handleProtocolChange"
          />
        </div>
      </template>
      <template #second>
        <div class="relative flex h-full flex-col">
          <management
            ref="managementRef"
            :module-tree="folderTree"
            :active-module="activeModule"
            :offspring-ids="offspringIds"
            :selected-protocols="selectedProtocols"
          />
        </div>
      </template>
    </MsSplitBox>
  </MsCard>
</template>

<script lang="ts" setup>
  import { provide } from 'vue';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import { RequestParam } from '../components/requestComposition/index.vue';
  import moduleTree from './components/moduleTree.vue';
  import management from './components/recycle/index.vue';

  import { ApiDefinitionGetModuleParams } from '@/models/apiTest/management';
  import { ModuleTreeNode } from '@/models/common';

  const activeModule = ref<string>('all');
  const folderTree = ref<ModuleTreeNode[]>([]);
  const folderTreePathMap = ref<Record<string, any>>({});
  const offspringIds = ref<string[]>([]);
  const selectedProtocols = ref<string[]>([]);
  const activeApi = ref<RequestParam>();
  const moduleTreeRef = ref<InstanceType<typeof moduleTree>>();
  const managementRef = ref<InstanceType<typeof management>>();

  function handleModuleInit(tree: ModuleTreeNode[], _protocol: string[], pathMap: Record<string, any>) {
    folderTree.value = tree;
    selectedProtocols.value = _protocol;
    folderTreePathMap.value = pathMap;
  }

  function handleNodeSelect(keys: string[], _offspringIds: string[]) {
    [activeModule.value] = keys;
    offspringIds.value = _offspringIds;
  }

  function handleProtocolChange(val: string[]) {
    selectedProtocols.value = val;
  }

  function refreshModuleTree() {
    moduleTreeRef.value?.refresh();
  }
  function refreshModuleTreeCount(params: ApiDefinitionGetModuleParams) {
    moduleTreeRef.value?.initModuleCount(params);
  }

  /** 向子孙组件提供方法和值 */
  provide('refreshModuleTree', refreshModuleTree);
  provide('refreshModuleTreeCount', refreshModuleTreeCount);
  provide('folderTreePathMap', folderTreePathMap.value);
</script>

<style lang="less" scoped></style>
