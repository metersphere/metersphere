<template>
  <div class="resContentWrapper">
    <!-- 循环计数器 -->
    <div v-if="detailItem.stepType === 'LOOP_CONTROLLER'" class="mb-4 flex justify-start">
      <MsPagination
        v-model:page-size="pageNation.pageSize"
        v-model:current="pageNation.current"
        :total="pageNation.total"
        size="mini"
        @change="loadLoop"
        @page-size-change="loadLoop"
      />
    </div>
    <div class="resContent">
      <div class="flex h-full w-full items-center justify-between rounded bg-[var(--color-text-n9)] px-4">
        <div class="font-medium">
          <span>{{ t('report.detail.api.resContent') }}</span>
          <span class="text-[rgb(var(--primary-5))]">
            <a-divider direction="vertical" :margin="8"></a-divider>

            子请求</span
          >
        </div>
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
                <div :style="{ color: statusCodeColor(activeStepDetail?.content?.responseResult.responseCode || '') }">
                  {{ activeStepDetail?.content?.responseResult.responseCode || '-' }}
                </div>
              </div>
            </template>
          </a-popover>
          <span class="text-[rgb(var(--success-6))]"
            >{{ activeStepDetail?.content?.responseResult?.responseTime }}ms</span
          >
          <span class="text-[rgb(var(--success-6))]"
            >{{ activeStepDetail?.content?.responseResult?.responseSize }}bytes</span
          >
          <!-- <span>Mock</span> -->
          <span>{{ props.environmentName }}</span>
        </div>
      </div>
    </div>
    <!-- 子请求开始 -->
    <div>
      <!-- TODO 最后写 看看能不能使用其他的代替 -->
    </div>
    <!-- 子请求结束 -->
    <!-- 响应内容tab开始 -->
    <div>
      <a-tabs v-model:active-key="showTab" class="no-content">
        <a-tab-pane v-for="it of tabList" :key="it.key" :title="t(it.title)" />
      </a-tabs>
      <a-divider :margin="0"></a-divider>
      <div v-if="showTab !== 'assertions'">
        <ResContent :script="showContent || ''" language="JSON" show-charset-change
      /></div>
      <div v-else>
        <assertTable :data="showContent" />
      </div>
    </div>
    <!-- 响应内容tab结束 -->
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { cloneDeep } from 'lodash-es';

  import MsPagination from '@/components/pure/ms-pagination/index';
  import assertTable from './step/assertTable.vue';
  import ResContent from './step/resContent.vue';

  import { reportCaseStepDetail, reportStepDetail } from '@/api/modules/api-test/report';
  import { useI18n } from '@/hooks/useI18n';

  import type { ReportStepDetail, ScenarioItemType } from '@/models/apiTest/report';

  const { t } = useI18n();
  const props = defineProps<{
    detailItem: ScenarioItemType; // 报告详情
    showType: 'API' | 'CASE'; // 接口 | 用例
    console?: string; // 控制台
    environmentName?: string; // 环境
  }>();

  const pageNation = ref({
    total: 1000,
    pageSize: 10,
    current: 1,
  });

  // 加载用例列表
  async function loadLoop() {}

  const tabList = ref([
    {
      key: 'body',
      title: 'report.detail.api.resBody',
    },
    {
      key: 'headers',
      title: 'report.detail.api.resHeader',
    },
    {
      key: 'realReq',
      title: 'report.detail.api.realReq',
    },
    {
      key: 'console',
      title: 'report.detail.api.console',
    },
    {
      key: 'extract',
      title: 'report.detail.api.extract',
    },
    {
      key: 'assertions',
      title: 'report.detail.api.assert',
    },
  ]);
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

  const showTab = ref('body');

  const reportDetailMap = {
    API: {
      stepDetail: reportStepDetail,
    },
    CASE: {
      stepDetail: reportCaseStepDetail,
    },
  };

  const activeIndex = ref<number>(0);
  const activeStepDetail = ref<ReportStepDetail>({});
  /**
   *  获取步骤详情
   */
  const stepDetailInfo = ref<ReportStepDetail[]>([]);
  async function getStepDetail() {
    try {
      const result = await reportDetailMap[props.showType].stepDetail(
        props.detailItem.reportId,
        props.detailItem.stepId
      );
      stepDetailInfo.value = cloneDeep(result) as ReportStepDetail[];
      activeStepDetail.value = stepDetailInfo.value[activeIndex.value];
    } catch (error) {
      console.log(error);
    }
  }

  /**
   * 获取请求内容
   */
  const showContent = computed(() => {
    if (showTab.value === 'console') {
      return props.console;
    }
    if (showTab.value === 'realReq') {
      return activeStepDetail.value.content?.body
        ? `${t('apiTestDebug.requestUrl')}:\n${activeStepDetail.value.content.url}\n${t('apiTestDebug.header')}:\n${
            activeStepDetail.value.content.headers
          }\nBody:\n${activeStepDetail.value.content.body.trim()}`
        : '';
    }
    if (showTab.value === 'extract') {
      return activeStepDetail.value.content?.responseResult.vars?.trim();
    }
    return activeStepDetail.value.content?.responseResult[showTab.value];
  });

  onMounted(() => {
    if (!props.detailItem.fold) {
      getStepDetail();
    }
  });
</script>

<style scoped lang="less">
  .resContentWrapper {
    position: relative;
    border: 1px solid var(--color-text-n8);
    border-top: none;
    border-radius: 0 0 6px 6px;
    @apply mb-4 bg-white p-4;
    .resContent {
      height: 38px;
      border-radius: 6px;
    }
  }
</style>
