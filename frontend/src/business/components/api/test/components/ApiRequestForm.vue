<template>
  <el-form :model="request" :rules="rules" ref="request" label-width="100px">
    <el-form-item :label="$t('api_test.request.name')" prop="name">
      <el-input v-model="request.name" maxlength="100"/>
    </el-form-item>

    <el-form-item :label="$t('api_test.request.url')" prop="url">
      <el-input v-model="request.url" maxlength="100" :placeholder="$t('api_test.request.url_description')">
        <el-select v-model="request.method" slot="prepend" class="request-method-select">
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
        <ms-api-key-value :items="request.parameters" :description="$t('api_test.request.parameters_desc')"/>
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
      <el-tab-pane :label="$t('api_test.request.extract')" name="extract" v-if="false">
        TODO
      </el-tab-pane>
    </el-tabs>
  </el-form>
</template>

<script>
  import MsApiKeyValue from "./ApiKeyValue";
  import MsApiBody from "./ApiBody";
  import MsApiAssertions from "./ApiAssertions";

  export default {
    name: "MsApiRequestForm",
    components: {MsApiAssertions, MsApiBody, MsApiKeyValue},
    props: {
      request: Object
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
