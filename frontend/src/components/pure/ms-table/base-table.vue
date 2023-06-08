<template>
  <div class="ms-base-tale">
    <select-all
      v-if="attrs.showSelectAll"
      class="custom-action"
      :total="selectTotal"
      :current="selectCurrent"
      @change="handleChange"
    />
    <a-table v-bind="$attrs" :selected-keys="props.selectedKeys" @selection-change="(e) => selectionChange(e, true)">
      <template v-for="(item, key, i) in slots" :key="i" #[key]="{ record, rowIndex, column }">
        <slot :name="key" v-bind="{ rowIndex, record, column }"></slot>
      </template>
    </a-table>
    <div v-if="selectCurrent > 0" class="mt-[21px]">
      <batch-action
        :select-row-count="selectCurrent"
        @batch-export="emit('batchExport')"
        @batch-edit="emit('batchEdit')"
        @batch-move="emit('batchMoveTo')"
        @batch-copy="emit('batchCopyTo')"
        @batch-related="emit('batchRelated')"
        @batch-generate="emit('batchGenerate')"
        @batch-add-public="emit('batchAddPublic')"
        @batch-delete="emit('batchDelete')"
        @clear="selectionChange([], true)"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
  // eslint-disable no-console
  import { useSlots, useAttrs, computed, ref, onMounted } from 'vue';
  import selectAll from './select-all.vue';
  import { MsTabelProps, SelectAllEnum, MsPaginationI } from './type';
  import BatchAction from './batchAction.vue';

  const batchleft = ref('10px');
  const props = defineProps({
    selectedKeys: {
      type: Array as unknown as () => (string | number)[],
      default: () => [],
    },
  });
  const emit = defineEmits<{
    (e: 'selectedChange', value: (string | number)[]): void;
    (e: 'batchExport'): void;
    (e: 'batchEdit'): void;
    (e: 'batchMoveTo'): void;
    (e: 'batchCopyTo'): void;
    (e: 'batchRelated'): void;
    (e: 'batchGenerate'): void;
    (e: 'batchAddPublic'): void;
    (e: 'batchDelete'): void;
  }>();
  const isSelectAll = ref(false);
  // 全选按钮-当前的条数
  const selectCurrent = ref(0);

  const slots = useSlots();
  const attrs = useAttrs();

  const { rowKey, pagination }: Partial<MsTabelProps> = attrs;

  // 全选按钮-总条数
  const selectTotal = computed(() => {
    const { data }: Partial<MsTabelProps> = attrs;
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
    const { data }: Partial<MsTabelProps> = attrs;
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
        return '10px';
    }
  };

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
      z-index: 100;
      border-radius: 2px;
      line-height: 40px;
      cursor: pointer;
    }
  }
</style>
