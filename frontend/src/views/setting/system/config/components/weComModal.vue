<template>
  <a-modal
    v-model:visible="detailVisible"
    title-align="start"
    class="ms-modal-upload ms-modal-medium"
    :width="680"
    :loading="loading"
  >
    <template #title>
      {{ t('project.messageManagement.WE_COM') }}
    </template>

    <a-form ref="weComFormRef" class="ms-form rounded-[4px]" :model="weComForm" layout="vertical">
      <a-form-item
        field="corpId"
        :label="t('system.config.qrCodeConfig.corpId')"
        :rules="[{ required: true, message: t('system.config.qrCodeConfig.corpId.required') }]"
        :validate-trigger="['blur', 'input']"
        asterisk-position="end"
      >
        <a-input v-model="weComForm.corpId" :max-length="255" :placeholder="t('formCreate.PleaseEnter')" />
      </a-form-item>
      <a-form-item
        field="agentId"
        :label="t('system.config.qrCodeConfig.agentId')"
        :rules="[{ required: true, message: t('system.config.qrCodeConfig.agentId.required') }]"
        :validate-trigger="['blur', 'input']"
        asterisk-position="end"
      >
        <a-input v-model="weComForm.agentId" :max-length="255" :placeholder="t('formCreate.PleaseEnter')" />
      </a-form-item>
      <a-form-item
        field="appSecret"
        :label="t('system.config.qrCodeConfig.appSecret')"
        :rules="[{ required: true, message: t('system.config.qrCodeConfig.appSecret.required') }]"
        :validate-trigger="['blur', 'input']"
        asterisk-position="end"
      >
        <a-input-password
          v-model="weComForm.appSecret"
          allow-clear
          :max-length="255"
          :placeholder="t('formCreate.PleaseEnter')"
        />
      </a-form-item>
    </a-form>
    <template #footer>
      <div class="footer-button">
        <div class="ms-switch">
          <a-switch
            v-model="weComForm.enable"
            class="ms-form-table-input-switch execute-form-table-input-switch"
            size="small"
          />
          <span class="ml-3 font-normal text-[var(--color-text-1)]">{{ t('system.config.qrCodeConfig.enable') }}</span>
        </div>
        <div>
          <a-button class="ml-[14px]" type="secondary" @click="cancelEdit">
            {{ t('common.cancel') }}
          </a-button>
          <a-button
            class="ml-[14px]"
            type="outline"
            :disabled="
              weComForm.corpId == '' && weComForm.appSecret == '' && weComForm.agentId == '' && weComForm.callBack == ''
            "
            @click="validateInfo"
          >
            {{ t('organization.service.testLink') }}
          </a-button>
          <a-button class="ml-[14px]" type="primary" @click="saveInfo">
            {{ t('common.confirm') }}
          </a-button>
        </div>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { FormInstance, ValidatedError } from '@arco-design/web-vue';

  import { getWeComInfo, saveWeComConfig, validateWeComConfig } from '@/api/modules/setting/qrCode';
  import { useI18n } from '@/hooks/useI18n';

  import { WeComInfo } from '@/models/setting/qrCode';

  import Message from '@arco-design/web-vue/es/message';

  const { t } = useI18n();
  const weComForm = ref<WeComInfo>({
    corpId: '',
    agentId: '',
    appSecret: '',
    callBack: '',
    enable: false,
    valid: false,
  });

  const weComFormRef = ref<FormInstance | null>(null);

  const emits = defineEmits<{
    (event: 'update:visible', visible: boolean): void;
    (event: 'success'): void;
  }>();

  const loading = ref<boolean>(false);
  const detailVisible = ref<boolean>(false);
  const props = defineProps<{
    visible: boolean;
  }>();

  // 集成列表
  const loadList = async () => {
    loading.value = true;
    try {
      weComForm.value = await getWeComInfo();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  };

  watchEffect(() => {
    detailVisible.value = props.visible;
  });
  watch(
    () => detailVisible.value,
    (val) => {
      emits('update:visible', val);
      loadList();
    }
  );

  function cancelEdit() {
    detailVisible.value = false;
    emits('update:visible', detailVisible.value);
  }

  async function validateInfo() {
    weComFormRef.value?.validate(async (errors: Record<string, ValidatedError> | undefined) => {
      if (!errors) {
        loading.value = true;
        try {
          await validateWeComConfig(weComForm.value);
          weComForm.value.valid = true;
          Message.success(t('organization.service.testLinkStatusTip'));
        } catch (error) {
          weComForm.value.valid = false;
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          loading.value = false;
        }
      }
    });
  }

  async function saveInfo() {
    weComFormRef.value?.validate(async (errors: Record<string, ValidatedError> | undefined) => {
      if (!errors) {
        loading.value = true;
        try {
          await saveWeComConfig(weComForm.value);
          Message.success(t('common.saveSuccess'));
          emits('success');
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          loading.value = false;
          detailVisible.value = false;
        }
      }
    });
  }
</script>

<style scoped lang="less">
  .footer-button {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-direction: row;
  }
  .ms-switch {
    display: flex;
    align-items: center;
    flex-direction: row;
  }
  .ms-button-group {
    display: flex;
    align-items: center;
    flex-direction: row;
  }
</style>
