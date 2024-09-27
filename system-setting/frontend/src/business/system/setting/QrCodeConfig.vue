<template>
  <div class="card_dashboard">
    <div v-for="item in filterList" :key="item.key" class="item">
        <div style="display: flex">
                <span>
                    <img class="ms-icon" :src="item.logo" alt="">
                </span>
          <div class="card_dashboard_top">
            <p>
              <span style="margin-right: 4px; font-weight: 600;">{{ item.title }}</span>
              <span v-if="!item.hasConfig" class="ms-enable">{{ $t('qrcode.service_unconfigured') }}</span>
              <span
                v-else
                class="ms-enable active"
                :style="{
                        background: 'rgb(242, 252, 247)',
                        color: 'rgb(0, 194, 97)',
                      }"
              >{{ $t('qrcode.service_configured') }}</span
              >
            </p>
            <p class="card-description">{{ item.description }}</p>
          </div>
        </div>
      <div class="card_dashboard_bottom">
        <div class="card_dashboard_bottom_left">
          <el-tooltip style="margin-right: 8px"  v-if="!item.hasConfig" :content="$t('qrcode.service_unconfiguredTip')" position="tl">
                    <span>
                      <el-button
                          v-if="!item.hasConfig"
                          type="outline"
                          class="arco-btn-outline--secondary"
                          size="mini"
                          :disabled="
                          !item.hasConfig || !checkPermission('SYSTEM_SETTING:READ+EDIT')
                        "
                          @click="getValidateHandler(item.key)"
                      >{{ $t('qrcode.service_testLink') }}</el-button></span>
          </el-tooltip>
          <el-button
              v-else
              :disabled="!item.hasConfig || !checkPermission('SYSTEM_SETTING:READ+EDIT')"
              size="mini"
              @click="getValidateHandler(item.key)"
          >{{ $t('qrcode.service_testLink') }}
          </el-button>
          <el-button
              v-if="item.edit"
              v-permission="['SYSTEM_SETTING:READ+EDIT']"
              size="mini"
              @click="editHandler(item.key)"
          >{{ $t('qrcode.service_edit') }}
          </el-button>
          <el-button
              v-else
              v-permission="['SYSTEM_SETTING:READ+EDIT']"
              size="mini"
              @click="editHandler(item.key)"
          >{{ $t('commons.add') }}
          </el-button>
        </div>
        <span>
            <el-tooltip v-if="!item.valid" :content="$t('qrcode.service_unconfiguredTip')" position="br">
              <span
              ><el-switch
                  v-model="item.enable"
                  size="small"
                  :disabled="true"
                  @change="(v) => changeStatus(v, item.key)"
              /></span>
            </el-tooltip>
            <el-switch
                v-else
                v-model="item.enable"
                size="small"
                :disabled="!checkPermission('SYSTEM_SETTING:READ+EDIT')"
                @change="(v) => changeStatus(v, item.key)"
            />
          </span>
      </div>
    </div>
    <we-com-modal :visible="showWeComModal" @success="loadList()"  @update="updateShowModal()"/>
    <ding-talk-modal :visible="showDingTalkModal" @success="loadList()" @update="updateShowModal()"/>
    <lark-modal :visible="showLarkModal" @success="loadList()" @update="updateShowModal()"/>
    <lark-suite-modal :visible="showLarkSuiteModal" @success="loadList()" @update="updateShowModal()"/>
  </div>
</template>

<script>
import {
    closeValidateDingTalk, closeValidateLark, closeValidateLarkSuite,
    closeValidateWeCom, enableDingTalk, enableLark, enableLarkSuite, enableWeCom,
    getPlatformSourceList,
    validateDingTalkConfig,
    validateLarkConfig,
    validateLarkSuiteConfig,
    validateWeComConfig
} from "@/api/qrcode";
import WeComModal from "@/business/system/setting/weComModal.vue";
import DingTalkModal from "@/business/system/setting/dingTalkModal.vue";
import LarkModal from "@/business/system/setting/larkModal.vue";
import LarkSuiteModal from "@/business/system/setting/larkSuiteModal.vue";


export default {
  name: "QrCodeConfig",
  components: {
    LarkSuiteModal,
    LarkModal,
    DingTalkModal,
    WeComModal
  },
  data() {
    return {
      filterList:[
        {
          key: 'WE_COM',
          title: this.$t('qrcode.service_WE_COM'),
          description: '为企业打造的专业办公管理工具',
          enable: false,
          valid: false,
          logo: '/assets/logo_wechat-work.svg',
          edit: false,
          hasConfig: false,
        },
        {
          key: 'DING_TALK',
          title: this.$t('qrcode.service_DING_TALK'),
          description: '企业级智能移动办公平台',
          enable: false,
          valid: false,
          logo: '/assets/logo_dingtalk.svg',
          edit: false,
          hasConfig: false,
        },
        {
          key: 'LARK',
          title: this.$t('qrcode.service_LARK'),
          description: '先进企业合作与管理平台',
          enable: false,
          valid: false,
          logo: '/assets/logo_lark.svg',
          edit: false,
          hasConfig: false,
        },
        {
          key: 'LARK_SUITE',
          title: this.$t('qrcode.service_LARK_SUITE'),
          description: '先进企业合作与管理平台',
          enable: false,
          valid: false,
          logo: '/assets/logo_lark.svg',
          edit: false,
          hasConfig: false,
        },
      ],
      loading:false,
      showWeComModal:false,
      showDingTalkModal:false,
      showLarkModal:false,
      showLarkSuiteModal:false,
      weComInfo:{
        corpId: '',
        agentId: '',
        appSecret: '',
        callBack: '',
        enable: false,
        valid: false,
      },
      dingTalkInfo:{
        agentId: '',
        appKey: '',
        appSecret: '',
        callBack: '',
        enable: false,
        valid: false,
      },
      larkInfo:{
        agentId: '',
        appSecret: '',
        callBack: '',
        enable: false,
        valid: false,
      },
      platformData:[]
    }
  },
  created() {
    this.loadList();
  },
  methods: {
    loadList() {
      this.loading = getPlatformSourceList().then(res => {
        this.platformData = res.data;
        this.filterList.forEach((filterKey) => {
              this.platformData.forEach((dataKey) => {
                  if (filterKey.key === dataKey.platform) {
                      filterKey.enable = dataKey.enable;
                      filterKey.valid = dataKey.valid;
                      filterKey.edit = true;
                      filterKey.hasConfig = dataKey.hasConfig;
                  }
              });
          });
      });
    },
    // 添加或配置
    editHandler(key) {
      console.log(key)
      if (key === 'WE_COM') {
        this.showWeComModal = true;
        this.showDingTalkModal = false;
        this.showLarkModal = false;
        this.showLarkSuiteModal = false;
      } else if (key === 'DING_TALK') {
        this.showWeComModal = false;
        this.showDingTalkModal = true;
        this.showLarkModal = false;
        this.showLarkSuiteModal = false;
      } else if (key === 'LARK') {
        this.showWeComModal = false;
        this.showDingTalkModal = false;
        this.showLarkModal = true;
        this.showLarkSuiteModal = false;
      } else if (key === 'LARK_SUITE') {
        this.showWeComModal = false;
        this.showDingTalkModal = false;
        this.showLarkModal = false;
        this.showLarkSuiteModal = true;
      }
    },

    // 外部测试连接
    async getValidateHandler(key) {
      this.loading = true;
      try {
        if (key === 'WE_COM') {
          validateWeComConfig(this.weComInfo).then(res => {
            this.$message.success(this.$t('qrcode.service_testLinkStatusTip'));
          }).catch(e => {
            this.$message.error(this.$t('qrcode.service_testLinkStatusErrorTip'));
          });
        } else if (key === 'DING_TALK') {
          validateDingTalkConfig(this.dingTalkInfo).then(res => {
            this.$message.success(this.$t('qrcode.service_testLinkStatusTip'));
          }).catch(e => {
            this.$message.error(this.$t('qrcode.service_testLinkStatusErrorTip'));
          });
        } else if (key === 'LARK') {
          validateLarkConfig(this.larkInfo).then(res => {
            this.$message.success(this.$t('qrcode.service_testLinkStatusTip'));
          }).catch(e => {
            this.$message.error(this.$t('qrcode.service_testLinkStatusErrorTip'));
          });
        } else if (key === 'LARK_SUITE') {
          await validateLarkSuiteConfig(this.larkInfo).then(res => {
            this.$message.success(this.$t('qrcode.service_testLinkStatusTip'));
          }).catch(e => {
            this.$message.error(this.$t('qrcode.service_testLinkStatusErrorTip'));
          });
        }
      } catch (error) {
        if (key === 'WE_COM') {
          await closeValidateWeCom();
        } else if (key === 'DING_TALK') {
          await closeValidateDingTalk();
        } else if (key === 'LARK') {
          await closeValidateLark();
        } else if (key === 'LARK_SUITE') {
          await closeValidateLarkSuite();
        }
        // eslint-disable-next-line no-console
        console.log(error);
      } finally {
        this.loadList();
        this.loading = false;
      }
    },
    checkPermission(key){
        return true;
    },
    // 切换状态
    async changeStatus(value, key) {
      this.loading = true;
      const message = value ? 'qrcode.service_enableSuccess' : 'qrcode.service_closeSuccess';
      let params = {
        enable: typeof value === 'boolean' ? value : false,
      };
      try {
        if (key === 'WE_COM') {
          await enableWeCom(params).then(res => {
            this.$message.success(this.$t(message));
          });
        } else if (key === 'DING_TALK') {
          await enableDingTalk(params).then(res => {
            this.$message.success(this.$t(message));
          });
        } else if (key === 'LARK') {
          await enableLark(params).then(res => {
            this.$message.success(this.$t(message));
          });
        } else if (key === 'LARK_SUITE') {
          await enableLarkSuite(params).then(res => {
            this.$message.success(this.$t(message));
          });
        }
        this.loadList();
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      } finally {
        this.loading = false;
      }
    },
    updateShowModal(){
      this.showWeComModal = false;
      this.showDingTalkModal = false;
      this.showLarkModal = false;
      this.showLarkSuiteModal = false;
    }
  }
}
</script>

<style scoped>
.card_dashboard{
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-start;
  align-content: flex-start;
  width: 100%;
  background-color: #f9f9fe;
}
.item{
  margin: 8px;
  padding: 20px 29px;
  height: 144px;
  border-radius: 4px;
  background: #fff;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  width: 20%;
}
.ms-enable {
  font-size: 12px;
  border-radius: 4px;
  background: #fff;
}
.card_dashboard_top{
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}
.card_dashboard_bottom{
  display: flex;
  justify-content: space-between;
}
.card_dashboard_bottom_left{
  display: flex;
  justify-content: flex-start;
}
.card-description{
  margin-top: 2px;
  color: #959598;
  font-size: .875rem;
  line-height: 1.25rem;
}
.ms-icon{
  float: left;
  margin-right: 2px;
  height: 40px;
  width: 40px;
  margin-top: 11px;
}

</style>
