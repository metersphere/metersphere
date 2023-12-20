<template>
  <div class="mb-[8px] flex items-center justify-between">
    <div class="font-medium">{{ t('ms.apiTestDebug.header') }}</div>
    <a-button type="outline" size="mini" @click="showBatchAddParamDrawer = true">
      {{ t('ms.apiTestDebug.batchAdd') }}
    </a-button>
  </div>
  <div class="relative">
    <MsBaseTable v-bind="propsRes" id="headerTable" v-on="propsEvent">
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
            :placeholder="t('ms.apiTestDebug.paramNamePlaceholder')"
            class="param-input"
            @input="addTableLine"
          />
        </a-popover>
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
        <paramDescInput v-model:desc="record.desc" @input="addTableLine" @dblclick="quickInputDesc(record)" />
      </template>
      <template #operation="{ rowIndex }">
        <icon-minus-circle
          v-if="paramsLength > 1 && rowIndex !== paramsLength - 1"
          class="cursor-pointer text-[var(--color-text-4)]"
          size="20"
          @click="deleteParam(rowIndex)"
        />
      </template>
    </MsBaseTable>
  </div>
  <MsDrawer
    v-model:visible="showBatchAddParamDrawer"
    :title="t('common.batchAdd')"
    :width="680"
    :ok-text="t('ms.apiTestDebug.apply')"
    disabled-width-drag
    @confirm="applyBatchParams"
  >
    <div class="flex h-full">
      <MsCodeEditor
        v-model:model-value="batchParamsCode"
        class="flex-1"
        theme="MS-text"
        height="calc(100% - 48px)"
        :show-full-screen="false"
      >
        <template #title>
          <div class="flex flex-col">
            <div class="text-[12px] leading-[16px] text-[var(--color-text-4)]">
              {{ t('ms.apiTestDebug.batchAddParamsTip') }}
            </div>
            <div class="text-[12px] leading-[16px] text-[var(--color-text-4)]">
              {{ t('ms.apiTestDebug.batchAddParamsTip2') }}
            </div>
          </div>
        </template>
      </MsCodeEditor>
    </div>
  </MsDrawer>
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
    <MsCodeEditor v-model:model-value="quickInputParamValue" theme="MS-text" height="300px" :show-full-screen="false">
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

<script setup lang="ts">
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsParamsInput from '@/components/business/ms-params-input/index.vue';
  import paramDescInput from './paramDescInput.vue';

  import { useI18n } from '@/hooks/useI18n';

  const props = defineProps<{
    params: any[];
    layout: 'horizontal' | 'vertical';
    secondBoxHeight: number;
  }>();

  const { t } = useI18n();

  const columns: MsTableColumn = [
    {
      title: 'ms.apiTestDebug.paramName',
      dataIndex: 'name',
      slotName: 'name',
    },
    {
      title: 'ms.apiTestDebug.paramValue',
      dataIndex: 'value',
      slotName: 'value',
    },
    {
      title: 'ms.apiTestDebug.desc',
      dataIndex: 'desc',
      slotName: 'desc',
    },
    {
      title: '',
      slotName: 'operation',
      width: 50,
    },
  ];
  const { propsRes, propsEvent } = useTable(() => Promise.resolve([]), {
    scroll: props.layout === 'horizontal' ? { x: '700px' } : { x: '100%' },
    heightUsed: props.layout === 'horizontal' ? 422 : 422 + props.secondBoxHeight,
    columns,
    selectable: true,
    draggable: { type: 'handle', width: 24 },
  });

  propsRes.value.data = props.params.concat({
    id: new Date().getTime(),
    name: '',
    value: '',
    desc: '',
  });

  watch(
    () => props.layout,
    (val) => {
      propsRes.value.heightUsed = val === 'horizontal' ? 422 : 422 + props.secondBoxHeight;
    },
    {
      immediate: true,
    }
  );

  watch(
    () => props.secondBoxHeight,
    (val) => {
      if (props.layout === 'vertical') {
        propsRes.value.heightUsed = 422 + val;
      }
    },
    {
      immediate: true,
    }
  );

  const paramsLength = computed(() => propsRes.value.data.length);

  function deleteParam(rowIndex: number) {
    propsRes.value.data.splice(rowIndex, 1);
  }

  /**
   * 当表格输入框变化时，给参数表格添加一行数据行
   * @param val 输入值
   */
  function addTableLine(val: string | number) {
    const lastData = propsRes.value.data[propsRes.value.data.length - 1];
    if (val && (lastData.name || lastData.value || lastData.desc)) {
      propsRes.value.data.push({
        id: new Date().getTime(),
        name: '',
        value: '',
        desc: '',
      } as any);
    }
  }

  const showBatchAddParamDrawer = ref(false);
  const batchParamsCode = ref('');

  /**
   * 批量参数代码转换为参数表格数据
   */
  function applyBatchParams() {
    const arr = batchParamsCode.value.replaceAll('\r', '\n').split('\n'); // 先将回车符替换成换行符，避免粘贴的代码是以回车符分割的，然后以换行符分割
    const resultArr = arr
      .map((item, i) => {
        const [name, value] = item.split(':');
        if (name && value) {
          return {
            id: new Date().getTime() + i,
            name: name.trim(),
            value: value.trim(),
            desc: '',
          };
        }
        return null;
      })
      .filter((item) => item);
    propsRes.value.data.splice(propsRes.value.data.length - 1, 0, ...(resultArr as any[]));
    showBatchAddParamDrawer.value = false;
    batchParamsCode.value = '';
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
  }
</script>

<style lang="less" scoped>
  :deep(.arco-table-th) {
    background-color: var(--color-text-n9);
  }
  :deep(.arco-table-cell-align-left) {
    padding: 16px 4px;
  }
  :deep(.arco-table-cell) {
    padding: 11px 4px;
  }
  .param-input:not(.arco-input-focus) {
    &:not(:hover) {
      border-color: transparent;
    }
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
