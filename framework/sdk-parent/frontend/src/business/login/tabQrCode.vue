<template>
  <div class="tab_qrcode">
    <el-tabs v-model="activeName" type="border-card" class="tabPlatform" @tab-click="handleClick">
      <el-tab-pane
          v-for="item of orgOptions"
          :key="item.value"
          :label="$t('qrcode.service_' + item.value)"
          :name="item.value"
          class="radioOneButton"
      >
      </el-tab-pane>
    </el-tabs>
    <div v-if="activeName === 'WE_COM'" class="login-qrcode">
      <div class="qrcode">
        <wecom-qr v-if="activeName === 'WE_COM'" />
      </div>
    </div>
    <div v-if="activeName === 'DING_TALK'" class="login-qrcode">
      <div class="qrcode">
        <div class="title">
          <img class="ms-icon" src="/assets/logo_dingtalk.svg" alt="">
          钉钉登录
        </div>
        <ding-talk-qr v-if="activeName === 'DING_TALK'" />
      </div>
    </div>
    <div v-if="activeName === 'LARK'" class="login-qrcode">
      <div class="qrcode">
        <div class="title">
          <img class="ms-icon" src="/assets/logo_lark.svg" alt="">
          飞书登录
        </div>
        <lark-qr-code v-if="activeName === 'LARK'" />
      </div>
    </div>
    <div v-if="activeName === 'LARK_SUITE'" class="login-qrcode">
      <div class="qrcode">
        <div class="title">
          <img class="ms-icon" src="/assets/logo_lark.svg" alt="">
          国际飞书登录
        </div>
        <lark-suite-qr-code v-if="activeName === 'LARK_SUITE'" />
      </div>
    </div>
  </div>
</template>

<script>
import dingTalkQr from './dingTalkQrCode.vue';
import WecomQr from './weComQrCode.vue';
import LarkQrCode from './larkQrCode.vue';
import LarkSuiteQrCode from './larkSuiteQrCode.vue';
import {getPlatformParamUrl} from "../../api/qrcode";


export default {
  name: "tabQrCode",
  props:{
    tabName: {
      type: String
    }
  },
  components: {
    dingTalkQr,
    WecomQr,
    LarkQrCode,
    LarkSuiteQrCode
  },
  data() {
    return {
      activeName:'',
      orgOptions:[],
    }
  },
  methods:{
    handleClick(val) {
      if (typeof val === 'string') {
        this.activeName = val;
      }
    },
    initActive() {
      for (let i = 0; i < this.orgOptions.length; i++) {
        const key = this.orgOptions[i].value;
        if (this.tabName === key) {
          this.$nextTick(() => {
            this.handleClick(key);
          });
          break;
        }
      }
    },
    async initPlatformInfo() {
      try {
        await getPlatformParamUrl().then(res => {
          this.orgOptions = res.data.map((e) => ({
            label: e.name,
            value: e.id,
          }));
        });
        this.initActive();
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      }
    }
  },
  created() {
    this.initPlatformInfo();
  }
}
</script>

<style scoped>
.tab_qrcode{
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  justify-content: space-around;
}
:deep(.el-tabs__nav){
  width: 100%;
  display: flex;
}
:deep(.el-tabs__item ){
  width: 100%;
  margin-left: 1px !important;
  margin-top: 0px !important;
}

.ms-icon{
  height: 24px;
  width: 24px;
}
.tabPlatform {
  min-width: 480px;
  height: 40px;
  box-shadow: 0 0px 0px 0 rgba(0, 0, 0, 0), 0 0 0px 0 rgba(0, 0, 0, 0);
}
.login-qrcode {
  display: flex;
  align-items: center;
  margin-top: 24px;
  max-width: 480px;
  flex-direction: column;
  .qrcode {
    display: flex;
    justify-content: center;
    align-items: center;
    overflow: hidden;
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
  }
}
.radioOneButton{
  width: 100%;
  display: flex;
  flex-direction: row;
  flex-wrap: nowrap;
  align-items: center;
  font-size: 16px;
  justify-content: center;
}
</style>
