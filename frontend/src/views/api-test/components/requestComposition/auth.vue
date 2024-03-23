<template>
  <div class="h-full rounded-[var(--border-radius-small)] border border-[var(--color-text-n8)] p-[16px]">
    <div class="mb-[8px]">{{ t('apiTestDebug.authType') }}</div>
    <a-radio-group v-model:model-value="authForm.authType" :disabled="props.disabled" class="mb-[16px]">
      <a-radio :value="RequestAuthType.NONE">No Auth</a-radio>
      <a-radio :value="RequestAuthType.BASIC">Basic Auth</a-radio>
      <a-radio :value="RequestAuthType.DIGEST">Digest Auth</a-radio>
    </a-radio-group>
    <a-form v-if="authForm.authType === 'BASIC'" ref="authFormRef" :model="authForm" layout="vertical">
      <a-form-item :label="t('apiTestDebug.username')">
        <a-input
          v-model:model-value="authForm.basicAuth.userName"
          :disabled="props.disabled"
          :placeholder="t('apiTestDebug.commonPlaceholder')"
          class="w-[450px]"
          :max-length="255"
        />
      </a-form-item>
      <a-form-item :label="t('apiTestDebug.password')">
        <a-input-password
          v-model:model-value="authForm.basicAuth.password"
          :disabled="props.disabled"
          autocomplete="new-password"
          :placeholder="t('apiTestDebug.commonPlaceholder')"
          class="w-[450px]"
        />
      </a-form-item>
    </a-form>
    <a-form v-else-if="authForm.authType == 'DIGEST'" ref="authFormRef" :model="authForm" layout="vertical">
      <a-form-item :label="t('apiTestDebug.username')">
        <a-input
          v-model:model-value="authForm.digestAuth.userName"
          :disabled="props.disabled"
          :placeholder="t('apiTestDebug.commonPlaceholder')"
          class="w-[450px]"
          :max-length="255"
        />
      </a-form-item>
      <a-form-item :label="t('apiTestDebug.password')">
        <a-input-password
          v-model:model-value="authForm.digestAuth.password"
          :disabled="props.disabled"
          autocomplete="new-password"
          :placeholder="t('apiTestDebug.commonPlaceholder')"
          class="w-[450px]"
        />
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';
  import { FormInstance } from '@arco-design/web-vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ExecuteAuthConfig } from '@/models/apiTest/common';
  import { RequestAuthType } from '@/enums/apiEnum';

  const props = defineProps<{
    params: ExecuteAuthConfig;
    disabled?: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'update:params', val: ExecuteAuthConfig): void;
    (e: 'change', val: ExecuteAuthConfig): void;
  }>();
  const { t } = useI18n();

  const authForm = useVModel(props, 'params', emit);
  const authFormRef = ref<FormInstance>();

  watch(
    () => authForm.value,
    () => {
      emit('change', authForm.value);
    },
    { deep: true }
  );
</script>

<style lang="less" scoped></style>
