<template>
  <MsDrawer
    v-model:visible="innerVisible"
    class="createAndEditCaseDrawer"
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
      <MsDetailCard
        :title="`【${apiDetailInfo.num}】${apiDetailInfo.name}`"
        :description="description"
        class="m-[16px] !flex-row justify-between"
      >
        <template #type="{ value }">
          <apiMethodName :method="value as RequestMethods" tag-size="small" is-tag />
        </template>
      </MsDetailCard>
      <a-form ref="formNameRef" class="flex-row items-start gap-[8px] px-[16px]" :model="detailForm" layout="vertical">
        <a-form-item field="name" label="" :rules="[{ required: true, message: t('case.caseNameRequired') }]">
          <a-input
            v-model:model-value="detailForm.name"
            :placeholder="t('case.caseNamePlaceholder')"
            allow-clear
            :max-length="255"
            show-word-limit
          />
        </a-form-item>
        <MsEnvironmentSelect ref="environmentSelectRef" :env="environmentId" />
        <executeButton
          ref="executeRef"
          v-permission="['PROJECT_API_DEFINITION_CASE:READ+EXECUTE']"
          :execute-loading="detailForm.executeLoading"
          @stop-debug="stopDebug"
          @execute="handleExecute"
        />
      </a-form>
      <div class="request-tab-and-response flex-1">
        <a-form ref="formRef" class="flex-row gap-[16px] px-[16px]" :model="detailForm" layout="vertical">
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
              <a-option v-for="item of Object.values(RequestCaseStatus)" :key="item" :value="item">
                <apiStatus :status="item" size="small" />
              </a-option>
            </a-select>
          </a-form-item>
          <a-form-item field="tags" :label="t('common.tag')">
            <MsTagsInput v-model:model-value="detailForm.tags" :max-tag-count="1" />
          </a-form-item>
        </a-form>
        <div class="flex items-center justify-between">
          <div class="px-[16px] font-medium">{{ t('apiTestManagement.requestParams') }}</div>
          <ApiChangeTag
            :ignore-api-change="detailForm.ignoreApiChange"
            :ignore-api-diff="detailForm.ignoreApiDiff"
            :inconsistent-with-api="detailForm.inconsistentWithApi"
            @show-diff="showDiffDrawer"
          />
        </div>

        <requestComposition
          ref="requestCompositionRef"
          v-model:request="detailForm"
          :is-case="true"
          :api-detail="apiDetailInfo as RequestParam"
          :upload-temp-file-api="uploadTempFileCase"
          :file-save-as-source-id="detailForm.id"
          :file-module-options-api="getTransferOptionsCase"
          :file-save-as-api="transferFileCase"
          is-definition
          @execute="handleExecute"
          @request-tab-click="requestTabClick"
        />
      </div>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { FormInstance, Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsDetailCard, { type Description } from '@/components/pure/ms-detail-card/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import type { CaseLevel } from '@/components/business/ms-case-associate/types';
  import MsEnvironmentSelect from '@/components/business/ms-environment-select/index.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';
  import executeButton from '@/views/api-test/components/executeButton.vue';
  import requestComposition, { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';
  import ApiChangeTag from '@/views/api-test/management/components/management/case/apiChangeTag.vue';

  import { localExecuteApiDebug } from '@/api/modules/api-test/common';
  import {
    addCase,
    caseFileCopy,
    debugCase,
    definitionFileCopy,
    getDefinitionDetail,
    getTransferOptionsCase,
    runCase,
    transferFileCase,
    updateCase,
    uploadTempFileCase,
  } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import useShortcutSave from '@/hooks/useShortcutSave';
  import useWebsocket from '@/hooks/useWebsocket';
  import useAppStore from '@/store/modules/app';
  import { getGenerateId } from '@/utils';

  import { AddApiCaseParams, ApiCaseDetail, ApiDefinitionDetail } from '@/models/apiTest/management';
  import { RequestCaseStatus, RequestDefinitionStatus, RequestMethods } from '@/enums/apiEnum';

  import { casePriorityOptions, defaultResponse } from '@/views/api-test/components/config';
  import { parseRequestBodyFiles } from '@/views/api-test/components/utils';

  const props = defineProps<{
    apiDetail?: RequestParam | ApiDefinitionDetail;
  }>();
  const emit = defineEmits<{
    (e: 'loadCase', id?: string): void;
    (e: 'showDiff', apiDetailInfo: ApiCaseDetail): void;
  }>();

  const { t } = useI18n();
  const appStore = useAppStore();

  const innerVisible = defineModel<boolean>('visible', {
    default: false,
  });

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

  const description = computed<Description[]>(() => [
    {
      key: 'type',
      locale: 'apiTestManagement.apiType',
      value: apiDetailInfo.value.method,
    },
    {
      key: 'path',
      locale: 'apiTestManagement.path',
      value: apiDetailInfo.value.url || apiDetailInfo.value.path,
      tooltipPosition: 'tr',
    },
  ]);

  const environmentId = ref(appStore.currentEnvConfig?.id);

  watch(
    () => appStore.currentEnvConfig?.id,
    (val) => {
      environmentId.value = val;
    }
  );

  const formRef = ref<FormInstance>();
  const formNameRef = ref<FormInstance>();
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

  function requestTabClick() {
    const element = document
      .querySelector('.createAndEditCaseDrawer')
      ?.querySelectorAll('.request-tab-and-response')[0];
    element?.scrollTo({
      top: 0,
      behavior: 'smooth',
    });
  }

  async function open(apiId: string, record?: ApiCaseDetail | RequestParam, isCopy?: boolean, aiCreate?: boolean) {
    appStore.showLoading();
    apiDefinitionId.value = apiId;
    // 从api下的用例里打开抽屉有api信息，从case下直接复制没有api信息
    if (props.apiDetail) {
      apiDetailInfo.value = cloneDeep(props.apiDetail);
    } else {
      await getApiDetail();
    }
    const apiRequestInfo = apiDetailInfo.value.request || apiDetailInfo.value;
    if (!isCopy && !record && apiDetailInfo.value.protocol === 'HTTP') {
      // 创建用例需要复制文件
      let copyFilesMap: Record<string, any> = {};
      const fileIds = parseRequestBodyFiles(apiRequestInfo.body, [], [], []).uploadFileIds;
      if (fileIds.length > 0) {
        try {
          copyFilesMap = await definitionFileCopy({
            resourceId: apiDetailInfo.value.id as string,
            fileIds,
          });
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      }
      parseRequestBodyFiles(apiRequestInfo.body, [], [], [], copyFilesMap); // 替换请求文件 id
    }
    // 创建的时候，请求参数为接口定义的请求参数
    environmentId.value = appStore.currentEnvConfig?.id;
    detailForm.value = {
      ...cloneDeep(defaultDetail.value),
      ...(apiDetailInfo.value.protocol === 'HTTP'
        ? {
            headers: apiDetailInfo.value.headers ?? apiRequestInfo.headers,
            body: apiDetailInfo.value?.body ?? apiRequestInfo.body,
            rest: apiDetailInfo.value.rest ?? apiRequestInfo.rest,
            query: apiDetailInfo.value.query ?? apiRequestInfo.query,
            authConfig: apiDetailInfo.value.authConfig ?? apiRequestInfo.authConfig,
            otherConfig: apiDetailInfo.value.otherConfig ?? apiRequestInfo.otherConfig,
            url: apiDetailInfo.value.url ?? apiRequestInfo.url,
          }
        : apiDetailInfo.value),
      children: apiDetailInfo.value.children ?? apiRequestInfo.children,
      aiCreate,
    };
    // 复制
    if (isCopy) {
      if (record?.protocol === 'HTTP') {
        // 复制的用例需要复制文件
        let copyFilesMap: Record<string, any> = {};
        const fileIds = parseRequestBodyFiles(record.request.body, [], [], []).uploadFileIds;
        if (fileIds.length > 0) {
          try {
            copyFilesMap = await caseFileCopy({
              resourceId: record.id as string,
              fileIds,
            });
          } catch (error) {
            // eslint-disable-next-line no-console
            console.log(error);
          }
        }
        parseRequestBodyFiles(record.request.body, [], [], [], copyFilesMap); // 替换请求文件 id
        detailForm.value = {
          ...cloneDeep(record as RequestParam),
        };
      }
      detailForm.value.name = `copy_${record?.name}`;
      detailForm.value.isCopy = true;
      detailForm.value.aiCreate = false; // 复制的用例不是 AI 创建的
      environmentId.value = record?.environmentId ?? environmentId.value;
      if (detailForm.value.name.length > 255) {
        detailForm.value.name = detailForm.value.name.slice(0, 255);
      }
    }
    // 编辑
    if (!isCopy && record?.id) {
      isEdit.value = true;
      detailForm.value = cloneDeep(record as RequestParam);
      detailForm.value.protocol = apiDetailInfo.value.protocol; // 保持协议一致
      environmentId.value = record?.environmentId ?? environmentId.value;
      detailForm.value.isNew = false;
    } else if (aiCreate) {
      detailForm.value = cloneDeep({
        ...record,
        ...record?.request,
        priority: 'P0',
        status: RequestDefinitionStatus.PROCESSING,
      });
      detailForm.value.id = getGenerateId(); // AI 创建的用例没有 id
      detailForm.value.protocol = apiDetailInfo.value.protocol; // 保持协议一致
      environmentId.value = record?.environmentId ?? environmentId.value;
      detailForm.value.isNew = true;
      detailForm.value.aiCreate = true; // AI 创建的用例
    }
    appStore.hideLoading();
    innerVisible.value = true;
    await nextTick();
    requestCompositionRef.value?.changeVerticalExpand(false); // 响应内容默认折叠
  }

  function handleSaveCaseCancel() {
    drawerLoading.value = false;
    isEdit.value = false;
    innerVisible.value = false;
    formRef.value?.resetFields();
    formNameRef.value?.resetFields();
  }

  function handleDrawerConfirm(isContinue: boolean) {
    formNameRef.value?.validate(async (errors) => {
      if (!errors) {
        // 检查全部的校验信息
        if (requestCompositionRef.value?.getFlattenedMessages()?.length) {
          requestCompositionRef.value?.showMessage();
          return;
        }
        drawerLoading.value = true;
        // 给后端传的参数
        const requestParams = await requestCompositionRef.value?.makeRequestParams();
        if (!requestParams) return;
        const { linkFileIds, uploadFileIds, request, unLinkFileIds, deleteFileIds } = requestParams;
        const { name, priority, status, tags, id, aiCreate } = detailForm.value;
        const params: AddApiCaseParams = {
          projectId: appStore.currentProjectId,
          environmentId: environmentId.value as string,
          apiDefinitionId: apiDefinitionId.value,
          linkFileIds: linkFileIds || [],
          uploadFileIds: uploadFileIds || [],
          request,
          id: id as string,
          name,
          priority,
          status,
          tags,
          unLinkFileIds,
          deleteFileIds,
          aiCreate,
        } as AddApiCaseParams;
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
        detailForm.value.aiCreate = false;
        drawerLoading.value = false;
      }
    });
  }

  const { registerCatchSaveShortcut, removeCatchSaveShortcut } = useShortcutSave(() => {
    handleDrawerConfirm(false);
  });
  watch(
    () => innerVisible.value,
    (val) => {
      if (val) {
        registerCatchSaveShortcut();
      } else {
        removeCatchSaveShortcut();
      }
    }
  );

  const executeRef = ref<InstanceType<typeof executeButton>>();
  const reportId = ref('');
  const websocket = ref<WebSocket>();
  const temporaryResponseMap: Record<string, any> = {}; // 缓存websocket返回的报告内容，避免执行接口后切换tab导致报告丢失
  // 开启websocket监听，接收执行结果
  async function debugSocket(executeType?: 'localExec' | 'serverExec') {
    const { createSocket, websocket: _websocket } = useWebsocket({
      reportId: reportId.value,
      socketUrl: executeType === 'localExec' ? '/ws/debug' : '',
      host: executeType === 'localExec' ? executeRef.value?.localExecuteUrl : '',
      onMessage: (event) => {
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
          requestCompositionRef.value?.changeVerticalExpand(true);
        }
      },
    });
    await createSocket();
    websocket.value = _websocket.value;
  }
  async function handleExecute(executeType?: 'localExec' | 'serverExec') {
    try {
      detailForm.value.executeLoading = true;
      detailForm.value.response = cloneDeep(defaultResponse);
      const makeRequestParams = await requestCompositionRef.value?.makeRequestParams(executeType); // 写在reportId之前，防止覆盖reportId
      reportId.value = getGenerateId();
      detailForm.value.reportId = reportId.value; // 存储报告ID
      let res;
      if (!makeRequestParams) return;
      const params = {
        environmentId: environmentId.value as string,
        frontendDebug: executeType === 'localExec',
        reportId: reportId.value,
        apiDefinitionId: detailForm.value.apiDefinitionId,
        request: makeRequestParams?.request,
        linkFileIds: makeRequestParams?.linkFileIds,
        uploadFileIds: makeRequestParams?.uploadFileIds,
      };
      await debugSocket(executeType); // 开启websocket
      if (!(detailForm.value.id as string).startsWith('c') && executeType === 'serverExec') {
        // 已创建的服务端
        res = await runCase({
          id: detailForm.value.id as string,
          projectId: detailForm.value.projectId,
          ...params,
          uploadFileIds: params.uploadFileIds || [],
          linkFileIds: params.linkFileIds || [],
        });
      } else {
        res = await debugCase({
          id: `case-${Date.now()}`,
          projectId: appStore.currentProjectId,
          ...params,
          uploadFileIds: params.uploadFileIds || [],
          linkFileIds: params.linkFileIds || [],
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

  // 查看与定义不一致
  function showDiffDrawer() {
    emit('showDiff', detailForm.value as unknown as ApiCaseDetail);
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
    width: 300px;
  }
  :deep(.ms-detail-card-desc) {
    gap: 16px;
    justify-content: end;
    flex: 1;
    & > div {
      width: auto;
      max-width: 50%;
    }
  }
  :deep(.arco-form):nth-of-type(1) > .arco-form-item .arco-form-item-label-col {
    display: none;
  }
  .request-tab-and-response {
    overflow-x: hidden;
    overflow-y: auto;
    .ms-scroll-bar();
    :deep(.request-composition) {
      height: calc(100% - 74px); // 74: formItem第二行52+请求参数文案22
    }
  }
</style>
