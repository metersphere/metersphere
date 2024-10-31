<template>
  <div class="history-container">
    <a-alert :show-icon="true" class="mb-[8px]" type="info">
      {{ t('apiScenario.params.priority') }}
    </a-alert>
  </div>
  <div class="mb-[8px] flex items-center justify-between">
    <a-radio-group v-model="activeRadio" type="button" size="medium">
      <a-radio value="convention">{{ t('apiScenario.params.convention') }}</a-radio>
      <a-radio value="csv">{{ t('apiScenario.params.csv') }}</a-radio>
    </a-radio-group>
    <a-input-search
      v-if="activeRadio === 'convention'"
      v-model="searchValue"
      :placeholder="t('apiScenario.params.searchPlaceholder')"
      allow-clear
      class="mr-[8px] w-[240px]"
      @search="handleSearch"
      @press-enter="handleSearch"
      @clear="handleSearch"
    />
  </div>
  <paramTable
    v-if="activeRadio === 'convention'"
    v-model:params="commonVariables"
    :columns="columns"
    :default-param-item="defaultNormalParamItem"
    :draggable="false"
    :selectable="false"
    :height-used="410"
    show-quick-copy
    @change="handleCommonVariablesChange"
    @batch-add="() => (batchAddKeyValVisible = true)"
  />
  <csvParamsTable
    v-else
    v-model:csvVariables="csvVariables"
    :scenario-id="props.scenarioId"
    @change="() => emit('change')"
  />
  <batchAddKeyVal
    v-model:visible="batchAddKeyValVisible"
    :params="commonVariables"
    :default-param-item="defaultNormalParamItem"
    no-param-type
    @apply="handleBatchParamApply"
  />
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n';

  import csvParamsTable from './common/csvParamsTable.vue';
  import batchAddKeyVal from '@/views/api-test/components/batchAddKeyVal.vue';
  import paramTable, { ParamTableColumn } from '@/views/api-test/components/paramTable.vue';

  import { CommonVariable, CsvVariable } from '@/models/apiTest/scenario';

  import { defaultNormalParamItem } from './config';
  import { filterKeyValParams } from '@/views/api-test/components/utils';

  const props = defineProps<{
    scenarioId?: string | number;
  }>();
  const emit = defineEmits<{
    (e: 'change'): void; //  数据发生变化
  }>();

  const { t } = useI18n();

  const commonVariables = defineModel<CommonVariable[]>('commonVariables', {
    required: true,
  });
  const csvVariables = defineModel<CsvVariable[]>('csvVariables', {
    required: true,
  });

  const searchValue = ref('');
  const firstSearch = ref(true);
  const backupParams = ref(commonVariables.value);
  const batchAddKeyValVisible = ref(false);
  const activeRadio = ref('convention');

  const columns: ParamTableColumn[] = [
    {
      title: 'apiScenario.params.name',
      dataIndex: 'key',
      slotName: 'key',
      needValidRepeat: true,
    },
    {
      title: 'apiScenario.params.type',
      dataIndex: 'paramType',
      slotName: 'paramType',
      options: [
        {
          label: t('common.constant'),
          value: 'CONSTANT',
        },
        {
          label: t('common.list'),
          value: 'LIST',
        },
      ],
      width: 150,
    },
    {
      title: 'apiScenario.params.paramValue',
      dataIndex: 'value',
      slotName: 'value',
    },
    {
      title: 'apiScenario.table.columns.status',
      dataIndex: 'enable',
      slotName: 'enable',
    },
    {
      title: 'apiScenario.params.tag',
      dataIndex: 'tags',
      slotName: 'tag',
      width: 200,
    },
    {
      title: 'common.desc',
      dataIndex: 'description',
      slotName: 'description',
    },
    {
      title: '',
      slotName: 'operation',
      titleSlotName: 'batchAddTitle',
      dataIndex: 'operation',
      width: 90,
    },
  ];

  function handleCommonVariablesChange(resultArr: any[], isInit?: boolean) {
    commonVariables.value = [...resultArr];
    if (!isInit) {
      emit('change');
      firstSearch.value = true;
    }
  }

  // 搜索
  function handleSearch() {
    if (firstSearch.value) {
      backupParams.value = [...commonVariables.value];
      firstSearch.value = false;
    }
    if (!searchValue.value) {
      commonVariables.value = [...backupParams.value];
    } else {
      const result = backupParams.value.filter(
        (item) => item.key.includes(searchValue.value) || item.tags.includes(searchValue.value)
      );
      commonVariables.value = [...result];
    }
  }

  /**
   * 批量参数代码转换为参数表格数据
   */
  function handleBatchParamApply(resultArr: any[]) {
    const filterResult = filterKeyValParams(commonVariables.value, defaultNormalParamItem);
    if (filterResult.lastDataIsDefault) {
      commonVariables.value = [...resultArr, commonVariables.value[commonVariables.value.length - 1]].filter(Boolean);
    } else {
      commonVariables.value = resultArr.filter(Boolean);
    }
    emit('change');
  }
</script>

<style lang="less"></style>
