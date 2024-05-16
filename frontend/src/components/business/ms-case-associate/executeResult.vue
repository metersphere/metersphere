<template>
  <div v-if="props.executeResult" class="flex items-center">
    <MsIcon
      :type="lastExecuteResultMap[props.executeResult]?.icon || ''"
      class="mr-1"
      :size="16"
      :style="{ color: lastExecuteResultMap[props.executeResult]?.color }"
    ></MsIcon>
    <span class="text-[14px]">{{ lastExecuteResultMap[props.executeResult]?.statusText || '-' }}</span>
  </div>
</template>

<script setup lang="ts">
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { LastExecuteResults, StatusType } from '@/enums/caseEnum';

  const { t } = useI18n();

  const props = defineProps<{
    executeResult?: LastExecuteResults;
  }>();

  const lastExecuteResultMap = {
    PENDING: {
      label: 'PENDING',
      icon: StatusType.PENDING,
      statusText: t('caseManagement.featureCase.nonExecution'),
      color: 'var(--color-text-brand)',
    },
    SUCCESS: {
      label: 'SUCCESS',
      icon: StatusType.SUCCESS,
      statusText: t('common.success'),
      color: '',
    },
    BLOCKED: {
      label: 'BLOCKED',
      icon: StatusType.BLOCKED,
      statusText: t('caseManagement.featureCase.chokeUp'),
      color: 'rgb(var(--warning-6))',
    },
    ERROR: {
      label: 'ERROR',
      icon: StatusType.ERROR,
      statusText: t('caseManagement.featureCase.failure'),
      color: '',
    },
  };

  // const status = computed(() => {
  //   if (props.executeResult) {
  //     const config = lastExecuteResultMap[props.executeResult];
  //     if (config) {
  //       return {
  //         text: t(config?.statusText || ''),
  //       };
  //     }
  //   }
  // });
</script>

<style lang="less" scoped></style>
