<template>
  <div class="flex w-full flex-col">
    <div>
      <a-radio-group v-model:model-value="condition.assertionBodyType" type="button" size="small">
        <a-radio v-for="item of responseRadios" :key="item.value" :value="item.value">
          {{ t(item.label) }}
        </a-radio>
      </a-radio-group>
    </div>
    <!-- jsonPath开始 -->
    <div v-if="condition.assertionBodyType === ResponseBodyAssertionType.JSON_PATH" class="mt-[16px]">
      <paramsTable
        ref="extractParamsTableRef"
        v-model:params="condition.jsonPathAssertion.assertions"
        :disabled-except-param="props.disabled"
        :selectable="true"
        :response="props.response"
        :columns="jsonPathColumns"
        :scroll="{ minWidth: '100%' }"
        :default-param-item="jsonPathDefaultParamItem"
        @change="(data:any[],isInit?: boolean) => handleChange(data, ResponseBodyAssertionType.JSON_PATH,isInit)"
        @more-action-select="(e, r) => handleExtractParamMoreActionSelect(e, r)"
      >
        <template #expression="{ record, rowIndex }">
          <a-popover
            position="tl"
            :disabled="!record.expression || record.expression.trim() === ''"
            class="ms-params-input-popover"
          >
            <template #content>
              <div class="ms-params-popover-title">
                {{ t('apiTestDebug.expression') }}
              </div>
              <div class="ms-params-popover-value">
                {{ record.expression }}
              </div>
            </template>
            <a-input
              v-model:model-value="record.expression"
              class="ms-params-input"
              :max-length="255"
              :disabled="props.disabled"
              :placeholder="t('apiTestDebug.commonPlaceholder')"
              @input="() => handleExpressionChange(rowIndex)"
              @change="() => handleExpressionChange(rowIndex)"
            >
              <template #suffix>
                <a-tooltip :disabled="props.disabled || !!props.response">
                  <template #content>
                    <div>{{ t('apiTestDebug.expressionTip1') }}</div>
                    <div>{{ t('apiTestDebug.expressionTip2') }}</div>
                    <div>{{ t('apiTestDebug.expressionTip3') }}</div>
                  </template>
                  <MsIcon
                    v-if="props.showExtraction"
                    :disabled="props.disabled"
                    type="icon-icon_flashlamp"
                    :size="15"
                    :class="
                      props.disabled || !props.response
                        ? 'ms-params-input-suffix-icon--disabled'
                        : 'ms-params-input-suffix-icon'
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
              <!-- <moreSetting v-model:config="activeRecord" is-popover class="mt-[12px]" /> -->
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
        <!-- <template #expectedTitle="{ columnConfig }">
          <div class="flex items-center text-[var(--color-text-3)]">
            {{ t('apiTestDebug.paramType') }}
            <a-tooltip :content="columnConfig.typeTitleTooltip" position="right">
              <icon-question-circle
                class="ml-[4px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
                size="16"
              />
            </a-tooltip>
          </div>
        </template> -->
      </paramsTable>
    </div>
    <!-- jsonPath结束 -->
    <!-- xPath开始 -->
    <div v-if="condition.assertionBodyType === ResponseBodyAssertionType.XPATH" class="mt-[16px]">
      <div class="text-[var(--color-text-1)]">{{ t('ms.assertion.responseContentType') }}</div>
      <a-radio-group
        v-model="condition.xpathAssertion.responseFormat"
        class="mb-[16px] mt-[16px]"
        type="button"
        size="small"
      >
        <a-radio key="XML" value="XML">XML</a-radio>
        <a-radio key="HTML" value="HTML">HTML</a-radio>
      </a-radio-group>
      <paramsTable
        ref="extractParamsTableRef"
        v-model:params="condition.xpathAssertion.assertions"
        :disabled-except-param="props.disabled"
        :selectable="true"
        :columns="xPathColumns"
        :scroll="{ minWidth: '100%' }"
        :default-param-item="xpathAssertParamsItem"
        @change="(data:any[],isInit?: boolean) => handleChange(data, ResponseBodyAssertionType.XPATH,isInit)"
        @more-action-select="(e, r) => handleExtractParamMoreActionSelect(e, r)"
      >
        <template #expression="{ record, rowIndex }">
          <a-popover
            position="tl"
            :disabled="!record.expression || record.expression.trim() === ''"
            class="ms-params-input-popover"
          >
            <template #content>
              <div class="ms-params-popover-title">
                {{ t('apiTestDebug.expression') }}
              </div>
              <div class="ms-params-popover-value">
                {{ record.expression }}
              </div>
            </template>
            <a-input
              v-model:model-value="record.expression"
              :disabled="props.disabled"
              class="ms-params-input"
              :max-length="255"
              :placeholder="t('apiTestDebug.commonPlaceholder')"
              @input="() => handleExpressionChange(rowIndex)"
              @change="() => handleExpressionChange(rowIndex)"
            >
              <template #suffix>
                <a-tooltip :disabled="props.disabled || !!props.response">
                  <template #content>
                    <div>{{ t('apiTestDebug.expressionTip1') }}</div>
                    <div>{{ t('apiTestDebug.expressionTip2') }}</div>
                    <div>{{ t('apiTestDebug.expressionTip3') }}</div>
                  </template>
                  <MsIcon
                    v-if="props.showExtraction"
                    type="icon-icon_flashlamp"
                    :size="15"
                    :class="
                      props.disabled || !props.response
                        ? 'ms-params-input-suffix-icon--disabled'
                        : 'ms-params-input-suffix-icon'
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
              <!-- <moreSetting v-model:config="activeRecord" is-popover class="mt-[12px]" /> -->
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
    <!-- xPath结束 -->
    <!-- document开始 -->
    <div v-if="condition.assertionBodyType === ResponseBodyAssertionType.DOCUMENT" class="relative mt-[16px]">
      <div class="text-[var(--color-text-1)]">
        {{ t('ms.assertion.responseContentType') }}
      </div>
      <a-radio-group v-model:model-value="condition.document.responseFormat" class="mt-[16px]" size="small">
        <a-radio value="JSON">JSON</a-radio>
        <a-radio value="XML">XML</a-radio>
      </a-radio-group>
      <div class="mt-[16px]">
        <a-checkbox v-model:model-value="condition.document.followApi" :disabled="props.disabled">
          <span class="text-[var(--color-text-1)]">{{ t('ms.assertion.followApi') }}</span>
        </a-checkbox>
      </div>
      <div class="mt-[16px]">
        <paramsTable
          v-model:params="condition.document.jsonAssertion"
          :disabled-except-param="props.disabled"
          :selectable="false"
          :columns="documentColumns"
          :scroll="{ minWidth: '100%' }"
          :height-used="580"
          :default-param-item="documentDefaultParamItem"
          :span-method="documentSpanMethod"
          is-tree-table
          @tree-delete="deleteAllParam"
          @change="(data) => handleChange(data, ResponseBodyAssertionType.DOCUMENT)"
        >
          <template #matchValueDelete="{ record }">
            <icon-minus-circle
              v-if="showDeleteSingle && (record.rowSpan > 1 || record.groupId)"
              class="ml-[8px] cursor-pointer text-[var(--color-text-4)]"
              size="20"
              :disabled="props.disabled"
              @click="deleteSingleParam(record)"
            />
          </template>
          <template #operationPre="{ record }">
            <a-tooltip v-if="['object', 'array'].includes(record.paramType)" :content="t('ms.assertion.addChild')">
              <div
                class="flex h-[24px] w-[24px] cursor-pointer items-center justify-center rounded text-[rgb(var(--primary-5))] hover:bg-[rgb(var(--primary-1))]"
                @click="addChild(record)"
              >
                <icon-plus size="16" />
              </div>
            </a-tooltip>
            <a-tooltip
              v-else-if="['string', 'integer', 'number', 'boolean'].includes(record.paramType) && record.id !== rootId"
              :content="t('ms.assertion.validateChild')"
            >
              <div
                class="flex h-[24px] w-[24px] cursor-pointer items-center justify-center rounded text-[rgb(var(--primary-5))] hover:bg-[rgb(var(--primary-1))]"
                @click="addValidateChild(record)"
              >
                <icon-bookmark size="16" />
              </div>
            </a-tooltip>
          </template>
        </paramsTable>
      </div>
    </div>
    <!-- document结束 -->
    <!-- 正则开始 -->
    <div v-if="condition.assertionBodyType === ResponseBodyAssertionType.REGEX" class="mt-[16px]">
      <paramsTable
        ref="extractParamsTableRef"
        v-model:params="condition.regexAssertion.assertions"
        :selectable="true"
        :disabled-except-param="props.disabled"
        :columns="regexColumns"
        :scroll="{ minWidth: '100%' }"
        :default-param-item="regexDefaultParamItem"
        @change="(data) => handleChange(data, ResponseBodyAssertionType.REGEX)"
        @more-action-select="(e, r) => handleExtractParamMoreActionSelect(e, r)"
      >
        <template #expression="{ record, rowIndex }">
          <a-popover
            position="tl"
            :disabled="!record.expression || record.expression.trim() === ''"
            class="ms-params-input-popover"
          >
            <template #content>
              <div class="ms-params-popover-title">
                {{ t('apiTestDebug.expression') }}
              </div>
              <div class="ms-params-popover-value">
                {{ record.expression }}
              </div>
            </template>
            <a-input
              v-model:model-value="record.expression"
              :disabled="props.disabled"
              class="ms-params-input"
              :max-length="255"
              @input="() => handleExpressionChange(rowIndex)"
              @change="() => handleExpressionChange(rowIndex)"
            >
              <template #suffix>
                <a-tooltip :disabled="props.disabled || !!props.response">
                  <template #content>
                    <div>{{ t('apiTestDebug.expressionTip1') }}</div>
                    <div>{{ t('apiTestDebug.expressionTip2') }}</div>
                    <div>{{ t('apiTestDebug.expressionTip3') }}</div>
                  </template>
                  <MsIcon
                    v-if="props.showExtraction"
                    type="icon-icon_flashlamp"
                    :size="15"
                    :class="
                      props.disabled || !props.response
                        ? 'ms-params-input-suffix-icon--disabled'
                        : 'ms-params-input-suffix-icon'
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
      </paramsTable>
    </div>
    <!-- 正则结束 -->
    <fastExtraction
      v-model:visible="fastExtractionVisible"
      :config="activeRecord"
      :response="props.response"
      :is-show-more-setting="false"
      @apply="handleFastExtractionApply"
    />
  </div>
</template>

<script setup lang="ts">
  import { TableColumnData, TableData } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import { statusCodeOptions } from '@/components/pure/ms-advance-filter';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import { TableOperationColumn } from '@/components/business/ms-user-group-comp/authTable.vue';
  import fastExtraction from '@/views/api-test/components/fastExtraction/index.vue';
  import moreSetting from '@/views/api-test/components/fastExtraction/moreSetting.vue';
  import paramsTable, { type ParamTableColumn } from '@/views/api-test/components/paramTable.vue';

  import { useI18n } from '@/hooks/useI18n';
  import {
    countNodes,
    countNodesByGroupId,
    deleteNodeById,
    deleteNodesByGroupId,
    findFirstByGroupId,
    insertNode,
  } from '@/utils/tree';

  import { JSONPathExtract, RegexExtract, XPathExtract } from '@/models/apiTest/common';
  import { RequestExtractExpressionEnum, ResponseBodyAssertionType } from '@/enums/apiEnum';

  import {
    jsonPathDefaultParamItem,
    regexDefaultParamItem,
    xpathAssertParamsItem,
  } from '@/views/api-test/components/config';

  const { t } = useI18n();

  // TODO: Param
  interface Param {
    [key: string]: any;
  }

  const props = withDefaults(
    defineProps<{
      response?: string;
      disabled?: boolean;
      showExtraction?: boolean;
    }>(),
    {
      showExtraction: false,
    }
  );

  const emit = defineEmits<{
    (e: 'copy'): void;
    (e: 'delete', id: number): void;
    (e: 'change', param: Param): void;
  }>();

  const condition = defineModel<Param>('data', {
    required: true,
  });

  const rootId = 0; // 1970-01-01 00:00:00 UTC

  const activeTab = ref(ResponseBodyAssertionType.JSON_PATH);

  const defaultParamItem = {
    jsonPathAssertion: {
      assertions: [],
    },
    xpathAssertion: { responseFormat: 'XML', assertions: [] },
    assertionBodyType: activeTab.value,
    regexAssertion: {
      assertions: [],
    },
  };

  const extractParamsTableRef = ref<InstanceType<typeof paramsTable>>();
  const fastExtractionVisible = ref(false);
  // const disabledExpressionSuffix = ref(false);
  export type ExpressionConfig = (RegexExtract | JSONPathExtract | XPathExtract) & Record<string, any>;

  const activeRecord = ref<any>({ ...xpathAssertParamsItem, id: '' }); // 用于暂存当前操作的提取参数表格项

  const responseRadios = [
    { label: 'ms.assertion.jsonPath', value: ResponseBodyAssertionType.JSON_PATH },
    { label: 'ms.assertion.xpath', value: ResponseBodyAssertionType.XPATH },
    // { label: 'ms.assertion.document', value: 'document' },
    { label: 'ms.assertion.regular', value: ResponseBodyAssertionType.REGEX },
  ];

  const jsonPathColumns: ParamTableColumn[] = [
    {
      title: 'ms.assertion.expression',
      dataIndex: 'expression',
      slotName: 'expression',
    },
    {
      title: 'ms.assertion.matchCondition',
      dataIndex: 'condition',
      slotName: 'condition',
      options: statusCodeOptions,
      width: 150,
    },
    {
      title: 'ms.assertion.matchValue',
      dataIndex: 'expectedValue',
      slotName: 'expectedValue',
      titleSlotName: 'expectedTitle',
      // typeTitleTooltip: t('ms.assertion.expectedValueTitle'),
    },
    {
      title: '',
      slotName: 'operation',
      fixed: 'right',
      width: 80,
      moreAction: [
        {
          eventTag: 'copy',
          label: 'common.copy',
        },
        // {
        //   eventTag: 'setting',
        //   label: 'common.setting',
        // },
      ],
    },
  ];

  onBeforeMount(() => {
    condition.value.jsonPathAssertion.assertions = condition.value.jsonPathAssertion.assertions?.map((e: Param) => {
      return {
        ...e,
        extractType: RequestExtractExpressionEnum.JSON_PATH,
      };
    });
    condition.value.xpathAssertion.assertions = condition.value.xpathAssertion.assertions?.map((e: Param) => {
      return {
        ...e,
        extractType: RequestExtractExpressionEnum.X_PATH,
      };
    });
    condition.value.regexAssertion.assertions = condition.value.regexAssertion.assertions?.map((e: Param) => {
      return {
        ...e,
        extractType: RequestExtractExpressionEnum.REGEX,
      };
    });
  });

  const handleChange = (data: any[], type: string, isInit?: boolean) => {
    switch (type) {
      case ResponseBodyAssertionType.JSON_PATH:
        condition.value.jsonPathAssertion.assertions = data;
        if (!isInit) {
          emit('change', { ...condition.value });
        }
        break;
      case ResponseBodyAssertionType.XPATH:
        condition.value.xpathAssertion.assertions = data;
        if (!isInit) {
          emit('change', {
            ...condition.value,
          });
        }
        break;
      case ResponseBodyAssertionType.DOCUMENT:
        condition.value.documentAssertion.jsonAssertion = data;
        break;
      case ResponseBodyAssertionType.REGEX:
        condition.value.regexAssertion.assertions = data;
        if (!isInit) {
          emit('change', { ...defaultParamItem, ...condition.value });
        }
        break;
      default:
        break;
    }
  };
  function handleExpressionChange(rowIndex: number) {
    extractParamsTableRef.value?.addTableLine(rowIndex);
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
      moreAction: [
        {
          eventTag: 'copy',
          label: 'common.copy',
        },
        // {
        //   eventTag: 'setting',
        //   label: 'common.setting',
        // },
      ],
    },
  ];
  const regexColumns: ParamTableColumn[] = [
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
      moreAction: [
        {
          eventTag: 'copy',
          label: 'common.copy',
        },
        // TODO 后台没有写 先不要了
        // {
        //   eventTag: 'setting',
        //   label: 'common.setting',
        // },
      ],
    },
  ];

  const documentColumns: ParamTableColumn[] = [
    {
      title: 'ms.assertion.paramsName',
      dataIndex: 'paramsName',
      slotName: 'key',
      addLineDisabled: true,
      width: 300,
    },
    {
      title: 'ms.assertion.mustInclude',
      dataIndex: 'mustInclude',
      slotName: 'mustContain',
      titleSlotName: 'documentMustIncludeTitle',
      align: 'left',
      width: 80,
    },
    {
      title: 'ms.assertion.typeChecking',
      dataIndex: 'typeChecking',
      slotName: 'typeChecking',
      titleSlotName: 'documentTypeCheckingTitle',
      align: 'left',
      width: 100,
    },
    {
      title: 'project.environmental.paramType',
      dataIndex: 'paramType',
      slotName: 'paramType',
      showInTable: true,
      showDrag: true,
      columnSelectorDisabled: true,
      addLineDisabled: true,
      options: [
        { label: 'object', value: 'object' },
        { label: 'array', value: 'array' },
        { label: 'string', value: 'string' },
        { label: 'integer', value: 'integer' },
        { label: 'number', value: 'number' },
        { label: 'boolean', value: 'boolean' },
      ],
      titleSlotName: 'typeTitle',
      typeTitleTooltip: t('project.environmental.paramTypeTooltip'),
      width: 100,
    },
    {
      title: 'ms.assertion.matchCondition',
      dataIndex: 'matchCondition',
      slotName: 'matchCondition',
      options: statusCodeOptions,
      width: 120,
    },
    {
      title: 'ms.assertion.matchValue',
      dataIndex: 'matchValue',
      slotName: 'matchValue',
      hasRequired: true,
      showDelete: true,
    },
    {
      title: '',
      slotName: 'operation',
      fixed: 'right',
      width: 100,
      align: 'right',
    },
  ];

  const documentDefaultParamItem = {
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
    switch (condition.value.assertionBodyType) {
      case ResponseBodyAssertionType.JSON_PATH:
        condition.value.jsonPathAssertion.assertions = condition.value.jsonPathAssertion.assertions?.map((e: Param) => {
          if (e.id === activeRecord.value.id) {
            record.moreSettingPopoverVisible = false;
            return {
              ...activeRecord.value,
              moreSettingPopoverVisible: false,
            } as any;
          }
          return e;
        });
        break;
      case ResponseBodyAssertionType.XPATH:
        condition.value.xpathAssertion.assertions = condition.value.xpathAssertion.assertions?.map((e: Param) => {
          if (e.id === activeRecord.value.id) {
            record.moreSettingPopoverVisible = false;
            return {
              ...activeRecord.value,
              moreSettingPopoverVisible: false,
            } as any;
          }
          return e;
        });
        break;
      case ResponseBodyAssertionType.REGEX:
        condition.value.regexAssertion.assertions = condition.value.regexAssertion.assertions?.map((e: Param) => {
          if (e.id === activeRecord.value.id) {
            record.moreSettingPopoverVisible = false;
            return {
              ...activeRecord.value,
              moreSettingPopoverVisible: false,
            } as any;
          }
          return { ...e };
        });
        break;
      default:
        break;
    }
    emit('change', { ...condition.value });
  }

  /**
   * 提取参数表格-保存快速提取的配置
   */
  function handleFastExtractionApply(
    config: RegexExtract | JSONPathExtract | XPathExtract,
    matchResult: string[] | string
  ) {
    condition.value.jsonPathAssertion.assertions = condition.value.jsonPathAssertion.assertions?.map((e: Param) => {
      if (e.id === activeRecord.value.id) {
        return {
          ...e,
          ...config,
          expectedValue: Array.isArray(matchResult) ? JSON.stringify(matchResult) : matchResult,
        };
      }
      return e;
    });
    condition.value.xpathAssertion.assertions = condition.value.xpathAssertion.assertions?.map((e: Param) => {
      if (e.id === activeRecord.value.id) {
        return {
          ...e,
          ...config,
        };
      }
      return e;
    });
    condition.value.regexAssertion.assertions = condition.value.regexAssertion.assertions?.map((e: Param) => {
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
      if (condition.value.assertionBodyType === ResponseBodyAssertionType.JSON_PATH) {
        extractParamsTableRef.value?.addTableLine(
          condition.value.jsonPathAssertion.assertions?.findIndex((e: Param) => e.id === activeRecord.value.id) || 0
        );
      }
      if (condition.value.assertionBodyType === ResponseBodyAssertionType.XPATH) {
        extractParamsTableRef.value?.addTableLine(
          condition.value.xpathAssertion.assertions?.findIndex((e: Param) => e.id === activeRecord.value.id) || 0
        );
      }
      if (condition.value.assertionBodyType === ResponseBodyAssertionType.REGEX) {
        extractParamsTableRef.value?.addTableLine(
          condition.value.xpathAssertion.regexAssertion?.findIndex((e: Param) => e.id === activeRecord.value.id) || 0
        );
      }
    });
    emit('change', { ...condition.value });
  }

  function copyItem(record: Record<string, any>) {
    switch (condition.value.assertionBodyType) {
      case ResponseBodyAssertionType.JSON_PATH:
        const jsonIndex = condition.value.jsonPathAssertion.assertions.findIndex(
          (item: Param) => item.id === record.id
        );
        if (jsonIndex > -1) {
          condition.value.jsonPathAssertion.assertions.splice(jsonIndex, 0, {
            ...record,
            id: new Date().getTime().toString(),
          });
          const temArr = cloneDeep(condition.value.jsonPathAssertion.assertions);
          condition.value.jsonPathAssertion.assertions = temArr;
          emit('change', { ...condition.value });
        }
        break;
      case ResponseBodyAssertionType.XPATH:
        const xpathIndex = condition.value.xpathAssertion.assertions.findIndex((item: Param) => item.id === record.id);
        if (xpathIndex > -1) {
          condition.value.xpathAssertion.assertions.splice(xpathIndex, 0, {
            ...record,
            id: new Date().getTime().toString(),
          });
          const temArr = cloneDeep(condition.value.xpathAssertion.assertions);
          condition.value.xpathAssertion.assertions = temArr;
        }
        emit('change', {
          ...condition.value,
        });
        break;
      case ResponseBodyAssertionType.DOCUMENT:
        condition.value.documentAssertion.jsonAssertion.push({
          ...record,
          id: new Date().getTime().toString(),
        });
        break;
      case ResponseBodyAssertionType.REGEX:
        const regIndex = condition.value.regexAssertion.assertions.findIndex((item: Param) => item.id === record.id);
        if (regIndex > -1) {
          condition.value.regexAssertion.assertions.splice(regIndex, 0, {
            ...record,
            id: new Date().getTime().toString(),
          });
          const temArr = cloneDeep(condition.value.regexAssertion.assertions);
          condition.value.regexAssertion.assertions = temArr;
        }
        emit('change', { ...condition.value });
        break;
      default:
        break;
    }
  }

  /**
   * 处理提取参数表格更多操作
   */
  function handleExtractParamMoreActionSelect(event: ActionsItem, record: Record<string, any>) {
    activeRecord.value = { ...record };
    if (event.eventTag === 'copy') {
      copyItem(record);
    } else if (event.eventTag === 'setting') {
      record.moreSettingPopoverVisible = true;
    }
  }

  function showFastExtraction(record: Record<string, any>) {
    if (props.disabled || !props.response) return;
    activeRecord.value = { ...record };
    fastExtractionVisible.value = true;
  }
  // 新增子项
  const addChild = (record: Record<string, any>) => {
    const children = record.children || [];
    const newRecord = {
      ...documentDefaultParamItem,
      id: new Date().getTime(),
      parentId: record.id,
    };
    record.children = [...children, newRecord];
  };

  // 添加验证子项
  const addValidateChild = (record: Record<string, any>) => {
    if (record.groupId) {
      // 子项点击，找到父级
      const parent = condition.value.documentAssertion.jsonAssertion.find((item: any) => item.id === record.groupId);
      insertNode(
        condition.value.documentAssertion.jsonAssertion,
        { id: record.id, groupId: record.groupId },
        {
          ...record,
          id: new Date().getTime(),
          groupId: parent ? parent.id : record.groupId,
          matchValue: '',
          matchCondition: '',
        }
      );
      if (parent) {
        parent.rowSpan = parent.rowSpan ? parent.rowSpan + 1 : 2;
      } else {
        // 找到第一个子节点
        const firstChildNode = findFirstByGroupId(condition.value.documentAssertion.jsonAssertion, record.groupId);
        if (firstChildNode) {
          firstChildNode.rowSpan = firstChildNode.rowSpan
            ? firstChildNode.rowSpan + 1
            : countNodesByGroupId(condition.value.documentAssertion.jsonAssertion, record.groupId) + 1;
        }
      }
    } else {
      record.rowSpan = record.rowSpan ? record.rowSpan + 1 : 2;
      // 父级点击
      insertNode(
        condition.value.documentAssertion.jsonAssertion,
        { id: record.id, groupId: record.groupId },
        {
          ...record,
          id: new Date().getTime(),
          groupId: record.id,
          matchValue: '',
          matchCondition: '',
        }
      );
    }
  };
  const showDeleteSingle = computed(() => {
    return countNodes(condition.value.documentAssertion.jsonAssertion) > 1;
  });
  const deleteSingleParam = (record: Record<string, any>) => {
    deleteNodeById(condition.value.documentAssertion.jsonAssertion, record.id);
  };
  const deleteAllParam = (record: Record<string, any>) => {
    if (record.groupId) {
      // 验证子项,根据groupId删除
      deleteNodesByGroupId(condition.value.documentAssertion.jsonAssertion, record.groupId);
    } else if (record.rowspan > 2) {
      // 验证主体, 根据主体的id 作为groupId删除
      deleteNodesByGroupId(condition.value.documentAssertion.jsonAssertion, record.id);
      // 删除本体
      deleteNodeById(condition.value.documentAssertion.jsonAssertion, record.id);
    } else {
      // 删除本体
      deleteNodeById(condition.value.documentAssertion.jsonAssertion, record.id);
    }
  };

  const documentSpanMethod = (data: {
    record: TableData;
    column: TableColumnData | TableOperationColumn;
    rowIndex: number;
    columnIndex: number;
  }): { rowspan?: number; colspan?: number } | void => {
    // groupId 是后端传过来的id，然后根据这个id去找到对应的子项
    // 前端根据groupId 去过滤出rowspan的数量,然后返回
    const { record, column } = data;
    const currentColumn = column as TableColumnData;
    if (record.rowSpan > 1) {
      const mergeColumns = ['key', 'mustContain', 'typeChecking', 'paramType', 'operation'];
      if (mergeColumns.includes(currentColumn.title as string)) {
        return {
          rowspan: record.rowSpan,
        };
      }
    }
  };
</script>
