<template>
  <MsDetailDrawer
    ref="detailDrawerRef"
    v-model:visible="showDrawer"
    :width="960"
    :footer="false"
    :title="t('project.fileManagement.detail')"
    :detail-id="props.reportId"
    :detail-index="props.activeReportIndex"
    :get-detail-func="reportDetail"
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
          <!-- TODO 虚拟数据替换接口后边 -->
          <span>
            {{ detail.environmentName || '-' }}
            <a-divider direction="vertical" :margin="4"></a-divider>
            {{ detail.poolName || '-' }}
            <a-divider direction="vertical" :margin="4"></a-divider>
            {{ detail.requestDuration || '-' }}
            <a-divider direction="vertical" :margin="4"></a-divider>
            {{ detail.createUser || '-' }}
          </span>
          <span>
            <span class="text-[var(--color-text-4)]">{{ t('report.detail.api.executionTime') }}</span>
            {{ dayjs(detail.startTime).format('YYYY-MM-DD HH:mm:ss') || '-' }}
            <span class="text-[var(--color-text-4)]">{{ t('report.detail.api.executionTimeTo') }}</span>
            {{ dayjs(detail.endTime).format('YYYY-MM-DD HH:mm:ss') || '-' }}
          </span>
        </div>
        <!-- 报告参数结束 -->
        <!-- 报告步骤分析和请求分析开始 -->
        <div class="analyze mb-1">
          <div class="step-analyze min-w-[522px]">
            <div class="block-title">{{ t('report.detail.api.stepAnalysis') }}</div>
            <div class="mb-2 flex items-center">
              <!-- 总数 -->
              <div class="countItem">
                <span class="mr-2 text-[var(--color-text-4)]"> {{ t('report.detail.stepTotal') }}</span>
                {{ detail.stepTotal || 0 }}
              </div>
              <!-- 通过 -->
              <div class="countItem">
                <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--success-6))]"></div>
                <div class="mr-2 text-[var(--color-text-4)]">{{ t('report.detail.successCount') }}</div>
                {{ detail.successCount || 0 }}
              </div>
              <!-- 误报 -->
              <div class="countItem">
                <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--warning-6))]"></div>
                <div class="mr-2 text-[var(--color-text-4)]">{{ t('report.detail.fakeErrorCount') }}</div>
                {{ detail.fakeErrorCount || 0 }}
              </div>
              <!-- 失败 -->
              <div class="countItem">
                <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--danger-6))]"></div>
                <div class="mr-2 text-[var(--color-text-4)]">{{ t('report.detail.errorCount') }}</div>
                {{ detail.errorCount || 0 }}
              </div>
              <!-- 未执行 -->
              <div class="countItem">
                <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[var(--color-text-input-border)]"></div>
                <div class="mr-2 text-[var(--color-text-4)]">{{ t('report.detail.pendingCount') }}</div>
                {{ detail.pendingCount || 0 }}
              </div>
            </div>
            <StepProgress :report-detail="detail" height="8px" radius="var(--border-radius-mini)" />
            <div class="card">
              <div class="timer-card mr-2">
                <div class="text-[var(--color-text-4)]">
                  <MsIcon type="icon-icon_time_outlined" class="text-[var(--color-text-4)]x mr-[4px]" size="16" />
                  {{ t('report.detail.api.totalTime') }}
                </div>
                <div>
                  <span class="ml-4 text-[18px] font-medium">{{ detail.requestDuration || 0 }}</span
                  >s
                </div>
              </div>
              <div class="timer-card mr-2">
                <div class="text-[var(--color-text-4)]">
                  <MsIcon type="icon-icon_time_outlined" class="mr-[4px] text-[var(--color-text-4)]" size="16" />
                  {{ t('report.detail.api.requestTotalTime') }}
                </div>
                <div>
                  <span class="ml-4 text-[18px] font-medium">{{ detail.requestDuration }}</span
                  >s
                </div>
              </div>
              <div class="timer-card min-w-[200px]">
                <div class="text-[var(--color-text-4)]">
                  <MsIcon type="icon-icon_yes_outlined" class="mr-[4px] text-[var(--color-text-4)]" size="16" />
                  {{ t('report.detail.api.assertPass') }}
                </div>
                <div class="flex items-center">
                  <span class="text-[18px] font-medium text-[var(--color-text-1)]"
                    >{{ detail.assertionPassRate || 0 }} <span>%</span></span
                  >
                  <a-divider direction="vertical" :margin="0" class="!mx-1"></a-divider>
                  <span class="text-[var(--color-text-1)]">{{
                    addCommasToNumber(detail.assertionSuccessCount || 0)
                  }}</span>
                  <span class="text-[var(--color-text-4)]">/ {{ addCommasToNumber(detail.assertionCount) || 0 }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="request-analyze">
            <div class="block-title">{{ t('report.detail.api.requestAnalysis') }}</div>
            <div class="flex min-h-[110px] items-center">
              <div class="relative mr-4">
                <div class="absolute bottom-0 left-[30%] top-[35%] text-center">
                  <div class="text-[12px] text-[(var(--color-text-4))]">{{ t('report.detail.api.total') }}</div>
                  <div class="text-[18px] font-medium">4</div>
                </div>
                <MsChart width="110px" height="110px" :options="charOptions" />
              </div>
              <div class="chart-legend grid flex-1 gap-y-3">
                <!-- 图例开始 -->
                <div v-for="item of legendData" :key="item.value" class="chart-legend-item">
                  <div class="chart-flag">
                    <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full" :class="item.class"></div>
                    <div class="mr-2 text-[var(--color-text-4)]">{{ item.label }}</div>
                  </div>
                  <div class="count">{{ item.count || 0 }}</div>
                  <div class="count">{{ item.rote || 0 }}%</div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <!-- 报告步骤分析和请求分析结束 -->
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
          <TiledList v-show="activeTab === 'tiled'" :active-type="activeTab" :report-detail="detail || []" />
          <!-- tab展示 -->
          <TiledList v-show="activeTab === 'tab'" :active-type="activeTab" :report-detail="detail || []" />
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

  import MsChart from '@/components/pure/chart/index.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import type { MsPaginationI } from '@/components/pure/ms-table/type';
  import MsDetailDrawer from '@/components/business/ms-detail-drawer/index.vue';
  import StepProgress from './stepProgress.vue';
  import TiledList from './tiledList.vue';

  import { reportDetail } from '@/api/modules/api-test/report';
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
  const innerFileId = ref(props.reportId);

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
  });

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
        {
          value: 0,
          name: t('report.detail.api.misstatement'),
          itemStyle: {
            color: '#FFC14E',
          },
        },
        {
          value: 0,
          name: t('report.detail.api.error'),
          itemStyle: {
            color: '#ED0303',
          },
        },
        {
          value: 0,
          name: t('report.detail.api.pending'),
          itemStyle: {
            color: '#D4D4D8',
          },
        },
      ],
    },
  });

  const legendData = ref<LegendData[]>([]);

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
        value: 'successCount',
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

    charOptions.value.series.data = tempArr.map((item: any) => {
      return {
        value: reportStepDetail.value[item.value] || 0,
        name: t(item.label),
        itemStyle: {
          color: item.color,
        },
      };
    });
    legendData.value = tempArr.map((item: any) => {
      return {
        ...item,
        label: t(item.label),
        count: reportStepDetail.value[item.value] || 0,
        rote: reportStepDetail.value[item.rateKey],
      };
    });
  }

  // 详情
  function loadedReport(detail: ReportDetail) {
    innerFileId.value = detail.id;
    reportStepDetail.value = cloneDeep(detail);
    initOptionsData();
  }

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

  const activeTab = ref('tiled');
  const condition = ref('');

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
  onMounted(() => {
    initOptionsData();
  });
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
      border-radius: 4px;
      @apply mb-2 flex justify-between;
      .step-analyze {
        padding: 16px;
        border-radius: 4px;
        @apply h-full bg-white;
        .countItem {
          @apply mr-6 flex items-center;
        }
        .card {
          @apply mt-4 flex items-center justify-between;
          .timer-card {
            border-radius: 6px;
            background-color: var(--color-text-n9);
            @apply flex flex-1 flex-col p-4;
          }
        }
      }
      .request-analyze {
        padding: 16px;
        border-radius: 4px;
        @apply ml-4 h-full flex-grow bg-white;
        .chart-legend {
          .chart-legend-item {
            @apply grid grid-cols-3;
          }
          .chart-flag {
            @apply flex items-center;
            .count {
              color: var(--color-text-1);
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
    @apply mb-4 font-medium;
  }
</style>
