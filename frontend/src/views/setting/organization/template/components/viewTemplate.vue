<template>
  <div class="wrapper-preview">
    <div class="preview-left pr-4">
      <DefectTemplateLeftContent v-if="props.templateType === 'BUG'" :defect-form="props.defectForm" />
      <CaseTemplateLeftContent v-else />
    </div>
    <div class="preview-right px-4">
      <!-- 系统内置的字段 {处理人, 状态...} -->
      <DefectTemplateRightSystemField v-if="props.templateType === 'BUG'" />
      <CaseTemplateRightSystemField v-else />

      <!-- 自定义字段开始 -->
      <MsFormCreate
        v-if="formRules.length"
        ref="formCreateRef"
        v-model:api="fApi"
        v-model:form-item="formItem"
        :form-rule="formRules"
      />

      <!-- 标签字段开始 -->
      <div class="tagWrapper">
        <a-form ref="viewFormRef" layout="vertical" :model="viewForm">
          <a-form-item field="tags" :label="t('system.orgTemplate.tags')" asterisk-position="end">
            <a-input :disabled="true" :placeholder="t('system.orgTemplate.noDefaultPlaceholder')" />
          </a-form-item>
        </a-form>
      </div>
      <!-- 标签字段结束 -->
    </div>
  </div>
</template>

<script setup lang="ts">
  /**
   * @description 模板-创建模板&编辑模板-预览模板
   */
  import { ref } from 'vue';

  import MsFormCreate from '@/components/pure/ms-form-create/ms-form-create.vue';
  import type { FormItem, FormRuleItem } from '@/components/pure/ms-form-create/types';
  import CaseTemplateLeftContent from './caseTemplateLeftContent.vue';
  import DefectTemplateLeftContent from './defectTemplateLeftContent.vue';
  import CaseTemplateRightSystemField from '@/views/setting/organization/template/components/caseTemplateRightSystemField.vue';
  import DefectTemplateRightSystemField from '@/views/setting/organization/template/components/defectTemplateRightSystemField.vue';

  import { useI18n } from '@/hooks/useI18n';

  import type { DefinedFieldItem, SeneType } from '@/models/setting/template';

  const { t } = useI18n();

  const props = defineProps<{
    templateType: SeneType; // 模板场景
    selectField: DefinedFieldItem[]; // 选择模板字段
    defectForm: Record<string, any>; // 缺陷详情
  }>();

  const formRuleField = ref<FormItem[][]>([]);
  const formRules = ref<FormItem[]>([]);
  const formItem = ref<FormRuleItem[]>([]);
  const fApi = ref(null);
  const formCreateRef = ref();

  const viewForm = ref<Record<string, any>>({
    tags: '',
  });

  // 处理表单格式
  const getFormRules = () => {
    formRuleField.value = [];
    formRules.value = [];
    if (props.selectField && props.selectField.length) {
      props.selectField.forEach((item: DefinedFieldItem) => {
        const currentFormItem = item.formRules?.map((rule: any) => {
          let optionsItem = [];
          if (rule.options && rule.options.length) {
            optionsItem = rule.options.map((opt: any) => {
              return {
                text: opt.label,
                value: opt.value,
              };
            });
          }
          return {
            type: item.type,
            name: rule.field,
            label: item.name,
            value: rule.value,
            options: optionsItem,
            required: item.required,
            props: {
              modelValue: rule.value,
              options: optionsItem,
              disabled: true,
            },
          };
        });
        formRuleField.value.push(currentFormItem as FormItem[]);
      });
      formRules.value = formRuleField.value.flatMap((item) => item);
    }
  };

  watchEffect(() => {
    getFormRules();
  });

  onBeforeUnmount(() => {
    formRules.value = [];
    formRuleField.value = [];
  });

  defineExpose({
    getFormRules,
  });
</script>

<style scoped lang="less">
  .wrapper-preview {
    display: flex;
    .preview-left {
      width: 100%;
      border-right: 1px solid var(--color-text-n8);
    }
    .preview-right {
      width: 428px;
    }
  }
  :deep(.arco-picker-disabled) {
    border-color: var(--color-text-n8);
    background: var(--color-text-n8);
    &:hover {
      border-color: var(--color-text-n8);
      background: var(--color-text-n8);
    }
  }
</style>
