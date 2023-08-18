<template>
  <a-modal
    v-model:visible="detailVisible"
    title-align="start"
    class="ms-modal-form ms-modal-medium"
    :ok-text="t('organization.member.Confirm')"
    :cancel-text="t('organization.member.Cancel')"
  >
    <template #title> {{ title }} </template>
    <div>
      <MsFormCreate v-model:api="fApi" :rule="formRules" :option="options" />
    </div>
    <template #footer>
      <div class="flex justify-between">
        <div class="flex flex-row items-center justify-center">
          <a-switch v-model="isEnable" :disabled="isDisabled" size="small" />
          <a-tooltip>
            <template #content>
              <div class="text-sm">{{ t('organization.service.statusEnableTip') }}</div>
              <div class="text-sm">{{ t('organization.service.statusDisableTip') }}</div>
            </template>
            <icon-question-circle class="ml-2 text-[--color-text-4]" />
          </a-tooltip>
        </div>
        <a-space>
          <a-button type="secondary" @click="handleCancel">{{ t('organization.service.Cancel') }}</a-button>
          <a-button type="outline" :loading="testLoading" @click="testLink">{{
            t('organization.service.testLink')
          }}</a-button>
          <a-button type="primary" :loading="loading" @click="saveHandler">{{
            t('organization.service.Confirm')
          }}</a-button>
        </a-space>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref, watchEffect, watch } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import MsFormCreate from '@/components/pure/ms-form-create/formCreate.vue';
  import { configScript, addOrUpdate, postValidate } from '@/api/modules/setting/serviceIntegration';
  import { Message } from '@arco-design/web-vue';
  import type { ServiceItem, AddOrUpdateServiceModel } from '@/models/setting/serviceIntegration';
  import useLoading from '@/hooks/useLoading';
  import { useUserStore } from '@/store';

  const { t } = useI18n();
  const userStore = useUserStore();
  const lastOrganizationId = userStore.$state?.lastOrganizationId as string;

  const emits = defineEmits<{
    (event: 'update:visible', visible: boolean): void;
    (event: 'success'): void;
  }>();

  const props = defineProps<{
    visible: boolean;
  }>();
  const detailVisible = ref<boolean>(false);
  const { loading, setLoading } = useLoading(false);

  const fApi = ref<any>({});
  const options = ref({
    resetBtn: false,
    submitBtn: false,
    on: false,
    form: {
      layout: 'vertical',
      labelAlign: 'left',
    },
    row: {
      gutter: 0,
    },
    wrap: {
      'asterisk-position': 'end',
      'validate-trigger': ['change'],
    },
  });
  const formRules = ref<any>([]);
  const title = ref<string>('');
  const formItem = ref<any>({});
  watchEffect(() => {
    detailVisible.value = props.visible;
  });
  watch(
    () => detailVisible.value,
    (val) => {
      emits('update:visible', val);
    }
  );

  const testLoading = ref<boolean>(false);
  // 禁用
  const isDisabled = ref<boolean>(false);
  // 是否配置
  const isConfigOrigin = ref<boolean | undefined>(false);
  // 状态
  const isEnable = ref<boolean | undefined>(false);
  // 插件id
  const pluginId = ref<string>('');
  const type = ref<string>('');

  const handleCancel = () => {
    fApi.value.clearValidateState();
    detailVisible.value = false;
  };

  const submit = () => {
    fApi.value?.submit(async (formData: FormData) => {
      setLoading(true);
      try {
        const params: AddOrUpdateServiceModel = {
          id: type.value === 'edit' ? formItem.value.id : undefined,
          pluginId: pluginId.value,
          enable: isEnable.value,
          organizationId: lastOrganizationId,
          configuration: { ...formData },
        };
        const message =
          type.value === 'edit' ? t('organization.service.updateSuccess') : t('organization.service.configSuccess');
        await addOrUpdate(params, type.value);
        Message.success(message);
        handleCancel();
        emits('success');
      } catch (error) {
        console.log(error);
      } finally {
        setLoading(false);
      }
    });
  };
  const saveHandler = () => {
    fApi.value?.validate((valid: any, fail: any) => {
      if (valid) {
        submit();
      } else {
        console.log(fail);
      }
    });
  };

  // 获取配置脚本
  const getPluginScript = async (cuurentPluginId: string) => {
    try {
      const result = await configScript(cuurentPluginId);
      formRules.value = [...result];
      if (type.value === 'edit') {
        fApi.value.nextTick(() => {
          fApi.value.setValue({ ...formItem.value.configuration });
          fApi.value.refresh();
        });
      }
    } catch (error) {
      console.log(error);
    }
  };
  // 测试连接是否通过
  const testLink = async () => {
    testLoading.value = true;
    try {
      const formValue = {
        ...fApi.value.formData(),
      };
      await postValidate(formValue, pluginId.value);
      if (!isConfigOrigin.value) isDisabled.value = false;
      Message.success(t('organization.service.successMessage'));
    } catch (error) {
      console.log(error);
    } finally {
      testLoading.value = false;
    }
  };

  // 创建&编辑
  const addOrEdit = (serviceItem: ServiceItem) => {
    type.value = serviceItem.config ? 'edit' : 'create';
    isConfigOrigin.value = serviceItem.config;
    isEnable.value = serviceItem.enable;
    isDisabled.value = !serviceItem.config;
    pluginId.value = serviceItem.pluginId as string;
    formItem.value = { ...serviceItem };
    getPluginScript(pluginId.value);
  };
  defineExpose({
    addOrEdit,
    title,
  });
</script>

<style scoped></style>
