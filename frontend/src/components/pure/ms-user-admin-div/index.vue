<template>
  <div class="flex flex-row flex-nowrap">
    <div class="flex flex-row flex-nowrap">{{ props.name }}</div>
    <div v-if="props.isAdmin" class="ml-1 flex flex-row flex-nowrap text-[var(--color-text-4)]">{{
      `(${t('common.admin')})`
    }}</div>
  </div>
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();
  const props = withDefaults(defineProps<{ name: string; isAdmin?: boolean }>(), { isAdmin: false });
</script>
