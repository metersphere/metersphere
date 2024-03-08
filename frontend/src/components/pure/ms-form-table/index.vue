<template>
  <MsBaseTable
    v-bind="propsRes"
    :hoverable="false"
    no-disable
    is-simple-setting
    :span-method="props.spanMethod"
    v-on="propsEvent"
  >
    <template
      v-for="item of props.columns.filter((e) => e.slotName !== undefined)"
      #[item.slotName!]="{ record, rowIndex, column }"
    >
      <slot :name="item.slotName" v-bind="{ record, rowIndex, column, dataIndex: item.dataIndex, columnConfig: item }">
        {{ record[item.dataIndex as string] || '-' }}
      </slot>
    </template>
  </MsBaseTable>
</template>

<script setup lang="ts">
  import { TableColumnData, TableData } from '@arco-design/web-vue';

  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumnData } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import useTableStore from '@/hooks/useTableStore';

  import { SelectAllEnum, TableKeyEnum } from '@/enums/tableEnum';

  import { TableOperationColumn } from '@arco-design/web-vue/es/table/interface';

  export interface FormTableColumn extends MsTableColumnData {
    enable?: boolean; // 是否启用
    [key: string]: any; // 扩展属性
  }

  const props = withDefaults(
    defineProps<{
      data?: any[];
      columns: FormTableColumn[];
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
      isTreeTable?: boolean; // 是否树形表格
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
    (e: 'change', data: any[]): void; // 都触发这个事件以通知父组件表格数据被更改
  }>();

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
    (val) => {
      propsRes.value.data = val;
    },
    {
      immediate: true,
    }
  );

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
  :deep(.arco-table-cell-align-left) {
    padding: 8px;
  }
  :deep(.arco-table-col-fixed-right) {
    .arco-table-cell-align-left {
      padding: 8px;
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
        color: var(--color-text-1);
      }
      .arco-select {
        border-color: transparent !important;
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
  :deep(.arco-table-expand-btn) {
    background: transparent;
  }
</style>
