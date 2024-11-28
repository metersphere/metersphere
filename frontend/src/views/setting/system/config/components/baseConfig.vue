<template>
  <div>
    <MsCard class="mb-[16px]" :loading="baseLoading" simple auto-height>
      <div class="mb-[16px] flex justify-between">
        <div class="card-title">
          <div class="card-title-line"></div>
          <div class="card-title-text">{{ t('system.config.baseInfo') }}</div>
        </div>
        <a-button
          v-permission="['SYSTEM_PARAMETER_SETTING_BASE:READ+UPDATE']"
          type="outline"
          size="mini"
          @click="baseInfoModalVisible = true"
        >
          {{ t('system.config.update') }}
        </a-button>
      </div>
      <MsDescription :descriptions="baseInfoDesc" class="no-bottom" :column="2" />
    </MsCard>
    <MsCard class="mb-[16px]" :loading="emailLoading" simple auto-height>
      <div class="mb-[16px] flex justify-between">
        <div class="card-title">
          <div class="card-title-line"></div>
          <div class="card-title-text">{{ t('system.config.emailConfig') }}</div>
        </div>
        <a-button
          v-permission="['SYSTEM_PARAMETER_SETTING_BASE:READ+UPDATE']"
          type="outline"
          size="mini"
          @click="emailConfigDrawerVisible = true"
        >
          {{ t('system.config.update') }}
        </a-button>
      </div>
      <MsDescription :descriptions="emailInfoDesc" :column="2">
        <template #value="{ item }">
          <template v-if="item.key && ['ssl', 'tsl'].includes(item.key)">
            <div v-if="item.value === 'true'" class="flex items-center">
              <icon-check-circle-fill class="mr-[8px] text-[rgb(var(--success-6))]" />{{
                t('system.config.email.open')
              }}
            </div>
            <div v-else-if="item.value === 'false'" class="flex items-center">
              <MsIcon type="icon-icon_disable" class="mr-[4px] text-[var(--color-text-4)]" />
              {{ t('system.config.email.close') }}
            </div>
            <div v-else>-</div>
          </template>
          <template v-else-if="item.key === 'password' && item.value !== null">
            <span v-if="pswInVisible">
              {{ item.value }}
              <icon-eye class="cursor-pointer text-[var(--color-text-4)]" @click="togglePswVisible" />
            </span>
            <span v-else>
              {{ desensitize(item.value as string) }}
              <icon-eye-invisible class="cursor-pointer text-[var(--color-text-4)]" @click="togglePswVisible" />
            </span>
          </template>
          <template v-else>
            {{ item.value === undefined || item.value === null || item.value?.toString() === '' ? '-' : item.value }}
          </template>
        </template>
      </MsDescription>
      <a-button
        v-if="emailConfigForm.host !== null"
        type="outline"
        size="mini"
        class="arco-btn-outline--secondary mt-[16px]"
        :loading="testLoading"
        @click="testLink('page')"
      >
        {{ t('system.config.email.test') }}
      </a-button>
    </MsCard>
    <MsCard class="mb-[16px]" :loading="fileSizeLimitLoading" simple auto-height>
      <div class="mb-[16px] flex justify-between">
        <div class="card-title">
          <div class="card-title-line"></div>
          <div class="card-title-text">
            {{ t('system.config.fileLimit') }}
            <div class="text-[var(--color-text-4)]">(M)</div>
          </div>
        </div>
      </div>
      <a-input-number
        v-model:model-value="fileSizeLimit"
        class="w-[130px]"
        :disabled="fileSizeLimitLoading || !hasAnyPermission(['SYSTEM_PARAMETER_SETTING_BASE:READ+UPDATE'])"
        :min="0"
        :max="1024"
        :precision="0"
        mode="button"
        model-event="input"
        @blur="() => saveFileSizeLimitConfig()"
      />
    </MsCard>
    <a-modal
      v-model:visible="baseInfoModalVisible"
      :title="t('system.config.baseInfo.updateTitle')"
      :ok-text="t('system.config.baseInfo.update')"
      :ok-loading="baseModalLoading"
      title-align="start"
      class="ms-modal-form"
      :on-before-ok="updateBaseInfo"
      @cancel="baseInfoCancel"
    >
      <a-form ref="baseFormRef" :model="baseInfoForm" layout="vertical">
        <a-form-item
          :label="t('system.config.pageUrl')"
          field="url"
          asterisk-position="end"
          :rules="[{ required: true, message: t('system.config.baseInfo.pageUrlRequired') }]"
          required
          class="mb-0"
        >
          <a-input
            v-model:model-value="baseInfoForm.url"
            :max-length="255"
            :placeholder="t('system.config.baseInfo.pageUrlPlaceholder')"
            allow-clear
          ></a-input>
          <MsFormItemSub :text="t('system.config.baseInfo.pageUrlSub', { url: defaultUrl })" @fill="fillDefaultUrl" />
        </a-form-item>
      </a-form>
    </a-modal>
    <MsDrawer
      v-model:visible="emailConfigDrawerVisible"
      :title="t('system.config.email.updateTitle')"
      :ok-text="t('system.config.email.update')"
      :ok-loading="emailDrawerLoading"
      :width="680"
      @confirm="updateEmailConfig"
      @cancel="emailConfigCancel"
    >
      <a-form ref="emailFormRef" :model="emailConfigForm" layout="vertical">
        <a-form-item
          :label="t('system.config.email.host')"
          field="host"
          asterisk-position="end"
          :rules="[{ required: true, message: t('system.config.email.hostRequired') }]"
          required
        >
          <a-input
            v-model:model-value="emailConfigForm.host"
            :max-length="255"
            :placeholder="t('system.config.email.hostPlaceholder')"
            class="w-[436px]"
            allow-clear
          ></a-input>
        </a-form-item>
        <a-form-item
          :label="t('system.config.email.port')"
          field="port"
          asterisk-position="end"
          :rules="[{ required: true, message: t('system.config.email.portRequired') }]"
          required
        >
          <a-input
            v-model:model-value="emailConfigForm.port"
            :max-length="255"
            :placeholder="t('system.config.email.portPlaceholder')"
            class="w-[240px]"
            allow-clear
          ></a-input>
        </a-form-item>
        <a-form-item :label="t('system.config.email.ssl')" field="ssl" asterisk-position="end">
          <a-switch v-model:model-value="emailConfigForm.ssl" type="line" />
          <MsFormItemSub :text="t('system.config.email.sslTip')" :show-fill-icon="false" />
        </a-form-item>
        <a-form-item :label="t('system.config.email.tsl')" field="tsl" asterisk-position="end">
          <a-switch v-model:model-value="emailConfigForm.tsl" type="line" />
          <MsFormItemSub :text="t('system.config.email.tslTip')" :show-fill-icon="false" />
        </a-form-item>
        <a-form-item
          :label="t('system.config.email.account')"
          field="account"
          asterisk-position="end"
          :rules="[{ required: true, message: t('system.config.email.accountRequired') }, emailRule]"
          required
        >
          <a-input
            v-model:model-value="emailConfigForm.account"
            :max-length="255"
            :placeholder="t('system.config.email.accountPlaceholder')"
            autocomplete="off"
            class="w-[436px]"
            allow-clear
          ></a-input>
        </a-form-item>
        <a-form-item :label="t('system.config.email.password')" field="password" asterisk-position="end">
          <a-input-password
            v-model:model-value="emailConfigForm.password"
            :max-length="64"
            :placeholder="t('system.config.email.passwordPlaceholder')"
            autocomplete="new-password"
            class="w-[436px]"
            allow-clear
          />
        </a-form-item>
        <a-form-item :label="t('system.config.email.from')" field="from" asterisk-position="end" :rules="[emailRule]">
          <a-input
            v-model:model-value="emailConfigForm.from"
            :max-length="255"
            :placeholder="t('system.config.email.fromPlaceholder')"
            class="w-[436px]"
            allow-clear
          ></a-input>
          <MsFormItemSub :text="t('system.config.email.fromTip')" :show-fill-icon="false" />
        </a-form-item>
        <a-form-item
          :label="t('system.config.email.recipient')"
          field="recipient"
          asterisk-position="end"
          :rules="[emailRule]"
        >
          <a-input
            v-model:model-value="emailConfigForm.recipient"
            :max-length="255"
            :placeholder="t('system.config.email.recipientPlaceholder')"
            class="w-[436px]"
            allow-clear
          ></a-input>
        </a-form-item>
      </a-form>
      <a-button type="outline" class="flex-1" :loading="drawerTestLoading" @click="testLink('drawer')">
        {{ t('system.config.email.test') }}
      </a-button>
    </MsDrawer>
  </div>
</template>

<script setup lang="ts">
  import { onBeforeMount, ref } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsDescription, { Description } from '@/components/pure/ms-description/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsFormItemSub from '@/components/business/ms-form-item-sub/index.vue';

  import {
    getBaseInfo,
    getEmailInfo,
    saveBaseInfo,
    saveEmailInfo,
    saveUploadConfig,
    testEmail,
  } from '@/api/modules/setting/config';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import useLicenseStore from '@/store/modules/setting/license';
  import { desensitize } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';
  import { validateEmail } from '@/utils/validate';

  import type { EmailConfig, TestEmailParams } from '@/models/setting/config';

  import type { FormInstance, ValidatedError } from '@arco-design/web-vue';

  const { t } = useI18n();
  const appStore = useAppStore();

  const baseLoading = ref(false);
  const baseModalLoading = ref(false);
  const baseInfoModalVisible = ref(false);
  const baseFormRef = ref<FormInstance>();
  const baseInfo = ref({
    url: 'http://127.0.0.1:8081',
  });
  const baseInfoForm = ref({ ...baseInfo.value });
  const baseInfoDesc = ref<Description[]>([]);
  // 默认示例
  const defaultUrl = 'https://metersphere.com';
  const fileSizeLimit = ref(50);

  function fillDefaultUrl() {
    baseInfoForm.value.url = defaultUrl;
  }

  /**
   * 初始化基础信息
   */
  const licenseStore = useLicenseStore();
  async function initBaseInfo() {
    try {
      baseLoading.value = true;
      const res = await getBaseInfo();
      baseInfo.value = { ...res };
      baseInfoForm.value = { ...res };
      if (licenseStore.hasLicense()) {
        baseInfoDesc.value = [
          {
            label: t('system.config.pageUrl'),
            value: res.url,
          },
        ];
      } else {
        baseInfoDesc.value = [
          {
            label: t('system.config.pageUrl'),
            value: res.url,
          },
        ];
      }
      fileSizeLimit.value = Number(res.fileMaxSize);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      baseLoading.value = false;
    }
  }

  /**
   * 拼接基础信息参数
   */
  function makeBaseInfoParams() {
    const { url } = baseInfoForm.value;
    return [{ paramKey: 'base.url', paramValue: url, type: 'text' }];
  }

  /**
   * 保存基础信息
   */
  function updateBaseInfo(done: (closed: boolean) => void) {
    baseFormRef.value?.validate(async (errors: Record<string, ValidatedError> | undefined) => {
      if (!errors) {
        try {
          baseModalLoading.value = true;
          await saveBaseInfo(makeBaseInfoParams());
          Message.success(t('system.config.baseInfo.updateSuccess'));
          done(true);
          initBaseInfo();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
          done(false);
        } finally {
          baseModalLoading.value = false;
        }
      }
    });
  }

  function baseInfoCancel() {
    baseFormRef.value?.resetFields();
    baseInfoForm.value = { ...baseInfo.value };
  }

  const emailLoading = ref(false);
  const emailDrawerLoading = ref(false);
  const testLoading = ref(false);
  const drawerTestLoading = ref(false);
  const emailConfigDrawerVisible = ref(false);
  const emailConfig = ref({
    host: '', // 主机
    port: '', // 端口
    account: '', // 账户
    from: '', // 发件人
    password: '', // 密码
    ssl: false, // 是否开启ssl
    tsl: false, // 是否开启tsl
    recipient: '', // 收件人
  });
  const emailConfigForm = ref({ ...emailConfig.value });
  const emailFormRef = ref<FormInstance>();
  const emailInfoDesc = ref<Description[]>([]);

  const pswInVisible = ref(false); // 是否展示未脱敏密码

  function togglePswVisible() {
    pswInVisible.value = !pswInVisible.value;
  }

  const emailRule = {
    validator: (value: string | undefined, callback: (error?: string) => void) => {
      if (value && !validateEmail(value)) {
        callback(t('system.config.email.emailErrTip'));
      }
    },
  };

  /**
   * 初始化邮箱信息
   */
  async function initEmailInfo() {
    try {
      emailLoading.value = true;
      const res = await getEmailInfo();
      const ssl = res.ssl === 'true';
      const tsl = res.tsl === 'true';
      emailConfig.value = { ...res, ssl, tsl };
      emailConfigForm.value = { ...res, ssl, tsl };
      const { host, port, account, password, from, recipient } = res;
      emailInfoDesc.value = [
        {
          label: t('system.config.email.host'),
          value: host,
        },
        {
          label: t('system.config.email.port'),
          value: port,
        },
        {
          label: t('system.config.email.account'),
          value: account,
        },
        {
          label: t('system.config.email.password'),
          value: password,
          key: 'password',
        },
        {
          label: t('system.config.email.from'),
          value: from,
        },
        {
          label: t('system.config.email.recipient'),
          value: recipient,
        },
        {
          label: t('system.config.email.ssl'),
          value: ssl.toString(),
          key: 'ssl',
        },
        {
          label: t('system.config.email.tsl'),
          value: tsl.toString(),
          key: 'tsl',
        },
      ];
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      emailLoading.value = false;
    }
  }

  /**
   * 拼接邮箱信息参数
   */
  function makeEmailParams() {
    const { host, port, account, password, from, recipient, ssl, tsl } = emailConfigForm.value;
    return [
      { paramKey: 'smtp.host', paramValue: host, type: 'text' },
      { paramKey: 'smtp.port', paramValue: port, type: 'text' },
      { paramKey: 'smtp.account', paramValue: account, type: 'text' },
      { paramKey: 'smtp.password', paramValue: password, type: 'password' },
      { paramKey: 'smtp.from', paramValue: from, type: 'text' },
      { paramKey: 'smtp.recipient', paramValue: recipient, type: 'text' },
      { paramKey: 'smtp.ssl', paramValue: ssl?.toString(), type: 'text' },
      { paramKey: 'smtp.tsl', paramValue: tsl?.toString(), type: 'text' },
    ];
  }

  /**
   * 保存邮箱信息
   */
  function updateEmailConfig() {
    emailFormRef.value?.validate(async (errors: Record<string, ValidatedError> | undefined) => {
      if (!errors) {
        try {
          emailDrawerLoading.value = true;
          await saveEmailInfo(makeEmailParams());
          Message.success(t('system.config.email.updateSuccess'));
          emailConfigDrawerVisible.value = false;
          initEmailInfo();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          emailDrawerLoading.value = false;
        }
      }
    });
  }

  function emailConfigCancel() {
    emailFormRef.value?.resetFields();
    emailConfigForm.value = { ...emailConfig.value };
  }

  /**
   * 拼接测试邮箱参数
   * @param form 抽屉中的表单/页面中的表单
   */
  function makeEmailTestParams(form: EmailConfig) {
    const { host, port, account, password, from, recipient, ssl, tsl } = form;
    return {
      'smtp.host': host,
      'smtp.port': port,
      'smtp.account': account,
      'smtp.password': password,
      'smtp.from': from,
      'smtp.ssl': ssl,
      'smtp.tsl': tsl,
      'smtp.recipient': recipient,
    };
  }

  /**
   * 测试邮箱
   * @param emailInfo 来源于抽屉/页面
   */
  async function testLink(emailInfo: 'page' | 'drawer') {
    try {
      let params = {} as TestEmailParams;
      if (emailInfo === 'drawer') {
        let validError = false;
        await emailFormRef.value?.validate((errors: Record<string, ValidatedError> | undefined) => {
          if (!errors) {
            drawerTestLoading.value = true;
            params = makeEmailTestParams({
              ...emailConfigForm.value,
              ssl: emailConfigForm.value.ssl?.toString(),
              tsl: emailConfigForm.value.tsl?.toString(),
            });
          } else {
            validError = true;
          }
        });
        if (validError) return;
      } else {
        testLoading.value = true;
        params = makeEmailTestParams({
          ...emailConfig.value,
          ssl: emailConfig.value.ssl?.toString(),
          tsl: emailConfig.value.tsl?.toString(),
        });
      }
      await testEmail(params);
      Message.success(t('system.config.email.testSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      testLoading.value = false;
      drawerTestLoading.value = false;
    }
  }

  const fileSizeLimitLoading = ref(false);

  async function saveFileSizeLimitConfig() {
    try {
      fileSizeLimitLoading.value = true;
      if (fileSizeLimit.value === undefined || fileSizeLimit.value === null || fileSizeLimit.value === 0) {
        fileSizeLimit.value = 1024;
      }
      await saveUploadConfig([
        {
          paramKey: 'upload.file.size',
          paramValue: fileSizeLimit.value.toString(),
          type: 'text',
        },
      ]);
      Message.success(t('common.updateSuccess'));
      appStore.setFileMaxSize(fileSizeLimit.value);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      fileSizeLimitLoading.value = false;
    }
  }

  onBeforeMount(() => {
    initBaseInfo();
    initEmailInfo();
  });
</script>

<style lang="less" scoped>
  :deep(.no-bottom) {
    .arco-descriptions-item-label,
    .arco-descriptions-item-value {
      @apply pb-0;
    }
  }
  .card-title {
    @apply flex items-center;

    gap: 8px;
    .card-title-line {
      width: 3px;
      height: 14px;
      border-radius: var(--border-radius-small);
      background: rgb(var(--primary-4));
    }
    .card-title-text {
      @apply flex items-center;

      gap: 2px;
      color: var(--color-text-1);
    }
  }
</style>
