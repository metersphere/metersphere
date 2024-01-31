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
      {{ title }}
    </template>
    <div class="form">
      <a-form ref="demandFormRef" :model="modelForm" size="large" layout="vertical">
        <a-form-item :label="t('caseManagement.featureCase.tableColumnID')" asterisk-position="end" field="demandId">
          <a-input
            v-model="modelForm.demandId"
            :max-length="20"
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
        <a-form-item :label="t('caseManagement.featureCase.requirementUrl')" asterisk-position="end" field="demandUrl">
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
      <a-button class="ml-[12px]" type="primary" :loading="confirmLoading" @click="handleOK(false)">
        {{ updateName ? t('common.update') : t('common.create') }}
      </a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { FormInstance, Message, ValidatedError } from '@arco-design/web-vue';

  import { addDemandRequest, updateDemand } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import type { CreateOrUpdateDemand, DemandFormList, DemandItem } from '@/models/caseManagement/featureCase';

  const appStore = useAppStore();

  const { t } = useI18n();
  const props = defineProps<{
    caseId: string;
    visible: boolean;
    form: DemandItem;
  }>();

  const emit = defineEmits<{
    (e: 'update:visible', v: boolean): void;
    (e: 'success'): void;
  }>();
  const pageConfig = computed(() => appStore.pageConfig);
  const form = ref<CreateOrUpdateDemand>({
    id: '',
    caseId: props.caseId,
    demandPlatform: pageConfig.value.platformName,
  });

  const updateName = ref<string>('');

  const showModal = ref<boolean>(false);

  const confirmLoading = ref<boolean>(false);

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
        try {
          const { demandId, demandName, demandUrl } = modelForm.value;
          confirmLoading.value = true;
          const params: CreateOrUpdateDemand = {
            ...form.value,
            demandList: [{ demandId, demandName, demandUrl }],
          };
          if (form.value.id) {
            await updateDemand(params);
            Message.success(t('common.updateSuccess'));
          } else {
            await addDemandRequest(params);
            Message.success(t('common.addSuccess'));
          }
          if (!isContinue) {
            handleCancel();
          }
          resetForm();
          emit('success');
        } catch (error) {
          console.log(error);
        } finally {
          confirmLoading.value = false;
        }
      } else {
        return false;
      }
    });
  }

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

  const title = ref<string>('');
  watchEffect(() => {
    title.value = form.value.id
      ? t('caseManagement.featureCase.updateDemand', { name: props.form.demandName })
      : t('caseManagement.featureCase.addDemand');
  });

  watch(
    () => props.form,
    (val) => {
      modelForm.value = { ...val };
      form.value.id = val.id;
      updateName.value = val.demandName;
    }
  );
</script>

<style scoped></style>
