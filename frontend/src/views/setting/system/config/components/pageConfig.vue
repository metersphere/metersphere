<template>
  <div class="relative">
    <!-- 风格、主题色配置 -->
    <MsCard class="mb-[16px]" :loading="pageLoading" simple auto-height>
      <div class="config-title">
        {{ t('system.config.page.theme') }}
        <a-tooltip :content="t('system.config.page.themeTip')" position="tl" class="themeTip">
          <icon-question-circle class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-6))]" />
        </a-tooltip>
      </div>
      <a-radio-group v-model:model-value="pageConfig.theme" type="button" class="mb-[4px]">
        <a-radio v-for="item of themeList" :key="item.value" :value="item.value">
          {{ t(item.label) }}
        </a-radio>
      </a-radio-group>
      <div v-if="pageConfig.theme === 'custom'" class="ml-[4px]">
        <MsColorSelect key="customTheme" v-model:pure-color="pageConfig.customTheme" />
      </div>
      <div class="config-title mt-[16px]">
        {{ t('system.config.page.style') }}
        <a-tooltip :content="t('system.config.page.styleTip')" position="tl" mini>
          <icon-question-circle class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-6))]" />
        </a-tooltip>
      </div>
      <a-radio-group v-model:model-value="pageConfig.style" type="button" class="mb-[4px]">
        <a-radio v-for="item of styleList" :key="item.value" :value="item.value">
          {{ t(item.label) }}
        </a-radio>
      </a-radio-group>
      <div v-if="pageConfig.style === 'custom'" class="mb-[4px] ml-[4px]">
        <MsColorSelect key="customStyle" v-model:pure-color="pageConfig.customStyle" />
      </div>
    </MsCard>
    <!-- 登录页配置 -->
    <MsCard class="mb-[16px]" :loading="pageLoading" simple auto-height>
      <div class="config-title">
        {{ t('system.config.page.loginPageConfig') }}
      </div>
      <div class="config-content">
        <div class="config-title !mb-[8px] flex items-center justify-between">
          {{ t('system.config.page.pagePreview') }}
          <MsButton
            v-permission="['SYSTEM_PARAMETER_SETTING_DISPLAY:READ+UPDATE']"
            class="!leading-none"
            @click="resetLoginPageConfig"
            >{{ t('system.config.page.reset') }}</MsButton
          >
        </div>
        <!-- 登录页预览盒子 -->
        <div :class="['config-preview', currentLocale === 'en-US' ? 'config-preview--en' : '']">
          <div ref="loginPageFullRef" class="login-preview">
            <div :class="['config-preview-head', isLoginPageFullscreen ? 'full-preview-head' : '']">
              <div class="flex items-center justify-between overflow-hidden">
                <img v-if="pageConfig.icon[0]?.url" :src="pageConfig.icon[0].url" class="h-[18px] w-[18px]" />
                <svg-icon v-else name="logo" class="h-[18px] w-[18px]"></svg-icon>
                <div class="one-line-text ml-[4px] text-[10px]">{{ pageConfig.title }}</div>
              </div>
              <div
                class="w-[96px] cursor-pointer text-right !text-[var(--color-text-4)]"
                @click="loginFullscreenToggle"
              >
                <MsIcon v-if="isLoginPageFullscreen" type="icon-icon_off_screen" />
                <MsIcon v-else type="icon-icon_full_screen_one" />
              </div>
            </div>
            <!-- 登录页预览实际渲染 DOM，按三种屏幕尺寸缩放 -->
            <div :class="['page-preview', isLoginPageFullscreen ? 'full-preview' : 'normal-preview']">
              <banner :banner="pageConfig.loginImage[0]?.url || defaultLoginImage" is-preview />
              <loginForm
                :slogan="pageConfig.slogan"
                :logo="pageConfig.loginLogo[0]?.url || defaultLoginLogo"
                is-preview
              />
            </div>
          </div>
          <div class="config-form">
            <div class="config-form-card">
              <div class="mb-[8px] flex items-center justify-between">
                <div class="flex items-center">
                  {{ t('system.config.page.icon') }}
                  <a-tag
                    v-show="pageConfig.icon[0]?.file"
                    type="warn"
                    color="rgb(var(--warning-2))"
                    class="ml-[4px] !text-[rgb(var(--warning-6))]"
                    size="small"
                  >
                    {{ t('system.config.page.unsave') }}
                  </a-tag>
                </div>
                <MsUpload
                  v-model:file-list="pageConfig.icon"
                  accept="image"
                  :max-size="200"
                  size-unit="KB"
                  :auto-upload="false"
                >
                  <a-button
                    v-permission="['SYSTEM_PARAMETER_SETTING_DISPLAY:READ+UPDATE']"
                    type="outline"
                    class="arco-btn-outline--secondary"
                    size="mini"
                  >
                    {{ t('system.config.page.replace') }}
                  </a-button>
                </MsUpload>
              </div>
              <p class="text-[12px] leading-[16px] text-[var(--color-text-4)]">{{ t('system.config.page.iconTip') }}</p>
            </div>
            <div class="config-form-card">
              <div class="mb-[8px] flex items-center justify-between">
                <div class="flex items-center">
                  {{ t('system.config.page.loginLogo') }}
                  <a-tag
                    v-show="pageConfig.loginLogo[0]?.file"
                    type="warn"
                    color="rgb(var(--warning-2))"
                    class="ml-[4px] !text-[rgb(var(--warning-6))]"
                    size="small"
                  >
                    {{ t('system.config.page.unsave') }}
                  </a-tag>
                </div>
                <MsUpload
                  v-model:file-list="pageConfig.loginLogo"
                  accept="image"
                  :max-size="200"
                  size-unit="KB"
                  :auto-upload="false"
                >
                  <a-button
                    v-permission="['SYSTEM_PARAMETER_SETTING_DISPLAY:READ+UPDATE']"
                    type="outline"
                    class="arco-btn-outline--secondary"
                    size="mini"
                  >
                    {{ t('system.config.page.replace') }}
                  </a-button>
                </MsUpload>
              </div>
              <p class="text-[12px] leading-[16px] text-[var(--color-text-4)]">
                {{ t('system.config.page.loginLogoTip') }}
              </p>
            </div>
            <div class="config-form-card">
              <div class="mb-[8px] flex items-center justify-between">
                <div class="flex items-center">
                  {{ t('system.config.page.loginBg') }}
                  <a-tag
                    v-show="pageConfig.loginImage[0]?.file"
                    type="warn"
                    color="rgb(var(--warning-2))"
                    class="ml-[4px] !text-[rgb(var(--warning-6))]"
                    size="small"
                  >
                    {{ t('system.config.page.unsave') }}
                  </a-tag>
                </div>
                <MsUpload
                  v-model:file-list="pageConfig.loginImage"
                  accept="image"
                  :max-size="1"
                  size-unit="MB"
                  :auto-upload="false"
                >
                  <a-button
                    v-permission="['SYSTEM_PARAMETER_SETTING_DISPLAY:READ+UPDATE']"
                    type="outline"
                    class="arco-btn-outline--secondary"
                    size="mini"
                  >
                    {{ t('system.config.page.replace') }}
                  </a-button>
                </MsUpload>
              </div>
              <p class="text-[12px] leading-[16px] text-[var(--color-text-4)]">
                {{ t('system.config.page.loginBgTip') }}
              </p>
            </div>
            <a-form
              ref="loginConfigFormRef"
              :model="pageConfig"
              layout="vertical"
              :rules="{ slogan: [{ required: true, message: t('system.config.page.sloganRequired') }] }"
            >
              <a-form-item
                :label="t('system.config.page.slogan')"
                field="slogan"
                asterisk-position="end"
                class="mb-[12px]"
                required
              >
                <a-input
                  v-model:model-value="pageConfig.slogan"
                  :placeholder="t('system.config.page.sloganPlaceholder')"
                  :max-length="255"
                  :disabled="!hasAnyPermission(['SYSTEM_PARAMETER_SETTING_DISPLAY:READ+UPDATE'])"
                ></a-input>
                <MsFormItemSub :text="t('system.config.page.sloganTip')" :show-fill-icon="false" />
              </a-form-item>
              <a-form-item :label="t('system.config.page.title')" field="title">
                <a-input
                  v-model:model-value="pageConfig.title"
                  :placeholder="t('system.config.page.titlePlaceholder')"
                  :max-length="255"
                  :disabled="!hasAnyPermission(['SYSTEM_PARAMETER_SETTING_DISPLAY:READ+UPDATE'])"
                ></a-input>
                <MsFormItemSub :text="t('system.config.page.titleTip')" :show-fill-icon="false" />
              </a-form-item>
            </a-form>
          </div>
        </div>
        <div class="mt-[8px] text-[var(--color-text-4)]">{{ t('system.config.page.loginPreviewTip') }}</div>
      </div>
    </MsCard>
    <!-- 平台主页面配置 -->
    <MsCard class="mb-[96px]" :loading="pageLoading" simple auto-height>
      <div class="config-title">
        {{ t('system.config.page.platformConfig') }}
      </div>
      <div class="config-content border border-solid border-[var(--color-text-n8)] !bg-[var(--color-text-fff)]">
        <div class="config-title !mb-[8px] flex items-center justify-between">
          {{ t('system.config.page.pagePreview') }}
          <MsButton
            v-permission="['SYSTEM_PARAMETER_SETTING_DISPLAY:READ+UPDATE']"
            class="!leading-none"
            @click="resetPlatformConfig"
            >{{ t('system.config.page.reset') }}</MsButton
          >
        </div>
        <!-- 平台主页预览盒子 -->
        <div :class="['config-preview', '!h-[290px]', currentLocale === 'en-US' ? '!h-[340px]' : '']">
          <div ref="platformPageFullRef" class="login-preview">
            <div
              class="absolute right-[12px] top-[8px] z-[999] w-[96px] cursor-pointer text-right !text-[var(--color-text-4)]"
              @click="platformFullscreenToggle"
            >
              <MsIcon v-if="isPlatformPageFullscreen" type="icon-icon_off_screen" />
              <MsIcon v-else type="icon-icon_full_screen_one" />
            </div>
            <!-- 平台主页预览实际渲染 DOM，按三种屏幕尺寸缩放 -->
            <div
              :class="[
                'page-preview',
                'platform-preview',
                '!h-[550px]',
                isPlatformPageFullscreen ? 'full-preview' : 'normal-preview',
              ]"
            >
              <defaultLayout
                :logo="pageConfig.logoPlatform[0]?.url || defaultPlatformLogo"
                :name="pageConfig.platformName"
                class="overflow-hidden"
                is-preview
              >
                <div class="w-full bg-[var(--color-text-fff)]" style="height: calc(100% - 28px)"></div>
              </defaultLayout>
            </div>
          </div>
          <div class="config-form">
            <div class="config-form-card">
              <div class="mb-[8px] flex items-center justify-between">
                <div class="flex items-center">
                  {{ t('system.config.page.platformLogo') }}
                  <a-tag
                    v-show="pageConfig.logoPlatform[0]?.file"
                    type="warn"
                    color="rgb(var(--warning-2))"
                    class="ml-[4px] !text-[rgb(var(--warning-6))]"
                    size="small"
                  >
                    {{ t('system.config.page.unsave') }}
                  </a-tag>
                </div>
                <MsUpload
                  v-model:file-list="pageConfig.logoPlatform"
                  accept="image"
                  :max-size="200"
                  size-unit="KB"
                  :auto-upload="false"
                >
                  <a-button
                    v-permission="['SYSTEM_PARAMETER_SETTING_DISPLAY:READ+UPDATE']"
                    type="outline"
                    class="arco-btn-outline--secondary"
                    size="mini"
                  >
                    {{ t('system.config.page.replace') }}
                  </a-button>
                </MsUpload>
              </div>
              <p class="text-[12px] leading-[16px] text-[var(--color-text-4)]">
                {{ t('system.config.page.platformLogoTip') }}
              </p>
            </div>
            <a-form
              ref="platformConfigFormRef"
              :model="pageConfig"
              layout="vertical"
              :rules="{ platformName: [{ required: true, message: t('system.config.page.platformNameRequired') }] }"
            >
              <a-form-item
                :label="t('common.name')"
                field="platformName"
                asterisk-position="end"
                class="mb-[12px]"
                required
              >
                <a-input
                  v-model:model-value="pageConfig.platformName"
                  :placeholder="t('system.config.page.platformNamePlaceholder')"
                  :max-length="255"
                  :disabled="!hasAnyPermission(['SYSTEM_PARAMETER_SETTING_DISPLAY:READ+UPDATE'])"
                ></a-input>
                <MsFormItemSub :text="t('system.config.page.platformNameTip')" :show-fill-icon="false" />
              </a-form-item>
              <a-form-item :label="t('system.config.page.helpDoc')" field="helpDoc" class="mb-[12px]">
                <a-input
                  v-model:model-value="pageConfig.helpDoc"
                  :placeholder="t('system.config.page.helpDocPlaceholder')"
                  :max-length="255"
                  :disabled="!hasAnyPermission(['SYSTEM_PARAMETER_SETTING_DISPLAY:READ+UPDATE'])"
                ></a-input>
                <MsFormItemSub :text="t('system.config.page.helpDocTip')" :show-fill-icon="false" />
              </a-form-item>
            </a-form>
          </div>
        </div>
        <div class="mt-[8px] text-[var(--color-text-4)]">{{ t('system.config.page.platformConfigTip') }}</div>
      </div>
    </MsCard>
    <div
      class="fixed bottom-0 right-[16px] z-[999] flex justify-between bg-[var(--color-text-fff)] p-[24px] shadow-[0_-1px_4px_rgba(2,2,2,0.1)]"
      :style="{ width: `calc(100% - ${menuWidth + 16}px)` }"
    >
      <a-button v-permission="['SYSTEM_PARAMETER_SETTING_DISPLAY:READ+UPDATE']" type="secondary" @click="resetAll">
        {{ t('system.config.page.resetAll') }}
      </a-button>
      <a-button v-permission="['SYSTEM_PARAMETER_SETTING_DISPLAY:READ+UPDATE']" type="primary" @click="beforeSave">
        {{ t('system.config.page.save') }}
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { computed, onBeforeUnmount, ref, watch } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsColorSelect from '@/components/pure/ms-color-select/index.vue';
  import MsUpload from '@/components/pure/ms-upload/index.vue';
  import MsFormItemSub from '@/components/business/ms-form-item-sub/index.vue';
  import defaultLayout from '@/layout/default-layout.vue';
  import banner from '@/views/login/components/banner.vue';
  import loginForm from '@/views/login/components/login-form.vue';

  import { savePageConfig } from '@/api/modules/setting/config';
  import useFullScreen from '@/hooks/useFullScreen';
  import { useI18n } from '@/hooks/useI18n';
  import useLocale from '@/locale/useLocale';
  import useAppStore from '@/store/modules/app';
  import { sleep } from '@/utils';
  import { scrollIntoView } from '@/utils/dom';
  import { hasAnyPermission } from '@/utils/permission';
  import { setCustomTheme, setPlatformColor, watchStyle, watchTheme } from '@/utils/theme';

  import type { FormInstance, ValidatedError } from '@arco-design/web-vue';

  const defaultLoginImage = `${import.meta.env.BASE_URL}images/login-banner.jpg`;
  const defaultLoginLogo = `${import.meta.env.BASE_URL}images/login-logo.svg`;
  const defaultPlatformLogo = `${import.meta.env.BASE_URL}images/MeterSphere-logo.svg`;
  const { t } = useI18n();
  const { currentLocale } = useLocale();
  const appStore = useAppStore();
  const menuWidth = computed(() => {
    return appStore.menuCollapse ? appStore.collapsedWidth : appStore.menuWidth;
  });
  const pageLoading = ref(false);
  const pageConfig = ref({ ...appStore.pageConfig, slogan: t(appStore.pageConfig.slogan) });
  const loginPageFullRef = ref<HTMLElement | null>(null);
  const platformPageFullRef = ref<HTMLElement | null>(null);
  const { isFullScreen: isLoginPageFullscreen, toggleFullScreen: loginFullscreenToggle } =
    useFullScreen(loginPageFullRef);
  const { isFullScreen: isPlatformPageFullscreen, toggleFullScreen: platformFullscreenToggle } =
    useFullScreen(platformPageFullRef);
  const loginConfigFormRef = ref<FormInstance>();
  const platformConfigFormRef = ref<FormInstance>();

  const styleList = [
    {
      label: 'system.config.page.default',
      value: 'default',
    },
    {
      label: 'system.config.page.follow',
      value: 'follow',
    },
    {
      label: 'system.config.page.custom',
      value: 'custom',
    },
  ];

  const themeList = [
    {
      label: 'system.config.page.default',
      value: 'default',
    },
    {
      label: 'system.config.page.custom',
      value: 'custom',
    },
  ];

  watch(
    () => pageConfig.value.style,
    (val) => {
      watchStyle(val, pageConfig.value);
    }
  );

  watch(
    () => pageConfig.value.customStyle,
    (val) => {
      if (val && pageConfig.value.style === 'custom') {
        setPlatformColor(val);
      }
    }
  );

  watch(
    () => pageConfig.value.theme,
    (val) => {
      watchTheme(val, pageConfig.value);
    }
  );

  watch(
    () => pageConfig.value.customTheme,
    (val) => {
      if (val && pageConfig.value.theme === 'custom') {
        setCustomTheme(val);
        if (pageConfig.value.style === 'follow') {
          // 若平台风格跟随主题色
          setPlatformColor(pageConfig.value.customTheme, true);
        }
      }
    }
  );

  function resetLoginPageConfig() {
    pageConfig.value = {
      ...pageConfig.value,
      ...appStore.defaultLoginConfig,
      slogan: t(appStore.defaultLoginConfig.slogan),
    };
  }

  function resetPlatformConfig() {
    pageConfig.value = {
      ...pageConfig.value,
      ...appStore.defaultPlatformConfig,
    };
  }

  /**
   * 全部重置
   */
  function resetAll() {
    pageConfig.value = { ...appStore.getDefaultPageConfig, slogan: t(appStore.defaultLoginConfig.slogan) };
  }

  function makeParams() {
    const request = [
      {
        paramKey: 'ui.icon',
        paramValue: pageConfig.value.icon[0]?.url,
        type: 'file',
        fileName: pageConfig.value.icon[0]?.name,
        original: pageConfig.value.icon.length === 0, // 是否为默认值
        hasFile: pageConfig.value.icon[0]?.file, // 是否是上传了文件
      },
      {
        paramKey: 'ui.loginLogo',
        paramValue: pageConfig.value.loginLogo[0]?.url,
        type: 'file',
        fileName: pageConfig.value.loginLogo[0]?.name,
        original: pageConfig.value.loginLogo.length === 0,
        hasFile: pageConfig.value.loginLogo[0]?.file,
      },
      {
        paramKey: 'ui.loginImage',
        paramValue: pageConfig.value.loginImage[0]?.url,
        type: 'file',
        fileName: pageConfig.value.loginImage[0]?.name,
        original: pageConfig.value.loginImage.length === 0,
        hasFile: pageConfig.value.loginImage[0]?.file,
      },
      {
        paramKey: 'ui.logoPlatform',
        paramValue: pageConfig.value.logoPlatform[0]?.url,
        type: 'file',
        fileName: pageConfig.value.logoPlatform[0]?.name,
        original: pageConfig.value.logoPlatform.length === 0,
        hasFile: pageConfig.value.logoPlatform[0]?.file,
      },
      { paramKey: 'ui.slogan', paramValue: pageConfig.value.slogan, type: 'text' },
      { paramKey: 'ui.title', paramValue: pageConfig.value.title, type: 'text' },
      {
        paramKey: 'ui.style',
        paramValue: pageConfig.value.style === 'custom' ? pageConfig.value.customStyle : pageConfig.value.style,
        type: 'text',
      },
      {
        paramKey: 'ui.theme',
        paramValue: pageConfig.value.theme === 'custom' ? pageConfig.value.customTheme : pageConfig.value.theme,
        type: 'text',
      },
      { paramKey: 'ui.helpDoc', paramValue: pageConfig.value.helpDoc, type: 'text' },
      { paramKey: 'ui.platformName', paramValue: pageConfig.value.platformName, type: 'text' },
    ].filter((e) => {
      if (e.type === 'file') {
        return e.hasFile || e.original;
      }
      return true;
    });
    const fileList = [
      pageConfig.value.icon[0]?.file
        ? new File([pageConfig.value.icon[0].file as File], `ui.icon,${pageConfig.value.icon[0].file?.name}`)
        : undefined,
      pageConfig.value.loginLogo[0]?.file
        ? new File(
            [pageConfig.value.loginLogo[0].file as File],
            `ui.loginLogo,${pageConfig.value.loginLogo[0].file?.name}`
          )
        : undefined,
      pageConfig.value.loginImage[0]?.file
        ? new File(
            [pageConfig.value.loginImage[0].file as File],
            `ui.loginImage,${pageConfig.value.loginImage[0].file?.name}`
          )
        : undefined,
      pageConfig.value.logoPlatform[0]?.file
        ? new File(
            [pageConfig.value.logoPlatform[0]?.file as File],
            `ui.logoPlatform,${pageConfig.value.logoPlatform[0].file?.name}`
          )
        : undefined,
    ].filter((e) => e !== undefined);
    return { request, fileList };
  }

  const isSave = ref(false); // 是否保存，没有保存但是更改了主题配置需要重置一下。保存了就不需要重置了。

  /**
   * 保存并应用
   */
  async function save() {
    try {
      pageLoading.value = true;
      await savePageConfig(makeParams());
      Message.success(t('system.config.page.saveSuccess'));
      await sleep(300);
      window.location.reload();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      pageLoading.value = false;
    }
  }

  /**
   * 保存前校验
   */
  async function beforeSave() {
    try {
      await loginConfigFormRef.value?.validate((errors: Record<string, ValidatedError> | undefined) => {
        if (errors) {
          throw new Error('登录页表单校验不通过');
        }
      });
      await platformConfigFormRef.value?.validate((errors: Record<string, ValidatedError> | undefined) => {
        if (errors) {
          throw new Error('平台页表单校验不通过');
        }
      });
      save();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      const errDom = document.querySelector('.arco-form-item-message');
      scrollIntoView(errDom, { block: 'center' });
    }
  }

  onBeforeUnmount(() => {
    if (isSave.value === false) {
      // 离开前未保存，需要判断是否更改了主题和风格，改了的话需要重置回来
      if (
        pageConfig.value.style === 'custom'
          ? pageConfig.value.customStyle !== appStore.pageConfig.style
          : pageConfig.value.style !== appStore.pageConfig.style
      ) {
        watchStyle(appStore.pageConfig.style, appStore.pageConfig);
      }
      if (
        pageConfig.value.theme === 'custom'
          ? pageConfig.value.customTheme !== appStore.pageConfig.theme
          : pageConfig.value.theme !== appStore.pageConfig.theme
      ) {
        watchTheme(appStore.pageConfig.theme, appStore.pageConfig);
      }
    }
  });
</script>

<style lang="less" scoped>
  .config-title {
    @apply flex items-center font-medium;

    margin-bottom: 16px;
  }
  .config-content {
    padding: 16px;
    min-width: 1150px;
    border-radius: var(--border-radius-small);
    background-color: var(--color-text-n9);
    .config-content-head {
      @apply flex items-center justify-between;
    }
    .config-preview {
      @apply relative flex  items-start overflow-hidden;

      height: 495px;
      &--en {
        height: 595px;
      }
      @media screen and (min-width: 1600px) {
        height: 550px;
        &--en {
          height: 570px;
        }
      }
      @media screen and (min-width: 1800px) {
        height: auto;
      }
      .config-preview-head {
        @apply flex items-center justify-between;

        padding: 8px;
        width: 735px;
        background-color: var(--color-text-fff);
        @media screen and (min-width: 1600px) {
          width: 100%;
        }
      }
      .full-preview-head {
        width: 100vw;
      }
      .login-preview {
        @apply relative;

        width: 740px;
        background-color: var(--color-text-fff);
        @media screen and (min-width: 1600px) {
          width: 888px;
        }
        @media screen and (min-width: 1800px) {
          width: 100%;
        }
        .normal-preview {
          transform: translate(-25%, -25%) scale(0.5);
          @media screen and (min-width: 1600px) {
            transform: translate(-20%, -20%) scale(0.6);
          }
          @media screen and (min-width: 1800px) {
            transform: none;
          }
        }
        .page-preview {
          @apply relative flex flex-1;

          width: 1480px;
          height: 916px;
          transform-origin: center;
          @media screen and (min-width: 1800px) {
            width: 100%;
            height: 632px;
          }
        }
        .platform-preview {
          @media screen and (min-width: 1800px) {
            transform: translate(0, 0) scale(1);
          }
        }
        .full-preview {
          width: 100vw;
          height: 100vh !important;
          transform: none;
        }
      }
      .config-form {
        margin-left: 12px;
        width: 40%;
        .config-form-card {
          margin-bottom: 8px;
          padding: 12px;
          border: 1px solid var(--color-text-n8);
          border-radius: var(--border-radius-small);
          background-color: var(--color-text-fff);
        }
      }
    }
  }
</style>

<style>
  .arco-tooltip-content {
    max-width: 100%;
  }
</style>
