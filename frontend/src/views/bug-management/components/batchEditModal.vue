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
          :rules="[{ required: true, message: t('bugManagement.batchUpdate.required.attribute') }]"
        >
          <a-select
            v-model:model-value="form.attribute"
            :placeholder="t('common.pleaseSelect')"
            @change="handleAttributeChange"
          >
            <a-optgroup :label="t('bugManagement.batchUpdate.systemFiled')">
              <a-option v-for="item in systemOptionList" :key="item.value" :value="item.value">
                {{ item.label }}
              </a-option>
            </a-optgroup>
          </a-select>
        </a-form-item>
        <a-form-item
          v-if="['input', 'date', 'single_select'].includes(valueMode)"
          field="inputValue"
          asterisk-position="end"
          :label="t('bugManagement.batchUpdate.update')"
          :rules="[{ required: true, message: t('bugManagement.batchUpdate.required.value') }]"
        >
          <template v-if="valueMode === 'input'">
            <a-input v-model:model-value="form.inputValue" :disabled="!form.attribute" />
          </template>
          <template v-else-if="valueMode === 'date'">
            <a-date-picker v-model:model-value="form.inputValue" :disabled="!form.attribute" />
          </template>
          <template v-else-if="valueMode === 'single_select'">
            <a-select
              v-model:model-value="form.inputValue"
              :place-holder="t('common.pleaseSelect')"
              :disabled="!form.attribute"
              :placeholder="t('common.pleaseSelect')"
            >
              <a-option v-for="item in customFiledOption" :key="item.value" :value="item.value">
                {{ item.text }}
              </a-option>
            </a-select>
          </template>
        </a-form-item>
        <div v-else-if="valueMode === 'tags'">
          <a-form-item class="mb-[16px]" field="type" :label="t('common.type')">
            <a-radio-group v-model:model-value="selectedTagType" size="small">
              <a-radio :value="TagUpdateTypeEnum.UPDATE"> {{ t('common.update') }}</a-radio>
              <a-radio :value="TagUpdateTypeEnum.APPEND"> {{ t('caseManagement.featureCase.appendTag') }}</a-radio>
              <a-radio :value="TagUpdateTypeEnum.CLEAR">{{ t('common.clear') }}</a-radio>
            </a-radio-group>
          </a-form-item>
          <a-form-item
            v-if="valueMode === 'tags' && selectedTagType !== TagUpdateTypeEnum.CLEAR"
            field="value"
            :validate-trigger="['blur', 'input']"
            :label="t('common.batchUpdate')"
            asterisk-position="end"
            :rules="[{ required: true, message: t('common.inputPleaseEnterTags') }]"
          >
            <MsTagsInput
              v-model:modelValue="form.value"
              allow-clear
              empty-priority-highest
              :disabled="!form.attribute"
            />
            <div class="text-[12px] leading-[20px] text-[var(--color-text-4)]">{{ t('ms.tagsInput.tagLimitTip') }}</div>
          </a-form-item>
        </div>
        <a-form-item
          v-else
          field="value"
          asterisk-position="end"
          :label="t('bugManagement.batchUpdate.update')"
          :validate-trigger="['blur', 'input']"
          :rules="[{ required: true, message: t('common.inputPleaseEnterTags') }]"
        >
          <template v-if="valueMode === 'user_selector'">
            <MsUserSelector
              v-model:model-value="form.value"
              :type="UserRequestTypeEnum.PROJECT_PERMISSION_MEMBER"
              :load-option-params="{ projectId: appStore.currentProjectId }"
              :disabled="!form.attribute"
            />
          </template>
          <template v-else-if="valueMode === 'multiple_select'">
            <a-select v-model:model-value="form.value" :disabled="!form.attribute" multiple>
              <a-option v-for="item in customFiledOption" :key="item.value" :value="item.value">
                {{ item.text }}
              </a-option>
            </a-select>
          </template>
        </a-form-item>
      </a-form>
    </div>
    <template #footer>
      <div class="flex flex-row items-center justify-end">
        <div class="flex flex-row gap-[8px]">
          <a-button type="secondary" :loading="loading" @click="handleCancel">
            {{ t('common.cancel') }}
          </a-button>
          <a-button type="primary" :loading="loading" @click="handleConfirm">
            {{ t('common.update') }}
          </a-button>
        </div>
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

  import type { BugBatchUpdateFiledType } from '@/models/bug-management';
  import { BugBatchUpdateFiledForm, BugEditCustomField } from '@/models/bug-management';
  import { SelectValue } from '@/models/projectManagement/menuManagement';
  import { TagUpdateTypeEnum } from '@/enums/commonEnum';

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
  const selectedTagType = ref<TagUpdateTypeEnum>(TagUpdateTypeEnum.UPDATE);

  const formReset = () => {
    form.attribute = '';
    form.value = [];
    form.inputValue = '';
    form.append = false;
    valueMode.value = 'single_select';
    selectedTagType.value = TagUpdateTypeEnum.UPDATE;
  };

  const handleCancel = () => {
    formReset();
    currentVisible.value = false;
    showAppend.value = false;
    loading.value = false;
  };
  const customFiledOption = ref<{ text: string; value: string }[]>([]);

  const handleAttributeChange = (value: SelectValue) => {
    form.value = [];
    form.inputValue = '';
    showAppend.value = false;
    if (value === 'tags') {
      valueMode.value = 'tags';
      showAppend.value = true;
    } else if (value === 'handleUser') {
      valueMode.value = 'user_selector';
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
            ...props.selectParam,
            projectId: appStore.currentProjectId,
            [form.attribute]: form.value || form.inputValue,
            append: selectedTagType.value === TagUpdateTypeEnum.APPEND,
            clear: selectedTagType.value === TagUpdateTypeEnum.CLEAR,
          };
          await updateBatchBug(tmpObj);
          Message.success(t('common.updateSuccess'));
          emit('submit');
          handleCancel();
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
