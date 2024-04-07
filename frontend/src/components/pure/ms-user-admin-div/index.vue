<template>
  <div class="flex flex-row flex-nowrap">
    <a-tooltip content-class="max-w-[600px]" :content="props.name">
      <div class="flex flex-row flex-nowrap">{{ characterLimit(props.name) }}</div>
    </a-tooltip>
    <div v-if="props.isAdmin" class="ml-1 flex flex-row flex-nowrap text-[var(--color-text-4)]">{{
      `(${t('common.admin')})`
    }}</div>
  </div>
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';
  import { characterLimit } from '@/utils';

  const { t } = useI18n();
  const props = withDefaults(defineProps<{ name?: string; isAdmin?: boolean }>(), { isAdmin: false });
</script>