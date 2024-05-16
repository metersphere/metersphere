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
    @change="handleCommonVariablesChange"
    @batch-add="() => (batchAddKeyValVisible = true)"
  />
  <paramTable
    v-else
    v-model:params="csvVariables"
    :columns="csvColumns"
    :default-param-item="defaultCsvParamItem"
    :draggable="false"
    :selectable="false"
    @change="handleCsvVariablesChange"
    @batch-add="() => (batchAddKeyValVisible = true)"
  >
    <template #operationPre="{ record }">
      <a-trigger
        v-model:popup-visible="record.settingVisible"
        trigger="click"
        position="br"
        class="scenario-csv-trigger"
      >
        <MsButton type="text" class="!mr-0" @click="handleRecordConfig(record)">
          {{ t('apiScenario.params.config') }}
        </MsButton>
        <template #content>
          <div class="scenario-csv-trigger-content">
            <div class="mb-[16px] flex items-center">
              <div class="font-semibold text-[var(--color-text-1)]">{{ t('apiScenario.params.csvConfig') }}</div>
              <!-- <div class="text-[var(--color-text-4)]">({{ record.key }})</div> -->
            </div>
            <div class="scenario-csv-trigger-content-scroll">
              <a-form ref="paramFormRef" :model="paramForm" layout="vertical">
                <a-form-item
                  field="name"
                  :label="t('apiScenario.params.csvName')"
                  :rules="[{ required: true, message: t('apiScenario.params.csvNameNotNull') }]"
                  asterisk-position="end"
                  class="mb-[16px]"
                >
                  <a-input v-model:model-value="paramForm.name" :max-length="255"></a-input>
                </a-form-item>
                <a-form-item field="variableNames" :label="t('apiScenario.params.csvParamName')" class="mb-[16px]">
                  <a-input
                    v-model:model-value="paramForm.variableNames"
                    :placeholder="t('apiScenario.params.csvParamNamePlaceholder')"
                  ></a-input>
                </a-form-item>
                <a-form-item field="encoding" :label="t('apiScenario.params.csvFileCode')" class="mb-[16px]">
                  <a-select
                    v-model:model-value="paramForm.encoding"
                    :options="encodingOptions"
                    class="w-[120px]"
                  ></a-select>
                </a-form-item>
                <a-form-item field="delimiter" :label="t('apiScenario.params.csvSplitChar')" class="mb-[16px]">
                  <a-input
                    v-model:model-value="paramForm.delimiter"
                    :placeholder="t('common.pleaseInput')"
                    :max-length="64"
                    class="w-[120px]"
                  ></a-input>
                </a-form-item>
                <a-form-item
                  field="ignoreFirstLine"
                  :label="t('apiScenario.params.csvIgnoreFirstLine')"
                  class="mb-[16px]"
                >
                  <a-radio-group v-model:model-value="paramForm.ignoreFirstLine">
                    <a-radio :value="false">False</a-radio>
                    <a-radio :value="true">True</a-radio>
                  </a-radio-group>
                </a-form-item>
                <a-form-item field="random" :label="t('apiScenario.params.csvIsRandom')" class="mb-[16px]">
                  <a-radio-group v-model:model-value="paramForm.random">
                    <a-radio :value="false">False</a-radio>
                    <a-radio :value="true">True</a-radio>
                  </a-radio-group>
                </a-form-item>
                <a-form-item field="allowQuotedData" :label="t('apiScenario.params.csvQuoteAllow')" class="mb-[16px]">
                  <a-radio-group v-model:model-value="paramForm.allowQuotedData">
                    <a-radio :value="false">False</a-radio>
                    <a-radio :value="true">True</a-radio>
                  </a-radio-group>
                </a-form-item>
                <a-form-item field="recycleOnEof" :label="t('apiScenario.params.csvRecycle')" class="mb-[16px]">
                  <a-radio-group v-model:model-value="paramForm.recycleOnEof">
                    <a-radio :value="false">False</a-radio>
                    <a-radio :value="true">True</a-radio>
                  </a-radio-group>
                </a-form-item>
                <a-form-item field="stopThreadOnEof" :label="t('apiScenario.params.csvStop')" class="mb-[16px]">
                  <a-radio-group v-model:model-value="paramForm.stopThreadOnEof">
                    <a-radio :value="false">False</a-radio>
                    <a-radio :value="true">True</a-radio>
                  </a-radio-group>
                </a-form-item>
              </a-form>
            </div>
            <div class="flex items-center justify-end gap-[8px]">
              <a-button type="secondary" @click="cancelConfig">{{ t('common.cancel') }}</a-button>
              <a-button type="primary" @click="applyConfig">{{ t('ms.paramsInput.apply') }}</a-button>
            </div>
          </div>
        </template>
      </a-trigger>
    </template>
  </paramTable>
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
  import { FormInstance } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import batchAddKeyVal from '@/views/api-test/components/batchAddKeyVal.vue';
  import paramTable, { ParamTableColumn } from '@/views/api-test/components/paramTable.vue';

  import { CommonVariable, CsvVariable } from '@/models/apiTest/scenario';

  import { defaultCsvParamItem, defaultNormalParamItem } from '@/views/api-test/components/config';
  import { filterKeyValParams } from '@/views/api-test/components/utils';

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
      title: 'apiScenario.params.desc',
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

  function handleCsvVariablesChange(resultArr: any[], isInit?: boolean) {
    csvVariables.value = [...resultArr];
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

  const csvColumns: ParamTableColumn[] = [
    {
      title: 'apiScenario.params.csvName',
      dataIndex: 'name',
      slotName: 'name',
      needValidRepeat: true,
    },
    {
      title: 'apiScenario.params.csvScoped',
      dataIndex: 'scope',
      slotName: 'scope',
      typeOptions: [
        {
          label: t('apiScenario.scenario'),
          value: 'SCENARIO',
        },
        {
          label: t('apiScenario.step'),
          value: 'STEP',
        },
      ],
      width: 80,
      titleSlotName: 'typeTitle',
      typeTitleTooltip: [t('apiScenario.params.csvScopedTip1'), t('apiScenario.params.csvScopedTip2')],
    },
    {
      title: 'apiScenario.params.file',
      dataIndex: 'file',
      slotName: 'file',
    },
    {
      title: 'apiScenario.table.columns.status',
      dataIndex: 'enable',
      slotName: 'enable',
    },
    {
      title: '',
      slotName: 'operation',
      dataIndex: 'operation',
      width: 90,
    },
  ];

  const paramFormRef = ref<FormInstance>();
  const paramForm = ref<CsvVariable>(cloneDeep(defaultCsvParamItem));
  const encodingOptions = [
    {
      label: 'UTF-8',
      value: 'UTF-8',
    },
    {
      label: 'UTF-16',
      value: 'UTF-16',
    },
    {
      label: 'GBK',
      value: 'GBK',
    },
    {
      label: 'ISO-8859-15',
      value: 'ISO-8859-15',
    },
    {
      label: 'US-ASCII',
      value: 'US-ASCII',
    },
  ];

  function handleRecordConfig(record: CsvVariable) {
    paramForm.value = cloneDeep(record);
  }

  function cancelConfig() {
    paramFormRef.value?.resetFields();
  }

  function applyConfig() {
    paramFormRef.value?.validate((errors) => {
      if (!errors) {
        csvVariables.value = csvVariables.value.map((e) => {
          if (e.id === paramForm.value.id) {
            return {
              ...paramForm.value,
              settingVisible: false,
            };
          }
          return e;
        });
        emit('change');
      }
    });
  }
</script>

<style lang="less">
  .scenario-csv-trigger {
    @apply bg-white;
    .scenario-csv-trigger-content {
      padding: 16px;
      width: 400px;
      border-radius: var(--border-radius-medium);
      box-shadow: 0 5px 5px -3px rgb(0 0 0 / 10%), 0 8px 10px 1px rgb(0 0 0 / 6%), 0 3px 14px 2px rgb(0 0 0 / 5%);
      &::before {
        @apply absolute left-0 top-0;

        content: '';
        z-index: -1;
        width: 200%;
        height: 200%;
        border: 1px solid var(--color-text-input-border);
        border-radius: 12px;
        transform-origin: 0 0;
        transform: scale(0.5, 0.5);
      }
      .scenario-csv-trigger-content-scroll {
        .ms-scroll-bar();

        overflow-y: auto;
        margin-right: -6px;
        max-height: 400px;
        .scenario-csv-trigger-content-scroll-preview {
          @apply w-full overflow-y-auto overflow-x-hidden break-all;
          .ms-scroll-bar();

          max-height: 100px;
          color: var(--color-text-1);
        }
      }
    }
  }
</style>
