<template>
  <a-menu
    v-if="appStore.topMenus.length > 0"
    class="bg-transparent"
    mode="horizontal"
    :default-selected-keys="[appStore.topMenus[0].name]"
  >
    <a-menu-item v-for="menu of appStore.topMenus" :key="(menu.name as string)" @click="jumpPath(menu.name)">
      {{ t(menu.meta?.locale || '') }}
    </a-menu-item>
  </a-menu>
</template>

<script setup lang="ts">
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
        }
      }
    });
  }, true);

  function jumpPath(route: RouteRecordName | undefined) {
    router.push({ name: route });
  }
</script>

<style lang="less" scoped></style>
