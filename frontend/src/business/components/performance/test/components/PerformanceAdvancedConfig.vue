<template>
  <div>
    <el-row>
      <el-col :span="8">
        <h3>{{ $t('load_test.params') }}</h3>
        <el-button :disabled="readOnly" icon="el-icon-circle-plus-outline" plain size="mini" @click="add('params')">
          {{ $t('commons.add') }}
        </el-button>
      </el-col>
    </el-row>
    <!-- -->
    <el-row>
      <el-col :span="24">
        <el-table :data="params" size="mini" class="tb-edit" align="center" border highlight-current-row>
          <el-table-column
            align="center"
            :label="$t('load_test.param_name')"
            show-overflow-tooltip>
            <template v-slot:default="{row}">
              <el-input
                size="mini"
                v-if="!readOnly"
                type="textarea"
                :rows="1"
                class="edit-input"
                v-model="row.name"
                :placeholder="$t('load_test.param_name')"
                clearable>
              </el-input>
              <span>{{ row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column
            align="center"
            :label="$t('load_test.enable')"
            show-overflow-tooltip>
            <template v-slot:default="{row}">
              <el-switch
                :disabled="!row.edit || readOnly"
                size="mini"
                v-model="row.enable"
                inactive-color="#DCDFE6">
              </el-switch>
            </template>
          </el-table-column>
          <el-table-column
            :label="$t('load_test.param_value')"
            show-overflow-tooltip align="center">
            <template v-slot:default="{row}">
              <!-- <template v-if="row.edit">
                 <el-input v-model="row.value" class="edit-input" size="mini"/>
               </template>
               <span v-else>{{ row.value }}</span>-->
              <el-input
                size="mini"
                v-if="!readOnly"
                type="textarea"
                class="edit-input"
                :rows="1"
                v-model="row.value"
                :placeholder="$t('load_test.param_value')"
                clearable></el-input>
              <span>{{ row.value }}</span>
            </template>
          </el-table-column>
          <el-table-column align="center" :label="$t('load_test.operating')">
            <template v-slot:default="{row, $index}">
              <ms-table-operator-button :disabled="readOnly" :tip="$t('commons.delete')" icon="el-icon-delete"
                                        type="danger"
                                        @exec="del(row, 'params', $index)"/>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>

    <el-row>
      <el-col :span="8">
        <el-form :inline="true">
          <el-form-item>
            <div>{{ $t('load_test.connect_timeout') }}</div>
          </el-form-item>
          <el-form-item>
            <el-input-number :disabled="readOnly" size="mini" v-model="timeout" :min="10"
                             :max="100000"></el-input-number>
          </el-form-item>
          <el-form-item>
            ms
          </el-form-item>
        </el-form>
      </el-col>
      <el-col :span="8">
        <el-form :inline="true">
          <el-form-item>
            <div>{{ $t('load_test.response_timeout') }}</div>
          </el-form-item>
          <el-form-item>
            <el-input-number :disabled="readOnly" size="mini" v-model="responseTimeout"></el-input-number>
          </el-form-item>
          <el-form-item>
            ms
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="8">
        <el-form :inline="true">
          <el-form-item>
            <div>{{ $t('load_test.custom_http_code') }}</div>
          </el-form-item>
          <el-form-item>
            <el-input :disabled="readOnly" size="mini" v-model="statusCodeStr"
                      :placeholder="$t('load_test.separated_by_commas')"
                      @input="checkStatusCode"></el-input>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="8">
        <el-form :inline="true">
          <el-form-item>
            <div>
              {{ $t('load_test.granularity') }}
              <el-popover
                placement="bottom"
                width="400"
                trigger="hover">
                <el-table :data="granularityData">
                  <el-table-column property="start" :label="$t('load_test.duration')">
                    <template v-slot:default="scope">
                      <span>{{ scope.row.start }}S - {{ scope.row.end }}S</span>
                    </template>
                  </el-table-column>
                  <el-table-column property="granularity" :label="$t('load_test.granularity')"/>
                </el-table>
                <i slot="reference" class="el-icon-info pointer"/>
              </el-popover>
            </div>
          </el-form-item>
          <el-form-item>
            <el-select v-model="granularity" :placeholder="$t('commons.please_select')" size="mini" clearable>
              <el-option v-for="op in granularityData" :key="op.granularity" :label="op.granularity" :value="op.granularity"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import MsTableOperatorButton from "../../../common/components/MsTableOperatorButton";

export default {
  name: "PerformanceAdvancedConfig",
  components: {MsTableOperatorButton},
  data() {
    return {
      timeout: 60000,
      responseTimeout: null,
      statusCode: [],
      domains: [],
      params: [],
      statusCodeStr: '',
      granularity: undefined,
      granularityData: [
        {start: 0, end: 100, granularity: 1},
        {start: 101, end: 500, granularity: 5},
        {start: 501, end: 1000, granularity: 10},
        {start: 1001, end: 3000, granularity: 30},
        {start: 3001, end: 6000, granularity: 60},
        {start: 6001, end: 30000, granularity: 300},
        {start: 30001, end: 60000, granularity: 600},
        {start: 60001, end: 180000, granularity: 1800},
        {start: 180001, end: 360000, granularity: 3600},
      ]
    }
  },
  props: {
    readOnly: {
      type: Boolean,
      default: false
    },
    testId: String,
  },
  mounted() {
    if (this.testId) {
      this.getAdvancedConfig();
    }
  },
  watch: {
    testId() {
      if (this.testId) {
        this.getAdvancedConfig();
      }
    }
  },
  methods: {
    getAdvancedConfig() {
      this.$get('/performance/get-advanced-config/' + this.testId, (response) => {
        if (response.data) {
          let data = JSON.parse(response.data);
          this.timeout = data.timeout || 10;
          this.responseTimeout = data.responseTimeout;
          this.statusCode = data.statusCode || [];
          this.statusCodeStr = this.statusCode.join(',');
          this.domains = data.domains || [];
          this.params = data.params || [];
          this.granularity = data.granularity;
        }
      });
    },
    add(dataName) {
      if (dataName === 'domains') {
        this[dataName].push({
          domain: 'fit2cloud.com',
          enable: true,
          ip: '127.0.0.1',
          edit: true,
        });
      }
      if (dataName === 'params') {
        this[dataName].push({
          name: 'param1',
          enable: true,
          value: '0',
          edit: true,
        });
      }
    },
    edit(row) {
      row.edit = !row.edit
    },
    del(row, dataName, index) {
      this[dataName].splice(index, 1);
    },
    confirmEdit(row) {
      row.edit = false;
      row.enable = true;
    },
    groupBy(data, key) {
      return data.reduce((p, c) => {
        let name = c[key];
        if (!p.hasOwnProperty(name)) {
          p[name] = 0;
        }
        p[name]++;
        return p;
      }, {});
    },
    validConfig() {
      let counts = this.groupBy(this.domains, 'domain');
      for (let c in counts) {
        if (counts[c] > 1) {
          this.$error(this.$t('load_test.domain_is_duplicate'));
          return false;
        }
      }
      counts = this.groupBy(this.params, 'name');
      for (let c in counts) {
        if (counts[c] > 1) {
          this.$error(this.$t('load_test.param_is_duplicate'));
          return false;
        }
      }
      if (this.domains.filter(d => !d.domain || !d.ip).length > 0) {
        this.$error(this.$t('load_test.domain_ip_is_empty'));
        return false;
      }
      if (this.params.filter(d => !d.name || !d.value).length > 0) {
        this.$error(this.$t('load_test.param_name_value_is_empty'));
        return false;
      }
      return true;
    },
    checkStatusCode() {
      let license_num = this.statusCodeStr;
      license_num = license_num.replace(/[^\d,]/g, ''); // 清除“数字”和“.”以外的字符
      this.statusCodeStr = license_num;
    },
    cancelAllEdit() {
      this.domains.forEach(d => d.edit = false);
      this.params.forEach(d => d.edit = false);
    },
    configurations() {
      let statusCode = [];
      if (this.statusCodeStr) {
        statusCode = this.statusCodeStr.split(',');
      }
      return {
        timeout: this.timeout,
        responseTimeout: this.responseTimeout,
        statusCode: statusCode,
        params: this.params,
        domains: this.domains,
        granularity: this.granularity,
      };
    },
  }
}
</script>

<style scoped>
.el-row {
  margin-bottom: 10px;
}

.edit-input {
  padding-right: 0px;
}

.tb-edit .el-textarea {
  display: none;
}

.tb-edit .current-row .el-textarea {
  display: block;
}

.tb-edit .current-row .el-textarea + span {
  display: none;
}

.el-col {
  text-align: left;
}

.el-col .el-table {
  align: center;
}

.pointer {
  cursor: pointer;
}

</style>
