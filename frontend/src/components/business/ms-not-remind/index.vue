<template>
  <a-alert v-if="!getIsVisited()" :show-icon="false" :type="props.type" closable @close="addVisited">
    <slot>{{ t(props.tip || '') }}</slot>
    <template #close-element>
      <span class="text-[14px]">{{ t('common.notRemind') }}</span>
    </template>
  </a-alert>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';
  import useVisit from '@/hooks/useVisit';

  const props = defineProps<{
    tip?: string;
    type?: 'error' | 'normal' | 'success' | 'warning' | 'info';
    visitedKey: string;
  }>();

  const { t } = useI18n();

  const { addVisited, getIsVisited } = useVisit(props.visitedKey);
</script>

<style lang="less" scoped></style>
