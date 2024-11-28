<template>
  <MsCard
    :loading="loading"
    :header-min-width="1100"
    :min-width="150"
    auto-height
    hide-footer
    no-bottom-radius
    no-content-padding
    hide-divider
    :handle-back="backToTable"
  >
    <template #headerLeft>
      <a-tooltip :content="reviewDetail.name">
        <div class="one-line-text mr-[8px] max-w-[300px] font-medium text-[var(--color-text-1)]">
          {{ reviewDetail.name }}
        </div>
      </a-tooltip>
      <div
        class="rounded-[0_999px_999px_0] border border-solid border-[text-[rgb(var(--primary-5))]] px-[8px] py-[2px] text-[12px] leading-[16px] text-[rgb(var(--primary-5))]"
      >
        <MsIcon type="icon-icon-contacts" size="13" />
        {{
          reviewDetail.reviewPassRule === 'SINGLE'
            ? t('caseManagement.caseReview.single')
            : t('caseManagement.caseReview.multi')
        }}
      </div>
      <MsStatusTag :status="(reviewDetail.status as ReviewStatus)" class="mx-[16px]" />
    </template>
    <template #headerRight>
      <MsButton
        v-permission="['CASE_REVIEW:READ+UPDATE']"
        type="button"
        status="default"
        @click="associateDrawerVisible = true"
      >
        <MsIcon type="icon-icon_link-record_outlined" class="mr-[8px]" />
        {{ t('ms.case.associate.title') }}
      </MsButton>
      <MsButton v-permission="['CASE_REVIEW:READ+UPDATE']" type="button" status="default" @click="editReview">
        <MsIcon type="icon-icon_edit_outlined" class="mr-[8px]" />
        {{ t('common.edit') }}
      </MsButton>
      <MsButton v-permission="['CASE_REVIEW:READ+ADD']" type="button" status="default" @click="copyReview">
        <MsIcon type="icon-icon_copy_outlined" class="mr-[8px]" />
        {{ t('common.copy') }}
      </MsButton>
      <MsButton
        v-permission="['CASE_REVIEW:READ+UPDATE']"
        type="button"
        status="default"
        :loading="followLoading"
        @click="toggleFollowReview"
      >
        <MsIcon
          :type="reviewDetail.followFlag ? 'icon-icon_collect_filled' : 'icon-icon_collection_outlined'"
          :class="`mr-[8px] ${reviewDetail.followFlag ? 'text-[rgb(var(--warning-6))]' : ''}`"
        />
        {{ t(reviewDetail.followFlag ? 'common.forked' : 'common.fork') }}
      </MsButton>
      <MsTableMoreAction :list="moreAction" @select="handleMoreSelect">
        <MsButton v-permission="['FUNCTIONAL_CASE:READ+ADD', 'CASE_REVIEW:READ+DELETE']" type="button" status="default">
          <MsIcon type="icon-icon_more_outlined" class="mr-[8px]" />
          {{ t('common.more') }}
        </MsButton>
      </MsTableMoreAction>
    </template>
    <template #subHeader>
      <div class="mt-[16px] w-[476px]">
        <div class="mb-[4px] flex items-center gap-[24px]">
          <div class="text-[var(--color-text-4)]">
            <span class="mr-[8px]">{{ t('caseManagement.caseReview.reviewedCase') }}</span>
            <span v-if="reviewDetail.status === 'PREPARED'" class="text-[var(--color-text-1)]">-</span>
            <span v-else>
              <span class="text-[var(--color-text-1)]"> {{ reviewDetail.reviewedCount }}/ </span
              >{{ reviewDetail.caseCount }}
            </span>
          </div>
          <div class="text-[var(--color-text-4)]">
            <span class="mr-[8px]">{{ t('caseManagement.caseReview.passRate') }}</span>
            <span v-if="reviewDetail.status === 'PREPARED'" class="text-[var(--color-text-1)]">-</span>
            <span v-else>
              <span class="text-[var(--color-text-1)]"> {{ reviewDetail.passRate }}% </span>
            </span>
          </div>
        </div>
        <passRateLine
          ref="passRateLineRef"
          :review-detail="reviewDetail"
          height="8px"
          radius="var(--border-radius-mini)"
        />
      </div>
    </template>
    <!-- <div class="px-[24px]">
      <a-divider class="my-0" />
      <a-tabs v-model:active-key="showTab" class="no-content">
        <a-tab-pane v-for="item of tabList" :key="item.key" :title="item.title" />
      </a-tabs>
    </div> -->
  </MsCard>
  <!-- special-height的170: 上面卡片高度105 + mt的16 -->
  <MsCard class="mt-[16px]" :special-height="121" simple has-breadcrumb no-content-padding>
    <MsSplitBox :not-show-first="isAdvancedSearchMode">
      <template #first>
        <div class="p-[16px]">
          <CaseTree
            ref="folderTreeRef"
            :modules-count="modulesCount"
            :selected-keys="selectedKeys"
            @folder-node-select="handleFolderNodeSelect"
          />
        </div>
      </template>
      <template #second>
        <CaseTable
          ref="caseTableRef"
          :active-folder="activeFolderId"
          :review-pass-rule="reviewDetail.reviewPassRule"
          :offspring-ids="offspringIds"
          :modules-count="modulesCount"
          :review-progress="reviewProgress"
          @refresh="initDetail()"
          @link="associateDrawerVisible = true"
          @select-parent-node="selectParentNode"
          @handle-adv-search="handleAdvSearch"
        ></CaseTable>
      </template>
    </MsSplitBox>
  </MsCard>
  <AssociateDrawer
    v-model:visible="associateDrawerVisible"
    v-model:project="associateDrawerProject"
    :review-id="reviewId"
    :reviewers="reviewDetail.reviewers.map((e) => e.userId)"
    @success="writeAssociateCases"
  />
  <deleteReviewModal v-model:visible="deleteModalVisible" :record="reviewDetail" @success="handleDeleteSuccess" />
</template>

<script setup lang="ts">
  /**
   * @description 功能测试-用例评审-评审详情
   */
  import { useRoute, useRouter } from 'vue-router';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsStatusTag from '@/components/business/ms-status-tag/index.vue';
  import AssociateDrawer from './components/create/associateDrawer.vue';
  import CaseTable from './components/detail/caseTable.vue';
  import CaseTree from './components/detail/caseTree.vue';
  import deleteReviewModal from './components/index/deleteReviewModal.vue';
  import passRateLine from './components/passRateLine.vue';

  import { associateReviewCase, followReview, getReviewDetail } from '@/api/modules/case-management/caseReview';
  import { reviewDefaultDetail } from '@/config/caseManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import useCaseReviewStore from '@/store/modules/case/caseReview';
  import useUserStore from '@/store/modules/user';

  import type { BaseAssociateCaseRequest, ReviewItem, ReviewStatus } from '@/models/caseManagement/caseReview';
  import { ModuleTreeNode } from '@/models/common';
  import { CaseManagementRouteEnum } from '@/enums/routeEnum';

  defineOptions({
    name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_DETAIL,
  });
  const router = useRouter();
  const route = useRoute();
  const userStore = useUserStore();
  const appStore = useAppStore();
  const { t } = useI18n();
  const caseReviewStore = useCaseReviewStore();

  const loading = ref(false);
  const reviewDetail = ref<ReviewItem>({
    ...reviewDefaultDetail,
  });
  const reviewId = ref(route.query.id as string);
  const passRateLineRef = ref<InstanceType<typeof passRateLine>>();
  const reviewProgress = computed(() => passRateLineRef.value?.progress ?? '');

  async function initDetail() {
    try {
      loading.value = true;
      reviewDetail.value = await getReviewDetail(reviewId.value);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  // const showTab = ref(0);
  // const tabList = ref([
  //   {
  //     key: 0,
  //     title: t('menu.caseManagement.featureCase'),
  //   },
  // ]);

  const modulesCount = computed(() => caseReviewStore.modulesCount);

  const folderTreeRef = ref<InstanceType<typeof CaseTree>>();
  const activeFolderId = ref<string>('all');
  const offspringIds = ref<string[]>([]);
  const selectedKeys = computed({
    get: () => [activeFolderId.value],
    set: (val) => val,
  });
  const caseTableRef = ref<InstanceType<typeof CaseTable>>();

  const isAdvancedSearchMode = ref(false);
  function handleAdvSearch(isStartAdvance: boolean) {
    isAdvancedSearchMode.value = isStartAdvance;
    folderTreeRef.value?.setActiveFolder('all');
  }

  function selectParentNode(folderTree: ModuleTreeNode[]) {
    folderTreeRef.value?.selectParentNode(folderTree);
  }

  function handleFolderNodeSelect(ids: string[], _offspringIds: string[]) {
    [activeFolderId.value] = ids;
    offspringIds.value = [..._offspringIds];
    caseTableRef.value?.resetSelector();
  }

  const associateDrawerVisible = ref(false);
  const associateDrawerProject = ref(appStore.currentProjectId);

  // 关联用例
  async function writeAssociateCases(params: BaseAssociateCaseRequest & { reviewers: string[] }) {
    try {
      loading.value = true;
      await associateReviewCase({
        reviewId: reviewId.value,
        projectId: appStore.currentProjectId,
        reviewers: params.reviewers,
        baseAssociateCaseRequest: params,
      });
      Message.success(t('caseManagement.caseReview.associateSuccess'));
      await initDetail();
      folderTreeRef.value?.initModules();
      caseTableRef.value?.refresh();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  function editReview() {
    router.push({
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_CREATE,
      query: {
        id: reviewId.value,
      },
    });
  }

  function createCase() {
    router.push({
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE_DETAIL,
      query: {
        reviewId: reviewId.value,
      },
    });
  }

  const deleteModalVisible = ref(false);
  function handleDeleteSuccess() {
    router.back();
  }

  const fullActions = [
    // {
    //   label: t('caseManagement.caseReview.archive'),
    //   eventTag: 'archive',
    //   icon: 'icon-icon-draft',
    // },
    // {
    //   label: t('common.export'),
    //   eventTag: 'export',
    //   icon: 'icon-icon_upload_outlined',
    // },
    {
      label: t('caseManagement.caseReview.quickCreate'),
      eventTag: 'createCase',
      icon: 'icon-icon_add_outlined-1',
      permission: ['FUNCTIONAL_CASE:READ+ADD'],
    },
    // {
    //   label: t('caseManagement.caseReview.createTestPlan'),
    //   eventTag: 'createTestPlan',
    //   icon: 'icon-icon_add_outlined-1',
    // },
    {
      isDivider: true,
    },
    {
      label: t('common.delete'),
      eventTag: 'delete',
      icon: 'icon-icon_delete-trash_outlined1',
      danger: true,
      permission: ['CASE_REVIEW:READ+DELETE'],
    },
  ];
  const moreAction = computed(() => {
    if (reviewDetail.value.status === 'COMPLETED') {
      return [...fullActions];
    }
    // if (reviewDetail.value.status === 'ARCHIVED') {
    //   return fullActions.filter((e) => e.eventTag === 'delete');
    // }
    return fullActions.filter((e) => e.eventTag !== 'archive');
  });

  function handleMoreSelect(item: ActionsItem) {
    switch (item.eventTag) {
      case 'createCase':
        createCase();
        break;
      case 'delete':
        deleteModalVisible.value = true;
        break;
      default:
        break;
    }
  }

  const followLoading = ref(false);
  async function toggleFollowReview() {
    try {
      followLoading.value = true;
      await followReview({
        userId: userStore.id || '',
        caseReviewId: reviewId.value,
      });
      Message.success(
        reviewDetail.value.followFlag
          ? t('caseManagement.caseReview.unFollowSuccess')
          : t('caseManagement.caseReview.followSuccess')
      );
      reviewDetail.value.followFlag = !reviewDetail.value.followFlag;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      followLoading.value = false;
    }
  }

  function backToTable() {
    router.push({
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW,
    });
  }

  function copyReview() {
    router.push({
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_CREATE,
      query: {
        copyId: reviewId.value,
      },
    });
  }

  onMounted(() => {
    initDetail();
  });
</script>

<style lang="less" scoped>
  :deep(.arco-tabs-content) {
    @apply hidden;
  }
</style>
