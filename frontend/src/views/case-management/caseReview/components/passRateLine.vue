<template>
  <MsColorLine
    :color-data="[
      {
        percentage: (props.reviewDetail.passCount / props.reviewDetail.caseCount) * 100,
        color: 'rgb(var(--success-6))',
      },
      {
        percentage: (props.reviewDetail.failCount / props.reviewDetail.caseCount) * 100,
        color: 'rgb(var(--danger-6))',
      },
      {
        percentage: (props.reviewDetail.reviewCount / props.reviewDetail.caseCount) * 100,
        color: 'rgb(var(--warning-6))',
      },
      {
        percentage: (props.reviewDetail.reviewingCount / props.reviewDetail.caseCount) * 100,
        color: 'rgb(var(--link-6))',
      },
    ]"
    :height="props.height"
    :radius="props.radius"
  >
    <template #popoverContent>
      <table>
        <tr>
          <td class="pr-[8px] text-[var(--color-text-4)]">{{ t('caseManagement.caseReview.progress') }}</td>
          <td class="font-medium text-[var(--color-text-1)]">
            {{
              `${(
                ((props.reviewDetail.passCount + props.reviewDetail.failCount) / props.reviewDetail.caseCount) *
                100
              ).toFixed(2)}%`
            }}
            <span
              >({{
                `${props.reviewDetail.passCount + props.reviewDetail.failCount}/${props.reviewDetail.caseCount}`
              }})</span
            >
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--success-6))]"></div>
            <div>{{ t('caseManagement.caseReview.pass') }}</div>
          </td>
          <td class="popover-value-td">
            {{ props.reviewDetail.passCount }}
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--danger-6))]"></div>
            <div>{{ t('caseManagement.caseReview.fail') }}</div>
          </td>
          <td class="popover-value-td">
            {{ props.reviewDetail.failCount }}
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--warning-6))]"></div>
            <div>{{ t('caseManagement.caseReview.reReview') }}</div>
          </td>
          <td class="popover-value-td">
            {{ props.reviewDetail.reviewCount }}
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--link-6))]"></div>
            <div>{{ t('caseManagement.caseReview.reviewing') }}</div>
          </td>
          <td class="popover-value-td">
            {{ props.reviewDetail.reviewingCount }}
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
    reviewDetail: {
      passCount: number;
      failCount: number;
      reviewCount: number;
      reviewingCount: number;
      caseCount: number;
      [key: string]: any;
    };
    height: string;
    radius?: string;
  }>();
  const { t } = useI18n();
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
