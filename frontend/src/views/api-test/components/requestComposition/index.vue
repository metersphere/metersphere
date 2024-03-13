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
    <div class="px-[18px] pt-[8px]">
      <div class="flex flex-wrap items-center justify-between gap-[12px]">
        <div class="flex flex-1 items-center gap-[16px]">
          <a-select
            v-if="requestVModel.isNew"
            v-model:model-value="requestVModel.protocol"
            :options="protocolOptions"
            :loading="protocolLoading"
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
              <div class="one-line-text max-w-[350px]">
                {{ requestVModel.label }}
              </div>
            </a-tooltip>
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
              :placeholder="
                props.isDefinition ? t('apiTestDebug.definitionUrlPlaceholder') : t('apiTestDebug.urlPlaceholder')
              "
              allow-clear
              class="hover:z-10"
              :style="isUrlError ? 'border: 1px solid rgb(var(--danger-6);z-index: 10' : ''"
              @input="() => (isUrlError = false)"
              @change="handleUrlChange"
            />
          </a-input-group>
        </div>
        <div>
          <a-radio-group
            v-if="props.isDefinition"
            v-model:model-value="requestVModel.mode"
            type="button"
            class="mr-[12px]"
          >
            <a-radio value="definition">{{ t('apiTestManagement.definition') }}</a-radio>
            <a-radio value="debug">{{ t('apiTestManagement.debug') }}</a-radio>
          </a-radio-group>
          <!-- 接口定义-调试模式下，可执行 -->
          <template
            v-if="
              (!props.isDefinition || (props.isDefinition && requestVModel.mode === 'debug')) &&
              hasAnyPermission([props.permissionMap.execute])
            "
          >
            <a-dropdown-button
              v-if="!requestVModel.executeLoading"
              :disabled="requestVModel.executeLoading || (isHttpProtocol && !requestVModel.url)"
              class="exec-btn"
              @click="() => execute(isPriorityLocalExec ? 'localExec' : 'serverExec')"
              @select="execute"
            >
              {{ isPriorityLocalExec ? t('apiTestDebug.localExec') : t('apiTestDebug.serverExec') }}
              <template v-if="hasLocalExec" #icon>
                <icon-down />
              </template>
              <template v-if="hasLocalExec" #content>
                <a-doption :value="isPriorityLocalExec ? 'serverExec' : 'localExec'">
                  {{ isPriorityLocalExec ? t('apiTestDebug.serverExec') : t('apiTestDebug.localExec') }}
                </a-doption>
              </template>
            </a-dropdown-button>
            <a-button v-else type="primary" class="mr-[12px]" @click="stopDebug">{{ t('common.stop') }}</a-button>
          </template>
          <!-- 接口定义-且有保存或更新权限 -->
          <template
            v-if="
              props.isDefinition &&
              (requestVModel.isNew
                ? hasAnyPermission([props.permissionMap.create])
                : hasAnyPermission([props.permissionMap.update]))
            "
          >
            <!-- 接口定义-调试模式，可保存或保存为新用例 -->
            <a-dropdown
              v-if="requestVModel.mode === 'debug'"
              :loading="saveLoading || (isHttpProtocol && !requestVModel.url)"
              @select="handleSelect"
            >
              <a-button type="secondary">
                {{ t('common.save') }}
              </a-button>
              <template #content>
                <a-doption value="save">{{ t('common.save') }}</a-doption>
                <a-doption value="saveAsCase">{{ t('apiTestManagement.saveAsCase') }}</a-doption>
              </template>
            </a-dropdown>
            <!-- 接口定义-定义模式，直接保存接口定义 -->
            <a-button v-else type="primary" :loading="saveLoading" @click="() => handleSelect('save')">
              {{ t('common.save') }}
            </a-button>
          </template>
          <!-- 接口调试，支持快捷保存 -->
          <a-button
            v-else-if="
              requestVModel.isNew
                ? hasAnyPermission([props.permissionMap.create])
                : hasAnyPermission([props.permissionMap.update])
            "
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
        </div>
      </div>
    </div>
    <div class="px-[16px]">
      <MsTab
        v-model:active-key="requestVModel.activeTab"
        :content-tab-list="contentTabList"
        :get-text-func="getTabBadge"
        class="no-content relative mt-[8px]"
      />
    </div>
    <div ref="splitContainerRef" class="h-[calc(100%-97px)]">
      <MsSplitBox
        ref="horizontalSplitBoxRef"
        :size="props.isDefinition ? 0.7 : 1"
        :max="props.isDefinition ? 0.9 : 1"
        :min="props.isDefinition ? 0.7 : 1"
        :disabled="!props.isDefinition"
        :class="!props.isDefinition ? 'hidden-second' : ''"
        :first-container-class="!props.isDefinition ? 'border-r-0' : ''"
        direction="horizontal"
        expand-direction="right"
      >
        <template #first>
          <MsSplitBox
            ref="verticalSplitBoxRef"
            v-model:size="splitBoxSize"
            :max="!showResponse ? 1 : 0.98"
            min="10px"
            :direction="activeLayout"
            second-container-class="!overflow-y-hidden"
            :class="!showResponse ? 'hidden-second' : ''"
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
                      :layout="activeLayout"
                      :second-box-height="secondBoxHeight"
                      @change="handleActiveDebugChange"
                    />
                    <httpBody
                      v-else-if="requestVModel.activeTab === RequestComposition.BODY"
                      v-model:params="requestVModel.body"
                      :layout="activeLayout"
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
                      :second-box-height="secondBoxHeight"
                      @change="handleActiveDebugChange"
                    />
                    <httpRest
                      v-else-if="requestVModel.activeTab === RequestComposition.REST"
                      v-model:params="requestVModel.rest"
                      :layout="activeLayout"
                      :second-box-height="secondBoxHeight"
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
                      :layout="activeLayout"
                      :second-box-height="secondBoxHeight"
                      :is-definition="props.isDefinition"
                      @change="handleActiveDebugChange"
                    />
                    <assertion
                      v-else-if="requestVModel.activeTab === RequestComposition.ASSERTION"
                      v-model:params="requestVModel.children[0].assertionConfig.assertions"
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
                </div>
              </a-spin>
            </template>
            <template #second>
              <response
                v-show="showResponse"
                v-model:active-layout="activeLayout"
                v-model:active-tab="requestVModel.responseActiveTab"
                v-model:response-definition="requestVModel.responseDefinition"
                :is-http-protocol="isHttpProtocol"
                :is-priority-local-exec="isPriorityLocalExec"
                :request-url="requestVModel.url"
                :is-expanded="isVerticalExpanded"
                :hide-layout-switch="props.hideResponseLayoutSwitch"
                :request-task-result="requestVModel.response"
                :is-edit="props.isDefinition && isHttpProtocol"
                :upload-temp-file-api="props.uploadTempFileApi"
                :loading="requestVModel.executeLoading || loading"
                @change-expand="changeVerticalExpand"
                @change-layout="handleActiveLayoutChange"
                @change="handleActiveDebugChange"
                @execute="execute"
              />
            </template>
          </MsSplitBox>
        </template>
        <template v-if="props.isDefinition" #second>
          <div class="p-[16px]">
            <!-- TODO:第一版没有模板 -->
            <!-- <MsFormCreate v-model:api="fApi" :rule="currentApiTemplateRules" :option="options" /> -->
            <a-form ref="activeApiTabFormRef" :model="requestVModel" layout="vertical">
              <a-form-item
                field="name"
                :label="t('apiTestManagement.apiName')"
                class="mb-[16px]"
                :rules="[{ required: true, message: t('apiTestManagement.apiNameRequired') }]"
              >
                <a-input
                  v-model:model-value="requestVModel.name"
                  :max-length="255"
                  :placeholder="t('apiTestManagement.apiNamePlaceholder')"
                  allow-clear
                  @change="handleActiveApiChange"
                />
              </a-form-item>
              <a-form-item :label="t('apiTestManagement.belongModule')" class="mb-[16px]">
                <a-tree-select
                  v-model:modelValue="requestVModel.moduleId"
                  :data="selectTree"
                  :field-names="{ title: 'name', key: 'id', children: 'children' }"
                  :tree-props="{
                    virtualListProps: {
                      height: 200,
                      threshold: 200,
                    },
                  }"
                  allow-search
                  @change="handleActiveApiChange"
                />
              </a-form-item>
              <a-form-item :label="t('apiTestManagement.apiStatus')" class="mb-[16px]">
                <a-select
                  v-model:model-value="requestVModel.status"
                  :placeholder="t('common.pleaseSelect')"
                  class="param-input w-full"
                  @change="handleActiveApiChange"
                >
                  <template #label>
                    <apiStatus :status="requestVModel.status" />
                  </template>
                  <a-option v-for="item of Object.values(RequestDefinitionStatus)" :key="item" :value="item">
                    <apiStatus :status="item" />
                  </a-option>
                </a-select>
              </a-form-item>
              <a-form-item :label="t('common.tag')" class="mb-[16px]">
                <MsTagsInput v-model:model-value="requestVModel.tags" @change="handleActiveApiChange" />
              </a-form-item>
              <a-form-item :label="t('common.desc')" class="mb-[16px]">
                <a-textarea
                  v-model:model-value="requestVModel.description"
                  :max-length="1000"
                  @change="handleActiveApiChange"
                />
              </a-form-item>
            </a-form>
            <!-- TODO:第一版先不做依赖 -->
            <!-- <div class="mb-[8px] flex items-center">
                  <div class="text-[var(--color-text-2)]">
                    {{ t('apiTestManagement.addDependency') }}
                  </div>
                  <a-divider margin="4px" direction="vertical" />
                  <MsButton
                    type="text"
                    class="font-medium"
                    :disabled="requestVModel.preDependency.length === 0 && requestVModel.postDependency.length === 0"
                    @click="clearAllDependency"
                  >
                    {{ t('apiTestManagement.clearSelected') }}
                  </MsButton>
                </div>
                <div class="rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)] p-[12px]">
                  <div class="flex items-center">
                    <div class="flex items-center gap-[4px] text-[var(--color-text-2)]">
                      {{ t('apiTestManagement.preDependency') }}
                      <div class="text-[rgb(var(--primary-5))]">
                        {{ requestVModel.preDependency.length }}
                      </div>
                      {{ t('apiTestManagement.dependencyUnit') }}
                    </div>
                    <a-divider margin="8px" direction="vertical" />
                    <MsButton type="text" class="font-medium" @click="handleDddDependency('pre')">
                      {{ t('apiTestManagement.addPreDependency') }}
                    </MsButton>
                  </div>
                  <div class="mt-[8px] flex items-center">
                    <div class="flex items-center gap-[4px] text-[var(--color-text-2)]">
                      {{ t('apiTestManagement.postDependency') }}
                      <div class="text-[rgb(var(--primary-5))]">
                        {{ requestVModel.postDependency.length }}
                      </div>
                      {{ t('apiTestManagement.dependencyUnit') }}
                    </div>
                    <a-divider margin="8px" direction="vertical" />
                    <MsButton type="text" class="font-medium" @click="handleDddDependency('post')">
                      {{ t('apiTestManagement.addPostDependency') }}
                    </MsButton>
                  </div>
                </div> -->
          </div>
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
          v-model:modelValue="saveModalForm.moduleId"
          :data="selectTree"
          :field-names="{ title: 'name', key: 'id', children: 'children' }"
          :tree-props="{
            virtualListProps: {
              height: 200,
              threshold: 200,
            },
          }"
          allow-search
        />
      </a-form-item>
    </a-form>
  </a-modal>
  <a-modal
    v-model:visible="saveCaseModalVisible"
    :title="t('common.save')"
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
        <a-input v-model:model-value="saveCaseModalForm.name" :placeholder="t('case.caseNamePlaceholder')" />
      </a-form-item>
      <a-form-item field="priority" :label="t('case.caseLevel')">
        <a-select v-model:model-value="saveCaseModalForm.priority" :options="casePriorityOptions"></a-select>
      </a-form-item>
      <a-form-item field="status" :label="t('common.status')">
        <a-select v-model:model-value="saveCaseModalForm.status" :options="caseStatusOptions"></a-select>
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
  <addDependencyDrawer v-if="props.isDefinition" v-model:visible="showAddDependencyDrawer" :mode="addDependencyMode" />
</template>

<script setup lang="ts">
  import { FormInstance, Message, SelectOptionData } from '@arco-design/web-vue';
  import { cloneDeep, debounce } from 'lodash-es';

  import { TabItem } from '@/components/pure/ms-editable-tab/types';
  import MsFormCreate from '@/components/pure/ms-form-create/formCreate.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
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
  import apiStatus from '@/views/api-test/components/apiStatus.vue';

  import { getPluginScript, getProtocolList } from '@/api/modules/api-test/common';
  import { addCase } from '@/api/modules/api-test/management';
  import { getSocket } from '@/api/modules/project-management/commonScript';
  import { getLocalConfig } from '@/api/modules/user/index';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import { filterTree, getGenerateId, parseQueryParams } from '@/utils';
  import { scrollIntoView } from '@/utils/dom';
  import { registerCatchSaveShortcut, removeCatchSaveShortcut } from '@/utils/event';
  import { hasAnyPermission } from '@/utils/permission';

  import {
    ExecuteApiRequestFullParams,
    ExecuteConditionConfig,
    ExecuteRequestParams,
    PluginConfig,
    RequestTaskResult,
  } from '@/models/apiTest/common';
  import { AddApiCaseParams } from '@/models/apiTest/management';
  import { ModuleTreeNode, TransferFileParams } from '@/models/common';
  import { EnvConfig } from '@/models/projectManagement/environmental';
  import {
    RequestAuthType,
    RequestBodyFormat,
    RequestCaseStatus,
    RequestComposition,
    RequestConditionProcessor,
    RequestDefinitionStatus,
    RequestMethods,
    RequestParamsType,
  } from '@/enums/apiEnum';

  import type { ResponseItem } from './response/edit.vue';
  import {
    casePriorityOptions,
    caseStatusOptions,
    defaultBodyParamsItem,
    defaultHeaderParamsItem,
    defaultKeyValueParamItem,
    defaultRequestParamsItem,
  } from '@/views/api-test/components/config';
  import { filterKeyValParams, parseRequestBodyFiles } from '@/views/api-test/components/utils';
  import type { Api } from '@form-create/arco-design';

  // 懒加载Http协议组件
  const httpHeader = defineAsyncComponent(() => import('./header.vue'));
  const httpBody = defineAsyncComponent(() => import('./body.vue'));
  const httpQuery = defineAsyncComponent(() => import('./query.vue'));
  const httpRest = defineAsyncComponent(() => import('./rest.vue'));
  const addDependencyDrawer = defineAsyncComponent(
    () => import('@/views/api-test/management/components/addDependencyDrawer.vue')
  );

  export interface RequestCustomAttr {
    isNew: boolean;
    protocol: string;
    activeTab: RequestComposition;
    mode?: 'definition' | 'debug';
    executeLoading: boolean; // 执行中loading
    isCopy?: boolean; // 是否是复制
  }
  export type RequestParam = ExecuteApiRequestFullParams & {
    responseDefinition?: ResponseItem[];
    response?: RequestTaskResult;
  } & RequestCustomAttr &
    TabItem;

  const props = defineProps<{
    request: RequestParam; // 请求参数集合
    moduleTree: ModuleTreeNode[]; // 模块树
    detailLoading?: boolean; // 详情加载状态
    isDefinition?: boolean; // 是否是接口定义模式
    hideResponseLayoutSwitch?: boolean; // 是否隐藏响应体的布局切换
    otherParams?: Record<string, any>; // 保存请求时的其他参数
    currentEnvConfig?: EnvConfig;
    executeApi: (params: ExecuteRequestParams) => Promise<any>; // 执行接口
    localExecuteApi: (url: string, params: ExecuteRequestParams) => Promise<any>; // 本地执行接口
    createApi: (...args) => Promise<any>; // 创建接口
    updateApi: (...args) => Promise<any>; // 更新接口
    uploadTempFileApi?: (...args) => Promise<any>; // 上传临时文件接口
    fileSaveAsSourceId?: string | number; // 文件转存关联的资源id
    fileSaveAsApi?: (params: TransferFileParams) => Promise<string>; // 文件转存接口
    fileModuleOptionsApi?: (projectId: string) => Promise<ModuleTreeNode[]>; // 文件转存目录下拉框接口
    permissionMap: {
      execute: string;
      create: string;
      update: string;
    };
  }>();
  const emit = defineEmits(['addDone']);

  const appStore = useAppStore();
  const { t } = useI18n();

  const loading = defineModel<boolean>('detailLoading', { default: false });
  const requestVModel = defineModel<RequestParam>('request', { required: true });

  const isHttpProtocol = computed(() => requestVModel.value.protocol === 'HTTP');
  const temporaryResponseMap = {}; // 缓存websocket返回的报告内容，避免执行接口后切换tab导致报告丢失
  const isInitPluginForm = ref(false);

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
    if (isHttpProtocol.value) {
      if (props.isDefinition) {
        // 接口定义，定义模式隐藏前后置、断言
        return requestVModel.value.mode === 'debug'
          ? httpContentTabList
          : httpContentTabList.filter((e) => !commonContentTabKey.includes(e.value));
      }
      // 接口调试无断言
      return httpContentTabList.filter((e) => e.value !== RequestComposition.ASSERTION);
    }
    if (props.isDefinition) {
      // 接口定义，定义模式隐藏前后置、断言
      return requestVModel.value.mode === 'definition'
        ? pluginContentTab
        : [...pluginContentTab, ...httpContentTabList.filter((e) => commonContentTabKey.includes(e.value))];
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
  async function initLocalConfig() {
    if (hasLocalExec.value) {
      return;
    }
    try {
      const res = await getLocalConfig();
      const apiLocalExec = res.find((e) => e.type === 'API');
      if (apiLocalExec) {
        hasLocalExec.value = true;
        isPriorityLocalExec.value = apiLocalExec.enable || false;
        localExecuteUrl.value = apiLocalExec.userUrl || '';
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const pluginScriptMap = ref<Record<string, PluginConfig>>({}); // 存储初始化过后的插件配置
  const temporaryPluginFormMap: Record<string, any> = {}; // 缓存插件表单，避免切换tab导致动态表单数据丢失
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
    temporaryPluginFormMap[requestVModel.value.id] = fApi.value?.formData();
    handleActiveDebugChange();
  }, 300);

  /**
   * 控制插件表单字段显示
   */
  function controlPluginFormFields() {
    const allFields = fApi.value?.fields();
    let fields: string[] = [];
    if (props.isDefinition) {
      // 接口定义使用接口定义的字段集
      // 根据 apiDefinitionFields 字段集合展示需要的字段，隐藏其他字段
      fields = pluginScriptMap.value[requestVModel.value.protocol].apiDefinitionFields || [];
    } else {
      // 根据 apiDebugFields 字段集合展示需要的字段，隐藏其他字段
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
    if (tempForm || !requestVModel.value.isNew || requestVModel.value.isCopy) {
      // 如果缓存的表单数据存在或者是编辑状态，则需要将之前的输入数据填充
      const formData = tempForm || requestVModel.value;
      if (fApi.value) {
        fApi.value.nextTick(() => {
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
      fApi.value?.nextTick(() => {
        controlPluginFormFields();
      });
      nextTick(() => {
        // 如果是没有缓存也不是编辑，则需要重置表单，因为 form-create 只有一个实例，已经被其他有数据的 tab 污染了，需要重置
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

  watch(
    () => requestVModel.value.id,
    async () => {
      if (requestVModel.value.protocol !== 'HTTP') {
        requestVModel.value.activeTab = RequestComposition.PLUGIN;
        if (protocolOptions.value.length === 0) {
          // 还没初始化过协议列表，则初始化；在这里初始化是为了阻塞脚本的初始化，避免脚本初始化时协议列表还没初始化
          await initProtocolList();
        }
        initPluginScript();
      } else {
        initProtocolList();
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

  const showResponse = computed(
    () =>
      isHttpProtocol.value ||
      !props.isDefinition ||
      requestVModel.value.response?.requestResults[0]?.responseResult.responseCode
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

  const horizontalSplitBoxRef = ref<InstanceType<typeof MsSplitBox>>();
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
        splitContainerRef.value
          ? `${splitContainerRef.value.clientHeight - (props.hideResponseLayoutSwitch ? 37 : 42)}px`
          : 0
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

  function handleActiveApiChange() {
    if (requestVModel.value) {
      requestVModel.value.unSaved = true;
    }
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
    path: requestVModel.value.url,
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
        })),
      };
    } else {
      requestName = requestVModel.value.isNew ? saveModalForm.value.name : requestVModel.value.name;
      requestModuleId = requestVModel.value.isNew ? saveModalForm.value.moduleId : requestVModel.value.moduleId;
    }
    return {
      id: requestVModel.value.id.toString(),
      reportId: reportId.value,
      environmentId: props.currentEnvConfig?.id || '',
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
    if (isHttpProtocol.value) {
      try {
        requestVModel.value.executeLoading = true;
        const res = await props.executeApi(makeRequestParams(executeType));
        if (executeType === 'localExec') {
          await props.localExecuteApi(localExecuteUrl.value, res);
        }
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
            const res = await props.executeApi(makeRequestParams(executeType));
            if (executeType === 'localExec') {
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

  async function updateRequest() {
    try {
      saveLoading.value = true;
      await props.updateApi({
        ...makeRequestParams(),
        ...props.otherParams,
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

  /**
   * 保存请求
   */
  async function realSave(fullParams?: Record<string, any>, silence?: boolean) {
    try {
      if (!silence) {
        saveLoading.value = true;
      }
      let params;
      if (props.isDefinition) {
        params = {
          ...(fullParams || makeRequestParams()),
          ...props.otherParams,
        };
      } else {
        params = {
          ...(fullParams || makeRequestParams()),
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
      console.log('requestVModel.value', requestVModel.value);
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
    try {
      if (!isHttpProtocol.value) {
        // 插件需要校验动态表单
        await fApi.value?.validate();
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
  function saveAsCase() {
    saveCaseModalFormRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          saveCaseLoading.value = true;
          const definitionParams = makeRequestParams();
          if (requestVModel.value.isNew) {
            // 未保存过的接口保存为用例，先保存接口定义，再保存为用例
            await realSave(definitionParams, true);
          }
          const params: AddApiCaseParams = {
            ...definitionParams,
            ...saveCaseModalForm.value,
            projectId: appStore.currentProjectId,
            environmentId: props.currentEnvConfig?.id || '',
            apiDefinitionId: requestVModel.value.id,
          };
          await addCase(params);
          emit('addDone');
          Message.success(t('common.saveSuccess'));
          saveCaseModalVisible.value = false;
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          saveCaseLoading.value = false;
        }
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

  const activeApiTabFormRef = ref<FormInstance>();
  const isUrlError = ref(false);
  function handleSelect(value: string | number | Record<string, any> | undefined) {
    if (requestVModel.value.url === '' && requestVModel.value.protocol === 'HTTP') {
      isUrlError.value = true;
      return;
    }
    isUrlError.value = false;
    activeApiTabFormRef.value?.validate(async (errors) => {
      if (errors) {
        horizontalSplitBoxRef.value?.expand();
      } else {
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

  onBeforeMount(() => {
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
</style>
