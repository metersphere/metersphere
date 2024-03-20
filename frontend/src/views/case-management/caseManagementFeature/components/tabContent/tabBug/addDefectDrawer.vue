<template>
  <MsDrawer
    v-model:visible="showDrawer"
    :mask="true"
    :title="t('caseManagement.featureCase.createDefect')"
    :ok-text="t('common.confirm')"
    :ok-loading="drawerLoading"
    :width="800"
    :mask-closable="true"
    unmount-on-close
    :show-continue="true"
    @continue="handleDrawerConfirm(true)"
    @confirm="handleDrawerConfirm"
    @cancel="handleDrawerCancel"
  >
    <a-form ref="formRef" :model="form" layout="vertical">
      <a-form-item
        field="title"
        :label="t('bugManagement.bugName')"
        :rules="[{ required: true, message: t('bugManagement.edit.nameIsRequired') }]"
        :placeholder="t('bugManagement.edit.pleaseInputBugName')"
      >
        <a-input v-model="form.title" :max-length="255" />
      </a-form-item>
      <a-form-item :label="t('bugManagement.edit.content')">
        <MsRichText
          v-model:raw="form.description"
          :upload-image="handleUploadImage"
          :preview-url="EditorPreviewFileUrl"
        />
      </a-form-item>
    </a-form>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { FormInstance, Message, ValidatedError } from '@arco-design/web-vue';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';

  import {
    createOrUpdateBug,
    editorUploadFile,
    getTemplateDetailInfo,
    getTemplateOption,
  } from '@/api/modules/bug-management/index';
  import { EditorPreviewFileUrl } from '@/api/requrls/bug-management';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import { TemplateOption } from '@/models/common';

  const appStore = useAppStore();

  const props = defineProps<{
    visible: boolean;
    caseId: string;
  }>();

  const emit = defineEmits<{
    (e: 'update:visible', visible: boolean): void;
    (e: 'success'): void;
  }>();

  const { t } = useI18n();

  const templateOptions = ref<TemplateOption[]>([]);

  // TODO缺陷类型
  const initForm: any = {
    title: '',
    templateId: '',
    projectId: appStore.currentProjectId,
    description: '',
    customFields: [],
  };

  const form = ref({ ...initForm });

  const showDrawer = computed({
    get() {
      return props.visible;
    },
    set(value) {
      emit('update:visible', value);
    },
  });

  const formRef = ref<FormInstance | null>(null);
  const templateCustomFields = ref([]);
  function handleDrawerCancel() {
    formRef.value?.resetFields();
    form.value = { ...initForm };
    showDrawer.value = false;
  }

  const drawerLoading = ref<boolean>(false);

  function handleDrawerConfirm(isContinue: boolean) {
    formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (!errors) {
        drawerLoading.value = true;
        try {
          await createOrUpdateBug({
            request: { ...form.value, customFields: templateCustomFields.value, caseId: props.caseId },
            fileList: [],
          });
          emit('success');
          Message.success(t('caseManagement.featureCase.quicklyCreateDefectSuccess'));
          if (!isContinue) {
            handleDrawerCancel();
          }
          form.value = { ...initForm };
        } catch (error) {
          console.log(error);
        } finally {
          drawerLoading.value = false;
        }
      }
    });
  }

  async function initBugTemplate() {
    try {
      templateOptions.value = await getTemplateOption(appStore.currentProjectId);
      form.value.templateId = templateOptions.value.find((item) => item.enableDefault)?.id as string;
      const result = await getTemplateDetailInfo({ id: form.value.templateId, projectId: appStore.currentProjectId });
      templateCustomFields.value = result.customFields.map((item: any) => {
        return {
          id: item.fieldId,
          name: item.fieldName,
          type: item.type,
          value: (Array.isArray(item.defaultValue) ? JSON.stringify(item.defaultValue) : item.defaultValue) || '',
        };
      });
    } catch (error) {
      console.log(error);
    }
  }

  async function handleUploadImage(file: File) {
    const { data } = await editorUploadFile({
      fileList: [file],
    });
    return data;
  }

  watch(
    () => showDrawer.value,
    (val) => {
      if (val) {
        initBugTemplate();
      }
    }
  );
</script>

<style scoped></style>
