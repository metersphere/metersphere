<template>
  <caseReportCom :detail-info="detail" />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';

  import caseReportCom from './component/caseReportCom.vue';

  import { getShareReportInfo, reportCaseDetail } from '@/api/modules/api-test/report';
  import { NOT_FOUND_RESOURCE } from '@/router/constants';

  import type { ReportDetail } from '@/models/apiTest/report';

  const detail = ref<ReportDetail>();

  const route = useRoute();

  const router = useRouter();
  async function getDetail() {
    try {
      const res = await getShareReportInfo(route.query.shareId as string);
      if (res.deleted) {
        router.push({
          name: NOT_FOUND_RESOURCE,
          query: {
            type: 'DELETE',
          },
        });
      }

      if (res.expired) {
        router.push({
          name: NOT_FOUND_RESOURCE,
          query: {
            type: 'EXPIRED',
          },
        });
        return;
      }
      detail.value = await reportCaseDetail(res.reportId, route.query.shareId as string);
    } catch (error) {
      console.log(error);
    }
  }

  watchEffect(() => {
    if (route.query.shareId) {
      getDetail();
    }
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
      background: var(--color-text-fff);
      @apply mb-4;

      background-color: var(--color-text-fff);
    }
    .analyze {
      min-height: 196px;
      max-height: 200px;
      border-radius: 4px;
      @apply mb-2 flex justify-between;

      background-color: var(--color-text-fff);
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
      background-color: var(--color-text-fff);
    }
  }
  .block-title {
    font-size: 14px;
    @apply font-medium;
  }
</style>
