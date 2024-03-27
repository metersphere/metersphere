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
            <span> {{ characterLimit(activeStep?.name) }}</span>
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
        :execute-loading="requestVModel.executeLoading"
        @execute="handleExecute"
        @stop-debug="stopDebug"
      />
    </div>
    <requestAndResponse
      ref="requestAndResponseRef"
      :detail-loading="loading"
      :disabled-except-param="isQuote"
      :disabled-param-value="isQuote"
      :request="requestVModel"
      :is-priority-local-exec="isPriorityLocalExec"
      :file-save-as-source-id="requestVModel.resourceId"
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
  import { InputInstance } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import stepType from './stepType/stepType.vue';
  import executeButton from '@/views/api-test/components/executeButton.vue';
  import requestAndResponse from '@/views/api-test/components/requestAndResponse.vue';
  import { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';

  import {
    getCaseDetail,
    getTransferOptionsCase,
    transferFileCase,
    uploadTempFileCase,
  } from '@/api/modules/api-test/management';
  import { characterLimit } from '@/utils';

  import { RequestResult } from '@/models/apiTest/common';
  import { ScenarioStepItem } from '@/models/apiTest/scenario';
  import {
    RequestAuthType,
    RequestComposition,
    RequestMethods,
    ResponseComposition,
    ScenarioStepRefType,
    ScenarioStepType,
  } from '@/enums/apiEnum';

  import { defaultBodyParams, defaultResponse, defaultResponseItem } from '@/views/api-test/components/config';
  import { parseRequestBodyFiles } from '@/views/api-test/components/utils';

  const props = defineProps<{
    request?: RequestParam; // 请求参数集合
    stepResponses?: Record<string | number, RequestResult>;
  }>();
  const emit = defineEmits<{
    (e: 'applyStep', request: RequestParam): void;
    (e: 'deleteStep'): void;
    (e: 'execute', request: RequestParam, executeType?: 'localExec' | 'serverExec'): void;
    (e: 'stopDebug'): void;
  }>();

  const { t } = useI18n();

  const visible = defineModel<boolean>('visible', { required: true });
  const activeStep = defineModel<ScenarioStepItem>('activeStep', {
    required: false,
  });

  const defaultCaseParams: RequestParam = {
    id: `case-${Date.now()}`,
    resourceId: '',
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

  const requestVModel = ref<RequestParam>(props.request || cloneDeep(defaultCaseParams));
  const isCopyCase = computed(
    () =>
      activeStep.value?.stepType === ScenarioStepType.API_CASE && activeStep.value?.refType === ScenarioStepRefType.COPY
  );
  const isCopyNeedInit = computed(() => isCopyCase.value && props.request === undefined);
  const isQuote = computed(
    () =>
      activeStep.value?.stepType === ScenarioStepType.API_CASE && activeStep.value?.refType === ScenarioStepRefType.REF
  );
  const isHttpProtocol = computed(() => requestVModel.value.protocol === 'HTTP');

  const stepName = ref(activeStep.value?.name);
  watchEffect(() => {
    stepName.value = activeStep.value?.name;
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

  const executeRef = ref<InstanceType<typeof executeButton>>();
  const requestAndResponseRef = ref<InstanceType<typeof requestAndResponse>>();
  const isPriorityLocalExec = computed(() => executeRef.value?.isPriorityLocalExec ?? false);

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

  /**
   * 执行调试
   * @param val 执行类型
   */
  async function handleExecute(executeType?: 'localExec' | 'serverExec') {
    requestVModel.value.executeLoading = true;
    if (isHttpProtocol.value) {
      emit('execute', requestAndResponseRef.value?.makeRequestParams(executeType), executeType);
    } else {
      // 插件需要校验动态表单
      // fApi.value?.validate(async (valid) => {
      //   if (valid === true) {
      //     emit('execute', requestAndResponseRef.value?.makeRequestParams(executeType), executeType);
      //   } else {
      //     requestVModel.value.activeTab = RequestComposition.PLUGIN;
      //     nextTick(() => {
      //       scrollIntoView(document.querySelector('.arco-form-item-message'), { block: 'center' });
      //     });
      //   }
      // });
    }
  }

  function stopDebug() {
    emit('stopDebug');
  }

  function handleClose() {
    if (!isQuote.value) {
      emit('applyStep', { ...requestVModel.value, ...requestAndResponseRef.value?.makeRequestParams() });
    }
  }

  function handleDelete() {
    emit('deleteStep');
  }

  const loading = ref(false);
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
        ...parseRequestBodyResult,
      };
      nextTick(() => {
        requestAndResponseRef.value?.setActiveTabByFirst();
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
        requestVModel.value = {
          ...cloneDeep(defaultCaseParams),
          ...props.request,
          response: {
            requestResults: [props.stepResponses?.[props.request?.stepId] || defaultResponse.requestResults[0]],
            console: props.stepResponses?.[props.request?.stepId]?.console || '',
          },
        };
        if (isQuote.value || isCopyNeedInit.value) {
          // 引用时，需要初始化引用的详情；复制只在第一次初始化的时候需要加载后台数据(request.request是复制请求时列表参数字段request会为 null，以此判断释放第一次初始化)
          initQuoteCaseDetail();
        }
      }
    }
  );
</script>
