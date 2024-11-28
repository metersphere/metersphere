<template>
  <a-form ref="formRef" :model="propsRes" :disabled="props.disabled" layout="vertical">
    <MsBaseTable
      v-bind="propsRes"
      v-model:original-selected-keys="originalSelectedKeys"
      v-model:expanded-keys="expandedKeys"
      :hoverable="false"
      is-simple-setting
      :span-method="props.spanMethod"
      :class="[
        !props.selectable && !props.draggable ? 'ms-form-table-no-left-action' : '',
        props.disabled ? 'ms-form-table--disabled' : '',
      ]"
      bordered
      :row-class="rowClass"
      :filter="filterData"
      v-on="propsEvent"
      @drag-change="tableChange"
      @init-end="validateAndUpdateErrorMessageList"
      @select="handleSelect"
      @select-all="handleSelectAll"
    >
      <!-- 展开行-->
      <template #expand-icon="{ expanded, record }">
        <a-tooltip
          :content="
            t(
              record.children.length === 0
                ? 'msFormTable.noChildren'
                : expanded
                ? 'msFormTable.collapse'
                : 'msFormTable.expand'
            )
          "
        >
          <div
            class="flex items-end gap-[2px] text-[var(--color-text-4)]"
            :class="[
              expanded ? '!text-[rgb(var(--primary-5))]' : '',
              record.children.length === 0 ? 'cursor-not-allowed' : '',
            ]"
          >
            <MsIcon type="icon-icon_split_turn-down_arrow" />
            <div v-if="record.children" class="break-keep">{{ record.children.length }}</div>
          </div>
        </a-tooltip>
      </template>
      <template #columnFilter="{ column }">
        <DefaultFilter
          v-if="(column.filterConfig && column.filterConfig.options?.length) || column?.filterConfig?.remoteMethod"
          v-model:checked-list="column.filterCheckedList"
          class="ml-[4px]"
          :options="column.filterConfig.options"
          :data-index="column.dataIndex"
          v-bind="column.filterConfig"
          :filter="filterData"
          @handle-confirm="(v) => handleFilterConfirm(v, column.dataIndex as string, column.isCustomParam || false)"
          @click.stop="null"
        >
          <template #item="{ filterItem }">
            <slot :name="column.filterConfig.filterSlotName" :filter-content="filterItem"> </slot>
          </template>
        </DefaultFilter>
      </template>
      <template
        v-for="item of props.columns.filter((e) => e.slotName !== undefined)"
        :key="item.toString()"
        #[item.slotName!]="{ record, rowIndex, column }"
      >
        <a-form-item
          :field="`data[${rowIndex}].${item.dataIndex}`"
          label=""
          :rules="{
            validator: (value, callback) => {
              validRepeat(rowIndex, item.dataIndex as string, record[item.dataIndex as string], record, callback);
            },
          }"
          :disabled="props.disabled || item.disabled || record.disabled"
        >
          <slot
            :name="item.slotName"
            v-bind="{ record, rowIndex, column, dataIndex: item.dataIndex, columnConfig: item }"
          >
            <a-tooltip
              v-if="item.hasRequired"
              :content="t(record.required ? 'msFormTable.paramRequired' : 'msFormTable.paramNotRequired')"
            >
              <div
                class="ms-form-table-required-button"
                :class="[
                  record.required ? 'ms-form-table-required-button--required' : '',
                  props.disabled ? 'ms-form-table-required-button--disabled' : '',
                ]"
                @click="toggleRequired(record, rowIndex, column)"
              >
                <article>*</article>
              </div>
            </a-tooltip>
            <div v-if="item.isNull && item.isNull(record)" class="ms-form-table-td-text">-</div>
            <a-input
              v-else-if="item.inputType === 'input'"
              v-model:model-value="record[item.dataIndex as string]"
              :placeholder="t(item.locale || 'common.pleaseInput')"
              class="ms-form-table-input ms-form-table-input--hasPlaceholder"
              :max-length="255"
              :size="item.size || 'medium'"
              @input="() => handleFormChange(record, rowIndex, item)"
            />
            <a-select
              v-else-if="item.inputType === 'select'"
              v-model:model-value="record[item.dataIndex as string]"
              :options="item.options || []"
              class="ms-form-table-input w-full"
              :size="item.size || 'medium'"
              @change="() => handleFormChange(record, rowIndex, item)"
            />
            <MsTagsInput
              v-else-if="item.inputType === 'tags'"
              v-model:model-value="record[item.dataIndex as string]"
              :max-tag-count="1"
              input-class="ms-form-table-input"
              :size="item.size || 'medium'"
              @change="() => handleFormChange(record, rowIndex, item)"
              @clear="() => handleFormChange(record, rowIndex, item)"
            />
            <a-switch
              v-else-if="item.inputType === 'switch'"
              v-model:model-value="record[item.dataIndex as string]"
              :size="(item.size as any) || 'medium'"
              class="ms-form-table-input-switch"
              type="line"
              @change="() => handleFormChange(record, rowIndex, item)"
            />
            <a-checkbox
              v-else-if="item.inputType === 'checkbox'"
              v-model:model-value="record[item.dataIndex as string]"
              :size="item.size || 'medium'"
              @change="() => handleFormChange(record, rowIndex, item)"
            />
            <template v-else-if="item.inputType === 'autoComplete'">
              <a-auto-complete
                v-model:model-value="record[item.dataIndex as string]"
                :data="item.autoCompleteParams?.filter((e) => e.isShow === true)"
                class="ms-form-table-input"
                :trigger-props="{ contentClass: 'ms-form-table-input-trigger' }"
                :filter-option="false"
                :size="item.size || 'medium'"
                @search="(val) => handleSearchParams(val, item)"
                @change="() => handleFormChange(record, rowIndex, item)"
                @select="(val) => selectAutoComplete(val, record, item)"
              >
                <template #option="{ data: opt }">
                  <div class="w-[350px]">
                    {{ opt.raw.value }}
                    <a-tooltip :content="t(opt.raw.desc)" position="bl" :mouse-enter-delay="300">
                      <div class="one-line-text max-w-full text-[12px] leading-[16px] text-[var(--color-text-4)]">
                        {{ t(opt.raw.desc) }}
                      </div>
                    </a-tooltip>
                  </div>
                </template>
              </a-auto-complete>
            </template>
            <a-input-number
              v-else-if="item.inputType === 'inputNumber'"
              v-model:model-value="record[item.dataIndex as string]"
              class="ms-form-table-input-number"
              :min="item.min"
              :max="item.max"
              :size="item.size || 'medium'"
              :precision="item.precision"
              @change="() => handleFormChange(record, rowIndex, item)"
            ></a-input-number>
            <a-textarea
              v-else-if="item.inputType === 'textarea'"
              v-model:model-value="record[item.dataIndex as string]"
              class="ms-form-table-input"
              :auto-size="{ minRows: 1, maxRows: 1 }"
              :size="item.size || 'medium'"
              :max-length="item.maxLength"
              @input="() => handleFormChange(record, rowIndex, item)"
            ></a-textarea>
            <MsQuickInput
              v-else-if="item.inputType === 'quickInput'"
              v-model:model-value="record[item.dataIndex as string]"
              :title="item.title as string || ''"
              :disabled="props.disabled || item.disabled || record.disabled"
              :max-length="item.maxLength"
              class="ms-form-table-input"
              type="textarea"
              @input="() => handleFormChange(record, rowIndex, item)"
              @change="() => handleFormChange(record, rowIndex, item)"
            >
            </MsQuickInput>
            <div v-else-if="item.inputType === 'text'" class="one-line-text pl-[8px]">
              {{
                typeof item.valueFormat === 'function'
                  ? item.valueFormat(record)
                  : record[item.dataIndex as string] || '-'
              }}
            </div>
            <template v-else-if="item.dataIndex === 'action'">
              <div
                :key="item.dataIndex"
                class="flex flex-row items-center"
                :class="{ 'justify-end': item.align === 'right' }"
              >
                <slot
                  name="operationPre"
                  v-bind="{ record, rowIndex, column, dataIndex: item.dataIndex, columnConfig: item }"
                ></slot>
                <MsTableMoreAction
                  v-if="item.moreAction"
                  :list="getMoreActionList(item.moreAction, record)"
                  @select="(e) => handleMoreActionSelect(e, record, item, rowIndex)"
                />
                <slot
                  name="operationDelete"
                  v-bind="{ record, rowIndex, column, dataIndex: item.dataIndex, columnConfig: item }"
                >
                  <icon-minus-circle
                    v-if="dataLength > 1 && rowIndex !== dataLength - 1"
                    class="ml-[8px] cursor-pointer text-[var(--color-text-4)]"
                    size="20"
                    @click="deleteParam(record, rowIndex)"
                  />
                </slot>
              </div>
            </template>
          </slot>
        </a-form-item>
      </template>
      <template
        v-for="item of props.columns.filter((e) => e.titleSlotName !== undefined)"
        #[item.titleSlotName!]="{ record, rowIndex, column }"
      >
        <slot
          :name="item.titleSlotName"
          v-bind="{ record, rowIndex, column, dataIndex: item.dataIndex, columnConfig: item }"
        >
        </slot>
      </template>
    </MsBaseTable>
  </a-form>
</template>

<script setup lang="ts">
  import { FormInstance } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import DefaultFilter from '@/components/pure/ms-table/comp/defaultFilter.vue';
  import type { MsTableColumnData, MsTableProps } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import MsQuickInput from '@/components/business/ms-quick-input/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useTableStore from '@/hooks/useTableStore';

  import { SelectAllEnum, TableKeyEnum } from '@/enums/tableEnum';

  import { ActionsItem } from '../ms-table-more-action/types';
  import type { SelectOptionData, TableColumnData, TableData, TableRowSelection } from '@arco-design/web-vue';
  import { TableOperationColumn } from '@arco-design/web-vue/es/table/interface';

  export interface FormTableColumn extends MsTableColumnData {
    enable?: boolean; // 是否启用
    required?: boolean; // 是否必填
    inputType?:
      | 'input'
      | 'select'
      | 'tags'
      | 'switch'
      | 'text'
      | 'checkbox'
      | 'autoComplete'
      | 'textarea'
      | 'inputNumber'
      | 'quickInput'; // 输入组件类型
    autoCompleteParams?: SelectOptionData[]; // 自动补全参数
    needValidRepeat?: boolean; // 是否需要判断重复
    maxLength?: number;
    disabled?: boolean; // 是否禁用
    size?: 'mini' | 'small' | 'medium' | 'large'; // 输入框大小
    // 数字输入框属性
    min?: number;
    max?: number;
    step?: number;
    precision?: number;
    valueFormat?: (record: Record<string, any>) => string; // 展示值格式化，仅在inputType为text时生效
    isNull?: (record: Record<string, any>) => boolean; // 需要判断是否为空，为空展示‘-’，不展示表单或文本
    [key: string]: any; // 扩展属性
  }

  const props = withDefaults(
    defineProps<{
      data?: Record<string, any>[];
      columns: FormTableColumn[];
      defaultParamDataItem?: Record<string, any>;
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
      rowSelection?: TableRowSelection;
      diffMode?: 'add' | 'delete';
      spanMethod?: (data: {
        record: TableData;
        column: TableColumnData | TableOperationColumn;
        rowIndex: number;
        columnIndex: number;
      }) => { rowspan?: number; colspan?: number } | void;
    }>(),
    {
      data: () => [],
      selectable: true,
      showSetting: false,
      tableKey: undefined,
    }
  );
  const emit = defineEmits<{
    (e: 'change', data: Record<string, any>[]): void; // 都触发这个事件以通知父组件表格数据被更改
    (e: 'formChange', record: Record<string, any>, columnConfig: FormTableColumn, rowIndex: number): void; // 输入项变化
    (
      e: 'moreActionSelect',
      event: ActionsItem,
      record: Record<string, any>,
      columnConfig: FormTableColumn,
      rowIndex: number
    ): void;
    (e: 'rowSelect', rowKeys: (string | number)[], _rowKey: string | number, record: TableData): void;
    (e: 'selectAll', checked: boolean): void;
    (
      e: 'filterChange',
      dataIndex: string,
      value: string[] | (string | number | boolean)[] | undefined,
      isCustomParam: boolean
    ): void;
    (e: 'sorterChange', value: { [key: string]: string }): void;
  }>();

  const { t } = useI18n();
  const tableStore = useTableStore();

  const expandedKeys = defineModel<string[]>('expandedKeys', { default: [] });
  const originalSelectedKeys = defineModel<(string | number)[]>('originalSelectedKeys', { default: [] });

  const filterData = ref<Record<string, any>>({});

  async function initColumns() {
    if (props.showSetting && props.tableKey) {
      await tableStore.initColumn(props.tableKey, props.columns);
    }
  }

  const tableProps = ref<Partial<MsTableProps<Record<string, any>>>>({
    firstColumnWidth: 32,
    tableKey: props.showSetting ? props.tableKey : undefined,
    scroll: props.scroll,
    heightUsed: props.heightUsed,
    columns: props.columns,
    selectable: props.selectable,
    draggable: props.draggable ? { type: 'handle', width: 24 } : undefined,
    showSetting: props.showSetting,
    disabled: props.disabled,
    showSelectorAll: props.showSelectorAll,
    showPagination: false,
    rowSelection: props.selectable ? undefined : props.rowSelection,
  });

  const { propsRes, propsEvent } = useTable(() => Promise.resolve([]), tableProps.value);
  const selectedKeys = computed(() => propsRes.value.data.filter((e) => e.enable).map((e) => e.id));
  propsEvent.value.rowSelectChange = (record: Record<string, any>) => {
    propsRes.value.data = propsRes.value.data.map((e) => {
      if (e.id === record.id) {
        e.enable = !e.enable;
      }
      return e;
    });
    emit('change', propsRes.value.data);
  };
  propsEvent.value.selectAllChange = (v: SelectAllEnum) => {
    propsRes.value.data = propsRes.value.data.map((e) => {
      e.enable = v !== SelectAllEnum.NONE;
      return e;
    });
    emit('change', propsRes.value.data);
  };
  propsEvent.value.sorterChange = (value: { [key: string]: string }) => {
    emit('sorterChange', value);
  };

  const dataLength = computed(() => propsRes.value.data.length);

  const handleFilterConfirm = (
    value: string[] | (string | number | boolean)[] | undefined,
    dataIndex: string,
    isCustomParam: boolean
  ) => {
    filterData.value[dataIndex] = value;
    emit('filterChange', dataIndex, value, isCustomParam);
  };

  // 校验重复
  const formRef = ref<FormInstance>();
  async function validRepeat(
    rowIndex: number,
    dataIndex: string,
    value: any,
    record: Record<string, any>,
    callback: (error?: string) => void
  ) {
    const currentColumn = props.columns.find((item) => item.dataIndex === dataIndex);
    if (!currentColumn?.needValidRepeat) {
      callback();
      return;
    }
    if (record.parent) {
      (record.parent.children as Record<string, any>[])?.forEach((row, index) => {
        if (row[dataIndex].length && index !== rowIndex && row[dataIndex] === value) {
          callback(`${t(currentColumn?.title as string)}${t('msFormTable.paramRepeatMessage')}`);
        }
      });
    } else {
      propsRes.value.data?.forEach((row, index) => {
        if (row[dataIndex].length && index !== rowIndex && row[dataIndex] === value) {
          callback(`${t(currentColumn?.title as string)}${t('msFormTable.paramRepeatMessage')}`);
        }
      });
    }
    callback();
  }

  // 校验并更新错误信息列表
  const setErrorMessageList: ((params: string[]) => void) | undefined = inject('setErrorMessageList', undefined);
  const errorMessageList = ref<string[]>([]); // 错误信息列表
  async function validateAndUpdateErrorMessageList() {
    if (typeof setErrorMessageList === 'function' && props.columns.some((item) => item.needValidRepeat)) {
      await nextTick();
      formRef.value?.validate((errors) => {
        errorMessageList.value = !errors ? [] : [...new Set(Object.values(errors).map(({ message }) => message))];
        setErrorMessageList(errorMessageList.value);
      });
    }
  }

  watch(
    () => selectedKeys.value,
    (arr) => {
      propsRes.value.selectedKeys = new Set(arr);
    }
  );

  watch(
    () => props.heightUsed,
    (val) => {
      propsRes.value.heightUsed = val;
    }
  );

  watch(
    () => props.data,
    (arr) => {
      propsRes.value.data = arr as any[];
      validateAndUpdateErrorMessageList();
    },
    {
      immediate: true,
    }
  );

  function tableChange() {
    emit('change', propsRes.value.data);
  }

  function emitChange(from: string, isInit?: boolean) {
    if (!isInit) {
      emit('change', propsRes.value.data);
    }
  }

  /**
   * 当表格输入框变化时，给参数表格添加一行数据行
   * @param val 输入值
   * @param key 当前列的 key
   * @param isForce 是否强制添加
   */
  function addTableLine(rowIndex: number, addLineDisabled?: boolean, isInit?: boolean) {
    if (addLineDisabled) {
      return;
    }
    if (rowIndex === props.data.length - 1) {
      // 最后一行的更改才会触发添加新一行
      const id = new Date().getTime().toString();
      propsRes.value.data.push({
        id,
        enable: true, // 是否勾选
        ...cloneDeep(props.defaultParamDataItem), // 深拷贝，避免有嵌套引用类型，数据隔离
      } as any);
      emitChange('addTableLine', isInit);
    }
  }

  function handleFormChange(record: Record<string, any>, rowIndex: number, columnConfig: FormTableColumn) {
    emit('formChange', record, columnConfig, rowIndex);
    addTableLine(rowIndex, columnConfig.addLineDisabled);
  }

  function toggleRequired(record: Record<string, any>, rowIndex: number, columnConfig: FormTableColumn) {
    if (props.disabled) {
      return;
    }
    record.required = !record.required;
    emit('formChange', record, columnConfig, rowIndex);
    addTableLine(rowIndex, columnConfig.addLineDisabled);
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

  function handleMoreActionSelect(
    event: ActionsItem,
    record: Record<string, any>,
    columnConfig: FormTableColumn,
    rowIndex: number
  ) {
    emit('moreActionSelect', event, record, columnConfig, rowIndex);
  }

  function deleteParam(record: Record<string, any>, rowIndex: number) {
    propsRes.value.data.splice(rowIndex, 1);
    emitChange('deleteParam');
  }

  /**
   * 搜索变量
   * @param val 变量名
   */
  function handleSearchParams(val: string, item: FormTableColumn) {
    item.autoCompleteParams = item.autoCompleteParams?.map((e) => {
      e.isShow = e.label?.includes(val);
      return e;
    });
  }

  function selectAutoComplete(val: string, record: Record<string, any>, item: FormTableColumn) {
    record[item.dataIndex as string] = val;
  }

  function handleSelect(rowKeys: (string | number)[], rowKey: string | number, record: TableData) {
    emit('rowSelect', rowKeys, rowKey, record);
  }

  function handleSelectAll(checked: boolean) {
    emit('selectAll', checked);
  }

  function rowClass(record: TableData) {
    if (record.diff) {
      if (props.diffMode === 'add') {
        return 'add-row-class';
      }
      if (props.diffMode === 'delete') {
        return 'delete-row-class';
      }
    }
    return '';
  }

  defineExpose({
    validateAndUpdateErrorMessageList,
  });

  await initColumns();
</script>

<style lang="less" scoped>
  :deep(.ms-base-table) {
    .arco-table-th {
      .arco-table-cell {
        padding: 5px 8px !important;
        line-height: 1.5715;
      }
      .arco-table-cell-with-sorter {
        margin-left: 0;
      }
    }
    .arco-table-td {
      height: 32px;
      .arco-table-cell {
        padding: 0 !important;
        .arco-select-view-single,
        .arco-input-wrapper,
        .arco-textarea-wrapper {
          border-radius: 0;
        }
        article {
          position: relative;
          top: 50%;
          display: inline-block;
          transform: translateY(-50%); /* 垂直居中 */
        }
      }
    }
    .ms-form-table-td-text {
      padding: 0 8px;
    }
    .arco-table-col-fixed-right {
      .arco-table-cell {
        padding: 0 8px !important;
      }
    }
    .arco-table-th {
      .arco-icon-settings {
        margin-left: 4px;
      }
    }
    .arco-table-tr-checked {
      .arco-table-td {
        background-color: var(--color-text-fff);
      }
    }
    .arco-scrollbar-track-direction-horizontal {
      bottom: -8px;
    }
    .arco-textarea-disabled,
    .arco-input-disabled,
    .arco-select-view-disabled {
      @apply !bg-transparent;
      .arco-select-view-value {
        color: var(--color-text-4) !important;
      }
    }
  }
  .ms-form-table--disabled {
    :deep(.arco-table-td-content) {
      span,
      div:not(.ms-form-table-required-button--required) {
        color: var(--color-text-4) !important;
      }
    }
  }
  :deep(.arco-table-content) {
    border-top: 1px solid var(--color-text-n8) !important;
    border-right: 1px solid var(--color-text-n8) !important;
    border-left: 1px solid var(--color-text-n8) !important;
    border-radius: var(--border-radius-small);
  }
  :deep(.arco-table-th) {
    line-height: normal;
  }
  :deep(.arco-table .arco-table-cell) {
    padding: 8px 2px;
  }
  :deep(.arco-table-expand-btn) {
    padding: 0 5px;
    width: auto;
  }
  :deep(.arco-table-cell-expand-icon) {
    .arco-table-cell-inline-icon {
      margin-right: 0;
    }
  }
  .ms-form-table-no-left-action {
    :deep(.arco-table .arco-table-cell) {
      padding: 8px 16px;
    }
  }
  :deep(.arco-table-cell-align-left) {
    padding: 8px;
  }
  :deep(.arco-table-col-fixed-right) {
    .arco-table-cell-align-left {
      padding: 8px;
    }
  }
  :deep(.ms-table-row-disabled) {
    td {
      background-color: var(--color-text-fff) !important;
    }
    .arco-btn-icon {
      border-color: var(--color-text-n8);
      color: var(--color-text-4);
    }
    *:not(.arco-btn-icon) {
      border-color: var(--color-text-n8) !important;
      color: var(--color-text-4) !important;
    }
  }
  :deep(.ms-form-table-input:not(.arco-input-focus, .arco-select-view-focus, .arco-textarea-focus)) {
    border-radius: 0;
    &:not(:hover) {
      @apply bg-transparent;

      border-color: transparent !important;
      .arco-input::placeholder {
        @apply invisible;
      }
      .arco-select-view-icon {
        @apply invisible;
      }
      .arco-select-view-value {
        color: var(--color-text-1);
      }
      .arco-select {
        border-color: transparent !important;
      }
    }
  }
  :deep(.ms-form-table-input--hasPlaceholder) {
    .arco-input::placeholder {
      @apply !visible;
    }
  }
  :deep(.ms-form-table-input-trigger) {
    width: 350px;
    .arco-select-dropdown-list {
      .arco-select-option {
        @apply !h-auto;

        padding: 2px 8px !important;
      }
    }
  }
  :deep(.ms-form-table-input-number) {
    @apply bg-transparent pr-0;

    border-color: transparent !important;
    .arco-input-suffix {
      @apply hidden;
    }
    &:hover,
    &.arco-input-focus {
      border-color: rgb(var(--primary-5)) !important;
      .arco-input {
        @apply text-left;
      }
      .arco-input-suffix {
        @apply inline-flex;
      }
    }
  }
  :deep(.arco-table-expand-btn) {
    background: transparent;
  }
  :deep(.arco-form-item) {
    margin-bottom: 0;
    .arco-form-item-label-col {
      display: none;
    }
    .arco-form-item-content,
    .arco-form-item-wrapper-col {
      min-height: auto;
    }
    .arco-form-item-message {
      margin-bottom: 0;
    }
    .arco-form-item-content-flex {
      flex-wrap: nowrap;
    }
  }
  :deep(.add-row-class) {
    .arco-table-td {
      background: rgb(var(--success-1));
      .arco-table-td-content {
        background: rgb(var(--success-1));
      }
    }
  }
  :deep(.delete-row-class) {
    .arco-table-td {
      background: rgb(var(--danger-1));
      .arco-table-td-content {
        background: rgb(var(--danger-1));
      }
    }
  }
</style>
