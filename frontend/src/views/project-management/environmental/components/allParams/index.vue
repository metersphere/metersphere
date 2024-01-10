<template>
  <div class="mb-[8px] flex items-center justify-between">
    <a-input
      v-model:value="searchValue"
      :placeholder="t('project.environmental.searchParamsHolder')"
      allow-clear
      class="w-[240px]"
      @blur="handleSearch"
      @press-enter="handleSearch"
    >
      <template #prefix>
        <span class="arco-icon-hover">
          <icon-search class="cursor-pointer" @click="handleSearch" />
        </span>
      </template>
    </a-input>
    <batchAddKeyVal :params="innerParams" @apply="handleBatchParamApply" />
  </div>
  <AllParamsTable v-model:params="innerParams" @change="handleParamTableChange" />
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import AllParamsTable from './AllParamsTable.vue';
  import batchAddKeyVal from '@/views/api-test/debug/components/debug/batchAddKeyVal.vue';

  import { useI18n } from '@/hooks/useI18n';

  const props = defineProps<{
    params: any[];
  }>();
  const emit = defineEmits<{
    (e: 'update:params', value: any[]): void;
    (e: 'change'): void; //  数据发生变化
  }>();

  const searchValue = ref('');

  const { t } = useI18n();

  const innerParams = useVModel(props, 'params', emit);

  /**
   * 批量参数代码转换为参数表格数据
   */
  function handleBatchParamApply(resultArr: any[]) {
    if (resultArr.length < innerParams.value.length) {
      innerParams.value.splice(0, innerParams.value.length - 1, ...resultArr);
    } else {
      innerParams.value = [...resultArr, innerParams.value[innerParams.value.length - 1]];
    }
    emit('change');
  }

  function handleParamTableChange(resultArr: any[], isInit?: boolean) {
    innerParams.value = [...resultArr];
    if (!isInit) {
      emit('change');
    }
  }

  function handleSearch() {
    if (searchValue.value.length === 0) {
      return;
    }
    const result = innerParams.value.filter((item) => item.name.includes(searchValue.value));
    if (result.length === 0) {
      return;
    }
    innerParams.value = [...result];
  }
</script>

<style lang="less" scoped></style>
