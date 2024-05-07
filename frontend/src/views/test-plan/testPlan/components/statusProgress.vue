<template>
  <MsColorLine :color-data="colorData" :height="props.height" :radius="props.radius">
    <template #popoverContent>
      <table class="min-w-[144px]">
        <tr>
          <td class="popover-label-td">
            <div>{{ t('project.testPlanIndex.tolerance') }}</div>
          </td>
          <td class="popover-value-td">
            {{ props.statusDetail.tolerance }}
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div>{{ t('project.testPlanIndex.executionProgress') }}</div>
          </td>
          <td class="popover-value-td">
            {{ props.statusDetail.executionProgress }}
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[var(--color-text-input-border)]"></div>
            <div>{{ t('common.unExecute') }}</div>
          </td>
          <td class="popover-value-td">
            {{ props.statusDetail.UNPENDING }}
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--link-6))]"></div>
            <div>{{ t('common.running') }}</div>
          </td>
          <td class="popover-value-td">
            {{ props.statusDetail.RUNNING }}
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--success-6))]"></div>
            <div>{{ t('common.pass') }}</div>
          </td>
          <td class="popover-value-td">
            {{ props.statusDetail.SUCCESS }}
          </td>
        </tr>
        <tr>
          <td class="popover-label-td">
            <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full bg-[rgb(var(--danger-6))]"></div>
            <div>{{ t('common.unPass') }}</div>
          </td>
          <td class="popover-value-td">
            {{ props.statusDetail.ERROR }}
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
    statusDetail: {
      tolerance: number;
      UNPENDING: number;
      RUNNING: number;
      SUCCESS: number;
      ERROR: number;
      executionProgress: string;
      [key: string]: any;
    };
    height: string;
    radius?: string;
  }>();
  const { t } = useI18n();

  const getCountTotal = computed(() => {
    const { UNPENDING, RUNNING, ERROR, SUCCESS } = props.statusDetail;
    return UNPENDING + RUNNING + ERROR + SUCCESS;
  });

  const colorData = computed(() => {
    if (
      props.statusDetail.UNPENDING === 0 &&
      props.statusDetail.RUNNING === 0 &&
      props.statusDetail.ERROR === 0 &&
      props.statusDetail.SUCCESS === 0
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
        percentage: (props.statusDetail.SUCCESS / getCountTotal.value) * 100,
        color: 'rgb(var(--success-6))',
      },
      {
        percentage: (props.statusDetail.ERROR / getCountTotal.value) * 100,
        color: 'rgb(var(--danger-6))',
      },
      {
        percentage: (props.statusDetail.RUNNING / getCountTotal.value) * 100,
        color: 'rgb(var(--link-6))',
      },
      {
        percentage: (props.statusDetail.UNPENDING / getCountTotal.value) * 100,
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
