<template>
  <a-modal
    v-model:visible="showBatchSyncModal"
    title-align="start"
    class="ms-modal-upload ms-modal-medium"
    :width="600"
    @close="resetForm"
  >
    <template #title>
      {{ t('case.apiSyncChange') }}
      <div class="text-[var(--color-text-4)]">
        {{
          t('common.selectedCount', {
            count: props.batchParams.currentSelectCount || 0,
          })
        }}
      </div>
    </template>

    <a-alert class="mb-[16px]" type="warning">{{ t('case.apiSyncModalAlert') }}</a-alert>
    <div class="mb-[8px]">
      {{ t('case.syncItem') }}
    </div>
    <a-checkbox-group v-model="checkType">
      <a-checkbox v-for="item of checkList" :key="item.value" :value="item.value">
        <div class="flex items-center">
          {{ item.label }}
          <a-tooltip v-if="item.tooltip" :content="item.tooltip" position="top">
            <div class="flex items-center">
              <icon-question-circle
                class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
                size="16"
              />
            </div>
          </a-tooltip>
        </div>
      </a-checkbox>
    </a-checkbox-group>
    <div class="my-[16px] flex items-center">
      <a-switch v-model:model-value="form.deleteRedundantParam" size="small" />
      <div class="ml-[8px] text-[var(--color-text-1)]">{{ t('case.deleteNotCorrespondValue') }}</div>
    </div>

    <div class="my-[16px] flex items-center">
      {{ t('case.changeNotice') }}
      <a-tooltip :content="t('case.confirmMessageStatusEnable')" position="bl">
        <div class="flex items-center">
          <icon-question-circle
            class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
            size="16"
          />
        </div>
      </a-tooltip>
    </div>
    <div class="my-[16px] flex items-center">
      <a-switch v-model:model-value="form.notificationConfig.apiCaseCreator" size="small" />
      <div class="ml-[8px] text-[var(--color-text-1)]">{{ t('case.NoticeApiCaseCreator') }}</div>
    </div>
    <div class="my-[16px] flex items-center">
      <a-switch v-model:model-value="form.notificationConfig.scenarioCreator" size="small" />
      <div class="ml-[8px] text-[var(--color-text-1)]">{{ t('case.NoticeApiScenarioCreator') }}</div>
    </div>

    <template #footer>
      <a-button type="secondary" @click="resetForm">{{ t('common.cancel') }}</a-button>
      <a-button type="primary" :loading="props.loading" :disabled="!checkType.length" @click="confirmBatchSync">
        {{ t('case.apiSyncChange') }}
      </a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { cloneDeep } from 'lodash-es';

  import type { BatchActionQueryParams } from '@/components/pure/ms-table/type';

  import { useI18n } from '@/hooks/useI18n';

  import type { batchSyncForm, syncItem } from '@/models/apiTest/management';
  import { RequestComposition } from '@/enums/apiEnum';

  const { t } = useI18n();

  const props = defineProps<{
    batchParams: BatchActionQueryParams;
    loading: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'batchSync', form: batchSyncForm): void;
  }>();

  const showBatchSyncModal = defineModel<boolean>('visible', {
    required: true,
  });

  const initForm: batchSyncForm = {
    notificationConfig: {
      apiCaseCreator: true,
      scenarioCreator: true,
    },
    // 同步项目
    syncItems: {
      header: false,
      body: false,
      query: false,
      rest: false,
    },
    deleteRedundantParam: false,
  };

  const form = ref<batchSyncForm>(cloneDeep(initForm));
  const initCheckList = [
    RequestComposition.HEADER,
    RequestComposition.BODY,
    RequestComposition.QUERY,
    RequestComposition.REST,
  ];
  const checkType = ref([...initCheckList]);

  const checkList = ref([
    {
      value: RequestComposition.HEADER,
      label: t('apiTestDebug.header'),
    },
    {
      value: RequestComposition.BODY,
      label: t('apiTestDebug.body'),
      tooltip: t('case.onlySyncNewParamsOrValue'),
    },
    {
      value: RequestComposition.QUERY,
      label: RequestComposition.QUERY,
    },
    {
      value: RequestComposition.REST,
      label: RequestComposition.REST,
    },
  ]);

  function resetForm() {
    form.value = cloneDeep(initForm);
    checkType.value = [...initCheckList];
    showBatchSyncModal.value = false;
  }

  function confirmBatchSync() {
    checkType.value.forEach((e: string) => {
      const key = e.toLowerCase() as keyof syncItem;
      form.value.syncItems[key] = true;
    });

    emit('batchSync', form.value);
  }

  defineExpose({
    resetForm,
  });
</script>

<style scoped></style>
