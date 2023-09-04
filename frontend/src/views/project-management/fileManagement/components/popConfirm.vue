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
        {{ props.mode === 'add' ? t('project.fileManagement.addSubModule') : t('project.fileManagement.rename') }}
      </div>
      <a-form ref="formRef" :model="form" layout="vertical">
        <a-form-item
          class="hidden-item"
          field="name"
          :rules="[{ required: true, message: t('project.fileManagement.nameNotNull') }, { validator: validateName }]"
        >
          <a-input
            v-model:model-value="form.name"
            :max-length="50"
            :placeholder="props.placeholder || t('project.fileManagement.namePlaceholder')"
            class="w-[245px]"
            @press-enter="beforeConfirm(undefined)"
          ></a-input>
        </a-form-item>
      </a-form>
    </template>
    <slot></slot>
  </a-popconfirm>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import { Message } from '@arco-design/web-vue';

  import type { FormInstance } from '@arco-design/web-vue';

  const props = defineProps<{
    mode: 'add' | 'rename';
    visible?: boolean;
    title?: string;
    allNames: string[];
    popupContainer?: string;
    placeholder?: string;
  }>();

  const emit = defineEmits(['update:visible', 'close']);

  const { t } = useI18n();

  const innerVisible = ref(props.visible || false);
  const form = ref({
    name: props.title || '',
  });
  const formRef = ref<FormInstance>();
  const loading = ref(false);

  watch(
    () => props.title,
    (val) => {
      form.value.name = val || '';
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
    form.value.name = '';
    formRef.value?.resetFields();
  }
</script>

<style lang="less" scoped></style>
