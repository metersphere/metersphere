<template>
  <div class="wrapper flex min-h-[500px]" :style="{ height: 'calc(100vh - 74px)' }">
    <MsMenuPanel
      :title="t('project.permission.projectAndPermission')"
      :default-key="currentKey"
      :menu-list="menuList"
      class="mr-[16px] w-[208px] min-w-[208px] bg-[var(--color-text-fff)] p-[16px]"
      @toggle-menu="toggleMenu"
    />
    <!-- 272:左边MsMenuPanel的宽度224+边距16*3 -->
    <MsCard simple :other-width="272" :min-width="700" :loading="isLoading">
      <router-view></router-view>
    </MsCard>
  </div>
</template>

<script setup lang="ts">
  /**
   * @description 项目管理-项目与权限
   */
  import { onBeforeMount, provide, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsMenuPanel from '@/components/pure/ms-menu-panel/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import usePermission from '@/hooks/usePermission';
  import useLicenseStore from '@/store/modules/setting/license';

  import { ProjectManagementRouteEnum } from '@/enums/routeEnum';

  const { t } = useI18n();
  const permission = usePermission();

  const router = useRouter();
  const route = useRoute();
  const licenseStore = useLicenseStore();

  function memberPermissionShowCondition(): boolean {
    let show = false;
    const routerList = router.getRoutes();
    for (let i = 0; i < routerList.length; i++) {
      const rou = routerList[i];
      if (
        [
          ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_MEMBER,
          ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_USER_GROUP,
        ].includes(rou.name as ProjectManagementRouteEnum)
      ) {
        show = permission.accessRouter(rou);
      }
      if (show) {
        break;
      }
    }
    return show;
  }

  const sourceMenuList = ref([
    {
      key: 'project',
      title: t('project.permission.project'),
      level: 1,
      name: '',
    },
    {
      key: 'projectBasicInfo',
      title: t('project.permission.basicInfo'),
      level: 2,
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_BASIC_INFO,
    },
    {
      key: 'projectMenuManage',
      title: t('project.permission.menuManagement'),
      level: 2,
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT,
    },
    // TODO 第一版不开启版本
    // {
    //   key: 'projectVersion',
    //   title: t('project.permission.projectVersion'),
    //   level: 2,
    //   name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_VERSION,
    // },
    {
      key: 'memberPermission',
      title: t('project.permission.memberPermission'),
      level: 1,
      name: '',
      showCondition: memberPermissionShowCondition,
    },
    {
      key: 'projectMember',
      title: t('project.permission.member'),
      level: 2,
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_MEMBER,
    },
    {
      key: 'projectUserGroup',
      title: t('project.permission.userGroup'),
      level: 2,
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_USER_GROUP,
    },
  ]);
  const menuList = computed(() => {
    const routerList = router.getRoutes();
    return sourceMenuList.value.filter((item) => {
      if (item.name) {
        const routerItem = routerList.find((rou) => rou.name === item.name);
        if (!routerItem) return false;
        if (routerItem.name === ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_VERSION) {
          return licenseStore.hasLicense() && permission.accessRouter(routerItem);
        }
        return permission.accessRouter(routerItem);
      }
      return true;
    });
  });

  const currentKey = ref<string>('');

  const toggleMenu = (itemName: string) => {
    if (itemName) {
      currentKey.value = itemName;
      router.push({ name: itemName });
    }
  };

  const isLoading = ref(false);

  const reload = (flag: boolean) => {
    isLoading.value = flag;
  };
  provide('reload', reload);

  const setInitRoute = () => {
    if (route?.name) currentKey.value = route.name as string;
  };

  onBeforeMount(() => {
    setInitRoute();
    licenseStore.getValidateLicense();
  });
</script>

<style scoped lang="less">
  .left-menu-wrapper {
    border-radius: 12px;
    color: var(--color-text-1);
    box-shadow: 0 0 10px rgb(120 56 135/ 5%);
    .left-content {
      width: 100%;
      .menu {
        .menu-item {
          height: 38px;
          line-height: 38px;
        }
      }
    }
  }
  .right-menu-wrapper {
    border-radius: 12px;
    background-color: var(--color-text-fff);
    box-shadow: 0 0 10px rgb(120 56 135/ 5%);
  }
  .is-active {
    border-radius: 4px;
    color: rgb(var(--primary-5));
    background-color: rgb(var(--primary-1));
  }
  .rightTest {
    height: 100%;
  }
</style>
