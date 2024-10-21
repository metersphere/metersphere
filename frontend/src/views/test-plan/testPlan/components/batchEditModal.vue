<template>
  <a-modal v-model:visible="isVisible" title-align="start" class="ms-modal-upload ms-modal-medium" :width="480">
    <template #title>
      {{ t('common.edit') }}
      <div class="text-[var(--color-text-4)]">
        {{
          t('common.selectedCount', {
            count: props.batchParams.currentSelectCount,
          })
        }}
      </div>
    </template>
    <a-form ref="formRef" class="rounded-[4px]" :model="form" layout="vertical">
      <a-form-item
        field="selectedAttrsId"
        :label="t('apiTestManagement.chooseAttr')"
        :rules="[{ required: true, message: t('apiTestManagement.attrRequired') }]"
        asterisk-position="end"
      >
        <a-select v-model="form.selectedAttrsId" :placeholder="t('common.pleaseSelect')">
          <a-option v-for="item of attrOptions" :key="item.value" :value="item.value">
            {{ t(item.name) }}
          </a-option>
        </a-select>
      </a-form-item>
      <a-form-item
        v-if="form.selectedAttrsId === 'tags'"
        :class="`${selectedTagType === TagUpdateTypeEnum.CLEAR ? 'mb-0' : 'mb-[16px]'}`"
        field="type"
        :label="t('common.type')"
      >
        <a-radio-group v-model:model-value="selectedTagType" size="small">
          <a-radio :value="TagUpdateTypeEnum.UPDATE"> {{ t('common.update') }}</a-radio>
          <a-radio :value="TagUpdateTypeEnum.APPEND"> {{ t('caseManagement.featureCase.appendTag') }}</a-radio>
          <a-radio :value="TagUpdateTypeEnum.CLEAR">{{ t('common.clear') }}</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item
        v-if="form.selectedAttrsId === 'tags' && selectedTagType !== TagUpdateTypeEnum.CLEAR"
        field="tags"
        :label="t('common.batchUpdate')"
        :rules="[{ required: true, message: t('common.inputPleaseEnterTags') }]"
        asterisk-position="end"
        :validate-trigger="['blur', 'input']"
        class="mb-0"
        required
      >
        <MsTagsInput v-model:modelValue="form.tags" allow-clear empty-priority-highest></MsTagsInput>
        <div class="text-[12px] leading-[20px] text-[var(--color-text-4)]">{{ t('ms.tagsInput.tagLimitTip') }}</div>
      </a-form-item>
      <a-form-item
        v-if="form.selectedAttrsId !== 'tags'"
        field="value"
        :label="t('common.batchUpdate')"
        :rules="[{ required: true, message: t('apiTestManagement.valueRequired') }]"
        asterisk-position="end"
        class="mb-0"
      >
        <a-select v-model="form.value" :placeholder="t('common.pleaseSelect')" :disabled="form.selectedAttrsId === ''">
          <a-option v-for="item of valueOptions" :key="item.value" :value="item.value">
            {{ t(item.label) }}
          </a-option>
        </a-select>
      </a-form-item>
    </a-form>

    <template #footer>
      <div class="flex justify-end">
        <div class="flex justify-end">
          <a-button type="secondary" :disabled="batchEditLoading" @click="closeHandler">
            {{ t('common.cancel') }}
          </a-button>
          <a-button class="ml-3" type="primary" :loading="batchEditLoading" @click="confirmHandler">
            {{ t('common.update') }}
          </a-button>
        </div>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { FormInstance } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import type { BatchActionQueryParams } from '@/components/pure/ms-table/type';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';

  import { batchEditTestPlan } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import { TableQueryParams } from '@/models/common';
  import { TagUpdateTypeEnum } from '@/enums/commonEnum';
  import { testPlanTypeEnum } from '@/enums/testPlanEnum';

  import Message from '@arco-design/web-vue/es/message';

  const isVisible = ref<boolean>(false);
  const { t } = useI18n();
  const appStore = useAppStore();
  const props = defineProps<{
    visible: boolean;
    batchParams: BatchActionQueryParams;
    activeFolder: string;
    offspringIds: string[];
    condition?: TableQueryParams;
    showType: keyof typeof testPlanTypeEnum;
  }>();

  const emits = defineEmits<{
    (e: 'update:visible', visible: boolean): void;
    (e: 'success'): void;
  }>();

  const initForm = {
    selectedAttrsId: '',
    append: false,
    tags: [],
    value: '',
  };
  const form = ref(cloneDeep(initForm));

  const attrOptions = [
    {
      name: 'common.tag',
      value: 'tags',
    },
  ];

  const formRef = ref<FormInstance | null>(null);
  const selectedTagType = ref<TagUpdateTypeEnum>(TagUpdateTypeEnum.UPDATE);

  function closeHandler() {
    isVisible.value = false;
    formRef.value?.resetFields();
    form.value = {
      selectedAttrsId: '',
      append: false,
      tags: [],
      value: '',
    };
    selectedTagType.value = TagUpdateTypeEnum.UPDATE;
  }

  const batchEditLoading = ref(false);

  function confirmHandler() {
    formRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          batchEditLoading.value = true;
          const { selectedIds, selectAll, excludeIds } = props.batchParams;
          const params: TableQueryParams = {
            selectIds: selectedIds || [],
            selectAll: !!selectAll,
            excludeIds: excludeIds || [],
            projectId: appStore.currentProjectId,
            moduleIds: props.activeFolder === 'all' ? [] : [props.activeFolder, ...props.offspringIds],
            condition: {
              ...props.condition,
            },
            ...form.value,
            append: selectedTagType.value === TagUpdateTypeEnum.APPEND,
            type: props.showType,
            editColumn: 'TAGS',
            clear: selectedTagType.value === TagUpdateTypeEnum.CLEAR,
          };
          await batchEditTestPlan(params);
          Message.success(t('caseManagement.featureCase.editSuccess'));
          closeHandler();
          emits('success');
        } catch (e) {
          console.log(e);
        } finally {
          batchEditLoading.value = false;
        }
      } else {
        return false;
      }
    });
  }

  const valueOptions = ref<{ value: string; label: string }[]>([]);

  watch(
    () => isVisible.value,
    (val) => {
      emits('update:visible', val);
    }
  );

  watch(
    () => props.visible,
    (val) => {
      isVisible.value = val;
    }
  );
</script>

<style scoped></style>
