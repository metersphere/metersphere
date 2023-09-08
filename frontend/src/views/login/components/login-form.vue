<template>
  <div class="login-form" :style="props.isPreview ? 'height: inherit' : 'height: 100vh'">
    <div class="title">
      <div class="flex justify-center">
        <img :src="innerLogo" class="h-[60px] w-[290px]" />
      </div>
      <div class="title-0 mt-[16px] flex justify-center">
        <span class="title-welcome">{{ innerSlogan || $t('login.form.title') }}</span>
      </div>
    </div>
    <div class="form mt-[32px]">
      <a-form ref="formRef" :model="userInfo" @submit="handleSubmit">
        <a-form-item class="login-form-item" field="radio" hide-label>
          <a-radio-group v-model="userInfo.authenticate" type="button">
            <a-radio value="LOCAL">普通登陆</a-radio>
            <a-radio value="LDAP">LDAP</a-radio>
            <a-radio value="OAuth2">OAuth2 测试</a-radio>
            <a-radio value="OIDC 90">OIDC 90</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item
          class="login-form-item"
          field="username"
          :rules="[{ required: true, message: $t('login.form.userName.errMsg') }]"
          :validate-trigger="['change', 'blur']"
          hide-label
        >
          <a-input v-model="userInfo.username" :placeholder="$t('login.form.userName.placeholder')" />
        </a-form-item>
        <a-form-item
          class="login-form-item"
          field="password"
          :rules="[{ required: true, message: $t('login.form.password.errMsg') }]"
          :validate-trigger="['change', 'blur']"
          hide-label
        >
          <a-input-password
            v-model="userInfo.password"
            :placeholder="$t('login.form.password.placeholder')"
            allow-clear
          />
        </a-form-item>
        <div class="mt-[12px]">
          <a-button type="primary" html-type="submit" long :loading="loading">
            {{ $t('login.form.login') }}
          </a-button>
        </div>
      </a-form>
      <div v-if="props.isPreview" class="mask"></div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref, reactive, computed } from 'vue';
  import { useRouter } from 'vue-router';
  import { Message } from '@arco-design/web-vue';
  import { ValidatedError } from '@arco-design/web-vue/es/form/interface';
  import { useI18n } from '@/hooks/useI18n';
  import { useStorage } from '@vueuse/core';
  import { useUserStore, useAppStore } from '@/store';
  import useLoading from '@/hooks/useLoading';
  import { setLoginExpires } from '@/utils/auth';
  import { GetLoginLogoUrl } from '@/api/requrls/setting/config';
  import type { LoginData } from '@/models/user';
  import { WorkbenchRouteEnum } from '@/enums/routeEnum';
  import { encrypted } from '@/utils';

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
    username: 'admin',
    password: 'metersphere',
  });

  const userInfo = reactive({
    authenticate: 'LOCAL',
    username: 'admin',
    password: 'metersphere',
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
        setLoginExpires();
        router.push({
          name: (redirect as string) || WorkbenchRouteEnum.WORKBENCH,
          query: {
            ...othersQuery,
          },
        });
      } catch (err) {
        errorMessage.value = (err as Error).message;
      } finally {
        setLoading(false);
      }
    }
  };
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
    }
  }
</style>
