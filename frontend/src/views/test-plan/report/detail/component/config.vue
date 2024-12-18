<template>
  <!-- 配置头开始 -->
  <div class="report-name">
    <div class="font-medium">
      {{ t('testPlan.testPlanDetail.generateReport') }}
    </div>
    <div class="flex items-center">
      <a-form ref="formRef" class="mt-1 max-w-[710px]" :model="reportForm">
        <a-form-item
          field="reportName"
          asterisk-position="end"
          :hide-label="true"
          hide-asterisk
          content-class="contentClass"
          class="mb-0 max-w-[732px]"
        >
          <a-input
            v-model:model-value="reportForm.reportName"
            :placeholder="t('report.detail.enterReportNamePlaceHolder')"
            :max-length="255"
            class="w-[732px]"
            :error="isError"
            @input="inputHandler"
          ></a-input>
        </a-form-item>
      </a-form>
      <div class="ml-[12px]">
        <a-button type="secondary" @click="cancelHandler">{{ t('common.cancel') }}</a-button>
        <a-button class="ml-[12px]" type="primary" :loading="confirmLoading" @click="handleSave">{{
          t('common.save')
        }}</a-button>
      </div>
    </div>
  </div>
  <!-- 配置头结束 -->
  <!-- 报告头 -->
  <div class="config-container">
    <div class="config-left-container">
      <div class="sticky top-[16px]">
        <div class="mb-[16px] flex items-center justify-between">
          <div class="flex items-center">
            <div class="text-[16px] font-medium">{{ t('report.detail.baseField') }}</div>
            <a-tooltip :content="t('report.detail.customFieldTooltip')" position="right">
              <icon-question-circle
                class="ml-[4px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
                size="16"
              />
            </a-tooltip>
          </div>
          <MsButton :disabled="!hasChange" class="cursor-pointer text-[rgb(var(--primary-5))]" @click="handleReset"
            >{{ t('common.resetDefault') }}
          </MsButton>
        </div>
        <!-- 自定义字段列表 -->
        <VueDraggable
          v-model="configList"
          :sort="false"
          class="custom-card-list w-full"
          :group="{ name: 'report', pull: 'clone', put: false }"
          :clone="onClone"
        >
          <div
            v-for="item of configList"
            v-show="showItem(item)"
            :key="item.value"
            :class="`${getHoverClass(item)} custom-card-item`"
            @click.stop="addField(item)"
          >
            <a-tooltip :mouse-enter-delay="300" :content="t(item.label)" position="top">
              <div class="flex items-center justify-between">
                <div :class="`${getLabelClass(item)} custom-card-item-label one-line-text max-w-[calc(100%-14px)]`">
                  {{ t(item.label) }}
                </div>
                <icon-close
                  v-if="item.type !== FieldTypeEnum.SYSTEM"
                  :style="{ 'font-size': '14px' }"
                  class="cursor-pointer text-[var(--color-text-3)]"
                  @click.stop="removeField(item)"
                />
              </div>
            </a-tooltip>
          </div>
          <a-tooltip class="ms-tooltip-white" :disabled="!limitCustomLength">
            <a-button type="outline" class="!h-[30px]" :disabled="limitCustomLength" @click.stop="addCustomField">
              <div class="flex flex-row items-center gap-[8px]">
                <icon-plus />
                <span>{{ t('report.detail.customButton') }}</span>
              </div>
            </a-button>
            <template #content>
              <div class="text-[var(--color-text-1)]">{{ t('report.detail.customMaxNumber') }}</div>
            </template>
          </a-tooltip>
        </VueDraggable>
      </div>
    </div>
    <div class="config-right-container">
      <ViewReport
        ref="viewReportRef"
        v-model:card-list="cardItemList"
        :detail-info="props.detailInfo"
        :is-drawer="props.isDrawer"
        :is-group="props.isGroup"
        :is-preview="false"
        @update-custom="updateCustom"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep, isEqual } from 'lodash-es';
  import { VueDraggable } from 'vue-draggable-plus';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import ViewReport from '@/views/test-plan/report/detail/component/viewReport.vue';

  import { manualReportGen } from '@/api/modules/test-plan/report';
  import { defaultReportDetail } from '@/config/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import { getGenerateId } from '@/utils';

  import type {
    configItem,
    manualReportGenParams,
    PlanReportDetail,
    SelectedReportCardTypes,
  } from '@/models/testPlan/testPlanReport';
  import { TriggerModeLabelEnum } from '@/enums/reportEnum';
  import { TestPlanRouteEnum } from '@/enums/routeEnum';
  import { FieldTypeEnum, ReportCardTypeEnum } from '@/enums/testPlanReportEnum';

  import { defaultCustomConfig, defaultGroupCardConfig, defaultGroupConfig, defaultSingleConfig } from './reportConfig';
  import { getSummaryDetail } from '@/views/test-plan/report/utils';

  const { t } = useI18n();

  const router = useRouter();
  const route = useRoute();
  const appStore = useAppStore();
  const props = defineProps<{
    detailInfo: PlanReportDetail;
    isDrawer?: boolean;
    isGroup?: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'updateSuccess'): void;
  }>();

  const detail = ref<PlanReportDetail>({ ...cloneDeep(defaultReportDetail) });

  const isError = ref(false);
  const hasChange = ref(false);
  const reportForm = ref({
    reportName: '',
  });

  function inputHandler(value: string) {
    if (value.trim().length === 0) {
      isError.value = true;
    }
    isError.value = false;
  }

  watchEffect(() => {
    if (props.detailInfo) {
      detail.value = cloneDeep(props.detailInfo);
      reportForm.value.reportName = detail.value.name;
    }
  });

  const functionalCaseTotal = computed(() => getSummaryDetail(detail.value.functionalCount).caseTotal);
  const apiCaseTotal = computed(() => getSummaryDetail(detail.value.apiCaseCount).caseTotal);
  const scenarioCaseTotal = computed(() => getSummaryDetail(detail.value.apiScenarioCount).caseTotal);

  const configList = ref<configItem[]>([]);
  const cardItemList = ref<configItem[]>([]);

  const detailType = [
    ReportCardTypeEnum.FUNCTIONAL_DETAIL,
    ReportCardTypeEnum.API_CASE_DETAIL,
    ReportCardTypeEnum.SCENARIO_CASE_DETAIL,
  ];

  const hasCaseList = computed<Record<SelectedReportCardTypes, number>>(() => {
    return {
      [ReportCardTypeEnum.SCENARIO_CASE_DETAIL]: scenarioCaseTotal.value,
      [ReportCardTypeEnum.FUNCTIONAL_DETAIL]: functionalCaseTotal.value,
      [ReportCardTypeEnum.API_CASE_DETAIL]: apiCaseTotal.value,
    };
  });

  function filterNotHasCase(list: configItem[]) {
    return list.filter((item: any) => {
      if (detailType.includes(item.value)) {
        return hasCaseList.value[item.value as SelectedReportCardTypes] > 0;
      }
      return true;
    });
  }

  function initDefaultConfig() {
    const tempDefaultGroupConfig = filterNotHasCase(defaultGroupConfig);
    const tempSingleGroupConfig = filterNotHasCase(defaultSingleConfig);
    configList.value = props.isGroup ? cloneDeep(tempDefaultGroupConfig) : cloneDeep(tempSingleGroupConfig);
    cardItemList.value = props.isGroup ? cloneDeep(defaultGroupCardConfig) : cloneDeep(configList.value);
  }

  watch(
    () => hasCaseList.value,
    () => {
      initDefaultConfig();
    }
  );

  function getExist(cardItem: configItem) {
    return cardItemList.value.find((item) => item.id === cardItem.id);
  }

  function getHoverClass(cardItem: configItem) {
    if (getExist(cardItem) && cardItem.type !== FieldTypeEnum.SYSTEM) {
      return 'hover-selected-item-class';
    }
    if (!getExist(cardItem) && cardItem.type === FieldTypeEnum.SYSTEM) {
      return 'hover-item-class';
    }
    return '';
  }

  function getLabelClass(cardItem: configItem) {
    const isSystemColor = cardItem.type === FieldTypeEnum.SYSTEM ? 'cursor-not-allowed' : '';
    return getExist(cardItem)
      ? `text-[var(--color-text-4)] ${isSystemColor}`
      : `text-[var(--color-text-1)] cursor-pointer hover:text-[rgb(var(--primary-4))]`;
  }

  const limitCustomLength = computed(
    () => configList.value.filter((item) => item.value === ReportCardTypeEnum.CUSTOM_CARD).length >= 10
  );

  const deleteCard = (cardItem: configItem) => {
    cardItemList.value = cardItemList.value.filter((item) => item.id !== cardItem.id);
  };

  function addCustomField() {
    if (limitCustomLength.value) {
      return;
    }
    const id = getGenerateId();
    configList.value.push({
      ...defaultCustomConfig,
      id,
    });
  }

  // 恢复默认
  function handleReset() {
    initDefaultConfig();
    nextTick(() => {
      hasChange.value = false;
    });
  }

  function resetConfigEditList(list: configItem[]) {
    return list.map((item: configItem) => {
      return {
        ...item,
        enableEdit: false,
      };
    });
  }
  const isInit = ref(true);

  watch(
    [() => configList.value, () => cardItemList.value],
    () => {
      const configValue = resetConfigEditList(cloneDeep(configList.value));
      const cardItemValue = resetConfigEditList(cloneDeep(cardItemList.value));
      const isisEqualList = props.isGroup ? cloneDeep(defaultGroupConfig) : cloneDeep(defaultSingleConfig);
      if (!isEqual(configValue, isisEqualList) || (!isEqual(cardItemValue, isisEqualList) && !isInit.value)) {
        if (isInit.value) {
          isInit.value = false;
        } else {
          nextTick(() => {
            hasChange.value = true;
          });
        }
      }
    },
    { deep: true }
  );

  // 移除字段
  function removeField(currentItem: configItem) {
    configList.value = configList.value.filter((item) => item.id !== currentItem.id);
    deleteCard(currentItem);
  }
  // 添加字段
  function addField(cardItem: configItem) {
    const isHasCard = cardItemList.value.find((item) => item.id === cardItem.id);
    if (!isHasCard) {
      cardItemList.value.push(cardItem);
    }
  }
  // 拖拽克隆
  function onClone(element: Record<'name' | 'id', string>) {
    const isHasCard = cardItemList.value.find((item) => item.id === element.id);
    if (!isHasCard) {
      return element;
    }
  }

  function cancelHandler() {
    router.back();
  }

  // 更新自定义字段
  function updateCustom(currentItem: configItem) {
    const currentIndex = configList.value.findIndex((item) => item.id === currentItem.id);
    configList.value.splice(currentIndex, 1, currentItem);
  }

  function showItem(item: configItem) {
    switch (item.value) {
      case ReportCardTypeEnum.FUNCTIONAL_DETAIL:
        return functionalCaseTotal.value > 0;
      case ReportCardTypeEnum.API_CASE_DETAIL:
        return apiCaseTotal.value > 0;
      case ReportCardTypeEnum.SCENARIO_CASE_DETAIL:
        return scenarioCaseTotal.value > 0;
      default:
        return true;
    }
  }

  function makeParams() {
    const richFileIds: string[] = [];
    const addComponentList = cardItemList.value.map((item, index) => {
      if (item.richTextTmpFileIds) {
        richFileIds.concat(item.richTextTmpFileIds);
      }
      return {
        name: item.value,
        label: t(item.label),
        type: item.type,
        value: item.content || '',
        pos: index + 1,
      };
    });
    return {
      projectId: appStore.currentProjectId,
      testPlanId: route.query.id as string,
      triggerMode: TriggerModeLabelEnum.MANUAL,
      reportName: reportForm.value.reportName,
      components: addComponentList,
      richTextTmpFileIds: richFileIds,
    };
  }

  const confirmLoading = ref<boolean>(false);
  const viewReportRef = ref<InstanceType<typeof ViewReport>>();
  // 保存配置
  async function handleSave() {
    if (!reportForm.value.reportName) {
      isError.value = true;
      Message.error(t('report.detail.reportNameNotEmpty'));
      return;
    }
    confirmLoading.value = true;
    try {
      const params: manualReportGenParams = makeParams();
      const reportId = await manualReportGen(params);
      viewReportRef.value?.setIsSave(true);
      Message.success(t('report.detail.manualGenReportSuccess'));
      if (reportId) {
        router.push({
          name: TestPlanRouteEnum.TEST_PLAN_REPORT_DETAIL,
          query: {
            id: reportId,
            type: props.isGroup ? 'GROUP' : 'TEST_PLAN',
          },
        });
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      confirmLoading.value = false;
    }
  }

  onBeforeMount(() => {
    initDefaultConfig();
  });
</script>

<style scoped lang="less">
  .report-name {
    padding: 0 16px;
    height: 56px;
    border-color: var(--color-text-n8);
    background-color: var(--color-text-fff);
    @apply flex items-center justify-between border-b;
  }
  .block-title {
    @apply mb-4 font-medium;
  }
  .config-container {
    @apply flex w-full;
    .config-left-container {
      margin-bottom: 16px;
      padding: 16px;
      width: 300px;
      border-radius: 0 0 10px 10px;
      box-sizing: border-box;
      background-color: var(--color-text-fff);
      .custom-card-list {
        @apply grid grid-cols-2 gap-2;
        .custom-card-item {
          padding: 4px 8px;
          border-radius: 4px;
          background: var(--color-bg-3);
          &.custom-button {
            border: 1px solid rgb(var(--primary-5));
            color: rgb(var(--primary-5));
            @apply cursor-pointer;
          }
        }
      }
    }
    .config-right-container {
      padding: 16px 0 16px 16px;
      width: calc(100% - 300px);
      min-width: 1000px;
      background: var(--color-bg-3);
    }
  }
  .hover-item-class {
    &:hover {
      .custom-card-item-label {
        color: rgb(var(--primary-4));
      }
    }
  }
  .hover-selected-item-class {
    &:hover {
      .custom-card-item-label {
        color: rgb(var(--primary-3));
      }
    }
  }
</style>
