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
        <a-table-column v-for="(item, key) in props.columns" :key="key">
          <template #title>
            <span>{{ t(item.title as string) }}</span>
            <div v-if="item.showSetting">
              <columnSelector />
            </div>
          </template>
          <template #cell="{ column, record, rowIndex }">
            <slot v-if="item.slotName" :name="item.slotName" v-bind="{ record, rowIndex, column }"></slot>
            <template v-else>{{ record[item.dataIndex as string] }}</template>
          </template>
        </a-table-column>
      </template>
    </a-table>
    <div v-if="selectCurrent > 0 && attrs.showSelectAll" class="mt-[21px]">
      <batch-action
        :select-row-count="selectCurrent"
        :action-config="props.actionConfig"
        @batch-action="(item: BatchActionParams) => emit('batchAction', item)"
        @clear="selectionChange([], true)"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';
  import { useAttrs, computed, ref, onMounted } from 'vue';
  import selectAll from './select-all.vue';
  import {
    MsTableProps,
    SelectAllEnum,
    MsPaginationI,
    BatchActionParams,
    BatchActionConfig,
    MsTableColumnData,
  } from './type';
  import BatchAction from './batchAction.vue';

  import type { TableData } from '@arco-design/web-vue';

  const batchleft = ref('10px');
  const { t } = useI18n();
  const props = defineProps<{
    selectedKeys?: (string | number)[];
    actionConfig?: BatchActionConfig;
    columns?: MsTableColumnData[];
    noDisable?: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'selectedChange', value: (string | number)[]): void;
    (e: 'batchAction', value: BatchActionParams): void;
  }>();
  const isSelectAll = ref(false);
  // 全选按钮-当前的条数
  const selectCurrent = ref(0);

  const attrs = useAttrs();

  const { rowKey, pagination }: Partial<MsTableProps> = attrs;

  // 全选按钮-总条数
  const selectTotal = computed(() => {
    const { data }: Partial<MsTableProps> = attrs;
    if (pagination) {
      const { pageSize } = pagination as MsPaginationI;
      return pageSize;
    }
    return data ? data.length : 20;
  });
  // 选择行change事件
  const selectionChange = (arr: (string | number)[], setCurrentSelect: boolean) => {
    emit('selectedChange', arr);
    if (setCurrentSelect) {
      selectCurrent.value = arr.length;
    }
  };

  // 全选change事件
  const handleChange = (v: string) => {
    const { data }: Partial<MsTableProps> = attrs;
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
      const { total } = pagination as MsPaginationI;
      if (data && data.length > 0) {
        selectionChange(
          data.map((item: any) => item[rowKey || 'id']),
          true
        );
        selectCurrent.value = total;
      }
    }
  };

  // 根据参数获取全选按钮的位置
  const getBatchLeft = () => {
    if (attrs.enableDrag) {
      return '30px';
    }
    switch (attrs.size) {
      case 'small':
        return '10px';
      case 'mini':
        return '10px';
      default:
        return '8px';
    }
  };

  function getRowClass(record: TableData) {
    if (!record.raw.enable && !props.noDisable) {
      return 'ms-table-row-disabled';
    }
  }

  onMounted(() => {
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
  }
</style>
