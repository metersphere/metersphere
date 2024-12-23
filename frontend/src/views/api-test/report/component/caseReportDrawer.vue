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
      <div class="ms-drawer-right-operation-button flex items-center">
        <a-dropdown position="br" @select="shareHandler">
          <MsButton
            v-permission="['PROJECT_API_REPORT:READ+SHARE']"
            type="icon"
            status="secondary"
            class="mr-4 !rounded-[var(--border-radius-small)]"
            :disabled="loading"
            :loading="shareLoading"
            @click="shareHandler"
          >
            <MsIcon type="icon-icon_link-copy_outlined" class="mr-2 font-[16px]" />
            {{ t('common.share') }}
          </MsButton>
          <template #content>
            <a-doption>
              <span>{{ t('report.detail.api.copyLink') }}</span
              ><span>{{ t('report.detail.api.copyLinkTimeEnd', { time: props.shareTime }) }}</span>
            </a-doption>
          </template>
        </a-dropdown>
        <MsButton
          v-permission="['PROJECT_API_REPORT:READ+EXPORT']"
          type="icon"
          status="secondary"
          class="mr-4 !rounded-[var(--border-radius-small)]"
          @click="exportHandler"
        >
          <MsIcon type="icon-icon_into-item_outlined" class="mr-2 font-[16px]" />
          {{ t('common.export') }}
        </MsButton>
      </div>
    </template>
    <template #default="{ loading }">
      <a-spin class="h-full w-full" :loading="loading">
        <CaseReportCom :detail-info="reportStepDetail" />
      </a-spin>
    </template>
  </MsDetailDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useClipboard } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import type { MsPaginationI } from '@/components/pure/ms-table/type';
  import MsDetailDrawer from '@/components/business/ms-detail-drawer/index.vue';
  import CaseReportCom from './caseReportCom.vue';

  import { getShareInfo, reportCaseDetail } from '@/api/modules/api-test/report';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import { useAppStore } from '@/store';

  import type { ReportDetail } from '@/models/apiTest/report';
  import { FullPageEnum, RouteEnum } from '@/enums/routeEnum';
  import { ExecuteStatusEnum } from '@/enums/taskCenter';

  const appStore = useAppStore();
  const { copy, isSupported } = useClipboard({ legacy: true });
  const { t } = useI18n();
  const { openNewPage } = useOpenNewPage();

  const props = defineProps<{
    visible: boolean;
    reportId: string;
    activeReportIndex: number;
    tableData: any[];
    pagination: MsPaginationI;
    pageChange: (page: number) => Promise<void>;
    shareTime: string;
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
  const detailDrawerRef = ref();

  const initReportDetail = {
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
  };

  const reportStepDetail = ref<ReportDetail>({
    ...initReportDetail,
  });

  /**
   * 分享share
   */
  const shareLoading = ref<boolean>(false);
  const shareLink = ref<string>('');

  async function shareHandler() {
    try {
      const res = await getShareInfo({
        reportId: reportStepDetail.value.id,
        projectId: appStore.currentProjectId,
      });
      const shareId = res.shareUrl;

      const { origin } = window.location;
      shareLink.value = `${origin}/#/${RouteEnum.SHARE}/${RouteEnum.SHARE_REPORT_CASE}${shareId}`;
      if (isSupported) {
        copy(shareLink.value);
        Message.info(t('bugManagement.detail.shareTip'));
      } else {
        Message.error(t('common.copyNotSupport'));
      }
    } catch (error) {
      console.log(error);
    }
  }

  // 详情
  function loadedReport(detail: ReportDetail) {
    innerReportId.value = detail.id;
    reportStepDetail.value = { ...initReportDetail };
    reportStepDetail.value = cloneDeep(detail);
  }

  watch(
    () => showDrawer.value,
    (val) => {
      if (!val) {
        reportStepDetail.value = { ...initReportDetail };
      }
    }
  );

  function exportHandler() {
    openNewPage(FullPageEnum.FULL_PAGE_API_CASE_EXPORT_PDF, {
      id: props.reportId,
    });
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
      background: var(--color-text-fff);
      @apply mb-4;

      background-color: var(--color-text-fff);
    }
    .analyze {
      height: 196px;
      border-radius: 4px;
      @apply mb-4 flex justify-between;

      background-color: var(--color-text-fff);
      .request-analyze {
        @apply flex h-full flex-1 flex-col p-4;
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
        @apply flex h-full flex-1 flex-col p-4;
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
      background-color: var(--color-text-fff);
    }
  }
  .block-title {
    font-size: 14px;
    @apply font-medium;
  }
</style>
