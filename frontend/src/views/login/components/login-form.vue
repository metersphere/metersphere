<template>
  <div class="login-form" :style="props.isPreview ? 'height: inherit' : 'height: 100vh'">
    <div class="title">
      <div class="flex justify-center">
        <img :src="innerLogo" class="h-[60px] w-[290px]" />
      </div>
      <div class="title-0 mt-[16px] flex justify-center">
        <span class="title-welcome">{{ t(innerSlogan || '') || t('login.form.title') }}</span>
      </div>
    </div>

    <div class="form mt-[32px] min-w-[416px]">
      <div class="mb-7 text-[18px] font-medium text-[rgb(var(--primary-5))]">LDAP登录</div>
      <a-form ref="formRef" :model="userInfo" @submit="handleSubmit">
        <!-- TOTO 第一版本暂时只考虑普通登录 -->
        <!-- <a-form-item class="login-form-item" field="radio" hide-label>
          <a-radio-group v-model="userInfo.authenticate" type="button">
            <a-radio value="LOCAL">{{ t('login.form.normalLogin') }}</a-radio>
            <a-radio value="LDAP">LDAP</a-radio>
            <a-radio value="OAuth2">{{ t('login.form.oauth2Test') }}</a-radio>
            <a-radio value="OIDC 90">OIDC 90</a-radio>
          </a-radio-group>
        </a-form-item> -->

        <a-form-item
          class="login-form-item"
          field="username"
          :rules="[{ required: true, message: t('login.form.userName.errMsg') }]"
          :validate-trigger="['change', 'blur']"
          hide-label
        >
          <a-input v-model="userInfo.username" :max-length="255" :placeholder="t('login.form.userName.placeholder')" />
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
            :placeholder="t('login.form.password.placeholder')"
            allow-clear
          />
        </a-form-item>
        <div class="mb-6 mt-[12px]">
          <a-button type="primary" html-type="submit" long :loading="loading">
            {{ t('login.form.login') }}
          </a-button>
        </div>
        <a-divider orientation="center" type="dashed">
          <span class="text-xs font-normal text-[var(--color-text-4)]">{{ t('login.form.modeLoginMethods') }}</span>
        </a-divider>
        <div class="flex items-center justify-center">
          <div class="loginType"> <svg-icon width="18px" height="18px" name="userLogin"></svg-icon></div>
          <div class="loginType">
            <span class="type-text text-[10px]">OIDC</span>
          </div>
          <div class="loginType">
            <span class="type-text text-[7px]">OAUTH</span>
          </div>
        </div>
      </a-form>
      <div v-if="props.isPreview" class="mask"></div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { computed, reactive, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { useStorage } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';

  import { GetLoginLogoUrl } from '@/api/requrls/setting/config';
  import { useI18n } from '@/hooks/useI18n';
  import useLoading from '@/hooks/useLoading';
  import { useAppStore, useUserStore } from '@/store';
  import { encrypted } from '@/utils';
  import { setLoginExpires } from '@/utils/auth';
  import { getFirstRouteNameByPermission, routerNameHasPermission } from '@/utils/permission';

  import type { LoginData } from '@/models/user';

  import { ValidatedError } from '@arco-design/web-vue/es/form/interface';

  const router = useRouter();
  const { t } = useI18n();
  const userStore = useUserStore();
  const appStore = useAppStore();

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

  const errorMessage = ref('');
  const { loading, setLoading } = useLoading();

  const loginConfig = useStorage('login-config', {
    rememberPassword: true,
    username: '',
    password: '',
  });

  const userInfo = reactive({
    authenticate: 'LOCAL',
    username: '',
    password: '',
  });

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
        await userStore.login({
          username: encrypted(values.username),
          password: encrypted(values.password),
          authenticate: values.authenticate,
        } as LoginData);
        Message.success(t('login.form.login.success'));
        const { rememberPassword } = loginConfig.value;
        const { username, password } = values;
        loginConfig.value.username = rememberPassword ? username : '';
        loginConfig.value.password = rememberPassword ? password : '';
        const { redirect, ...othersQuery } = router.currentRoute.value.query;
        const redirectHasPermission = redirect && routerNameHasPermission(redirect as string, router.getRoutes());
        const currentRouteName = getFirstRouteNameByPermission(router.getRoutes());
        setLoginExpires();
        router.push({
          name: redirectHasPermission ? (redirect as string) : currentRouteName,
          query: {
            ...othersQuery,
            organizationId: appStore.currentOrgId,
            projectId: appStore.currentProjectId,
          },
        });
      } catch (err) {
        errorMessage.value = (err as Error).message;
        // eslint-disable-next-line no-console
        console.log(err);
      } finally {
        setLoading(false);
      }
    }
  };
  onMounted(async () => {
    // userStore.getAuthenticationList();
    // try {
    //   const res = await getAuthenticationList();
    //   console.log(res);
    // } catch (error) {
    //   console.log(error);
    // }
  });
</script>

<style lang="less" scoped>
  /* stylelint-disable color-function-notation */
  .login-form {
    @apply flex flex-1 flex-col items-center justify-center;

    background: linear-gradient(
      26.72deg,
      rgba(var(--primary-5), 0.02) 0%,
      rgba(var(--primary-5), 0.1) 51.67%,
      var(--color-text-fff) 100%
    );
    .title-welcome {
      color: rgb(var(--primary-5));
    }
    .form {
      @apply relative bg-white;

      padding: 40px;
      border-radius: var(--border-radius-large);
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
</style>
