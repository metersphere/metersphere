<template>
  <MsCard class="mb-[16px]" hide-back hide-footer auto-height no-content-padding hide-divider>
    <template #headerLeft>
      <div v-if="route.query.id" class="flex items-center font-medium"
        >{{ t('report.name') }}
        <a-tooltip :content="detail.name" :mouse-enter-delay="300"
          ><div class="one-line-text max-w-[300px]">{{ detail.name }}</div>
        </a-tooltip>
      </div>
    </template>
    <template #headerRight>
      <a-popover position="bottom" content-class="response-popover-content">
        <div>
          <span class="text-[var(--color-text-4)]">{{ t('report.detail.api.executionTime') }}</span>
          {{ detail.executeTime ? dayjs(detail.executeTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
          <span class="text-[var(--color-text-4)]">{{ t('report.detail.api.executionTimeTo') }}</span>
          {{ detail.endTime ? dayjs(detail.endTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
        </div>
        <template #content>
          <div class="max-w-[400px] items-center gap-[8px] text-[14px]">
            <div class="flex-shrink-0 text-[var(--color-text-4)]">{{ t('report.detail.api.executionTime') }}</div>
            <div class="mt-2">
              {{ dayjs(detail.executeTime).format('YYYY-MM-DD HH:mm:ss') }}
            </div>
          </div>
        </template>
      </a-popover>
      <MsButton
        v-permission="['PROJECT_API_REPORT:READ+SHARE']"
        type="icon"
        status="secondary"
        class="ml-4 !rounded-[var(--border-radius-small)]"
        :loading="shareLoading"
        @click="shareHandler"
      >
        <MsIcon type="icon-icon_share1" class="mr-2 font-[16px]" />
        {{ t('common.share') }}
      </MsButton>
    </template>
  </MsCard>
  <div class="analysis-wrapper">
    <div class="analysis">
      <div>
        <div class="block-title">{{ t('report.detail.api.requestAnalysis') }}</div>
        <ul class="report-analysis">
          <li class="report-analysis-item">
            <div class="report-analysis-item-icon">
              <svg-icon class="mr-2" width="24px" height="24px" name="threshold" />
              <span>{{ t('report.detail.threshold') }}</span>
            </div>
            <div>
              <span class="report-analysis-item-number">{{ detail.passThreshold }}</span>
              <span class="report-analysis-item-unit">(%)</span>
            </div>
          </li>
          <li class="report-analysis-item">
            <div class="report-analysis-item-icon">
              <svg-icon class="mr-2" width="24px" height="24px" name="passRate" />
              <span>{{ t('report.detail.reportPassRate') }}</span>
            </div>
            <div>
              <span class="report-analysis-item-number">{{ detail.passRate }}</span>
              <span class="report-analysis-item-unit"> (%)</span>
            </div>
          </li>
          <li class="report-analysis-item">
            <div class="report-analysis-item-icon">
              <svg-icon class="mr-2" width="24px" height="24px" name="passRate" />
              <span>{{ t('report.detail.performCompletion') }}</span>
            </div>
            <div>
              <span class="report-analysis-item-number">{{ detail.executeRate }}</span>
              <span class="report-analysis-item-unit">(%)</span>
            </div>
          </li>
          <li class="report-analysis-item">
            <div class="report-analysis-item-icon">
              <svg-icon class="mr-2" width="24px" height="24px" name="bugTotal" />
              <span>{{ t('report.detail.totalDefects') }}</span>
            </div>
            <div>
              <span class="report-analysis-item-number">{{ addCommasToNumber(detail.bugCount) }}</span>
              <span class="report-analysis-item-unit">({{ t('report.detail.number') }})</span>
            </div>
          </li>
        </ul>
      </div>
    </div>
    <div class="analysis mx-4">
      <div>
        <div class="block-title">{{ t('report.detail.executionAnalysis') }}</div>
        <SetReportChart
          size="160px"
          offset="top-[34%] right-0 bottom-0 left-0"
          :legend-data="legendData"
          :options="charOptions"
          :request-total="getIndicators(detail.caseTotal) || 0"
        />
      </div>
    </div>
    <div class="analysis">
      <div>
        <div class="block-title">{{ t('report.detail.useCaseAnalysis') }}</div>
        <div class="flex">
          <div class="w-[70%]">
            <SingleStatusProgress :detail="detail" status="pending" />
            <SingleStatusProgress :detail="detail" status="success" />
            <SingleStatusProgress :detail="detail" status="block" />
            <SingleStatusProgress :detail="detail" status="error" />
          </div>
          <div class="relative w-[30%]">
            <div class="charts absolute w-full text-center">
              <div class="text-[12px] !text-[var(--color-text-4)]">{{ t('report.passRate') }}</div>
              <a-popover position="bottom" content-class="response-popover-content">
                <div class="flex justify-center text-[18px] font-medium">
                  <div class="one-line-text max-w-[60px] text-[var(--color-text-1)]">{{ detail.passRate }}% </div>
                </div>
                <template #content>
                  <div class="min-w-[95px] max-w-[400px] p-4 text-[14px]">
                    <div class="text-[12px] font-medium text-[var(--color-text-4)]">{{ t('report.passRate') }}</div>
                    <div class="mt-2 text-[18px] font-medium text-[var(--color-text-1)]">{{ detail.passRate }} %</div>
                  </div>
                </template>
              </a-popover>
            </div>
            <div class="flex h-full w-full items-center justify-center">
              <MsChart width="120px" height="120px" :options="functionCaseOptions"
            /></div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <MsCard class="mb-[16px]" simple auto-height>
    <div class="font-medium">{{ t('report.detail.reportSummary') }}</div>
    <MsRichText
      v-model:raw="richText.summary"
      v-model:filedIds="richText.richTextTmpFileIds"
      :upload-image="handleUploadImage"
      :preview-url="PreviewEditorImageUrl"
      class="mt-[8px] w-full"
    />
    <div v-show="showButton" class="mt-[16px] flex items-center gap-[12px]">
      <a-button type="primary" @click="handleUpdateReportDetail">{{ t('common.save') }}</a-button>
      <a-button type="secondary" @click="handleCancel">{{ t('common.cancel') }}</a-button>
    </div>
  </MsCard>
  <MsCard simple auto-height>
    <MsTab
      v-model:active-key="activeTab"
      :show-badge="false"
      :content-tab-list="contentTabList"
      no-content
      class="relative mb-[16px] border-b"
    />
    <BugTable v-if="activeTab === 'bug'" :report-id="reportId" :share-id="shareId" />
    <FeatureCaseTable v-if="activeTab === 'featureCase'" :report-id="reportId" :share-id="shareId" />
  </MsCard>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { useEventListener } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';
  import dayjs from 'dayjs';

  import MsChart from '@/components/pure/chart/index.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';
  import MsTab from '@/components/pure/ms-tab/index.vue';
  import SingleStatusProgress from '../component/singleStatusProgress.vue';
  import BugTable from './component/bugTable.vue';
  import FeatureCaseTable from './component/featureCaseTable.vue';
  import SetReportChart from '@/views/api-test/report/component/case/setReportChart.vue';

  import { editorUploadFile, getReportDetail, updateReportDetail } from '@/api/modules/test-plan/report';
  import { PreviewEditorImageUrl } from '@/api/requrls/case-management/featureCase';
  import { defaultReportDetail, statusConfig } from '@/config/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import { addCommasToNumber } from '@/utils';

  import type { LegendData } from '@/models/apiTest/report';
  import type { PlanReportDetail, StatusListType } from '@/models/testPlan/testPlanReport';

  import { getIndicators } from '@/views/api-test/report/utils';

  const { t } = useI18n();

  const route = useRoute();

  const reportId = ref<string>(route.query.id as string);
  const shareId = ref<string>(route.query.shareId as string);

  const detail = ref<PlanReportDetail>({ ...cloneDeep(defaultReportDetail) });
  const showButton = ref(false);
  const richText = ref<{ summary: string; richTextTmpFileIds?: string[] }>({
    summary: '',
  });

  // 分享
  const shareLoading = ref<boolean>(false);

  function shareHandler() {}

  const legendData = ref<LegendData[]>([]);

  const charOptions = ref({
    tooltip: {
      show: false,
      trigger: 'item',
    },
    legend: {
      show: false,
    },
    series: {
      name: '',
      type: 'pie',
      radius: ['65%', '80%'],
      avoidLabelOverlap: false,
      label: {
        show: false,
        position: 'center',
      },
      emphasis: {
        label: {
          show: false,
          fontSize: 40,
          fontWeight: 'bold',
        },
      },
      labelLine: {
        show: false,
      },
      data: [
        {
          value: 0,
          name: t('common.success'),
          itemStyle: {
            color: '#00C261',
          },
        },
        {
          value: 0,
          name: t('common.fakeError'),
          itemStyle: {
            color: '#FFC14E',
          },
        },
        {
          value: 0,
          name: t('common.fail'),
          itemStyle: {
            color: '#ED0303',
          },
        },
        {
          value: 0,
          name: t('common.unExecute'),
          itemStyle: {
            color: '#D4D4D8',
          },
        },
        {
          value: 0,
          name: t('common.block'),
          itemStyle: {
            color: '#B379C8',
          },
        },
      ],
    },
  });

  const functionCaseOptions = ref({
    tooltip: {
      show: false,
      trigger: 'item',
    },
    legend: {
      show: false,
    },
    series: {
      name: '',
      type: 'pie',
      radius: ['65%', '80%'],
      avoidLabelOverlap: false,
      label: {
        show: false,
        position: 'center',
      },
      emphasis: {
        label: {
          show: false,
          fontSize: 40,
          fontWeight: 'bold',
        },
      },
      labelLine: {
        show: false,
      },
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

  // 初始化图表
  function initOptionsData() {
    charOptions.value.series.data = statusConfig.map((item: StatusListType) => {
      return {
        value: detail.value.executeCount[item.value] || 0,
        name: t(item.label),
        itemStyle: {
          color: item.color,
        },
      };
    });
    legendData.value = statusConfig.map((item: StatusListType) => {
      const rate = (detail.value.executeCount[item.value] / detail.value.caseTotal) * 100;
      return {
        ...item,
        label: t(item.label),
        count: detail.value.executeCount[item.value] || 0,
        rote: `${Number.isNaN(rate) ? 0 : rate.toFixed(2)}%`,
      };
    }) as unknown as LegendData[];

    functionCaseOptions.value.series.data = statusConfig.map((item: StatusListType) => {
      return {
        value: detail.value.functionalCount[item.value] || 0,
        name: t(item.label),
        itemStyle: {
          color: item.color,
        },
      };
    });
  }

  async function getDetail() {
    try {
      detail.value = await getReportDetail(reportId.value);
      richText.value = { summary: detail.value.summary };
    } catch (error) {
      console.log(error);
    }
  }

  onMounted(async () => {
    nextTick(() => {
      const editorContent = document.querySelector('.editor-content');
      useEventListener(editorContent, 'click', () => {
        showButton.value = true;
      });
    });
    await getDetail();
    initOptionsData();
  });

  async function handleUploadImage(file: File) {
    const { data } = await editorUploadFile({
      fileList: [file],
    });
    return data;
  }
  async function handleUpdateReportDetail() {
    try {
      await updateReportDetail({
        id: detail.value.id,
        summary: richText.value.summary,
        richTextTmpFileIds: richText.value.richTextTmpFileIds ?? [],
      });
      Message.success(t('common.updateSuccess'));
      showButton.value = false;
      await getDetail();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }
  function handleCancel() {
    richText.value = { summary: detail.value.summary };
    showButton.value = false;
  }

  const activeTab = ref('bug');
  const contentTabList = ref([
    {
      value: 'bug',
      label: t('report.detail.bugDetails'),
    },
    {
      value: 'featureCase',
      label: t('report.detail.featureCaseDetails'),
    },
  ]);
</script>

<style scoped lang="less">
  .block-title {
    @apply mb-4 font-medium;
  }
  .analysis-wrapper {
    height: 250px;
    @apply mb-4 flex items-center;
    .analysis {
      padding: 24px;
      box-shadow: 0 0 10px rgba(120 56 135/ 5%);
      @apply h-full flex-1 rounded-xl bg-white;
      .report-analysis {
        .report-analysis-item {
          padding: 4px 8px;
          border-radius: 4px;
          background-color: var(--color-text-n9);
          @apply mb-3 flex items-center justify-between;
          .report-analysis-item-icon {
            @apply flex items-center;
          }
          .report-analysis-item-number {
            @apply font-medium;
          }
          .report-analysis-item-unit {
            color: var(--color-text-4);
            @apply ml-1;
          }
        }
      }
      .charts {
        top: 34%;
        right: 0;
        bottom: 0;
        left: 0;
        z-index: 99;
        margin: auto;
      }
    }
  }
  :deep(.rich-wrapper) .halo-rich-text-editor .ProseMirror {
    height: 58px;
  }
</style>
