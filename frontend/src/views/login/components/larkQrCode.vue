<template>
  <div id="lark-qr" class="lark-qrName" />
</template>

<script lang="ts" setup>
  import { useScriptTag } from '@vueuse/core';

  import { getLarkInfo } from '@/api/modules/user';

  const { load } = useScriptTag(
    'https://lf-package-cn.feishucdn.com/obj/feishu-static/lark/passport/qrcode/LarkSSOSDKWebQRCode-1.0.3.js'
  );

  const initActive = async () => {
    const data = await getLarkInfo();
    await load(true);
    // const callBack = encodeURIComponent(window.location.origin);
    const redirectUrL = encodeURIComponent(window.location.origin);
    // const url = `https://passport.feishu.cn/suite/passport/oauth/authorize?client_id=${data.agentId}&redirect_uri=${callBack}&response_type=code&state=fit2cloud-lark-qr`;
    const url = `https://passport.feishu.cn/suite/passport/oauth/authorize?client_id=${data.agentId}&redirect_uri=${redirectUrL}&response_type=code&state=fit2cloud-lark-qr`;

    const QRLoginObj = window.QRLogin({
      id: 'lark-qr',
      goto: url,
      width: '300',
      height: '300',
      style: 'width:300px;height:300px', // 可选的，二维码html标签的style属性
    });

    // function handleMessage
    if (typeof window.addEventListener !== 'undefined') {
      window.addEventListener('message', async (event: any) => {
        // 使用 matchOrigin 和 matchData 方法来判断 message 和来自的页面 url 是否合法
        if (QRLoginObj.matchOrigin(event.origin) && QRLoginObj.matchData(event.data)) {
          const loginTmpCode = event.data.tmp_code;
          // 在授权页面地址上拼接上参数 tmp_code，并跳转
          window.location.href = `${url}&tmp_code=${loginTmpCode}`;
        }
      });
    }
  };
  onMounted(() => {
    initActive();
  });
</script>

<style lang="less" scoped>
  .lark-qrName {
    width: 300px;
    height: 300px;
  }
</style>
