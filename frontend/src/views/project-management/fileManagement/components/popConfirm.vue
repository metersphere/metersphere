<template>
  <a-popconfirm
    v-model:popup-visible="innerVisible"
    class="ms-pop-confirm--hidden-icon"
    position="bottom"
    :ok-loading="loading"
    :cancel-button-props="{ disabled: loading }"
    :on-before-ok="beforeConfirm"
    :popup-container="props.popupContainer || 'body'"
    @popup-visible-change="reset"
  >
    <template #content>
      <div class="mb-[8px] font-medium">
        {{
          props.title ||
          (props.mode === 'add' ? t('project.fileManagement.addSubModule') : t('project.fileManagement.rename'))
        }}
      </div>
      <a-form ref="formRef" :model="form" layout="vertical">
        <a-form-item
          class="hidden-item"
          field="field"
          :rules="[{ required: true, message: t('project.fileManagement.nameNotNull') }, { validator: validateName }]"
        >
          <a-textarea
            v-if="props.fieldConfig?.isTextArea"
            v-model:model-value="form.field"
            :max-length="props.fieldConfig?.maxLength"
            :auto-size="{ maxRows: 4 }"
            :placeholder="props.fieldConfig?.placeholder || t('project.fileManagement.namePlaceholder')"
            class="w-[245px]"
            @press-enter="beforeConfirm(undefined)"
          >
          </a-textarea>
          <a-input
            v-else
            v-model:model-value="form.field"
            :max-length="props.fieldConfig?.maxLength"
            :placeholder="props.fieldConfig?.placeholder || t('project.fileManagement.namePlaceholder')"
            class="w-[245px]"
            @press-enter="beforeConfirm(undefined)"
          />
        </a-form-item>
      </a-form>
    </template>
    <slot></slot>
  </a-popconfirm>
</template>

<script setup lang="ts">
  import { onBeforeMount, ref, watch } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import { Message } from '@arco-design/web-vue';

  import type { FormInstance, FieldRule } from '@arco-design/web-vue';

  interface FieldConfig {
    field?: string;
    rules?: FieldRule[];
    placeholder?: string;
    maxLength?: number;
    isTextArea?: boolean;
  }

  const props = defineProps<{
    mode: 'add' | 'rename';
    visible?: boolean;
    title?: string;
    allNames: string[];
    popupContainer?: string;
    fieldConfig?: FieldConfig;
  }>();

  const emit = defineEmits(['update:visible', 'close']);

  const { t } = useI18n();

  const innerVisible = ref(props.visible || false);
  const form = ref({
    field: props.fieldConfig?.field || '',
  });
  const formRef = ref<FormInstance>();
  const loading = ref(false);

  watch(
    () => props.fieldConfig?.field,
    (val) => {
      form.value.field = val || '';
    }
  );

  watch(
    () => props.visible,
    (val) => {
      innerVisible.value = val;
    }
  );

  watch(
    () => innerVisible.value,
    (val) => {
      if (!val) {
        emit('close');
      }
      emit('update:visible', val);
    }
  );

  function beforeConfirm(done?: (closed: boolean) => void) {
    formRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          loading.value = true;
          if (props.mode === 'add') {
            Message.success(t('project.fileManagement.addSubModuleSuccess'));
          } else {
            Message.success(t('project.fileManagement.renameSuccess'));
          }
          if (done) {
            done(true);
          } else {
            innerVisible.value = false;
          }
        } catch (error) {
          console.log(error);
        } finally {
          loading.value = false;
        }
      } else if (done) {
        done(false);
      }
    });
  }

  function validateName(value: any, callback: (error?: string | undefined) => void) {
    if (props.allNames.includes(value)) {
      callback(t('project.fileManagement.nameExist'));
    }
  }

  function reset() {
    form.value.field = '';
    formRef.value?.resetFields();
  }
</script>

<style lang="less" scoped></style>
