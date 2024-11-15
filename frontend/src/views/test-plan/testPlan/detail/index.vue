<template>
  <MsCard
    :loading="loading"
    :header-min-width="1100"
    :min-width="150"
    auto-height
    hide-footer
    no-content-padding
    hide-divider
    hide-back
  >
    <template #headerLeft>
      <MsStatusTag :status="countDetail.status" />
      <a-tooltip :content="`[${detail.num}]${detail.name}`">
        <div class="one-line-text ml-[8px] max-w-[360px] gap-[4px] font-medium text-[var(--color-text-1)]">
          <span>[{{ detail.num }}]</span>
          {{ detail.name }}
        </div>
      </a-tooltip>
    </template>
    <template #headerRight>
      <MsButton v-if="isEnableEdit" type="button" status="default" @click="editorCopyHandler(false)">
        <MsIcon type="icon-icon_edit_outlined" class="mr-[8px]" />
        {{ t('common.edit') }}
      </MsButton>
      <MsTableMoreAction
        v-if="countDetail.status !== 'ARCHIVED'"
        :list="reportMoreAction"
        @select="handleMoreReportSelect"
      >
        <MsButton v-if="hasAnyPermission(['PROJECT_TEST_PLAN:READ+EXECUTE'])" type="button" status="default">
          <MsIcon type="icon-icon_generate_report" class="mr-[8px]" />
          {{ t('testPlan.testPlanDetail.generateReport') }}
        </MsButton>
      </MsTableMoreAction>
      <MsButton
        v-if="hasAnyPermission(['PROJECT_TEST_PLAN:READ+ADD']) && countDetail.status !== 'ARCHIVED'"
        type="button"
        status="default"
        :loading="copyLoading"
        @click="copyHandler"
      >
        <MsIcon type="icon-icon_copy_outlined" class="mr-[8px]" />
        {{ t('common.copy') }}
      </MsButton>
      <MsButton v-if="isEnableEdit" type="button" status="default" :loading="followLoading" @click="followHandler">
        <MsIcon
          :type="detail.followFlag ? 'icon-icon_collect_filled' : 'icon-icon_collection_outlined'"
          :class="`mr-[8px] ${detail.followFlag ? 'text-[rgb(var(--warning-6))]' : ''}`"
        />
        {{ t(detail.followFlag ? 'common.forked' : 'common.fork') }}
      </MsButton>
      <MsButton v-if="countDetail.status === 'ARCHIVED'" status="danger" type="button" @click="deleteHandler">
        <MsIcon type="icon-icon_delete-trash_outlined1" class="mr-[8px] text-[rgb(var(--danger-6))]" />
        <span class="text-[rgb(var(--danger-6))]"> {{ t('common.delete') }}</span>
      </MsButton>
      <MsTableMoreAction v-else :list="moreAction" @select="handleMoreSelect">
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
        <StatusProgress
          :type="testPlanTypeEnum.TEST_PLAN"
          :status-detail="countDetail"
          height="8px"
          radius="var(--border-radius-mini)"
        />
      </div>
    </template>
    <MsTab
      v-model:active-key="activeTab"
      :get-text-func="getTabBadge"
      :content-tab-list="tabList"
      :change-interceptor="changeTabInterceptor"
      no-content
      class="relative mx-[16px]"
    />
  </MsCard>
  <MsCard class="mt-[16px]" simple has-breadcrumb no-content-padding>
    <Plan
      v-if="activeTab === 'plan'"
      :plan-id="planId"
      :status="countDetail.status || 'PREPARED'"
      @refresh="initDetail"
    />
    <FeatureCase
      v-else-if="activeTab === 'featureCase'"
      :can-edit="countDetail.status !== 'ARCHIVED'"
      @refresh="initDetail"
    />
    <BugManagement
      v-else-if="activeTab === 'defectList'"
      :can-edit="countDetail.status !== 'ARCHIVED'"
      @refresh="initDetail"
    />
    <ApiCase v-else-if="activeTab === 'apiCase'" :can-edit="countDetail.status !== 'ARCHIVED'" @refresh="initDetail" />
    <ApiScenario
      v-else-if="activeTab === 'apiScenario'"
      :can-edit="countDetail.status !== 'ARCHIVED'"
      @refresh="initDetail"
    />
    <ExecuteHistory v-else-if="activeTab === 'executeHistory'" />
  </MsCard>
  <CreateAndEditPlanDrawer
    v-model:visible="showPlanDrawer"
    :plan-id="planId"
    :module-tree="testPlanTree"
    @load-plan-list="successHandler"
  />
  <ActionModal
    v-model:visible="showStatusDeleteModal"
    :schedule-config="countDetail.scheduleConfig"
    :record="activeRecord"
    @success="okHandler"
  />
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue';
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
  import StatusProgress from '../components/statusProgress.vue';
  import ApiCase from './apiCase/index.vue';
  import ApiScenario from './apiScenario/index.vue';
  import BugManagement from './bugManagement/index.vue';
  import ExecuteHistory from './executeHistory/index.vue';
  import FeatureCase from './featureCase/index.vue';
  import Plan from './plan/index.vue';
  import CreateAndEditPlanDrawer from '@/views/test-plan/testPlan/createAndEditPlanDrawer.vue';

  import { getBugList } from '@/api/modules/bug-management';
  import {
    archivedPlan,
    followPlanRequest,
    generateReport,
    getPlanPassRate,
    getTestPlanDetail,
    getTestPlanModule,
    testPlanAndGroupCopy,
  } from '@/api/modules/test-plan/testPlan';
  import { defaultDetailCount, testPlanDefaultDetail } from '@/config/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import useAppStore from '@/store/modules/app';
  import useCacheStore from '@/store/modules/cache/cache';
  import useMinderStore from '@/store/modules/components/minder-editor';
  import useUserStore from '@/store/modules/user';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { ModuleTreeNode } from '@/models/common';
  import type { PassRateCountDetail, TestPlanDetail, TestPlanItem } from '@/models/testPlan/testPlan';
  import { TestPlanRouteEnum } from '@/enums/routeEnum';
  import { testPlanTypeEnum } from '@/enums/testPlanEnum';

  defineOptions({
    name: TestPlanRouteEnum.TEST_PLAN_INDEX_DETAIL,
  });
  const cacheStore = useCacheStore();

  const userStore = useUserStore();
  const appStore = useAppStore();
  const { openModal } = useModal();
  const { t } = useI18n();
  const { openNewPage } = useOpenNewPage();

  const route = useRoute();
  const router = useRouter();
  const minderStore = useMinderStore();

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
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const createdBugCount = ref<number>(0);

  async function initBugList() {
    if (!hasAnyPermission(['PROJECT_BUG:READ'])) {
      return;
    }
    const res = await getBugList({
      current: 1,
      pageSize: 10,
      sort: {},
      filter: {},
      keyword: '',
      combine: {},
      searchMode: 'AND',
      projectId: appStore.currentProjectId,
    });
    createdBugCount.value = res.total || 0;
  }
  provide('existedDefect', createdBugCount);

  async function initDetail() {
    try {
      loading.value = true;
      detail.value = await getTestPlanDetail(planId.value);
      getStatistics();
      initBugList();
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
    if (countDetail.value.status === 'COMPLETED') {
      return [...fullActions];
    }
    return fullActions.filter((e) => e.eventTag !== 'archive');
  });

  const isEnableEdit = computed(() => {
    return hasAnyPermission(['PROJECT_TEST_PLAN:READ+UPDATE']) && countDetail.value.status !== 'ARCHIVED';
  });

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
          // eslint-disable-next-line no-console
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
    activeRecord.value.status = countDetail.value.status;
    showStatusDeleteModal.value = true;
  }

  // 删除或者删除弹窗归档成功
  function okHandler(isDelete: boolean) {
    if (isDelete) {
      router.push({
        name: TestPlanRouteEnum.TEST_PLAN_INDEX,
      });
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

  const activeTab = ref('plan');
  watch(
    () => detail.value,
    () => {
      const { functionalCaseCount, apiCaseCount, apiScenarioCount } = detail.value || {};
      if (
        (!functionalCaseCount && activeTab.value === 'featureCase') ||
        (!apiCaseCount && activeTab.value === 'apiCase') ||
        (!apiScenarioCount && activeTab.value === 'apiScenario')
      ) {
        activeTab.value = 'plan';
      }
    }
  );
  const tabList = computed(() => {
    return [
      {
        value: 'plan',
        label: t('testPlan.plan'), // 测试规划
        show: true,
      },
      {
        value: 'featureCase',
        label: t('menu.caseManagement.featureCase'), // 功能用例
        show: detail.value.functionalCaseCount,
      },
      {
        value: 'apiCase',
        label: t('testPlan.testPlanIndex.apiCase'),
        show: detail.value?.apiCaseCount,
      },
      {
        value: 'apiScenario',
        label: t('caseManagement.featureCase.sceneCase'),
        show: detail.value?.apiScenarioCount,
      },
      {
        value: 'defectList',
        label: t('caseManagement.featureCase.defectList'), // 缺陷列表
        show: true,
      },
      {
        value: 'executeHistory',
        label: t('testPlan.featureCase.executionHistory'), // 执行历史
        show: true,
      },
    ].filter((tab) => tab.show); // 过滤掉不显示的 tab
  });
  function getTabBadge(tabKey: string) {
    switch (tabKey) {
      case 'featureCase':
        const count = detail.value.functionalCaseCount ?? 0;
        return `${count > 0 ? count : ''}`;
      case 'defectList':
        const bugCount = detail.value.bugCount ?? 0;
        return `${bugCount > 0 ? bugCount : ''}`;
      case 'apiCase':
        const apiCaseCount = detail.value?.apiCaseCount ?? 0;
        return `${apiCaseCount > 0 ? apiCaseCount : ''}`;
      case 'apiScenario':
        const apiScenarioCount = detail.value?.apiScenarioCount ?? 0;
        return `${apiScenarioCount > 0 ? apiScenarioCount : ''}`;
      default:
        return '';
    }
  }

  const showPlanDrawer = ref(false);

  // 生成报告
  async function handleGenerateReport() {
    try {
      loading.value = true;
      const reportId = await generateReport({
        projectId: appStore.currentProjectId,
        testPlanId: detail.value.id as string,
        triggerMode: 'MANUAL',
      });
      openNewPage(TestPlanRouteEnum.TEST_PLAN_REPORT_DETAIL, {
        id: reportId,
        type: testPlanTypeEnum.TEST_PLAN,
      });
      Message.success(t('testPlan.testPlanDetail.successfullyGenerated'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }
  // 自定义报告
  function configReportHandler() {
    try {
      router.push({
        name: TestPlanRouteEnum.TEST_PLAN_INDEX_CONFIG,
        query: {
          id: detail.value.id,
          type: detail.value.type === testPlanTypeEnum.GROUP ? 'GROUP' : 'TEST_PLAN',
        },
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const reportMoreAction: ActionsItem[] = [
    {
      label: t('testPlan.planAutomaticGeneration'),
      eventTag: 'autoGeneration',
      permission: ['PROJECT_TEST_PLAN:READ+EXECUTE'],
    },
    {
      label: t('testPlan.planConfigReport'),
      eventTag: 'configReport',
      permission: ['PROJECT_TEST_PLAN:READ+EXECUTE'],
    },
  ];

  function handleMoreReportSelect(item: ActionsItem) {
    switch (item.eventTag) {
      case 'autoGeneration':
        handleGenerateReport();
        break;
      case 'configReport':
        configReportHandler();
        break;
      default:
        break;
    }
  }

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
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  function changeTabInterceptor(newVal: string | number, oldVal: string | number, done: () => void) {
    if (oldVal === 'plan' && minderStore.minderUnsaved) {
      openModal({
        type: 'warning',
        title: t('common.tip'),
        content: t('ms.minders.leaveUnsavedTip'),
        okText: t('common.confirm'),
        cancelText: t('common.cancel'),
        okButtonProps: {
          status: 'normal',
        },
        onBeforeOk: async () => {
          done();
        },
        hideCancel: false,
      });
      return;
    }
    done();
  }

  // 复制
  const copyLoading = ref<boolean>(false);
  async function copyHandler() {
    copyLoading.value = true;
    try {
      const res = await testPlanAndGroupCopy(route.query.id as string);
      Message.success(t('common.copySuccess'));
      router.push({
        name: TestPlanRouteEnum.TEST_PLAN_INDEX_DETAIL,
        query: {
          id: res.operationId,
        },
      });
      initDetail();
      initPlanTree();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      copyLoading.value = false;
    }
  }

  watch(
    () => activeTab.value,
    (val) => {
      if (val) {
        initDetail();
      }
    }
  );
  const isActivated = computed(() => cacheStore.cacheViews.includes(TestPlanRouteEnum.TEST_PLAN_INDEX_DETAIL));

  provide('isActivated', isActivated);

  onBeforeMount(() => {
    if (!isActivated.value) {
      if (route.query.type === 'featureCase') {
        activeTab.value = 'featureCase';
      }
      initDetail();
      initPlanTree();
    }
  });

  onActivated(() => {
    if (isActivated.value) {
      if (route.query.type === 'featureCase') {
        activeTab.value = 'featureCase';
      }
      initDetail();
      initPlanTree();
    }
  });
</script>

<style lang="less" scoped>
  :deep(.arco-tabs-content) {
    @apply hidden;
  }
  :deep(.arco-table-tr) {
    .operation-button {
      opacity: 0;
    }
    &:hover {
      .operation-button {
        opacity: 1;
      }
    }
  }
</style>
