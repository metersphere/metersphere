<template>
  <div id="wecom-qr" class="wecom-qr">
  </div>
</template>

<script>

  import * as ww from '@wecom/jssdk';
  import { WWLoginPanelSizeType, WWLoginRedirectType, WWLoginType } from '@wecom/jssdk';
  import {useUserStore} from "@/store";
  import {getWeComCallback, getWeComInfo} from "../../api/qrcode";
  import {getCurrentUserId} from "../../utils/token";
  import {getLanguage} from "../../api/user";
  import {setLanguage} from "@/i18n";
  import {hasPermissions} from "../../utils/permission";

  export default {
    name: "weComQrCode",
    data() {
      return {
        wwLogin: {},
        orgOptions:[],
        isWeComLogin: false,
        lastUser: sessionStorage.getItem('lastUser'),
        otherQuery: {},
      }
    },
    methods:{
      checkRedirectUrl() {
        if (this.lastUser === getCurrentUserId()) {
          this.$router.push({path: sessionStorage.getItem('redirectUrl') || '/'});
          return;
        }
        let redirectUrl = '/';
        if (hasPermissions('PROJECT_USER:READ', 'PROJECT_ENVIRONMENT:READ', 'PROJECT_OPERATING_LOG:READ', 'PROJECT_FILE:READ+JAR', 'PROJECT_FILE:READ+FILE', 'PROJECT_CUSTOM_CODE:READ', 'PROJECT_MESSAGE:READ', 'PROJECT_TEMPLATE:READ')) {
          redirectUrl = '/project/home';
        } else if (hasPermissions('WORKSPACE_SERVICE:READ', 'WORKSPACE_USER:READ', 'WORKSPACE_PROJECT_MANAGER:READ', 'WORKSPACE_PROJECT_ENVIRONMENT:READ', 'WORKSPACE_OPERATING_LOG:READ')) {
          redirectUrl = '/setting/project/:type';
        } else if (hasPermissions('SYSTEM_USER:READ', 'SYSTEM_WORKSPACE:READ', 'SYSTEM_GROUP:READ', 'SYSTEM_TEST_POOL:READ', 'SYSTEM_SETTING:READ', 'SYSTEM_AUTH:READ', 'SYSTEM_QUOTA:READ', 'SYSTEM_OPERATING_LOG:READ')) {
          redirectUrl = '/setting';
        } else {
          redirectUrl = '/';
        }

        sessionStorage.setItem('redirectUrl', redirectUrl);
        sessionStorage.setItem('lastUser', getCurrentUserId());
        this.$router.push({name: "login_redirect", path: redirectUrl || '/', query: this.otherQuery});
      },

      doLogin(weComCallback) {
        const userStore = useUserStore()
        // 删除缓存
        sessionStorage.removeItem('changePassword');
        userStore.getIsLogin()
            .then(res => {
              this.getLanguage(res.data.language);
              window.location.href = "/";
              sessionStorage.setItem('loginSuccess', 'true');
              sessionStorage.setItem('changePassword', weComCallback.message);
              localStorage.setItem('AuthenticateType', 'QRCODE');
            })
            .catch(data => {
              // 保存公钥
              localStorage.setItem("publicKey", data.message);
              let lang = localStorage.getItem("language");
              if (lang) {
                setLanguage(lang);
              }
              this.checkRedirectUrl()
            });
      },
      getLanguage(language) {
        if (!language) {
          getLanguage()
              .then(response => {
                language = response.data;
                localStorage.setItem('default_language', language);
              });
        }
      },

      async init () {
        await getWeComInfo().then(res => {
          const data = res.data;
          this.wwLogin= ww.createWWLoginPanel({
            el: '#wecom-qr',
            params: {
              login_type: WWLoginType.corpApp,
              appid: data.corpId ? data.corpId : '',
              agentid: data.agentId,
              redirect_uri: window.location.origin,
              state: 'fit2cloud-wecom-qr',
              redirect_type: WWLoginRedirectType.callback,
              panel_size: WWLoginPanelSizeType.middle,
            },
            onCheckWeComLogin: this.isWeComLogin,
            onLoginSuccess(code) {
              getWeComCallback(code).then(res=>{
                const weComCallback = res.data;
                this.doLogin(weComCallback)
                localStorage.setItem('loginType', 'WE_COM');
              });
            },
            onLoginFail(err) {
              console.log(err.errMsg);
            },
          });
        });

      }
    },
    created() {
      this.init();

    }
  }

</script>

<style scoped>
  .wecom-qr {
    margin-top: -20px;
  }
</style>
