<template>
  <div id="ding-talk-qr" class="ding-talk-qrName" />
</template>

<script>


import {getDingInfo} from "../../api/qrcode";
import loadJs from "../../utils/remoteJs";
import {getCurrentUserId} from "../../utils/token";
import {hasPermissions} from "../../utils/permission";
import {useUserStore} from "@/store";
import axios from "axios";

export default {
    name:'dingTalkQrCode',
    data(){
      return{
      }
    },
    methods:{
      initActive(){
         getDingInfo().then(res=>{
          const dingData =res.data;
          const router = this.$router
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
              (loginResult) => {
                const {redirectUrl, authCode, state} = loginResult;
                axios.get("/sso/callback/ding_talk?code="+authCode).then((response) => {
                  if (response.data && response.data.data) {
                    // 也可以在不跳转页面的情况下，使用code进行授权
                    const weComCallback = response.data.data;
                    const userStore = useUserStore()
                    // 删除缓存
                    userStore.checkPermission(response.data);
                    sessionStorage.removeItem('changePassword');
                    localStorage.setItem('default_language', weComCallback.language);
                    sessionStorage.setItem('loginSuccess', 'true');
                    sessionStorage.setItem('changePassword', false);
                    localStorage.setItem('AuthenticateType', 'QRCODE');
                    if (sessionStorage.getItem('lastUser') === getCurrentUserId()) {
                      router.push({path: sessionStorage.getItem('redirectUrl') || '/'});
                      return;
                    }
                    let routerUrl = '/';
                    if (hasPermissions('PROJECT_USER:READ', 'PROJECT_ENVIRONMENT:READ', 'PROJECT_OPERATING_LOG:READ', 'PROJECT_FILE:READ+JAR', 'PROJECT_FILE:READ+FILE', 'PROJECT_CUSTOM_CODE:READ', 'PROJECT_MESSAGE:READ', 'PROJECT_TEMPLATE:READ')) {
                      routerUrl = '/project/home';
                    } else if (hasPermissions('WORKSPACE_SERVICE:READ', 'WORKSPACE_USER:READ', 'WORKSPACE_PROJECT_MANAGER:READ', 'WORKSPACE_PROJECT_ENVIRONMENT:READ', 'WORKSPACE_OPERATING_LOG:READ')) {
                      routerUrl = '/setting/project/:type';
                    } else if (hasPermissions('SYSTEM_USER:READ', 'SYSTEM_WORKSPACE:READ', 'SYSTEM_GROUP:READ', 'SYSTEM_TEST_POOL:READ', 'SYSTEM_SETTING:READ', 'SYSTEM_AUTH:READ', 'SYSTEM_QUOTA:READ', 'SYSTEM_OPERATING_LOG:READ')) {
                      routerUrl = '/setting';
                    } else {
                      routerUrl = '/';
                    }
                    console.log("routerUrl")
                    console.log(routerUrl)
                    sessionStorage.setItem('redirectUrl', routerUrl);
                    sessionStorage.setItem('lastUser', getCurrentUserId());
                    router.push({name: "login_redirect", path: routerUrl || '/', query: {}});
                    localStorage.setItem('loginType', 'DING_TALK');
                  }
                }).catch((err)=>{
                  this.$message.error(err.response.data.message);
                  console.log("axios")
                  console.log(err)
                });
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
