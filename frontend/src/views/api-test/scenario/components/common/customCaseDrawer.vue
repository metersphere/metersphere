<template>
  <MsDrawer
    v-model:visible="visible"
    :width="900"
    :footer="false"
    show-full-screen
    no-content-padding
    @close="handleClose"
  >
    <template #title>
      <stepType v-if="activeStep?.stepType" :step="activeStep" class="mr-[4px]" />
      <a-input
        v-if="activeStep?.name"
        v-show="isShowEditStepNameInput"
        ref="stepNameInputRef"
        v-model:model-value="activeStep.name"
        class="flex-1"
        :placeholder="t('apiScenario.pleaseInputStepName')"
        :max-length="255"
        show-word-limit
        @press-enter="updateStepName"
        @blur="updateStepName"
      />
      <div v-show="!isShowEditStepNameInput" class="flex flex-1 items-center justify-between">
        <div class="flex items-center gap-[8px]">
          <a-tooltip :content="activeStep?.name">
            <div class="one-line-text max-w-[300px]"> {{ characterLimit(activeStep?.name) }}</div>
          </a-tooltip>
          <MsIcon type="icon-icon_edit_outlined" class="edit-script-name-icon" @click="showEditScriptNameInput" />
        </div>
        <div class="right-operation-button-icon flex items-center">
          <MsButton type="icon" status="secondary">
            <MsIcon type="icon-icon_swich" />
            {{ t('common.replace') }}
          </MsButton>
          <MsButton class="mr-4" type="icon" status="secondary" @click="handleDelete">
            <MsIcon type="icon-icon_delete-trash_outlined" />
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
      <div class="flex items-center gap-[16px] p-[16px] pb-[8px]">
        <a-input
          v-if="activeStep?.stepType && (_stepType.isCopyCase || _stepType.isQuoteCase)"
          v-model:model-value="requestVModel.name"
          :max-length="255"
          :show-word-limit="isEditableApi"
          :placeholder="t('apiTestManagement.apiNamePlaceholder')"
          :disabled="!isEditableApi"
          allow-clear
        />
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
      <div class="px-[16px]">
        <MsTab
          v-model:active-key="requestVModel.activeTab"
          :content-tab-list="contentTabList"
          :get-text-func="getTabBadge"
          no-content
          class="relative mt-[8px] border-b"
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
                    :disabled-param-value="!isEditableApi"
                    :disabled-except-param="!isEditableApi"
                    :layout="activeLayout"
                    :second-box-height="secondBoxHeight"
                    @change="handleActiveDebugChange"
                  />
                  <httpBody
                    v-else-if="requestVModel.activeTab === RequestComposition.BODY"
                    v-model:params="requestVModel.body"
                    :layout="activeLayout"
                    :disabled-param-value="!isEditableApi"
                    :disabled-except-param="!isEditableApi"
                    :second-box-height="secondBoxHeight"
                    :upload-temp-file-api="uploadTempFileCase"
                    :file-save-as-source-id="scenarioId"
                    :file-save-as-api="transferFileCase"
                    :file-module-options-api="getTransferOptionsCase"
                    @change="handleActiveDebugChange"
                  />
                  <httpQuery
                    v-else-if="requestVModel.activeTab === RequestComposition.QUERY"
                    v-model:params="requestVModel.query"
                    :layout="activeLayout"
                    :disabled-param-value="!isEditableApi"
                    :disabled-except-param="!isEditableApi"
                    :second-box-height="secondBoxHeight"
                    @change="handleActiveDebugChange"
                  />
                  <httpRest
                    v-else-if="requestVModel.activeTab === RequestComposition.REST"
                    v-model:params="requestVModel.rest"
                    :layout="activeLayout"
                    :disabled-param-value="!isEditableApi"
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
                <loopPagination v-model:current-loop="currentLoop" :loop-total="loopTotal" class="!mb-0" />
              </template>
            </response>
          </template>
        </MsSplitBox>
      </div>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import { InputInstance, Message, SelectOptionData } from '@arco-design/web-vue';
  import { cloneDeep, debounce } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsFormCreate from '@/components/pure/ms-form-create/formCreate.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import MsTab from '@/components/pure/ms-tab/index.vue';
  import assertion from '@/components/business/ms-assertion/index.vue';
  import loopPagination from './loopPagination.vue';
  import stepType from './stepType/stepType.vue';
  import auth from '@/views/api-test/components/requestComposition/auth.vue';
  import postcondition from '@/views/api-test/components/requestComposition/postcondition.vue';
  import precondition from '@/views/api-test/components/requestComposition/precondition.vue';
  import response from '@/views/api-test/components/requestComposition/response/index.vue';
  import setting from '@/views/api-test/components/requestComposition/setting.vue';
  import { RequestParam } from '@/views/api-test/scenario/components/common/customApiDrawer.vue';

  import { getPluginScript, getProtocolList } from '@/api/modules/api-test/common';
  import {
    getCaseDetail,
    getTransferOptionsCase,
    transferFileCase,
    uploadTempFileCase,
  } from '@/api/modules/api-test/management';
  import { useAppStore } from '@/store';
  import { characterLimit } from '@/utils';
  import { scrollIntoView } from '@/utils/dom';

  import { ExecuteConditionConfig, PluginConfig, RequestResult } from '@/models/apiTest/common';
  import { ScenarioStepFileParams, ScenarioStepItem } from '@/models/apiTest/scenario';
  import {
    RequestAuthType,
    RequestBodyFormat,
    RequestComposition,
    RequestConditionProcessor,
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
    defaultKeyValueParamItem,
    defaultRequestParamsItem,
    defaultResponse,
  } from '@/views/api-test/components/config';
  import { filterKeyValParams, parseRequestBodyFiles } from '@/views/api-test/components/utils';
  import { Api } from '@form-create/arco-design';
  // 懒加载Http协议组件
  const httpHeader = defineAsyncComponent(() => import('@/views/api-test/components/requestComposition/header.vue'));
  const httpBody = defineAsyncComponent(() => import('@/views/api-test/components/requestComposition/body.vue'));
  const httpQuery = defineAsyncComponent(() => import('@/views/api-test/components/requestComposition/query.vue'));
  const httpRest = defineAsyncComponent(() => import('@/views/api-test/components/requestComposition/rest.vue'));

  const props = defineProps<{
    request?: RequestParam; // 请求参数集合
    stepResponses?: Record<string | number, RequestResult[]>;
    fileParams?: ScenarioStepFileParams;
  }>();
  const emit = defineEmits<{
    (e: 'applyStep', request: RequestParam): void;
    (e: 'deleteStep'): void;
    (e: 'execute', request: RequestParam, executeType?: 'localExec' | 'serverExec'): void;
    (e: 'stopDebug'): void;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();

  const visible = defineModel<boolean>('visible', { required: true });
  const activeStep = defineModel<ScenarioStepItem>('activeStep', {
    required: false,
  });
  // 注入祖先组件提供的属性
  const scenarioId = inject<string | number>('scenarioId');
  const isPriorityLocalExec = inject<Ref<boolean>>('isPriorityLocalExec');

  const defaultApiParams: RequestParam = {
    name: '',
    type: 'api',
    stepId: '',
    resourceId: '',
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
          enableGlobal: true,
          assertions: [],
        },
        postProcessorConfig: {
          enableGlobal: true,
          processors: [],
        },
        preProcessorConfig: {
          enableGlobal: true,
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
  const _stepType = computed(() => {
    if (activeStep.value) {
      return getStepType(activeStep.value);
    }
    return {
      isCopyCase: false,
      isQuoteCase: false,
    };
  });
  const isCopyCase = computed(
    () =>
      activeStep.value?.stepType === ScenarioStepType.API_CASE && activeStep.value?.refType === ScenarioStepRefType.COPY
  );
  const isCopyNeedInit = computed(() => isCopyCase.value && props.request === undefined);
  const isQuote = computed(
    () =>
      activeStep.value?.stepType === ScenarioStepType.API_CASE && activeStep.value?.refType === ScenarioStepRefType.REF
  );
  const currentLoop = ref(1);
  const currentResponse = computed(() => {
    if (activeStep.value?.id) {
      return props.stepResponses?.[activeStep.value?.id]?.[currentLoop.value - 1];
    }
  });
  const loopTotal = computed(() => (activeStep.value?.id && props.stepResponses?.[activeStep.value?.id]?.length) || 0);
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
  const isEditableApi = computed(() => _stepType.value.isCopyCase);
  const isHttpProtocol = computed(() => requestVModel.value.protocol === 'HTTP');
  const isInitPluginForm = ref(false);
  const loading = ref(false);

  function handleActiveDebugChange() {
    if (!loading.value || (!isHttpProtocol.value && isInitPluginForm.value)) {
      // 如果是因为加载详情触发的change则不需要标记为未保存；或者是插件协议的话需要等待表单初始化完毕
      requestVModel.value.unSaved = true;
    }
  }

  const isShowEditStepNameInput = ref(false);
  const stepNameInputRef = ref<InputInstance>();
  function showEditScriptNameInput() {
    isShowEditStepNameInput.value = true;
    nextTick(() => {
      stepNameInputRef.value?.focus();
    });
  }
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

  const hasLocalExec = ref(false); // 是否配置了api本地执行

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
      if (activeStep.value?.refType === ScenarioStepRefType.COPY) {
        // 复制 case 才能编辑，才需要计算
        parseRequestBodyResult = parseRequestBodyFiles(
          requestVModel.value.body,
          props.fileParams?.uploadFileIds || requestVModel.value.uploadFileIds, // 外面解析详情的时候传入，或引用 case 在requestVModel内存储
          props.fileParams?.linkFileIds || requestVModel.value.linkFileIds // 外面解析详情的时候传入，或引用 case 在requestVModel内存储
        );
      }
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
    emit('stopDebug');
  }

  function handleClose() {
    // 关闭时若不是创建行为则是编辑行为，需要触发 applyStep，引用 case 不能更改不需要触发
    if (!requestVModel.value.isNew && activeStep.value?.refType === ScenarioStepRefType.COPY) {
      emit('applyStep', cloneDeep(makeRequestParams()));
    }
  }

  // const showAddDependencyDrawer = ref(false);
  // const addDependencyMode = ref<'pre' | 'post'>('pre');

  function handleDelete() {
    emit('deleteStep');
  }

  async function initQuoteCaseDetail() {
    try {
      loading.value = true;
      const res = await getCaseDetail(activeStep.value?.resourceId || '');
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
        url: res.path,
        name: res.name, // request里面还有个name但是是null
        resourceId: res.id,
        stepId: activeStep.value?.id || '',
        ...parseRequestBodyResult,
      };
      nextTick(() => {
        requestVModel.value.activeTab = contentTabList.value[0].value;
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
        if (protocolOptions.value.length === 0) {
          await initProtocolList();
        }
        // 查看自定义请求、引用 api、复制 api
        requestVModel.value = cloneDeep({
          ...defaultApiParams,
          ...props.request,
          isNew: false,
        });
        if (isQuote.value || isCopyNeedInit.value) {
          // 引用时，需要初始化引用的详情；复制只在第一次初始化的时候需要加载后台数据(request.request是复制请求时列表参数字段request会为 null，以此判断释放第一次初始化)
          await initQuoteCaseDetail();
        }
        handleActiveDebugProtocolChange(requestVModel.value.protocol);
      }
    }
  );
</script>

<style lang="less" scoped>
  .exec-btn {
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
  :deep(.arco-tabs-tab:first-child) {
    margin-left: 0;
  }
  :deep(.arco-tabs-tab) {
    @apply leading-none;
  }
</style>
