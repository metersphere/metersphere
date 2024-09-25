<template>
  <a-modal
    v-model:visible="currentVisible"
    title-align="start"
    class="ms-modal-form ms-modal-small"
    unmount-on-close
    @cancel="handleCancel"
  >
    <template #title>
      <div class="flex flex-row items-center">
        <icon-exclamation-circle-fill class="text-[16px] text-[rgb(var(--danger-6))]" />
        <div class="one-line-text ml-[8px]">{{
          t('bugManagement.detail.deleteTitle', { name: characterLimit(props.name) })
        }}</div>
      </div>
    </template>
    <div class="form">
      <a-form ref="formRef" class="rounded-[4px]" :model="form" layout="vertical">
        <a-form-item
          field="name"
          asterisk-position="end"
          :label="t('bugManagement.deleteLabel')"
          :rules="[
            {
              validator(value, callback) {
                if (value !== props.name) {
                  callback(t('bugManagement.nameIsIncorrect'));
                } else {
                  callback();
                }
              },
            },
          ]"
        >
          <a-input v-model:model-value="form.name" :placeholder="t('bugManagement.edit.pleaseInputBugName')" />
        </a-form-item>
      </a-form>
    </div>
    <template #footer>
      <a-button type="secondary" :loading="loading" @click="handleCancel">
        {{ t('common.cancel') }}
      </a-button>
      <a-button type="primary" status="danger" :loading="loading" :disabled="!form.name" @click="handleConfirm">
        {{ t('common.confirmDelete') }}
      </a-button>
    </template>
  </a-modal>
</template>

<script lang="ts" setup>
  import { computed, reactive, ref } from 'vue';
  import { type FormInstance, Message, type ValidatedError } from '@arco-design/web-vue';

  import { useI18n } from '@/hooks/useI18n';
  import { characterLimit } from '@/utils';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
    name?: string;
    id: string;
    remoteFunc(params: Record<string, any>): Promise<any>; // 远程模式下的请求函数，返回一个 Promise
  }>();

  const emit = defineEmits<{
    (e: 'submit'): void;
    (e: 'update:visible', value: boolean): void;
  }>();

  const currentVisible = computed({
    get() {
      return props.visible;
    },
    set(value) {
      emit('update:visible', value);
    },
  });
  const loading = ref(false);

  const form = reactive({
    name: '',
  });

  const formRef = ref<FormInstance>();

  const handleCancel = () => {
    currentVisible.value = false;
    form.name = '';
    loading.value = false;
  };

  const handleConfirm = () => {
    formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (!errors) {
        try {
          loading.value = true;
          await props.remoteFunc({ id: props.id });
          Message.success(t('common.deleteSuccess'));
          handleCancel();
          emit('submit');
        } catch (error) {
          // eslint-disable-next-line no-console
          console.error(error);
        } finally {
          loading.value = false;
        }
      }
    });
  };
</script>

<style lang="less" scoped>
  :deep(.arco-form-item-label) {
    color: var(--color-text-2) !important;
  }
</style>
