<template>
  <a-popconfirm
    v-model:popup-visible="isVisible"
    class="ms-pop-confirm--hidden-icon"
    position="bottom"
    :ok-loading="loading"
    :on-before-ok="beforeConfirm"
    :popup-container="props.popupContainer || 'body'"
    @popup-visible-change="visibleChange"
  >
    <template #content>
      <div class="mb-[8px] font-medium">
        {{ props.title || '' }}
      </div>
      <a-form ref="formRef" :model="form" layout="vertical">
        <a-form-item
          class="hidden-item"
          field="name"
          :rules="[{ required: true, message: t('featureTest.featureCase.nameNotNullTip') }]"
        >
          <a-input
            v-model:model-value="form.name"
            :max-length="50"
            :placeholder="props.placeholder || ''"
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
  import { ref, watch } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import type { FormInstance } from '@arco-design/web-vue';
  import { Message } from '@arco-design/web-vue';

  const { t } = useI18n();

  export type OperationType = 'add' | 'rename';

  const isVisible = ref<boolean>(false);

  const loading = ref<boolean>(false);

  const props = defineProps<{
    operationType: OperationType;
    title: string;
    nodeName?: string;
    visible?: boolean;
    popupContainer?: string;
    placeholder?: string;
  }>();

  const emits = defineEmits<{
    (e: 'update:visible', visible: boolean): void;
    (e: 'close'): void;
  }>();

  const form = ref({
    name: props.title || '',
  });

  const formRef = ref<FormInstance>();

  const visibleChange = () => {
    form.value.name = '';
    formRef.value?.resetFields();
  };

  const beforeConfirm = (done?: (closed: boolean) => void) => {
    formRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          loading.value = true;
          Message.success(
            props.operationType === 'add'
              ? t('featureTest.featureCase.addSubModuleSuccess')
              : t('featureTest.featureCase.renameSuccess')
          );
          if (done) {
            done(true);
          } else {
            isVisible.value = false;
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
  };

  watch(
    () => props.nodeName,
    (val) => {
      form.value.name = val || '';
    }
  );

  watch(
    () => props.visible,
    (val) => {
      isVisible.value = val;
    }
  );

  watch(
    () => isVisible.value,
    (val) => {
      if (!val) {
        emits('close');
      }
      emits('update:visible', val);
    }
  );
</script>

<style scoped></style>
