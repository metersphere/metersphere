<template>
  <MsBaseTable
    v-bind="propsRes"
    :hoverable="false"
    is-simple-setting
    :span-method="props.spanMethod"
    :class="!props.selectable && !props.draggable ? 'ms-form-table-no-left-action' : ''"
    v-on="propsEvent"
  >
    <!-- 展开行-->
    <template #expand-icon="{ expanded, record }">
      <div class="flex items-center gap-[2px] text-[var(--color-text-4)]">
        <MsIcon :type="expanded ? 'icon-icon_split_turn-down_arrow' : 'icon-icon_split-turn-down-left'" />
        <div v-if="record.children">{{ record.children.length }}</div>
      </div>
    </template>
    <template
      v-for="item of props.columns.filter((e) => e.slotName !== undefined)"
      #[item.slotName!]="{ record, rowIndex, column }"
    >
      <slot :name="item.slotName" v-bind="{ record, rowIndex, column, dataIndex: item.dataIndex, columnConfig: item }">
        <a-tooltip
          v-if="item.hasRequired"
          :content="t(record.required ? 'msFormTable.paramRequired' : 'msFormTable.paramNotRequired')"
        >
          <MsButton
            type="icon"
            :class="[
              record.required ? '!text-[rgb(var(--danger-5))]' : '!text-[var(--color-text-brand)]',
              '!mr-[4px] !p-[4px]',
            ]"
            size="mini"
            @click="toggleRequired(record, rowIndex, item)"
          >
            <div>*</div>
          </MsButton>
        </a-tooltip>
        <a-input
          v-if="item.inputType === 'input'"
          v-model:model-value="record[item.dataIndex as string]"
          :placeholder="t(item.locale)"
          class="ms-form-table-input"
          :max-length="255"
          size="mini"
          @input="() => handleFormChange(record, rowIndex, item)"
        />
        <a-select
          v-else-if="item.inputType === 'select'"
          v-model:model-value="record[item.dataIndex as string]"
          :options="item.typeOptions || []"
          class="ms-form-table-input w-full"
          size="mini"
          @change="() => handleFormChange(record, rowIndex, item)"
        />
        <a-popover
          v-else-if="item.inputType === 'tags'"
          position="tl"
          :disabled="record[item.dataIndex as string].length === 0"
          class="ms-params-input-popover"
        >
          <template #content>
            <div class="ms-form-table-popover-title">
              {{ t('common.tag') }}
            </div>
            <div class="ms-form-table-popover-value">
              <MsTagsGroup is-string-tag :tag-list="record[item.dataIndex as string]" />
            </div>
          </template>
          <MsTagsInput
            v-model:model-value="record[item.dataIndex as string]"
            :max-tag-count="1"
            input-class="ms-form-table-input"
            size="mini"
            @change="() => handleFormChange(record, rowIndex, item)"
            @clear="() => handleFormChange(record, rowIndex, item)"
          />
        </a-popover>
        <a-switch
          v-else-if="item.inputType === 'switch'"
          v-model:model-value="record[item.dataIndex as string]"
          size="small"
          class="ms-form-table-input-switch"
          type="line"
          @change="() => handleFormChange(record, rowIndex, item)"
        />
        <a-checkbox
          v-else-if="item.inputType === 'checkbox'"
          v-model:model-value="record[item.dataIndex as string]"
          @change="() => handleFormChange(record, rowIndex, item)"
        />
        <template v-else-if="item.inputType === 'autoComplete'">
          <a-auto-complete
            v-model:model-value="record[item.dataIndex as string]"
            :data="item.autoCompleteParams?.filter((e) => e.isShow === true)"
            class="ms-form-table-input"
            :trigger-props="{ contentClass: 'ms-form-table-input-trigger' }"
            :filter-option="false"
            size="small"
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
        <template v-else-if="item.inputType === 'text'">
          {{
            typeof item.valueFormat === 'function' ? item.valueFormat(record) : record[item.dataIndex as string] || '-'
          }}
        </template>
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
            <icon-minus-circle
              v-if="dataLength > 1 && rowIndex !== dataLength - 1"
              class="ml-[8px] cursor-pointer text-[var(--color-text-4)]"
              size="20"
              @click="deleteParam(record, rowIndex)"
            />
          </div>
        </template>
      </slot>
    </template>
  </MsBaseTable>
</template>

<script setup lang="ts">
  import { cloneDeep } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumnData } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import MsTagsGroup from '@/components/pure/ms-tag/ms-tag-group.vue';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useTableStore from '@/hooks/useTableStore';

  import { SelectAllEnum, TableKeyEnum } from '@/enums/tableEnum';

  import { ActionsItem } from '../ms-table-more-action/types';
  import type { SelectOptionData, TableColumnData, TableData } from '@arco-design/web-vue';
  import { TableOperationColumn } from '@arco-design/web-vue/es/table/interface';

  export interface FormTableColumn extends MsTableColumnData {
    enable?: boolean; // 是否启用
    required?: boolean; // 是否必填
    inputType?: 'input' | 'select' | 'tags' | 'switch' | 'text' | 'checkbox' | 'autoComplete'; // 输入组件类型
    autoCompleteParams?: SelectOptionData[]; // 自动补全参数
    valueFormat?: (record: Record<string, any>) => string; // 展示值格式化，仅在inputType为text时生效
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
  }>();

  const { t } = useI18n();
  const tableStore = useTableStore();

  async function initColumns() {
    if (props.showSetting && props.tableKey) {
      await tableStore.initColumn(props.tableKey, props.columns);
    }
  }

  const { propsRes, propsEvent } = useTable(() => Promise.resolve([]), {
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
  });

  const selectedKeys = computed(() => propsRes.value.data.filter((e) => e.enable).map((e) => e.id));
  propsEvent.value.rowSelectChange = (key: string) => {
    propsRes.value.data = propsRes.value.data.map((e) => {
      if (e.id === key) {
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

  const dataLength = computed(() => propsRes.value.data.length);

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
    },
    {
      immediate: true,
    }
  );

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
        ...cloneDeep(props.defaultParamDataItem), // 深拷贝，避免有嵌套引用类型，数据隔离
        enable: true, // 是否勾选
      } as any);
      emitChange('addTableLine', isInit);
    }
  }

  function handleFormChange(record: Record<string, any>, rowIndex: number, columnConfig: FormTableColumn) {
    emit('formChange', record, columnConfig, rowIndex);
    addTableLine(rowIndex, columnConfig.addLineDisabled);
  }

  function toggleRequired(record: Record<string, any>, rowIndex: number, columnConfig: FormTableColumn) {
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

  await initColumns();
</script>

<style lang="less" scoped>
  :deep(.arco-table-th) {
    background-color: var(--color-text-n9);
    line-height: normal;
  }
  :deep(.arco-table .arco-table-cell) {
    padding: 8px 2px;
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
      background-color: white !important;
    }
    * {
      color: var(--color-text-4) !important;
    }
  }
  :deep(.ms-form-table-input:not(.arco-input-focus, .arco-select-view-focus)) {
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
  :deep(.arco-table-expand-btn) {
    background: transparent;
  }
  .ms-form-table-popover-title {
    @apply font-medium;

    margin-bottom: 4px;
    font-size: 12px;
    font-weight: 500;
    line-height: 16px;
    color: var(--color-text-1);
  }
  .ms-form-table-popover-value {
    min-width: 100px;
    max-width: 280px;
    font-size: 12px;
    line-height: 16px;
    color: var(--color-text-1);
  }
</style>
