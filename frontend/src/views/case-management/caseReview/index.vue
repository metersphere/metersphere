<template>
  <MsCard simple no-content-padding>
    <div class="flex items-center justify-between border-b border-[var(--color-text-n8)] p-[24px_24px_16px_24px]">
      <a-button type="primary" @click="goCreateReview">{{ t('caseManagement.caseReview.create') }}</a-button>
      <a-radio-group v-model:model-value="showType" type="button" class="file-show-type">
        <a-radio value="all">{{ t('common.all') }}</a-radio>
        <a-radio value="reviewByMe">{{ t('caseManagement.caseReview.waitMyReview') }}</a-radio>
        <a-radio value="createByMe">{{ t('caseManagement.caseReview.myCreate') }}</a-radio>
      </a-radio-group>
    </div>
    <div class="relative h-[calc(100%-73px)]">
      <MsSplitBox>
        <template #first>
          <div class="px-[24px] py-[16px]">
            <ModuleTree
              ref="folderTreeRef"
              :show-type="showType"
              :modules-count="modulesCount"
              @folder-node-select="handleFolderNodeSelect"
              @init="initModuleTree"
            />
          </div>
        </template>
        <template #second>
          <ReviewTable
            :active-folder="activeFolderId"
            :module-tree="moduleTree"
            :tree-path-map="moduleTreePathMap"
            :show-type="showType"
            :offspring-ids="offspringIds"
            @go-create="goCreateReview"
            @init="initModuleCount"
          />
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

  import { reviewModuleCount } from '@/api/modules/case-management/caseReview';
  import { useI18n } from '@/hooks/useI18n';

  import { ReviewListQueryParams } from '@/models/caseManagement/caseReview';
  import { ModuleTreeNode } from '@/models/common';
  import { CaseManagementRouteEnum } from '@/enums/routeEnum';

  const router = useRouter();
  const { t } = useI18n();

  type ShowType = 'all' | 'reviewByMe' | 'createByMe';

  const showType = ref<ShowType>('all');

  const folderTreeRef = ref<InstanceType<typeof ModuleTree>>();
  const activeFolderId = ref<string>('all');
  const offspringIds = ref<string[]>([]);
  const moduleTree = ref<ModuleTreeNode[]>([]);
  const moduleTreePathMap = ref<Record<string, any>>({});
  const modulesCount = ref<Record<string, number>>({});

  function initModuleTree(tree: ModuleTreeNode[], pathMap: Record<string, any>) {
    moduleTree.value = unref(tree);
    moduleTreePathMap.value = pathMap;
  }

  function handleFolderNodeSelect(ids: string[], _offspringIds: string[]) {
    [activeFolderId.value] = ids;
    offspringIds.value = [..._offspringIds];
  }

  async function initModuleCount(params: ReviewListQueryParams) {
    try {
      const res = await reviewModuleCount(params);
      modulesCount.value = res;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
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
