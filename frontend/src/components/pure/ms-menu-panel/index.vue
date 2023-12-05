<template>
  <div class="menu-wrapper">
    <div class="menu-content">
      <div v-if="props.title" class="mb-2 font-medium">{{ props.title }}</div>
      <div class="menu">
        <div
          v-for="(item, index) of props.menuList"
          :key="item.name"
          class="menu-item px-2"
          :class="{
            'text-[--color-text-4]': item.level === 1,
            'menu-item--active': item.name === currentKey && item.level !== 1,
            'cursor-pointer': item.level !== 1,
            'mt-[2px]': item.level === 1 && index !== 0,
            [props.activeClass || '']: item.name === currentKey && item.level !== 1,
          }"
          :style="{
            'border-top': item.level === 1 && index !== 0 ? '1px solid var(--color-border-2)' : 'none',
          }"
        >
          <div @click="toggleMenu(item.name)">{{ item.title }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  const props = defineProps<{
    title?: string;
    defaultKey?: string;
    menuList: {
      title: string;
      level: number;
      name: string;
    }[];
    activeClass?: string;
  }>();
  const emit = defineEmits<{
    (e: 'toggleMenu', val: string): void;
  }>();

  const currentKey = ref(props.defaultKey);

  watch(
    () => props.defaultKey,
    (val) => {
      currentKey.value = val;
    }
  );

  const toggleMenu = (itemName: string) => {
    if (itemName) {
      currentKey.value = itemName;
      emit('toggleMenu', itemName);
    }
  };
</script>

<style lang="less" scoped>
  .menu-wrapper {
    border-radius: 12px;
    color: var(--color-text-1);
    box-shadow: 0 0 10px rgb(120 56 135/ 5%);
    .menu-content {
      width: 100%;
      .menu {
        .menu-item {
          height: 38px;
          line-height: 38px;
          font-family: 'PingFang SC';
        }
      }
    }
  }
  .menu-item--active {
    border-radius: 4px;
    color: rgb(var(--primary-5));
    background-color: rgb(var(--primary-1));
  }
</style>
