<template>
  <div class="login-form flex flex-col items-center">
    <div class="title">
      <div class="mt-40 flex justify-center">
        <svg-icon :width="'290px'" :height="'60px'" :name="'login-logo'" />
      </div>
      <div class="title-0 flex justify-center">
        <span class="title-welcome">{{ $t('login.form.title') }}</span>
      </div>
    </div>
    <div class="form mt-20">
      <a-form ref="formRef" :model="userInfo" @submit="handleSubmit">
        <a-form-item field="radio" hide-label>
          <a-radio-group v-model="userInfo.authenticate">
            <a-radio value="LDAP">LDAP</a-radio>
            <a-radio value="LOCAL">普通登陆</a-radio>
            <a-radio value="OIDC 90">OIDC 90</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item
          field="username"
          :rules="[{ required: true, message: $t('login.form.userName.errMsg') }]"
          :validate-trigger="['change', 'blur']"
          hide-label
        >
          <a-input
            v-model="userInfo.username"
            :placeholder="$t('login.form.userName.placeholder')"
            style="border-radius: 1.5rem"
          >
            <template #prefix>
              <icon-user />
            </template>
          </a-input>
        </a-form-item>
        <a-form-item
          field="password"
          :rules="[{ required: true, message: $t('login.form.password.errMsg') }]"
          :validate-trigger="['change', 'blur']"
          hide-label
        >
          <a-input-password
            v-model="userInfo.password"
            :placeholder="$t('login.form.password.placeholder')"
            allow-clear
            style="border-radius: 1.5rem"
          >
            <template #prefix>
              <icon-lock />
            </template>
          </a-input-password>
        </a-form-item>
        <div class="mt-4">
          <a-button style="border-radius: 1.5rem" type="primary" html-type="submit" long :loading="loading">
            {{ $t('login.form.login') }}
          </a-button>
        </div>
      </a-form>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref, reactive } from 'vue';
  import { useRouter } from 'vue-router';
  import { Message } from '@arco-design/web-vue';
  import { ValidatedError } from '@arco-design/web-vue/es/form/interface';
  import { useI18n } from '@/hooks/useI18n';
  import { useStorage } from '@vueuse/core';
  import { useUserStore } from '@/store';
  import useLoading from '@/hooks/useLoading';
  import type { LoginData } from '@/models/user';

  const router = useRouter();
  const { t } = useI18n();
  const errorMessage = ref('');
  const { loading, setLoading } = useLoading();
  const userStore = useUserStore();

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
        await userStore.login(values as LoginData);
        const { redirect, ...othersQuery } = router.currentRoute.value.query;
        router.push({
          name: (redirect as string) || 'Workplace',
          query: {
            ...othersQuery,
          },
        });
        Message.success(t('login.form.login.success'));
        const { rememberPassword } = loginConfig.value;
        const { username, password } = values;
        // 实际生产环境需要进行加密存储。
        // The actual production environment requires encrypted storage.
        loginConfig.value.username = rememberPassword ? username : '';
        loginConfig.value.password = rememberPassword ? password : '';
      } catch (err) {
        errorMessage.value = (err as Error).message;
      } finally {
        setLoading(false);
      }
    }
  };
</script>

<style lang="less" scoped>
  .login-form {
    .title-welcome {
      color: #783887;
    }
    .form {
      width: 443px;
    }
  }
</style>
