<template>
  <MsCard :min-width="1180" simple no-content-padding>
    <MsSplitBox :size="0.25" :max="0.5">
      <template #first>
        <div class="p-[24px]">
          <moduleTree
            ref="moduleTreeRef"
            :active-node-id="activeApi?.id"
            @init="handleModuleInit"
            @new-api="newApi"
            @import="importDrawerVisible = true"
            @folder-node-select="handleNodeSelect"
            @click-api-node="handleApiNodeClick"
            @change-protocol="handleProtocolChange"
          />
        </div>
        <!-- <div class="b-0 absolute w-[88%]">
                <a-divider class="!my-0 !mb-2" />
                <div class="case h-[38px]">
                  <div class="flex items-center" :class="getActiveClass('recycle')" @click="setActiveFolder('recycle')">
                    <MsIcon type="icon-icon_delete-trash_outlined" class="folder-icon" />
                    <div class="folder-name mx-[4px]">{{ t('caseManagement.featureCase.recycle') }}</div>
                    <div class="folder-count">({{ recycleModulesCount.all || 0 }})</div></div
                  >
                </div>
              </div> -->
      </template>
      <template #second>
        <div class="relative flex h-full flex-col">
          <div
            id="managementContainer"
            :class="['absolute z-[101] h-full w-full', importDrawerVisible ? '' : 'invisible']"
            style="transition: all 0.3s"
          >
            <importApi
              v-model:visible="importDrawerVisible"
              :module-tree="folderTree"
              popup-container="#managementContainer"
              @done="handleImportDone"
            />
          </div>
          <management
            ref="managementRef"
            :module-tree="folderTree"
            :active-module="activeModule"
            :offspring-ids="offspringIds"
            :protocol="protocol"
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
  import importApi from './components/import.vue';
  import management from './components/management/index.vue';
  import moduleTree from './components/moduleTree.vue';

  import { ModuleTreeNode } from '@/models/common';

  const activeModule = ref<string>('all');
  const folderTree = ref<ModuleTreeNode[]>([]);
  const folderTreePathMap = ref<Record<string, any>>({});
  const importDrawerVisible = ref(false);
  const offspringIds = ref<string[]>([]);
  const protocol = ref('HTTP');
  const activeApi = ref<RequestParam>();
  const moduleTreeRef = ref<InstanceType<typeof moduleTree>>();
  const managementRef = ref<InstanceType<typeof management>>();

  function handleModuleInit(tree, _protocol: string, pathMap: Record<string, any>) {
    folderTree.value = tree;
    protocol.value = _protocol;
    folderTreePathMap.value = pathMap;
  }

  function newApi() {
    managementRef.value?.newTab();
  }

  function handleNodeSelect(keys: string[], _offspringIds: string[]) {
    [activeModule.value] = keys;
    offspringIds.value = _offspringIds;
  }

  function handleApiNodeClick(node: ModuleTreeNode) {
    managementRef.value?.newTab(node);
  }

  function setActiveApi(params: RequestParam) {
    activeApi.value = params;
  }

  function handleProtocolChange(val: string) {
    protocol.value = val;
  }

  function refreshModuleTree() {
    moduleTreeRef.value?.refresh();
  }

  function handleImportDone() {
    refreshModuleTree();
    managementRef.value?.refreshApiTable();
  }

  /** 向子孙组件提供方法和值 */
  provide('setActiveApi', setActiveApi);
  provide('refreshModuleTree', refreshModuleTree);
  provide('folderTreePathMap', folderTreePathMap.value);
</script>

<style lang="less" scoped></style>
