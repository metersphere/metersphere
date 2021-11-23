<template>
  <div>
    <el-form :model="commonConfig" :rules="rules" ref="commonConfig" :disabled="isReadOnly" label-width="30px">
      <ms-api-scenario-variables :show-copy="false" :items="commonConfig.variables"/>
      <el-form-item>
        <el-switch v-model="commonConfig.enableHost" active-text="Hosts"/>
      </el-form-item>
      <el-form-item>
        <span>{{ $t('api_test.environment.request_timeout') }}:</span>
        <el-input-number style="margin-left: 20px" controls-position="right" size="small"
                         v-model="commonConfig.requestTimeout">
          {{ $t('api_test.environment.globalVariable') }}
        </el-input-number>
        <span style="margin-left: 30px">{{ $t('api_test.environment.response_timeout') }}:</span>
        <el-input-number style="margin-left: 20px" controls-position="right" size="small"
                         v-model="commonConfig.responseTimeout">
          {{ $t('api_test.environment.globalVariable') }}
        </el-input-number>
      </el-form-item>
      <el-form-item>
        <ms-api-host-table v-if="commonConfig.enableHost" :hostTable="commonConfig.hosts" ref="refHostTable"/>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import {CommonConfig} from "../../model/EnvironmentModel";
import MsApiScenarioVariables from "../ApiScenarioVariables";
import MsApiHostTable from "../ApiHostTable";

export default {
  name: "MsEnvironmentCommonConfig",
  components: {MsApiHostTable, MsApiScenarioVariables},
  props: {
    commonConfig: new CommonConfig(),
    isReadOnly: {
      type: Boolean,
      default: false
    },
  },
  data() {
    return {
      rules: {},
    };
  },
  created() {
    if (!this.commonConfig.requestTimeout) {
      this.$set(this.commonConfig, 'requestTimeout', 60000);
    }
    if (!this.commonConfig.responseTimeout) {
      this.$set(this.commonConfig, 'responseTimeout', 60000);
    }
  },
  watch: {
    commonConfig() {
      if (!this.commonConfig.requestTimeout) {
        this.$set(this.commonConfig, 'requestTimeout', 60000);
      }
      if (!this.commonConfig.responseTimeout) {
        this.$set(this.commonConfig, 'responseTimeout', 60000);
      }
    }
  },
  methods: {
    validate() {
      let isValidate = false;
      this.$refs['commonConfig'].validate((valid) => {
        if (valid) {
          // 校验host列表
          let valHost = true;
          if (this.commonConfig.enableHost) {
            for (let i = 0; i < this.commonConfig.hosts.length; i++) {
              valHost = this.$refs['refHostTable'].confirm(this.commonConfig.hosts[i]);
            }
          }
          if (valHost) {
            isValidate = true;
          } else {
            isValidate = false;
          }
        } else {
          isValidate = false;
        }
      });
      return isValidate;
    }
  }
};
</script>

<style scoped>

</style>
