<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
  <!-- HTTP 请求参数 -->
  <div style="border:1px #DCDFE6 solid;" v-if="!loading">
    <el-tabs v-model="activeName" class="request-tabs">
      <!-- 请求头-->
      <el-tab-pane :label="$t('api_test.request.headers')" name="headers" v-if="request.header">
        <ms-api-key-value-detail :items="request.header" :showDesc="true" :format="request.headerId"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.definition.request.status_code')" name="statusCode" v-if="request.statusCode">
        <ms-api-key-value-detail :items="request.statusCode" :showDesc="true" :format="request.headerId"/>
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
          <el-radio-button label="json" v-if="request.body.jsonSchema"/>
          <el-radio-button label="raw" v-if="request.body.raw_1 || request.body.raw_2"/>
          <el-radio-button label="form" v-if="request.body.form"/>
        </el-radio-group>
        <ms-json-code-edit :body="request.body" ref="jsonCodeEdit" v-if="activeBody === 'json'"/>
        <pre v-html="getDiff(request.body.raw_2,request.body.raw_1)" v-if="activeBody === 'raw'"></pre>
        <ms-api-key-value-detail :show-required="true" :items="request.body.form" :showDesc="true" :format="request.headerId" v-if="activeBody === 'form'"/>
      </el-tab-pane>

      <!--认证配置 -->
      <el-tab-pane :label="$t('api_test.definition.request.auth_config')" name="authConfig" v-if="request.body_auth">
        <el-table :data="request.body_auth">
          <el-table-column prop="columnTitle" :label="$t('operating_log.change_field')"/>
          <el-table-column prop="originalValue" :label="$t('operating_log.before_change')">
            <template v-slot:default="scope">
              <el-tooltip :content="scope.row.originalValue">
                <div class="current-value ms-tag-del">{{ scope.row.originalValue }}</div>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column prop="newValue" :label="$t('operating_log.after_change')">
            <template v-slot:default="scope">
              <el-tooltip :content="scope.row.newValue">
                <div class="current-value ms-tag-add">{{ scope.row.newValue }}</div>
              </el-tooltip>
            </template>
          </el-table-column>
        </el-table>

      </el-tab-pane>

      <el-tab-pane :label="$t('api_test.definition.request.other_config')" name="advancedConfig" v-if="request.body_config">
        <el-table :data="request.body_config">
          <el-table-column prop="columnTitle" :label="$t('operating_log.change_field')"/>
          <el-table-column prop="originalValue" :label="$t('operating_log.before_change')">
            <template v-slot:default="scope">
              <el-tooltip :content="scope.row.originalValue">
                <div class="current-value ms-tag-del">{{ scope.row.originalValue }}</div>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column prop="newValue" :label="$t('operating_log.after_change')">
            <template v-slot:default="scope">
              <el-tooltip :content="scope.row.newValue">
                <div class="current-value ms-tag-add">{{ scope.row.newValue }}</div>
              </el-tooltip>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
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
    this.active();
  },
  watch: {
    'request.headerId'() {
      this.active();
    }
  },
  methods: {
    active() {
      if (this.request.header) {
        this.activeName = "headers";
      } else if (this.request.query) {
        this.activeName = "parameters";
      } else if (this.request.rest) {
        this.activeName = "rest";
      } else if (this.request.body && (this.request.body.jsonSchema || this.request.body.form || this.request.body.raw_1 || this.request.body.raw_2)) {
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
      } else if (this.request.body_config) {
        this.activeName = "advancedConfig";
      } else if (this.request.body_auth) {
        this.activeName = "authConfig";
      } else if (this.request.statusCode) {
        this.activeName = "statusCode";
      }
      if (this.request.body && (this.request.body.jsonSchema || this.request.body.form || this.request.body.raw_1 || this.request.body.raw_2)) {
        if (this.request.body.jsonSchema) {
          this.activeBody = "json";
        }
        if (this.request.body.form) {
          this.activeBody = "form";
        }
        if (this.request.body.raw_1 || this.request.body.raw_2) {
          this.activeBody = "raw";
        }
      }
      this.reloadCodeEdit();
    },
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

.current-value {
  display: inline-block;
  overflow-x: hidden;
  padding-bottom: 0;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
  width: 120px;
}

.ms-tag-del {
  text-decoration: line-through;
  text-decoration-color: red;
  -moz-text-decoration-line: line-through;
  background: #F3E6E7;
}

.ms-tag-add {
  background: #E2ECDC;
}

@import "~jsondiffpatch/dist/formatters-styles/html.css";
@import "~jsondiffpatch/dist/formatters-styles/annotated.css";

</style>
