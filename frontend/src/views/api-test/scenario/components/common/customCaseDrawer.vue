<template>
  <MsDrawer
    v-model:visible="visible"
    unmount-on-close
    :mask="false"
    :width="900"
    :footer="false"
    :show-full-screen="!isShowEditStepNameInput"
    no-content-padding
    @close="handleClose"
  >
    <template #title>
      <stepType v-if="props.activeStep?.type" :type="props.activeStep?.type" class="mr-[4px]" />
      <a-input
        v-show="isShowEditStepNameInput"
        ref="stepNameInputRef"
        v-model:model-value="stepName"
        class="flex-1"
        :placeholder="t('apiScenario.pleaseInputStepName')"
        :max-length="255"
        show-word-limit
        @press-enter="updateStepName"
        @blur="updateStepName"
      />
      <div v-show="!isShowEditStepNameInput" class="flex flex-1 items-center justify-between">
        <div class="flex items-center gap-[8px]">
          <a-tooltip :content="stepName">
            <span> {{ characterLimit(stepName) }}</span>
          </a-tooltip>
          <MsIcon type="icon-icon_edit_outlined" class="edit-script-name-icon" @click="showEditScriptNameInput" />
        </div>
        <div class="right-operation-button-icon flex items-center">
          <MsButton v-permission="['PROJECT_API_DEFINITION_CASE:READ+UPDATE']" type="icon" status="secondary">
            <MsIcon type="icon-icon_swich" />
            {{ t('common.replace') }}
          </MsButton>
          <MsButton
            v-permission="['PROJECT_API_DEFINITION_CASE:READ+UPDATE']"
            class="mr-4"
            type="icon"
            status="secondary"
          >
            <MsIcon type="icon-icon_delete-trash_outlined" />
            {{ t('common.delete') }}
          </MsButton>
        </div>
      </div>
    </template>
    <div class="flex items-center p-[16px]">
      <a-input
        v-model:model-value="requestVModel.name"
        :placeholder="t('apiTestManagement.apiNamePlaceholder')"
        allow-clear
        :max-length="255"
        :show-word-limit="!isQuote"
        :disabled="isQuote"
      />
      <executeButton
        ref="executeRef"
        class="ml-[16px]"
        is-emit
        :detail="requestVModel"
        @execute="handleExecute"
        @stop-debug="stopDebug"
      />
    </div>
    <requestAndResponse
      ref="requestAndResponseRef"
      :detail-loading="loading"
      :disabled-except-param="isQuote"
      :default-params="defaultCaseParams"
      :request="requestVModel"
      :is-priority-local-exec="isPriorityLocalExec"
      :file-save-as-source-id="requestVModel.id"
      :file-module-options-api="getTransferOptionsCase"
      :file-save-as-api="transferFileCase"
      :upload-temp-file="uploadTempFileCase"
      is-show-common-content-tab-key
      @execute="handleExecute"
    />
  </MsDrawer>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import { InputInstance, Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import { ScenarioStepItem } from '../step/stepTree.vue';
  import stepType from './stepType.vue';
  import executeButton from '@/views/api-test/components/executeButton.vue';
  import requestAndResponse from '@/views/api-test/components/requestAndResponse.vue';
  import { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';

  import { localExecuteApiDebug } from '@/api/modules/api-test/common';
  import {
    debugCase,
    getCaseDetail,
    getTransferOptionsCase,
    runCase,
    transferFileCase,
    uploadTempFileCase,
  } from '@/api/modules/api-test/management';
  import { getSocket } from '@/api/modules/project-management/commonScript';
  import { getLocalConfig } from '@/api/modules/user/index';
  import { characterLimit, getGenerateId } from '@/utils';

  import { LocalConfig } from '@/models/user';
  import {
    RequestAuthType,
    RequestComposition,
    RequestMethods,
    ResponseComposition,
    ScenarioStepType,
  } from '@/enums/apiEnum';

  import { defaultBodyParams, defaultResponse, defaultResponseItem } from '@/views/api-test/components/config';
  import { parseRequestBodyFiles } from '@/views/api-test/components/utils';

  const props = defineProps<{
    activeStep?: ScenarioStepItem;
    request?: RequestParam; // 请求参数集合
  }>();
  const emit = defineEmits<{
    (e: 'applyStep', request: RequestParam): void;
  }>();

  const { t } = useI18n();

  const visible = defineModel<boolean>('visible', { required: true });

  const defaultCaseParams: RequestParam = {
    id: `case-${Date.now()}`,
    type: 'case',
    moduleId: 'root',
    protocol: 'HTTP',
    tags: [],
    description: '',
    priority: 'P0',
    url: '',
    activeTab: RequestComposition.HEADER,
    closable: true,
    method: RequestMethods.GET,
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
    responseDefinition: [cloneDeep(defaultResponseItem)],
    isNew: true,
    unSaved: false,
    executeLoading: false,
    preDependency: [], // 前置依赖
    postDependency: [], // 后置依赖
  };

  const isCopyNeedInit = computed(
    () => props.activeStep?.type === ScenarioStepType.COPY_CASE && props.request?.request === null
  );
  const isQuote = computed(() => props.activeStep?.type === ScenarioStepType.QUOTE_CASE);

  const stepName = ref(props.activeStep?.name);
  watchEffect(() => {
    stepName.value = props.activeStep?.name;
  });
  const requestVModel = ref<RequestParam>(cloneDeep(defaultCaseParams));

  const executeRef = ref<InstanceType<typeof executeButton>>();
  const requestAndResponseRef = ref<InstanceType<typeof requestAndResponse>>();
  const isPriorityLocalExec = computed(() => executeRef.value?.isPriorityLocalExec ?? false);

  const apiLocalExec = ref<Record<string, any> | LocalConfig | undefined>({});
  async function initLocalConfig() {
    try {
      const res = await getLocalConfig();
      apiLocalExec.value = res.find((e) => e.type === 'API');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }
  provide('apiLocalExec', readonly(apiLocalExec));

  const isShowEditStepNameInput = ref(false);
  const stepNameInputRef = ref<InputInstance>();
  function showEditScriptNameInput() {
    isShowEditStepNameInput.value = true;
    nextTick(() => {
      stepNameInputRef.value?.focus();
    });
  }
  function updateStepName() {
    // TODO: 更新步骤名称接口
    Message.success(t('common.updateSuccess'));
    isShowEditStepNameInput.value = false;
  }

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
        if (requestVModel.value.reportId === data.reportId) {
          // 判断当前查看的tab是否是当前返回的报告的tab，是的话直接赋值
          requestVModel.value.response = data.taskResult; // 渲染出用例详情和创建用例抽屉的响应数据
          requestVModel.value.executeLoading = false;
        } else {
          // 不是则需要把报告缓存起来，等切换到对应的tab再赋值
          temporaryResponseMap[data.reportId] = data.taskResult;
        }
      } else if (data.msgType === 'EXEC_END') {
        // 执行结束，关闭websocket
        websocket.value?.close();
        requestVModel.value.executeLoading = false;
      }
    });
  }
  async function handleExecute(executeType?: 'localExec' | 'serverExec') {
    try {
      requestVModel.value.executeLoading = true;
      requestVModel.value.response = cloneDeep(defaultResponse);
      const makeRequestParams = requestAndResponseRef.value?.makeRequestParams(executeType); // 写在reportId之前，防止覆盖reportId
      reportId.value = getGenerateId();
      requestVModel.value.reportId = reportId.value; // 存储报告ID
      debugSocket(executeType); // 开启websocket
      let res;
      const params = {
        ...makeRequestParams,
        reportId: reportId.value,
      };
      if (!(requestVModel.value.id as string).startsWith('c') && executeType === 'serverExec') {
        // 已创建的服务端
        res = await runCase(params);
      } else {
        res = await debugCase(params);
      }
      if (executeType === 'localExec') {
        await localExecuteApiDebug(executeRef.value?.localExecuteUrl ?? '', res);
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      requestVModel.value.executeLoading = false;
    }
  }
  function stopDebug() {
    websocket.value?.close();
    requestVModel.value.executeLoading = false;
  }

  function handleClose() {
    emit('applyStep', requestVModel.value);
  }

  const loading = ref(false);
  async function initQuoteCaseDetail() {
    try {
      loading.value = true;
      const res = await getCaseDetail(requestVModel.value.id as string);
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
          requestVModel.value = { ...cloneDeep(defaultCaseParams), ...props.request };
          if (isQuote.value || isCopyNeedInit.value) {
            // 引用时，需要初始化引用的详情；复制只在第一次初始化的时候需要加载后台数据(request.request是复制请求时列表参数字段request会为 null，以此判断释放第一次初始化)
            initQuoteCaseDetail();
          }
        }
        await initLocalConfig();
      }
    }
  );
</script>
