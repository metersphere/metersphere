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
        <div class="ml-[8px]">{{ t('bugManagement.batchEdit') }}</div>
        <div v-if="props.selectCount" class="ml-[8px] text-[var(--color-text-4)]">
          {{ t('bugManagement.selectedCount', { count: props.selectCount }) }}
        </div>
      </div>
    </template>
    <div class="form">
      <a-form ref="formRef" class="rounded-[4px]" :model="form" layout="vertical">
        <a-form-item
          field="name"
          asterisk-position="end"
          :label="t('bugManagement.deleteLabel')"
          :rules="[{ required: true }]"
        >
          <a-input v-model:model-value="form.label" />
        </a-form-item>
      </a-form>
    </div>
    <template #footer>
      <a-button type="secondary" :loading="loading" @click="handleCancel">
        {{ t('common.cancel') }}
      </a-button>
      <a-button type="primary" :loading="loading" :disabled="!form.label" @click="handleConfirm">
        {{ t('common.update') }}
      </a-button>
    </template>
  </a-modal>
</template>

<script lang="ts" setup>
  import { computed, reactive, ref } from 'vue';
  import { type FormInstance, Message, type ValidatedError } from '@arco-design/web-vue';

  import { FilterFormItem } from '@/components/pure/ms-advance-filter/type';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
    configList: FilterFormItem[];
    selectCount: number;
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
    label: '',
    value: '',
    append: false,
  });

  const formRef = ref<FormInstance>();

  const handleCancel = () => {
    currentVisible.value = false;
    form.label = '';
    loading.value = false;
  };

  const handleConfirm = () => {
    formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (!errors) {
        try {
          loading.value = true;
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
