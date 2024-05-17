<template>
  <MsCard
    :loading="loading"
    :header-min-width="1100"
    :min-width="150"
    auto-height
    hide-footer
    no-content-padding
    hide-divider
  >
    <template #headerLeft>
      <MsStatusTag :status="detail.status || 'PREPARED'" />
      <a-tooltip :content="`[${detail.num}]${detail.name}`">
        <div class="one-line-text ml-[4px] max-w-[360px] gap-[4px] font-medium text-[var(--color-text-1)]">
          <span>[{{ detail.num }}]</span>
          {{ detail.name }}
        </div>
      </a-tooltip>
    </template>
    <template #headerRight>
      <MsButton v-permission="['PROJECT_TEST_PLAN:READ+ASSOCIATION']" type="button" status="default" @click="linkCase">
        <MsIcon type="icon-icon_link-record_outlined1" class="mr-[8px]" />
        {{ t('ms.case.associate.title') }}
      </MsButton>
      <MsButton
        v-permission="['PROJECT_TEST_PLAN:READ+UPDATE']"
        type="button"
        status="default"
        @click="editorCopyHandler(false)"
      >
        <MsIcon type="icon-icon_edit_outlined" class="mr-[8px]" />
        {{ t('common.edit') }}
      </MsButton>
      <MsButton
        v-permission="['PROJECT_TEST_PLAN:READ+ADD']"
        type="button"
        status="default"
        @click="editorCopyHandler(true)"
      >
        <MsIcon type="icon-icon_copy_outlined" class="mr-[8px]" />
        {{ t('common.copy') }}
      </MsButton>
      <MsButton
        v-permission="['PROJECT_TEST_PLAN:READ+UPDATE']"
        type="button"
        status="default"
        :loading="followLoading"
        @click="followHandler"
      >
        <MsIcon
          :type="detail.followFlag ? 'icon-icon_collect_filled' : 'icon-icon_collection_outlined'"
          :class="`mr-[8px] ${detail.followFlag ? 'text-[rgb(var(--warning-6))]' : ''}`"
        />
        {{ t(detail.followFlag ? 'common.forked' : 'common.fork') }}
      </MsButton>
      <MsTableMoreAction :list="moreAction" @select="handleMoreSelect">
        <MsButton v-permission="['PROJECT_TEST_PLAN:READ+DELETE']" type="button" status="default">
          <MsIcon type="icon-icon_more_outlined" class="mr-[8px]" />
          {{ t('common.more') }}
        </MsButton>
      </MsTableMoreAction>
    </template>
    <template #subHeader>
      <div class="mt-[16px] w-[476px]">
        <div class="mb-[8px] flex items-center gap-[24px] text-[12px]">
          <div class="text-[var(--color-text-4)]">
            <span class="mr-[8px]">{{ t('testPlan.testPlanDetail.executed') }}</span>
            <span>
              <span class="mr-1 font-medium text-[var(--color-text-1)]"> {{ hasExecutedCount }} </span>/<span
                class="ml-1"
                >{{ countDetail.caseTotal }}</span
              >
            </span>
          </div>
          <div class="text-[var(--color-text-4)]">
            <span class="mr-[8px]">{{ t('caseManagement.caseReview.passRate') }}</span>
            <span>
              <span class="font-medium text-[var(--color-text-1)]"> {{ countDetail.passRate }}% </span>
            </span>
          </div>
        </div>
        <StatusProgress :status-detail="countDetail" height="8px" radius="var(--border-radius-mini)" />
      </div>
    </template>
    <MsTab
      v-model:active-key="activeTab"
      :get-text-func="getTabBadge"
      :content-tab-list="tabList"
      no-content
      class="relative mx-[16px] border-b"
    />
  </MsCard>
  <!-- special-height的174: 上面卡片高度158 + mt的16 -->
  <MsCard class="mt-[16px]" :special-height="174" simple has-breadcrumb no-content-padding>
    <FeatureCase v-if="activeTab === 'featureCase'" :repeat-case="detail.repeatCase" @refresh="getStatistics" />
    <!-- TODO 先不上 -->
    <!-- <BugManagement v-if="activeTab === 'defectList'" :plan-id="detail.id" /> -->
  </MsCard>
  <AssociateDrawer v-model:visible="caseAssociateVisible" :associated-ids="hasSelectedIds" @success="success" />
  <CreateAndEditPlanDrawer
    v-model:visible="showPlanDrawer"
    :plan-id="planId"
    :is-copy="isCopy"
    :module-tree="testPlanTree"
    @load-plan-list="successHandler"
  />
  <ActionModal v-model:visible="showStatusDeleteModal" :record="activeRecord" @success="okHandler" />
</template>

<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTab from '@/components/pure/ms-tab/index.vue';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsStatusTag from '@/components/business/ms-status-tag/index.vue';
  import ActionModal from '../components/actionModal.vue';
  import AssociateDrawer from '../components/associateDrawer.vue';
  import StatusProgress from '../components/statusProgress.vue';
  import BugManagement from './bugManagement/index.vue';
  import FeatureCase from './featureCase/index.vue';
  import CreateAndEditPlanDrawer from '@/views/test-plan/testPlan/createAndEditPlanDrawer.vue';

  import {
    archivedPlan,
    associationCaseToPlan,
    followPlanRequest,
    getPlanPassRate,
    getTestPlanDetail,
    getTestPlanModule,
  } from '@/api/modules/test-plan/testPlan';
  import { defaultDetailCount, testPlanDefaultDetail } from '@/config/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useAppStore from '@/store/modules/app';
  import useUserStore from '@/store/modules/user';
  import { characterLimit } from '@/utils';

  import { ModuleTreeNode } from '@/models/common';
  import type {
    AssociateCaseRequest,
    PassRateCountDetail,
    TestPlanDetail,
    TestPlanItem,
  } from '@/models/testPlan/testPlan';

  const userStore = useUserStore();
  const appStore = useAppStore();
  const { openModal } = useModal();
  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const loading = ref(false);
  const planId = ref(route.query.id as string);
  const detail = ref<TestPlanDetail>({
    ...testPlanDefaultDetail,
  });

  const countDetail = ref<PassRateCountDetail>({ ...defaultDetailCount });

  const hasExecutedCount = computed(() => {
    const { successCount, fakeErrorCount, errorCount, blockCount } = countDetail.value;
    return successCount + fakeErrorCount + errorCount + blockCount;
  });
  // 初始化统计
  async function getStatistics() {
    try {
      const result = await getPlanPassRate([planId.value]);
      // eslint-disable-next-line prefer-destructuring
      countDetail.value = result[0];
    } catch (error) {
      console.log(error);
    }
  }
  async function initDetail() {
    try {
      loading.value = true;
      detail.value = await getTestPlanDetail(planId.value);
      getStatistics();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  const fullActions = [
    {
      label: t('common.archive'),
      eventTag: 'archive',
      permission: ['PROJECT_TEST_PLAN:READ+UPDATE'],
    },
    {
      isDivider: true,
    },
    {
      label: t('common.delete'),
      eventTag: 'delete',
      danger: true,
      permission: ['PROJECT_TEST_PLAN:READ+DELETE'],
    },
  ];
  const moreAction = computed(() => {
    if (detail.value.status === 'COMPLETED') {
      return [...fullActions];
    }
    return fullActions.filter((e) => e.eventTag !== 'archive');
  });

  function getTabBadge(tabKey: string) {
    switch (tabKey) {
      case 'featureCase':
        const count = detail.value.functionalCaseCount ?? 0;
        return `${count > 0 ? count : ''}`;
      default:
        return '';
    }
  }

  function archiveHandler() {
    openModal({
      type: 'warning',
      title: t('common.archiveConfirmTitle', { name: characterLimit(detail.value.name) }),
      content: t('testPlan.testPlanIndex.confirmArchivePlan'),
      okText: t('common.archive'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'normal',
      },
      onBeforeOk: async () => {
        try {
          await archivedPlan(planId.value);
          Message.success(t('common.batchArchiveSuccess'));
          initDetail();
        } catch (error) {
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }
  const showStatusDeleteModal = ref<boolean>(false);
  const activeRecord = ref<TestPlanItem | TestPlanDetail | undefined>();
  // 删除
  function deleteHandler() {
    activeRecord.value = cloneDeep(detail.value);
    showStatusDeleteModal.value = true;
  }

  // 删除或者删除弹窗归档成功
  function okHandler(isDelete: boolean) {
    if (isDelete) {
      router.back();
    } else {
      initDetail();
    }
  }

  function handleMoreSelect(item: ActionsItem) {
    switch (item.eventTag) {
      case 'archive':
        archiveHandler();
        break;
      case 'delete':
        deleteHandler();
        break;
      default:
        break;
    }
  }

  const activeTab = ref('featureCase');
  const tabList = ref([
    {
      value: 'featureCase',
      label: t('menu.caseManagement.featureCase'),
    },
    // TODO 先不上
    // {
    //   key: 'defectList',
    //   title: t('caseManagement.featureCase.defectList'),
    // },
  ]);
  const hasSelectedIds = ref<string[]>([]);
  const caseAssociateVisible = ref(false);
  // 关联用例
  function linkCase() {
    caseAssociateVisible.value = true;
  }
  const showPlanDrawer = ref(false);

  // 更新 | 复制
  const isCopy = ref<boolean>(false);
  function editorCopyHandler(copyFlog: boolean) {
    isCopy.value = copyFlog;
    showPlanDrawer.value = true;
  }

  const followLoading = ref<boolean>(false);
  // 关注
  async function followHandler() {
    try {
      followLoading.value = true;
      await followPlanRequest({
        userId: userStore.id || '',
        testPlanId: detail.value.id as string,
      });
      Message.success(
        detail.value.followFlag
          ? t('caseManagement.caseReview.unFollowSuccess')
          : t('caseManagement.caseReview.followSuccess')
      );
      detail.value.followFlag = !detail.value.followFlag;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      followLoading.value = false;
    }
  }

  function successHandler() {
    initDetail();
  }
  const testPlanTree = ref<ModuleTreeNode[]>([]);
  async function initPlanTree() {
    try {
      testPlanTree.value = await getTestPlanModule({ projectId: appStore.currentProjectId });
    } catch (error) {
      console.log(error);
    }
  }

  // 关联用例到测试计划
  async function success(params: AssociateCaseRequest) {
    try {
      await associationCaseToPlan({
        functionalSelectIds: params.selectIds,
        testPlanId: planId.value,
      });
      Message.success(t('ms.case.associate.associateSuccess'));
      caseAssociateVisible.value = false;
      initDetail();
    } catch (error) {
      console.log(error);
    }
  }

  onBeforeMount(() => {
    initDetail();
    initPlanTree();
  });
</script>

<style lang="less" scoped>
  :deep(.arco-tabs-content) {
    @apply hidden;
  }
</style>
