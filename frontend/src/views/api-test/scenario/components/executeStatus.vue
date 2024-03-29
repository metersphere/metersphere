<template>
  <MsTag :self-style="status.style" :size="props.size"> {{ status.text }}</MsTag>
</template>

<script setup lang="ts">
  import MsTag, { Size } from '@/components/pure/ms-tag/ms-tag.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ExecuteStatusFilters } from '@/enums/apiEnum';

  const props = defineProps<{
    status: ExecuteStatusFilters;
    size?: Size;
  }>();

  const { t } = useI18n();

  const statusMap = {
    [ExecuteStatusFilters.PENDING]: {
      bgColor: 'var(--color-text-n8)',
      color: 'var(--color-text-4)',
      text: 'apiScenario.executeHistory.status.pending',
    },
    [ExecuteStatusFilters.RUNNING]: {
      bgColor: 'rgb(var(--link-2))',
      color: 'rgb(var(--link-5))',
      text: 'apiScenario.executeHistory.status.running',
    },
    // [ExecuteStatusFilters.RERUNNING]: {
    //   bgColor: 'rgb(var(--link-2))',
    //   color: 'rgb(var(--link-6))',
    //   text: 'apiScenario.executeHistory.status.rerunning',
    // },
    [ExecuteStatusFilters.ERROR]: {
      bgColor: 'rgb(var(--danger-2))',
      color: 'rgb(var(--danger-5))',
      text: 'apiScenario.executeHistory.status.error',
    },
    [ExecuteStatusFilters.SUCCESS]: {
      bgColor: 'rgb(var(--success-2))',
      color: 'rgb(var(--success-5))',
      text: 'apiScenario.executeHistory.status.success',
    },
    [ExecuteStatusFilters.FAKE_ERROR]: {
      bgColor: 'rgb(var(--warning-2))',
      color: 'rgb(var(--warning-5))',
      text: 'apiScenario.executeHistory.status.fake.error',
    },
    [ExecuteStatusFilters.STOPPED]: {
      bgColor: 'rgb(var(--link-2))',
      color: 'rgb(var(--color-border-2))',
      text: 'apiScenario.executeHistory.status.fake.stopped',
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
