<template>
  <MsDrawer
    v-model:visible="innerVisible"
    :title="modelTitle"
    :width="800"
    unmount-on-close
    :ok-text="okText"
    :save-continue-text="t('case.saveContinueText')"
    :show-continue="!props.planId?.length"
    :ok-loading="drawerLoading"
    @confirm="handleDrawerConfirm(false)"
    @continue="handleDrawerConfirm(true)"
    @cancel="handleCancel"
  >
    <a-form ref="formRef" :model="form" layout="vertical">
      <a-form-item
        field="name"
        :label="t('caseManagement.featureCase.planName')"
        :rules="[{ required: true, message: t('testPlan.planForm.nameRequired') }]"
        asterisk-position="end"
        class="w-[732px]"
      >
        <a-input v-model="form.name" :max-length="255" :placeholder="t('testPlan.planForm.namePlaceholder')" />
      </a-form-item>
      <a-form-item field="description" :label="t('common.desc')" class="w-[732px]">
        <a-textarea v-model:model-value="form.description" :placeholder="t('common.pleaseInput')" :max-length="1000" />
      </a-form-item>
      <a-form-item
        field="type"
        :label="props.planId?.length ? t('caseManagement.featureCase.moveTo') : t('testPlan.planForm.createTo')"
      >
        <a-radio-group v-model:model-value="form.isGroup">
          <a-radio :value="false">{{ t('testPlan.testPlanGroup.module') }}</a-radio>
          <a-radio :value="true">{{ t('testPlan.testPlanIndex.testPlanGroup') }}</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item
        v-if="form.isGroup"
        field="groupId"
        :rules="[{ required: true, message: t('testPlan.planForm.testPlanGroupRequired') }]"
        :label="t('testPlan.testPlanIndex.testPlanGroup')"
        class="w-[436px]"
        asterisk-position="end"
      >
        <a-select v-model="form.groupId" allow-search :placeholder="t('common.pleaseSelect')">
          <a-option v-for="item of groupList" :key="item.id" :value="item.id">
            {{ item.name }}
          </a-option>
        </a-select>
      </a-form-item>
      <a-form-item v-else field="moduleId" :label="t('common.belongModule')" class="w-[436px]">
        <a-tree-select
          v-model="form.moduleId"
          :data="props.moduleTree"
          :field-names="{ title: 'name', key: 'id', children: 'children' }"
          :tree-props="{
            virtualListProps: {
              height: 200,
              threshold: 200,
            },
          }"
          allow-search
          :filter-tree-node="filterTreeNode"
        >
          <template #tree-slot-title="node">
            <a-tooltip :content="`${node.name}`" position="tl">
              <div class="inline-flex w-full">
                <div class="one-line-text w-[240px]">
                  {{ node.name }}
                </div>
              </div>
            </a-tooltip>
          </template>
        </a-tree-select>
      </a-form-item>
      <a-form-item
        field="cycle"
        :label="t('testPlan.planForm.planStartAndEndTime')"
        asterisk-position="end"
        class="w-[436px]"
      >
        <a-range-picker
          v-model:model-value="form.cycle"
          show-time
          value-format="timestamp"
          :separator="t('common.to')"
          :time-picker-props="{
            defaultValue: tempRange,
          }"
          :disabled-time="disabledTime"
          @select="handleTimeSelect"
        />
      </a-form-item>
      <a-form-item field="tags" :label="t('common.tag')" class="w-[436px]">
        <MsTagsInput v-model:model-value="form.tags" :max-tag-count="10" />
      </a-form-item>
      <MsMoreSettingCollapse>
        <template #content>
          <div v-for="item in switchList" :key="item.key" class="mb-[24px] flex items-center gap-[8px]">
            <a-switch v-model="form[item.key]" size="small" />
            {{ t(item.label) }}
            <a-tooltip :position="item.tooltipPosition">
              <template #content>
                <div v-for="descItem in item.desc" :key="descItem">{{ t(descItem) }}</div>
              </template>
              <IconQuestionCircle class="h-[16px] w-[16px] text-[--color-text-4] hover:text-[rgb(var(--primary-5))]" />
            </a-tooltip>
          </div>
          <a-form-item
            field="passThreshold"
            :label="t('testPlan.planForm.passThreshold')"
            :rules="[{ required: true, message: t('testPlan.planForm.passThresholdRequired') }]"
            asterisk-position="end"
          >
            <a-input-number
              v-model:model-value="form.passThreshold"
              size="small"
              mode="button"
              class="w-[120px]"
              :min="1"
              :max="100"
              :precision="2"
              :default-value="100"
            />
            <template #label>
              {{ t('testPlan.planForm.passThreshold') }}
              <a-tooltip position="tl" :content="t('testPlan.planForm.passThresholdTip')">
                <IconQuestionCircle
                  class="h-[16px] w-[16px] text-[--color-text-4] hover:text-[rgb(var(--primary-5))]"
                />
              </a-tooltip>
            </template>
          </a-form-item>
        </template>
      </MsMoreSettingCollapse>
    </a-form>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { FormInstance, Message, SelectOptionData, ValidatedError } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';
  import dayjs from 'dayjs';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsMoreSettingCollapse from '@/components/pure/ms-more-setting-collapse/index.vue';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';

  import {
    addTestPlan,
    getPlanGroupOptions,
    getTestPlanDetail,
    updateTestPlan,
  } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { filterTreeNode } from '@/utils';

  import { ModuleTreeNode } from '@/models/common';
  import type { AddTestPlanParams, SwitchListModel } from '@/models/testPlan/testPlan';
  import { TestPlanRouteEnum } from '@/enums/routeEnum';
  import { testPlanTypeEnum } from '@/enums/testPlanEnum';

  import { DisabledTimeProps } from '@arco-design/web-vue/es/date-picker/interface';

  const props = defineProps<{
    planId?: string;
    moduleTree?: ModuleTreeNode[];
    moduleId?: string;
  }>();
  const innerVisible = defineModel<boolean>('visible', {
    required: true,
  });

  const emit = defineEmits<{
    (e: 'close'): void;
    (e: 'loadPlanList'): void;
  }>();

  const { t } = useI18n();
  const appStore = useAppStore();
  const router = useRouter();

  const drawerLoading = ref(false);
  const formRef = ref<FormInstance>();

  const moduleId = computed(() => {
    return props.moduleId && props.moduleId !== 'all' ? props.moduleId : 'root';
  });

  const initForm: AddTestPlanParams = {
    isGroup: false,
    name: '',
    projectId: '',
    moduleId: moduleId.value,
    cycle: [],
    tags: [],
    description: '',
    testPlanning: false,
    automaticStatusUpdate: false,
    repeatCase: false,
    passThreshold: 100,
    type: testPlanTypeEnum.TEST_PLAN,
    baseAssociateCaseRequest: { selectIds: [], selectAll: false, condition: {} },
  };
  const form = ref<AddTestPlanParams>(cloneDeep(initForm));

  const tempRange = ref<(Date | string | number)[]>(['00:00:00', '00:00:00']);

  function makeLessNumbers(value: number, isSecond = false) {
    const res = [];
    for (let i = 0; i < value; i++) {
      res.push(i);
    }
    return isSecond && res.length === 0 ? [0] : res; // 秒至少相差 1 秒
  }

  function disabledTime(current: Date, type: 'start' | 'end'): DisabledTimeProps {
    if (type === 'end') {
      const currentDate = dayjs(current);
      const startDate = dayjs(tempRange.value[0]);
      // 结束时间至少比开始时间多一秒
      return {
        disabledHours: () => {
          if (currentDate.isSame(startDate, 'D')) {
            return makeLessNumbers(startDate.get('h'));
          }
          return [];
        },
        disabledMinutes: () => {
          if (currentDate.isSame(startDate, 'D') && currentDate.isSame(startDate, 'h')) {
            return makeLessNumbers(startDate.get('m'));
          }
          return [];
        },
        disabledSeconds: () => {
          if (
            currentDate.isSame(startDate, 'D') &&
            currentDate.isSame(startDate, 'h') &&
            currentDate.isSame(startDate, 'm')
          ) {
            return makeLessNumbers(startDate.get('s'), true);
          }
          return [];
        },
      };
    }
    return {};
  }

  function handleTimeSelect(value: (Date | string | number | undefined)[]) {
    if (value) {
      // 要用 留着
      // const start = dayjs(value[0]);
      // const end = dayjs(value[1]);
      // if (start.isSame(end, 'D') && end.hour() === 0 && end.minute() === 0 && end.second() === 0) {
      //   const newEnd = end.hour(23).minute(59).second(59);
      //   value[1] = newEnd.valueOf();
      // }
      const start = (value as number[])[0];
      const end = (value as number[])[1];
      if (start > end) {
        tempRange.value = [end, start];
      } else {
        tempRange.value = value as number[];
      }
    }
  }

  const switchList: SwitchListModel[] = [
    {
      key: 'repeatCase',
      label: 'testPlan.planForm.associateRepeatCase',
      tooltipPosition: 'bl',
      desc: ['testPlan.planForm.repeatCaseTip1', 'testPlan.planForm.repeatCaseTip2'],
    },
    {
      key: 'automaticStatusUpdate',
      label: 'testPlan.planForm.allowUpdateStatus',
      tooltipPosition: 'bl',
      desc: ['testPlan.planForm.enableAutomaticStatusTip', 'testPlan.planForm.closeAutomaticStatusTip'],
    },
  ];

  function handleCancel() {
    innerVisible.value = false;
    formRef.value?.resetFields();
    form.value = cloneDeep(initForm);
    emit('close');
  }

  function toDetail(id: string) {
    router.push({
      name: TestPlanRouteEnum.TEST_PLAN_INDEX_DETAIL,
      query: {
        id,
      },
    });
  }

  function handleDrawerConfirm(isContinue: boolean) {
    formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (!errors) {
        drawerLoading.value = true;
        let res = null;
        try {
          const params: AddTestPlanParams = {
            ...cloneDeep(form.value),
            plannedStartTime: form.value.cycle ? form.value.cycle[0] : undefined,
            plannedEndTime: form.value.cycle ? form.value.cycle[1] : undefined,
            projectId: appStore.currentProjectId,
          };
          if (!params.isGroup && params.groupId) {
            delete params.groupId;
          }
          if (!props.planId?.length) {
            res = await addTestPlan(params);
            Message.success(t('common.createSuccess'));
          } else {
            await updateTestPlan(params);
            Message.success(t('common.updateSuccess'));
          }
          emit('loadPlanList');
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          drawerLoading.value = false;
        }
        if (!isContinue) {
          if (!props.planId?.length) {
            // 跳转到测试计划详情
            toDetail(res.id);
          }
          handleCancel();
        } else {
          form.value = { ...cloneDeep(initForm), moduleId: form.value.moduleId };
        }
      }
    });
  }

  async function getDetail() {
    try {
      if (props.planId?.length) {
        const result = await getTestPlanDetail(props.planId);
        form.value = cloneDeep(result);

        form.value.cycle = [result.plannedStartTime as number, result.plannedEndTime as number];
        form.value.passThreshold = parseFloat(result.passThreshold.toString());
        form.value.isGroup = result.groupId !== 'NONE';
        form.value.groupId = result.groupId !== 'NONE' ? result.groupId : '';
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const groupList = ref<SelectOptionData>([]);
  async function initGroupOptions() {
    try {
      groupList.value = await getPlanGroupOptions(appStore.currentProjectId);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  watch(
    () => innerVisible.value,
    (val) => {
      if (val) {
        form.value = cloneDeep(initForm);
        getDetail();
        initGroupOptions();
        form.value.moduleId = moduleId.value;
      }
    }
  );

  const modelTitle = computed(() => {
    return props.planId ? t('testPlan.testPlanIndex.updateTestPlan') : t('testPlan.testPlanIndex.createTestPlan');
  });

  const okText = computed(() => {
    return props.planId ? t('common.update') : t('common.create');
  });
</script>
