<template>
  <MsCard :min-width="1100" has-breadcrumb simple no-content-padding>
    <div class="flex h-full w-full">
      <!-- 左侧 -->
      <div class="flex h-full w-[318px] flex-col border-r border-[var(--color-text-n8)] p-[16px]">
        <a-tooltip :content="`[${planDetail.num}]${planDetail.name}`">
          <div class="one-line-text w-full gap-[4px] font-medium">
            <span>[{{ planDetail.num }}]</span>
            {{ planDetail.name }}
          </div>
        </a-tooltip>
        <div class="my-[8px] flex">
          <a-input-search
            v-model:model-value="keyword"
            :placeholder="t('caseManagement.caseReview.searchPlaceholder')"
            allow-clear
            class="mr-[8px] w-[176px]"
            @search="loadCaseList"
            @press-enter="loadCaseList"
            @clear="loadCaseList"
          />
          <a-select
            v-model:model-value="lastExecResult"
            :options="executeResultOptions"
            class="flex-1"
            @change="loadCaseList"
          >
          </a-select>
        </div>
        <a-spin :loading="caseListLoading" class="w-full flex-1 overflow-hidden">
          <div class="case-list">
            <div
              v-for="item of caseList"
              :key="item.id"
              :class="['case-item', caseDetail.id === item.id ? 'case-item--active' : '']"
            >
              <div class="mb-[8px] flex items-center justify-between">
                <div class="text-[var(--color-text-4)]">{{ item.num }}</div>
                <ExecuteResult :execute-result="item.lastExecResult" />
              </div>
              <a-tooltip :content="item.name">
                <div class="one-line-text">{{ item.name }}</div>
              </a-tooltip>
            </div>
            <MsEmpty v-if="caseList.length === 0" />
          </div>
          <!-- TODO 样式 -->
          <MsPagination
            v-model:page-size="pageNation.pageSize"
            v-model:current="pageNation.current"
            :total="pageNation.total"
            size="mini"
            simple
            @change="loadCaseList"
            @page-size-change="loadCaseList"
          />
        </a-spin>
      </div>
      <!-- 右侧 -->
      <a-spin :loading="caseDetailLoading" class="relative flex flex-1 flex-col p-[16px]">
        <div class="flex">
          <div class="mr-[24px] flex flex-1 items-center">
            <MsStatusTag :status="caseDetail.status || 'PREPARED'" />
            <div class="ml-[8px] mr-[2px] font-medium text-[rgb(var(--primary-5))]">[{{ caseDetail.num }}]</div>
            <div class="flex-1 overflow-hidden">
              <a-tooltip :content="caseDetail.name">
                <div class="one-line-text max-w-[100%] font-medium">
                  {{ caseDetail.name }}
                </div>
              </a-tooltip>
            </div>
          </div>
          <a-button type="outline">{{ t('common.edit') }}</a-button>
        </div>
        <MsTab
          v-model:active-key="activeTab"
          :show-badge="false"
          :content-tab-list="contentTabList"
          no-content
          class="relative border-b"
        />
        <div class="tab-content">
          <BugList v-if="activeTab === 'defectList'" :case-id="caseDetail.id" />
          <ExecutionHistory v-if="activeTab === 'executionHistory'" :case-id="caseDetail.id" />
        </div>
      </a-spin>
    </div>
  </MsCard>
</template>

<script setup lang="ts">
  import { useRoute } from 'vue-router';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsEmpty from '@/components/pure/ms-empty/index.vue';
  import MsPagination from '@/components/pure/ms-pagination/index';
  import MsTab from '@/components/pure/ms-tab/index.vue';
  import ExecuteResult from '@/components/business/ms-case-associate/executeResult.vue';
  import MsStatusTag from '@/components/business/ms-status-tag/index.vue';
  import BugList from './bug/index.vue';
  import ExecutionHistory from '@/views/test-plan/testPlan/detail/featureCase/detail/executionHistory/index.vue';

  import { getPlanDetailFeatureCaseList } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { PlanDetailFeatureCaseItem } from '@/models/testPlan/testPlan';

  import { executionResultMap } from '@/views/case-management/caseManagementFeature/components/utils';

  const { t } = useI18n();
  const route = useRoute();
  const appStore = useAppStore();

  // TODO
  const planDetail = ref({ num: '111', name: '222lalallalallalalalal222lalallalallalalalal222lalallalallalalalal' });

  const keyword = ref('');
  const lastExecResult = ref('');
  const executeResultOptions = computed(() => {
    return [
      { label: t('common.all'), value: '' },
      ...Object.keys(executionResultMap).map((key) => {
        return {
          value: key,
          label: executionResultMap[key].statusText,
        };
      }),
    ];
  });
  const caseList = ref<PlanDetailFeatureCaseItem[]>([]);
  const pageNation = ref({
    total: 0,
    pageSize: 10,
    current: 1,
  });
  const otherListQueryParams = ref<Record<string, any>>({});
  const caseListLoading = ref(false);
  // 加载用例列表
  async function loadCaseList() {
    try {
      caseListLoading.value = true;
      const res = await getPlanDetailFeatureCaseList({
        projectId: appStore.currentProjectId,
        testPlanId: route.query.id as string,
        keyword: keyword.value,
        current: pageNation.value.current || 1,
        pageSize: pageNation.value.pageSize,
        filter: lastExecResult.value
          ? {
              lastExecResult: [lastExecResult.value],
            }
          : undefined,
        ...otherListQueryParams.value,
      });
      caseList.value = res.list;
      pageNation.value.total = res.total;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      caseListLoading.value = false;
    }
  }

  const caseDetail = ref<any>({});
  const caseDetailLoading = ref(false);
  const activeTab = ref('detail');
  const contentTabList = ref([
    {
      value: 'baseInfo',
      label: t('common.baseInfo'),
    },
    {
      value: 'detail',
      label: t('common.detail'),
    },
    {
      value: 'defectList',
      label: t('caseManagement.featureCase.defectList'),
    },
    {
      value: 'executionHistory',
      label: t('testPlan.featureCase.executionHistory'),
    },
  ]);

  onBeforeMount(async () => {
    const lastPageParams = window.history.state.params ? JSON.parse(window.history.state.params) : null; // 获取上个页面带过来的表格查询参数
    if (lastPageParams) {
      const { total, pageSize, current, keyword: _keyword, sort, moduleIds } = lastPageParams;
      pageNation.value = {
        total: total || 0,
        pageSize,
        current,
      };
      keyword.value = _keyword;
      otherListQueryParams.value = {
        sort,
        moduleIds,
      };
    }
    await loadCaseList();
    // TODO 获取用例详情 暂时
    caseDetail.value = caseList.value[0] ?? {};
  });
</script>

<style lang="less" scoped>
  .case-list {
    @apply flex flex-col  overflow-y-auto;

    margin-bottom: 16px;
    height: calc(100% - 40px);
    gap: 8px;
    .ms-scroll-bar();
    .case-item {
      @apply cursor-pointer;

      padding: 8px;
      border: 1px solid var(--color-text-n8);
      border-radius: var(--border-radius-small);
      &:hover {
        border: 1px solid rgb(var(--primary-4));
      }
    }
    .case-item--active {
      border: 1px solid rgb(var(--primary-5));
      background-color: var(--color-text-n9);
    }
  }
  .tab-content {
    .ms-scroll-bar();
    @apply py-4;
  }
</style>
