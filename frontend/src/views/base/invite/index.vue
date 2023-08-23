<template>
  <div class="invite-page">
    <div class="form-box w-1/3 rounded-[12px] bg-white">
      <div class="form-box-title">{{ t('invite.title') }}</div>
      <a-form class="p-[24px_40px_40px_40px]" :model="form" :rules="rules" layout="vertical" @submit="confirmInvite">
        <a-form-item field="email" class="hidden-item">
          <a-input v-model="form.email" :placeholder="t('invite.emailPlaceholder')" allow-clear />
        </a-form-item>
        <a-form-item field="name" class="hidden-item">
          <a-input v-model="form.name" :placeholder="t('invite.namePlaceholder')" allow-clear />
        </a-form-item>
        <a-form-item field="password" class="hidden-item">
          <a-popover position="tl" trigger="focus" :title="t('invite.passwordTipTitle')">
            <a-input-password
              v-model="form.password"
              :placeholder="t('invite.passwordPlaceholder')"
              allow-clear
              autocomplete="new-password"
              @input="validatePsw"
              @clear="validatePsw(form.password)"
            />
            <template #content>
              <div class="check-list-item">
                <template v-if="pswLengthValidateRes">
                  <icon-check-circle-fill class="check-list-item--success" />{{ t('invite.passwordLengthRule') }}
                </template>
                <template v-else>
                  <icon-close-circle-fill class="check-list-item--error" />{{ t('invite.passwordLengthRule') }}
                </template>
              </div>
              <div class="check-list-item">
                <template v-if="pswValidateRes">
                  <icon-check-circle-fill class="check-list-item--success" />{{ t('invite.passwordWordRule') }}
                </template>
                <template v-else>
                  <icon-close-circle-fill class="check-list-item--error" />{{ t('invite.passwordWordRule') }}
                </template>
              </div>
            </template>
          </a-popover>
        </a-form-item>
        <a-form-item field="repassword" class="hidden-item">
          <a-input-password
            v-model="form.repassword"
            :placeholder="t('invite.repasswordPlaceholder')"
            autocomplete="new-password"
            allow-clear
          />
        </a-form-item>
        <a-button type="primary" html-type="submit">{{ t('invite.confirm') }}</a-button>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import { validateEmail, validatePasswordLength, validateWordPassword } from '@/utils/validate';

  const { t } = useI18n();

  const form = ref({
    name: '',
    email: '',
    password: '',
    repassword: '',
  });

  const pswValidateRes = ref(false);
  const pswLengthValidateRes = ref(false);

  const rules = {
    email: [
      { required: true, message: t('invite.emailNotNull') },
      {
        validator: (value: string, callback: (error?: string) => void) => {
          if (!validateEmail(value)) {
            callback(t('invite.emailErr'));
          }
        },
      },
    ],
    name: [{ required: true, message: t('invite.nameNotNull') }],
    password: [
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
    repassword: [{ required: true, message: t('invite.repasswordNotNull') }],
  };

  function validatePsw(value: string) {
    pswValidateRes.value = validateWordPassword(value);
    pswLengthValidateRes.value = validatePasswordLength(value);
  }

  function confirmInvite() {}
</script>

<style lang="less" scoped>
  .invite-page {
    @apply flex items-center justify-center;

    width: 100vw;
    height: 100vh;
    background-repeat: no-repeat;
    background-size: 100% 100%;
    background-image: url('@/assets/images/invite-bg.jpg');
    .form-box {
      @apply flex flex-col items-center justify-center;

      border: 3px solid #ffffff;
      box-shadow: 0 6px 30px rgb(120 56 135 / 5%), 0 16px 24px rgb(120 56 135 / 5%), 0 8px 10px rgb(120 56 135 / 5%);
      .form-box-title {
        @apply w-full text-center font-medium;

        padding: 24px;
        font-size: 24px;
        border-radius: var(--border-radius-large) var(--border-radius-large) 0 0;
        background-color: var(--color-text-n9);
      }
      .hidden-item {
        margin-bottom: 24px;
      }
    }
  }
  .check-list-item {
    @apply flex items-center gap-2;
    &:first-child {
      @apply mt-2;
    }
    &:not(:last-child) {
      @apply mb-2;
    }
    .check-list-item--success {
      color: rgb(var(--success-6));
    }
    .check-list-item--error {
      color: rgb(var(--danger-6));
    }
  }
</style>
