<template>
  <a-popover position="bottom" content-class="testPlanTable-caseCount-popper" :disabled="getFunctionalCount(id) < 1">
    <div>{{ getFunctionalCount(id) }}</div>
    <template #content>
      <table class="min-w-[140px] max-w-[176px]">
        <tr>
          <td class="popover-label-td !pt-0">
            <div>{{ t('testPlan.testPlanIndex.TotalCases') }}</div>
          </td>
          <td class="popover-value-td !pt-0">
            {{ defaultCountDetailMap[id]?.caseTotal ?? '0' }}
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="text-[var(--color-text-1)]">{{ t('testPlan.testPlanIndex.functionalUseCase') }}</div>
          </td>
          <td class="popover-value-td">
            {{ defaultCountDetailMap[id]?.functionalCaseCount ?? '0' }}
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="text-[var(--color-text-1)]">{{ t('testPlan.testPlanIndex.apiCase') }}</div>
          </td>
          <td class="popover-value-td">
            {{ defaultCountDetailMap[id]?.apiCaseCount ?? '0' }}
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="text-[var(--color-text-1)]">{{ t('testPlan.testPlanIndex.apiScenarioCase') }}</div>
          </td>
          <td class="popover-value-td">
            {{ defaultCountDetailMap[id]?.apiScenarioCount ?? '0' }}
          </td>
        </tr>
      </table>
    </template>
  </a-popover>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';

  import { PassRateCountDetail } from '@/models/testPlan/testPlan';

  const props = defineProps<{
    id: string;
    defaultCountDetailMap: Record<string, PassRateCountDetail>;
  }>();

  const { t } = useI18n();

  function getFunctionalCount(id: string) {
    return props.defaultCountDetailMap[id]?.caseTotal ?? 0;
  }
</script>

<style lang="less">
  .testPlanTable-caseCount-popper {
    padding: 16px;
    .arco-popover-title {
      @apply hidden;
    }
    .arco-popover-content {
      @apply mt-0;
    }
  }
</style>

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
