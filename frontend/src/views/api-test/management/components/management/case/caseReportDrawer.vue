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
      <a-dropdown position="br" @select="shareHandler">
        <MsButton
          v-permission="['PROJECT_API_REPORT:READ+SHARE']"
          type="icon"
          status="secondary"
          class="mr-4 !rounded-[var(--border-radius-small)]"
          @click="shareHandler"
        >
          <MsIcon type="icon-icon_share1" class="mr-2 font-[16px]" />
          {{ t('common.share') }}
        </MsButton>
        <template #content>
          <a-doption>
            <span>{{ t('report.detail.api.copyLink') }}</span
            ><span>{{ t('report.detail.api.copyLinkTimeEnd', { time: shareTime }) }}</span>
          </a-doption>
        </template>
      </a-dropdown>
    </template>
    <CaseReportCom :detail-info="reportStepDetail" />
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import CaseReportCom from '@/views/api-test/report/component/caseReportCom.vue';

  import { getShareInfo, getShareTime, reportCaseDetail } from '@/api/modules/api-test/report';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import type { ReportDetail } from '@/models/apiTest/report';
  import { RouteEnum } from '@/enums/routeEnum';

  const props = defineProps<{
    reportId: string;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();

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
  };
  const reportStepDetail = ref<ReportDetail>({
    ...initReportStepDetail,
  });
  async function getReportCaseDetail() {
    try {
      reportStepDetail.value = await reportCaseDetail(props.reportId);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  watch(
    () => innerVisible.value,
    async (val) => {
      if (val) {
        reportStepDetail.value = { ...initReportStepDetail };
        await getReportCaseDetail();
      }
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
      shareLink.value = `${origin}/#/${RouteEnum.SHARE}/${RouteEnum.SHARE_REPORT_CASE}${shareId}`;
      if (navigator.clipboard) {
        navigator.clipboard.writeText(shareLink.value).then(
          () => {
            Message.info(t('bugManagement.detail.shareTip'));
          },
          (e) => {
            Message.error(e);
          }
        );
      } else {
        const input = document.createElement('input');
        input.value = shareLink.value;
        document.body.appendChild(input);
        input.select();
        document.execCommand('copy');
        document.body.removeChild(input);
        Message.info(t('bugManagement.detail.shareTip'));
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const shareTime = ref<string>('');
  async function getTime() {
    try {
      const res = await getShareTime(appStore.currentProjectId);
      const match = res.match(/^(\d+)([MYHD])$/);
      if (match) {
        const value = parseInt(match[1], 10);
        const type = match[2];
        const translations = {
          M: t('msTimeSelector.month'),
          Y: t('msTimeSelector.year'),
          H: t('msTimeSelector.hour'),
          D: t('msTimeSelector.day'),
        };
        shareTime.value = value + (translations[type] || translations.D);
      }
    } catch (error) {
      console.log(error);
    }
  }
  onMounted(() => {
    getTime();
  });
</script>
