<template>
  <div id="app">
    <!-- 主应用容器 -->
    <router-view/>
    <!-- 子应用容器 -->
    <div id="micro-app"></div>
  </div>
</template>
<script>


import {getQueryVariable, getUrlParameterWidthRegExp} from "@/utils";
import axios from "axios";
import {useUserStore} from "@/store";
import {getCurrentUserId} from "@/utils/token";
import {hasPermissions} from "@/utils/permission";

export default {
  name: "AppLayout",
  data() {
    return {
    };
  },
  beforeMount() {
    const router = this.$router
    const code = getQueryVariable('code');
    const state = getQueryVariable('state') || '';
    if (state.split('#')[0] === 'fit2cloud-lark-qr' && state.split('#')[1] === "/" ) {
      this.loading = true;
      try {
        axios.get("/sso/callback/lark?code="+code).then((response) => {
          console.log(response)
          const weComCallback = response.data.data;
          const userStore = useUserStore()
          // 删除缓存
          userStore.checkPermission(response.data);
          sessionStorage.removeItem('changePassword');
          localStorage.setItem('default_language', weComCallback.language);
          sessionStorage.setItem('loginSuccess', 'true');
          sessionStorage.setItem('changePassword', weComCallback.message);
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
          this.loading = false;
          router.push({name: "login_redirect", path: redirectUrl || '/', query: {}});
          localStorage.setItem('loginType', 'LARK');
        })
      } catch (err) {
        // eslint-disable-next-line no-console
        console.log(err);
      }
    }
    if (state.split('#')[0] === 'fit2cloud-lark-suite-qr' && state.split('#')[1] === "/") {
      this.loading = true;
      try {
        axios.get("/sso/callback/lark?lark_suite="+code).then((response) => {
          const weComCallback = response.data.data;
          const userStore = useUserStore()
          // 删除缓存
          userStore.checkPermission(response.data);
          sessionStorage.removeItem('changePassword');
          localStorage.setItem('default_language', weComCallback.language);
          sessionStorage.setItem('loginSuccess', 'true');
          sessionStorage.setItem('changePassword', weComCallback.message);
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
          this.loading = false;
          router.push({name: "login_redirect", path: redirectUrl || '/', query: {}});
          localStorage.setItem('loginType', 'LARK_SUITE');
        })
      } catch (err) {
        // eslint-disable-next-line no-console
        console.log(err);
      }
    }

    if (getQueryVariable('code') && getQueryVariable('state')) {
      const currentUrl = window.location.href;
      const url = new URL(currentUrl);
      getUrlParameterWidthRegExp('code');
      getUrlParameterWidthRegExp('state');
      url.searchParams.delete('code');
      url.searchParams.delete('state');
      const newUrl = url.toString();
      // 或者在不刷新页面的情况下更新URL（比如使用 History API）
      window.history.replaceState({}, document.title, newUrl);
    }
  }
}

</script>
