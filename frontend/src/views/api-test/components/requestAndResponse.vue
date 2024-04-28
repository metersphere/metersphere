<template>
  <div class="request-and-response flex-1">
    <MsTab
      v-model:active-key="requestVModel.activeTab"
      :content-tab-list="contentTabList"
      :get-text-func="getTabBadge"
      no-content
      class="relative border-b"
    />
    <a-spin class="block h-[300px] w-full p-[16px]" :loading="requestVModel.executeLoading || loading">
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
        :layout="activeLayout"
        :disabled-param-value="props.disabledParamValue"
        :disabled-except-param="props.disabledExceptParam"
        :second-box-height="secondBoxHeight"
        @change="handleActiveDebugChange"
      />
      <httpBody
        v-else-if="requestVModel.activeTab === RequestComposition.BODY"
        v-model:params="requestVModel.body"
        :layout="activeLayout"
        :disabled-param-value="props.disabledParamValue"
        :disabled-except-param="props.disabledExceptParam"
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
        :disabled-param-value="props.disabledParamValue"
        :disabled-except-param="props.disabledExceptParam"
        :second-box-height="secondBoxHeight"
        @change="handleActiveDebugChange"
      />
      <httpRest
        v-else-if="requestVModel.activeTab === RequestComposition.REST"
        v-model:params="requestVModel.rest"
        :layout="activeLayout"
        :disabled-param-value="props.disabledParamValue"
        :disabled-except-param="props.disabledExceptParam"
        :second-box-height="secondBoxHeight"
        @change="handleActiveDebugChange"
      />
      <precondition
        v-else-if="requestVModel.activeTab === RequestComposition.PRECONDITION"
        v-model:config="requestVModel.children[0].preProcessorConfig"
        :disabled="props.disabledExceptParam"
        :is-definition="false"
        @change="handleActiveDebugChange"
      />
      <postcondition
        v-else-if="requestVModel.activeTab === RequestComposition.POST_CONDITION"
        v-model:config="requestVModel.children[0].postProcessorConfig"
        :disabled="props.disabledExceptParam"
        :response="requestVModel.response?.requestResults[0]?.responseResult.body"
        :layout="activeLayout"
        :second-box-height="secondBoxHeight"
        :is-definition="false"
        @change="handleActiveDebugChange"
      />
      <assertion
        v-else-if="requestVModel.activeTab === RequestComposition.ASSERTION"
        v-model:params="requestVModel.children[0].assertionConfig.assertions"
        :disabled="props.disabledExceptParam"
        :is-definition="false"
        :show-extraction="true"
        :assertion-config="requestVModel.children[0].assertionConfig"
      />
      <auth
        v-else-if="requestVModel.activeTab === RequestComposition.AUTH"
        v-model:params="requestVModel.authConfig"
        :disabled="props.disabledExceptParam"
        @change="handleActiveDebugChange"
      />
      <setting
        v-else-if="requestVModel.activeTab === RequestComposition.SETTING"
        v-model:params="requestVModel.otherConfig"
        :disabled="props.disabledExceptParam"
        @change="handleActiveDebugChange"
      />
    </a-spin>
    <response
      v-model:active-layout="activeLayout"
      v-model:active-tab="requestVModel.responseActiveTab"
      :is-http-protocol="isHttpProtocol"
      :is-priority-local-exec="props.isPriorityLocalExec"
      :request-url="requestVModel.url"
      :is-expanded="true"
      :request-result="requestVModel.response?.requestResults[0]"
      :console="requestVModel.response?.console"
      :is-edit="false"
      hide-layout-switch
      :upload-temp-file-api="props.uploadTempFileApi"
      :loading="requestVModel.executeLoading || loading"
      :is-definition="true"
      @change="handleActiveDebugChange"
      @execute="(val) => emit('execute', val)"
    />
  </div>
</template>

<script setup lang="ts">
  import { Message, SelectOptionData } from '@arco-design/web-vue';
  import { debounce } from 'lodash-es';

  import MsFormCreate from '@/components/pure/ms-form-create/formCreate.vue';
  import MsTab from '@/components/pure/ms-tab/index.vue';
  import assertion from '@/components/business/ms-assertion/index.vue';
  import auth from '@/views/api-test/components/requestComposition/auth.vue';
  import { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';
  import postcondition from '@/views/api-test/components/requestComposition/postcondition.vue';
  import precondition from '@/views/api-test/components/requestComposition/precondition.vue';
  import response from '@/views/api-test/components/requestComposition/response/index.vue';
  import setting from '@/views/api-test/components/requestComposition/setting.vue';

  import { getPluginScript, getProtocolList } from '@/api/modules/api-test/common';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import { PluginConfig } from '@/models/apiTest/common';
  import { ModuleTreeNode, TransferFileParams } from '@/models/common';
  import { RequestAuthType, RequestBodyFormat, RequestComposition } from '@/enums/apiEnum';

  import {
    defaultBodyParamsItem,
    defaultHeaderParamsItem,
    defaultRequestParamsItem,
  } from '@/views/api-test/components/config';
  import {
    filterAssertions,
    filterConditionsSqlValidParams,
    filterKeyValParams,
    parseRequestBodyFiles,
  } from '@/views/api-test/components/utils';
  import type { Api } from '@form-create/arco-design';

  // 懒加载Http协议组件
  const httpHeader = defineAsyncComponent(() => import('@/views/api-test/components/requestComposition/header.vue'));
  const httpBody = defineAsyncComponent(() => import('@/views/api-test/components/requestComposition/body.vue'));
  const httpQuery = defineAsyncComponent(() => import('@/views/api-test/components/requestComposition/query.vue'));
  const httpRest = defineAsyncComponent(() => import('@/views/api-test/components/requestComposition/rest.vue'));

  const props = defineProps<{
    request?: RequestParam; // 请求参数集合
    detailLoading?: boolean; // 详情加载状态
    isPriorityLocalExec?: boolean; // 是否优先本地执行
    disabledParamValue?: boolean; // 参数值禁用
    disabledExceptParam?: boolean; // 除了可以修改参数值其他都禁用
    isShowCommonContentTabKey?: boolean; // 是否展示请求内容公共tabKey
    uploadTempFileApi?: (...args: any) => Promise<any>; // 上传临时文件接口
    fileSaveAsSourceId?: string | number; // 文件转存关联的资源id
    fileSaveAsApi?: (params: TransferFileParams) => Promise<string>; // 文件转存接口
    fileModuleOptionsApi?: (projectId: string) => Promise<ModuleTreeNode[]>; // 文件转存目录下拉框接口
  }>();
  const emit = defineEmits<{
    (e: 'execute', executeType?: 'localExec' | 'serverExec'): void;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();

  const loading = defineModel<boolean>('detailLoading', { default: false });
  const requestVModel = defineModel<RequestParam>('request', { required: true });
  const isHttpProtocol = computed(() => requestVModel.value.protocol === 'HTTP');

  const activeLayout = ref<'horizontal' | 'vertical'>('vertical');
  const secondBoxHeight = ref(0);

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
      // 引用CASE：如果[请求头、query、rest、前后置、断言]没有数据则直接隐藏tab
      if (props.disabledExceptParam) {
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
      return props.isShowCommonContentTabKey
        ? httpContentTabList
        : httpContentTabList.filter((e) => !commonContentTabKey.includes(e.value));
    }
    if (props.disabledExceptParam) {
      return [
        ...pluginContentTab,
        ...httpContentTabList
          .filter((e) => commonContentTabKey.includes(e.value))
          .filter(
            (item) =>
              !(!preProcessorNum.value && item.value === RequestComposition.PRECONDITION) &&
              !(!postProcessorNum.value && item.value === RequestComposition.POST_CONDITION) &&
              !(!assertionsNum.value && item.value === RequestComposition.ASSERTION)
          ),
      ];
    }
    return [...pluginContentTab, ...httpContentTabList.filter((e) => commonContentTabKey.includes(e.value))];
  });
  // 获取 tab 的参数数量徽标
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
        return requestVModel.value.authConfig.authType !== RequestAuthType.NONE ? '1' : '';
      default:
        return '';
    }
  }
  // 设置第一个tab为当前tab
  function setActiveTabByFirst() {
    requestVModel.value.activeTab = contentTabList.value[0].value;
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

  const pluginError = ref(false);
  const pluginLoading = ref(false);
  const isInitPluginForm = ref(false);
  const pluginScriptMap = ref<Record<string, PluginConfig>>({}); // 存储初始化过后的插件配置
  const temporaryPluginFormMap: Record<string, any> = {}; // 缓存插件表单，避免切换tab导致动态表单数据丢失

  const fApi = ref<Api>();
  const currentPluginOptions = computed<Record<string, any>>(
    () => pluginScriptMap.value[requestVModel.value.protocol]?.options || {}
  );
  const currentPluginScript = computed<Record<string, any>[]>(
    () => pluginScriptMap.value[requestVModel.value.protocol]?.script || []
  );

  function handleActiveDebugChange() {
    if (!loading.value || (!isHttpProtocol.value && isInitPluginForm.value)) {
      // 如果是因为加载详情触发的change则不需要标记为未保存；或者是插件协议的话需要等待表单初始化完毕
      requestVModel.value.unSaved = true;
    }
  }

  // 处理插件表单输入框变化
  const handlePluginFormChange = debounce(() => {
    temporaryPluginFormMap[requestVModel.value.id] = fApi.value?.formData();
    handleActiveDebugChange();
  }, 300);

  // 控制插件表单字段显示
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

  // 设置插件表单数据
  function setPluginFormData() {
    const tempForm = temporaryPluginFormMap[requestVModel.value.id];
    if (tempForm || !requestVModel.value.isNew || requestVModel.value.isCopy) {
      // 如果缓存的表单数据存在或者是编辑状态，则需要将之前的输入数据填充
      const formData = tempForm || requestVModel.value;
      if (fApi.value) {
        fApi.value.nextTick(() => {
          const form: Record<string, any> = {};
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
      fApi.value?.nextTick(() => {
        controlPluginFormFields();
      });
      nextTick(() => {
        // 如果是没有缓存也不是编辑，则需要重置表单，因为 form-create 只有一个实例，已经被其他有数据的 tab 污染了，需要重置
        fApi.value?.resetFields();
      });
    }
  }
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
      // 已经初始化过
      setPluginFormData();
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

  // 生成请求参数
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
        requestVModel.value.responseDefinition,
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

    const requestName = requestVModel.value.name ?? '';
    const requestModuleId = requestVModel.value.moduleId ?? '';
    const { assertionConfig } = requestVModel.value.children[0];
    return {
      id: requestVModel.value.id.toString(),
      name: requestName,
      moduleId: requestModuleId,
      protocol: requestVModel.value.protocol,
      method: isHttpProtocol.value ? requestVModel.value.method : requestVModel.value.protocol,
      path: isHttpProtocol.value ? requestVModel.value.url || requestVModel.value.path : undefined,
      request: {
        ...requestParams,
        name: requestName,
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
      },
      ...parseRequestBodyResult,
      projectId: appStore.currentProjectId,
      frontendDebug: executeType === 'localExec',
      isNew: requestVModel.value.isNew,
    };
  }

  onBeforeMount(() => {
    initProtocolList();
  });

  defineExpose({
    initPluginScript,
    handleActiveDebugChange,
    makeRequestParams,
    setActiveTabByFirst,
  });
</script>

<style lang="less" scoped>
  .request-and-response {
    :deep(.response) {
      height: 400px; // TODO: 暂时
    }
  }
</style>
