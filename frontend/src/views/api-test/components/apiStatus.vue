<template>
  <MsTag :self-style="status.style" :size="props.size"> {{ status.text }}</MsTag>
</template>

<script setup lang="ts">
  import MsTag, { Size } from '@/components/pure/ms-tag/ms-tag.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ApiScenarioStatus, RequestCaseStatus, RequestDefinitionStatus } from '@/enums/apiEnum';

  const props = defineProps<{
    status: RequestDefinitionStatus | ApiScenarioStatus | RequestCaseStatus;
    size?: Size;
  }>();

  const { t } = useI18n();

  const statusMap = {
    [RequestDefinitionStatus.DEPRECATED]: {
      bgColor: 'var(--color-text-n8)',
      color: 'var(--color-text-4)',
      text: 'apiTestManagement.deprecate',
    },
    [RequestDefinitionStatus.PROCESSING]: {
      bgColor: 'rgb(var(--link-2))',
      color: 'rgb(var(--link-5))',
      text: 'apiTestManagement.processing',
    },
    [ApiScenarioStatus.UNDERWAY]: {
      bgColor: 'rgb(var(--link-2))',
      color: 'rgb(var(--link-5))',
      text: 'apiTestManagement.processing',
    },
    [RequestDefinitionStatus.DEBUGGING]: {
      bgColor: 'rgb(var(--link-2))',
      color: 'rgb(var(--link-6))',
      text: 'apiTestManagement.debugging',
    },
    [RequestDefinitionStatus.DONE]: {
      bgColor: 'rgb(var(--success-2))',
      color: 'rgb(var(--success-6))',
      text: 'apiTestManagement.done',
    },
    [ApiScenarioStatus.COMPLETED]: {
      bgColor: 'rgb(var(--success-2))',
      color: 'rgb(var(--success-6))',
      text: 'apiTestManagement.done',
    },
  };
  const status = computed(() => {
    const config = statusMap[props.status];
    return {
      style: {
        backgroundColor: config?.bgColor,
        color: config?.color,
      },
      text: t(config?.text),
    };
  });
</script>

<style lang="less" scoped></style>
