<template>
  <div id="ding-talk-qr" class="ding-talk-qrName" />
</template>

<script>


import {getDingCallback, getDingInfo} from "../../api/qrcode";
import loadJs from "../../utils/remoteJs";
import {getCurrentUserId} from "../../utils/token";
import {hasPermissions} from "../../utils/permission";
import {useUserStore} from "@/store";
import {setLanguage} from "@/i18n";
import {getLanguage} from "../../api/user";
import {DEFAULT_LANGUAGE} from "../../utils/constants";

export default {
    name:'dingTalkQrCode',
    data(){
      return{
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
      doLogin(callback) {
        const userStore = useUserStore()
        // 删除缓存
        sessionStorage.removeItem('changePassword');
        userStore.getIsLogin()
            .then(res => {
              this.getLanguage(res.data.language);
              sessionStorage.setItem('loginSuccess', 'true');
              sessionStorage.setItem('changePassword', callback.message);
              localStorage.setItem('AuthenticateType', 'QRCODE');
              this.checkRedirectUrl()
            })
            .catch(data => {
              // 保存公钥
              localStorage.setItem("publicKey", data.message);
              let lang = localStorage.getItem("language");
              if (lang) {
                setLanguage(lang);
              }
              window.location.href = "/";
            });
      },
      getLanguage(language) {
        if (!language) {
          getLanguage()
              .then(response => {
                language = response.data;
                localStorage.setItem(DEFAULT_LANGUAGE, language);
              });
        }
      },

      async initActive(){
        await getDingInfo().then(res=>{
          const dingData =res.data;
          const url = encodeURIComponent(window.location.origin);
          window.DTFrameLogin(
              {
                id: 'ding-talk-qr',
                width: 300,
                height: 300,
              },
              {
                redirect_uri: url,
                client_id: dingData.appKey ? dingData.appKey : '',
                scope: 'openid',
                response_type: 'code',
                state: 'fit2cloud-ding-qr',
                prompt: 'consent',
              },
              async (loginResult) => {
                const { authCode } = loginResult;
                const dingCallback = getDingCallback(authCode);
                // 也可以在不跳转页面的情况下，使用code进行授权
                this.doLogin(dingCallback);
                localStorage.setItem('loginType', 'DING_TALK');
              },
              (errorMsg) => {
                // 这里一般需要展示登录失败的具体原因,可以使用toast等轻提示
                this.$message.error(`errorMsg of errorCbk: ${errorMsg}`);
              }
          );
        });

      }

    },
    created() {
      loadJs('https://g.alicdn.com/dingding/h5-dingtalk-login/0.21.0/ddlogin.js').then(()=>{
        // 加载成功，进行后续操作
        this.initActive();
      })

    }
  }
</script>

<style scoped>
  .ding-talk-qrName {
    width: 300px;
    height: 300px;
  }
</style>
