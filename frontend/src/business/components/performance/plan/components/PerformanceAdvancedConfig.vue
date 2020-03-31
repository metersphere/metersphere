<template>
  <div>

    <el-row type="flex" justify="start">
      <el-col :span="8">
        <h3>{{$t('load_test.domain_bind')}}</h3>
      </el-col>
      <el-col :span="8">
        <el-button type="primary" plain size="mini" @click="add('domains')">{{$t('commons.add')}}</el-button>
      </el-col>
    </el-row>
    <!-- -->
    <el-row>
      <el-table :data="domains">
        <el-table-column
          :label="$t('load_test.domain')"
          show-overflow-tooltip>
          <template slot-scope="{row}">
            <template v-if="row.edit">
              <el-input v-model="row.domain" class="edit-input" size="mini"/>
            </template>
            <span v-else>{{ row.domain }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('load_test.enable')"
          show-overflow-tooltip>
          <template slot-scope="{row}">
            <el-switch
              :disabled="!row.edit"
              size="mini"
              v-model="row.enable"
              active-color="#13ce66"
              inactive-color="#ff4949">
            </el-switch>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('load_test.ip')"
          show-overflow-tooltip>
          <template slot-scope="{row}">
            <template v-if="row.edit">
              <el-input v-model="row.ip" class="edit-input" size="mini"/>
            </template>
            <span v-else>{{ row.ip }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center">
          <template slot-scope="{row, $index}">
            <template v-if="row.edit">
              <el-button
                class="cancel-btn"
                size="mini"
                icon="el-icon-refresh"
                type="warning"
                circle
                @click="cancelEdit(row)">
              </el-button>
              <el-button
                type="success"
                size="mini"
                icon="el-icon-circle-check"
                circle
                @click="confirmEdit(row)">
              </el-button>
            </template>
            <el-button
              v-else
              type="primary"
              size="mini"
              icon="el-icon-edit"
              circle
              @click="edit(row)">
            </el-button>
            <el-button
              type="danger"
              size="mini"
              icon="el-icon-delete"
              circle
              @click="del(row, 'domains', $index)">
            </el-button>
          </template>
        </el-table-column>
      </el-table>

    </el-row>

    <el-row>
      <el-col :span="8">
        <h3>{{$t('load_test.params')}}</h3>
      </el-col>
      <el-col :span="8">
        <el-button type="primary" plain size="mini" @click="add('params')">{{$t('commons.add')}}</el-button>
      </el-col>
    </el-row>
    <!-- -->
    <el-row>
      <el-table :data="params">
        <el-table-column
          :label="$t('load_test.param_name')"
          show-overflow-tooltip>
          <template slot-scope="{row}">
            <template v-if="row.edit">
              <el-input v-model="row.name" class="edit-input" size="mini"/>
            </template>
            <span v-else>{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('load_test.enable')"
          show-overflow-tooltip>
          <template slot-scope="{row}">
            <el-switch
              :disabled="!row.edit"
              size="mini"
              v-model="row.enable"
              active-color="#13ce66"
              inactive-color="#ff4949">
            </el-switch>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('load_test.param_value')"
          show-overflow-tooltip>
          <template slot-scope="{row}">
            <template v-if="row.edit">
              <el-input v-model="row.value" class="edit-input" size="mini"/>
            </template>
            <span v-else>{{ row.value }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center">
          <template slot-scope="{row, $index}">
            <template v-if="row.edit">
              <el-button
                class="cancel-btn"
                size="mini"
                icon="el-icon-refresh"
                type="warning"
                circle
                @click="cancelEdit(row)">
              </el-button>
              <el-button
                type="success"
                size="mini"
                icon="el-icon-circle-check"
                circle
                @click="confirmEdit(row)">
              </el-button>
            </template>
            <el-button
              v-else
              type="primary"
              size="mini"
              icon="el-icon-edit"
              circle
              @click="edit(row)">
            </el-button>
            <el-button
              type="danger"
              size="mini"
              icon="el-icon-delete"
              circle
              @click="del(row, 'params', $index)">
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-row>

    <el-row>
      <el-col :span="8">
        <el-form :inline="true">
          <el-form-item>
            <div>{{$t('load_test.connect_timeout')}}</div>
          </el-form-item>
          <el-form-item>
            <el-input-number size="mini" v-model="timeout" :min="10" :max="100000"></el-input-number>
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
            <div>{{$t('load_test.custom_http_code')}}</div>
          </el-form-item>
          <el-form-item>
            <el-input size="mini" v-model="statusCodeStr" :placeholder="$t('load_test.separated_by_commas')"
                      @input="checkStatusCode"></el-input>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
  </div>
</template>

<script>
  export default {
    name: "PerformanceAdvancedConfig",
    data() {
      return {
        timeout: 10,
        statusCode: [],
        domains: [],
        params: [],
        statusCodeStr: '',
      }
    },
    mounted() {
      let testId = this.$route.path.split('/')[4];
      if (testId) {
        this.getAdvancedConfig(testId);
      }
    },
    watch: {
      '$route'(to) {
        if (to.name !== 'createPerTest' && to.name !== 'editPerTest') {
          return;
        }
        let testId = to.path.split('/')[4];
        if (testId) {
          this.getAdvancedConfig(testId);
        }
      }
    },
    methods: {
      revertObject(row) {
        Object.keys(row).forEach(function (key) {
          row[key] = row[key + 'Origin'];
        });
      },
      saveOriginObject(row) {
        Object.keys(row).forEach(function (key) {
          row[key + 'Origin'] = row[key];
        });
      },
      delOriginObject(row) {
        Object.keys(row).forEach(function (key) {
          delete row[key + 'Origin'];
        });
      },
      getAdvancedConfig(testId) {
        this.$get('/testplan/get-advanced-config/' + testId, (response) => {
          if (response.data) {
            let data = JSON.parse(response.data);
            this.timeout = data.timeout || 10;
            this.statusCode = data.statusCode || [];
            this.statusCodeStr = this.statusCode.join(',');
            this.domains = data.domains || [];
            this.params = data.params || [];
            this.domains.forEach(d => d.edit = false);
            this.params.forEach(d => d.edit = false);
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
        this.saveOriginObject(row);
        row.edit = !row.edit
      },
      del(row, dataName, index) {
        this[dataName].splice(index, 1);
      },
      cancelEdit(row) {
        row.edit = false;
        // rollback changes
        this.revertObject(row);
      },
      confirmEdit(row) {
        row.edit = false;
        this.saveOriginObject(row);
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
            this.$message.error(this.$t('load_test.domain_is_duplicate'));
            return false;
          }
        }
        counts = this.groupBy(this.params, 'name');
        for (let c in counts) {
          if (counts[c] > 1) {
            this.$message.error(this.$t('load_test.param_is_duplicate'));
            return false;
          }
        }
        if (this.domains.filter(d => !d.domain || !d.ip).length > 0) {
          this.$message.error(this.$t('load_test.domain_ip_is_empty'));
          return false;
        }
        if (this.params.filter(d => !d.name || !d.value).length > 0) {
          this.$message.error(this.$t('load_test.param_name_value_is_empty'));
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
        this.domains.forEach(d => this.delOriginObject(d));
        this.params.forEach(d => this.delOriginObject(d));
        let statusCode = [];
        if (this.statusCodeStr) {
          statusCode = this.statusCodeStr.split(',');
        }
        return {
          timeout: this.timeout,
          statusCode: statusCode,
          params: this.params,
          domains: this.domains,
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
    padding-right: 100px;
  }

</style>
