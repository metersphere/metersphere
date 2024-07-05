<template>
  <a-modal
    v-model:visible="detailVisible"
    title-align="start"
    class="ms-modal-form ms-modal-medium"
    :ok-text="t('organization.member.Confirm')"
    :cancel-text="t('organization.member.Cancel')"
    unmount-on-close
  >
    <template #title>
      <div class="relative flex w-full items-center justify-between">
        {{ title }}
        <div
          v-if="pluginId.toLowerCase().includes('tapd')"
          class="absolute bottom-[-40px] right-0 z-10 cursor-pointer text-[#0D68FF] underline"
          @click="openTapd"
        >
          {{ t('organization.service.applyTapdAccount') }}
        </div>
      </div>
    </template>
    <div class="wrapper">
      <MsFormCreate v-model:api="fApi" v-model:form-item="formItemList" :form-rule="formRules" :option="options">
      </MsFormCreate>
    </div>
    <template #footer>
      <div class="flex justify-between">
        <div class="flex flex-row items-center justify-center">
          <a-switch v-model="isEnable" :disabled="isDisabled" size="small" type="line" />
          <a-tooltip>
            <template #content>
              <div class="text-sm">
                {{
                  pluginId === 'jira'
                    ? t('organization.service.statusJiraEnableTip')
                    : t('organization.service.statusEnableTip')
                }}
              </div>
              <div class="text-sm">
                {{
                  pluginId === 'jira'
                    ? t('organization.service.statusJiraDisableTip')
                    : t('organization.service.statusDisableTip')
                }}
              </div>
            </template>
            <icon-question-circle class="ml-2 text-[--color-text-4]" />
          </a-tooltip>
        </div>
        <div>
          <a-button type="secondary" @click="handleCancel">{{ t('organization.service.Cancel') }}</a-button>
          <a-button class="ml-[12px]" type="outline" :loading="testLoading" @click="testLink">
            {{ t('organization.service.testLink') }}
          </a-button>
          <a-button class="ml-[12px]" type="primary" :loading="loading" @click="saveHandler">
            {{ t('organization.service.Confirm') }}
          </a-button>
        </div>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref, watch, watchEffect } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsFormCreate from '@/components/pure/ms-form-create/ms-form-create.vue';
  import type { FormItem, FormRuleItem } from '@/components/pure/ms-form-create/types';

  import { addOrUpdate, configScript, postValidate } from '@/api/modules/setting/serviceIntegration';
  import { useI18n } from '@/hooks/useI18n';
  import useLoading from '@/hooks/useLoading';
  import { useAppStore } from '@/store';

  import type { AddOrUpdateServiceModel, ServiceItem } from '@/models/setting/serviceIntegration';

  const { t } = useI18n();
  const appStore = useAppStore();
  const lastOrganizationId = appStore.currentOrgId;

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
  const formRules = ref<FormItem[]>([]);
  const formItemList = ref<FormRuleItem[]>([]);
  const title = ref<string>('');
  const formItem = ref<AddOrUpdateServiceModel>({});
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
      if (valid === true) {
        submit();
      } else {
        console.log(fail);
      }
    });
  };

  // 获取配置脚本
  const getPluginScript = async (cuurentPluginId: string) => {
    try {
      formRules.value = [];
      formItemList.value = [];
      const result = await configScript(cuurentPluginId);
      formRules.value = [...result];
      if (type.value === 'edit') {
        fApi.value.setValue({ ...formItem.value.configuration });
        fApi.value.refresh();
      }
    } catch (error) {
      console.log(error);
    }
  };
  // 测试连接是否通过
  const testLink = async () => {
    testLoading.value = true;
    fApi.value?.validate(async (valid: any) => {
      if (valid === true) {
        try {
          const formValue = {
            ...fApi.value.formData(),
          };
          await postValidate(formValue, `${pluginId.value}/${lastOrganizationId}`);
          if (!isConfigOrigin.value) isDisabled.value = false;
          Message.success(t('organization.service.successMessage'));
        } catch (error) {
          console.log(error);
        } finally {
          testLoading.value = false;
        }
      } else {
        testLoading.value = false;
      }
    });
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

  function openTapd() {
    window.open('https://jsj.top/f/Lpk1sh');
  }

  defineExpose({
    addOrEdit,
    title,
  });
</script>

<style scoped></style>
