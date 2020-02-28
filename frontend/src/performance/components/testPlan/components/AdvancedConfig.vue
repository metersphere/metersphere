<template>
  <div class="el-tab-pane-box">

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
            <template v-if="row.edit">
              <el-switch
                size="mini"
                v-model="row.enable"
                active-color="#13ce66"
                inactive-color="#ff4949">
              </el-switch>
            </template>
            <span v-else>{{ row.enable }}</span>
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
          <template slot-scope="{row}">
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
              @click="del(row, 'domains', 'domain')">
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
            <template v-if="row.edit">
              <el-switch
                size="mini"
                v-model="row.enable"
                active-color="#13ce66"
                inactive-color="#ff4949">
              </el-switch>
            </template>
            <span v-else>{{ row.enable }}</span>
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
          <template slot-scope="{row}">
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
              @click="del(row, 'params', 'name')">
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-row>

    <el-row>
      <el-col :span="8">
        建立连接超时时间 10 ms
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="8">
        自定义 HTTP 响应成功状态码 302
      </el-col>
    </el-row>
  </div>
</template>

<script>
  export default {
    name: "MsTestPlanAdvancedConfig",
    data() {
      return {
        domains: [
          {domain: 'baidu.com0', enable: true, ip: '127.0.0.1', edit: false},
          {domain: 'baidu.com1', enable: true, ip: '127.0.0.1', edit: false},
          {domain: 'baidu.com2', enable: true, ip: '127.0.0.1', edit: false},
          {domain: 'baidu.com3', enable: true, ip: '127.0.0.1', edit: false},
        ],
        params: [
          {name: 'param1', value: '13134', enable: true, edit: false},
          {name: 'param2', value: '13134', enable: true, edit: false},
          {name: 'param3', value: '13134', enable: true, edit: false},
          {name: 'param4', value: '13134', enable: true, edit: false},
        ]
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
      add(dataName) {
        if (dataName === 'domains') {
          this[dataName].push({
            domain: '',
            enable: true,
            ip: '',
            edit: false,
          });
        }
        if (dataName === 'params') {
          this[dataName].push({
            name: '',
            enable: true,
            value: '',
            edit: false,
          });
        }
      },
      edit(row) {
        this.saveOriginObject(row);
        row.edit = !row.edit
      },
      del(row, dataName, id) {
        this[dataName] = this[dataName].filter((d) => d[id] !== row[id]);
      },
      cancelEdit(row) {
        row.edit = false;
        // rollback changes
        this.revertObject(row);
        this.$message({
          message: 'The row has been restored to the original value',
          type: 'warning'
        })
      },
      confirmEdit(row) {
        row.edit = false;
        this.saveOriginObject(row);
        this.$message({
          message: 'The row has been edited',
          type: 'success'
        })
      },
      validConfig() {
        return this.domains.filter(d => !d.domain || !d.ip).length === 0
          &&
          this.params.filter(d => !d.name || !d.value).length === 0;
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

  .container-tab >>> .el-tabs__content {
    flex-grow: 1;
    overflow-y: scroll;
  }
</style>
