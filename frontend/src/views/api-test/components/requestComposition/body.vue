<template>
  <div class="mb-[8px] flex items-center justify-between">
    <a-radio-group
      v-model:model-value="innerParams.bodyType"
      type="button"
      size="small"
      @change="(val) => changeBodyFormat(val as RequestBodyFormat)"
    >
      <a-radio v-for="item of RequestBodyFormat" :key="item" :value="item">
        {{ requestBodyTypeMap[item] }}
      </a-radio>
    </a-radio-group>
    <batchAddKeyVal
      v-if="showParamTable"
      :params="currentTableParams"
      :default-param-item="defaultParamItem"
      @apply="handleBatchParamApply"
    />
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
    :default-param-item="defaultParamItem"
    :upload-temp-file-api="props.uploadTempFileApi"
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
    :default-param-item="defaultParamItem"
    @change="handleParamTableChange"
  />
  <div v-else-if="innerParams.bodyType === RequestBodyFormat.BINARY">
    <div class="mb-[16px] flex justify-between gap-[8px] bg-[var(--color-text-n9)] p-[12px]">
      <a-input
        v-model:model-value="innerParams.binaryBody.description"
        :placeholder="t('common.desc')"
        :max-length="255"
      />
      <MsAddAttachment
        v-model:file-list="fileList"
        mode="input"
        :multiple="false"
        :fields="{
          id: 'fileId',
          name: 'fileName',
        }"
        @change="handleFileChange"
      />
    </div>
    <!-- <div class="flex items-center">
      <a-switch v-model:model-value="innerParams.binarySend" class="mr-[8px]" size="small" type="line"></a-switch>
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
    </div> -->
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
  import { MsFileItem } from '@/components/pure/ms-upload/types';
  import MsAddAttachment from '@/components/business/ms-add-attachment/index.vue';
  import batchAddKeyVal from '@/views/api-test/components/batchAddKeyVal.vue';
  import paramTable, { type ParamTableColumn } from '@/views/api-test/components/paramTable.vue';

  import { requestBodyTypeMap } from '@/config/apiTest';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import { ExecuteBody, ExecuteRequestFormBodyFormValue } from '@/models/apiTest/debug';
  import { RequestBodyFormat, RequestContentTypeEnum, RequestParamsType } from '@/enums/apiEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const props = defineProps<{
    params: ExecuteBody;
    layout: 'horizontal' | 'vertical';
    secondBoxHeight: number;
    uploadTempFileApi?: (...args) => Promise<any>; // 上传临时文件接口
  }>();
  const emit = defineEmits<{
    (e: 'update:params', value: any[]): void;
    (e: 'change'): void;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();

  const innerParams = useVModel(props, 'params', emit);
  const defaultParamItem: ExecuteRequestFormBodyFormValue = {
    key: '',
    value: '',
    paramType: RequestParamsType.STRING,
    description: '',
    required: false,
    maxLength: undefined,
    minLength: undefined,
    encode: false,
    enable: true,
    contentType: RequestContentTypeEnum.TEXT,
    files: [],
  };
  const fileList = ref<any[]>(
    innerParams.value.binaryBody && innerParams.value.binaryBody.file ? [innerParams.value.binaryBody.file] : []
  );

  async function handleFileChange(files: MsFileItem[]) {
    if (files.length === 0) {
      innerParams.value.binaryBody.file = undefined;
      return;
    }
    if (!props.uploadTempFileApi) return;
    try {
      if (fileList.value[0]?.local) {
        appStore.showLoading();
        const res = await props.uploadTempFileApi(fileList.value[0].file);
        innerParams.value.binaryBody.file = {
          ...fileList.value[0],
          fileId: res.data,
          fileName: fileList.value[0]?.name || '',
          local: true,
        };
        appStore.hideLoading();
      } else {
        innerParams.value.binaryBody.file = {
          ...fileList.value[0],
          fileId: fileList.value[0].uid,
          fileName: fileList.value[0]?.name || '',
          local: false,
        };
      }
      emit('change');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const typeOptions = computed(() => {
    const fullOptions = Object.values(RequestParamsType).map((val) => ({
      label: val,
      value: val,
    }));
    if (innerParams.value.bodyType === RequestBodyFormat.FORM_DATA) {
      return fullOptions;
    }
    return fullOptions.filter((item) => item.value !== RequestParamsType.FILE && item.value !== RequestParamsType.JSON);
  });
  const columns = computed<ParamTableColumn[]>(() => {
    return [
      {
        title: 'apiTestDebug.paramName',
        dataIndex: 'key',
        slotName: 'key',
      },
      {
        title: 'apiTestDebug.paramType',
        dataIndex: 'paramType',
        slotName: 'paramType',
        hasRequired: true,
        typeOptions: typeOptions.value,
        width: 120,
      },
      {
        title: 'apiTestDebug.paramValue',
        dataIndex: 'value',
        slotName: 'value',
        multiple: true,
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
        title: 'apiTestDebug.encode',
        dataIndex: 'encode',
        slotName: 'encode',
        titleSlotName: 'encodeTitle',
        width: 80,
      },
      {
        title: 'apiTestDebug.desc',
        dataIndex: 'description',
        slotName: 'description',
      },
      {
        title: '',
        slotName: 'operation',
        fixed: 'right',
        format: innerParams.value.bodyType,
        width: innerParams.value.bodyType === RequestBodyFormat.FORM_DATA ? 90 : 50,
      },
    ];
  });

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

  function changeBodyFormat(val: RequestBodyFormat) {
    innerParams.value.bodyType = val;
    emit('change');
  }
</script>

<style lang="less" scoped></style>
