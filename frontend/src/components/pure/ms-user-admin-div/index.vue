<template>
  <div class="flex w-full flex-row flex-nowrap">
    <a-tooltip :content="props.name">
      <div class="one-line-text max-w-[calc(100%-60px)]">{{ props.name }}</div>
    </a-tooltip>
    <div v-if="props.isAdmin" class="ml-1 flex flex-row flex-nowrap text-[var(--color-text-4)]">{{
      `(${t('common.admin')})`
    }}</div>
  </div>
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();
  const props = withDefaults(defineProps<{ name?: string; isAdmin?: boolean }>(), { isAdmin: false });
</script>
