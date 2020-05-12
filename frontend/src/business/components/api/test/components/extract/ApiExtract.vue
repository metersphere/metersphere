<template>
  <div>
    <div class="extract-description">
      {{$t('api_test.request.extract.description')}}
    </div>
    <el-row :gutter="10">
      <el-col :span="4">
        <el-select class="extract-item" v-model="type" :placeholder="$t('api_test.request.extract.select_type')"
                   size="small">
          <el-option :label="$t('api_test.request.extract.regex')" :value="options.REGEX"/>
          <el-option label="JSONPath" :value="options.JSON_PATH"/>
          <el-option label="XPath" :value="options.XPATH"/>
        </el-select>
      </el-col>
      <el-col :span="20">
        <ms-api-extract-common :extract-type="type" :list="list" v-if="type"/>
      </el-col>
    </el-row>

    <ms-api-extract-edit :extract="extract"/>
  </div>
</template>

<script>
  import {EXTRACT_TYPE, Extract} from "../../model/ScenarioModel";
  import MsApiExtractEdit from "./ApiExtractEdit";
  import MsApiExtractCommon from "./ApiExtractCommon";

  export default {
    name: "MsApiExtract",

    components: {
      MsApiExtractCommon,
      MsApiExtractEdit,
    },

    props: {
      extract: Extract
    },

    data() {
      return {
        options: EXTRACT_TYPE,
        type: "",
      }
    },

    computed: {
      list() {
        switch (this.type) {
          case EXTRACT_TYPE.REGEX:
            return this.extract.regex;
          case EXTRACT_TYPE.JSON_PATH:
            return this.extract.json;
          case EXTRACT_TYPE.XPATH:
            return this.extract.xpath;
          default:
            return [];
        }
      }
    }
  }
</script>

<style scoped>
  .extract-description {
    font-size: 13px;
    margin-bottom: 10px;
  }

  .extract-item {
    width: 100%;
  }
</style>
