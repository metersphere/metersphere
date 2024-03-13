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
            <div>
              <div></div>
              <div></div>
              <div></div>
            </div>
          </div>

          <div class="request-analyze"> </div>
        </div>
        <!-- 报告步骤分析和请求分析结束 -->
        <!-- 报告明细开始 -->
        <div class="report-info"></div>
        <!-- 报告明细结束 -->
      </div>
    </template>
  </MsDetailDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import type { MsPaginationI } from '@/components/pure/ms-table/type';
  import MsDetailDrawer from '@/components/business/ms-detail-drawer/index.vue';
  import StepProgress from './stepProgress.vue';

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
      }
      .request-analyze {
        padding: 16px;
        border-radius: 4px;
        @apply h-full bg-white;
      }
    }
  }
  .block-title {
    @apply mb-4 font-medium;
  }
</style>
