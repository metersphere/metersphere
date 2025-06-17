<template>
  <MsDrawer
    v-model:visible="innerVisible"
    :title="modelTitle"
    :width="800"
    :show-continue="!props.currentModelId"
    no-content-padding
    unmount-on-close
    :ok-loading="loading"
    :ok-text="props.currentModelId ? t('common.update') : t('common.save')"
    @confirm="handleDrawerConfirm"
    @continue="handleDrawerConfirm(true)"
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
              <a-form-item
                field="baseName"
                :label="t('system.config.modelConfig.baseModel')"
                class="w-[400px]"
                :rules="[
                  { required: true, message: t('common.notNull', { value: t('system.config.modelConfig.baseModel') }) },
                ]"
                asterisk-position="end"
              >
                <a-auto-complete
                  v-model:model-value="form.baseName"
                  :data="baseModelTypeOptions.filter((e) => e.isShow === true)"
                  :filter-option="false"
                  allow-clear
                  :placeholder="t('system.config.modelConfig.baseModelPlaceholder')"
                  @search="(val) => handleSearchParams(val)"
                  @select="(val) => selectAutoComplete(val)"
                  @change="(val) => selectAutoComplete(val)"
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
                asterisk-position="end"
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
                  { required: true, message: t('common.notNull', { value: t('system.config.modelConfig.apiKey') }) },
                ]"
                field="appKey"
                asterisk-position="end"
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
        <div v-if="form.advSettingDTOList.length" class="collapse-more-top my-[16px] pt-[16px]">
          <expandCollapseWrap :title="t('system.config.modelConfig.advancedSettings')">
            <template #content>
              <MsBatchForm
                ref="batchFormRef"
                :models="batchFormModels"
                enable-type="circle"
                :form-mode="baseModelForm"
                :default-vals="form.advSettingDTOList"
                show-enable
                hide-add
                @change="handleBatchFormChange"
              />
            </template>
          </expandCollapseWrap>
        </div>
      </a-form>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { FormInstance, Message, SelectOptionData } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBatchForm from '@/components/business/ms-batch-form/index.vue';
  import type { FormItemModel } from '@/components/business/ms-batch-form/types';
  import MsSelect from '@/components/business/ms-select';
  import expandCollapseWrap from './expandCollapseWrap.vue';

  import { editModelConfig, getModelConfigDetail } from '@/api/modules/setting/config';
  import { editPersonalModelConfig, getPersonalModelConfigDetail } from '@/api/modules/user';
  import { baseModelTypeMap, getModelDefaultConfig, modelTypeOptions } from '@/config/modelConfig';
  import { useI18n } from '@/hooks/useI18n';
  import { useUserStore } from '@/store';

  import type { ModelFormConfigParams, SupplierModelItem } from '@/models/setting/modelConfig';
  import { ModelBaseTypeEnum, ModelOwnerTypeTypeEnum, ModelPermissionTypeEnum, ModelTypeEnum } from '@/enums/modelEnum';

  const { t } = useI18n();

  const userStore = useUserStore();

  const props = defineProps<{
    supplierModelItem: SupplierModelItem;
    currentModelId: string;
    modelKey: 'personal' | 'system';
  }>();

  const emit = defineEmits<{
    (e: 'close'): void;
    (e: 'refresh'): void;
  }>();

  const innerVisible = defineModel<boolean>('visible', {
    required: true,
  });

  const modelDetailApiMap = {
    personal: getPersonalModelConfigDetail,
    system: getModelConfigDetail,
  }[props.modelKey];

  const modelEditApiMap = {
    personal: editPersonalModelConfig,
    system: editModelConfig,
  }[props.modelKey];

  const getPrecisionFun = (_: FormItemModel, element: FormItemModel) => (element.name === 'maxTokens' ? 0 : 1);

  const visibility = ref(true);
  const baseModelForm = ref<'create' | 'edit'>('create');
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
    {
      field: 'value',
      type: 'inputNumber',
      label: 'system.config.modelConfig.paramsValue',
      rules: [{ required: true, message: t('common.notNull', { value: t('system.config.modelConfig.paramsValue') }) }],
      placeholder: 'common.pleaseInput',
      maxKey: 'maxValue',
      minKey: 'minValue',
      getPrecisionFun,
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
    type: ModelTypeEnum.LLM,
    providerName: ModelBaseTypeEnum.DeepSeek,
    permissionType: props.modelKey === 'personal' ? ModelPermissionTypeEnum.PRIVATE : ModelPermissionTypeEnum.PUBLIC,
    status: true,
    owner: (props.modelKey === 'personal' ? userStore?.id : '') || '',
    ownerType: props.modelKey === 'personal' ? ModelOwnerTypeTypeEnum.PERSONAL : ModelOwnerTypeTypeEnum.SYSTEM,
    baseName: '',
    appKey: '',
    apiUrl: '',
    advSettingDTOList: [],
  };

  const form = ref<ModelFormConfigParams>(cloneDeep(initForm));

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

  function initBaseModelName() {
    baseModelTypeOptions.value = baseModelTypeMap[props.supplierModelItem.value].map((e) => ({
      ...e,
      isShow: true,
    }));
  }

  function selectAutoComplete(val: string) {
    form.value.baseName = val;
    form.value.advSettingDTOList = getModelDefaultConfig(props.supplierModelItem.value, form.value.baseName);
  }

  function clearBaseName() {
    form.value.advSettingDTOList = [];
    initBaseModelName();
  }

  const formRef = ref<FormInstance | null>(null);
  function handleDrawerCancel() {
    innerVisible.value = false;
    formRef.value?.resetFields();
    form.value = cloneDeep(initForm);
    initBaseModelName();
    emit('close');
  }

  const loading = ref(false);
  async function handleSave(cb: (data: ModelFormConfigParams) => Promise<any>, isContinue = false) {
    try {
      loading.value = true;
      await cb(form.value);
      Message.success(form.value.id ? t('common.updateSuccess') : t('common.createSuccess'));
      emit('refresh');
      if (isContinue) {
        form.value = cloneDeep(initForm);
      } else {
        handleDrawerCancel();
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  function handleDrawerConfirm(isContinue = false) {
    formRef.value?.validate(async (errors) => {
      if (!errors) {
        if (form.value.advSettingDTOList.length) {
          batchFormRef.value?.formValidate(async (list: any) => {
            form.value.advSettingDTOList = [...list];
            handleSave(modelEditApiMap, isContinue);
          });
        } else {
          handleSave(modelEditApiMap, isContinue);
        }
      }
    });
  }

  async function getDetail() {
    if (props.currentModelId) {
      try {
        form.value = await modelDetailApiMap(props.currentModelId);
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      }
    }
  }

  watch(
    () => innerVisible.value,
    (val) => {
      if (val) {
        getDetail();
        form.value.providerName = props.supplierModelItem.value;
        initBaseModelName();
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
