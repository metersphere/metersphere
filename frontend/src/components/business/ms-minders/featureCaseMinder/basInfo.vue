<template>
  <div class="h-full pl-[16px]">
    <div class="baseInfo-form">
      <a-skeleton v-if="baseInfoLoading || props.loading" :loading="baseInfoLoading || props.loading" :animation="true">
        <a-space direction="vertical" class="w-full" size="large">
          <a-skeleton-line :rows="10" :line-height="30" :line-spacing="30" />
        </a-space>
      </a-skeleton>
      <a-form
        v-else
        ref="baseInfoFormRef"
        scroll-to-first-error
        :model="baseInfoForm"
        :disabled="!hasEditPermission"
        layout="vertical"
      >
        <a-form-item
          field="name"
          :label="t('ms.minders.caseName')"
          :rules="[{ required: true, message: t('ms.minders.caseNameNotNull') }]"
          asterisk-position="end"
        >
          <a-input v-model:model-value="baseInfoForm.name" :placeholder="t('common.pleaseInput')" allow-clear></a-input>
        </a-form-item>
        <MsFormCreate
          v-if="formRules.length"
          v-model:api="fApi"
          v-model:form-item="formItem"
          :form-rule="formRules"
          :disabled="!hasEditPermission"
        />
        <a-form-item field="tags" :label="t('common.tag')">
          <MsTagsInput v-model:model-value="baseInfoForm.tags" :max-tag-count="6" />
        </a-form-item>
      </a-form>
    </div>
    <div v-if="hasEditPermission" class="flex items-center gap-[12px] bg-[var(--color-text-fff)] py-[16px]">
      <a-tooltip :content="t('ms.minders.moduleNewTip')" :disabled="!props.activeCase.moduleIsNew">
        <a-button
          v-permission="['FUNCTIONAL_CASE:READ+UPDATE']"
          type="primary"
          :loading="saveLoading"
          :disabled="props.activeCase.moduleIsNew"
          @click="handleSave"
        >
          {{ t('common.save') }}
        </a-button>
      </a-tooltip>
      <a-button type="secondary" :disabled="saveLoading" @click="handleCancel">{{ t('common.cancel') }}</a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { FormInstance, Message } from '@arco-design/web-vue';

  import MsFormCreate from '@/components/pure/ms-form-create/ms-form-create.vue';
  import { FormItem, FormRuleItem } from '@/components/pure/ms-form-create/types';
  import { MinderJsonNode } from '@/components/pure/ms-minder-editor/props';
  import { setPriorityView } from '@/components/pure/ms-minder-editor/script/tool/utils';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';

  import {
    createCaseRequest,
    getCaseDefaultFields,
    updateCaseRequest,
  } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import useUserStore from '@/store/modules/user';
  import { hasAnyPermission } from '@/utils/permission';

  import { customFieldsItem, OptionsField } from '@/models/caseManagement/featureCase';

  import { initFormCreate } from '@/views/case-management/caseManagementFeature/components/utils';
  import { Api } from '@form-create/arco-design';

  const props = defineProps<{
    activeCase: Record<string, any>;
    loading: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'initTemplate', id: string): void;
    (e: 'cancel'): void;
    (e: 'saved'): void;
  }>();

  const appStore = useAppStore();
  const userStore = useUserStore();
  const { t } = useI18n();

  const hasEditPermission = hasAnyPermission(['FUNCTIONAL_CASE:READ+MINDER']);
  const baseInfoFormRef = ref<FormInstance>();
  const baseInfoForm = ref({
    name: '',
    tags: [] as string[],
    templateId: '',
    moduleId: props.activeCase.moduleId || 'root',
  });
  const baseInfoLoading = ref(false);

  const priorityField = ref('');

  const formRules = ref<FormItem[]>([]);
  const formItem = ref<FormRuleItem[]>([]);
  const fApi = ref<Api>();
  // 初始化模板默认字段
  async function initDefaultFields() {
    formRules.value = [];
    try {
      baseInfoLoading.value = true;
      const res = await getCaseDefaultFields(appStore.currentProjectId);
      const { customFields, id } = res;
      baseInfoForm.value.templateId = id;
      const result = customFields.map((item: any) => {
        const memberType = ['MEMBER', 'MULTIPLE_MEMBER'];
        let initValue = item.defaultValue;
        const optionsValue: OptionsField[] = item.options;
        if (memberType.includes(item.type)) {
          if (item.defaultValue === 'CREATE_USER' || item.defaultValue.includes('CREATE_USER')) {
            initValue = item.type === 'MEMBER' ? userStore.id : [userStore.id];
          }
        }
        if (item.internal && item.type === 'SELECT') {
          priorityField.value = item.fieldId;
          // 这里不需要再展示用例等级字段。TODO:过滤用例等级字段，等级字段后续可自定义，需要调整
          return false;
        }
        return {
          type: item.type,
          name: item.fieldId,
          label: item.fieldName,
          value: initValue,
          required: item.required,
          options: optionsValue || [],
          props: {
            modelValue: initValue,
            options: optionsValue || [],
          },
        };
      });
      formRules.value = result.filter((e: any) => e);
      baseInfoLoading.value = false;
      emit('initTemplate', id);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  onBeforeMount(() => {
    initDefaultFields();
  });

  const saveLoading = ref(false);

  function makeParams() {
    const priority = formItem.value.find((item) => item.title === 'Case Priority' || item.title === '用例等级')?.field;
    const selectedNode: MinderJsonNode = window.minder.getSelectedNode();
    const customFields = formItem.value.map((item: any) => {
      return {
        fieldId: item.field,
        value: Array.isArray(item.value) ? JSON.stringify(item.value) : item.value,
      };
    });
    const selectedNodePriorityNumber = selectedNode?.data?.priority ? selectedNode?.data?.priority : 0;
    const realPriorityNumber = selectedNodePriorityNumber > 0 ? selectedNodePriorityNumber - 1 : 0;
    const priorityNumber = `P${realPriorityNumber}`;
    if (!priority) {
      customFields.push({ fieldId: priorityField.value, value: priorityNumber });
    }
    return {
      ...baseInfoForm.value,
      moduleId: props.activeCase.moduleId || 'root',
      id: props.activeCase.id,
      projectId: appStore.currentProjectId,
      caseEditType: props.activeCase.caseEditType || 'STEP',
      customFields,
    };
  }

  async function realSave() {
    try {
      saveLoading.value = true;
      const params = makeParams();
      if (props.activeCase.isNew !== false) {
        const res = await createCaseRequest({
          request: params,
          fileList: [],
        });
        const selectedNode: MinderJsonNode = window.minder.getSelectedNode();
        if (selectedNode?.data) {
          selectedNode.data.id = res.data.id;
        }
      } else {
        await updateCaseRequest({
          request: params,
          fileList: [],
        });
      }
      const selectedNode: MinderJsonNode = window.minder.getSelectedNode();
      const priority = (formItem.value.find((item) => item.title === 'Case Priority' || item.title === '用例等级')
        ?.value || 'P0') as string;

      const selectedNodePriorityNumber = selectedNode?.data?.priority ? selectedNode?.data?.priority : 0;
      const realPriorityNumber = selectedNodePriorityNumber > 0 ? selectedNodePriorityNumber - 1 : 0;
      const priorityNumber = Number(priority.match(/\d+/)?.[0]) || realPriorityNumber || 0;
      formItem.value.forEach((item) => {
        formRules.value.forEach((rule) => {
          if (item.field === rule.name) {
            rule.value = item.value;
          }
        });
      });
      if (selectedNode?.data) {
        selectedNode.data = {
          ...selectedNode.data,
          text: baseInfoForm.value.name,
          priority: priorityNumber,
          isNew: false,
        };
        window.minder.execCommand('priority', priorityNumber + 1);
        setPriorityView(true, 'P');
        selectedNode.data.changed = false;
      }
      Message.success(t('common.saveSuccess'));
      emit('saved');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      saveLoading.value = false;
    }
  }

  function handleSave() {
    baseInfoFormRef.value?.validate((errors) => {
      if (!errors) {
        if (formRules.value.length > 0) {
          fApi.value?.validate(async (valid) => {
            if (valid === true) {
              realSave();
            } else {
              // 滚动到报错的位置
              const firstErrorId = Object.keys(valid)[0];
              const fieldElement = document.getElementById(firstErrorId);
              fieldElement?.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
          });
        } else {
          realSave();
        }
      }
    });
  }
  function handleCancel() {
    emit('cancel');
  }

  watch(
    () => props.activeCase.id,
    () => {
      baseInfoForm.value.name = props.activeCase.name;
      baseInfoForm.value.tags = props.activeCase.tags || [];
      if (props.activeCase.customFields) {
        formRules.value = initFormCreate(
          (props.activeCase.customFields || []).filter((item: customFieldsItem) => {
            if (item.internal && item.type === 'SELECT') {
              // 这里不需要再展示用例等级字段。TODO:过滤用例等级字段，等级字段后续可自定义，需要调整
              return false;
            }
            return true;
          }),
          ['FUNCTIONAL_CASE:READ+UPDATE']
        );
      }
    },
    {
      immediate: true,
    }
  );

  watch(
    () => props.activeCase.name,
    () => {
      baseInfoForm.value.name = props.activeCase.name;
    }
  );

  defineExpose({
    makeParams,
  });
</script>

<style lang="less" scoped>
  .baseInfo-form {
    .ms-scroll-bar();

    overflow-y: auto;
    height: calc(100% - 64px);
  }
</style>
