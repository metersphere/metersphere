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
      :scroll="{ x: '100%' }"
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
      :scroll="{ x: '100%' }"
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
    v-if="showDiff(RequestComposition.REST) && hiddenEmptyTable(RequestComposition.REST)"
    :style="{ 'padding-bottom': `${getBottomDistance(RequestComposition.REST)}px` }"
  >
    <MsFormTable
      :columns="queryRestColumns?.filter((e) => e.key !== '')"
      :data="previewDetail?.rest || []"
      :selectable="false"
      :diff-mode="props.mode"
      :scroll="{ x: '100%' }"
    />
  </div>
  <div
    v-if="showDiff(RequestComposition.REST) && !hiddenEmptyTable(RequestComposition.REST)"
    class="not-setting-data"
    :style="{ height: `${getBottomDistance(RequestComposition.REST, true)}px` }"
  >
    {{ t('case.notSetData') }}
  </div>
  <!-- 请求体 -->
  <div v-for="item of requestBodyList" :key="item.value">
    <div v-if="showDiff(item.value)" class="title">
      {{ item.title }}
    </div>
    <div
      v-if="showDiff(item.value) && hiddenEmptyTable(item.value)"
      :style="{ 'padding-bottom': `${getBottomDistance(item.value)}px` }"
    >
      <MsFormTable
        :columns="getBodyColumns(item.value)"
        :data="getBodyTableData(item.value)"
        :selectable="false"
        :show-setting="false"
        :table-key="TableKeyEnum.API_TEST_DEBUG_FORM_DATA"
        :diff-mode="props.mode"
        :scroll="{ x: '100%' }"
      />
    </div>
    <div
      v-if="showDiff(item.value) && !hiddenEmptyTable(item.value)"
      class="not-setting-data"
      :style="{ height: `${getBottomDistance(item.value, true)}px` }"
    >
      {{ t('case.notSetData') }}
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { cloneDeep } from 'lodash-es';

  import MsFormTable, { FormTableColumn } from '@/components/pure/ms-form-table/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { RequestBodyFormat, RequestComposition, RequestParamsType } from '@/enums/apiEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import type { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';

  const { t } = useI18n();

  const props = defineProps<{
    diffDistanceMap: Record<string, any>; // 距离填充
    detail: RequestParam;
    mode: 'add' | 'delete';
    isApi?: boolean;
  }>();

  const previewDetail = ref<RequestParam>(props.detail);

  const requestBodyList = ref([
    {
      value: RequestBodyFormat.FORM_DATA,
      title: `${t('apiTestManagement.requestBody')}-FORM_DATA`,
    },
    {
      value: RequestBodyFormat.WWW_FORM,
      title: `${t('apiTestManagement.requestBody')}-WWW_FORM`,
    },
  ]);

  /**
   * 请求头
   */
  const headerColumns: FormTableColumn[] = [
    {
      title: 'apiTestManagement.paramName',
      dataIndex: 'key',
      inputType: 'text',
      width: 220,
      showTooltip: true,
    },
    {
      title: 'apiTestManagement.paramVal',
      dataIndex: 'value',
      inputType: 'text',
      width: 220,
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
      showTooltip: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'apiTestManagement.paramVal',
      dataIndex: 'value',
      inputType: 'text',
      width: 200,
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
  ];

  /**
   * 请求体
   */

  function getBodyColumns(bodyType: RequestBodyFormat): FormTableColumn[] {
    if ([RequestBodyFormat.FORM_DATA, RequestBodyFormat.WWW_FORM].includes(bodyType)) {
      return [
        {
          title: 'apiTestManagement.paramName',
          dataIndex: 'key',
          inputType: 'text',
          width: 220,
          showTooltip: true,
          columnSelectorDisabled: true,
        },
        {
          title: 'apiTestManagement.paramVal',
          dataIndex: 'value',
          inputType: 'text',
          showTooltip: true,
          width: 200,
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
      ];
    }
    return [
      {
        title: 'common.desc',
        dataIndex: 'description',
        inputType: 'text',
        showTooltip: true,
        width: 300,
      },
      {
        title: 'apiTestManagement.paramVal',
        dataIndex: 'value',
        inputType: 'text',
        showTooltip: true,
        width: 300,
      },
    ];
  }

  function getBodyTableData(bodyType: string) {
    switch (bodyType) {
      case RequestBodyFormat.FORM_DATA:
        return (previewDetail.value.body?.formDataBody?.formValues || [])
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
