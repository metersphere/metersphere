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
      getLanguage(language) {
        if (!language) {
          getLanguage()
              .then(response => {
                language = response.data;
                localStorage.setItem('default_language', language);
              });
        }
      },
       init () {
         getWeComInfo().then(res => {
          const data = res.data;
          const router = this.$router
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
                const userStore = useUserStore()
                // 删除缓存
                userStore.checkPermission(res);
                sessionStorage.removeItem('changePassword');
                localStorage.setItem('default_language', weComCallback.language);
                sessionStorage.setItem('loginSuccess', 'true');
                sessionStorage.setItem('changePassword', false);
                localStorage.setItem('AuthenticateType', 'QRCODE');
                if (sessionStorage.getItem('lastUser') === getCurrentUserId()) {
                  router.push({path: sessionStorage.getItem('redirectUrl') || '/'});
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
                router.push({name: "login_redirect", path: redirectUrl || '/', query: {}});
                localStorage.setItem('loginType', 'WE_COM');
              }).catch((err)=>{
                this.$message.error(err.response.data.message);
              });
            },
            onLoginFail(err) {
              console.log("err");
              console.log(err);
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
