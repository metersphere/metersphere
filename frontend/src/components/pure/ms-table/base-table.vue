<template>
  <div class="ms-base-table">
    <a-table
      v-bind="$attrs"
      :row-class="getRowClass"
      :span-method="spanMethod"
      :columns="currentColumns"
      @sorter-change="(dataIndex: string,direction: string) => handleSortChange(dataIndex, direction)"
    >
      <template #optional="{ rowIndex, record }">
        <slot name="optional" v-bind="{ rowIndex, record }" />
      </template>
      <template #columns>
        <a-table-column v-if="attrs.selectable && props.selectedKeys" :width="60">
          <template #title>
            <select-all
              :total="selectTotal"
              :current="selectCurrent"
              :type="attrs.selectorType as ('checkbox' | 'radio')"
              @change="handleSelectAllChange"
            />
          </template>
          <template #cell="{ record }">
            <MsCheckbox
              :value="props.selectedKeys.has(record[rowKey || 'id'])"
              @change="rowSelectChange(record[rowKey || 'id'])"
            />
          </template>
        </a-table-column>
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
                  @blur="handleEditInputBlur(record, item.dataIndex as string, true)"
                  @keydown.enter="handleEditInputBlur(record, item.dataIndex as string, false)"
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
                    v-if="
                      item.editType &&
                      editActiveKey !== `${item.dataIndex}${rowIndex}` &&
                      !record.deleted &&
                      !record.internal
                    "
                    class="ml-2 cursor-pointer"
                    :class="{ 'ms-table-edit-active': editActiveKey === rowIndex }"
                    type="icon-icon_edit_outlined"
                    @click="handleEdit(item.dataIndex as string, rowIndex, record)"
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
      :class="{ 'justify-between': showBatchAction }"
    >
      <batch-action
        v-if="showBatchAction"
        :select-row-count="selectCurrent"
        :action-config="props.actionConfig"
        @batch-action="handleBatchAction"
        @clear="emit('clearSelector')"
      />
      <ms-pagination
        v-if="attrs.showPagination"
        v-show="props.selectorStatus !== SelectAllEnum.CURRENT"
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

  import type {
    MsTableProps,
    BatchActionParams,
    BatchActionConfig,
    MsTableColumn,
    MsPaginationI,
    BatchActionQueryParams,
  } from './type';
  import type { TableData } from '@arco-design/web-vue';
  import MsCheckbox from '../ms-checkbox/MsCheckbox.vue';

  const batchleft = ref('10px');
  const { t } = useI18n();
  const tableStore = useTableStore();
  const currentColumns = ref<MsTableColumn>([]);
  const appStore = useAppStore();

  const props = defineProps<{
    selectedKeys: Set<string>;
    excludeKeys: Set<string>;
    selectorStatus: SelectAllEnum;
    actionConfig?: BatchActionConfig;
    noDisable?: boolean;
    showSetting?: boolean;
    columns: MsTableColumn;
    spanMethod?: (params: { record: TableData; rowIndex: number; columnIndex: number }) => void;
  }>();
  const emit = defineEmits<{
    (e: 'batchAction', value: BatchActionParams, queryParams: BatchActionQueryParams): void;
    (e: 'pageChange', value: number): void;
    (e: 'pageSizeChange', value: number): void;
    (e: 'rowNameChange', value: TableData): void;
    (e: 'rowSelectChange', key: string): void;
    (e: 'selectAllChange', value: SelectAllEnum): void;
    (e: 'sorterChange', value: { [key: string]: string }): void;
    (e: 'clearSelector'): void;
  }>();
  const attrs = useAttrs();

  // 全选按钮-总条数
  const selectTotal = computed(() => {
    const { selectorStatus } = props;
    if (selectorStatus === SelectAllEnum.CURRENT) {
      return (attrs.msPagination as MsPaginationI)?.pageSize || appStore.pageSize;
    }
    return (attrs.msPagination as MsPaginationI)?.total || appStore.pageSize;
  });

  // 全选按钮-当前的条数
  const selectCurrent = computed(() => {
    const { selectorStatus, excludeKeys, selectedKeys } = props;
    if (selectorStatus === SelectAllEnum.ALL) {
      return selectTotal.value - excludeKeys.size;
    }
    return selectedKeys.size;
  });

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

  const initColumn = () => {
    let tmpArr: MsTableColumn = [];
    if (props.showSetting) {
      tmpArr = tableStore.getShowInTableColumns(attrs.tableKey as string) || [];
    } else {
      tmpArr = props.columns;
    }
    currentColumns.value = tmpArr;
  };

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

  const showBatchAction = computed(() => {
    return selectCurrent.value > 0 && attrs.selectable;
  });

  const handleBatchAction = (value: BatchActionParams) => {
    const { selectorStatus, selectedKeys, excludeKeys } = props;
    const queryParams: BatchActionQueryParams = {
      selectedIds: Array.from(selectedKeys),
      excludeIds: Array.from(excludeKeys),
      selectAll: selectorStatus === SelectAllEnum.ALL,
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
      // 触发的是Enter
      emit('rowNameChange', record);
      isEnter.value = true;
      editActiveKey.value = '';
    }
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
