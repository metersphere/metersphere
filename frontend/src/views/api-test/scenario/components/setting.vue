<template>
  <a-form :model="form" layout="vertical">
    <div style="display: flex">
      <div style="font-weight: bold">{{ t('apiScenario.setting.cookie.config') }}</div>
      <a-tooltip :content="t('apiScenario.setting.cookie.config.tip')">
        <div>
          <MsIcon
            class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
            type="icon-icon-maybe_outlined"
          />
        </div>
      </a-tooltip>
    </div>
    <div class="mb-[16px] mt-[10px] flex items-center gap-[8px]">
      <a-switch v-model:model-value="form.envCookie" type="line" size="small" />
      {{ t('apiScenario.setting.environment.cookie') }}
    </div>
    <div class="mb-[16px] flex items-center gap-[8px]">
      <a-switch v-model:model-value="form.shareCookie" type="line" size="small" />
      {{ t('apiScenario.setting.share.cookie') }}
      <a-tooltip :content="t('apiScenario.setting.share.cookie.tip')" position="right">
        <div>
          <MsIcon
            class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
            type="icon-icon-maybe_outlined"
          />
        </div>
      </a-tooltip>
    </div>
    <div style="font-weight: bold">
      {{ t('apiScenario.setting.run.config') }}
    </div>
    <div class="mb-[16px] mt-[10px] flex items-center gap-[8px]">
      <a-switch v-model:model-value="form.waitTime" type="line" size="small" />
      {{ t('apiScenario.setting.step.waitTime') }}
      <a-tooltip :content="t('apiScenario.setting.waitTime.tip')">
        <div>
          <MsIcon
            class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
            type="icon-icon-maybe_outlined"
          />
        </div>
      </a-tooltip>
    </div>

    <a-form-item v-if="form.waitTime" class="flex-1">
      <template #label>
        <div class="flex items-center">
          {{ t('apiScenario.setting.waitTime') }}
          <div class="text-[var(--color-text-brand)]">(ms)</div>
        </div>
      </template>
      <a-input-number v-model:model-value="form.connectTimeout" mode="button" :step="100" :min="0" class="w-[160px]" />
    </a-form-item>

    <a-form-item class="flex-1">
      <template #label>
        <div class="flex items-center">
          {{ t('apiScenario.setting.step.rule') }}
        </div>
      </template>
      <a-radio-group v-model:model-value="form.rule">
        <a-radio value="ignore">{{ t('apiScenario.setting.step.rule.ignore') }}</a-radio>
        <a-radio value="stop">{{ t('apiScenario.setting.step.rule.stop') }}</a-radio>
      </a-radio-group>
    </a-form-item>
  </a-form>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();
  // const emit = defineEmits(['update:formModeValue']); // 更新表单值?

  // const props = defineProps<{
  //   详情传入的表单值？
  // }>();

  const initForm = {
    envCookie: false,
    shareCookie: false,
    waitTime: false,
    connectTimeout: 0,
    rule: 'ignore',
  };
  const form = ref({ ...initForm });
</script>

<style lang="less" scoped></style>
