<template>
  <MsDialog
    v-model:visible="isVisible"
    dialog-size="small"
    :title="t('caseManagement.featureCase.batchEdit', { number: props.batchParams.currentSelectCount })"
    ok-text="common.update"
    :confirm="confirmHandler"
    :close="closeHandler"
    unmount-on-close
  >
    <div class="form">
      <a-form ref="formRef" :model="form" layout="vertical">
        <a-form-item
          field="selectedAttrsId"
          :label="t('caseManagement.featureCase.selectAttrs')"
          asterisk-position="end"
          :rules="[{ required: true, message: t('caseManagement.featureCase.selectAttrsNotNull') }]"
        >
          <a-select v-model="form.selectedAttrsId" :placeholder="t('caseManagement.featureCase.PleaseSelect')">
            <a-option v-for="item of totalAttrs" :key="item.fieldId" :value="item.fieldId">
              {{ item.fieldName }}
            </a-option>
            <a-option key="systemTags" value="systemTags">{{ t('caseManagement.featureCase.tags') }}</a-option>
          </a-select>
        </a-form-item>
        <a-form-item
          v-if="form.selectedAttrsId === 'systemTags'"
          class="mb-[16px]"
          field="type"
          :label="t('common.type')"
        >
          <a-radio-group v-model:model-value="selectedTagType" size="small">
            <a-radio :value="TagUpdateTypeEnum.UPDATE"> {{ t('common.update') }}</a-radio>
            <a-radio :value="TagUpdateTypeEnum.APPEND"> {{ t('caseManagement.featureCase.appendTag') }}</a-radio>
            <a-radio :value="TagUpdateTypeEnum.CLEAR">{{ t('common.clear') }}</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item
          v-if="form.selectedAttrsId === 'systemTags' && selectedTagType !== TagUpdateTypeEnum.CLEAR"
          field="tags"
          :validate-trigger="['blur', 'input']"
          :label="t('common.batchUpdate')"
          asterisk-position="end"
          :rules="[{ required: true, message: t('common.inputPleaseEnterTags') }]"
        >
          <MsTagsInput v-model:model-value="form.tags" allow-clear empty-priority-highest></MsTagsInput>
          <div class="text-[12px] leading-[20px] text-[var(--color-text-4)]">{{ t('ms.tagsInput.tagLimitTip') }}</div>
        </a-form-item>

        <MsFormCreate
          v-if="form.selectedAttrsId !== 'systemTags' && selectedTagType !== TagUpdateTypeEnum.CLEAR"
          ref="formCreateRef"
          v-model:api="fApi"
          v-model:form-item="formItem"
          :form-rule="formRules"
        />
      </a-form>
    </div>
  </MsDialog>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { FormInstance } from '@arco-design/web-vue';

  import MsDialog from '@/components/pure/ms-dialog/index.vue';
  import { FieldTypeFormRules } from '@/components/pure/ms-form-create/form-create';
  import MsFormCreate from '@/components/pure/ms-form-create/ms-form-create.vue';
  import type { FormItem, FormRuleItem } from '@/components/pure/ms-form-create/types';
  import type { BatchActionQueryParams } from '@/components/pure/ms-table/type';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';

  import { batchEditAttrs, getCaseDefaultFields } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { CustomAttributes } from '@/models/caseManagement/featureCase';
  import { TableQueryParams } from '@/models/common';
  import { TagUpdateTypeEnum } from '@/enums/commonEnum';

  import Message from '@arco-design/web-vue/es/message';

  const isVisible = ref<boolean>(false);
  const appStore = useAppStore();
  const { t } = useI18n();

  const props = defineProps<{
    visible: boolean;
    batchParams: BatchActionQueryParams;
    activeFolder: string;
    offspringIds: string[];
    condition?: TableQueryParams;
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
  const selectedTagType = ref<TagUpdateTypeEnum>(TagUpdateTypeEnum.UPDATE);

  const initDefaultForm: FormItem = {
    type: 'SELECT',
    name: 'name',
    label: 'common.batchUpdate',
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
        let formValue: string | string[] = item.defaultValue ?? FieldTypeFormRules[item.type].value;
        // 如果包含成员将默认成员清空重新设置
        const memberType = ['MEMBER', 'MULTIPLE_MEMBER'];
        if (val && formValue.includes('CREATE_USER') && memberType.includes(val)) {
          formValue = val === 'MEMBER' ? '' : [];
        }
        return {
          type: val,
          name: item.fieldId,
          label: 'common.batchUpdate',
          value: formValue,
          options: item.options,
          props: {
            modelValue: formValue,
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
    form.value.tags = [];
    selectedTagType.value = TagUpdateTypeEnum.UPDATE;
  }

  async function confirmHandler() {
    await formRef.value?.validate().then(async (error) => {
      if (!error) {
        try {
          const customField = {
            fieldId: '',
            value: '',
          };
          formItem.value?.forEach((item: any) => {
            customField.fieldId = item.field;
            customField.value = Array.isArray(item.value) ? JSON.stringify(item.value) : item.value;
          });
          const { selectedIds, selectAll, excludeIds } = props.batchParams;
          const params: TableQueryParams = {
            selectIds: selectedIds || [],
            selectAll: !!selectAll,
            excludeIds: excludeIds || [],
            projectId: currentProjectId.value,
            append: selectedTagType.value === TagUpdateTypeEnum.APPEND,
            tags: form.value.tags,
            moduleIds: props.activeFolder === 'all' ? [] : [props.activeFolder, ...props.offspringIds],
            customField: form.value.selectedAttrsId === 'systemTags' ? {} : customField,
            condition: {
              ...props.condition,
            },
            clear: selectedTagType.value === TagUpdateTypeEnum.CLEAR,
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
