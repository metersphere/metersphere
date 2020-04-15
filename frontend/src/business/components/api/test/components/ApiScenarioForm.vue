<template>
  <el-form :model="scenario" :rules="rules" ref="scenario" label-width="100px" label-position="left" v-if="isScenario">
    <el-form-item :label="$t('api_test.scenario.name')" prop="name">
      <el-input v-model="scenario.name"></el-input>
    </el-form-item>

    <el-form-item :label="$t('api_test.scenario.base_url')" prop="url">
      <el-input :placeholder="$t('api_test.scenario.base_url_describe')" v-model="scenario.url"></el-input>
    </el-form-item>

    <el-tabs v-model="activeName">
      <el-tab-pane :label="$t('api_test.scenario.variables')" name="variables">
        <ms-api-key-value :items="scenario.variables"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.scenario.headers')" name="headers">
        <ms-api-key-value :items="scenario.headers"/>
      </el-tab-pane>
    </el-tabs>
  </el-form>
</template>

<script>
  import MsApiKeyValue from "./ApiKeyValue";

  export default {
    name: "MsApiScenarioForm",
    components: {MsApiKeyValue},
    props: {
      scenario: Object
    },

    data() {
      return {
        activeName: "variables",
        rules: {}
      }
    },

    computed: {
      isScenario() {
        return this.scenario.type === "Scenario";
      }
    },
  }
</script>

<style scoped>

</style>
