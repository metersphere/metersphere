<template>
  <div class="mb-[8px] flex items-center justify-between">
    <a-radio-group
      v-model:model-value="innerParams.bodyType"
      type="button"
      size="small"
      :disabled="props.disabledBodyType"
      @change="(val) => changeBodyFormat(val as RequestBodyFormat)"
    >
      <a-radio v-for="item of RequestBodyFormat" :key="item" :value="item">
        {{ requestBodyTypeMap[item] }}
      </a-radio>
    </a-radio-group>
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
    :show-setting="true"
    :table-key="TableKeyEnum.API_TEST_DEBUG_FORM_DATA"
    :default-param-item="defaultBodyParamsItem"
    :upload-temp-file-api="props.uploadTempFileApi"
    :file-save-as-source-id="props.fileSaveAsSourceId"
    :file-save-as-api="props.fileSaveAsApi"
    :file-module-options-api="props.fileModuleOptionsApi"
    @change="handleParamTableChange"
    @batch-add="() => (batchAddKeyValVisible = true)"
  />
  <paramTable
    v-else-if="innerParams.bodyType === RequestBodyFormat.WWW_FORM"
    :disabled-param-value="props.disabledParamValue"
    :disabled-except-param="props.disabledExceptParam"
    :params="currentTableParams"
    :draggable="!props.disabledExceptParam"
    :scroll="{ minWidth: 1160 }"
    :columns="columns"
    :show-setting="true"
    :table-key="TableKeyEnum.API_TEST_DEBUG_FORM_URL_ENCODE"
    :default-param-item="defaultBodyParamsItem"
    @change="handleParamTableChange"
    @batch-add="() => (batchAddKeyValVisible = true)"
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
  <a-spin v-else :loading="bodyLoading" class="block h-[calc(100%-34px)]">
    <div
      v-if="innerParams.bodyType === RequestBodyFormat.JSON && !props.hideJsonSchema"
      class="mb-[8px] flex items-center justify-between"
    >
      <div class="flex items-center gap-[8px]">
        <MsButton
          type="text"
          class="!mr-0"
          :class="
            innerParams.jsonBody.enableJsonSchema
              ? 'font-medium !text-[rgb(var(--primary-5))]'
              : '!text-[var(--color-text-4)]'
          "
          @click="handleChangeJsonType('Schema')"
          >Schema</MsButton
        >
        <a-divider :margin="0" direction="vertical"></a-divider>
        <MsButton
          type="text"
          class="!mr-0"
          :class="
            !innerParams.jsonBody.enableJsonSchema
              ? 'font-medium !text-[rgb(var(--primary-5))]'
              : '!text-[var(--color-text-4)]'
          "
          @click="handleChangeJsonType('Json')"
          >Json</MsButton
        >
      </div>
      <a-button
        v-show="innerParams.jsonBody.enableJsonSchema"
        type="outline"
        class="arco-btn-outline--secondary px-[8px]"
        size="small"
        @click="previewJsonSchema"
      >
        <div class="flex items-center gap-[8px]">
          <icon-eye />
          {{ t('common.preview') }}
        </div>
      </a-button>
    </div>
    <MsJsonSchema
      v-if="innerParams.jsonBody.enableJsonSchema && innerParams.bodyType === RequestBodyFormat.JSON"
      ref="jsonSchemaRef"
      v-model:data="innerParams.jsonBody.jsonSchemaTableData"
      v-model:selectedKeys="innerParams.jsonBody.jsonSchemaTableSelectedRowKeys"
      :disabled="props.disabledExceptParam"
      @change="() => emit('change')"
    />
    <MsCodeEditor
      v-else
      v-model:model-value="currentBodyCode"
      :read-only="props.disabledExceptParam"
      theme="vs"
      height="100%"
      :show-full-screen="false"
      :show-theme-change="false"
      :show-code-format="!(props.disabledExceptParam || props.disabledParamValue)"
      :language="currentCodeLanguage"
      is-adaptive
    >
      <template v-if="!props.hideJsonSchema" #leftTitle>
        <a-popconfirm
          v-if="
            innerParams.bodyType === RequestBodyFormat.JSON && !props.disabledExceptParam && !props.disabledParamValue
          "
          v-model:popup-visible="autoMakeJsonTipVisible"
          class="ms-pop-confirm--hidden-cancel"
          :ok-text="t('common.gotIt')"
          :ok-button-props="{
            size: 'small',
          }"
          position="bl"
          :disabled="getIsVisited()"
          @ok="addVisited"
        >
          <a-button type="text" class="arco-btn-text--primary gap-[4px] p-[2px_6px]" size="small" @click="autoMakeJson">
            <MsIcon :size="14" type="icon-icon_press" />
            <div class="text-[12px]">{{ t('apiTestManagement.autoMake') }}</div>
          </a-button>
          <template #icon>
            <icon-info-circle-fill />
          </template>
          <template #content>
            <div class="flex flex-col gap-[8px]">
              <div class="font-medium">{{ t('apiTestManagement.autoMake') }}</div>
              <div class="text-[var(--color-text-2)]">{{ t('apiTestDebug.autoMakeJsonTip') }}</div>
            </div>
          </template>
        </a-popconfirm>
      </template>
    </MsCodeEditor>
  </a-spin>
  <batchAddKeyVal
    v-if="showParamTable"
    v-model:visible="batchAddKeyValVisible"
    :disabled="props.disabledExceptParam"
    :params="currentTableParams"
    :default-param-item="defaultBodyParamsItem"
    :accept-types="innerParams.bodyType === RequestBodyFormat.WWW_FORM ? wwwFormParamsTypes : undefined"
    has-standard
    @apply="handleBatchParamApply"
  />
</template>

<script setup lang="ts">
  import { Message, TableColumnData } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsJsonSchema from '@/components/pure/ms-json-schema/index.vue';
  import { parseSchemaToJsonSchemaTableData, parseTableDataToJsonSchema } from '@/components/pure/ms-json-schema/utils';
  import { MsFileItem } from '@/components/pure/ms-upload/types';
  import MsAddAttachment from '@/components/business/ms-add-attachment/index.vue';
  import batchAddKeyVal from '@/views/api-test/components/batchAddKeyVal.vue';
  import paramTable, { type ParamTableColumn } from '@/views/api-test/components/paramTable.vue';

  import { jsonSchemaAutoGenerate } from '@/api/modules/api-test/management';
  import { requestBodyTypeMap } from '@/config/apiTest';
  import { useI18n } from '@/hooks/useI18n';
  import useVisit from '@/hooks/useVisit';
  import useAppStore from '@/store/modules/app';

  import { ExecuteBody } from '@/models/apiTest/common';
  import { ModuleTreeNode, TransferFileParams } from '@/models/common';
  import { RequestBodyFormat, RequestParamsType } from '@/enums/apiEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import { filterKeyValParams } from '../utils';
  import { defaultBodyParamsItem } from '@/views/api-test/components/config';

  const props = defineProps<{
    disabledBodyType?: boolean; // 禁用body类型切换
    disabledParamValue?: boolean; // 参数值禁用
    disabledExceptParam?: boolean; // 除了可以修改参数值其他都禁用
    isDebug?: boolean; // 是否调试模式
    hideJsonSchema?: boolean; // 隐藏json schema
    isCase?: boolean; // 是否是 case
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
  const visitedKey = 'apiTestAutoMakeJsonTip';
  const { addVisited, getIsVisited } = useVisit(visitedKey);

  const innerParams = defineModel<ExecuteBody>('params', {
    required: true,
  });
  const bodyLoading = ref(false);

  const batchAddKeyValVisible = ref(false);
  const fileList = ref<MsFileItem[]>([]);

  watch(
    () => innerParams.value.binaryBody?.file,
    () => {
      if (innerParams.value.binaryBody?.file) {
        fileList.value = [innerParams.value.binaryBody.file as unknown as MsFileItem];
      }
    },
    {
      immediate: true,
      deep: true,
    }
  );

  watch(
    () => innerParams.value.formDataBody.formValues,
    () => {
      if (innerParams.value.formDataBody.formValues.length > 0) {
        let hasNullFiles = false;
        const newValues = innerParams.value.formDataBody.formValues.map((item) => {
          // 导入的接口files字段可能为 null，兜底处理
          hasNullFiles = item.files === null;
          return {
            ...item,
            files: item.files ? item.files : [],
          };
        });
        if (hasNullFiles) {
          innerParams.value.formDataBody.formValues = newValues;
        }
      }
    },
    {
      immediate: true,
    }
  );

  watch(
    () => props.isDebug,
    (val) => {
      if (val) {
        innerParams.value.jsonBody.enableJsonSchema = false;
      }
    }
  );

  watch(
    () => [props.hideJsonSchema, props.isCase],
    () => {
      if (props.hideJsonSchema) {
        innerParams.value.jsonBody.enableJsonSchema = false;
        return;
      }
      if (props.isCase) {
        innerParams.value.jsonBody.enableJsonSchema = false;
      }
    }
  );

  watchEffect(() => {
    if (
      innerParams.value.jsonBody.jsonSchema &&
      (!innerParams.value.jsonBody.jsonSchemaTableData || innerParams.value.jsonBody.jsonSchemaTableData.length === 0)
    ) {
      const { result, ids } = parseSchemaToJsonSchemaTableData(innerParams.value.jsonBody.jsonSchema);
      innerParams.value.jsonBody.jsonSchemaTableData = result;
      innerParams.value.jsonBody.jsonSchemaTableSelectedRowKeys = ids;
    } else if (
      !innerParams.value.jsonBody.jsonSchemaTableData ||
      innerParams.value.jsonBody.jsonSchemaTableData.length === 0
    ) {
      innerParams.value.jsonBody.jsonSchemaTableData = [];
      innerParams.value.jsonBody.jsonSchemaTableSelectedRowKeys = [];
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
          ...files[0],
          fileId: files[0]?.uid,
          fileName: files[0]?.originalName || '',
          fileAlias: files[0]?.name || '',
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

  const options = computed(() => {
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
        needValidRepeat: true,
        slotName: 'key',
        width: 240,
      },
      {
        title: 'apiTestDebug.paramType',
        dataIndex: 'paramType',
        slotName: 'paramType',
        titleSlotName: 'typeTitle',
        hasRequired: true,
        options: options.value,
        width: 130,
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
        title: 'common.desc',
        dataIndex: 'description',
        slotName: 'description',
      },
      ...(props.disabledExceptParam
        ? []
        : [
            {
              title: '',
              dataIndex: 'operation',
              slotName: 'operation',
              titleSlotName: 'batchAddTitle',
              fixed: 'right' as TableColumnData['fixed'],
              format: innerParams.value.bodyType,
              // width: innerParams.value.bodyType === RequestBodyFormat.FORM_DATA ? 100 : 100,
              width: 100,
            },
          ]),
    ];
  });

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

  const jsonSchemaRef = ref<InstanceType<typeof MsJsonSchema>>();

  function previewJsonSchema() {
    if (innerParams.value.jsonBody.enableJsonSchema) {
      jsonSchemaRef.value?.previewSchema();
    }
  }

  /**
   * 自动转换json schema为json
   */
  async function autoMakeJson() {
    if (!innerParams.value.jsonBody.enableJsonSchema) {
      try {
        bodyLoading.value = true;
        const schema = parseTableDataToJsonSchema(innerParams.value.jsonBody.jsonSchemaTableData?.[0]);
        if (schema) {
          // 再将 json schema 转换为 json 格式
          const res = await jsonSchemaAutoGenerate(schema);
          innerParams.value.jsonBody.jsonValue = res;
          emit('change');
        } else {
          Message.warning(t('apiTestManagement.pleaseInputJsonSchema'));
        }
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      } finally {
        bodyLoading.value = false;
      }
    }
  }

  // 当前显示的代码
  const currentBodyCode = computed({
    get() {
      if (innerParams.value.bodyType === RequestBodyFormat.JSON) {
        return innerParams.value.jsonBody.jsonValue || '';
      }
      if (innerParams.value.bodyType === RequestBodyFormat.XML) {
        return innerParams.value.xmlBody.value || '';
      }
      return innerParams.value.rawBody.value || '';
    },
    set(val) {
      if (innerParams.value.bodyType === RequestBodyFormat.JSON) {
        innerParams.value.jsonBody.jsonValue = val || '';
      } else if (innerParams.value.bodyType === RequestBodyFormat.XML) {
        innerParams.value.xmlBody.value = val || '';
      } else {
        innerParams.value.rawBody.value = val || '';
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

  const wwwFormParamsTypes = Object.values(RequestParamsType).filter(
    (val) => ![RequestParamsType.JSON, RequestParamsType.FILE].includes(val)
  );
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

  function handleParamTableChange(resultArr: any[], isInit?: boolean) {
    currentTableParams.value = [...resultArr];
    if (!isInit) {
      emit('change');
    }
  }

  function changeBodyFormat(val: RequestBodyFormat) {
    innerParams.value.bodyType = val;
    emit('change');
  }

  const autoMakeJsonTipVisible = ref(false);
  function handleChangeJsonType(type: 'Schema' | 'Json') {
    innerParams.value.jsonBody.enableJsonSchema = type === 'Schema';
    if (!getIsVisited()) {
      autoMakeJsonTipVisible.value = true;
    }
  }
</script>

<style lang="less" scoped></style>
