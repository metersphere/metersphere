<template>
  <a-form ref="formRef" :model="form" layout="vertical">
    <a-form-item
      class="hidden-item"
      hide-asterisk
      field="name"
      :validate-trigger="['change', 'input']"
      :rules="[{ required: true, message: t('advanceFilter.viewNameRequired') }, { validator: validateName }]"
    >
      <a-input
        ref="inputRef"
        v-model:model-value="form.name"
        :max-length="255"
        :placeholder="t('advanceFilter.viewNamePlaceholder')"
        :show-word-limit="!props.notShowWordLimit"
        @press-enter="handleSubmit"
        @blur="handleSubmit"
      />
    </a-form-item>
  </a-form>
</template>

<script setup lang="ts">
  import { FormInstance, InputInstance } from '@arco-design/web-vue';

  import { useI18n } from '@/hooks/useI18n';

  const props = defineProps<{
    allNames: string[];
    notShowWordLimit?: boolean;
  }>();
  const form = defineModel<Record<string, any>>('form', {
    required: true,
  });

  const emit = defineEmits<{
    (e: 'handleSubmit'): void;
  }>();

  const { t } = useI18n();

  const formRef = ref<FormInstance>();

  // 校验名称是否重复
  const validateName = (value: any, callback: (error?: string | undefined) => void) => {
    if (props.allNames.includes(value)) {
      callback(t('advanceFilter.viewNameNotRepeat'));
    }
  };

  const inputRef = ref<InputInstance>();
  function inputFocus() {
    inputRef.value?.focus();
  }

  function handleSubmit() {
    formRef.value?.validate(async (errors) => {
      if (!errors) {
        emit('handleSubmit');
      }
    });
  }

  function validateForm(cb: (param?: any) => void, params?: any) {
    formRef.value?.validate(async (errors) => {
      if (!errors) {
        cb(params);
      }
    });
  }

  defineExpose({
    inputFocus,
    validateForm,
  });
</script>
