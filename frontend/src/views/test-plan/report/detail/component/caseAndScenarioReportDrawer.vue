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
    <CaseReportCom
      v-if="!props.isScenario"
      :detail-info="reportStepDetail"
      :get-report-step-detail="props.getReportStepDetail"
    />
    <ScenarioCom v-else :detail-info="reportStepDetail" :get-report-step-detail="props.getReportStepDetail" />
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useClipboard } from '@vueuse/core';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import CaseReportCom from '@/views/api-test/report/component/caseReportCom.vue';
  import ScenarioCom from '@/views/api-test/report/component/scenarioCom.vue';

  import { reportCaseDetail,reportScenarioDetail } from '@/api/modules/test-plan/report';

  import type { ReportDetail } from '@/models/apiTest/report';

  const props = defineProps<{
    reportId: string;
    isScenario?: boolean;
    reportDetail?: (...args: any) => Promise<any>; // 获取报告接口
    getReportStepDetail?: (...args: any) => Promise<any>; // 获取步骤的详情内容接口
    shareId?: string;
  }>();

  const { copy, isSupported } = useClipboard({ legacy: true });

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
  async function getReportDetail() {
    try {
      if (props.reportDetail) {
        reportStepDetail.value = await props.reportDetail(props.reportId);
        return;
      }
      if (props.isScenario) {
        reportStepDetail.value = await reportScenarioDetail(props.reportId, props.shareId);
      } else {
        reportStepDetail.value = await reportCaseDetail(props.reportId, props.shareId);
      }
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
        await getReportDetail();
      }
    }
  );

</script>
