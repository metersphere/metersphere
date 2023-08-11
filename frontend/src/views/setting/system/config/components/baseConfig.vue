<template>
  <div>
    <MsCard class="mb-[16px]" :loading="baseloading" simple auto-height>
      <div class="mb-[16px] flex justify-between">
        <div class="text-[var(--color-text-000)]">{{ t('system.config.baseInfo') }}</div>
        <a-button type="outline" size="mini" @click="baseInfoDrawerVisible = true">
          {{ t('system.config.update') }}
        </a-button>
      </div>
      <MsDescription :descriptions="baseInfoDescs" class="no-bottom" :column="2" />
    </MsCard>
    <MsCard class="mb-[16px]" :loading="emailLoading" simple auto-height>
      <div class="mb-[16px] flex justify-between">
        <div class="text-[var(--color-text-000)]">{{ t('system.config.emailConfig') }}</div>
        <a-button type="outline" size="mini" @click="emailConfigDrawerVisible = true">
          {{ t('system.config.update') }}
        </a-button>
      </div>
      <MsDescription :descriptions="emailInfoDescs" :column="2">
        <template #value="{ item }">
          <div v-if="item.key && ['ssl', 'tsl'].includes(item.key)">
            <div v-if="item.value === 'true'" class="flex items-center">
              <icon-check-circle-fill class="mr-[8px] text-[rgb(var(--success-6))]" />{{
                t('system.config.email.open')
              }}
            </div>
            <div v-else class="flex items-center">
              <MsIcon type="icon-icon_disable" class="mr-[4px] text-[var(--color-text-4)]" />
              {{ t('system.config.email.close') }}
            </div>
          </div>
          <div v-else-if="item.key === 'password' && item.value?.toString() !== ''">
            <span v-if="pswInVisible">
              {{ item.value }}
              <icon-eye class="cursor-pointer text-[var(--color-text-4)]" @click="togglePswVisible" />
            </span>
            <span v-else>
              {{ desensitize(item.value as string) }}
              <icon-eye-invisible class="cursor-pointer text-[var(--color-text-4)]" @click="togglePswVisible" />
            </span>
          </div>
          <div v-else>{{ item.value?.toString() === '' ? '-' : item.value }}</div>
        </template>
      </MsDescription>
      <a-button
        type="outline"
        size="mini"
        class="arco-btn-outline--secondary"
        :loading="testLoading"
        @click="testLink('page')"
      >
        {{ t('system.config.email.test') }}
      </a-button>
    </MsCard>
    <MsDrawer
      v-model:visible="baseInfoDrawerVisible"
      :title="t('system.config.baseInfo.updateTitle')"
      :ok-text="t('system.config.baseInfo.update')"
      :ok-loading="baseDrawerLoading"
      :width="680"
      @confirm="updateBaseInfo"
      @cancel="baseInfoCancel"
    >
      <a-form ref="baseFormRef" :model="baseInfoForm" layout="vertical">
        <a-form-item
          :label="t('system.config.pageUrl')"
          field="url"
          asterisk-position="end"
          :rules="[{ required: true, message: t('system.config.baseInfo.pageUrlRequired') }]"
          required
        >
          <a-input
            v-model:model-value="baseInfoForm.url"
            :max-length="250"
            :placeholder="t('system.config.baseInfo.pageUrlPlaceholder')"
            allow-clear
          ></a-input>
          <MsFormItemSub :text="t('system.config.baseInfo.pageUrlSub', { url: defaultUrl })" @fill="fillDefaultUrl" />
        </a-form-item>
        <a-form-item :label="t('system.config.prometheus')" field="prometheusHost" asterisk-position="end">
          <a-input
            v-model:model-value="baseInfoForm.prometheusHost"
            :max-length="250"
            :placeholder="t('system.config.baseInfo.prometheusPlaceholder')"
            allow-clear
          ></a-input>
          <MsFormItemSub
            :text="t('system.config.baseInfo.prometheusSub', { prometheus: defaultPrometheus })"
            @fill="fillDefaultPrometheus"
          />
        </a-form-item>
      </a-form>
    </MsDrawer>
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
            :max-length="250"
            :placeholder="t('system.config.email.hostPlaceholder')"
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
            :max-length="250"
            :placeholder="t('system.config.email.portPlaceholder')"
            allow-clear
          ></a-input>
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
            :max-length="250"
            :placeholder="t('system.config.email.accountPlaceholder')"
            autocomplete="off"
            allow-clear
          ></a-input>
        </a-form-item>
        <a-form-item :label="t('system.config.email.password')" field="password" asterisk-position="end">
          <a-input-password
            v-model:model-value="emailConfigForm.password"
            :max-length="250"
            :placeholder="t('system.config.email.passwordPlaceholder')"
            autocomplete="off"
            allow-clear
          ></a-input-password>
        </a-form-item>
        <a-form-item :label="t('system.config.email.from')" field="from" asterisk-position="end" :rules="[emailRule]">
          <a-input
            v-model:model-value="emailConfigForm.from"
            :max-length="250"
            :placeholder="t('system.config.email.fromPlaceholder')"
            allow-clear
          ></a-input>
        </a-form-item>
        <a-form-item
          :label="t('system.config.email.recipient')"
          field="recipient"
          asterisk-position="end"
          :rules="[emailRule]"
        >
          <a-input
            v-model:model-value="emailConfigForm.recipient"
            :max-length="250"
            :placeholder="t('system.config.email.recipientPlaceholder')"
            allow-clear
          ></a-input>
        </a-form-item>
        <a-form-item :label="t('system.config.email.ssl')" field="ssl" asterisk-position="end">
          <a-switch v-model:model-value="emailConfigForm.ssl" />
          <MsFormItemSub :text="t('system.config.email.sslTip')" :show-fill-icon="false" />
        </a-form-item>
        <a-form-item :label="t('system.config.email.tsl')" field="tsl" asterisk-position="end">
          <a-switch v-model:model-value="emailConfigForm.tsl" />
          <MsFormItemSub :text="t('system.config.email.tslTip')" :show-fill-icon="false" />
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
  import { useI18n } from '@/hooks/useI18n';
  import { desensitize } from '@/utils';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsDescription, { Description } from '@/components/pure/ms-description/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsFormItemSub from '@/components/bussiness/ms-form-item-sub/index.vue';
  import { validateEmail } from '@/utils/validate';
  import { testEmail, saveBaseInfo, saveEmailInfo, getBaseInfo, getEmailInfo } from '@/api/modules/setting/config';

  import type { EmailConfig, TestEmailParams } from '@/models/setting/config';
  import type { FormInstance, ValidatedError } from '@arco-design/web-vue';

  const { t } = useI18n();

  const baseloading = ref(false);
  const baseDrawerLoading = ref(false);
  const baseInfoDrawerVisible = ref(false);
  const baseFormRef = ref<FormInstance>();
  const baseInfo = ref({
    url: 'http://127.0.0.1:8081',
    prometheusHost: 'http://ms-prometheus:9090',
  });
  const baseInfoForm = ref({ ...baseInfo.value });
  const baseInfoDescs = ref<Description[]>([]);
  // 默认示例
  const defaultUrl = 'https://metersphere.com';
  const defaultPrometheus = 'http://ms-prometheus:9090';

  function fillDefaultUrl() {
    baseInfoForm.value.url = defaultUrl;
  }

  function fillDefaultPrometheus() {
    baseInfoForm.value.prometheusHost = defaultPrometheus;
  }

  /**
   * 初始化基础信息
   */
  async function initBaseInfo() {
    try {
      baseloading.value = true;
      const res = await getBaseInfo();
      baseInfo.value = { ...res };
      baseInfoForm.value = { ...res };
      baseInfoDescs.value = [
        {
          label: t('system.config.pageUrl'),
          value: res.url,
        },
        {
          label: t('system.config.prometheus'),
          value: res.prometheusHost,
        },
      ];
    } catch (error) {
      console.log(error);
    } finally {
      baseloading.value = false;
    }
  }

  /**
   * 拼接基础信息参数
   */
  function makeBaseInfoParams() {
    const { url, prometheusHost } = baseInfoForm.value;
    return [
      { paramKey: 'base.url', paramValue: url, type: 'text' },
      { paramKey: 'base.prometheus.host', paramValue: prometheusHost, type: 'text' },
    ];
  }

  /**
   * 保存基础信息
   */
  function updateBaseInfo() {
    baseFormRef.value?.validate(async (errors: Record<string, ValidatedError> | undefined) => {
      if (!errors) {
        try {
          baseDrawerLoading.value = true;
          await saveBaseInfo(makeBaseInfoParams());
          Message.success(t('system.config.baseInfo.updateSuccess'));
          baseInfoDrawerVisible.value = false;
          initBaseInfo();
        } catch (error) {
          console.log(error);
        } finally {
          baseDrawerLoading.value = false;
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
  const emailInfoDescs = ref<Description[]>([]);

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
      const _ssl = Boolean(res.ssl);
      const _tsl = Boolean(res.tsl);
      emailConfig.value = { ...res, ssl: _ssl, tsl: _tsl };
      emailConfigForm.value = { ...res, ssl: _ssl, tsl: _tsl };
      const { host, port, account, password, from, recipient, ssl, tsl } = res;
      emailInfoDescs.value = [
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
          value: ssl,
          key: 'ssl',
        },
        {
          label: t('system.config.email.tsl'),
          value: tsl,
          key: 'tsl',
        },
      ];
    } catch (error) {
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
      { paramKey: 'smtp.password', paramValue: password, type: 'text' },
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
        drawerTestLoading.value = true;
        params = makeEmailTestParams({
          ...emailConfigForm.value,
          ssl: emailConfigForm.value.ssl?.toString(),
          tsl: emailConfigForm.value.tsl?.toString(),
        });
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
      console.log(error);
    } finally {
      testLoading.value = false;
      drawerTestLoading.value = false;
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
</style>
