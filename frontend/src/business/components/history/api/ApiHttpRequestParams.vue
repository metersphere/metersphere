<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
  <!-- HTTP 请求参数 -->
  <div style="border:1px #DCDFE6 solid;" v-if="!loading">
    <el-tabs v-model="activeName" class="request-tabs">
      <!-- 请求头-->
      <el-tab-pane :label="$t('api_test.request.headers')" name="headers" v-if="request.header">
        <ms-api-key-value-detail :items="request.header" :showDesc="true" :format="request.headerId"/>
      </el-tab-pane>

      <!--query 参数-->
      <el-tab-pane :label="$t('api_test.definition.request.query_param')" name="parameters" v-if="request.query">
        <ms-api-key-value-detail :show-required="true" :items="request.query" :showDesc="true" :format="request.headerId"/>
      </el-tab-pane>

      <!--REST 参数-->
      <el-tab-pane :label="$t('api_test.definition.request.rest_param')" name="rest" v-if="request.rest">
        <ms-api-key-value-detail :show-required="true" :items="request.rest" :showDesc="true" :format="request.headerId"/>
      </el-tab-pane>

      <!--请求体-->
      <el-tab-pane :label="$t('api_test.request.body')" name="body" v-if="request.body && (request.body.jsonSchema ||request.body.raw_1 || request.body.raw_2 )">
        <el-radio-group v-model="activeBody" size="mini">
          <el-radio-button label="json"/>
          <el-radio-button label="raw"/>
          <el-radio-button label="form"/>
        </el-radio-group>
        <ms-json-code-edit :body="request.body" ref="jsonCodeEdit" v-if="activeBody === 'json'"/>
        <pre v-html="getDiff(request.body.raw_2,request.body.raw_1)" v-if="activeBody === 'raw'"></pre>
        <ms-api-key-value-detail :show-required="true" :items="request.body.form" :showDesc="true" :format="request.headerId" v-if="activeBody === 'form'"/>
      </el-tab-pane>

      <!-- 认证配置 -->
      <!--      <el-tab-pane :label="$t('api_test.definition.request.auth_config')" name="authConfig">-->
      <!--      </el-tab-pane>-->

      <!--      <el-tab-pane :label="$t('api_test.definition.request.other_config')" name="advancedConfig">-->
      <!--      </el-tab-pane>-->
    </el-tabs>
  </div>
</template>

<script>
import MsJsonCodeEdit from "./json-view/ComparedEditor";
import MsApiKeyValueDetail from "./common/ApiKeyValueDetail";

const jsondiffpatch = require('jsondiffpatch');
const formattersHtml = jsondiffpatch.formatters.html;

export default {
  name: "MsApiHttpRequestParams",
  components: {MsJsonCodeEdit, MsApiKeyValueDetail},
  props: {
    method: String,
    request: {},
    type: String,
  },
  data() {
    return {
      activeName: "",
      activeBody: "",
      loading: false,
    }
  },
  created() {
    if (this.request.body && (this.request.body.jsonSchema || this.request.body.form || this.request.body.raw_1 || this.request.body.raw_2)) {
      this.activeName = "body";
      if (this.request.body.jsonSchema) {
        this.activeBody = "json";
      }
      if (this.request.body.form) {
        this.activeBody = "form";
      }
      if (this.request.body.raw_1 || this.request.body.raw_2) {
        this.activeBody = "raw";
      }
    } else if (this.request.query) {
      this.activeName = "parameters";
    } else if (this.request.header) {
      this.activeName = "headers";
    } else if (this.request.rest) {
      this.activeName = "rest";
    }
    this.reloadCodeEdit();
  },
  watch: {
    'request.headerId'() {
      if (this.request.body && (this.request.body.jsonSchema || this.request.body.form || this.request.body.raw_1 || this.request.body.raw_2)) {
        this.activeName = "body";
        if (this.request.body.json) {
          this.activeBody = "json";
        }
        if (this.request.body.form) {
          this.activeBody = "form";
        }
        if (this.request.body.raw_1 || this.request.body.raw_2) {
          this.activeBody = "raw";
        }
      } else if (this.request.query) {
        this.activeName = "parameters";
      } else if (this.request.header) {
        this.activeName = "headers";
      } else if (this.request.rest) {
        this.activeName = "rest";
      }
      this.reloadCodeEdit();
    }
  },
  methods: {
    getDiff(v1, v2) {
      let delta = jsondiffpatch.diff(v1, v2);
      return formattersHtml.format(delta, v1);
    },
    reloadCodeEdit() {
      this.loading = true;
      this.$nextTick(() => {
        this.loading = false;
      });
    },
  }
}
</script>

<style scoped>
.ms-query {
  background: #783887;
  color: white;
  height: 18px;
  border-radius: 42%;
}

.ms-header {
  background: #783887;
  color: white;
  height: 18px;
  border-radius: 42%;
}

.request-tabs {
  margin: 20px;
  min-height: 200px;
}

.ms-el-link {
  float: right;
  margin-right: 45px;
}

@import "~jsondiffpatch/dist/formatters-styles/html.css";
@import "~jsondiffpatch/dist/formatters-styles/annotated.css";

</style>
