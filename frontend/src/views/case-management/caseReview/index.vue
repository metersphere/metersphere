<template>
  <MsCard simple no-content-padding>
    <MsSplitBox :not-show-first="isAdvancedSearchMode">
      <template #first>
        <div class="p-[16px]">
          <ModuleTree
            ref="folderTreeRef"
            :show-type="showType"
            :modules-count="modulesCount"
            :is-expand-all="true"
            @folder-node-select="handleFolderNodeSelect"
            @init="initModuleTree"
            @create="goCreateReview"
            @node-delete="handleModuleTreeChange"
            @node-drop="handleModuleTreeChange"
          />
        </div>
      </template>
      <template #second>
        <ReviewTable
          ref="reviewTableRef"
          v-model:show-type="showType"
          :active-folder="activeFolderId"
          :module-tree="moduleTree"
          :tree-path-map="moduleTreePathMap"
          :offspring-ids="offspringIds"
          @go-create="goCreateReview"
          @init="initModuleCount"
          @handle-adv-search="handleAdvSearch"
        />
      </template>
    </MsSplitBox>
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

  import { ReviewListQueryParams } from '@/models/caseManagement/caseReview';
  import { ModuleTreeNode } from '@/models/common';
  import { CaseManagementRouteEnum } from '@/enums/routeEnum';

  defineOptions({
    name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW,
  });

  const router = useRouter();

  type ShowType = 'all' | 'reviewByMe' | 'createByMe';

  const showType = ref<ShowType>('all');

  const folderTreeRef = ref<InstanceType<typeof ModuleTree>>();
  const activeFolderId = ref<string>('all');
  const offspringIds = ref<string[]>([]);
  const moduleTree = ref<ModuleTreeNode[]>([]);
  const moduleTreePathMap = ref<Record<string, any>>({});
  const modulesCount = ref<Record<string, number>>({});
  const reviewTableRef = ref<InstanceType<typeof ReviewTable>>();

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

  function handleModuleTreeChange() {
    reviewTableRef.value?.searchReview();
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

  const isAdvancedSearchMode = computed(() => reviewTableRef.value?.isAdvancedSearchMode);
  function handleAdvSearch() {
    folderTreeRef.value?.setActiveFolder('all');
  }
</script>

<style lang="less" scoped></style>
