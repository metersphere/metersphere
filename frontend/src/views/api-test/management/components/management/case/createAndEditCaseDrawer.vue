<template>
  <MsDrawer
    v-model:visible="innerVisible"
    :title="isEdit ? t('case.updateCase') : t('case.createCase')"
    :width="894"
    no-content-padding
    unmount-on-close
    :ok-text="isEdit ? 'common.update' : 'common.create'"
    :ok-loading="drawerLoading"
    :save-continue-text="t('case.saveContinueText')"
    :show-continue="!isEdit && !!props.apiDetail"
    @confirm="handleDrawerConfirm(false)"
    @continue="handleDrawerConfirm(true)"
    @cancel="handleSaveCaseCancel"
  >
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
              <environmentSelect v-model:current-env="environmentId" />
              <execute
                ref="executeRef"
                v-model:detail="detailForm"
                :environment-id="currentEnvConfig?.id as string"
                :request="requestCompositionRef?.makeRequestParams"
              />
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
                  <apiStatus :status="detailForm.status" size="small" />
                </template>
                <a-option v-for="item of Object.values(RequestDefinitionStatus)" :key="item" :value="item">
                  <apiStatus :status="item" size="small" />
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
          :api-detail="apiDetailInfo as RequestParam"
          hide-response-layout-switch
          :upload-temp-file-api="uploadTempFileCase"
          :file-save-as-source-id="detailForm.id"
          :file-module-options-api="getTransferOptionsCase"
          :file-save-as-api="transferFileCase"
          :current-env-config="currentEnvConfig"
          is-definition
          @execute="(val: 'localExec' | 'serverExec')=>executeRef?.execute(val)"
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
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';
  import environmentSelect from '@/views/api-test/components/environmentSelect.vue';
  import execute from '@/views/api-test/components/executeButton.vue';
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

  const currentEnvConfig = inject<Ref<EnvConfig>>('currentEnvConfig');
  const environmentId = ref(currentEnvConfig?.value?.id);

  const formRef = ref<FormInstance>();
  const requestCompositionRef = ref<InstanceType<typeof requestComposition>>();
  const defaultCaseParams = inject<RequestParam>('defaultCaseParams');
  const defaultDetail = computed<RequestParam>(() => {
    return {
      ...(defaultCaseParams as RequestParam),
      apiDefinitionId: apiDefinitionId.value,
      protocol: apiDetailInfo.value.protocol,
    };
  });
  const detailForm = ref(cloneDeep(defaultDetail.value));
  const isEdit = ref(false);
  const executeRef = ref<InstanceType<typeof execute>>();

  async function open(apiId: string, record?: ApiCaseDetail | RequestParam, isCopy?: boolean) {
    apiDefinitionId.value = apiId;
    // 从api下的用例里打开抽屉有api信息，从case下直接复制没有api信息
    if (props.apiDetail) {
      apiDetailInfo.value = cloneDeep(props.apiDetail);
    } else {
      await getApiDetail();
    }
    // 创建或者复制的时候，请求参数为接口定义的请求参数
    detailForm.value = {
      ...cloneDeep(defaultDetail.value),
      ...(apiDetailInfo.value.protocol === 'HTTP'
        ? {
            headers: apiDetailInfo.value.headers ?? apiDetailInfo.value.request.headers,
            body: apiDetailInfo.value?.body ?? apiDetailInfo.value.request.body,
            rest: apiDetailInfo.value.rest ?? apiDetailInfo.value.request.rest,
            query: apiDetailInfo.value.query ?? apiDetailInfo.value.request.query,
          }
        : {}),
      url: apiDetailInfo.value.url ?? apiDetailInfo.value.request.url,
    };
    // 复制
    if (isCopy) {
      detailForm.value.name = `copy_${record?.name}`;
    }
    environmentId.value = currentEnvConfig?.value?.id;
    // 编辑
    if (!isCopy && record?.id) {
      isEdit.value = true;
      detailForm.value = cloneDeep(record as RequestParam);
      environmentId.value = record.environmentId;
      detailForm.value.isNew = false;
    }
    innerVisible.value = true;
    await nextTick();
    requestCompositionRef.value?.changeVerticalExpand(false); // 响应内容默认折叠
  }

  function handleSaveCaseCancel() {
    drawerLoading.value = false;
    isEdit.value = false;
    innerVisible.value = false;
    formRef.value?.resetFields();
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
          environmentId: environmentId.value as string,
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
          drawerLoading.value = false;
          return;
        }
        if (!isContinue) {
          handleSaveCaseCancel();
        }
        // 继续创建
        detailForm.value = {
          ...cloneDeep(defaultDetail.value),
          id: `case-${Date.now()}`,
          headers: apiDetailInfo.value.headers ?? apiDetailInfo.value.request.headers,
          body: apiDetailInfo.value.body ?? apiDetailInfo.value.request.body,
          rest: apiDetailInfo.value.rest ?? apiDetailInfo.value.request.rest,
          query: apiDetailInfo.value.query ?? apiDetailInfo.value.request.query,
          url: apiDetailInfo.value.url ?? apiDetailInfo.value.request.url,
        };
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
