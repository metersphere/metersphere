<template>
  <MsDetailDrawer
    ref="detailDrawerRef"
    v-model:visible="showDrawer"
    :width="1200"
    :footer="false"
    :title="reportStepDetail.name"
    :detail-id="props.reportId"
    :detail-index="props.activeReportIndex"
    :get-detail-func="reportCaseDetail"
    :pagination="props.pagination"
    :table-data="props.tableData"
    :page-change="props.pageChange"
    show-full-screen
    :unmount-on-close="true"
    @loaded="loadedReport"
  >
    <template #titleRight="{ loading }">
      <div class="rightButtons flex items-center">
        <MsButton
          type="icon"
          status="secondary"
          class="mr-4 !rounded-[var(--border-radius-small)]"
          :disabled="loading"
          :loading="shareLoading"
          @click="shareHandler"
        >
          <MsIcon type="icon-icon_share1" class="mr-2 font-[16px]" />
          {{ t('common.share') }}
        </MsButton>
        <MsButton
          type="icon"
          status="secondary"
          class="mr-4 !rounded-[var(--border-radius-small)]"
          :disabled="loading"
          :loading="exportLoading"
          @click="exportHandler"
        >
          <MsIcon type="icon-icon_move_outlined" class="mr-2 font-[16px]" />
          {{ t('common.export') }}
        </MsButton>
      </div>
    </template>
    <template #default="{ detail }">
      <div class="report-container h-full">
        <!-- 报告参数开始 -->
        <div class="report-header flex items-center justify-between">
          <span>
            {{ detail.environmentName || '-' }}
            <a-divider direction="vertical" :margin="4"></a-divider>
            {{ detail.poolName || '-' }}
            <a-divider direction="vertical" :margin="4"></a-divider>
            {{ detail.requestDuration || '-' }}
            <a-divider direction="vertical" :margin="4"></a-divider>
            {{ detail.creatUserName || '-' }}
          </span>
          <span>
            <span class="text-[var(--color-text-4)]">{{ t('report.detail.api.executionTime') }}</span>
            {{ dayjs(detail.startTime).format('YYYY-MM-DD HH:mm:ss') || '-' }}
            <span class="text-[var(--color-text-4)]">{{ t('report.detail.api.executionTimeTo') }}</span>
            {{ dayjs(detail.endTime).format('YYYY-MM-DD HH:mm:ss') || '-' }}
          </span>
        </div>
        <!-- 报告参数结束 -->
        <!-- 报告分析开始 -->
        <div class="analyze mb-1">
          <!-- 请求分析 -->
          <div class="request-analyze min-h-[110px]">
            <div class="block-title mb-4">{{ t('report.detail.api.requestAnalysis') }}</div>
            <!-- 独立报告 -->
            <IndepReportChart
              v-if="props.showType === 'INDEPENDENT'"
              :legend-data="legendData"
              :options="charOptions"
            />
            <SetReportChart v-else :legend-data="legendData" :options="charOptions" />
            <!-- 集合报告 -->
            <!-- </div> -->
          </div>
          <!-- 耗时分析 -->
          <div class="time-analyze">
            <div class="time-card mb-2 mt-[16px] h-[40px] flex-1 gap-4">
              <div class="time-card-item flex h-full">
                <MsIcon type="icon-icon_time_outlined" class="mr-[4px] text-[var(--color-text-4)]" size="16" />
                <span class="time-card-item-title">{{ t('report.detail.api.totalTime') }}</span>
                <span class="count">{{ getTotalTime }}</span
                ><span class="time-card-item-title">s</span>
              </div>
              <div class="time-card-item h-full">
                <MsIcon type="icon-icon_time_outlined" class="mr-[4px] text-[var(--color-text-4)]" size="16" />
                <span class="time-card-item-title"> {{ t('report.detail.api.requestTotalTime') }}</span>
                <span class="count">{{ detail.requestDuration || '-' }}</span
                ><span class="time-card-item-title">s</span>
              </div>
            </div>

            <div class="time-card flex-1 gap-4">
              <!-- 执行率 -->
              <div v-if="props.showType === 'INTEGRATED'" class="time-card-item-rote">
                <div class="time-card-item-rote-title">
                  <MsIcon type="icon-icon_yes_outlined" class="mr-[4px] text-[var(--color-text-4)]" size="16" />
                  {{ t('report.detail.api.executionRate') }}
                </div>
                <div class="flex items-center">
                  <span class="count"> {{ getExcuteRate(detail) }} %</span>
                  <a-divider direction="vertical" class="!h-[16px]" :margin="8"></a-divider>
                  <span>{{ getRequestEacuteCount }}</span>
                  <span class="mx-1 text-[var(--color-text-4)]">/ {{ getRequestTotalCount || 0 }}</span>
                </div>
              </div>
              <div class="time-card-item-rote">
                <div class="time-card-item-rote-title">
                  <MsIcon type="icon-icon_yes_outlined" class="mr-[4px] text-[var(--color-text-4)]" size="16" />
                  {{ t('report.detail.api.assertPass') }}
                </div>
                <div class="flex items-center">
                  <span class="count"
                    >{{ detail.assertionPassRate === 'Calculating' ? '-' : detail.assertionPassRate || '0.00' }}%</span
                  >
                  <a-divider direction="vertical" class="!h-[16px]" :margin="8"></a-divider>
                  <span>{{ addCommasToNumber(detail.assertionSuccessCount || 0) }}</span>
                  <span class="mx-1 text-[var(--color-text-4)]">/ {{ detail.assertionCount || 0 }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <!-- 报告步骤分析结束 -->
        <!-- 报告明细开始 -->
        <div class="report-info">
          <div class="mb-4 flex h-[36px] items-center justify-between">
            <div class="flex items-center">
              <div class="mr-2 font-medium leading-[36px]">{{ t('report.detail.api.reportDetail') }}</div>
              <a-radio-group v-model:model-value="activeTab" type="button" size="small">
                <a-radio v-for="item of methods" :key="item.value" :value="item.value">
                  {{ t(item.label) }}
                </a-radio>
              </a-radio-group>
            </div>
            <a-select v-model="condition" class="w-[240px]" :placeholder="t('report.detail.api.filterPlaceholder')">
              <a-option :key="1" :value="1"> 1 </a-option>
            </a-select>
          </div>
          <!-- 平铺模式 -->
          <TiledList
            v-show="activeTab === 'tiled'"
            show-type="CASE"
            :active-type="activeTab"
            :report-detail="detail || []"
          />
          <!-- tab展示 -->
          <TiledList
            v-show="activeTab === 'tab'"
            show-type="CASE"
            :active-type="activeTab"
            :report-detail="detail || []"
          />
        </div>
        <!-- 报告明细结束 -->
      </div>
    </template>
  </MsDetailDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { cloneDeep } from 'lodash-es';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import type { MsPaginationI } from '@/components/pure/ms-table/type';
  import MsDetailDrawer from '@/components/business/ms-detail-drawer/index.vue';
  import IndepReportChart from './case/IndepReportChart.vue';
  import SetReportChart from './case/setReportChart.vue';
  import TiledList from './tiledList.vue';

  import { reportCaseDetail } from '@/api/modules/api-test/report';
  import { useI18n } from '@/hooks/useI18n';
  import { addCommasToNumber } from '@/utils';

  import type { LegendData, ReportDetail } from '@/models/apiTest/report';

  const { t } = useI18n();

  const props = defineProps<{
    visible: boolean;
    reportId: string;
    activeReportIndex: number;
    tableData: any[];
    pagination: MsPaginationI;
    pageChange: (page: number) => Promise<void>;
    showType: string; // 报告类型 独立报告或集合报告
  }>();

  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
  }>();

  const showDrawer = computed({
    get() {
      return props.visible;
    },
    set(val) {
      emit('update:visible', val);
    },
  });
  const innerReportId = ref(props.reportId);

  const reportStepDetail = ref<ReportDetail>({
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
    status: '', // 报告状态/SUCCESS/ERROR
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

  /**
   * 分享share
   */
  const shareLoading = ref<boolean>(false);
  function shareHandler() {}

  /**
   * 导出
   */
  const exportLoading = ref<boolean>(false);
  function exportHandler() {}

  const charOptions = ref({
    tooltip: {
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
          name: t('report.detail.api.pass'),
          itemStyle: {
            color: '#00C261',
          },
        },
      ],
    },
  });

  const methods = ref([
    {
      label: t('report.detail.api.tiledDisplay'),
      value: 'tiled',
    },
    {
      label: t('report.detail.api.tabDisplay'),
      value: 'tab',
    },
  ]);
  const legendData = ref<LegendData[]>([]);
  const activeTab = ref('tiled');
  const condition = ref('');

  const getRequestTotalCount = computed(() => {
    const { errorCount, successCount, fakeErrorCount, pendingCount } = reportStepDetail.value;
    return addCommasToNumber(errorCount + successCount + fakeErrorCount + pendingCount);
  });
  // 执行数量
  const getRequestEacuteCount = computed(() => {
    const { errorCount, successCount, fakeErrorCount } = reportStepDetail.value;
    return addCommasToNumber(errorCount + successCount + fakeErrorCount);
  });

  function getExcuteRate(detail: ReportDetail) {
    return 100 - Number(detail.requestPendingRate) ? (100 - Number(detail.requestPendingRate)).toFixed(2) : '0.00';
  }

  const getTotalTime = computed(() => {
    const { endTime, startTime } = reportStepDetail.value;
    if (endTime && startTime && endTime !== 0 && startTime !== 0) {
      return endTime - startTime;
    }
    return '-';
  });

  function initOptionsData() {
    const tempArr = [
      {
        label: 'report.detail.api.pass',
        value: 'successCount',
        color: '#00C261',
        class: 'bg-[rgb(var(--success-6))]',
        rateKey: 'requestPassRate',
      },
      {
        label: 'report.detail.api.misstatement',
        value: 'fakeErrorCount',
        color: '#FFC14E',
        class: 'bg-[rgb(var(--warning-6))]',
        rateKey: 'requestFakeErrorRate',
      },
      {
        label: 'report.detail.api.error',
        value: 'errorCount',
        color: '#ED0303',
        class: 'bg-[rgb(var(--danger-6))]',
        rateKey: 'requestErrorRate',
      },
      {
        label: 'report.detail.api.pending',
        value: 'pendingCount',
        color: '#D4D4D8',
        class: 'bg-[var(--color-text-input-border)]',
        rateKey: 'requestPendingRate',
      },
    ];

    const validArr = props.showType === 'INTEGRATED' ? tempArr : tempArr.slice(0, 1);

    charOptions.value.series.data = validArr.map((item: any) => {
      return {
        value: reportStepDetail.value[item.value] || 0,
        name: t(item.label),
        itemStyle: {
          color: item.color,
        },
      };
    });
    legendData.value = validArr.map((item: any) => {
      return {
        ...item,
        label: t(item.label),
        count: reportStepDetail.value[item.value] || 0,
        rote: reportStepDetail.value[item.rateKey] === 'Calculating' ? '-' : reportStepDetail.value[item.rateKey],
      };
    });
  }

  // 详情
  function loadedReport(detail: ReportDetail) {
    innerReportId.value = detail.id;
    reportStepDetail.value = cloneDeep(detail);
    initOptionsData();
  }
</script>

<style scoped lang="less">
  .report-container {
    padding: 16px;
    height: calc(100vh - 56px);
    background: var(--color-text-n9);
    .report-header {
      padding: 0 16px;
      height: 54px;
      border-radius: 4px;
      background: white;
      @apply mb-4 bg-white;
    }
    .analyze {
      min-height: 196px;
      max-height: 200px;
      border-radius: 4px;
      @apply mb-2 flex justify-between  bg-white;
      .request-analyze {
        @apply flex flex-1 flex-col p-4;
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
        @apply flex flex-1 flex-col p-4;
        .time-card {
          @apply flex items-center justify-between;
          .time-card-item {
            border-radius: 6px;
            background: var(--color-text-n9);
            @apply mt-4 flex flex-1 flex-grow items-center px-4;
            .time-card-item-title {
              color: var(--color-text-4);
            }
            .count {
              font-size: 18px;
              @apply mx-2 font-medium;
            }
          }
          .time-card-item-rote {
            border-radius: 6px;
            background: var(--color-text-n9);
            @apply mt-4 flex flex-1 flex-grow flex-col p-4;
            .time-card-item-rote-title {
              color: var(--color-text-4);
              @apply mb-2;
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
      @apply bg-white;
    }
  }
  .block-title {
    font-size: 14px;
    @apply font-medium;
  }
</style>