<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
  <el-tabs v-model="activeName">
    <!-- 认证-->
    <el-tab-pane :label="$t('api_test.definition.request.verified')" name="verified">

      <el-form :model="authConfig" :rules="rule" ref="authConfig" label-position="right">
        <el-form-item :label="$t('api_test.definition.request.verification_method')" prop="verification">
          <el-select v-model="authConfig.verification" @change="change" :disabled="isReadOnly"
                     :placeholder="$t('api_test.definition.request.verification_method')" filterable size="small">
            <el-option
                v-for="item in options"
                :key="item.name"
                :label="item.name"
                :value="item.name">
            </el-option>
          </el-select>

        </el-form-item>

        <el-form-item :label="$t('api_test.request.tcp.username')" prop="username"
                      v-if="authConfig.verification!=undefined && authConfig.verification !='No Auth'">
          <el-input :placeholder="$t('api_test.request.tcp.username')" v-model="authConfig.username"
                    class="ms-http-input" size="small">
          </el-input>
        </el-form-item>

        <el-form-item :label="$t('commons.password')" prop="password"
                      v-if=" authConfig.verification!=undefined && authConfig.verification !='No Auth'">
          <el-input v-model="authConfig.password" :placeholder="$t('commons.password')" show-password autocomplete="off"
                    maxlength="100" show-word-limit/>
        </el-form-item>

      </el-form>

    </el-tab-pane>

    <!--加密-->
    <el-tab-pane :label="$t('api_test.definition.request.encryption')" name="encryption" v-if="encryptShow">
      <el-form :model="authConfig" size="small" :rules="rule"
               ref="authConfig">

        <el-form-item :label="$t('api_test.definition.request.encryption')" prop="encryption">
          <el-select v-model="authConfig.encrypt" :disabled="isReadOnly"
                     :placeholder="$t('api_test.definition.request.verification_method')" filterable size="small">
            <el-option
                v-for="item in encryptOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
    </el-tab-pane>
  </el-tabs>
</template>

<script>
import {createComponent} from "../jmeter/components";

export default {
  name: "MsApiAuthConfig",
  components: {},
  props: {
    request: {},
    encryptShow: {
      type: Boolean,
      default: true,
    },
    isReadOnly: Boolean,
  },
  watch: {
    request() {
      this.initData();
    }
  },
  created() {
    this.initData();
  },
  data() {
    return {
      options: [{name: "No Auth"}, {name: "Basic Auth"}],
      encryptOptions: [{id: false, name: this.$t('commons.encrypted')}],
      activeName: "verified",
      rule: {},
      authConfig: {},
    }
  },
  methods: {
    change() {
      if (this.authConfig.verification === "Basic Auth") {
        let authManager = createComponent("AuthManager");
        authManager.verification = "Basic Auth";
        authManager.environment = this.request.useEnvironment;
        if (this.request.hashTree == undefined) {
          this.request.hashTree = [];
        }
        // 这里做个判断，如果原来有值则不覆盖
        if (this.authConfig.username == undefined && this.authConfig.password == undefined) {
          this.authConfig = authManager;
        }
        this.request.authManager = this.authConfig;
      } else {
        let authManager = createComponent("AuthManager");
        authManager.verification = "No Auth";
        if (this.request.hashTree == undefined) {
          this.request.hashTree = [];
        }
        this.authConfig = authManager;
        this.request.authManager = this.authConfig;
      }
    },
    initData() {
      if (this.request.hashTree) {
        for (let index in this.request.hashTree) {
          if (this.request.hashTree[index].type == 'AuthManager') {
            this.request.authManager = this.request.hashTree[index];
            this.request.hashTree.splice(index, 1);
          }
        }
      }
      if (this.request.authManager) {
        this.authConfig = this.request.authManager;
      }
    }
  }
}
</script>

<style scoped>
/deep/ .el-tabs__nav-wrap::after {
  height: 0px;
}
</style>
