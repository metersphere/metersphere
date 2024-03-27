<template>
  <MsTag :self-style="status.style" :size="props.size"> {{ status.text }}</MsTag>
</template>

<script setup lang="ts">
  import MsTag, { Size } from '@/components/pure/ms-tag/ms-tag.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { CommonScriptStatusEnum } from '@/enums/commonScriptStatusEnum';

  const props = defineProps<{
    status: CommonScriptStatusEnum;
    size?: Size;
  }>();

  const { t } = useI18n();

  const statusMap = {
    [CommonScriptStatusEnum.PASSED]: {
      bgColor: 'rgb(var(--success-2))',
      color: 'rgb(var(--success-5))',
      text: 'project.commonScript.testsPass',
    },
    [CommonScriptStatusEnum.DRAFT]: {
      bgColor: 'var(--color-text-n8)',
      color: 'var(--color-text-2)',
      text: 'project.commonScript.draft',
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
