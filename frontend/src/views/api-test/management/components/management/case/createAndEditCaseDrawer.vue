<template>
  <MsDrawer
    v-model:visible="innerVisible"
    :title="t('case.createCase')"
    :width="894"
    no-content-padding
    :ok-text="t('common.create')"
    :ok-loading="drawerLoading"
    :save-continue-text="t('case.saveContinueText')"
    :show-continue="true"
    @confirm="handleDrawerConfirm"
    @continue="handleDrawerConfirm(true)"
    @cancel="handleSaveCaseCancel"
  >
    <template #headerLeft>
      <environmentSelect ref="environmentSelectRef" class="ml-[16px]" />
    </template>
    <div class="flex h-full flex-col overflow-hidden">
      <div class="px-[16px] pt-[16px]">
        <MsDetailCard
          :title="`【${apiDataDetail.num}】${apiDataDetail.name}`"
          :description="description"
          no-more
          class="!flex-row justify-between"
        >
          <template #type="{ value }">
            <apiMethodName :method="value as RequestMethods" tag-size="small" is-tag />
          </template>
        </MsDetailCard>
        <a-form ref="formRef" class="mt-[16px]" :model="caseModalForm" layout="vertical">
          <a-form-item field="name" label="" :rules="[{ required: true, message: t('case.caseNameRequired') }]">
            <div class="flex w-full items-center gap-[8px]">
              <a-input
                v-model:model-value="caseModalForm.name"
                :placeholder="t('case.caseNamePlaceholder')"
                allow-clear
                :max-length="255"
                show-word-limit
              />
              <a-button type="primary">
                {{ t('apiTestManagement.execute') }}
              </a-button>
            </div>
          </a-form-item>
          <div class="flex gap-[16px]">
            <a-form-item field="priority" :label="t('case.caseLevel')">
              <a-select v-model:model-value="caseModalForm.priority" :placeholder="t('common.pleaseSelect')">
                <template #label>
                  <span class="text-[var(--color-text-2)]"> <caseLevel :case-level="caseModalForm.priority" /></span>
                </template>
                <a-option v-for="item of casePriorityOptions" :key="item.value" :value="item.value">
                  <caseLevel :case-level="item.label as CaseLevel" />
                </a-option>
              </a-select>
            </a-form-item>
            <a-form-item field="status" :label="t('apiTestManagement.apiStatus')">
              <a-select v-model:model-value="caseModalForm.status" :placeholder="t('common.pleaseSelect')">
                <template #label>
                  <apiStatus :status="caseModalForm.status" />
                </template>
                <a-option v-for="item of Object.values(RequestDefinitionStatus)" :key="item" :value="item">
                  <apiStatus :status="item" />
                </a-option>
              </a-select>
            </a-form-item>
            <a-form-item field="tags" :label="t('common.tag')">
              <MsTagsInput v-model:model-value="caseModalForm.tags" />
            </a-form-item>
          </div>
        </a-form>
      </div>
      <div class="px-[16px] font-medium">{{ t('apiTestManagement.requestParams') }}</div>
      <div class="flex-1 overflow-hidden">
        <requestComposition
          ref="requestCompositionRef"
          v-model:request="apiDataDetail"
          :is-case="true"
          hide-response-layout-switch
          :upload-temp-file-api="uploadTempFileCase"
          :file-save-as-source-id="apiDataDetail.id"
          :file-module-options-api="getTransferOptionsCase"
          :file-save-as-api="transferFileCase"
          :current-env-config="currentEnvConfig"
          :is-definition="true"
        />
      </div>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { FormInstance, Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsDetailCard from '@/components/pure/ms-detail-card/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import type { CaseLevel } from '@/components/business/ms-case-associate/types';
  import environmentSelect from '../../environmentSelect.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';
  import { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';

  import {
    addCase,
    getTransferOptionsCase,
    transferFileCase,
    uploadTempFileCase,
  } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';

  import { ApiCaseDetail } from '@/models/apiTest/management';
  import { EnvConfig } from '@/models/projectManagement/environmental';
  import { RequestDefinitionStatus, RequestMethods } from '@/enums/apiEnum';

  import { casePriorityOptions } from '@/views/api-test/components/config';

  // 懒加载requestComposition组件
  const requestComposition = defineAsyncComponent(
    () => import('@/views/api-test/components/requestComposition/index.vue')
  );

  const props = defineProps<{
    apiDetail: RequestParam;
  }>();
  const emit = defineEmits(['loadCase']);

  const apiDataDetail = ref<RequestParam>(cloneDeep(props.apiDetail));

  const { t } = useI18n();

  const innerVisible = ref(false);

  const drawerLoading = ref(false);

  const description = computed(() => [
    {
      key: 'type',
      locale: 'apiTestManagement.apiType',
      value: apiDataDetail.value.method,
    },
    {
      key: 'path',
      locale: 'apiTestManagement.path',
      value: apiDataDetail.value.url || apiDataDetail.value.path,
    },
  ]);

  const environmentSelectRef = ref<InstanceType<typeof environmentSelect>>();
  const currentEnvConfig = computed<EnvConfig | undefined>(() => environmentSelectRef.value?.currentEnvConfig);

  const formRef = ref<FormInstance>();
  const initForm: any = {
    apiDefinitionId: apiDataDetail.value.id as string,
    name: '',
    priority: 'P0',
    tags: [],
    status: RequestDefinitionStatus.PROCESSING,
  };
  const caseModalForm = ref({ ...initForm });

  const requestCompositionRef = ref<InstanceType<typeof requestComposition>>();

  function open(record?: ApiCaseDetail, isCopy?: boolean) {
    innerVisible.value = true;
    if (isCopy) {
      caseModalForm.value.name = record?.name;
    }
  }

  function handleSaveCaseCancel() {
    innerVisible.value = false;
    formRef.value?.resetFields();
    caseModalForm.value = { ...initForm };
  }

  function handleDrawerConfirm(isContinue: boolean) {
    formRef.value?.validate(async (errors) => {
      if (!errors) {
        drawerLoading.value = true;
        const params = { ...requestCompositionRef.value?.makeRequestParams(), ...caseModalForm.value };
        try {
          await addCase(params);
          Message.success(t('common.updateSuccess'));
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
        if (!isContinue) {
          emit('loadCase');
          handleSaveCaseCancel();
        }
        caseModalForm.value = { ...initForm };
        drawerLoading.value = false;
      }
    });
  }

  defineExpose({
    open,
  });
</script>

<style lang="less" scoped>
  :deep(.arco-select-view-value) {
    font-weight: 400;
  }
  :deep(.ms-detail-card-title) {
    width: 50%;
  }
  :deep(.ms-detail-card-desc) {
    gap: 16px;
    & > div {
      width: auto;
    }
  }
  :deep(.arco-form > .arco-form-item):nth-child(1) .arco-form-item-label-col {
    display: none;
  }
  :deep(.request-and-response) {
    height: calc(100% - 56px);
  }
</style>
