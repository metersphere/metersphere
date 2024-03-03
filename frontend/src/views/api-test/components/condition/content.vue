<template>
  <div class="condition-content">
    <!-- 脚本操作 -->
    <template v-if="condition.processorType === RequestConditionProcessor.SCRIPT">
      <a-radio-group v-model:model-value="condition.enableCommonScript" class="mb-[8px]">
        <a-radio :value="false">{{ t('apiTestDebug.manual') }}</a-radio>
        <a-radio :value="true">{{ t('apiTestDebug.quote') }}</a-radio>
      </a-radio-group>
      <div
        v-if="!condition.enableCommonScript"
        class="relative flex-1 rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)]"
      >
        <div v-if="isShowEditScriptNameInput" class="absolute left-[12px] top-[12px] z-10 w-[calc(100%-24px)]">
          <a-input
            ref="scriptNameInputRef"
            v-model:model-value="condition.scriptName"
            :placeholder="t('apiTestDebug.preconditionScriptNamePlaceholder')"
            :max-length="255"
            size="small"
            @press-enter="isShowEditScriptNameInput = false"
            @blur="isShowEditScriptNameInput = false"
          />
        </div>
        <div class="flex items-center justify-between px-[12px] pt-[12px]">
          <div class="flex items-center">
            <a-tooltip :content="condition.scriptName">
              <div class="script-name-container">
                <div class="one-line-text mr-[4px] max-w-[110px] font-medium text-[var(--color-text-1)]">
                  {{ condition.scriptName }}
                </div>
                <MsIcon type="icon-icon_edit_outlined" class="edit-script-name-icon" @click="showEditScriptNameInput" />
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
                <div class="flex h-[412px]">
                  <MsCodeEditor
                    v-model:model-value="scriptEx"
                    class="flex-1"
                    theme="vs"
                    :language="LanguageEnum.BEANSHELL"
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
            <a-button type="outline" class="arco-btn-outline--secondary p-[0_8px]" size="mini" @click="undoScript">
              <template #icon>
                <MsIcon type="icon-icon_undo_outlined" class="text-var(--color-text-4)" size="12" />
              </template>
              {{ t('common.revoke') }}
            </a-button>
            <a-button type="outline" class="arco-btn-outline--secondary p-[0_8px]" size="mini" @click="clearScript">
              <template #icon>
                <MsIcon type="icon-icon_clear" class="text-var(--color-text-4)" size="12" />
              </template>
              {{ t('common.clear') }}
            </a-button>
            <a-button
              v-if="!props.isBuildIn"
              type="outline"
              class="arco-btn-outline--secondary p-[0_8px]"
              size="mini"
              @click="copyCondition"
            >
              {{ t('common.copy') }}
            </a-button>
            <a-button
              v-if="!props.isBuildIn"
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
            show-type="commonScript"
            :show-header="false"
          />
        </div>
      </div>
      <div v-else class="flex h-[calc(100%-47px)] flex-col">
        <div class="mb-[16px] flex w-full items-center bg-[var(--color-text-n9)] p-[12px]">
          <div class="text-[var(--color-text-2)]">
            {{ condition.commonScriptInfo?.name || '-' }}
          </div>
          <a-divider margin="8px" direction="vertical" />
          <MsButton type="text" class="font-medium" @click="showQuoteDrawer = true">
            {{ t('apiTestDebug.quote') }}
          </MsButton>
        </div>
        <a-radio-group v-model:model-value="commonScriptShowType" size="small" type="button" class="mb-[8px] w-fit">
          <a-radio value="parameters">{{ t('apiTestDebug.parameters') }}</a-radio>
          <a-radio value="scriptContent">{{ t('apiTestDebug.scriptContent') }}</a-radio>
        </a-radio-group>
        <MsBaseTable v-show="commonScriptShowType === 'parameters'" v-bind="propsRes" v-on="propsEvent">
          <template #value="{ record }">
            <a-tooltip :content="t(record.required ? 'apiTestDebug.paramRequired' : 'apiTestDebug.paramNotRequired')">
              <div
                :class="[
                  record.required ? '!text-[rgb(var(--danger-5))]' : '!text-[var(--color-text-brand)]',
                  '!mr-[4px] !p-[4px]',
                ]"
              >
                <div>*</div>
              </div>
            </a-tooltip>
            {{ record.value }}
          </template>
        </MsBaseTable>
        <div v-show="commonScriptShowType === 'scriptContent'" class="h-[calc(100%-76px)]">
          <MsCodeEditor
            v-if="condition.commonScriptInfo"
            v-model:model-value="condition.commonScriptInfo.script"
            theme="vs"
            height="100%"
            :language="condition.commonScriptInfo.scriptLanguage || LanguageEnum.BEANSHELL"
            :show-full-screen="false"
            :show-theme-change="false"
            read-only
          >
          </MsCodeEditor>
        </div>
      </div>
    </template>
    <!-- SQL操作 -->
    <template v-else-if="condition.processorType === RequestConditionProcessor.SQL">
      <div class="mb-[16px]">
        <div class="mb-[8px] text-[var(--color-text-1)]">{{ t('common.desc') }}</div>
        <a-input
          v-model:model-value="condition.description"
          :placeholder="t('apiTestDebug.commonPlaceholder')"
          :max-length="255"
        />
      </div>
      <div class="mb-[16px] flex w-full items-center bg-[var(--color-text-n9)] p-[12px]">
        <div class="text-[var(--color-text-2)]">
          {{ condition.scriptName || '-' }}
        </div>
        <a-divider margin="8px" direction="vertical" />
        <MsButton type="text" class="font-medium" @click="quoteSqlSourceDrawerVisible = true">
          {{ t('apiTestDebug.introduceSource') }}
        </MsButton>
      </div>
      <div class="mb-[8px] text-[var(--color-text-1)]">{{ t('apiTestDebug.sqlScript') }}</div>
      <div class="mb-[16px] h-[300px]">
        <MsCodeEditor
          v-model:model-value="condition.script"
          theme="vs"
          height="276px"
          :language="LanguageEnum.SQL"
          :show-full-screen="false"
          :show-theme-change="false"
          read-only
        >
        </MsCodeEditor>
      </div>
      <div class="mb-[16px]">
        <div class="mb-[8px] flex items-center text-[var(--color-text-1)]">
          {{ t('apiTestDebug.storageType') }}
          <a-tooltip position="right">
            <icon-question-circle
              class="ml-[4px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
              size="16"
            />
            <template #content>
              <div>{{ t('apiTestDebug.storageTypeTip1') }}</div>
              <div>{{ t('apiTestDebug.storageTypeTip2') }}</div>
            </template>
          </a-tooltip>
        </div>
      </div>
      <div class="mb-[16px]">
        <div class="mb-[8px] text-[var(--color-text-1)]">{{ t('apiTestDebug.storageByCol') }}</div>
        <a-input
          v-model:model-value="condition.variableNames"
          :max-length="255"
          :placeholder="t('apiTestDebug.storageByColPlaceholder', { a: '{id_1}', b: '{username_1}' })"
        />
      </div>
      <div class="sql-table-container">
        <div class="mb-[8px] text-[var(--color-text-1)]">{{ t('apiTestDebug.extractParameter') }}</div>
        <paramTable
          v-model:params="condition.variables"
          :columns="sqlSourceColumns"
          :selectable="false"
          @change="handleSqlSourceParamTableChange"
        />
      </div>
      <div class="mb-[16px]">
        <div class="mb-[8px] text-[var(--color-text-1)]">{{ t('apiTestDebug.storageByResult') }}</div>
        <a-input
          v-model:model-value="condition.resultVariable"
          :max-length="255"
          :placeholder="t('apiTestDebug.storageByResultPlaceholder', { a: '${result}' })"
        />
      </div>
    </template>
    <!-- 等待时间 -->
    <div v-else-if="condition.processorType === RequestConditionProcessor.TIME_WAITING">
      <div class="mb-[8px] flex items-center">
        {{ t('apiTestDebug.waitTime') }}
        <div class="text-[var(--color-text-4)]">(ms)</div>
      </div>
      <a-input-number
        v-model:model-value="condition.delay"
        mode="button"
        :step="100"
        :min="0"
        class="w-[160px]"
        model-event="input"
      />
    </div>
    <!-- 提取参数 -->
    <div v-else-if="condition.processorType === RequestConditionProcessor.EXTRACT">
      <paramTable
        ref="extractParamsTableRef"
        v-model:params="condition.extractParams"
        :default-param-item="defaultExtractParamItem"
        :columns="extractParamsColumns"
        :selectable="false"
        :scroll="{ x: '700px' }"
        :response="props.response"
        :height-used="(props.heightUsed || 0) + 68"
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
              @input="() => handleExpressionChange(rowIndex)"
              @change="() => handleExpressionChange(rowIndex)"
            >
              <template #suffix>
                <a-tooltip :disabled="!disabledExpressionSuffix">
                  <template #content>
                    <div>{{ t('apiTestDebug.expressionTip1') }}</div>
                    <div>{{ t('apiTestDebug.expressionTip2') }}</div>
                    <div>{{ t('apiTestDebug.expressionTip3') }}</div>
                  </template>
                  <MsIcon
                    type="icon-icon_flashlamp"
                    :size="15"
                    :class="
                      disabledExpressionSuffix ? 'ms-params-input-suffix-icon--disabled' : 'ms-params-input-suffix-icon'
                    "
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
            position="tl"
            trigger="click"
            :title="t('common.setting')"
            :content-style="{ width: '480px' }"
          >
            <template #content>
              <moreSetting v-model:config="activeRecord" is-popover class="mt-[12px]" />
              <div class="flex items-center justify-end gap-[8px]">
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
  <quoteSqlSourceDrawer v-model:visible="quoteSqlSourceDrawerVisible" @apply="handleQuoteSqlSourceApply" />
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
  import { useClipboard, useVModel } from '@vueuse/core';
  import { InputInstance, Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import InsertCommonScript from '@/components/business/ms-common-script/insertCommonScript.vue';
  import AddScriptDrawer from '@/components/business/ms-common-script/ms-addScriptDrawer.vue';
  import MsScriptDefined from '@/components/business/ms-common-script/scriptDefined.vue';
  import fastExtraction from '../fastExtraction/index.vue';
  import moreSetting from '../fastExtraction/moreSetting.vue';
  import paramTable, { type ParamTableColumn } from '../paramTable.vue';
  import quoteSqlSourceDrawer from '../quoteSqlSourceDrawer.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ExecuteConditionProcessor, JSONPathExtract, RegexExtract, XPathExtract } from '@/models/apiTest/debug';
  import { ParamsRequestType } from '@/models/projectManagement/commonScript';
  import {
    RequestConditionProcessor,
    RequestExtractEnvType,
    RequestExtractExpressionEnum,
    RequestExtractExpressionRuleType,
    RequestExtractResultMatchingRule,
    RequestExtractScope,
    ResponseBodyXPathAssertionFormat,
  } from '@/enums/apiEnum';

  export type ExpressionConfig = (RegexExtract | JSONPathExtract | XPathExtract) & Record<string, any>;

  const props = defineProps<{
    data: ExecuteConditionProcessor;
    response?: string; // 响应内容
    heightUsed?: number;
    isBuildIn?: boolean; // 是否是内置的条件
  }>();
  const emit = defineEmits<{
    (e: 'update:data', data: ExecuteConditionProcessor): void;
    (e: 'copy'): void;
    (e: 'delete', id: number): void;
    (e: 'change'): void;
  }>();

  const { t } = useI18n();

  const condition = useVModel(props, 'data', emit);
  // 是否显示脚本名称编辑框
  const isShowEditScriptNameInput = ref(false);
  const scriptNameInputRef = ref<InputInstance>();

  function showEditScriptNameInput() {
    isShowEditScriptNameInput.value = true;
    nextTick(() => {
      scriptNameInputRef.value?.focus();
    });
  }

  const scriptEx = ref(`// 这里可以输入脚本注释
value = vars.get("variable_name");
result = "variable_name".equals(value);
if (!result){
  msg = "assertion [" + value + " == 'variable_name']: false;";
  AssertionResult.setFailureMessage(msg);
  AssertionResult.setFailure(true);
}`);
  const { copy, isSupported } = useClipboard();

  function copyScriptEx() {
    if (isSupported) {
      copy(scriptEx.value);
      Message.success(t('apiTestDebug.scriptExCopySuccess'));
    } else {
      Message.warning(t('apiTestDebug.copyNotSupport'));
    }
  }

  const scriptDefinedRef = ref<InstanceType<typeof MsScriptDefined>>();

  function undoScript() {
    scriptDefinedRef.value?.undoHandler();
  }

  function clearScript() {
    condition.value.script = '';
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
    emit('delete', condition.value.id);
  }

  const commonScriptShowType = ref<'parameters' | 'scriptContent'>('parameters');
  const columns: MsTableColumn = [
    {
      title: 'apiTestDebug.paramName',
      slotName: 'key',
      dataIndex: 'key',
      showTooltip: true,
    },
    {
      title: 'apiTestDebug.paramValue',
      dataIndex: 'value',
      slotName: 'value',
    },
    {
      title: 'apiTestDebug.desc',
      dataIndex: 'description',
      slotName: 'description',
      showTooltip: true,
    },
  ];
  const { propsRes, propsEvent } = useTable(() => Promise.resolve([]), {
    scroll: { x: '100%' },
    columns,
    noDisable: true,
  });

  watch(
    () => condition.value.commonScriptInfo,
    (info) => {
      propsRes.value.data = info?.params as any[]; // 查看详情的时候需要赋值一下
    }
  );

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
    propsRes.value.data = (condition.value.commonScriptInfo?.params as any[]) || [];
    showQuoteDrawer.value = false;
  }

  const showAddScriptDrawer = ref(false);
  const paramsList = ref<ParamsRequestType[]>([]);
  const confirmLoading = ref(false);

  const sqlSourceColumns: ParamTableColumn[] = [
    {
      title: 'apiTestDebug.paramName',
      dataIndex: 'key',
      slotName: 'key',
    },
    {
      title: 'apiTestDebug.paramValue',
      dataIndex: 'value',
      slotName: 'value',
      isNormal: true,
    },
    {
      title: '',
      slotName: 'operation',
      width: 50,
    },
  ];
  const quoteSqlSourceDrawerVisible = ref(false);
  function handleQuoteSqlSourceApply(sqlSource: Record<string, any>) {
    condition.value.script = sqlSource.script;
    condition.value.dataSourceId = sqlSource.id;
    emit('change');
  }

  function handleSqlSourceParamTableChange(resultArr: any[], isInit?: boolean) {
    condition.value.variables = [...resultArr];
    if (!isInit) {
      emit('change');
    }
  }

  const extractParamsColumns: ParamTableColumn[] = [
    {
      title: 'apiTestDebug.paramName',
      dataIndex: 'variableName',
      slotName: 'key',
      width: 150,
    },
    {
      title: 'apiTestDebug.paramType',
      dataIndex: 'variableType',
      slotName: 'variableType',
      typeOptions: [
        {
          label: t('apiTestDebug.globalParameter'),
          value: RequestExtractEnvType.GLOBAL,
        },
        {
          label: t('apiTestDebug.envParameter'),
          value: RequestExtractEnvType.ENVIRONMENT,
        },
        {
          label: t('apiTestDebug.tempParameter'),
          value: RequestExtractEnvType.TEMPORARY,
        },
      ],
      width: 130,
    },
    {
      title: 'apiTestDebug.mode',
      dataIndex: 'extractType',
      slotName: 'extractType',
      typeOptions: [
        {
          label: t('apiTestDebug.regular'),
          value: RequestExtractExpressionEnum.REGEX,
        },
        {
          label: 'JSONPath',
          value: RequestExtractExpressionEnum.JSON_PATH,
        },
        {
          label: 'XPath',
          value: RequestExtractExpressionEnum.X_PATH,
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
  const disabledExpressionSuffix = ref(false);

  function handleExtractParamTableChange(resultArr: any[], isInit?: boolean) {
    condition.value.extractParams = [...resultArr];
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
    extractType: RequestExtractExpressionEnum.REGEX,
    expressionMatchingRule: RequestExtractExpressionRuleType.EXPRESSION,
    resultMatchingRule: RequestExtractResultMatchingRule.RANDOM,
    resultMatchingRuleNum: 1,
    responseFormat: ResponseBodyXPathAssertionFormat.XML,
    moreSettingPopoverVisible: false,
  };
  const fastExtractionVisible = ref(false);
  const activeRecord = ref({ ...defaultExtractParamItem }); // 用于暂存当前操作的提取参数表格项

  function showFastExtraction(record: ExpressionConfig) {
    activeRecord.value = { ...record };
    fastExtractionVisible.value = true;
  }

  function handleExpressionChange(rowIndex: number) {
    extractParamsTableRef.value?.addTableLine(rowIndex);
  }

  /**
   * 处理提取参数表格更多操作
   */
  function handleExtractParamMoreActionSelect(event: ActionsItem, record: ExpressionConfig) {
    activeRecord.value = { ...record };
    if (event.eventTag === 'copy') {
      emit('copy');
    } else if (event.eventTag === 'setting') {
      record.moreSettingPopoverVisible = true;
    }
  }

  /**
   * 提取参数表格-应用更多设置
   */
  function applyMoreSetting(record: ExpressionConfig) {
    condition.value.extractParams = condition.value.extractParams?.map((e) => {
      if (e.id === activeRecord.value.id) {
        record.moreSettingPopoverVisible = false;
        return {
          ...activeRecord.value,
          moreSettingPopoverVisible: false,
        } as any; // TOOD: 这里的后台类型应该是不对的，需要修改
      }
      return e;
    });
    emit('change');
  }

  /**
   * 提取参数表格-保存快速提取的配置
   */
  function handleFastExtractionApply(config: RegexExtract | JSONPathExtract | XPathExtract) {
    condition.value.extractParams = condition.value.extractParams?.map((e) => {
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
        condition.value.extractParams?.findIndex((e) => e.id === activeRecord.value.id) || 0
      );
    });
    emit('change');
  }
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
  .param-popover-subtitle {
    margin-bottom: 2px;
    font-size: 12px;
    line-height: 16px;
    color: var(--color-text-4);
  }
  .param-popover-value {
    min-width: 100px;
    max-width: 280px;
    font-size: 12px;
    line-height: 16px;
    color: var(--color-text-1);
  }
</style>
