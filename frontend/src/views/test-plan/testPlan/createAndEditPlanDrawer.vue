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
        class="w-[732px]"
      >
        <a-input v-model="form.name" :max-length="255" :placeholder="t('testPlan.planForm.namePlaceholder')" />
      </a-form-item>
      <a-form-item field="description" :label="t('common.desc')" class="w-[732px]">
        <a-textarea v-model:model-value="form.description" :placeholder="t('common.pleaseInput')" :max-length="1000" />
      </a-form-item>
      <a-form-item :label="t('common.belongModule')" class="w-[436px]">
        <a-tree-select
          v-model:modelValue="form.moduleId"
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
                <div class="one-line-text w-[240px] text-[var(--color-text-1)]">
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
            defaultValue: ['00:00:00', '00:00:00'],
          }"
          :disabled-time="disabledTime"
          @select="handleTimeSelect"
        />
      </a-form-item>
      <a-form-item field="tags" :label="t('common.tag')" class="w-[436px]">
        <MsTagsInput v-model:model-value="form.tags" :max-tag-count="10" />
      </a-form-item>
      <a-form-item v-if="!props.planId?.length">
        <template #label>
          <div class="flex items-center">
            {{ t('testPlan.planForm.pickCases') }}
            <a-divider margin="4px" direction="vertical" />
            <MsButton
              type="text"
              :disabled="form.baseAssociateCaseRequest?.selectIds.length === 0"
              @click="clearSelectedCases"
            >
              {{ t('caseManagement.caseReview.clearSelectedCases') }}
            </MsButton>
          </div>
        </template>
        <div class="flex w-[436px] items-center rounded bg-[var(--color-text-n9)] p-[12px]">
          <div class="text-[var(--color-text-2)]">
            {{
              t('caseManagement.caseReview.selectedCases', {
                count: getSelectedCount,
              })
            }}
          </div>
          <a-divider margin="8px" direction="vertical" />
          <MsButton type="text" class="font-medium" @click="caseAssociateVisible = true">
            {{ t('ms.case.associate.title') }}
          </MsButton>
        </div>
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
          <a-form-item field="passThreshold" :label="t('testPlan.planForm.passThreshold')">
            <a-input-number
              v-model:model-value="form.passThreshold"
              size="small"
              mode="button"
              class="w-[120px]"
              :min="1"
              :max="100"
              :default-value="100"
            />
          </a-form-item>
        </template>
      </MsMoreSettingCollapse>
    </a-form>
  </MsDrawer>
  <AssociateDrawer
    v-model:visible="caseAssociateVisible"
    :has-not-associated-ids="form.baseAssociateCaseRequest?.selectIds"
    @success="writeAssociateCases"
  />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { FormInstance, Message, TreeNodeData, ValidatedError } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsMoreSettingCollapse from '@/components/pure/ms-more-setting-collapse/index.vue';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import AssociateDrawer from './components/associateDrawer.vue';

  import { addTestPlan, copyTestPlan, getTestPlanDetail, updateTestPlan } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import { ModuleTreeNode } from '@/models/common';
  import type { AddTestPlanParams, AssociateCaseRequest, SwitchListModel } from '@/models/testPlan/testPlan';
  import { testPlanTypeEnum } from '@/enums/testPlanEnum';

  import { DisabledTimeProps } from '@arco-design/web-vue/es/date-picker/interface';

  const props = defineProps<{
    planId?: string;
    moduleTree?: ModuleTreeNode[];
    isCopy: boolean;
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

  const drawerLoading = ref(false);
  const formRef = ref<FormInstance>();
  const initForm: AddTestPlanParams = {
    groupId: 'NONE',
    name: '',
    projectId: '',
    moduleId: 'root',
    cycle: [],
    tags: [],
    description: '',
    testPlanning: false,
    automaticStatusUpdate: true,
    repeatCase: false,
    passThreshold: 100,
    type: testPlanTypeEnum.TEST_PLAN,
    baseAssociateCaseRequest: { selectIds: [], selectAll: false, condition: {} },
  };
  const form = ref<AddTestPlanParams>(cloneDeep(initForm));

  function filterTreeNode(searchValue: string, nodeData: TreeNodeData) {
    return (nodeData as ModuleTreeNode).name.toLowerCase().indexOf(searchValue.toLowerCase()) > -1;
  }

  const tempRange = ref<(Date | string | number | undefined)[]>([]);

  function makeLessNumbers(value: number) {
    const res = [];
    for (let i = 0; i < value; i++) {
      res.push(i);
    }
    return res;
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
            return makeLessNumbers(startDate.get('s'));
          }
          return [];
        },
      };
    }
    return {};
  }

  function handleTimeSelect(value: (Date | string | number | undefined)[]) {
    tempRange.value = value;
  }

  const switchList: SwitchListModel[] = [
    {
      key: 'repeatCase',
      label: 'testPlan.planForm.associateRepeatCase',
      tooltipPosition: 'bl',
      desc: ['testPlan.planForm.repeatCaseTip1', 'testPlan.planForm.repeatCaseTip2'],
    },
  ];

  const caseAssociateVisible = ref(false);
  function clearSelectedCases() {
    form.value.baseAssociateCaseRequest = cloneDeep(initForm.baseAssociateCaseRequest);
  }
  function writeAssociateCases(param: AssociateCaseRequest) {
    form.value.baseAssociateCaseRequest = { ...param };
  }

  function handleCancel() {
    innerVisible.value = false;
    formRef.value?.resetFields();
    form.value = cloneDeep(initForm);
    emit('close');
  }

  function handleDrawerConfirm(isContinue: boolean) {
    formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (!errors) {
        drawerLoading.value = true;
        try {
          const {
            id,
            name,
            moduleId,
            tags,
            description,
            testPlanning,
            automaticStatusUpdate,
            repeatCase,
            passThreshold,
            groupOption,
          } = form.value;
          const params: AddTestPlanParams = {
            ...cloneDeep(form.value),
            groupId: 'NONE',
            plannedStartTime: form.value.cycle ? form.value.cycle[0] : undefined,
            plannedEndTime: form.value.cycle ? form.value.cycle[1] : undefined,
            projectId: appStore.currentProjectId,
          };
          if (!props.planId?.length) {
            await addTestPlan(params);
            Message.success(t('common.createSuccess'));
          } else {
            if (props.isCopy) {
              const copyParams: AddTestPlanParams = {
                id,
                groupId: 'NONE',
                name,
                moduleId,
                tags,
                description,
                testPlanning,
                automaticStatusUpdate,
                repeatCase,
                passThreshold,
                baseAssociateCaseRequest: null,
                groupOption,
                plannedStartTime: form.value.cycle ? form.value.cycle[0] : undefined,
                plannedEndTime: form.value.cycle ? form.value.cycle[1] : undefined,
                projectId: appStore.currentProjectId,
                type: testPlanTypeEnum.TEST_PLAN,
              };
              await copyTestPlan(copyParams);
            } else {
              await updateTestPlan(params);
            }

            Message.success(props.isCopy ? t('common.copySuccess') : t('common.updateSuccess'));
          }
          emit('loadPlanList');
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          drawerLoading.value = false;
        }
        if (!isContinue) {
          handleCancel();
        }
        form.value.name = '';
      }
    });
  }

  async function getDetail() {
    try {
      if (props.planId?.length) {
        const result = await getTestPlanDetail(props.planId);
        form.value = cloneDeep(result);
        if (props.isCopy) {
          let copyName = `copy_${result.name}`;
          copyName = copyName.length > 255 ? copyName.slice(0, 255) : copyName;
          form.value.name = copyName;
        }

        form.value.cycle = [result.plannedStartTime as number, result.plannedEndTime as number];
      }
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
        if (!props.planId && props.moduleId) {
          form.value.moduleId = props.moduleId === 'all' ? 'root' : props.moduleId;
        }
      }
    }
  );

  const modelTitle = computed(() => {
    if (props.planId) {
      return props.isCopy ? t('testPlan.testPlanIndex.copyTestPlan') : t('testPlan.testPlanIndex.updateTestPlan');
    }
    return t('testPlan.testPlanIndex.createTestPlan');
  });

  const okText = computed(() => {
    if (props.planId) {
      return props.isCopy ? t('common.copy') : t('common.update');
    }
    return t('common.create');
  });

  const getSelectedCount = computed(() => {
    if (props.planId) {
      return form.value?.functionalCaseCount || 0;
    }
    return form.value.baseAssociateCaseRequest?.selectAll
      ? form.value.baseAssociateCaseRequest?.totalCount
      : form.value.baseAssociateCaseRequest?.selectIds.length;
  });
</script>
