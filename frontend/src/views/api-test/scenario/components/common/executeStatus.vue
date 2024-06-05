<template>
  <MsTag v-if="status" :self-style="status.style" :size="props.size">
    <div class="flex items-center justify-between gap-[4px]">
      <span>{{ status.text }}</span>
      <span v-if="props.extraText">{{ props.extraText }}</span>
    </div>
  </MsTag>
</template>

<script setup lang="ts">
  import MsTag, { Size } from '@/components/pure/ms-tag/ms-tag.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ScenarioExecuteStatus } from '@/enums/apiEnum';

  const props = defineProps<{
    status?: ScenarioExecuteStatus;
    size?: Size;
    extraText?: string;
  }>();

  const { t } = useI18n();

  const statusMap = {
    [ScenarioExecuteStatus.STOP]: {
      bgColor: 'rgb(var(--link-2))',
      color: 'rgb(var(--link-6))',
      text: 'common.stopped',
    },
    [ScenarioExecuteStatus.EXECUTING]: {
      bgColor: 'rgb(var(--link-2))',
      color: 'rgb(var(--link-6))',
      text: 'common.running',
    },
    [ScenarioExecuteStatus.FAILED]: {
      bgColor: 'rgb(var(--danger-2))',
      color: 'rgb(var(--danger-6))',
      text: 'common.fail',
    },
    [ScenarioExecuteStatus.FAKE_ERROR]: {
      bgColor: 'rgb(var(--warning-2))',
      color: 'rgb(var(--warning-5))',
      text: 'common.fakeError',
    },
    [ScenarioExecuteStatus.SUCCESS]: {
      bgColor: 'rgb(var(--success-2))',
      color: 'rgb(var(--success-6))',
      text: 'common.success',
    },
    [ScenarioExecuteStatus.UN_EXECUTE]: {
      bgColor: 'var(--color-text-n8)',
      color: 'var(--color-text-1)',
      text: 'common.unExecute',
    },
  };
  const status = computed(() => {
    if (props.status) {
      const config = statusMap[props.status];
      return {
        style: {
          backgroundColor: config?.bgColor,
          color: config?.color,
        },
        text: t(config?.text),
      };
    }
  });
</script>

<style lang="less" scoped></style>
