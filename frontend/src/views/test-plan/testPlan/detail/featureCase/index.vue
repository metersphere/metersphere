<template>
  <MsSplitBox>
    <template #first>
      <div class="p-[16px]">
        <a-radio-group v-model:model-value="treeType" size="medium" class="mb-[16px] w-full" type="button">
          <a-radio value="COLLECTION">{{ t('ms.case.associate.testSet') }}</a-radio>
          <a-radio value="MODULE">{{ t('common.module') }}</a-radio>
        </a-radio-group>
        <CaseTree
          ref="caseTreeRef"
          :tree-type="treeType"
          :modules-count="modulesCount"
          :selected-keys="selectedKeys"
          @folder-node-select="handleFolderNodeSelect"
        />
      </div>
    </template>
    <template #second>
      <CaseTable
        ref="caseTableRef"
        :tree-type="treeType"
        :plan-id="planId"
        :modules-count="modulesCount"
        :module-name="moduleName"
        :module-parent-id="moduleParentId"
        :active-module="activeFolderId"
        :offspring-ids="offspringIds"
        :can-edit="props.canEdit"
        @select-parent-node="selectParentNode"
        @refresh="emit('refresh')"
        @set-tree-type-to-module="setTreeTypeToModule"
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

  import { useI18n } from '@/hooks/useI18n';
  import useTestPlanFeatureCaseStore from '@/store/modules/testPlan/testPlanFeatureCase';

  import { ModuleTreeNode } from '@/models/common';

  const props = defineProps<{
    canEdit: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'refresh'): void;
  }>();

  const { t } = useI18n();
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

  const treeType = ref<'MODULE' | 'COLLECTION'>('COLLECTION');
  function setTreeTypeToModule() {
    treeType.value = 'MODULE'; // TODO lmy v3.4版本删除此代码
  }
  function getCaseTableList() {
    nextTick(async () => {
      await caseTreeRef.value?.initModules();
      if (activeFolderId.value !== 'all') {
        caseTreeRef.value?.setActiveFolder('all');
      } else {
        caseTableRef.value?.refresh();
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
