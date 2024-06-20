<template>
  <div :class="['ms-base-table', $slots.quickCreate ? 'ms-base-table--hasQuickCreate' : '']">
    <div v-if="$slots.quickCreate" class="ms-base-table-quickCreate">
      <slot name="quickCreate"></slot>
    </div>
    <!-- 表只做自适应不做可拖拽列 -->
    <a-table
      v-bind="{ ...$attrs, ...scrollObj }"
      :row-class="getRowClass"
      :column-resizable="true"
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
              :exclude-keys="Array.from(props.excludeKeys)"
              :current-data="attrs.data as Record<string,any>[]"
              :show-select-all="!!attrs.showPagination && props.showSelectorAll"
              :disabled="(attrs.data as []).length === 0"
              :row-key="rowKey"
              @change="handleSelectAllChange"
            />
          </template>
          <template #cell="{ record }">
            <MsCheckbox
              v-if="attrs.selectorType === 'checkbox'"
              :value="getChecked(record)"
              :indeterminate="getIndeterminate(record)"
              :disabled="isDisabledChildren(record)"
              @click.stop
              @change="rowSelectChange(record)"
            />
            <a-radio
              v-else-if="attrs.selectorType === 'radio'"
              v-model:model-value="record.tableChecked"
              @click.stop
              @change="(val) => handleRadioChange(val as boolean, record)"
            />
            <div v-if="attrs.showPagination && props.showSelectorAll" class="w-[16px]"></div>
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
          :header-cell-class="`${item.filterConfig && hasSelectedFilter(item) ? 'header-cell-filter' : ''} ${
            item.headerCellClass
          }`"
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
            <div
              :class="{
                'flex w-full flex-row flex-nowrap items-center gap-[4px]': !item.align,
              }"
            >
              <slot :name="item.titleSlotName" :column-config="item">
                <div v-if="item.title" class="title-name">
                  {{ t(item.title as string) }}
                </div>
              </slot>
              <columnSelectorIcon
                v-if="
                  props.showSetting &&
                  (item.slotName === SpecialColumnEnum.OPERATION || item.slotName === SpecialColumnEnum.ACTION)
                "
                :table-key="(attrs.tableKey as TableKeyEnum)"
                :is-simple="(attrs.isSimpleSetting as boolean)"
                :only-page-size="!!attrs.onlyPageSize"
                :show-pagination="!!attrs.showPagination"
                @show-setting="handleShowSetting"
                @init-data="handleInitColumn"
                @page-size-change="pageSizeChange"
              />
              <DefaultFilter
                v-else-if="(item.filterConfig && item.filterConfig.options?.length) || item?.filterConfig?.remoteMethod"
                class="ml-[4px]"
                :options="item.filterConfig.options"
                :data-index="item.dataIndex"
                v-bind="item.filterConfig"
                :filter="filterData"
                @handle-confirm="(v) => handleFilterConfirm(v, item.dataIndex as string, item.isCustomParam || false)"
                @click.stop="null"
              >
                <template #item="{ filterItem }">
                  <slot :name="item.filterConfig.filterSlotName" :filter-content="filterItem"> </slot>
                </template>
              </DefaultFilter>
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
                  :tag-position="item.tagPosition"
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
                    editActiveKey === `${record[rowKey || 'id']}` &&
                    item.editType &&
                    item.editType === ColumnEditTypeEnum.INPUT
                  "
                  :ref="(el: any) => setRefMap(el, `${record[rowKey|| 'id']}`)"
                  v-model="record[item.dataIndex as string]"
                  :max-length="255"
                  @click.stop
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
                    @click.stop="handleEdit(item.dataIndex as string, rowIndex, record)"
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
                  {{ getDisplayValue(record[item.dataIndex as string]) }}
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
        <!-- @desc: 这里为了树级别展开折叠如果子级别children不存在不展示展开折叠，所以原本组件的隐藏掉，改成自定义便于控制展示隐藏 -->
        <slot v-if="record.children && record.children.length" name="expand-icon" v-bind="{ expanded, record }">
          <div
            :class="`${
              expanded ? 'bg-[rgb(var(--primary-1))]' : 'bg-[var(--color-text-n8)]'
            } expand-btn-wrapper flex items-center justify-center`"
          >
            <MsIcon v-if="!expanded" :size="8" type="icon-icon_right_outlined" class="text-[var(--color-text-4)]" />
            <MsIcon v-else :size="8" class="text-[rgb(var(--primary-6))]" type="icon-icon_down_outlined" />
          </div>
        </slot>
      </template>

      <!-- 控制拖拽类 -->
      <template #tr="{ record }">
        <tr :class="!record.parent ? 'parent-tr' : 'children-tr'" />
      </template>
    </a-table>
    <div
      v-if="showBatchAction || !!attrs.showPagination"
      id="ms-table-footer-wrapper"
      class="mt-[16px] flex w-full flex-row flex-nowrap items-center overflow-hidden"
      :class="{ 'justify-between': showBatchAction }"
    >
      <span v-if="props.actionConfig && selectedCount > 0 && !showBatchAction" class="title text-[var(--color-text-2)]">
        {{ t('msTable.batch.selected', { count: selectedCount }) }}
        <a-button class="clear-btn ml-[12px] px-2" type="text" @click="emit('clearSelector')">
          {{ t('msTable.batch.clear') }}
        </a-button>
      </span>
      <div class="flex flex-grow items-center">
        <batch-action
          v-if="showBatchAction"
          class="flex-1"
          :select-row-count="selectedCount"
          :action-config="props.actionConfig"
          wrapper-id="ms-table-footer-wrapper"
          :size="props.paginationSize"
          @batch-action="handleBatchAction"
          @clear="() => emit('clearSelector')"
        />
      </div>
      <ms-pagination
        v-if="!!attrs.showPagination"
        :size="props.paginationSize || 'small'"
        v-bind="(attrs.msPagination as MsPaginationI)"
        :simple="!!showBatchAction"
        :show-jumper="(attrs.msPagination as MsPaginationI).total / (attrs.msPagination as MsPaginationI).pageSize > 5"
        @change="pageChange"
        @page-size-change="pageSizeChange"
      />
    </div>
    <ColumnSelector
      v-if="props.showSetting"
      v-model:visible="columnSelectorVisible"
      :show-jump-method="(attrs.showJumpMethod as boolean)"
      :table-key="(attrs.tableKey as TableKeyEnum)"
      :show-pagination="!!attrs.showPagination"
      :show-subdirectory="!!attrs.showSubdirectory"
      @init-data="handleInitColumn"
      @page-size-change="pageSizeChange"
      @module-change="() => emit('moduleChange')"
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
  import { ColumnEditTypeEnum, SelectAllEnum, SpecialColumnEnum, TableKeyEnum } from '@/enums/tableEnum';

  import type {
    BatchActionConfig,
    BatchActionParams,
    BatchActionQueryParams,
    MsPaginationI,
    MsTableColumn,
    MsTableColumnData,
    MsTableDataItem,
    MsTableProps,
  } from './type';
  import { getCurrentRecordChildrenIds } from './utils';
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
    excludeKeys: Set<string>;
    selectorStatus: SelectAllEnum;
    actionConfig?: BatchActionConfig;
    disabledConfig?: {
      disabledChildren?: boolean;
      parentKey?: string;
    };
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
    paginationSize?: 'small' | 'mini' | 'medium' | 'large';
  }>();
  const emit = defineEmits<{
    (e: 'batchAction', value: BatchActionParams, queryParams: BatchActionQueryParams): void;
    (e: 'pageChange', value: number): void;
    (e: 'pageSizeChange', value: number): void;
    (e: 'rowNameChange', value: TableData, cb: (v: boolean) => void): void;
    (e: 'rowSelectChange', record: TableData): void;
    (e: 'selectAllChange', value: SelectAllEnum, onlyCurrent: boolean): void;
    (e: 'dragChange', value: DragSortParams): void;
    (e: 'sorterChange', value: { [key: string]: string }): void;
    (e: 'expand', record: TableData): void | Promise<any>;
    (e: 'cell-click', record: TableData, column: TableColumnData, ev: Event): void | Promise<any>;
    (e: 'clearSelector'): void;
    (
      e: 'filterChange',
      dataIndex: string,
      value: string[] | (string | number | boolean)[] | undefined,
      isCustomParam: boolean
    ): void;
    (e: 'resetFilter', filterValue: Record<string, any>): void;
    (e: 'moduleChange'): void;
    (e: 'initEnd'): void;
    (e: 'reset'): void;
  }>();
  const attrs = useAttrs();

  // 编辑按钮的Active状态
  const editActiveKey = ref<string>('');

  // 编辑项的Ref

  const refMap = ref<Record<string, any>>({});
  const setRefMap = (el: any, id: string) => {
    if (el) {
      refMap.value[id] = el;
    }
  };

  // 编辑项的初始值，用于blur时恢复旧值
  const currentEditValue = ref<string>('');
  // 是否是enter触发
  const isEnter = ref<boolean>(false);

  const { rowKey }: Partial<MsTableProps<any>> = attrs;

  // 显示值 （不处理0）
  function getDisplayValue(value: any) {
    if (value === '' || value === null || value === undefined || Number.isNaN(value)) {
      return attrs.emptyDataShowLine ? '-' : '';
    }
    return value;
  }

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
        tmpArr = await tableStore.getShowInTableColumns(attrs.tableKey as TableKeyEnum);
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
      emit('initEnd');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error('InitColumn failed', error);
    }
  };

  const selectedKey = defineModel<string>('selectedKey', { default: '' }); // 内部维护的单选选中项
  const tempRecord = ref<TableData>({});

  watch(
    () => attrs.data,
    (arr) => {
      if (selectedKey.value && Array.isArray(arr) && arr.length > 0) {
        arr = arr.map((item: TableData) => {
          if (item.id === selectedKey.value) {
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
      selectedKey.value = record[rowKey as string];
      record.tableChecked = true;
      tempRecord.value.tableChecked = false;
      tempRecord.value = record;
    }
  }

  // 全选change事件
  const handleSelectAllChange = (v: SelectAllEnum, onlyCurrent: boolean) => {
    emit('selectAllChange', v, onlyCurrent);
  };
  // 行选择器change事件
  const rowSelectChange = (record: TableData) => {
    emit('rowSelectChange', record);
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
    return selectedCount.value > 0 && !!attrs.selectable && props.actionConfig;
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
      refMap.value[record[rowKey || 'id']] = null;
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

  function getCurrentList(data: TableData[], key: string, id: string) {
    return data.find((item) => {
      const currentChildrenIds = (item.children || []).map((e) => e[key]);
      if (currentChildrenIds?.includes(id)) {
        return true;
      }
      return false;
    });
  }

  // 拖拽排序
  const handleDragChange = (data: TableData[], extra: TableChangeExtra, currentData: TableData[]) => {
    if (!currentData || currentData.length === 1) {
      return;
    }

    if (extra && extra.dragTarget?.id) {
      let newDragData: TableData[] = data;
      let oldDragData: TableData[] = currentData;

      const newDragItem = getCurrentList(data, 'id', extra.dragTarget.id);
      const oldDragItem = getCurrentList(currentData, 'key', extra.dragTarget.id);

      if (newDragItem && newDragItem.children && oldDragItem && oldDragItem.children) {
        newDragData = newDragItem.children;
        oldDragData = oldDragItem.children;
      }

      let oldIndex = 0;
      let newIndex = 0;

      newIndex = newDragData.findIndex((item: any) => item.id === extra.dragTarget?.id);
      oldIndex = oldDragData.findIndex((item: any) => item.key === extra.dragTarget?.id);
      let position: 'AFTER' | 'BEFORE' = 'BEFORE';

      position = newIndex > oldIndex ? 'AFTER' : 'BEFORE';
      const params: DragSortParams = {
        projectId: appStore.currentProjectId,
        targetId: '', // 放置目标id
        moveMode: position,
        moveId: extra.dragTarget.id as string, // 拖拽id
      };

      let targetIndex;
      if (position === 'AFTER' && newIndex > 0) {
        targetIndex = newIndex - 1;
      } else if (position === 'AFTER') {
        params.moveMode = 'BEFORE';
        targetIndex = newIndex + 1;
      } else if (position === 'BEFORE' && newIndex < newDragData.length - 1) {
        targetIndex = newIndex + 1;
      } else {
        params.moveMode = 'AFTER';
        targetIndex = newIndex - 1;
      }
      params.targetId = newDragData[targetIndex]?.id ?? newDragData[newIndex]?.id;

      emit('dragChange', params);
    }
  };

  // 编辑单元格的input
  const handleEdit = (dataIndex: string, rowIndex: number, record: TableData) => {
    editActiveKey.value = record.id;
    currentEditValue.value = record[dataIndex];
    const refKey = `${record[rowKey as string]}`;
    if (refMap.value[refKey]) {
      refMap.value[refKey]?.focus();
    } else {
      nextTick(() => {
        refMap.value[refKey]?.focus();
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

  const filterData = computed(() => {
    return (attrs.filter || {}) as Record<string, any>;
  });

  const handleFilterConfirm = (
    value: string[] | (string | number | boolean)[] | undefined,
    dataIndex: string,
    isCustomParam: boolean
  ) => {
    emit('filterChange', dataIndex, value, isCustomParam);
  };

  function getChecked(record: TableData) {
    if (!record.children) {
      return props.selectedKeys.has(record[rowKey || 'id']);
    }

    const childKeyIds = getCurrentRecordChildrenIds(record.children, rowKey || 'id');
    return childKeyIds.every((key) => props.selectedKeys.has(key));
  }

  function getIndeterminate(record: TableData) {
    if (!record.children) {
      return false;
    }
    const childKeyIds = getCurrentRecordChildrenIds(record.children, rowKey || 'id');
    // 判断是否有被选中的元素
    const isSomeSelected = childKeyIds.some((key) => props.selectedKeys.has(key));
    // 判断是否所有元素都被选中
    const isEverySelected = childKeyIds.every((key) => props.selectedKeys.has(key));

    if (isSomeSelected && !isEverySelected) {
      return true;
    }
    return false;
  }

  function isDisabledChildren(record: TableData) {
    if (!props.disabledConfig?.disabledChildren) {
      return false;
    }
    // 子级禁用
    return !!record[props.disabledConfig.parentKey || 'parent'];
  }

  onMounted(async () => {
    await initColumn();
    batchLeft.value = getBatchLeft();
  });

  function hasSelectedFilter(item: MsTableColumnData) {
    if (item.filterConfig && item.dataIndex) {
      return (filterData.value[item.dataIndex] || []).length > 0;
    }
    return false;
  }

  watch(
    () => props.columns,
    () => {
      initColumn();
    }
  );

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
    :deep(.arco-table-cell) {
      padding: 8px 16px;
    }
    :deep(.arco-table-td) {
      line-height: normal;
      height: 48px;
    }
    :deep(.arco-table-size-mini) {
      .arco-table-td {
        height: 36px;
      }
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
    :deep(.arco-table-th):hover {
      .arco-table-column-handle {
        @apply inline-block;

        top: 50%;
        margin: 0 3px;
        padding: 4px 0;
        width: 2px;
        height: 24px;
        background-color: var(--color-text-n8);
        transform: translateY(-50%);
      }
    }
    .setting-icon {
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
      padding-top: 50px;
    }
    :deep(.arco-table-element:not(.arco-table-header .arco-table-element)) {
      padding-bottom: 50px;
      tbody {
        transform: translateY(50px);
      }
    }
    :deep(.arco-table-tr:first-child) {
      .arco-table-td {
        border-top: 1px solid var(--color-text-n8);
      }
    }
    .ms-base-table-quickCreate {
      @apply absolute left-0 flex w-full items-center;

      top: 39px;
      z-index: 11;
      padding: 14px 16px;
      background-color: var(--color-text-n9);
    }
  }
  :deep(.arco-table-operation) {
    .arco-table-td-content {
      @apply justify-center;
    }
    .arco-table-cell {
      padding: 0;
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
    background: transparent;
    .expand-btn-wrapper {
      width: 16px;
      height: 16px;
      border: none;
      border-radius: 50%;
      // background: var(--color-text-n8);
    }
  }
  :deep(.arco-table .arco-table-expand-btn:hover) {
    border-color: transparent;
    background: transparent;
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
    @apply !p-0;

    margin: 8px 16px;
    &:hover {
      @apply bg-white;
    }
    .arco-table-sorter {
      .arco-table-sorter-icon {
        &::before {
          content: '';
          position: absolute;
          z-index: 1;
          width: 14px;
          height: 8px;
        }
        &:nth-of-type(1) {
          &::before {
            background: url('@/assets/svg/icons/chevronUp.svg') center/cover;
          }
          &.arco-table-sorter-icon-active::before {
            background: url('@/assets/svg/icons/chevronUpActive.svg') center/cover;
          }
        }
        &:nth-of-type(2) {
          &::before {
            background: url('@/assets/svg/icons/chevronDown.svg') center/cover;
          }
          &.arco-table-sorter-icon-active::before {
            background: url('@/assets/svg/icons/chevronDownActive.svg') center/cover;
          }
        }
        svg {
          display: none;
        }
      }
    }
  }
  :deep(.arco-table-col-sorted) .arco-table-cell-with-sorter {
    width: fit-content;
    border-radius: var(--border-radius-small);
    background: rgb(var(--primary-1)) content-box;
    .arco-table-th-title {
      .title-name {
        color: rgb(var(--primary-5));
      }
    }
  }
  :deep(.header-cell-filter) {
    .arco-table-cell-with-filter {
      float: left;
      .arco-table-th-title {
        border-radius: 2px;
        background: rgb(var(--primary-1)) content-box;
        .filter-icon {
          color: rgb(var(--primary-5)) !important;
        }
        .title-name {
          color: rgb(var(--primary-5));
        }
      }
    }
  }
  :deep(.arco-table-th-title) {
    .title-name {
      @apply break-keep;

      color: var(--color-text-3);
    }
  }
</style>

<style lang="less">
  .arco-table-filters-content {
    @apply overflow-hidden;

    max-width: 300px;
    .arco-table-filters-content-list {
      @apply overflow-y-auto;
      .ms-scroll-bar();

      max-height: 400px;
    }
    .arco-checkbox-group {
      @apply flex w-full flex-col;
      .arco-checkbox {
        @apply w-full;
        .arco-checkbox-label {
          @apply flex-1 overflow-hidden;
        }
      }
    }
  }
</style>
