<template>
  <div>
    <MsCard class="mb-[16px]" :loading="loading" simple auto-height>
      <div class="mb-[16px] flex justify-between">
        <div class="font-medium text-[var(--color-text-1)]">{{ t('system.config.memoryCleanup') }}</div>
      </div>
      <a-radio-group v-model:model-value="activeType" type="button">
        <a-radio value="log">{{ t('system.config.memoryCleanup.log') }}</a-radio>
        <a-radio value="history">{{ t('system.config.memoryCleanup.history') }}</a-radio>
      </a-radio-group>
      <template v-if="activeType === 'log'">
        <div class="mb-[8px] mt-[16px] flex items-center">
          <div class="text-[var(--color-text-1)]">{{ t('system.config.memoryCleanup.keepTime') }}</div>
          <a-tooltip :content="t('system.config.memoryCleanup.keepTimeTip')" position="right">
            <icon-question-circle
              class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
              size="16"
            />
          </a-tooltip>
        </div>
        <a-input-number
          v-model:model-value="timeCount"
          class="w-[130px]"
          :disabled="saveLoading || !hasPermission"
          :min="0"
          @blur="() => saveConfig()"
        >
          <template #append>
            <a-select
              v-model:model-value="activeTime"
              :options="timeOptions"
              class="select-input-append"
              :disabled="!hasPermission"
              :loading="saveLoading"
              @change="() => saveConfig()"
            />
          </template>
        </a-input-number>
      </template>
      <template v-else>
        <div class="mb-[8px] mt-[16px] flex items-center">
          <div class="text-[var(--color-text-1)]">{{ t('system.config.memoryCleanup.saveCount') }}</div>
          <a-tooltip position="right">
            <template #content>
              <div>{{ t('system.config.memoryCleanup.saveCountTip') }}</div>
              <div>{{ t('system.config.memoryCleanup.numberTip') }}</div>
            </template>
            <icon-question-circle
              class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
              size="16"
            />
          </a-tooltip>
        </div>
        <a-input-number
          v-model:model-value="historyCount"
          class="w-[130px]"
          :disabled="saveLoading || !hasPermission"
          :min="0"
          :max="100001"
          @blur="() => saveConfig()"
        />
      </template>
    </MsCard>
  </div>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';

  import MsCard from '@/components/pure/ms-card/index.vue';

  import { getCleanupConfig, saveCleanupConfig } from '@/api/modules/setting/config';
  import { useI18n } from '@/hooks/useI18n';
  import { hasAnyPermission } from '@/utils/permission';

  const { t } = useI18n();
  const loading = ref(false);

  const activeType = ref('log');

  const timeCount = ref(6);
  const activeTime = ref('M');
  const timeOptions = [
    {
      label: t('system.config.memoryCleanup.day'),
      value: 'D',
    },
    {
      label: t('system.config.memoryCleanup.month'),
      value: 'M',
    },
    {
      label: t('system.config.memoryCleanup.year'),
      value: 'Y',
    },
  ];
  const historyCount = ref(10);

  onBeforeMount(async () => {
    try {
      loading.value = true;
      const res = await getCleanupConfig();
      if (res.operationLog) {
        const matches = res.operationLog.match(/(\d+)([MDY])$/);
        if (matches) {
          const [, number, letter] = matches;
          timeCount.value = Number(number);
          activeTime.value = letter;
        }
      }
      if (res.operationHistory) {
        historyCount.value = Number(res.operationHistory);
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  });

  const hasPermission = computed(() => {
    return hasAnyPermission(['SYSTEM_PARAMETER_SETTING_MEMORY_CLEAN:READ+UPDATE']);
  });

  const saveLoading = ref(false);

  async function saveConfig() {
    if (!hasPermission.value) {
      return;
    }
    saveLoading.value = true;
    await saveCleanupConfig([
      {
        paramKey: 'cleanConfig.operation.log',
        paramValue: `${timeCount.value}${activeTime.value}`,
        type: 'string',
      },
      {
        paramKey: 'cleanConfig.operation.history',
        paramValue: historyCount.value.toString(),
        type: 'string',
      },
    ]);
    saveLoading.value = false;
    Message.success(t('system.config.memoryCleanup.setSuccess'));
  }
</script>

<style lang="less" scoped>
  .ms-input-group--append();
</style>
