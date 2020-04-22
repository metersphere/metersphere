<template>
  <div>
    <el-row :gutter="10">
      <el-col :span="4">
        <el-select class="assertion-item" v-model="type" :placeholder="$t('api_test.request.assertions.select_type')"
                   size="small">
          <el-option :label="$t('api_test.request.assertions.text')" :value="options.TEXT"/>
          <el-option :label="$t('api_test.request.assertions.regex')" :value="options.REGEX"/>
          <el-option :label="$t('api_test.request.assertions.response_time')" :value="options.RESPONSE_TIME"/>
        </el-select>
      </el-col>
      <el-col :span="20">
        <ms-api-assertion-text :list="assertions.regex" v-if="type === options.TEXT"/>
        <ms-api-assertion-regex :list="assertions.regex" v-if="type === options.REGEX"/>
        <ms-api-assertion-response-time :response-time="assertions.responseTime" v-if="type === options.RESPONSE_TIME"/>
      </el-col>
    </el-row>

    <ms-api-assertions-edit :assertions="assertions"/>
  </div>
</template>

<script>
  import MsApiAssertionText from "./ApiAssertionText";
  import MsApiAssertionRegex from "./ApiAssertionRegex";
  import MsApiAssertionResponseTime from "./ApiAssertionResponseTime";
  import {ASSERTION_TYPE, Assertions, Regex} from "../model/ScenarioModel";
  import MsApiAssertionsEdit from "./ApiAssertionsEdit";

  export default {
    name: "MsApiAssertions",

    components: {MsApiAssertionsEdit, MsApiAssertionResponseTime, MsApiAssertionRegex, MsApiAssertionText},

    props: {
      assertions: Assertions
    },

    data() {
      return {
        options: ASSERTION_TYPE,
        type: "",
      }
    }
  }
</script>

<style scoped>
  .assertion-item {
    width: 100%;
  }
</style>
