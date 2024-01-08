<template>
  <MsDrawer
    v-model:visible="showDrawer"
    :mask="false"
    :title="t('caseManagement.featureCase.createDefect')"
    :ok-text="t('common.confirm')"
    :ok-loading="drawerLoading"
    :width="800"
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
        <a-input v-model="form.title" :max-length="255" show-word-limit />
      </a-form-item>
      <a-form-item :label="t('bugManagement.edit.content')">
        <MsRichText v-model:raw="form.description" />
      </a-form-item>
    </a-form>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { FormInstance, Message, ValidatedError } from '@arco-design/web-vue';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';

  import { createBug, getTemplageOption, getTemplateDetailInfo } from '@/api/modules/bug-management/index';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import { TemplateOption } from '@/models/common';

  const appStore = useAppStore();

  const props = defineProps<{
    visible: boolean;
  }>();

  const emit = defineEmits(['update:visible']);

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
          await createBug({ request: { ...form.value, customFields: templateCustomFields.value }, fileList: [] });
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

  onBeforeMount(async () => {
    templateOptions.value = await getTemplageOption({ projectId: appStore.currentProjectId });
    form.value.templateId = templateOptions.value.find((item) => item.enableDefault)?.id as string;
    const result = await getTemplateDetailInfo({ id: form.value.templateId, projectId: appStore.currentProjectId });
    templateCustomFields.value = result.customFields.map((item: any) => {
      return {
        id: item.fieldId,
        name: item.fieldName,
        type: item.type,
        value: item.defaultValue || '',
      };
    });
  });
</script>

<style scoped></style>
