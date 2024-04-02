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
              <environmentSelect ref="environmentSelectRef" v-model:current-env="environmentId" />
              <executeButton
                ref="executeRef"
                v-permission="['PROJECT_API_DEFINITION_CASE:READ+EXECUTE']"
                :execute-loading="detailForm.executeLoading"
                @stop-debug="stopDebug"
                @execute="handleExecute"
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
          @execute="handleExecute"
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
  import executeButton from '@/views/api-test/components/executeButton.vue';
  import requestComposition, { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';

  import { localExecuteApiDebug } from '@/api/modules/api-test/common';
  import {
    addCase,
    debugCase,
    getDefinitionDetail,
    getTransferOptionsCase,
    runCase,
    transferFileCase,
    updateCase,
    uploadTempFileCase,
  } from '@/api/modules/api-test/management';
  import { getSocket } from '@/api/modules/project-management/commonScript';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { getGenerateId } from '@/utils';

  import { AddApiCaseParams, ApiCaseDetail, ApiDefinitionDetail } from '@/models/apiTest/management';
  import { EnvConfig } from '@/models/projectManagement/environmental';
  import { RequestDefinitionStatus, RequestMethods } from '@/enums/apiEnum';

  import { casePriorityOptions, defaultResponse } from '@/views/api-test/components/config';

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

  async function open(apiId: string, record?: ApiCaseDetail | RequestParam, isCopy?: boolean) {
    apiDefinitionId.value = apiId;
    // 从api下的用例里打开抽屉有api信息，从case下直接复制没有api信息
    if (props.apiDetail) {
      apiDetailInfo.value = cloneDeep(props.apiDetail);
    } else {
      await getApiDetail();
    }
    // 创建的时候，请求参数为接口定义的请求参数
    detailForm.value = {
      ...cloneDeep(defaultDetail.value),
      ...(apiDetailInfo.value.protocol === 'HTTP'
        ? {
            headers: apiDetailInfo.value.headers ?? apiDetailInfo.value.request.headers,
            body: apiDetailInfo.value?.body ?? apiDetailInfo.value.request.body,
            rest: apiDetailInfo.value.rest ?? apiDetailInfo.value.request.rest,
            query: apiDetailInfo.value.query ?? apiDetailInfo.value.request.query,
            authConfig: apiDetailInfo.value.authConfig ?? apiDetailInfo.value.request.authConfig,
            otherConfig: apiDetailInfo.value.otherConfig ?? apiDetailInfo.value.request.otherConfig,
          }
        : {}),
      children: apiDetailInfo.value.children ?? apiDetailInfo.value.request.children,
      url: apiDetailInfo.value.url ?? apiDetailInfo.value.request.url,
    };
    // 复制
    if (isCopy) {
      detailForm.value = cloneDeep(record as RequestParam);
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
        // 保存并继续创建都以当前页面内容为基础,不需要还原
        detailForm.value.id = `case-${Date.now()}`;
        detailForm.value.name = '';
        drawerLoading.value = false;
      }
    });
  }
  const executeRef = ref<InstanceType<typeof executeButton>>();
  const reportId = ref('');
  const websocket = ref<WebSocket>();
  const temporaryResponseMap = {}; // 缓存websocket返回的报告内容，避免执行接口后切换tab导致报告丢失
  // 开启websocket监听，接收执行结果
  function debugSocket(executeType?: 'localExec' | 'serverExec') {
    websocket.value = getSocket(
      reportId.value,
      executeType === 'localExec' ? '/ws/debug' : '',
      executeType === 'localExec' ? executeRef.value?.localExecuteUrl : ''
    );
    websocket.value.addEventListener('message', (event) => {
      const data = JSON.parse(event.data);
      if (data.msgType === 'EXEC_RESULT') {
        if (detailForm.value.reportId === data.reportId) {
          // 判断当前查看的tab是否是当前返回的报告的tab，是的话直接赋值
          detailForm.value.response = data.taskResult; // 渲染出用例详情和创建用例抽屉的响应数据
          detailForm.value.executeLoading = false;
        } else {
          // 不是则需要把报告缓存起来，等切换到对应的tab再赋值
          temporaryResponseMap[data.reportId] = data.taskResult;
        }
      } else if (data.msgType === 'EXEC_END') {
        // 执行结束，关闭websocket
        websocket.value?.close();
        detailForm.value.executeLoading = false;
      }
    });
  }
  async function handleExecute(executeType?: 'localExec' | 'serverExec') {
    try {
      detailForm.value.executeLoading = true;
      detailForm.value.response = cloneDeep(defaultResponse);
      const makeRequestParams = requestCompositionRef.value?.makeRequestParams(executeType); // 写在reportId之前，防止覆盖reportId
      reportId.value = getGenerateId();
      detailForm.value.reportId = reportId.value; // 存储报告ID
      let res;
      const params = {
        environmentId: environmentId.value as string,
        frontendDebug: executeType === 'localExec',
        reportId: reportId.value,
        apiDefinitionId: detailForm.value.apiDefinitionId,
        request: makeRequestParams?.request,
        linkFileIds: makeRequestParams?.linkFileIds,
        uploadFileIds: makeRequestParams?.uploadFileIds,
      };
      debugSocket(executeType); // 开启websocket
      if (!(detailForm.value.id as string).startsWith('c') && executeType === 'serverExec') {
        // 已创建的服务端
        res = await runCase({
          id: detailForm.value.id as string,
          projectId: detailForm.value.projectId,
          ...params,
        });
      } else {
        res = await debugCase({
          id: `case-${Date.now()}`,
          projectId: appStore.currentProjectId,
          ...params,
        });
      }
      if (executeType === 'localExec') {
        await localExecuteApiDebug(executeRef.value?.localExecuteUrl as string, res);
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      detailForm.value.executeLoading = false;
    }
  }
  function stopDebug() {
    websocket.value?.close();
    detailForm.value.executeLoading = false;
  }

  const environmentSelectRef = ref<InstanceType<typeof environmentSelect>>();
  const currentEnvConfigByDrawer = computed<EnvConfig | undefined>(() => environmentSelectRef.value?.currentEnvConfig);
  provide('currentEnvConfig', readonly(currentEnvConfigByDrawer));

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
