<template>
  <MsSplitBox>
    <template #first>
      <div class="p-[16px]">
        <CaseTree
          ref="caseTreeRef"
          :tree-type="props.treeType"
          :modules-count="modulesCount"
          :selected-keys="selectedKeys"
          @folder-node-select="handleFolderNodeSelect"
        />
      </div>
    </template>
    <template #second>
      <CaseTable
        ref="caseTableRef"
        :tree-type="props.treeType"
        :plan-id="planId"
        :modules-count="modulesCount"
        :module-name="moduleName"
        :module-parent-id="moduleParentId"
        :active-module="activeFolderId"
        :offspring-ids="offspringIds"
        :can-edit="props.canEdit"
        @select-parent-node="selectParentNode"
        @refresh="emit('refresh')"
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

  import useTestPlanFeatureCaseStore from '@/store/modules/testPlan/testPlanFeatureCase';

  import { ModuleTreeNode } from '@/models/common';

  const props = defineProps<{
    canEdit: boolean;
    treeType: 'MODULE' | 'COLLECTION';
  }>();

  const emit = defineEmits<{
    (e: 'refresh'): void;
  }>();

  const route = useRoute();
  const testPlanFeatureCaseStore = useTestPlanFeatureCaseStore();

  const planId = ref(route.query.id as string);
  const modulesCount = computed(() => testPlanFeatureCaseStore.modulesCount);

  const caseTableRef = ref<InstanceType<typeof CaseTable>>();
  const activeFolderId = ref<string>('all');
  const moduleName = ref<string>('');
  const moduleParentId = ref<string>('');
  const offspringIds = ref<string[]>([]);
  const selectedKeys = computed({
    get: () => [activeFolderId.value],
    set: (val) => val,
  });
  function handleFolderNodeSelect(ids: string[], _offspringIds: string[], name?: string, parentId?: string) {
    [activeFolderId.value] = ids;
    offspringIds.value = [..._offspringIds];
    moduleName.value = name ?? '';
    moduleParentId.value = parentId ?? '';
    caseTableRef.value?.resetSelector();
  }

  const caseTreeRef = ref<InstanceType<typeof CaseTree>>();

  function selectParentNode(folderTree: ModuleTreeNode[]) {
    caseTreeRef.value?.selectParentNode(folderTree);
  }

  function getCaseTableList() {
    nextTick(async () => {
      await caseTreeRef.value?.initModules();
      if (activeFolderId.value !== 'all') {
        caseTreeRef.value?.setActiveFolder('all');
      } else {
        caseTableRef.value?.handleTreeTypeChange();
      }
    });
  }

  defineExpose({
    getCaseTableList,
  });
</script>
