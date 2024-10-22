<template>
  <a-tag
    :color="executeStatusMap[props.status]?.color"
    :class="executeStatusMap[props.status]?.class"
    :size="props.size"
  >
    {{ t(executeStatusMap[props.status]?.label) }}
  </a-tag>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';

  import { ReportExecStatus } from '@/enums/apiEnum';
  import { ExecuteStatusEnum } from '@/enums/taskCenter';

  import { executeStatusMap } from './config';

  const props = defineProps<{
    status: ReportExecStatus | ExecuteStatusEnum;
    size?: 'small' | 'medium' | 'large';
  }>();
  const { t } = useI18n();
</script>

<style lang="less" scoped></style>
