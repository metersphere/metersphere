<template>
  <div
    v-if="preheat && !props.isPreview"
    class="login-form"
    :style="props.isPreview ? 'height: inherit' : 'height: 100vh'"
  >
    <a-spin :loading="preheat" :tip="t('login.form.loading')"> </a-spin>
  </div>
  <div v-else class="login-form" :style="props.isPreview ? 'height: inherit' : 'height: 100vh'">
    <div class="title">
      <div class="flex justify-center">
        <img :src="innerLogo" class="h-[60px] w-[290px]" />
      </div>
      <div class="title-0 mt-[16px] flex justify-center">
        <span class="title-welcome one-line-text max-w-[300px]">
          {{ t(innerSlogan || '') || t('login.form.title') }}
        </span>
      </div>
    </div>

    <div class="form mt-[32px] min-w-[480px]">
      <div v-if="userInfo.authenticate === 'LOCAL'" class="mb-7 text-[18px] font-medium text-[rgb(var(--primary-5))]">
        {{ t('login.form.accountLogin') }}
      </div>
      <div
        v-if="isShowLDAP && userInfo.authenticate === 'LDAP'"
        class="mb-7 text-[18px] font-medium text-[rgb(var(--primary-5))]"
        >{{ t('login.form.LDAPLogin') }}</div
      >
      <a-form v-if="!showQrCodeTab" ref="formRef" :model="userInfo" @submit="handleSubmit">
        <!-- TOTO 第一版本暂时只考虑普通登录&LDAP -->
        <a-form-item
          class="login-form-item"
          field="username"
          :rules="[{ required: true, message: t('login.form.userName.errMsg') }]"
          :validate-trigger="['change', 'blur']"
          hide-label
        >
          <a-input
            v-model="userInfo.username"
            class="login-input"
            :max-length="64"
            size="large"
            :placeholder="
              userInfo.authenticate !== 'LOCAL'
                ? t('login.form.userName.placeholderOther')
                : t('login.form.userName.placeholder')
            "
          />
        </a-form-item>
        <a-form-item
          class="login-form-item"
          field="password"
          :rules="[{ required: true, message: t('login.form.password.errMsg') }]"
          :validate-trigger="['change', 'blur']"
          hide-label
        >
          <a-input-password
            v-model="userInfo.password"
            class="login-password-input"
            :placeholder="t('login.form.password.placeholder')"
            allow-clear
            :max-length="64"
            size="large"
          />
        </a-form-item>
        <div class="mb-[60px] mt-[12px]">
          <a-button type="primary" size="large" html-type="submit" long :loading="loading">
            {{ t('login.form.login') }}
          </a-button>
          <div v-if="showDemo" class="mb-[-16px] mt-[16px] flex items-center gap-[16px]">
            <div class="flex items-center">
              <div>{{ t('login.form.username') }}：</div>
              <div>demo</div>
            </div>
            <div class="flex items-center">
              <div>{{ t('login.form.password') }}：</div>
              <div>demo</div>
            </div>
          </div>
        </div>
      </a-form>
      <div v-if="showQrCodeTab">
        <tab-qr-code :tab-name="activeName ? activeName : orgOptions[0].value"></tab-qr-code>
      </div>
      <a-divider
        v-if="isShowLDAP || isShowOIDC || isShowOAUTH || isShowCAS || (isShowQRCode && orgOptions.length > 0)"
        orientation="center"
        type="dashed"
        class="m-0 mb-2"
      >
        <span class="text-[12px] font-normal text-[var(--color-text-4)]">{{ t('login.form.modeLoginMethods') }}</span>
      </a-divider>
      <div class="mt-4 flex items-center justify-center">
        <div
          v-if="isShowQRCode && !showQrCodeTab && orgOptions.length > 0"
          class="loginType"
          @click="switchLoginType('QR_CODE')"
        >
          <MsIcon type="icon-icon_scan_outlined" class="text-[rgb(var(--primary-5))]" />
        </div>
        <div v-if="userInfo.authenticate !== 'LDAP' && isShowLDAP" class="loginType" @click="switchLoginType('LDAP')">
          <span class="type-text text-[10px]">LDAP</span>
        </div>
        <div v-if="userInfo.authenticate !== 'LOCAL'" class="loginType" @click="switchLoginType('LOCAL')">
          <svg-icon width="18px" height="18px" name="userLogin"></svg-icon
        ></div>
        <div v-if="isShowOIDC && userInfo.authenticate !== 'OIDC'" class="loginType" @click="redirectAuth('OIDC')">
          <span class="type-text text-[10px]">OIDC</span>
        </div>
        <div v-if="isShowOAUTH && userInfo.authenticate !== 'OAUTH2'" class="loginType" @click="redirectAuth('OAUTH2')">
          <span class="type-text text-[10px]">OAuth</span>
        </div>
        <div v-if="isShowCAS && userInfo.authenticate !== 'CAS'" class="loginType" @click="redirectAuth('CAS')">
          <span class="type-text text-[10px]">CAS</span>
        </div>
      </div>
      <div v-if="props.isPreview" class="mask"></div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { Message, SelectOptionData } from '@arco-design/web-vue';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import TabQrCode from '@/views/login/components/tabQrCode.vue';

  import { getProjectInfo } from '@/api/modules/project-management/basicInfo';
  import { getAuthDetailByType } from '@/api/modules/setting/config';
  import { getPlatformParamUrl } from '@/api/modules/user';
  import { GetLoginLogoUrl } from '@/api/requrls/setting/config';
  import { useI18n } from '@/hooks/useI18n';
  import useLoading from '@/hooks/useLoading';
  import useModal from '@/hooks/useModal';
  import { NO_PROJECT_ROUTE_NAME, NO_RESOURCE_ROUTE_NAME } from '@/router/constants';
  import { useAppStore, useUserStore } from '@/store';
  import useLicenseStore from '@/store/modules/setting/license';
  import { encrypted } from '@/utils';
  import { getLongType, isLogin, setLoginExpires, setLongType } from '@/utils/auth';
  import { getFirstRouteNameByPermission, routerNameHasPermission } from '@/utils/permission';

  import type { LoginData } from '@/models/user';
  import { SettingRouteEnum } from '@/enums/routeEnum';

  import { ValidatedError } from '@arco-design/web-vue/es/form/interface';

  const router = useRouter();
  const { t } = useI18n();
  const userStore = useUserStore();
  const appStore = useAppStore();
  const licenseStore = useLicenseStore();

  const { openModal } = useModal();

  const orgOptions = ref<SelectOptionData[]>([]);

  const preheat = ref(true);

  const props = defineProps<{
    isPreview?: boolean;
    slogan?: string;
    logo?: string;
  }>();

  const innerLogo = computed(() => {
    return props.isPreview && props.logo ? props.logo : GetLoginLogoUrl;
  });

  const innerSlogan = computed(() => {
    return props.isPreview ? props.slogan : appStore.pageConfig.slogan;
  });

  const showDemo = window.location.hostname === 'demo.metersphere.com';

  const errorMessage = ref('');
  const { loading, setLoading } = useLoading();

  const userInfo = ref<{
    authenticate: string;
    username: string;
    password: string;
  }>({
    authenticate: getLongType() || 'LOCAL',
    username: '',
    password: '',
  });

  const showQrCodeTab = ref(false);
  const activeName = ref('');

  function switchLoginType(type: string) {
    userInfo.value.authenticate = type;
    if (type === 'QR_CODE') {
      showQrCodeTab.value = showQrCodeTab.value === false;
    } else {
      showQrCodeTab.value = false;
    }
  }

  const handleSubmit = async ({
    errors,
    values,
  }: {
    errors: Record<string, ValidatedError> | undefined;
    values: Record<string, any>;
  }) => {
    if (loading.value) return;
    if (!errors) {
      setLoading(true);
      try {
        try {
          await userStore.logout(true); // 登录之前先注销，防止未登出就继续登录导致报错
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log('logout error', error);
        }
        await userStore.login({
          username: encrypted(values.username),
          password: encrypted(values.password),
          authenticate: userInfo.value.authenticate,
        } as LoginData);
        setLoginExpires();
        setLongType(userInfo.value.authenticate);
        Message.success(t('login.form.login.success'));
        if (
          (!appStore.currentProjectId || appStore.currentProjectId === 'no_such_project') &&
          !router.currentRoute.value.path.startsWith(SettingRouteEnum.SETTING)
        ) {
          // 没有项目权限（用户所在的当前项目被禁用&用户被移除出去该项目/白板用户没有项目）且访问的页面非系统菜单模块，则重定向到无项目权限页面
          router.push({
            name: NO_PROJECT_ROUTE_NAME,
          });
          return;
        }
        const { redirect, ...othersQuery } = router.currentRoute.value.query;
        const redirectHasPermission =
          redirect &&
          ![NO_RESOURCE_ROUTE_NAME, NO_PROJECT_ROUTE_NAME].includes(redirect as string) &&
          routerNameHasPermission(redirect as string, router.getRoutes());
        const currentRouteName = getFirstRouteNameByPermission(router.getRoutes());
        const [res] = await Promise.all([getProjectInfo(appStore.currentProjectId), licenseStore.getValidateLicense()]); // 登录前校验 license 避免进入页面后无license状态
        if (!res || res.deleted) {
          router.push({
            name: NO_PROJECT_ROUTE_NAME,
          });
        }
        if (res) {
          appStore.setCurrentMenuConfig(res?.moduleIds || []);
        }
        router.push({
          name: redirectHasPermission ? (redirect as string) : currentRouteName,
          query: {
            ...othersQuery,
            orgId: appStore.currentOrgId,
            pId: appStore.currentProjectId,
          },
        });
      } catch (err) {
        errorMessage.value = (err as Error).message;
        // eslint-disable-next-line no-console
        console.log(err);
      } finally {
        setLoading(false);
        userStore.getAuthentication();
      }
    }
  };

  const isShowLDAP = computed(() => {
    return userStore.loginType.includes('LDAP');
  });
  const isShowOIDC = computed(() => {
    return userStore.loginType.includes('OIDC');
  });
  const isShowOAUTH = computed(() => {
    return userStore.loginType.includes('OAUTH2');
  });
  const isShowCAS = computed(() => {
    return userStore.loginType.includes('CAS');
  });

  const isShowQRCode = ref(true);

  async function initPlatformInfo() {
    try {
      const res = await getPlatformParamUrl();
      if (getLongType() && getLongType() !== 'LOCAL' && getLongType() !== 'LDAP') {
        showQrCodeTab.value = true;
        activeName.value = getLongType() || 'WE_COM';
      }
      orgOptions.value = res.map((e) => ({
        label: e.name,
        value: e.id,
      }));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  function redirectAuth(authType: string) {
    if (authType === 'LDAP' || authType === 'LOCAL') {
      return;
    }
    getAuthDetailByType(authType).then((res) => {
      if (!res) {
        return;
      }
      if (!res.enable) {
        Message.error(t('login.auth_not_enable'));
        return;
      }
      const authId = res.id;
      openModal({
        type: 'info',
        title: t('common.auth_redirect_tip'),
        content: '',
        okText: t('common.jump'),
        cancelText: t('common.cancel'),
        okButtonProps: {
          status: 'normal',
        },
        mask: true,
        maskStyle: { color: '#323233' },
        onBeforeOk: async () => {
          const config = JSON.parse(res.configuration);
          // eslint-disable-next-line no-eval
          const redirectUrl = eval(`\`${config.redirectUrl}\``);
          setLongType('LOCAL');
          let url;
          if (authType === 'CAS') {
            url = `${config.loginUrl}?service=${encodeURIComponent(redirectUrl)}`;
          }
          if (authType === 'OIDC') {
            url = `${config.authUrl}?client_id=${config.clientId}&redirect_uri=${redirectUrl}&response_type=code&scope=openid+profile+email&state=${authId}`;
            // 保存一个登录地址，禁用本地登录
            if (config.loginUrl) {
              localStorage.setItem('oidcLoginUrl', config.loginUrl);
            }
          }
          if (authType === 'OAUTH2') {
            url =
              `${config.authUrl}?client_id=${config.clientId}&response_type=code` +
              `&redirect_uri=${redirectUrl}&state=${authId}`;
            if (config.scope) {
              url += `&scope=${config.scope}`;
            }
          }
          if (url) {
            window.location.href = url;
          }
        },
        hideCancel: false,
      });
    });
  }

  onMounted(() => {
    if (!props.isPreview) {
      userStore.getAuthentication();
      initPlatformInfo();
      try {
        isLogin().then((res) => {
          preheat.value = res;
        });
      } catch (e) {
        preheat.value = false;
      }
    }
  });
</script>

<style lang="less" scoped>
  /* stylelint-disable color-function-notation */
  .login-form {
    @apply flex flex-1 flex-col items-center justify-center;
    .title-welcome {
      color: rgb(var(--primary-5));
    }
    .form {
      @apply relative;

      padding: 40px;
      border-radius: var(--border-radius-large);
      background-color: var(--color-text-fff);
      box-shadow: 0 8px 10px 0 #3232330d, 0 16px 24px 0 #3232330d, 0 6px 30px 0 #3232330d;
      .login-form-item {
        margin-bottom: 28px;
      }
      .mask {
        @apply absolute left-0 top-0 h-full w-full;
      }
      .loginType {
        margin: 0 8px;
        width: 32px;
        height: 32px;
        border: 1px solid var(--color-text-n8);
        border-radius: 50%;
        @apply flex cursor-pointer items-center justify-center;
        .type-text {
          color: rgb(var(--primary-5));
          @apply font-medium;
        }
      }
    }
  }
  :deep(.arco-divider-text) {
    padding: 0 8px !important;
  }
  .login-input {
    padding-right: 0;
    padding-left: 0;
    width: 400px;
    height: 36px;
  }
  .login-input :deep(.arco-input) {
    padding-right: 10px;
    padding-left: 10px;
  }
  .login-password-input {
    position: relative;
    padding-right: 0;
    padding-left: 0;
    width: 400px;
    height: 36px;
  }
  .login-password-input :deep(.arco-input) {
    padding-right: 50px;
    padding-left: 10px;
  }
  .login-password-input :deep(.arco-input-clear-btn) {
    position: absolute;
    top: 10px;
    float: right;
    margin-left: 350px;
  }
  .login-password-input :deep(.arco-input-suffix) {
    position: absolute;
    top: 10px;
    float: right;
    margin-left: 360px;
  }
</style>
