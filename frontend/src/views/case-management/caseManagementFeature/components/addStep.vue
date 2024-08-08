<template>
  <MsBaseTable v-bind="propsRes" ref="tableRef" :hoverable="false" v-on="propsEvent" @change="changeHandler">
    <template #index="{ rowIndex }">
      <div class="circle text-[12px] font-medium"> {{ rowIndex + 1 }}</div>
    </template>
    <template v-if="!props.isTestPlan" #caseStep="{ record }">
      <a-textarea
        :ref="(el: refItem) => setStepRefMap(el, record)"
        v-model="record.step"
        size="mini"
        :max-length="1000"
        :auto-size="true"
        class="w-max-[267px] param-input"
        :placeholder="t('system.orgTemplate.stepTip')"
        :disabled="props.isDisabled"
        @blur="blurHandler(record, 'step')"
      />
    </template>
    <template v-if="!props.isTestPlan" #expectedResult="{ record }">
      <a-textarea
        :ref="(el: refItem) => setExpectedRefMap(el, record)"
        v-model="record.expected"
        :max-length="1000"
        size="mini"
        :auto-size="true"
        class="w-max-[267px] param-input"
        :placeholder="t('system.orgTemplate.expectationTip')"
        :disabled="props.isDisabled"
        @blur="blurHandler(record, 'expected')"
      />
    </template>
    <template
      v-if="hasAnyPermission(['PROJECT_TEST_PLAN:READ+EXECUTE']) && !props.isDisabledTestPlan"
      #actualResult="{ record }"
    >
      <div v-if="props.isPreview">{{ record.actualResult }}</div>
      <a-textarea
        v-else
        v-model="record.actualResult"
        :max-length="1000"
        size="mini"
        :auto-size="true"
        class="w-max-[267px] param-input"
        :placeholder="t('system.orgTemplate.actualResultTip')"
      />
    </template>
    <template #lastExecResult="{ record }">
      <a-select
        v-if="hasAnyPermission(['PROJECT_TEST_PLAN:READ+EXECUTE']) && !props.isDisabledTestPlan && !props.isPreview"
        v-model:model-value="record.executeResult"
        :placeholder="t('common.pleaseSelect')"
        class="param-input w-full"
        allow-clear
      >
        <template #label>
          <span class="text-[var(--color-text-2)]"><ExecuteResult :execute-result="record.executeResult" /></span>
        </template>
        <a-option v-for="item in executionResultList" :key="item.key" :value="item.key">
          <ExecuteResult :execute-result="item.key" />
        </a-option>
      </a-select>
      <span v-else class="text-[var(--color-text-2)]"><ExecuteResult :execute-result="record.executeResult" /></span>
    </template>
    <template #operation="{ record }">
      <MsTableMoreAction
        v-if="!record.internal"
        :list="moreActionList"
        @select="(item:ActionsItem) => handleMoreActionSelect(item,record)"
      />
    </template>
  </MsBaseTable>
  <a-button v-if="!props.isDisabled" class="mt-2 px-0" type="text" @click="addStep">
    <template #icon>
      <icon-plus class="text-[14px]" />
    </template>
    {{ t('system.orgTemplate.addStep') }}
  </a-button>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { TableChangeExtra, TableData } from '@arco-design/web-vue';

  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn, MsTableProps } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import ExecuteResult from '@/components/business/ms-case-associate/executeResult.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { getGenerateId } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import type { StepList } from '@/models/caseManagement/featureCase';
  import { LastExecuteResults } from '@/enums/caseEnum';

  import { executionResultMap } from '@/views/case-management/caseManagementFeature/components/utils';

  type refItem = Element | ComponentPublicInstance | null;
  const { t } = useI18n();

  const props = withDefaults(
    defineProps<{
      stepList: any;
      isDisabled?: boolean;
      isScrollY?: boolean;
      scrollY?: number;
      isTestPlan?: boolean;
      isDisabledTestPlan?: boolean;
      isPreview?: boolean; // 仅预览不展示状态可操作下拉和文本框
    }>(),
    {
      isDisabled: false,
      isScrollY: true,
    }
  );

  const emit = defineEmits(['update:stepList']);

  const executionResultList = computed(() =>
    Object.values(executionResultMap).filter((item) => item.key !== LastExecuteResults.PENDING)
  );

  // 步骤描述
  const stepData = ref<StepList[]>([
    {
      id: getGenerateId(),
      step: '',
      expected: '',
      showStep: false,
      showExpected: false,
    },
  ]);

  const templateFieldColumns = ref<MsTableColumn>([
    {
      title: 'system.orgTemplate.numberIndex',
      dataIndex: 'index',
      slotName: 'index',
      width: 100,
      showDrag: false,
      showInTable: true,
    },
    {
      title: 'system.orgTemplate.useCaseStep',
      slotName: 'caseStep',
      dataIndex: 'step',
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'system.orgTemplate.expectedResult',
      dataIndex: 'expected',
      slotName: 'expectedResult',
      showDrag: true,
      showInTable: true,
    },
    ...(!props.isTestPlan
      ? []
      : [
          {
            title: 'system.orgTemplate.actualResult',
            dataIndex: 'actualResult',
            slotName: 'actualResult',
            showDrag: true,
            showInTable: true,
          },
          {
            title: 'system.orgTemplate.stepExecutionResult',
            dataIndex: 'executeResult',
            slotName: 'lastExecResult',
            showDrag: true,
            showInTable: true,
          },
        ]),
    {
      title: 'system.orgTemplate.operation',
      slotName: 'operation',
      fixed: 'right',
      width: 120,
      showInTable: true,
      showDrag: false,
    },
  ]);

  const moreActions: ActionsItem[] = [
    {
      label: 'caseManagement.featureCase.copyStep',
      eventTag: 'copyStep',
    },
    {
      label: 'caseManagement.featureCase.InsertStepsBefore',
      eventTag: 'InsertStepsBefore',
    },
    {
      label: 'caseManagement.featureCase.afterInsertingSteps',
      eventTag: 'afterInsertingSteps',
    },
    {
      isDivider: true,
    },
    {
      label: 'common.delete',
      danger: true,
      eventTag: 'delete',
    },
  ];

  const moreActionList = computed(() => {
    return stepData.value.length <= 1 ? moreActions.slice(0, moreActions.length - 2) : moreActions;
  });

  const tableProps = ref<Partial<MsTableProps<StepList>>>({
    columns: templateFieldColumns.value,
    scroll: { x: '100%', y: props.isScrollY ? props.scrollY ?? 400 : '' },
    selectable: false,
    noDisable: true,
    showSetting: false,
    showPagination: false,
    draggable: { type: 'handle' },
    draggableCondition: true,
  });

  const { propsRes, propsEvent, setProps } = useTable(undefined, tableProps.value);

  watch(
    () => props.isDisabled,
    (val) => {
      tableProps.value.draggableCondition = !val;
    },
    {
      immediate: true,
    }
  );

  // 复制步骤
  function copyStep(record: StepList) {
    const index = stepData.value.map((item: any) => item.id).indexOf(record.id);
    const insertItem = {
      ...record,
      id: getGenerateId(),
    };
    stepData.value.splice(index + 1, 0, insertItem);
  }

  // 删除步骤
  function deleteStep(record: StepList) {
    stepData.value = stepData.value.filter((item: any) => item.id !== record.id);
    setProps({ data: stepData.value });
  }

  // 步骤之前插入步骤
  function insertStepsBefore(record: StepList) {
    const index = stepData.value.map((item: any) => item.id).indexOf(record.id);
    const insertItem = {
      id: getGenerateId(),
      step: '',
      expected: '',
      showStep: false,
      showExpected: false,
    };
    stepData.value.splice(index, 0, insertItem);
  }

  // 步骤之后插入步骤
  function afterInsertingSteps(record: StepList) {
    const index = stepData.value.map((item: any) => item.id).indexOf(record.id);
    const insertItem = {
      id: getGenerateId(),
      step: '',
      expected: '',
      showStep: false,
      showExpected: false,
    };
    stepData.value.splice(index + 1, 0, insertItem);
  }

  // 更多操作
  const handleMoreActionSelect = (item: ActionsItem, record: StepList) => {
    switch (item.eventTag) {
      case 'copyStep':
        copyStep(record);
        break;
      case 'InsertStepsBefore':
        insertStepsBefore(record);
        break;
      case 'afterInsertingSteps':
        afterInsertingSteps(record);
        break;
      default:
        deleteStep(record);
        break;
    }
  };

  // 添加步骤
  const addStep = () => {
    stepData.value.push({
      id: getGenerateId(),
      step: '',
      expected: '',
      showStep: false,
      showExpected: false,
    });
  };

  const refStepMap: Record<string, any> = {};
  function setStepRefMap(el: refItem, record: StepList) {
    if (el) {
      refStepMap[`${record.id}`] = el;
    }
  }
  const expectedRefMap: Record<string, any> = {};

  function setExpectedRefMap(el: refItem, record: StepList) {
    if (el) {
      expectedRefMap[`${record.id}`] = el;
    }
  }
  // 编辑步骤
  function edit(record: StepList, type: string) {
    if (props.isDisabled) return;
    if (type === 'step') {
      record.showStep = true;
      nextTick(() => {
        refStepMap[record.id]?.focus();
      });
    } else {
      record.showExpected = true;
      nextTick(() => {
        expectedRefMap[record.id]?.focus();
      });
    }
  }

  // 失去焦点回调
  function blurHandler(record: StepList, type: string) {
    if (props.isDisabled) return;
    if (type === 'step') {
      record.showStep = false;
    } else {
      record.showExpected = false;
    }
  }

  const tableRef = ref<InstanceType<typeof MsBaseTable> | null>(null);

  watchEffect(() => {
    if (props.isDisabled) {
      tableRef.value?.initColumn(templateFieldColumns.value.slice(0, templateFieldColumns.value.length - 1));
    } else {
      tableRef.value?.initColumn(templateFieldColumns.value);
    }
  });

  function changeHandler(data: TableData[], extra: TableChangeExtra, currentData: TableData[]) {
    if (!currentData || currentData.length === 1) {
      return false;
    }
    stepData.value = data as StepList[];
  }

  watch(
    () => stepData.value,
    (val) => {
      emit('update:stepList', val);
      setProps({ data: stepData.value });
    },
    { deep: true }
  );

  watch(
    () => props.stepList,
    () => {
      stepData.value = props.stepList;
    },
    {
      immediate: true,
    }
  );

  onBeforeMount(() => {
    setProps({ data: stepData.value });
  });
</script>

<style scoped lang="less">
  .circle {
    width: 16px;
    height: 16px;
    line-height: 16px;
    border-radius: 50%;
    text-align: center;
    color: var(--color-text-4);
    background: var(--color-text-n8);
  }
  :deep(.param-input:not(.arco-input-focus, .arco-select-view-focus)) {
    &:not(:hover) {
      border-color: transparent !important;
      .arco-input::placeholder {
        @apply invisible;
      }
      .arco-select-view-icon {
        @apply invisible;
      }
      .arco-select-view-value {
        color: var(--color-text-brand);
      }
    }
  }
  :deep(.arco-textarea-wrapper.arco-textarea-disabled) {
    background: transparent;
  }
</style>
