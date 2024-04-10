<template>
  <MsDrawer
    v-model:visible="visible"
    :width="960"
    no-content-padding
    :show-continue="true"
    :footer="requestVModel.isNew === true"
    :ok-disabled="requestVModel.executeLoading || (isHttpProtocol && !requestVModel.url)"
    show-full-screen
    @confirm="handleSave"
    @continue="handleContinue"
    @close="handleClose"
  >
    <template #title>
      <div class="flex max-w-[60%] items-center gap-[8px]">
        <stepTypeVue
          v-if="props.step && [ScenarioStepType.API, ScenarioStepType.CUSTOM_REQUEST].includes(props.step?.stepType)"
          :step="props.step"
        />
        <a-tooltip :content="title" position="bottom">
          <div class="one-line-text">
            {{ title }}
          </div>
        </a-tooltip>
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
        <MsButton class="mr-4" type="icon" status="secondary" @click="emit('deleteStep')">
          <MsIcon type="icon-icon_delete-trash_outlined" />
          {{ t('common.delete') }}
        </MsButton>
      </div>
      <div
        v-if="!props.step || props.step?.stepType === ScenarioStepType.CUSTOM_REQUEST"
        class="customApiDrawer-title-right ml-auto flex items-center gap-[16px]"
      >
        <a-tooltip :content="currentEnvConfig?.name" :disabled="!currentEnvConfig?.name">
          <div class="one-line-text max-w-[250px] text-[14px] font-normal text-[var(--color-text-4)]">
            {{ t('apiScenario.env', { name: currentEnvConfig?.name }) }}
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
      <div class="px-[18px] pt-[8px]">
        <div class="flex flex-wrap items-center justify-between gap-[12px]">
          <div class="flex flex-1 items-center gap-[16px]">
            <a-select
              v-if="requestVModel.isNew"
              v-model:model-value="requestVModel.protocol"
              :options="protocolOptions"
              :loading="protocolLoading"
              :disabled="_stepType.isQuoteApi || props.step?.isQuoteScenarioStep"
              class="w-[90px]"
              @change="(val) => handleActiveDebugProtocolChange(val as string)"
            />
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
                  {{ currentEnvConfig?.httpConfig.find((e) => e.type === 'NONE')?.url }}
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
        <a-input
          v-if="props.step?.stepType && (_stepType.isCopyApi || _stepType.isQuoteApi) && isHttpProtocol"
          v-model:model-value="requestVModel.name"
          :max-length="255"
          :placeholder="t('apiTestManagement.apiNamePlaceholder')"
          :disabled="!isEditableApi"
          allow-clear
          class="mt-[8px]"
        />
      </div>
      <div class="px-[16px]">
        <MsTab
          v-if="requestVModel.activeTab"
          v-model:active-key="requestVModel.activeTab"
          :content-tab-list="contentTabList"
          :get-text-func="getTabBadge"
          class="no-content relative mt-[8px] border-b"
        />
      </div>
      <div ref="splitContainerRef" class="h-[calc(100%-87px)]">
        <MsSplitBox
          ref="verticalSplitBoxRef"
          v-model:size="splitBoxSize"
          :max="!showResponse ? 1 : 0.98"
          min="10px"
          :direction="activeLayout"
          second-container-class="!overflow-y-hidden"
          :class="!showResponse ? 'hidden-second' : 'show-second'"
          @expand-change="handleVerticalExpandChange"
        >
          <template #first>
            <a-spin class="block h-full w-full" :loading="requestVModel.executeLoading || loading">
              <div
                :class="`flex h-full min-w-[800px] flex-col p-[16px] ${
                  activeLayout === 'horizontal' ? ' pr-[16px]' : ''
                }`"
              >
                <div class="tab-pane-container">
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
                    :second-box-height="secondBoxHeight"
                    @change="handleActiveDebugChange"
                  />
                  <httpBody
                    v-else-if="requestVModel.activeTab === RequestComposition.BODY"
                    v-model:params="requestVModel.body"
                    :layout="activeLayout"
                    :disabled-param-value="!isEditableApi && !isEditableParamValue"
                    :disabled-except-param="!isEditableApi"
                    :second-box-height="secondBoxHeight"
                    :upload-temp-file-api="uploadTempFile"
                    :file-save-as-source-id="scenarioId"
                    :file-save-as-api="transferFile"
                    :file-module-options-api="getTransferOptions"
                    @change="handleActiveDebugChange"
                  />
                  <httpQuery
                    v-else-if="requestVModel.activeTab === RequestComposition.QUERY"
                    v-model:params="requestVModel.query"
                    :layout="activeLayout"
                    :disabled-param-value="!isEditableApi && !isEditableParamValue"
                    :disabled-except-param="!isEditableApi"
                    :second-box-height="secondBoxHeight"
                    @change="handleActiveDebugChange"
                  />
                  <httpRest
                    v-else-if="requestVModel.activeTab === RequestComposition.REST"
                    v-model:params="requestVModel.rest"
                    :layout="activeLayout"
                    :disabled-param-value="!isEditableApi && !isEditableParamValue"
                    :disabled-except-param="!isEditableApi"
                    :second-box-height="secondBoxHeight"
                    @change="handleActiveDebugChange"
                  />
                  <precondition
                    v-else-if="requestVModel.activeTab === RequestComposition.PRECONDITION"
                    v-model:config="requestVModel.children[0].preProcessorConfig"
                    is-definition
                    :disabled="!isEditableApi"
                    @change="handleActiveDebugChange"
                  />
                  <postcondition
                    v-else-if="requestVModel.activeTab === RequestComposition.POST_CONDITION"
                    v-model:config="requestVModel.children[0].postProcessorConfig"
                    :response="responseResultBody"
                    :layout="activeLayout"
                    :disabled="!isEditableApi"
                    :second-box-height="secondBoxHeight"
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
              </div>
            </a-spin>
          </template>
          <template #second>
            <response
              v-if="visible"
              v-show="showResponse"
              v-model:active-layout="activeLayout"
              v-model:active-tab="requestVModel.responseActiveTab"
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
              @change-expand="changeVerticalExpand"
              @change-layout="handleActiveLayoutChange"
              @execute="execute"
            >
              <template #titleRight>
                <loopPagination v-model:current-loop="currentLoop" :loop-total="loopTotal" />
              </template>
            </response>
          </template>
        </MsSplitBox>
      </div>
    </div>
    <!-- <addDependencyDrawer v-model:visible="showAddDependencyDrawer" :mode="addDependencyMode" /> -->
  </MsDrawer>
</template>

<script setup lang="ts">
  import { Message, SelectOptionData } from '@arco-design/web-vue';
  import { cloneDeep, debounce } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsFormCreate from '@/components/pure/ms-form-create/formCreate.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import MsTab from '@/components/pure/ms-tab/index.vue';
  import assertion from '@/components/business/ms-assertion/index.vue';
  import loopPagination from './loopPagination.vue';
  import replaceButton from './replaceButton.vue';
  import stepTypeVue from './stepType/stepType.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import apiMethodSelect from '@/views/api-test/components/apiMethodSelect.vue';
  import auth from '@/views/api-test/components/requestComposition/auth.vue';
  import postcondition from '@/views/api-test/components/requestComposition/postcondition.vue';
  import precondition from '@/views/api-test/components/requestComposition/precondition.vue';
  import response from '@/views/api-test/components/requestComposition/response/index.vue';
  import setting from '@/views/api-test/components/requestComposition/setting.vue';

  import { getPluginScript, getProtocolList } from '@/api/modules/api-test/common';
  import { getDefinitionDetail } from '@/api/modules/api-test/management';
  import { getTransferOptions, transferFile, uploadTempFile } from '@/api/modules/api-test/scenario';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import { getGenerateId, parseQueryParams } from '@/utils';
  import { scrollIntoView } from '@/utils/dom';

  import {
    ExecuteApiRequestFullParams,
    ExecuteConditionConfig,
    PluginConfig,
    RequestResult,
    RequestTaskResult,
  } from '@/models/apiTest/common';
  import { ScenarioStepFileParams, ScenarioStepItem } from '@/models/apiTest/scenario';
  import { EnvConfig } from '@/models/projectManagement/environmental';
  import {
    RequestAuthType,
    RequestBodyFormat,
    RequestComposition,
    RequestConditionProcessor,
    RequestMethods,
    ResponseComposition,
    ScenarioStepType,
  } from '@/enums/apiEnum';

  import getStepType from './stepType/utils';
  import {
    defaultBodyParams,
    defaultBodyParamsItem,
    defaultHeaderParamsItem,
    defaultKeyValueParamItem,
    defaultRequestParamsItem,
    defaultResponse,
  } from '@/views/api-test/components/config';
  import { filterKeyValParams, parseRequestBodyFiles } from '@/views/api-test/components/utils';
  import type { Api } from '@form-create/arco-design';

  // 懒加载Http协议组件
  const httpHeader = defineAsyncComponent(() => import('@/views/api-test/components/requestComposition/header.vue'));
  const httpBody = defineAsyncComponent(() => import('@/views/api-test/components/requestComposition/body.vue'));
  const httpQuery = defineAsyncComponent(() => import('@/views/api-test/components/requestComposition/query.vue'));
  const httpRest = defineAsyncComponent(() => import('@/views/api-test/components/requestComposition/rest.vue'));
  // const addDependencyDrawer = defineAsyncComponent(
  //   () => import('@/views/api-test/management/components/addDependencyDrawer.vue')
  // );

  export interface RequestCustomAttr {
    type: 'api';
    name: string;
    stepId: string | number; // 所属步骤 id
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
  }

  export type RequestParam = ExecuteApiRequestFullParams & {
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
  const currentEnvConfig = inject<Ref<EnvConfig>>('currentEnvConfig');
  const hasLocalExec = inject<Ref<boolean>>('hasLocalExec');
  const isPriorityLocalExec = inject<Ref<boolean>>('isPriorityLocalExec');

  const visible = defineModel<boolean>('visible', { required: true });
  const loading = defineModel<boolean>('detailLoading', { default: false });

  const defaultApiParams: RequestParam = {
    name: '',
    type: 'api',
    stepId: '',
    resourceId: '',
    customizeRequest: true,
    customizeRequestEnvEnable: false,
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
  };

  const requestVModel = ref<RequestParam>(defaultApiParams);
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
      return props.step?.name;
    }
    return t('apiScenario.customApi');
  });
  // 是否显示环境域名前缀
  const showEnvPrefix = computed(
    () =>
      requestVModel.value.customizeRequestEnvEnable &&
      currentEnvConfig?.value.httpConfig.find((e) => e.type === 'NONE')?.url
  );
  const currentLoop = ref(1);
  const currentResponse = computed(() => {
    if (requestVModel.value.stepId === props.step?.uniqueId && props.step?.uniqueId) {
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
      if (val && val[requestVModel.value.stepId]) {
        requestVModel.value.executeLoading = false;
      }
    },
    {
      deep: true,
    }
  );

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

  function handleActiveDebugChange() {
    if (!loading.value || (!isHttpProtocol.value && isInitPluginForm.value)) {
      // 如果是因为加载详情触发的change则不需要标记为未保存；或者是插件协议的话需要等待表单初始化完毕
      requestVModel.value.unSaved = true;
    }
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

  // 处理插件表单输入框变化
  const handlePluginFormChange = debounce(() => {
    if (isEditableApi.value) {
      // 复制或者新建的时候需要缓存表单数据，引用的不能更改
      temporaryPluginFormMap[requestVModel.value.stepId] = fApi.value?.formData();
    }
    handleActiveDebugChange();
  }, 300);

  /**
   * 控制插件表单字段显示
   */
  function controlPluginFormFields() {
    const currentFormFields = fApi.value?.fields();
    let fields: string[] = [];
    if (requestVModel.value.customizeRequestEnvEnable) {
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
    return fields;
  }

  /**
   * 设置插件表单数据
   */
  function setPluginFormData() {
    const tempForm = temporaryPluginFormMap[requestVModel.value.stepId];
    if (tempForm || !requestVModel.value.isNew) {
      // 如果缓存的表单数据存在或者是编辑状态，则需要将之前的输入数据填充
      const formData = isEditableApi.value ? tempForm || requestVModel.value : requestVModel.value;
      nextTick(() => {
        if (fApi.value) {
          fApi.value.nextRefresh(() => {
            const form = {};
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
      if (!Object.values(RequestMethods).includes(requestVModel.value.method)) {
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
    () => isHttpProtocol.value || requestVModel.value.response?.requestResults[0]?.responseResult.responseCode
  );
  const splitBoxSize = ref<string | number>(!showResponse.value ? 1 : 0.6);
  const activeLayout = ref<'horizontal' | 'vertical'>('vertical');
  const splitContainerRef = ref<HTMLElement>();
  const secondBoxHeight = ref(0);

  watch(
    () => splitBoxSize.value,
    debounce((val) => {
      // 动画 300ms
      if (splitContainerRef.value) {
        if (typeof val === 'string' && val.includes('px')) {
          val = Number(val.split('px')[0]);
          secondBoxHeight.value = splitContainerRef.value.clientHeight - val;
        } else {
          secondBoxHeight.value = splitContainerRef.value.clientHeight * (1 - val);
        }
      }
    }, 300),
    {
      immediate: true,
    }
  );

  const verticalSplitBoxRef = ref<InstanceType<typeof MsSplitBox>>();
  const isVerticalExpanded = ref(true);

  function handleVerticalExpandChange(val: boolean) {
    isVerticalExpanded.value = val;
  }

  function changeVerticalExpand(val: boolean) {
    isVerticalExpanded.value = val;
    if (val) {
      verticalSplitBoxRef.value?.expand(0.6);
    } else {
      verticalSplitBoxRef.value?.collapse(
        splitContainerRef.value ? `${splitContainerRef.value.clientHeight - 42}px` : 0
      );
    }
  }

  watch(
    () => showResponse.value,
    (val) => {
      nextTick(() => {
        if (val) {
          changeVerticalExpand(true);
        } else {
          isVerticalExpanded.value = false;
          verticalSplitBoxRef.value?.collapse(1);
        }
      });
    }
  );

  function handleActiveLayoutChange() {
    isVerticalExpanded.value = true;
    splitBoxSize.value = 0.6;
    verticalSplitBoxRef.value?.expand(0.6);
  }

  function filterConditionsSqlValidParams(condition: ExecuteConditionConfig) {
    const conditionCopy = cloneDeep(condition);
    conditionCopy.processors = conditionCopy.processors.map((processor) => {
      if (processor.processorType === RequestConditionProcessor.SQL) {
        processor.extractParams = filterKeyValParams(
          processor.extractParams || [],
          defaultKeyValueParamItem
        ).validParams;
      }
      return processor;
    });
    return conditionCopy;
  }

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
        props.fileParams?.uploadFileIds || requestVModel.value.uploadFileIds, // 外面解析详情的时候传入，或引用 api 在requestVModel内存储
        props.fileParams?.linkFileIds || requestVModel.value.linkFileIds // 外面解析详情的时候传入，或引用 api 在requestVModel内存储
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
    return {
      ...requestParams,
      resourceId: requestVModel.value.resourceId,
      stepId: requestVModel.value.stepId,
      activeTab: requestVModel.value.protocol === 'HTTP' ? RequestComposition.HEADER : RequestComposition.PLUGIN,
      responseActiveTab: ResponseComposition.BODY,
      protocol: requestVModel.value.protocol,
      method: isHttpProtocol.value ? requestVModel.value.method : requestVModel.value.protocol,
      name: requestVModel.value.name,
      unSaved: requestVModel.value.unSaved,
      customizeRequest: requestVModel.value.customizeRequest,
      customizeRequestEnvEnable: requestVModel.value.customizeRequestEnvEnable,
      children: [
        {
          polymorphicName: 'MsCommonElement', // 协议多态名称，写死MsCommonElement
          assertionConfig: requestVModel.value.children[0].assertionConfig,
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
      emit('execute', makeRequestParams(executeType), executeType);
    } else {
      // 插件需要校验动态表单
      fApi.value?.validate(async (valid) => {
        if (valid === true) {
          emit('execute', makeRequestParams(executeType), executeType);
        } else {
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

  function handleContinue() {
    emit('addStep', cloneDeep(makeRequestParams()));
  }

  function handleSave() {
    handleContinue();
    visible.value = false;
  }

  function handleClose() {
    // 关闭时若不是创建行为则是编辑行为，需要触发 applyStep
    if (!requestVModel.value.isNew) {
      emit('applyStep', cloneDeep(makeRequestParams()));
    }
  }

  const isUrlError = ref(false);
  // const showAddDependencyDrawer = ref(false);
  // const addDependencyMode = ref<'pre' | 'post'>('pre');

  async function initQuoteApiDetail() {
    try {
      loading.value = true;
      const res = await getDefinitionDetail(props.step?.resourceId || '');
      let parseRequestBodyResult;
      if (res.protocol === 'HTTP') {
        parseRequestBodyResult = parseRequestBodyFiles(res.request.body); // 解析请求体中的文件，将详情中的文件 id 集合收集，更新时以判断文件是否删除以及是否新上传的文件
      }
      requestVModel.value = {
        executeLoading: false,
        activeTab: contentTabList.value[0].value,
        unSaved: false,
        isNew: false,
        label: res.name,
        ...res.request,
        ...res,
        response: cloneDeep(defaultResponse),
        url: res.path,
        name: res.name, // request里面还有个name但是是null
        resourceId: res.id,
        stepId: props.step?.uniqueId || '',
        responseActiveTab: ResponseComposition.BODY,
        ...parseRequestBodyResult,
      };
      if (_stepType.value.isQuoteApi && props.request && isHttpProtocol.value && !props.step?.isQuoteScenarioStep) {
        // 是引用 api、并且传入了请求参数、且不是插件、且不是引用场景下的步骤
        // 初始化引用的详情后，需要要把外面传入的数据的请求头、请求体、query、rest里面的参数值写入
        ['headers', 'query', 'rest'].forEach((type) => {
          props.request?.[type]?.forEach((item) => {
            if (!item.key.length) return;
            const index = requestVModel.value[type]?.findIndex((itemReq) => itemReq.key === item.key);
            if (index > -1) {
              requestVModel.value[type][index].value = item.value;
            }
          });
        });
        ['formDataBody', 'wwwFormBody'].forEach((type) => {
          props.request?.body?.[type].formValues.forEach((item) => {
            if (!item.key.length) return;
            const index = requestVModel.value.body[type].formValues.findIndex((itemReq) => itemReq.key === item.key);
            if (index > -1) {
              requestVModel.value.body[type].formValues[index].value = item.value;
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
    emit('replace', newStep);
  }

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
            url: props.request.path, // 后台字段是 path
            activeTab: contentTabList.value[0].value,
            responseActiveTab: ResponseComposition.BODY,
            stepId: props.step?.uniqueId || '',
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
          requestVModel.value = cloneDeep({
            ...defaultApiParams,
            stepId: getGenerateId(),
          });
        }
        requestVModel.value.activeTab = contentTabList.value[0].value;
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
      color: white !important;
      background-color: rgb(var(--primary-5)) !important;
      .btn-base-primary-hover();
      .btn-base-primary-active();
      .btn-base-primary-disabled();
    }
  }
  .tab-pane-container {
    @apply flex-1 overflow-y-auto;
    .ms-scroll-bar();
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
</style>
