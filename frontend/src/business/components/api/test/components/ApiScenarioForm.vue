<template>
  <el-form :model="scenario" :rules="rules" ref="scenario" label-width="100px" v-loading="result.loading">
    <el-form-item :label="$t('api_test.scenario.name')" prop="name">
      <el-input :disabled="isReadOnly" v-model="scenario.name" maxlength="100" show-word-limit/>
    </el-form-item>

      <el-form-item :label="$t('api_test.environment.environment')">
        <el-select :disabled="isReadOnly" v-model="scenario.environmentId" class="environment-select" @change="environmentChange" clearable>
          <el-option v-for="(environment, index) in environments" :key="index" :label="environment.name + ': ' + environment.protocol + '://' + environment.socket" :value="environment.id"/>
          <el-button class="environment-button" size="mini" type="primary" @click="openEnvironmentConfig">{{$t('api_test.environment.environment_config')}}</el-button>
        </el-select>
      </el-form-item>

<!--      <el-form-item :label="$t('api_test.scenario.base_url')" prop="url">-->
<!--        <el-input :placeholder="$t('api_test.scenario.base_url_description')" v-model="scenario.url" maxlength="200"/>-->
<!--      </el-form-item>-->

    <el-tabs v-model="activeName">
      <el-tab-pane :label="$t('api_test.scenario.variables')" name="parameters">
        <ms-api-scenario-variables :is-read-only="isReadOnly" :items="scenario.variables" :description="$t('api_test.scenario.kv_description')"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.scenario.headers')" name="headers">
        <ms-api-key-value :is-read-only="isReadOnly" :items="scenario.headers" :description="$t('api_test.scenario.kv_description')"/>
      </el-tab-pane>
    </el-tabs>

    <api-environment-config ref="environmentConfig" @close="environmentConfigClose"/>

  </el-form>

</template>

<script>
  import MsApiKeyValue from "./ApiKeyValue";
  import {Scenario} from "../model/ScenarioModel";
  import MsApiScenarioVariables from "./ApiScenarioVariables";
  import ApiEnvironmentConfig from "./ApiEnvironmentConfig";

  export default {
    name: "MsApiScenarioForm",
    components: {ApiEnvironmentConfig, MsApiScenarioVariables, MsApiKeyValue},
    props: {
      scenario: Scenario,
      projectId: String,
      isReadOnly: {
        type: Boolean,
        default: false
      }
    },
    created() {
      this.getEnvironments();
    },
    data() {
      return {
        result: {},
        activeName: "parameters",
        environments: [],
        rules: {
          name: [
            {max: 100, message: this.$t('commons.input_limit', [0, 100]), trigger: 'blur'}
          ],
          url: [
            {max: 100, message: this.$t('commons.input_limit', [0, 100]), trigger: 'blur'}
          ]
        }
      }
    },
    methods: {
      getEnvironments() {
        if (this.projectId) {
          this.result = this.$get('/api/environment/list/' + this.projectId, response => {
            this.environments = response.data;
            for (let i in this.environments) {
              if (this.environments[i].id === this.scenario.environmentId) {
                this.scenario.environment = this.environments[i];
                break;
              }
            }
          });
        }
      },
      environmentChange(value) {
        for (let i in this.environments) {
          if (this.environments[i].id === value) {
            this.scenario.environment = this.environments[i];
            this.scenario.requests.forEach(request => {
              request.environment = this.environments[i];
            });
            break;
          }
        }
        if (!value) {
          this.scenario.environment = undefined;
          this.scenario.requests.forEach(request => {
            request.environment = undefined;
          });
        }
      },
      openEnvironmentConfig() {
        this.$refs.environmentConfig.open(this.projectId);
      },
      environmentConfigClose() {
        this.getEnvironments();
      }
    }
  }
</script>

<style scoped>

  .environment-select {
    width: 100%;
  }

  .environment-button {
    margin-left: 20px;
    padding: 7px;
  }

</style>
