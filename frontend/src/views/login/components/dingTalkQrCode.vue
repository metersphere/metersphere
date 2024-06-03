<template>
  <div id="ding-talk-qr" class="ding-talk-qrName" />
</template>

<script lang="ts" setup>
  import { useScriptTag } from '@vueuse/core';

  const { load } = useScriptTag('https://g.alicdn.com/dingding/h5-dingtalk-login/0.21.0/ddlogin.js');

  const a = encodeURIComponent('https://s0my5tnf41e2.ngrok.xiaomiqiu123.top/');

  const url = `https://login.dingtalk.com/oauth2/auth?redirect_uri=${a}&response_type=code&client_id=dinglsfxhodjquu4gq2x&scope=openid&state=dddd&prompt=consent`;

  const initActive = async () => {
    await load(true);
    window.DTFrameLogin(
      {
        id: 'ding-talk-qr',
        width: 300,
        height: 300,
      },
      {
        redirect_uri: a,
        client_id: 'dinglsfxhodjquu4gq2x',
        scope: 'openid',
        response_type: 'code',
        state: 'xxxxxxxxx',
        prompt: 'consent',
      },
      (loginResult) => {
        const { redirectUrl, authCode, state } = loginResult;
        // 这里可以直接进行重定向
        window.location.href = redirectUrl;
        // 也可以在不跳转页面的情况下，使用code进行授权
        console.log(authCode);
      },
      (errorMsg) => {
        // 这里一般需要展示登录失败的具体原因,可以使用toast等轻提示
        console.error(`errorMsg of errorCbk: ${errorMsg}`);
      }
    );
  };
  onMounted(() => {
    initActive();
  });
</script>

<style lang="less" scoped>
  .ding-talk-qrName {
    width: 300px;
    height: 300px;
  }
</style>
