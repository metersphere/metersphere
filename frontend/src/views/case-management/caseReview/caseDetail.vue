<template>
  <MsCard :min-width="1100" has-breadcrumb hide-footer no-content-padding hide-divider show-full-screen>
    <template #headerLeft>
      <a-tooltip :content="reviewDetail.name">
        <div class="one-line-text mr-[8px] max-w-[300px] font-medium text-[var(--color-text-000)]">
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
      <div v-show="reviewDetail.reviewPassRule === 'MULTIPLE'" class="ml-[16px] flex items-center">
        <a-switch v-model:model-value="onlyMineStatus" size="small" class="mr-[8px]" type="line" />
        {{ t('caseManagement.caseReview.myReviewStatus') }}
      </div>
    </template>
    <div class="flex h-full w-full border-t border-[var(--color-text-n8)]">
      <div class="h-full w-[356px] border-r border-[var(--color-text-n8)] py-[16px] pl-[24px] pr-[16px]">
        <div class="mb-[16px] flex">
          <a-input-search
            v-model:model-value="keyword"
            :placeholder="t('caseManagement.caseReview.searchPlaceholder')"
            allow-clear
            class="mr-[8px] w-[240px]"
            @search="loadCaseList"
            @press-enter="loadCaseList"
            @clear="loadCaseList"
          />
          <a-select
            v-model:model-value="type"
            :options="typeOptions"
            class="w-[92px]"
            :disabled="onlyMineStatus"
            @change="loadCaseList"
          >
          </a-select>
        </div>
        <a-spin :loading="caseListLoading" class="h-[calc(100%-46px)] w-full">
          <div class="case-list">
            <div
              v-for="item of caseList"
              :key="item.caseId"
              :class="['case-item', caseDetail.id === item.caseId ? 'case-item--active' : '']"
              @click="changeActiveCase(item)"
            >
              <div class="mb-[4px] flex items-center justify-between">
                <div>{{ item.num }}</div>
                <div v-if="onlyMineStatus" class="flex items-center gap-[4px] leading-[22px]">
                  <MsIcon
                    :type="reviewResultMap[item.myStatus]?.icon ?? ''"
                    :style="{ color: reviewResultMap[item.myStatus]?.color }"
                  />
                  {{ t(reviewResultMap[item.myStatus]?.label) }}
                </div>
                <div v-else class="flex items-center gap-[4px] leading-[22px]">
                  <MsIcon
                    :type="reviewResultMap[item.status]?.icon"
                    :style="{ color: reviewResultMap[item.status]?.color }"
                  />
                  {{ t(reviewResultMap[item.status]?.label) }}
                </div>
              </div>
              <a-tooltip :content="item.name">
                <div class="one-line-text">{{ item.name }}</div>
              </a-tooltip>
            </div>
            <MsEmpty v-if="caseList.length === 0" />
          </div>
          <MsPagination
            v-model:page-size="pageNation.pageSize"
            v-model:current="pageNation.current"
            :total="pageNation.total"
            size="mini"
            show-jumper
            simple-only-jumper
            @change="loadCaseList"
            @page-size-change="loadCaseList"
          />
        </a-spin>
      </div>
      <a-spin :loading="caseDetailLoading" class="relative flex flex-1 flex-col overflow-hidden">
        <MsEmpty v-if="!caseList.length" />
        <template v-else>
          <div class="content-center">
            <div class="rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)] p-[16px]">
              <div class="mb-[12px] flex items-center">
                <div class="mr-[16px] flex-1 overflow-hidden">
                  <a-tooltip :content="`【${caseDetail.num}】${caseDetail.name}`">
                    <div
                      class="one-line-text w-[fit-content] max-w-[100%] cursor-pointer font-medium text-[rgb(var(--primary-5))]"
                      @click="goCaseDetail"
                    >
                      【{{ caseDetail.num }}】{{ caseDetail.name }}
                    </div>
                  </a-tooltip>
                </div>
                <a-button
                  v-permission="['FUNCTIONAL_CASE:READ+UPDATE']"
                  type="outline"
                  size="mini"
                  class="arco-btn-outline--secondary"
                  @click="editCaseVisible = true"
                >
                  {{ t('common.edit') }}
                </a-button>
              </div>
              <div class="flex items-center">
                <MsIcon type="icon-icon_folder_filled1" class="mr-[4px] text-[var(--color-text-4)]" />
                <a-tooltip :content="caseDetail.moduleName || t('common.root')">
                  <div class="one-line-text mr-[8px] max-w-[300px] font-medium text-[var(--color-text-000)]">
                    {{ caseDetail.moduleName || t('common.root') }}
                  </div>
                </a-tooltip>
                <div class="case-detail-label">
                  {{ t('caseManagement.caseReview.caseLevel') }}
                </div>
                <div class="case-detail-value">
                  <caseLevel :case-level="caseDetailLevel" />
                </div>
                <!-- <div class="case-detail-label">
                {{ t('caseManagement.caseReview.caseVersion') }}
              </div>
              <div class="case-detail-value">
                <MsIcon type="icon-icon_version" size="13" class="mr-[4px]" />
                {{ caseDetail.versionName }}
              </div> -->
                <div class="case-detail-label">
                  {{ t('caseManagement.caseReview.reviewResult') }}
                </div>
                <div class="case-detail-value">
                  <ReviewStatusTrigger v-if="reviewDetail.reviewPassRule === 'MULTIPLE'" ref="reviewStatusTriggerRef" />
                  <div
                    v-if="reviewResultMap[activeCaseReviewStatus as ReviewResult] && reviewDetail.reviewPassRule !== 'MULTIPLE'"
                    class="flex items-center gap-[4px]"
                  >
                    <MsIcon
                      :type="reviewResultMap[activeCaseReviewStatus as ReviewResult].icon"
                      :style="{
                          color: reviewResultMap[activeCaseReviewStatus as ReviewResult].color,
                        }"
                    />
                    {{ t(reviewResultMap[activeCaseReviewStatus as ReviewResult].label) }}
                  </div>
                </div>
              </div>
            </div>
            <a-tabs v-model:active-key="showTab" class="no-content">
              <a-tab-pane :key="tabList[0].key" :title="tabList[0].title" />
              <a-tab-pane :key="tabList[1].key" :title="tabList[1].title" />
              <a-tab-pane :key="tabList[2].key">
                <template #title>
                  <div class="flex items-center">
                    {{ tabList[2].title }}
                    <div
                      v-if="caseDetail.demandCount > 0"
                      style="min-width: 16px; text-align: center; align-content: center"
                      :class="`ml-[4px] h-[16px] rounded-full ${
                        showTab === tabList[2].key
                          ? 'bg-[rgb(var(--primary-9))] text-[rgb(var(--primary-5))]'
                          : 'bg-[var(--color-text-brand)] text-white'
                      } px-[4px] text-[12px]`"
                    >
                      {{ caseDetail.demandCount > 99 ? '99+' : caseDetail.demandCount }}
                    </div>
                  </div>
                </template>
              </a-tab-pane>
              <a-tab-pane :key="tabList[3].key" :title="tabList[3].title" />
            </a-tabs>
            <a-divider class="my-0" />
            <MsDescription
              v-if="showTab === 'baseInfo'"
              :descriptions="descriptions"
              label-width="90px"
              class="mt-[16px]"
            />
            <div v-else-if="showTab === 'detail'" class="mt-[16px] h-full">
              <caseTabDetail :form="caseDetail" :allow-edit="false" />
            </div>
            <div v-else-if="showTab === 'demand'">
              <div class="mt-[16px] flex items-center justify-between">
                {{ t('caseManagement.caseReview.demandCases') }}
                <a-input-search
                  v-model="demandKeyword"
                  :placeholder="t('caseManagement.caseReview.demandSearchPlaceholder')"
                  allow-clear
                  class="w-[300px]"
                  @press-enter="searchDemand"
                  @search="searchDemand"
                  @clear="searchDemand"
                />
              </div>
              <caseTabDemand
                ref="caseDemandRef"
                :fun-params="{ projectId: appStore.currentProjectId, caseId: activeCaseId, keyword: demandKeyword }"
                :show-empty="false"
              />
            </div>
            <div v-else class="flex flex-1 flex-col overflow-hidden pl-[16px] pt-[16px]">
              <div class="ms-comment-list">
                <a-spin :loading="reviewHistoryListLoading" class="h-full w-full">
                  <div v-for="item of reviewHistoryList" :key="item.id" class="ms-comment-list-item">
                    <MSAvatar :avatar="item.userLogo" />
                    <div class="flex-1 overflow-hidden">
                      <div class="flex items-center gap-[8px]">
                        <div class="flex-1 overflow-hidden">
                          <a-tooltip :content="item.userName">
                            <div
                              class="one-line-text w-[fit-content] max-w-[100%] font-medium text-[var(--color-text-1)]"
                            >
                              {{ item.userName }}
                            </div>
                          </a-tooltip>
                        </div>
                        <div v-if="item.status === 'PASS'" class="flex items-center">
                          <MsIcon type="icon-icon_succeed_filled" class="mr-[4px] text-[rgb(var(--success-6))]" />
                          {{ t('caseManagement.caseReview.pass') }}
                        </div>
                        <div v-else-if="item.status === 'UN_PASS'" class="flex items-center">
                          <MsIcon type="icon-icon_close_filled" class="mr-[4px] text-[rgb(var(--danger-6))]" />
                          {{ t('caseManagement.caseReview.fail') }}
                        </div>
                        <div v-else-if="item.status === 'UNDER_REVIEWED'" class="flex items-center">
                          <MsIcon type="icon-icon_warning_filled" class="mr-[4px] text-[rgb(var(--warning-6))]" />
                          {{ t('caseManagement.caseReview.suggestion') }}
                        </div>
                        <div v-else-if="item.status === 'RE_REVIEWED'" class="flex items-center">
                          <MsIcon type="icon-icon_resubmit_filled" class="mr-[4px] text-[rgb(var(--warning-6))]" />
                          {{ t('caseManagement.caseReview.reReview') }}
                        </div>
                      </div>
                      <div class="markdown-body ml-[48px]" v-html="item.contentText"></div>
                      <div class="mt-[8px] text-[12px] leading-[16px] text-[var(--color-text-4)]">
                        {{ dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss') }}
                      </div>
                    </div>
                  </div>
                  <MsEmpty v-if="reviewHistoryList.length === 0" />
                </a-spin>
              </div>
            </div>
          </div>
          <div class="content-footer">
            <div class="mb-[12px] flex items-center justify-between">
              <div class="font-medium text-[var(--color-text-1)]">
                {{ t('caseManagement.caseReview.startReview') }}
              </div>
              <div class="flex items-center">
                <a-switch v-model:model-value="autoNext" class="mx-[8px]" size="small" type="line" />
                <div class="text-[var(--color-text-4)]">{{ t('caseManagement.caseReview.autoNext') }}</div>
                <a-tooltip position="right">
                  <template #content>
                    <div>{{ t('caseManagement.caseReview.autoNextTip1') }}</div>
                    <div>{{ t('caseManagement.caseReview.autoNextTip2') }}</div>
                  </template>
                  <icon-question-circle
                    class="mb-[2px] ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
                    size="16"
                  />
                </a-tooltip>
              </div>
            </div>
            <reviewForm
              :review-id="reviewId"
              :case-id="activeCaseId"
              :review-pass-rule="reviewDetail.reviewPassRule"
              @done="reviewDone"
            />
          </div>
        </template>
      </a-spin>
    </div>
  </MsCard>
  <EditCaseDetailDrawer v-model:visible="editCaseVisible" :case-id="activeCaseId" @load-case="loadCase" />
</template>

<script setup lang="ts">
  /**
   * @description 功能测试-用例评审-用例详情
   */
  import { useRoute, useRouter } from 'vue-router';
  import dayjs from 'dayjs';

  import MSAvatar from '@/components/pure/ms-avatar/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsDescription, { Description } from '@/components/pure/ms-description/index.vue';
  import MsEmpty from '@/components/pure/ms-empty/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsPagination from '@/components/pure/ms-pagination/index';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import type { CaseLevel } from '@/components/business/ms-case-associate/types';
  import ReviewStatusTrigger from '@/components/business/ms-minders/caseReviewMinder/components/reviewStatusTrigger.vue';
  import caseTabDemand from '../caseManagementFeature/components/tabContent/tabDemand/associatedDemandTable.vue';
  import caseTabDetail from '../caseManagementFeature/components/tabContent/tabDetail.vue';
  import EditCaseDetailDrawer from './components/editCaseDetailDrawer.vue';
  import reviewForm from './components/reviewForm.vue';

  import {
    getCaseReviewHistoryList,
    getReviewDetail,
    getReviewDetailCasePage,
  } from '@/api/modules/case-management/caseReview';
  import { getCaseDetail } from '@/api/modules/case-management/featureCase';
  import { reviewDefaultDetail, reviewResultMap } from '@/config/caseManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import { ReviewCaseItem, ReviewHistoryItem, ReviewItem, ReviewResult } from '@/models/caseManagement/caseReview';
  import type { DetailCase } from '@/models/caseManagement/featureCase';
  import { CaseManagementRouteEnum } from '@/enums/routeEnum';

  import { getCustomField } from '@/views/case-management/caseManagementFeature/components/utils';

  const route = useRoute();
  const router = useRouter();
  const appStore = useAppStore();
  const { t } = useI18n();

  const reviewDetail = ref<ReviewItem>({ ...reviewDefaultDetail });
  const reviewId = ref(route.query.id as string);
  const loading = ref(false);

  // 初始化评审详情
  async function initDetail() {
    try {
      loading.value = true;
      const res = await getReviewDetail(reviewId.value);
      reviewDetail.value = res;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  const type = ref('');
  const typeOptions = ref([
    { label: t('common.all'), value: '' },
    { label: t(reviewResultMap.UN_REVIEWED.label), value: 'UN_REVIEWED' },
    { label: t(reviewResultMap.PASS.label), value: 'PASS' },
    { label: t(reviewResultMap.UN_PASS.label), value: 'UN_PASS' },
    { label: t(reviewResultMap.RE_REVIEWED.label), value: 'RE_REVIEWED' },
  ]);

  const viewFlag = ref(false);
  const onlyMineStatus = ref(false);
  const keyword = ref('');
  const caseList = ref<ReviewCaseItem[]>([]);
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
      const res = await getReviewDetailCasePage({
        projectId: appStore.currentProjectId,
        reviewId: reviewId.value,
        viewFlag: viewFlag.value,
        viewStatusFlag: onlyMineStatus.value,
        keyword: keyword.value,
        current: pageNation.value.current || 1,
        pageSize: pageNation.value.pageSize,
        filter: type.value
          ? {
              status: [type.value],
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

  watch(
    () => onlyMineStatus.value,
    () => {
      pageNation.value.current = 1;
      loadCaseList();
    }
  );

  const activeCaseId = ref(route.query.caseId as string);
  const activeCaseReviewStatus = computed(() => {
    const activeCase = caseList.value.find((e) => e.caseId === activeCaseId.value);
    return onlyMineStatus.value ? activeCase?.myStatus : activeCase?.status;
  });
  const defaultCaseDetail: DetailCase = {
    id: '',
    projectId: '',
    templateId: '',
    name: '',
    prerequisite: '', // prerequisite
    caseEditType: '', // 编辑模式：步骤模式/文本模式
    steps: '',
    textDescription: '',
    expectedResult: '', // 预期结果
    description: '',
    publicCase: false, // 是否公共用例
    moduleId: '',
    versionId: '',
    tags: [],
    customFields: [], // 自定义字段集合
    relateFileMetaIds: [], // 关联文件ID集合
    reviewStatus: 'UN_REVIEWED',
    functionalPriority: '',
  };
  const caseDetail = ref<DetailCase>({ ...defaultCaseDetail });
  const descriptions = ref<Description[]>([]);
  const caseDetailLevel = computed<CaseLevel>(() => {
    if (caseDetail.value.functionalPriority) {
      return caseDetail.value.functionalPriority as CaseLevel;
    }
    return 'P1';
  });

  function changeActiveCase(item: ReviewCaseItem) {
    if (activeCaseId.value !== item.caseId) {
      activeCaseId.value = item.caseId;
    }
  }
  const caseDetailLoading = ref(false);

  // 加载用例详情
  async function loadCaseDetail() {
    try {
      caseDetailLoading.value = true;
      const res = await getCaseDetail(activeCaseId.value);
      caseDetail.value = res;
      descriptions.value = [
        {
          label: t('caseManagement.caseReview.belongModule'),
          value: res.moduleName || t('common.root'),
        },
        // 解析用例模板的自定义字段
        ...res.customFields.map((e: Record<string, any>) => {
          try {
            return {
              label: e.fieldName,
              value: getCustomField(e),
            };
          } catch (error) {
            return {
              label: e.fieldName,
              value: e.defaultValue,
            };
          }
        }),
        {
          label: t('caseManagement.caseReview.creator'),
          value: res.createUserName || '',
        },
        {
          label: t('caseManagement.caseReview.createTime'),
          value: dayjs().format('YYYY-MM-DD HH:mm:ss'),
        },
      ];
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      caseDetailLoading.value = false;
    }
  }

  const showTab = ref('detail');
  const tabList = ref([
    {
      key: 'baseInfo',
      title: t('caseManagement.caseReview.caseBaseInfo'),
    },
    {
      key: 'detail',
      title: t('caseManagement.caseReview.caseDetail'),
    },
    {
      key: 'demand',
      title: t('caseManagement.caseReview.caseDemand'),
    },
    {
      key: 'reviewHistory',
      title: t('caseManagement.caseReview.reviewHistory'),
    },
  ]);

  const reviewHistoryListLoading = ref(false);
  const reviewHistoryList = ref<ReviewHistoryItem[]>([]);

  // 加载评审历史列表
  async function initReviewHistoryList() {
    try {
      reviewHistoryListLoading.value = true;
      const res = await getCaseReviewHistoryList(reviewId.value, activeCaseId.value);
      reviewHistoryList.value = res;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      reviewHistoryListLoading.value = false;
    }
  }

  const reviewStatusTriggerRef = ref<InstanceType<typeof ReviewStatusTrigger>>();
  function initReviewerAndStatus() {
    reviewStatusTriggerRef.value?.initReviewerAndStatus(reviewId.value, activeCaseId.value);
  }

  watch(
    () => activeCaseId.value,
    () => {
      console.log('🤔️ =>', activeCaseId.value);
      loadCaseDetail();
      initReviewerAndStatus();
      initReviewHistoryList();
    }
  );

  const autoNext = ref(false);
  const demandKeyword = ref('');
  const caseDemandRef = ref<InstanceType<typeof caseTabDemand>>();

  function searchDemand() {
    caseDemandRef.value?.initData();
  }

  function goCaseDetail() {
    window.open(
      `${window.location.origin}#${
        router.resolve({ name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE }).fullPath
      }?id=${activeCaseId.value}&orgId=${appStore.currentOrgId}&pId=${appStore.currentProjectId}`
    );
  }

  async function reviewDone(status: string) {
    if (autoNext.value) {
      // 自动下一个，更改激活的 id会刷新详情
      const index = caseList.value.findIndex((e) => e.caseId === activeCaseId.value);

      // 如果过滤的状态和评审状态不一样，则这条将从当前列表排除
      const oneMissingCase = type.value !== '' && status !== type.value;
      if (oneMissingCase) {
        if ((pageNation.value.current - 1) * pageNation.value.pageSize + index + 1 < pageNation.value.total) {
          // 不是最后一个
          await loadCaseList();
          activeCaseId.value = caseList.value[index].caseId;
        } else {
          // 是最后一个，如果列表还有其他数据，则选中第一条；如果没有其他数据，则显示暂无数据
          await loadCaseList();
          if (caseList.value.length > 1) {
            activeCaseId.value = caseList.value[0].caseId;
          }
        }
        return;
      }

      if (index < caseList.value.length - 1) {
        await loadCaseList();
        activeCaseId.value = caseList.value[index + 1].caseId;
      } else if (pageNation.value.current * pageNation.value.pageSize < pageNation.value.total) {
        // 当前页不是最后一页，则加载下一页并激活第一个用例
        pageNation.value.current += 1;
        await loadCaseList();
        activeCaseId.value = caseList.value[0].caseId;
      } else {
        // 当前是最后一个，刷新数据
        loadCaseDetail();
        initReviewHistoryList();
        loadCaseList();
        initReviewerAndStatus();
      }
    } else {
      // 不自动下一个才请求详情
      loadCaseDetail();
      initReviewHistoryList();
      loadCaseList();
      initReviewerAndStatus();
    }
  }

  const editCaseVisible = ref(false);

  async function loadCase() {
    await loadCaseList();
    loadCaseDetail();
  }

  onBeforeMount(async () => {
    const lastPageParams = window.history.state.params ? JSON.parse(window.history.state.params) : null; // 获取上个页面带过来的表格查询参数
    if (lastPageParams) {
      const {
        total,
        pageSize,
        current,
        viewFlag: _onlyMine,
        keyword: _keyword,
        combine,
        sort,
        searchMode,
        moduleIds,
      } = lastPageParams;
      pageNation.value = {
        total: total || 0,
        pageSize,
        current,
      };
      viewFlag.value = !!_onlyMine;
      keyword.value = _keyword;
      otherListQueryParams.value = {
        combine,
        sort,
        searchMode,
        moduleIds,
      };
    } else {
      keyword.value = route.query.reviewId as string;
    }
    await initDetail();
    loadCase();
    initReviewerAndStatus();
    if (showTab.value === 'detail') {
      initReviewHistoryList();
    }
  });
</script>

<style lang="less" scoped>
  .case-list {
    .ms-scroll-bar();

    overflow-y: auto;
    margin-bottom: 16px;
    padding: 16px;
    height: calc(100% - 46px);
    border-radius: var(--border-radius-small);
    background: var(--color-text-n9);
    .case-item {
      @apply cursor-pointer;
      &:not(:last-child) {
        margin-bottom: 8px;
      }

      padding: 16px;
      border-radius: var(--border-radius-small);
      background-color: white;
    }
    .case-item--active {
      @apply relative;

      border: 1px solid rgb(var(--primary-5));
      background-color: rgb(var(--primary-1));
    }
  }
  .case-detail-label {
    margin-right: 8px;
    color: var(--color-text-4);
  }
  .case-detail-value {
    @apply flex items-center;

    margin-right: 16px;
  }
  :deep(.arco-tabs-content) {
    @apply hidden;
  }
  .content-center {
    @apply flex-1 overflow-auto;
    .ms-scroll-bar();

    padding: 16px;
    align-content: start;
  }
  .content-footer {
    padding: 16px;
    box-shadow: 0 -1px 4px 0 rgb(31 35 41 / 10%);
    :deep(.arco-radio-label) {
      @apply inline-flex;
    }
  }
  :deep(.overall-review-result) {
    padding: 0;
  }
</style>

<style lang="less">
  .review-result-trigger-content {
    width: 160px;
  }
</style>
