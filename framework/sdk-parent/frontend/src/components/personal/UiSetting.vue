<template>
  <div class="setting-container">
    <div class="server-setting-row">
      <div class="server-label">{{$t("system_config.local_selenium_url")}}</div>
      <div class="server-input">
        <el-input
          size="small"
          type="text"
          v-model="seleniumServer"
          @input="change()"
        />
      </div>
      <div class="server-desc">(示例: http://localhost:4444)</div>
    </div>
    <div class="setting-opt-row">
      <el-button size="small" @click="cancel">{{
          $t("commons.cancel")
        }}
      </el-button>
      <el-button
        size="small"
        type="primary"
        @click="updateSeleniumServer()"
        @keydown.enter.native.prevent
      >{{ $t("commons.confirm") }}
      </el-button
      >
    </div>
  </div>
</template>

<script>
import {getUserInfo, updateSeleniumServer} from "../../api/user";

export default {
  name: "UiSetting",
  data() {
    return {
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
      let res = await getUserInfo();
      if (res.data) {
        this.seleniumServer = res.data.seleniumServer || "";
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
      this.result = updateSeleniumServer({seleniumServer: this.seleniumServer})
        .then((response) => {
            if (!response.data) {
              this.$error(this.$t("selenium-server 配置失败！"));
            } else {
              this.$success(this.$t("commons.modify_success"));
            }
          }
        );
    },
    isURL(url) {
      let regEx =
        /^(http|https):\/\/(([0-9]{1,3}\.){3}[0-9]{1,3}|([0-9a-z_!~*'()-]+\.)*([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\.[a-z]{2,6})(:[0-9]{1,5})?$/;
      let localhostRegEx = /^http:\/\/localhost:[0-9]{1,5}$/;
      if (regEx.test(url) || localhostRegEx.test(url)) {
        return true;
      } else {
        return false;
      }
    },
  },
};
</script>

<style scoped>
.setting-container {
  padding-left: 50px;
}

.setting-container .server-setting-row {
  margin: 10px 0px;
}

.setting-container .server-setting-row .server-label {
  font-size: 15px;
  color: #606266;
}

.setting-container .server-setting-row .server-input {
  margin: 10px 0px 5px 0;
}

.setting-container .server-setting-row .server-input .el-input__inner {
  width: 300px !important;
}

.setting-container .server-setting-row .server-desc {
  color: #909399;
  font-size: 11px;
}

.setting-container .setting-opt-row {
  margin-top: 18px;
}

</style>
