<template>
  <el-form :model="scenario" :rules="rules" ref="scenario" label-width="100px">
    <el-form-item :label="$t('api_test.scenario.name')" prop="name">
      <el-input v-model="scenario.name" maxlength="100" show-word-limit/>
    </el-form-item>

    <!--    <el-form-item :label="$t('api_test.scenario.base_url')" prop="url">-->
    <!--      <el-input :placeholder="$t('api_test.scenario.base_url_description')" v-model="scenario.url" maxlength="100"/>-->
    <!--    </el-form-item>-->

    <el-tabs v-model="activeName">
      <el-tab-pane :label="$t('api_test.scenario.variables')" name="parameters">
        <ms-api-scenario-variables :items="scenario.variables" :description="$t('api_test.scenario.kv_description')"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.scenario.headers')" name="headers">
        <ms-api-key-value :items="scenario.headers" :description="$t('api_test.scenario.kv_description')"/>
      </el-tab-pane>
    </el-tabs>
  </el-form>
</template>

<script>
  import MsApiKeyValue from "./ApiKeyValue";
  import {Scenario} from "../model/ScenarioModel";
  import MsApiScenarioVariables from "./ApiScenarioVariables";

  export default {
    name: "MsApiScenarioForm",
    components: {MsApiScenarioVariables, MsApiKeyValue},
    props: {
      scenario: Scenario
    },

    data() {
      return {
        activeName: "parameters",
        rules: {
          name: [
            {max: 100, message: this.$t('commons.input_limit', [0, 100]), trigger: 'blur'}
          ],
          url: [
            {max: 100, message: this.$t('commons.input_limit', [0, 100]), trigger: 'blur'}
          ]
        }
      }
    }
  }
</script>

<style scoped>

</style>
