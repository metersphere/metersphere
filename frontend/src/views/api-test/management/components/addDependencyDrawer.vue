<template>
  <MsDrawer
    v-model:visible="innerVisible"
    :title="props.mode === 'pre' ? t('apiTestManagement.addPreDependency') : t('apiTestManagement.addPostDependency')"
    :width="960"
    no-content-padding
  >
    <div v-if="innerVisible" class="flex h-full w-full overflow-hidden px-[16px]">
      <moduleTree
        class="w-[200px] pt-[16px]"
        read-only
        @init="(val) => (folderTree = val)"
        @folder-node-select="handleNodeSelect"
        @change-protocol="handleProtocolChange"
      />
      <a-divider direction="vertical" :margin="16"></a-divider>
      <apiTable
        :active-module="activeModule"
        :offspring-ids="offspringIds"
        class="flex-1 overflow-hidden !pl-0 !pr-[16px]"
        :selected-protocols="selectedProtocols"
        read-only
      />
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import apiTable from './management/api/apiTable.vue';
  import moduleTree from '@/views/api-test/management/components/moduleTree.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ModuleTreeNode } from '@/models/common';

  const props = defineProps<{
    visible: boolean;
    mode: 'pre' | 'post'; // pre: 前置依赖，post: 后置依赖
  }>();

  const { t } = useI18n();

  const innerVisible = defineModel<boolean>('visible', {
    default: false,
  });

  const folderTree = ref<ModuleTreeNode[]>([]);
  const activeModule = ref<string>('all');
  const offspringIds = ref<string[]>([]);
  const selectedProtocols = ref<string[]>([]);

  function handleNodeSelect(keys: string[], _offspringIds: string[]) {
    [activeModule.value] = keys;
    offspringIds.value = _offspringIds;
  }

  function handleProtocolChange(val: string[]) {
    selectedProtocols.value = val;
  }
</script>

<style lang="less" scoped></style>
