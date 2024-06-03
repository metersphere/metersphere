<template>
  <MsSplitBox>
    <template #first>
      <CaseTree
        ref="caseTreeRef"
        :modules-count="modulesCount"
        :selected-keys="selectedKeys"
        @folder-node-select="handleFolderNodeSelect"
        @init="initModuleTree"
      />
    </template>
    <template #second>
      <CaseTable
        ref="caseTableRef"
        :plan-id="planId"
        :modules-count="modulesCount"
        :module-name="moduleName"
        :repeat-case="props.repeatCase"
        :active-module="activeFolderId"
        :offspring-ids="offspringIds"
        :module-tree="moduleTree"
        :can-edit="props.canEdit"
        @get-module-count="getModuleCount"
        @refresh="emit('refresh')"
        @init-modules="initModules"
      ></CaseTable>
    </template>
  </MsSplitBox>
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue';
  import { useRoute } from 'vue-router';

  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import CaseTable from './components/scenarioTable.vue';
  import CaseTree from './components/scenarioTree.vue';

  import { getFeatureCaseModuleCount } from '@/api/modules/test-plan/testPlan';

  import { ModuleTreeNode } from '@/models/common';
  import type { PlanDetailFeatureCaseListQueryParams } from '@/models/testPlan/testPlan';

  const props = defineProps<{
    repeatCase: boolean;
    canEdit: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'refresh'): void;
  }>();

  const route = useRoute();

  const planId = ref(route.query.id as string);
  const modulesCount = ref<Record<string, any>>({});
  async function getModuleCount(params: PlanDetailFeatureCaseListQueryParams) {
    try {
      // TODO 联调
      modulesCount.value = await getFeatureCaseModuleCount(params);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const caseTableRef = ref<InstanceType<typeof CaseTable>>();
  const activeFolderId = ref<string>('all');
  const moduleName = ref<string>('');
  const offspringIds = ref<string[]>([]);
  const selectedKeys = computed({
    get: () => [activeFolderId.value],
    set: (val) => val,
  });
  function handleFolderNodeSelect(ids: string[], _offspringIds: string[], name?: string) {
    [activeFolderId.value] = ids;
    offspringIds.value = [..._offspringIds];
    moduleName.value = name ?? '';
    caseTableRef.value?.resetSelector();
  }

  const moduleTree = ref<ModuleTreeNode[]>([]);
  function initModuleTree(tree: ModuleTreeNode[]) {
    moduleTree.value = unref(tree);
  }

  const caseTreeRef = ref<InstanceType<typeof CaseTree>>();
  function initModules() {
    caseTreeRef.value?.initModules();
  }

  function getCaseTableList() {
    initModules();
    caseTableRef.value?.loadCaseList();
  }

  defineExpose({
    getCaseTableList,
  });
</script>
