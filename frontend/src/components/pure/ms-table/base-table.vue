<template>
  <div :class="['ms-base-table', $slots.quickCreate ? 'ms-base-table--hasQuickCreate' : '']">
    <div v-if="$slots.quickCreate" class="ms-base-table-quickCreate">
      <slot name="quickCreate"></slot>
    </div>
    <!-- 表只做自适应不做可拖拽列 -->
    <a-table
      v-bind="{ ...$attrs, ...scrollObj }"
      :row-class="getRowClass"
      :column-resizable="false"
      :span-method="spanMethod"
      :columns="currentColumns"
      :expanded-keys="props.expandedKeys"
      :span-all="props.spanAll"
      @expand="(rowKey, record) => emit('expand', record)"
      @change="(data: TableData[], extra: TableChangeExtra, currentData: TableData[]) => handleDragChange(data, extra, currentData)"
      @sorter-change="(dataIndex: string,direction: string) => handleSortChange(dataIndex, direction)"
      @cell-click="(record: TableData,column: TableColumnData,ev: Event) => emit('cell-click',record, column,ev)"
    >
      <template #optional="{ rowIndex, record }">
        <slot name="optional" v-bind="{ rowIndex, record }" />
      </template>
      <template #columns>
        <a-table-column
          v-if="attrs.selectable && props.selectedKeys"
          :width="props.firstColumnWidth || 60"
          fixed="left"
          cell-class="arco-table-operation"
          body-cell-class="arco-table-operation"
        >
          <template #title>
            <SelectALL
              v-if="attrs.selectorType === 'checkbox'"
              :total="attrs.showPagination ? (attrs.msPagination as MsPaginationI).total : (attrs.data as MsTableDataItem<TableData>[]).length"
              :selected-keys="props.selectedKeys"
              :current-data="attrs.data as Record<string,any>[]"
              :show-select-all="!!attrs.showPagination && props.showSelectorAll"
              :disabled="(attrs.data as []).length === 0"
              @change="handleSelectAllChange"
            />
          </template>
          <template #cell="{ record }">
            <MsCheckbox
              v-if="attrs.selectorType === 'checkbox'"
              :value="props.selectedKeys.has(record[rowKey || 'id'])"
              @change="rowSelectChange(record[rowKey || 'id'])"
            />
            <a-radio
              v-else-if="attrs.selectorType === 'radio'"
              v-model:model-value="record.tableChecked"
              @change="(val) => handleRadioChange(val as boolean, record)"
            />
          </template>
        </a-table-column>
        <a-table-column
          v-for="(item, idx) in currentColumns"
          :key="idx"
          :width="item.isTag || item.isStringTag ? item.width || 360 : item.width"
          :align="item.align"
          :fixed="item.fixed"
          :sortable="item.sortable"
          :filterable="item.filterable"
          :cell-class="item.cellClass"
          :header-cell-class="item.headerCellClass"
          :body-cell-class="item.bodyCellClass"
          :summary-cell-class="item.summaryCellClass"
          :cell-style="item.cellStyle"
          :header-cell-style="item.headerCellStyle"
          :body-cell-style="item.bodyCellStyle"
          :summary-cell-style="item.summaryCellStyle"
          :index="item.index"
          :tooltip="item.tooltip"
          :title="item.slotName"
        >
          <template #title>
            <div :class="{ 'flex w-full flex-row flex-nowrap items-center gap-[16px]': !item.align }">
              <slot :name="item.titleSlotName" :column-config="item">
                <div v-if="item.title" class="text-[var(--color-text-3)]">{{ t(item.title as string) }}</div>
              </slot>
              <columnSelectorIcon
                v-if="
                  props.showSetting &&
                  (item.slotName === SpecialColumnEnum.OPERATION || item.slotName === SpecialColumnEnum.ACTION)
                "
                :table-key="(attrs.tableKey as string)"
                :is-simple="(attrs.isSimpleSetting as boolean)"
                @show-setting="handleShowSetting"
                @init-data="handleInitColumn"
              />
              <slot v-else-if="item.filterConfig" :name="item.filterConfig.filterSlotName">
                <DefaultFilter
                  class="ml-[4px]"
                  :options="item.filterConfig.options"
                  :multiple="(item.filterConfig.multiple as boolean)"
                  @handle-confirm="(v) => handleFilterConfirm(v, item.dataIndex as string, item.filterConfig?.multiple || false,item.isCustomParam || false)"
                />
              </slot>
            </div>
          </template>
          <template #cell="{ column, record, rowIndex }">
            <div :class="{ 'flex w-full flex-row items-center': !item.isTag && !item.align }">
              <template v-if="item.dataIndex === SpecialColumnEnum.ENABLE">
                <slot name="enable" v-bind="{ record }">
                  <div v-if="record.enable" class="flex flex-row flex-nowrap items-center gap-[2px]">
                    <icon-check-circle-fill class="text-[rgb(var(--success-6))]" />
                    <div>{{ item.enableTitle ? t(item.enableTitle) : t('msTable.enable') }}</div>
                  </div>
                  <div v-else class="flex flex-row flex-nowrap items-center gap-[2px]">
                    <MsIcon type="icon-icon_disable" class="text-[var(--color-text-4)]" />
                    <div class="text-[var(--color-text-1)]">
                      {{ item.disableTitle ? t(item.disableTitle) : t('msTable.disable') }}
                    </div>
                  </div>
                </slot>
              </template>
              <template v-else-if="item.isTag || item.isStringTag">
                <template
                  v-if="!record[item.dataIndex as string] || (Array.isArray(record[item.dataIndex as string]) && record[item.dataIndex as string].length === 0)"
                >
                  <slot :name="item.slotName" v-bind="{ record, rowIndex, column, columnConfig: item }"> - </slot>
                </template>
                <MsTagGroup
                  v-else
                  :is-string-tag="item.isStringTag"
                  :tag-list="record[item.dataIndex as string]"
                  type="primary"
                  theme="outline"
                />
              </template>
              <template v-else-if="item.slotName === SpecialColumnEnum.OPERATION">
                <slot name="operation" v-bind="{ record, rowIndex, columnConfig: item }" />
              </template>
              <template v-else-if="item.slotName === SpecialColumnEnum.ACTION">
                <slot name="action" v-bind="{ record, rowIndex }" />
              </template>
              <template v-else-if="item.showTooltip">
                <a-input
                  v-if="
                    editActiveKey === `${item.dataIndex}${rowIndex}` &&
                    item.editType &&
                    item.editType === ColumnEditTypeEnum.INPUT
                  "
                  ref="currentInputRef"
                  v-model="record[item.dataIndex as string]"
                  :max-length="255"
                  @blur="handleEditInputBlur(record, item.dataIndex as string, true)"
                  @keydown.enter="handleEditInputBlur(record, item.dataIndex as string, false)"
                />
                <a-tooltip
                  v-else
                  placement="top"
                  content-class="max-w-[400px]"
                  :content="String(record[item.dataIndex as string])"
                  :disabled="record[item.dataIndex as string] === '' || record[item.dataIndex as string] === undefined || record[item.dataIndex as string] === null"
                >
                  <div class="one-line-text">
                    <slot :name="item.slotName" v-bind="{ record, rowIndex, column, columnConfig: item }">
                      {{ record[item.dataIndex as string] || (attrs.emptyDataShowLine ? '-' : '') }}
                    </slot>
                  </div>
                </a-tooltip>
                <div class="edit-icon">
                  <MsIcon
                    v-if="
                      item.editType &&
                      editActiveKey !== `${item.dataIndex}${rowIndex}` &&
                      !record.deleted &&
                      record.scopeId !== 'global'
                    "
                    class="ml-2 cursor-pointer"
                    :class="{ 'ms-table-edit-active': editActiveKey === rowIndex }"
                    type="icon-icon_edit_outlined"
                    @click="handleEdit(item.dataIndex as string, rowIndex, record)"
                  />
                </div>
                <div>
                  <slot :name="item.revokeDeletedSlot" v-bind="{ record, rowIndex, column, columnConfig: item }"></slot>
                </div>
              </template>
              <template v-else>
                <slot
                  :name="item.slotName"
                  v-bind="{ record, rowIndex, column, dataIndex: item.dataIndex, columnConfig: item }"
                >
                  {{ record[item.dataIndex as string] || (attrs.emptyDataShowLine ? '-' : '') }}
                </slot>
              </template>
            </div>
          </template>
        </a-table-column>
      </template>

      <template #empty>
        <div class="w-full">
          <slot name="empty">
            <div class="flex h-[40px] flex-col items-center justify-center">
              <span class="text-[14px] text-[var(--color-text-4)]">{{ t('common.noData') }}</span>
            </div>
          </slot>
        </div>
      </template>
      <template #expand-icon="{ expanded, record }">
        <slot name="expand-icon" v-bind="{ expanded, record }">
          <MsIcon v-if="!expanded" :size="8" type="icon-icon_right_outlined" class="text-[var(--color-text-4)]" />
          <MsIcon v-else :size="8" class="text-[rgb(var(--primary-6))]" type="icon-icon_down_outlined" />
        </slot>
      </template>
    </a-table>
    <div
      v-if="attrs.showFooterActionWrap"
      class="mt-[16px] flex h-[32px] flex-row flex-nowrap items-center"
      :class="{ 'justify-between': showBatchAction }"
    >
      <span v-if="!props.actionConfig && selectedCount > 0" class="title text-[var(--color-text-2)]">
        {{ t('msTable.batch.selected', { count: selectedCount }) }}
        <a-button class="clear-btn ml-[12px] px-2" type="text" @click="emit('clearSelector')">
          {{ t('msTable.batch.clear') }}
        </a-button>
      </span>
      <div class="flex flex-grow">
        <batch-action
          v-if="showBatchAction"
          class="flex-1"
          :select-row-count="selectedCount"
          :action-config="props.actionConfig"
          @batch-action="handleBatchAction"
          @clear="emit('clearSelector')"
        />
      </div>
      <div class="min-w-[500px]">
        <ms-pagination
          v-if="!!attrs.showPagination"
          size="small"
          v-bind="(attrs.msPagination as MsPaginationI)"
          hide-on-single-page
          @change="pageChange"
          @page-size-change="pageSizeChange"
        />
      </div>
    </div>
    <ColumnSelector
      v-if="props.showSetting"
      v-model:visible="columnSelectorVisible"
      :show-jump-method="(attrs.showJumpMethod as boolean)"
      :table-key="(attrs.tableKey as string)"
      :show-pagination="!!attrs.showPagination"
      :show-subdirectory="!!attrs.showSubdirectory"
      @init-data="handleInitColumn"
      @page-size-change="pageSizeChange"
      @module-change="emit('moduleChange')"
    ></ColumnSelector>
  </div>
</template>

<script lang="ts" setup>
  import { computed, defineModel, nextTick, onMounted, ref, useAttrs, watch } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsPagination from '@/components/pure/ms-pagination/index';
  import MsTagGroup from '@/components/pure/ms-tag/ms-tag-group.vue';
  import MsCheckbox from '../ms-checkbox/MsCheckbox.vue';
  import BatchAction from './batchAction.vue';
  import ColumnSelector from './columnSelector.vue';
  import columnSelectorIcon from './columnSelectorIcon.vue';
  import DefaultFilter from './comp/defaultFilter.vue';
  import SelectALL from './select-all.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore, useTableStore } from '@/store';

  import { DragSortParams } from '@/models/common';
  import { ColumnEditTypeEnum, SelectAllEnum, SpecialColumnEnum } from '@/enums/tableEnum';

  import type {
    BatchActionConfig,
    BatchActionParams,
    BatchActionQueryParams,
    MsPaginationI,
    MsTableColumn,
    MsTableDataItem,
    MsTableProps,
  } from './type';
  import type { TableChangeExtra, TableColumnData, TableData } from '@arco-design/web-vue';
  import type { TableOperationColumn } from '@arco-design/web-vue/es/table/interface';

  const batchLeft = ref('10px');
  const { t } = useI18n();
  const tableStore = useTableStore();
  const currentColumns = ref<MsTableColumn>([]);
  const appStore = useAppStore();
  const columnSelectorVisible = ref(false);

  const props = defineProps<{
    selectedKeys: Set<string>;
    selectedKey: string;
    excludeKeys: Set<string>;
    selectorStatus: SelectAllEnum;
    actionConfig?: BatchActionConfig;
    noDisable?: boolean;
    showSetting?: boolean;
    columns: MsTableColumn;
    spanMethod?: (params: {
      record: TableData;
      column: TableColumnData | TableOperationColumn;
      rowIndex: number;
      columnIndex: number;
    }) => void | {
      rowspan?: number | undefined;
      colspan?: number | undefined;
    };
    expandedKeys?: string[];
    rowClass?: string | any[] | Record<string, any> | ((record: TableData, rowIndex: number) => any);
    spanAll?: boolean;
    showSelectorAll?: boolean;
    firstColumnWidth?: number; // 选择、拖拽列的宽度
  }>();
  const emit = defineEmits<{
    (e: 'update:selectedKey', value: string): void;
    (e: 'batchAction', value: BatchActionParams, queryParams: BatchActionQueryParams): void;
    (e: 'pageChange', value: number): void;
    (e: 'pageSizeChange', value: number): void;
    (e: 'rowNameChange', value: TableData, cb: (v: boolean) => void): void;
    (e: 'rowSelectChange', key: string): void;
    (e: 'selectAllChange', value: SelectAllEnum): void;
    (e: 'dragChange', value: DragSortParams): void;
    (e: 'sorterChange', value: { [key: string]: string }): void;
    (e: 'expand', record: TableData): void | Promise<any>;
    (e: 'cell-click', record: TableData, column: TableColumnData, ev: Event): void | Promise<any>;
    (e: 'clearSelector'): void;
    (e: 'filterChange', dataIndex: string, value: (string | number)[], multiple: boolean, isCustomParam: boolean): void;
    (e: 'moduleChange'): void;
  }>();
  const attrs = useAttrs();

  // 编辑按钮的Active状态
  const editActiveKey = ref<string>('');
  // 编辑项的Ref
  const currentInputRef = ref();
  // 编辑项的初始值，用于blur时恢复旧值
  const currentEditValue = ref<string>('');
  // 是否是enter触发
  const isEnter = ref<boolean>(false);

  const { rowKey }: Partial<MsTableProps<any>> = attrs;
  // 第一行表格合并
  const currentSpanMethod = ({
    rowIndex,
    columnIndex,
  }: {
    record: TableData;
    rowIndex: number;
    columnIndex: number;
  }) => {
    if (rowIndex === 0 && columnIndex === 0) {
      return {
        rowspan: 1,
        colspan: currentColumns.value.length,
      };
    }
  };
  const spanMethod = computed(() => {
    if (props.spanMethod) {
      return props.spanMethod;
    }
    if (attrs.showFirstOperation) {
      return currentSpanMethod;
    }
    return undefined;
  });

  const scrollObj = ref<Record<string, any>>({});
  const initColumn = async (arr?: MsTableColumn) => {
    try {
      let tmpArr: MsTableColumn = [];
      if (props.showSetting) {
        tmpArr = await tableStore.getShowInTableColumns(attrs.tableKey as string);
      } else {
        tmpArr = props.columns;
      }
      currentColumns.value = arr || tmpArr;
      // 如果是完全没有列展示除了固定列需要对操作列宽度进行限制和浮动位置限制
      if (props.showSetting) {
        const isNoDragColumnsLength = (arr || tmpArr || []).length;
        if (isNoDragColumnsLength <= 3) {
          currentColumns.value = (arr || tmpArr || []).map((item: any) => {
            const currentItem = { ...item };
            if (item.slotName === SpecialColumnEnum.OPERATION || item.slotName === SpecialColumnEnum.ACTION) {
              return {
                ...currentItem,
                fixed: 'right',
              };
            }
            delete currentItem.width;
            return currentItem;
          });
          scrollObj.value = {
            scroll: {
              x: 'auto',
            },
          };
        } else {
          scrollObj.value = {};
          currentColumns.value = arr || tmpArr;
        }
      } else {
        scrollObj.value = {};
        currentColumns.value = arr || tmpArr;
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error('InitColumn failed', error);
    }
  };

  const innerSelectedKey = defineModel<string>('selectedKey', { default: '' }); // 内部维护的单选选中项
  const tempRecord = ref<TableData>({});

  watch(
    () => attrs.data,
    (arr) => {
      if (innerSelectedKey.value && Array.isArray(arr) && arr.length > 0) {
        arr = arr.map((item: TableData) => {
          if (item.id === innerSelectedKey.value) {
            item.tableChecked = true;
            tempRecord.value = item;
          }
          return item;
        });
      }
    },
    {
      immediate: true,
    }
  );

  function handleRadioChange(val: boolean, record: TableData) {
    if (val) {
      innerSelectedKey.value = record.id;
      record.tableChecked = true;
      tempRecord.value.tableChecked = false;
      tempRecord.value = record;
    }
  }

  // 全选change事件
  const handleSelectAllChange = (v: SelectAllEnum) => {
    emit('selectAllChange', v);
  };
  // 行选择器change事件
  const rowSelectChange = (key: string) => {
    emit('rowSelectChange', key);
  };

  // 分页change事件
  const pageChange = (v: number) => {
    emit('pageChange', v);
  };
  // 分页size change事件
  const pageSizeChange = (v: number) => {
    emit('pageSizeChange', v);
  };

  const selectedCount = computed(() => {
    if (props.selectorStatus === SelectAllEnum.ALL && attrs.msPagination) {
      return (attrs.msPagination as MsPaginationI).total - props.excludeKeys.size;
    }
    return props.selectedKeys.size;
  });

  const showBatchAction = computed(() => {
    return selectedCount.value > 0 && attrs.selectable;
  });

  const handleBatchAction = (value: BatchActionParams) => {
    const { selectorStatus, selectedKeys, excludeKeys } = props;
    const queryParams: BatchActionQueryParams = {
      selectedIds: Array.from(selectedKeys),
      excludeIds: Array.from(excludeKeys),
      selectAll: selectorStatus === SelectAllEnum.ALL,
      currentSelectCount: selectedCount.value,
      params: {
        ...(attrs.msPagination as MsPaginationI),
      },
    };
    emit('batchAction', value, queryParams);
  };

  const handleEditInputBlur = (record: TableData, dataIndex: string, isBlur: boolean) => {
    if (isBlur) {
      if (!isEnter.value) {
        // 不是由Enter触发的blur
        record[dataIndex] = currentEditValue.value;
      }
      isEnter.value = false;
      currentInputRef.value = null;
      editActiveKey.value = '';
      currentEditValue.value = '';
    } else {
      if (!record[dataIndex]) {
        Message.warning(t('common.value.notNull'));
        return;
      }
      // 触发的是Enter
      emit('rowNameChange', record, (v: boolean) => {
        if (!v) {
          // 如果接口报错，没有成功的修改，恢复旧值
          record[dataIndex] = currentEditValue.value;
        }
        isEnter.value = true;
        editActiveKey.value = '';
      });
    }
  };

  // 排序change事件
  const handleSortChange = (dataIndex: string, direction: string) => {
    let firstIndex = 0;
    if (attrs.selectable) {
      firstIndex = 1;
    }
    const regex = /^__arco_data_index_(\d+)$/;
    const match = dataIndex.match(regex);
    const lastDigit = match && Number(match[1]);
    if (lastDigit !== null && !Number.isNaN(lastDigit)) {
      dataIndex = currentColumns.value[lastDigit - firstIndex].dataIndex as string;
    }
    let sortOrder = '';
    if (direction === 'ascend') {
      sortOrder = 'asc';
    } else if (direction === 'descend') {
      sortOrder = 'desc';
    }

    emit('sorterChange', sortOrder ? { [dataIndex]: sortOrder } : {});
  };

  // 拖拽排序
  const handleDragChange = (data: TableData[], extra: TableChangeExtra, currentData: TableData[]) => {
    if (!currentData || currentData.length === 1) {
      return;
    }

    if (extra && extra.dragTarget?.id) {
      const params: DragSortParams = {
        projectId: appStore.currentProjectId,
        targetId: '', // 放置目标id
        moveMode: 'BEFORE',
        moveId: extra.dragTarget.id as string, // 拖拽id
      };
      const index = currentData.findIndex((item: any) => item.key === extra.dragTarget?.id);

      if (index > -1 && currentData[index + 1]) {
        params.moveMode = 'BEFORE';
        params.targetId = currentData[index + 1].raw.id;
      } else if (index > -1 && !currentData[index + 1]) {
        if (index > -1 && currentData[index - 1]) {
          params.moveMode = 'AFTER';
          params.targetId = currentData[index - 1].raw.id;
        }
      }
      emit('dragChange', params);
    }
  };

  // 编辑单元格的input
  const handleEdit = (dataIndex: string, rowIndex: number, record: TableData) => {
    editActiveKey.value = dataIndex + rowIndex;
    currentEditValue.value = record[dataIndex];
    if (currentInputRef.value) {
      currentInputRef.value[0].focus();
    } else {
      nextTick(() => {
        currentInputRef.value[0].focus();
      });
    }
  };

  // 根据参数获取全选按钮的位置
  const getBatchLeft = () => {
    if (attrs.enableDrag) {
      return '30px';
    }
    switch (attrs.size) {
      case 'small':
        return '15px';
      case 'mini':
        return '10px';
      default:
        return '8px';
    }
  };

  const handleInitColumn = async () => {
    await initColumn();
  };

  function getRowClass(record: TableData, rowIndex: number) {
    if (props.rowClass) {
      return typeof props.rowClass === 'function' ? props.rowClass(record, rowIndex) : props.rowClass;
    }

    if (record.enable === false && !props.noDisable) {
      return 'ms-table-row-disabled';
    }
  }
  const handleShowSetting = () => {
    columnSelectorVisible.value = true;
  };

  const handleFilterConfirm = (
    value: (string | number)[],
    dataIndex: string,
    multiple: boolean,
    isCustomParam: boolean
  ) => {
    emit('filterChange', dataIndex, value, multiple, isCustomParam);
  };

  onMounted(async () => {
    await initColumn();
    batchLeft.value = getBatchLeft();
  });

  defineExpose({
    initColumn,
  });
</script>

<style lang="less" scoped>
  .ms-base-table {
    position: relative;
    .ms-table-edit-active {
      color: rgb(var(--primary-5));
    }
    .edit-icon {
      color: rgb(var(--primary-7));
      opacity: 0;
    }
    :deep(.arco-table-cell-align-left) {
      .arco-table-td-content {
        @apply flex items-center;
      }
    }
    :deep(.arco-table-hover) {
      :not(.arco-table-dragging) {
        .arco-table-tr:not(.arco-table-tr-empty):not(.arco-table-tr-summary):hover {
          .arco-table-td:not(.arco-table-col-fixed-left):not(.arco-table-col-fixed-right) {
            .edit-icon {
              opacity: 1;
            }
          }
        }
      }
    }
    .setting-icon {
      margin-left: 16px;
      color: var(--color-text-4);
      background-color: var(--color-text-10);
      cursor: pointer;
      &:hover {
        color: rgba(var(--primary-5));
      }
    }
  }
  .ms-base-table--hasQuickCreate {
    :deep(.arco-table-body:not(.arco-scrollbar-container)) {
      padding-top: 54px;
    }
    :deep(.arco-table-element:not(.arco-table-header .arco-table-element)) {
      padding-bottom: 54px;
      tbody {
        transform: translateY(54px);
      }
    }
    :deep(.arco-table-tr:first-child) {
      .arco-table-td {
        border-top: 1px solid var(--color-text-n8);
      }
    }
    .ms-base-table-quickCreate {
      @apply absolute left-0 flex w-full items-center;

      top: 55px;
      z-index: 11;
      padding: 16px;
      background-color: var(--color-text-n9);
    }
  }
  :deep(.arco-table-operation) {
    .arco-table-td-content {
      @apply justify-center;
    }
  }
  :deep(.ms-table-select-all) {
    .dropdown-icon {
      background: none !important;
    }
    .arco-checkbox-icon {
      &::after {
        width: 8px;
        background: rgb(var(--primary-5));
      }
    }
    .arco-checkbox-indeterminate .arco-checkbox-icon {
      border-color: rgb(var(--primary-5));
      background: rgb(var(--primary-1));
    }
    .arco-icon-hover:hover::before {
      background: none;
    }
  }
  :deep(.arco-checkbox-icon) {
    width: 16px;
    height: 16px;
    border-radius: 2px;
    &:hover {
      border-color: rgb(var(--primary-5));
    }
    &::before {
      background: none !important;
    }
  }
  :deep(.arco-checkbox:hover .arco-checkbox-icon-hover::before) {
    background: none !important;
  }
  :deep(.arco-table .arco-table-expand-btn) {
    border-color: transparent;
  }
  :deep(.arco-table-tr-expand .arco-table-td) {
    padding: 0;
    background: none;
  }
  :deep(.arco-table-tr-expand .arco-table-cell) {
    padding: 0 !important;
  }
  :deep(.collapsebtn) {
    width: 16px;
    height: 16px;
    border-radius: 50%;
    background: var(--color-text-n8) !important;
    @apply bg-white;
  }
  :deep(.expand) {
    width: 16px;
    height: 16px;
    border-radius: 50%;
    background: rgb(var(--primary-1));
  }
  :deep(.arco-table-expand-btn) {
    width: 16px;
    height: 16px;
    border: none;
    border-radius: 50%;
    background: var(--color-text-n8);
  }
  :deep(.arco-table .arco-table-expand-btn:hover) {
    border-color: transparent;
  }
  :deep(.arco-table-drag-handle) {
    .arco-icon-drag-dot-vertical {
      color: var(--color-text-brand);
    }
  }
  :deep(.arco-table-col-sorted) {
    @apply bg-white;
  }
  :deep(.arco-table-cell-with-sorter) {
    &:hover {
      @apply bg-white;
    }
    .arco-table-sorter-icon:not(.arco-table-sorter-icon-active) {
      .arco-icon-caret-up {
        color: var(--color-neutral-5);
      }
    }
  }
</style>
