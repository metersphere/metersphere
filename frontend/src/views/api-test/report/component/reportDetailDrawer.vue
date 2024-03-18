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
    <template #default>
      <div class="report-container h-full">
        <!-- 报告参数开始 -->
        <div class="report-header flex items-center justify-between">
          <!-- TODO 虚拟数据替换接口后边 -->
          <span>
            dev环境
            <a-divider direction="vertical" :margin="4"></a-divider>
            66 资源池
            <a-divider direction="vertical" :margin="4"></a-divider>
            1000ms
            <a-divider direction="vertical" :margin="4"></a-divider>
            admin
          </span>
          <span>
            <span class="text-[var(--color-text-4)]">执行时间</span>
            2023-08-10 17:53:03
            <span class="text-[var(--color-text-4)]">至</span>
            2023-08-10 17:53:03
          </span>
        </div>
        <!-- 报告参数结束 -->
        <!-- 报告步骤分析和请求分析开始 -->
        <div class="analyze">
          <div class="step-analyze min-w-[522px]">
            <div class="block-title">步骤分析</div>
            <div class="mb-2 flex items-center">
              <!-- 总数 -->
              <div class="countItem">
                <span class="mr-2 text-[var(--color-text-4)]"> {{ t('report.detail.stepTotal') }}</span>
                {{ reportStepDetail.stepTotal }}
              </div>
              <!-- 通过 -->
              <div class="countItem">
                <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--success-6))]"></div>
                <div class="mr-2 text-[var(--color-text-4)]">{{ t('report.detail.successCount') }}</div>
                {{ reportStepDetail.successCount }}
              </div>
              <!-- 误报 -->
              <div class="countItem">
                <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--warning-6))]"></div>
                <div class="mr-2 text-[var(--color-text-4)]">{{ t('report.detail.fakeErrorCount') }}</div>
                {{ reportStepDetail.fakeErrorCount }}
              </div>
              <!-- 失败 -->
              <div class="countItem">
                <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--danger-6))]"></div>
                <div class="mr-2 text-[var(--color-text-4)]">{{ t('report.detail.errorCount') }}</div>
                {{ reportStepDetail.errorCount }}
              </div>
              <!-- 未执行 -->
              <div class="countItem">
                <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[var(--color-text-input-border)]"></div>
                <div class="mr-2 text-[var(--color-text-4)]">{{ t('report.detail.pendingCount') }}</div>
                {{ reportStepDetail.pendingCount }}
              </div>
            </div>
            <StepProgress :report-detail="reportStepDetail" height="8px" radius="var(--border-radius-mini)" />
            <div class="card">
              <div class="timer-card mr-2">
                <div class="text-[var(--color-text-4)]">
                  <MsIcon type="icon-icon_time_outlined" class="text-[var(--color-text-4)]x mr-[4px]" size="16" />
                  总耗时
                </div>
                <div> <span class="ml-4 text-[18px] font-medium">3</span>s </div>
              </div>
              <div class="timer-card mr-2">
                <div class="text-[var(--color-text-4)]">
                  <MsIcon type="icon-icon_time_outlined" class="mr-[4px] text-[var(--color-text-4)]" size="16" />
                  请求总耗时
                </div>
                <div> <span class="ml-4 text-[18px] font-medium">3</span>s </div>
              </div>
              <div class="timer-card min-w-[200px]">
                <div class="text-[var(--color-text-4)]">
                  <MsIcon type="icon-icon_yes_outlined" class="mr-[4px] text-[var(--color-text-4)]" size="16" />
                  断言通过率
                </div>
                <div class="flex items-center">
                  <span class="text-[18px] font-medium text-[var(--color-text-1)]">99.99 <span>%</span></span>
                  <a-divider direction="vertical" :margin="0" class="!mx-1"></a-divider>
                  <span class="text-[var(--color-text-1)]">1,000</span>
                  <span class="text-[var(--color-text-4)]">/ 1,000</span>
                </div>
              </div>
            </div>
          </div>

          <div class="request-analyze">
            <div class="block-title">请求分析</div>
            <div class="flex min-h-[110px] items-center">
              <div class="relative mr-4">
                <div class="absolute bottom-0 left-[30%] top-[35%] text-center">
                  <div class="text-[12px] text-[(var(--color-text-4))]">总数 (个)</div>
                  <div class="text-[18px] font-medium">4</div>
                </div>
                <MsChart width="110px" height="110px" :options="charOptions" />
              </div>
              <div class="chart-legend grid flex-1 gap-y-4">
                <div class="chart-legend-item">
                  <div class="chart-flag">
                    <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--success-6))]"></div>
                    <div class="mr-2 text-[var(--color-text-4)]">{{ t('report.detail.successCount') }}</div>
                  </div>
                  <div class="count">24</div>
                  <div class="count">99.99%</div>
                </div>
                <div class="chart-legend-item">
                  <div class="chart-flag">
                    <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--warning-6))]"></div>
                    <div class="mr-2 text-[var(--color-text-4)]">{{ t('report.detail.fakeErrorCount') }}</div>
                  </div>

                  <div class="count">24</div>
                  <div class="count">99.99%</div>
                </div>
                <div class="chart-legend-item">
                  <div class="chart-flag">
                    <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--danger-6))]"></div>
                    <div class="mr-2 text-[var(--color-text-4)]">{{ t('report.detail.errorCount') }}</div>
                  </div>
                  <div class="count">24</div>
                  <div class="count">99.99%</div>
                </div>
                <div class="chart-legend-item">
                  <div class="chart-flag">
                    <div
                      class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[var(--color-text-input-border)]"
                    ></div>
                    <div class="mr-2 text-[var(--color-text-4)]">{{ t('report.detail.pendingCount') }}</div>
                  </div>

                  <div class="count">24</div>
                  <div class="count">99.99%</div>
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
              <div class="mr-2 font-medium leading-[36px]">报告明细</div>
              <a-radio-group v-model:model-value="activeTab" type="button" size="small">
                <a-radio v-for="item of methods" :key="item.value" :value="item.value">
                  {{ t(item.label) }}
                </a-radio>
              </a-radio-group>
            </div>
            <a-select v-model="condition" class="w-[240px]" placeholder="请选择过滤条件">
              <a-option :key="1" :value="1"> 1 </a-option>
            </a-select>
          </div>
          <TiledList v-show="activeTab === 'tiled'" />
        </div>
        <!-- 报告明细结束 -->
      </div>
    </template>
  </MsDetailDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsChart from '@/components/pure/chart/index.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import type { MsPaginationI } from '@/components/pure/ms-table/type';
  import MsDetailDrawer from '@/components/business/ms-detail-drawer/index.vue';
  import StepProgress from './stepProgress.vue';
  import TiledList from './tiledList.vue';

  import { reportDetail } from '@/api/modules/api-test/report';
  import { useI18n } from '@/hooks/useI18n';

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
  function loadedReport(detail: Record<string, any>) {
    innerFileId.value = detail.id;
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

  const reportStepDetail = ref({
    stepTotal: 8,
    errorCount: 2,
    fakeErrorCount: 8,
    pendingCount: 9,
    successCount: 9,
  });

  const charOptions = ref({
    tooltip: {
      trigger: 'item',
    },
    legend: {
      show: false,
    },
    series: [
      {
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
            value: 1048,
            name: '通过',
            itemStyle: {
              color: '#00C261',
            },
          },
          {
            value: 735,
            name: '误报',
            itemStyle: {
              color: '#FFC14E',
            },
          },
          {
            value: 580,
            name: '失败',
            itemStyle: {
              color: '#ED0303',
            },
          },
          {
            value: 484,
            name: '未执行',
            itemStyle: {
              color: '#D4D4D8',
            },
          },
        ],
      },
    ],
  });

  const activeTab = ref('tiled');
  const condition = ref('');

  const methods = ref([
    {
      label: '平铺展示',
      value: 'tiled',
    },
    {
      label: 'Tab展示',
      value: 'tab',
    },
  ]);
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
      @apply mb-4 flex justify-between;
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
