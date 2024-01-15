<template>
  <MsBaseTable v-bind="propsRes" :hoverable="false" v-on="propsEvent">
    <template #name="{ record }">
      <a-popover position="tl" :disabled="!record.name || record.name.trim() === ''" class="ms-params-input-popover">
        <template #content>
          <div class="param-popover-title">
            {{ t('ms.apiTestDebug.paramName') }}
          </div>
          <div class="param-popover-value">
            {{ record.name }}
          </div>
        </template>
        <a-input
          v-model:model-value="record.name"
          :placeholder="t('project.environmental.paramNamePlaceholder')"
          class="param-input"
          @input="(val) => addTableLine(val)"
        />
      </a-popover>
    </template>
    <template #type="{ record }">
      <a-select v-model:model-value="record.type" class="param-input" @change="(val) => handleTypeChange(val)">
        <a-option v-for="element in typeOptions" :key="element.value" :value="element.value">{{
          t(element.label)
        }}</a-option>
      </a-select>
    </template>
    <template #value="{ record }">
      <MsParamsInput
        v-model:value="record.value"
        @change="addTableLine"
        @dblclick="quickInputParams(record)"
        @apply="handleParamSettingApply"
      />
    </template>
    <template #desc="{ record }">
      <ParamDescInput
        v-model:desc="record.desc"
        @input="addTableLine"
        @dblclick="quickInputDesc(record)"
        @change="handleDescChange"
      />
    </template>
    <template #operation="{ record, rowIndex }">
      <div class="flex flex-row items-center gap-[16px]">
        <a-switch v-if="rowIndex" v-model:model-value="record.enable" size="small" />
        <icon-minus-circle
          v-if="paramsLength > 1 && rowIndex !== paramsLength - 1"
          class="cursor-pointer text-[var(--color-text-4)]"
          size="20"
          @click="deleteParam(rowIndex)"
        />
      </div>
    </template>
    <template #tag="{ record }">
      <ParamTagInput
        v-model:model-value="record.tag"
        @input="(val) => addTableLine(val)"
        @dblclick="quickInputDesc(record)"
        @change="handleDescChange"
      />
    </template>
  </MsBaseTable>
  <a-modal
    v-model:visible="showQuickInputParam"
    :title="t('ms.paramsInput.value')"
    :ok-text="t('ms.apiTestDebug.apply')"
    class="ms-modal-form"
    body-class="!p-0"
    :width="680"
    title-align="start"
    @ok="applyQuickInputParam"
    @close="clearQuickInputParam"
  >
    <MsCodeEditor
      v-if="showQuickInputParam"
      v-model:model-value="quickInputParamValue"
      theme="MS-text"
      height="300px"
      :show-full-screen="false"
    >
      <template #title>
        <div class="flex justify-between">
          <div class="text-[var(--color-text-1)]">
            {{ t('ms.apiTestDebug.quickInputParamsTip') }}
          </div>
        </div>
      </template>
    </MsCodeEditor>
  </a-modal>
  <a-modal
    v-model:visible="showQuickInputDesc"
    :title="t('ms.apiTestDebug.desc')"
    :ok-text="t('common.save')"
    :ok-button-props="{ disabled: !quickInputDescValue || quickInputDescValue.trim() === '' }"
    class="ms-modal-form"
    body-class="!p-0"
    :width="480"
    title-align="start"
    :auto-size="{ minRows: 2 }"
    @ok="applyQuickInputDesc"
    @close="clearQuickInputDesc"
  >
    <a-textarea
      v-model:model-value="quickInputDescValue"
      :placeholder="t('ms.apiTestDebug.descPlaceholder')"
      :max-length="255"
      show-word-limit
    ></a-textarea>
  </a-modal>
</template>

<script async setup lang="ts">
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumnData } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsParamsInput from '@/components/business/ms-params-input/index.vue';
  import ParamDescInput from './ParamDescInput.vue';
  import ParamTagInput from './ParamTagInput.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { useTableStore } from '@/store';

  import { TableKeyEnum } from '@/enums/tableEnum';

  interface Param {
    id: number;
    name: string;
    type: string;
    value: string;
    desc: string;
    tag: string[];
    enable: boolean;
  }

  const props = withDefaults(
    defineProps<{
      params: Param[];
      scroll?: {
        x?: number | string;
        y?: number | string;
        maxHeight?: number | string;
        minWidth?: number | string;
      };
      disabled?: boolean; // 是否禁用
      showSetting?: boolean; // 是否显示列设置
      tableKey?: TableKeyEnum; // 表格key showSetting为true时必传
      columns: MsTableColumnData[]; // 表格列配置 showSetting为false时必传
      showSelectorAll?: boolean; // 是否显示全选
      heightUsed?: number;
    }>(),
    {
      disabled: false,
      showSetting: false,
      tableKey: undefined,
      showSelectorAll: false,
      heightUsed: 0,
    }
  );
  const emit = defineEmits<{
    (e: 'update:params', value: Param[]): void;
    (e: 'change', data: Param[], isInit?: boolean): void;
  }>();

  const { t } = useI18n();

  const defaultParams: Omit<Param, 'id'> = {
    name: '',
    type: 'string',
    value: '',
    desc: '',
    tag: [],
    enable: true,
  };
  const allType = [
    {
      label: 'common.string',
      value: 'string',
    },
    {
      label: 'common.integer',
      value: 'integer',
    },
    {
      label: 'common.number',
      value: 'number',
    },
    {
      label: 'common.array',
      value: 'array',
    },
    {
      label: 'common.json',
      value: 'json',
    },
    {
      label: 'common.file',
      value: 'file',
    },
  ];

  const tableStore = useTableStore();

  const typeOptions = computed(() => {
    return allType;
  });

  if (props.showSetting && props.tableKey) {
    await tableStore.initColumn(props.tableKey, props.columns);
  }

  const { propsRes, propsEvent } = useTable<Param>(undefined, {
    tableKey: props.showSetting ? props.tableKey : undefined,
    columns: props.columns,
    scroll: props.scroll,
    heightUsed: props.heightUsed,
    selectable: true,
    draggable: { type: 'handle', width: 24 },
    showSetting: props.showSetting,
    disabled: props.disabled,
    showSelectorAll: props.showSelectorAll,
  });

  watch(
    () => props.params,
    (val) => {
      if (val.length > 0) {
        propsRes.value.data = val;
      } else {
        propsRes.value.data = props.params.concat({
          id: new Date().getTime(),
          name: '',
          type: 'string',
          value: '',
          desc: '',
          tag: [],
          enable: true,
        });
        emit('change', propsRes.value.data, true);
      }
    },
    {
      immediate: true,
    }
  );

  watch(
    () => props.heightUsed,
    (val) => {
      propsRes.value.heightUsed = val;
    }
  );

  const paramsLength = computed(() => propsRes.value.data.length);

  function deleteParam(rowIndex: number) {
    propsRes.value.data.splice(rowIndex, 1);
    emit('change', propsRes.value.data);
  }

  /**
   * 当表格输入框变化时，给参数表格添加一行数据行
   * @param val 输入值
   * @param isForce 是否强制添加
   */
  function addTableLine(val?: string | number, isForce?: boolean) {
    const lastData = propsRes.value.data[propsRes.value.data.length - 1];
    const isNotChange = Object.keys(defaultParams).every((key) => {
      if (key === 'id') {
        return true;
      }
      if (key === 'tag') {
        return lastData[key].length === 0;
      }
      return lastData[key] === defaultParams[key as any];
    });
    if (isForce || (val !== '' && val !== undefined && !isNotChange)) {
      propsRes.value.data = [...propsRes.value.data, { id: new Date().getTime(), ...defaultParams }];
      emit('change', propsRes.value.data);
    }
  }

  const showQuickInputParam = ref(false);
  const activeQuickInputRecord = ref<any>({});
  const quickInputParamValue = ref('');

  function quickInputParams(record: any) {
    activeQuickInputRecord.value = record;
    showQuickInputParam.value = true;
    quickInputParamValue.value = record.value;
  }

  function clearQuickInputParam() {
    activeQuickInputRecord.value = {};
    quickInputParamValue.value = '';
  }

  function applyQuickInputParam() {
    activeQuickInputRecord.value.value = quickInputParamValue.value;
    showQuickInputParam.value = false;
    clearQuickInputParam();
    addTableLine(quickInputParamValue.value, true);
    emit('change', propsRes.value.data);
  }

  function handleParamSettingApply(val: string | number) {
    addTableLine(val);
  }

  const showQuickInputDesc = ref(false);
  const quickInputDescValue = ref('');

  function quickInputDesc(record: any) {
    activeQuickInputRecord.value = record;
    showQuickInputDesc.value = true;
    quickInputDescValue.value = record.desc;
  }

  function clearQuickInputDesc() {
    activeQuickInputRecord.value = {};
    quickInputDescValue.value = '';
  }

  function applyQuickInputDesc() {
    activeQuickInputRecord.value.desc = quickInputDescValue.value;
    showQuickInputDesc.value = false;
    clearQuickInputDesc();
    addTableLine(quickInputDescValue.value, true);
    emit('change', propsRes.value.data);
  }

  function handleDescChange() {
    emit('change', propsRes.value.data);
  }

  function handleTypeChange(
    val: string | number | boolean | Record<string, any> | (string | number | boolean | Record<string, any>)[]
  ) {
    addTableLine(val as string);
  }
</script>

<style lang="less" scoped>
  :deep(.setting-icon) {
    margin-left: 0 !important;
  }
  :deep(.arco-table-th) {
    background-color: var(--color-text-n9);
  }
  :deep(.arco-table-cell-align-left) {
    padding: 16px 4px;
  }
  :deep(.arco-table-cell) {
    padding: 11px 4px;
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
        color: var(--color-text-brand);
      }
    }
  }
  .param-input-switch:not(:hover).arco-switch-checked {
    background-color: rgb(var(--primary-3)) !important;
  }
  .content-type-trigger-content {
    @apply bg-white;

    padding: 8px;
    border-radius: var(--border-radius-small);
    box-shadow: 0 4px 10px -1px rgb(100 100 102 / 15%);
  }
  .param-input {
    .param-input-mock-icon {
      @apply invisible;
    }
    &:hover,
    &.arco-input-focus {
      .param-input-mock-icon {
        @apply visible cursor-pointer;
        &:hover {
          color: rgb(var(--primary-5));
        }
      }
    }
  }
  .param-popover-title {
    @apply font-medium;

    margin-bottom: 4px;
    font-size: 12px;
    font-weight: 500;
    line-height: 16px;
    color: var(--color-text-1);
  }
  .param-popover-subtitle {
    margin-bottom: 2px;
    font-size: 12px;
    line-height: 16px;
    color: var(--color-text-4);
  }
  .param-popover-value {
    min-width: 100px;
    max-width: 280px;
    font-size: 12px;
    line-height: 16px;
    color: var(--color-text-1);
  }
</style>
