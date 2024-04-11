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
      :disabled="props.disabledExceptParam"
      :params="currentTableParams"
      :default-param-item="defaultBodyParamsItem"
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
    :disabled-param-value="props.disabledParamValue"
    :disabled-except-param="props.disabledExceptParam"
    :params="currentTableParams"
    :draggable="!props.disabledExceptParam"
    :scroll="{ minWidth: 1160 }"
    :columns="columns"
    :height-used="heightUsed"
    :show-setting="true"
    :table-key="TableKeyEnum.API_TEST_DEBUG_FORM_DATA"
    :default-param-item="defaultBodyParamsItem"
    :upload-temp-file-api="props.uploadTempFileApi"
    :file-save-as-source-id="props.fileSaveAsSourceId"
    :file-save-as-api="props.fileSaveAsApi"
    :file-module-options-api="props.fileModuleOptionsApi"
    @change="handleParamTableChange"
  />
  <paramTable
    v-else-if="innerParams.bodyType === RequestBodyFormat.WWW_FORM"
    :disabled-param-value="props.disabledParamValue"
    :disabled-except-param="props.disabledExceptParam"
    :params="currentTableParams"
    :draggable="!props.disabledExceptParam"
    :scroll="{ minWidth: 1160 }"
    :columns="columns"
    :height-used="heightUsed"
    :show-setting="true"
    :table-key="TableKeyEnum.API_TEST_DEBUG_FORM_URL_ENCODE"
    :default-param-item="defaultBodyParamsItem"
    @change="handleParamTableChange"
  />
  <div v-else-if="innerParams.bodyType === RequestBodyFormat.BINARY">
    <div class="mb-[16px] flex justify-between gap-[8px] bg-[var(--color-text-n9)] p-[12px]">
      <a-input
        v-model:model-value="innerParams.binaryBody.description"
        :disabled="props.disabledExceptParam"
        :placeholder="t('common.desc')"
        :max-length="255"
      />
      <MsAddAttachment
        v-model:file-list="fileList"
        :disabled="props.disabledParamValue"
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
  <div v-else class="flex h-[calc(100%-34px)]">
    <MsCodeEditor
      v-model:model-value="currentBodyCode"
      :read-only="props.disabledExceptParam"
      class="flex-1"
      theme="vs"
      height="100%"
      :show-full-screen="false"
      :show-theme-change="false"
      :show-code-format="true"
      :language="currentCodeLanguage"
    >
    </MsCodeEditor>
  </div>
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';
  import { TableColumnData } from '@arco-design/web-vue';

  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';
  import { MsFileItem } from '@/components/pure/ms-upload/types';
  import MsAddAttachment from '@/components/business/ms-add-attachment/index.vue';
  import batchAddKeyVal from '@/views/api-test/components/batchAddKeyVal.vue';
  import paramTable, { type ParamTableColumn } from '@/views/api-test/components/paramTable.vue';

  import { requestBodyTypeMap } from '@/config/apiTest';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import { ExecuteBody } from '@/models/apiTest/common';
  import { ModuleTreeNode, TransferFileParams } from '@/models/common';
  import { RequestBodyFormat, RequestParamsType } from '@/enums/apiEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import { filterKeyValParams } from '../utils';
  import { defaultBodyParamsItem } from '@/views/api-test/components/config';

  const props = defineProps<{
    params: ExecuteBody;
    layout: 'horizontal' | 'vertical';
    disabledParamValue?: boolean; // 参数值禁用
    disabledExceptParam?: boolean; // 除了可以修改参数值其他都禁用
    secondBoxHeight: number;
    isDrawer?: boolean;
    uploadTempFileApi?: (file: File) => Promise<any>; // 上传临时文件接口
    fileSaveAsSourceId?: string | number; // 文件转存关联的资源id
    fileSaveAsApi?: (params: TransferFileParams) => Promise<string>; // 文件转存接口
    fileModuleOptionsApi?: (projectId: string) => Promise<ModuleTreeNode[]>; // 文件转存目录下拉框接口
  }>();
  const emit = defineEmits<{
    (e: 'update:params', value: any[]): void;
    (e: 'change'): void;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();

  const innerParams = useVModel(props, 'params', emit);
  const fileList = ref<MsFileItem[]>([]);

  onBeforeMount(() => {
    if (innerParams.value.binaryBody && innerParams.value.binaryBody.file) {
      fileList.value = [innerParams.value.binaryBody.file as unknown as MsFileItem];
    }
  });

  async function handleFileChange(files: MsFileItem[], file?: MsFileItem) {
    try {
      if (file?.local && file.file && props.uploadTempFileApi) {
        // 本地上传
        appStore.showLoading();
        const res = await props.uploadTempFileApi(file.file);
        innerParams.value.binaryBody.file = {
          ...file,
          fileId: res.data,
          fileName: file?.name || '',
          fileAlias: file?.name || '',
          local: true,
        };
        appStore.hideLoading();
      } else {
        // 关联文件
        innerParams.value.binaryBody.file = {
          ...fileList.value[0],
          fileId: fileList.value[0]?.uid,
          fileName: fileList.value[0]?.originalName || '',
          fileAlias: fileList.value[0]?.name || '',
          local: false,
        };
      }
      if (innerParams.value.binaryBody.file && !innerParams.value.binaryBody.file.fileId) {
        innerParams.value.binaryBody.file = undefined;
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
        width: 240,
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
      ...(innerParams.value.bodyType !== RequestBodyFormat.FORM_DATA
        ? [
            {
              title: 'apiTestDebug.encode',
              dataIndex: 'encode',
              slotName: 'encode',
              titleSlotName: 'encodeTitle',
              width: 80,
            },
          ]
        : []),
      {
        title: 'apiTestDebug.desc',
        dataIndex: 'description',
        slotName: 'description',
      },
      ...(props.disabledExceptParam
        ? []
        : [
            {
              title: '',
              slotName: 'operation',
              fixed: 'right' as TableColumnData['fixed'],
              format: innerParams.value.bodyType,
              width: innerParams.value.bodyType === RequestBodyFormat.FORM_DATA ? 65 : 35,
            },
          ]),
    ];
  });

  const heightUsed = ref<number | undefined>(undefined);

  watch(
    () => props.layout,
    (val) => {
      const otherHeight = props.isDrawer ? 328 : 430;
      heightUsed.value = val === 'horizontal' ? otherHeight : otherHeight + props.secondBoxHeight;
    },
    {
      immediate: true,
    }
  );

  watch(
    () => props.secondBoxHeight,
    (val) => {
      if (props.layout === 'vertical') {
        heightUsed.value = (props.isDrawer ? 328 : 430) + val;
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
    const files = currentTableParams.value.filter((item) => item.paramType === RequestParamsType.FILE);
    const filterResult = filterKeyValParams(currentTableParams.value, defaultBodyParamsItem);
    if (filterResult.lastDataIsDefault) {
      currentTableParams.value = [
        ...files,
        ...resultArr,
        currentTableParams.value[currentTableParams.value.length - 1],
      ].filter(Boolean);
    } else {
      currentTableParams.value = [...files, ...resultArr].filter(Boolean);
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
