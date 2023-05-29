<template>
  <el-dialog
    :title="$t('api_test.automation.scenario_total')"
    :close-on-click-modal="false"
    :visible.sync="visible"
    class="visible-dialog"
    width="80%"
    @close="close"
    v-loading="loading"
    append-to-body>
    <fieldset :disabled="disabled" class="ms-fieldset">
      <el-collapse-transition>
        <el-tabs v-model="activeName" @tab-click="reloadTable">
          <el-tab-pane :label="$t('api_test.scenario.variables')" name="variable">
            <div>
              <el-row style="margin-bottom: 10px">
                <div style="float: left">
                  <el-input
                    :placeholder="$t('commons.search_by_name')"
                    v-model="selectVariable"
                    size="small"
                    @change="filter"
                    @keyup.enter="filter">
                    <el-select
                      v-model="searchType"
                      slot="prepend"
                      :placeholder="$t('test_resource_pool.type')"
                      style="width: 90px"
                      @change="filter">
                      <el-option value="ALL" :label="$t('api_test.automation.all')"></el-option>
                      <el-option value="CONSTANT" :label="$t('api_test.automation.constant')"></el-option>
                      <el-option value="LIST" :label="$t('test_track.case.list')"></el-option>
                      <el-option value="CSV" label="CSV"></el-option>
                      <el-option value="COUNTER" :label="$t('api_test.automation.counter')"></el-option>
                      <el-option value="RANDOM" :label="$t('api_test.automation.random')"></el-option>
                    </el-select>
                  </el-input>
                </div>

                <div style="float: right">
                  <el-select
                    v-model="selectType"
                    :placeholder="$t('test_resource_pool.type')"
                    style="width: 90px"
                    size="small"
                    @change="filter">
                    <el-option value="CONSTANT" :label="$t('api_test.automation.constant')"></el-option>
                    <el-option value="LIST" :label="$t('test_track.case.list')"></el-option>
                    <el-option value="CSV" label="CSV"></el-option>
                    <el-option value="COUNTER" :label="$t('api_test.automation.counter')"></el-option>
                    <el-option value="RANDOM" :label="$t('api_test.automation.random')"></el-option>
                  </el-select>
                  <el-button size="small" style="margin-left: 10px" type="primary" @click="addVariable">
                    {{ $t('commons.add') }}
                  </el-button>
                  <el-dropdown style="margin-left: 10px">
                    <el-button size="small">
                      <span class="tip-font">{{ $t('commons.more_operator') }}</span>
                      <i class="el-icon-arrow-down el-icon--right" />
                    </el-button>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item @click.native.stop="importVariable" :disabled="disabled">
                        {{ $t('commons.import_variable') }}
                      </el-dropdown-item>
                      <el-dropdown-item @click.native.stop="exportVariable" :disabled="disabled">
                        {{ $t('commons.export_variable') }}
                      </el-dropdown-item>
                      <el-dropdown-item @click.native.stop="batchAddParameter" :disabled="disabled">
                        {{ $t('commons.batch_add') }}
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </div>
              </el-row>
              <el-row>
                <el-col :span="12">
                  <div style="border: 1px #dcdfe6 solid; min-height: 400px; border-radius: 4px; width: 100%">
                    <ms-table
                      v-loading="loading"
                      row-key="id"
                      :data="variables"
                      :total="variables.length"
                      :screen-height="screenHeight"
                      :batch-operators="batchButtons"
                      :remember-order="true"
                      :highlightCurrentRow="true"
                      :fields.sync="fields"
                      :field-key="tableHeaderKey"
                      @handleRowClick="handleRowClick"
                      @refresh="onChange"
                      ref="variableTable">
                      <span v-for="item in fields" :key="item.key">
                        <ms-table-column
                          prop="num"
                          :field="item"
                          :fields-width="fieldsWidth"
                          sortable
                          label="ID"
                          min-width="60">
                        </ms-table-column>
                        <ms-table-column
                          prop="name"
                          :field="item"
                          :fields-width="fieldsWidth"
                          :label="$t('api_test.variable_name')"
                          min-width="100"
                          sortable>
                        </ms-table-column>
                        <ms-table-column
                          prop="type"
                          :field="item"
                          :fields-width="fieldsWidth"
                          :label="$t('test_track.case.type')"
                          min-width="70"
                          sortable>
                          <template v-slot:default="scope">
                            <span>{{ types.get(scope.row.type) }}</span>
                          </template>
                        </ms-table-column>
                        <ms-table-column
                          prop="value"
                          :field="item"
                          :fields-width="fieldsWidth"
                          :label="$t('api_test.value')"
                          sortable>
                        </ms-table-column>
                        <ms-table-column
                          prop="description"
                          :field="item"
                          :fields-width="fieldsWidth"
                          :label="$t('commons.description')"
                          min-width="70"
                          sortable>
                        </ms-table-column>
                      </span>
                    </ms-table>
                    <batch-add-parameter @batchSave="batchSaveParameter" ref="batchAddParameter" />
                  </div>
                </el-col>
                <el-col :span="12">
                  <ms-edit-constant v-if="editData.type == 'CONSTANT'" ref="parameters" :editData.sync="editData" />
                  <ms-edit-counter v-if="editData.type == 'COUNTER'" ref="counter" :editData.sync="editData" />
                  <ms-edit-random v-if="editData.type == 'RANDOM'" ref="random" :editData.sync="editData" />
                  <ms-edit-list-value v-if="editData.type == 'LIST'" ref="listValue" :editData="editData" />
                  <ms-edit-csv v-if="editData.type === 'CSV' && !loading" ref="csv" :editData.sync="editData" :disabled="disabled"/>
                  <div v-if="editData.type" style="float: right">
                    <el-button size="small" style="margin-left: 10px" type="primary" @click="confirmVariable">
                      {{ $t('commons.confirm') }}
                    </el-button>
                    <el-button size="small" style="margin-left: 10px" @click="cancelVariable"
                    >{{ $t('commons.cancel') }}
                    </el-button>
                    <el-button v-if="showDelete" size="small" style="margin-left: 10px" @click="deleteVariable">
                      {{ $t('commons.delete') }}
                    </el-button>
                  </div>
                </el-col>
              </el-row>
            </div>
          </el-tab-pane>
          <el-tab-pane :label="$t('api_test.scenario.headers')" name="headers">
            <!-- 请求头-->
            <el-tooltip
              class="item-tabs"
              effect="dark"
              :content="$t('api_test.request.headers')"
              placement="top-start"
              slot="label">
              <span
              >{{ $t('api_test.request.headers') }}
                <div class="el-step__icon is-text ms-api-col ms-header" v-if="headers.length > 1">
                  <div class="el-step__icon-inner">
                    {{ headers.length - 1 }}
                  </div>
                </div>
              </span>
            </el-tooltip>
            <el-row>
              <el-link class="ms-variable-link" @click="batchAddHeader" type="primary" :disabled="disabled">
                {{ $t('commons.batch_add') }}
              </el-link>
            </el-row>
            <div style="min-height: 400px">
              <ms-api-key-value :items="headers" :suggestions="headerSuggestions" />
              <batch-add-parameter @batchSave="batchSaveHeader" ref="batchAddHeader" />
            </div>
          </el-tab-pane>
        </el-tabs>
        <template v-slot:footer>
          <div>
            <el-button type="primary" @click="save">{{ $t('commons.confirm') }}</el-button>
          </div>
        </template>
      </el-collapse-transition>
    </fieldset>
    <variable-import ref="variableImport" @mergeData="mergeData"></variable-import>
  </el-dialog>
</template>

<script>
import MsEditConstant from './EditConstant';
import MsDialogFooter from 'metersphere-frontend/src/components/MsDialogFooter';
import MsTableHeader from 'metersphere-frontend/src/components/MsTableHeader';
import MsTablePagination from 'metersphere-frontend/src/components/pagination/TablePagination';
import MsEditCounter from './EditCounter';
import MsEditRandom from './EditRandom';
import MsEditListValue from './EditListValue';
import MsEditCsv from './EditCsv';
import { downloadFile, getUUID } from 'metersphere-frontend/src/utils';
import MsApiKeyValue from '../../../definition/components/ApiKeyValue';
import BatchAddParameter from '../../../definition/components/basis/BatchAddParameter';
import { KeyValue } from '../../../definition/model/ApiTestModel';
import { REQUEST_HEADERS } from 'metersphere-frontend/src/utils/constants';

import {diff} from 'jsondiffpatch';
import MsTable from 'metersphere-frontend/src/components/table/MsTable';
import MsTableColumn from 'metersphere-frontend/src/components/table/MsTableColumn';
import { getCustomTableHeader, getCustomTableWidth } from 'metersphere-frontend/src/utils/tableUtils';
import VariableImport from '@/business/automation/scenario/variable/VariableImport';


export default {
  name: 'MsVariableList',
  components: {
    MsEditConstant,
    MsDialogFooter,
    MsTableHeader,
    MsTablePagination,
    MsEditCounter,
    MsEditRandom,
    MsEditListValue,
    MsEditCsv,
    MsApiKeyValue,
    BatchAddParameter,
    MsTableColumn,
    MsTable,
    VariableImport,
  },
  data() {
    return {
      variables: [],
      headers: [],
      variablesOld: [],
      headersOld: [],
      activeName: 'variable',
      searchType: '',
      selectVariable: '',
      condition: {},
      types: new Map([
        ['CONSTANT', this.$t('api_test.automation.constant')],
        ['LIST', this.$t('test_track.case.list')],
        ['CSV', 'CSV'],
        ['COUNTER', this.$t('api_test.automation.counter')],
        ['RANDOM', this.$t('api_test.automation.random')],
      ]),
      visible: false,
      selection: [],
      loading: false,
      currentPage: 1,
      editData: {},
      pageSize: 10,
      total: 0,
      headerSuggestions: REQUEST_HEADERS,
      disabled: false,
      selectType: 'CONSTANT',
      showDelete: false,
      tableHeaderKey: 'VARIABLE_LIST_TABLE',
      fields: getCustomTableHeader('VARIABLE_LIST_TABLE', undefined),
      fieldsWidth: getCustomTableWidth('TRACK_REPORT_TABLE'),
      screenHeight: '400px',
      batchButtons: [
        {
          name: this.$t('api_test.definition.request.batch_delete'),
          handleClick: this.handleDeleteBatch,
        },
      ],
    };
  },
  methods: {
    reloadTable() {
      if (this.$refs.variableTable) {
        this.$refs.variableTable.reloadTable();
      }
    },
    importVariable() {
      this.$refs.variableImport.open();
    },
    mergeData(data, modeId) {
      JSON.parse(data).map((importData) => {
        importData.id = getUUID();
        importData.enable = true;
        importData.showMore = false;
        let sameNameIndex = this.variables.findIndex((d) => d.name === importData.name);
        if (sameNameIndex !== -1) {
          if (modeId === 'fullCoverage') {
            this.variables.splice(sameNameIndex, 1, importData);
          }
        } else {
          this.variables.splice(this.variables.length, 0, importData);
        }
        this.sortParameters();
      });
    },
    exportVariable() {
      if (this.$refs.variableTable.selectIds.length < 1) {
        this.$warning(this.$t('api_test.environment.select_variable'));
        return;
      }
      let variablesJson = [];
      let messages = '';
      let rows = this.$refs.variableTable.selectRows;
      rows.forEach((row) => {
        if (row.type === 'CSV') {
          messages = this.$t('variables.csv_download');
        }
        if (row.name) {
          variablesJson.push(row);
        }
      });
      if (messages !== '') {
        this.$warning(messages);
        return;
      }
      downloadFile('MS_' + variablesJson.length + '_Environments_variables.json', JSON.stringify(variablesJson));
    },
    batchAddParameter() {
      this.$refs.batchAddParameter.open('scenario');
    },
    batchAddHeader() {
      this.$refs.batchAddHeader.open();
    },
    _handleBatchVars(data) {
      if (data) {
        let params = data.split(/[\r\n]+/);
        let keyValues = [];
        params.forEach((item) => {
          if (item) {
            let line = item.split(/：|:/);
            let values = item.substr(line[0].length + 1).trim();
            let required = false;
            keyValues.push(
              new KeyValue({
                name: line[0],
                required: required,
                value: values,
                type: 'CONSTANT',
                valid: false,
                file: false,
                encode: true,
                enable: true,
              })
            );
          }
        });
        return keyValues;
      }
    },
    batchSaveHeader(data) {
      if (data) {
        let keyValues = this._handleBatchVars(data);
        keyValues.forEach((item) => {
          this.format(this.headers, item);
        });
      }
    },
    format(array, obj) {
      if (array) {
        let isAdd = true;
        for (let i in array) {
          let item = array[i];
          if (item.name === obj.name) {
            item.value = obj.value;
            isAdd = false;
          }
        }
        if (isAdd) {
          this.headers.splice(
            this.headers.indexOf((h) => !h.name),
            0,
            obj
          );
        }
      }
    },
    batchSaveParameter(data) {
      if (data) {
        let keyValues = this._handleBatchVars(data);
        keyValues.forEach((keyValue) => {
          let isAdd = true;
          keyValue.id = getUUID();
          this.variables.forEach((item) => {
            if (item.name === keyValue.name) {
              item.value = keyValue.value;
              isAdd = false;
            }
          });
          if (isAdd) {
            this.variables.splice(this.variables.length, 0, keyValue);
          }
        });
        this.sortParameters();
      }
    },
    handleClick(command) {
      this.editData = { delimiter: ',', quotedData: 'false' };
      this.editData.type = command;
      this.addParameters(this.editData);
    },

    addParameters(v) {
      v.id = getUUID();
      if (v.type === 'CSV') {
        v.delimiter = v.delimiter ? v.delimiter : ',';
      }
      this.variables.push(v);
      let index = 1;
      this.variables.forEach((item) => {
        item.num = index;
        index++;
      });
    },
    sortParameters() {
      let index = 1;
      this.variables.forEach((item) => {
        item.num = index;
        item.showMore = false;
        index++;
      });
      this.selection = [];
    },
    updateParameters(v) {
      this.editData = JSON.parse(JSON.stringify(v));
      this.updateFiles();
      let data = [];
      this.variables.forEach((item) => {
        if (item.id === v.id) {
          item = v;
        }
        data.push(item);
      });
      this.variables = data;
    },
    select(selection) {
      this.selection = selection.map((s) => s.id);
    },
    open: function (variables, headers, disabled) {
      if (variables) {
        this.variables = variables;
      }
      if (headers) {
        this.headers = headers;
      }
      this.variablesOld = JSON.parse(JSON.stringify(this.variables));
      this.headersOld = JSON.parse(JSON.stringify(this.headers));
      this.visible = true;
      this.disabled = disabled;
      this.$nextTick(() => {
        this.$refs.variableTable.doLayout();
        let variable = [];
        let messages = '';
        this.variables.forEach((item) => {
          if (variable.indexOf(item.name) !== -1) {
            messages += item.name + ' ,';
          } else {
            variable.push(item.name);
          }
        });
        if (messages !== '') {
          this.$alert(
            this.$t('api_test.scenario.variables') +
            '【' +
            messages.substr(0, messages.length - 1) +
            '】' +
            this.$t('load_test.param_is_duplicate')
          );
        }
      });
    },
    save() {
      this.visible = false;
    },
    close() {
      this.visible = false;
      let saveVariables = [];
      this.variables.forEach((item) => {
        item.hidden = undefined;
        if (item.name && item.name != '') {
          item.showMore = false;
          saveVariables.push(item);
        }
      });
      this.selectVariable = '';
      this.searchType = '';
      this.selectType = 'CONSTANT';
      this.editData = {};
      if (
        diff(JSON.parse(JSON.stringify(this.variables)), this.variablesOld) ||
        diff(JSON.parse(JSON.stringify(this.headers)), this.headersOld)
      ) {
        this.$emit('setVariables', saveVariables, this.headers);
      }
    },
    addVariable() {
      this.editData = { delimiter: ',', quotedData: 'false', files: [] };
      this.editData.type = this.selectType;
      this.showDelete = false;
      if (this.editData.type === 'CSV' && this.$refs.csv) {
        this.$refs.csv.cleanPreview();
      }
      this.$refs.variableTable.cancelCurrentRow();
    },
    confirmVariable() {
      if (this.editData && (!this.editData.name || this.editData.name == '')) {
        this.$warning(this.$t('api_test.automation.variable_warning'));
        return;
      }
      let repeatKey = '';
      this.variables.forEach((item) => {
        if (item.name === this.editData.name && item.id !== this.editData.id) {
          repeatKey = item.name;
        }
      });
      if (repeatKey !== '') {
        this.$warning(
          this.$t('api_test.scenario.variables') + '【' + repeatKey + '】' + this.$t('load_test.param_is_duplicate')
        );
        return;
      }
      if (this.editData.type === 'CSV' && this.$refs.csv) {
        if (this.editData.files.length === 0) {
          this.$warning(this.$t('api_test.automation.csv_warning'));
          return;
        }
        this.variables.forEach((item) => {
          if (item.id === this.editData.id) {
            item.files = this.editData.files;
          }
        });
      }
      // 更新场景，修改左边数据
      if (this.showDelete) {
        this.updateParameters(this.editData);
      } else {
        // 新增场景，往左边新加
        this.addParameters(this.editData);
        this.addVariable();
        this.$refs.variableTable.cancelCurrentRow();
      }
      this.reload();
      this.$success(this.$t('commons.save_success'));
    },
    reload() {
      this.loading = true;
      this.$nextTick(() => {
        this.loading = false;
      });
    },
    cancelVariable() {
      this.$refs.variableTable.cancelCurrentRow();
      // 清空表单
      this.editData = {};
    },
    deleteVariable() {
      let ids = [this.editData.id];
      if (ids.length == 0) {
        this.$warning(this.$t('api_test.environment.delete_info'));
        return;
      }
      let message = '';
      ids.forEach((row) => {
        const v = this.variables.find((d) => d.id === row);
        if (v.name) {
          message += v.name + ';';
        }
      });
      if (message !== '') {
        message = message.substr(0, message.length - 1);
        this.$alert(this.$t('api_test.environment.variables_delete_info') + '：【 ' + message + ' 】？', '', {
          confirmButtonText: this.$t('commons.confirm'),
          callback: (action) => {
            if (action === 'confirm') {
              ids.forEach((row) => {
                const index = this.variables.findIndex((d) => d.id === row);
                this.variables.splice(index, 1);
              });
              this.sortParameters();
              this.editData = {};
            }
          },
        });
      } else {
        ids.forEach((row) => {
          const index = this.variables.findIndex((d) => d.id === row);
          this.variables.splice(index, 1);
        });
        this.sortParameters();
        this.editData = {};
      }
    },
    handleDeleteBatch() {
      this.$alert(this.$t('api_test.environment.variables_delete_info') + ' ' + ' ？', '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let ids = this.$refs.variableTable.selectIds;
            ids.forEach((row) => {
              const index = this.variables.findIndex((d) => d.id === row);
              this.variables.splice(index, 1);
            });
            // this.editData = {type: "CONSTANT"};
            this.sortParameters();
            this.editData = {};
            this.$refs.variableTable.cancelCurrentRow();
            this.$refs.variableTable.clear();
          }
        },
      });
    },
    filter() {
      let data = [];
      this.variables.forEach((item) => {
        if (this.searchType && this.searchType != '' && this.selectVariable && this.selectVariable != '') {
          if (
            (item.type &&
              item.type.toLowerCase().indexOf(this.searchType.toLowerCase()) == -1 &&
              this.searchType != 'ALL') ||
            (item.name && item.name.toLowerCase().indexOf(this.selectVariable.toLowerCase()) == -1)
          ) {
            item.hidden = true;
          } else {
            item.hidden = undefined;
          }
        } else if (this.selectVariable && this.selectVariable != '') {
          if (item.name && item.name.toLowerCase().indexOf(this.selectVariable.toLowerCase()) == -1) {
            item.hidden = true;
          } else {
            item.hidden = undefined;
          }
        } else if (this.searchType && this.searchType != '') {
          if (
            item.type &&
            item.type.toLowerCase().indexOf(this.searchType.toLowerCase()) == -1 &&
            this.searchType != 'ALL'
          ) {
            item.hidden = true;
          } else {
            item.hidden = undefined;
          }
        } else {
          item.hidden = undefined;
        }
        if (this.searchType === 'ALL' && !(this.selectVariable && this.selectVariable != '')) {
          item.hidden = undefined;
        }
        data.push(item);
      });
      this.variables = data;
    },
    createFilter(queryString) {
      return (item) => {
        return item.type && item.type.toLowerCase().indexOf(queryString.toLowerCase()) !== -1;
      };
    },
    handleRowClick(row) {
      // 做深拷贝
      this.editData = JSON.parse(JSON.stringify(row));
      this.updateFiles();
      this.showDelete = true;
    },
    updateFiles() {
      this.variables.forEach((item) => {
        if (item.id === this.editData.id) {
          this.editData.files = item.files;
        }
      });
    },
    onChange() {
      this.selection = [];
      this.sortParameters();
    },
  },
};
</script>

<style scoped>
.ms-variable-hidden-row {
  display: none;
}

.ms-variable-header {
  background: #783887;
  color: white;
  height: 18px;
  border-radius: 42%;
}

.ms-variable-link {
  float: right;
  margin-right: 45px;
}

fieldset {
  padding: 0px;
  margin: 0px;
  min-width: 100%;
  min-inline-size: 0px;
  border: 0px;
}

.ms-fieldset .ms-step-tree-cell::-webkit-scrollbar {
  width: 0 !important;
}

.ms-select {
  width: 100px;
  margin-right: 10px;
}

:deep(.table-select-icon) {
  display: none !important;
}

.ms-header {
  background: #783887;
  color: white;
  height: 18px;
  font-size: xx-small;
  border-radius: 50%;
}
</style>
