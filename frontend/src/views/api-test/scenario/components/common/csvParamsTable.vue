<template>
  <paramTable
    v-model:params="csvVariables"
    :columns="csvColumns"
    :default-param-item="defaultCsvParamItem"
    :draggable="false"
    :selectable="false"
    :height-used="410"
    :delete-intercept="deleteIntercept"
    :type-change-intercept="typeChangeIntercept"
    :enable-change-intercept="enableChangeIntercept"
    :upload-temp-file-api="uploadTempFile"
    :file-save-as-source-id="props.scenarioId"
    :file-save-as-api="transferFile"
    :file-module-options-api="getTransferOptions"
    @change="(data) => handleCsvVariablesChange(data as CsvVariable[])"
  >
    <template #operationPre="{ record }">
      <a-trigger
        v-model:popup-visible="record.settingVisible"
        trigger="click"
        position="left"
        class="scenario-csv-trigger"
        :popup-translate="[-2, 244]"
      >
        <MsButton type="text" class="!mr-0" size="mini" @click="handleRecordConfig(record)">
          {{ t('apiScenario.params.config') }}
        </MsButton>
        <template #content>
          <div class="scenario-csv-trigger-content">
            <div class="mb-[16px] flex items-center">
              <div class="font-semibold text-[var(--color-text-1)]">{{ t('apiScenario.params.csvConfig') }}</div>
              <!-- <div class="text-[var(--color-text-4)]">({{ record.key }})</div> -->
            </div>
            <div class="scenario-csv-trigger-content-scroll">
              <a-form ref="paramFormRef" :model="paramForm" layout="vertical" scroll-to-first-error>
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
              <a-button type="secondary" @click="cancelConfig(record)">{{ t('common.cancel') }}</a-button>
              <a-button type="primary" @click="applyConfig">{{ t('ms.paramsInput.apply') }}</a-button>
            </div>
          </div>
        </template>
      </a-trigger>
    </template>
  </paramTable>
</template>

<script setup lang="ts">
  import { FormInstance, Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import paramTable, { ParamTableColumn } from '@/views/api-test/components/paramTable.vue';

  import { getTransferOptions, transferFile, uploadTempFile } from '@/api/modules/api-test/scenario';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { getGenerateId } from '@/utils';

  import { CsvVariable } from '@/models/apiTest/scenario';

  import { defaultCsvParamItem } from '../config';
  import { filterKeyValParams } from '@/views/api-test/components/utils';

  const props = defineProps<{
    scenarioId?: string | number;
  }>();
  const emit = defineEmits<{
    (e: 'change'): void; //  数据发生变化
  }>();

  const { t } = useI18n();
  const { openModal } = useModal();

  const csvVariables = defineModel<CsvVariable[]>('csvVariables', {
    required: true,
  });

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
      options: [
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
      accept: 'csv',
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

  function handleCsvVariablesChange(resultArr: CsvVariable[], isInit?: boolean) {
    csvVariables.value = resultArr.map((e) => ({ ...e, enable: e.name && e.file.fileId ? e.enable : false }));
    if (!isInit) {
      emit('change');
    }
  }

  function handleRecordConfig(record: CsvVariable) {
    paramForm.value = cloneDeep(record);
  }

  function cancelConfig(record: CsvVariable) {
    paramFormRef.value?.resetFields();
    record.settingVisible = false;
  }

  function applyConfig() {
    paramFormRef.value?.validate((errors) => {
      if (!errors) {
        let newArr = csvVariables.value.map((e) => {
          if (e.id === paramForm.value.id) {
            return {
              ...paramForm.value,
              settingVisible: false,
            };
          }
          return e;
        });
        if (newArr.findIndex((e) => e.id === paramForm.value.id) === newArr.length - 1) {
          newArr = newArr.concat({
            ...cloneDeep(defaultCsvParamItem),
            id: getGenerateId(),
          });
        }
        csvVariables.value = newArr;
        emit('change');
      }
    });
  }

  function deleteIntercept(record: CsvVariable, deleteCall: () => void) {
    if (!!record.name && !!record.file.fileId) {
      // 删除有效参数才二次确认
      openModal({
        type: 'error',
        title: t('apiScenario.deleteCsvConfirm', { name: record.name }),
        content: t('apiScenario.deleteCsvConfirmContent'),
        okText: t('common.confirmDelete'),
        cancelText: t('common.cancel'),
        okButtonProps: {
          status: 'danger',
        },
        onBeforeOk: async () => {
          deleteCall();
          Message.success(t('apiScenario.deleteCsvSuccess'));
        },
        hideCancel: false,
      });
    } else {
      deleteCall();
    }
  }

  function typeChangeIntercept(record: CsvVariable, doChange: () => void) {
    if (!!record.name && !!record.file.fileId) {
      // 更改有效参数才二次确认
      record.scope = record.scope === 'SCENARIO' ? 'STEP' : 'SCENARIO'; // 先把值改回修改前的值
      openModal({
        type: 'warning',
        title: t('apiScenario.changeScopeConfirm', {
          type: record.scope === 'SCENARIO' ? t('apiScenario.step') : t('apiScenario.scenario'),
        }),
        content:
          record.scope === 'SCENARIO'
            ? t('apiScenario.changeScopeToStepConfirmContent')
            : t('apiScenario.changeScopeToScenarioConfirmContent'),
        okText: t('apiScenario.confirmChange'),
        cancelText: t('common.cancel'),
        onBeforeOk: async () => {
          record.scope = record.scope === 'SCENARIO' ? 'STEP' : 'SCENARIO';
          doChange();
          Message.success(t('apiScenario.changeScopeSuccess'));
        },
        hideCancel: false,
      });
    } else {
      doChange();
    }
  }

  function enableChangeIntercept(record: CsvVariable, val: string | number | boolean) {
    if (val) {
      if (!record.name) {
        Message.warning(t('apiScenario.csvNameNotNull'));
        return false;
      }
      if (!record.file.fileId) {
        Message.warning(t('apiScenario.csvFileNotNull'));
        return false;
      }
      return true;
    }
    return true;
  }

  onBeforeMount(() => {
    if (
      csvVariables.value.length > 0 &&
      !filterKeyValParams(csvVariables.value, defaultCsvParamItem).lastDataIsDefault
    ) {
      csvVariables.value.push({
        ...cloneDeep(defaultCsvParamItem),
        id: getGenerateId(),
      });
    }
  });
</script>

<style lang="less">
  .scenario-csv-trigger {
    background-color: var(--color-text-fff);
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
