<template>
  <ReportHeader v-if="!props.isDrawer && props.isPreview" :detail="detail" :share-id="shareId" :is-group="false" />
  <div class="analysis-wrapper" :data-cards="cardCount">
    <SystemTrigger :is-preview="props.isPreview">
      <div :class="`${getAnalysisHover} analysis min-w-[238px]`">
        <div class="block-title">{{ t('report.detail.api.reportAnalysis') }}</div>
        <ReportMetricsItem
          v-for="analysisItem in reportAnalysisList"
          :key="analysisItem.name"
          :item-info="analysisItem"
        />
      </div>
    </SystemTrigger>
    <SystemTrigger :is-preview="props.isPreview">
      <div :class="`${getAnalysisHover} analysis min-w-[410px]`">
        <ExecuteAnalysis :detail="detail" />
      </div>
      <template #content>
        <div class="arco-table-filters-content px-[8px] py-[4px]">{{ t('report.detail.systemInternalTooltip') }}</div>
      </template>
    </SystemTrigger>
    <SystemTrigger :is-preview="props.isPreview">
      <div v-if="functionalCaseTotal" :class="`${getAnalysisHover} analysis min-w-[330px]`">
        <div class="block-title">{{ t('report.detail.useCaseAnalysis') }}</div>
        <div class="flex">
          <div class="w-[70%]">
            <SingleStatusProgress :detail="detail" type="FUNCTIONAL" status="pending" />
            <SingleStatusProgress :detail="detail" type="FUNCTIONAL" status="success" />
            <SingleStatusProgress :detail="detail" type="FUNCTIONAL" status="block" />
            <SingleStatusProgress :detail="detail" type="FUNCTIONAL" status="error" />
          </div>
          <div class="relative w-[30%] min-w-[150px]">
            <div class="charts absolute w-full text-center">
              <div class="text-[12px] !text-[var(--color-text-4)]">{{ t('report.passRate') }}</div>
              <a-popover position="bottom" content-class="response-popover-content">
                <div class="flex justify-center text-[18px] font-medium">
                  <div class="one-line-text max-w-[80px] text-[var(--color-text-1)]">{{ functionCasePassRate }} </div>
                </div>
                <template #content>
                  <div class="min-w-[95px] max-w-[400px] p-4 text-[14px]">
                    <div class="text-[12px] font-medium text-[var(--color-text-4)]">{{ t('report.passRate') }}</div>
                    <div class="mt-2 text-[18px] font-medium text-[var(--color-text-1)]">{{
                      functionCasePassRate
                    }}</div>
                  </div>
                </template>
              </a-popover>
            </div>
            <div class="flex h-full w-full min-w-[150px] items-center justify-center">
              <MsChart width="150px" height="150px" :options="functionCaseOptions" />
            </div>
          </div>
        </div>
      </div>
    </SystemTrigger>
    <SystemTrigger :is-preview="props.isPreview">
      <div v-if="apiCaseTotal" :class="`${getAnalysisHover} analysis min-w-[330px]`">
        <div class="block-title">{{ t('report.detail.apiUseCaseAnalysis') }}</div>
        <div class="flex">
          <div class="w-[70%]">
            <SingleStatusProgress type="API" :detail="detail" status="pending" />
            <SingleStatusProgress type="API" :detail="detail" status="success" />
            <SingleStatusProgress type="API" :detail="detail" status="fakeError" />
            <SingleStatusProgress type="API" :detail="detail" status="error" />
          </div>
          <div class="relative w-[30%] min-w-[150px]">
            <div class="charts absolute w-full text-center">
              <div class="text-[12px] !text-[var(--color-text-4)]">{{ t('report.passRate') }}</div>
              <a-popover position="bottom" content-class="response-popover-content">
                <div class="flex justify-center text-[18px] font-medium">
                  <div class="one-line-text max-w-[80px] text-[var(--color-text-1)]">{{ apiCasePassRate }} </div>
                </div>
                <template #content>
                  <div class="min-w-[95px] max-w-[400px] p-4 text-[14px]">
                    <div class="text-[12px] font-medium text-[var(--color-text-4)]">{{ t('report.passRate') }}</div>
                    <div class="mt-2 text-[18px] font-medium text-[var(--color-text-1)]">{{ apiCasePassRate }}</div>
                  </div>
                </template>
              </a-popover>
            </div>
            <div class="flex h-full w-full min-w-[150px] items-center justify-center">
              <MsChart width="150px" height="150px" :options="apiCaseOptions" />
            </div>
          </div>
        </div>
      </div>
      <template #content>
        <div class="arco-table-filters-content px-[8px] py-[4px]">{{ t('report.detail.systemInternalTooltip') }}</div>
      </template>
    </SystemTrigger>
    <SystemTrigger :is-preview="props.isPreview">
      <div v-if="scenarioCaseTotal" :class="`${getAnalysisHover} analysis min-w-[330px]`">
        <div class="block-title">{{ t('report.detail.scenarioUseCaseAnalysis') }}</div>
        <div class="flex">
          <div class="w-[70%]">
            <SingleStatusProgress type="SCENARIO" :detail="detail" status="pending" />
            <SingleStatusProgress type="SCENARIO" :detail="detail" status="success" />
            <SingleStatusProgress type="SCENARIO" :detail="detail" status="fakeError" />
            <SingleStatusProgress type="SCENARIO" :detail="detail" status="error" />
          </div>
          <div class="relative w-[30%] min-w-[150px]">
            <div class="charts absolute w-full text-center">
              <div class="text-[12px] !text-[var(--color-text-4)]">{{ t('report.passRate') }}</div>
              <a-popover position="bottom" content-class="response-popover-content">
                <div class="flex justify-center text-[18px] font-medium">
                  <div class="one-line-text max-w-[80px] text-[var(--color-text-1)]">{{ scenarioCasePassRate }} </div>
                </div>
                <template #content>
                  <div class="min-w-[95px] max-w-[400px] p-4 text-[14px]">
                    <div class="text-[12px] font-medium text-[var(--color-text-4)]">{{ t('report.passRate') }}</div>
                    <div class="mt-2 text-[18px] font-medium text-[var(--color-text-1)]">{{
                      scenarioCasePassRate
                    }}</div>
                  </div>
                </template>
              </a-popover>
            </div>
            <div class="flex h-full w-full min-w-[150px] items-center justify-center">
              <MsChart width="150px" height="150px" :options="scenarioCaseOptions" />
            </div>
          </div>
        </div>
      </div>
    </SystemTrigger>
  </div>

  <div :class="`${props.isPreview ? 'mt-[16px]' : 'mt-[24px]'} drag-container`">
    <VueDraggable v-model="innerCardList" :disabled="props.isPreview" group="report">
      <div
        v-for="(item, index) of innerCardList"
        v-show="showItem(item)"
        :key="item.id"
        :class="`${props.isPreview ? 'mt-[16px]' : 'hover-card mt-[24px]'} card-item`"
      >
        <div v-if="!props.isPreview" class="action">
          <div class="actionList">
            <a-tooltip :content="t('system.orgTemplate.toTop')">
              <MsIcon
                type="icon-icon_up_outlined"
                size="16"
                :class="getColor(index, 'top')"
                @click="moveCard(item, 'top')"
              />
            </a-tooltip>
            <a-divider direction="vertical" class="!m-0 !mx-2" />
            <a-tooltip :content="t('system.orgTemplate.toBottom')">
              <MsIcon
                :class="getColor(index, 'bottom')"
                type="icon-icon_down_outlined"
                size="16"
                @click="moveCard(item, 'bottom')"
              />
            </a-tooltip>
            <a-divider direction="vertical" class="!m-0 !mx-2" />
            <a-tooltip v-if="allowEdit(item.value)" :content="t('common.edit')">
              <MsIcon type="icon-icon_edit_outlined" size="16" @click="editField(item)" />
            </a-tooltip>
            <a-divider v-if="allowEdit(item.value)" direction="vertical" class="!m-0 !mx-2" />
            <a-tooltip :content="t('common.delete')">
              <MsIcon type="icon-icon_delete-trash_filled" size="16" @click="deleteCard(item)" />
            </a-tooltip>
          </div>
        </div>
        <MsCard simple auto-height auto-width>
          <div v-if="item.value !== ReportCardTypeEnum.CUSTOM_CARD" class="mb-[8px] font-medium">
            {{ t(item.label) }}
          </div>
          <ReportDetailTable
            v-if="item.value === ReportCardTypeEnum.SUB_PLAN_DETAIL"
            v-model:current-mode="currentMode"
            :report-id="detail.id"
            :share-id="shareId"
            :is-preview="props.isPreview"
          />
          <Summary
            v-else-if="item.value === ReportCardTypeEnum.SUMMARY"
            :rich-text="{
              content: item.content || '',
              label: t(item.label),
              richTextTmpFileIds: [],
            }"
            :share-id="shareId"
            :is-preview="props.isPreview"
            :can-edit="item.enableEdit"
            :show-button="showButton"
            :is-plan-group="props.isGroup"
            :detail="detail"
            @update-summary="(formValue:customValueForm) => updateCustom(formValue, item)"
            @cancel="() => handleCancelCustom(item)"
            @handle-summary="(value:string) => handleSummary(value,item)"
            @dblclick="handleDoubleClick(item)"
          />
          <BugTable
            v-else-if="item.value === ReportCardTypeEnum.BUG_DETAIL"
            :report-id="detail.id"
            :share-id="shareId"
            :is-preview="props.isPreview"
          />

          <FeatureCaseTable
            v-else-if="item.value === ReportCardTypeEnum.FUNCTIONAL_DETAIL"
            :report-id="detail.id"
            :share-id="shareId"
            :is-preview="props.isPreview"
            :is-group="props.isGroup"
          />
          <ApiAndScenarioTable
            v-else-if="
              item.value === ReportCardTypeEnum.API_CASE_DETAIL ||
              item.value === ReportCardTypeEnum.SCENARIO_CASE_DETAIL
            "
            :report-id="detail.id"
            :share-id="shareId"
            :active-type="item.value"
            :is-preview="props.isPreview"
          />
          <CustomRichText
            v-else-if="item.value === ReportCardTypeEnum.CUSTOM_CARD"
            :can-edit="item.enableEdit"
            :share-id="shareId"
            :current-id="item.id"
            :custom-form="{
              content: item.content,
              label: t(item.label),
              richTextTmpFileIds: [],
            }"
            @update-custom="(formValue:customValueForm)=>updateCustom(formValue,item)"
            @dblclick="handleDoubleClick(item)"
            @cancel="() => handleCancelCustom(item)"
          />
        </MsCard>
      </div>
    </VueDraggable>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { useEventListener } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';
  import { VueDraggable } from 'vue-draggable-plus';

  import MsChart from '@/components/pure/chart/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import SingleStatusProgress from '@/views/test-plan/report/component/singleStatusProgress.vue';
  import CustomRichText from '@/views/test-plan/report/detail/component/custom-card/customRichText.vue';
  import ApiAndScenarioTable from '@/views/test-plan/report/detail/component/system-card/apiAndScenarioTable.vue';
  import BugTable from '@/views/test-plan/report/detail/component/system-card/bugTable.vue';
  import ExecuteAnalysis from '@/views/test-plan/report/detail/component/system-card/executeAnalysis.vue';
  import FeatureCaseTable from '@/views/test-plan/report/detail/component/system-card/featureCaseTable.vue';
  import ReportDetailTable from '@/views/test-plan/report/detail/component/system-card/reportDetailTable.vue';
  import ReportHeader from '@/views/test-plan/report/detail/component/system-card/reportHeader.vue';
  import ReportMetricsItem from '@/views/test-plan/report/detail/component/system-card/ReportMetricsItem.vue';
  import Summary from '@/views/test-plan/report/detail/component/system-card/summary.vue';
  import SystemTrigger from '@/views/test-plan/report/detail/component/system-card/systemTrigger.vue';

  import { getReportLayout, updateReportDetail } from '@/api/modules/test-plan/report';
  import { commonConfig, defaultCount, defaultReportDetail, seriesConfig, statusConfig } from '@/config/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import { addCommasToNumber } from '@/utils';

  import { UpdateReportDetailParams } from '@/models/testPlan/report';
  import type {
    configItem,
    countDetail,
    PlanReportDetail,
    ReportMetricsItemModel,
    StatusListType,
  } from '@/models/testPlan/testPlanReport';
  import { customValueForm } from '@/models/testPlan/testPlanReport';
  import { ReportCardTypeEnum } from '@/enums/testPlanReportEnum';

  import { defaultGroupConfig, defaultSingleConfig } from './reportConfig';
  import { getSummaryDetail } from '@/views/test-plan/report/utils';

  const { t } = useI18n();

  const route = useRoute();
  const props = defineProps<{
    detailInfo: PlanReportDetail;
    isDrawer?: boolean;
    isGroup?: boolean;
    isPreview?: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'updateSuccess'): void;
    (e: 'updateCustom', item: configItem): void;
  }>();

  const innerCardList = defineModel<configItem[]>('cardList', {
    default: [],
  });

  const detail = ref<PlanReportDetail>({ ...cloneDeep(defaultReportDetail) });
  const showButton = ref<boolean>(false);

  const richText = ref<{ summary: string; richTextTmpFileIds?: string[] }>({
    summary: '',
  });

  const reportForm = ref({
    reportName: '',
  });

  const getAnalysisHover = computed(() => (props.isPreview ? '' : 'hover-analysis cursor-not-allowed'));

  /**
   * 分享share
   */
  const shareId = ref<string>(route.query.shareId as string);

  // 功能用例分析
  const functionCaseOptions = ref({
    ...commonConfig,
    series: {
      ...seriesConfig,
      data: [
        {
          value: 0,
          name: t('common.success'),
          itemStyle: {
            color: '#00C261',
          },
        },
      ],
    },
  });
  // 接口用例分析
  const apiCaseOptions = ref({
    ...commonConfig,
    series: {
      ...seriesConfig,
      data: [
        {
          value: 0,
          name: t('common.success'),
          itemStyle: {
            color: '#00C261',
          },
        },
      ],
    },
  });
  // 场景用例分析
  const scenarioCaseOptions = ref({
    ...commonConfig,
    series: {
      ...seriesConfig,
      data: [
        {
          value: 0,
          name: t('common.success'),
          itemStyle: {
            color: '#00C261',
          },
        },
      ],
    },
  });

  // 获取通过率
  function getPassRateData(caseDetailCount: countDetail) {
    const caseCountDetail = caseDetailCount || defaultCount;
    const passRateData = statusConfig.filter((item) => ['success'].includes(item.value));
    const { success } = caseCountDetail;
    const valueList = success ? statusConfig : passRateData;
    return valueList.map((item: StatusListType) => {
      return {
        value: caseCountDetail[item.value] || 0,
        name: t(item.label),
        itemStyle: {
          color: success ? item.color : '#D4D4D8',
          borderWidth: 2,
          borderColor: '#ffffff',
        },
      };
    });
  }

  // 初始化图表
  function initOptionsData() {
    const { functionalCount, apiCaseCount, apiScenarioCount } = detail.value;
    functionCaseOptions.value.series.data = getPassRateData(functionalCount);
    apiCaseOptions.value.series.data = getPassRateData(apiCaseCount);
    scenarioCaseOptions.value.series.data = getPassRateData(apiScenarioCount);
  }

  const reportAnalysisList = computed<ReportMetricsItemModel[]>(() => [
    {
      name: t('report.detail.threshold'),
      value: detail.value.passThreshold,
      unit: '%',
      icon: 'threshold',
    },
    {
      name: t('report.passRate'),
      value: detail.value.passRate,
      unit: '%',
      icon: 'passRate',
    },
    {
      name: t('report.detail.performCompletion'),
      value: detail.value.executeRate,
      unit: '%',
      icon: 'passRate',
    },
    {
      name: t('report.detail.totalDefects'),
      value: addCommasToNumber(detail.value.bugCount),
      unit: t('report.detail.number'),
      icon: 'bugTotal',
    },
  ]);

  const functionCasePassRate = computed(() => {
    const apiCaseDetail = getSummaryDetail(detail.value.functionalCount || defaultCount);
    return apiCaseDetail.successRate;
  });

  const apiCasePassRate = computed(() => {
    const apiCaseDetail = getSummaryDetail(detail.value.apiCaseCount || defaultCount);
    return apiCaseDetail.successRate;
  });

  const scenarioCasePassRate = computed(() => {
    const apiScenarioDetail = getSummaryDetail(detail.value.apiScenarioCount || defaultCount);
    return apiScenarioDetail.successRate;
  });
  const functionalCaseTotal = computed(() => getSummaryDetail(detail.value.functionalCount).caseTotal);
  const apiCaseTotal = computed(() => getSummaryDetail(detail.value.apiCaseCount).caseTotal);
  const scenarioCaseTotal = computed(() => getSummaryDetail(detail.value.apiScenarioCount).caseTotal);

  function showItem(item: configItem) {
    switch (item.value) {
      case ReportCardTypeEnum.FUNCTIONAL_DETAIL:
        return functionalCaseTotal.value > 0;
      case ReportCardTypeEnum.API_CASE_DETAIL:
        return apiCaseTotal.value > 0;
      case ReportCardTypeEnum.SCENARIO_CASE_DETAIL:
        return scenarioCaseTotal.value > 0;
      default:
        return true;
    }
  }

  const cardCount = computed(() => {
    const totalList = [functionalCaseTotal.value, apiCaseTotal.value, scenarioCaseTotal.value];
    let count = 2;
    totalList.forEach((item: number) => {
      if (item > 0) {
        count++;
      }
    });
    return count;
  });

  const originLayoutInfo = ref([]);

  async function getDefaultLayout() {
    try {
      const res = await getReportLayout(detail.value.id, shareId.value);
      const result = res.map((item: any) => {
        return {
          id: item.id,
          value: item.name,
          label: item.label,
          content: item.value || '',
          type: item.type,
          enableEdit: false,
          richTextTmpFileIds: item.richTextTmpFileIds,
        };
      });
      innerCardList.value = result;
      originLayoutInfo.value = cloneDeep(result);
    } catch (error) {
      console.log(error);
    }
  }

  watchEffect(() => {
    if (props.detailInfo) {
      detail.value = cloneDeep(props.detailInfo);
      richText.value.summary = detail.value.summary;
      reportForm.value.reportName = detail.value.name;
      initOptionsData();
      if (props.isPreview) {
        if (!detail.value.defaultLayout && detail.value.id) {
          getDefaultLayout();
        } else {
          innerCardList.value = props.isGroup ? cloneDeep(defaultGroupConfig) : cloneDeep(defaultSingleConfig);
        }
      }
    }
  });

  onMounted(async () => {
    nextTick(() => {
      const editorContent = document.querySelector('.editor-content');
      useEventListener(editorContent, 'click', () => {
        showButton.value = true;
      });
    });
  });

  function handleCancelCustom(cardItem: configItem) {
    const originItem = originLayoutInfo.value.find((item: configItem) => item.id === cardItem.id);
    const index = originLayoutInfo.value.findIndex((e: configItem) => e.id === cardItem.id);
    if (originItem && index !== -1) {
      innerCardList.value.splice(index, 1, originItem);
    }
    showButton.value = false;
    cardItem.enableEdit = false;
  }

  function handleSummary(content: string, cardItem: configItem) {
    cardItem.content = content;
  }

  const currentMode = ref<string>('drawer');

  function getColor(index: number, type: string) {
    if (type === 'top' && index === 0) {
      return ['text-[rgb(var(--primary-3))]'];
    }
    if (type === 'bottom' && index === innerCardList.value.length - 1) {
      return ['text-[rgb(var(--primary-3))]'];
    }
  }

  const allowEditType = [ReportCardTypeEnum.CUSTOM_CARD, ReportCardTypeEnum.SUMMARY];
  function allowEdit(value: ReportCardTypeEnum) {
    return allowEditType.includes(value);
  }
  // 移动卡片
  function moveCard(cardItem: configItem, type: string) {
    const moveIndex = innerCardList.value.findIndex((item: any) => item.id === cardItem.id);
    if (type === 'top') {
      if (moveIndex === 0) {
        return;
      }
      innerCardList.value.splice(moveIndex, 1);
      innerCardList.value.splice(moveIndex - 1, 0, cardItem);
    } else {
      if (moveIndex === innerCardList.value.length - 1) {
        return;
      }
      innerCardList.value.splice(moveIndex, 1);
      innerCardList.value.splice(moveIndex + 1, 0, cardItem);
    }
  }
  // 删除卡片
  const deleteCard = (cardItem: configItem) => {
    innerCardList.value = innerCardList.value.filter((item) => item.id !== cardItem.id);
  };

  // 编辑模式和预览模式切换
  function editField(cardItem: configItem) {
    if (allowEditType.includes(cardItem.value)) {
      cardItem.enableEdit = !cardItem.enableEdit;
    }
  }

  function handleDoubleClick(cardItem: configItem) {
    if (cardItem.value === ReportCardTypeEnum.SUMMARY) {
      showButton.value = true;
    }
    cardItem.enableEdit = !cardItem.enableEdit;
  }

  async function handleUpdateReportDetail(currentItem: configItem) {
    try {
      const params: UpdateReportDetailParams = {
        id: detail.value.id,
        componentId: currentItem.id,
        componentValue: currentItem.content,
        richTextTmpFileIds: currentItem.richTextTmpFileIds,
      };
      await updateReportDetail(params);
      Message.success(t('common.updateSuccess'));
      if (currentItem.value === ReportCardTypeEnum.SUMMARY) {
        showButton.value = false;
      } else {
        currentItem.enableEdit = !currentItem.enableEdit;
      }
      emit('updateSuccess');
    } catch (error) {
      console.log(error);
    }
  }

  function updateCustom(formValue: customValueForm, currentItem: configItem) {
    const newCurrentItem: configItem = {
      ...currentItem,
      ...formValue,
    };

    innerCardList.value = innerCardList.value.map((item: configItem) => {
      if (item.id === currentItem.id) {
        return {
          ...item,
          ...formValue,
          enableEdit: false,
        };
      }
      return item;
    });
    if (!props.isPreview) {
      emit('updateCustom', newCurrentItem);
    } else {
      handleUpdateReportDetail(newCurrentItem);
    }
  }
</script>

<style scoped lang="less">
  .report-name {
    padding: 0 16px;
    height: 56px;
    @apply flex items-center justify-between border-b bg-white;
  }
  .block-title {
    @apply mb-4 font-medium;
  }
  .config-right-container {
    padding: 16px;
    width: calc(100% - 300px);
    background: var(--color-bg-3);
  }
  .analysis-wrapper {
    @apply mb-4 grid items-center gap-4;
    .analysis {
      padding: 24px;
      height: 250px;
      border: 1px solid transparent;
      box-shadow: 0 0 10px rgba(120 56 135/ 5%);
      @apply rounded-xl bg-white;
      .charts {
        top: 36%;
        right: 0;
        bottom: 0;
        left: 0;
        z-index: 99;
        margin: auto;
      }
    }
    &[data-cards='2'],
    &[data-cards='4'] {
      grid-template-columns: repeat(2, 1fr);
    }
    &[data-cards='3'] {
      grid-template-columns: repeat(3, 1fr);
    }
    // 有5个的时候，上面2个，下面3个
    &[data-cards='5'] {
      grid-template-columns: repeat(6, 1fr);
      & > .analysis:nth-child(1),
      & > .analysis:nth-child(2) {
        grid-column: span 3;
      }
      & > .analysis:nth-child(n + 3) {
        grid-column: span 2;
      }
    }
  }
  .hover-analysis {
    &:hover {
      border: 1px solid rgb(var(--primary-5));
    }
  }
  .drag-container {
    .card-item {
      position: relative;
      border: 1px solid transparent;
      border-radius: 12px;
      .action {
        position: absolute;
        top: -14px;
        left: 50%;
        transform: translateX(-50%);
        z-index: 9 !important;
        background: white;
        opacity: 0;
        @apply flex items-center justify-end;
        .actionList {
          padding: 4px;
          border-radius: 4px;
          @apply flex items-center justify-center;
        }
      }
      &:hover > .action {
        opacity: 1;
      }
      &:hover > .action > .actionList {
        color: rgb(var(--primary-5));
        box-shadow: 0 4px 10px -1px rgba(100 100 102/ 15%);
      }
    }
  }
  .hover-card {
    &:hover {
      border: 1px solid rgb(var(--primary-5));
      background: var(--color-text-n9);
    }
  }
</style>
