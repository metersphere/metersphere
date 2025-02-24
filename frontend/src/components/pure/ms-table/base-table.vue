<template>
  <div :class="['ms-base-table', $slots.quickCreate ? 'ms-base-table--hasQuickCreate' : '']">
    <div v-if="$slots.quickCreate" class="ms-base-table-quickCreate">
      <slot name="quickCreate"></slot>
    </div>
    <a-table
      v-bind="{ ...$attrs, ...scrollObj }"
      v-model:selected-keys="originalSelectedKeys"
      v-model:expanded-keys="expandedKeys"
      :row-class="getRowClass"
      :column-resizable="true"
      :span-method="spanMethod"
      :columns="currentColumns"
      :span-all="props.spanAll"
      @select="originalRowSelectChange"
      @select-all="originalSelectAll"
      @expand="(rowKey, record) => emit('expand', record)"
      @change="(data: TableData[], extra: TableChangeExtra, currentData: TableData[]) => handleDragChange(data, extra, currentData)"
      @sorter-change="(dataIndex: string,direction: string) => handleSortChange(dataIndex, direction)"
      @cell-click="(record: TableData,column: TableColumnData,ev: Event) => emit('cellClick',record, column,ev)"
      @column-resize="columnResize"
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
              :row-selection-disabled-config="attrs.rowSelectionDisabledConfig as MsTableRowSelectionDisabledConfig"
              @change="handleSelectAllChange"
            />
          </template>
          <template #cell="{ record }">
            <template v-if="!isDisabledChildren(record)">
              <MsCheckbox
                v-if="attrs.selectorType === 'checkbox'"
                :value="getChecked(record)"
                :disabled="getDisabled(record)"
                :indeterminate="getIndeterminate(record)"
                @click.stop
                @change="rowSelectChange(record)"
              />
              <a-radio
                v-else-if="attrs.selectorType === 'radio'"
                v-model:model-value="record.tableChecked"
                @click.stop
                @change="(val) => handleRadioChange(val as boolean, record)"
              />
            </template>
            <div v-if="attrs.showPagination && props.showSelectorAll" class="w-[16px]"></div>
          </template>
        </a-table-column>
        <!-- 最小宽度200+16的内边距 -->
        <a-table-column
          v-for="(item, idx) in currentColumns"
          :key="idx"
          :width="item.isTag || item.isStringTag ? 216 : item.width"
          :min-width="item.isTag || item.isStringTag ? 216 : 80"
          :align="item.align"
          :fixed="item.fixed"
          :sortable="item.sortable"
          :filterable="item.filterable"
          :cell-class="item.isTag || item.isStringTag ? `ms-tag-cell-class ${item.cellClass || ''}` : item.cellClass"
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
                :is-hidden-setting="!!attrs.isHiddenSetting"
                @show-setting="handleShowSetting"
                @init-data="handleInitColumn"
                @page-size-change="pageSizeChange"
              />
              <slot
                v-else-if="
                  !props.notShowTableFilter &&
                  ((item.filterConfig && item.filterConfig.options?.length) || item?.filterConfig?.remoteMethod)
                "
                name="columnFilter"
                :column="item"
              >
                <DefaultFilter
                  v-model:checked-list="item.filterCheckedList"
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
              </slot>
            </div>
          </template>
          <template #cell="{ column, record, rowIndex }">
            <div :class="{ 'flex w-full flex-row items-center': !item.isTag && !item.align }">
              <template v-if="item.dataIndex === SpecialColumnEnum.ENABLE">
                <slot name="enable" v-bind="{ record }">
                  <a-switch
                    v-model:model-value="record.enable"
                    size="small"
                    :disabled="!hasAnyPermission(item.permission)"
                    :before-change="(val) => handleChangeEnable(val, record)"
                  />
                </slot>
              </template>
              <template v-else-if="item.isTag || item.isStringTag">
                <slot :name="item.slotName" v-bind="{ record, rowIndex, column, columnConfig: item }">
                  <MsTagGroup
                    :is-string-tag="item.isStringTag"
                    :tag-list="record[item.dataIndex as string]"
                    :type="item.tagPrimary || 'primary'"
                    theme="outline"
                    show-table
                    :tag-position="item.tagPosition"
                  />
                </slot>
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
                  position="tl"
                  content-class="max-w-[400px]"
                  :content="String(record[item.dataIndex as string])"
                  :disabled="record[item.dataIndex as string] === '' || record[item.dataIndex as string] === undefined || record[item.dataIndex as string] === null"
                >
                  <div class="one-line-text w-full">
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
        <slot
          v-if="(record.children && record.children.length) || $attrs.expandable"
          name="expand-icon"
          v-bind="{ expanded, record }"
        >
          <div
            :class="`${
              expanded ? 'expanded-border bg-[rgb(var(--primary-1))]' : 'not-expanded-border bg-[var(--color-text-n8)]'
            } expand-btn-wrapper flex items-center justify-center`"
          >
            <MsIcon v-if="!expanded" :size="8" type="icon-icon_right_outlined" class="text-[var(--color-text-4)]" />
            <MsIcon v-else :size="8" class="text-[rgb(var(--primary-5))]" type="icon-icon_down_outlined" />
          </div>
        </slot>
      </template>
    </a-table>
    <div
      v-if="showBatchAction || !!attrs.showPagination || alwaysShowSelectedCount"
      id="ms-table-footer-wrapper"
      class="mt-[16px] flex w-full flex-row flex-nowrap items-center overflow-hidden"
      :class="{ 'justify-between': showBatchAction }"
    >
      <span v-if="props.actionConfig && selectedCount > 0 && !showBatchAction" class="title text-[var(--color-text-2)]">
        <slot name="count">
          {{ t('msTable.batch.selected', { count: selectedCount }) }}
        </slot>
        <a-button class="clear-btn ml-[12px] px-2" type="text" @click="emit('clearSelector')">
          {{ t('msTable.batch.clear') }}
        </a-button>
      </span>
      <div class="flex flex-grow items-center">
        <batch-action
          v-if="showBatchAction || alwaysShowSelectedCount"
          class="flex-1"
          :select-row-count="selectedCount"
          :action-config="props.actionConfig"
          wrapper-id="ms-table-footer-wrapper"
          :size="props.paginationSize"
          @batch-action="handleBatchAction"
          @clear="() => emit('clearSelector')"
        >
          <template #count>
            <slot name="count"></slot>
          </template>
        </batch-action>
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
  import { Message } from '@arco-design/web-vue';
  import { debounce, throttle } from 'lodash-es';

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
  import { hasAnyPermission } from '@/utils/permission';

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
    MsTableRowSelectionDisabledConfig,
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
    rowClass?: string | any[] | Record<string, any> | ((record: TableData, rowIndex: number) => any);
    spanAll?: boolean;
    showSelectorAll?: boolean;
    firstColumnWidth?: number; // 选择、拖拽列的宽度
    paginationSize?: 'small' | 'mini' | 'medium' | 'large';
    alwaysShowSelectedCount?: boolean; // 是否总是保持显示已选项
    notShowTableFilter?: boolean; // 不显示表头筛选
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
    (e: 'cellClick', record: TableData, column: TableColumnData, ev: Event): void | Promise<any>;
    (e: 'clearSelector'): void;
    (e: 'enableChange', record: any, newValue: string | number | boolean): void;
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
    (e: 'select', rowKeys: (string | number)[], _rowKey: string | number, record: TableData): void;
    (e: 'selectAll', checked: boolean): void;
  }>();
  const attrs = useAttrs();

  const expandedKeys = defineModel<string[]>('expandedKeys', { default: [] });
  const originalSelectedKeys = defineModel<(string | number)[]>('originalSelectedKeys', { default: [] });

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
  // TODO 第一个方案的标签列优化，暂时不删除，会影响排序问题暂不用
  // const columnLastWidthMap = ref<Record<string, any>>({});

  // function getColumnTagLastWidthMap(column: MsTableColumnData) {
  //   const editTgaMinColumnWidth = 200;
  //   // 如果是编辑列标签则最小宽度保留200
  //   if (column?.allowEditTag) {
  //     if (columnLastWidthMap.value[column.dataIndex as string] < editTgaMinColumnWidth) {
  //       return editTgaMinColumnWidth;
  //     }
  //     return columnLastWidthMap.value[column.dataIndex as string];
  //   }
  //   return columnLastWidthMap.value[column.dataIndex as string];
  // }

  // // 获取单个标签的宽度
  // const getTagWidth = (tag: Record<string, any>, lastText: string) => {
  //   const maxTagWidth = 144; // 单个标签最大宽度
  //   const spanPadding = 8; // 标签内边距
  //   const spanBorder = 2; // 标签边框
  //   const marginRight = 4; // 标签之间的间距
  //   const fillWidth = 8; // 填充宽度

  //   const el = document.createElement('div');
  //   el.style.visibility = 'hidden';
  //   el.style.position = 'absolute';
  //   el.style.fontSize = '12px';
  //   // 最后一个标签显示为 +n，按照+n的宽度来计算
  //   el.textContent = lastText || tag.name || tag;

  //   document.body.appendChild(el);
  //   let width = el.offsetWidth + spanPadding * 2 + spanBorder;

  //   // 衡量宽度后将元素移除
  //   document.body.removeChild(el);
  //   width = width > maxTagWidth ? maxTagWidth + marginRight + fillWidth : width + marginRight + fillWidth;
  //   return width;
  // };

  // // 获取所有行里边对应列标签宽度总和
  // const getRowTagTotalWidth = (tags: TableData[]) => {
  //   const maxShowTagCount = 3; // 包含数字标签最多展示3个
  //   const tablePadding = 24; // 表单元格内边距总和

  //   const tagArr = tags.length > maxShowTagCount ? tags.slice(0, maxShowTagCount) : tags;
  //   const totalWidth = tagArr.reduce((acc, tag, index) => {
  //     const lastText = index === maxShowTagCount - 1 ? `+${tags.length - maxShowTagCount - 1}` : '';
  //     const width = getTagWidth(tag, lastText);
  //     return acc + width;
  //   }, 0);

  //   return totalWidth + tablePadding;
  // };

  // const getAllTagsFromData = (rows: TableData[], dataIndex: string): any[] => {
  //   const allTags: Record<string, any>[] = [];

  //   const collectTags = (nodes: TableData[]) => {
  //     nodes.forEach((node) => {
  //       if (node[dataIndex]) {
  //         allTags.push(node[dataIndex]);
  //       }
  //       if (node.children && node.children.length > 0) {
  //         collectTags(node.children);
  //       }
  //     });
  //   };

  //   collectTags(rows);
  //   return allTags;
  // };

  // // TODO 求总和里边最大宽度作为标签列宽 这里需要考虑一下性能优化
  // const getMaxRowTagWidth = (rows: TableData[], dataIndex: string) => {
  //   const allTags = getAllTagsFromData(rows, dataIndex);
  //   const rowWidths = (allTags || []).map((tags: any) => {
  //     return getRowTagTotalWidth(tags);
  //   });
  //   // 确保返回非负值
  //   return Math.max(...rowWidths, 0);
  // };

  // watch(
  //   () => attrs.data,
  //   (val) => {
  //     if (val) {
  //       const minTagWidth = 60; // 确保标签行没有标签标题正常展示宽度最少为60
  //       currentColumns.value.forEach((column) => {
  //         const dataIndex = column.dataIndex as string;
  //         if (column.isTag || column.isStringTag) {
  //           const lastWidth = getMaxRowTagWidth((val as TableData[]) || [], dataIndex);
  //           columnLastWidthMap.value[dataIndex] = lastWidth < minTagWidth ? minTagWidth : lastWidth;
  //         }
  //       });
  //     }
  //   }
  // );

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
  // 表格原生行选择器change事件
  function originalRowSelectChange(rowKeys: (string | number)[], _rowKey: string | number, record: TableData) {
    emit('select', rowKeys, _rowKey, record);
  }

  // 表格原生选择全部事件
  function originalSelectAll(checked: boolean) {
    emit('selectAll', checked);
  }

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
      const currentChildrenIds = (item.children || []).map((e: any) => e[key]);
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

    if (extra && extra.dragTarget?.[rowKey || 'id']) {
      let newDragData: TableData[] = data;
      let oldDragData: TableData[] = attrs.data as TableData[]; // attrs.data 在这仍保留了原本数据顺序

      const newDragItem = getCurrentList(data, rowKey || 'id', extra.dragTarget[rowKey || 'id']);
      const oldDragItem = getCurrentList(oldDragData, rowKey || 'id', extra.dragTarget[rowKey || 'id']);

      if (newDragItem && newDragItem.children && oldDragItem && oldDragItem.children) {
        newDragData = newDragItem.children;
        oldDragData = oldDragItem.children;
      }

      let oldIndex = 0;
      let newIndex = 0;

      newIndex = newDragData.findIndex((item: any) => item[rowKey || 'id'] === extra.dragTarget?.[rowKey || 'id']);
      oldIndex = oldDragData.findIndex((item: any) => item[rowKey || 'id'] === extra.dragTarget?.[rowKey || 'id']);
      let position: 'AFTER' | 'BEFORE' = 'BEFORE';
      position = newIndex > oldIndex ? 'AFTER' : 'BEFORE';
      const params: DragSortParams = {
        projectId: appStore.currentProjectId,
        targetId: '', // 放置目标id
        moveMode: position,
        moveId: extra.dragTarget[rowKey || 'id'] as string, // 拖拽id
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
      params.targetId = newDragData[targetIndex]?.[rowKey || 'id'] ?? newDragData[newIndex]?.[rowKey || 'id'];
      // 只有一个的时候不允许拖拽放置参数报错
      if (newDragData.length > 1) {
        emit('dragChange', params);
      }
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

  function handleChangeEnable(newValue: string | number | boolean, record: TableData) {
    emit('enableChange', record, newValue);
    return false;
  }

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
    emit('sorterChange', {});
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
    value: string[] | (string | number | boolean)[] | undefined,
    dataIndex: string,
    isCustomParam: boolean
  ) => {
    emit('filterChange', dataIndex, value, isCustomParam);
  };

  function isDisabledChildren(record: TableData) {
    if (!(attrs.rowSelectionDisabledConfig as MsTableRowSelectionDisabledConfig)?.disabledChildren) {
      return false;
    }
    // 子级禁用
    return !!record[(attrs.rowSelectionDisabledConfig as MsTableRowSelectionDisabledConfig).parentKey || 'parent'];
  }

  function getChecked(record: TableData) {
    if (
      !record.children ||
      (attrs.rowSelectionDisabledConfig as MsTableRowSelectionDisabledConfig)?.disabledChildren ||
      !(attrs.rowSelectionDisabledConfig as MsTableRowSelectionDisabledConfig)?.checkStrictly
    ) {
      return props.selectedKeys.has(record[rowKey || 'id']);
    }

    const childKeyIds = getCurrentRecordChildrenIds(record.children, rowKey || 'id');
    return childKeyIds.every((key) => props.selectedKeys.has(key));
  }

  function getIndeterminate(record: TableData) {
    // 如果不是父子级关联关系不呈现半选状态
    const { rowSelectionDisabledConfig } = attrs;
    if (!(rowSelectionDisabledConfig as MsTableRowSelectionDisabledConfig)?.checkStrictly) {
      return false;
    }
    if (!record.children || (attrs.rowSelectionDisabledConfig as MsTableRowSelectionDisabledConfig)?.disabledChildren) {
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

  function getDisabled(record: TableData) {
    const disableKey = (attrs?.rowSelectionDisabledConfig as MsTableRowSelectionDisabledConfig)?.disabledKey;
    return disableKey ? record[disableKey] : false;
  }

  const filterData = computed(() => {
    return { ...(attrs.filter || {}) } as Record<string, any>;
  });

  function hasSelectedFilter(item: MsTableColumnData) {
    if (item.filterConfig && item.dataIndex) {
      return (filterData.value[item.dataIndex] || []).length > 0;
    }
    return false;
  }
  // TODO 方案二（试行）： 通过样式获取来控制自适应调整列宽，展示标签显示数量 目前没有测到有影响的地方
  // 单个标签列的可见性处理
  function updateTagVisibility(cell: HTMLElement) {
    const labelsGroupList = cell.querySelectorAll<HTMLElement>('.ms-tag-group') || [];
    const moreTipsNumber = cell.querySelector<HTMLElement>('.ms-tag-num');

    const availableWidth = cell.clientWidth; // 获取当前列的可用宽度
    let totalWidth = 0;
    let hiddenTags = 0;
    const marginRight = 3;
    const visibleTags: HTMLElement[] = [];
    labelsGroupList.forEach((label: HTMLElement) => {
      label.style.display = 'inline-block'; // 重置标签的显示状态
      totalWidth += label.offsetWidth + marginRight; // 计算当前标签的总宽度
      // 暂时不隐藏标签，先收集显示的标签
      visibleTags.push(label);

      if (moreTipsNumber && totalWidth + moreTipsNumber.offsetWidth > availableWidth) {
        // 需要隐藏的标签数量不能少于1个
        if (visibleTags.length > 1) {
          // 隐藏超出的标签
          label.style.display = 'none';
          hiddenTags++;
        } else {
          // 如果可见标签少于等于1个，强制显示
          label.style.display = 'inline-block';
        }
      }
    });

    if (hiddenTags > 0 && moreTipsNumber) {
      moreTipsNumber.textContent = `+${hiddenTags}`;
      moreTipsNumber.style.display = 'inline-block'; // 显示隐藏数量
    } else if (moreTipsNumber) {
      moreTipsNumber.style.display = 'none'; // 没有隐藏的标签时隐藏数量标签
    }
  }

  // 更新所有行的标签可见性
  const updateAllTagVisibility = () => {
    const cellsTagGroupList = document.querySelectorAll<HTMLElement>('.tag-group-class') || [];
    cellsTagGroupList.forEach((cell: HTMLElement) => {
      updateTagVisibility(cell);
    });
  };

  function initDefaultFilter() {
    currentColumns.value.forEach((e) => {
      const dataIndexKey = e.dataIndex as string;
      // 初始化配置了filterConfig默认值回显已选项
      if (e.filterConfig?.options && filterData.value[dataIndexKey] && filterData.value[dataIndexKey].length) {
        e.filterCheckedList = filterData.value[dataIndexKey];
      }
    });
  }

  onMounted(async () => {
    await initColumn();
    updateAllTagVisibility();
    batchLeft.value = getBatchLeft();
    initDefaultFilter();
  });

  const updateColumnWidth = throttle(async (dataIndex: string, width: number) => {
    if (dataIndex) {
      const [index] = dataIndex.split('_').slice(-1);
      const lastIndex = attrs.selectable ? Number(index) - 1 : Number(index);

      if (lastIndex > -1) {
        currentColumns.value[lastIndex].width = width;
        await tableStore.updateColumnWidth(attrs.tableKey as TableKeyEnum, currentColumns.value);
      }
    }
  }, 200);

  function columnResize(dataIndex: string, width: number) {
    if (dataIndex) {
      updateColumnWidth(dataIndex, width);
      updateAllTagVisibility();
    }
  }
  let lastWindowWidth = window.innerWidth;

  const updateTagAdaptive = debounce(() => {
    const currentWidth = window.innerWidth;

    // 仅在屏幕尺寸宽度发生变化时重新适应
    if (currentWidth !== lastWindowWidth) {
      lastWindowWidth = currentWidth;
      updateAllTagVisibility();
    }
  }, 100);

  watch(
    () => attrs.data,
    (val) => {
      if (val) {
        nextTick(() => {
          updateAllTagVisibility();
        });
      }
    }
  );

  onMounted(() => {
    // 监听窗口大小变化
    window.addEventListener('resize', updateTagAdaptive);
  });

  // 组件卸载时移除监听器
  onBeforeUnmount(() => {
    window.removeEventListener('resize', updateTagAdaptive);
  });

  watch(
    () => props.columns,
    () => {
      initColumn();
    }
  );

  watch(
    () => props.notShowTableFilter,
    (val: boolean) => {
      if (val) {
        // 表格基础筛选条件清空选中
        currentColumns.value.forEach((column) => {
          column.filterCheckedList = [];
        });
      }
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
    :deep(.arco-table-header) {
      background-color: var(--color-text-fff);
      .arco-table-th {
        background-color: var(--color-text-fff);
      }
    }
    :deep(.arco-table-body) {
      background-color: var(--color-text-fff);
      .arco-table-td {
        border-bottom: 1px solid var(--color-text-n8);
        color: var(--color-text-1);
        background-color: var(--color-text-fff);
      }
    }
    :deep(.arco-table-border .arco-table-scroll-y) {
      border-right: 1px solid var(--color-text-n8) !important;
      border-bottom: 1px solid var(--color-text-n8) !important;
    }
    :deep(.arco-table-border .arco-table-tr .arco-table-th) {
      border-bottom: 1px solid var(--color-text-n8) !important;
    }
    :deep(.arco-table-tr):hover {
      background-color: rgb(var(--primary-1)) !important;
      .arco-table-td,
      .arco-table-td.arco-table-col-fixed-left::before,
      .arco-table-td.arco-table-col-fixed-right::before {
        background-color: rgb(var(--primary-1)) !important;
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
        background-color: var(--color-text-n9);
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
      background-color: var(--color-text-fff);
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
  :deep(.arco-table-col-fixed-left.arco-table-operation) {
    span.arco-table-column-handle {
      pointer-events: none;
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
      background: var(--color-text-fff);
    }
  }
  .not-expanded-border {
    border: 1px solid var(--color-text-n8) !important;
  }
  .expanded-border {
    border: 1px solid rgb(var(--primary-5)) !important;
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
    background: var(--color-text-fff);
  }
  :deep(.arco-table-cell-with-sorter) {
    @apply !p-0;

    margin: 8px 16px;
    &:hover {
      background: var(--color-text-fff);
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
        padding-left: 4px;
        border-radius: 2px 0 0 2px;
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
          padding-left: 4px;
          border-radius: 2px 0 0 2px;
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
  :deep(.arco-table-td.ms-tag-cell-class) {
    .arco-table-td-content {
      > div {
        width: 100%;
      }
    }
  }
</style>

<style lang="less">
  .arco-table-filters-content {
    @apply overflow-hidden;

    max-width: 300px;
    border-color: var(--color-text-n8);
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
