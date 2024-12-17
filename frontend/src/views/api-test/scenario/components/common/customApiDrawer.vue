<template>
  <div>
    <MsDrawer
      v-model:visible="visible"
      :width="960"
      class="customApiDrawer"
      no-content-padding
      :show-continue="true"
      :footer="requestVModel.isNew === true"
      :ok-disabled="requestVModel.executeLoading || (isHttpProtocol && !requestVModel.url) || loading"
      :handle-before-cancel="handleBeforeCancel"
      show-full-screen
      unmount-on-close
      @confirm="handleSave"
      @continue="handleContinue"
      @close="handleClose"
    >
      <template #title>
        <div class="flex flex-1 items-center gap-[8px] overflow-hidden">
          <div
            v-if="props.step"
            class="flex h-[16px] min-w-[16px] items-center justify-center rounded-full bg-[var(--color-text-brand)] pr-[2px] !text-white"
          >
            {{ props.step.sort }}
          </div>
          <stepTypeVue
            v-if="props.step && [ScenarioStepType.API, ScenarioStepType.CUSTOM_REQUEST].includes(props.step?.stepType)"
            :step="props.step"
          />
          <div v-if="!isShowEditStepNameInput" class="flex flex-1 items-center gap-[4px] overflow-hidden">
            <a-tooltip :content="title" position="bottom">
              <div class="one-line-text">
                {{ title }}
              </div>
              <MsIcon
                v-if="!props.step || !props.step.isQuoteScenarioStep"
                type="icon-icon_edit_outlined"
                class="min-w-[16px] cursor-pointer hover:text-[rgb(var(--primary-5))]"
                @click="isShowEditStepNameInput = true"
              />
            </a-tooltip>
          </div>
          <a-input
            v-if="isShowEditStepNameInput"
            v-model:model-value="requestVModel.stepName"
            class="flex-1"
            :placeholder="t('apiScenario.pleaseInputStepName')"
            :max-length="255"
            show-word-limit
            @press-enter="updateStepName"
            @blur="updateStepName"
          />
        </div>
        <div v-show="!isShowEditStepNameInput" class="ml-auto flex items-center gap-[16px]">
          <div
            v-if="!props.step || props.step?.stepType === ScenarioStepType.CUSTOM_REQUEST"
            class="customApiDrawer-title-right flex items-center gap-[16px]"
          >
            <a-tooltip :content="appStore.currentEnvConfig?.name" :disabled="!appStore.currentEnvConfig?.name">
              <div class="one-line-text max-w-[250px] text-[14px] font-normal text-[var(--color-text-4)]">
                {{ t('apiScenario.env', { name: appStore.currentEnvConfig?.name }) }}
              </div>
            </a-tooltip>
            <a-select
              v-model:model-value="requestVModel.customizeRequestEnvEnable"
              class="w-[150px]"
              :disabled="props.step?.isQuoteScenarioStep"
              @change="handleUseEnvChange"
            >
              <template #prefix>
                <div> {{ t('project.environmental.env') }} </div>
              </template>
              <a-option :value="true">{{ t('common.quote') }}</a-option>
              <a-option :value="false">{{ t('common.notQuote') }}</a-option>
            </a-select>
          </div>
          <div
            v-if="props.step && !props.step.isQuoteScenarioStep"
            class="right-operation-button-icon ml-auto flex items-center"
          >
            <replaceButton
              v-if="props.step.resourceId && props.step?.stepType !== ScenarioStepType.CUSTOM_REQUEST"
              :steps="props.steps"
              :step="props.step"
              :resource-id="props.step.resourceId"
              :scenario-id="scenarioId"
              @replace="handleReplace"
            />
            <MsButton type="icon" status="secondary" class="mr-4" @click="emit('deleteStep')">
              <MsIcon type="icon-icon_delete-trash_outlined1" />
              {{ t('common.delete') }}
            </MsButton>
          </div>
        </div>
      </template>
      <a-empty
        v-if="pluginError && !isHttpProtocol"
        :description="t('apiTestDebug.noPlugin')"
        class="h-[200px] items-center justify-center"
      >
        <template #image>
          <MsIcon type="icon-icon_plugin_outlined" size="48" />
        </template>
      </a-empty>
      <div v-show="!pluginError || isHttpProtocol" class="flex h-full flex-col">
        <div class="flex flex-wrap items-center justify-between gap-[12px] px-[18px] pt-[8px]">
          <div class="flex flex-1 items-center gap-[16px]">
            <a-select
              v-if="requestVModel.isNew"
              v-model:model-value="requestVModel.protocol"
              :loading="protocolLoading"
              :disabled="_stepType.isQuoteApi || props.step?.isQuoteScenarioStep"
              class="w-[90px]"
            >
              <a-tooltip
                v-for="item of protocolOptions"
                :key="item.value as string"
                :content="item.label"
                :mouse-enter-delay="300"
              >
                <a-option :value="item.value">
                  {{ item.label }}
                </a-option>
              </a-tooltip>
            </a-select>
            <div v-else class="flex items-center gap-[4px]">
              <apiMethodName
                :method="(requestVModel.protocol as RequestMethods)"
                tag-background-color="rgb(var(--link-7))"
                tag-text-color="white"
                is-tag
                class="flex items-center"
              />
              <a-tooltip v-if="!isHttpProtocol" :content="requestVModel.name" :mouse-enter-delay="500">
                <div class="one-line-text max-w-[350px]"> {{ requestVModel.name }}</div>
              </a-tooltip>
            </div>
            <a-input-group v-if="isHttpProtocol" class="flex-1">
              <apiMethodSelect
                v-model:model-value="requestVModel.method"
                class="w-[140px]"
                :disabled="_stepType.isQuoteApi || props.step?.isQuoteScenarioStep"
                @change="handleActiveDebugChange"
              />
              <a-input
                v-model:model-value="requestVModel.url"
                :max-length="255"
                :placeholder="showEnvPrefix ? t('apiScenario.pleaseInputUrl') : t('apiTestDebug.urlPlaceholder')"
                allow-clear
                class="hover:z-10"
                :style="isUrlError ? 'border: 1px solid rgb(var(--danger-6);z-index: 10' : ''"
                :disabled="_stepType.isQuoteApi || props.step?.isQuoteScenarioStep"
                @input="() => (isUrlError = false)"
                @change="handleUrlChange"
              >
                <template v-if="showEnvPrefix" #prefix>
                  {{ (appStore.currentEnvConfig as EnvConfig)?.httpConfig.find((e) => e.type === 'NONE')?.url }}
                </template>
                <template #suffix>
                  <MsIcon
                    type="icon-icon_curl"
                    :size="18"
                    class="cursor-pointer hover:text-[rgb(var(--primary-5))]"
                    @click="importDialogVisible = true"
                  />
                </template>
              </a-input>
            </a-input-group>
          </div>
          <div v-permission="[props.permissionMap?.execute]">
            <template v-if="hasLocalExec">
              <a-dropdown-button
                v-if="!requestVModel.executeLoading"
                :disabled="requestVModel.executeLoading || (isHttpProtocol && !requestVModel.url)"
                class="exec-btn"
                @click="() => execute(isPriorityLocalExec ? 'localExec' : 'serverExec')"
                @select="execute"
              >
                {{ isPriorityLocalExec ? t('apiTestDebug.localExec') : t('apiTestDebug.serverExec') }}
                <template #icon>
                  <icon-down />
                </template>
                <template #content>
                  <a-doption :value="isPriorityLocalExec ? 'serverExec' : 'localExec'">
                    {{ isPriorityLocalExec ? t('apiTestDebug.serverExec') : t('apiTestDebug.localExec') }}
                  </a-doption>
                </template>
              </a-dropdown-button>
              <a-button v-else type="primary" class="mr-[12px]" @click="stopDebug">
                {{ t('common.stop') }}
              </a-button>
            </template>
            <a-button
              v-else-if="!requestVModel.executeLoading"
              class="mr-[12px]"
              type="primary"
              @click="() => execute('serverExec')"
            >
              {{ t('apiTestDebug.serverExec') }}
            </a-button>
            <a-button v-else type="primary" class="mr-[12px]" @click="stopDebug">
              {{ t('common.stop') }}
            </a-button>
          </div>
        </div>
        <div class="request-tab-and-response flex-1">
          <div class="px-[18px]">
            <a-input
              v-if="props.step?.stepType && _stepType.isQuoteApi && isHttpProtocol"
              v-model:model-value="requestVModel.name"
              :max-length="255"
              :placeholder="t('apiTestManagement.apiNamePlaceholder')"
              :disabled="!isEditableApi"
              allow-clear
              class="my-[8px]"
            />
          </div>
          <MsTab
            v-if="requestVModel.activeTab"
            v-model:active-key="requestVModel.activeTab"
            :content-tab-list="contentTabList"
            :get-text-func="getTabBadge"
            class="sticky-content no-content relative top-0 mx-[16px] border-b"
            @tab-click="requestTabClick"
          />
          <div :class="`request-content-and-response ${activeLayout}`">
            <a-spin class="request block h-full w-full" :loading="requestVModel.executeLoading || loading">
              <div class="request-tab-pane flex flex-col p-[16px]">
                <a-spin
                  v-show="requestVModel.activeTab === RequestComposition.PLUGIN"
                  :loading="pluginLoading"
                  class="min-h-[100px] w-full"
                >
                  <MsFormCreate
                    v-model:api="fApi"
                    :rule="currentPluginScript"
                    :option="currentPluginOptions"
                    @change="
                      () => {
                        if (isInitPluginForm) {
                          handlePluginFormChange();
                        }
                      }
                    "
                  />
                </a-spin>
                <httpHeader
                  v-if="requestVModel.activeTab === RequestComposition.HEADER"
                  v-model:params="requestVModel.headers"
                  :disabled-param-value="!isEditableApi && !isEditableParamValue"
                  :disabled-except-param="!isEditableApi"
                  :layout="activeLayout"
                  @change="handleActiveDebugChange"
                />
                <httpBody
                  v-else-if="requestVModel.activeTab === RequestComposition.BODY"
                  v-model:params="requestVModel.body"
                  :disabled-param-value="!isEditableApi && !isEditableParamValue"
                  :disabled-except-param="!isEditableApi"
                  :disabled-body-type="!isEditableApi"
                  :upload-temp-file-api="uploadTempFile"
                  :file-save-as-source-id="props.step?.id"
                  :file-save-as-api="stepTransferFile"
                  :file-module-options-api="getTransferOptions"
                  @change="handleActiveDebugChange"
                />
                <httpQuery
                  v-else-if="requestVModel.activeTab === RequestComposition.QUERY"
                  v-model:params="requestVModel.query"
                  :disabled-param-value="!isEditableApi && !isEditableParamValue"
                  :disabled-except-param="!isEditableApi"
                  @change="handleActiveDebugChange"
                />
                <httpRest
                  v-else-if="requestVModel.activeTab === RequestComposition.REST"
                  v-model:params="requestVModel.rest"
                  :disabled-param-value="!isEditableApi && !isEditableParamValue"
                  :disabled-except-param="!isEditableApi"
                  @change="handleActiveDebugChange"
                />
                <precondition
                  v-else-if="requestVModel.activeTab === RequestComposition.PRECONDITION"
                  v-model:config="requestVModel.children[0].preProcessorConfig"
                  is-definition
                  :disabled="!isEditableApi"
                  :tip-content="t('apiScenario.openGlobalPreConditionTip')"
                  @change="handleActiveDebugChange"
                />
                <postcondition
                  v-else-if="requestVModel.activeTab === RequestComposition.POST_CONDITION"
                  v-model:config="requestVModel.children[0].postProcessorConfig"
                  :response="responseResultBody"
                  :disabled="!isEditableApi"
                  :tip-content="t('apiScenario.openGlobalPostConditionTip')"
                  is-definition
                  @change="handleActiveDebugChange"
                />
                <assertion
                  v-else-if="requestVModel.activeTab === RequestComposition.ASSERTION"
                  v-model:params="requestVModel.children[0].assertionConfig.assertions"
                  :response="responseResultBody"
                  is-definition
                  :disabled="!isEditableApi"
                  :assertion-config="requestVModel.children[0].assertionConfig"
                  :show-extraction="true"
                />
                <auth
                  v-else-if="requestVModel.activeTab === RequestComposition.AUTH"
                  v-model:params="requestVModel.authConfig"
                  :disabled="!isEditableApi"
                  @change="handleActiveDebugChange"
                />
                <setting
                  v-else-if="requestVModel.activeTab === RequestComposition.SETTING"
                  v-model:params="requestVModel.otherConfig"
                  :disabled="!isEditableApi"
                  @change="handleActiveDebugChange"
                />
              </div>
            </a-spin>

            <response
              v-if="visible"
              v-show="showResponse"
              ref="responseRef"
              v-model:active-layout="activeLayout"
              v-model:active-tab="requestVModel.responseActiveTab"
              class="response"
              :is-http-protocol="isHttpProtocol"
              :is-priority-local-exec="isPriorityLocalExec"
              :request-url="requestVModel.url"
              :is-expanded="isVerticalExpanded"
              :request-result="currentResponse"
              :console="currentResponse?.console"
              :is-edit="false"
              is-definition
              hide-layout-switch
              :loading="requestVModel.executeLoading || loading"
              @execute="execute"
            >
              <template #titleRight>
                <loopPagination v-model:current-loop="currentLoop" :loop-total="loopTotal" />
              </template>
            </response>
          </div>
        </div>
      </div>
      <!-- <addDependencyDrawer v-model:visible="showAddDependencyDrawer" :mode="addDependencyMode" /> -->
    </MsDrawer>
    <importCurlDialog v-model:visible="importDialogVisible" @done="handleImportCurlDone" />
  </div>
</template>

<script setup lang="ts">
  import { Message, SelectOptionData } from '@arco-design/web-vue';
  import { cloneDeep, debounce } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsFormCreate from '@/components/pure/ms-form-create/formCreate.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTab from '@/components/pure/ms-tab/index.vue';
  import assertion from '@/components/business/ms-assertion/index.vue';
  import loopPagination from './loopPagination.vue';
  import replaceButton from './replaceButton.vue';
  import stepTypeVue from './stepType/stepType.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import apiMethodSelect from '@/views/api-test/components/apiMethodSelect.vue';
  import importCurlDialog from '@/views/api-test/components/importCurlDialog.vue';
  import auth from '@/views/api-test/components/requestComposition/auth.vue';
  import { TabErrorMessage } from '@/views/api-test/components/requestComposition/index.vue';
  import postcondition from '@/views/api-test/components/requestComposition/postcondition.vue';
  import precondition from '@/views/api-test/components/requestComposition/precondition.vue';
  import setting from '@/views/api-test/components/requestComposition/setting.vue';

  import { getPluginScript, getProtocolList } from '@/api/modules/api-test/common';
  import { getDefinitionDetail } from '@/api/modules/api-test/management';
  import {
    getTransferOptions,
    scenarioCopyStepFiles,
    stepTransferFile,
    uploadTempFile,
  } from '@/api/modules/api-test/scenario';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import { getGenerateId, parseQueryParams } from '@/utils';
  import { scrollIntoView } from '@/utils/dom';
  import { getLocalStorage, setLocalStorage } from '@/utils/local-storage';

  import {
    CurlParseResult,
    EnableKeyValueParam,
    ExecuteApiRequestFullParams,
    ExecuteBody,
    ExecutePluginRequestParams,
    ExecuteRequestCommonParam,
    ExecuteRequestFormBody,
    PluginConfig,
    RequestResult,
    RequestTaskResult,
  } from '@/models/apiTest/common';
  import { ScenarioStepFileParams, ScenarioStepItem } from '@/models/apiTest/scenario';
  import type { EnvConfig } from '@/models/projectManagement/environmental';
  import {
    ProtocolKeyEnum,
    RequestAuthType,
    RequestBodyFormat,
    RequestComposition,
    RequestMethods,
    ResponseComposition,
    ScenarioStepRefType,
    ScenarioStepType,
  } from '@/enums/apiEnum';

  import getStepType from './stepType/utils';
  import {
    defaultBodyParams,
    defaultBodyParamsItem,
    defaultHeaderParamsItem,
    defaultRequestParamsItem,
    defaultResponse,
  } from '@/views/api-test/components/config';
  import {
    filterAssertions,
    filterConditionsSqlValidParams,
    filterKeyValParams,
    parseCurlBody,
    parseRequestBodyFiles,
  } from '@/views/api-test/components/utils';
  import type { Api } from '@form-create/arco-design';

  // 懒加载Http协议组件
  const httpHeader = defineAsyncComponent(() => import('@/views/api-test/components/requestComposition/header.vue'));
  const httpBody = defineAsyncComponent(() => import('@/views/api-test/components/requestComposition/body.vue'));
  const httpQuery = defineAsyncComponent(() => import('@/views/api-test/components/requestComposition/query.vue'));
  const httpRest = defineAsyncComponent(() => import('@/views/api-test/components/requestComposition/rest.vue'));
  const response = defineAsyncComponent(
    () => import('@/views/api-test/components/requestComposition/response/index.vue')
  );
  // const addDependencyDrawer = defineAsyncComponent(
  //   () => import('@/views/api-test/management/components/addDependencyDrawer.vue')
  // );

  export interface RequestCustomAttr {
    type?: 'api';
    label: string;
    name: string;
    stepId: string | number; // 所属步骤 id
    uniqueId: string | number; // 前端生成的唯一 id
    stepName: string; // 所属步骤名称
    resourceId: string | number; // 引用、复制的资源 id
    isNew: boolean;
    protocol: string;
    activeTab: RequestComposition;
    executeLoading: boolean; // 执行中loading
    isCopy?: boolean; // 是否是复制
    isExecute?: boolean; // 是否是执行
    responseActiveTab: ResponseComposition;
    unSaved: boolean;
    uploadFileIds: string[];
    linkFileIds: string[];
    deleteFileIds?: string[];
    unLinkFileIds?: string[];
    errorMessageInfo?: {
      [key: string]: Record<string, any>;
    };
  }

  export type RequestParam = (ExecuteApiRequestFullParams | ExecutePluginRequestParams) & {
    response?: RequestTaskResult;
    customizeRequest?: boolean;
    customizeRequestEnvEnable?: boolean;
  } & RequestCustomAttr;

  const props = defineProps<{
    request?: RequestParam; // 请求参数集合
    step?: ScenarioStepItem;
    steps: ScenarioStepItem[];
    detailLoading?: boolean; // 详情加载状态
    permissionMap?: {
      execute: string;
    };
    stepResponses?: Record<string | number, RequestResult[]>;
    fileParams?: ScenarioStepFileParams;
  }>();

  const emit = defineEmits<{
    (e: 'addStep', request: RequestParam): void;
    (e: 'applyStep', request: RequestParam): void;
    (e: 'execute', request: RequestParam, executeType?: 'localExec' | 'serverExec'): void;
    (e: 'stopDebug'): void;
    (e: 'deleteStep'): void;
    (e: 'replace', newStep: ScenarioStepItem): void;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();

  // 注入祖先组件提供的属性
  const scenarioId = inject<string | number>('scenarioId');
  const hasLocalExec = inject<Ref<boolean>>('hasLocalExec');
  const isPriorityLocalExec = inject<Ref<boolean>>('isPriorityLocalExec');

  const visible = defineModel<boolean>('visible', { required: true });
  const loading = defineModel<boolean>('detailLoading', { default: false });

  const defaultApiParams: RequestParam = {
    label: '',
    name: '',
    type: 'api',
    stepId: '',
    uniqueId: '',
    stepName: '',
    resourceId: '',
    customizeRequest: true,
    customizeRequestEnvEnable: true,
    protocol: 'HTTP',
    url: '',
    activeTab: RequestComposition.HEADER,
    method: RequestMethods.GET,
    unSaved: false,
    headers: [],
    body: cloneDeep(defaultBodyParams),
    query: [],
    rest: [],
    polymorphicName: '',
    path: '',
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
    response: cloneDeep(defaultResponse),
    responseActiveTab: ResponseComposition.BODY,
    isNew: true,
    executeLoading: false,
    errorMessageInfo: {},
  };

  const requestVModel = ref<RequestParam>(defaultApiParams);
  const copyStepFileIdsMap = ref<Record<string, any>>({});
  // 步骤类型判断
  const _stepType = computed(() => {
    if (props.step) {
      return getStepType(props.step);
    }
    return {
      isCopyApi: false,
      isQuoteApi: false,
    };
  });
  // 抽屉标题
  const title = computed(() => {
    if (
      _stepType.value.isCopyApi ||
      _stepType.value.isQuoteApi ||
      props.step?.stepType === ScenarioStepType.CUSTOM_REQUEST
    ) {
      return requestVModel.value.stepName || requestVModel.value.name || props.step?.name;
    }
    return requestVModel.value.stepName || requestVModel.value.name || t('apiScenario.customApi');
  });
  // 是否显示环境域名前缀
  const showEnvPrefix = computed(
    () =>
      requestVModel.value.customizeRequest &&
      requestVModel.value.customizeRequestEnvEnable &&
      appStore.currentEnvConfig?.httpConfig.find((e) => e.type === 'NONE')?.url
  );
  const currentLoop = ref(1);
  const currentResponse = computed(() => {
    if (requestVModel.value.uniqueId === props.step?.uniqueId && props.step?.uniqueId) {
      // 判断当前步骤 id 与传入的激活步骤 id 是否一致，避免在操作插入步骤时带入的是其他步骤的响应内容
      return props.stepResponses?.[props.step?.uniqueId]?.[currentLoop.value - 1];
    }
  });
  const loopTotal = computed(() => (props.step?.uniqueId && props.stepResponses?.[props.step?.uniqueId]?.length) || 0);
  // 执行响应结果 body 部分
  const responseResultBody = computed(() => {
    return currentResponse.value?.responseResult.body;
  });

  watch(
    () => props.stepResponses,
    (val) => {
      if (val && val[requestVModel.value.uniqueId]) {
        requestVModel.value.executeLoading = false;
      }
    },
    {
      deep: true,
    }
  );

  function requestTabClick() {
    const element = document.querySelector('.customApiDrawer')?.querySelectorAll('.request-tab-and-response')[0];
    element?.scrollTo({
      top: 0,
      behavior: 'smooth',
    });
  }

  // 复制 api 只要加载过一次后就会保存，所以 props.request 是不为空的
  const isCopyApiNeedInit = computed(() => _stepType.value.isCopyApi && props.request === undefined);
  // 全可编辑接口
  const isEditableApi = computed(
    () =>
      !props.step?.isQuoteScenarioStep &&
      (_stepType.value.isCopyApi || props.step?.stepType === ScenarioStepType.CUSTOM_REQUEST || !props.step)
  );
  // 非引用场景下的引用 api只可更改参数值接口
  const isEditableParamValue = computed(() => !props.step?.isQuoteScenarioStep && _stepType.value.isQuoteApi);
  // 是否是 HTTP 协议
  const isHttpProtocol = computed(() => requestVModel.value.protocol === 'HTTP');
  const isInitPluginForm = ref(false);
  const isSwitchingContent = ref(false); // 是否正在切换请求内容，当传入的详情数据变化时记录，避免触发未保存

  function handleActiveDebugChange() {
    if ((!isSwitchingContent.value && !loading.value) || (!isHttpProtocol.value && isInitPluginForm.value)) {
      // 如果是因为加载详情触发的change则不需要标记为未保存；或者是插件协议的话需要等待表单初始化完毕
      requestVModel.value.unSaved = true;
    }
  }

  const isShowEditStepNameInput = ref(false);

  function updateStepName() {
    isShowEditStepNameInput.value = false;
  }

  // 请求内容公共tabKey
  const commonContentTabKey = [
    RequestComposition.PRECONDITION,
    RequestComposition.POST_CONDITION,
    RequestComposition.ASSERTION,
  ];
  // 请求内容插件tab
  const pluginContentTab = [
    {
      value: RequestComposition.PLUGIN,
      label: t('apiTestDebug.pluginData'),
    },
  ];
  // Http 请求的tab
  const httpContentTabList = [
    {
      value: RequestComposition.HEADER,
      label: t('apiTestDebug.header'),
    },
    {
      value: RequestComposition.BODY,
      label: t('apiTestDebug.body'),
    },
    {
      value: RequestComposition.QUERY,
      label: 'Query',
    },
    {
      value: RequestComposition.REST,
      label: RequestComposition.REST,
    },
    {
      value: RequestComposition.PRECONDITION,
      label: t('apiTestDebug.prefix'),
    },
    {
      value: RequestComposition.POST_CONDITION,
      label: t('apiTestDebug.post'),
    },
    {
      value: RequestComposition.ASSERTION,
      label: t('apiTestDebug.assertion'),
    },
    {
      value: RequestComposition.AUTH,
      label: t('apiTestDebug.auth'),
    },
    {
      value: RequestComposition.SETTING,
      label: t('apiTestDebug.setting'),
    },
  ];
  const headerNum = computed(
    () => filterKeyValParams(requestVModel.value?.headers ?? [], defaultHeaderParamsItem).validParams?.length
  );
  const restNum = computed(
    () => filterKeyValParams(requestVModel.value?.rest ?? [], defaultRequestParamsItem).validParams?.length
  );
  const queryNum = computed(
    () => filterKeyValParams(requestVModel.value?.query ?? [], defaultRequestParamsItem).validParams?.length
  );
  const preProcessorNum = computed(() => requestVModel.value.children[0].preProcessorConfig.processors.length);
  const postProcessorNum = computed(() => requestVModel.value.children[0].postProcessorConfig.processors.length);
  const assertionsNum = computed(() => requestVModel.value.children[0].assertionConfig.assertions.length);
  // 根据协议类型获取请求内容tab
  const contentTabList = computed(() => {
    // HTTP 协议 tabs
    if (isHttpProtocol.value) {
      // 引用API：请求头、query、rest、前后置、断言。如果没有数据直接隐藏tab
      if (!isEditableApi.value) {
        return httpContentTabList.filter(
          (item) =>
            !(!restNum.value && item.value === RequestComposition.REST) &&
            !(!queryNum.value && item.value === RequestComposition.QUERY) &&
            !(!headerNum.value && item.value === RequestComposition.HEADER) &&
            !(!preProcessorNum.value && item.value === RequestComposition.PRECONDITION) &&
            !(!postProcessorNum.value && item.value === RequestComposition.POST_CONDITION) &&
            !(!assertionsNum.value && item.value === RequestComposition.ASSERTION)
        );
      }
      return httpContentTabList;
    }
    if (!isEditableApi.value) {
      return [...pluginContentTab, ...httpContentTabList.filter((e) => commonContentTabKey.includes(e.value))].filter(
        (item) =>
          !(!preProcessorNum.value && item.value === RequestComposition.PRECONDITION) &&
          !(!postProcessorNum.value && item.value === RequestComposition.POST_CONDITION) &&
          !(!assertionsNum.value && item.value === RequestComposition.ASSERTION)
      );
    }
    return [...pluginContentTab, ...httpContentTabList.filter((e) => commonContentTabKey.includes(e.value))];
  });

  /**
   * 获取 tab 的参数数量徽标
   */
  function getTabBadge(tabKey: RequestComposition) {
    switch (tabKey) {
      case RequestComposition.HEADER:
        return `${headerNum.value > 0 ? headerNum.value : ''}`;
      case RequestComposition.BODY:
        return requestVModel.value.body?.bodyType !== RequestBodyFormat.NONE ? '1' : '';
      case RequestComposition.QUERY:
        return `${queryNum.value > 0 ? queryNum.value : ''}`;
      case RequestComposition.REST:
        return `${restNum.value > 0 ? restNum.value : ''}`;
      case RequestComposition.PRECONDITION:
        return `${preProcessorNum.value > 99 ? '99+' : preProcessorNum.value || ''}`;
      case RequestComposition.POST_CONDITION:
        return `${postProcessorNum.value > 99 ? '99+' : postProcessorNum.value || ''}`;
      case RequestComposition.ASSERTION:
        return `${assertionsNum.value > 99 ? '99+' : assertionsNum.value || ''}`;
      case RequestComposition.AUTH:
        return requestVModel.value.authConfig?.authType !== RequestAuthType.NONE ? '1' : '';
      default:
        return '';
    }
  }

  const protocolLoading = ref(false);
  const protocolOptions = ref<SelectOptionData[]>([]);

  async function initProtocolList() {
    try {
      protocolLoading.value = true;
      const res = await getProtocolList(appStore.currentOrgId);
      protocolOptions.value = res.map((e) => ({
        label: e.protocol,
        value: e.protocol,
        polymorphicName: e.polymorphicName,
        pluginId: e.pluginId,
      }));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      protocolLoading.value = false;
    }
  }

  const pluginScriptMap = ref<Record<string, PluginConfig>>({}); // 存储初始化过后的插件配置
  const temporaryPluginFormMap: Record<string, any> = {}; // 缓存插件表单，避免切换传入的 API 数据导致动态表单数据丢失
  const pluginLoading = ref(false);
  const fApi = ref<Api>();
  const currentPluginOptions = computed<Record<string, any>>(
    () => pluginScriptMap.value[requestVModel.value.protocol]?.options || {}
  );
  const currentPluginScript = computed<Record<string, any>[]>(
    () => pluginScriptMap.value[requestVModel.value.protocol]?.script || []
  );

  /**
   * 控制插件表单字段显示
   */
  function controlPluginFormFields() {
    const currentFormFields = fApi.value?.fields();
    let fields: string[] = [];
    if (requestVModel.value.customizeRequestEnvEnable || _stepType.value.isQuoteApi) {
      // 自定义请求引用环境或是引用的 api 时，使用环境的字段显示
      fields = pluginScriptMap.value[requestVModel.value.protocol].apiDefinitionFields || [];
    } else {
      fields = pluginScriptMap.value[requestVModel.value.protocol].apiDebugFields || [];
    }
    // 确保fields展示完整
    fApi.value?.hidden(false, fields);
    if (currentFormFields && currentFormFields.length < fields.length) {
      fApi.value?.hidden(true, currentFormFields?.filter((e) => !fields.includes(e)) || []);
    } else {
      // 隐藏多余的字段
      fApi.value?.hidden(true, currentFormFields?.filter((e) => !fields.includes(e)) || []);
    }
    fApi.value?.refresh(); // 刷新表单，避免字段显隐切换后部分字段不显示
    return fields;
  }

  // 处理插件表单输入框变化
  const handlePluginFormChange = debounce(() => {
    if (isEditableApi.value) {
      // 复制或者新建的时候需要缓存表单数据，引用的不能更改
      temporaryPluginFormMap[requestVModel.value.uniqueId] = fApi.value?.formData();
      controlPluginFormFields(); // TODO:临时解决插件表单通过表单交互控制字段显隐未隐藏/显示环境字段的问题
    }
    handleActiveDebugChange();
  }, 300);

  /**
   * 设置插件表单数据
   */
  function setPluginFormData() {
    const tempForm = temporaryPluginFormMap[requestVModel.value.uniqueId];
    if (tempForm || !requestVModel.value.isNew) {
      // 如果缓存的表单数据存在或者是编辑状态，则需要将之前的输入数据填充
      const formData = isEditableApi.value ? tempForm || requestVModel.value : requestVModel.value;
      nextTick(() => {
        if (fApi.value) {
          fApi.value.nextRefresh(() => {
            const form: Record<string, any> = {};
            controlPluginFormFields().forEach((key) => {
              form[key] = formData[key];
            });
            fApi.value?.setValue(cloneDeep(form));
            setTimeout(() => {
              // 初始化时赋值会触发表单数据变更，300ms 是为了与 handlePluginFormChange的防抖时间保持一致
              isInitPluginForm.value = true;
            }, 300);
          });
        }
      });
    } else {
      nextTick(() => {
        controlPluginFormFields();
        fApi.value?.clearValidateState();
        fApi.value?.resetFields();
        isInitPluginForm.value = true;
      });
    }
  }

  // 切换是否使用环境变量
  function handleUseEnvChange() {
    if (!isHttpProtocol.value) {
      controlPluginFormFields();
    }
  }

  const pluginError = ref(false);

  async function initPluginScript() {
    const pluginId = protocolOptions.value.find((e) => e.value === requestVModel.value.protocol)?.pluginId;
    if (!pluginId) {
      Message.warning(t('apiTestDebug.noPluginTip'));
      pluginError.value = true;
      return;
    }
    pluginError.value = false;
    isInitPluginForm.value = false;
    if (pluginScriptMap.value[requestVModel.value.protocol] !== undefined) {
      setPluginFormData();
      // 已经初始化过
      return;
    }
    try {
      pluginLoading.value = true;
      const res = await getPluginScript(pluginId);
      pluginScriptMap.value[requestVModel.value.protocol] = res;
      setPluginFormData();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      pluginLoading.value = false;
    }
  }

  /**
   * 处理协议切换，非 HTTP 协议切换到插件协议时需要初始化插件表单
   */
  function handleActiveDebugProtocolChange(val: string) {
    if (val !== 'HTTP') {
      requestVModel.value.activeTab = RequestComposition.PLUGIN;
      initPluginScript();
    } else {
      requestVModel.value.activeTab = RequestComposition.HEADER;
      if (!Object.values(RequestMethods).includes(requestVModel.value.method as RequestMethods)) {
        // 第三方插件协议切换到 HTTP 时，请求方法默认设置 GET
        requestVModel.value.method = RequestMethods.GET;
      }
    }
    handleActiveDebugChange();
  }

  /**
   *  处理url输入框变化，解析成参数表格
   */
  function handleUrlChange(val: string) {
    const params = parseQueryParams(val.trim());
    if (params.length > 0) {
      requestVModel.value.query = [
        ...params.map((e, i) => ({
          id: (new Date().getTime() + i).toString(),
          ...defaultRequestParamsItem,
          ...e,
        })),
        cloneDeep(defaultRequestParamsItem),
      ];
      requestVModel.value.activeTab = RequestComposition.QUERY;
      [requestVModel.value.url] = val.split('?');
    }
    handleActiveDebugChange();
  }

  const showResponse = computed(
    () => isHttpProtocol.value || requestVModel.value.response?.requestResults[0]?.responseResult
  );
  const activeLayout = ref<'horizontal' | 'vertical'>('vertical');
  const isVerticalExpanded = computed(() => activeLayout.value === 'vertical');
  const responseRef = ref<InstanceType<typeof response>>();

  function changeVerticalExpand(val: boolean) {
    responseRef.value?.changeExpand(val);
  }
  watch(
    () => showResponse.value,
    (val) => {
      nextTick(() => {
        if (val) {
          changeVerticalExpand(true);
        } else {
          changeVerticalExpand(false);
        }
      });
    }
  );

  /**
   * 生成请求参数
   * @param executeType 执行类型，执行时传入
   */
  function makeRequestParams(executeType?: 'localExec' | 'serverExec') {
    const isExecute = executeType === 'localExec' || executeType === 'serverExec';
    const polymorphicName = protocolOptions.value.find(
      (e) => e.value === requestVModel.value.protocol
    )?.polymorphicName; // 协议多态名称
    let parseRequestBodyResult;
    let requestParams;
    if (isHttpProtocol.value) {
      const { formDataBody, wwwFormBody } = requestVModel.value.body;
      const realFormDataBodyValues = filterKeyValParams(
        formDataBody.formValues,
        defaultBodyParamsItem,
        isExecute
      ).validParams;
      const realWwwFormBodyValues = filterKeyValParams(
        wwwFormBody.formValues,
        defaultBodyParamsItem,
        isExecute
      ).validParams;
      parseRequestBodyResult = parseRequestBodyFiles(
        requestVModel.value.body,
        undefined,
        props.fileParams?.uploadFileIds || requestVModel.value.uploadFileIds, // 外面解析详情的时候传入，或引用 api 在requestVModel内存储
        props.fileParams?.linkFileIds || requestVModel.value.linkFileIds, // 外面解析详情的时候传入，或引用 api 在requestVModel内存储
        copyStepFileIdsMap.value
      );
      requestParams = {
        authConfig: requestVModel.value.authConfig,
        body: {
          ...requestVModel.value.body,
          formDataBody: {
            formValues: realFormDataBodyValues,
          },
          wwwFormBody: {
            formValues: realWwwFormBodyValues,
          },
        },
        headers: filterKeyValParams(requestVModel.value.headers, defaultHeaderParamsItem, isExecute).validParams,
        otherConfig: requestVModel.value.otherConfig,
        path: requestVModel.value.url || requestVModel.value.path,
        query: filterKeyValParams(requestVModel.value.query, defaultRequestParamsItem, isExecute).validParams,
        rest: filterKeyValParams(requestVModel.value.rest, defaultRequestParamsItem, isExecute).validParams,
        url: requestVModel.value.url,
        polymorphicName,
      };
    } else {
      requestParams = {
        ...fApi.value?.formData(),
        polymorphicName,
      };
    }
    const { assertionConfig } = requestVModel.value.children[0];
    return {
      ...requestParams,
      resourceId: requestVModel.value.resourceId,
      stepId: requestVModel.value.stepId,
      uniqueId: requestVModel.value.uniqueId,
      activeTab: requestVModel.value.protocol === 'HTTP' ? RequestComposition.HEADER : RequestComposition.PLUGIN,
      responseActiveTab: ResponseComposition.BODY,
      protocol: requestVModel.value.protocol,
      method: isHttpProtocol.value ? requestVModel.value.method : requestVModel.value.protocol,
      name: requestVModel.value.stepName || requestVModel.value.name,
      unSaved: requestVModel.value.unSaved,
      customizeRequest: requestVModel.value.customizeRequest,
      customizeRequestEnvEnable: requestVModel.value.customizeRequestEnvEnable,
      children: [
        {
          polymorphicName: 'MsCommonElement', // 协议多态名称，写死MsCommonElement
          assertionConfig: {
            ...requestVModel.value.children[0].assertionConfig,
            assertions: filterAssertions(assertionConfig, isExecute),
          },
          postProcessorConfig: filterConditionsSqlValidParams(requestVModel.value.children[0].postProcessorConfig),
          preProcessorConfig: filterConditionsSqlValidParams(requestVModel.value.children[0].preProcessorConfig),
        },
      ],
      executeLoading: isExecute,
      ...parseRequestBodyResult,
    };
  }

  /**
   * 执行调试
   * @param val 执行类型
   */
  async function execute(executeType?: 'localExec' | 'serverExec') {
    requestVModel.value.executeLoading = true;
    if (isHttpProtocol.value) {
      emit('execute', makeRequestParams(executeType) as RequestParam, executeType);
    } else {
      // 插件需要校验动态表单
      fApi.value?.validate(async (valid) => {
        if (valid === true) {
          emit('execute', makeRequestParams(executeType) as RequestParam, executeType);
        } else {
          requestVModel.value.executeLoading = false;
          requestVModel.value.activeTab = RequestComposition.PLUGIN;
          nextTick(() => {
            scrollIntoView(document.querySelector('.arco-form-item-message'), { block: 'center' });
          });
        }
      });
    }
  }

  function stopDebug() {
    requestVModel.value.executeLoading = false;
    emit('stopDebug');
  }

  function initErrorMessageInfoItem(key: string) {
    if (requestVModel.value.errorMessageInfo && !requestVModel.value.errorMessageInfo[key]) {
      requestVModel.value.errorMessageInfo[key] = {};
    }
  }

  function setChildErrorMessage(key: number | string, listItem: TabErrorMessage) {
    if (requestVModel.value.errorMessageInfo) {
      requestVModel.value.errorMessageInfo[requestVModel.value.activeTab][key] = cloneDeep(listItem);
    }
  }

  function changeTabErrorMessageList(tabKey: string, formErrorMessageList: string[]) {
    if (!requestVModel.value.errorMessageInfo) return;
    const label = contentTabList.value.find((item) => item.value === tabKey)?.label ?? '';
    const listItem: TabErrorMessage = {
      value: tabKey,
      label,
      messageList: formErrorMessageList,
    };
    initErrorMessageInfoItem(requestVModel.value.activeTab);
    if (requestVModel.value.activeTab === RequestComposition.BODY) {
      setChildErrorMessage(requestVModel.value.body.bodyType, listItem);
    } else if (requestVModel.value.activeTab === RequestComposition.POST_CONDITION) {
      setChildErrorMessage(requestVModel.value.children[0].postProcessorConfig.activeItemId as number, listItem);
    } else if (requestVModel.value.activeTab === RequestComposition.PRECONDITION) {
      setChildErrorMessage(requestVModel.value.children[0].preProcessorConfig.activeItemId as number, listItem);
    } else {
      requestVModel.value.errorMessageInfo[requestVModel.value.activeTab] = cloneDeep(listItem);
    }
  }

  const setErrorMessageList = debounce((list: string[]) => {
    changeTabErrorMessageList(requestVModel.value.activeTab, list);
  }, 300);
  provide('setErrorMessageList', setErrorMessageList);

  // 需要最终提示的信息
  function getFlattenedMessages() {
    if (!requestVModel.value.errorMessageInfo) return;
    const flattenedMessages: { label: string; messageList: string[] }[] = [];
    const { errorMessageInfo } = requestVModel.value;
    Object.entries(errorMessageInfo).forEach(([key, item]) => {
      const label = item.label || Object.values(item)[0]?.label;
      // 处理前后置已删除的
      if ([RequestComposition.POST_CONDITION as string, RequestComposition.PRECONDITION as string].includes(key)) {
        const processorIds = requestVModel.value.children[0][
          key === RequestComposition.POST_CONDITION ? 'postProcessorConfig' : 'preProcessorConfig'
        ].processors.map((processorItem) => String(processorItem.id));
        Object.entries(item).forEach(([childKey, childItem]) => {
          if (!processorIds.includes(childKey)) {
            childItem.messageList = [];
          }
        });
      }
      const messageList: string[] =
        item.messageList || [...new Set(Object.values(item).flatMap((child) => child.messageList))] || [];
      if (messageList.length) {
        flattenedMessages.push({ label, messageList: [...new Set(messageList)] });
      }
    });
    return flattenedMessages;
  }

  function showMessage() {
    getFlattenedMessages()?.forEach(({ label, messageList }) => {
      messageList?.forEach((message) => {
        Message.error(`${label}${message}`);
      });
    });
  }

  function handleContinue() {
    // 检查全部的校验信息
    if (getFlattenedMessages()?.length) {
      showMessage();
      return;
    }
    emit('addStep', cloneDeep(makeRequestParams()) as RequestParam);
    requestVModel.value.stepId = getGenerateId(); // 保存后需要重新生成 id
  }

  function handleSave() {
    handleContinue();
    if (getFlattenedMessages()?.length) return;
    visible.value = false;
  }

  function handleBeforeCancel() {
    // 检查全部的校验信息
    if (getFlattenedMessages()?.length) {
      showMessage();
      return false;
    }
    return true;
  }

  function handleClose() {
    // 关闭时若不是创建行为则是编辑行为，需要触发 applyStep
    if (!requestVModel.value.isNew) {
      emit('applyStep', cloneDeep(makeRequestParams()) as RequestParam);
    }
  }

  const isUrlError = ref(false);
  // const showAddDependencyDrawer = ref(false);
  // const addDependencyMode = ref<'pre' | 'post'>('pre');

  function setDefaultActiveTab() {
    if (requestVModel.value.body.bodyType !== RequestBodyFormat.NONE) {
      requestVModel.value.activeTab = RequestComposition.BODY;
    } else if (requestVModel.value.query.length > 0) {
      requestVModel.value.activeTab = RequestComposition.QUERY;
    } else if (requestVModel.value.rest.length > 0) {
      requestVModel.value.activeTab = RequestComposition.REST;
    } else if (requestVModel.value.headers.length > 0) {
      requestVModel.value.activeTab = RequestComposition.HEADER;
    } else {
      requestVModel.value.activeTab = RequestComposition.BODY;
    }
  }

  async function initQuoteApiDetail() {
    try {
      loading.value = true;
      const res = await getDefinitionDetail(props.step?.resourceId || '');
      let parseRequestBodyResult;
      if (res.protocol === 'HTTP') {
        if ((props.step?.copyFromStepId || props.step?.refType === ScenarioStepRefType.COPY) && props.step?.isNew) {
          // 复制的步骤需要复制文件
          const fileIds = parseRequestBodyFiles(res.request.body, [], [], []).uploadFileIds;
          if (fileIds.length > 0) {
            copyStepFileIdsMap.value = await scenarioCopyStepFiles({
              copyFromStepId: props.step?.copyFromStepId,
              resourceId: props.step?.resourceId,
              stepType: props.step?.stepType,
              refType: props.step?.refType,
              isTempFile: false, // 复制未保存的步骤时 true
              fileIds,
            });
          }
          parseRequestBodyFiles(res.request.body, [], [], [], copyStepFileIdsMap.value);
        } else {
          parseRequestBodyResult = parseRequestBodyFiles(res.request.body, [], [], [], copyStepFileIdsMap.value); // 解析请求体中的文件，将详情中的文件 id 集合收集，更新时以判断文件是否删除以及是否新上传的文件
        }
      }
      requestVModel.value = {
        executeLoading: false,
        activeTab: contentTabList.value[0].value,
        unSaved: false,
        isNew: false,
        label: props.step?.name || res.name,
        stepName: props.step?.name || res.name,
        ...res.request,
        ...res,
        response: cloneDeep(defaultResponse),
        url: res.path,
        name: res.name, // request里面还有个name但是是null
        resourceId: res.id,
        stepId: props.step?.id || '',
        uniqueId: props.step?.uniqueId || '',
        responseActiveTab: ResponseComposition.BODY,
        ...parseRequestBodyResult,
      };
      if (_stepType.value.isQuoteApi && props.request && isHttpProtocol.value && !props.step?.isQuoteScenarioStep) {
        // 是引用 api、并且传入了请求参数、且不是插件、且不是引用场景下的步骤
        // 初始化引用的详情后，需要要把外面传入的数据的请求头、请求体、query、rest里面的参数值写入
        (['headers', 'query', 'rest'] as (keyof RequestParam)[]).forEach((type: keyof RequestParam) => {
          (props.request?.[type] as (EnableKeyValueParam | ExecuteRequestCommonParam)[])?.forEach(
            (item: EnableKeyValueParam) => {
              if (!item.key.length) return;
              const index = (
                requestVModel.value[type] as (EnableKeyValueParam | ExecuteRequestCommonParam)[]
              )?.findIndex((itemReq) => itemReq.key === item.key);
              if (index > -1) {
                (requestVModel.value[type] as (EnableKeyValueParam | ExecuteRequestCommonParam)[])[index].value =
                  item.value;
              }
            }
          );
        });
        (['formDataBody', 'wwwFormBody'] as (keyof ExecuteBody)[]).forEach((type: keyof ExecuteBody) => {
          (props.request?.body?.[type] as ExecuteRequestFormBody).formValues.forEach((item) => {
            if (!item.key.length) return;
            const index = (requestVModel.value.body[type] as ExecuteRequestFormBody).formValues.findIndex(
              (itemReq) => itemReq.key === item.key
            );
            if (index > -1) {
              (requestVModel.value.body[type] as ExecuteRequestFormBody).formValues[index].value = item.value;
              (requestVModel.value.body[type] as ExecuteRequestFormBody).formValues[index].files = item.files;
            }
          });
        });
        if (props.request?.body.binaryBody.file) {
          requestVModel.value.body.binaryBody.file = props.request?.body.binaryBody.file;
        }
      }
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

  /**
   * 替换步骤
   * @param newStep 替换的新步骤
   */
  function handleReplace(newStep: ScenarioStepItem) {
    emit('replace', {
      ...newStep,
      name: props.step?.name || newStep.name,
    });
  }

  const importDialogVisible = ref(false);
  function handleImportCurlDone(res: CurlParseResult) {
    const { url, method, headers, queryParams, bodyType, body } = res;
    const requestBody = parseCurlBody(bodyType, body);
    requestVModel.value.url = url;
    requestVModel.value.method = method;
    requestVModel.value.headers = Object.keys(headers).map((e) => ({
      ...defaultHeaderParamsItem,
      key: e,
      value: headers[e],
    }));
    requestVModel.value.query = Object.keys(queryParams).map((e) => ({
      ...defaultRequestParamsItem,
      key: e,
      value: queryParams[e],
    }));
    requestVModel.value.body = requestBody;
    importDialogVisible.value = false;
    nextTick(() => {
      handleActiveDebugChange();
    });
  }

  watch(
    () => props.request?.uniqueId,
    () => {
      isSwitchingContent.value = true;
    },
    {
      immediate: true,
    }
  );

  watch(
    () => requestVModel.value.protocol,
    (val) => {
      if (requestVModel.value.isNew) {
        setLocalStorage(ProtocolKeyEnum.API_SCENARIO_CUSTOM_PROTOCOL, val);
      }
      handleActiveDebugProtocolChange(val);
    }
  );

  watch(
    () => appStore.loading,
    (val) => {
      loading.value = val;
    }
  );

  watch(
    () => visible.value,
    async (val) => {
      if (val) {
        if (protocolOptions.value.length === 0) {
          await initProtocolList();
        }
        if (props.request) {
          // 查看自定义请求、引用 api、复制 api
          requestVModel.value = cloneDeep({
            ...defaultApiParams,
            ...props.request,
            name: props.step?.name || props.request.name,
            stepName: props.step?.name || props.request.name,
            url: props.request.path, // 后台字段是 path
            activeTab: contentTabList.value[0].value,
            responseActiveTab: ResponseComposition.BODY,
            stepId: props.step?.uniqueId || '',
            uniqueId: props.step?.uniqueId || '',
            customizeRequestEnvEnable:
              props.step?.refType === 'DIRECT' ? props.request.customizeRequestEnvEnable : true, // 自定义请求保留本身保存的是否引用环境，其他的请求固定是引用环境
            isNew: false,
          });
          if (_stepType.value.isQuoteApi) {
            // 引用接口时，每次都要获取源接口数据
            await initQuoteApiDetail();
          }
          handleActiveDebugProtocolChange(requestVModel.value.protocol);
        } else if (_stepType.value.isQuoteApi || isCopyApiNeedInit.value) {
          // 引用接口时，需要初始化引用接口的详情；复制只在第一次初始化的时候需要加载后台数据，复制 api 只要加载过一次后就会保存，所以 props.request 是不为空的
          await initQuoteApiDetail();
          handleActiveDebugProtocolChange(requestVModel.value.protocol);
        } else {
          // 新建自定义请求
          const localProtocol = getLocalStorage<string>(ProtocolKeyEnum.API_SCENARIO_CUSTOM_PROTOCOL);
          const id = getGenerateId();
          requestVModel.value = cloneDeep({
            ...defaultApiParams,
            stepId: id,
            uniqueId: id,
            customizeRequestEnvEnable: false,
            protocol:
              localProtocol?.length && protocolOptions.value.some((item) => item.value === localProtocol)
                ? localProtocol
                : 'HTTP',
          });
        }
        requestVModel.value.activeTab = contentTabList.value[0].value;
        if (requestVModel.value.protocol === 'HTTP') {
          setDefaultActiveTab();
        }
        handleActiveDebugProtocolChange(requestVModel.value.protocol);
        nextTick(() => {
          isSwitchingContent.value = false;
        });
      }
    },
    {
      immediate: true,
    }
  );
</script>

<style lang="less">
  .hidden-second {
    :deep(.arco-split-trigger, .arco-split-pane-second) {
      @apply hidden;
    }
  }
  .show-second {
    :deep(.arco-split-trigger, .arco-split-pane-second) {
      @apply block;
    }
  }
</style>

<style lang="less" scoped>
  .exec-btn {
    margin-right: 12px;
    :deep(.arco-btn) {
      color: var(--color-text-fff) !important;
      background-color: rgb(var(--primary-5)) !important;
      .btn-base-primary-hover();
      .btn-base-primary-active();
      .btn-base-primary-disabled();
    }
  }
  :deep(.no-content) {
    .arco-tabs-content {
      display: none;
    }
  }
  :deep(.arco-tabs-tab:first-child) {
    margin-left: 0;
  }
  :deep(.arco-tabs-tab) {
    @apply leading-none;
  }
  .request-tab-and-response {
    overflow-x: hidden;
    overflow-y: auto;
    .ms-scroll-bar();
  }
  .sticky-content {
    @apply sticky;

    z-index: 101;
    background-color: var(--color-text-fff);
  }
  .request-content-and-response {
    display: flex;
    &.vertical {
      flex-direction: column;
      .response :deep(.response-head) {
        @apply sticky;

        top: 46px; // 请求参数tab高度(不算border-bottom)
        z-index: 11;
        background-color: var(--color-text-fff);
      }
      .request-tab-pane {
        min-height: 400px;
      }
    }
  }
</style>
