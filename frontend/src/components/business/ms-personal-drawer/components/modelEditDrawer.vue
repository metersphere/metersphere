<template>
  <MsDrawer
    v-model:visible="innerVisible"
    :title="modelTitle"
    :width="800"
    :show-continue="true"
    no-content-padding
    unmount-on-close
    :ok-text="props.currentModelId ? t('common.update') : t('common.save')"
    @confirm="handleDrawerConfirm"
    @cancel="handleDrawerCancel"
  >
    <div class="p-[16px]">
      <a-form ref="formRef" :model="form" layout="vertical">
        <div>
          <expandCollapseWrap :title="t('system.config.modelConfig.baseInfo')">
            <template #content>
              <a-form-item
                field="name"
                :label="t('system.config.modelConfig.modelName')"
                :rules="[
                  { required: true, message: t('common.notNull', { value: t('system.config.modelConfig.modelName') }) },
                ]"
                class="model-name-form"
                asterisk-position="end"
              >
                <template #label>
                  <div class="flex items-center">
                    {{ t('system.config.modelConfig.modelName') }}
                    <a-tooltip :content="t('system.config.modelConfig.modelNameTooltip')" position="top" mini>
                      <icon-question-circle
                        class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-6))]"
                      />
                    </a-tooltip>
                  </div>
                </template>
                <a-input
                  v-model="form.name"
                  :max-length="255"
                  :placeholder="t('system.config.modelConfig.modelNamePlaceholder')"
                />
              </a-form-item>
              <a-form-item
                field="type"
                :label="t('system.config.modelConfig.modelType')"
                :rules="[
                  { required: true, message: t('common.notNull', { value: t('system.config.modelConfig.modelType') }) },
                ]"
                asterisk-position="end"
                class="w-[400px]"
              >
                <MsSelect v-model:model-value="form.type" :options="modelTypeOptions" />
              </a-form-item>
              <a-form-item field="baseName" :label="t('system.config.modelConfig.baseModel')" class="w-[400px]">
                <a-auto-complete
                  v-model:model-value="form.baseName"
                  :data="baseModelTypeOptions"
                  class="ms-form-table-input ms-form-table-input--hasPlaceholder"
                  :filter-option="false"
                  allow-clear
                  :placeholder="t('system.config.modelConfig.baseModelPlaceholder')"
                  @search="(val) => handleSearchParams(val)"
                  @select="(val) => selectAutoComplete(val)"
                  @clear="clearBaseName"
                >
                  <template #option="{ data: opt }">
                    <div class="flex w-[350px] items-center gap-[8px]">
                      <div>{{ t(opt.raw.label) }}</div>
                      <a-tooltip v-if="opt.raw.tooltip" :content="t(opt.raw.tooltip)">
                        <MsIcon
                          type="icon-icon_info_outlined"
                          class="text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
                          size="16"
                        />
                      </a-tooltip>
                    </div>
                  </template>
                </a-auto-complete>
              </a-form-item>
              <a-form-item
                :rules="[
                  {
                    required: true,
                    message: t('common.notNull', { value: t('system.config.modelConfig.apiHostName') }),
                  },
                ]"
                field="apiUrl"
                :label="t('system.config.modelConfig.apiHostName')"
              >
                <a-input
                  v-model="form.apiUrl"
                  :max-length="255"
                  :placeholder="t('system.config.modelConfig.apiHostNamePlaceholder')"
                />
              </a-form-item>
              <a-form-item
                :rules="[
                  { required: true, message: t('common.notNull', { value: t('system.config.modelConfig.secretKey') }) },
                ]"
                field="appKey"
                :label="t('system.config.modelConfig.apiKey')"
              >
                <a-input-password
                  v-model="form.appKey"
                  v-model:visibility="visibility"
                  :default-visibility="false"
                  :max-length="255"
                  :placeholder="t('system.config.modelConfig.apiKeyPlaceholder')"
                  allow-clear
                />
              </a-form-item>
            </template>
          </expandCollapseWrap>
        </div>
        <div class="collapse-more-top my-[16px] pt-[16px]">
          <ExpandCollapseWrap :title="t('system.config.modelConfig.advancedSettings')">
            <template #content>
              <MsBatchForm
                v-if="form.baseName"
                ref="batchFormRef"
                :models="batchFormModels"
                :form-mode="userFormMode"
                add-text="system.user.addUser"
                :default-vals="form.list"
                show-enable
                hide-add
                @change="handleBatchFormChange"
              />
              <div v-else class="bg-[var(--color-text-n9)] p-[16px] text-center text-[var(--color-text-4)]">
                {{ t('system.config.modelConfig.inputBaseInfoTip') }}
              </div>
            </template>
          </ExpandCollapseWrap>
        </div>
      </a-form>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { SelectOptionData } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBatchForm from '@/components/business/ms-batch-form/index.vue';
  import type { FormItemModel } from '@/components/business/ms-batch-form/types';
  import MsSelect from '@/components/business/ms-select';
  import expandCollapseWrap from './expandCollapseWrap.vue';

  import { baseModelTypeMap, getModelDefaultConfig } from '@/config/modelConfig';
  import { useI18n } from '@/hooks/useI18n';

  import type { ModelForm, SupplierModelItem } from '@/models/setting/modelConfig';

  const { t } = useI18n();

  const props = defineProps<{
    supplierModelItem: SupplierModelItem;
    currentModelId: string;
  }>();

  const emit = defineEmits<{
    (e: 'close'): void;
  }>();

  const innerVisible = defineModel<boolean>('visible', {
    required: true,
  });

  const visibility = ref(false);
  const userFormMode = ref<'create' | 'edit'>('create');
  const batchFormRef = ref<InstanceType<typeof MsBatchForm>>();
  const batchFormModels: Ref<FormItemModel[]> = ref([
    {
      field: 'name',
      type: 'input',
      label: 'system.config.modelConfig.params',
      rules: [{ required: true, message: t('common.notNull', { value: t('system.config.modelConfig.params') }) }],
      placeholder: 'common.pleaseInput',
      disabled: true,
    },
    {
      field: 'label',
      type: 'input',
      label: 'system.config.modelConfig.displayName',
      rules: [{ required: true, message: t('common.notNull', { value: t('system.config.modelConfig.displayName') }) }],
      placeholder: 'common.pleaseInput',
      disabled: true,
    },
    // TODO 默认值需要确认是否需要范围框
    {
      field: 'value',
      type: 'inputNumber',
      label: 'system.config.modelConfig.paramsValue',
      rules: [{ required: true, message: t('common.notNull', { value: t('system.config.modelConfig.paramsValue') }) }],
      placeholder: 'common.pleaseInput',
      maxKey: 'maxValue',
      minKey: 'minValue',
    },
  ]);

  const modelTitle = computed(
    () =>
      `${props.currentModelId ? t('system.config.modelConfig.editModel') : t('system.config.modelConfig.addModel')}（${
        props.supplierModelItem.name
      }）`
  );

  const initForm = {
    id: '',
    name: '',
    type: 'LLM',
    baseName: '',
    apiUrl: '',
    appKey: '',
    list: [],
  };

  const form = ref<ModelForm>(cloneDeep(initForm));

  const modelTypeOptions = [
    {
      label: t('system.config.modelConfig.largeLanguageModel'),
      value: 'LLM',
    },
  ];

  const baseModelTypeOptions = ref<SelectOptionData[]>([]);
  const isBatchFormChange = ref(false);
  function handleBatchFormChange() {
    isBatchFormChange.value = true;
  }
  function handleSearchParams(val: string) {
    baseModelTypeOptions.value = baseModelTypeOptions.value?.map((e) => {
      e.isShow = (e.label || '').toLowerCase().includes(val.toLowerCase());
      return e;
    });
  }

  function selectAutoComplete(val: string) {
    form.value.baseName = val;
    form.value.list = getModelDefaultConfig(props.supplierModelItem.value, form.value.baseName);
  }

  function clearBaseName() {
    form.value.list = [];
  }

  function handleDrawerConfirm() {}

  function handleDrawerCancel() {
    form.value = cloneDeep(initForm);
    emit('close');
  }

  watch(
    () => innerVisible.value,
    (val) => {
      if (val) {
        baseModelTypeOptions.value = baseModelTypeMap[props.supplierModelItem.value];
        form.value.list = getModelDefaultConfig(props.supplierModelItem.value, form.value.baseName);
      }
    }
  );
</script>

<style lang="less">
  .model-name-form {
    .arco-form-item-label {
      @apply flex flex-nowrap items-center;
    }
  }
  .collapse-more-top {
    border-top: 1px dotted var(--color-text-n8);
  }
</style>
