<template>
  <div v-loading="isReloadData">
    <el-tabs v-model="activeName">
      <el-tab-pane :label="$t('api_test.definition.request.req_param')" name="parameters" v-if="request.base && request.base.length > 0">
        <el-table :data="request.base">
          <el-table-column prop="columnTitle" :label="$t('operating_log.change_field')">
          </el-table-column>
          <el-table-column prop="originalValue" :label="$t('operating_log.before_change')">
          </el-table-column>
          <el-table-column prop="newValue" :label="$t('operating_log.after_change')">
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.scenario.variables')" name="variables" v-if="request.variables && request.variables.length >0">
        <ms-api-key-value-detail :items="request.variables" :showDesc="true" :format="request.headerId"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.request.sql.sql_script')" name="sql" v-if="request.query_1 || request.query_2">
        <pre v-html="getDiff(request.query_2,request.query_1)"/>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import MsApiKeyValueDetail from "./common/ApiKeyValueDetail";

const jsondiffpatch = require('jsondiffpatch');
const formattersHtml = jsondiffpatch.formatters.html;

export default {
  name: "MsApiJdbcParameters",
  components: {MsApiKeyValueDetail},
  props: {
    request: {},
    basisData: {},
    moduleOptions: Array,
    showScript: {
      type: Boolean,
      default: true,
    },
    isReadOnly: {
      type: Boolean,
      default: false
    },
  },
  data() {
    return {
      spanNum: 21,
      environments: [],
      currentEnvironment: {},
      databaseConfigsOptions: [],
      isReloadData: false,
      activeName: "variables",
      rules: {},
    }
  },
  watch: {
    'request.headerId'() {
      if (this.request.base) {
        this.activeName = "parameters";
      } else if (this.request.variables) {
        this.activeName = "variables";
      } else if (this.request.query_1 || this.request.query_2) {
        this.activeName = "sql";
      }
    }
  },
  created() {
    if (this.request.base) {
      this.activeName = "parameters";
    } else if (this.request.variables) {
      this.activeName = "variables";
    } else if (this.request.query_1 || this.request.query_2) {
      this.activeName = "sql";
    }
  },
  computed: {},
  methods: {
    getDiff(v1, v2) {
      let delta = jsondiffpatch.diff(v1, v2);
      return formattersHtml.format(delta, v1);
    },
  }
}
</script>

<style scoped>

.one-row .el-form-item {
  display: inline-block;
}

.one-row .el-form-item:nth-child(2) {
  margin-left: 60px;
}

@import "~jsondiffpatch/dist/formatters-styles/html.css";
@import "~jsondiffpatch/dist/formatters-styles/annotated.css";

</style>
