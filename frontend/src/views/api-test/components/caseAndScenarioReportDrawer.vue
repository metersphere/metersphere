<template>
  <MsDrawer
    v-model:visible="innerVisible"
    :title="reportStepDetail.name"
    :width="1200"
    :footer="false"
    unmount-on-close
    no-content-padding
    show-full-screen
  >
    <template #tbutton>
      <div class="ms-drawer-right-operation-button flex items-center">
        <a-dropdown v-if="!props.doNotShowShare" position="br" @select="shareHandler">
          <MsButton
            v-permission="['PROJECT_API_REPORT:READ+SHARE']"
            type="icon"
            status="secondary"
            class="mr-4 !rounded-[var(--border-radius-small)] text-[var(--color-text-1)]"
            @click="shareHandler"
          >
            <MsIcon type="icon-icon_link-copy_outlined" class="mr-2 font-[16px] text-[var(--color-text-1)]" />
            {{ t('common.share') }}
          </MsButton>
          <template #content>
            <a-doption>
              <span>{{ t('report.detail.api.copyLink') }}</span
              ><span>{{ t('report.detail.api.copyLinkTimeEnd', { time: shareTime }) }}</span>
            </a-doption>
          </template>
        </a-dropdown>
        <MsButton
          v-permission="['PROJECT_API_REPORT:READ+EXPORT']"
          type="icon"
          status="secondary"
          class="mr-4 !rounded-[var(--border-radius-small)] text-[var(--color-text-1)]"
          @click="exportHandler"
        >
          <MsIcon type="icon-icon_bottom-align_outlined" class="mr-2 font-[16px] text-[var(--color-text-1)]" />
          {{ t('common.export') }}
        </MsButton>
      </div>
    </template>
    <a-spin :loading="loading" class="block">
      <CaseReportCom
        v-if="!props.isScenario"
        :detail-info="reportStepDetail"
        :is-filter-step="props.isFilterStep"
        :case-name="props.caseName"
        :case-id="props.caseId"
        :get-report-step-detail="props.getReportStepDetail"
      />
      <ScenarioCom
        v-else
        :detail-info="reportStepDetail"
        :is-filter-step="props.isFilterStep"
        :case-name="props.caseName"
        :case-id="props.caseId"
        :get-report-step-detail="props.getReportStepDetail"
      />
    </a-spin>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { useClipboard } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import CaseReportCom from '@/views/api-test/report/component/caseReportCom.vue';
  import ScenarioCom from '@/views/api-test/report/component/scenarioCom.vue';

  import { getShareInfo, getShareTime, reportCaseDetail, reportScenarioDetail } from '@/api/modules/api-test/report';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import { useAppStore } from '@/store';

  import type { ReportDetail } from '@/models/apiTest/report';
  import { FullPageEnum, RouteEnum } from '@/enums/routeEnum';
  import { ExecuteStatusEnum } from '@/enums/taskCenter';

  const props = defineProps<{
    reportId: string;
    isScenario?: boolean;
    doNotShowShare?: boolean; // 不展示分享按钮
    reportDetail?: (...args: any) => Promise<any>; // 获取报告接口
    getReportStepDetail?: (...args: any) => Promise<any>; // 获取步骤的详情内容接口
    caseName?: string; // 用例名称
    caseId?: string; // 用例id
    isFilterStep?: boolean;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();
  const { copy, isSupported } = useClipboard({ legacy: true });
  const route = useRoute();
  const { openNewPage } = useOpenNewPage();

  const innerVisible = defineModel<boolean>('visible', {
    required: true,
  });

  const initReportStepDetail = {
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
    ...initReportStepDetail,
  });
  const loading = ref(false);

  async function getReportDetail() {
    try {
      loading.value = true;
      if (props.reportDetail) {
        reportStepDetail.value = await props.reportDetail(props.reportId, route.query.shareId as string | undefined);
        return;
      }
      if (props.isScenario) {
        reportStepDetail.value = await reportScenarioDetail(props.reportId);
      } else {
        reportStepDetail.value = await reportCaseDetail(props.reportId);
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  watch(
    () => innerVisible.value,
    async (val) => {
      if (val) {
        reportStepDetail.value = { ...initReportStepDetail };
        await getReportDetail();
      }
    },
    {
      immediate: true,
    }
  );

  // 分享share
  const shareLink = ref<string>('');
  async function shareHandler() {
    try {
      const res = await getShareInfo({
        reportId: reportStepDetail.value.id,
        projectId: appStore.currentProjectId,
      });
      const shareId = res.shareUrl;

      const { origin } = window.location;
      shareLink.value = `${origin}/#/${RouteEnum.SHARE}/${
        props.isScenario ? RouteEnum.SHARE_REPORT_SCENARIO : RouteEnum.SHARE_REPORT_CASE
      }${shareId}`;
      if (isSupported) {
        copy(shareLink.value);
        Message.info(t('bugManagement.detail.shareTip'));
      } else {
        Message.error(t('common.copyNotSupport'));
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const shareTime = ref<string>('');
  async function getTime() {
    if (!appStore.currentProjectId) {
      return;
    }
    try {
      const res = await getShareTime(appStore.currentProjectId);
      const match = res.match(/^(\d+)([MYHD])$/);
      if (match) {
        const value = parseInt(match[1], 10);
        const type = match[2];
        const translations: Record<string, string> = {
          M: t('msTimeSelector.month'),
          Y: t('msTimeSelector.year'),
          H: t('msTimeSelector.hour'),
          D: t('msTimeSelector.day'),
        };
        shareTime.value = value + (translations[type] || translations.D);
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  function exportHandler() {
    openNewPage(
      props.isScenario ? FullPageEnum.FULL_PAGE_SCENARIO_EXPORT_PDF : FullPageEnum.FULL_PAGE_API_CASE_EXPORT_PDF,
      {
        id: props.reportId,
      }
    );
  }

  onMounted(() => {
    if (!props.doNotShowShare) {
      getTime();
    }
  });
</script>
