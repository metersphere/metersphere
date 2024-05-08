<template>
  <MsDialog
    v-model:visible="isVisible"
    dialog-size="small"
    :title="t('testPlan.testPlanIndex.batchEdit', { number: props.batchParams.currentSelectCount })"
    ok-text="common.update"
    :confirm="confirmHandler"
    :close="closeHandler"
    unmount-on-close
    :switch-props="{
      switchName: t('caseManagement.featureCase.appendTag'),
      switchTooltip: t('caseManagement.featureCase.enableTags'),
      showSwitch: form.selectedAttrsId === 'tags' ? true : false,
      enable: form.append,
    }"
  >
    <div class="form">
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
          field="values"
          :label="t('apiTestManagement.batchUpdate')"
          :validate-trigger="['blur', 'input']"
          :rules="[{ required: true, message: t('apiTestManagement.valueRequired') }]"
          asterisk-position="end"
          class="mb-0"
          required
        >
          <MsTagsInput
            v-model:model-value="form.tags"
            placeholder="common.tagsInputPlaceholder"
            allow-clear
            unique-value
            retain-input-value
          />
        </a-form-item>
        <a-form-item
          v-else
          field="value"
          :label="t('apiTestManagement.batchUpdate')"
          :rules="[{ required: true, message: t('apiTestManagement.valueRequired') }]"
          asterisk-position="end"
          class="mb-0"
        >
          <a-select
            v-model="form.value"
            :placeholder="t('common.pleaseSelect')"
            :disabled="form.selectedAttrsId === ''"
          >
            <a-option v-for="item of valueOptions" :key="item.value" :value="item.value">
              {{ t(item.label) }}
            </a-option>
          </a-select>
        </a-form-item>
      </a-form>
    </div>
  </MsDialog>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { FormInstance, ValidatedError } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsDialog from '@/components/pure/ms-dialog/index.vue';
  import type { BatchActionQueryParams } from '@/components/pure/ms-table/type';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import { TableQueryParams } from '@/models/common';

  import Message from '@arco-design/web-vue/es/message';

  const isVisible = ref<boolean>(false);
  const appStore = useAppStore();
  const { t } = useI18n();

  const props = defineProps<{
    visible: boolean;
    batchParams: BatchActionQueryParams;
    activeFolder: string;
    offspringIds: string[];
    condition?: TableQueryParams;
  }>();

  const emits = defineEmits<{
    (e: 'update:visible', visible: boolean): void;
    (e: 'success'): void;
  }>();

  const currentProjectId = computed(() => appStore.currentProjectId);
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

  function closeHandler() {
    isVisible.value = false;
    formRef.value?.resetFields();
    form.value = { ...initForm };
  }

  async function confirmHandler(enable: boolean | undefined) {
    formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (!errors) {
        try {
          const customField = {
            fieldId: '',
            value: '',
          };
          const { selectedIds, selectAll, excludeIds } = props.batchParams;
          const params: TableQueryParams = {
            selectIds: selectedIds || [],
            selectAll: !!selectAll,
            excludeIds: excludeIds || [],
            projectId: currentProjectId.value,
            append: enable as boolean,
            tags: form.value.tags,
            moduleIds: props.activeFolder === 'all' ? [] : [props.activeFolder, ...props.offspringIds],
            customField: form.value.selectedAttrsId === 'systemTags' ? {} : customField,
            condition: {
              ...props.condition,
            },
          };
          Message.success(t('caseManagement.featureCase.editSuccess'));
          closeHandler();
          emits('success');
        } catch (e) {
          console.log(e);
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
