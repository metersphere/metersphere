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
            {{ props.reportDetail.successCount }}
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--warning-6))]"></div>
            <div>{{ t('report.detail.fakeErrorCount') }}</div>
          </td>
          <td class="popover-value-td">
            {{ props.reportDetail.fakeErrorCount }}
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--danger-6))]"></div>
            <div>{{ t('report.detail.errorCount') }}</div>
          </td>
          <td class="popover-value-td">
            {{ props.reportDetail.errorCount }}
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[var(--color-text-input-border)]"></div>
            <div>{{ t('report.detail.pendingCount') }}</div>
          </td>
          <td class="popover-value-td">
            {{ props.reportDetail.pendingCount }}
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

  const colorData = computed(() => {
    if (
      props.reportDetail.status === 'ERROR' ||
      (props.reportDetail.successCount === 0 &&
        props.reportDetail.errorCount === 0 &&
        props.reportDetail.fakeErrorCount === 0 &&
        props.reportDetail.pendingCount === 0)
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
        percentage: (props.reportDetail.successCount / props.reportDetail.stepTotal) * 100,
        color: 'rgb(var(--success-6))',
      },
      {
        percentage: (props.reportDetail.errorCount / props.reportDetail.stepTotal) * 100,
        color: 'rgb(var(--danger-6))',
      },
      {
        percentage: (props.reportDetail.fakeErrorCount / props.reportDetail.stepTotal) * 100,
        color: 'rgb(var(--warning-6))',
      },
      {
        percentage: (props.reportDetail.pendingCount / props.reportDetail.stepTotal) * 100,
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
