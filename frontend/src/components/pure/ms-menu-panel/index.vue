<template>
  <div class="menu-wrapper">
    <div class="menu-content">
      <div v-if="props.title" class="mb-2 font-medium">{{ props.title }}</div>
      <div class="menu">
        <template v-for="(item, index) of innerMenuList" :key="item.name">
          <div
            v-if="item.showCondition ? item.showCondition() : true"
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
            @click.stop="toggleMenu(item)"
          >
            <div>{{ item.title }}</div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { hasAllPermission } from '@/utils/permission';

  export interface MenuItem {
    title: string;
    level: number;
    name: string;
    showCondition?: () => boolean; // 是否显示
  }
  const props = defineProps<{
    title?: string;
    defaultKey?: string;
    menuList: MenuItem[];
    activeClass?: string;
  }>();
  const emit = defineEmits<{
    (e: 'toggleMenu', val: string): void;
  }>();

  const currentKey = ref(props.defaultKey);

  const innerMenuList = computed(() => {
    return props.menuList.filter((item: any) => hasAllPermission(item.permission));
  });

  watch(
    () => props.defaultKey,
    (val) => {
      currentKey.value = val;
    }
  );

  const toggleMenu = (item: MenuItem) => {
    if (item.level !== 1 && item.name !== currentKey.value) {
      currentKey.value = item.name;
      emit('toggleMenu', item.name);
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
