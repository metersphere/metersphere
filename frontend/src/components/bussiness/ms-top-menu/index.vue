<template>
  <a-menu
    v-show="appStore.topMenus.length > 0"
    v-model:selected-keys="activeMenus"
    class="bg-transparent"
    mode="horizontal"
    @menu-item-click="setCurrentTopMenu"
  >
    <a-menu-item v-for="menu of appStore.topMenus" :key="(menu.name as string)" @click="jumpPath(menu.name)">
      {{ t(menu.meta?.locale || '') }}
    </a-menu-item>
  </a-menu>
</template>

<script setup lang="ts">
  import { Ref, ref, watch } from 'vue';
  import { useRouter, RouteRecordRaw, RouteRecordNormalized, RouteRecordName } from 'vue-router';
  import { cloneDeep } from 'lodash-es';
  import { useAppStore } from '@/store';
  import { listenerRouteChange } from '@/utils/route-listener';
  import usePermission from '@/hooks/usePermission';
  import appClientMenus from '@/router/app-menus';
  import { useI18n } from '@/hooks/useI18n';

  const copyRouter = cloneDeep(appClientMenus) as RouteRecordNormalized[];
  const permission = usePermission();
  const appStore = useAppStore();
  const router = useRouter();
  const { t } = useI18n();
  const activeMenus: Ref<RouteRecordName[]> = ref([]);

  watch(
    () => appStore.getCurrentTopMenu?.name,
    (val) => {
      activeMenus.value = [val || ''];
    }
  );

  function setCurrentTopMenu(key: string) {
    const secParent = appStore.topMenus.find((el: RouteRecordRaw) => {
      return (el?.name as string).includes(key);
    });

    if (secParent) {
      appStore.setCurrentTopMenu(secParent);
    }
  }

  /**
   * 监听路由变化，存储打开的三级子路由
   */
  listenerRouteChange((newRoute) => {
    const { name } = newRoute;
    copyRouter.forEach((el: RouteRecordRaw) => {
      // 权限校验通过
      if (permission.accessRouter(el)) {
        if (name && (name as string).includes((el?.name as string) || '')) {
          const currentParent = el?.children?.find(
            (item) => name && (name as string).includes((item?.name as string) || '')
          );
          appStore.setTopMenus(currentParent?.children?.filter((item) => item.meta?.isTopMenu));
          setCurrentTopMenu(name as string);
        }
      }
    });
  }, true);

  function jumpPath(route: RouteRecordName | undefined) {
    router.push({ name: route });
  }
</script>

<style lang="less" scoped></style>
