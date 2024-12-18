<template>
  <ReportHeader
    v-if="!props.isDrawer && props.isPreview"
    :detail="detail"
    :share-id="shareId"
    :is-group="props.isGroup"
  />
  <div class="analysis-wrapper" :data-cards="cardCount">
    <SystemTrigger :is-preview="props.isPreview">
      <div :class="`${getAnalysisHover} analysis min-w-[330px]`">
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
        <ExecuteAnalysis :detail="detail" :animation="true" />
      </div>
      <template #content>
        <div class="arco-table-filters-content px-[8px] py-[4px]">{{ t('report.detail.systemInternalTooltip') }}</div>
      </template>
    </SystemTrigger>
    <SystemTrigger :is-preview="props.isPreview">
      <div v-if="functionalCaseTotal" :class="`${getAnalysisHover} analysis min-w-[330px]`">
        <div class="block-title">{{ t('report.detail.useCaseAnalysis') }}</div>
        <div class="flex">
          <div class="mr-[24px] flex-1">
            <SingleStatusProgress :detail="detail" type="FUNCTIONAL" status="pending" />
            <SingleStatusProgress :detail="detail" type="FUNCTIONAL" status="success" />
            <SingleStatusProgress :detail="detail" type="FUNCTIONAL" status="block" />
            <SingleStatusProgress :detail="detail" type="FUNCTIONAL" status="error" class="!mb-0" />
          </div>
          <div class="relative">
            <div class="charts">
              <div class="text-[12px] !text-[var(--color-text-4)]">{{ t('report.passRate') }}</div>
              <div class="flex justify-center text-[16px] font-medium">
                <div class="one-line-text max-w-[100px] text-[var(--color-text-1)]">{{ functionCasePassRate }} </div>
              </div>
            </div>
            <div class="flex h-full w-full items-center justify-center">
              <MsChart width="100px" height="100px" :options="functionCaseOptions" />
            </div>
          </div>
        </div>
      </div>
    </SystemTrigger>
    <SystemTrigger :is-preview="props.isPreview">
      <div v-if="apiCaseTotal" :class="`${getAnalysisHover} analysis min-w-[330px]`">
        <div class="block-title">{{ t('report.detail.apiUseCaseAnalysis') }}</div>
        <div class="flex">
          <div class="mr-[24px] flex-1">
            <SingleStatusProgress type="API" :detail="detail" status="pending" />
            <SingleStatusProgress type="API" :detail="detail" status="success" />
            <SingleStatusProgress type="API" :detail="detail" status="fakeError" />
            <SingleStatusProgress type="API" :detail="detail" status="error" class="!mb-0" />
          </div>
          <div class="relative">
            <div class="charts">
              <div class="text-[12px] !text-[var(--color-text-4)]">{{ t('report.passRate') }}</div>
              <div class="flex justify-center text-[16px] font-medium">
                <div class="one-line-text max-w-[100px] text-[var(--color-text-1)]">{{ apiCasePassRate }} </div>
              </div>
            </div>
            <div class="flex h-full w-full items-center justify-center">
              <MsChart width="100px" height="100px" :options="apiCaseOptions" />
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
          <div class="mr-[24px] flex-1">
            <SingleStatusProgress type="SCENARIO" :detail="detail" status="pending" />
            <SingleStatusProgress type="SCENARIO" :detail="detail" status="success" />
            <SingleStatusProgress type="SCENARIO" :detail="detail" status="fakeError" />
            <SingleStatusProgress type="SCENARIO" :detail="detail" status="error" class="!mb-0" />
          </div>
          <div class="relative">
            <div class="charts">
              <div class="text-[12px] !text-[var(--color-text-4)]">{{ t('report.passRate') }}</div>
              <div class="flex justify-center text-[16px] font-medium">
                <div class="one-line-text max-w-[100px] text-[var(--color-text-1)]">{{ scenarioCasePassRate }} </div>
              </div>
            </div>
            <div class="flex h-full w-full items-center justify-center">
              <MsChart width="100px" height="100px" :options="scenarioCaseOptions" />
            </div>
          </div>
        </div>
      </div>
    </SystemTrigger>
  </div>

  <div class="drag-container mt-[16px]">
    <VueDraggable v-model="innerCardList" :disabled="props.isPreview" group="report">
      <div
        v-for="(item, index) of innerCardList"
        v-show="showItem(item)"
        :key="item.id"
        :class="`${props.isPreview ? '' : 'hover-card'} card-item mt-[16px]`"
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
              <MsIcon type="icon-icon_delete-trash_outlined1" size="16" @click="deleteCard(item)" />
            </a-tooltip>
          </div>
        </div>
        <div class="wrapper-preview-card">
          <div class="flex items-center justify-between">
            <div v-if="item.value === ReportCardTypeEnum.SUMMARY" class="mb-[8px] font-medium">
              {{ t(item.label) }}
            </div>
          </div>
          <ReportDetailTable
            v-if="item.value === ReportCardTypeEnum.SUB_PLAN_DETAIL"
            :report-id="detail.id"
            :share-id="shareId"
            :is-preview="props.isPreview"
            :label="t(item.label)"
          />
          <Summary
            v-if="item.value === ReportCardTypeEnum.SUMMARY"
            :rich-text="getContent(item)"
            :share-id="shareId"
            :is-preview="props.isPreview"
            :can-edit="item.enableEdit"
            :is-plan-group="props.isGroup"
            :detail="detail"
            @update-summary="(formValue:customValueForm) => updateCustom(formValue, item)"
            @cancel="() => handleCancelCustom(item)"
            @handle-summary="(value:string) => handleSummary(value,item)"
            @handle-click="handleClick(item)"
            @handle-set-save="setIsSave(false)"
          />
          <BugTable
            v-else-if="item.value === ReportCardTypeEnum.BUG_DETAIL"
            :report-id="detail.id"
            :share-id="shareId"
            :is-preview="props.isPreview"
            :label="t(item.label)"
          />
          <TestSetTableIndex
            v-else-if="hasTestSet.includes(item.value)"
            :report-id="detail.id"
            :share-id="shareId"
            :active-type="item.value"
            :is-preview="props.isPreview"
            :label="t(item.label)"
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
            @handle-click="handleClick(item)"
            @cancel="() => handleCancelCustom(item)"
            @handle-set-save="setIsSave(false)"
          />
        </div>
      </div>
    </VueDraggable>
  </div>
</template>

<script setup lang="ts">
  import { useRoute } from 'vue-router';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';
  import { VueDraggable } from 'vue-draggable-plus';

  import MsChart from '@/components/pure/chart/index.vue';
  import SingleStatusProgress from '@/views/test-plan/report/component/singleStatusProgress.vue';
  import CustomRichText from '@/views/test-plan/report/detail/component/custom-card/customRichText.vue';
  import BugTable from '@/views/test-plan/report/detail/component/system-card/bugTable.vue';
  import ExecuteAnalysis from '@/views/test-plan/report/detail/component/system-card/executeAnalysis.vue';
  import ReportDetailTable from '@/views/test-plan/report/detail/component/system-card/reportDetailTable.vue';
  import ReportHeader from '@/views/test-plan/report/detail/component/system-card/reportHeader.vue';
  import ReportMetricsItem from '@/views/test-plan/report/detail/component/system-card/ReportMetricsItem.vue';
  import Summary from '@/views/test-plan/report/detail/component/system-card/summary.vue';
  import SystemTrigger from '@/views/test-plan/report/detail/component/system-card/systemTrigger.vue';
  import TestSetTableIndex from '@/views/test-plan/report/detail/component/system-card/testTableIndex.vue';

  import { getReportLayout, updateReportDetail } from '@/api/modules/test-plan/report';
  import getVisualThemeColor from '@/config/chartTheme';
  import {
    commonConfig,
    defaultCount,
    defaultReportDetail,
    seriesConfig,
    statusConfig,
    toolTipConfig,
  } from '@/config/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useLeaveUnSaveTip from '@/hooks/useLeaveUnSaveTip';
  import { useAppStore } from '@/store';
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

  const appStore = useAppStore();

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

  const { setIsSave } = useLeaveUnSaveTip({
    leaveTitle: 'common.tip',
    leaveContent: 'common.editUnsavedLeave',
    tipType: 'warning',
  });

  const innerCardList = defineModel<configItem[]>('cardList', {
    default: [],
  });

  const detail = ref<PlanReportDetail>({ ...cloneDeep(defaultReportDetail) });

  const richText = ref<{ summary: string; richTextTmpFileIds?: string[] }>({
    summary: '',
  });

  const reportForm = ref({
    reportName: '',
  });

  const getAnalysisHover = computed(() => (props.isPreview ? '' : 'hover-analysis cursor-not-allowed'));

  const isPlanGroup = ref(props.isGroup);
  provide('isPlanGroup', isPlanGroup);
  const hasTestSet = [
    ReportCardTypeEnum.FUNCTIONAL_DETAIL,
    ReportCardTypeEnum.API_CASE_DETAIL,
    ReportCardTypeEnum.SCENARIO_CASE_DETAIL,
  ];
  /**
   * 分享share
   */
  const shareId = ref<string>(route.query.shareId as string);

  // 功能用例分析
  const functionCaseOptions = ref<Record<string, any>>({
    ...commonConfig,
    tooltip: {
      ...toolTipConfig,
    },
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
  const apiCaseOptions = ref<Record<string, any>>({
    ...commonConfig,
    tooltip: {
      ...toolTipConfig,
    },
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
  const scenarioCaseOptions = ref<Record<string, any>>({
    ...commonConfig,
    tooltip: {
      ...toolTipConfig,
    },
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
    const chartBorderWidth = valueList.filter((e) => Number(caseCountDetail[e.value]) > 0).length === 1 ? 0 : 2;

    const lastValueList = valueList
      .filter((item) => caseCountDetail[item.value] > 0)
      .map((item: StatusListType) => {
        const color = item.color === '#D4D4D8' ? getVisualThemeColor('initItemStyleColor') : item.color;
        return {
          value: caseCountDetail[item.value] || 0,
          name: t(item.label),
          itemStyle: {
            color: success ? color : getVisualThemeColor('initItemStyleColor'),
            borderWidth: chartBorderWidth,
            borderColor: getVisualThemeColor('itemStyleBorderColor'),
          },
        };
      });

    return lastValueList.length
      ? lastValueList
      : [
          {
            value: 1,
            tooltip: {
              show: false,
            },
            itemStyle: {
              color: getVisualThemeColor('initItemStyleColor'),
            },
          },
        ];
  }

  // 初始化图表
  function initOptionsData() {
    const { functionalCount, apiCaseCount, apiScenarioCount } = detail.value;
    functionCaseOptions.value.series.data = getPassRateData(functionalCount);
    apiCaseOptions.value.series.data = getPassRateData(apiCaseCount);
    scenarioCaseOptions.value.series.data = getPassRateData(apiScenarioCount);
  }

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
  const functionalCaseTotal = computed(() =>
    props.isPreview ? detail.value.functionalTotal : getSummaryDetail(detail.value.functionalCount).caseTotal
  );
  const apiCaseTotal = computed(() =>
    props.isPreview ? detail.value.apiCaseTotal : getSummaryDetail(detail.value.apiCaseCount).caseTotal
  );
  const scenarioCaseTotal = computed(() =>
    props.isPreview ? detail.value.apiScenarioTotal : getSummaryDetail(detail.value.apiScenarioCount).caseTotal
  );

  const reportAnalysisList = computed<ReportMetricsItemModel[]>(() => {
    if (props.isGroup) {
      return [
        {
          name: t('report.detail.testPlanTotal'),
          value: detail.value.planCount,
          unit: t('report.detail.number'),
          icon: 'plan_total',
        },
        {
          name: t('report.detail.testPlanCaseTotal'),
          value: detail.value.caseTotal,
          unit: t('report.detail.number'),
          icon: 'case_total',
        },
        {
          name: t('report.passRate'),
          value: detail.value.passRate,
          unit: '%',
          icon: 'passRate',
        },
        {
          name: t('report.detail.totalDefects'),
          value: addCommasToNumber(detail.value.bugCount),
          unit: t('report.detail.number'),
          icon: 'bugTotal',
        },
      ];
    }
    return [
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
    ];
  });

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
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const isDefaultLayout = ref<boolean>(false);

  watch(
    () => props.detailInfo,
    (val) => {
      if (val) {
        detail.value = cloneDeep(props.detailInfo);
        const { defaultLayout, id, name, summary } = detail.value;
        isDefaultLayout.value = defaultLayout;
        richText.value.summary = summary;
        reportForm.value.reportName = name;
        initOptionsData();
        if (props.isPreview) {
          if (!defaultLayout && id) {
            getDefaultLayout();
          } else {
            innerCardList.value = props.isGroup ? cloneDeep(defaultGroupConfig) : cloneDeep(defaultSingleConfig);
          }
        }
      }
    },
    { deep: true }
  );

  // 获取内容详情
  function getContent(item: configItem): customValueForm {
    if (isDefaultLayout.value) {
      return {
        content: richText.value.summary || '',
        label: t(item.label),
        richTextTmpFileIds: [],
      };
    }
    return {
      content: item.content || '',
      label: t(item.label),
      richTextTmpFileIds: item.richTextTmpFileIds,
    };
  }

  function handleCancelCustom(cardItem: configItem) {
    const originItem = originLayoutInfo.value.find((item: configItem) => item.id === cardItem.id);
    const index = originLayoutInfo.value.findIndex((e: configItem) => e.id === cardItem.id);
    if (originItem && index !== -1) {
      innerCardList.value.splice(index, 1, originItem);
    }
    cardItem.enableEdit = false;
    if (props.isPreview) {
      setIsSave(true);
    }
    if (isDefaultLayout.value) {
      cardItem.content = detail.value.summary;
      richText.value.summary = detail.value.summary;
    }
  }

  function handleSummary(content: string, cardItem: configItem) {
    if (isDefaultLayout.value) {
      richText.value.summary = content;
    } else {
      cardItem.content = content;
    }
  }

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
    setIsSave(false);
  }
  // 删除卡片
  const deleteCard = (cardItem: configItem) => {
    innerCardList.value = innerCardList.value.filter((item) => item.id !== cardItem.id);
    setIsSave(false);
  };

  // 编辑模式和预览模式切换
  function editField(cardItem: configItem) {
    if (allowEditType.includes(cardItem.value)) {
      cardItem.enableEdit = !cardItem.enableEdit;
    }
  }

  function handleClick(cardItem: configItem) {
    cardItem.enableEdit = true;
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
      setIsSave(true);
      currentItem.enableEdit = false;
      emit('updateSuccess');
    } catch (error) {
      // eslint-disable-next-line no-console
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

  watch(
    () => appStore.isDarkTheme,
    () => {
      initOptionsData();
    }
  );

  defineExpose({
    setIsSave,
  });
</script>

<style scoped lang="less">
  .report-name {
    padding: 0 16px;
    height: 56px;
    @apply flex items-center justify-between border-b;

    border-color: var(--color-text-n8);
    background-color: var(--color-text-fff);
  }
  .block-title {
    @apply mb-4 font-medium;
  }
  .analysis-wrapper {
    @apply mb-4 grid items-center gap-4;
    .analysis {
      padding: 24px;
      height: 250px;
      border: 1px solid transparent;
      background-color: var(--color-text-fff);
      box-shadow: 0 0 10px rgba(120 56 135/ 5%);
      @apply rounded-xl;
      .charts {
        @apply absolute text-center;

        top: 50%;
        right: 0;
        bottom: 0;
        left: 50%;
        z-index: 99;
        width: 70px;
        height: 42px;
        transform: translateY(-50%) translateX(-50%);
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
      width: 100%;
      border: 1px solid transparent;
      border-radius: 12px;
      .action {
        position: absolute;
        top: -14px;
        left: 50%;
        transform: translateX(-50%);
        z-index: 9 !important;
        background: var(--color-text-fff);
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
  .wrapper-preview-card {
    display: flex;
    padding: 16px;
    border-radius: 12px;
    background-color: var(--color-text-fff);
    box-shadow: 0 0 10px rgb(120 56 135 / 5%);
    @apply flex-col;
  }
</style>
