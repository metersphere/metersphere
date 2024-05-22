<template>
  <div class="mb-[16px]">
    <div class="flex items-center justify-between">
      <div
        ><span class="text-[var(--color-text-4)]">{{ t(statusObject.label) }}</span
        ><span class="ml-2 font-medium text-[var(--color-text-1)]">{{ countDetailData[props.status] }}</span></div
      >
      <div class="font-medium text-[var(--color-text-1)]">{{ getPassRate }}</div>
    </div>

    <MsColorLine :color-data="colorData" height="8px" radius="2px">
      <template #popoverContent>
        <table class="min-w-[230px]">
          <tr class="flex items-center">
            <td class="w-[50%]">
              <div>{{ t('testPlan.testPlanIndex.TotalCases') }}</div>
            </td>
            <td class="-ml-[2px] font-medium">
              {{ props.detail.caseTotal || 0 }}
            </td>
          </tr>
          <tr class="popover-tr">
            <td class="popover-label-td">
              <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[var(--color-text-input-border)]"></div>
              <div>{{ t('common.unExecute') }}</div>
            </td>
            <td class="popover-value-td-count">
              {{ addCommasToNumber(countDetailData.pending) }}
            </td>
            <td class="popover-value-td-pass">
              {{ statusExecuteRate.pendingRateResult }}
            </td>
          </tr>
          <tr class="popover-tr">
            <td class="popover-label-td">
              <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--success-6))]"></div>
              <div>{{ t('common.success') }}</div>
            </td>
            <td class="popover-value-td-count">
              {{ addCommasToNumber(countDetailData.success) }}
            </td>
            <td class="popover-value-td-pass">
              {{ statusExecuteRate.successRateResult }}
            </td>
          </tr>
          <!-- TODO 没有接口暂时不上 -->
          <!-- <tr>
            <td class="popover-label-td">
              <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--warning-6))]"></div>
              <div>{{ t('common.fakeError') }}</div>
            </td>
            <td class="popover-value-td">
              {{ detailCount.fakeErrorCount }}
            </td>
          </tr> -->
          <tr class="popover-tr">
            <td class="popover-label-td">
              <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[var(--color-fill-p-3)]"></div>
              <div>{{ t('common.block') }}</div>
            </td>
            <td class="popover-value-td-count">
              {{ addCommasToNumber(countDetailData.block) }}
            </td>
            <td class="popover-value-td-pass">
              {{ statusExecuteRate.blockRateResult }}
            </td>
          </tr>
          <tr class="popover-tr">
            <td class="popover-label-td">
              <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--danger-6))]"></div>
              <div>{{ t('common.fail') }}</div>
            </td>
            <td class="popover-value-td-count">
              {{ addCommasToNumber(countDetailData.error) }}
            </td>
            <td class="popover-value-td-pass">
              {{ statusExecuteRate.errorRateResult }}
            </td>
          </tr>
        </table>
      </template>
    </MsColorLine>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsColorLine from '@/components/pure/ms-color-line/index.vue';

  import { statusConfig } from '@/config/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import { addCommasToNumber } from '@/utils';

  import type { countDetail, PlanReportDetail } from '@/models/testPlan/testPlanReport';

  const { t } = useI18n();
  const props = defineProps<{
    detail: PlanReportDetail;
    status: keyof countDetail;
  }>();

  const defaultStatus = {
    label: 'common.unExecute',
    value: 'pending',
    color: '#D4D4D8',
    class: 'bg-[var(--color-text-input-border)]',
    rateKey: 'requestPendingRate',
    key: 'PENDING',
  };

  const statusObject = computed(() => {
    return statusConfig.find((e) => e.value === props.status) || defaultStatus;
  });

  const countDetailData = computed(() => {
    return props.detail.functionalCount;
  });

  const colorData = computed(() => {
    if (countDetailData.value[props.status] === 0) {
      return [
        {
          percentage: 100,
          color: 'var(--color-text-n8)',
        },
      ];
    }
    return [
      {
        percentage: (countDetailData.value[props.status] / props.detail.caseTotal) * 100,
        color: statusObject.value.color,
      },
    ];
  });

  const getPassRate = computed(() => {
    const result = (countDetailData.value[props.status] / props.detail.caseTotal) * 100;
    return `${Number.isNaN(result) ? 0 : result.toFixed(2)}%`;
  });

  const calculateRate = (count: number) => {
    const rate = (count / props.detail.caseTotal) * 100;
    return `${Number.isNaN(rate) ? 0 : rate.toFixed(2)}%`;
  };

  const statusExecuteRate = computed(() => {
    const pendingRateResult = calculateRate(countDetailData.value.pending);
    const successRateResult = calculateRate(countDetailData.value.success);
    const errorRateResult = calculateRate(countDetailData.value.error);
    const blockRateResult = calculateRate(countDetailData.value.block);

    return {
      pendingRateResult,
      successRateResult,
      errorRateResult,
      blockRateResult,
    };
  });
</script>

<style scoped lang="less">
  .popover-tr {
    @apply flex items-center justify-between;
    .popover-label-td {
      padding: 8px 8px 0 0;
      width: 220px;
      color: var(--color-text-4);
      @apply flex flex-1 items-center;
    }
    .popover-value-td-count {
      @apply flex-1 text-center font-medium;

      padding-top: 8px;
      color: var(--color-text-1);
    }
    .popover-value-td-pass {
      @apply flex-1 text-right font-medium;

      padding-top: 8px;
      color: var(--color-text-1);
    }
  }
</style>
