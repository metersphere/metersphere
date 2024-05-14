<template>
  <MsColorLine :color-data="colorData" :height="props.height" :radius="props.radius">
    <!-- TODO 这个页面还得根据实际业务调整-->
    <template #popoverContent>
      <table>
        <tr>
          <td class="pr-[8px] text-[var(--color-text-4)]">{{ t('caseManagement.caseReview.progress') }}</td>
          <td class="font-medium text-[var(--color-text-1)]">
            {{ progress }}
            <span> ({{ `${props.detail.passCount + props.detail.unPassCount}/${props.detail.caseCount}` }}) </span>
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--success-6))]"></div>
            <div>{{ t('caseManagement.caseReview.pass') }}</div>
          </td>
          <td class="popover-value-td">
            {{ props.detail.passCount }}
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--danger-6))]"></div>
            <div>{{ t('caseManagement.caseReview.fail') }}</div>
          </td>
          <td class="popover-value-td">
            {{ props.detail.unPassCount }}
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--warning-6))]"></div>
            <div>{{ t('caseManagement.caseReview.reReview') }}</div>
          </td>
          <td class="popover-value-td">
            {{ props.detail.reReviewedCount }}
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--link-6))]"></div>
            <div>{{ t('caseManagement.caseReview.reviewing') }}</div>
          </td>
          <td class="popover-value-td">
            {{ props.detail.underReviewedCount }}
          </td>
        </tr>
      </table>
    </template>
  </MsColorLine>
</template>

<script setup lang="ts">
  import MsColorLine from '@/components/pure/ms-color-line/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  const props = defineProps<{
    detail: {
      passCount: number;
      unPassCount: number;
      reReviewedCount: number;
      underReviewedCount: number;
      caseCount: number;
      [key: string]: any;
    };
    height: string;
    radius?: string;
  }>();
  const { t } = useI18n();

  const colorData = computed(() => {
    return [
      {
        percentage: 100,
        color: 'var(--color-text-n8)',
      },
    ];
    // TODO 实际业务调整
    // return [
    //   {
    //     percentage: (props.detail.passCount / props.detail.caseCount) * 100,
    //     color: 'rgb(var(--success-6))',
    //   },
    //   {
    //     percentage: (props.detail.unPassCount / props.detail.caseCount) * 100,
    //     color: 'rgb(var(--danger-6))',
    //   },
    //   {
    //     percentage: (props.detail.reReviewedCount / props.detail.caseCount) * 100,
    //     color: 'rgb(var(--warning-6))',
    //   },
    //   {
    //     percentage: (props.detail.underReviewedCount / props.detail.caseCount) * 100,
    //     color: 'rgb(var(--link-6))',
    //   },
    // ];
  });
  const progress = computed(() => {
    const result = ((props.detail.passCount + props.detail.unPassCount) / props.detail.caseCount) * 100;
    return `${Number.isNaN(result) ? 0 : result.toFixed(2)}%`;
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
