<template>
  <a-empty :description="t('common.noData')" :in-config-provider="props.inConfigProvider">
    <template #image>
      <span class="hidden"></span>
    </template>
  </a-empty>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';

  const props = withDefaults(
    defineProps<{
      inConfigProvider?: boolean;
    }>(),
    {
      inConfigProvider: false,
    }
  );

  const { t } = useI18n();
</script>

<style lang="less" scoped></style>
