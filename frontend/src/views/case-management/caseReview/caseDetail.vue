<template>
  <MsCard :min-width="1100" has-breadcrumb hide-footer no-content-padding hide-divider>
    <template #headerLeft>
      <a-tooltip :content="reviewName">
        <div class="one-line-text mr-[8px] max-w-[260px] font-medium text-[var(--color-text-000)]">
          {{ reviewName }}
        </div>
      </a-tooltip>
      <div
        class="rounded-[0_999px_999px_0] border border-solid border-[text-[rgb(var(--primary-5))]] px-[8px] py-[2px] text-[12px] leading-[16px] text-[rgb(var(--primary-5))]"
      >
        <MsIcon type="icon-icon-contacts" size="13" />
        {{ t('caseManagement.caseReview.single') }}
      </div>
      <div class="ml-[16px] flex items-center">
        <a-switch v-model:model-value="onlyMine" size="small" class="mr-[8px]" />
        {{ t('caseManagement.caseReview.myReview') }}
      </div>
    </template>
    <div class="h-full px-[24px]">
      <a-divider class="my-0" />
      <div class="flex h-[calc(100%-1px)] w-full">
        <div class="h-full w-[356px] border-r border-[var(--color-text-n8)] pr-[16px] pt-[16px]">
          <div class="mb-[16px] flex">
            <a-input
              v-model:model-value="keyword"
              :placeholder="t('caseManagement.caseReview.searchPlaceholder')"
              allow-clear
              class="mr-[8px] w-[240px]"
            />
            <a-select v-model:model-value="type" :options="typeOptions" class="w-[92px]"></a-select>
          </div>
          <div class="case-list">
            <div
              v-for="item of caseList"
              :key="item.id"
              :class="['case-item', activeCase.id === item.id ? 'case-item--active' : '']"
              @click="changeActiveCase(item)"
            >
              <div class="mb-[4px] flex items-center justify-between">
                <div>{{ item.id }}</div>
                <div class="flex items-center gap-[4px]">
                  <MsIcon
                    :type="resultMap[item.result as ResultMap].icon"
                    :style="{color: resultMap[item.result as ResultMap].color}"
                  />
                  {{ t(resultMap[item.result as ResultMap].label) }}
                </div>
              </div>
              <a-tooltip :content="item.name">
                <div class="one-line-text">{{ item.name }}</div>
              </a-tooltip>
            </div>
          </div>
          <MsPagination :total="total" :page-size="pageSize" :current="pageCurrent" size="mini" simple />
        </div>
        <div class="relative flex w-[calc(100%-356px)] flex-col">
          <div class="pl-[16px] pt-[16px]">
            <div class="rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)] p-[16px]">
              <div class="mb-[12px] flex items-center justify-between">
                <a-tooltip :content="activeCase.module">
                  <div class="one-line-text cursor-pointer font-medium text-[rgb(var(--primary-5))]">
                    【{{ activeCase.id }}】{{ activeCase.name }}
                  </div>
                </a-tooltip>
                <a-button type="outline" size="mini" class="arco-btn-outline--secondary">
                  {{ t('common.edit') }}
                </a-button>
              </div>
              <div class="flex items-center">
                <MsIcon type="icon-icon_folder_filled1" class="mr-[4px] text-[var(--color-text-4)]" />
                <a-tooltip :content="activeCase.module">
                  <div class="one-line-text mr-[8px] max-w-[260px] font-medium text-[var(--color-text-000)]">
                    {{ activeCase.module }}
                  </div>
                </a-tooltip>
                <div class="case-detail-label">
                  {{ t('caseManagement.caseReview.caseLevel') }}
                </div>
                <div class="case-detail-value">
                  <caseLevel :case-level="activeCase.level" />
                </div>
                <div class="case-detail-label">
                  {{ t('caseManagement.caseReview.caseVersion') }}
                </div>
                <div class="case-detail-value">
                  <MsIcon type="icon-icon_version" size="13" class="mr-[4px]" />
                  {{ activeCase.version }}
                </div>
                <div class="case-detail-label">
                  {{ t('caseManagement.caseReview.reviewResult') }}
                </div>
                <div class="case-detail-value">
                  <div class="flex items-center gap-[4px]">
                    <MsIcon
                      :type="resultMap[activeCase.result].icon"
                      :style="{
                        color: resultMap[activeCase.result].color,
                      }"
                    />
                    {{ t(resultMap[activeCase.result].label) }}
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
                      :class="`ml-[4px] rounded-full ${
                        showTab === tabList[2].key ? 'bg-[rgb(var(--primary-5))]' : 'bg-[var(--color-text-brand)]'
                      } px-[4px] text-[12px] text-white`"
                    >
                      {{ caseDetail.demandCount > 99 ? '99+' : caseDetail.demandCount }}
                    </div>
                  </div>
                </template>
              </a-tab-pane>
            </a-tabs>
          </div>
          <a-divider class="my-0" />
          <div class="content-center">
            <MsDescription v-if="showTab === 'baseInfo'" :descriptions="descriptions" label-width="90px" />
            <div v-else-if="showTab === 'detail'" class="h-full">
              <MsSplitBox :size="0.8" direction="vertical" min="0" :max="0.99">
                <template #top>
                  <caseTabDetail :form="detailForm" :allow-edit="false" />
                </template>
                <template #bottom>
                  <div class="flex h-full flex-col overflow-hidden">
                    <div class="mb-[8px] font-medium text-[var(--color-text-1)]">
                      {{ t('caseManagement.caseReview.reviewHistory') }}
                    </div>
                    <div class="review-history-list">
                      <div v-for="item of reviewHistoryList" :key="item.id" class="mb-[16px]">
                        <div class="flex items-center">
                          <a-avatar>A</a-avatar>
                          <div class="ml-[8px] flex items-center">
                            <div class="font-medium text-[var(--color-text-1)]">{{ item.reviewer }}</div>
                            <a-divider direction="vertical" margin="8px"></a-divider>
                            <div v-if="item.result === 1" class="flex items-center">
                              <MsIcon type="icon-icon_succeed_filled" class="mr-[4px] text-[rgb(var(--success-6))]" />
                              {{ t('caseManagement.caseReview.pass') }}
                            </div>
                            <div v-else-if="item.result === 2" class="flex items-center">
                              <MsIcon type="icon-icon_close_filled" class="mr-[4px] text-[rgb(var(--danger-6))]" />
                              {{ t('caseManagement.caseReview.fail') }}
                            </div>
                            <div v-else-if="item.result === 3" class="flex items-center">
                              <MsIcon type="icon-icon_warning_filled" class="mr-[4px] text-[rgb(var(--warning-6))]" />
                              {{ t('caseManagement.caseReview.suggestion') }}
                            </div>
                            <div v-else-if="item.result === 4" class="flex items-center">
                              <MsIcon type="icon-icon_resubmit_filled" class="mr-[4px] text-[rgb(var(--warning-6))]" />
                              {{ t('caseManagement.caseReview.reReview') }}
                            </div>
                          </div>
                        </div>
                        <div class="ml-[48px] text-[var(--color-text-2)]">{{ item.reason }}</div>
                        <div class="ml-[48px] mt-[8px] text-[var(--color-text-4)]">{{ item.time }}</div>
                      </div>
                    </div>
                  </div>
                </template>
              </MsSplitBox>
            </div>
            <div v-else>
              <div class="flex items-center justify-between">
                {{ t('caseManagement.caseReview.demandCases') }}
                <a-input-search
                  v-model="demandKeyword"
                  :placeholder="t('caseManagement.caseReview.demandSearchPlaceholder')"
                  allow-clear
                  class="w-[300px]"
                  @press-enter="searchDemand"
                  @search="searchDemand"
                />
              </div>
              <caseTabDemand
                ref="caseDemandRef"
                :fun-params="{ caseId: route.query.id as string, keyword: demandKeyword }"
              />
            </div>
          </div>
          <div class="content-footer">
            <div class="mb-[16px] flex items-center">
              <div class="font-medium text-[var(--color-text-1)]">
                {{ t('caseManagement.caseReview.startReview') }}
              </div>
              <a-switch v-model:model-value="autoNext" class="mx-[8px]" size="small" />
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
            <a-form ref="dialogFormRef" :model="caseResultForm" layout="vertical">
              <a-form-item field="reason" :label="t('caseManagement.caseReview.reviewResult')" class="mb-[8px]">
                <a-radio-group v-model:model-value="caseResultForm.result" @change="() => dialogFormRef?.resetFields()">
                  <a-radio value="pass">
                    <div class="inline-flex items-center">
                      <MsIcon type="icon-icon_succeed_filled" class="mr-[4px] text-[rgb(var(--success-6))]" />
                      {{ t('caseManagement.caseReview.pass') }}
                    </div>
                  </a-radio>
                  <a-radio value="fail">
                    <div class="inline-flex items-center">
                      <MsIcon type="icon-icon_close_filled" class="mr-[4px] text-[rgb(var(--danger-6))]" />
                      {{ t('caseManagement.caseReview.fail') }}
                    </div>
                  </a-radio>
                  <a-radio value="suggestion">
                    <div class="inline-flex items-center">
                      <MsIcon type="icon-icon_warning_filled" class="mr-[4px] text-[rgb(var(--warning-6))]" />
                      {{ t('caseManagement.caseReview.suggestion') }}
                    </div>
                    <a-tooltip :content="t('caseManagement.caseReview.suggestionTip')" position="right">
                      <icon-question-circle
                        class="ml-[4px] mt-[2px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
                        size="16"
                      />
                    </a-tooltip>
                  </a-radio>
                </a-radio-group>
              </a-form-item>
              <a-form-item
                field="reason"
                :label="t('caseManagement.caseReview.reason')"
                :rules="
                  caseResultForm.result === 'fail'
                    ? [{ required: true, message: t('caseManagement.caseReview.reasonRequired') }]
                    : []
                "
                asterisk-position="end"
                class="mb-0"
              >
                <a-input
                  v-model:model-value="caseResultForm.reason"
                  :placeholder="t('caseManagement.caseReview.reasonPlaceholder')"
                />
              </a-form-item>
            </a-form>
            <a-button type="primary" class="mt-[16px]">
              {{ t('caseManagement.caseReview.submitReview') }}
            </a-button>
          </div>
        </div>
      </div>
    </div>
  </MsCard>
</template>

<script setup lang="ts">
  /**
   * @description 功能测试-用例评审-用例详情
   */
  import { useRoute } from 'vue-router';
  import { FormInstance } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsDescription from '@/components/pure/ms-description/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsPagination from '@/components/pure/ms-pagination/index';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import type { CaseLevel } from '@/components/business/ms-case-associate/types';
  import caseTabDemand from '../caseManagementFeature/components/tabContent/tabDemand/associatedDemandTable.vue';
  import caseTabDetail from '../caseManagementFeature/components/tabContent/tabDetail.vue';

  import { useI18n } from '@/hooks/useI18n';

  import type { DetailCase } from '@/models/caseManagement/featureCase';

  const route = useRoute();
  const { t } = useI18n();

  const reviewName = ref('打算肯定还是觉得还是觉得还是计划的');
  const caseDetail = ref({
    demandCount: 999,
  });
  const onlyMine = ref(false);
  const keyword = ref('');

  type ResultMap = 0 | 1 | 2 | 3;
  const resultMap = {
    0: {
      label: t('caseManagement.caseReview.unReview'),
      color: 'var(--color-text-input-border)',
      icon: 'icon-icon_block_filled',
    },
    1: {
      label: t('caseManagement.caseReview.reviewPass'),
      color: 'rgb(var(--success-6))',
      icon: 'icon-icon_succeed_filled',
    },
    2: {
      label: t('caseManagement.caseReview.fail'),
      color: 'rgb(var(--danger-6))',
      icon: 'icon-icon_close_filled',
    },
    3: {
      label: t('caseManagement.caseReview.reReview'),
      color: 'rgb(var(--warning-6))',
      icon: 'icon-icon_resubmit_filled',
    },
  } as const;
  const type = ref('');
  const typeOptions = ref([
    { label: '全部', value: '' },
    { label: resultMap[0].label, value: 'unReview' },
    { label: resultMap[1].label, value: 'reviewPass' },
    { label: resultMap[2].label, value: 'fail' },
    { label: resultMap[3].label, value: 'reReview' },
  ]);

  const initDetail: DetailCase = {
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
  };
  const detailForm = ref<DetailCase>({ ...initDetail });

  const caseList = ref([
    {
      id: 'g4ggtrgrtg',
      name: '打算肯定还是觉得还是觉得还是计划的',
      module: '模块名称模块名称模块名称模块名称模块名称模块名称模块名称模块名称模块名称',
      level: 0 as CaseLevel,
      result: 0 as ResultMap,
      version: '1.0.0',
    },
    {
      id: 2,
      name: '打算肯定还是觉得还是觉得还是计划的',
      result: 1,
      module: '模块名称模块名称模块名称模块名称模块名称模块名称模块名称模块名称模块名称',
      level: 0 as CaseLevel,
      version: '1.0.0',
    },
    {
      id: 3,
      name: '打算肯定还是觉得还是觉得还是计划的',
      result: 2,
      module: '模块名称模块名称模块名称模块名称模块名称模块名称模块名称模块名称模块名称',
      level: 0 as CaseLevel,
      version: '1.0.0',
    },
    {
      id: 4,
      name: '打算肯定还是觉得还是觉得还是计划的',
      result: 3,
      module: '模块名称模块名称模块名称模块名称模块名称模块名称模块名称模块名称模块名称',
      level: 0 as CaseLevel,
      version: '1.0.0',
    },
  ]);
  const total = ref(10);
  const pageSize = ref(10);
  const pageCurrent = ref(1);
  const activeCase = ref({
    id: 'g4ggtrgrtg',
    name: '打算肯定还是觉得还是觉得还是计划的打撒打扫打扫',
    module: '模块名称模块名称模块名称模块名称模块名称模块名称模块名称模块名称模块名称',
    level: 0 as CaseLevel,
    result: 0 as ResultMap,
    version: '1.0.0',
  });

  function changeActiveCase(item: any) {
    if (activeCase.value.id !== item.id) {
      activeCase.value = item;
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
  ]);

  const descriptions = ref([
    {
      label: t('caseManagement.caseReview.belongModule'),
      value: '模块模块',
    },
    {
      label: t('caseManagement.caseReview.caseStatus'),
      value: '未开始',
    },
    {
      label: t('caseManagement.caseReview.responsiblePerson'),
      value: '张三',
    },
    {
      label: t('caseManagement.caseReview.creator'),
      value: '李四',
    },
    {
      label: t('caseManagement.caseReview.createTime'),
      value: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    },
  ]);

  const reviewHistoryList = ref([
    {
      id: 1,
      reviewer: '张三',
      avatar: '',
      result: 1,
      reason: '',
      time: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      id: 2,
      reviewer: '李四',
      avatar: '',
      result: 2,
      reason: '不通过',
      time: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      id: 3,
      reviewer: '王五',
      avatar: '',
      result: 3,
      reason: '建议修改',
      time: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      id: 4,
      reviewer: '李六',
      avatar: '',
      result: 4,
      reason: '重新提',
      time: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    },
  ]);

  const autoNext = ref(false);
  const caseResultForm = ref({
    result: 'pass',
    reason: '',
  });
  const dialogFormRef = ref<FormInstance>();
  const demandKeyword = ref('');
  const caseDemandRef = ref<InstanceType<typeof caseTabDemand>>();

  function searchDemand() {
    caseDemandRef.value?.initData();
  }
</script>

<style lang="less" scoped>
  .case-list {
    .ms-scroll-bar();

    margin-bottom: 16px;
    padding: 16px;
    border-radius: var(--border-radius-small);
    background: var(--color-text-n9);
    .case-item {
      @apply cursor-pointer;

      margin-bottom: 8px;
      padding: 16px;
      border-radius: var(--border-radius-small);
      background-color: white;
    }
    .case-item--active {
      @apply relative;

      background-color: rgb(var(--primary-1));
      box-shadow: inset 0 0 0.5px 0.5px rgb(var(--primary-5));
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

    padding: 16px 0 16px 16px;
    .review-history-list {
      @apply overflow-auto;
      .ms-scroll-bar();

      padding: 16px 0 16px 16px;
    }
  }
  .content-footer {
    padding: 16px;
    width: calc(100% + 32px);
    box-shadow: 0 -1px 4px 0 rgb(31 35 41 / 10%);
    :deep(.arco-radio-label) {
      @apply inline-flex;
    }
  }
</style>
