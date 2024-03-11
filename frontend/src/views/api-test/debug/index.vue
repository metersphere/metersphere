<template>
  <!-- TODO:接口请求超过5S以上可展示取消请求按钮，避免用户过长等待 -->
  <MsCard :loading="loading" show-full-screen simple no-content-padding>
    <MsSplitBox :size="0.25" :max="0.5">
      <template #first>
        <moduleTree
          ref="moduleTreeRef"
          :active-node-id="activeDebug.id"
          @init="(val) => (folderTree = val)"
          @new-api="addDebugTab"
          @click-api-node="openApiTab"
          @import="importDrawerVisible = true"
          @rename-finish="handleRenameFinish"
          @delete-finish="handleDeleteFinish"
        />
      </template>
      <template #second>
        <div class="flex h-full flex-col">
          <div class="border-b border-[var(--color-text-n8)] p-[12px_18px]">
            <MsEditableTab
              v-model:active-tab="activeDebug"
              v-model:tabs="debugTabs"
              :limit="10"
              :readonly="!hasAnyPermission(['PROJECT_API_DEBUG:READ+ADD'])"
              at-least-one
              @add="addDebugTab"
            >
              <template #label="{ tab }">
                <apiMethodName :method="tab.protocol === 'HTTP' ? tab.method : tab.protocol" class="mr-[4px]" />
                {{ tab.label }}
              </template>
            </MsEditableTab>
          </div>
          <div class="flex-1 overflow-hidden">
            <debug
              v-model:detail-loading="loading"
              v-model:request="activeDebug"
              :module-tree="folderTree"
              :create-api="addDebug"
              :update-api="updateDebug"
              :execute-api="executeDebug"
              :local-execute-api="localExecuteApiDebug"
              :upload-temp-file-api="uploadTempFile"
              :file-save-as-source-id="activeDebug.id"
              :file-save-as-api="transferFile"
              :file-module-options-api="getTransferOptions"
              :permission-map="{
                execute: 'PROJECT_API_DEBUG:READ+EXECUTE',
                update: 'PROJECT_API_DEBUG:READ+UPDATE',
                create: 'PROJECT_API_DEBUG:READ+ADD',
              }"
              @add-done="handleDebugAddDone"
            />
          </div>
        </div>
      </template>
    </MsSplitBox>
  </MsCard>
  <MsDrawer
    v-model:visible="importDrawerVisible"
    :width="680"
    :ok-disabled="curlCode.trim() === ''"
    disabled-width-drag
    @cancel="curlCode = ''"
    @confirm="handleCurlImportConfirm"
  >
    <template #title>
      <a-tooltip position="right" :content="t('apiTestDebug.importByCURLTip')">
        {{ t('apiTestDebug.importByCURL') }}
        <icon-exclamation-circle
          class="ml-[4px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
          size="16"
        />
      </a-tooltip>
    </template>
    <div class="h-full">
      <MsCodeEditor
        v-if="importDrawerVisible"
        v-model:model-value="curlCode"
        theme="MS-text"
        height="100%"
        :language="LanguageEnum.PLAINTEXT"
        :show-theme-change="false"
        :show-full-screen="false"
      >
      </MsCodeEditor>
    </div>
  </MsDrawer>
</template>

<script lang="ts" setup>
  import { onBeforeRouteLeave } from 'vue-router';
  import { cloneDeep } from 'lodash-es';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsEditableTab from '@/components/pure/ms-editable-tab/index.vue';
  import { TabItem } from '@/components/pure/ms-editable-tab/types';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import moduleTree from './components/moduleTree.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import debug, { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';

  import { localExecuteApiDebug } from '@/api/modules/api-test/common';
  import {
    addDebug,
    executeDebug,
    getDebugDetail,
    getTransferOptions,
    transferFile,
    updateDebug,
    uploadTempFile,
  } from '@/api/modules/api-test/debug';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { parseCurlScript } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { ExecuteBody, RequestTaskResult } from '@/models/apiTest/common';
  import { ModuleTreeNode } from '@/models/common';
  import {
    RequestAuthType,
    RequestBodyFormat,
    RequestComposition,
    RequestContentTypeEnum,
    RequestMethods,
    RequestParamsType,
    ResponseComposition,
  } from '@/enums/apiEnum';

  import { parseRequestBodyFiles } from '../components/utils';

  const { t } = useI18n();
  const { openModal } = useModal();

  const moduleTreeRef = ref<InstanceType<typeof moduleTree>>();
  const folderTree = ref<ModuleTreeNode[]>([]);
  const importDrawerVisible = ref(false);
  const curlCode = ref('');
  const loading = ref(false);

  async function handleDebugAddDone() {
    await moduleTreeRef.value?.initModules();
    moduleTreeRef.value?.initModuleCount();
  }

  const initDefaultId = `debug-${Date.now()}`;
  const defaultBodyParams: ExecuteBody = {
    bodyType: RequestBodyFormat.NONE,
    formDataBody: {
      formValues: [],
    },
    wwwFormBody: {
      formValues: [],
    },
    jsonBody: {
      jsonValue: '',
    },
    xmlBody: { value: '' },
    binaryBody: {
      description: '',
      file: undefined,
    },
    rawBody: { value: '' },
  };
  const defaultResponse: RequestTaskResult = {
    requestResults: [
      {
        body: '',
        headers: '',
        url: '',
        method: '',
        responseResult: {
          body: '',
          contentType: '',
          headers: '',
          dnsLookupTime: 0,
          downloadTime: 0,
          latency: 0,
          responseCode: 0,
          responseTime: 0,
          responseSize: 0,
          socketInitTime: 0,
          sslHandshakeTime: 0,
          tcpHandshakeTime: 0,
          transferStartTime: 0,
        },
      },
    ],
    console: '',
  }; // 调试返回的响应内容
  const defaultDebugParams: RequestParam = {
    id: initDefaultId,
    moduleId: 'root',
    protocol: 'HTTP',
    url: '',
    activeTab: RequestComposition.HEADER,
    label: t('apiTestDebug.newApi'),
    closable: true,
    method: RequestMethods.GET,
    unSaved: false,
    headers: [],
    body: cloneDeep(defaultBodyParams),
    query: [],
    rest: [],
    polymorphicName: '',
    name: '',
    path: '',
    projectId: '',
    uploadFileIds: [],
    linkFileIds: [],
    authConfig: {
      authType: RequestAuthType.NONE,
      basicAuth: {
        userName: '',
        password: '',
      },
      digestAuth: {
        userName: '',
        password: '',
      },
    },
    children: [
      {
        polymorphicName: 'MsCommonElement', // 协议多态名称，写死MsCommonElement
        assertionConfig: {
          enableGlobal: false,
          assertions: [],
        },
        postProcessorConfig: {
          enableGlobal: false,
          processors: [],
        },
        preProcessorConfig: {
          enableGlobal: false,
          processors: [],
        },
      },
    ],
    otherConfig: {
      connectTimeout: 60000,
      responseTimeout: 60000,
      certificateAlias: '',
      followRedirects: true,
      autoRedirects: false,
    },
    responseActiveTab: ResponseComposition.BODY,
    response: cloneDeep(defaultResponse),
    isNew: true,
    executeLoading: false,
  };
  const debugTabs = ref<RequestParam[]>([cloneDeep(defaultDebugParams)]);
  const activeDebug = ref<RequestParam>(debugTabs.value[0]);

  function handleActiveDebugChange() {
    if (!loading.value) {
      // 如果是因为加载详情触发的change则不需要标记为未保存
      activeDebug.value.unSaved = true;
    }
  }

  function addDebugTab(defaultProps?: Partial<TabItem>) {
    const id = `debug-${Date.now()}`;
    debugTabs.value.push({
      ...cloneDeep(defaultDebugParams),
      id,
      isNew: !defaultProps?.id, // 新开的tab标记为前端新增的调试，因为此时都已经有id了；但是如果是查看打开的会有携带id
      ...defaultProps,
    });
    activeDebug.value = debugTabs.value[debugTabs.value.length - 1];
  }

  async function openApiTab(apiInfo: ModuleTreeNode) {
    const isLoadedTabIndex = debugTabs.value.findIndex((e) => e.id === apiInfo.id);
    if (isLoadedTabIndex > -1) {
      // 如果点击的请求在tab中已经存在，则直接切换到该tab
      activeDebug.value = debugTabs.value[isLoadedTabIndex];
      return;
    }
    try {
      loading.value = true;
      const res = await getDebugDetail(apiInfo.id);
      let parseRequestBodyResult;
      if (res.protocol === 'HTTP') {
        parseRequestBodyResult = parseRequestBodyFiles(res.request.body); // 解析请求体中的文件，将详情中的文件 id 集合收集，更新时以判断文件是否删除以及是否新上传的文件
      }
      addDebugTab({
        label: apiInfo.name,
        ...res,
        response: cloneDeep(defaultResponse),
        ...res.request,
        url: res.path,
        name: res.name, // request里面还有个name但是是null
        ...parseRequestBodyResult,
      });
      nextTick(() => {
        // 等待内容渲染出来再隐藏loading
        loading.value = false;
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      loading.value = false;
    }
  }

  function handleCurlImportConfirm() {
    const { url, headers, queryParameters } = parseCurlScript(curlCode.value);
    addDebugTab({
      url,
      headers: headers?.map((e) => ({
        contentType: RequestContentTypeEnum.TEXT,
        description: '',
        enable: true,
        ...e,
      })),
      query: queryParameters?.map((e) => ({
        paramType: RequestParamsType.STRING,
        description: '',
        required: false,
        maxLength: undefined,
        minLength: undefined,
        encode: false,
        enable: true,
        ...e,
      })),
    });
    curlCode.value = '';
    importDrawerVisible.value = false;
    nextTick(() => {
      handleActiveDebugChange();
    });
  }

  function handleRenameFinish(name: string, id: string) {
    debugTabs.value = debugTabs.value.map((tab) => {
      if (tab.id === id) {
        tab.label = name;
        tab.name = name;
      }
      return tab;
    });
  }

  function handleDeleteFinish(node: ModuleTreeNode) {
    let index;
    if (node.type === 'API') {
      // 如果是接口
      index = debugTabs.value.findIndex((tab) => tab.id === node.id);
    } else {
      // 如果是文件夹
      index = debugTabs.value.findIndex((tab) => tab.moduleId === node.id);
    }
    if (index > -1) {
      debugTabs.value.splice(index, 1);
      if (activeDebug.value.id === node.id) {
        // 如果查看的tab被删除了，则切换到第一个tab
        if (debugTabs.value.length > 0) {
          [activeDebug.value] = debugTabs.value;
        } else {
          addDebugTab();
        }
      }
    }
  }

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  let isLeaving = false;
  onBeforeRouteLeave((to, from, next) => {
    if (
      !isLeaving &&
      debugTabs.value.some((tab) => tab.unSaved) &&
      hasAnyPermission(['PROJECT_API_DEBUG:READ+ADD', 'PROJECT_API_DEBUG:READ+UPDATE'])
    ) {
      isLeaving = true;
      // 如果有未保存的调试则提示用户
      openModal({
        type: 'warning',
        title: t('common.tip'),
        content: t('apiTestDebug.unsavedLeave'),
        hideCancel: false,
        cancelText: t('common.stay'),
        okText: t('common.leave'),
        onBeforeOk: async () => {
          next();
        },
        onCancel: () => {
          isLeaving = false;
        },
      });
    } else {
      next();
    }
  });
</script>

<style lang="less" scoped></style>
