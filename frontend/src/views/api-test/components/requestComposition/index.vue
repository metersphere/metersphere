<template>
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
    <div class="px-[24px] pt-[16px]">
      <div class="mb-[8px] flex items-center justify-between">
        <div class="flex flex-1 items-center gap-[16px]">
          <a-select
            v-if="requestVModel.isNew"
            v-model:model-value="requestVModel.protocol"
            :options="protocolOptions"
            :loading="protocolLoading"
            class="mr-[4px] w-[90px]"
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
            <div v-if="!isHttpProtocol">
              {{ requestVModel.label }}
            </div>
          </div>
          <a-input-group v-if="isHttpProtocol" class="flex-1">
            <apiMethodSelect
              v-model:model-value="requestVModel.method"
              class="w-[140px]"
              @change="handleActiveDebugChange"
            />
            <a-input
              v-model:model-value="requestVModel.url"
              :max-length="255"
              :placeholder="t('apiTestDebug.urlPlaceholder')"
              @change="handleUrlChange"
            />
          </a-input-group>
        </div>
        <div class="ml-[16px]">
          <a-dropdown-button
            :button-props="{ loading: requestVModel.executeLoading }"
            :disabled="requestVModel.executeLoading"
            class="exec-btn"
            @click="execute"
            @select="execute"
          >
            {{ isPriorityLocalExec ? t('apiTestDebug.localExec') : t('apiTestDebug.serverExec') }}
            <template v-if="hasLocalExec" #icon>
              <icon-down />
            </template>
            <template v-if="hasLocalExec" #content>
              <a-doption :value="isPriorityLocalExec ? 'localExec' : 'serverExec'">
                {{ isPriorityLocalExec ? t('apiTestDebug.serverExec') : t('apiTestDebug.localExec') }}
              </a-doption>
            </template>
          </a-dropdown-button>
          <a-dropdown v-if="props.isDefinition" @select="handleSelect">
            <a-button type="secondary">{{ t('common.save') }}</a-button>
            <template #content>
              <a-doption value="save">{{ t('common.save') }}</a-doption>
              <a-doption value="saveAsCase">{{ t('apiTestManagement.saveAsCase') }}</a-doption>
            </template>
          </a-dropdown>
          <a-button v-else type="secondary" :loading="saveLoading" @click="handleSaveShortcut">
            <div class="flex items-center">
              {{ t('common.save') }}
              <div class="text-[var(--color-text-4)]">(<icon-command size="14" />+S)</div>
            </div>
          </a-button>
        </div>
      </div>
      <a-input
        v-if="props.isDefinition"
        v-model:model-value="requestVModel.name"
        :max-length="255"
        :placeholder="t('apiTestManagement.apiNamePlaceholder')"
        @change="handleActiveDebugChange"
      />
    </div>
    <div ref="splitContainerRef" class="h-[calc(100%-52px)]">
      <MsSplitBox
        ref="splitBoxRef"
        v-model:size="splitBoxSize"
        :max="0.98"
        min="10px"
        :direction="activeLayout"
        second-container-class="!overflow-y-hidden"
        @expand-change="handleExpandChange"
      >
        <template #first>
          <div
            :class="`flex h-full min-w-[800px] flex-col px-[24px] pb-[16px] ${
              activeLayout === 'horizontal' ? ' pr-[16px]' : ''
            }`"
          >
            <div>
              <a-tabs v-model:active-key="requestVModel.activeTab" class="no-content mb-[16px]">
                <a-tab-pane v-for="item of contentTabList" :key="item.value" :title="item.label" />
              </a-tabs>
            </div>
            <div class="tab-pane-container">
              <template v-if="requestVModel.activeTab === RequestComposition.PLUGIN || isInitPluginForm">
                <a-spin
                  v-show="requestVModel.activeTab === RequestComposition.PLUGIN"
                  :loading="pluginLoading"
                  class="min-h-[100px] w-full"
                >
                  <MsFormCreate
                    v-model:api="fApi"
                    :rule="currentPluginScript"
                    :option="currentPluginOptions"
                    @mounted="() => (isInitPluginForm = true)"
                    @change="handlePluginFormChange"
                  />
                </a-spin>
              </template>
              <debugHeader
                v-if="requestVModel.activeTab === RequestComposition.HEADER"
                v-model:params="requestVModel.headers"
                :layout="activeLayout"
                :second-box-height="secondBoxHeight"
                @change="handleActiveDebugChange"
              />
              <debugBody
                v-else-if="requestVModel.activeTab === RequestComposition.BODY"
                v-model:params="requestVModel.body"
                :layout="activeLayout"
                :second-box-height="secondBoxHeight"
                :upload-temp-file-api="props.uploadTempFileApi"
                @change="handleActiveDebugChange"
              />
              <debugQuery
                v-else-if="requestVModel.activeTab === RequestComposition.QUERY"
                v-model:params="requestVModel.query"
                :layout="activeLayout"
                :second-box-height="secondBoxHeight"
                @change="handleActiveDebugChange"
              />
              <debugRest
                v-else-if="requestVModel.activeTab === RequestComposition.REST"
                v-model:params="requestVModel.rest"
                :layout="activeLayout"
                :second-box-height="secondBoxHeight"
                @change="handleActiveDebugChange"
              />
              <precondition
                v-else-if="requestVModel.activeTab === RequestComposition.PRECONDITION"
                v-model:config="requestVModel.children[0].preProcessorConfig"
                @change="handleActiveDebugChange"
              />
              <postcondition
                v-else-if="requestVModel.activeTab === RequestComposition.POST_CONDITION"
                v-model:config="requestVModel.children[0].postProcessorConfig"
                :response="requestVModel.response.requestResults[0]?.responseResult.body"
                :layout="activeLayout"
                :second-box-height="secondBoxHeight"
                @change="handleActiveDebugChange"
              />
              <debugAuth
                v-else-if="requestVModel.activeTab === RequestComposition.AUTH"
                v-model:params="requestVModel.authConfig"
                @change="handleActiveDebugChange"
              />
              <debugSetting
                v-else-if="requestVModel.activeTab === RequestComposition.SETTING"
                v-model:params="requestVModel.otherConfig"
                @change="handleActiveDebugChange"
              />
            </div>
          </div>
        </template>
        <template #second>
          <response
            v-model:active-layout="activeLayout"
            v-model:active-tab="requestVModel.responseActiveTab"
            :is-expanded="isExpanded"
            :response="requestVModel.response"
            :hide-layout-swicth="props.hideResponseLayoutSwitch"
            @change-expand="changeExpand"
            @change-layout="handleActiveLayoutChange"
          />
        </template>
      </MsSplitBox>
    </div>
  </div>
  <a-modal
    v-model:visible="saveModalVisible"
    :title="t('common.save')"
    :ok-loading="saveLoading"
    class="ms-modal-form"
    title-align="start"
    body-class="!p-0"
    @before-ok="handleSave"
    @cancel="handleCancel"
  >
    <a-form ref="saveModalFormRef" :model="saveModalForm" layout="vertical">
      <a-form-item
        field="name"
        :label="t('apiTestDebug.requestName')"
        :rules="[{ required: true, message: t('apiTestDebug.requestNameRequired') }]"
        asterisk-position="end"
      >
        <a-input v-model:model-value="saveModalForm.name" :placeholder="t('apiTestDebug.requestNamePlaceholder')" />
      </a-form-item>
      <a-form-item
        v-if="isHttpProtocol"
        field="path"
        :label="t('apiTestDebug.requestUrl')"
        :rules="[{ required: true, message: t('apiTestDebug.requestUrlRequired') }]"
        asterisk-position="end"
      >
        <a-input v-model:model-value="saveModalForm.path" :placeholder="t('apiTestDebug.commonPlaceholder')" />
      </a-form-item>
      <a-form-item :label="t('apiTestDebug.requestModule')" class="mb-0">
        <a-tree-select
          v-model:modelValue="saveModalForm.moduleId"
          :data="selectTree"
          :field-names="{ title: 'name', key: 'id', children: 'children' }"
          allow-search
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
  import { FormInstance, Message, SelectOptionData } from '@arco-design/web-vue';
  import { cloneDeep, debounce } from 'lodash-es';

  import { TabItem } from '@/components/pure/ms-editable-tab/types';
  import MsFormCreate from '@/components/pure/ms-form-create/formCreate.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import debugAuth from './auth.vue';
  import postcondition from './postcondition.vue';
  import precondition from './precondition.vue';
  import response from './response.vue';
  import debugSetting from './setting.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import apiMethodSelect from '@/views/api-test/components/apiMethodSelect.vue';

  import { getPluginScript, getProtocolList } from '@/api/modules/api-test/management';
  import { getSocket } from '@/api/modules/project-management/commonScript';
  import { getLocalConfig } from '@/api/modules/user/index';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import { filterTree, getGenerateId, parseQueryParams } from '@/utils';
  import { scrollIntoView } from '@/utils/dom';
  import { registerCatchSaveShortcut, removeCatchSaveShortcut } from '@/utils/event';

  import { PluginConfig } from '@/models/apiTest/common';
  import { ExecuteHTTPRequestFullParams } from '@/models/apiTest/debug';
  import { ModuleTreeNode } from '@/models/common';
  import { RequestComposition, RequestMethods, RequestParamsType } from '@/enums/apiEnum';

  import { parseRequestBodyFiles } from '../utils';
  import { Api } from '@form-create/arco-design';

  // 懒加载Http协议组件
  const debugHeader = defineAsyncComponent(() => import('./header.vue'));
  const debugBody = defineAsyncComponent(() => import('./body.vue'));
  const debugQuery = defineAsyncComponent(() => import('./query.vue'));
  const debugRest = defineAsyncComponent(() => import('./rest.vue'));

  export interface RequestCustomAttr {
    isNew: boolean;
    protocol: string;
    activeTab: RequestComposition;
  }
  export type RequestParam = ExecuteHTTPRequestFullParams & RequestCustomAttr & TabItem;

  const props = defineProps<{
    request: RequestParam; // 请求参数集合
    moduleTree: ModuleTreeNode[]; // 模块树
    detailLoading: boolean; // 详情加载状态
    isDefinition?: boolean; // 是否是接口定义模式
    hideResponseLayoutSwitch?: boolean; // 是否隐藏响应体的布局切换
    executeApi: (...args) => Promise<any>; // 执行接口
    createApi: (...args) => Promise<any>; // 创建接口
    updateApi: (...args) => Promise<any>; // 更新接口
    uploadTempFileApi?: (...args) => Promise<any>; // 上传临时文件接口
  }>();
  const emit = defineEmits(['addDone']);

  const appStore = useAppStore();
  const { t } = useI18n();

  const loading = defineModel<boolean>('detailLoading', { default: false });
  const requestVModel = defineModel<RequestParam>('request', { required: true });
  requestVModel.value.executeLoading = false; // 注册loading
  const isHttpProtocol = computed(() => requestVModel.value.protocol === 'HTTP');
  const temporaryResponseMap = {}; // 缓存websocket返回的报告内容，避免执行接口后切换tab导致报告丢失

  watch(
    () => props.request.id,
    () => {
      if (temporaryResponseMap[props.request.reportId]) {
        // 如果有缓存的报告未读取，则直接赋值
        requestVModel.value.response = temporaryResponseMap[props.request.reportId];
        requestVModel.value.executeLoading = false;
        delete temporaryResponseMap[props.request.reportId];
      }
    }
  );

  function handleActiveDebugChange() {
    if (!loading.value) {
      // 如果是因为加载详情触发的change则不需要标记为未保存
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
  const contentTabList = computed(() =>
    isHttpProtocol.value
      ? httpContentTabList
      : [...pluginContentTab, ...httpContentTabList.filter((e) => commonContentTabKey.includes(e.value))]
  );
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
  async function initLocalConfig() {
    try {
      const res = await getLocalConfig();
      const apiLocalExec = res.find((e) => e.type === 'API');
      if (apiLocalExec) {
        hasLocalExec.value = true;
        isPriorityLocalExec.value = apiLocalExec.enable || false;
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const pluginScriptMap = ref<Record<string, PluginConfig>>({}); // 存储初始化过后的插件配置
  const temporaryPluginFormMap: Record<string, any> = {}; // 缓存插件表单，避免切换tab导致动态表单数据丢失
  const pluginLoading = ref(false);
  const isInitPluginForm = ref(false);
  const fApi = ref<Api>();
  const currentPluginOptions = computed<Record<string, any>>(
    () => pluginScriptMap.value[requestVModel.value.protocol]?.options || {}
  );
  const currentPluginScript = computed<Record<string, any>[]>(
    () => pluginScriptMap.value[requestVModel.value.protocol]?.script || []
  );

  // 处理插件表单输入框变化
  const handlePluginFormChange = debounce(() => {
    temporaryPluginFormMap[requestVModel.value.id] = fApi.value?.formData();
    handleActiveDebugChange();
  }, 300);

  /**
   * 设置插件表单数据
   */
  function setPluginFormData() {
    const tempForm = temporaryPluginFormMap[requestVModel.value.id];
    if (tempForm || !requestVModel.value.isNew) {
      // 如果缓存的表单数据存在或者是编辑状态，则需要将之前的输入数据填充
      fApi.value?.nextRefresh(() => {
        fApi.value?.reload(currentPluginScript.value);
        const formData = tempForm || requestVModel.value;
        if (fApi.value) {
          const form = {};
          fApi.value.fields().forEach((key) => {
            form[key] = formData[key];
          });
          fApi.value?.setValue(form);
        }
      });
    } else {
      // 如果是没有缓存也不是编辑，则需要重置表单，因为 form-create 只有一个实例，已经被其他有数据的 tab 污染了，需要重置
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

  watch(
    () => requestVModel.value.id,
    () => {
      if (requestVModel.value.protocol !== 'HTTP') {
        requestVModel.value.activeTab = RequestComposition.PLUGIN;
        initPluginScript();
      }
    },
    {
      immediate: true,
    }
  );

  /**
   *  处理url输入框变化，解析成参数表格
   */
  function handleUrlChange(val: string) {
    const params = parseQueryParams(val.trim());
    if (params.length > 0) {
      requestVModel.value.query.splice(
        0,
        requestVModel.value.query.length - 2,
        ...params.map((e, i) => ({
          id: (new Date().getTime() + i).toString(),
          paramType: RequestParamsType.STRING,
          description: '',
          required: false,
          maxLength: undefined,
          minLength: undefined,
          encode: false,
          enable: true,
          ...e,
        }))
      );
      requestVModel.value.activeTab = RequestComposition.QUERY;
    }
    handleActiveDebugChange();
  }

  const splitBoxSize = ref<string | number>(0.6);
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

  const splitBoxRef = ref<InstanceType<typeof MsSplitBox>>();
  const isExpanded = ref(true);

  function handleExpandChange(val: boolean) {
    isExpanded.value = val;
  }
  function changeExpand(val: boolean) {
    isExpanded.value = val;
    if (val) {
      splitBoxRef.value?.expand(0.6);
    } else {
      splitBoxRef.value?.collapse(
        splitContainerRef.value
          ? `${splitContainerRef.value.clientHeight - (props.hideResponseLayoutSwitch ? 37 : 42)}px`
          : 0
      );
    }
  }

  function handleActiveLayoutChange() {
    isExpanded.value = true;
    splitBoxSize.value = 0.6;
    splitBoxRef.value?.expand(0.6);
  }

  const reportId = ref('');
  const websocket = ref<WebSocket>();
  function debugSocket() {
    websocket.value = getSocket(reportId.value);
    websocket.value.addEventListener('message', (event) => {
      const data = JSON.parse(event.data);
      if (data.msgType === 'EXEC_RESULT') {
        if (requestVModel.value.reportId === data.reportId) {
          // 判断当前查看的tab是否是当前返回的报告的tab，是的话直接赋值
          requestVModel.value.response = data.taskResult;
          requestVModel.value.executeLoading = false;
        } else {
          // 不是则需要把报告缓存起来，等切换到对应的tab再赋值
          temporaryResponseMap[data.reportId] = data.taskResult;
        }
      } else if (data.msgType === 'EXEC_END') {
        // 执行结束，关闭websocket
        websocket.value?.close();
      }
    });
  }

  const saveModalVisible = ref(false);
  const saveModalForm = ref({
    name: '',
    path: requestVModel.value.url || '',
    moduleId: 'root',
  });
  const saveModalFormRef = ref<FormInstance>();
  const saveLoading = ref(false);
  const selectTree = computed(() =>
    filterTree(cloneDeep(props.moduleTree), (e) => {
      e.draggable = false;
      return e.type === 'MODULE';
    })
  );

  watch(
    () => saveModalVisible.value,
    (val) => {
      if (!val) {
        saveModalFormRef.value?.resetFields();
      }
    }
  );

  function makeRequestParams() {
    const { formDataBody, wwwFormBody } = requestVModel.value.body;
    const polymorphicName = protocolOptions.value.find(
      (e) => e.value === requestVModel.value.protocol
    )?.polymorphicName; // 协议多态名称
    let parseRequestBodyResult;
    let requestParams;
    if (isHttpProtocol.value) {
      const realFormDataBodyValues = formDataBody.formValues.filter((e, i) => i !== formDataBody.formValues.length - 1); // 去掉最后一行空行
      const realWwwFormBodyValues = wwwFormBody.formValues.filter((e, i) => i !== wwwFormBody.formValues.length - 1); // 去掉最后一行空行
      parseRequestBodyResult = parseRequestBodyFiles(
        requestVModel.value.body,
        requestVModel.value.uploadFileIds,
        requestVModel.value.linkFileIds
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
        }, // TODO:binaryBody还没对接
        headers: requestVModel.value.headers.filter((e, i) => i !== requestVModel.value.headers.length - 1), // 去掉最后一行空行
        method: requestVModel.value.method,
        otherConfig: requestVModel.value.otherConfig,
        path: requestVModel.value.url,
        query: requestVModel.value.query.filter((e, i) => i !== requestVModel.value.query.length - 1), // 去掉最后一行空行
        rest: requestVModel.value.rest.filter((e, i) => i !== requestVModel.value.rest.length - 1), // 去掉最后一行空行
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
    debugSocket(); // 开启websocket
    return {
      id: requestVModel.value.id.toString(),
      reportId: reportId.value,
      environmentId: '',
      name: requestVModel.value.isNew ? saveModalForm.value.name : requestVModel.value.name,
      moduleId: requestVModel.value.isNew ? saveModalForm.value.moduleId : requestVModel.value.moduleId,
      request: {
        ...requestParams,
        name: requestVModel.value.isNew ? saveModalForm.value.name : requestVModel.value.name,
        children: [
          {
            polymorphicName: 'MsCommonElement', // 协议多态名称，写死MsCommonElement
            assertionConfig: {
              // TODO:暂时不做断言
              enableGlobal: false,
              assertions: [],
            },
            postProcessorConfig: requestVModel.value.children[0].postProcessorConfig,
            preProcessorConfig: requestVModel.value.children[0].preProcessorConfig,
          },
        ],
      },
      ...parseRequestBodyResult,
      projectId: appStore.currentProjectId,
    };
  }

  /**
   * 执行调试
   * @param val 执行类型
   */
  async function execute(execuetType?: 'localExec' | 'serverExec') {
    // TODO:本地&服务端执行判断
    if (isHttpProtocol.value) {
      try {
        requestVModel.value.executeLoading = true;
        await props.executeApi(makeRequestParams());
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
        requestVModel.value.executeLoading = false;
      }
    } else {
      // 插件需要校验动态表单
      fApi.value?.validate(async (valid) => {
        if (valid === true) {
          try {
            requestVModel.value.executeLoading = true;
            await props.executeApi(makeRequestParams());
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

  async function updateDebug() {
    try {
      saveLoading.value = true;
      await props.updateApi({
        ...makeRequestParams(),
        protocol: requestVModel.value.protocol,
        method: isHttpProtocol.value ? requestVModel.value.method : requestVModel.value.protocol,
        deleteFileIds: [], // TODO:删除文件集合
        unLinkRefIds: [], // TODO:取消关联文件集合
      });
      Message.success(t('common.updateSuccess'));
      requestVModel.value.unSaved = false;
      emit('addDone');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      saveLoading.value = false;
    }
  }

  async function handleSave(done: (closed: boolean) => void) {
    saveModalFormRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          saveLoading.value = true;
          if (requestVModel.value.isNew) {
            // 若是新建的调试，走添加
            const res = await props.createApi({
              ...makeRequestParams(),
              ...saveModalForm.value,
              protocol: requestVModel.value.protocol,
              method: isHttpProtocol.value ? requestVModel.value.method : requestVModel.value.protocol,
            });
            requestVModel.value.id = res.id;
            requestVModel.value.isNew = false;
            Message.success(t('common.saveSuccess'));
            requestVModel.value.unSaved = false;
            requestVModel.value.name = saveModalForm.value.name;
            requestVModel.value.label = saveModalForm.value.name;
            saveLoading.value = false;
            saveModalVisible.value = false;
            done(true);
            emit('addDone');
          } else {
            updateDebug();
          }
        } catch (error) {
          saveLoading.value = false;
        }
      }
    });
    done(false);
  }

  async function handleSaveShortcut() {
    if (!requestVModel.value.isNew) {
      // 更新接口不需要弹窗，直接更新保存
      updateDebug();
      return;
    }
    try {
      if (!isHttpProtocol.value) {
        // 插件需要校验动态表单
        await fApi.value?.validate();
      }
      saveModalForm.value = {
        name: requestVModel.value.name || '',
        path: requestVModel.value.url || '',
        moduleId: 'root',
      };
      saveModalVisible.value = true;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      // 校验不通过则不进行保存
      requestVModel.value.activeTab = RequestComposition.PLUGIN;
      nextTick(() => {
        scrollIntoView(document.querySelector('.arco-form-item-message'), { block: 'center' });
      });
    }
  }

  function handleSelect(value: string | number | Record<string, any> | undefined) {
    switch (value) {
      case 'save':
        console.log('save');
        break;
      case 'saveAsCase':
        console.log('saveAsCase');
        break;
      default:
        break;
    }
  }

  function handleCancel() {
    saveModalFormRef.value?.resetFields();
  }

  onBeforeMount(() => {
    initProtocolList();
    initLocalConfig();
  });

  onMounted(() => {
    if (!props.isDefinition) {
      registerCatchSaveShortcut(handleSaveShortcut);
    }
  });

  onBeforeUnmount(() => {
    if (!props.isDefinition) {
      removeCatchSaveShortcut(handleSaveShortcut);
    }
  });
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
</style>
