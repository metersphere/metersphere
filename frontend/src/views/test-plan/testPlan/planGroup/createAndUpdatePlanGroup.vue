<template>
  <a-modal
    v-model:visible="innerVisible"
    title-align="start"
    :title="modelTitle"
    body-class="p-0"
    :width="600"
    :cancel-button-props="{ disabled: confirmLoading }"
    :ok-loading="confirmLoading"
    :ok-text="t('caseManagement.caseReview.commitResult')"
    @before-ok="handleConfirm"
    @close="handleCancel"
  >
    <a-form ref="formRef" :model="form" layout="vertical">
      <a-form-item
        field="name"
        :label="t('testPlan.testPlanGroup.name')"
        :rules="[{ required: true, message: t('testPlan.planForm.nameGroupPlaceholder') }]"
        asterisk-position="end"
      >
        <a-input
          v-model:model-value="form.name"
          :max-length="255"
          :placeholder="t('testPlan.testPlanGroup.planNamePlaceholder')"
        />
      </a-form-item>
      <a-form-item :label="t('caseManagement.featureCase.ModuleOwned')">
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
              <div class="one-line-text w-[400px]">{{ node.name }}</div>
            </a-tooltip>
          </template>
        </a-tree-select>
      </a-form-item>
      <a-form-item field="tags" :label="t('common.tag')">
        <MsTagsInput v-model:modelValue="form.tags"></MsTagsInput>
      </a-form-item>
    </a-form>

    <template #footer>
      <a-button type="secondary" @click="handleCancel">{{ t('common.cancel') }}</a-button>
      <a-button class="ml-[12px]" type="primary" :loading="confirmLoading" @click="handleConfirm">
        {{ okText }}
      </a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { FormInstance, Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';

  import { addTestPlan, getTestPlanDetail, updateTestPlan } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { filterTreeNode } from '@/utils';

  import { ModuleTreeNode } from '@/models/common';
  import type { AddTestPlanParams } from '@/models/testPlan/testPlan';
  import { testPlanTypeEnum } from '@/enums/testPlanEnum';

  const appStore = useAppStore();

  const { t } = useI18n();

  const props = defineProps<{
    planGroupId?: string;
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

  const initPlanGroupForm: AddTestPlanParams = {
    groupId: 'NONE',
    name: '',
    projectId: appStore.currentProjectId,
    moduleId: 'root',
    cycle: [],
    tags: [],
    description: '',
    testPlanning: false,
    automaticStatusUpdate: true,
    repeatCase: false,
    passThreshold: 100,
    type: testPlanTypeEnum.GROUP,
    baseAssociateCaseRequest: { selectIds: [], selectAll: false, condition: {} },
  };
  const form = ref<AddTestPlanParams>(cloneDeep(initPlanGroupForm));
  const confirmLoading = ref<boolean>(false);

  const formRef = ref<FormInstance>();

  function handleCancel() {
    innerVisible.value = false;
    formRef.value?.resetFields();
    form.value = cloneDeep(initPlanGroupForm);
    emit('close');
  }

  function handleConfirm() {
    formRef.value?.validate(async (errors) => {
      if (!errors) {
        confirmLoading.value = true;
        try {
          const params = {
            ...cloneDeep(form.value),
            groupId: 'NONE',
            projectId: appStore.currentProjectId,
            type: testPlanTypeEnum.GROUP,
          };
          if (!props.planGroupId?.length) {
            await addTestPlan(params);
            Message.success(t('common.createSuccess'));
          } else {
            await updateTestPlan(params);
            Message.success(t('common.updateSuccess'));
          }
          emit('loadPlanList');
          handleCancel();
        } catch (error) {
          console.log(error);
        } finally {
          confirmLoading.value = false;
        }
      }
    });
  }

  const modelTitle = computed(() => {
    return props.planGroupId
      ? t('testPlan.testPlanGroup.updatePlanGroupTitle')
      : t('testPlan.testPlanGroup.newPlanGroupTitle');
  });

  async function getDetail() {
    try {
      if (props.planGroupId?.length) {
        const result = await getTestPlanDetail(props.planGroupId);
        form.value = cloneDeep(result);
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const okText = computed(() => {
    return props.planGroupId ? t('common.update') : t('common.create');
  });

  watch(
    () => innerVisible.value,
    (val) => {
      if (val) {
        form.value = cloneDeep(initPlanGroupForm);
        getDetail();
        form.value.moduleId = props.moduleId && props.moduleId !== 'all' ? props.moduleId : 'root';
      }
    }
  );
</script>

<style scoped></style>
