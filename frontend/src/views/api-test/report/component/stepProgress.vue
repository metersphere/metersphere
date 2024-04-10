<template>
  <MsColorLine :color-data="colorData" :height="props.height" :radius="props.radius">
    <template #popoverContent>
      <table class="min-w-[144px]">
        <tr>
          <td class="popover-label-td">
            <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--success-6))]"></div>
            <div>{{ t('report.detail.successCount') }}</div>
          </td>
          <td class="popover-value-td">
            {{ props.reportDetail.stepSuccessCount }}
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--warning-6))]"></div>
            <div>{{ t('report.detail.fakeErrorCount') }}</div>
          </td>
          <td class="popover-value-td">
            {{ props.reportDetail.stepFakeErrorCount }}
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--danger-6))]"></div>
            <div>{{ t('report.detail.errorCount') }}</div>
          </td>
          <td class="popover-value-td">
            {{ props.reportDetail.stepErrorCount }}
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[var(--color-text-input-border)]"></div>
            <div>{{ t('report.detail.pendingCount') }}</div>
          </td>
          <td class="popover-value-td">
            {{ props.reportDetail.stepPendingCount }}
          </td>
        </tr>
      </table>
    </template>
  </MsColorLine>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsColorLine from '@/components/pure/ms-color-line/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  const props = defineProps<{
    reportDetail: {
      stepTotal: number;
      errorCount: number;
      fakeErrorCount: number;
      pendingCount: number;
      successCount: number;
      [key: string]: any;
    };
    height: string;
    radius?: string;
  }>();
  const { t } = useI18n();

  const getCountTotal = computed(() => {
    const { stepSuccessCount, stepFakeErrorCount, stepErrorCount, stepPendingCount } = props.reportDetail;
    return stepSuccessCount + stepFakeErrorCount + stepErrorCount + stepPendingCount;
  });

  const colorData = computed(() => {
    if (
      props.reportDetail.stepSuccessCount === 0 &&
      props.reportDetail.stepErrorCount === 0 &&
      props.reportDetail.stepFakeErrorCount === 0 &&
      props.reportDetail.stepPendingCount === 0
    ) {
      return [
        {
          percentage: 100,
          color: 'var(--color-text-n8)',
        },
      ];
    }
    return [
      {
        percentage: (props.reportDetail.stepSuccessCount / getCountTotal.value) * 100,
        color: 'rgb(var(--success-6))',
      },
      {
        percentage: (props.reportDetail.stepFakeErrorCount / getCountTotal.value) * 100,
        color: 'rgb(var(--danger-6))',
      },
      {
        percentage: (props.reportDetail.stepErrorCount / getCountTotal.value) * 100,
        color: 'rgb(var(--warning-6))',
      },
      {
        percentage: (props.reportDetail.stepPendingCount / getCountTotal.value) * 100,
        color: 'var(--color-text-input-border)',
      },
    ];
  });
</script>

<style lang="less" scoped>
  .popover-label-td {
    @apply flex items-center;

    padding: 8px 8px 0 0;
    color: var(--color-text-4);
  }
  .popover-value-td {
    @apply font-medium;

    padding-top: 8px;
    color: var(--color-text-1);
  }
</style>
