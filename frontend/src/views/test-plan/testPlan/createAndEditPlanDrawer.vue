<template>
  <MsDrawer
    v-model:visible="innerVisible"
    :title="form.id ? t('case.updateCase') : t('testPlan.testPlanIndex.createTestPlan')"
    :width="800"
    unmount-on-close
    :ok-text="form.id ? 'common.update' : 'common.create'"
    :save-continue-text="t('case.saveContinueText')"
    :show-continue="!form.id"
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
        />
      </a-form-item>
      <a-form-item field="tags" :label="t('common.tag')" class="w-[436px]">
        <MsTagsInput v-model:model-value="form.tags" :max-tag-count="10" :max-length="50" />
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
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { FormInstance, Message, TreeNodeData } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsMoreSettingCollapse from '@/components/pure/ms-more-setting-collapse/index.vue';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';

  import { addTestPlan } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import { ModuleTreeNode } from '@/models/common';
  import type { AddTestPlanParams, SwitchListModel } from '@/models/testPlan/testPlan';
  import { testPlanTypeEnum } from '@/enums/testPlanEnum';

  const { t } = useI18n();
  const appStore = useAppStore();

  const props = defineProps<{
    moduleTree?: ModuleTreeNode[];
  }>();
  const innerVisible = defineModel<boolean>('visible', {
    required: true,
  });

  const drawerLoading = ref(false);
  const formRef = ref<FormInstance>();
  const initForm: AddTestPlanParams = {
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

  const switchList: SwitchListModel[] = [
    {
      key: 'repeatCase',
      label: 'testPlan.planForm.associateRepeatCase',
      tooltipPosition: 'bl',
      desc: ['testPlan.planForm.repeatCaseTip1', 'testPlan.planForm.repeatCaseTip2'],
    },
  ];

  function handleCancel() {
    innerVisible.value = false;
    formRef.value?.resetFields();
    form.value = cloneDeep(initForm);
  }

  function handleDrawerConfirm(isContinue: boolean) {
    formRef.value?.validate(async (errors) => {
      if (!errors) {
        drawerLoading.value = true;
        try {
          // TODO 更新
          const params: AddTestPlanParams = {
            ...cloneDeep(form.value),
            plannedStartTime: form.value.cycle ? form.value.cycle[0] : undefined,
            plannedEndTime: form.value.cycle ? form.value.cycle[1] : undefined,
            projectId: appStore.currentProjectId,
          };
          if (!form.value?.id) {
            await addTestPlan(params);
            Message.success(t('common.createSuccess'));
          }
          // TODO 刷新外层数据
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
</script>
