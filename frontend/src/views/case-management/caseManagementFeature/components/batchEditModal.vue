<template>
  <MsDialog
    v-model:visible="isVisible"
    dialog-size="small"
    :title="t('featureTest.featureCase.batchEdit', { number: props.batchParams.currentSelectCount })"
    ok-text="common.confirm"
    :confirm="confirmHandler"
    :close="closeHandler"
    :switch-props="{
      switchName: t('featureTest.featureCase.appendTag'),
      switchTooltip: t('featureTest.featureCase.enableTags'),
      showSwitch: form.selectedAttrsId === 'systemTags' ? true : false,
      enable: form.append,
    }"
  >
    <div class="form">
      <a-form ref="formRef" :model="form" size="large" layout="vertical">
        <a-form-item
          field="selectedAttrsId"
          :label="t('featureTest.featureCase.selectAttrs')"
          asterisk-position="end"
          :rules="[{ required: true, message: t('system.orgTemplate.stateNameNotNull') }]"
        >
          <a-select v-model="form.selectedAttrsId" :placeholder="t('featureTest.featureCase.PleaseSelect')">
            <a-option v-for="item of totalAttrs" :key="item.fieldId" :value="item.fieldId">{{
              item.fieldName
            }}</a-option>
            <a-option key="systemTags" value="systemTags">{{ t('featureTest.featureCase.tags') }}</a-option>
          </a-select>
        </a-form-item>
        <a-form-item
          v-if="form.selectedAttrsId === 'systemTags'"
          field="tags"
          :label="t('featureTest.featureCase.batchUpdate')"
          asterisk-position="end"
          :rules="[{ required: true, message: t('featureTest.featureCase.PleaseInputTags') }]"
        >
          <a-input-tag
            v-model="form.tags"
            :placeholder="t('featureTest.featureCase.pleaseEnterInputTags')"
            allow-clear
          />
        </a-form-item>

        <MsFormCreate
          v-if="formRules.length && form.selectedAttrsId !== 'systemTags'"
          ref="formCreateRef"
          v-model:api="fApi"
          :form-rule="formRules"
          :form-create-key="FormCreateKeyEnum.CASE_CUSTOM_ATTRS"
        />
      </a-form>
    </div>
  </MsDialog>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { FormInstance } from '@arco-design/web-vue';

  import MsDialog from '@/components/pure/ms-dialog/index.vue';
  import MsFormCreate from '@/components/pure/ms-form-create/form-create.vue';
  import type { FormItem } from '@/components/pure/ms-form-create/types';
  import type { BatchActionQueryParams } from '@/components/pure/ms-table/type';

  import { batchEditAttrs, getCaseDefaultFields } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import useFormCreateStore from '@/store/modules/form-create/form-create';

  import type { BatchEditCaseType, CustomAttributes } from '@/models/caseManagement/featureCase';
  import { FormCreateKeyEnum } from '@/enums/formCreateEnum';

  import Message from '@arco-design/web-vue/es/message';

  const isVisible = ref<boolean>(false);
  const appStore = useAppStore();
  const formCreateStore = useFormCreateStore();
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
    label: 'featureTest.featureCase.batchUpdate',
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
  const fApi = ref<any>({});
  const formCreateValue = computed(() => formCreateStore.formCreateRuleMap.get(FormCreateKeyEnum.CASE_CUSTOM_ATTRS));

  const updateType = computed(() => {
    return totalAttrs.value.find((item: any) => item.fieldId === form.value.selectedAttrsId)?.type;
  });

  watch(
    () => updateType.value,
    (val) => {
      const currentAttrs = totalAttrs.value.filter((item: any) => item.fieldId === form.value.selectedAttrsId);
      if (val) {
        formRules.value = currentAttrs.map((item: CustomAttributes) => {
          return {
            type: val,
            name: item.fieldId,
            label: 'featureTest.featureCase.batchUpdate',
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
          formCreateValue.value?.forEach((item: any) => {
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
          Message.success(t('featureTest.featureCase.editSuccess'));
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
