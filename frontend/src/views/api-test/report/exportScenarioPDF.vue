<template>
  <a-spin :loading="loading" :tip="t('report.detail.exportingPdf')" class="report-detail-container">
    <div id="report-detail" class="report-detail">
      <div class="mb-[16px] rounded-[var(--border-radius-small)] bg-white p-[16px]">{{ reportStepDetail?.name }}</div>
      <ScenarioCom :detail-info="reportStepDetail" is-export />
    </div>
  </a-spin>
</template>

<script setup lang="ts">
  import { useRoute } from 'vue-router';
  import { Message } from '@arco-design/web-vue';

  import ScenarioCom from './component/scenarioCom.vue';

  import { reportScenarioDetail } from '@/api/modules/api-test/report';
  import { logScenarioExport } from '@/api/modules/api-test/scenario';
  import { useI18n } from '@/hooks/useI18n';
  import exportPDF from '@/utils/exportPdf';

  import { ReportDetail } from '@/models/apiTest/report';

  const route = useRoute();
  const { t } = useI18n();

  const loading = ref(false);
  const reportStepDetail = ref<ReportDetail>();

  async function initReportDetail() {
    try {
      loading.value = true;
      reportStepDetail.value = await reportScenarioDetail(route.query.id as string);
      setTimeout(() => {
        nextTick(async () => {
          await exportPDF(reportStepDetail.value?.name || '', 'report-detail');
          loading.value = false;
          Message.success(t('report.detail.exportPdfSuccess'));
        });
      }, 500);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  async function logExport() {
    try {
      await logScenarioExport(route.query.id as string);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  onBeforeMount(() => {
    if (route.query.id) {
      initReportDetail();
      logExport();
    }
  });
</script>

<style lang="less">
  .arco-spin-mask-icon {
    @apply !fixed;
  }
</style>

<style lang="less" scoped>
  .report-detail-container {
    @apply flex justify-center;
    .report-detail {
      @apply overflow-x-auto;

      padding: 16px;
      width: 1190px;
      .ms-scroll-bar();
    }
  }
</style>
