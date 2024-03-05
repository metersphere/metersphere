<template>
  <div class="navbar">
    <div class="left-side">
      <a-space>
        <div class="one-line-text flex max-w-[145px] items-center">
          <img :src="props.logo" class="mr-[4px] h-[34px] w-[32px]" />
          <div class="font-['Helvetica_Neue'] text-[16px] font-bold text-[rgb(var(--primary-5))]">{{ props.name }}</div>
        </div>
      </a-space>
    </div>
    <div v-if="!props.isPreview" class="center-side">
      <template v-if="showProjectSelect">
        <a-divider direction="vertical" class="ml-0" />
        <a-select
          v-model:model-value="appStore.currentProjectId"
          class="w-[200px] focus-within:!bg-[var(--color-text-n8)] hover:!bg-[var(--color-text-n8)]"
          :bordered="false"
          allow-search
          @change="selectProject"
        >
          <template #arrow-icon>
            <icon-caret-down />
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
        <a-divider direction="vertical" class="mr-0" />
      </template>
      <TopMenu />
    </div>
    <ul v-if="!props.isPreview && !props.hideRight" class="right-side">
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
            <a-badge :count="9" dot>
              <a-button type="secondary" @click="goMessageCenter">
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
            <MessageBox />
          </template>
        </a-popover>
      </li>
      <li>
        <a-tooltip :content="t('settings.navbar.task')">
          <a-button type="secondary" @click="goTaskCenter">
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
            <a-doption value="doc">
              <component :is="IconQuestionCircle"></component>
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
    </ul>
  </div>
  <TaskCenterModal v-model:visible="taskCenterVisible" />
  <MessageCenterDrawer v-model:visible="messageCenterVisible" />
</template>

<script lang="ts" setup>
  import { computed, ref, watch } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { useClipboard } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';

  import MessageBox from '@/components/pure/message-box/index.vue';
  import MessageCenterDrawer from '@/components/business/ms-message/MessageCenterDrawer.vue';
  import TopMenu from '@/components/business/ms-top-menu/index.vue';
  import TaskCenterModal from './taskCenterModal.vue';

  import { switchProject } from '@/api/modules/project-management/project';
  import { MENU_LEVEL, type PathMapRoute } from '@/config/pathMap';
  import { useI18n } from '@/hooks/useI18n';
  import usePathMap from '@/hooks/usePathMap';
  import { LOCALE_OPTIONS } from '@/locale';
  import useLocale from '@/locale/useLocale';
  import useAppStore from '@/store/modules/app';
  import useLicenseStore from '@/store/modules/setting/license';
  import useUserStore from '@/store/modules/user';

  import { IconInfoCircle, IconQuestionCircle } from '@arco-design/web-vue/es/icon';

  const licenseStore = useLicenseStore();

  const props = defineProps<{
    isPreview?: boolean;
    logo?: string;
    name?: string;
    hideRight?: boolean;
  }>();

  const appStore = useAppStore();
  const userStore = useUserStore();
  const route = useRoute();
  const router = useRouter();
  const { t } = useI18n();

  watch(
    () => appStore.currentOrgId,
    async () => {
      appStore.initProjectList();
    },
    {
      immediate: true,
    }
  );

  const showProjectSelect = computed(() => {
    const { getRouteLevelByKey } = usePathMap();
    // 非项目级别页面不需要展示项目选择器
    return getRouteLevelByKey(route.name as PathMapRoute) === MENU_LEVEL[2];
  });

  async function selectProject(
    value: string | number | boolean | Record<string, any> | (string | number | boolean | Record<string, any>)[]
  ) {
    appStore.setCurrentProjectId(value as string);
    try {
      appStore.showLoading();
      await switchProject({
        projectId: value as string,
        userId: userStore.id || '',
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      appStore.hideLoading();
      router.replace({
        path: route.path,
        query: {
          ...route.query,
          organizationId: appStore.currentOrgId,
          projectId: appStore.currentProjectId,
        },
      });
    }
  }

  const { copy, isSupported } = useClipboard();
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
  function goMessageCenter() {
    messageCenterVisible.value = true;
  }
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
