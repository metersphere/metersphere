<template>
  <div class="h-full pl-[16px]">
    <div class="baseInfo-form">
      <a-skeleton v-if="baseInfoLoading || props.loading" :loading="baseInfoLoading || props.loading" :animation="true">
        <a-space direction="vertical" class="w-full" size="large">
          <a-skeleton-line :rows="10" :line-height="30" :line-spacing="30" />
        </a-space>
      </a-skeleton>
      <a-form v-else ref="baseInfoFormRef" :model="baseInfoForm" :disabled="!hasEditPermission" layout="vertical">
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
          ref="formCreateRef"
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
    <div v-if="hasEditPermission" class="flex items-center gap-[12px] bg-white py-[16px]">
      <a-button
        v-permission="['FUNCTIONAL_CASE:READ+UPDATE']"
        type="primary"
        :loading="saveLoading"
        @click="handleSave"
      >
        {{ t('common.save') }}
      </a-button>
      <a-button type="secondary" :disabled="saveLoading" @click="handleCancel">{{ t('common.cancel') }}</a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { FormInstance, Message } from '@arco-design/web-vue';

  import MsFormCreate from '@/components/pure/ms-form-create/ms-form-create.vue';
  import { FormItem, FormRuleItem } from '@/components/pure/ms-form-create/types';
  import { MinderJsonNode } from '@/components/pure/ms-minder-editor/props';
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

  import { OptionsFieldId } from '@/models/caseManagement/featureCase';

  import { initFormCreate } from '@/views/case-management/caseManagementFeature/components/utils';
  import { Api } from '@form-create/arco-design';

  const props = defineProps<{
    activeCase: Record<string, any>;
    loading: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'initTemplate', id: string): void;
    (e: 'cancel'): void;
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
    moduleId: 'root',
  });
  const baseInfoLoading = ref(false);

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
        const optionsValue: OptionsFieldId[] = item.options;
        if (memberType.includes(item.type)) {
          if (item.defaultValue === 'CREATE_USER' || item.defaultValue.includes('CREATE_USER')) {
            initValue = item.type === 'MEMBER' ? userStore.id : [userStore.id];
          }
        }
        if (item.internal && item.type === 'SELECT') {
          // TODO:过滤用例等级字段，等级字段后续可自定义，需要调整
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
    return {
      ...baseInfoForm.value,
      id: props.activeCase.id,
      projectId: appStore.currentProjectId,
      caseEditType: props.activeCase.caseEditType || 'STEP',
      customFields: formItem.value.map((item: any) => {
        return {
          fieldId: item.field,
          value: Array.isArray(item.value) ? JSON.stringify(item.value) : item.value,
        };
      }),
    };
  }

  function handleSave() {
    baseInfoFormRef.value?.validate((errors) => {
      if (!errors) {
        fApi.value?.validate(async (valid) => {
          if (valid === true) {
            try {
              saveLoading.value = true;
              if (props.activeCase.isNew !== false) {
                const res = await createCaseRequest({
                  request: makeParams(),
                  fileList: [],
                });
                const selectedNode: MinderJsonNode = window.minder.getSelectedNode();
                if (selectedNode?.data) {
                  selectedNode.data.id = res.id;
                }
              } else {
                await updateCaseRequest({
                  request: makeParams(),
                  fileList: [],
                });
              }
              const selectedNode: MinderJsonNode = window.minder.getSelectedNode();
              if (selectedNode?.data) {
                selectedNode.data.text = baseInfoForm.value.name;
              }
              Message.success(t('common.saveSuccess'));
            } catch (error) {
              // eslint-disable-next-line no-console
              console.log(error);
            } finally {
              saveLoading.value = false;
            }
          }
        });
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
      formRules.value = initFormCreate(props.activeCase.customFields || [], ['FUNCTIONAL_CASE:READ+UPDATE']);
    },
    {
      immediate: true,
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
