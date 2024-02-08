<template>
  <div class="h-full rounded-[var(--border-radius-small)] border border-[var(--color-text-n8)] p-[16px]">
    <a-form :model="settingForm" layout="vertical">
      <div class="flex items-center gap-[32px]">
        <a-form-item class="flex-1">
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
      </div>
      <a-form-item :label="t('apiTestDebug.certificateAlias')">
        <a-input
          v-model:model-value="settingForm.certificateAlias"
          :max-length="255"
          :placeholder="t('apiTestDebug.commonPlaceholder')"
          class="w-[450px]"
        />
      </a-form-item>
      <a-form-item :label="t('apiTestDebug.redirect')">
        <a-radio v-model:model-value="settingForm.followRedirects">{{ t('apiTestDebug.follow') }}</a-radio>
        <a-radio v-model:model-value="settingForm.autoRedirects" class="ml-[24px]">
          {{ t('apiTestDebug.auto') }}
        </a-radio>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import { useI18n } from '@/hooks/useI18n';

  import { ExecuteOtherConfig } from '@/models/apiTest/debug';

  const props = defineProps<{
    params: ExecuteOtherConfig;
  }>();
  const emit = defineEmits<{
    (e: 'update:params', val: ExecuteOtherConfig): void;
    (e: 'change', val: ExecuteOtherConfig): void;
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
