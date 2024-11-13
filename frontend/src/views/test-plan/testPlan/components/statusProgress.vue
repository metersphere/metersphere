<template>
  <MsColorLine :color-data="colorData" :height="props.height" :radius="props.radius">
    <template #popoverContent>
      <table class="min-w-[144px]">
        <tr v-if="props.type === testPlanTypeEnum.TEST_PLAN">
          <td class="popover-label-td !pt-0">
            <div>{{ t('testPlan.testPlanIndex.threshold') }}</div>
          </td>
          <td class="popover-value-td !pt-0"> {{ detailCount.passThreshold }}% </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div>{{ t('testPlan.testPlanIndex.executionProgress') }}</div>
          </td>
          <td class="popover-value-td"> {{ detailCount.executeRate }}% </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--success-6))]"></div>
            <div>{{ t('common.success') }}</div>
          </td>
          <td class="popover-value-td">
            {{ detailCount.successCount }}
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--danger-6))]"></div>
            <div>{{ t('common.fail') }}</div>
          </td>
          <td class="popover-value-td">
            {{ detailCount.errorCount }}
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--warning-6))]"></div>
            <div>{{ t('common.fakeError') }}</div>
          </td>
          <td class="popover-value-td">
            {{ detailCount.fakeErrorCount }}
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[var(--color-fill-p-3)]"></div>
            <div>{{ t('common.block') }}</div>
          </td>
          <td class="popover-value-td">
            {{ detailCount.blockCount }}
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[var(--color-text-input-border)]"></div>
            <div>{{ t('common.unExecute') }}</div>
          </td>
          <td class="popover-value-td">
            {{ detailCount.pendingCount }}
          </td>
        </tr>
      </table>
    </template>
  </MsColorLine>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsColorLine from '@/components/pure/ms-color-line/index.vue';

  import { defaultDetailCount } from '@/config/testPlan';
  import { useI18n } from '@/hooks/useI18n';

  import type { PassRateCountDetail } from '@/models/testPlan/testPlan';
  import { testPlanTypeEnum } from '@/enums/testPlanEnum';

  const props = defineProps<{
    statusDetail: PassRateCountDetail | undefined;
    height: string;
    type: testPlanTypeEnum;
    radius?: string;
  }>();
  const { t } = useI18n();

  const detailCount = ref({ ...defaultDetailCount });
  watchEffect(() => {
    detailCount.value = {
      ...defaultDetailCount,
      ...props.statusDetail,
    };
  });

  const colorData = computed(() => {
    const { caseTotal, successCount, errorCount, fakeErrorCount, blockCount, pendingCount } = detailCount.value;
    if (fakeErrorCount === 0 && blockCount === 0 && errorCount === 0 && successCount === 0 && pendingCount === 0) {
      return [
        {
          percentage: 100,
          color: 'var(--color-text-n8)',
        },
      ];
    }
    return [
      {
        percentage: (successCount / caseTotal) * 100,
        color: 'rgb(var(--success-6))',
      },
      {
        percentage: (errorCount / caseTotal) * 100,
        color: 'rgb(var(--danger-6))',
      },
      {
        percentage: (blockCount / caseTotal) * 100,
        color: 'var(--color-fill-p-3)',
      },
      {
        percentage: (fakeErrorCount / caseTotal) * 100,
        color: 'rgb(var(--warning-6))',
      },
      {
        percentage: (pendingCount / caseTotal) * 100,
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
    @apply text-right font-medium;

    padding-top: 8px;
    color: var(--color-text-1);
  }
</style>
