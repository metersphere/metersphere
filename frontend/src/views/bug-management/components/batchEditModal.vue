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
        <div v-if="selectCount" class="ml-[8px] text-[var(--color-text-4)]">
          {{ t('bugManagement.selectedCount', { count: selectCount }) }}
        </div>
      </div>
    </template>
    <div class="form">
      <a-form ref="formRef" class="rounded-[4px]" :model="form" layout="vertical">
        <a-form-item
          field="attribute"
          asterisk-position="end"
          :label="t('bugManagement.batchUpdate.attribute')"
          :rules="[{ required: true }]"
        >
          <a-select v-model:model-value="form.attribute" :options="[]" />
        </a-form-item>
        <a-form-item
          field="value"
          asterisk-position="end"
          :label="t('bugManagement.batchUpdate.update')"
          :rules="[{ required: true }]"
        >
          <a-select v-model:model-value="form.value" :disabled="!form.attribute" :options="[]" />
        </a-form-item>
      </a-form>
    </div>
    <template #footer>
      <a-button type="secondary" :loading="loading" @click="handleCancel">
        {{ t('common.cancel') }}
      </a-button>
      <a-button type="primary" :loading="loading" @click="handleConfirm">
        {{ t('common.update') }}
      </a-button>
    </template>
  </a-modal>
</template>

<script lang="ts" setup>
  import { computed, reactive, ref } from 'vue';
  import { type FormInstance, Message, type ValidatedError } from '@arco-design/web-vue';

  import { BatchActionQueryParams } from '@/components/pure/ms-table/type';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
    selectParam: BatchActionQueryParams;
  }>();
  const emit = defineEmits<{
    (e: 'submit'): void;
    (e: 'update:visible', value: boolean): void;
  }>();
  const selectCount = computed(() => props.selectParam.currentSelectCount);
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
    attribute: '',
    value: [],
    append: false,
  });

  const formRef = ref<FormInstance>();

  const handleCancel = () => {
    currentVisible.value = false;
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
