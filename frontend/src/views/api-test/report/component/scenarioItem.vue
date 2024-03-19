<template>
  <template v-for="(item, index) in list" :key="item.stepId">
    <div
      :style="{
        'padding-left': `${16 * (item.level as number)}px`,
      }"
    >
      <div
        class="scenario-class cursor-pointer rounded-t-md px-8"
        :class="[
          item.level !== 0 ? 'border border-solid border-[var(--color-text-n8)]' : '',
          ...getBorderAndRadius(item),
        ]"
        @click="showDetail(item)"
      >
        <div class="flex h-[46px] items-center">
          <!-- 序号 -->
          <span class="index mr-2 text-[var(--color-text-4)]">{{ index }}</span>
          <!-- 展开折叠控制器 -->
          <div
            v-if="item.level !== 0 && showApiType.includes(item.stepType) && props.activeType === 'tab'"
            class="mx-2"
          >
            <span
              v-if="item.fold"
              class="collapsebtn flex items-center justify-center"
              @click.stop="expandHandler(item)"
            >
              <icon-right class="text-[var(--color-text-4)]" :style="{ 'font-size': '12px' }" />
            </span>
            <span v-else class="expand flex items-center justify-center" @click.stop="expandHandler(item)">
              <icon-down class="text-[rgb(var(--primary-6))]" :style="{ 'font-size': '12px' }" />
            </span>
          </div>

          <MsIcon type="icon-icon_split_turn-down_arrow" class="mx-[4px] text-[var(--color-text-4)]" size="16" />
          <!-- 场景count -->
          <span class="mr-2 text-[var(--color-text-4)]">{{ (item.children || []).length }}</span>
          <!-- 循环控制器 -->
          <ConditionStatus :status="item.stepType || ''" />
          <span class="ml-2">{{ item.name || '-' }}</span>
        </div>
        <div>
          <MsTag class="cursor-pointer" :type="item.status === 'SUCCESS' ? 'success' : 'danger'" theme="light">
            {{ item.status === 'SUCCESS' ? t('report.detail.api.pass') : t('report.detail.api.resError') }}
          </MsTag>
          <span class="statusCode">
            {{ t('report.detail.api.statusCode') }} <span class="code">{{ item.code || '-' }}</span></span
          >
          <span class="resTime">
            {{ t('report.detail.api.responseTime') }}
            <span class="resTimeCount">{{ item.requestTime || 0 }}ms</span></span
          >
          <span class="resSize">
            {{ t('report.detail.api.responseSize') }}
            <span class="resTimeCount">{{ item.responseSize || 0 }} bytes</span></span
          >
        </div>
      </div>
    </div>
    <a-divider v-if="item.level === 0" :margin="0" class="!mb-4"></a-divider>
    <!-- 响应内容开始 -->
    <div
      v-if="item.level !== 0 && showApiType.includes(item.stepType) && props.activeType === 'tab' && !item.fold"
      :style="{
        'padding-left': `${16 * (item.level as number)}px`,
      }"
    >
      <div class="resContentWrapper">
        <!-- 循环计数器 -->
        <div v-if="item.stepType === 'LOOP_CONTROLLER'" class="mb-4 flex justify-start">
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
            <div class="font-medium">{{ t('report.detail.api.resContent') }}</div>
            <div class="grid grid-cols-5 gap-2 text-center">
              <span>401</span>
              <span class="text-[rgb(var(--success-6))]">247ms</span>
              <span class="text-[rgb(var(--success-6))]">50bytes</span>
              <span>Mock</span>
              <span>66</span>
            </div>
          </div>
        </div>
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
            <assertTable :data="showContent || []" />
          </div>
        </div>
        <!-- 响应内容tab结束 -->
      </div>
    </div>
    <!-- 响应内容结束 -->
    <ScenarioItem
      v-if="'children' in item"
      :list="item.children"
      :active-type="props.activeType"
      @detail="showDetail"
    />
  </template>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsPagination from '@/components/pure/ms-pagination/index';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import ConditionStatus from './conditionStatus.vue';
  import assertTable from './step/assertTable.vue';
  import ResContent from './step/resContent.vue';

  import { reportStepDetail } from '@/api/modules/api-test/report';
  import { useI18n } from '@/hooks/useI18n';

  import type { ReportStepDetail, ScenarioItemType } from '@/models/apiTest/report';

  const { t } = useI18n();
  const props = withDefaults(
    defineProps<{
      showBorder?: boolean;
      hasBottomMargin?: boolean;
      list: ScenarioItemType[];
      activeType: string;
    }>(),
    {
      showBorder: true,
      hasBottomMargin: true,
    }
  );

  const emit = defineEmits(['expand', 'detail']);
  const activeItem = ref();
  function showDetail(item: ScenarioItemType) {
    activeItem.value = item;
    emit('detail', activeItem.value);
  }

  const pageNation = ref({
    total: 1000,
    pageSize: 10,
    current: 1,
  });
  // 加载用例列表
  async function loadLoop() {}

  // const scenarioItem = computed({
  //   get: () => {
  //     return props.list;
  //   },
  //   set: (val) => {
  //     scenarioItem.value = val;
  //   },
  // });

  const showApiType = ref<string[]>(['API', 'API_CASE', 'CUSTOM_API', 'LOOP_CONTROLLER']);

  const stepDetail = ref<ReportStepDetail>({});

  async function getStepDetail(item: ScenarioItemType) {
    try {
      const result = await reportStepDetail(item.reportId, item.stepId);
      stepDetail.value = result;
    } catch (error) {
      console.log(error);
    }
  }

  async function expandHandler(item: ScenarioItemType) {
    item.fold = !item.fold;
    // 如果展开则获取报告步骤详情
    if (!item.fold) {
      getStepDetail(item);
    }
  }

  function getBorderAndRadius(item: ScenarioItemType) {
    if (props.activeType === 'tab') {
      if (!item.fold && showApiType.value.includes(item.stepType)) {
        return ['rounded-b-none', 'mb-0'];
      }
      return ['mb-1', 'rounded-[4px]'];
    }
    return ['mb-1', 'rounded-[4px]'];
  }

  const showTab = ref('body');
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

  const showContent = computed(() => {
    return stepDetail.value.content?.responseResult[showTab.value];
  });
</script>

<style scoped lang="less">
  .scenario-class {
    // border-radius: 4px;
    @apply flex items-center justify-between px-2;
    .index {
      width: 16px;
      height: 16px;
      line-height: 16px;
      border-radius: 50%;
      color: white;
      background: var(--color-text-brand);
      @apply inline-block text-center;
    }
    .resTime,
    .resSize,
    .statusCode {
      margin-right: 8px;
      color: var(--color-text-4);
      .resTimeCount {
        color: rgb(var(--success-6));
      }
    }
  }
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
  :deep(.expand) {
    width: 16px;
    height: 16px;
    border-radius: 50%;
    background: rgb(var(--primary-1));
  }
  :deep(.collapsebtn) {
    width: 16px;
    height: 16px;
    border-radius: 50%;
    background: var(--color-text-n8) !important;
    @apply bg-white;
  }
  :deep(.arco-table-expand-btn) {
    width: 16px;
    height: 16px;
    border: none;
    border-radius: 50%;
    background: var(--color-text-n8) !important;
  }
  :deep(.no-content) {
    .arco-tabs-content {
      display: none;
    }
  }
</style>
