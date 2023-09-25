<template>
  <a-dropdown :trigger="props.trigger || 'hover'" @select="selectHandler" @popup-visible-change="visibleChange">
    <slot>
      <MsButton><icon-more /></MsButton>
    </slot>
    <template #content>
      <template v-for="item of props.list">
        <a-divider v-if="item.isDivider" :key="`${item.label}-divider`" margin="4px" />
        <a-doption v-else :key="item.label" :class="item.danger ? 'error-6' : ''">{{ t(item.label || '') }}</a-doption>
      </template>
    </template>
  </a-dropdown>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';
  import MsButton from '@/components/pure/ms-button/index.vue';

  import type { ActionsItem, SelectedValue } from './types';

  const { t } = useI18n();
  const props = defineProps<{
    list: ActionsItem[];
    trigger?: 'click' | 'hover' | 'focus' | 'contextMenu' | ('click' | 'hover' | 'focus' | 'contextMenu')[] | undefined;
  }>();

  const emit = defineEmits(['select', 'close']);

  function selectHandler(value: SelectedValue) {
    const item = props.list.find((e: ActionsItem) => t(e.label || '') === value);
    emit('select', item);
  }

  function visibleChange(val: boolean) {
    if (!val) {
      emit('close');
    }
  }
</script>

<style lang="less" scoped>
  .error-6 {
    color: rgb(var(--danger-6));
    &:hover {
      color: rgb(var(--danger-6));
    }
  }
</style>
