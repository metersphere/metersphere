<template>
  <MsCard simple no-content-padding>
    <div class="flex items-center justify-between border-b border-[var(--color-text-n8)] p-[24px_24px_16px_24px]">
      <a-button type="primary" @click="goCreateReview">{{ t('caseManagement.caseReview.create') }}</a-button>
      <a-radio-group v-model:model-value="showType" type="button" class="file-show-type" @change="changeShowType">
        <a-radio value="all">{{ t('common.all') }}</a-radio>
        <a-radio value="wait">{{ t('caseManagement.caseReview.waitMyReview') }}</a-radio>
        <a-radio value="create">{{ t('caseManagement.caseReview.myCreate') }}</a-radio>
      </a-radio-group>
    </div>
    <div class="relative h-[calc(100%-73px)]">
      <MsSplitBox>
        <template #left>
          <div class="px-[24px] py-[16px]">
            <ModuleTree ref="folderTreeRef" @folder-node-select="handleFolderNodeSelect" @init="initModuleTree" />
          </div>
        </template>
        <template #right>
          <ReviewTable :active-folder="activeFolderId" :module-tree="moduleTree" />
        </template>
      </MsSplitBox>
    </div>
  </MsCard>
</template>

<script setup lang="ts">
  /**
   * @description 功能测试-用例评审-评审列表
   */
  import { useRouter } from 'vue-router';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import ModuleTree from './components/index/moduleTree.vue';
  import ReviewTable from './components/index/reviewTable.vue';

  import { useI18n } from '@/hooks/useI18n';

  import type { ModuleTreeNode } from '@/models/projectManagement/file';
  import { CaseManagementRouteEnum } from '@/enums/routeEnum';

  const router = useRouter();
  const { t } = useI18n();

  type ShowType = 'all' | 'wait' | 'create';

  const showType = ref<ShowType>('all');

  function changeShowType(val: string | number | boolean) {
    console.log('changeShowType', val);
  }

  const folderTreeRef = ref<InstanceType<typeof ModuleTree>>();
  const activeFolderId = ref<string | number>('all');
  const moduleTree = ref<ModuleTreeNode[]>([]);

  function initModuleTree(tree: ModuleTreeNode[]) {
    moduleTree.value = unref(tree);
  }

  function handleFolderNodeSelect(ids: (string | number)[]) {
    [activeFolderId.value] = ids;
  }

  function goCreateReview() {
    router.push({
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_CREATE,
      query:
        activeFolderId.value === 'all'
          ? {}
          : {
              moduleId: activeFolderId.value,
            },
    });
  }
</script>

<style lang="less" scoped></style>
