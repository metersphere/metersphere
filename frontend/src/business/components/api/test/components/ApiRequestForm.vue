<template>
  <el-form :model="request" :rules="rules" ref="request" label-width="100px">
    <el-form-item :label="$t('api_test.request.name')" prop="name">
      <el-input v-model="request.name" maxlength="100" show-word-limit/>
    </el-form-item>

    <el-form-item :label="$t('api_test.request.url')" prop="url">
      <el-input v-model="request.url" maxlength="100" :placeholder="$t('api_test.request.url_description')"
                @change="urlChange" clearable>
        <el-select v-model="request.method" slot="prepend" class="request-method-select" @change="methodChange">
          <el-option label="GET" value="GET"/>
          <el-option label="POST" value="POST"/>
          <el-option label="PUT" value="PUT"/>
          <el-option label="PATCH" value="PATCH"/>
          <el-option label="DELETE" value="DELETE"/>
          <el-option label="OPTIONS" value="OPTIONS"/>
          <el-option label="HEAD" value="HEAD"/>
          <el-option label="CONNECT" value="CONNECT"/>
        </el-select>
      </el-input>
    </el-form-item>

    <el-tabs v-model="activeName">
      <el-tab-pane :label="$t('api_test.request.parameters')" name="parameters">
        <ms-api-key-value :items="request.parameters" :description="$t('api_test.request.parameters_desc')"
                          @change="parametersChange"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.request.headers')" name="headers">
        <ms-api-key-value :items="request.headers"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.request.body')" name="body" v-if="isNotGet">
        <ms-api-body :body="request.body"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.request.assertions.label')" name="assertions">
        <ms-api-assertions :assertions="request.assertions"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.request.extract.label')" name="extract">
        <ms-api-extract :extract="request.extract"/>
      </el-tab-pane>
    </el-tabs>
  </el-form>
</template>

<script>
  import MsApiKeyValue from "./ApiKeyValue";
  import MsApiBody from "./ApiBody";
  import MsApiAssertions from "./assertion/ApiAssertions";
  import {KeyValue, Request} from "../model/ScenarioModel";
  import MsApiExtract from "./extract/ApiExtract";

  export default {
    name: "MsApiRequestForm",
    components: {MsApiExtract, MsApiAssertions, MsApiBody, MsApiKeyValue},
    props: {
      request: Request
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
    },

    methods: {
      urlChange() {
        if (!this.request.url) return;

        let parameters = [];
        let url = new URL(this.addProtocol(this.request.url));
        url.searchParams.forEach((value, key) => {
          if (key && value) {
            parameters.push(new KeyValue(key, value));
          }
        });
        // 添加一个空的，用于填写
        parameters.push(new KeyValue());
        this.request.parameters = parameters;
        this.request.url = url.toString();
      },
      methodChange(value) {
        if (value === 'GET' && this.activeName === 'body') {
          this.activeName = 'parameters';
        }
      },
      parametersChange(parameters) {
        if (!this.request.url) return;
        let url = new URL(this.addProtocol(this.request.url));
        url.search = "";
        parameters.forEach(function (parameter) {
          if (parameter.name && parameter.value) {
            url.searchParams.append(parameter.name, parameter.value);
          }
        })
        this.request.url = url.toString();
      },
      addProtocol(url) {
        if (url) {
          if (!url.toLowerCase().startsWith("https") && !url.toLowerCase().startsWith("http")) {
            return "https://" + url;
          }
        }
        return url;
      }
    },

    computed: {
      isNotGet() {
        return this.request.method !== "GET";
      }
    }
  }
</script>

<style scoped>
  .request-method-select {
    width: 110px;
  }
</style>
