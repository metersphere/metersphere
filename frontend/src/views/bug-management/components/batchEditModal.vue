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
        <div>{{ t('bugManagement.batchEdit') }}</div>
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
          <a-select v-model:model-value="form.attribute" @change="handleArrtibuteChange">
            <a-optgroup :label="t('bugManagement.batchUpdate.systemFiled')">
              <a-option v-for="item in systemOptionList" :key="item.value" :value="item.value">{{
                item.label
              }}</a-option>
            </a-optgroup>
            <!-- V3.0 先不上自定义字段的批量更新 -->
            <!-- <a-optgroup :label="t('bugManagement.batchUpdate.customFiled')">
              <a-option v-for="item in customOptionList" :disabled="form.attribute === 'status'" :key="item.value" :value="item.value">{{
                item.label
              }}</a-option>
            </a-optgroup> -->
          </a-select>
        </a-form-item>
        <a-form-item
          v-if="['input', 'date', 'single_select'].includes(valueMode)"
          field="inputValue"
          asterisk-position="end"
          :label="t('bugManagement.batchUpdate.update')"
          :rules="[{ required: true }]"
        >
          <template v-if="valueMode === 'input'">
            <a-input v-model:model-value="form.inputValue" :disabled="!form.attribute" />
          </template>
          <template v-else-if="valueMode === 'date'">
            <a-date-picker v-model:model-value="form.inputValue" :disabled="!form.attribute" />
          </template>
          <template v-else-if="valueMode === 'single_select'">
            <a-select v-model:model-value="form.inputValue" :disabled="!form.attribute">
              <a-option v-for="item in customFiledOption" :key="item.value" :value="item.value">{{
                item.text
              }}</a-option>
            </a-select>
          </template>
        </a-form-item>
        <a-form-item
          v-else
          field="value"
          asterisk-position="end"
          :label="t('bugManagement.batchUpdate.update')"
          :rules="[{ required: true }]"
        >
          <template v-if="valueMode === 'tags'">
            <MsTagsInput v-model:modelValue="form.value" :disabled="!form.attribute"></MsTagsInput>
          </template>
          <template v-else-if="valueMode === 'user_selector'">
            <MsUserSelector
              v-model:model-value="form.value"
              :type="UserRequestTypeEnum.PROJECT_PERMISSION_MEMBER"
              :load-option-params="{ projectId: appStore.currentProjectId }"
              :disabled="!form.attribute"
            />
          </template>
          <template v-else-if="valueMode === 'multiple_select'">
            <a-select v-model:model-value="form.value" :disabled="!form.attribute" multiple>
              <a-option v-for="item in customFiledOption" :key="item.value" :value="item.value">{{
                item.text
              }}</a-option>
            </a-select>
          </template>
        </a-form-item>
      </a-form>
    </div>
    <template #footer>
      <div class="flex flex-row items-center justify-between">
        <div>
          <div v-if="showAppend" class="flex flex-row items-center gap-[4px]">
            <a-switch v-model:model-value="form.append" size="small" type="line" />
            <span class="text-[var(--color-text-1)]">{{ t('bugManagement.batchUpdate.update') }}</span>
            <a-tooltip position="top">
              <template #content>
                <div>{{ t('bugManagement.batchUpdate.openAppend') }}</div>
                <div>{{ t('bugManagement.batchUpdate.closeAppend') }}</div>
              </template>
              <MsIcon
                type="icon-icon-maybe_outlined"
                class="text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
              />
            </a-tooltip>
          </div>
        </div>
        <div class="flex flex-row gap-[8px]"
          ><a-button type="secondary" :loading="loading" @click="handleCancel">
            {{ t('common.cancel') }}
          </a-button>
          <a-button type="primary" :loading="loading" @click="handleConfirm">
            {{ t('common.update') }}
          </a-button></div
        >
      </div>
    </template>
  </a-modal>
</template>

<script lang="ts" setup>
  import { computed, reactive, ref } from 'vue';
  import { type FormInstance, Message, type ValidatedError } from '@arco-design/web-vue';

  import { BatchActionQueryParams } from '@/components/pure/ms-table/type';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import { MsUserSelector } from '@/components/business/ms-user-selector';
  import { UserRequestTypeEnum } from '@/components/business/ms-user-selector/utils';

  import { updateBatchBug } from '@/api/modules/bug-management';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import { tableParamsToRequestParams } from '@/utils';

  import type { BugBatchUpdateFiledType } from '@/models/bug-management';
  import { BugBatchUpdateFiledForm, BugEditCustomField } from '@/models/bug-management';
  import { SelectValue } from '@/models/projectManagement/menuManagement';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
    selectParam: BatchActionQueryParams;
    customFields: BugEditCustomField[];
  }>();
  const emit = defineEmits<{
    (e: 'submit'): void;
    (e: 'update:visible', value: boolean): void;
  }>();
  const appStore = useAppStore();
  const selectCount = computed(() => props.selectParam.currentSelectCount);
  const systemOptionList = computed(() => [
    {
      label: t('bugManagement.batchUpdate.tag'),
      value: 'tags',
    },
  ]);
  const currentVisible = computed({
    get() {
      return props.visible;
    },
    set(value) {
      emit('update:visible', value);
    },
  });
  const loading = ref(false);

  const form = reactive<BugBatchUpdateFiledForm>({
    // 批量更新的属性
    attribute: '',
    // 批量更新的值
    value: [],
    append: false,
    inputValue: '',
  });

  const valueMode = ref<BugBatchUpdateFiledType>('single_select');

  const showAppend = ref(false);

  const formRef = ref<FormInstance>();

  const formReset = () => {
    form.attribute = '';
    form.value = [];
    form.inputValue = '';
    form.append = false;
  };

  const handleCancel = () => {
    formReset();
    currentVisible.value = false;
    loading.value = false;
  };
  const customFiledOption = ref<{ text: string; value: string }[]>([]);

  const handleArrtibuteChange = (value: SelectValue) => {
    form.value = [];
    form.inputValue = '';
    if (value === 'tags') {
      valueMode.value = 'tags';
      showAppend.value = true;
    } else if (value === 'handleUser') {
      valueMode.value = 'user_selector';
      showAppend.value = true;
    } else {
      // 自定义字段
      const customField = props.customFields.find((item) => item.fieldId === value);
      if (customField) {
        if (customField.type?.toLowerCase() === 'input') {
          showAppend.value = false;
          valueMode.value = 'input';
          form.inputValue = '';
        } else {
          // select
          customFiledOption.value = JSON.parse(customField.platformOptionJson as string) || [];
          const isMutiple = customField.type?.toLocaleLowerCase() === 'select' && customField.isMutiple;
          showAppend.value = isMutiple || false;
          valueMode.value = isMutiple ? 'multiple_select' : 'single_select';
        }
      }
    }
  };

  const handleConfirm = () => {
    formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (!errors) {
        try {
          loading.value = true;
          const tmpObj = {
            ...tableParamsToRequestParams(props.selectParam),
            projectId: appStore.currentProjectId,
            [form.attribute]: form.value || form.inputValue,
            append: form.append,
          };
          await updateBatchBug(tmpObj);
          Message.success(t('common.updateSuccess'));
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
