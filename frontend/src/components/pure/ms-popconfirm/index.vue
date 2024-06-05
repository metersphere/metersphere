<template>
  <a-popover
    v-bind="attrs"
    v-model:popup-visible="currentVisible"
    :type="props.type"
    :class="props.isDelete ? 'w-[352px]' : ''"
    trigger="click"
    :popup-container="props.popupContainer || 'body'"
    :position="props.isDelete ? 'rb' : 'bottom'"
    @popup-visible-change="reset"
  >
    <template #content>
      <div class="flex flex-row flex-nowrap items-center">
        <slot name="icon">
          <MsIcon
            v-if="props.isDelete"
            type="icon-icon_warning_filled"
            class="mr-[2px] text-xl text-[rgb(var(--danger-6))]"
          />
        </slot>
        <div :class="[titleClass]">
          {{ props.title || '' }}
        </div>
      </div>
      <!-- 描述展示 -->
      <div v-if="props.subTitleTip" class="ml-8 mt-2 text-sm text-[var(--color-text-2)]">
        {{ props.subTitleTip }}
      </div>
      <!-- 表单展示 -->
      <a-form v-else ref="formRef" :model="form" layout="vertical">
        <a-form-item
          class="hidden-item"
          field="field"
          :rules="
            props.fieldConfig?.rules || [
              { required: true, message: t('popConfirm.nameNotNull') },
              { validator: validateName },
            ]
          "
        >
          <a-textarea
            v-if="props.fieldConfig?.isTextArea"
            v-model:model-value="form.field"
            :max-length="props.fieldConfig?.maxLength || 1000"
            :auto-size="{ maxRows: 4 }"
            :placeholder="props.fieldConfig?.placeholder"
            class="w-[245px]"
            :rules="props.fieldConfig?.rules || []"
            @press-enter="handleConfirm"
          >
          </a-textarea>
          <a-input
            v-else
            v-model:model-value="form.field"
            :max-length="255"
            :placeholder="props.fieldConfig?.placeholder"
            class="w-[245px]"
            @press-enter="handleConfirm"
          />
        </a-form-item>
      </a-form>
      <div class="mb-1 mt-4 flex flex-row flex-nowrap justify-end gap-2">
        <a-button type="secondary" size="mini" :disabled="props.loading" @click="handleCancel">
          {{ props.cancelText || t('common.cancel') }}
        </a-button>
        <a-button type="primary" size="mini" :loading="props.loading" @click="handleConfirm">
          {{ t(props.okText) || t('common.confirm') }}
        </a-button>
      </div>
    </template>

    <slot></slot>
  </a-popover>
</template>

<script setup lang="ts">
  import { computed, ref, useAttrs, watch } from 'vue';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import type { FieldRule, FormInstance } from '@arco-design/web-vue';

  export type types = 'error' | 'info' | 'success' | 'warning';

  const { t } = useI18n();

  interface FieldConfig {
    field?: string;
    rules?: FieldRule[];
    placeholder?: string;
    maxLength?: number;
    isTextArea?: boolean;
    nameExistTipText?: string; // 添加重复提示文本
  }

  export interface ConfirmValue {
    field: string;
    id?: string;
  }

  const props = withDefaults(
    defineProps<{
      title: string; // 文本提示标题
      subTitleTip?: string; // 子内容提示
      type?: types;
      isDelete?: boolean; // 当前使用是否是移除
      loading?: boolean;
      okText?: string; // 确定按钮文本
      cancelText?: string;
      visible?: boolean; // 是否打开
      popupContainer?: string;
      fieldConfig?: FieldConfig; // 表单配置项
      allNames?: string[]; // 添加或者重命名名称重复
      nodeId?: string; // 节点 id
    }>(),
    {
      type: 'warning',
      isDelete: true, // 默认移除pop
      okText: 'common.remove',
    }
  );
  const emits = defineEmits<{
    (e: 'confirm', formValue: ConfirmValue, cancel?: () => void): void;
    (e: 'cancel'): void;
    (e: 'update:visible', visible: boolean): void;
  }>();

  const currentVisible = ref(props.visible || false);

  const attrs = useAttrs();
  const formRef = ref<FormInstance>();

  // 表单
  const form = ref({
    field: props.fieldConfig?.field || '',
  });

  // 重置
  const reset = () => {
    form.value.field = '';
    formRef.value?.resetFields();
  };

  const handleCancel = () => {
    currentVisible.value = false;
    emits('cancel');
    reset();
  };

  const emitConfirm = () => emits('confirm', { ...form.value, id: props.nodeId }, handleCancel);
  const handleConfirm = () => {
    if (!formRef.value) {
      emitConfirm();
      return;
    }
    formRef.value?.validate((errors) => {
      if (!errors) {
        emitConfirm();
      }
    });
  };
  // 获取当前标题的样式
  const titleClass = computed(() => {
    return props.isDelete
      ? 'ml-2 font-medium text-[var(--color-text-1)] text-[14px]'
      : 'mb-[8px] font-medium text-[var(--color-text-1)] text-[14px] leading-[22px]';
  });

  watch(
    () => props.fieldConfig?.field,
    (val) => {
      form.value.field = val || '';
    }
  );

  watch(
    () => props.visible,
    (val) => {
      currentVisible.value = val;
    }
  );

  watch(
    () => currentVisible.value,
    (val) => {
      if (!val) {
        emits('cancel');
      }
      emits('update:visible', val);
    }
  );

  // 校验名称是否重复
  const validateName = (value: any, callback: (error?: string | undefined) => void) => {
    if ((props.allNames || []).includes(value)) {
      if (props.fieldConfig && props.fieldConfig.nameExistTipText) {
        callback(t(props.fieldConfig.nameExistTipText));
      } else {
        callback(t('popConfirm.nameExist'));
      }
    }
  };
</script>

<style scoped lang="less">
  :deep(.arco-trigger-content) {
    padding: 16px;
  }
</style>
