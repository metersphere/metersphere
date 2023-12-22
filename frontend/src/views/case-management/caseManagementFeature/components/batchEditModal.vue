<template>
  <MsDialog
    v-model:visible="isVisible"
    dialog-size="small"
    :title="t('caseManagement.featureCase.batchEdit', { number: props.batchParams.currentSelectCount })"
    ok-text="common.confirm"
    :confirm="confirmHandler"
    :close="closeHandler"
    :switch-props="{
      switchName: t('caseManagement.featureCase.appendTag'),
      switchTooltip: t('caseManagement.featureCase.enableTags'),
      showSwitch: form.selectedAttrsId === 'systemTags' ? true : false,
      enable: form.append,
    }"
  >
    <div class="form">
      <a-form ref="formRef" :model="form" size="large" layout="vertical">
        <a-form-item
          field="selectedAttrsId"
          :label="t('caseManagement.featureCase.selectAttrs')"
          asterisk-position="end"
          :rules="[{ required: true, message: t('system.orgTemplate.stateNameNotNull') }]"
        >
          <a-select v-model="form.selectedAttrsId" :placeholder="t('caseManagement.featureCase.PleaseSelect')">
            <a-option v-for="item of totalAttrs" :key="item.fieldId" :value="item.fieldId">{{
              item.fieldName
            }}</a-option>
            <a-option key="systemTags" value="systemTags">{{ t('caseManagement.featureCase.tags') }}</a-option>
          </a-select>
        </a-form-item>
        <a-form-item
          v-if="form.selectedAttrsId === 'systemTags'"
          field="tags"
          :label="t('caseManagement.featureCase.batchUpdate')"
          asterisk-position="end"
          :rules="[{ required: true, message: t('caseManagement.featureCase.PleaseInputTags') }]"
        >
          <a-input-tag
            v-model="form.tags"
            :placeholder="t('caseManagement.featureCase.pleaseEnterInputTags')"
            allow-clear
          />
        </a-form-item>

        <MsFormCreate ref="formCreateRef" v-model:api="fApi" v-model:form-item="formItem" :form-rule="formRules" />
      </a-form>
    </div>
  </MsDialog>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { FormInstance } from '@arco-design/web-vue';

  import MsDialog from '@/components/pure/ms-dialog/index.vue';
  import MsFormCreate from '@/components/pure/ms-form-create/ms-form-create.vue';
  import type { FormItem, FormRuleItem } from '@/components/pure/ms-form-create/types';
  import type { BatchActionQueryParams } from '@/components/pure/ms-table/type';

  import { batchEditAttrs, getCaseDefaultFields } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { BatchEditCaseType, CustomAttributes } from '@/models/caseManagement/featureCase';

  import Message from '@arco-design/web-vue/es/message';

  const isVisible = ref<boolean>(false);
  const appStore = useAppStore();
  const { t } = useI18n();

  const props = defineProps<{
    visible: boolean;
    batchParams: BatchActionQueryParams;
  }>();

  const emits = defineEmits<{
    (e: 'update:visible', visible: boolean): void;
    (e: 'success'): void;
  }>();

  const currentProjectId = computed(() => appStore.currentProjectId);
  const initForm = {
    selectedAttrsId: '',
    append: false,
    tags: [],
  };
  const form = ref({ ...initForm });

  const formRef = ref<FormInstance | null>(null);

  const initDefaultForm: FormItem = {
    type: 'SELECT',
    name: 'name',
    label: 'caseManagement.featureCase.batchUpdate',
    value: '',
    options: [],
    props: {
      modelValue: '',
      options: [],
      disabled: true,
    },
    required: true,
  };

  /**
   * 初始化批量编辑属性
   */
  const totalAttrs = ref<CustomAttributes[]>([]);

  async function initDefaultFields() {
    try {
      const res = await getCaseDefaultFields(currentProjectId.value);
      totalAttrs.value = res.customFields;
    } catch (error) {
      console.log(error);
    }
  }

  const formRules = ref<FormItem[]>([{ ...initDefaultForm }]);

  const formItem = ref<FormRuleItem[]>([]);

  const fApi = ref<any>({});

  const updateType = computed(() => {
    return totalAttrs.value.find((item: any) => item.fieldId === form.value.selectedAttrsId)?.type;
  });
  watch(
    () => updateType.value,
    (val) => {
      const currentAttrs = totalAttrs.value.filter((item: any) => item.fieldId === form.value.selectedAttrsId);
      formRules.value = [];
      formRules.value = currentAttrs.map((item: CustomAttributes) => {
        return {
          type: val,
          name: item.fieldId,
          label: 'caseManagement.featureCase.batchUpdate',
          value: item.defaultValue,
          options: item.options,
          props: {
            modelValue: item.defaultValue,
            options: item.options,
            disabled: !form.value.selectedAttrsId,
          },
          required: item.required,
        };
      }) as FormItem[];
    }
  );

  function closeHandler() {
    isVisible.value = false;
    formRef.value?.resetFields();
    form.value = { ...initForm };
    formRules.value = [{ ...initDefaultForm }];
  }

  async function confirmHandler(enable: boolean | undefined) {
    await formRef.value?.validate().then(async (error) => {
      if (!error) {
        try {
          const customField = {
            fieldId: '',
            value: '',
          };
          formItem.value?.forEach((item: any) => {
            customField.fieldId = item.field;
            customField.value = JSON.stringify(item.value);
          });
          const params: BatchEditCaseType = {
            selectIds: props.batchParams.selectedIds as string[],
            projectId: currentProjectId.value,
            append: enable as boolean,
            tags: form.value.tags,
            customField,
          };
          await batchEditAttrs(params);
          Message.success(t('caseManagement.featureCase.editSuccess'));
          closeHandler();
          emits('success');
        } catch (e) {
          console.log(e);
        }
      } else {
        return false;
      }
    });
  }

  watch(
    () => isVisible.value,
    (val) => {
      emits('update:visible', val);
    }
  );

  watch(
    () => props.visible,
    (val) => {
      isVisible.value = val;
      if (val) {
        initDefaultFields();
      }
    }
  );

  onBeforeUnmount(() => {
    formRules.value = [];
  });
</script>

<style scoped></style>
