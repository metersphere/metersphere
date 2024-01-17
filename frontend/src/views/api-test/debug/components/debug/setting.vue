<template>
  <div class="pb-[24px]">
    <div class="mb-[8px] font-medium">{{ t('apiTestDebug.auth') }}</div>
    <div class="rounded-[var(--border-radius-small)] border border-[var(--color-text-n8)] p-[16px]">
      <div class="mb-[8px]">{{ t('apiTestDebug.setting') }}</div>
      <a-form :model="settingForm" layout="vertical">
        <a-form-item>
          <template #label>
            <div class="flex items-center">
              {{ t('apiTestDebug.connectTimeout') }}
              <div class="text-[var(--color-text-brand)]">(ms)</div>
            </div>
          </template>
          <a-input-number
            v-model:model-value="settingForm.connectTimeout"
            mode="button"
            :step="100"
            :min="0"
            class="w-[160px]"
          />
        </a-form-item>
        <a-form-item>
          <template #label>
            <div class="flex items-center">
              {{ t('apiTestDebug.responseTimeout') }}
              <div class="text-[var(--color-text-brand)]">(ms)</div>
            </div>
          </template>
          <a-input-number
            v-model:model-value="settingForm.responseTimeout"
            mode="button"
            :step="100"
            :min="0"
            class="w-[160px]"
          />
        </a-form-item>
        <a-form-item :label="t('apiTestDebug.certificateAlias')">
          <a-input
            v-model:model-value="settingForm.certificateAlias"
            :placeholder="t('apiTestDebug.commonPlaceholder')"
            class="w-[450px]"
          />
        </a-form-item>
        <a-form-item :label="t('apiTestDebug.redirect')">
          <a-radio-group v-model:model-value="settingForm.redirect">
            <a-radio value="follow">{{ t('apiTestDebug.follow') }}</a-radio>
            <a-radio value="auto">{{ t('apiTestDebug.auto') }}</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import { useI18n } from '@/hooks/useI18n';

  interface SettingForm {
    connectTimeout: number;
    responseTimeout: number;
    certificateAlias: string;
    redirect: 'follow' | 'auto';
  }
  const props = defineProps<{
    params: SettingForm;
  }>();
  const emit = defineEmits<{
    (e: 'update:params', val: SettingForm): void;
    (e: 'change', val: SettingForm): void;
  }>();
  const { t } = useI18n();

  const settingForm = useVModel(props, 'params', emit);

  watch(
    () => settingForm.value,
    () => {
      emit('change', settingForm.value);
    },
    { deep: true }
  );
</script>

<style lang="less" scoped></style>
