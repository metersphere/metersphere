<template>
  <div class="wrapper flex min-h-[500px]" :style="{ height: 'calc(100vh - 90px)' }">
    <div class="left-menu-wrapper mr-[16px] w-[208px] min-w-[208px] bg-white p-[24px]">
      <div class="left-content">
        <div class="mb-2 font-medium">{{ t('project.permission.projectAndPermission') }}</div>
        <div class="menu">
          <div
            v-for="(item, index) of menuList"
            :key="item.key"
            class="menu-item px-2"
            :class="{
              'text-[--color-text-4]': item.level === 1,
              'is-active': item.name === currentKey && item.level !== 1,
              'cursor-pointer': item.level !== 1,
            }"
            :style="{
              'border-top': item.level === 1 && index !== 0 ? '1px solid var(--color-border-2)' : 'none',
            }"
          >
            <div @click="toggleMenu(item.name)">{{ t(item.title) }}</div>
          </div>
        </div>
      </div>
    </div>
    <MsCard simple :other-width="290" :min-width="700" :loading="isLoading">
      <router-view></router-view>
    </MsCard>
  </div>
</template>

<script setup lang="ts">
  /**
   * @description 项目管理-项目与权限
   */
  import { ref, onBeforeMount, provide } from 'vue';
  import { ProjectManagementRouteEnum } from '@/enums/routeEnum';
  import { useI18n } from '@/hooks/useI18n';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import { useRouter, useRoute } from 'vue-router';

  const { t } = useI18n();

  const router = useRouter();
  const route = useRoute();
  const menuList = ref([
    {
      key: 'project',
      title: 'project.permission.project',
      level: 1,
      name: '',
    },
    {
      key: 'projectBasicInfo',
      title: 'project.permission.basicInfo',
      level: 2,
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_BASIC_INFO,
    },
    {
      key: 'projectMenuManage',
      title: 'project.permission.menuManagement',
      level: 2,
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT,
    },
    {
      key: 'projectVersion',
      title: 'project.permission.projectVersion',
      level: 2,
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_VERSION,
    },
    {
      key: 'memberPermission',
      title: 'project.permission.memberPermission',
      level: 1,
      name: '',
    },
    {
      key: 'projectMember',
      title: 'project.permission.member',
      level: 2,
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_MEMBER,
    },
    {
      key: 'projectUserGroup',
      title: 'project.permission.userGroup',
      level: 2,
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_USER_GROUP,
    },
  ]);

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
          font-family: 'PingFang SC';
        }
      }
    }
  }
  .right-menu-wrapper {
    border-radius: 12px;
    box-shadow: 0 0 10px rgb(120 56 135/ 5%);
    @apply bg-white;
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
