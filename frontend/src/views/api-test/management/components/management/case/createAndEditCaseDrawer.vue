<template>
  <MsDrawer
    v-model:visible="innerVisible"
    :title="isEdit ? t('case.updateCase') : t('case.createCase')"
    :width="894"
    no-content-padding
    :ok-text="isEdit ? 'common.update' : 'common.create'"
    :ok-loading="drawerLoading"
    :save-continue-text="t('case.saveContinueText')"
    :show-continue="!isEdit && !!props.apiDetail"
    @confirm="handleDrawerConfirm(false)"
    @continue="handleDrawerConfirm(true)"
    @cancel="handleSaveCaseCancel"
  >
    <template #headerLeft>
      <environmentSelect ref="environmentSelectRef" class="ml-[16px]" />
    </template>
    <div class="flex h-full flex-col overflow-hidden">
      <div class="px-[16px] pt-[16px]">
        <MsDetailCard
          :title="`【${apiDetailInfo.num}】${apiDetailInfo.name}`"
          :description="description"
          class="!flex-row justify-between"
        >
          <template #type="{ value }">
            <apiMethodName :method="value as RequestMethods" tag-size="small" is-tag />
          </template>
        </MsDetailCard>
        <a-form ref="formRef" class="mt-[16px]" :model="detailForm" layout="vertical">
          <a-form-item field="name" label="" :rules="[{ required: true, message: t('case.caseNameRequired') }]">
            <div class="flex w-full items-center gap-[8px]">
              <a-input
                v-model:model-value="detailForm.name"
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
              <a-select v-model:model-value="detailForm.priority" :placeholder="t('common.pleaseSelect')">
                <template #label>
                  <span class="text-[var(--color-text-2)]"> <caseLevel :case-level="detailForm.priority" /></span>
                </template>
                <a-option v-for="item of casePriorityOptions" :key="item.value" :value="item.value">
                  <caseLevel :case-level="item.label as CaseLevel" />
                </a-option>
              </a-select>
            </a-form-item>
            <a-form-item field="status" :label="t('apiTestManagement.apiStatus')">
              <a-select v-model:model-value="detailForm.status" :placeholder="t('common.pleaseSelect')">
                <template #label>
                  <apiStatus :status="detailForm.status" />
                </template>
                <a-option v-for="item of Object.values(RequestDefinitionStatus)" :key="item" :value="item">
                  <apiStatus :status="item" />
                </a-option>
              </a-select>
            </a-form-item>
            <a-form-item field="tags" :label="t('common.tag')">
              <MsTagsInput v-model:model-value="detailForm.tags" />
            </a-form-item>
          </div>
        </a-form>
      </div>
      <div class="px-[16px] font-medium">{{ t('apiTestManagement.requestParams') }}</div>
      <div class="flex-1 overflow-hidden">
        <requestComposition
          ref="requestCompositionRef"
          v-model:request="detailForm"
          :is-case="true"
          hide-response-layout-switch
          :upload-temp-file-api="uploadTempFileCase"
          :file-save-as-source-id="detailForm.id"
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
  import requestComposition, { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';

  import {
    addCase,
    getDefinitionDetail,
    getTransferOptionsCase,
    transferFileCase,
    updateCase,
    uploadTempFileCase,
  } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import { AddApiCaseParams, ApiCaseDetail, ApiDefinitionDetail } from '@/models/apiTest/management';
  import { EnvConfig } from '@/models/projectManagement/environmental';
  import { RequestDefinitionStatus, RequestMethods } from '@/enums/apiEnum';

  import { casePriorityOptions } from '@/views/api-test/components/config';

  const props = defineProps<{
    apiDetail?: RequestParam | ApiDefinitionDetail;
  }>();
  const emit = defineEmits<{
    (e: 'loadCase', id?: string): void;
  }>();

  const { t } = useI18n();
  const appStore = useAppStore();

  const innerVisible = ref(false);
  const drawerLoading = ref(false);

  const apiDefinitionId = ref('');
  const apiDetailInfo = ref<Record<string, any>>({});
  async function getApiDetail() {
    try {
      apiDetailInfo.value = await getDefinitionDetail(apiDefinitionId.value);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const description = computed(() => [
    {
      key: 'type',
      locale: 'apiTestManagement.apiType',
      value: apiDetailInfo.value.method,
    },
    {
      key: 'path',
      locale: 'apiTestManagement.path',
      value: apiDetailInfo.value.url || apiDetailInfo.value.path,
    },
  ]);

  const environmentSelectRef = ref<InstanceType<typeof environmentSelect>>();
  const currentEnvConfig = computed<EnvConfig | undefined>(() => environmentSelectRef.value?.currentEnvConfig);

  const formRef = ref<FormInstance>();
  const requestCompositionRef = ref<InstanceType<typeof requestComposition>>();
  const defaultCaseParams = inject<RequestParam>('defaultCaseParams');
  const defaultDetail: RequestParam = {
    apiDefinitionId: apiDefinitionId.value,
    ...(defaultCaseParams as RequestParam),
  };
  const detailForm = ref(cloneDeep(defaultDetail));
  const isEdit = ref(false);

  function open(apiId: string, record?: ApiCaseDetail | RequestParam, isCopy?: boolean) {
    apiDefinitionId.value = apiId;
    // 从api下的用例里打开抽屉有api信息，从case下直接复制没有api信息
    if (props.apiDetail) {
      apiDetailInfo.value = props.apiDetail;
    } else {
      getApiDetail();
    }
    // 复制
    if (isCopy) {
      detailForm.value.name = `copy_${record?.name}`;
    }
    // 编辑
    if (!isCopy && record?.id) {
      isEdit.value = true;
      detailForm.value = cloneDeep(record as RequestParam);
    }
    innerVisible.value = true;
  }

  function handleSaveCaseCancel() {
    drawerLoading.value = false;
    isEdit.value = false;
    innerVisible.value = false;
    formRef.value?.resetFields();
    detailForm.value = cloneDeep(defaultDetail);
  }

  function handleDrawerConfirm(isContinue: boolean) {
    formRef.value?.validate(async (errors) => {
      if (!errors) {
        drawerLoading.value = true;
        // 给后端传的参数
        if (!requestCompositionRef.value?.makeRequestParams()) return;
        const { linkFileIds, uploadFileIds, request, unLinkFileIds, deleteFileIds } =
          requestCompositionRef.value.makeRequestParams();
        const { name, priority, status, tags, id } = detailForm.value;
        const params: AddApiCaseParams = {
          projectId: appStore.currentProjectId,
          environmentId: currentEnvConfig.value?.id as string,
          apiDefinitionId: apiDefinitionId.value,
          linkFileIds,
          uploadFileIds,
          request,
          id: id as string,
          name,
          priority,
          status,
          tags,
          unLinkFileIds,
          deleteFileIds,
        };
        try {
          if (isEdit.value) {
            await updateCase(params);
            Message.success(t('common.updateSuccess'));
          } else {
            await addCase(params);
            Message.success(t('common.createSuccess'));
          }
          emit('loadCase', id as string);
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
        if (!isContinue) {
          handleSaveCaseCancel();
        }
        detailForm.value = cloneDeep(defaultDetail);
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
