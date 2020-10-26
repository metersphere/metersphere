<template>
  <div>
    <div class="assertion-add">
      <el-row :gutter="10">
        <el-col :span="4">
          <el-select :disabled="isReadOnly" class="assertion-item" v-model="type" :placeholder="$t('api_test.request.assertions.select_type')"
                     size="small">
            <el-option :label="$t('api_test.request.assertions.text')" :value="options.TEXT"/>
            <el-option :label="$t('api_test.request.assertions.regex')" :value="options.REGEX"/>
            <el-option :label="'JSONPath'" :value="options.JSON_PATH"/>
            <el-option :label="$t('api_test.request.assertions.response_time')" :value="options.DURATION"/>
          </el-select>
        </el-col>
        <el-col :span="20">
          <ms-api-assertion-text :is-read-only="isReadOnly" :list="assertions.regex" v-if="type === options.TEXT" :callback="after"/>
          <ms-api-assertion-regex :is-read-only="isReadOnly" :list="assertions.regex" v-if="type === options.REGEX" :callback="after"/>
          <ms-api-assertion-json-path :is-read-only="isReadOnly" :list="assertions.jsonPath" v-if="type === options.JSON_PATH" :callback="after"/>
          <ms-api-assertion-duration :is-read-only="isReadOnly" v-model="time" :duration="assertions.duration"
                                     v-if="type === options.DURATION" :callback="after"/>
          <el-button v-if="!type" :disabled="true" type="primary" size="small">Add</el-button>
        </el-col>
      </el-row>
    </div>

    <div >

      <el-row :gutter="10" style="text-align: right;">

          <el-button

          size="small"
          type="primary"
          @click="suggestJson"
          >推荐JSONPath断言</el-button>
          <el-button
            size="small"
            type="danger"
            @click="clearJson"
          >清空JSONPath断言</el-button>

      </el-row>

    </div>





    <ms-api-assertions-edit :is-read-only="isReadOnly" :assertions="assertions"/>
  </div>
</template>

<script>
  import MsApiAssertionText from "./ApiAssertionText";
  import MsApiAssertionRegex from "./ApiAssertionRegex";
  import MsApiAssertionDuration from "./ApiAssertionDuration";
  import {ASSERTION_TYPE, Assertions, JSONPath} from "../../model/ScenarioModel";
  import MsApiAssertionsEdit from "./ApiAssertionsEdit";
  import MsApiAssertionJsonPath from "./ApiAssertionJsonPath";

  export default {
    name: "MsApiAssertions",

    components: {
      MsApiAssertionJsonPath,
      MsApiAssertionsEdit, MsApiAssertionDuration, MsApiAssertionRegex, MsApiAssertionText},

    props: {
      assertions: Assertions,
      jsonPathList: Array,
      isReadOnly: {
        type: Boolean,
        default: false
      }
    },

    data() {
      return {
        options: ASSERTION_TYPE,
        time: "",
        type: "",
      }
    },

    methods: {
      after() {
        this.type = "";
      },
      suggestJson() {
        console.log("This is suggestJson")
        // console.log(this.jsonPathList);
        this.jsonPathList.forEach((item) => {
          let jsonItem = new JSONPath();
          jsonItem.expression=item.json_path;
          jsonItem.expect=item.json_value;
          jsonItem.setJSONPathDescription();
          this.assertions.jsonPath.push(jsonItem);
        });

      },
      clearJson() {
        console.log("This is suggestJson")
        // console.log(this.jsonPathList);
        this.assertions.jsonPath = [];

      }
    }

  }
</script>

<style scoped>
  .assertion-item {
    width: 100%;
  }

  .assertion-add {
    padding: 10px;
    border: #DCDFE6 solid 1px;
    margin: 5px 0;
    border-radius: 5px;
  }

  .bg-purple-dark {
    background: #99a9bf;
  }
  .bg-purple {
    background: #d3dce6;
  }
  .bg-purple-light {
    background: #e5e9f2;
  }
  .grid-content {
    border-radius: 4px;
    min-height: 36px;
  }
  .row-bg {
    padding: 10px 0;
    background-color: #f9fafc;
  }
</style>
