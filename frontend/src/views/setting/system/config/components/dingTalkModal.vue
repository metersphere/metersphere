<template>
  <a-modal
    v-model:visible="detailVisible"
    title-align="start"
    class="ms-modal-upload ms-modal-medium"
    :width="680"
    :loading="loading"
  >
    <template #title>
      {{ t('project.messageManagement.DING_TALK') }}
    </template>

    <a-form ref="dingTalkFormRef" class="ms-form rounded-[4px]" :model="dingTalkForm" layout="vertical">
      <a-form-item
        field="appKey"
        :label="t('system.config.qrCodeConfig.appKey')"
        :rules="[{ required: true, message: t('system.config.qrCodeConfig.appKey.required') }]"
        :validate-trigger="['blur', 'input']"
        asterisk-position="end"
      >
        <a-input v-model="dingTalkForm.appKey" :max-length="255" :placeholder="t('formCreate.PleaseEnter')" />
      </a-form-item>
      <a-form-item
        field="agentId"
        :label="t('system.config.qrCodeConfig.agentId')"
        :rules="[{ required: true, message: t('system.config.qrCodeConfig.agentId.required') }]"
        :validate-trigger="['blur', 'input']"
        asterisk-position="end"
      >
        <a-input v-model="dingTalkForm.agentId" :max-length="255" :placeholder="t('formCreate.PleaseEnter')" />
      </a-form-item>
      <a-form-item
        field="appSecret"
        :label="t('system.config.qrCodeConfig.appSecret')"
        :rules="[{ required: true, message: t('system.config.qrCodeConfig.appSecret.required') }]"
        :validate-trigger="['blur', 'input']"
        asterisk-position="end"
      >
        <a-input-password
          v-model="dingTalkForm.appSecret"
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
            v-model="dingTalkForm.enable"
            class="ms-form-table-input-switch execute-form-table-input-switch"
            size="small"
          />
          <span class="ml-3 font-normal text-[var(--color-text-1)]">{{ t('system.config.qrCodeConfig.enable') }}</span>
        </div>
        <div class="ms-button-group">
          <a-button type="secondary" class="ml-[14px]" @click="cancelEdit">
            {{ t('common.cancel') }}
          </a-button>
          <a-button
            type="outline"
            class="ml-[14px]"
            :disabled="
              dingTalkForm.appKey == '' &&
              dingTalkForm.appSecret == '' &&
              dingTalkForm.agentId == '' &&
              dingTalkForm.callBack == ''
            "
            @click="validateInfo"
          >
            {{ t('organization.service.testLink') }}
          </a-button>
          <a-button type="primary" class="ml-[14px]" @click="saveInfo">
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

  import { getDingInfo, saveDingTalkConfig, validateDingTalkConfig } from '@/api/modules/setting/qrCode';
  import { useI18n } from '@/hooks/useI18n';

  import { DingTalkInfo } from '@/models/setting/qrCode';

  import Message from '@arco-design/web-vue/es/message';

  const { t } = useI18n();
  const dingTalkForm = ref<DingTalkInfo>({
    agentId: '',
    appKey: '',
    appSecret: '',
    callBack: '',
    enable: false,
    valid: false,
  });

  const dingTalkFormRef = ref<FormInstance | null>(null);

  const loading = ref<boolean>(false);
  const detailVisible = ref<boolean>(false);
  const props = defineProps<{
    visible: boolean;
  }>();

  const emits = defineEmits<{
    (event: 'update:visible', visible: boolean): void;
    (event: 'success'): void;
  }>();

  // 集成列表
  const loadList = async () => {
    loading.value = true;
    try {
      dingTalkForm.value = await getDingInfo();
    } catch (error) {
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
    dingTalkFormRef.value?.validate(async (errors: Record<string, ValidatedError> | undefined) => {
      if (!errors) {
        loading.value = true;
        try {
          await validateDingTalkConfig(dingTalkForm.value);
          dingTalkForm.value.valid = true;
          Message.success(t('organization.service.testLinkStatusTip'));
        } catch (error) {
          console.log(error);
        } finally {
          loading.value = false;
        }
      }
    });
  }

  async function saveInfo() {
    dingTalkFormRef.value?.validate(async (errors: Record<string, ValidatedError> | undefined) => {
      if (!errors) {
        loading.value = true;
        try {
          await saveDingTalkConfig(dingTalkForm.value);
          Message.success(t('organization.service.testLinkStatusTip'));
          emits('success');
        } catch (error) {
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
    align-items: center;
    flex-direction: row;
    justify-content: space-between;
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
