<template>
  <a-radio-group v-if="activeName != ''" v-model="activeName" type="button" class="tabPlatform" @change="handleClick">
    <a-radio
      v-for="item of orgOptions"
      :key="item.value"
      :value="item.value"
      :v-show="item.label"
      class="radioOneButton"
    >
      {{ t('project.messageManagement.' + item.value) }}
    </a-radio>
    <!--    <a-tab-pane key="lark" :title="t('project.messageManagement.LARK')"></a-tab-pane>
    <a-tab-pane key="larksuite" :title="t('project.messageManagement.LARK_SUITE')"></a-tab-pane>-->
  </a-radio-group>
  <div v-if="activeName === 'WE_COM'" class="login-qrcode">
    <div class="qrcode">
      <wecom-qr v-if="activeName === 'WE_COM'" />
    </div>
  </div>
  <div v-if="activeName === 'DING_TALK'" class="login-qrcode">
    <div class="qrcode">
      <div class="title">
        <MsIcon type="icon-logo_dingtalk" size="24"></MsIcon>
        钉钉登录
      </div>
      <ding-talk-qr v-if="activeName === 'DING_TALK'" />
    </div>
  </div>
  <div v-if="activeName === 'LARK'" class="login-qrcode">
    <div class="qrcode">
      <div class="title">
        <MsIcon type="icon-logo_lark" size="24"></MsIcon>
        飞书登录
      </div>
      <lark-qr-code v-if="activeName === 'LARK'" />
    </div>
  </div>
  <div v-if="activeName === 'LARK_SUITE'" class="login-qrcode">
    <div class="qrcode">
      <div class="title">
        <MsIcon type="icon-logo_lark" size="24"></MsIcon>
        国际飞书登录
      </div>
      <lark-suite-qr-code v-if="activeName === 'LARK_SUITE'" />
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { useI18n } from 'vue-i18n';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import dingTalkQr from './dingTalkQrCode.vue';
  import WecomQr from './weComQrCode.vue';
  import LarkQrCode from '@/views/login/components/larkQrCode.vue';
  import LarkSuiteQrCode from '@/views/login/components/larkSuiteQrCode.vue';

  import { getPlatformParamUrl } from '@/api/modules/user';

  const { t } = useI18n();

  const activeName = ref('');

  interface qrOption {
    value: string;
    label: string;
  }

  const orgOptions = ref<qrOption[]>([]);
  const props = defineProps<{
    tabName: string | number | boolean | Record<string, unknown> | undefined;
  }>();
  function handleClick(val: string | number | boolean) {
    if (typeof val === 'string') {
      activeName.value = val;
    }
  }
  const initActive = () => {
    for (let i = 0; i < orgOptions.value.length; i++) {
      const key = orgOptions.value[i].value;
      if (props.tabName === key) {
        nextTick(() => {
          handleClick(key);
        });
        break;
      }
    }
  };
  async function initPlatformInfo() {
    try {
      const res = await getPlatformParamUrl();
      orgOptions.value = res.map((e) => ({
        label: e.name,
        value: e.id,
      }));
      initActive();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }
  onMounted(() => {
    initPlatformInfo();
  });
</script>

<style lang="less" scoped>
  .tabPlatform {
    min-width: 480px;
    height: 40px;
  }
  .login-qrcode {
    display: flex;
    align-items: center;
    margin-top: 24px;
    min-width: 480px;
    flex-direction: column;
    .qrcode {
      display: flex;
      justify-content: center;
      align-items: center;
      overflow: hidden;
      border-radius: 8px;
      background: #ffffff;
      flex-direction: column;
    }
    .title {
      display: flex;
      justify-content: center;
      align-items: center;
      overflow: hidden;
      font-size: 18px;
      font-weight: 500;
      font-style: normal;
      line-height: 26px;
      margin-bottom: -24px;
      z-index: 100000;
      .ed-icon {
        margin-right: 8px;
        font-size: 24px;
      }
    }
  }
  .radioOneButton {
    width: 100%;
    display: flex;
    flex-direction: row;
    flex-wrap: nowrap;
    align-items: center;
    font-size: 16px;
    justify-content: center;
  }
</style>
