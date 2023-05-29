<template>
  <div>
    <div>
      <div style="padding-bottom: 10px; float: left">
        <el-input
          :placeholder="$t('api_test.search_by_variables')"
          size="mini"
          v-model="selectVariable"
          @change="filter"
          @keyup.enter="filter">
        </el-input>
      </div>
      <div style="padding-bottom: 10px; float: right">
        <ms-table-button
          v-permission="['PROJECT_ENVIRONMENT:READ+IMPORT']"
          icon="el-icon-box"
          :content="$t('commons.import')"
          @click="importJSON" />
        <el-dropdown
          @command="handleExportCommand"
          class="scenario-ext-btn"
          trigger="hover"
          v-permission="['PROJECT_ENVIRONMENT:READ+EXPORT']">
          <ms-table-button style="margin-left: 10px" icon="el-icon-box" :content="$t('commons.export')" />
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="exportApi">{{ $t('envrionment.export_variable_tip') }}</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>

        <el-link style="margin-left: 10px" @click="batchAdd" type="primary" :disabled="isReadOnly">
          {{ $t('commons.batch_add') }}
        </el-link>
      </div>
    </div>
    <div
      style="
        border: 1px #dcdfe6 solid;
        min-height: 300px;
        border-radius: 4px;
        width: 99%;
        margin-top: 10px;
        clear: both;
      ">
      <ms-table
        v-loading="loading"
        row-key="id"
        :data="variables"
        :screen-height="screenHeight"
        :batch-operators="batchButtons"
        :remember-order="true"
        :highlightCurrentRow="true"
        :page-size="pageSize"
        :total="total"
        enableSelection
        :condition="condition"
        @refresh="onChange"
        ref="variableTable">
        <ms-table-column prop="num" sortable label="ID" min-width="60" />
        <ms-table-column
          prop="scope"
          sortable
          :label="$t('commons.scope')"
          :filters="scopeTypeFilters"
          :filter-method="filterScope"
          min-width="120">
          <template slot-scope="scope">
            <el-select
              v-model="scope.row.scope"
              :placeholder="$t('commons.please_select')"
              size="mini"
              @change="changeType(scope.row)">
              <el-option v-for="item in scopeTypeFilters" :key="item.value" :label="item.text" :value="item.value" />
            </el-select>
          </template>
        </ms-table-column>

        <ms-table-column prop="name" :label="$t('api_test.variable_name')" min-width="200" sortable>
          <template slot-scope="scope">
            <el-input
              v-model="scope.row.name"
              size="mini"
              maxlength="200"
              :placeholder="$t('api_test.variable_name')"
              show-word-limit
              @change="change" />
          </template>
        </ms-table-column>
        <ms-table-column prop="type" :label="$t('test_track.case.type')" min-width="140" sortable>
          <template slot-scope="scope">
            <el-select
              v-model="scope.row.type"
              v-if="!scope.row.scope || scope.row.scope == 'api'"
              :placeholder="$t('commons.please_select')"
              size="mini"
              @change="changeType(scope.row)">
              <el-option v-for="item in typeSelectOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>

            <el-select v-else v-model="scope.row.type" :placeholder="$t('commons.please_select')" size="mini">
              <el-option
                v-for="item in uiTypeSelectOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value" />
            </el-select>
          </template>
        </ms-table-column>

        <el-table-column prop="value" :label="$t('api_test.value')" min-width="200px" sortable show-overflow-tooltip>
          <template slot-scope="scope">
            <el-input
              v-model="scope.row.value"
              size="mini"
              v-if="scope.row.type !== 'CSV'"
              :placeholder="valueText(scope.row)"
              :disabled="scope.row.type === 'COUNTER' || scope.row.type === 'RANDOM'" />
            <csv-file-upload :parameter="scope.row" v-if="scope.row.type === 'CSV'" />
          </template>
        </el-table-column>
        <ms-table-column prop="description" :label="$t('commons.remark')" min-width="160" sortable>
          <template slot-scope="scope">
            <el-input v-model="scope.row.description" size="mini" />
          </template>
        </ms-table-column>

        <ms-table-column :label="$t('commons.operating')" width="150">
          <template v-slot:default="scope">
            <span>
              <el-switch v-model="scope.row.enable" size="mini" />
              <el-button
                icon="el-icon-delete"
                type="danger"
                circle
                size="mini"
                style="margin-left: 10px"
                @click="remove(scope.row)"
                v-if="isDisable(scope.row)" />
              <el-button
                v-if="(!scope.row.scope || scope.row.scope == 'api') && scope.row.type !== 'LIST'"
                icon="el-icon-setting"
                circle
                size="mini"
                style="margin-left: 10px"
                @click="openSetting(scope.row)"
                @blur="change"/>
            </span>
          </template>
        </ms-table-column>
      </ms-table>
      <ms-table-pagination
        :change="nextPage"
        :current-page.sync="currentPage"
        :page-size.sync="pageSize"
        :total="total" />
    </div>
    <batch-add-parameter @batchSave="batchSave" ref="batchAdd" />
    <api-variable-setting ref="apiVariableSetting" @changeData="change"></api-variable-setting>
    <variable-import ref="variableImport" @mergeData="mergeData"></variable-import>
  </div>
</template>

<script>
import { KeyValue } from '../../../model/EnvTestModel';
import MsApiVariableInput from './ApiVariableInput';
import BatchAddParameter from './BatchAddParameter';
import MsTableButton from '../../MsTableButton';
import MsTable from '../../table/MsTable';
import MsTableColumn from '../../table/MsTableColumn';
import ApiVariableSetting from './ApiVariableSetting';
import CsvFileUpload from './variable/CsvFileUpload';
import { downloadFile, getUUID, operationConfirm } from '../../../utils';
import VariableImport from './variable/VariableImport';
import {forEach} from 'lodash-es';
import MsTablePagination from '../../pagination/TablePagination';

export default {
  name: 'MsApiScenarioVariables',
  components: {
    BatchAddParameter,
    MsApiVariableInput,
    MsTableButton,
    MsTable,
    MsTableColumn,
    ApiVariableSetting,
    CsvFileUpload,
    VariableImport,
    MsTablePagination,
  },
  props: {
    items: Array,
    isReadOnly: {
      type: Boolean,
      default: false,
    },
    showVariable: {
      type: Boolean,
      default: true,
    },
    showCopy: {
      type: Boolean,
      default: true,
    },
  },
  data() {
    return {
      currentPage: 1,
      pageSize: 10,
      total: 0,
      loading: false,
      screenHeight: '460px',
      batchButtons: [
        {
          name: this.$t('api_test.definition.request.batch_delete'),
          handleClick: this.handleDeleteBatch,
        },
      ],
      typeSelectOptions: [
        { value: 'CONSTANT', label: this.$t('api_test.automation.constant') },
        { value: 'LIST', label: this.$t('test_track.case.list') },
        { value: 'CSV', label: 'CSV' },
        { value: 'COUNTER', label: this.$t('api_test.automation.counter') },
        { value: 'RANDOM', label: this.$t('api_test.automation.random') },
      ],
      uiTypeSelectOptions: [
        { value: 'STRING', label: this.$t('api_test.automation.string') },
        { value: 'ARRAY', label: this.$t('api_test.automation.array') },
        { value: 'JSON', label: this.$t('api_test.automation.json') },
        { value: 'NUMBER', label: this.$t('api_test.automation.number') },
      ],
      variables: {},
      selectVariable: '',
      editData: {},
      allData: [],
      lastPage: 1,
      scopeTypeFilters: [
        { text: this.$t('commons.api'), value: 'api' },
        { text: this.$t('commons.ui_test'), value: 'ui' },
      ],
      condition: {
       selectAll : false,
        unSelectIds: [],
      }
    };
  },
  watch: {
    items: {
      handler(v) {
        this.allData = v;
        this.pageSize = 10;
        this.total = this.allData.length;
        this.sortParameters();
        this.nextPage();
      },
      immediate: true,
      deep: true,
    },
  },
  methods: {
    remove: function (index) {
      const dataIndex = this.variables.findIndex((d) => d.name === index.name);
      this.variables.splice(dataIndex, 1);

      const allDataIndex = this.allData.findIndex((d) => d.name === index.name);
      this.allData.splice(allDataIndex, 1);
      this.nextPage();
    },
    nextPage() {
      // 如果是第一页，则截取0到pageSize（每页显示多少条数据）即可
      if (this.currentPage == 1) {
        this.variables = this.allData.slice(0, this.pageSize);
        this.variables.forEach((item) => {
          item.showMore = false;
        });
        return;
      }
      let start = (this.currentPage - 1) * this.pageSize;
      let end = this.currentPage * this.pageSize;
      this.variables = this.allData.slice(start, end);
      this.total = this.allData.length;
    },
    change: function () {
      let isNeedCreate = true;
      let removeIndex = -1;
      let repeatKey = '';
      this.items.forEach((item, index) => {
        this.items.forEach((row, rowIndex) => {
          if (item.name === row.name && index !== rowIndex) {
            repeatKey = item.name;
          }
        });
        if (!item.name && !item.value) {
          // 多余的空行
          if (index !== this.items.length - 1) {
            removeIndex = index;
          }
          // 没有空行，需要创建空行
          isNeedCreate = false;
        }
      });
      if (repeatKey !== '') {
        this.$warning(
          this.$t('api_test.environment.common_config') +
          '【' +
          repeatKey +
          '】' +
          this.$t('load_test.param_is_duplicate')
        );
      }
      if (isNeedCreate) {
        this.items.push(new KeyValue({ enable: true, id: getUUID(), type: 'CONSTANT', scope: 'api' }));
      }
      this.currentPage = Math.ceil(this.items.length / this.pageSize);
      this.$emit('change', this.items);
      // TODO 检查key重复
    },
    changeType(data) {
      data.value = '';
      if (!data.delimiter || (!data.files && data.files.length === 0) || !data.quotedData) {
        data.delimiter = ',';
        data.files = [];
        data.quotedData = 'false';
      }

      if (!data.scope || data.scope == 'ui') {
        data.type = 'STRING';
      }
    },
    valueText(data) {
      switch (data.type) {
        case 'LIST':
          return this.$t('api_test.environment.list_info');
        case 'CONSTANT':
          return this.$t('api_test.value');
        case 'COUNTER':
        case 'RANDOM':
          return this.$t('api_test.environment.advanced_setting');
        default:
          return this.$t('api_test.value');
      }
    },
    querySearch(queryString, cb) {
      let restaurants = [
        { value: 'UTF-8' },
        { value: 'UTF-16' },
        { value: 'GB2312' },
        { value: 'ISO-8859-15' },
        { value: 'US-ASCll' },
      ];
      let results = queryString ? restaurants.filter(this.createFilter(queryString)) : restaurants;
      // 调用 callback 返回建议列表的数据
      cb(results);
    },
    sortParameters() {
      let index = 1;
      this.allData.forEach((item) => {
        item.num = index;
        if (!item.type || item.type === 'text') {
          item.type = 'CONSTANT';
        }
        if (!item.id) {
          item.id = getUUID();
        }
        if (item.remark) {
          this.$set(item, 'description', item.remark);
          item.remark = undefined;
        }
        if (!item.scope) {
          this.$set(item, 'scope', 'api');
        }
        index++;
      });
    },
    handleDeleteBatch() {
      operationConfirm(this, this.$t('api_test.environment.variables_delete_info') + ' ？', () => {
        let ids = this.$refs.variableTable.selectRows;
        ids.forEach((row) => {
          if (row.id) {
            const index = this.variables.findIndex((d) => d.id === row.id);
            const allIndex = this.allData.findIndex((d) => d.id === row.id);
            if (index !== this.variables.length - 1) {
              this.variables.splice(index, 1);
            }
            if (allIndex !== this.allData.length - 1) {
              this.allData.splice(allIndex, 1);
            }
          }
        });
        this.sortParameters();
        this.$refs.variableTable.cancelCurrentRow();
        this.$refs.variableTable.clear();
        this.variables.forEach((item) => {
          item.showMore = false;
        });
      });
    },
    filter(scope) {
      let datas = [];
      this.items.forEach((item) => {
        if (this.selectVariable && this.selectVariable != '' && item.name) {
          if (item.name.toLowerCase().indexOf(this.selectVariable.toLowerCase()) === -1) {
            item.hidden = true;
          } else {
            item.hidden = undefined;
          }
        } else {
          item.hidden = undefined;
        }
        if (!item.hidden) {
          datas.push(item);
        }
      });
      this.total = datas.length;
      // 如果是第一页，则截取0到pageSize（每页显示多少条数据）即可
      if (this.currentPage == 1) {
        this.variables = datas.slice(0, this.pageSize);
        return;
      }
      let start = (this.currentPage - 1) * this.pageSize;
      let end = this.currentPage * this.pageSize;
      this.variables = datas.slice(start, end);
    },
    filterScope(value, row) {
      if (value == 'ui') {
        return row.scope == 'ui';
      }
      return !row.scope || row.scope == 'api';
    },
    openSetting(data) {
      this.$refs.apiVariableSetting.open(data);
    },
    isDisable: function (row) {
      const index = this.items.findIndex((d) => d.name === row.name);
      return this.items.length - 1 !== index;
    },
    _handleBatchVars(data) {
      let params = data.split(/[\r\n]+/);
      let keyValues = [];
      params.forEach((item) => {
        if (item) {
          let line = item.split(/：|:/);
          let values = item.substr(line[0].length + 1).trim();
          let required = false;
          keyValues.push(
            new KeyValue({
              scope: 'api',
              name: line[0],
              required: required,
              value: values,
              type: 'CONSTANT',
              valid: false,
              file: false,
              encode: true,
              enable: true,
              description: undefined,
            })
          );
        }
      });
      return keyValues;
    },
    batchAdd() {
      this.$refs.batchAdd.open();
    },
    batchSave(data) {
      if (data) {
        let keyValues = this._handleBatchVars(data);
        keyValues.forEach((keyValue) => {
          let isAdd = true;
          this.items.forEach((item) => {
            if (item.name === keyValue.name) {
              item.value = keyValue.value;
              isAdd = false;
            }
          });
          if (isAdd) {
            this.items.splice(
              this.variables.indexOf((i) => !i.name),
              0,
              keyValue
            );
          }
        });
      }
      this.currentPage = Math.ceil(this.items.length / this.pageSize);
    },
    onChange() {
      this.sortParameters();
    },
    exportJSON() {
      let apiVariable = [];
      this.$refs.variableTable.selectRows.forEach((r) => {
        if (!r.scope || r.scope != 'ui') {
          apiVariable.push(r);
        }
      });

      if (apiVariable.length < 1) {
        this.$warning(this.$t('api_test.environment.select_api_variable'));
        return;
      }
      let variablesJson = [];
      let messages = '';
      let rows = this.$refs.variableTable.selectRows;
      if (this.condition.selectAll) {
        rows = this.allData;
      }
      rows.forEach((row) => {
        if (row.type === 'CSV') {
          messages = this.$t('variables.csv_download');
        }
        if (row.name && (!row.scope || row.scope == 'api')) {
          variablesJson.push(row);
        }
      });
      if (messages !== '') {
        this.$warning(messages);
        return;
      }
      downloadFile('MS_' + variablesJson.length + '_Environments_variables.json', JSON.stringify(variablesJson));
    },
    importJSON() {
      this.$refs.variableImport.open();
    },
    mergeData(data, modeId) {
      JSON.parse(data).map((importData) => {
        importData.id = getUUID();
        importData.enable = true;
        importData.showMore = false;
        let sameNameIndex = this.items.findIndex((d) => d.name === importData.name);
        if (sameNameIndex !== -1) {
          if (modeId === 'fullCoverage') {
            this.items.splice(sameNameIndex, 1, importData);
          }
        } else {
          this.items.splice(this.items.length - 1, 0, importData);
        }
      });
      this.currentPage = Math.ceil(this.items.length / this.pageSize);
    },
    handleExportCommand(command) {
      this.exportJSON();
    },
  },
  created() {
    if (this.items.length === 0) {
      this.items.push(new KeyValue({ enable: true, scope: 'api' }));
    } else {
      //历史数据默认是 api 应用场景
      forEach(this.items, (item) => {
        delete item.hidden;
        if (!item.scope) {
          this.$set(item, 'scope', 'api');
        }
      });
      this.allData = this.items;
      this.nextPage();
    }
    this.total = this.allData.length;
  },
};
</script>

<style scoped>
.kv-description {
  font-size: 13px;
}

.kv-checkbox {
  width: 20px;
  margin-right: 10px;
}

.kv-row {
  margin-top: 10px;
}

.kv-delete {
  width: 60px;
}

</style>
