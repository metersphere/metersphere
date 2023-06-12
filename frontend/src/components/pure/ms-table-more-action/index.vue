<template>
  <a-dropdown trigger="hover" @select="selectHandler">
    <MsButton><icon-more /></MsButton>
    <template #content>
      <template v-for="item of props.list">
        <a-divider v-if="item.isDivider" :key="`${item.label}-divider`" class="mx-0 my-[6px]" />
        <a-doption v-else :key="item.label" :class="item.danger ? 'error-6' : ''">{{ item.label }}</a-doption>
      </template>
    </template>
  </a-dropdown>
</template>

<script setup lang="ts">
  import MsButton from '@/components/pure/ms-button/index.vue';

  import type { ActionsItem, SelectedValue } from './types';

  const props = defineProps<{
    list: ActionsItem[];
  }>();

  const emit = defineEmits(['select']);

  function selectHandler(value: SelectedValue) {
    const item = props.list.find((e: ActionsItem) => e.label === value);
    emit('select', item);
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
