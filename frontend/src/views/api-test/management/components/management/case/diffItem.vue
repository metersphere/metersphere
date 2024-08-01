<template>
  <div v-if="showDiff(RequestComposition.HEADER)" class="title">{{ t('apiTestDebug.header') }}</div>
  <div
    v-if="showDiff(RequestComposition.HEADER) && hiddenEmptyTable(RequestComposition.HEADER)"
    :style="{ 'padding-bottom': `${getBottomDistance(RequestComposition.HEADER)}px` }"
  >
    <MsFormTable
      :columns="headerColumns"
      :data="previewDetail?.headers?.filter((e) => e.key !== '') || []"
      :selectable="false"
      :diff-mode="props.mode"
    />
  </div>
  <div
    v-if="showDiff(RequestComposition.HEADER) && !hiddenEmptyTable(RequestComposition.HEADER)"
    class="not-setting-data"
    :style="{ height: `${getBottomDistance(RequestComposition.HEADER, true)}px` }"
  >
    {{ t('case.notSetData') }}
  </div>
  <div v-if="showDiff(RequestComposition.QUERY)" class="title">Query</div>
  <div
    v-if="showDiff(RequestComposition.QUERY) && hiddenEmptyTable(RequestComposition.QUERY)"
    :style="{ 'padding-bottom': `${getBottomDistance(RequestComposition.QUERY)}px` }"
  >
    <MsFormTable
      :columns="queryRestColumns"
      :data="previewDetail?.query?.filter((e) => e.key !== '') || []"
      :selectable="false"
      :diff-mode="props.mode"
    />
  </div>
  <div
    v-if="showDiff(RequestComposition.QUERY) && !hiddenEmptyTable(RequestComposition.QUERY)"
    class="not-setting-data"
    :style="{ height: `${getBottomDistance(RequestComposition.QUERY, true)}px` }"
  >
    {{ t('case.notSetData') }}
  </div>
  <div v-if="showDiff(RequestComposition.REST)" class="title">REST</div>
  <div
    v-if="showDiff(RequestComposition.REST)"
    :style="{ 'padding-bottom': `${getBottomDistance(RequestComposition.REST)}px` }"
  >
    <MsFormTable
      :columns="queryRestColumns?.filter((e) => e.key !== '')"
      :data="previewDetail?.rest || []"
      :selectable="false"
      :diff-mode="props.mode"
    />
  </div>
  <div
    v-if="showDiff(RequestComposition.REST) && !hiddenEmptyTable(RequestComposition.REST)"
    class="not-setting-data"
    :style="{ height: `${getBottomDistance(RequestComposition.REST, true)}px` }"
  >
    {{ t('case.notSetData') }}
  </div>

  <div class="title flex items-center justify-between">
    <div class="detail-item-title-text">
      {{ `${t('apiTestManagement.requestBody')}-${previewDetail?.body?.bodyType}` }}
    </div>
    <a-radio-group
      v-if="previewDetail?.body?.bodyType === RequestBodyFormat.JSON && props.isApi"
      v-model:model-value="bodyShowType"
      type="button"
      size="mini"
    >
      <a-radio value="schema">Schema</a-radio>
      <a-radio value="json">JSON</a-radio>
    </a-radio-group>
  </div>
  <div
    v-if="
      (previewDetail?.body?.bodyType === RequestBodyFormat.FORM_DATA ||
        previewDetail?.body?.bodyType === RequestBodyFormat.WWW_FORM) &&
      showDiff(previewDetail?.body?.bodyType) &&
      hiddenEmptyTable(previewDetail?.body?.bodyType)
    "
    :style="{ 'padding-bottom': `${getBottomDistance(previewDetail.value?.body?.bodyType)}px` }"
  >
    <MsFormTable
      :columns="bodyColumns"
      :data="bodyTableData"
      :selectable="false"
      :show-setting="true"
      :table-key="TableKeyEnum.API_TEST_DEBUG_FORM_DATA"
      :diff-mode="props.mode"
    />
  </div>
  <div
    v-if="showDiff(previewDetail?.body?.bodyType) && !hiddenEmptyTable(previewDetail?.body?.bodyType)"
    class="not-setting-data"
    :style="{ height: `${getBottomDistance(previewDetail?.body?.bodyType, true)}px` }"
  >
    {{ t('case.notSetData') }}
  </div>
  <template
    v-else-if="
      [RequestBodyFormat.JSON, RequestBodyFormat.RAW, RequestBodyFormat.XML].includes(previewDetail?.body?.bodyType)
    "
  >
    <MsJsonSchema
      v-if="previewDetail?.body?.bodyType === RequestBodyFormat.JSON && bodyShowType === 'schema' && props.isApi"
      :data="previewDetail.body.jsonBody.jsonSchemaTableData"
      disabled
    />
    <MsCodeEditor
      v-else
      :model-value="bodyCode"
      theme="vs"
      height="200px"
      :language="bodyCodeLanguage"
      :show-full-screen="false"
      :show-theme-change="false"
      read-only
    >
      <template #rightTitle>
        <a-button
          type="outline"
          class="arco-btn-outline--secondary p-[0_8px]"
          size="mini"
          @click="copyScript(bodyCode)"
        >
          <template #icon>
            <MsIcon type="icon-icon_copy_outlined" class="text-var(--color-text-4)" size="12" />
          </template>
        </a-button>
      </template>
    </MsCodeEditor>
  </template>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useClipboard } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';
  import MsFormTable, { FormTableColumn } from '@/components/pure/ms-form-table/index.vue';
  import MsJsonSchema from '@/components/pure/ms-json-schema/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { RequestBodyFormat, RequestComposition, RequestParamsType } from '@/enums/apiEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import type { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';

  const { copy, isSupported } = useClipboard({ legacy: true });
  const { t } = useI18n();

  const props = defineProps<{
    diffDistanceMap: Record<string, any>; // 距离填充
    detail: RequestParam;
    mode: 'add' | 'delete';
    isApi?: boolean;
  }>();

  const previewDetail = ref<RequestParam>(props.detail);

  /**
   * 请求头
   */
  const headerColumns: FormTableColumn[] = [
    {
      title: 'apiTestManagement.paramName',
      dataIndex: 'key',
      inputType: 'text',
      width: 220,
    },
    {
      title: 'apiTestManagement.paramVal',
      dataIndex: 'value',
      inputType: 'text',
      width: 220,
    },
    {
      title: 'common.desc',
      dataIndex: 'description',
      inputType: 'text',
      showTooltip: true,
    },
  ];

  /**
   * Query & Rest
   */
  const queryRestColumns: FormTableColumn[] = [
    {
      title: 'apiTestManagement.paramName',
      dataIndex: 'key',
      inputType: 'text',
      width: 220,
      columnSelectorDisabled: true,
    },
    {
      title: 'apiTestManagement.required',
      dataIndex: 'required',
      slotName: 'required',
      inputType: 'text',
      columnSelectorDisabled: true,
      valueFormat: (record) => {
        return record.required ? t('common.yes') : t('common.no');
      },
      width: 68,
    },
    {
      title: 'apiTestDebug.paramType',
      dataIndex: 'paramType',
      inputType: 'text',
      width: 96,
      columnSelectorDisabled: true,
    },
    {
      title: 'apiTestManagement.paramVal',
      dataIndex: 'value',
      inputType: 'text',
    },
    {
      title: 'apiTestDebug.paramLengthRange',
      dataIndex: 'lengthRange',
      slotName: 'lengthRange',
      inputType: 'text',
      showInTable: false,
      valueFormat: (record) => {
        return [null, undefined].includes(record.minLength) && [null, undefined].includes(record.maxLength)
          ? '-'
          : `${record.minLength} ${t('common.to')} ${record.maxLength}`;
      },
      width: 110,
    },
    {
      title: 'apiTestDebug.encode',
      dataIndex: 'encode',
      slotName: 'encode',
      inputType: 'text',
      showInTable: false,
      valueFormat: (record) => {
        return record.encode ? t('common.yes') : t('common.no');
      },
      width: 68,
    },
    {
      title: 'common.desc',
      dataIndex: 'description',
      inputType: 'text',
      showTooltip: true,
    },
    {
      title: '',
      dataIndex: 'operation',
      slotName: 'operation',
      fixed: 'right',
      width: 100,
    },
  ];

  const bodyShowType = ref('schema');

  /**
   * 请求体
   */
  const bodyColumns = computed<FormTableColumn[]>(() => {
    if ([RequestBodyFormat.FORM_DATA, RequestBodyFormat.WWW_FORM].includes(previewDetail.value?.body?.bodyType)) {
      return [
        {
          title: 'apiTestManagement.paramName',
          dataIndex: 'key',
          inputType: 'text',
          width: 220,
          columnSelectorDisabled: true,
        },
        {
          title: 'apiTestManagement.required',
          dataIndex: 'required',
          slotName: 'required',
          inputType: 'text',
          columnSelectorDisabled: true,
          valueFormat: (record) => {
            return record.required ? t('common.yes') : t('common.no');
          },
          width: 68,
        },
        {
          title: 'apiTestManagement.paramsType',
          dataIndex: 'paramType',
          inputType: 'text',
          width: 96,
          columnSelectorDisabled: true,
        },
        {
          title: 'apiTestManagement.paramVal',
          dataIndex: 'value',
          inputType: 'text',
          showTooltip: true,
        },
        {
          title: 'apiTestDebug.paramLengthRange',
          dataIndex: 'lengthRange',
          slotName: 'lengthRange',
          inputType: 'text',
          showInTable: false,
          valueFormat: (record) => {
            return [null, undefined].includes(record.minLength) && [null, undefined].includes(record.maxLength)
              ? '-'
              : `${record.minLength} ${t('common.to')} ${record.maxLength}`;
          },
          width: 110,
        },
        {
          title: 'apiTestDebug.encode',
          dataIndex: 'encode',
          slotName: 'encode',
          inputType: 'text',
          showInTable: false,
          valueFormat: (record) => {
            return record.encode ? t('common.yes') : t('common.no');
          },
          width: 68,
        },
        {
          title: 'common.desc',
          dataIndex: 'description',
          inputType: 'text',
          showTooltip: true,
        },
        {
          title: '',
          dataIndex: 'operation',
          slotName: 'operation',
          fixed: 'right',
          width: 100,
        },
      ];
    }
    return [
      {
        title: 'common.desc',
        dataIndex: 'description',
        inputType: 'text',
        showTooltip: true,
      },
      {
        title: 'apiTestManagement.paramVal',
        dataIndex: 'value',
        inputType: 'text',
        showTooltip: true,
      },
    ];
  });

  const bodyTableData = computed(() => {
    switch (previewDetail.value?.body?.bodyType) {
      case RequestBodyFormat.FORM_DATA:
        return (previewDetail.value.body.formDataBody?.formValues || [])
          .map((e) => ({
            ...e,
            value: e.paramType === RequestParamsType.FILE ? e.files?.map((file) => file.fileName).join('、') : e.value,
          }))
          ?.filter((e) => e.key !== '');
      case RequestBodyFormat.WWW_FORM:
        return previewDetail.value.body.wwwFormBody?.formValues?.filter((e) => e.key !== '') || [];
      case RequestBodyFormat.BINARY:
        return [
          {
            description: previewDetail.value.body.binaryBody.description,
            value: previewDetail.value.body.binaryBody.file?.fileName,
          },
        ];
      default:
        return [];
    }
  });

  const bodyCode = computed(() => {
    switch (previewDetail.value?.body?.bodyType) {
      case RequestBodyFormat.FORM_DATA:
        return previewDetail.value.body.formDataBody?.formValues?.map((item) => `${item.key}:${item.value}`).join('\n');
      case RequestBodyFormat.WWW_FORM:
        return previewDetail.value.body.wwwFormBody?.formValues?.map((item) => `${item.key}:${item.value}`).join('\n');
      case RequestBodyFormat.RAW:
        return previewDetail.value.body.rawBody?.value;
      case RequestBodyFormat.JSON:
        return previewDetail.value.body.jsonBody?.jsonValue;
      case RequestBodyFormat.XML:
        return previewDetail.value.body.xmlBody?.value;
      default:
        return '';
    }
  });

  const bodyCodeLanguage = computed(() => {
    if (previewDetail.value?.body?.bodyType === RequestBodyFormat.JSON) {
      return LanguageEnum.JSON;
    }
    if (previewDetail.value?.body?.bodyType === RequestBodyFormat.XML) {
      return LanguageEnum.XML;
    }
    return LanguageEnum.PLAINTEXT;
  });

  function copyScript(val: string) {
    if (isSupported) {
      copy(val);
      Message.success(t('common.copySuccess'));
    } else {
      Message.warning(t('apiTestDebug.copyNotSupport'));
    }
  }

  const typeKey = computed(() => (props.isApi ? 'api' : 'case'));

  // 设置非空间距确保行内容长度不同能够水平看齐对比
  function getBottomDistance(type: string, isEmpty = false) {
    const isEmptyDefaultDis = isEmpty ? 34 : 0;
    if (props.diffDistanceMap[type] && props.diffDistanceMap[type][typeKey.value]) {
      return props.diffDistanceMap[type][typeKey.value] * 32 + isEmptyDefaultDis;
    }
    return 0;
  }

  // 是否显示对比
  function showDiff(type: string) {
    return props.diffDistanceMap[type]?.display;
  }

  // 去掉空表
  function hiddenEmptyTable(type: string) {
    if (props.isApi) {
      return props.diffDistanceMap[type]?.showEmptyApiTable;
    }
    return props.diffDistanceMap[type]?.showEmptyCaseTable;
  }

  watchEffect(() => {
    if (props.detail) {
      previewDetail.value = cloneDeep(props.detail);
    }
  });
</script>

<style scoped lang="less">
  .title-type {
    color: var(--color-text-1);
    @apply font-medium;
  }
  .title {
    color: var(--color-text-1);
    @apply my-4;
  }
  .detail-item-title {
    margin-bottom: 8px;
    gap: 16px;
    @apply flex items-center justify-between;
    .detail-item-title-text {
      @apply font-medium;

      color: var(--color-text-1);
    }
  }
  .not-setting-data {
    border: 1px solid var(--color-border-2);
    border-radius: 4px;
    @apply flex items-center justify-center;
  }
</style>
