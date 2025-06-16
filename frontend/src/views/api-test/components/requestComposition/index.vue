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
  <div v-show="!pluginError || isHttpProtocol" class="request-composition flex h-full flex-col">
    <div v-if="!props.isCase" class="mb-[8px] px-[18px] pt-[8px]">
      <div class="flex flex-wrap items-baseline justify-between gap-[12px]">
        <div class="flex flex-1 flex-wrap items-center gap-[16px]">
          <a-select
            v-if="requestVModel.isNew && !isHttpProtocol"
            v-model:model-value="requestVModel.protocol"
            :loading="protocolLoading"
            class="w-[100px]"
            @change="(val) => handleActiveDebugProtocolChange(val as string)"
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
          <div v-else-if="!requestVModel.isNew" class="flex items-center gap-[4px]">
            <apiMethodName
              :method="(requestVModel.protocol as RequestMethods)"
              tag-background-color="rgb(var(--link-7))"
              tag-text-color="white"
              is-tag
              class="flex items-center"
            />
            <a-tooltip v-if="!isHttpProtocol" :content="requestVModel.label" :mouse-enter-delay="500">
              <div class="one-line-text max-w-[350px]">
                {{ requestVModel.label }}
              </div>
            </a-tooltip>
          </div>
          <a-input-group v-if="isHttpProtocol" class="flex-1">
            <a-select
              v-if="requestVModel.isNew"
              v-model:model-value="requestVModel.protocol"
              :loading="protocolLoading"
              class="w-[100px] hover:z-10"
              @change="(val) => handleActiveDebugProtocolChange(val as string)"
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
            <apiMethodSelect
              v-model:model-value="requestVModel.method"
              class="w-[120px]"
              @change="handleActiveDebugChange"
            />
            <a-input
              ref="urlInputRef"
              v-model:model-value="requestVModel.url"
              :max-length="255"
              :placeholder="
                props.isDefinition ? t('apiTestDebug.definitionUrlPlaceholder') : t('apiTestDebug.urlPlaceholder')
              "
              allow-clear
              class="flex-1 hover:z-10"
              :style="isUrlError ? 'border: 1px solid rgb(var(--danger-6));z-index: 10' : ''"
              @input="() => (isUrlError = false)"
              @change="handleUrlChange"
            >
              <template v-if="!props.isDefinition" #suffix>
                <MsIcon
                  type="icon-icon_curl"
                  :size="18"
                  class="cursor-pointer hover:text-[rgb(var(--primary-5))]"
                  @click="emit('import')"
                />
              </template>
            </a-input>
          </a-input-group>
          <div v-if="isUrlError" class="url-input-tip">
            <span>{{ t('apiTestDebug.apiUrlRequired') }}</span>
          </div>
        </div>
        <div>
          <a-radio-group
            v-if="props.isDefinition"
            v-model:model-value="requestVModel.mode"
            type="button"
            class="mr-[12px]"
            size="medium"
          >
            <a-radio value="definition">{{ t('apiTestManagement.definition') }}</a-radio>
            <a-radio value="debug">{{ t('apiTestManagement.debug') }}</a-radio>
          </a-radio-group>
          <!-- 接口定义-调试模式下，可执行 -->
          <template
            v-if="
              (!props.isDefinition || (props.isDefinition && requestVModel.mode === 'debug')) &&
              props.permissionMap &&
              hasAnyPermission([props.permissionMap.execute])
            "
          >
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
              :disabled="requestVModel.executeLoading || (isHttpProtocol && !requestVModel.url)"
              @click="() => execute('serverExec')"
            >
              {{ t('apiTestDebug.serverExec') }}
            </a-button>
            <a-button v-else type="primary" class="mr-[12px]" @click="stopDebug">
              {{ t('common.stop') }}
            </a-button>
          </template>
          <!-- 接口定义-且有保存或更新权限 -->
          <template
            v-if="
              props.isDefinition &&
              (requestVModel.isNew
                ? props.permissionMap && hasAnyPermission([props.permissionMap.create])
                : props.permissionMap && hasAnyPermission([props.permissionMap.update]))
            "
          >
            <!-- 接口定义-调试模式，可保存或保存为新用例 -->
            <a-dropdown-button
              v-if="requestVModel.mode === 'debug'"
              type="outline"
              class="arco-btn-group-outline--secondary"
              :disabled="(isHttpProtocol && !requestVModel.url) || saveLoading"
              @click="() => handleSelect('save')"
            >
              {{ t('common.save') }}
              <template #icon>
                <icon-down />
              </template>
              <template #content>
                <a-doption value="saveAsCase" @click="() => handleSelect('saveAsCase')">
                  {{ t('apiTestManagement.saveAsCase') }}
                </a-doption>
              </template>
            </a-dropdown-button>
            <!-- 接口定义-定义模式，直接保存接口定义 -->
            <a-button v-else type="primary" :loading="saveLoading" @click="() => handleSelect('save')">
              {{ t('common.save') }}
            </a-button>
          </template>
          <!-- 接口调试，支持快捷保存 -->
          <template
            v-else-if="
              requestVModel.isNew
                ? props.permissionMap && hasAnyPermission([props.permissionMap.create])
                : props.permissionMap && hasAnyPermission([props.permissionMap.update])
            "
          >
            <!-- 接口调试-可保存或保存为新接口定义 -->
            <a-dropdown-button
              v-if="
                props.permissionMap &&
                props.permissionMap.saveASApi &&
                hasAllPermission([props.permissionMap.create, props.permissionMap.saveASApi])
              "
              :disabled="(isHttpProtocol && !requestVModel.url) || saveLoading"
              type="outline"
              class="arco-btn-group-outline--secondary"
              @click="handleSaveShortcut"
            >
              <div class="flex items-center">
                {{ t('common.save') }}
                <div class="text-[var(--color-text-4)]">(<icon-command size="14" />+S)</div>
              </div>
              <template #icon>
                <icon-down />
              </template>
              <template #content>
                <a-doption value="saveAsApi" @click="() => handleSelect('saveAsApi')">
                  {{ t('apiTestDebug.saveAsApi') }}
                </a-doption>
              </template>
            </a-dropdown-button>
            <a-button
              v-else
              type="secondary"
              :disabled="isHttpProtocol && !requestVModel.url"
              :loading="saveLoading"
              @click="handleSaveShortcut"
            >
              <div class="flex items-center">
                {{ t('common.save') }}
                <div class="text-[var(--color-text-4)]">(<icon-command size="14" />+S)</div>
              </div>
            </a-button>
          </template>
        </div>
        <div v-if="props.isDefinition" class="w-full">
          <a-input
            ref="nameInputRef"
            v-model:model-value="requestVModel.name"
            :max-length="255"
            :placeholder="t('apiTestManagement.apiNamePlaceholder')"
            :style="isNameError ? 'border: 1px solid rgb(var(--danger-6));z-index: 10' : ''"
            allow-clear
            @input="() => (isNameError = false)"
            @change="handleActiveDebugChange"
          />
          <div v-if="isNameError" class="name-input-tip">
            <span>{{ t('apiTestManagement.apiNameRequired') }}</span>
          </div>
        </div>
      </div>
    </div>
    <div :class="`${!props.isCase ? 'request-tab-and-response' : ''} flex-1`">
      <MsTab
        v-model:active-key="requestVModel.activeTab"
        :content-tab-list="contentTabList"
        :get-text-func="getTabBadge"
        class="sticky-content no-content relative top-0 px-[16px]"
        @tab-click="requestTabClick"
      />
      <div :class="`request-content-and-response ${activeLayout}`">
        <a-spin class="request" :loading="requestVModel.executeLoading || loading">
          <div class="request-tab-pane flex flex-col p-[16px] pt-[8px]">
            <apiBaseForm
              v-if="!props.isCase && props.isDefinition"
              v-show="requestVModel.activeTab === RequestComposition.BASE_INFO"
              ref="apiBaseFormRef"
              v-model:request-v-model="requestVModel"
              :select-tree="selectTree as ModuleTreeNode[]"
            />
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
              @change="handleActiveDebugChange"
            />
            <httpBody
              v-else-if="requestVModel.activeTab === RequestComposition.BODY"
              v-model:params="requestVModel.body"
              :upload-temp-file-api="props.uploadTempFileApi"
              :file-save-as-source-id="props.fileSaveAsSourceId"
              :file-save-as-api="props.fileSaveAsApi"
              :file-module-options-api="props.fileModuleOptionsApi"
              :is-debug="requestVModel.mode === 'debug'"
              :hide-json-schema="props.hideJsonSchema"
              :is-case="props.isCase"
              @change="handleActiveDebugChange"
            />
            <httpQuery
              v-else-if="requestVModel.activeTab === RequestComposition.QUERY"
              v-model:params="requestVModel.query"
              @change="handleActiveDebugChange"
            />
            <httpRest
              v-else-if="requestVModel.activeTab === RequestComposition.REST"
              v-model:params="requestVModel.rest"
              @change="handleActiveDebugChange"
            />
            <precondition
              v-else-if="requestVModel.activeTab === RequestComposition.PRECONDITION"
              v-model:config="requestVModel.children[0].preProcessorConfig"
              :is-definition="props.isDefinition"
              @change="handleActiveDebugChange"
            />
            <postcondition
              v-else-if="requestVModel.activeTab === RequestComposition.POST_CONDITION"
              v-model:config="requestVModel.children[0].postProcessorConfig"
              :response="requestVModel.response?.requestResults[0]?.responseResult.body"
              :is-definition="props.isDefinition"
              @change="handleActiveDebugChange"
            />
            <assertion
              v-else-if="requestVModel.activeTab === RequestComposition.ASSERTION"
              v-model:params="requestVModel.children[0].assertionConfig.assertions"
              :is-definition="props.isDefinition"
              :response="requestVModel.response?.requestResults[0]?.responseResult.body"
              :assertion-config="requestVModel.children[0].assertionConfig"
              :show-extraction="true"
              @change="handleActiveDebugChange"
            />
            <auth
              v-else-if="requestVModel.activeTab === RequestComposition.AUTH"
              v-model:params="requestVModel.authConfig"
              @change="handleActiveDebugChange"
            />
            <setting
              v-else-if="requestVModel.activeTab === RequestComposition.SETTING"
              v-model:params="requestVModel.otherConfig"
              @change="handleActiveDebugChange"
            />
          </div>
        </a-spin>
        <response
          v-show="showResponse"
          ref="responseRef"
          v-model:active-layout="activeLayout"
          v-model:active-tab="requestVModel.responseActiveTab"
          v-model:response-definition="requestVModel.responseDefinition"
          class="response"
          :show-response-result-button="requestVModel.mode === 'debug'"
          :is-http-protocol="isHttpProtocol"
          :is-priority-local-exec="isPriorityLocalExec"
          :request-url="requestVModel.url"
          :is-expanded="isVerticalExpanded"
          :hide-layout-switch="props.hideResponseLayoutSwitch"
          :request-result="requestVModel.response?.requestResults[0]"
          :console="requestVModel.response?.console"
          :is-edit="props.isDefinition && isHttpProtocol && !props.isCase"
          :upload-temp-file-api="props.uploadTempFileApi"
          :loading="requestVModel.executeLoading || loading"
          :is-definition="props.isDefinition"
          @change="handleActiveDebugChange"
          @execute="(executeType) => (props.isCase ? emit('execute', executeType) : execute(executeType))"
        />
      </div>
    </div>
  </div>
  <a-modal
    v-if="!isCase"
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
        <a-input
          v-model:model-value="saveModalForm.name"
          :max-length="255"
          :placeholder="t('apiTestDebug.requestNamePlaceholder')"
        />
      </a-form-item>
      <a-form-item
        v-if="isHttpProtocol"
        field="path"
        :label="t('apiTestDebug.requestUrl')"
        :rules="[{ required: true, message: t('apiTestDebug.requestUrlRequired') }]"
        asterisk-position="end"
      >
        <a-input
          v-model:model-value="saveModalForm.path"
          :max-length="255"
          :placeholder="t('apiTestDebug.commonPlaceholder')"
        />
      </a-form-item>
      <a-form-item :label="t('apiTestDebug.requestModule')" class="mb-0">
        <a-tree-select
          v-model:model-value="saveModalForm.moduleId"
          :data="selectTree as ModuleTreeNode[]"
          :field-names="{ title: 'name', key: 'id', children: 'children' }"
          :tree-props="{
            virtualListProps: {
              height: 200,
              threshold: 200,
            },
          }"
          :filter-tree-node="filterTreeNode"
          allow-search
        >
          <template #tree-slot-title="node">
            <a-tooltip :content="`${node.name}`" position="tl">
              <div class="one-line-text w-[300px]">{{ node.name }}</div>
            </a-tooltip>
          </template>
        </a-tree-select>
      </a-form-item>
    </a-form>
  </a-modal>
  <a-modal
    v-if="!isCase"
    v-model:visible="saveCaseModalVisible"
    :title="t('apiTestManagement.saveAsCase')"
    :ok-loading="saveCaseLoading"
    class="ms-modal-form"
    title-align="start"
    body-class="!p-0"
    @before-ok="saveAsCase"
    @cancel="handleSaveCaseCancel"
  >
    <a-form ref="saveCaseModalFormRef" :model="saveCaseModalForm" layout="vertical">
      <a-form-item
        field="name"
        :label="t('case.caseName')"
        :rules="[{ required: true, message: t('case.caseNameRequired') }]"
        asterisk-position="end"
      >
        <a-input
          v-model:model-value="saveCaseModalForm.name"
          :placeholder="t('case.caseNamePlaceholder')"
          :max-length="255"
        />
      </a-form-item>
      <a-form-item field="priority" :label="t('case.caseLevel')">
        <a-select v-model:model-value="saveCaseModalForm.priority" :options="casePriorityOptions"></a-select>
      </a-form-item>
      <a-form-item field="status" :label="t('common.status')">
        <a-select v-model:model-value="saveCaseModalForm.status">
          <a-option v-for="item in caseStatusOptions" :key="item.value" :value="item.value">
            {{ t(item.label) }}
          </a-option>
        </a-select>
      </a-form-item>
      <a-form-item field="tags" :label="t('common.tag')">
        <MsTagsInput
          v-model:model-value="saveCaseModalForm.tags"
          placeholder="common.tagsInputPlaceholder"
          allow-clear
          unique-value
          retain-input-value
        />
      </a-form-item>
    </a-form>
  </a-modal>
  <saveAsApiModal
    v-if="tempApiDetail"
    v-model:visible="saveNewApiModalVisible"
    :detail="tempApiDetail"
  ></saveAsApiModal>
  <addDependencyDrawer v-if="props.isDefinition" v-model:visible="showAddDependencyDrawer" :mode="addDependencyMode" />
</template>

<script setup lang="ts">
  // TODO:代码拆分，结构优化
  import { FormInstance, InputInstance, Message, SelectOptionData } from '@arco-design/web-vue';
  import { cloneDeep, debounce } from 'lodash-es';

  import { TabItem } from '@/components/pure/ms-editable-tab/types';
  import MsFormCreate from '@/components/pure/ms-form-create/formCreate.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import { parseTableDataToJsonSchema } from '@/components/pure/ms-json-schema/utils';
  import MsTab from '@/components/pure/ms-tab/index.vue';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import assertion from '@/components/business/ms-assertion/index.vue';
  import auth from './auth.vue';
  import postcondition from './postcondition.vue';
  import precondition from './precondition.vue';
  import response from './response/index.vue';
  import setting from './setting.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import apiMethodSelect from '@/views/api-test/components/apiMethodSelect.vue';
  import saveAsApiModal from '@/views/api-test/components/saveAsApiModal.vue';
  import apiBaseForm from '@/views/api-test/management/components/management/api/apiBaseForm.vue';

  import { getPluginScript, getProtocolList } from '@/api/modules/api-test/common';
  import { addCase } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import useShortcutSave from '@/hooks/useShortcutSave';
  import useWebsocket from '@/hooks/useWebsocket';
  import useRequestCompositionStore from '@/store/modules/api/requestComposition';
  import useAppStore from '@/store/modules/app';
  import useUserStore from '@/store/modules/user';
  import { filterTree, filterTreeNode, getGenerateId, parseQueryParams } from '@/utils';
  import { scrollIntoView } from '@/utils/dom';
  import { hasAllPermission, hasAnyPermission } from '@/utils/permission';

  import {
    ExecuteApiRequestFullParams,
    ExecuteRequestParams,
    PluginConfig,
    RequestTaskResult,
  } from '@/models/apiTest/common';
  import { AddApiCaseParams } from '@/models/apiTest/management';
  import { ModuleTreeNode, TransferFileParams } from '@/models/common';
  import {
    ProtocolKeyEnum,
    RequestAuthType,
    RequestBodyFormat,
    RequestCaseStatus,
    RequestComposition,
    RequestMethods,
  } from '@/enums/apiEnum';

  import type { ResponseItem } from './response/edit.vue';
  import {
    casePriorityOptions,
    caseStatusOptions,
    defaultBodyParamsItem,
    defaultHeaderParamsItem,
    defaultKeyValueParamItem,
    defaultRequestParamsItem,
    defaultResponse,
  } from '@/views/api-test/components/config';
  import {
    filterAssertions,
    filterConditionsSqlValidParams,
    filterKeyValParams,
    parseRequestBodyFiles,
  } from '@/views/api-test/components/utils';
  import type { Api } from '@form-create/arco-design';

  // 懒加载Http协议组件
  const httpHeader = defineAsyncComponent(() => import('./header.vue'));
  const httpBody = defineAsyncComponent(() => import('./body.vue'));
  const httpQuery = defineAsyncComponent(() => import('./query.vue'));
  const httpRest = defineAsyncComponent(() => import('./rest.vue'));
  const addDependencyDrawer = defineAsyncComponent(
    () => import('@/views/api-test/management/components/addDependencyDrawer.vue')
  );

  export interface TabErrorMessage {
    value: string;
    label: string;
    messageList: string[];
  }

  export interface RequestCustomAttr {
    type: 'api' | 'case' | 'mock' | 'doc'; // 展示的请求 tab 类型；api包含了接口调试和接口定义
    isNew: boolean;
    protocol: string;
    activeTab: RequestComposition;
    mode?: 'definition' | 'debug'; // 接口定义时，展示的定义模式/调试模式（显示的 tab 不同）
    executeLoading: boolean; // 执行中loading
    isCopy?: boolean; // 是否是复制
    isExecute?: boolean; // 是否是执行
    errorMessageInfo?: {
      [key: string]: Record<string, any>;
    };
  }
  export type RequestParam = ExecuteApiRequestFullParams & {
    responseDefinition?: ResponseItem[];
    response?: RequestTaskResult;
  } & RequestCustomAttr &
    TabItem;

  const props = defineProps<{
    request: RequestParam; // 请求参数集合
    protocolKey?: ProtocolKeyEnum; // 用于记住协议下拉值
    moduleTree?: ModuleTreeNode[]; // 模块树
    isCase?: boolean; // 是否是用例引用的组件,只显示请求参数和响应内容,响应内容默认为空且折叠
    apiDetail?: RequestParam; // 用例引用的时候需要接口定义的数据
    detailLoading?: boolean; // 详情加载状态
    isDefinition?: boolean; // 是否是接口定义模式
    hideResponseLayoutSwitch?: boolean; // 是否隐藏响应体的布局切换
    otherParams?: Record<string, any>; // 保存请求时的其他参数
    hideJsonSchema?: boolean; // 是否隐藏json schema
    executeApi?: (params: ExecuteRequestParams) => Promise<any>; // 执行接口
    localExecuteApi?: (url: string, params: ExecuteRequestParams) => Promise<any>; // 本地执行接口
    createApi?: (...args: any) => Promise<any>; // 创建接口
    updateApi?: (...args: any) => Promise<any>; // 更新接口
    uploadTempFileApi?: (...args: any) => Promise<any>; // 上传临时文件接口
    fileSaveAsSourceId?: string | number; // 文件转存关联的资源id
    fileSaveAsApi?: (params: TransferFileParams) => Promise<string>; // 文件转存接口
    fileModuleOptionsApi?: (projectId: string) => Promise<ModuleTreeNode[]>; // 文件转存目录下拉框接口
    permissionMap?: {
      execute: string;
      create: string;
      update: string;
      saveASApi?: string;
    };
  }>();
  const emit = defineEmits<{
    (e: 'execute', executeType: 'localExec' | 'serverExec'): void;
    (e: 'addDone'): void;
    (e: 'requestTabClick'): void;
    (e: 'import'): void;
  }>();

  const appStore = useAppStore();
  const userStore = useUserStore();
  const { t } = useI18n();
  const requestCompositionStore = useRequestCompositionStore();

  const loading = defineModel<boolean>('detailLoading', { default: false });
  const requestVModel = defineModel<RequestParam>('request', { required: true });

  const isHttpProtocol = computed(() => requestVModel.value.protocol === 'HTTP');
  const temporaryResponseMap: Record<string, any> = {}; // 缓存websocket返回的报告内容，避免执行接口后切换tab导致报告丢失
  const isInitPluginForm = ref(false);
  const isSwitchingContent = ref(false); // 是否正在切换显示内容，防止因切换显示内容导致触发更改

  function handleActiveDebugChange() {
    if ((!isSwitchingContent.value && !loading.value) || (!isHttpProtocol.value && isInitPluginForm.value)) {
      // 如果是因为加载详情或切换显示内容触发的change则不需要标记为未保存；或者是插件协议的话需要等待表单初始化完毕
      requestVModel.value.unSaved = true;
    }
  }

  // 基本信息tab
  const baseInfoContentTab = [
    {
      value: RequestComposition.BASE_INFO,
      label: t('apiScenario.baseInfo'),
    },
  ];
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
  // 根据协议类型获取请求内容tab
  const contentTabList = computed(() => {
    // HTTP 协议 tabs
    if (isHttpProtocol.value) {
      if (props.isCase) {
        return httpContentTabList;
      }
      if (props.isDefinition) {
        // 接口定义，定义模式隐藏前后置、断言
        return requestVModel.value.mode === 'debug'
          ? [...baseInfoContentTab, ...httpContentTabList]
          : [...baseInfoContentTab, ...httpContentTabList.filter((e) => !commonContentTabKey.includes(e.value))];
      }
      // 接口调试无断言
      return httpContentTabList.filter((e) => e.value !== RequestComposition.ASSERTION);
    }
    // 插件 tabs
    if (props.isDefinition && !props.isCase) {
      // 接口定义，定义模式隐藏前后置、断言
      return requestVModel.value.mode === 'definition'
        ? [...baseInfoContentTab, ...pluginContentTab]
        : [
            ...baseInfoContentTab,
            ...pluginContentTab,
            ...httpContentTabList.filter((e) => commonContentTabKey.includes(e.value)),
          ];
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
        const queryNum = filterKeyValParams(requestVModel.value.query || [], defaultRequestParamsItem).validParams
          .length;
        return `${queryNum > 0 ? queryNum : ''}`;
      case RequestComposition.REST:
        const restNum = filterKeyValParams(requestVModel.value.rest || [], defaultRequestParamsItem).validParams.length;
        return `${restNum > 0 ? restNum : ''}`;
      case RequestComposition.PRECONDITION:
        return `${
          requestVModel.value.children[0]?.preProcessorConfig.processors.length > 99
            ? '99+'
            : requestVModel.value.children[0]?.preProcessorConfig.processors.length || ''
        }`;
      case RequestComposition.POST_CONDITION:
        return `${
          requestVModel.value.children[0]?.postProcessorConfig.processors.length > 99
            ? '99+'
            : requestVModel.value.children[0]?.postProcessorConfig.processors.length || ''
        }`;
      case RequestComposition.ASSERTION:
        return `${
          requestVModel.value.children[0]?.assertionConfig.assertions.length > 99
            ? '99+'
            : requestVModel.value.children[0]?.assertionConfig.assertions.length || ''
        }`;
      case RequestComposition.AUTH:
        return requestVModel.value.authConfig.authType !== RequestAuthType.NONE ? '1' : '';
      default:
        return '';
    }
  }
  function requestTabClick() {
    if (!props.isCase) {
      const element = document.querySelector('.request-tab-and-response');
      element?.scrollTo({
        top: 0,
        behavior: 'smooth',
      });
    } else {
      emit('requestTabClick');
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

  const hasLocalExec = computed(() => userStore.hasLocalExec); // 是否配置了api本地执行
  const isPriorityLocalExec = computed(() => userStore.isPriorityLocalExec); // 是否优先本地执行
  const localExecuteUrl = computed(() => userStore.localExecuteUrl); // 本地执行地址

  const pluginScriptMap = ref<Record<string, PluginConfig>>({}); // 存储初始化过后的插件配置
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
    if (props.isDefinition) {
      // 接口定义使用接口定义的字段集
      // 根据 apiDefinitionFields 字段集合展示需要的字段，隐藏其他字段
      fields = pluginScriptMap.value[requestVModel.value.protocol].apiDefinitionFields || [];
    } else {
      // 根据 apiDebugFields 字段集合展示需要的字段，隐藏其他字段
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

  // 处理插件表单输入框变化
  const handlePluginFormChange = debounce(() => {
    requestCompositionStore.setPluginFormMap(requestVModel.value.id, fApi.value?.formData());
    controlPluginFormFields(); // TODO:临时解决插件表单通过表单交互控制字段显隐未隐藏/显示环境字段的问题
    handleActiveDebugChange();
  }, 300);

  /**
   * 设置插件表单数据
   */
  async function setPluginFormData() {
    const tempForm = requestCompositionStore.pluginFormMap[requestVModel.value.id];
    if (tempForm || !requestVModel.value.isNew || requestVModel.value.isCopy) {
      // 如果缓存的表单数据存在或者是编辑状态，则需要将之前的输入数据填充
      const formData = tempForm || requestVModel.value;
      if (fApi.value) {
        fApi.value.nextTick(() => {
          // 这里使用nextTick是因为插件表单使用v-if动态渲染，所以每次切换到插件表单时都会重新渲染插件表单并触发
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
        isInitPluginForm.value = true;
      });
    }
    await nextTick();
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
      await setPluginFormData();
      // 已经初始化过
      return;
    }
    try {
      pluginLoading.value = true;
      const res = await getPluginScript(pluginId);
      pluginScriptMap.value[requestVModel.value.protocol] = res;
      await setPluginFormData();
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
    if (props.protocolKey) {
      localStorage.setItem(props.protocolKey, requestVModel.value.protocol);
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
    () =>
      isHttpProtocol.value ||
      !props.isDefinition ||
      requestVModel.value.response?.requestResults[0]?.responseResult ||
      props.isCase
  );
  const activeLayout = ref<'horizontal' | 'vertical'>('vertical');
  const responseRef = ref<InstanceType<typeof response>>();
  const isVerticalExpanded = computed(() => activeLayout.value === 'vertical');
  function changeVerticalExpand(val: boolean) {
    responseRef.value?.changeExpand(val);
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

  const saveModalVisible = ref(false);
  const saveModalForm = ref({
    name: '',
    path: requestVModel.value.url,
    moduleId: 'root',
  });
  const saveModalFormRef = ref<FormInstance>();
  const saveLoading = ref(false);
  const selectTree = computed(() => {
    if (
      requestVModel.value.activeTab === RequestComposition.BASE_INFO ||
      saveModalVisible.value ||
      (!props.isCase && props.isDefinition && saveModalVisible.value)
    ) {
      // 切换到基础信息 tab、调试模式打开保存弹窗，或者是接口定义模式下打开保存弹窗才进行计算，避免大数据量导致进入时就计算卡顿 TODO:worker线程处理计算任务
      return filterTree(cloneDeep(props.moduleTree || []), (e) => {
        e.draggable = false;
        return e.type === 'MODULE';
      });
    }
    return [];
  });

  watch(
    () => saveModalVisible.value,
    (val) => {
      if (!val) {
        saveModalFormRef.value?.resetFields();
      }
    }
  );

  const reportId = ref('');
  const websocket = ref<WebSocket>();
  /**
   * 开启websocket监听，接收执行结果
   */
  async function debugSocket(executeType?: 'localExec' | 'serverExec') {
    const { createSocket, websocket: _websocket } = useWebsocket({
      reportId: reportId.value,
      socketUrl: executeType === 'localExec' ? '/ws/debug' : '',
      host: executeType === 'localExec' ? localExecuteUrl.value : '',
      onMessage: (event) => {
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
          if (requestVModel.value.reportId === data.reportId) {
            requestVModel.value.executeLoading = false;
            requestVModel.value.isExecute = false;
          }
        }
      },
    });
    await createSocket();
    websocket.value = _websocket.value;
  }

  /**
   * 生成请求参数
   * @param executeType 执行类型，执行时传入
   */
  async function makeRequestParams(executeType?: 'localExec' | 'serverExec') {
    const isExecute = executeType === 'localExec' || executeType === 'serverExec';
    const { formDataBody, wwwFormBody, jsonBody } = requestVModel.value.body;
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
          jsonBody: {
            jsonValue: jsonBody.jsonValue,
            enableJsonSchema: jsonBody.enableJsonSchema,
            jsonSchema: jsonBody.jsonSchemaTableData
              ? parseTableDataToJsonSchema(jsonBody.jsonSchemaTableData[0])
              : undefined,
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
    if (isExecute && !props.isCase) {
      await debugSocket(executeType); // 开启websocket
    }
    let requestName = '';
    let requestModuleId = '';
    let apiDefinitionParams: Record<string, any> = {};
    if (props.isDefinition) {
      // 接口定义有响应内容定义
      requestName = requestVModel.value.name;
      requestModuleId = requestVModel.value.moduleId;
      apiDefinitionParams = {
        tags: requestVModel.value.tags,
        description: requestVModel.value.description,
        status: requestVModel.value.status,
        response: requestVModel.value.responseDefinition?.map((e) => ({
          ...e,
          headers: filterKeyValParams(e.headers, defaultKeyValueParamItem, isExecute).validParams,
          body: {
            ...e.body,
            jsonBody: {
              jsonValue: e.body.jsonBody.jsonValue,
              enableJsonSchema: jsonBody.enableJsonSchema,
              jsonSchema: e.body.jsonBody.jsonSchemaTableData
                ? parseTableDataToJsonSchema(e.body.jsonBody.jsonSchemaTableData[0])
                : undefined,
            },
          },
        })),
      };
    } else {
      requestName = requestVModel.value.isNew ? saveModalForm.value.name : requestVModel.value.name;
      requestModuleId = requestVModel.value.isNew ? saveModalForm.value.moduleId : requestVModel.value.moduleId;
    }

    // 处理断言参数
    const { assertionConfig } = requestVModel.value.children[0];
    return {
      id: requestVModel.value.id.toString(),
      reportId: reportId.value,
      environmentId: appStore.currentEnvConfig?.id || '',
      name: requestName,
      moduleId: requestModuleId,
      num: requestVModel.value.num,
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
            assertionConfig: {
              ...assertionConfig,
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

  /**
   * 执行调试
   * @param val 执行类型
   */
  async function execute(executeType?: 'localExec' | 'serverExec') {
    if (isHttpProtocol.value) {
      try {
        if (!props.executeApi) return;
        await nextTick();
        requestVModel.value.executeLoading = true;
        requestVModel.value.response = cloneDeep(defaultResponse);
        const res = await props.executeApi((await makeRequestParams(executeType)) as ExecuteRequestParams);
        if (executeType === 'localExec' && props.localExecuteApi && localExecuteUrl.value) {
          await props.localExecuteApi(localExecuteUrl.value, res);
        }
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
        websocket.value?.close();
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
            const res = await props.executeApi((await makeRequestParams(executeType)) as ExecuteRequestParams);
            if (executeType === 'localExec' && props.localExecuteApi && localExecuteUrl.value) {
              await props.localExecuteApi(localExecuteUrl.value, res);
            }
          } catch (error) {
            // eslint-disable-next-line no-console
            console.log(error);
            websocket.value?.close();
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

  watch(
    () => requestVModel.value.id,
    async () => {
      isSwitchingContent.value = true; // 正在切换内容
      if (requestVModel.value.protocol !== 'HTTP') {
        requestVModel.value.activeTab = RequestComposition.PLUGIN;
        if (protocolOptions.value.length === 0) {
          // 还没初始化过协议列表，则初始化；在这里初始化是为了阻塞脚本的初始化，避免脚本初始化时协议列表还没初始化
          await initProtocolList();
        }
        await initPluginScript();
      } else {
        setDefaultActiveTab();
        if (protocolOptions.value.length === 0) {
          await initProtocolList();
        }
      }
      if (!props.isCase) {
        responseRef.value?.setActiveResponse(requestVModel.value.mode === 'debug' ? 'result' : 'content');
      }
      if (props.request.isExecute && !requestVModel.value.executeLoading) {
        // 如果是执行操作打开接口详情，且该接口不在执行状态中，则立即执行
        if (requestVModel.value.protocol !== 'HTTP') {
          setTimeout(() => {
            execute(isPriorityLocalExec.value ? 'localExec' : 'serverExec');
          }, 100);
        } else {
          execute(isPriorityLocalExec.value ? 'localExec' : 'serverExec');
        }
      } else if (temporaryResponseMap[props.request.reportId]) {
        // 如果有缓存的报告未读取，则直接赋值
        requestVModel.value.response = temporaryResponseMap[props.request.reportId];
        requestVModel.value.executeLoading = false;
        delete temporaryResponseMap[props.request.reportId];
      }
      nextTick(() => {
        isSwitchingContent.value = false; // 切换内容结束
      });
    },
    {
      immediate: true,
    }
  );

  watch(
    () => requestVModel.value?.mode,
    (val: 'debug' | 'definition' | undefined) => {
      if (val) {
        responseRef.value?.setActiveResponse(val === 'debug' ? 'result' : 'content');
      }
    }
  );

  async function updateRequest() {
    try {
      if (!props.updateApi) return;
      saveLoading.value = true;
      const requestParams = await makeRequestParams();
      const res = await props.updateApi({
        ...requestParams,
        ...props.otherParams,
      });
      Message.success(t('common.updateSuccess'));
      requestVModel.value.updateTime = res.updateTime;
      requestVModel.value.unSaved = false;
      const parseRequestBodyResult = parseRequestBodyFiles(
        requestVModel.value.body,
        requestVModel.value.responseDefinition
      );
      requestVModel.value.uploadFileIds = parseRequestBodyResult.uploadFileIds;
      requestVModel.value.linkFileIds = parseRequestBodyResult.linkFileIds;
      emit('addDone');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      saveLoading.value = false;
    }
  }

  /**
   * 保存请求
   * @param fullParams 保存时传入的参数
   * @param silence 是否静默保存（接口定义另存为用例时要先静默保存接口）
   */
  async function realSave(fullParams?: Record<string, any>, silence?: boolean) {
    try {
      if (!props.createApi) return;
      if (!silence) {
        saveLoading.value = true;
      }
      let params;
      const requestParams = await makeRequestParams();
      if (props.isDefinition) {
        params = {
          ...(fullParams || requestParams),
          ...props.otherParams,
        };
      } else {
        params = {
          ...(fullParams || requestParams),
          ...saveModalForm.value,
          path: isHttpProtocol.value ? saveModalForm.value.path : undefined,
          ...props.otherParams,
        };
      }
      const res = await props.createApi(params);
      if (!silence) {
        Message.success(t('common.saveSuccess'));
      }
      requestVModel.value.id = res.id;
      requestVModel.value.num = res.num;
      requestVModel.value.isNew = false;
      requestVModel.value.unSaved = false;
      requestVModel.value.name = res.name;
      requestVModel.value.label = res.name;
      requestVModel.value.url = res.path;
      requestVModel.value.path = res.path;
      requestVModel.value.moduleId = res.moduleId;
      if (!isHttpProtocol.value) {
        requestVModel.value = {
          ...requestVModel.value,
          ...fApi.value?.formData(), // 存储插件表单数据
          uploadFileIds: requestParams.uploadFileIds,
          linkFileIds: requestParams.linkFileIds,
        };
      } else {
        requestVModel.value.uploadFileIds = requestParams.uploadFileIds;
        requestVModel.value.linkFileIds = requestParams.linkFileIds;
      }
      if (!props.isDefinition) {
        saveModalVisible.value = false;
      }
      if (!silence) {
        saveLoading.value = false;
        emit('addDone');
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      saveLoading.value = false;
    }
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

  function handleSave(done: (closed: boolean) => void) {
    saveModalFormRef.value?.validate(async (errors) => {
      if (!errors) {
        await realSave();
        done(true);
      }
    });
    done(false);
  }

  /**
   * 保存快捷键处理
   */
  async function handleSaveShortcut() {
    if (isHttpProtocol.value && !requestVModel.value.url) {
      return;
    }
    try {
      if (!isHttpProtocol.value) {
        // 插件需要校验动态表单
        await fApi.value?.validate();
      }
      // 检查全部的校验信息
      if (getFlattenedMessages()?.length) {
        showMessage();
        return;
      }
      if (!requestVModel.value.isNew) {
        // 更新接口不需要弹窗，直接更新保存
        updateRequest();
        return;
      }
      if (!props.isDefinition) {
        // 接口调试需要弹窗保存
        saveModalForm.value = {
          name: requestVModel.value.name || '',
          path: requestVModel.value.url || '',
          moduleId: 'root',
        };
        saveModalVisible.value = true;
      } else {
        realSave();
      }
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

  const saveCaseModalVisible = ref(false);
  const saveCaseLoading = ref(false);
  const saveCaseModalForm = ref({
    name: '',
    priority: 'P0',
    status: RequestCaseStatus.PROCESSING,
    tags: [],
  });
  const saveCaseModalFormRef = ref<FormInstance>();

  function handleSaveCaseCancel() {
    saveCaseModalForm.value = {
      name: '',
      priority: 'P0',
      status: RequestCaseStatus.PROCESSING,
      tags: [],
    };
    saveCaseModalVisible.value = false;
  }

  // 保存为用例
  function saveAsCase(done: (closed: boolean) => void) {
    saveCaseModalFormRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          saveCaseLoading.value = true;
          const definitionParams = await makeRequestParams();
          if (requestVModel.value.isNew) {
            // 未保存过的接口保存为用例，先保存接口定义，再保存为用例
            await realSave(definitionParams, true);
            done(true);
          }
          if (!requestVModel.value.isNew) {
            const params: AddApiCaseParams = {
              ...definitionParams,
              ...saveCaseModalForm.value,
              projectId: appStore.currentProjectId,
              environmentId: appStore.currentEnvConfig?.id || '',
              apiDefinitionId: requestVModel.value.id,
              uploadFileIds: definitionParams.uploadFileIds || [],
              linkFileIds: definitionParams.linkFileIds || [],
            };
            await addCase(params);
            emit('addDone');
            Message.success(t('common.saveSuccess'));
            handleSaveCaseCancel();
            saveCaseLoading.value = false;
            done(true);
          }
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
          done(false);
        } finally {
          handleSaveCaseCancel();
          saveCaseLoading.value = false;
        }
      } else {
        saveCaseLoading.value = false;
        done(false);
      }
    });
  }

  // 未保存过的接口保存为用例，先保存接口定义，再保存为用例
  async function saveNewDefinition() {
    try {
      if (!isHttpProtocol.value) {
        // 插件需要校验动态表单
        await fApi.value?.validate();
      }
      saveCaseModalVisible.value = true;
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

  const apiBaseFormRef = ref<InstanceType<typeof apiBaseForm>>();
  const isUrlError = ref(false);
  const isNameError = ref(false);
  const tempApiDetail = ref<RequestParam>();
  const saveNewApiModalVisible = ref(false);

  async function handleSelect(value: string | number | Record<string, any> | undefined) {
    if (requestVModel.value.url === '' && requestVModel.value.protocol === 'HTTP') {
      isUrlError.value = true;
      return;
    }
    if (requestVModel.value.name === '') {
      isNameError.value = true;
      return;
    }
    isUrlError.value = false;
    isNameError.value = false;
    if (value === 'saveAsApi') {
      const params = await makeRequestParams();
      tempApiDetail.value = {
        ...params,
        ...params.request,
        polymorphicName: params.request.polymorphicName,
      } as unknown as RequestParam;
      saveNewApiModalVisible.value = true;
      return;
    }
    apiBaseFormRef.value?.formRef?.validate(async (errors) => {
      if (errors) {
        requestVModel.value.activeTab = RequestComposition.BASE_INFO;
      } else {
        // 检查全部的校验信息
        if (getFlattenedMessages()?.length) {
          showMessage();
          return;
        }
        switch (value) {
          case 'save':
            handleSaveShortcut();
            break;
          case 'saveAsCase':
            saveNewDefinition();
            break;
          default:
            break;
        }
      }
    });
  }

  // const fApi = ref();
  // const options = {
  //   form: {
  //     layout: 'vertical',
  //     labelPosition: 'right',
  //     size: 'small',
  //     labelWidth: '00px',
  //     hideRequiredAsterisk: false,
  //     showMessage: true,
  //     inlineMessage: false,
  //     scrollToFirstError: true,
  //   },
  //   submitBtn: false,
  //   resetBtn: false,
  // };
  // const currentApiTemplateRules = [];
  const showAddDependencyDrawer = ref(false);
  const addDependencyMode = ref<'pre' | 'post'>('pre');

  // function handleDddDependency(value: string | number | Record<string, any> | undefined) {
  //   switch (value) {
  //     case 'pre':
  //       addDependencyMode.value = 'pre';
  //       showAddDependencyDrawer.value = true;
  //       break;
  //     case 'post':
  //       addDependencyMode.value = 'post';
  //       showAddDependencyDrawer.value = true;
  //       break;
  //     default:
  //       break;
  //   }
  // }

  // function clearAllDependency() {
  //   activeApiTab.value.preDependency = [];
  //   activeApiTab.value.postDependency = [];
  // }

  function handleCancel() {
    saveModalFormRef.value?.resetFields();
  }

  const nameInputRef = ref<InputInstance>();
  const urlInputRef = ref<InputInstance>();
  function inputBlur() {
    urlInputRef.value?.blur();
    nameInputRef.value?.blur();
  }
  const { registerCatchSaveShortcut, removeCatchSaveShortcut } = useShortcutSave(() => {
    inputBlur(); // 先失焦再保存
    if (!props.isDefinition) {
      handleSaveShortcut();
    } else {
      handleSelect('save');
    }
  });

  onMounted(() => {
    if (
      !props.isCase &&
      (requestVModel.value.isNew
        ? props.permissionMap && hasAnyPermission([props.permissionMap.create])
        : props.permissionMap && hasAnyPermission([props.permissionMap.update]))
    ) {
      registerCatchSaveShortcut();
    }
  });

  onBeforeUnmount(() => {
    if (!props.isCase) {
      removeCatchSaveShortcut();
    }
  });

  defineExpose({
    execute,
    makeRequestParams,
    changeVerticalExpand,
    getFlattenedMessages,
    showMessage,
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
  .url-input-tip {
    @apply w-full;

    margin-top: -14px;
    padding-left: 226px;
    font-size: 12px;
    color: rgb(var(--danger-6));
    line-height: 16px;
  }
  .name-input-tip {
    @apply w-full;

    margin-top: 4px;
    font-size: 12px;
    color: rgb(var(--danger-6));
    line-height: 16px;
  }
  .request-tab-and-response {
    overflow-x: hidden;
    overflow-y: auto;
    .ms-scroll-bar();
  }
  .sticky-content {
    @apply sticky;

    z-index: 101; // .arco-scrollbar-track是100
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
    &.horizontal {
      flex-direction: row;
      min-height: calc(100% - 49px); // 49px:请求参数tab高度
      .request {
        flex: 1;
        overflow-x: auto;
        border-right: 1px solid var(--color-text-n8);
        .ms-scroll-bar();
        .request-tab-pane {
          min-width: 800px;
        }
      }
      .response {
        width: 500px;
      }
    }
  }
</style>
