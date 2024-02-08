<template>
  <div class="mb-[8px] flex items-center justify-between">
    <a-radio-group
      v-model:model-value="innerParams.bodyType"
      type="button"
      size="small"
      @change="(val) => changeBodyFormat(val as string)"
    >
      <a-radio v-for="item of RequestBodyFormat" :key="item" :value="item">{{ requestBodyTypeMap[item] }}</a-radio>
    </a-radio-group>
    <batchAddKeyVal v-if="showParamTable" :params="currentTableParams" @apply="handleBatchParamApply" />
  </div>
  <div
    v-if="innerParams.bodyType === RequestBodyFormat.NONE"
    class="flex h-[100px] items-center justify-center rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)] text-[var(--color-text-4)]"
  >
    {{ t('apiTestDebug.noneBody') }}
  </div>
  <paramTable
    v-else-if="innerParams.bodyType === RequestBodyFormat.FORM_DATA"
    v-model:params="currentTableParams"
    :scroll="{ minWidth: 1160 }"
    :columns="columns"
    :height-used="heightUsed"
    :show-setting="true"
    :table-key="TableKeyEnum.API_TEST_DEBUG_FORM_DATA"
    @change="handleParamTableChange"
  />
  <paramTable
    v-else-if="innerParams.bodyType === RequestBodyFormat.WWW_FORM"
    v-model:params="currentTableParams"
    :scroll="{ minWidth: 1160 }"
    :columns="columns"
    :height-used="heightUsed"
    :show-setting="true"
    :table-key="TableKeyEnum.API_TEST_DEBUG_FORM_URL_ENCODE"
    @change="handleParamTableChange"
  />
  <div v-else-if="innerParams.bodyType === RequestBodyFormat.BINARY">
    <div class="mb-[16px] flex justify-between gap-[8px] bg-[var(--color-text-n9)] p-[12px]">
      <a-input
        v-model:model-value="innerParams.binaryBody.description"
        :placeholder="t('common.desc')"
        :max-length="255"
      />
    </div>
    <div class="flex items-center">
      <!-- <a-switch v-model:model-value="innerParams.binarySend" class="mr-[8px]" size="small" type="line"></a-switch> -->
      <span>{{ t('apiTestDebug.sendAsMainText') }}</span>
      <a-tooltip position="right">
        <template #content>
          <div>{{ t('apiTestDebug.sendAsMainTextTip1') }}</div>
          <div>{{ t('apiTestDebug.sendAsMainTextTip2') }}</div>
        </template>
        <icon-question-circle
          class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
          size="16"
        />
      </a-tooltip>
    </div>
  </div>
  <div v-else class="flex h-[calc(100%-100px)]">
    <MsCodeEditor
      v-model:model-value="currentBodyCode"
      class="flex-1"
      theme="vs-dark"
      height="100%"
      :show-full-screen="false"
      :language="currentCodeLanguage"
    >
      <template #title>
        <div class="flex flex-col">
          <div class="text-[12px] leading-[16px] text-[var(--color-text-4)]">
            {{ t('apiTestDebug.batchAddParamsTip') }}
          </div>
          <div class="text-[12px] leading-[16px] text-[var(--color-text-4)]">
            {{ t('apiTestDebug.batchAddParamsTip2') }}
          </div>
        </div>
      </template>
    </MsCodeEditor>
  </div>
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';
  import batchAddKeyVal from '@/views/api-test/components/batchAddKeyVal.vue';
  import paramTable, { type ParamTableColumn } from '@/views/api-test/components/paramTable.vue';

  import { requestBodyTypeMap } from '@/config/apiTest';
  import { useI18n } from '@/hooks/useI18n';

  import { ExecuteBody } from '@/models/apiTest/debug';
  import { RequestBodyFormat, RequestParamsType } from '@/enums/apiEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const props = defineProps<{
    params: ExecuteBody;
    layout: 'horizontal' | 'vertical';
    secondBoxHeight: number;
  }>();
  const emit = defineEmits<{
    (e: 'update:params', value: any[]): void;
    (e: 'change'): void;
  }>();

  const { t } = useI18n();

  const innerParams = useVModel(props, 'params', emit);

  const columns = computed<ParamTableColumn[]>(() => [
    {
      title: 'apiTestDebug.paramName',
      dataIndex: 'name',
      slotName: 'name',
    },
    {
      title: 'apiTestDebug.paramType',
      dataIndex: 'type',
      slotName: 'type',
      hasRequired: true,
      typeOptions: Object.keys(RequestParamsType).map((key) => ({
        label: RequestParamsType[key],
        value: key,
      })),
      width: 120,
    },
    {
      title: 'apiTestDebug.paramValue',
      dataIndex: 'value',
      slotName: 'value',
      width: 240,
    },
    {
      title: 'apiTestDebug.paramLengthRange',
      dataIndex: 'lengthRange',
      slotName: 'lengthRange',
      align: 'center',
      width: 200,
    },
    {
      title: 'apiTestDebug.desc',
      dataIndex: 'description',
      slotName: 'description',
    },
    {
      title: 'apiTestDebug.encode',
      dataIndex: 'encode',
      slotName: 'encode',
      titleSlotName: 'encodeTitle',
    },
    {
      title: '',
      slotName: 'operation',
      fixed: 'right',
      format: innerParams.value.bodyType,
      width: innerParams.value.bodyType === RequestBodyFormat.FORM_DATA ? 90 : 50,
    },
  ]);

  const heightUsed = ref<number | undefined>(undefined);

  watch(
    () => props.layout,
    (val) => {
      heightUsed.value = val === 'horizontal' ? 428 : 428 + props.secondBoxHeight;
    },
    {
      immediate: true,
    }
  );

  watch(
    () => props.secondBoxHeight,
    (val) => {
      if (props.layout === 'vertical') {
        heightUsed.value = 428 + val;
      }
    },
    {
      immediate: true,
    }
  );

  const showParamTable = computed(() => {
    // 仅当格式为FORM_DATA或X_WWW_FORM_URLENCODED时，显示参数表格
    return [RequestBodyFormat.FORM_DATA, RequestBodyFormat.WWW_FORM].includes(innerParams.value.bodyType);
  });
  // 当前显示的参数表格数据
  const currentTableParams = computed({
    get() {
      if (innerParams.value.bodyType === RequestBodyFormat.FORM_DATA) {
        return innerParams.value.formDataBody.formValues;
      }
      return innerParams.value.wwwFormBody.formValues;
    },
    set(val) {
      if (innerParams.value.bodyType === RequestBodyFormat.FORM_DATA) {
        innerParams.value.formDataBody.formValues = val;
      } else {
        innerParams.value.wwwFormBody.formValues = val;
      }
    },
  });
  // 当前显示的代码
  const currentBodyCode = computed({
    get() {
      if (innerParams.value.bodyType === RequestBodyFormat.JSON) {
        return innerParams.value.jsonBody.jsonValue;
      }
      if (innerParams.value.bodyType === RequestBodyFormat.XML) {
        return innerParams.value.xmlBody.value;
      }
      return innerParams.value.rawBody.value;
    },
    set(val) {
      if (innerParams.value.bodyType === RequestBodyFormat.JSON) {
        innerParams.value.jsonBody.jsonValue = val;
      } else if (innerParams.value.bodyType === RequestBodyFormat.XML) {
        innerParams.value.xmlBody.value = val;
      } else {
        innerParams.value.rawBody.value = val;
      }
    },
  });
  // 当前代码编辑器的语言
  const currentCodeLanguage = computed(() => {
    if (innerParams.value.bodyType === RequestBodyFormat.JSON) {
      return LanguageEnum.JSON;
    }
    if (innerParams.value.bodyType === RequestBodyFormat.XML) {
      return LanguageEnum.XML;
    }
    return LanguageEnum.PLAINTEXT;
  });

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

  function changeBodyFormat(val: string) {
    innerParams.value.bodyType = val as RequestBodyFormat;
    emit('change');
  }
</script>

<style lang="less" scoped></style>
