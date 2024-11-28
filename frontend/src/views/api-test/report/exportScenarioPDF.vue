<template>
  <a-spin
    :loading="batchLoading || loading"
    :tip="batchLoading ? batchExportTip : t('report.detail.exportingPdf')"
    class="report-detail-container"
  >
    <div id="report-detail" class="report-detail">
      <div class="mb-[16px] break-all rounded-[var(--border-radius-small)] bg-[var(--color-text-fff)] p-[16px]">
        {{ reportStepDetail?.name }}
      </div>
      <ScenarioCom :detail-info="reportStepDetail" is-export />
    </div>
  </a-spin>
</template>

<script setup lang="ts">
  import { useRoute } from 'vue-router';
  import { Message } from '@arco-design/web-vue';

  import ScenarioCom from './component/scenarioCom.vue';

  import { reportScenarioDetail } from '@/api/modules/api-test/report';
  import {
    getScenarioBatchExportParams,
    logScenarioReportBatchExport,
    logScenarioReportExport,
  } from '@/api/modules/api-test/scenario';
  import { useI18n } from '@/hooks/useI18n';
  import { characterLimit } from '@/utils';
  import exportPDF from '@/utils/exportPdf';

  import { ReportDetail } from '@/models/apiTest/report';
  import { BatchApiParams } from '@/models/common';

  const route = useRoute();
  const { t } = useI18n();

  const loading = ref(false);
  const reportStepDetail = ref<ReportDetail>();

  async function initReportDetail(id?: string) {
    try {
      loading.value = true;
      reportStepDetail.value = await reportScenarioDetail(id || (route.query.id as string));
      await new Promise((resolve) => {
        setTimeout(async () => {
          await nextTick(async () => {
            await exportPDF(reportStepDetail.value?.name || '', 'report-detail');
            loading.value = false;
            Message.success(
              t('report.detail.exportPdfSuccess', { name: characterLimit(reportStepDetail.value?.name, 50) })
            );
            resolve(true);
          });
        }, 500); // TODO:树组件渲染延迟导致导出 pdf 时内容不全，暂时延迟 500ms
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  async function logExport(params?: BatchApiParams) {
    if (params) {
      await logScenarioReportBatchExport(params);
    } else {
      await logScenarioReportExport(route.query.id as string);
    }
  }

  const batchLoading = ref<boolean>(false);
  const batchIds = ref<string[]>([]);
  const exportCurrent = ref<number>(0);
  const exportTotal = ref<number>(0);
  const batchExportTip = computed(() =>
    t('report.detail.batchExportingPdf', { current: exportCurrent.value, total: exportTotal.value })
  );

  async function initBatchIds(params: BatchApiParams) {
    try {
      batchLoading.value = true;
      batchIds.value = await getScenarioBatchExportParams(params);
      exportTotal.value = batchIds.value.length;
      while (batchIds.value.length > 0) {
        exportCurrent.value += 1;
        // eslint-disable-next-line no-await-in-loop
        await initReportDetail(batchIds.value.shift());
      }
      nextTick(() => {
        // 等最后一条成功提示后清空
        Message.clear();
        Message.success(t('report.detail.batchExportPdfSuccess'));
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      batchLoading.value = false;
    }
  }

  onBeforeMount(() => {
    window.addEventListener('message', (event) => {
      if (event.origin !== window.location.origin) {
        return;
      }
      const { data, eventId } = JSON.parse(event.data);
      // 发送响应回旧标签页
      window.opener.postMessage(eventId, window.location.origin);
      // 初始化批量导出报告 id 集合
      initBatchIds(data);
      logExport(data);
    });
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
