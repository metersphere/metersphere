<template>
  <div class="mb-[16px] flex items-center justify-between">
    <div class="font-medium text-[var(--color-text-1)]">{{ t('ms.personal.setPsw') }}</div>
  </div>
  <a-form ref="formRef" :model="form" layout="vertical" :rules="rules">
    <a-form-item field="password" :label="t('ms.personal.currentPsw')" asterisk-position="end">
      <a-input-password
        v-model="form.password"
        :placeholder="t('invite.passwordPlaceholder')"
        allow-clear
        autocomplete="new-password"
      />
    </a-form-item>
    <a-form-item field="newPsw" :label="t('ms.personal.newPsw')" asterisk-position="end">
      <MsPasswordInput v-model:password="form.newPsw" />
      <MsFormItemSub :text="t('ms.personal.changePswTip')" :show-fill-icon="false" />
    </a-form-item>
    <a-form-item field="confirmPsw" class="hidden-item">
      <a-input-password
        v-model="form.confirmPsw"
        :placeholder="t('invite.passwordPlaceholder')"
        allow-clear
        autocomplete="new-password"
      />
    </a-form-item>
    <a-form-item>
      <a-button type="primary" class="mt-[16px]" :loading="loading" @click="changePsw">
        {{ t('common.save') }}
      </a-button>
    </a-form-item>
  </a-form>
</template>

<script setup lang="ts">
  import { useRouter } from 'vue-router';
  import { FormInstance, Message } from '@arco-design/web-vue';

  import MsPasswordInput from '@/components/pure/ms-password-input/index.vue';
  import MsFormItemSub from '@/components/business/ms-form-item-sub/index.vue';

  import { updatePsw } from '@/api/modules/user';
  import { useI18n } from '@/hooks/useI18n';
  import useUserStore from '@/store/modules/user';
  import { encrypted } from '@/utils';
  import { clearToken } from '@/utils/auth';
  import { validatePasswordLength, validateWordPassword } from '@/utils/validate';

  const router = useRouter();
  const userStore = useUserStore();
  const { t } = useI18n();

  const form = ref({
    password: '',
    newPsw: '',
    confirmPsw: '',
  });
  const formRef = ref<FormInstance>();
  const loading = ref(false);
  const pswValidateRes = ref(false);
  const pswLengthValidateRes = ref(false);

  const rules = {
    password: [{ required: true, message: t('invite.passwordNotNull') }],
    newPsw: [
      { required: true, message: t('invite.passwordNotNull') },
      {
        validator: (value: string, callback: (error?: string) => void) => {
          pswValidateRes.value = validateWordPassword(value);
          pswLengthValidateRes.value = validatePasswordLength(value);
          if (!pswLengthValidateRes.value) {
            callback(t('invite.passwordLengthRule'));
          } else if (!pswValidateRes.value) {
            callback(t('invite.passwordWordRule'));
          }
        },
      },
    ],
    confirmPsw: [
      { required: true, message: t('invite.repasswordNotNull') },
      {
        validator: (value: string, callback: (error?: string) => void) => {
          if (value !== form.value.newPsw) {
            callback(t('invite.repasswordNotSame'));
          }
        },
      },
    ],
  };

  const counting = ref(3);

  function changePsw() {
    formRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          loading.value = true;
          await updatePsw({
            id: userStore.id || '',
            newPassword: encrypted(form.value.newPsw) || '',
            oldPassword: encrypted(form.value.password) || '',
          });
          Message.success({
            content: t('ms.personal.updatePswSuccess', { count: counting.value }),
            duration: 1000,
          });
          const timer = setInterval(() => {
            counting.value--;
            Message.success({
              content: t('ms.personal.updatePswSuccess', { count: counting.value }),
              duration: 1000,
            });
          }, 1000);
          setTimeout(() => {
            clearInterval(timer);
            clearToken();
            router.push({
              name: 'login',
              query: {
                ...router.currentRoute.value.query,
                redirect: router.currentRoute.value.name as string,
              },
            });
          }, 3000);
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          loading.value = false;
        }
      }
    });
  }
</script>

<style lang="less" scoped></style>
