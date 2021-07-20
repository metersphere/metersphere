<template>
  <div>
    <!--  基本配置  -->
    <el-row>
      <el-col :span="6">
        <el-form :inline="true">
          <el-form-item>
            <div>{{ $t('load_test.connect_timeout') }}</div>
          </el-form-item>
          <el-form-item>
            <el-input-number
                :disabled="readOnly" size="mini" v-model="timeout"
                :min="0"/>
          </el-form-item>
          <el-form-item>
            ms
          </el-form-item>
        </el-form>
      </el-col>
      <el-col :span="6">
        <el-form :inline="true">
          <el-form-item>
            <div>{{ $t('load_test.response_timeout') }}</div>
          </el-form-item>
          <el-form-item>
            <el-input-number
                :disabled="readOnly" size="mini" :min="0"
                v-model="responseTimeout"/>
          </el-form-item>
          <el-form-item>
            ms
          </el-form-item>
        </el-form>
      </el-col>
      <el-col :span="6">
        <el-form :inline="true">
          <el-form-item>
            <div>
              {{ $t('load_test.granularity') }}
              <el-popover
                  placement="left"
                  width="300"
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
            <el-select v-model="granularity" :placeholder="$t('commons.please_select')" size="mini"
                       clearable>
              <el-option v-for="op in granularityData" :key="op.granularity" :label="op.granularity"
                         :value="op.granularity"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </el-col>
      <el-col :span="6">
        <el-form :inline="true">
          <el-form-item>
            <div>{{ $t('load_test.custom_http_code') }}</div>
          </el-form-item>
          <el-form-item>
            <el-input
                :disabled="readOnly" size="mini" v-model="statusCodeStr"
                :placeholder="$t('load_test.separated_by_commas')"
                @input="checkStatusCode"></el-input>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
    <!--  csv 配置  -->
    <el-row>
      <el-col :span="8">
        <h3>CSVDataSet</h3>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="24">
        <el-table :data="csvFiles" size="mini" class="tb-edit" align="center" border highlight-current-row>
          <el-table-column
              align="center"
              prop="name"
              :label="$t('commons.name')">
          </el-table-column>
          <el-table-column align="center" prop="csvSplit" :label="$t('load_test.csv_split')">
            <template v-slot:default="{row}">
              <el-switch :disabled="readOnly" v-model="row.csvSplit"/>
            </template>
          </el-table-column>
          <el-table-column align="center" prop="csvHasHeader" :label="$t('load_test.csv_has_header')">
            <template v-slot:default="{row}">
              <el-switch :disabled="readOnly || !row.csvSplit" v-model="row.csvHasHeader"/>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>

    <!-- 参数列表 -->
    <el-row>
      <el-col :span="8">
        <h3>{{ $t('load_test.params') }}</h3>
        <el-button :disabled="readOnly" icon="el-icon-circle-plus-outline" plain size="mini" @click="add('params')">
          {{ $t('commons.add') }}
        </el-button>
      </el-col>
    </el-row>
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

    <!-- JMeter Properties -->
    <el-row>
      <el-col :span="8">
        <h3>JMeter Properties</h3>
        <el-button :disabled="readOnly" icon="el-icon-circle-plus-outline" plain size="mini" @click="add('properties')">
          {{ $t('commons.add') }}
        </el-button>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="24">
        <el-table :data="properties" size="mini" class="tb-edit" align="center" border highlight-current-row>
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
                                        @exec="del(row, 'properties', $index)"/>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>

    <!-- 监控配置   -->
    <el-row>
      <el-col :span="8">
        <h3>{{ $t('commons.monitor') }}</h3>
        <el-button :disabled="readOnly" icon="el-icon-circle-plus-outline" plain size="mini" @click="addMonitor">
          {{ $t('commons.add') }}
        </el-button>
        <el-button :disabled="readOnly" icon="el-icon-circle-plus-outline" plain size="mini"
                   @click="batchAddMonitor">
          {{ $t('commons.batch_add') }}
        </el-button>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="24">
        <el-table :data="monitorParams" size="mini" class="tb-edit" border highlight-current-row>
          <el-table-column
              align="center"
              prop="name"
              :label="$t('commons.name')">
          </el-table-column>
          <!--        <el-table-column-->
          <!--          align="center"-->
          <!--          prop="environmentName"-->
          <!--          label="所属环境">-->
          <!--        </el-table-column>-->
          <!--        <el-table-column-->
          <!--          align="center"-->
          <!--          prop="authStatus"-->
          <!--          label="认证状态">-->
          <!--        </el-table-column>-->
          <!--        <el-table-column-->
          <!--          align="center"-->
          <!--          prop="monitorStatus"-->
          <!--          label="监控状态">-->
          <el-table-column
              align="center"
              prop="ip"
              label="IP">
          </el-table-column>
          <el-table-column
              align="center"
              prop="port"
              label="Port">
          </el-table-column>
          <el-table-column
              align="center"
              prop="description"
              :label="$t('commons.description')">
          </el-table-column>
          <el-table-column align="center" :label="$t('load_test.operating')">
            <template v-slot:default="{row, $index}">
              <ms-table-operator-button :disabled="readOnly" tip="编辑" icon="el-icon-edit"
                                        type="primary"
                                        @exec="modifyMonitor(row, $index)"/>
              <ms-table-operator-button :disabled="readOnly" :tip="$t('commons.delete')" icon="el-icon-delete"
                                        type="danger"
                                        @exec="delMonitor(row, $index)"/>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>

    <edit-monitor ref="monitorDialog" :testId="testId" :list.sync="monitorParams"/>
    <batch-add-monitor ref="batchMonitorDialog" @batchSave="batchSave"/>
  </div>
</template>

<script>
import MsTableOperatorButton from "../../../common/components/MsTableOperatorButton";
import EditMonitor from "@/business/components/performance/test/components/EditMonitor";
import {hasPermission} from "@/common/js/utils";
import BatchAddMonitor from "@/business/components/performance/test/components/BatchAddMonitor";

export default {
  name: "PerformanceAdvancedConfig",
  components: {BatchAddMonitor, EditMonitor, MsTableOperatorButton},
  data() {
    return {
      timeout: undefined,
      responseTimeout: undefined,
      statusCode: [],
      domains: [],
      params: [],
      properties: [],
      monitorParams: [],
      csvFiles: [],
      csvConfig: [],
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
      ],
      readOnly: false,
    };
  },
  props: {
    testId: String,
  },
  mounted() {
    if (this.testId) {
      this.getAdvancedConfig();
    }
    this.readOnly = !hasPermission('PROJECT_PERFORMANCE_TEST:READ+EDIT');
  },
  watch: {
    testId() {
      if (this.testId) {
        this.getAdvancedConfig();
      }
    },
    csvFiles() {
      if (this.csvConfig && this.csvFiles) {
        this.csvFiles.forEach(f => {
          f.csvSplit = this.csvConfig[f.name]?.csvSplit;
          f.csvHasHeader = this.csvConfig[f.name]?.csvHasHeader;
        });
      }
    }
  },
  methods: {
    getAdvancedConfig() {
      this.$get('/performance/get-advanced-config/' + this.testId, (response) => {
        if (response.data) {
          let data = JSON.parse(response.data);
          this.timeout = data.timeout;
          this.responseTimeout = data.responseTimeout;
          this.statusCode = data.statusCode || [];
          this.statusCodeStr = this.statusCode.join(',');
          this.domains = data.domains || [];
          this.params = data.params || [];
          this.granularity = data.granularity;
          this.monitorParams = data.monitorParams || [];
          this.properties = data.properties || [];
          this.csvConfig = data.csvConfig;
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
      if (dataName === 'properties') {
        this[dataName].push({
          name: 'prop1',
          enable: true,
          value: '0',
          edit: true,
        });
      }
    },
    edit(row) {
      row.edit = !row.edit;
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
      counts = this.groupBy(this.properties, 'name');
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
        properties: this.properties,
        csvConfig: this.csvFiles.reduce((result, curr) => {
          result[curr.name] = {csvHasHeader: curr.csvHasHeader, csvSplit: curr.csvSplit};
          return result;
        }, {}),
        domains: this.domains,
        granularity: this.granularity,
        monitorParams: this.monitorParams
      };
    },
    addMonitor() {
      this.$refs.monitorDialog.open();
    },
    batchAddMonitor() {
      this.$refs.batchMonitorDialog.open();
    },
    batchSave(params) {
      let targets = this._handleBatchVars(params);
      targets.forEach(row => {
        this.monitorParams.push(row);
      });
    },
    _handleBatchVars(data) {
      let params = data.split("\n");
      let keyValues = [];
      params.forEach(item => {
        let line = item.split(/，|,/);
        if (line.length < 3) {
          return;
        }
        keyValues.push({
          name: line[0],
          ip: line[1],
          port: line[2],
          description: line[3] || '',
        });
      });
      return keyValues;
    },
    modifyMonitor(row, index) {
      this.$refs.monitorDialog.open(row, index);
    },
    delMonitor(row, index) {
      this.monitorParams.splice(index, 1);
    },
    refreshStatus() {

    },
  }
};
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
