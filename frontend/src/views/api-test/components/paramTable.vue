<template>
  <MsBaseTable v-bind="propsRes" :hoverable="false" no-disable v-on="propsEvent">
    <!-- 表格头 slot -->
    <template #encodeTitle>
      <div class="flex items-center text-[var(--color-text-3)]">
        {{ t('apiTestDebug.encode') }}
        <a-tooltip position="right">
          <icon-question-circle
            class="ml-[4px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
            size="16"
          />
          <template #content>
            <div>{{ t('apiTestDebug.encodeTip1') }}</div>
            <div>{{ t('apiTestDebug.encodeTip2') }}</div>
          </template>
        </a-tooltip>
      </div>
    </template>
    <template #typeTitle="{ columnConfig }">
      <div class="flex items-center text-[var(--color-text-3)]">
        {{ t('apiTestDebug.paramType') }}
        <a-tooltip :content="columnConfig.typeTitleTooltip" position="right">
          <icon-question-circle
            class="ml-[4px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
            size="16"
          />
        </a-tooltip>
      </div>
    </template>
    <!-- 表格列 slot -->
    <template #name="{ record, columnConfig }">
      <a-popover position="tl" :disabled="!record.name || record.name.trim() === ''" class="ms-params-input-popover">
        <template #content>
          <div class="param-popover-title">
            {{ t('apiTestDebug.paramName') }}
          </div>
          <div class="param-popover-value">
            {{ record[columnConfig.dataIndex as string] }}
          </div>
        </template>
        <a-input
          v-model:model-value="record[columnConfig.dataIndex as string]"
          :placeholder="t('apiTestDebug.paramNamePlaceholder')"
          class="param-input"
          :max-length="255"
          @input="(val) => addTableLine(val, 'name')"
        />
      </a-popover>
    </template>
    <template #type="{ record, columnConfig }">
      <a-tooltip
        v-if="columnConfig.hasRequired"
        :content="t(record.required ? 'apiTestDebug.paramRequired' : 'apiTestDebug.paramNotRequired')"
      >
        <MsButton
          type="icon"
          :class="[
            record.required ? '!text-[rgb(var(--danger-5))]' : '!text-[var(--color-text-brand)]',
            '!mr-[4px] !p-[4px]',
          ]"
          @click="record.required = !record.required"
        >
          <div>*</div>
        </MsButton>
      </a-tooltip>
      <a-select
        v-model:model-value="record.type"
        :options="columnConfig.typeOptions || []"
        class="param-input w-full"
        @change="(val) => handleTypeChange(val, record)"
      />
    </template>
    <template #expressionType="{ record, columnConfig }">
      <a-select
        v-model:model-value="record.expressionType"
        :options="columnConfig.typeOptions || []"
        class="param-input w-[110px]"
        @change="(val) => handleExpressionTypeChange(val)"
      />
    </template>
    <template #range="{ record, columnConfig }">
      <a-select
        v-model:model-value="record.range"
        :options="columnConfig.typeOptions || []"
        class="param-input w-[180px]"
        @change="(val) => handleRangeChange(val)"
      />
    </template>
    <template #expression="{ record, rowIndex, columnConfig }">
      <slot name="expression" :record="record" :row-index="rowIndex" :column-config="columnConfig"></slot>
    </template>
    <template #value="{ record, columnConfig }">
      <a-popover
        v-if="columnConfig.isNormal"
        position="tl"
        :disabled="!record.value || record.value.trim() === ''"
        class="ms-params-input-popover"
      >
        <template #content>
          <div class="param-popover-title">
            {{ t('apiTestDebug.paramValue') }}
          </div>
          <div class="param-popover-value">
            {{ record.value }}
          </div>
        </template>
        <a-input
          v-model:model-value="record.value"
          class="param-input"
          :placeholder="t('apiTestDebug.commonPlaceholder')"
          :max-length="255"
          @input="(val) => addTableLine(val, 'value')"
        />
      </a-popover>
      <MsParamsInput
        v-else
        v-model:value="record.value"
        @change="(val) => addTableLine(val, 'value')"
        @dblclick="quickInputParams(record)"
        @apply="handleParamSettingApply"
      />
    </template>
    <template #lengthRange="{ record }">
      <div class="flex items-center justify-between">
        <a-input-number
          v-model:model-value="record.min"
          :placeholder="t('apiTestDebug.paramMin')"
          class="param-input param-input-number"
          @input="(val) => addTableLine(val || '', 'min')"
        />
        <div class="mx-[4px]">～</div>
        <a-input-number
          v-model:model-value="record.max"
          :placeholder="t('apiTestDebug.paramMax')"
          class="param-input"
          @input="(val) => addTableLine(val || '', 'max')"
        />
      </div>
    </template>
    <template #tag="{ record, columnConfig }">
      <a-popover
        position="tl"
        :disabled="record[columnConfig.dataIndex as string].length === 0"
        class="ms-params-input-popover"
      >
        <template #content>
          <div class="param-popover-title">
            {{ t('common.tag') }}
          </div>
          <div class="param-popover-value">
            <MsTagsGroup is-string-tag :tag-list="record[columnConfig.dataIndex as string]" />
          </div>
        </template>
        <MsTagsInput
          v-model:model-value="record[columnConfig.dataIndex as string]"
          :max-tag-count="1"
          class="param-input"
          @change="(val) => addTableLine(val, 'tag')"
        />
      </a-popover>
    </template>
    <template #desc="{ record, columnConfig }">
      <paramDescInput
        v-model:desc="record[columnConfig.dataIndex as string]"
        @input="(val) => addTableLine(val, 'desc')"
        @dblclick="quickInputDesc(record)"
        @change="handleDescChange"
      />
    </template>
    <template #encode="{ record }">
      <a-switch
        v-model:model-value="record.encode"
        size="small"
        class="param-input-switch"
        type="line"
        @change="(val) => addTableLine(val.toString(), 'encode')"
      />
    </template>
    <template #mustContain="{ record, columnConfig }">
      <a-checkbox v-model:model-value="record[columnConfig.dataIndex as string]" @change="(val) => addTableLine(val)" />
    </template>
    <template #operation="{ record, rowIndex, columnConfig }">
      <slot name="operationPre" :record="record" :row-index="rowIndex" :column-config="columnConfig"></slot>
      <MsTableMoreAction
        v-if="columnConfig.moreAction"
        :list="getMoreActionList(columnConfig.moreAction, record)"
        @select="(e) => handleMoreActionSelect(e, record)"
      />
      <a-trigger
        v-if="columnConfig.format && columnConfig.format !== RequestBodyFormat.X_WWW_FORM_URLENCODED"
        trigger="click"
        position="br"
      >
        <MsButton type="icon" class="mr-[8px]"><icon-more /></MsButton>
        <template #content>
          <div class="content-type-trigger-content">
            <div class="mb-[8px] text-[var(--color-text-1)]">Content-Type</div>
            <a-select
              v-model:model-value="record.contentType"
              :options="Object.values(RequestContentTypeEnum).map((e) => ({ label: e, value: e }))"
              allow-create
              @change="(val) => addTableLine(val as string, 'contentType')"
            />
          </div>
        </template>
      </a-trigger>
      <a-switch
        v-if="columnConfig.hasEnable"
        v-model:model-value="record.enable"
        size="small"
        type="line"
        @change="(val) => addTableLine(val, 'enable')"
      />
      <icon-minus-circle
        v-if="paramsLength > 1 && rowIndex !== paramsLength - 1"
        class="cursor-pointer text-[var(--color-text-4)]"
        size="20"
        @click="deleteParam(rowIndex)"
      />
    </template>
    <template #responseHeader="{ record, columnConfig }">
      <a-select v-model="record.responseHeader" class="param-input" @change="(val) => addTableLine(val as string)">
        <a-option v-for="item in columnConfig.options" :key="item.value">{{ t(item.label) }}</a-option>
      </a-select>
    </template>
    <template #matchCondition="{ record, columnConfig }">
      <a-select v-model="record.condition" class="param-input" @change="(val) => addTableLine(val as string)">
        <a-option v-for="item in columnConfig.options" :key="item.value">{{ t(item.label) }}</a-option>
      </a-select>
    </template>
    <template #matchValue="{ record }">
      <a-input v-model="record.matchValue" class="param-input" @change="(val) => addTableLine(val)" />
    </template>
    <template #project="{ record, columnConfig }">
      <a-select
        v-model="record.projectId"
        class="param-input"
        @change="(val) => handelProjectChange(val as string, record.projectId)"
      >
        <a-option v-for="item in columnConfig.options" :key="item.id">{{ item.name }}</a-option>
      </a-select>
    </template>
    <template #environment="{ record, columnConfig }">
      <a-select v-model="record.environmentId" class="param-input">
        <a-option v-for="item in columnConfig.options" :key="item.id">{{ item.name }}</a-option>
      </a-select>
    </template>
  </MsBaseTable>
  <a-modal
    v-model:visible="showQuickInputParam"
    :title="t('ms.paramsInput.value')"
    :ok-text="t('apiTestDebug.apply')"
    :ok-button-props="{ disabled: !quickInputParamValue || quickInputParamValue.trim() === '' }"
    class="ms-modal-form"
    body-class="!p-0"
    :width="680"
    title-align="start"
    @ok="applyQuickInputParam"
    @close="clearQuickInputParam"
  >
    <MsCodeEditor
      v-if="showQuickInputParam"
      v-model:model-value="quickInputParamValue"
      theme="MS-text"
      height="300px"
      :show-full-screen="false"
    >
      <template #title>
        <div class="flex justify-between">
          <div class="text-[var(--color-text-1)]">
            {{ t('apiTestDebug.quickInputParamsTip') }}
          </div>
        </div>
      </template>
    </MsCodeEditor>
  </a-modal>
  <a-modal
    v-model:visible="showQuickInputDesc"
    :title="t('apiTestDebug.desc')"
    :ok-text="t('common.save')"
    :ok-button-props="{ disabled: !quickInputDescValue || quickInputDescValue.trim() === '' }"
    class="ms-modal-form"
    body-class="!p-0"
    :width="480"
    title-align="start"
    :auto-size="{ minRows: 2 }"
    @ok="applyQuickInputDesc"
    @close="clearQuickInputDesc"
  >
    <a-textarea
      v-model:model-value="quickInputDescValue"
      :placeholder="t('apiTestDebug.descPlaceholder')"
      :max-length="1000"
    ></a-textarea>
  </a-modal>
</template>

<script async setup lang="ts">
  import { isEqual } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumnData } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTagsGroup from '@/components/pure/ms-tag/ms-tag-group.vue';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import MsParamsInput from '@/components/business/ms-params-input/index.vue';
  import paramDescInput from './paramDescInput.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useTableStore from '@/hooks/useTableStore';

  import { RequestBodyFormat, RequestContentTypeEnum } from '@/enums/apiEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  interface Param {
    id: number;
    required: boolean;
    name: string;
    type: string;
    value: string;
    min: number | undefined;
    max: number | undefined;
    contentType: RequestContentTypeEnum;
    desc: string;
    encode: boolean;
    tag: string[];
    enable: boolean;
    mustContain: boolean;
    [key: string]: any;
  }

  export type ParamTableColumn = MsTableColumnData & {
    isNormal?: boolean; // 用于 value 列区分是普通输入框还是 MsParamsInput
    hasRequired?: boolean; // 用于 type 列区分是否有 required 星号
    typeOptions?: { label: string; value: string }[]; // 用于 type 列选择器选项
    typeTitleTooltip?: string; // 用于 type 表头列展示的 tooltip
    hasEnable?: boolean; // 用于 operation 列区分是否有 enable 开关
    moreAction?: ActionsItem[]; // 用于 operation 列更多操作按钮配置
    format?: RequestBodyFormat | 'query' | 'rest'; // 用于 operation 列区分是否有请求体格式选择器
  };

  const props = withDefaults(
    defineProps<{
      params: any[];
      defaultParamItem?: Partial<Param>; // 默认参数项，用于添加新行时的默认值
      columns: ParamTableColumn[];
      scroll?: {
        x?: number | string;
        y?: number | string;
        maxHeight?: number | string;
        minWidth?: number | string;
      };
      heightUsed?: number;
      draggable?: boolean;
      selectable?: boolean;
      showSetting?: boolean; // 是否显示列设置
      tableKey?: TableKeyEnum; // 表格key showSetting为true时必传
      disabled?: boolean; // 是否禁用
      showSelectorAll?: boolean; // 是否显示全选
      isSimpleSetting?: boolean; // 是否简单Column设置
      response?: string; // 响应内容
    }>(),
    {
      selectable: true,
      showSetting: false,
      tableKey: undefined,
      isSimpleSetting: true,
      defaultParamItem: () => ({
        required: false,
        name: '',
        type: 'string',
        value: '',
        min: undefined,
        max: undefined,
        contentType: RequestContentTypeEnum.TEXT,
        tag: [],
        desc: '',
        encode: false,
        enable: false,
        mustContain: false,
      }),
    }
  );
  const emit = defineEmits<{
    (e: 'change', data: any[], isInit?: boolean): void; // 都触发这个事件以通知父组件参数数组被更改
    (e: 'moreActionSelect', event: ActionsItem, record: Record<string, any>): void;
    (e: 'projectChange', projectId: string): void;
  }>();

  const { t } = useI18n();

  const tableStore = useTableStore();
  async function initColumns() {
    if (props.showSetting && props.tableKey) {
      await tableStore.initColumn(props.tableKey, props.columns);
    }
  }
  initColumns();

  const { propsRes, propsEvent } = useTable(() => Promise.resolve([]), {
    tableKey: props.showSetting ? props.tableKey : undefined,
    scroll: props.scroll,
    heightUsed: props.heightUsed,
    columns: props.columns,
    selectable: props.selectable,
    draggable: props.draggable ? { type: 'handle', width: 24 } : undefined,
    showSetting: props.showSetting,
    disabled: props.disabled,
    showSelectorAll: props.showSelectorAll,
    isSimpleSetting: props.isSimpleSetting,
  });

  watch(
    () => props.heightUsed,
    (val) => {
      propsRes.value.heightUsed = val;
    }
  );

  const paramsLength = computed(() => propsRes.value.data.length);

  function deleteParam(rowIndex: number) {
    propsRes.value.data.splice(rowIndex, 1);
    emit('change', propsRes.value.data);
  }

  /**
   * 当表格输入框变化时，给参数表格添加一行数据行
   * @param val 输入值
   * @param key 当前列的 key
   * @param isForce 是否强制添加
   */
  function addTableLine(
    val?: string | number | boolean | (string | number | boolean)[],
    key?: string,
    isForce?: boolean
  ) {
    const lastData = { ...propsRes.value.data[propsRes.value.data.length - 1] };
    delete lastData.id;
    // 当不传入输入值或对应列的 key 时，遍历整个数据对象判断是否有变化；当传入输入值或对应列的 key 时，判断对应列的值是否有变化
    const isNotChange =
      val === undefined || key === undefined
        ? isEqual(lastData, props.defaultParamItem)
        : isEqual(lastData[key], props.defaultParamItem[key]);
    if (isForce || (val !== '' && !isNotChange)) {
      propsRes.value.data.push({
        id: new Date().getTime(),
        ...props.defaultParamItem,
      } as any);
      emit('change', propsRes.value.data);
    }
  }

  watch(
    () => props.params,
    (val) => {
      if (val.length > 0) {
        const lastData = { ...val[val.length - 1] };
        delete lastData.id; // 删除 id 属性，避免影响判断是否有变化
        const isNotChange = isEqual(lastData, props.defaultParamItem);
        propsRes.value.data = val;
        if (!isNotChange) {
          addTableLine();
        }
      } else {
        propsRes.value.data = [
          {
            id: new Date().getTime(), // 默认给时间戳 id，若 props.defaultParamItem 有 id，则覆盖
            ...props.defaultParamItem,
          },
        ] as any[];
        emit('change', propsRes.value.data, true);
      }
    },
    {
      immediate: true,
    }
  );

  const showQuickInputParam = ref(false);
  const activeQuickInputRecord = ref<any>({});
  const quickInputParamValue = ref('');

  function quickInputParams(record: any) {
    activeQuickInputRecord.value = record;
    showQuickInputParam.value = true;
    quickInputParamValue.value = record.value;
  }

  function clearQuickInputParam() {
    activeQuickInputRecord.value = {};
    quickInputParamValue.value = '';
  }

  function applyQuickInputParam() {
    activeQuickInputRecord.value.value = quickInputParamValue.value;
    showQuickInputParam.value = false;
    clearQuickInputParam();
    addTableLine(quickInputParamValue.value, 'value', true);
    emit('change', propsRes.value.data);
  }

  function handleParamSettingApply(val: string | number) {
    addTableLine(val, 'value');
  }

  const showQuickInputDesc = ref(false);
  const quickInputDescValue = ref('');

  function quickInputDesc(record: any) {
    activeQuickInputRecord.value = record;
    showQuickInputDesc.value = true;
    quickInputDescValue.value = record.desc;
  }

  function clearQuickInputDesc() {
    activeQuickInputRecord.value = {};
    quickInputDescValue.value = '';
  }

  function applyQuickInputDesc() {
    activeQuickInputRecord.value.desc = quickInputDescValue.value;
    showQuickInputDesc.value = false;
    clearQuickInputDesc();
    addTableLine(quickInputDescValue.value, 'desc', true);
    emit('change', propsRes.value.data);
  }

  function handleDescChange() {
    emit('change', propsRes.value.data);
  }

  function handleTypeChange(
    val: string | number | boolean | Record<string, any> | (string | number | boolean | Record<string, any>)[],
    record: Partial<Param>
  ) {
    addTableLine(val as string, 'type');
    // 根据参数类型自动推断 Content-Type 类型
    if (record.contentType) {
      if (val === 'file') {
        record.contentType = RequestContentTypeEnum.OCTET_STREAM;
      } else if (val === 'json') {
        record.contentType = RequestContentTypeEnum.JSON;
      } else {
        record.contentType = RequestContentTypeEnum.TEXT;
      }
    }
  }

  function handleExpressionTypeChange(
    val: string | number | boolean | Record<string, any> | (string | number | boolean | Record<string, any>)[]
  ) {
    addTableLine(val as string, 'expressionType');
  }

  function handleRangeChange(
    val: string | number | boolean | Record<string, any> | (string | number | boolean | Record<string, any>)[]
  ) {
    addTableLine(val as string, 'range');
  }

  /**
   * 获取更多操作按钮列表
   * @param actions 按钮列表
   * @param record 当前行数据
   */
  function getMoreActionList(actions: ActionsItem[], record: Record<string, any>) {
    if (props.columns.findIndex((e) => e.dataIndex === 'expression') !== -1) {
      // 如果有expression列，就需要根据expression的值来判断按钮列表是否禁用
      if (record.expression === '' || record.expression === undefined || record.expression === null) {
        return actions.map((e) => ({ ...e, disabled: true }));
      }
      return actions;
    }
    return actions;
  }

  function handleMoreActionSelect(event: ActionsItem, record: Record<string, any>) {
    emit('moreActionSelect', event, record);
  }

  function handelProjectChange(val: string, projectId: string) {
    emit('projectChange', projectId);
    addTableLine(val as string, 'projectId');
  }

  defineExpose({
    addTableLine,
  });
</script>

<style lang="less" scoped>
  :deep(.arco-table-th) {
    background-color: var(--color-text-n9);
  }
  :deep(.arco-table-cell-align-left) {
    padding: 16px 2px;
  }
  :deep(.arco-table-td) {
    .arco-table-cell {
      padding: 12px 2px;
    }
  }
  :deep(.param-input:not(.arco-input-focus, .arco-select-view-focus)) {
    &:not(:hover) {
      border-color: transparent !important;
      .arco-input::placeholder {
        @apply invisible;
      }
      .arco-select-view-icon {
        @apply invisible;
      }
      .arco-select-view-value {
        color: var(--color-text-brand);
      }
    }
  }
  :deep(.param-input-number) {
    @apply pr-0;
    .arco-input {
      @apply text-right;
    }
    .arco-input-suffix {
      @apply hidden;
    }
    &:hover,
    &.arco-input-focus {
      .arco-input {
        @apply text-left;
      }
      .arco-input-suffix {
        @apply inline-flex;
      }
    }
  }
  .content-type-trigger-content {
    @apply bg-white;

    padding: 8px;
    border-radius: var(--border-radius-small);
    box-shadow: 0 4px 10px -1px rgb(100 100 102 / 15%);
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
