<template>
  <a-popover position="tl" trigger="focus" :title="t('ms.passwordInput.passwordTipTitle')">
    <a-input-password
      v-model="innerPsw"
      :placeholder="t('ms.passwordInput.passwordPlaceholder')"
      allow-clear
      autocomplete="new-password"
      :max-length="32"
      @input="validatePsw"
      @clear="validatePsw(innerPsw)"
    />
    <template #content>
      <div class="check-list-item">
        <template v-if="pswLengthValidateRes">
          <icon-check-circle-fill class="check-list-item--success" />{{ t('ms.passwordInput.passwordLengthRule') }}
        </template>
        <template v-else>
          <icon-close-circle-fill class="check-list-item--error" />{{ t('ms.passwordInput.passwordLengthRule') }}
        </template>
      </div>
      <div class="check-list-item">
        <template v-if="pswValidateRes">
          <icon-check-circle-fill class="check-list-item--success" />
          {{ t('ms.passwordInput.passwordWordRule', { symbol: '!@#$%^&*()_+.' }) }}
        </template>
        <template v-else>
          <icon-close-circle-fill class="check-list-item--error" />
          {{ t('ms.passwordInput.passwordWordRule', { symbol: '!@#$%^&*()_+.' }) }}
        </template>
      </div>
    </template>
  </a-popover>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';
  import { validatePasswordLength, validateWordPassword } from '@/utils/validate';

  const props = defineProps<{
    password: string;
  }>();
  const emit = defineEmits<{
    (e: 'update:password', val: string): void;
  }>();

  const { t } = useI18n();

  const innerPsw = ref(props.password);

  watch(
    () => props.password,
    (val) => {
      innerPsw.value = val;
    }
  );

  watch(
    () => innerPsw.value,
    (val) => {
      emit('update:password', val);
    }
  );

  const pswValidateRes = ref(false);
  const pswLengthValidateRes = ref(false);

  function validatePsw(value: string) {
    pswValidateRes.value = validateWordPassword(value);
    pswLengthValidateRes.value = validatePasswordLength(value);
  }
</script>

<style lang="less" scoped>
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
