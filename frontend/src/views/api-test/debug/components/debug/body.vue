<template>
  <div class="mb-[8px] flex items-center justify-between">
    <div class="font-medium">{{ t('ms.apiTestDebug.body') }}</div>
    <div class="flex items-center gap-[16px]">
      <batchAddKeyVal v-if="showParamTable" :params="currentTableParams" @apply="handleBatchParamApply" />
      <a-radio-group v-model:model-value="format" type="button" size="small" @change="formatChange">
        <a-radio v-for="item of RequestBodyFormat" :key="item" :value="item">{{ item }}</a-radio>
      </a-radio-group>
    </div>
  </div>
  <div
    v-if="format === RequestBodyFormat.NONE"
    class="flex h-[100px] items-center justify-center rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)] text-[var(--color-text-4)]"
  >
    {{ t('ms.apiTestDebug.noneBody') }}
  </div>
  <paramTable
    v-else-if="showParamTable"
    v-model:params="currentTableParams"
    :scroll="{ minWidth: 1160 }"
    :format="format"
    :columns="columns"
    :height-used="heightUsed"
    @change="handleParamTableChange"
  />
  <div v-else class="flex h-[calc(100%-100px)]">
    <MsCodeEditor
      v-model:model-value="currentBodyCode"
      class="flex-1"
      theme="vs-dark"
      height="calc(100% - 48px)"
      :show-full-screen="false"
      :language="currentCodeLanguage"
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
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import paramTable from '../../../components/paramTable.vue';
  import batchAddKeyVal from './batchAddKeyVal.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { RequestBodyFormat } from '@/enums/apiEnum';

  export interface BodyParams {
    format: RequestBodyFormat;
    formData: Record<string, any>[];
    formUrlEncode: Record<string, any>[];
    json: string;
    xml: string;
    binary: string;
    raw: string;
  }
  const props = defineProps<{
    params: BodyParams;
    layout: 'horizontal' | 'vertical';
    secondBoxHeight: number;
  }>();
  const emit = defineEmits<{
    (e: 'update:params', value: any[]): void;
    (e: 'change'): void;
  }>();

  const { t } = useI18n();

  const innerParams = useVModel(props, 'params', emit);

  const columns: MsTableColumn = [
    {
      title: 'ms.apiTestDebug.paramName',
      dataIndex: 'name',
      slotName: 'name',
    },
    {
      title: 'ms.apiTestDebug.paramType',
      dataIndex: 'type',
      slotName: 'type',
      width: 120,
    },
    {
      title: 'ms.apiTestDebug.paramValue',
      dataIndex: 'value',
      slotName: 'value',
      width: 240,
    },
    {
      title: 'ms.apiTestDebug.paramLengthRange',
      dataIndex: 'lengthRange',
      slotName: 'lengthRange',
      width: 200,
    },
    {
      title: 'ms.apiTestDebug.desc',
      dataIndex: 'desc',
      slotName: 'desc',
    },
    {
      title: 'ms.apiTestDebug.encode',
      dataIndex: 'encode',
      slotName: 'encode',
      titleSlotName: 'encodeTitle',
    },
    {
      title: '',
      slotName: 'operation',
      fixed: 'right',
      width: 80,
    },
  ];

  const heightUsed = ref<number | undefined>(undefined);

  watch(
    () => props.layout,
    (val) => {
      heightUsed.value = val === 'horizontal' ? 422 : 422 + props.secondBoxHeight;
    },
    {
      immediate: true,
    }
  );

  watch(
    () => props.secondBoxHeight,
    (val) => {
      if (props.layout === 'vertical') {
        heightUsed.value = 422 + val;
      }
    },
    {
      immediate: true,
    }
  );

  const format = ref(RequestBodyFormat.NONE);
  const showParamTable = computed(() => {
    return [RequestBodyFormat.FORM_DATA, RequestBodyFormat.X_WWW_FORM_URLENCODED].includes(format.value);
  });
  const currentTableParams = computed({
    get() {
      if (format.value === RequestBodyFormat.FORM_DATA) {
        return innerParams.value.formData;
      }
      return innerParams.value.formUrlEncode;
    },
    set(val) {
      if (format.value === RequestBodyFormat.FORM_DATA) {
        innerParams.value.formData = val;
      } else {
        innerParams.value.formUrlEncode = val;
      }
    },
  });
  const currentBodyCode = computed({
    get() {
      if (format.value === RequestBodyFormat.JSON) {
        return innerParams.value.json;
      }
      if (format.value === RequestBodyFormat.XML) {
        return innerParams.value.xml;
      }
      return innerParams.value.raw;
    },
    set(val) {
      if (format.value === RequestBodyFormat.JSON) {
        innerParams.value.json = val;
      } else if (format.value === RequestBodyFormat.XML) {
        innerParams.value.xml = val;
      } else {
        innerParams.value.raw = val;
      }
    },
  });
  const currentCodeLanguage = computed(() => {
    if (format.value === RequestBodyFormat.JSON) {
      return 'json';
    }
    if (format.value === RequestBodyFormat.XML) {
      return 'xml';
    }
    return 'plaintext';
  });

  function formatChange() {
    console.log('formatChange', format.value);
  }

  /**
   * 批量参数代码转换为参数表格数据
   */
  function handleBatchParamApply(resultArr: any[]) {
    if (resultArr.length < currentTableParams.value.length) {
      currentTableParams.value.splice(0, currentTableParams.value.length - 1, ...resultArr);
    } else {
      currentTableParams.value = [...resultArr, currentTableParams.value[currentTableParams.value.length - 1]];
    }
    emit('change');
  }

  function handleParamTableChange(resultArr: any[]) {
    currentTableParams.value = [...resultArr];
    emit('change');
  }
</script>

<style lang="less" scoped></style>
