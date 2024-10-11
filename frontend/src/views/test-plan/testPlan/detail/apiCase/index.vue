<template>
  <MsSplitBox :not-show-first="isAdvancedSearchMode">
    <template #first>
      <div class="p-[16px]">
        <a-radio-group v-model:model-value="treeType" size="medium" class="mb-[16px] w-full" type="button">
          <a-radio value="COLLECTION">{{ t('ms.case.associate.testSet') }}</a-radio>
          <a-radio value="MODULE">{{ t('common.module') }}</a-radio>
        </a-radio-group>
        <CaseTree
          ref="caseTreeRef"
          :modules-count="modulesCount"
          :selected-keys="selectedKeys"
          :tree-type="treeType"
          @folder-node-select="handleFolderNodeSelect"
          @init="initModuleTree"
          @change-protocol="handleProtocolChange"
        />
      </div>
    </template>
    <template #second>
      <CaseTable
        ref="caseTableRef"
        :plan-id="planId"
        :tree-type="treeType"
        :all-protocol-list="allProtocolList"
        :module-parent-id="moduleParentId"
        :active-module="activeFolderId"
        :offspring-ids="offspringIds"
        :module-tree="moduleTree"
        :can-edit="props.canEdit"
        :selected-protocols="selectedProtocols"
        @get-module-count="getModuleCount"
        @refresh="emit('refresh')"
        @handle-adv-search="handleAdvSearch"
        @init-modules="initModules"
      ></CaseTable>
    </template>
  </MsSplitBox>
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue';
  import { useRoute } from 'vue-router';

  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import CaseTable from './components/caseTable.vue';
  import CaseTree from './components/caseTree.vue';

  import { getApiCaseModuleCount } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';

  import { ModuleTreeNode } from '@/models/common';
  import type { PlanDetailApiCaseQueryParams } from '@/models/testPlan/testPlan';

  const props = defineProps<{
    canEdit: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'refresh'): void;
  }>();

  const { t } = useI18n();
  const route = useRoute();

  const planId = ref(route.query.id as string);
  const modulesCount = ref<Record<string, any>>({});
  const selectedProtocols = ref<string[]>([]);
  function handleProtocolChange(val: string[]) {
    selectedProtocols.value = val;
  }
  async function getModuleCount(params: PlanDetailApiCaseQueryParams) {
    try {
      modulesCount.value = await getApiCaseModuleCount(params);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const caseTableRef = ref<InstanceType<typeof CaseTable>>();
  const activeFolderId = ref<string>('all');
  const moduleParentId = ref<string>('');
  const offspringIds = ref<string[]>([]);
  const selectedKeys = computed({
    get: () => [activeFolderId.value],
    set: (val) => val,
  });
  function handleFolderNodeSelect(ids: string[], _offspringIds: string[], parentId?: string) {
    [activeFolderId.value] = ids;
    offspringIds.value = [..._offspringIds];
    moduleParentId.value = parentId ?? '';
    caseTableRef.value?.resetSelector();
  }

  const moduleTree = ref<ModuleTreeNode[]>([]);
  function initModuleTree(tree: ModuleTreeNode[]) {
    moduleTree.value = unref(tree);
  }

  const caseTreeRef = ref<InstanceType<typeof CaseTree>>();
  const allProtocolList = computed<string[]>(() => caseTreeRef.value?.allProtocolList ?? []);

  function initModules() {
    caseTreeRef.value?.initModules();
  }

  const isAdvancedSearchMode = ref(false);
  function handleAdvSearch(isStartAdvance: boolean) {
    isAdvancedSearchMode.value = isStartAdvance;
    caseTreeRef.value?.setActiveFolder('all');
  }

  const treeType = ref<'MODULE' | 'COLLECTION'>('COLLECTION');
  function getCaseTableList() {
    nextTick(() => {
      initModules();
      if (activeFolderId.value !== 'all') {
        caseTreeRef.value?.setActiveFolder('all');
      } else {
        caseTableRef.value?.loadCaseList();
      }
    });
  }
  watch(
    () => treeType.value,
    () => {
      getCaseTableList();
    }
  );
</script>

<style lang="less" scoped>
  :deep(.arco-radio-button) {
    flex: 1;
    text-align: center;
  }
</style>
