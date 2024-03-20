<template>
  <MsDrawer
    ref="detailDrawerRef"
    v-model:visible="showDrawer"
    :width="960"
    :footer="false"
    :title="t('步骤名称')"
    show-full-screen
    :unmount-on-close="true"
  >
    <template #headerLeft>
      <div class="scene-type"> API </div>
    </template>
    <div>
      <div class="mb-4 flex justify-start">
        <MsPagination
          v-if="props.scenarioDetail.stepType === 'LOOP_CONTROLLER'"
          v-model:page-size="pageNation.pageSize"
          v-model:current="pageNation.current"
          :total="pageNation.total"
          size="mini"
          @change="loadLoop"
          @page-size-change="loadLoop"
      /></div>

      <ms-base-table
        ref="tableRef"
        v-bind="propsRes"
        v-model:expandedKeys="expandedKeys"
        no-disable
        :indent-size="0"
        v-on="propsEvent"
      >
        <template #titleName>
          <div class="flex w-full justify-between">
            <div class="font-medium">{{ t('report.detail.api.resContent') }}</div>
            <div class="grid grid-cols-4 gap-2 text-center">
              <a-popover position="left" content-class="response-popover-content">
                <div
                  class="one-line-text max-w-[200px]"
                  :style="{ color: statusCodeColor(activeStepDetail?.content?.responseResult.responseCode || '') }"
                >
                  {{ activeStepDetail?.content?.responseResult.responseCode || '-' }}
                </div>
                <template #content>
                  <div class="flex items-center gap-[8px] text-[14px]">
                    <div class="text-[var(--color-text-4)]">{{ t('apiTestDebug.statusCode') }}</div>
                    <div
                      :style="{ color: statusCodeColor(activeStepDetail.content?.responseResult.responseCode || '') }"
                    >
                      {{ activeStepDetail?.content?.responseResult.responseCode || '-' }}
                    </div>
                  </div>
                </template>
              </a-popover>
              <span class="text-[rgb(var(--success-6))]"
                >{{ activeStepDetail.content?.responseResult.responseTime }}ms</span
              >
              <span class="text-[rgb(var(--success-6))]">
                {{ activeStepDetail.content?.responseResult.responseSize }} bytes</span
              >
              <!-- <span>Mock</span> -->
              <span>{{ props.environmentName }}</span>
            </div>
          </div>
        </template>
        <template #name="{ record }">
          <span class="font-medium">
            {{ record.name }}
          </span>

          <div v-if="record.showScript && !record.isAssertion" class="w-full">
            <ResContent :script="record.script || ''" language="JSON" :show-charset-change="record.showScript" />
          </div>
          <div v-if="record.isAssertion" class="w-full">
            <assertTable :data="record.assertions" />
          </div>
        </template>
      </ms-base-table>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { cloneDeep } from 'lodash-es';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsPagination from '@/components/pure/ms-pagination/index';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import assertTable from './assertTable.vue';
  import ResContent from './resContent.vue';

  import { reportCaseStepDetail, reportStepDetail } from '@/api/modules/api-test/report';
  import { useI18n } from '@/hooks/useI18n';
  import { getGenerateId } from '@/utils';

  import type { ReportStepDetail, ScenarioDetailItem } from '@/models/apiTest/report';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
    stepId: string;
    activeStepIndex: number;
    scenarioDetail: ScenarioDetailItem;
    showType: 'API' | 'CASE'; // 接口场景|用例
    console?: string; //  控制台
    environmentName?: string; // 环境名称
  }>();

  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
  }>();

  const showDrawer = computed({
    get() {
      return props.visible;
    },
    set(val) {
      emit('update:visible', val);
    },
  });
  const innerFileId = ref(props.stepId);

  function loadedStep(detail: Record<string, any>) {
    innerFileId.value = detail.id;
  }

  const pageNation = ref({
    total: 1000,
    pageSize: 10,
    current: 1,
  });

  const tableRef = ref<InstanceType<typeof MsBaseTable> | null>(null);

  const expandedKeys = ref<string[]>([]);

  const columns: MsTableColumn = [
    {
      title: 'report.detail.api.resContent',
      dataIndex: 'name',
      slotName: 'name',
      titleSlotName: 'titleName',
      fixed: 'left',
      headerCellClass: 'titleClass',
      bodyCellClass: (record) => {
        if (record.children) {
          return '';
        }
        return 'cellClassWrapper';
      },
    },
  ];

  const { propsRes, propsEvent } = useTable(undefined, {
    columns,
    scroll: { x: 'auto' },
    showPagination: false,
    hoverable: false,
    showExpand: true,
    rowKey: 'id',
    rowClass: (record: any) => {
      if (record.children) {
        return 'gray-td-bg';
      }
    },
  });

  async function loadLoop() {}

  /**
   *  响应状态码对应颜色
   */

  function statusCodeColor(code: string) {
    if (code) {
      const resCode = Number(code);
      if (resCode >= 200 && resCode < 300) {
        return 'rgb(var(--success-7)';
      }
      if (resCode >= 300 && resCode < 400) {
        return 'rgb(var(--warning-7)';
      }
      return 'rgb(var(--danger-7)';
    }
    return '';
  }

  const stepDetailInfo = ref<ReportStepDetail[]>([]);

  // 处理内容
  function getRequestItem(item: ReportStepDetail) {
    const headers = item.content?.responseResult.headers;
    const body = item.content?.responseResult.body;
    const realRequest = item.content?.body
      ? `${t('apiTestDebug.requestUrl')}:\n${item.content?.url}\n${t('apiTestDebug.header')}:\n${
          item.content?.headers
        }\nBody:\n${item.content?.body.trim()}`
      : '';
    const assertionList = item.content?.responseResult.assertions;
    const extractValue = item.content?.responseResult.vars?.trim();
    return {
      headers,
      body,
      realRequest,
      assertionList,
      extractValue,
    };
  }

  const tempStepMap = ref<Record<string, any>>({});
  function setTableMaps(currentStepId: string, paramsObj: Record<string, any>) {
    const { headers, body, realRequest, assertionList, extractValue } = paramsObj;
    tempStepMap.value[currentStepId] = [
      {
        id: getGenerateId(),
        name: '响应体',
        script: '',
        showScript: false,
        isAssertion: false,
        assertions: [],
        children: [
          {
            showScript: true,
            script: body,
            isAssertion: false,
            assertions: [],
          },
        ],
      },
      {
        id: getGenerateId(),
        name: '响应头',
        script: '',
        showScript: false,
        isAssertion: false,
        assertions: [],
        children: [
          {
            script: headers,
            showScript: true,
            isAssertion: false,
            assertions: [],
          },
        ],
      },
      {
        id: getGenerateId(),
        name: '实际请求',
        script: '',
        showScript: false,
        isAssertion: false,
        assertions: [],
        children: [
          {
            script: realRequest,
            showScript: true,
            isAssertion: false,
            assertions: [],
          },
        ],
      },
      {
        id: getGenerateId(),
        name: '控制台',
        script: '',
        isAssertion: false,
        showScript: false,
        assertions: [],
        children: [
          {
            script: props.console,
            showScript: true,
            isAssertion: false,
            assertions: [],
          },
        ],
      },
      {
        id: getGenerateId(),
        name: '提取',
        script: '',
        isAssertion: false,
        showScript: false,
        assertions: [],
        children: [
          {
            script: extractValue,
            showScript: true,
            isAssertion: false,
            assertions: [],
          },
        ],
      },
      {
        id: getGenerateId(),
        name: '断言',
        script: '',
        showScript: false,
        isAssertion: false,
        assertions: [],
        children: [
          {
            script: '',
            showScript: false,
            isAssertion: true,
            assertions: assertionList,
          },
        ],
      },
    ];
  }

  function setResValue(list: ReportStepDetail[]) {
    for (let i = 0; i < list.length; i++) {
      const currentStepId = list[i].id as string;
      const paramsObj = getRequestItem(list[i]);
      setTableMaps(currentStepId, paramsObj);
      if (list[i].content?.subRequestResults && list[i].content?.subRequestResults.length) {
        setResValue(list[i].content?.subRequestResults);
      }
    }
  }

  const reportDetailMap = {
    API: {
      stepDetail: reportStepDetail,
    },
    CASE: {
      stepDetail: reportCaseStepDetail,
    },
  };

  /**
   *  获取步骤详情
   */
  const activeStepId = ref<string>('');
  const activeStepDetail = ref<ReportStepDetail>({});
  const activeIndex = ref<number>(0);
  async function getStepDetail() {
    try {
      const result = await reportDetailMap[props.showType].stepDetail(
        props.scenarioDetail.reportId as string,
        props.scenarioDetail.stepId as string
      );
      stepDetailInfo.value = cloneDeep(result) as ReportStepDetail[];
      activeStepId.value = stepDetailInfo.value[0].id as string;
      activeStepDetail.value = stepDetailInfo.value.find((item) => item.id === activeStepId.value) || {};
      setResValue(stepDetailInfo.value);
      propsRes.value.data = tempStepMap.value[activeStepId.value];
    } catch (error) {
      console.log(error);
    }
  }

  watchEffect(() => {
    if (props.scenarioDetail.reportId && props.scenarioDetail.stepId) {
      getStepDetail();
    }
  });
</script>

<style scoped lang="less">
  .scene-type {
    margin-left: 8px;
    padding: 0 2px;
    font-size: 12px;
    line-height: 16px;
    border: 1px solid rgb(var(--link-6));
    border-radius: 0 12px 12px 0;
    color: rgb(var(--link-6));
  }
  .resContentHeader {
    @apply flex justify-between;
  }
  :deep(.gray-td-bg) {
    td {
      background-color: var(--color-text-n9);
    }
  }
  :deep(.titleClass .arco-table-th-title) {
    @apply w-full;
  }
  :deep(.cellClassWrapper > .arco-table-cell) {
    padding: 0 !important;
    span {
      padding-left: 0 !important;
    }
  }
</style>
