<template>
  <div class="report-container h-full">
    <!-- 报告参数开始 -->
    <ReportDetailHeader :detail="detail" show-type="CASE" />
    <!-- 报告参数结束 -->
    <!-- 报告分析开始 -->
    <div class="analyze">
      <!-- 请求分析 -->
      <div class="block-title">{{ t('report.detail.api.requestAnalysis') }}</div>
      <div class="flex justify-between">
        <div class="request-analyze">
          <SetReportChart
            :legend-data="legendData"
            :options="charOptions"
            :request-total="getIndicators(detail.total) || 0"
          />
        </div>
        <!-- 耗时分析 -->
        <div class="time-analyze gap-[12px]">
          <div class="time-card flex-1 gap-[12px]">
            <div class="time-card-item">
              <MsIcon type="icon-icon_time_outlined" class="mr-[4px] text-[var(--color-text-4)]" size="16" />
              <span class="time-card-item-title">{{ t('report.detail.api.totalTime') }}</span>
              <a-popover position="bottom" content-class="response-popover-content">
                <span class="count">{{ getTotalTime.split('-')[0] }}</span
                ><span class="time-card-item-title">{{ getTotalTime.split('-')[1] || 'ms' }}</span>
                <template #content>
                  <div class="min-w-[140px] max-w-[400px] p-4 text-[14px]">
                    <div class="text-[var(--color-text-4)]">{{ t('report.detail.api.totalTime') }}</div>
                    <div class="mt-2 text-[var(--color-text-1)]">
                      <span class="text-[18px] font-medium">{{ getTotalTime.split('-')[0] }}</span
                      >{{ getTotalTime.split('-')[1] || 'ms' }}</div
                    >
                  </div>
                </template>
              </a-popover>
            </div>
            <div class="time-card-item h-full">
              <MsIcon type="icon-icon_time_outlined" class="mr-[4px] text-[var(--color-text-4)]" size="16" />
              <span class="time-card-item-title"> {{ t('report.detail.api.requestTotalTime') }}</span>
              <a-popover position="bottom" content-class="response-popover-content">
                <div class="flex items-end">
                  <div class="count">
                    {{ detail.requestDuration !== null ? formatDuration(detail.requestDuration).split('-')[0] : '0' }}
                  </div>
                  <div class="time-card-item-title">
                    {{ detail.requestDuration !== null ? formatDuration(detail.requestDuration).split('-')[1] : 'ms' }}
                  </div>
                </div>

                <template #content>
                  <div class="min-w-[140px] max-w-[400px] p-4 text-[14px]">
                    <div class="text-[var(--color-text-4)]">{{ t('report.detail.api.requestTotalTime') }}</div>
                    <div class="mt-2 text-[var(--color-text-1)]">
                      <span class="text-[18px] font-medium">{{
                        detail.requestDuration !== null ? formatDuration(detail.requestDuration).split('-')[0] : '0'
                      }}</span
                      >{{
                        detail.requestDuration !== null ? formatDuration(detail.requestDuration).split('-')[1] : 'ms'
                      }}</div
                    >
                  </div>
                </template>
              </a-popover>
            </div>
          </div>

          <div class="time-card flex-1 gap-4">
            <!-- 执行率 -->
            <div v-if="detail.integrated" class="time-card-item-rote">
              <div class="time-card-item-rote-title">
                <MsIcon type="icon-icon_yes_outlined" class="mr-[4px] text-[var(--color-text-4)]" size="16" />
                {{ t('report.detail.api.executionRate') }}
              </div>
              <div class="flex items-center">
                <a-popover position="bottom" content-class="response-popover-content">
                  <div class="count one-line-text max-w-[80px]"> {{ getExcuteRate() }} </div
                  ><span v-show="getExcuteRate() !== 'Calculating'">%</span>
                  <a-divider direction="vertical" class="!h-[16px]" :margin="8"></a-divider>
                  <span>{{ getIndicators(getRequestEacuteCount) }}</span>
                  <span class="mx-1 text-[var(--color-text-4)]">/ {{ getIndicators(getRequestTotalCount) }}</span>
                  <template #content>
                    <div class="min-w-[190px] max-w-[400px] p-4 text-[14px]">
                      <div class="text-[var(--color-text-4)]">{{ t('report.detail.api.executionRate') }}</div>
                      <div class="mt-2 flex items-center justify-between">
                        <div class="count text-[18px] font-medium">
                          {{ getExcuteRate() }} <span v-show="getExcuteRate() !== 'Calculating'">%</span>
                        </div>
                        <div>
                          <span>{{ getIndicators(getRequestEacuteCount) }}</span>
                          <span class="mx-1 text-[var(--color-text-4)]"
                            >/ {{ getIndicators(getRequestTotalCount) }}</span
                          >
                        </div>
                      </div>
                    </div>
                  </template>
                </a-popover>
              </div>
            </div>
            <div class="time-card-item-rote">
              <div class="time-card-item-rote-title">
                <MsIcon type="icon-icon_yes_outlined" class="mr-[4px] text-[var(--color-text-4)]" size="16" />
                {{ t('report.detail.api.assertPass') }}
              </div>

              <div class="flex items-center">
                <a-popover position="bottom" content-class="response-popover-content">
                  <div class="flex items-center">
                    <div class="count one-line-text max-w-[80px]">{{ detail.assertionPassRate || '0.00' }}</div
                    ><span v-show="detail.assertionPassRate !== 'Calculating'" class="ml-1">%</span>
                    <a-divider direction="vertical" class="!h-[16px]" :margin="8"></a-divider>
                    <div class="one-line-text max-w-[80px]">{{
                      getIndicators(detail.assertionSuccessCount) !== 'Calculating'
                        ? addCommasToNumber(detail.assertionSuccessCount || 0)
                        : getIndicators(detail.assertionSuccessCount)
                    }}</div>
                    <span class="mx-1 text-[var(--color-text-4)]">/</span>
                    <div class="one-line-text max-w-[80px]">
                      {{
                        getIndicators(detail.assertionCount) !== 'Calculating'
                          ? addCommasToNumber(detail.assertionCount)
                          : getIndicators(detail.assertionCount)
                      }}</div
                    >
                  </div>

                  <template #content>
                    <div class="min-w-[190px] max-w-[400px] p-4 text-[14px]">
                      <div class="text-[var(--color-text-4)]">{{ t('report.detail.api.assertPass') }}</div>
                      <div class="mt-2 flex items-center justify-between">
                        <div class="text-[18px] font-medium text-[var(--color-text-1)]"
                          >{{ getIndicators(detail.assertionPassRate) }}
                          <span v-show="detail.assertionPassRate !== 'Calculating'">%</span></div
                        >
                        <div>
                          <span class="text-[var(--color-text-1)]">{{
                            getIndicators(detail.assertionSuccessCount) !== 'Calculating'
                              ? addCommasToNumber(detail.assertionSuccessCount || 0)
                              : getIndicators(detail.assertionSuccessCount)
                          }}</span>
                          <span class="text-[var(--color-text-4)]"
                            ><span class="mx-1">/</span>
                            {{
                              getIndicators(detail.assertionCount) !== 'Calculating'
                                ? addCommasToNumber(detail.assertionCount)
                                : getIndicators(detail.assertionCount)
                            }}</span
                          >
                        </div>
                      </div>
                    </div>
                  </template>
                </a-popover>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!-- 报告步骤分析结束 -->
    <!-- 报告明细开始 -->
    <div class="report-info">
      <reportInfoHeader
        v-model:keywordName="keywordName"
        v-model:keyword="cascaderKeywords"
        v-model:active-tab="activeTab"
        show-type="CASE"
        :is-export="props.isExport"
        @search="searchHandler"
        @reset="resetHandler"
      />
      <TiledList
        ref="tiledListRef"
        v-model:keyword-name="keywordName"
        :key-words="cascaderKeywords"
        show-type="CASE"
        :case-id="props.caseId"
        :case-name="props.caseName"
        :active-type="activeTab"
        :report-detail="detail || []"
        :get-report-step-detail="props.getReportStepDetail"
        :is-filter-step="props.isFilterStep"
        :is-export="props.isExport"
      />
    </div>
    <!-- 报告明细结束 -->
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { cloneDeep } from 'lodash-es';

  import SetReportChart from './case/setReportChart.vue';
  import ReportDetailHeader from './reportDetailHeader.vue';
  import reportInfoHeader from './step/reportInfoHeaders.vue';
  import TiledList from './tiledList.vue';

  import { commonConfig, toolTipConfig } from '@/config/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import { addCommasToNumber, formatDuration } from '@/utils';

  import type { LegendData, ReportDetail } from '@/models/apiTest/report';
  import { ExecuteStatusEnum } from '@/enums/taskCenter';

  import { getIndicators } from '../utils';

  const { t } = useI18n();
  const props = defineProps<{
    detailInfo?: ReportDetail;
    getReportStepDetail?: (...args: any) => Promise<any>; // 获取步骤的详情内容接口
    isExport?: boolean;
    isFilterStep?: boolean; // 是否打开抽屉之前过滤用例步骤
    caseName?: string; // 用例名称关键字
    caseId?: string; // 用例id
  }>();

  const detail = ref<ReportDetail>({
    id: '',
    name: '', // 报告名称
    testPlanId: '',
    createUser: '',
    deleteTime: 0,
    deleteUser: '',
    deleted: false,
    updateUser: '',
    updateTime: 0,
    startTime: 0, // 开始时间/同创建时间一致
    endTime: 0, //  结束时间/报告执行完成
    requestDuration: 0, // 请求总耗时
    status: ExecuteStatusEnum.PENDING, // 报告状态/SUCCESS/ERROR
    triggerMode: '', // 触发方式
    runMode: '', // 执行模式
    poolId: '', // 资源池
    poolName: '', // 资源池名称
    versionId: '',
    integrated: false, // 是否是集成报告
    projectId: '',
    environmentId: '', // 环境id
    environmentName: '', // 环境名称
    errorCount: 0, // 失败数
    fakeErrorCount: 0, // 误报数
    pendingCount: 0, // 未执行数
    successCount: 0, // 成功数
    assertionCount: 0, // 总断言数
    assertionSuccessCount: 0, // 成功断言数
    requestErrorRate: '', // 请求失败率
    requestPendingRate: '', // 请求未执行率
    requestFakeErrorRate: '', // 请求误报率
    requestPassRate: '', // 请求通过率
    assertionPassRate: '', // 断言通过率
    scriptIdentifier: '', // 脚本标识
    children: [], // 步骤列表
    stepTotal: 0, // 步骤总数
    console: '',
  });

  const cascaderKeywords = ref<string>('');
  const keywordName = ref<string>(props.caseName || '');

  const getTotalTime = computed(() => {
    if (detail.value) {
      const { endTime, startTime } = detail.value;
      if (endTime && startTime && endTime !== 0 && startTime !== 0) {
        return formatDuration(endTime - startTime);
      }
      return '0';
    }
    return '0';
  });

  const legendData = ref<LegendData[]>([]);
  const charOptions = ref({
    ...commonConfig,
    tooltip: {
      ...toolTipConfig,
    },
    legend: {
      show: false,
    },
    series: {
      name: '',
      type: 'pie',
      radius: ['62%', '80%'],
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
          name: t('common.pass'),
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
      ],
    },
  });
  const activeTab = ref<'tiled' | 'tab'>('tiled');

  function getExcuteRate() {
    if (detail.value.requestPendingRate && detail.value.requestPendingRate !== 'Calculating') {
      return (100 - Number(detail.value.requestPendingRate)).toFixed(2);
    }
    return getIndicators(detail.value.requestPendingRate);
  }

  // 执行数量
  const getRequestEacuteCount = computed(() => {
    const { errorCount, successCount, fakeErrorCount } = detail.value;
    return addCommasToNumber(errorCount + successCount + fakeErrorCount);
  });

  const getRequestTotalCount = computed(() => {
    const { errorCount, successCount, fakeErrorCount, pendingCount } = detail.value;
    return addCommasToNumber(errorCount + successCount + fakeErrorCount + pendingCount);
  });

  function initOptionsData() {
    const tempArr = [
      {
        label: 'common.pass',
        value: 'successCount',
        color: '#00C261',
        class: 'bg-[rgb(var(--success-6))]',
        rateKey: 'requestPassRate',
        key: 'SUCCESS',
      },
      {
        label: 'common.fakeError',
        value: 'fakeErrorCount',
        color: '#FFC14E',
        class: 'bg-[rgb(var(--warning-6))]',
        rateKey: 'requestFakeErrorRate',
        key: 'FAKE_ERROR',
      },
      {
        label: 'common.fail',
        value: 'errorCount',
        color: '#ED0303',
        class: 'bg-[rgb(var(--danger-6))]',
        rateKey: 'requestErrorRate',
        key: 'ERROR',
      },
      {
        label: 'common.unExecute',
        value: 'pendingCount',
        color: '#D4D4D8',
        class: 'bg-[var(--color-text-input-border)]',
        rateKey: 'requestPendingRate',
        key: 'PENDING',
      },
    ];
    let validArr;
    if (props?.detailInfo?.integrated) {
      validArr = cloneDeep(tempArr);
    } else {
      validArr = tempArr.filter((e) => e.key === props?.detailInfo?.status);
    }
    const pieBorderWidth = validArr.filter((e) => Number(detail.value[e.value]) > 0).length === 1 ? 0 : 2;
    charOptions.value.series.data = validArr.map((item: any) => {
      return {
        value: detail.value[item.value] || 0,
        name: t(item.label),
        itemStyle: {
          color: item.color,
          borderWidth: pieBorderWidth,
          borderColor: '#ffffff',
        },
      };
    });

    legendData.value = validArr.map((item: any) => {
      return {
        ...item,
        label: t(item.label),
        count: detail.value[item.value] || 0,
        rote: `${detail.value[item.rateKey] || 0}%`,
      };
    });
  }

  const tiledListRef = ref<InstanceType<typeof TiledList>>();
  function searchHandler() {
    if (keywordName.value) {
      tiledListRef.value?.updateDebouncedSearch();
    } else {
      tiledListRef.value?.initStepTree();
    }
  }

  function resetHandler() {
    tiledListRef.value?.initStepTree();
  }

  watchEffect(() => {
    if (props.detailInfo) {
      detail.value = props.detailInfo;
      initOptionsData();
    }
  });
</script>

<style scoped lang="less">
  .report-container {
    height: calc(100vh - 56px);
    background: var(--color-text-n9);
    .report-header {
      padding: 0 16px;
      height: 54px;
      border-radius: 4px;
      background: var(--color-text-fff);
      @apply mb-4;

      background-color: var(--color-text-fff);
    }
    .analyze {
      @apply mb-4;

      padding: 16px;
      border-radius: 4px;
      background-color: var(--color-text-fff);
      .request-analyze {
        @apply flex h-full flex-1 flex-col;
        .chart-legend {
          .chart-legend-item {
            @apply grid grid-cols-3 gap-2;
          }
          .chart-flag {
            @apply flex items-center;
            .count {
              color: var(--color-text-1);
            }
          }
        }
      }
      .time-analyze {
        @apply flex h-full flex-1 flex-col px-4;
        .time-card {
          @apply flex items-center justify-between;
          .time-card-item {
            @apply flex flex-1 flex-grow items-end;

            padding: 9px 12px;
            border-radius: 6px;
            background: var(--color-text-n9);
            .time-card-item-title {
              color: var(--color-text-4);
              line-height: 16px;
            }
            .count {
              @apply mx-2 font-medium;

              line-height: 22px;
              font-size: 18px;
            }
          }
          .time-card-item-rote {
            @apply flex flex-1 flex-grow flex-col;

            padding: 9px 12px;
            border-radius: 6px;
            background: var(--color-text-n9);
            .time-card-item-rote-title {
              @apply mb-2 flex items-center;

              color: var(--color-text-4);
            }
            .count {
              font-size: 18px;
              @apply mx-2 font-medium;
            }
          }
        }
      }
    }
    .report-info {
      padding: 16px;
      border-radius: 4px;
      background-color: var(--color-text-fff);
    }
  }
  .block-title {
    @apply font-medium;

    margin-bottom: 16px;
    font-size: 14px;
  }
</style>
