<template>
  <div class="navbar">
    <div class="flex w-[200px] items-center px-[16px]">
      <a-space>
        <div class="one-line-text flex max-w-[145px] items-center">
          <img :src="props.logo" class="mr-[4px] h-[34px] w-[32px]" />
          <a-tooltip :content="props.name">
            <div class="one-line-text font-['Helvetica_Neue'] text-[16px] font-bold text-[rgb(var(--primary-5))]">
              {{ props.name }}
            </div>
          </a-tooltip>
        </div>
      </a-space>
    </div>
    <div v-if="!props.isPreview" class="center-side">
      <template v-if="showProjectSelect">
        <a-select
          v-model:model-value="appStore.currentProjectId"
          class="mr-[8px] w-[200px] focus-within:!bg-[var(--color-text-n8)] hover:!bg-[var(--color-text-n8)]"
          :bordered="false"
          :fallback-option="false"
          allow-search
          @change="selectProject"
        >
          <template #arrow-icon>
            <icon-caret-down />
          </template>
          <template v-if="hasAnyPermission(['ORGANIZATION_PROJECT:READ+ADD'])" #header>
            <a-button
              class="select-header-button mb-[4px] h-[28px] w-full justify-start pl-[7px] pr-0"
              type="text"
              @click="projectVisible = true"
            >
              <template #icon>
                <MsIcon type="icon-icon_add_outlined" />
              </template>
              {{ t('settings.navbar.createProject') }}
            </a-button>
          </template>
          <a-tooltip
            v-for="project of appStore.projectList"
            :key="project.id"
            :mouse-enter-delay="500"
            :content="project.name"
          >
            <a-option
              :value="project.id"
              :class="project.id === appStore.currentProjectId ? 'arco-select-option-selected' : ''"
            >
              {{ project.name }}
            </a-option>
          </a-tooltip>
        </a-select>
      </template>
      <TopMenu />
    </div>
    <ul v-if="!props.isPreview && !props.hideRight" class="right-side">
      <li>
        <a-tooltip :content="t('settings.navbar.ai')" position="br">
          <a-button v-if="aiStore.aiSourceNameList.length > 0" type="secondary" @click="openAI">
            <template #icon>
              <svg-icon width="18px" height="18px" name="ai" />
            </template>
          </a-button>
        </a-tooltip>
      </li>
      <!-- <li>
        <a-tooltip :content="t('settings.navbar.search')">
          <a-button type="secondary">
            <template #icon>
              <icon-search />
            </template>
          </a-button>
        </a-tooltip>
      </li> -->
      <li>
        <a-tooltip :content="t('settings.navbar.alerts')">
          <div class="message-box-trigger">
            <a-badge v-if="unReadCount > 0" :count="9" dot>
              <a-button type="secondary" @click="goMessageCenter">
                <template #icon>
                  <icon-notification />
                </template>
              </a-button>
            </a-badge>
            <a-button v-else type="secondary" @click="goMessageCenter">
              <template #icon>
                <icon-notification />
              </template>
            </a-button>
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
            <MessageBox />
          </template>
        </a-popover>
      </li>
      <li v-if="hasAnyPermission(['PROJECT_CASE_TASK_CENTER:READ', 'PROJECT_SCHEDULE_TASK_CENTER:READ'])">
        <a-tooltip :content="t('settings.navbar.task')">
          <a-button type="secondary" @click="goTaskCenter">
            <template #icon>
              <icon-calendar-clock />
            </template>
          </a-button>
        </a-tooltip>
      </li>
      <li>
        <a-dropdown trigger="click" position="br" @select="handleHelpSelect">
          <a-tooltip :content="t('settings.navbar.help')">
            <a-button type="secondary">
              <template #icon>
                <icon-question-circle />
              </template>
            </a-button>
          </a-tooltip>
          <template #content>
            <a-doption v-if="appStore.pageConfig.helpDoc" value="doc">
              <MsIcon type="icon-icon-maybe_outlined" />
              {{ t('settings.help.doc') }}
            </a-doption>
            <a-popover position="left">
              <a-doption value="version">
                <component :is="IconInfoCircle"></component>
                {{ t('settings.help.versionInfo') }}
              </a-doption>
              <template #content>
                <div
                  class="flex cursor-pointer items-center gap-[4px] text-[14px] text-[var(--color-text-1)]"
                  @click="copyVersion"
                >
                  <div class="text-[var(--color-text-4)]">{{ t('settings.help.version') }}：</div>
                  {{ appStore.version }}
                </div>
              </template>
            </a-popover>
            <a-doption value="forumHelp">
              <MsIcon type="icon-icon_forum" />
              {{ t('settings.help.forumHelp') }}
            </a-doption>
            <a-doption value="github">
              <MsIcon type="icon-icon_github" />
              {{ t('settings.help.gitHubProject') }}
            </a-doption>
            <a-doption value="enterprise">
              <MsIcon type="icon-icon_vip" />
              {{ t('settings.help.enterpriseVersionTrial') }}
            </a-doption>
          </template>
        </a-dropdown>
      </li>
      <li>
        <a-tooltip :content="t('settings.themeColor')" position="br">
          <a-switch v-model:model-value="isSun" size="small" @change="handleSunChange">
            <template #checked-icon>
              <icon-sun-fill />
            </template>
            <template #unchecked-icon>
              <icon-moon-fill />
            </template>
          </a-switch>
        </a-tooltip>
      </li>
      <li>
        <a-dropdown trigger="click" position="br" @select="changeLanguage as any">
          <a-tooltip :content="t('settings.language')" position="br">
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
    </ul>
  </div>
  <MsAIDrawer v-if="isInitAiDrawer" v-model:visible="aiDrawerVisible" type="chat" />
  <TaskCenterDrawer v-if="taskCenterVisible" v-model:visible="taskCenterVisible" />
  <MessageCenterDrawer v-if="messageCenterVisible" v-model:visible="messageCenterVisible" />
  <AddProjectModal :visible="projectVisible" @cancel="projectVisible = false" />
</template>

<script lang="ts" setup>
  import { computed, ref, watch } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { useClipboard } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';

  import MessageBox from '@/components/pure/message-box/index.vue';
  import TopMenu from '@/components/business/ms-top-menu/index.vue';
  import AddProjectModal from '@/views/setting/organization/project/components/addProjectModal.vue';

  import { getMessageUnReadCount } from '@/api/modules/message';
  import { switchProject } from '@/api/modules/project-management/project';
  import { updateLanguage } from '@/api/modules/user';
  import { MENU_LEVEL, type PathMapRoute } from '@/config/pathMap';
  import { useI18n } from '@/hooks/useI18n';
  import usePathMap from '@/hooks/usePathMap';
  import { LOCALE_OPTIONS } from '@/locale';
  import useLocale from '@/locale/useLocale';
  import useAppStore from '@/store/modules/app';
  import useGlobalStore from '@/store/modules/global';
  import useAIStore from '@/store/modules/setting/ai';
  import useUserStore from '@/store/modules/user';
  import { getFirstRouteNameByPermission, hasAnyPermission } from '@/utils/permission';
  import { setDarkTheme, watchStyle, watchTheme } from '@/utils/theme';

  import { GlobalEventNameEnum } from '@/enums/commonEnum';

  import { IconInfoCircle } from '@arco-design/web-vue/es/icon';
  import type { LocaleType } from '#/global';

  const TaskCenterDrawer = defineAsyncComponent(() => import('@/components/business/ms-task-center-drawer/index.vue'));
  const MessageCenterDrawer = defineAsyncComponent(
    () => import('@/components/business/ms-message/MessageCenterDrawer.vue')
  );
  const MsAIDrawer = defineAsyncComponent(() => import('@/components/business/ms-ai-drawer/index.vue'));

  const props = defineProps<{
    isPreview?: boolean;
    logo?: string;
    name?: string;
    hideRight?: boolean;
  }>();

  const appStore = useAppStore();
  const userStore = useUserStore();
  const globalStore = useGlobalStore();
  const aiStore = useAIStore();
  const route = useRoute();
  const router = useRouter();
  const { t } = useI18n();
  const unReadCount = ref<number>(0);

  async function checkMessageRead() {
    if (appStore.currentProjectId && appStore.currentProjectId !== 'no_such_project') {
      unReadCount.value = await getMessageUnReadCount(appStore.currentProjectId);
    }
  }
  watch(
    () => appStore.currentOrgId,
    async () => {
      appStore.initProjectList();
    },
    {
      immediate: true,
    }
  );

  watch(
    () => appStore.getCurrentTopMenu?.name,
    () => {
      checkMessageRead();
    },
    {
      immediate: true,
    }
  );

  const projectVisible = ref(false);
  const showProjectSelect = computed(() => {
    const { getRouteLevelByKey } = usePathMap();
    // 非项目级别页面不需要展示项目选择器
    const level = getRouteLevelByKey(route.name as PathMapRoute);
    return level === MENU_LEVEL[2] || level === null;
  });

  async function selectProject(
    value: string | number | boolean | Record<string, any> | (string | number | boolean | Record<string, any>)[]
  ) {
    try {
      appStore.showLoading();
      await switchProject({
        projectId: value as string,
        userId: userStore.id || '',
      });
      await userStore.checkIsLogin(true);
      router.replace({
        name: getFirstRouteNameByPermission(router.getRoutes()),
        query: {
          orgId: appStore.currentOrgId,
          pId: value as string,
        },
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      appStore.hideLoading();
    }
  }

  const { copy, isSupported } = useClipboard({ legacy: true });
  function copyVersion() {
    if (isSupported) {
      copy(appStore.version);
      Message.success(t('common.copySuccess'));
    } else {
      Message.warning(t('common.copyNotSupport'));
    }
  }

  const { changeLocale, currentLocale } = useLocale();
  const locales = [...LOCALE_OPTIONS];

  const refBtn = ref();
  const messageCenterVisible = ref<boolean>(false);

  const taskCenterVisible = ref<boolean>(false);
  function goTaskCenter() {
    taskCenterVisible.value = true;
  }
  watch(
    () => globalStore.getGlobalEvent,
    (event) => {
      if (event && event.id && event.name === GlobalEventNameEnum.OPEN_TASK_CENTER) {
        goTaskCenter();
      }
    }
  );

  function goMessageCenter() {
    messageCenterVisible.value = true;
  }

  const isInitAiDrawer = ref<boolean>(false);
  const aiDrawerVisible = ref<boolean>(false);
  function openAI() {
    isInitAiDrawer.value = true;
    aiDrawerVisible.value = true;
  }

  const isSun = ref(!appStore.isDarkTheme);

  watch(
    () => appStore.isDarkTheme,
    (val) => {
      if (val) {
        // 暗黑模式
        setDarkTheme();
      } else {
        // 初始化平台风格和主题色
        watchStyle(appStore.pageConfig.style, appStore.pageConfig);
        watchTheme(appStore.pageConfig.theme, appStore.pageConfig);
      }
    },
    {
      immediate: true,
    }
  );

  function handleSunChange(val: string | number | boolean) {
    appStore.setDarkTheme(!val);
  }

  function changeLanguage(locale: LocaleType) {
    // 修改当前用户的语言
    updateLanguage({ language: locale });
    changeLocale(locale);
  }
  function handleHelpSelect(val: string | number | Record<string, any> | undefined) {
    switch (val) {
      case 'doc':
        window.open(appStore.pageConfig.helpDoc, '_blank');
        break;
      case 'forumHelp':
        window.open('https://bbs.fit2cloud.com/c/ms/8', '_blank');
        break;
      case 'github':
        window.open('https://github.com/metersphere/metersphere', '_blank');
        break;
      case 'enterprise':
        window.open('https://jinshuju.net/f/CzzAOe', '_blank');
        break;
      default:
        break;
    }
  }

  onMounted(() => {
    if (route.query.task) {
      goTaskCenter();
    }
  });
</script>

<style scoped lang="less">
  .navbar {
    @apply flex h-full justify-between bg-transparent;
  }
  .select-header-button.arco-btn-text:not(:disabled):hover {
    background-color: rgb(var(--primary-1)) !important;
  }
  .center-side {
    @apply flex flex-1 items-center;
  }
  .right-side {
    @apply flex list-none;

    padding-right: 16px;
    gap: 8px;
    :deep(.locale-select) {
      border-radius: 20px;
    }
    li {
      @apply flex items-center;
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
      line-height: 24px;
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
  .arco-trigger-menu-vertical {
    max-height: 500px;
    .arco-trigger-menu-selected {
      @apply !font-normal;

      color: rgb(var(--primary-5)) !important;
    }
  }
</style>
