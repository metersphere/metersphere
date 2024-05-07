<template>
  <a-modal
    v-model:visible="showModalVisible"
    class="ms-modal-form ms-modal-small"
    title-align="start"
    :mask-closable="false"
  >
    <template #title>
      {{ form.id ? t('testPlan.testPlanIndex.updateScheduledTask') : t('testPlan.testPlanIndex.createScheduledTask') }}
    </template>
    <a-form ref="formRef" :model="form" layout="vertical">
      <a-form-item :label="t('testPlan.testPlanIndex.triggerTime')" asterisk-position="end">
        <a-select v-model:model-value="form.time" :placeholder="t('common.pleaseSelect')">
          <a-option v-for="item of syncFrequencyOptions" :key="item.value" :value="item.value">
            <span class="text-[var(--color-text-2)]"> {{ item.value }}</span
            ><span class="ml-1 text-[var(--color-text-n4)] hover:text-[rgb(var(--primary-5))]">
              {{ item.label }}
            </span>
          </a-option>
          <template #footer>
            <div class="mb-[6px] mt-[4px] p-[3px_8px]">
              <MsButton type="text" class="text-[rgb(var(--primary-5))]" @click="createCustomFrequency">
                {{ t('project.testPlanIndex.customFrequency') }}
              </MsButton>
            </div>
          </template>
        </a-select>
      </a-form-item>
      <a-radio-group v-model="form.env" class="mb-4">
        <a-radio value="">
          {{ t('project.testPlanIndex.defaultEnv') }}
          <span class="float-right mx-1 mt-[1px]">
            <a-tooltip :content="t('testPlan.testPlanIndex.envTip')" position="top">
              <IconQuestionCircle class="h-[16px] w-[16px] text-[--color-text-4] hover:text-[rgb(var(--primary-5))]"
            /></a-tooltip>
          </span>
        </a-radio>
        <a-radio value="new"> {{ t('project.testPlanIndex.newEnv') }}</a-radio>
      </a-radio-group>
      <a-radio-group v-model="form.methods">
        <a-radio value="serial">{{ t('testPlan.testPlanIndex.serial') }}</a-radio>
        <a-radio value="parallel">{{ t('testPlan.testPlanIndex.parallel') }}</a-radio>
      </a-radio-group>
      <a-form-item :label="t('testPlan.testPlanIndex.resourcePool')" asterisk-position="end" class="mb-0">
        <a-select
          v-model="form.resourcePoolIds"
          :placeholder="t('common.pleaseSelect')"
          :field-names="{ value: 'id', label: 'name' }"
        >
          <template #label="{ data }">
            {{ data }}
          </template>
          <a-option v-for="item of resourcesList" :key="item.id" :value="item.id">
            <div class="flex w-full items-center justify-between">
              <div>
                {{ item.name }}
              </div>
              <div class="flex items-center">
                <span class="text-[var(--color-text-4)]">CPU</span>
                <span class="mx-2"> {{ item.cpuRate }}</span>
                <MsTag theme="outline" :type="item.status ? 'link' : 'success'" size="small">
                  {{ item.status ? t('project.testPlanIndex.doing') : t('project.testPlanIndex.inFreeTime') }}
                </MsTag>
              </div>
            </div>
          </a-option>
        </a-select>
      </a-form-item>
    </a-form>
    <template #footer>
      <div class="flex items-center justify-between">
        <div class="flex flex-row items-center justify-center">
          <a-switch v-model="form.enable" size="small" type="line" />
          <span class="ml-2">{{ t('testPlan.testPlanIndex.timingState') }}</span>
          <a-tooltip size="mini" position="top">
            <template #content>
              <div>{{ t('testPlan.testPlanIndex.timingStateEnable') }}</div>
              <div>{{ t('testPlan.testPlanIndex.timingStateClose') }}</div>
            </template>
            <div class="mx-1 flex items-center">
              <span class="mt-[2px]"
                ><IconQuestionCircle class="h-[16px] w-[16px] text-[--color-text-4] hover:text-[rgb(var(--primary-5))]"
              /></span>
            </div>
          </a-tooltip>
        </div>
        <div>
          <a-button type="secondary" class="mr-3" @click="handleCancel">{{ t('system.plugin.pluginCancel') }}</a-button>
          <a-button type="primary" :loading="confirmLoading" @click="handleCreate">{{ t('common.create') }}</a-button>
        </div>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useVModel } from '@vueuse/core';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import type { ResourcesItem } from '@/models/testPlan/testPlan';

  const appStore = useAppStore();

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
  }>();
  const showModalVisible = useVModel(props, 'visible', emit);

  const initForm = {
    id: '',
    time: '',
    env: '',
    resourcePoolIds: '',
    enable: false,
    methods: 'parallel',
  };

  const form = ref({ ...initForm });

  const confirmLoading = ref<boolean>(false);
  const formRef = ref();
  function handleCreate() {}

  function resetForm() {
    form.value = { ...initForm };
  }

  function handleCancel() {
    showModalVisible.value = false;
    formRef.value?.resetFields();
    resetForm();
  }

  const syncFrequencyOptions = [
    { label: t('apiTestManagement.timeTaskHour'), value: '0 0 0/1 * * ?' },
    { label: t('apiTestManagement.timeTaskSixHour'), value: '0 0 0/6 * * ?' },
    { label: t('apiTestManagement.timeTaskTwelveHour'), value: '0 0 0/12 * * ?' },
    { label: t('apiTestManagement.timeTaskDay'), value: '0 0 0 * * ?' },
  ];

  const resourcesList = ref<ResourcesItem[]>([
    {
      id: '1',
      name: '200.4',
      cpuRate: '80%',
      status: true,
    },
    {
      id: '2',
      name: 'LOCAL',
      cpuRate: '80%',
      status: true,
    },
  ]);

  function createCustomFrequency() {}
</script>

<style scoped lang="less">
  :deep(.arco-select-option-content) {
    @apply w-full;
  }
</style>
