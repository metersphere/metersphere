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
      <a-switch v-model:model-value="form.enableGlobalCookie" type="line" size="small" @change="emit('change')" />
      {{ t('apiScenario.setting.environment.cookie') }}
    </div>
    <div class="mb-[16px] flex items-center gap-[8px]">
      <a-switch v-model:model-value="form.enableCookieShare" type="line" size="small" @change="emit('change')" />
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

    <a-form-item class="flex-1">
      <template #label>
        <div class="flex items-center">
          {{ t('apiScenario.setting.step.rule') }}
        </div>
      </template>
      <a-radio-group v-model:model-value="form.failureStrategy" @change="emit('change')">
        <a-radio :value="ScenarioFailureStrategy.CONTINUE">{{ t('apiScenario.setting.step.rule.ignore') }}</a-radio>
        <a-radio :value="ScenarioFailureStrategy.STOP">{{ t('apiScenario.setting.step.rule.stop') }}</a-radio>
      </a-radio-group>
    </a-form-item>
  </a-form>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';

  import { OtherConfig } from '@/models/apiTest/scenario';
  import { ScenarioFailureStrategy } from '@/enums/apiEnum';

  const emit = defineEmits<{
    (e: 'change'): void;
  }>();

  const { t } = useI18n();

  const form = defineModel<OtherConfig>('otherConfig', {
    required: true,
  });

  function handleInputChange() {
    if (!form.value.stepWaitTime) {
      form.value.stepWaitTime = 1000;
    }
  }
</script>

<style lang="less" scoped></style>
