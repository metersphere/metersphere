<template>
  <div class="navbar">
    <div class="left-side">
      <a-space>
        <div class="one-line-text flex max-w-[145px] items-center">
          <img :src="props.logo" class="mr-[4px] h-[32px] w-[32px]" />
          {{ props.name }}
        </div>
      </a-space>
    </div>
    <div v-if="!props.isPreview" class="center-side">
      <template v-if="showProjectSelect">
        <a-divider direction="vertical" class="ml-0" />
        <a-select
          class="w-auto min-w-[150px] max-w-[200px] focus-within:!bg-[var(--color-text-n8)] hover:!bg-[var(--color-text-n8)]"
          :default-value="appStore.getCurrentProjectId"
          :bordered="false"
          @change="selectProject"
        >
          <template #arrow-icon>
            <icon-caret-down />
          </template>
          <a-tooltip v-for="project of projectList" :key="project.id" :mouse-enter-delay="500" :content="project.name">
            <a-option
              :value="project.id"
              :class="project.id === appStore.getCurrentProjectId ? 'arco-select-option-selected' : ''"
            >
              {{ project.name }}
            </a-option>
          </a-tooltip>
        </a-select>
        <a-divider direction="vertical" class="mr-0" />
      </template>
      <TopMenu />
    </div>
    <ul v-if="!props.isPreview" class="right-side">
      <li>
        <a-tooltip :content="t('settings.navbar.search')">
          <a-button type="secondary">
            <template #icon>
              <icon-search />
            </template>
          </a-button>
        </a-tooltip>
      </li>
      <li>
        <a-tooltip :content="t('settings.navbar.alerts')">
          <div class="message-box-trigger">
            <a-badge :count="9" dot>
              <a-button type="secondary" @click="setPopoverVisible">
                <template #icon>
                  <icon-notification />
                </template>
              </a-button>
            </a-badge>
          </div>
        </a-tooltip>
        <a-popover
          trigger="click"
          :arrow-style="{ display: 'none' }"
          :content-style="{ padding: 0, minWidth: '400px' }"
          content-class="message-popover"
        >
          <div ref="refBtn" class="ref-btn"></div>
          <template #content>
            <message-box />
          </template>
        </a-popover>
      </li>
      <li>
        <a-tooltip :content="t('settings.navbar.task')">
          <a-button type="secondary">
            <template #icon>
              <icon-calendar-clock />
            </template>
          </a-button>
        </a-tooltip>
      </li>
      <li>
        <a-dropdown trigger="click" position="br">
          <a-tooltip :content="t('settings.navbar.help')">
            <a-button type="secondary">
              <template #icon>
                <icon-question-circle />
              </template>
            </a-button>
          </a-tooltip>
          <template #content>
            <a-doption v-for="item in helpCenterList" :key="item.name" :value="item.name">
              <component :is="item.icon"></component>
              {{ t(item.name) }}
            </a-doption>
          </template>
        </a-dropdown>
      </li>
      <li>
        <a-dropdown trigger="click" position="br" @select="changeLocale as any">
          <a-tooltip :content="t('settings.language')">
            <a-button type="secondary">
              <template #icon>
                <icon-translate />
              </template>
            </a-button>
          </a-tooltip>
          <template #content>
            <a-doption v-for="item in locales" :key="item.value" :value="item.value">
              <template #icon>
                <icon-check v-show="item.value === currentLocale" />
              </template>
              {{ item.label }}
            </a-doption>
          </template>
        </a-dropdown>
      </li>
      <!-- <li>
        <a-tooltip :content="isFullscreen ? t('settings.navbar.screen.toExit') : t('settings.navbar.screen.toFull')">
          <a-button class="nav-btn" type="outline" :shape="'circle'" @click="toggleFullScreen">
            <template #icon>
              <icon-fullscreen-exit v-if="isFullscreen" />
              <icon-fullscreen v-else />
            </template>
          </a-button>
        </a-tooltip>
      </li> -->
      <!-- <li>
        <a-tooltip :content="t('settings.title')">
          <a-button class="nav-btn" type="outline" :shape="'circle'" @click="setVisible">
            <template #icon>
              <icon-settings />
            </template>
          </a-button>
        </a-tooltip>
      </li> -->
      <!-- <li>
        <a-dropdown trigger="click">
          <a-avatar :size="32" :style="{ marginRight: '8px', cursor: 'pointer' }">
            <img alt="avatar" :src="avatar" />
          </a-avatar>
          <template #content>
            <a-doption>
              <a-space @click="switchRoles">
                <icon-tag />
                <span>
                  {{ t('messageBox.switchRoles') }}
                </span>
              </a-space>
            </a-doption>
            <a-doption>
              <a-space @click="$router.push({ name: 'Info' })">
                <icon-user />
                <span>
                  {{ t('messageBox.userCenter') }}
                </span>
              </a-space>
            </a-doption>
            <a-doption>
              <a-space @click="$router.push({ name: 'Setting' })">
                <icon-settings />
                <span>
                  {{ t('messageBox.userSettings') }}
                </span>
              </a-space>
            </a-doption>
            <a-doption>
              <a-space @click="handleLogout">
                <icon-export />
                <span>
                  {{ t('messageBox.logout') }}
                </span>
              </a-space>
            </a-doption>
          </template>
        </a-dropdown>
      </li> -->
    </ul>
  </div>
</template>

<script lang="ts" setup>
  import { ref, computed, Ref, onBeforeMount } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { IconCompass, IconQuestionCircle, IconFile, IconInfoCircle } from '@arco-design/web-vue/es/icon';
  // import { Message } from '@arco-design/web-vue';
  // import { useFullscreen } from '@vueuse/core';
  import { useAppStore } from '@/store';
  import { LOCALE_OPTIONS } from '@/locale';
  import useLocale from '@/locale/useLocale';
  // import useUser from '@/hooks/useUser';
  import TopMenu from '@/components/business/ms-top-menu/index.vue';
  import MessageBox from '../message-box/index.vue';
  import { getProjectList } from '@/api/modules/project-management/project';
  import { useI18n } from '@/hooks/useI18n';
  import usePathMap from '@/hooks/usePathMap';
  import { MENU_LEVEL, type PathMapRoute } from '@/config/pathMap';

  import type { ProjectListItem } from '@/models/setting/project';

  const props = defineProps<{
    isPreview?: boolean;
    logo?: string;
    name?: string;
  }>();

  const appStore = useAppStore();
  // const { logout } = useUser();
  const route = useRoute();
  const router = useRouter();
  const { t } = useI18n();

  const projectList: Ref<ProjectListItem[]> = ref([]);

  onBeforeMount(async () => {
    try {
      const res = await getProjectList(appStore.getCurrentOrgId);
      projectList.value = res;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  });

  const showProjectSelect = computed(() => {
    const { getRouteLevelByKey } = usePathMap();
    // 非项目级别页面不需要展示项目选择器
    return getRouteLevelByKey(route.name as PathMapRoute) === MENU_LEVEL[2];
  });

  function selectProject(
    value: string | number | boolean | Record<string, any> | (string | number | boolean | Record<string, any>)[]
  ) {
    appStore.setCurrentProjectId(value as string);
    router.replace({
      path: route.path,
      query: {
        organizationId: appStore.currentOrgId,
        projectId: appStore.currentProjectId,
      },
    });
  }

  const helpCenterList = [
    {
      name: 'settings.help.guide',
      icon: IconCompass,
      route: '/help-center/guide',
    },
    {
      name: 'settings.help.doc',
      icon: IconQuestionCircle,
      route: '/help-center/guide',
    },
    {
      name: 'settings.help.APIDoc',
      icon: IconFile,
      route: '/help-center/guide',
    },
    {
      name: 'settings.help.version',
      icon: IconInfoCircle,
      route: '/help-center/guide',
    },
  ];

  const { changeLocale, currentLocale } = useLocale();
  // const { isFullscreen, toggle: toggleFullScreen } = useFullscreen();
  const locales = [...LOCALE_OPTIONS];
  // const avatar = computed(() => {
  //   return userStore.avatar;
  // });
  // const setVisible = () => {
  //   appStore.updateSettings({ globalSettings: true });
  // };
  const refBtn = ref();
  const setPopoverVisible = () => {
    const event = new MouseEvent('click', {
      view: window,
      bubbles: true,
      cancelable: true,
    });
    refBtn.value.dispatchEvent(event);
  };
  // const handleLogout = () => {
  //   logout();
  // };
  // const switchRoles = async () => {
  //   const res = await userStore.switchRoles();
  //   Message.success(res as string);
  // };
</script>

<style scoped lang="less">
  .navbar {
    @apply flex h-full justify-between bg-transparent;
  }
  .left-side {
    @apply flex items-center;

    padding-left: 24px;
    width: 185px;
  }
  .center-side {
    @apply flex flex-1 items-center;
  }
  .right-side {
    @apply flex list-none;

    padding-right: 20px;
    :deep(.locale-select) {
      border-radius: 20px;
    }
    li {
      @apply flex items-center;

      padding-left: 10px;
      .arco-btn-secondary {
        @apply !bg-transparent;

        color: var(--color-text-4) !important;
        &:hover,
        &:focus-visible {
          color: var(--color-text-1) !important;
        }
      }
    }
    a {
      @apply no-underline;

      color: var(--color-text-1);
    }
    .nav-btn {
      font-size: 16px;
      border-color: rgb(var(--gray-2));
      color: rgb(var(--gray-8));
    }
    .trigger-btn,
    .ref-btn {
      @apply absolute;

      bottom: 14px;
    }
    .trigger-btn {
      margin-left: 14px;
    }
  }
</style>

<style lang="less">
  .message-popover {
    .arco-popover-content {
      @apply mt-0;
    }
  }
  .arco-menu-horizontal {
    .arco-menu-inner {
      .arco-menu-item,
      .arco-menu-overflow-sub-menu {
        @apply !bg-transparent;
      }
      .arco-menu-selected {
        @apply !font-normal;

        color: rgb(var(--primary-5)) !important;
        .arco-menu-selected-label {
          bottom: -11px;
          background-color: rgb(var(--primary-5)) !important;
        }
      }
    }
  }
  .arco-trigger-menu-vertical {
    max-height: 500px;
    .arco-trigger-menu-selected {
      @apply !font-normal;

      color: rgb(var(--primary-5)) !important;
    }
  }
</style>
@/models/setting/project @/api/modules/setting/project @/api/modules/project-management/project
