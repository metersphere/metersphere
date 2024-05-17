<template>
  <a-modal
    v-model:visible="showModal"
    title-align="start"
    class="ms-modal-form ms-modal-medium"
    :cancel-text="t('common.cancel')"
    unmount-on-close
    @close="handleCancel"
  >
    <template #title>
      <a-tooltip :content="title">
        <span> {{ characterLimit(title) }}</span>
      </a-tooltip>
    </template>

    <div class="form">
      <a-form ref="demandFormRef" :model="modelForm" layout="vertical">
        <a-form-item :label="t('caseManagement.featureCase.tableColumnID')" asterisk-position="end" field="demandId">
          <a-input
            v-model="modelForm.demandId"
            :max-length="255"
            :placeholder="t('caseManagement.featureCase.pleaseEnterID')"
          />
        </a-form-item>
        <a-form-item
          :label="t('caseManagement.featureCase.requirementTitle')"
          asterisk-position="end"
          field="demandName"
          :rules="[{ required: true, message: t('caseManagement.featureCase.pleaseEnterTitle') }]"
        >
          <a-input
            v-model="modelForm.demandName"
            :max-length="255"
            :placeholder="t('caseManagement.featureCase.pleaseEnterTitle')"
          />
        </a-form-item>
        <a-form-item
          :label="t('caseManagement.featureCase.requirementUrl')"
          asterisk-position="end"
          field="demandUrl"
          :rules="[
            {
              validator(value, cb) {
                if (value) {
                  if (regexUrl.test(value)) {
                    return cb();
                  } else {
                    return cb(t('caseManagement.featureCase.pleaseEnterCorrectURLFormat'));
                  }
                } else {
                  return cb();
                }
              },
            },
          ]"
        >
          <a-input
            v-model="modelForm.demandUrl"
            :max-length="255"
            :placeholder="t('caseManagement.featureCase.pleaseEnterRequirementUrl')"
          />
        </a-form-item>
      </a-form>
    </div>
    <template #footer>
      <a-button type="secondary" @click="handleCancel">{{ t('common.cancel') }}</a-button>
      <a-button v-if="!form.id" type="secondary" @click="handleOK(true)">{{ t('ms.dialog.saveContinue') }}</a-button>
      <a-button class="ml-[12px]" type="primary" :loading="props.loading" @click="handleOK(false)">
        {{ updateName ? t('common.update') : t('common.create') }}
      </a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { FormInstance, ValidatedError } from '@arco-design/web-vue';

  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import { characterLimit, regexUrl } from '@/utils';

  import type { CreateOrUpdateDemand, DemandFormList, DemandItem } from '@/models/caseManagement/featureCase';

  const appStore = useAppStore();

  const { t } = useI18n();
  const props = defineProps<{
    caseId: string;
    visible: boolean;
    form: DemandItem;
    loading: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'update:visible', v: boolean): void;
    (e: 'update:form', v: CreateOrUpdateDemand): void;
    (e: 'success'): void;
    (e: 'save', params: CreateOrUpdateDemand, isContinue: boolean): void;
  }>();
  const pageConfig = computed(() => appStore.pageConfig);
  const form = ref<CreateOrUpdateDemand>({
    id: '',
    caseId: props.caseId,
    demandPlatform: pageConfig.value.platformName,
  });

  const updateName = ref<string>('');

  const showModal = ref<boolean>(false);

  const initModelForm: DemandFormList = {
    demandId: '',
    demandName: '',
    demandUrl: '',
  };

  const modelForm = ref<DemandFormList>({
    ...initModelForm,
  });

  const demandFormRef = ref<FormInstance | null>(null);

  function resetForm() {
    modelForm.value = { ...initModelForm };
    form.value.id = '';
  }

  function handleCancel() {
    demandFormRef.value?.resetFields();
    showModal.value = false;
    resetForm();
  }

  function handleOK(isContinue: boolean) {
    demandFormRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (!errors) {
        const { demandId, demandName, demandUrl } = modelForm.value;
        const params: CreateOrUpdateDemand = {
          ...form.value,
          demandList: [{ demandId, demandName, demandUrl }],
        };
        emit('save', params, isContinue);
      } else {
        return false;
      }
    });
  }
  const title = ref<string>('');
  watchEffect(() => {
    title.value = form.value.id
      ? t('caseManagement.featureCase.updateDemand', { name: props.form.demandName })
      : t('caseManagement.featureCase.addDemand');
  });

  watch(
    () => props.visible,
    (val) => {
      showModal.value = val;
    }
  );

  watch(
    () => showModal.value,
    (val) => {
      emit('update:visible', val);
    }
  );

  watch(
    () => props.form,
    (val) => {
      form.value.id = '';
      modelForm.value = { ...val };
      form.value.id = val.id;
      updateName.value = val.demandName;
    }
  );

  watch(
    () => form.value,
    (val) => {
      if (val) {
        emit('update:form', val);
      }
    }
  );

  defineExpose({
    resetForm,
  });
</script>

<style scoped></style>
