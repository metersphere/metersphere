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
  <paramsTable
    v-model:params="innerParams"
    :table-key="props.tableKey"
    :columns="columns"
    show-setting
    @change="handleParamTableChange"
  />
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import paramsTable, { type ParamTableColumn } from '@/views/api-test/components/paramTable.vue';
  import batchAddKeyVal from '@/views/api-test/debug/components/debug/batchAddKeyVal.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { TableKeyEnum } from '@/enums/tableEnum';

  const props = withDefaults(
    defineProps<{
      params: any[];
      tableKey: TableKeyEnum;
    }>(),
    {
      tableKey: TableKeyEnum.PROJECT_MANAGEMENT_ENV_ALL_PARAM,
    }
  );
  const emit = defineEmits<{
    (e: 'update:params', value: any[]): void;
    (e: 'change'): void; //  数据发生变化
  }>();

  const searchValue = ref('');

  const { t } = useI18n();

  const innerParams = useVModel(props, 'params', emit);

  const columns: ParamTableColumn[] = [
    {
      title: 'project.environmental.paramName',
      dataIndex: 'name',
      slotName: 'name',
      showInTable: true,
      showDrag: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'project.environmental.paramType',
      dataIndex: 'type',
      slotName: 'type',
      showInTable: true,
      showDrag: true,
      columnSelectorDisabled: true,
      typeOptions: [
        {
          label: t('common.string'),
          value: 'string',
        },
        {
          label: t('common.integer'),
          value: 'integer',
        },
        {
          label: t('common.number'),
          value: 'number',
        },
        {
          label: t('common.array'),
          value: 'array',
        },
        {
          label: t('common.json'),
          value: 'json',
        },
        {
          label: t('common.file'),
          value: 'file',
        },
      ],
      titleSlotName: 'typeTitle',
      typeTitleTooltip: t('project.environmental.paramTypeTooltip'),
    },
    {
      title: 'project.environmental.paramValue',
      dataIndex: 'value',
      slotName: 'value',
      showInTable: true,
      showDrag: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'project.environmental.tag',
      dataIndex: 'tag',
      slotName: 'tag',
      width: 200,
      showInTable: true,
      showDrag: true,
    },
    {
      title: 'project.environmental.desc',
      dataIndex: 'desc',
      slotName: 'desc',
      showInTable: true,
      showDrag: true,
    },
    {
      title: '',
      columnTitle: 'common.operation',
      slotName: 'operation',
      dataIndex: 'operation',
      width: 50,
      showInTable: true,
      showDrag: true,
    },
  ];

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
