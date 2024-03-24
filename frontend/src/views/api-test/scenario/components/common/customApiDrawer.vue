<template>
  <MsDrawer
    v-model:visible="visible"
    :width="960"
    no-content-padding
    :show-continue="true"
    :footer="!!requestVModel.isNew"
    @confirm="handleSave"
    @continue="handleContinue"
    @close="handleClose"
  >
    <template #title>
      <div class="flex items-center gap-[8px]">
        <stepType
          v-if="props.requestType"
          v-show="props.requestType !== ScenarioStepType.CUSTOM_API"
          :type="props.requestType"
        />
        {{ title }}
      </div>
      <div v-if="requestVModel.isNew" class="ml-auto flex items-center gap-[16px]">
        <div v-show="requestVModel.useEnv === 'false'" class="text-[14px] font-normal text-[var(--color-text-4)]">
          {{ t('apiScenario.env', { name: props.envDetailItem?.name }) }}
        </div>
        <MsSelect
          v-model:model-value="requestVModel.useEnv"
          :allow-search="false"
          :options="[
            { label: t('common.quote'), value: 'true' },
            { label: t('common.notQuote'), value: 'false' },
          ]"
          :multiple="false"
          value-key="value"
          label-key="label"
          :prefix="t('project.environmental.env')"
          class="w-[150px]"
          @change="handleUseEnvChange"
        >
        </MsSelect>
      </div>
    </template>
    <div v-show="!pluginError || isHttpProtocol" class="flex h-full flex-col">
      <div class="px-[18px] pt-[8px]">
        <div class="flex flex-wrap items-center justify-between gap-[12px]">
          <div class="flex flex-1 items-center gap-[16px]">
            <a-select
              v-if="requestVModel.isNew"
              v-model:model-value="requestVModel.protocol"
              :options="protocolOptions"
              :loading="protocolLoading"
              :disabled="props.requestType === ScenarioStepType.QUOTE_API"
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
              <a-tooltip v-if="!isHttpProtocol" :content="requestVModel.label" :mouse-enter-delay="500">
                <div class="one-line-text max-w-[350px]"> {{ requestVModel.label }}</div>
              </a-tooltip>
            </div>
            <a-input-group v-if="isHttpProtocol" class="flex-1">
              <apiMethodSelect
                v-model:model-value="requestVModel.method"
                class="w-[140px]"
                :disabled="props.requestType === ScenarioStepType.QUOTE_API"
                @change="handleActiveDebugChange"
              />
              <a-input
                v-model:model-value="requestVModel.url"
                :max-length="255"
                :placeholder="t('apiTestDebug.urlPlaceholder')"
                allow-clear
                class="hover:z-10"
                :style="isUrlError ? 'border: 1px solid rgb(var(--danger-6);z-index: 10' : ''"
                :disabled="props.requestType === ScenarioStepType.QUOTE_API"
                @input="() => (isUrlError = false)"
                @change="handleUrlChange"
              />
            </a-input-group>
          </div>
          <div>
            <a-dropdown-button
              v-if="hasLocalExec"
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
            <a-button v-else-if="!requestVModel.executeLoading" type="primary" @click="() => execute('serverExec')">
              {{ t('apiTestDebug.serverExec') }}
            </a-button>
            <a-button v-else type="primary" class="mr-[12px]" @click="stopDebug">{{ t('common.stop') }}</a-button>
          </div>
        </div>
        <a-input
          v-if="
            props.requestType &&
            [ScenarioStepType.QUOTE_API, ScenarioStepType.COPY_API].includes(props.requestType) &&
            isHttpProtocol
          "
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
          v-model:active-key="requestVModel.activeTab"
          :content-tab-list="contentTabList"
          :get-text-func="getTabBadge"
          class="no-content relative mt-[8px] border-b"
        />
      </div>
      <div ref="splitContainerRef" class="request-and-response h-[calc(100%-87px)]">
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
                    v-if="requestVModel.activeTab === RequestComposition.PLUGIN"
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
                    :disabled-except-param="!isEditableApi"
                    :layout="activeLayout"
                    :second-box-height="secondBoxHeight"
                    @change="handleActiveDebugChange"
                  />
                  <httpBody
                    v-else-if="requestVModel.activeTab === RequestComposition.BODY"
                    v-model:params="requestVModel.body"
                    :layout="activeLayout"
                    :disabled-except-param="!isEditableApi"
                    :second-box-height="secondBoxHeight"
                    :upload-temp-file-api="props.uploadTempFileApi"
                    :file-save-as-source-id="props.fileSaveAsSourceId"
                    :file-save-as-api="props.fileSaveAsApi"
                    :file-module-options-api="props.fileModuleOptionsApi"
                    @change="handleActiveDebugChange"
                  />
                  <httpQuery
                    v-else-if="requestVModel.activeTab === RequestComposition.QUERY"
                    v-model:params="requestVModel.query"
                    :layout="activeLayout"
                    :disabled-except-param="!isEditableApi"
                    :second-box-height="secondBoxHeight"
                    @change="handleActiveDebugChange"
                  />
                  <httpRest
                    v-else-if="requestVModel.activeTab === RequestComposition.REST"
                    v-model:params="requestVModel.rest"
                    :layout="activeLayout"
                    :disabled-except-param="!isEditableApi"
                    :second-box-height="secondBoxHeight"
                    @change="handleActiveDebugChange"
                  />
                  <precondition
                    v-else-if="requestVModel.activeTab === RequestComposition.PRECONDITION"
                    v-model:config="requestVModel.children[0].preProcessorConfig"
                    :is-definition="false"
                    :disabled="!isEditableApi"
                    @change="handleActiveDebugChange"
                  />
                  <postcondition
                    v-else-if="requestVModel.activeTab === RequestComposition.POST_CONDITION"
                    v-model:config="requestVModel.children[0].postProcessorConfig"
                    :response="requestVModel.response?.requestResults[0]?.responseResult.body"
                    :layout="activeLayout"
                    :disabled="!isEditableApi"
                    :second-box-height="secondBoxHeight"
                    :is-definition="false"
                    @change="handleActiveDebugChange"
                  />
                  <assertion
                    v-else-if="requestVModel.activeTab === RequestComposition.ASSERTION"
                    v-model:params="requestVModel.children[0].assertionConfig.assertions"
                    :is-definition="false"
                    :disabled="!isEditableApi"
                    :assertion-config="requestVModel.children[0].assertionConfig"
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
              :request-task-result="requestVModel.response"
              :is-edit="false"
              :upload-temp-file-api="props.uploadTempFileApi"
              :loading="requestVModel.executeLoading || loading"
              :is-definition="false"
              @change-expand="changeVerticalExpand"
              @change-layout="handleActiveLayoutChange"
              @change="handleActiveDebugChange"
              @execute="execute"
            />
          </template>
        </MsSplitBox>
      </div>
    </div>
    <a-empty
      v-if="pluginError && !isHttpProtocol"
      :description="t('apiTestDebug.noPlugin')"
      class="h-[200px] items-center justify-center"
    >
      <template #image>
        <MsIcon type="icon-icon_plugin_outlined" size="48" />
      </template>
    </a-empty>
    <!-- <addDependencyDrawer v-model:visible="showAddDependencyDrawer" :mode="addDependencyMode" /> -->
  </MsDrawer>
</template>

<script setup lang="ts">
  import { Message, SelectOptionData } from '@arco-design/web-vue';
  import { cloneDeep, debounce } from 'lodash-es';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import { TabItem } from '@/components/pure/ms-editable-tab/types';
  import MsFormCreate from '@/components/pure/ms-form-create/formCreate.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import MsTab from '@/components/pure/ms-tab/index.vue';
  import assertion from '@/components/business/ms-assertion/index.vue';
  import MsSelect from '@/components/business/ms-select';
  import stepType from './stepType.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import apiMethodSelect from '@/views/api-test/components/apiMethodSelect.vue';
  import auth from '@/views/api-test/components/requestComposition/auth.vue';
  import postcondition from '@/views/api-test/components/requestComposition/postcondition.vue';
  import precondition from '@/views/api-test/components/requestComposition/precondition.vue';
  import response from '@/views/api-test/components/requestComposition/response/index.vue';
  import setting from '@/views/api-test/components/requestComposition/setting.vue';

  import { getPluginScript, getProtocolList } from '@/api/modules/api-test/common';
  import { getDefinitionDetail } from '@/api/modules/api-test/management';
  import { getSocket } from '@/api/modules/project-management/commonScript';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import { getGenerateId, parseQueryParams } from '@/utils';
  import { scrollIntoView } from '@/utils/dom';

  import {
    ExecuteApiRequestFullParams,
    ExecuteConditionConfig,
    ExecuteRequestParams,
    PluginConfig,
    RequestTaskResult,
  } from '@/models/apiTest/common';
  import { ModuleTreeNode, TransferFileParams } from '@/models/common';
  import {
    RequestAuthType,
    RequestBodyFormat,
    RequestComposition,
    RequestConditionProcessor,
    RequestMethods,
    ResponseComposition,
    ScenarioStepType,
  } from '@/enums/apiEnum';

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
    isNew: boolean;
    protocol: string;
    activeTab: RequestComposition;
    executeLoading: boolean; // 执行中loading
    isCopy?: boolean; // 是否是复制
    isExecute?: boolean; // 是否是执行
  }

  export type RequestParam = ExecuteApiRequestFullParams & {
    response?: RequestTaskResult;
    useEnv: string;
    request?: ExecuteApiRequestFullParams; // 请求参数集合
  } & RequestCustomAttr &
    TabItem;

  const props = defineProps<{
    request?: RequestParam; // 请求参数集合
    requestType?: ScenarioStepType;
    stepName: string;
    detailLoading?: boolean; // 详情加载状态
    envDetailItem?: {
      id?: string;
      projectId: string;
      name: string;
    };
    executeApi?: (params: ExecuteRequestParams) => Promise<any>; // 执行接口
    localExecuteApi?: (url: string, params: ExecuteRequestParams) => Promise<any>; // 本地执行接口
    uploadTempFileApi?: (...args) => Promise<any>; // 上传临时文件接口
    fileSaveAsSourceId?: string | number; // 文件转存关联的资源id
    fileSaveAsApi?: (params: TransferFileParams) => Promise<string>; // 文件转存接口
    fileModuleOptionsApi?: (projectId: string) => Promise<ModuleTreeNode[]>; // 文件转存目录下拉框接口
    permissionMap?: {
      execute: string;
      create: string;
      update: string;
    };
  }>();

  const emit = defineEmits<{
    (e: 'addStep', request: RequestParam): void;
    (e: 'applyStep', request: RequestParam): void;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();

  const visible = defineModel<boolean>('visible', { required: true });
  const loading = defineModel<boolean>('detailLoading', { default: false });

  const defaultDebugParams: RequestParam = {
    type: 'api',
    id: '',
    useEnv: 'false',
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

  const requestVModel = ref<RequestParam>(props.request || defaultDebugParams);
  const title = computed(() => {
    if (props.requestType && [ScenarioStepType.COPY_API, ScenarioStepType.QUOTE_API].includes(props.requestType)) {
      return props.stepName;
    }
    return t('apiScenario.customApi');
  });
  const isHttpProtocol = computed(() => requestVModel.value.protocol === 'HTTP');
  const temporaryResponseMap = {}; // 缓存websocket返回的报告内容，避免执行接口后切换tab导致报告丢失
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
      label: RequestComposition.QUERY,
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
  // 根据协议类型获取请求内容tab
  const contentTabList = computed(() => {
    // HTTP 协议 tabs
    if (isHttpProtocol.value) {
      return httpContentTabList;
    }
    return [...pluginContentTab, ...httpContentTabList.filter((e) => commonContentTabKey.includes(e.value))];
  });

  /**
   * 获取 tab 的参数数量徽标
   */
  function getTabBadge(tabKey: RequestComposition) {
    switch (tabKey) {
      case RequestComposition.HEADER:
        const headerNum = filterKeyValParams(requestVModel.value.headers, defaultHeaderParamsItem).validParams.length;
        return `${headerNum > 0 ? headerNum : ''}`;
      case RequestComposition.BODY:
        return requestVModel.value.body?.bodyType !== RequestBodyFormat.NONE ? '1' : '';
      case RequestComposition.QUERY:
        const queryNum = filterKeyValParams(requestVModel.value.query, defaultRequestParamsItem).validParams.length;
        return `${queryNum > 0 ? queryNum : ''}`;
      case RequestComposition.REST:
        const restNum = filterKeyValParams(requestVModel.value.rest, defaultRequestParamsItem).validParams.length;
        return `${restNum > 0 ? restNum : ''}`;
      case RequestComposition.PRECONDITION:
        return `${requestVModel.value.children[0].preProcessorConfig.processors.length || ''}`;
      case RequestComposition.POST_CONDITION:
        return `${requestVModel.value.children[0].postProcessorConfig.processors.length || ''}`;
      case RequestComposition.ASSERTION:
        return `${requestVModel.value.children[0].assertionConfig.assertions.length || ''}`;
      case RequestComposition.AUTH:
        return requestVModel.value.authConfig.authType !== RequestAuthType.NONE ? '1' : '';
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

  const hasLocalExec = ref(false); // 是否配置了api本地执行
  const isPriorityLocalExec = ref(false); // 是否优先本地执行
  const localExecuteUrl = ref('');

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
  const isCopyApiNeedInit = computed(
    () => props.requestType === ScenarioStepType.COPY_API && props.request?.request === null
  );
  const isEditableApi = computed(
    () => props.requestType === ScenarioStepType.COPY_API || props.requestType === ScenarioStepType.CUSTOM_API
  );

  // 处理插件表单输入框变化
  const handlePluginFormChange = debounce(() => {
    if (isEditableApi.value) {
      // 复制或者新建的时候需要缓存表单数据，引用的不能更改
      temporaryPluginFormMap[requestVModel.value.id] = fApi.value?.formData();
    }
    handleActiveDebugChange();
  }, 300);

  /**
   * 控制插件表单字段显示
   */
  function controlPluginFormFields() {
    const allFields = fApi.value?.fields();
    let fields: string[] = [];
    if (requestVModel.value.useEnv === 'true') {
      fields = pluginScriptMap.value[requestVModel.value.protocol].apiDefinitionFields || [];
    } else {
      fields = pluginScriptMap.value[requestVModel.value.protocol].apiDebugFields || [];
    }
    fApi.value?.hidden(true, allFields?.filter((e) => !fields.includes(e)) || []);
    return fields;
  }

  /**
   * 设置插件表单数据
   */
  function setPluginFormData() {
    const tempForm = temporaryPluginFormMap[requestVModel.value.id];
    if (tempForm || !requestVModel.value.isNew) {
      // 如果缓存的表单数据存在或者是编辑状态，则需要将之前的输入数据填充
      const formData = isEditableApi.value ? tempForm || requestVModel.value : requestVModel.value;
      if (fApi.value) {
        fApi.value.nextRefresh(() => {
          const form = {};
          controlPluginFormFields().forEach((key) => {
            form[key] = formData[key];
          });
          fApi.value?.setValue(form);
          setTimeout(() => {
            // 初始化时赋值会触发表单数据变更，300ms 是为了与 handlePluginFormChange的防抖时间保持一致
            isInitPluginForm.value = true;
          }, 300);
        });
      }
    } else {
      nextTick(() => {
        controlPluginFormFields();
      });
    }
  }

  // 切换是否使用环境变量
  async function handleUseEnvChange() {
    if (!isHttpProtocol.value) {
      const pluginId = protocolOptions.value.find((e) => e.value === requestVModel.value.protocol)?.pluginId;
      const res = await getPluginScript(pluginId);
      pluginScriptMap.value[requestVModel.value.protocol] = res;
      fApi.value?.nextTick(() => {
        controlPluginFormFields();
      });
      nextTick(() => {
        fApi.value?.resetFields();
      });
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
    () => showResponse.value,
    (val) => {
      if (val) {
        splitBoxSize.value = 0.6;
      } else {
        splitBoxSize.value = 1;
      }
    }
  );

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
      if (val) {
        changeVerticalExpand(true);
      } else {
        changeVerticalExpand(false);
      }
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

  const reportId = ref('');
  const websocket = ref<WebSocket>();

  /**
   * 开启websocket监听，接收执行结果
   */
  function debugSocket(executeType?: 'localExec' | 'serverExec') {
    websocket.value = getSocket(
      reportId.value,
      executeType === 'localExec' ? '/ws/debug' : '',
      executeType === 'localExec' ? localExecuteUrl.value : ''
    );
    websocket.value.addEventListener('message', (event) => {
      const data = JSON.parse(event.data);
      if (data.msgType === 'EXEC_RESULT') {
        if (requestVModel.value.reportId === data.reportId) {
          // 判断当前查看的tab是否是当前返回的报告的tab，是的话直接赋值
          requestVModel.value.response = data.taskResult;
          requestVModel.value.executeLoading = false;
          requestVModel.value.isExecute = false;
        } else {
          // 不是则需要把报告缓存起来，等切换到对应的tab再赋值
          temporaryResponseMap[data.reportId] = data.taskResult;
        }
      } else if (data.msgType === 'EXEC_END') {
        // 执行结束，关闭websocket
        websocket.value?.close();
        requestVModel.value.executeLoading = false;
        requestVModel.value.isExecute = false;
      }
    });
  }

  /**
   * 生成请求参数
   * @param executeType 执行类型，执行时传入
   */
  function makeRequestParams(executeType?: 'localExec' | 'serverExec') {
    const isExecute = executeType === 'localExec' || executeType === 'serverExec';
    const { formDataBody, wwwFormBody } = requestVModel.value.body;
    const polymorphicName = protocolOptions.value.find(
      (e) => e.value === requestVModel.value.protocol
    )?.polymorphicName; // 协议多态名称
    let parseRequestBodyResult;
    let requestParams;
    if (isHttpProtocol.value) {
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
        requestVModel.value.uploadFileIds, // 外面解析详情的时候传入
        requestVModel.value.linkFileIds // 外面解析详情的时候传入
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
        method: requestVModel.value.method,
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
    reportId.value = getGenerateId();
    requestVModel.value.reportId = reportId.value; // 存储报告ID
    debugSocket(executeType); // 开启websocket
    let requestName = '';
    let requestModuleId = '';
    const apiDefinitionParams: Record<string, any> = {};
    requestName = requestVModel.value.name;
    requestModuleId = requestVModel.value.moduleId;
    return {
      id: requestVModel.value.id.toString(),
      reportId: reportId.value,
      environmentId: props.envDetailItem?.id || '',
      name: requestName,
      moduleId: requestModuleId,
      ...apiDefinitionParams,
      protocol: requestVModel.value.protocol,
      method: isHttpProtocol.value ? requestVModel.value.method : requestVModel.value.protocol,
      path: isHttpProtocol.value ? requestVModel.value.url || requestVModel.value.path : undefined,
      request: {
        ...requestParams,
        name: requestName,
        children: [
          {
            polymorphicName: 'MsCommonElement', // 协议多态名称，写死MsCommonElement
            assertionConfig: requestVModel.value.children[0].assertionConfig,
            postProcessorConfig: filterConditionsSqlValidParams(requestVModel.value.children[0].postProcessorConfig),
            preProcessorConfig: filterConditionsSqlValidParams(requestVModel.value.children[0].preProcessorConfig),
          },
        ],
      },
      ...parseRequestBodyResult,
      projectId: appStore.currentProjectId,
      frontendDebug: executeType === 'localExec',
      isNew: requestVModel.value.isNew,
    };
  }

  /**
   * 执行调试
   * @param val 执行类型
   */
  async function execute(executeType?: 'localExec' | 'serverExec') {
    // todo 执行，待测试
    if (isHttpProtocol.value) {
      try {
        if (!props.executeApi) return;
        requestVModel.value.executeLoading = true;
        requestVModel.value.response = cloneDeep(defaultResponse);
        const res = await props.executeApi(makeRequestParams(executeType));
        if (executeType === 'localExec' && props.localExecuteApi) {
          await props.localExecuteApi(localExecuteUrl.value, res);
        }
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      } finally {
        requestVModel.value.executeLoading = false;
      }
    } else {
      // 插件需要校验动态表单
      fApi.value?.validate(async (valid) => {
        if (valid === true) {
          try {
            if (!props.executeApi) return;
            requestVModel.value.executeLoading = true;
            requestVModel.value.response = cloneDeep(defaultResponse);
            const res = await props.executeApi(makeRequestParams(executeType));
            if (executeType === 'localExec' && props.localExecuteApi) {
              await props.localExecuteApi(localExecuteUrl.value, res);
            }
          } catch (error) {
            // eslint-disable-next-line no-console
            console.log(error);
            requestVModel.value.executeLoading = false;
          }
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
    websocket.value?.close();
    requestVModel.value.executeLoading = false;
  }

  function handleContinue() {
    requestVModel.value.isNew = false; // 添加完就不是新建了
    emit('addStep', requestVModel.value);
  }

  function handleSave() {
    handleContinue();
    visible.value = false;
  }

  function handleClose() {
    // 关闭时若不是创建行为则是编辑行为，需要触发 applyStep
    if (!requestVModel.value.isNew) {
      emit('applyStep', { ...requestVModel.value, ...makeRequestParams() });
    }
  }

  const isUrlError = ref(false);
  // const showAddDependencyDrawer = ref(false);
  // const addDependencyMode = ref<'pre' | 'post'>('pre');

  async function initQuoteApiDetail() {
    try {
      loading.value = true;
      const res = await getDefinitionDetail(requestVModel.value.id);
      let parseRequestBodyResult;
      if (res.protocol === 'HTTP') {
        parseRequestBodyResult = parseRequestBodyFiles(res.request.body); // 解析请求体中的文件，将详情中的文件 id 集合收集，更新时以判断文件是否删除以及是否新上传的文件
      }
      requestVModel.value = {
        responseActiveTab: ResponseComposition.BODY,
        executeLoading: false,
        activeTab: res.protocol === 'HTTP' ? RequestComposition.HEADER : RequestComposition.PLUGIN,
        unSaved: false,
        isNew: false,
        label: res.name,
        ...res.request,
        ...res,
        response: cloneDeep(defaultResponse),
        responseDefinition: res.response.map((e) => ({ ...e, responseActiveTab: ResponseComposition.BODY })),
        url: res.path,
        name: res.name, // request里面还有个name但是是null
        id: res.id,
        ...parseRequestBodyResult,
      };
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

  watch(
    () => visible.value,
    async (val) => {
      if (val) {
        if (props.request) {
          requestVModel.value = { ...defaultDebugParams, ...props.request };
          if (
            props.requestType === ScenarioStepType.QUOTE_API ||
            isCopyApiNeedInit.value
            // 引用接口时，需要初始化引用接口的详情；复制只在第一次初始化的时候需要加载后台数据(request.request是复制请求时列表参数字段request会为 null，以此判断释放第一次初始化)
          ) {
            await initQuoteApiDetail();
          }
          // TODO: 类型报错
          // if (
          //   props.requestType === ScenarioStepType.QUOTE_API &&
          //   props.request.request &&
          //   requestVModel.value.request
          // ) {
          //   // 初始化引用的详情后，需要要把外面传入的数据的请求头、请求体、query、rest里面的参数值写入
          //   ['headers', 'query', 'rest'].forEach((type) => {
          //     props.request.request[type]?.forEach((item) => {
          //       const index = requestVModel.value.request[type]?.findIndex((itemReq) => itemReq.key === item.key);
          //       if (index > -1) {
          //         requestVModel.value.request[type][index].value = item.value;
          //         requestVModel.value[type] = requestVModel.value.request[type];
          //       }
          //     });
          //   });
          //   if (props.request.request.body.bodyType !== 'NONE') {
          //     ['formDataBody', 'wwwFormBody'].forEach((type) => {
          //       props.request.request.body[type].formValues.forEach((item) => {
          //         const index = requestVModel.value.request.body[type].formValues.findIndex(
          //           (itemReq) => itemReq.key === item.key
          //         );
          //         if (index > -1) {
          //           requestVModel.value.request.body[type]?.formValues[index].value = item.value;
          //           requestVModel.value.body = requestVModel.value.request?.body;
          //         }
          //       });
          //     });
          //   }
          // }
        }
        await initProtocolList();
        if (props.request) {
          handleActiveDebugProtocolChange(requestVModel.value.protocol);
        }
      }
    }
  );
</script>

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
  .hidden-second {
    :deep(.arco-split-trigger) {
      @apply hidden;
    }
  }
  .show-second {
    :deep(.arco-split-trigger) {
      @apply block;
    }
  }
</style>
