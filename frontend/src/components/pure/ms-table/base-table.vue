<template>
  <div class="ms-base-table">
    <select-all
      v-if="attrs.selectable && attrs.showSelectAll"
      class="custom-action"
      :total="selectTotal"
      :current="selectCurrent"
      @change="handleChange"
    />
    <a-table
      v-bind="$attrs"
      :row-class="getRowClass"
      :selected-keys="props.selectedKeys"
      :span-method="spanMethod"
      :columns="currentColumns"
      @selection-change="(e) => selectionChange(e, true)"
      @sorter-change="(dataIndex: string,direction: string) => handleSortChange(dataIndex, direction)"
    >
      <template #optional="{ rowIndex, record }">
        <slot name="optional" v-bind="{ rowIndex, record }" />
      </template>
      <template #columns>
        <a-table-column
          v-for="(item, idx) in currentColumns"
          :key="idx"
          :width="item.width"
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
        >
          <template #title>
            <div
              v-if="props.showSetting && idx === currentColumns.length - 1"
              class="flex flex-row flex-nowrap items-center"
            >
              <slot :name="item.titleSlotName">
                <div class="text-[var(--color-text-3)]">{{ t(item.title as string) }}</div>
              </slot>
              <ColumnSelector :table-key="(attrs.tableKey as string)" @close="handleColumnSelectorClose" />
            </div>
            <slot v-else :name="item.titleSlotName">
              <div class="text-[var(--color-text-3)]">{{ t(item.title as string) }}</div>
            </slot>
          </template>
          <template #cell="{ column, record, rowIndex }">
            <div class="flex flex-row flex-nowrap items-center">
              <template v-if="item.dataIndex === SpecialColumnEnum.ENABLE">
                <slot name="enable" v-bind="{ record }">
                  <template v-if="record.enable">
                    <icon-check-circle-fill class="mr-[2px] text-[rgb(var(--success-6))]" />
                    {{ item.enableTitle ? t(item.enableTitle) : t('msTable.enable') }}
                  </template>
                  <template v-else>
                    <MsIcon type="icon-icon_disable" class="mr-[2px] text-[var(--color-text-4)]" />
                    <span class="text-[var(--color-text-1)]">
                      {{ item.disableTitle ? t(item.disableTitle) : t('msTable.disable') }}
                    </span>
                  </template>
                </slot>
              </template>
              <template v-else-if="item.isTag">
                <div class="one-line-text max-w-[456px]">
                  <slot :name="item.slotName" v-bind="{ record, rowIndex, column }">
                    {{ record[item.dataIndex as string] || '-' }}
                  </slot>
                </div>
              </template>
              <template v-else-if="item.slotName === SpecialColumnEnum.OPERATION">
                <slot name="operation" v-bind="{ record, rowIndex }" />
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
                  @blur="handleEditInputBlur()"
                  @press-enter="handleEditInputEnter(record)"
                />
                <a-tooltip v-else placement="top" :content="String(record[item.dataIndex as string])">
                  <div class="one-line-text max-w-[300px]">
                    <slot :name="item.slotName" v-bind="{ record, rowIndex, column }">
                      {{ record[item.dataIndex as string] || '-' }}
                    </slot>
                  </div>
                </a-tooltip>
                <div class="edit-icon">
                  <MsIcon
                    v-if="item.editType && editActiveKey !== `${item.dataIndex}${rowIndex}` && !record.deleted"
                    class="ml-2 cursor-pointer"
                    :class="{ 'ms-table-edit-active': editActiveKey === rowIndex }"
                    type="icon-icon_edit_outlined"
                    @click="handleEdit(item.dataIndex as string, rowIndex)"
                  />
                </div>
                <div>
                  <slot :name="item.revokeDeletedSlot" v-bind="{ record, rowIndex, column }"></slot>
                </div>
              </template>
              <template v-else>
                <slot :name="item.slotName" v-bind="{ record, rowIndex, column }">
                  {{ record[item.dataIndex as string] || '-' }}
                </slot>
              </template>
            </div>
          </template>
        </a-table-column>
      </template>

      <template #empty>
        <slot name="empty">
          <div class="flex h-[20px] flex-col items-center justify-center">
            <span class="text-[14px] text-[var(--color-text-4)]">{{ t('msTable.empty') }}</span>
          </div>
        </slot>
      </template>
    </a-table>
    <div
      class="mt-[16px] flex h-[32px] w-[100%] flex-row flex-nowrap items-center justify-end px-0"
      :class="{ 'batch-action': showBatchAction }"
    >
      <batch-action
        v-if="showBatchAction"
        :select-row-count="selectCurrent"
        :action-config="props.actionConfig"
        @batch-action="(item: BatchActionParams) => emit('batchAction', item)"
        @clear="selectionChange([], true)"
      />
      <ms-pagination
        v-else-if="attrs.showPagination"
        size="small"
        v-bind="attrs.msPagination"
        hide-on-single-page
        @change="pageChange"
        @page-size-change="pageSizeChange"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';
  import { useAttrs, computed, ref, onMounted, nextTick } from 'vue';
  import { useAppStore, useTableStore } from '@/store';
  import selectAll from './select-all.vue';
  import { SpecialColumnEnum, SelectAllEnum, ColumnEditTypeEnum } from '@/enums/tableEnum';
  import BatchAction from './batchAction.vue';
  import MsPagination from '@/components/pure/ms-pagination/index';
  import ColumnSelector from './columnSelector.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  import type { MsTableProps, MsPaginationI, BatchActionParams, BatchActionConfig, MsTableColumn } from './type';
  import type { TableData } from '@arco-design/web-vue';

  const batchleft = ref('10px');
  const { t } = useI18n();
  const tableStore = useTableStore();
  const appStore = useAppStore();
  const currentColumns = ref<MsTableColumn>([]);

  const props = defineProps<{
    selectedKeys?: (string | number)[];
    actionConfig?: BatchActionConfig;
    noDisable?: boolean;
    showSetting?: boolean;
    columns: MsTableColumn;
    spanMethod?: (params: { record: TableData; rowIndex: number; columnIndex: number }) => void;
  }>();
  const emit = defineEmits<{
    (e: 'selectedChange', value: (string | number)[]): void;
    (e: 'batchAction', value: BatchActionParams): void;
    (e: 'pageChange', value: number): void;
    (e: 'pageSizeChange', value: number): void;
    (e: 'rowNameChange', value: TableData): void;
    (e: 'sorterChange', value: { [key: string]: string }): void;
  }>();
  const isSelectAll = ref(false);
  const attrs = useAttrs();
  // 全选按钮-当前的条数
  const selectCurrent = ref(0);
  // 全选按钮-总条数
  const selectTotal = ref(0);
  // 编辑按钮的Active状态
  const editActiveKey = ref<string>('');
  // 编辑input的Ref
  const currentInputRef = ref();
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

  const setSelectAllTotal = (isAll: boolean) => {
    const { data, msPagination }: Partial<MsTableProps<any>> = attrs;
    if (isAll) {
      selectTotal.value = msPagination?.total || data?.length || appStore.pageSize;
    } else {
      selectTotal.value = data ? data.length : appStore.pageSize;
    }
  };

  const initColumn = () => {
    let tmpArr: MsTableColumn = [];
    if (props.showSetting) {
      tmpArr = tableStore.getShowInTableColumns(attrs.tableKey as string) || [];
    } else {
      tmpArr = props.columns;
    }
    currentColumns.value = tmpArr;
  };

  // 选择公共执行方法
  const selectionChange = (arr: (string | number)[], setCurrentSelect: boolean, isAll = false) => {
    setSelectAllTotal(isAll);
    emit('selectedChange', arr);
    if (setCurrentSelect) {
      selectCurrent.value = arr.length;
    }
  };

  // 全选change事件
  const handleChange = (v: string) => {
    const { data, msPagination }: Partial<MsTableProps<any>> = attrs;
    isSelectAll.value = v === SelectAllEnum.ALL;
    if (v === SelectAllEnum.NONE) {
      selectionChange([], true);
    }
    if (v === SelectAllEnum.CURRENT) {
      if (data && data.length > 0) {
        selectionChange(
          data.map((item: any) => item[rowKey || 'id']),
          true
        );
      }
    }
    if (v === SelectAllEnum.ALL) {
      const { total } = msPagination as MsPaginationI;
      if (data && data.length > 0) {
        selectionChange(
          data.map((item: any) => item[rowKey || 'id']),
          false,
          true
        );
        selectCurrent.value = total;
      }
    }
  };

  // 分页change事件
  const pageChange = (v: number) => {
    emit('pageChange', v);
  };
  // 分页size change事件
  const pageSizeChange = (v: number) => {
    emit('pageSizeChange', v);
  };

  const showBatchAction = computed(() => {
    return selectCurrent.value > 0 && attrs.showSelectAll;
  });

  const handleEditInputEnter = (record: TableData) => {
    editActiveKey.value = '';
    emit('rowNameChange', record);
  };

  const handleEditInputBlur = () => {
    currentInputRef.value = null;
    editActiveKey.value = '';
  };

  // 排序change事件
  const handleSortChange = (dataIndex: string, direction: string) => {
    const regex = /^__arco_data_index_(\d+)$/;
    const match = dataIndex.match(regex);
    const lastDigit = match && (match[1] as unknown as number);
    if (lastDigit) {
      dataIndex = currentColumns.value[lastDigit].dataIndex as string;
    }
    let sortOrder = '';
    if (direction === 'ascend') {
      sortOrder = 'asc';
    } else if (direction === 'descend') {
      sortOrder = 'desc';
    }

    emit('sorterChange', sortOrder ? { [dataIndex]: sortOrder } : {});
  };

  // 编辑单元格的input
  const handleEdit = (dataIndex: string, rowIndex: number) => {
    editActiveKey.value = dataIndex + rowIndex;
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

  const handleColumnSelectorClose = () => {
    initColumn();
  };

  function getRowClass(record: TableData) {
    if (!record.enable && !props.noDisable) {
      return 'ms-table-row-disabled';
    }
  }

  onMounted(() => {
    initColumn();
    batchleft.value = getBatchLeft();
  });
</script>

<style lang="less" scoped>
  .ms-base-table {
    position: relative;
    .custom-action {
      position: absolute;
      top: 13px;
      left: v-bind(batchleft);
      z-index: 99;
      border-radius: 2px;
      line-height: 40px;
      cursor: pointer;
    }
    .batch-action {
      justify-content: flex-start;
    }
    .ms-table-edit-active {
      color: rgb(var(--primary-5));
    }
    .edit-icon {
      color: rgb(var(--primary-7));
      opacity: 0;
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
  :deep(.ms-table-select-all) {
    .arco-checkbox {
      margin-left: 0.5rem;
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
</style>
