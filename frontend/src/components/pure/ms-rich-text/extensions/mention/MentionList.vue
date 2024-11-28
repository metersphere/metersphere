<template>
  <div class="items">
    <template v-if="items.length">
      <button
        v-for="(item, index) in items"
        :key="index"
        class="item one-line-text"
        :class="{ 'is-selected': index === selectedIndex }"
        @click="selectItem(index)"
      >
        {{ item.name }}
      </button>
    </template>
    <div v-else class="item"> No result </div>
  </div>
</template>

<script lang="ts" setup>
  import type { UserListItem } from '@/models/setting/user';

  const props = defineProps<{
    items: UserListItem[];
    command: (item: UserListItem) => void;
  }>();

  const selectedIndex = ref(0);

  watch(
    () => props.items,
    () => {
      selectedIndex.value = 0;
    }
  );

  function handleKeyUp() {
    selectedIndex.value = (selectedIndex.value + props.items.length - 1) % props.items.length;
  }

  function handleKeyDown() {
    selectedIndex.value = (selectedIndex.value + 1) % props.items.length;
  }

  function handleSelectItem(index: number) {
    const item = props.items[index];

    if (item) {
      props.command({ id: item.id, label: `${item.name}` } as any);
    }
  }

  function handleKeyEnter() {
    handleSelectItem(selectedIndex.value);
  }

  function onKeyDown({ event }: { event: KeyboardEvent }) {
    if (event.key === 'ArrowUp' || (event.key === 'k' && event.ctrlKey)) {
      handleKeyUp();
      return true;
    }

    if (event.key === 'ArrowDown' || (event.key === 'j' && event.ctrlKey)) {
      handleKeyDown();
      return true;
    }

    if (event.key === 'Enter') {
      handleKeyEnter();
      return true;
    }
    return false;
  }

  function selectItem(index: any) {
    const item = props.items[index];
    if (item) {
      props.command({ id: item.id, label: `${item.name}`, style: 'color:blur' } as any);
    }
  }

  defineExpose({
    onKeyDown,
  });
</script>

<style lang="less">
  .items {
    position: relative;
    overflow: auto;
    padding: 8px;
    max-height: 220px;
    border-radius: 4px;
    color: block;
    background-color: var(--color-text-fff);
    box-shadow: 0 0 0 1px rgba(0 0 0 / 5%), 0 10px 20px rgba(0 0 0 / 10%);
    .ms-scroll-bar();
  }
  .item {
    display: block;
    margin: 0;
    padding: 4px 8px;
    width: 100%;
    border-radius: 4px;
    text-align: left;
    background: transparent;
    &.is-selected {
      background: var(--color-bg-3);
    }
    &:hover {
      background: var(--color-bg-3);
    }
  }
</style>
