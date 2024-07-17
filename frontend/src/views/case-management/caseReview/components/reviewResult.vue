<template>
  <div v-if="props.status" class="flex items-center">
    <MsIcon
      :type="resultMap[props.status]?.icon || ''"
      class="mr-[4px]"
      :size="props.iconSize"
      :style="{ color: resultMap[props.status]?.color }"
    ></MsIcon>
    {{ t(resultMap[props.status]?.label) }}
  </div>
</template>

<script setup lang="ts">
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  import { reviewResultMap } from '@/config/caseManagement';
  import { useI18n } from '@/hooks/useI18n';

  import { ReviewResult } from '@/models/caseManagement/caseReview';

  const { t } = useI18n();

  const props = withDefaults(
    defineProps<{
      status?: ReviewResult;
      isPart?: boolean; // 为true时，'UNDER_REVIEWED'字段对应的是'建议'
      iconSize?: number;
    }>(),
    {
      iconSize: 16,
    }
  );

  const resultMap = computed(() =>
    props.isPart
      ? {
          ...reviewResultMap,
          ...{
            PASS: {
              label: 'common.pass',
              color: 'rgb(var(--success-6))',
              icon: 'icon-icon_succeed_filled',
            },
            UNDER_REVIEWED: {
              label: 'caseManagement.caseReview.suggestion',
              color: 'rgb(var(--warning-6))',
              icon: 'icon-icon_warning_filled',
            },
          },
        }
      : reviewResultMap
  );
</script>
