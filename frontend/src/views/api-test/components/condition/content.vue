<template>
  <div class="condition-content h-full">
    <!-- 脚本操作 -->
    <template
      v-if="
        condition.processorType === RequestConditionProcessor.SCRIPT ||
        condition.processorType === RequestConditionProcessor.SCENARIO_SCRIPT ||
        condition.processorType === RequestConditionProcessor.REQUEST_SCRIPT ||
        condition?.assertionType === RequestConditionProcessor.SCRIPT
      "
    >
      <!-- 前后置请求开始 -->
      <div v-if="props.showPrePostRequest" class="mt-4">
        <a-radio-group
          v-model="condition.beforeStepScript"
          type="button"
          size="small"
          :default-value="true"
          :disabled="hasPreAndPost"
        >
          <a-radio :value="true"> {{ props?.requestRadioTextProps?.pre }}</a-radio>
          <a-radio :value="false"> {{ props?.requestRadioTextProps?.post }} </a-radio>
        </a-radio-group>
        <a-tooltip position="br" :content="t('apiTestDebug.preconditionAssociateResultDesc')">
          <IconQuestionCircle class="ml-2 h-[16px] w-[16px] text-[--color-text-4] hover:text-[rgb(var(--primary-5))]" />
          <template #content>
            <div>{{ props?.requestRadioTextProps?.preTip }}</div>
            <div>{{ props?.requestRadioTextProps?.postTip }}</div>
          </template>
        </a-tooltip>
      </div>
      <div v-if="props.showPrePostRequest" class="my-4">
        <MsSelect
          v-model:model-value="condition.ignoreProtocols"
          :style="{ width: '332px' }"
          :allow-search="false"
          allow-clear
          :options="protocolList"
          :multiple="true"
          :has-all-select="true"
          value-key="protocol"
          label-key="protocol"
          :prefix="t('project.environmental.preOrPost.ignoreProtocols')"
        >
        </MsSelect>
      </div>
      <!-- 前后置请求结束 -->
      <div class="flex items-center justify-between">
        <a-radio-group
          v-model="condition.enableCommonScript"
          class="mb-[8px]"
          :disabled="props.disabled"
          @change="emit('change')"
        >
          <a-radio :value="false">{{ t('apiTestDebug.manual') }}</a-radio>
          <a-radio v-if="hasAnyPermission(['PROJECT_CUSTOM_FUNCTION:READ'])" :value="true">
            {{ t('apiTestDebug.quote') }}
          </a-radio>
        </a-radio-group>
        <div v-if="props.showAssociatedScene" class="flex items-center">
          <a-switch
            v-model="condition.associateScenarioResult"
            class="mr-2"
            size="small"
            type="line"
            :disabled="props.disabled"
            @change="emit('change')"
          />
          {{ t('apiTestDebug.preconditionAssociatedSceneResult') }}
          <a-tooltip position="br" :content="t('apiTestDebug.preconditionAssociateResultDesc')">
            <IconQuestionCircle
              class="ml-2 h-[16px] w-[16px] text-[--color-text-4] hover:text-[rgb(var(--primary-5))]"
            />
          </a-tooltip>
        </div>
      </div>

      <div
        v-if="!condition.enableCommonScript"
        class="relative flex-1 rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)]"
      >
        <div v-if="isShowEditScriptNameInput" class="absolute left-[12px] top-[12px] z-10 w-[calc(100%-24px)]">
          <a-input
            ref="scriptNameInputRef"
            v-model:model-value="condition.name"
            :placeholder="t('apiTestDebug.preconditionScriptNamePlaceholder')"
            :max-length="255"
            :disabled="props.disabled"
            size="small"
            @press-enter="handleScriptNameInputBlur"
            @blur="handleScriptNameInputBlur"
            @change="() => emit('change')"
          />
        </div>
        <div class="flex items-center justify-between px-[12px] pt-[12px]">
          <div class="flex items-center">
            <a-tooltip v-if="condition.name" :content="condition.name">
              <div class="script-name-container">
                <div class="one-line-text mr-[4px] max-w-[110px] font-medium text-[var(--color-text-1)]">
                  {{ condition.name }}
                </div>
                <MsIcon
                  v-show="!props.disabled"
                  type="icon-icon_edit_outlined"
                  class="edit-script-name-icon"
                  @click="showEditScriptNameInput"
                />
              </div>
            </a-tooltip>
            <a-popover class="h-auto" position="right">
              <div class="text-[rgb(var(--primary-5))]">{{ t('apiTestDebug.scriptEx') }}</div>
              <template #content>
                <div class="mb-[8px] flex items-center justify-between">
                  <div class="text-[14px] font-medium text-[var(--color-text-1)]">
                    {{ t('apiTestDebug.scriptEx') }}
                  </div>
                  <a-button
                    type="outline"
                    class="arco-btn-outline--secondary p-[0_8px]"
                    size="mini"
                    @click="copyScriptEx"
                  >
                    {{ t('common.copy') }}
                  </a-button>
                </div>
                <div class="h-[412px]">
                  <MsCodeEditor
                    v-model:model-value="scriptEx"
                    theme="vs"
                    :language="LanguageEnum.BEANSHELL_JSR233"
                    width="500px"
                    height="388px"
                    :show-full-screen="false"
                    :show-theme-change="false"
                    read-only
                  >
                  </MsCodeEditor>
                </div>
              </template>
            </a-popover>
          </div>
          <div class="flex items-center gap-[8px]">
            <a-button
              :disabled="props.disabled"
              type="outline"
              class="arco-btn-outline--secondary p-[0_8px]"
              size="mini"
              @click="formatCoding"
            >
              <template #icon>
                <MsIcon type="icon-icon_clear" class="text-var(--color-text-4)" size="12" />
              </template>
              {{ t('project.commonScript.formatting') }}
            </a-button>
            <a-button
              :disabled="props.disabled"
              type="outline"
              class="arco-btn-outline--secondary p-[0_8px]"
              size="mini"
              @click="clearScript"
            >
              <template #icon>
                <MsIcon type="icon-icon_clear" class="text-var(--color-text-4)" size="12" />
              </template>
              {{ t('common.clear') }}
            </a-button>
            <a-button
              v-if="!props.isBuildIn && !props.showPrePostRequest"
              :disabled="props.disabled"
              type="outline"
              class="arco-btn-outline--secondary p-[0_8px]"
              size="mini"
              @click="copyCondition"
            >
              {{ t('common.copy') }}
            </a-button>
            <a-button
              v-if="!props.isBuildIn"
              :disabled="props.disabled"
              type="outline"
              class="arco-btn-outline--secondary p-[0_8px]"
              size="mini"
              @click="deleteCondition"
            >
              {{ t('common.delete') }}
            </a-button>
          </div>
        </div>
        <div class="h-[calc(100%-24px)] min-h-[300px]">
          <MsScriptDefined
            v-if="condition.script !== undefined && condition.scriptLanguage !== undefined"
            ref="scriptDefinedRef"
            v-model:code="condition.script"
            v-model:language="condition.scriptLanguage"
            :disabled="props.disabled"
            show-type="commonScript"
            :show-header="false"
            :script-code-editor-height="props.scriptCodeEditorHeight"
            @change="() => emit('change')"
          />
        </div>
      </div>
      <div v-else class="flex h-[calc(100%-47px)] flex-col">
        <div class="mb-[8px] flex w-full items-center bg-[var(--color-text-n9)] p-[12px]">
          <div class="break-word max-w-[70%] break-all text-[var(--color-text-2)]">
            {{ condition.commonScriptInfo?.name || '-' }}
          </div>
          <a-divider margin="8px" direction="vertical" />
          <MsButton
            v-permission="['PROJECT_CUSTOM_FUNCTION:READ']"
            type="text"
            class="font-medium"
            :disabled="props.disabled"
            @click="showQuoteDrawer = true"
          >
            {{ t('apiTestDebug.quote') }}
          </MsButton>
        </div>
        <div v-if="showParameters() || showScript()" class="min-h-[300px]">
          <div>
            <a-radio-group v-model:model-value="commonScriptShowType" size="small" type="button" class="mb-[8px] w-fit">
              <a-radio v-if="showParameters()" value="parameters">{{ t('apiTestDebug.parameters') }}</a-radio>
              <a-radio value="scriptContent">{{ t('apiTestDebug.scriptContent') }}</a-radio>
            </a-radio-group>
          </div>
          <div class="h-[calc(100%-76px)]">
            <paramTable
              v-if="commonScriptShowType === 'parameters'"
              v-model:params="scriptParams"
              :disabled-param-value="props.disabled"
              :scroll="{ x: '100%' }"
              :columns="scriptColumns"
              :default-param-item="{
                key: '',
                value: '',
                description: '',
                required: false,
              }"
              :selectable="false"
              @change="() => emit('change')"
            />
            <MsCodeEditor
              v-else-if="commonScriptShowType === 'scriptContent' && condition.commonScriptInfo"
              v-model:model-value="condition.commonScriptInfo.script"
              theme="vs"
              :height="props.sqlCodeEditorHeight || '100%'"
              :language="condition.commonScriptInfo.scriptLanguage || LanguageEnum.BEANSHELL_JSR233"
              :show-full-screen="false"
              :show-theme-change="false"
              read-only
            />
          </div>
        </div>
      </div>
    </template>
    <!-- SQL操作 -->
    <template v-else-if="condition.processorType === RequestConditionProcessor.SQL">
      <div class="mb-[8px]">
        <a-input
          v-model:model-value="condition.name"
          :disabled="props.disabled"
          :placeholder="t('ms.paramsInput.sqlOperationName')"
          :max-length="255"
          @input="() => emit('change')"
        />
      </div>
      <div class="mb-[8px] flex w-full items-center bg-[var(--color-text-n9)] p-[12px]">
        <div class="max-w-[70%] break-words text-[var(--color-text-2)]">
          {{ condition.dataSourceName || '-' }}
        </div>
        <a-divider margin="8px" direction="vertical" />
        <MsButton
          type="text"
          class="font-medium"
          :disabled="props.disabled"
          @click="quoteSqlSourceDrawerVisible = true"
        >
          {{ t('apiTestDebug.introduceSource') }}
        </MsButton>
      </div>
      <template v-if="condition.dataSourceId">
        <div class="mb-[8px] text-[var(--color-text-1)]">{{ t('apiTestDebug.sqlScript') }}</div>
        <div class="mb-[8px]">
          <MsCodeEditor
            v-model:model-value="condition.script"
            :read-only="props.disabled"
            theme="vs"
            height="120px"
            :language="LanguageEnum.SQL"
            :show-full-screen="false"
            :show-theme-change="false"
            @change="() => emit('change')"
          >
          </MsCodeEditor>
        </div>
        <div class="mb-[8px]">
          <div class="mb-[8px] flex items-center text-[var(--color-text-1)]">
            {{ t('apiTestDebug.storageByCol') }}
            <a-tooltip position="right" :content="t('apiTestDebug.storageColTip')">
              <icon-question-circle
                class="ml-[4px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
                size="16"
              />
            </a-tooltip>
          </div>
          <a-input
            v-model:model-value="condition.variableNames"
            :max-length="255"
            :disabled="props.disabled"
            :placeholder="
              t('apiTestDebug.storageByColPlaceholder', { a: 'id', b: 'email', c: '${id_1}', d: '${email_1}' })
            "
            @input="() => emit('change')"
          />
        </div>
        <div class="sql-table-container">
          <div class="mb-[8px] text-[var(--color-text-1)]">
            {{ t('apiTestDebug.extractParameter') }}
          </div>
          <paramTable
            :params="condition.extractParams"
            :disabled-except-param="props.disabled"
            :columns="sqlSourceColumns"
            :selectable="false"
            :default-param-item="defaultKeyValueParamItem"
            @change="handleSqlSourceParamTableChange"
          />
        </div>
        <div class="mt-[8px]">
          <div class="mb-[8px] flex items-center text-[var(--color-text-1)]">
            {{ t('apiTestDebug.storageByResult') }}
            <a-tooltip position="right" :content="t('apiTestDebug.storageResultTip')">
              <icon-question-circle
                class="ml-[4px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
                size="16"
              />
            </a-tooltip>
          </div>
          <a-input
            v-model:model-value="condition.resultVariable"
            :disabled="props.disabled"
            :max-length="255"
            :placeholder="t('apiTestDebug.storageByResultPlaceholder', { a: 'result', b: '${result}' })"
            @input="() => emit('change')"
          />
        </div>
      </template>
    </template>
    <!-- 等待时间 -->
    <div v-else-if="condition.processorType === RequestConditionProcessor.TIME_WAITING">
      <div class="mb-[8px] flex items-center">
        {{ t('apiTestDebug.waitTime') }}
        <div class="text-[var(--color-text-4)]">(ms)</div>
      </div>
      <a-input-number
        v-model:model-value="condition.delay"
        :disabled="props.disabled"
        mode="button"
        :step="100"
        :min="1"
        :precision="0"
        :max="600000"
        :default-value="1000"
        class="w-[160px]"
        model-event="input"
        @blur="handleDelayBlur"
      />
    </div>
    <!-- 提取参数 -->
    <div v-else-if="condition.processorType === RequestConditionProcessor.EXTRACT">
      <paramTable
        ref="extractParamsTableRef"
        :params="condition.extractors"
        :disabled-except-param="props.disabled"
        :disabled-param-value="props.disabled"
        :default-param-item="defaultExtractParamItem"
        :columns="extractParamsColumns"
        :selectable="false"
        :scroll="{ x: '700px' }"
        :response="props.response"
        @change="handleExtractParamTableChange"
        @more-action-select="(e,r)=> handleExtractParamMoreActionSelect(e,r as ExpressionConfig)"
      >
        <template #expression="{ record, rowIndex }">
          <a-popover
            position="tl"
            :disabled="!record.expression || record.expression.trim() === ''"
            class="ms-params-input-popover"
          >
            <template #content>
              <div class="param-popover-title">
                {{ t('apiTestDebug.expression') }}
              </div>
              <div class="param-popover-value">
                {{ record.expression }}
              </div>
            </template>
            <a-input
              v-model:model-value="record.expression"
              class="ms-params-input"
              :max-length="255"
              :placeholder="t('ms.paramsInput.commonPlaceholder')"
              size="mini"
              :disabled="props.disabled"
              @input="() => handleExpressionChange(rowIndex)"
              @change="() => handleExpressionChange(rowIndex)"
            >
              <template #suffix>
                <a-tooltip v-if="!props.disabled" :disabled="!!props.response">
                  <template #content>
                    <div>{{ t('apiTestDebug.expressionTip1') }}</div>
                    <div>{{ t('apiTestDebug.expressionTip2') }}</div>
                    <div>{{ t('apiTestDebug.expressionTip3') }}</div>
                  </template>
                  <MsIcon
                    type="icon-icon_flashlamp"
                    :size="15"
                    :class="!props.response ? 'ms-params-input-suffix-icon--disabled' : 'ms-params-input-suffix-icon'"
                    @click.stop="() => showFastExtraction(record)"
                  />
                </a-tooltip>
              </template>
            </a-input>
          </a-popover>
        </template>
        <template #operationPre="{ record }">
          <a-popover
            v-model:popupVisible="record.moreSettingPopoverVisible"
            position="tr"
            trigger="click"
            :title="t('common.setting')"
            :content-style="{ width: '480px' }"
          >
            <template #content>
              <moreSetting v-model:config="activeRecord" is-popover class="mt-[12px]" />
              <div v-show="!props.disabled" class="flex items-center justify-end gap-[8px]">
                <a-button type="secondary" size="mini" @click="record.moreSettingPopoverVisible = false">
                  {{ t('common.cancel') }}
                </a-button>
                <a-button type="primary" size="mini" @click="() => applyMoreSetting(record)">
                  {{ t('common.confirm') }}
                </a-button>
              </div>
            </template>
            <span class="invisible relative"></span>
          </a-popover>
        </template>
      </paramTable>
    </div>
  </div>
  <quoteSqlSourceDrawer
    v-model:visible="quoteSqlSourceDrawerVisible"
    :selected-key="condition.dataSourceId"
    @apply="handleQuoteSqlSourceApply"
  />
  <fastExtraction
    v-model:visible="fastExtractionVisible"
    :response="props.response"
    :config="activeRecord"
    @apply="handleFastExtractionApply"
  />
  <InsertCommonScript
    v-model:visible="showQuoteDrawer"
    :checked-id="condition.commonScriptInfo?.id"
    enable-radio-selected
    @save="saveQuoteScriptHandler"
  />
  <AddScriptDrawer
    v-model:visible="showAddScriptDrawer"
    v-model:params="paramsList"
    :confirm-loading="confirmLoading"
    ok-text="common.apply"
    :enable-radio-selected="true"
  />
</template>

<script setup lang="ts">
  import { useClipboard } from '@vueuse/core';
  import { InputInstance, Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import InsertCommonScript from '@/components/business/ms-common-script/insertCommonScript.vue';
  import AddScriptDrawer from '@/components/business/ms-common-script/ms-addScriptDrawer.vue';
  import MsScriptDefined from '@/components/business/ms-common-script/scriptDefined.vue';
  import MsSelect from '@/components/business/ms-select';
  import fastExtraction from '../fastExtraction/index.vue';
  import moreSetting from '../fastExtraction/moreSetting.vue';
  import paramTable, { type ParamTableColumn } from '../paramTable.vue';
  import quoteSqlSourceDrawer from '../quoteSqlSourceDrawer.vue';

  import { getProtocolList } from '@/api/modules/api-test/common';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useAppStore from '@/store/modules/app';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import {
    ExecuteConditionProcessor,
    JSONPathExtract,
    KeyValueParam,
    ProtocolItem,
    RegexExtract,
    XPathExtract,
  } from '@/models/apiTest/common';
  import { ParamsRequestType } from '@/models/projectManagement/commonScript';
  import { DataSourceItem } from '@/models/projectManagement/environmental';
  import {
    RequestConditionProcessor,
    RequestExtractEnvType,
    RequestExtractExpressionEnum,
    RequestExtractExpressionRuleType,
    RequestExtractResultMatchingRule,
    RequestExtractScope,
    ResponseBodyXPathAssertionFormat,
  } from '@/enums/apiEnum';

  import { defaultKeyValueParamItem, extractTypeOptions } from '@/views/api-test/components/config';

  const { openModal } = useModal();

  export type ExpressionConfig = (RegexExtract | JSONPathExtract | XPathExtract) & Record<string, any>;
  const appStore = useAppStore();
  const props = withDefaults(
    defineProps<{
      disabled?: boolean;
      response?: string; // 响应内容
      isBuildIn?: boolean; // 是否是内置的条件
      showAssociatedScene?: boolean; // 是否展示关联场景结果
      requestRadioTextProps?: Record<string, any>; // 前后置请求前后置按钮文本
      showPrePostRequest?: boolean; // 是否展示前后置请求忽略
      totalList?: ExecuteConditionProcessor[]; // 总列表
      sqlCodeEditorHeight?: string; // sql脚本编辑器高度
      scriptCodeEditorHeight?: string; // 脚本的高度
    }>(),
    {
      showAssociatedScene: false,
      showPrePostRequest: false,
    }
  );
  const emit = defineEmits<{
    (e: 'copy'): void;
    (e: 'delete', id: number): void;
    (e: 'change'): void;
  }>();

  const { t } = useI18n();

  const condition = defineModel<ExecuteConditionProcessor>('data', {
    required: true,
  });

  function filterDataSource() {
    if (condition.value.processorType === RequestConditionProcessor.SQL && condition.value.dataSourceId) {
      // 如果是SQL类型的条件且已选数据源，需要根据环境切换数据源
      const dataSourceItem = appStore.currentEnvConfig?.dataSources.find(
        (item) => item.dataSource === condition.value.dataSourceName
      );
      if (appStore.currentEnvConfig?.dataSources.length === 0) {
        // 如果没有数据源，就清除已选的数据源
        condition.value.dataSourceName = '';
        condition.value.dataSourceId = '';
      } else if (dataSourceItem) {
        // 每次初始化都去查找一下最新的数据源，因为切换环境的时候数据源也需要切换
        condition.value.dataSourceName = dataSourceItem.dataSource;
        condition.value.dataSourceId = dataSourceItem.id;
      } else if (appStore.currentEnvConfig && appStore.currentEnvConfig?.dataSources.length > 0) {
        // 如果没有找到，就默认取第一个数据源
        condition.value.dataSourceName = appStore.currentEnvConfig?.dataSources[0].dataSource;
        condition.value.dataSourceId = appStore.currentEnvConfig?.dataSources[0].id;
      }
    }
  }

  watch(
    () => appStore.currentEnvConfig?.id,
    () => {
      filterDataSource();
    },
    {
      immediate: true,
      deep: true,
    }
  );

  watch(
    () => condition.value.id,
    () => {
      filterDataSource();
    }
  );

  // 是否显示脚本名称编辑框
  const isShowEditScriptNameInput = ref(false);
  const scriptNameInputRef = ref<InputInstance>();

  function showEditScriptNameInput() {
    isShowEditScriptNameInput.value = true;
    nextTick(() => {
      scriptNameInputRef.value?.focus();
    });
  }

  const beanShellJsr233 = ref(`// 这里可以输入脚本注释
value = vars.get("variable_name");
expectedValue = "expected_value";
result = expectedValue.equals(value);
if (!result){
  msg = "assertion [" + value + " == " + expectedValue + "]: false;";
  AssertionResult.setFailureMessage(msg);
  AssertionResult.setFailure(true);
}`);

  const beanshell = ref(`// 这里可以输入脚本注释
value = vars.get("variable_name");
expectedValue = "expected_value";
result = expectedValue.equals(value);
if (!result){
  msg = "assertion [" + value + " == " + expectedValue + "]: false;";
  FailureMessage = msg;
  Failure = true;
}`);

  const python3 = ref(`# 这里可以输入脚本注释
value = vars.get("variable_name");
expectedValue = "expected_value";
result = expectedValue == value;
if not result :
  msg = "assertion [" + value + " == " + expectedValue + "]: false;";
  AssertionResult.setFailureMessage(msg);
  AssertionResult.setFailure(True);`);

  const javaScript = ref(`// 这里可以输入脚本注释
value = vars.get("variable_name");
expectedValue = "expected_value"
result = expectedValue === value;
if (!result){
  msg = "assertion [" + value + " == " + expectedValue + "]: false;";
  AssertionResult.setFailureMessage(msg);
  AssertionResult.setFailure(true);
}`);

  let scriptEx = beanShellJsr233;

  const { copy, isSupported } = useClipboard({ legacy: true });

  watch(
    () => condition.value.scriptLanguage,
    (scriptLanguage) => {
      if (scriptLanguage === LanguageEnum.BEANSHELL) {
        scriptEx = beanshell;
      } else if (scriptLanguage === LanguageEnum.PYTHON) {
        scriptEx = python3;
      } else if (scriptLanguage === LanguageEnum.JAVASCRIPT) {
        scriptEx = javaScript;
      } else {
        scriptEx = beanShellJsr233;
      }
    },
    { deep: true, immediate: true }
  );

  function copyScriptEx() {
    if (isSupported) {
      copy(scriptEx.value);
      Message.success(t('apiTestDebug.scriptExCopySuccess'));
    } else {
      Message.warning(t('apiTestDebug.copyNotSupport'));
    }
  }

  const scriptDefinedRef = ref<InstanceType<typeof MsScriptDefined>>();

  function clearScript() {
    condition.value.script = '';
  }
  function formatCoding() {
    scriptDefinedRef.value?.formatCoding();
  }

  /**
   * 复制条件
   */
  function copyCondition() {
    emit('copy');
  }

  /**
   * 删除条件
   */
  function deleteCondition() {
    openModal({
      type: 'error',
      title: t('system.orgTemplate.deleteTemplateTitle', { name: characterLimit(condition.value.name) }),
      content: t('script.delete.confirm'),
      okText: t('system.userGroup.confirmDelete'),
      cancelText: t('system.userGroup.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          emit('delete', condition.value.id);
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  const commonScriptShowType = ref<'parameters' | 'scriptContent'>('parameters');

  const scriptParams = ref<any[]>([]);

  const scriptColumns: MsTableColumn = [
    {
      title: 'project.commonScript.ParameterNames',
      slotName: 'key',
      dataIndex: 'key',
      addLineDisabled: true,
      disabledColumn: true,
    },
    {
      title: 'project.commonScript.ParameterValue',
      dataIndex: 'value',
      slotName: 'value',
      addLineDisabled: true,
    },
    {
      title: 'common.desc',
      slotName: 'description',
      dataIndex: 'description',
      addLineDisabled: true,
      disabledColumn: true,
    },
    {
      title: 'project.commonScript.isRequired',
      slotName: 'mustContain',
      dataIndex: 'required',
      addLineDisabled: true,
      disabledColumn: true,
    },
  ];

  function handleScriptNameInputBlur() {
    if (condition.value.name === '') {
      condition.value.name = t('apiTestDebug.preconditionScriptName');
    }
    isShowEditScriptNameInput.value = false;
  }

  const showQuoteDrawer = ref(false);
  function saveQuoteScriptHandler(item: any) {
    // TODO:any
    condition.value.commonScriptInfo = {
      id: item.id,
      script: item.script,
      name: item.name,
      scriptLanguage: item.type,
      params: (JSON.parse(item.params) || []).map((e: any) => {
        return {
          key: e.name,
          ...e,
        };
      }),
    };
    scriptParams.value = (condition.value.commonScriptInfo?.params as any[]) || [];
    showQuoteDrawer.value = false;
    Message.success(t('apiTestDebug.introduceSourceApplySuccess'));
  }

  const showAddScriptDrawer = ref(false);
  const paramsList = ref<ParamsRequestType[]>([]);
  const confirmLoading = ref(false);

  const sqlSourceColumns: ParamTableColumn[] = [
    {
      title: 'apiTestDebug.paramName',
      dataIndex: 'key',
      slotName: 'key',
      needValidRepeat: true,
    },
    {
      title: 'apiTestDebug.extractValueByColumn',
      titleSlotName: 'extractValueTitle',
      dataIndex: 'value',
      slotName: 'value',
      isNormal: true,
    },
    {
      title: '',
      dataIndex: 'operation',
      slotName: 'operation',
      width: 50,
    },
  ];
  const quoteSqlSourceDrawerVisible = ref(false);
  function handleQuoteSqlSourceApply(sqlSource: DataSourceItem) {
    condition.value.dataSourceName = sqlSource.dataSource;
    condition.value.dataSourceId = sqlSource.id;
    emit('change');
    Message.success(t('apiTestDebug.introduceSourceApplySuccess'));
  }

  function handleSqlSourceParamTableChange(resultArr: any[], isInit?: boolean) {
    condition.value.extractParams = [...resultArr];
    if (!isInit) {
      emit('change');
    }
  }

  function handleDelayBlur() {
    if (!condition.value.delay) {
      condition.value.delay = 1000;
    }
  }

  const extractParamsColumns: ParamTableColumn[] = [
    {
      title: 'apiTestDebug.paramName',
      dataIndex: 'variableName',
      slotName: 'key',
      needValidRepeat: true,
      width: 150,
    },
    {
      title: 'apiTestDebug.paramType',
      dataIndex: 'variableType',
      slotName: 'variableType',
      typeOptions: extractTypeOptions.map((item) => {
        return {
          label: t(item.label),
          value: item.value,
        };
      }),
      width: 150,
    },
    {
      title: 'apiTestDebug.mode',
      dataIndex: 'extractType',
      slotName: 'extractType',
      typeOptions: [
        {
          label: 'JSONPath',
          value: RequestExtractExpressionEnum.JSON_PATH,
        },
        {
          label: 'XPath',
          value: RequestExtractExpressionEnum.X_PATH,
        },
        {
          label: t('apiTestDebug.regular'),
          value: RequestExtractExpressionEnum.REGEX,
        },
      ],
      width: 120,
    },
    {
      title: 'apiTestDebug.range',
      dataIndex: 'extractScope',
      slotName: 'extractScope',
      typeOptions: [
        {
          label: 'Body',
          value: RequestExtractScope.BODY,
        },
        {
          label: 'Body (unescaped)',
          value: RequestExtractScope.UNESCAPED_BODY,
        },
        {
          label: 'Body as a Document',
          value: RequestExtractScope.BODY_AS_DOCUMENT,
        },
        {
          label: 'URL',
          value: RequestExtractScope.URL,
        },
        {
          label: 'Request Headers',
          value: RequestExtractScope.REQUEST_HEADERS,
        },
        {
          label: 'Response Headers',
          value: RequestExtractScope.RESPONSE_HEADERS,
        },
        {
          label: 'Response Code',
          value: RequestExtractScope.RESPONSE_CODE,
        },
        {
          label: 'Response Message',
          value: RequestExtractScope.RESPONSE_MESSAGE,
        },
      ],
      width: 190,
    },
    {
      title: 'apiTestDebug.expression',
      dataIndex: 'expression',
      slotName: 'expression',
      width: 200,
    },
    {
      title: '',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      moreAction: [
        {
          eventTag: 'copy',
          label: 'common.copy',
        },
        {
          eventTag: 'setting',
          label: 'common.setting',
        },
      ],
      width: 80,
    },
  ];

  function handleExtractParamTableChange(resultArr: any[], isInit?: boolean) {
    condition.value.extractors = [...resultArr];
    if (!isInit) {
      emit('change');
    }
  }

  const extractParamsTableRef = ref<InstanceType<typeof paramTable>>();
  const defaultExtractParamItem: ExpressionConfig = {
    enable: true,
    variableName: '',
    variableType: RequestExtractEnvType.TEMPORARY,
    extractScope: RequestExtractScope.BODY,
    expression: '',
    extractType: RequestExtractExpressionEnum.JSON_PATH,
    expressionMatchingRule: RequestExtractExpressionRuleType.EXPRESSION,
    resultMatchingRule: RequestExtractResultMatchingRule.RANDOM,
    resultMatchingRuleNum: 1,
    responseFormat: ResponseBodyXPathAssertionFormat.XML,
    moreSettingPopoverVisible: false,
  };
  const fastExtractionVisible = ref(false);
  const activeRecord = ref({ ...defaultExtractParamItem }); // 用于暂存当前操作的提取参数表格项

  function showFastExtraction(record: ExpressionConfig) {
    if (props.disabled || !props.response) return;
    activeRecord.value = { ...record };
    fastExtractionVisible.value = true;
  }

  function handleExpressionChange(rowIndex: number) {
    extractParamsTableRef.value?.addTableLine(rowIndex);
  }

  function copyItem(record: ExpressionConfig) {
    if (condition.value.extractors) {
      const currentIndex = condition.value.extractors.findIndex((item: ExpressionConfig) => item.id === record.id);

      const currentExtractorsItem = cloneDeep(record);
      if (currentIndex > -1) {
        condition.value.extractors.splice(currentIndex, 0, {
          ...currentExtractorsItem,
          id: new Date().getTime().toString(),
        });
        const temList = cloneDeep(condition.value?.extractors);
        condition.value.extractors = temList;
      }
    }
  }

  /**
   * 处理提取参数表格更多操作
   */
  function handleExtractParamMoreActionSelect(event: ActionsItem, record: ExpressionConfig) {
    activeRecord.value = { ...record };
    if (event.eventTag === 'copy') {
      // emit('copy');
      // 复制提取行
      copyItem(record);
    } else if (event.eventTag === 'setting') {
      record.moreSettingPopoverVisible = true;
    }
  }

  /**
   * 提取参数表格-应用更多设置
   */
  function applyMoreSetting(record: ExpressionConfig) {
    condition.value.extractors = condition.value.extractors?.map((e) => {
      if (e.id === activeRecord.value.id) {
        record.moreSettingPopoverVisible = false;
        return {
          ...activeRecord.value,
          moreSettingPopoverVisible: false,
        };
      }
      return e;
    });
    emit('change');
  }

  function showParameters() {
    if (condition.value.commonScriptInfo && condition.value.commonScriptInfo.params) {
      let emptyKeyCount = 0;
      condition.value.commonScriptInfo.params.forEach((item: KeyValueParam) => {
        if (item.key !== '') {
          emptyKeyCount++;
        }
      });
      if (emptyKeyCount > 0) {
        return true;
      }
      return false;
    }
    return false;
  }
  function showScript() {
    if (condition.value.commonScriptInfo && condition.value.commonScriptInfo.script !== '') {
      return true;
    }
    return false;
  }

  /**
   * 提取参数表格-保存快速提取的配置
   */
  function handleFastExtractionApply(config: RegexExtract | JSONPathExtract | XPathExtract) {
    condition.value.extractors = condition.value.extractors?.map((e) => {
      if (e.id === activeRecord.value.id) {
        return {
          ...e,
          ...config,
        };
      }
      return e;
    });
    fastExtractionVisible.value = false;
    nextTick(() => {
      extractParamsTableRef.value?.addTableLine(
        condition.value.extractors?.findIndex((e) => e.id === activeRecord.value.id) || 0
      );
    });
    emit('change');
  }

  const protocolList = ref<ProtocolItem[]>([]);
  onBeforeMount(async () => {
    if (props.showPrePostRequest) {
      try {
        // TODO:数据从外面传进来
        protocolList.value = await getProtocolList(appStore.currentOrgId);
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      }
    }
  });
  watch(
    () => condition.value.commonScriptInfo,
    (info) => {
      scriptParams.value = info?.params as any[]; // 查看详情的时候需要赋值一下
      if (!showParameters()) {
        commonScriptShowType.value = 'scriptContent';
      }
    },
    { deep: true, immediate: true }
  );

  const hasPreAndPost = computed(() => {
    if (props.showPrePostRequest) {
      return (
        (props?.totalList || []).filter(
          (item) => item.beforeStepScript && item.processorType === RequestConditionProcessor.REQUEST_SCRIPT
        ).length > 0 &&
        (props?.totalList || []).filter(
          (item) => !item.beforeStepScript && item.processorType === RequestConditionProcessor.REQUEST_SCRIPT
        ).length > 0
      );
    }
    return true;
  });
</script>

<style lang="less" scoped>
  :deep(.arco-table-th) {
    background-color: var(--color-text-n9);
  }
  .condition-content {
    @apply flex flex-1 flex-col overflow-y-auto;
    .ms-scroll-bar();

    padding: 8px;
    border: 1px solid var(--color-text-n8);
    border-radius: var(--border-radius-small);
    .script-name-container {
      @apply flex items-center;

      margin-right: 16px;
      &:hover {
        .edit-script-name-icon {
          @apply visible;
        }
      }
      .edit-script-name-icon {
        @apply invisible cursor-pointer;

        color: rgb(var(--primary-5));
      }
    }
  }
  .param-popover-title {
    @apply font-medium;

    margin-bottom: 4px;
    font-size: 12px;
    font-weight: 500;
    line-height: 16px;
    color: var(--color-text-1);
  }
  .param-popover-value {
    min-width: 100px;
    max-width: 280px;
    font-size: 12px;
    line-height: 16px;
    color: var(--color-text-1);
  }
</style>
