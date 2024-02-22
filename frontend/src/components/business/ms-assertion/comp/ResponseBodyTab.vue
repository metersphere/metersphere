<template>
  <div class="flex w-full flex-col">
    <div>
      <a-radio-group v-model:model-value="activeTab" type="button" size="small">
        <a-radio v-for="item of responseRadios" :key="item.value" :value="item.value">
          {{ t(item.label) }}
        </a-radio>
      </a-radio-group>
    </div>
    <div v-if="activeTab === 'jsonPath'" class="mt-[16px]">
      <paramsTable
        v-model:params="innerParams.jsonPath"
        :selectable="false"
        :columns="jsonPathColumns"
        :scroll="{ minWidth: '700px' }"
        :default-param-item="jsonPathDefaultParamItem"
        @change="handleChange"
        @more-action-select="(e,r)=> handleExtractParamMoreActionSelect(e,r as ExpressionConfig)"
      >
        <template #expression="{ record }">
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
              @input="handleExpressionChange"
              @change="handleExpressionChange"
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
                    @click.stop="() => showFastExtraction(record, RequestExtractExpressionEnum.JSON_PATH)"
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
      </paramsTable>
    </div>
    <div v-if="activeTab === 'xPath'" class="mt-[16px]">
      <div class="text-[var(--color-text-1)]">{{ t('ms.assertion.responseContentType') }}</div>
      <a-radio-group
        v-model:model-value="innerParams.xPath.responseFormat"
        class="mb-[16px] mt-[16px]"
        type="button"
        size="small"
      >
        <a-radio value="XML">XML</a-radio>
        <a-radio value="HTML">HTML</a-radio>
      </a-radio-group>
      <paramsTable
        v-model:params="innerParams.xPath.data"
        :selectable="false"
        :columns="xPathColumns"
        :scroll="{ minWidth: '700px' }"
        :default-param-item="xPathDefaultParamItem"
        @change="handleChange"
        @more-action-select="(e,r)=> handleExtractParamMoreActionSelect(e,r as ExpressionConfig)"
      >
        <template #expression="{ record }">
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
              @input="handleExpressionChange"
              @change="handleExpressionChange"
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
                    @click.stop="() => showFastExtraction(record, RequestExtractExpressionEnum.JSON_PATH)"
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
      </paramsTable>
    </div>
    <div v-if="activeTab === 'document'">
      <paramsTable
        v-model:params="innerParams.document.data"
        :selectable="false"
        :columns="documentColumns"
        :scroll="{
          minWidth: '700px',
        }"
        :default-param-item="documentDefaultParamItem"
        @change="handleChange"
        @more-action-select="(e,r)=> handleExtractParamMoreActionSelect(e,r as ExpressionConfig)"
      >
        <template #operationPre="{ record }">
          <a-tooltip v-if="['object', 'array'].includes(record.paramType)" :content="t('ms.assertion.addChild')">
            <div
              class="flex h-[24px] w-[24px] cursor-pointer items-center justify-center rounded text-[rgb(var(--primary-5))] hover:bg-[rgb(var(--primary-1))]"
              @click="addChild"
            >
              <icon-plus size="14" />
            </div>
          </a-tooltip>
          <a-tooltip v-else :content="t('ms.assertion.validateChild')">
            <div
              class="flex h-[24px] w-[24px] cursor-pointer items-center justify-center rounded text-[rgb(var(--primary-5))] hover:bg-[rgb(var(--primary-1))]"
              @click="addValidateChild"
            >
              <icon-bookmark size="14" />
            </div>
          </a-tooltip>
        </template>
      </paramsTable>
    </div>
    <div v-if="activeTab === 'regular'" class="mt-[16px]">
      <paramsTable
        v-model:params="innerParams.regular"
        :selectable="false"
        :columns="xPathColumns"
        :scroll="{ minWidth: '700px' }"
        :default-param-item="xPathDefaultParamItem"
        @change="handleChange"
        @more-action-select="(e,r)=> handleExtractParamMoreActionSelect(e,r as ExpressionConfig)"
      >
        <template #expression="{ record }">
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
              @input="handleExpressionChange"
              @change="handleExpressionChange"
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
                    @click.stop="() => showFastExtraction(record, RequestExtractExpressionEnum.JSON_PATH)"
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
      </paramsTable>
    </div>
    <div v-if="activeTab === 'script'" class="mt-[16px]">
      <conditionContent
        :data="innerParams.script"
        class="mt-[16px]"
        @copy="copyListItem"
        @delete="deleteListItem"
        @change="emit('change')"
      />
    </div>
  </div>
  <fastExtraction v-model:visible="fastExtractionVisible" :config="activeRecord" @apply="handleFastExtractionApply" />
</template>

<script setup lang="ts">
  import { statusCodeOptions } from '@/components/pure/ms-advance-filter';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import conditionContent from '@/views/api-test/components/condition/content.vue';
  import fastExtraction from '@/views/api-test/components/fastExtraction/index.vue';
  import moreSetting from '@/views/api-test/components/fastExtraction/moreSetting.vue';
  import paramsTable, { type ParamTableColumn } from '@/views/api-test/components/paramTable.vue';

  import { useI18n } from '@/hooks/useI18n';

  import {
    ExecuteConditionProcessor,
    ExpressionType,
    JSONPathExtract,
    RegexExtract,
    XPathExtract,
  } from '@/models/apiTest/debug';
  import {
    RequestConditionProcessor,
    RequestConditionScriptLanguage,
    RequestExtractEnvType,
    RequestExtractExpressionEnum,
    RequestExtractExpressionRuleType,
    RequestExtractResultMatchingRule,
    RequestExtractScope,
    ResponseBodyXPathAssertionFormat,
  } from '@/enums/apiEnum';

  interface Param {
    [key: string]: any;
  }

  const emit = defineEmits<{
    (e: 'update:data', data: ExecuteConditionProcessor): void;
    (e: 'copy'): void;
    (e: 'delete', id: number): void;
    (e: 'change'): void;
  }>();
  const { t } = useI18n();

  // const innerParams = defineModel<Param>('modelValue', {
  //   default: {
  //     jsonPath: [],
  //     xPath: { responseFormat: 'XML', data: [] },
  //     script: {
  //       id: new Date().getTime(),
  //       processorType: RequestConditionProcessor.SCRIPT,
  //       scriptName: '断言脚本名称',
  //       enableCommonScript: false,
  //       params: [],
  //     },
  //   },
  // });
  const innerParams = ref<Param>({
    jsonPath: [],
    xPath: { responseFormat: 'XML', data: [] },
    document: {
      data: [],
      responseFormat: 'JSON',
      followApi: false,
    },
    script: {
      id: new Date().getTime(),
      processorType: RequestConditionProcessor.SCRIPT,
      scriptName: '断言脚本名称',
      enableCommonScript: false,
      params: [],
      scriptId: '',
      scriptLanguage: RequestConditionScriptLanguage.JAVASCRIPT,
      script: new Date().getTime().toString(),
    },
  });
  const activeTab = ref('jsonPath');
  const extractParamsTableRef = ref<InstanceType<typeof paramsTable>>();
  const fastExtractionVisible = ref(false);
  const disabledExpressionSuffix = ref(false);
  export type ExpressionConfig = (RegexExtract | JSONPathExtract | XPathExtract) & Record<string, any>;

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

  const activeRecord = ref({ ...defaultExtractParamItem }); // 用于暂存当前操作的提取参数表格项

  const responseRadios = [
    { label: 'ms.assertion.jsonPath', value: 'jsonPath' },
    { label: 'ms.assertion.xpath', value: 'xPath' },
    { label: 'ms.assertion.document', value: 'document' },
    { label: 'ms.assertion.regular', value: 'regular' },
    { label: 'ms.assertion.script', value: 'script' },
  ];

  const jsonPathColumns: ParamTableColumn[] = [
    {
      title: 'ms.assertion.expression',
      dataIndex: 'expression',
      slotName: 'expression',
    },
    {
      title: 'ms.assertion.matchCondition',
      dataIndex: 'matchCondition',
      slotName: 'matchCondition',
      options: statusCodeOptions,
    },
    {
      title: 'ms.assertion.matchValue',
      dataIndex: 'matchValue',
      slotName: 'matchValue',
    },
    {
      title: '',
      slotName: 'operation',
      fixed: 'right',
      width: 130,
      hasDisable: true,
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
    },
  ];

  const jsonPathDefaultParamItem = {
    expression: '',
    matchCondition: '',
    matchValue: '',
    enable: true,
  };
  const handleChange = () => {
    emit('change');
  };
  function handleExpressionChange(val: string) {
    extractParamsTableRef.value?.addTableLine(val, 'expression');
  }

  const xPathColumns: ParamTableColumn[] = [
    {
      title: 'ms.assertion.expression',
      dataIndex: 'expression',
      slotName: 'expression',
    },
    {
      title: '',
      slotName: 'operation',
      fixed: 'right',
      width: 130,
      hasDisable: true,
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
    },
  ];

  const xPathDefaultParamItem = {
    expression: '',
    enable: true,
  };

  const documentColumns: ParamTableColumn[] = [
    {
      title: 'ms.assertion.paramsName',
      dataIndex: 'paramsName',
      slotName: 'key',
    },
    {
      title: 'ms.assertion.mustInclude',
      dataIndex: 'mustInclude',
      slotName: 'mustContain',
      titleSlotName: 'documentMustIncludeTitle',
      align: 'left',
    },
    {
      title: 'ms.assertion.typeChecking',
      dataIndex: 'typeChecking',
      slotName: 'typeChecking',
      titleSlotName: 'documentTypeCheckingTitle',
      align: 'left',
    },
    {
      title: 'project.environmental.paramType',
      dataIndex: 'paramType',
      slotName: 'paramType',
      showInTable: true,
      showDrag: true,
      columnSelectorDisabled: true,
      typeOptions: [
        { label: 'object', value: 'object' },
        { label: 'array', value: 'array' },
        { label: 'string', value: 'string' },
        { label: 'integer', value: 'integer' },
        { label: 'number', value: 'number' },
        { label: 'boolean', value: 'boolean' },
      ],
      titleSlotName: 'typeTitle',
      typeTitleTooltip: t('project.environmental.paramTypeTooltip'),
    },
    {
      title: 'ms.assertion.matchCondition',
      dataIndex: 'matchCondition',
      slotName: 'matchCondition',
      options: statusCodeOptions,
    },
    {
      title: 'ms.assertion.matchValue',
      dataIndex: 'matchValue',
      slotName: 'matchValue',
      hasRequired: true,
    },
    {
      title: '',
      slotName: 'operation',
      fixed: 'right',
      width: 130,
    },
  ];
  const documentDefaultParamItem = {
    id: new Date().getTime(),
    paramsName: '',
    mustInclude: false,
    typeChecking: false,
    paramType: '',
    matchCondition: '',
    matchValue: '',
  };

  /**
   * 提取参数表格-应用更多设置
   */
  function applyMoreSetting(record: ExpressionConfig) {
    // condition.value.extractParams = condition.value.extractParams?.map((e) => {
    //   if (e.id === activeRecord.value.id) {
    //     record.moreSettingPopoverVisible = false;
    //     return {
    //       ...activeRecord.value,
    //       moreSettingPopoverVisible: false,
    //     } as any; // TOOD: 这里的后台类型应该是不对的，需要修改
    //   }
    //   return e;
    // });
    // emit('change');
  }

  /**
   * 提取参数表格-保存快速提取的配置
   */
  function handleFastExtractionApply(config: RegexExtract | JSONPathExtract | XPathExtract) {
    // condition.value.extractParams = condition.value.extractParams?.map((e) => {
    //   if (e.id === activeRecord.value.id) {
    //     return {
    //       ...e,
    //       ...config,
    //     };
    //   }
    //   return e;
    // });
    // fastExtractionVisible.value = false;
    // nextTick(() => {
    //   extractParamsTableRef.value?.addTableLine();
    // });
    // emit('change');
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
   * 复制列表项
   */
  function copyListItem() {}

  /**
   * 删除列表项
   */
  function deleteListItem(id: string | number) {}

  function showFastExtraction(record: ExpressionConfig, type: ExpressionType) {
    activeRecord.value = { ...record, extractType: type };
    fastExtractionVisible.value = true;
  }
  // 新增子项
  const addChild = (record: Record<string, any>) => {
    const children = record.children || [];
    const newRecord = {
      id: new Date().getTime(),
      parentId: record.id,
      rowIndex: children.length,
    };
    record.children = [...children, newRecord];
  };

  // 添加验证子项
  const addValidateChild = (record: Record<string, any>) => {
    const children = record.children || [];
    const newRecord = {
      id: new Date().getTime(),
      parentId: record.id,
      rowIndex: children.length,
    };
    record.children = [...children, newRecord];
  };
</script>
