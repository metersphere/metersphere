<template>
  <ReportHeader v-if="!props.isDrawer" :detail="detail" :share-id="shareId" is-group />
  <div class="analysis-wrapper">
    <div class="analysis min-w-[238px]">
      <div class="block-title">{{ t('report.detail.api.reportAnalysis') }}</div>
      <ReportMetricsItem
        v-for="analysisItem in reportAnalysisList"
        :key="analysisItem.name"
        :item-info="analysisItem"
      />
    </div>
    <div class="analysis min-w-[410px]">
      <ExecuteAnalysis :detail="detail" />
    </div>
  </div>
  <Summary
    v-model:richText="richText"
    :share-id="shareId"
    :show-button="showButton"
    :is-plan-group="true"
    :detail="detail"
    @update-summary="handleUpdateReportDetail"
    @cancel="handleCancel"
    @handle-summary="handleSummary"
  />
  <MsCard simple auto-height auto-width>
    <div class="mb-[16px] flex items-center justify-between">
      <div class="block-title">{{ t('report.detail.api.reportDetail') }}</div>
      <a-radio-group class="mb-2" :model-value="currentMode" type="button" @change="handleModeChange">
        <a-radio value="drawer">
          <div class="mode-button">
            <MsIcon :class="{ 'active-color': currentMode === 'drawer' }" type="icon-icon_drawer" />
            <span class="mode-button-title">{{ t('msTable.columnSetting.drawer') }}</span>
          </div>
        </a-radio>
        <a-radio value="new_window">
          <div class="mode-button">
            <MsIcon :class="{ 'active-color': currentMode === 'new_window' }" type="icon-icon_into-item_outlined" />
            <span class="mode-button-title">{{ t('msTable.columnSetting.newWindow') }}</span>
          </div>
        </a-radio>
      </a-radio-group>
    </div>
    <ReportDetailTable :current-mode="currentMode" :report-id="detail.id" :share-id="shareId" />
  </MsCard>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { useEventListener } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import ExecuteAnalysis from '@/views/test-plan/report/detail/component/executeAnalysis.vue';
  import ReportDetailTable from '@/views/test-plan/report/detail/component/reportDetailTable.vue';
  import ReportHeader from '@/views/test-plan/report/detail/component/reportHeader.vue';
  import ReportMetricsItem from '@/views/test-plan/report/detail/component/ReportMetricsItem.vue';
  import Summary from '@/views/test-plan/report/detail/component/summary.vue';

  import { updateReportDetail } from '@/api/modules/test-plan/report';
  import { defaultReportDetail } from '@/config/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import { addCommasToNumber } from '@/utils';

  import type { PlanReportDetail, ReportMetricsItemModel } from '@/models/testPlan/testPlanReport';

  const { t } = useI18n();

  const route = useRoute();
  const props = defineProps<{
    detailInfo: PlanReportDetail;
    isDrawer?: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'updateSuccess'): void;
  }>();

  const detail = ref<PlanReportDetail>({ ...cloneDeep(defaultReportDetail) });
  const shareId = ref<string>(route.query.shareId as string);

  const reportAnalysisList = computed<ReportMetricsItemModel[]>(() => [
    {
      name: t('report.detail.testPlanTotal'),
      value: addCommasToNumber(detail.value.planCount),
      unit: t('report.detail.number'),
      icon: 'plan_total',
    },
    {
      name: t('report.detail.testPlanCaseTotal'),
      value: addCommasToNumber(detail.value.caseTotal),
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
  ]);

  const showButton = ref(false);

  const richText = ref<{ summary: string; richTextTmpFileIds?: string[] }>({
    summary: '',
  });

  async function handleUpdateReportDetail() {
    try {
      await updateReportDetail({
        id: detail.value.id,
        summary: richText.value.summary,
        richTextTmpFileIds: richText.value.richTextTmpFileIds ?? [],
      });
      Message.success(t('common.updateSuccess'));
      showButton.value = false;
      emit('updateSuccess');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  function handleCancel() {
    richText.value = { summary: detail.value.summary };
    showButton.value = false;
  }

  function handleSummary(content: string) {
    richText.value.summary = content;
  }

  const currentMode = ref<string>('drawer');
  const handleModeChange = (value: string | number | boolean) => {
    currentMode.value = value as string;
  };

  watchEffect(() => {
    if (props.detailInfo) {
      detail.value = cloneDeep(props.detailInfo);
      richText.value.summary = detail.value.summary;
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
</script>

<style scoped lang="less">
  .block-title {
    @apply mb-4 font-medium;
  }
  .analysis-wrapper {
    @apply mb-4 flex flex-wrap items-center gap-4;
    .analysis {
      padding: 24px;
      height: 250px;
      box-shadow: 0 0 10px rgba(120 56 135/ 5%);
      @apply flex-1 rounded-xl bg-white;
      .charts {
        top: 36%;
        right: 0;
        bottom: 0;
        left: 0;
        z-index: 99;
        margin: auto;
      }
    }
  }
</style>
