<template>
  <div class="flex h-full flex-col">
    <div class="px-[24px] pt-[16px]">
      <div class="mb-[8px] flex items-center justify-between">
        <div class="flex flex-1">
          <a-select
            v-model:model-value="requsetVModel.protocol"
            :options="protocolOptions"
            :loading="protocolLoading"
            class="mr-[4px] w-[90px]"
            @change="(val) => handleActiveDebugProtocolChange(val as string)"
          />
          <a-input-group v-if="isHttpProtocol" class="flex-1">
            <apiMethodSelect
              v-model:model-value="requsetVModel.method"
              class="w-[140px]"
              @change="handleActiveDebugChange"
            />
            <a-input
              v-model:model-value="requsetVModel.url"
              :max-length="255"
              :placeholder="t('apiTestDebug.urlPlaceholder')"
              @change="handleActiveDebugChange"
            />
          </a-input-group>
        </div>
        <div class="ml-[16px]">
          <a-dropdown-button
            :button-props="{ loading: requsetVModel.executeLoading }"
            :disabled="requsetVModel.executeLoading"
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
          <a-dropdown v-if="props.isDefiniton" @select="handleSelect">
            <a-button type="secondary">{{ t('common.save') }}</a-button>
            <template #content>
              <a-doption value="save">{{ t('common.save') }}</a-doption>
              <a-doption value="saveAsCase">{{ t('apiTestManagement.saveAsCase') }}</a-doption>
            </template>
          </a-dropdown>
          <a-button v-else type="secondary" @click="handleSaveShortcut">
            <div class="flex items-center">
              {{ t('common.save') }}
              <div class="text-[var(--color-text-4)]">(<icon-command size="14" />+S)</div>
            </div>
          </a-button>
        </div>
      </div>
      <a-input
        v-if="props.isDefiniton"
        v-model:model-value="requsetVModel.name"
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
              <a-tabs v-model:active-key="requsetVModel.activeTab" class="no-content mb-[16px]">
                <a-tab-pane v-for="item of contentTabList" :key="item.value" :title="item.label" />
              </a-tabs>
            </div>
            <div class="tab-pane-container">
              <template v-if="isInitPluginForm || requsetVModel.activeTab === RequestComposition.PLUGIN">
                <a-spin v-show="requsetVModel.activeTab === RequestComposition.PLUGIN" :loading="pluginLoading">
                  <MsFormCreate v-model:api="fApi" :rule="currentPluginScript" :option="options" />
                </a-spin>
              </template>
              <debugHeader
                v-if="requsetVModel.activeTab === RequestComposition.HEADER"
                v-model:params="requsetVModel.headers"
                :layout="activeLayout"
                :second-box-height="secondBoxHeight"
                @change="handleActiveDebugChange"
              />
              <debugBody
                v-else-if="requsetVModel.activeTab === RequestComposition.BODY"
                v-model:params="requsetVModel.body"
                :layout="activeLayout"
                :second-box-height="secondBoxHeight"
                @change="handleActiveDebugChange"
              />
              <debugQuery
                v-else-if="requsetVModel.activeTab === RequestComposition.QUERY"
                v-model:params="requsetVModel.query"
                :layout="activeLayout"
                :second-box-height="secondBoxHeight"
                @change="handleActiveDebugChange"
              />
              <debugRest
                v-else-if="requsetVModel.activeTab === RequestComposition.REST"
                v-model:params="requsetVModel.rest"
                :layout="activeLayout"
                :second-box-height="secondBoxHeight"
                @change="handleActiveDebugChange"
              />
              <precondition
                v-else-if="requsetVModel.activeTab === RequestComposition.PRECONDITION"
                v-model:config="requsetVModel.children[0].preProcessorConfig"
                @change="handleActiveDebugChange"
              />
              <postcondition
                v-else-if="requsetVModel.activeTab === RequestComposition.POST_CONDITION"
                v-model:config="requsetVModel.children[0].postProcessorConfig"
                :response="requsetVModel.response.requestResults[0]?.responseResult.body"
                :layout="activeLayout"
                :second-box-height="secondBoxHeight"
                @change="handleActiveDebugChange"
              />
              <debugAuth
                v-else-if="requsetVModel.activeTab === RequestComposition.AUTH"
                v-model:params="requsetVModel.authConfig"
                @change="handleActiveDebugChange"
              />
              <debugSetting
                v-else-if="requsetVModel.activeTab === RequestComposition.SETTING"
                v-model:params="requsetVModel.otherConfig"
                @change="handleActiveDebugChange"
              />
            </div>
          </div>
        </template>
        <template #second>
          <response
            v-model:active-layout="activeLayout"
            v-model:active-tab="requsetVModel.responseActiveTab"
            :is-expanded="isExpanded"
            :response="requsetVModel.response"
            :hide-layout-swicth="props.hideResponseLayoutSwicth"
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
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import debugAuth from './auth.vue';
  import postcondition from './postcondition.vue';
  import precondition from './precondition.vue';
  import response from './response.vue';
  import debugSetting from './setting.vue';
  import apiMethodSelect from '@/views/api-test/components/apiMethodSelect.vue';

  import { getPluginScript, getProtocolList } from '@/api/modules/api-test/management';
  import { getSocket } from '@/api/modules/project-management/commonScript';
  import { getLocalConfig } from '@/api/modules/user/index';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import { filterTree, getGenerateId } from '@/utils';
  import { scrollIntoView } from '@/utils/dom';
  import { registerCatchSaveShortcut, removeCatchSaveShortcut } from '@/utils/event';

  import { ExecuteHTTPRequestFullParams } from '@/models/apiTest/debug';
  import { ModuleTreeNode } from '@/models/common';
  import { RequestComposition } from '@/enums/apiEnum';

  // 懒加载Http协议组件
  const debugHeader = defineAsyncComponent(() => import('./header.vue'));
  const debugBody = defineAsyncComponent(() => import('./body.vue'));
  const debugQuery = defineAsyncComponent(() => import('./query.vue'));
  const debugRest = defineAsyncComponent(() => import('./rest.vue'));

  export type RequestParam = ExecuteHTTPRequestFullParams & TabItem & Record<string, any>;

  const props = defineProps<{
    request: RequestParam; // 请求参数集合
    moduleTree: ModuleTreeNode[]; // 模块树
    detailLoading: boolean; // 详情加载状态
    isDefiniton?: boolean; // 是否是接口定义模式
    hideResponseLayoutSwicth?: boolean; // 是否隐藏响应体的布局切换
    executeApi: (...args) => Promise<any>; // 执行接口
    createApi: (...args) => Promise<any>; // 创建接口
    updateApi: (...args) => Promise<any>; // 更新接口
  }>();
  const emit = defineEmits(['addDone']);

  const appStore = useAppStore();
  const { t } = useI18n();

  const loading = defineModel('detailLoading', { default: false });
  const requsetVModel = defineModel<RequestParam>('request', { required: true });
  requsetVModel.value.executeLoading = false; // 注册loading
  const isHttpProtocol = computed(() => requsetVModel.value.protocol === 'HTTP');
  const isInitPluginForm = ref(false); // 是否初始化过插件表单
  const temporyResponseMap = {}; // 缓存websocket返回的报告内容，避免执行接口后切换tab导致报告丢失

  watch(
    () => requsetVModel.value.protocol,
    (val) => {
      if (val !== 'HTTP') {
        isInitPluginForm.value = true;
      }
    },
    {
      immediate: true,
    }
  );

  watch(
    () => props.request.id,
    () => {
      if (temporyResponseMap[props.request.reportId]) {
        // 如果有缓存的报告未读取，则直接赋值
        requsetVModel.value.response = temporyResponseMap[props.request.reportId];
        requsetVModel.value.executeLoading = false;
        delete temporyResponseMap[props.request.reportId];
      }
    }
  );

  function handleActiveDebugChange() {
    if (!loading.value) {
      // 如果是因为加载详情触发的change则不需要标记为未保存
      requsetVModel.value.unSaved = true;
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

  const pluginScriptMap = ref<Record<string, any>>({}); // 存储初始化过后的插件配置
  const pluginLoading = ref(false);
  const currentPluginScript = computed<Record<string, any>[]>(
    () => pluginScriptMap.value[requsetVModel.value.protocol] || []
  );
  async function initPluginScript() {
    if (pluginScriptMap.value[requsetVModel.value.protocol] !== undefined) {
      // 已经初始化过
      return;
    }
    try {
      pluginLoading.value = true;
      const res = await getPluginScript(
        protocolOptions.value.find((e) => e.value === requsetVModel.value.protocol)?.pluginId || ''
      );
      pluginScriptMap.value[requsetVModel.value.protocol] = res.script;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      pluginLoading.value = false;
    }
  }

  function handleActiveDebugProtocolChange(val: string) {
    if (val !== 'HTTP') {
      requsetVModel.value.activeTab = RequestComposition.PLUGIN;
      initPluginScript();
    } else {
      requsetVModel.value.activeTab = RequestComposition.HEADER;
    }
    handleActiveDebugChange();
  }

  const fApi = ref();
  const options = {
    form: {
      labelAlign: 'right',
      autoLabelWidth: true,
      size: 'small',
      hideRequiredAsterisk: false,
      showMessage: true,
      inlineMessage: false,
      scrollToFirstError: true,
    },
    submitBtn: false,
    resetBtn: false,
  };

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
          ? `${splitContainerRef.value.clientHeight - (props.hideResponseLayoutSwicth ? 37 : 42)}px`
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
        if (requsetVModel.value.reportId === data.reportId) {
          // 判断当前查看的tab是否是当前返回的报告的tab，是的话直接赋值
          requsetVModel.value.response = data.taskResult;
          requsetVModel.value.executeLoading = false;
        } else {
          // 不是则需要把报告缓存起来，等切换到对应的tab再赋值
          temporyResponseMap[data.reportId] = data.taskResult;
        }
      }
    });
  }

  function makeRequestParams() {
    const polymorphicName = protocolOptions.value.find(
      (e) => e.value === requsetVModel.value.protocol
    )?.polymorphicName; // 协议多态名称
    let requestParams;
    if (isHttpProtocol.value) {
      requestParams = {
        authConfig: requsetVModel.value.authConfig,
        body: {
          ...requsetVModel.value.body,
          binaryBody: undefined,
          formDataBody: {
            formValues: requsetVModel.value.body.formDataBody.formValues.filter(
              (e, i) => i !== requsetVModel.value.body.formDataBody.formValues.length - 1
            ), // 去掉最后一行空行
          },
          wwwFormBody: {
            formValues: requsetVModel.value.body.wwwFormBody.formValues.filter(
              (e, i) => i !== requsetVModel.value.body.wwwFormBody.formValues.length - 1
            ), // 去掉最后一行空行
          },
        }, // TODO:binaryBody还没对接
        headers: requsetVModel.value.headers.filter((e, i) => i !== requsetVModel.value.headers.length - 1), // 去掉最后一行空行
        method: requsetVModel.value.method,
        otherConfig: requsetVModel.value.otherConfig,
        path: requsetVModel.value.url,
        query: requsetVModel.value.query.filter((e, i) => i !== requsetVModel.value.query.length - 1), // 去掉最后一行空行
        rest: requsetVModel.value.rest.filter((e, i) => i !== requsetVModel.value.rest.length - 1), // 去掉最后一行空行
        url: requsetVModel.value.url,
        polymorphicName,
      };
    } else {
      requestParams = {
        ...fApi.value.form,
        polymorphicName,
      };
    }
    reportId.value = getGenerateId();
    requsetVModel.value.reportId = reportId.value; // 存储报告ID
    debugSocket(); // 开启websocket
    return {
      id: requsetVModel.value.id.toString(),
      reportId: reportId.value,
      environmentId: '',
      tempFileIds: [],
      request: {
        ...requestParams,
        children: [
          {
            polymorphicName: 'MsCommonElement', // 协议多态名称，写死MsCommonElement
            assertionConfig: {
              // TODO:暂时不做断言
              enableGlobal: false,
              assertions: [],
            },
            postProcessorConfig: requsetVModel.value.children[0].postProcessorConfig,
            preProcessorConfig: requsetVModel.value.children[0].preProcessorConfig,
          },
        ],
      },
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
        requsetVModel.value.executeLoading = true;
        await props.executeApi(makeRequestParams());
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
        requsetVModel.value.executeLoading = false;
      }
    } else {
      // 插件需要校验动态表单
      fApi.value?.validate(async (valid) => {
        if (valid === true) {
          try {
            requsetVModel.value.executeLoading = true;
            await props.executeApi(makeRequestParams());
          } catch (error) {
            // eslint-disable-next-line no-console
            console.log(error);
            requsetVModel.value.executeLoading = false;
          }
        } else {
          requsetVModel.value.activeTab = RequestComposition.PLUGIN;
          nextTick(() => {
            scrollIntoView(document.querySelector('.arco-form-item-message'), { block: 'center' });
          });
        }
      });
    }
  }

  const saveModalVisible = ref(false);
  const saveModalForm = ref({
    name: '',
    path: requsetVModel.value.url || '',
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

  async function handleSaveShortcut() {
    try {
      if (!isHttpProtocol.value) {
        // 插件需要校验动态表单
        await fApi.value?.validate();
      }
      saveModalForm.value = {
        name: requsetVModel.value.name || '',
        path: requsetVModel.value.url || '',
        moduleId: 'root',
      };
      saveModalVisible.value = true;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      // 校验不通过则不进行保存
      requsetVModel.value.activeTab = RequestComposition.PLUGIN;
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

  async function handleSave(done: (closed: boolean) => void) {
    saveModalFormRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          saveLoading.value = true;
          if (requsetVModel.value.isNew) {
            // 若是新建的调试，走添加
            await props.createApi({
              ...makeRequestParams(),
              ...saveModalForm.value,
              protocol: requsetVModel.value.protocol,
              method: isHttpProtocol.value ? requsetVModel.value.method : requsetVModel.value.protocol,
              uploadFileIds: [],
              linkFileIds: [],
            });
          } else {
            await props.updateApi({
              ...makeRequestParams(),
              ...saveModalForm.value,
              protocol: requsetVModel.value.protocol,
              method: isHttpProtocol.value ? requsetVModel.value.method : requsetVModel.value.protocol,
              uploadFileIds: [],
              linkFileIds: [],
              deleteFileIds: [], // TODO:删除文件集合
              unLinkRefIds: [], // TODO:取消关联文件集合
            });
          }
          saveLoading.value = false;
          saveModalVisible.value = false;
          done(true);
          requsetVModel.value.unSaved = false;
          requsetVModel.value.name = saveModalForm.value.name;
          requsetVModel.value.label = saveModalForm.value.name;
          emit('addDone');
          Message.success(requsetVModel.value.isNew ? t('common.saveSuccess') : t('common.updateSuccess'));
        } catch (error) {
          saveLoading.value = false;
        }
      }
    });
    done(false);
  }

  onBeforeMount(() => {
    initProtocolList();
    initLocalConfig();
  });

  onMounted(() => {
    if (!props.isDefiniton) {
      registerCatchSaveShortcut(handleSaveShortcut);
    }
  });

  onBeforeUnmount(() => {
    if (!props.isDefiniton) {
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
