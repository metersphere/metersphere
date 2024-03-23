<template>
  <div class="history-container">
    <a-alert v-if="isShowTip" :show-icon="true" class="mb-[16px]" type="info">
      {{ t('apiScenario.params.priority') }}
    </a-alert>
  </div>
  <!--  <div class="mb-[16px]">
    <a-radio-group v-model="activeRadio" type="button" size="medium">
      <a-radio value="convention">{{ t('apiScenario.params.convention') }}</a-radio>
    </a-radio-group>
  </div>-->
  <div class="mb-[16px] flex items-center justify-between">
    <a-input-search
      v-model="searchValue"
      :placeholder="t('apiScenario.params.searchPlaceholder')"
      allow-clear
      class="mr-[8px] w-[240px]"
      @search="handleSearch"
      @press-enter="handleSearch"
    />
    <batchAddKeyVal
      :params="innerParams"
      :default-param-item="defaultHeaderParamsItem"
      no-param-type
      @apply="handleBatchParamApply"
    />
  </div>
  <paramTable
    v-model:params="innerParams"
    :columns="columns"
    :default-param-item="defaultParamItem"
    draggable
    @change="handleParamTableChange"
  />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useVModel } from '@vueuse/core';

  import batchAddKeyVal from '@/views/api-test/components/batchAddKeyVal.vue';
  import paramTable, { ParamTableColumn } from '@/views/api-test/components/paramTable.vue';

  import { CommonVariable } from '@/models/apiTest/scenario';

  import { defaultHeaderParamsItem } from '@/views/api-test/components/config';
  import { filterKeyValParams } from '@/views/api-test/components/utils';

  const props = defineProps<{
    activeKey?: string;
    params: CommonVariable[];
  }>();
  const emit = defineEmits<{
    (e: 'update:params', value: CommonVariable[]): void;
    (e: 'change'): void; //  数据发生变化
  }>();
  const innerParams = useVModel(props, 'params', emit);
  const isShowTip = ref<boolean>(true);
  const { t } = useI18n();
  const searchValue = ref('');
  const firstSearch = ref(true);
  const backupParams = ref(props.params);

  const defaultParamItem = {
    key: '',
    paramType: 'CONSTANT',
    value: '',
    description: '',
    tags: [],
    enable: true,
  };

  const columns: ParamTableColumn[] = [
    {
      title: 'apiScenario.params.name',
      dataIndex: 'key',
      slotName: 'key',
    },
    {
      title: 'apiScenario.params.type',
      dataIndex: 'paramType',
      slotName: 'paramType',
      typeOptions: [
        {
          label: t('common.constant'),
          value: 'CONSTANT',
        },
        {
          label: t('common.list'),
          value: 'LIST',
        },
      ],
      titleSlotName: 'typeTitle',
    },
    {
      title: 'apiScenario.params.paramValue',
      dataIndex: 'value',
      slotName: 'value',
    },
    {
      title: 'apiScenario.params.tag',
      dataIndex: 'tags',
      slotName: 'tag',
      width: 200,
    },
    {
      title: 'apiScenario.params.desc',
      dataIndex: 'description',
      slotName: 'description',
    },
    {
      title: '',
      slotName: 'operation',
      dataIndex: 'operation',
      width: 50,
    },
  ];

  function handleParamTableChange(resultArr: any[], isInit?: boolean) {
    innerParams.value = [...resultArr];
    if (!isInit) {
      emit('change');
      firstSearch.value = true;
    }
  }

  // 搜索
  function handleSearch() {
    if (firstSearch.value) {
      backupParams.value = [...innerParams.value];
      firstSearch.value = false;
    }
    if (!searchValue.value) {
      innerParams.value = [...backupParams.value];
    } else {
      const result = backupParams.value.filter(
        (item) => item.key.includes(searchValue.value) || item.tags.includes(searchValue.value)
      );
      innerParams.value = [...result];
    }
  }

  /**
   * 批量参数代码转换为参数表格数据
   */
  function handleBatchParamApply(resultArr: any[]) {
    const filterResult = filterKeyValParams(innerParams.value, defaultHeaderParamsItem);
    if (filterResult.lastDataIsDefault) {
      innerParams.value = [...resultArr, innerParams.value[innerParams.value.length - 1]].filter(Boolean);
    } else {
      innerParams.value = resultArr.filter(Boolean);
    }
    emit('change');
  }
</script>

<style lang="less" scoped></style>
