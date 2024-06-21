<template>
  <a-modal
    v-model:visible="visible"
    title-align="start"
    body-class="p-0"
    :cancel-button-props="{ disabled: batchLoading }"
    @close="handleCancel"
  >
    <template #title>
      {{ t('common.batchMove') }}
      <div class="ml-1 text-[var(--color-text-4)]">
        {{
          t('common.selectedCount', {
            count: props.count,
          })
        }}
      </div>
    </template>
    <a-form ref="formRef" :model="form" layout="vertical">
      <a-form-item
        :rules="[{ required: true, message: t('testPlan.api.testSetRequired') }]"
        field="targetCollectionId"
        :label="t('ms.minders.testSet')"
        asterisk-position="end"
      >
        <MsSelect
          v-model:modelValue="form.targetCollectionId"
          mode="static"
          :placeholder="t('common.pleaseSelect')"
          :options="options"
          :search-keys="['label']"
          allow-search
        />
      </a-form-item>
    </a-form>
    <template #footer>
      <a-button type="secondary" @click="handleCancel">{{ t('common.cancel') }}</a-button>
      <a-button class="ml-[12px]" type="primary" :loading="batchLoading" @click="handleMove">
        {{ t('common.move') }}
      </a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { FormInstance, Message } from '@arco-design/web-vue';

  import MsSelect from '@/components/business/ms-select';

  import { useI18n } from '@/hooks/useI18n';

  import { ModuleTreeNode } from '@/models/common';
  import type { BatchMoveApiCaseParams } from '@/models/testPlan/testPlan';

  const props = defineProps<{
    count: number;
    params?: BatchMoveApiCaseParams;
    batchMove: (...args: any) => Promise<any>; // 移动接口
    moduleTree: ModuleTreeNode[];
  }>();

  const emit = defineEmits<{
    (e: 'loadList'): void;
  }>();

  const visible = defineModel<boolean>('visible', {
    required: true,
  });

  const { t } = useI18n();

  const formRef = ref<FormInstance>();
  const batchLoading = ref(false);
  const form = ref<{ targetCollectionId: string }>({ targetCollectionId: '' });

  const options = computed(() =>
    props.moduleTree.map((item) => {
      return {
        label: item.name,
        value: item.id,
      };
    })
  );

  function handleCancel() {
    visible.value = false;
    form.value = { targetCollectionId: '' };
  }

  async function handleMove() {
    formRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          batchLoading.value = true;
          await props.batchMove({
            ...props.params,
            ...form.value,
          });
          handleCancel();
          Message.success(t('common.moveSuccess'));
          emit('loadList');
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          batchLoading.value = false;
        }
      }
    });
  }
</script>
