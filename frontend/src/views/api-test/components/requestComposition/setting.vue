<template>
  <div class="setting">
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
            :disabled="props.disabled"
            mode="button"
            :step="100"
            :max="600000"
            :precision="0"
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
            :disabled="props.disabled"
            mode="button"
            :step="100"
            :precision="0"
            :max="600000"
            :min="0"
            class="w-[160px]"
          />
        </a-form-item>
      </div>
      <!-- <a-form-item :label="t('apiTestDebug.certificateAlias')">
        <a-input
          v-model:model-value="settingForm.certificateAlias"
          :max-length="255"
          :placeholder="t('apiTestDebug.commonPlaceholder')"
          class="w-[450px]"
        />
      </a-form-item> -->
      <a-form-item :label="t('apiTestDebug.redirect')">
        <a-checkbox
          v-model:model-value="settingForm.followRedirects"
          :disabled="props.disabled"
          @change="(val) => handleFollowRedirectsChange(val as boolean)"
        >
          {{ t('apiTestDebug.follow') }}
        </a-checkbox>
        <a-checkbox
          v-model:model-value="settingForm.autoRedirects"
          class="ml-[24px]"
          :disabled="props.disabled"
          @change="val => handleAutoRedirectsChange(val as boolean)"
        >
          {{ t('apiTestDebug.auto') }}
        </a-checkbox>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import { useI18n } from '@/hooks/useI18n';

  import { ExecuteOtherConfig } from '@/models/apiTest/common';

  const props = defineProps<{
    params: ExecuteOtherConfig;
    disabled?: boolean;
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

  function handleFollowRedirectsChange(val: boolean) {
    if (val) {
      settingForm.value.autoRedirects = false;
    }
  }

  function handleAutoRedirectsChange(val: boolean) {
    if (val) {
      settingForm.value.followRedirects = false;
    }
  }
</script>

<style lang="less" scoped>
  .setting {
    @apply h-full overflow-y-auto;
    .ms-scroll-bar();

    padding: 16px;
    border: 1px solid var(--color-text-n8);
    border-radius: var(--border-radius-small);
  }
</style>
