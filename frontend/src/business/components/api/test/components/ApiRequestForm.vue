<template>
  <el-form :model="request" :rules="rules" ref="request" label-width="100px" label-position="left" v-if="isRequest">
    <el-form-item :label="$t('api_test.request.name')" prop="name">
      <el-input v-model="request.name"></el-input>
    </el-form-item>

    <el-form-item :label="$t('api_test.request.url')" prop="url">
      <el-input v-model="request.url" :placeholder="$t('api_test.request.url_describe')">
        <el-select v-model="request.method" slot="prepend" class="request-method-select">
          <el-option label="GET" value="GET"></el-option>
          <el-option label="POST" value="POST"></el-option>
          <el-option label="PUT" value="PUT"></el-option>
          <el-option label="PATCH" value="PATCH"></el-option>
          <el-option label="DELETE" value="DELETE"></el-option>
          <el-option label="OPTIONS" value="OPTIONS"></el-option>
          <el-option label="HEAD" value="HEAD"></el-option>
          <el-option label="CONNECT" value="CONNECT"></el-option>
        </el-select>
      </el-input>
    </el-form-item>

    <el-tabs v-model="activeName">
      <el-tab-pane :label="$t('api_test.request.parameters')" name="parameters">
        <ms-api-key-value :items="request.parameters" :description="$t('api_test.request.parameters_desc')"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.request.headers')" name="headers">
        <ms-api-key-value :items="request.headers"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.request.body')" name="body" v-if="isNotGet">
        <ms-api-body :body="request.body"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.request.assertions')" name="assertions" v-if="false">
        TODO
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.request.extract')" name="extract" v-if="false">
        TODO
      </el-tab-pane>
    </el-tabs>
  </el-form>
</template>

<script>
  import MsApiKeyValue from "./ApiKeyValue";
  import MsApiBody from "./ApiBody";

  export default {
    name: "MsApiRequestForm",
    components: {MsApiBody, MsApiKeyValue},
    props: {
      request: Object
    },

    data() {
      return {
        activeName: "parameters",
        rules: {}
      }
    },

    computed: {
      isRequest() {
        return this.request.type === "Request";
      },
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
