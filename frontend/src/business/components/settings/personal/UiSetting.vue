<template>
  <div class="setting-container">
    <div class="server-setting-row">
      <div class="server-label">本地 selenium-server 地址</div>
      <div class="server-input">
        <el-input
          size="small"
          type="text"
          v-model="seleniumServer"
          @input="change()"
        />
      </div>
      <div class="server-desc">(示例: http://192.168.1.101:4444)</div>
    </div>
    <div class="setting-opt-row">
      <el-button size="small" @click="cancel">{{
        $t("commons.cancel")
      }}</el-button>
      <el-button
        size="small"
        type="primary"
        @click="updateSeleniumServer()"
        @keydown.enter.native.prevent
        >{{ $t("commons.confirm") }}</el-button
      >
    </div>
  </div>
</template>

<script>
import { getCurrentUserId } from "@/common/js/utils";
export default {
  name: "UiSetting",
  data() {
    return {
      updateSeleniumServerPath: "/user/update/seleniumServer",
      seleniumServer: "",
    };
  },
  mounted() {
    this.getUserSeleniumServer();
    setTimeout(() => {
      this.change();
    }, 10);
  },
  methods: {
    change() {
      this.$forceUpdate();
    },
    cancel() {
      this.$emit("cancel");
    },
    confirm() {
      this.$emit("confirm");
    },
    async getUserSeleniumServer() {
      let userId = getCurrentUserId();
      let res = await this.$get(`/user/info/${userId}`);
      if (res.data) {
        this.seleniumServer = res.data.data.seleniumServer || "";
      }
    },
    updateSeleniumServer() {
      if (this.seleniumServer) {
        if (!this.isURL(this.seleniumServer)) {
          this.$error(this.$t("selenium-server 输入不合法！"));
          return;
        }
      }
      //更新 seleniumServer地址
      this.result = this.$post(
        this.updateSeleniumServerPath,
        { seleniumServer: this.seleniumServer },
        (response) => {
          if (!response.data) {
            this.$error(this.$t("selenium-server 配置失败！"));
          } else {
            this.$success(this.$t("commons.modify_success"));
          }
        }
      );
    },
    isURL(str_url) {
      let strRegex =
        "^((https|http|ftp|rtsp|mms)://)" +
        "(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?" + //ftp的user@
        "(([0-9]{1,3}\.){3}[0-9]{1,3}" + // IP形式的URL- 199.194.52.184
        "|" + // 允许IP和DOMAIN（域名）
        "([0-9a-z_!~*'()-]+\.)*" + // 域名- www.
        "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\." + // 二级域名
        "[a-z]{2,6})" + // first level domain- .com or .museum
        "(:[0-9]{1,5})?" + // 端口- :80
        "((/?)|" + // a slash isn't required if there is no file name
        "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?)$";
      let re = new RegExp(strRegex);
      //re.test()
      if (re.test(str_url)) {
        return true;
      } else {
        return false;
      }
    },
  },
};
</script>

<style scope lang="less">
.setting-container {
  padding-left: 50px;
  .server-setting-row {
    margin: 10px 0px;
    .server-label {
      font-size: 15px;
      color: #606266;
    }

    .server-input {
      margin: 10px 0px 5px 0;
      .el-input__inner {
        width: 300px !important;
      }
    }

    .server-desc {
      color: #909399;
      font-size: 11px;
    }
  }
  .setting-opt-row {
    margin-top: 18px;
    el-button {
    }
  }
}
</style>
