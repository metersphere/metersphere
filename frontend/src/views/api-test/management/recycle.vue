<template>
  <MsCard :min-width="1180" simple no-content-padding>
    <MsSplitBox :size="0.25" :max="0.5">
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
            :protocol="protocol"
          />
        </div>
      </template>
    </MsSplitBox>
  </MsCard>
</template>

<script lang="ts" setup>
  import { provide } from 'vue';
  import { useRouter } from 'vue-router';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import { RequestParam } from '../components/requestComposition/index.vue';
  import moduleTree from './components/moduleTree.vue';
  import management from './components/recycle/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ModuleTreeNode } from '@/models/common';

  const { t } = useI18n();
  const router = useRouter();
  const activeModule = ref<string>('all');
  const folderTree = ref<ModuleTreeNode[]>([]);
  const folderTreePathMap = ref<Record<string, any>>({});
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

  function handleNodeSelect(keys: string[], _offspringIds: string[]) {
    [activeModule.value] = keys;
    offspringIds.value = _offspringIds;
  }

  function handleProtocolChange(val: string) {
    protocol.value = val;
  }

  function refreshModuleTree() {
    moduleTreeRef.value?.refresh();
  }

  /** 向子孙组件提供方法和值 */
  provide('refreshModuleTree', refreshModuleTree);
  provide('folderTreePathMap', folderTreePathMap.value);
</script>

<style lang="less" scoped></style>
