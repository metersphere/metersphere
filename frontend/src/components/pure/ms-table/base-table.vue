<template>
  <div class="ms-base-tale">
    <select-all
      v-if="attrs.showSelectAll"
      class="custom-action"
      :total="selectTotal"
      :current="selectCurrent"
      @change="handleChange"
    />
    <a-table
      v-bind="$attrs"
      :row-class="getRowClass"
      :selected-keys="props.selectedKeys"
      @selection-change="(e) => selectionChange(e, true)"
    >
      <template #columns>
        <a-table-column
          v-for="(item, idx) in columns"
          :key="idx"
          :width="item.width"
          :align="item.align"
          :fixed="item.fixed"
          :ellipsis="item.ellipsis"
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
            <div v-if="attrs.showSetting && idx === columns.length - 1" class="column-selector">
              <div class="title">{{ t(item.title as string) }}</div>
              <ColumnSelector :table-key="(attrs.tableKey as string)" @close="handleColumnSelectorClose" />
            </div>
            <slot v-else-if="item.titleSlotName" :name="item.titleSlotName" />
            <div v-else class="title">{{ t(item.title as string) }}</div>
          </template>
          <template #cell="{ column, record, rowIndex }">
            <div class="flex flex-row items-center">
              <template v-if="item.dataIndex === SpecialColumnEnum.ENABLE">
                <div v-if="record.enable" class="flex items-center">
                  <icon-check-circle-fill class="mr-[2px] text-[rgb(var(--success-6))]" />
                  {{ t('system.user.tableEnable') }}
                </div>
                <div v-else class="flex items-center text-[var(--color-text-4)]">
                  <MsIcon type="icon-icon_disable" class="mr-[2px]" />
                  {{ t('system.user.tableDisable') }}
                </div>
              </template>
              <template v-else>
                <a-input
                  v-if="editActiveKey === rowIndex && item.dataIndex === editKey"
                  ref="currentInputRef"
                  v-model="record[item.dataIndex as string]"
                  @blur="handleEditInputBlur()"
                  @press-enter="handleEditInputEnter(record)"
                />
                <slot v-else :name="item.slotName" v-bind="{ record, rowIndex, column }">
                  <span>{{ record[item.dataIndex as string] }}</span>
                </slot>
                <MsIcon
                  v-if="item.editable && item.dataIndex === editKey"
                  class="ml-2 cursor-pointer"
                  :class="{ 'ms-table-edit-active': editActiveKey === rowIndex }"
                  type="icon-icon_edit_outlined"
                  @click="handleEdit(rowIndex)"
                />
              </template>
            </div>
          </template>
        </a-table-column>
      </template>
    </a-table>
    <div
      class="flex h-[64px] w-[100%] flex-row flex-nowrap items-center justify-end px-0 py-4"
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
        @change="pageChange"
        @page-size-change="pageSizeChange"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';
  import { useAttrs, computed, ref, onMounted } from 'vue';
  import { useAppStore, useTableStore } from '@/store';
  import selectAll from './select-all.vue';
  import {
    MsTableProps,
    SelectAllEnum,
    MsPaginationI,
    BatchActionParams,
    BatchActionConfig,
    MsTableColumn,
    SpecialColumnEnum,
  } from './type';
  import BatchAction from './batchAction.vue';
  import MsPagination from '@/components/pure/ms-pagination/index';
  import type { TableData } from '@arco-design/web-vue';
  import ColumnSelector from './columnSelector.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  const batchleft = ref('10px');
  const { t } = useI18n();
  const tableStore = useTableStore();
  const appStore = useAppStore();
  const columns = ref<MsTableColumn>([]);

  const props = defineProps<{
    selectedKeys?: (string | number)[];
    actionConfig?: BatchActionConfig;
    noDisable?: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'selectedChange', value: (string | number)[]): void;
    (e: 'batchAction', value: BatchActionParams): void;
    (e: 'pageChange', value: number): void;
    (e: 'pageSizeChange', value: number): void;
    (e: 'rowNameChange', value: TableData): void;
  }>();
  const isSelectAll = ref(false);
  const attrs = useAttrs();
  // 全选按钮-当前的条数
  const selectCurrent = ref(0);
  // 全选按钮-总条数
  const selectTotal = ref(0);
  // 编辑按钮的Active状态
  const editActiveKey = ref(-1);
  // 编辑input的Ref
  const currentInputRef = ref(null);
  const { rowKey, editKey }: Partial<MsTableProps> = attrs;

  const setSelectAllTotal = (isAll: boolean) => {
    const { data, msPagination }: Partial<MsTableProps> = attrs;
    if (isAll) {
      selectTotal.value = msPagination?.total || data?.length || appStore.pageSize;
    } else {
      selectTotal.value = data ? data.length : appStore.pageSize;
    }
  };

  const initColumn = () => {
    columns.value = tableStore.getShowInTableColumns(attrs.tableKey as string);
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
    const { data, msPagination }: Partial<MsTableProps> = attrs;
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
    editActiveKey.value = -1;
    emit('rowNameChange', record);
  };

  const handleEditInputBlur = () => {
    editActiveKey.value = -1;
  };

  // 编辑单元格的input
  const handleEdit = (rowIndex: number) => {
    editActiveKey.value = rowIndex;
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
  .ms-base-tale {
    position: relative;
    .custom-action {
      position: absolute;
      top: 3px;
      left: v-bind(batchleft);
      z-index: 99;
      border-radius: 2px;
      line-height: 40px;
      cursor: pointer;
    }
    .column-selector {
      display: flex;
      flex-flow: row nowrap;
      align-items: center;
    }
    .title {
      color: var(--color-text-3);
    }
    .batch-action {
      justify-content: flex-start;
    }
    .pop-title {
      color: var(--color-text-1);
    }
    .ms-table-edit-active {
      color: rgb(var(--primary-5));
    }
  }
</style>
