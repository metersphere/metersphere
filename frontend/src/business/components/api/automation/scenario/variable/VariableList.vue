<template>
  <el-dialog :title="$t('api_test.automation.scenario_total')" :close-on-click-modal="false"
             :visible.sync="visible" class="visible-dialog" width="80%"
             @close="close" v-loading="loading" append-to-body>
    <fieldset :disabled="disabled" class="ms-fieldset">
      <el-collapse-transition>

        <el-tabs v-model="activeName">
          <el-tab-pane :label="$t('api_test.scenario.variables')" name="variable">
            <div>
              <el-row style="margin-bottom: 10px">
                <div style="float: left">
                  <el-input :placeholder="$t('commons.search_by_name')" v-model="selectVariable" size="small"
                            @change="filter"
                            @keyup.enter="filter">
                    <el-select v-model="searchType" slot="prepend" :placeholder="$t('test_resource_pool.type')"
                               style="width: 90px" @change="filter">
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
                  <el-select v-model="selectType" :placeholder="$t('test_resource_pool.type')" style="width: 90px" size="small" @change="filter">
                    <el-option value="CONSTANT" :label="$t('api_test.automation.constant')"></el-option>
                    <el-option value="LIST" :label="$t('test_track.case.list')"></el-option>
                    <el-option value="CSV" label="CSV"></el-option>
                    <el-option value="COUNTER" :label="$t('api_test.automation.counter')"></el-option>
                    <el-option value="RANDOM" :label="$t('api_test.automation.random')"></el-option>
                  </el-select>
                  <el-button size="small" style="margin-left: 10px" type="primary" @click="addVariable">{{ $t('commons.add') }}</el-button>
                  <el-link @click="batchAddParameter" type="primary" :disabled="disabled" style="margin-left: 10px">
                    {{ $t("commons.batch_add") }}
                  </el-link>
                </div>
              </el-row>
              <el-row>
                <el-col :span="12">
                  <div style="border:1px #DCDFE6 solid; min-height: 400px;border-radius: 4px ;width: 100% ;">
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
                          </span>
                        </ms-table>
                    <batch-add-parameter @batchSave="batchSaveParameter" ref="batchAddParameter"/>
                  </div>
                </el-col>
                <el-col :span="12">
                  <ms-edit-constant v-if="editData.type=='CONSTANT'" ref="parameters" :editData.sync="editData"/>
                  <ms-edit-counter v-if="editData.type=='COUNTER'" ref="counter" :editData.sync="editData"/>
                  <ms-edit-random v-if="editData.type=='RANDOM'" ref="random" :editData.sync="editData"/>
                  <ms-edit-list-value v-if="editData.type=='LIST'" ref="listValue" :editData="editData"/>
                  <ms-edit-csv v-if="editData.type=='CSV'" ref="csv" :editData.sync="editData"/>
                  <div v-if="editData.type" style="float: right">
                    <el-button size="small" style="margin-left: 10px" type="primary" @click="confirmVariable">{{ $t('commons.confirm') }}</el-button>
                    <el-button size="small" style="margin-left: 10px" @click="cancelVariable">{{ $t('commons.cancel') }}</el-button>
                    <el-button v-if="showDelete" size="small" style="margin-left: 10px" @click="deleteVariable">{{ $t('commons.delete') }}</el-button>
                  </div>
                </el-col>
              </el-row>
            </div>
          </el-tab-pane>
          <el-tab-pane :label="$t('api_test.scenario.headers')" name="headers">
            <!-- 请求头-->
            <el-tooltip class="item-tabs" effect="dark" :content="$t('api_test.request.headers')" placement="top-start"
                        slot="label">
          <span>{{ $t('api_test.request.headers') }}
            <div class="el-step__icon is-text ms-api-col ms-variable-header" v-if="headers.length>1">
              <div class="el-step__icon-inner">{{ headers.length - 1 }}</div>
            </div>
          </span>
            </el-tooltip>
            <el-row>
              <el-link class="ms-variable-link" @click="batchAddHeader" type="primary" :disabled="disabled">
                {{ $t("commons.batch_add") }}
              </el-link>
            </el-row>
            <div style="min-height: 400px">
              <ms-api-key-value :items="headers" :suggestions="headerSuggestions"/>
              <batch-add-parameter @batchSave="batchSaveHeader" ref="batchAddHeader"/>
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
  </el-dialog>
</template>

<script>
  import MsEditConstant from "./EditConstant";
  import MsDialogFooter from "../../../../common/components/MsDialogFooter";
  import MsTableHeader from "@/business/components/common/components/MsTableHeader";
  import MsTablePagination from "@/business/components/common/pagination/TablePagination";
  import MsEditCounter from "./EditCounter";
  import MsEditRandom from "./EditRandom";
  import MsEditListValue from "./EditListValue";
  import MsEditCsv from "./EditCsv";
  import {getUUID} from "@/common/js/utils";
  import MsApiKeyValue from "../../../definition/components/ApiKeyValue";
  import BatchAddParameter from "../../../definition/components/basis/BatchAddParameter";
  import {KeyValue} from "../../../definition/model/ApiTestModel";
  import {REQUEST_HEADERS} from "@/common/js/constants";

  import MsTable from "@/business/components/common/components/table/MsTable";
  import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
  import {
    getCustomTableWidth,
    getCustomTableHeader
  } from "@/common/js/tableUtils";

  export default {
    name: "MsVariableList",
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
    },
    data() {
      return {
        variables: [],
        headers: [],
        activeName: "variable",
        searchType: "",
        selectVariable: "",
        condition: {},
        types: new Map([
          ['CONSTANT', this.$t('api_test.automation.constant')],
          ['LIST', this.$t('test_track.case.list')],
          ['CSV', 'CSV'],
          ['COUNTER', this.$t('api_test.automation.counter')],
          ['RANDOM', this.$t('api_test.automation.random')]
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
        selectType: "",
        showDelete: false,
        tableHeaderKey: "VARIABLE_LIST_TABLE",
        fields: getCustomTableHeader('VARIABLE_LIST_TABLE'),
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
      batchAddParameter() {
        this.$refs.batchAddParameter.open();
      },
      batchAddHeader() {
        this.$refs.batchAddHeader.open();
      },
      _handleBatchVars(data) {
        if (data) {
          let params = data.split("\n");
          let keyValues = [];
          params.forEach(item => {
            let line = item.split(/：|:/);
            let required = false;
            keyValues.unshift(new KeyValue({
              name: line[0],
              required: required,
              value: line[1],
              description: line[2],
              type: "text",
              valid: false,
              file: false,
              encode: true,
              enable: true,
              contentType: "text/plain"
            }));
          })
          return keyValues;
        }
      },
      batchSaveHeader(data) {
        if (data) {
          let keyValues = this._handleBatchVars(data);
          keyValues.forEach(item => {
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
            this.headers.unshift(obj);
          }
        }
      },
      batchSaveParameter(data) {
        if (data) {
          let keyValues = this._handleBatchVars(data);
          keyValues.forEach(item => {
            item.type = 'CONSTANT';
            this.addParameters(item);
          });
        }
      },
      handleClick(command) {
        this.editData = {delimiter: ",", quotedData: 'false'};
        this.editData.type = command;
        this.addParameters(this.editData);
      },

      addParameters(v) {
        v.id = getUUID();
        if (v.type === 'CSV') {
          v.delimiter = ",";
          v.quotedData = false;
        }
        this.variables.push(v);
        let index = 1;
        this.variables.forEach(item => {
          item.num = index;
          index++;
        });
      },
      sortParamters() {
        let index = 1;
        this.variables.forEach(item => {
          item.num = index;
          index++;
        });
      },
      updateParameters(v) {
        this.editData = JSON.parse(JSON.stringify(v));
        this.updateFiles();
        let datas = [];
        this.variables.forEach(item => {
          if(item.id === v.id){
            item = v;
          }
          datas.push(item);
        });
        this.variables = datas;
      },
      select(selection) {
        this.selection = selection.map(s => s.id);
      },
      open: function (variables, headers, disabled) {
        if (variables) {
          this.variables = variables;
        }
        if (headers) {
          this.headers = headers;
        }
        this.visible = true;
        this.disabled = disabled;
      },
      save() {
        this.visible = false;
      },
      close() {
        this.visible = false;
        let saveVariables = [];
        this.variables.forEach(item => {
          item.hidden = undefined;
          if (item.name && item.name != "") {
            saveVariables.push(item);
          }
        });
        this.selectVariable = "";
        this.searchType = "";
        this.$emit('setVariables', saveVariables, this.headers);
      },
      addVariable() {
        this.editData = {delimiter: ",", quotedData: 'false',files:[]};
        this.editData.type = this.selectType;
        this.showDelete = false;
        this.$refs.variableTable.cancelCurrentRow();
      },
      confirmVariable() {
        if (this.editData && (this.editData.name == undefined || this.editData.name == '')) {
          this.$warning("变量名不能为空");
          return;
        }
        // 更新场景，修改左边数据
        if(this.showDelete){
          this.updateParameters(this.editData);
        }else{
          // 新增场景，往左边新加
          this.addParameters(this.editData);
          this.addVariable();
          this.$refs.variableTable.cancelCurrentRow();
        }
        this.$success(this.$t('commons.save_success'));
      },
      cancelVariable() {
        this.$refs.variableTable.cancelCurrentRow();
        // 清空表单
        this.editData = {};
      },
      deleteVariable() {
        let ids = [this.editData.id];
        if (ids.length == 0) {
          this.$warning("请选择一条数据删除");
          return;
        }
        let message = "";
        ids.forEach(row => {
          const v = this.variables.find(d => d.id === row);
          if (v.name) {
            message += v.name + ";";
          }
        });
        if (message !== "") {
          message = message.substr(0, message.length - 1);
          this.$alert('是否确认删除变量：【 ' + message + " 】？", '', {
            confirmButtonText: this.$t('commons.confirm'),
            callback: (action) => {
              if (action === 'confirm') {
                ids.forEach(row => {
                  const index = this.variables.findIndex(d => d.id === row);
                  this.variables.splice(index, 1);
                });
                this.sortParamters();
                this.editData = {};
              }
            }
          });
        } else {
          ids.forEach(row => {
            const index = this.variables.findIndex(d => d.id === row);
            this.variables.splice(index, 1);
          });
          this.sortParamters();
          this.editData = {};
        }
      },
      handleDeleteBatch() {
        this.$alert("是否确认删除所选变量" + ' ' + " ？", '', {
          confirmButtonText: this.$t('commons.confirm'),
          callback: (action) => {
            if (action === 'confirm') {
              let ids = this.$refs.variableTable.selectIds;
              ids.forEach(row => {
                const index = this.variables.findIndex(d => d.id === row);
                this.variables.splice(index, 1);
              });
              // this.editData = {type: "CONSTANT"};
              this.sortParamters();
              this.editData = {};
              this.$refs.variableTable.cancelCurrentRow();
              this.$refs.variableTable.clear();
            }
          }
        });
      },
      filter() {
        let datas = [];
        this.variables.forEach(item => {
          if (this.searchType && this.searchType != "" && this.selectVariable && this.selectVariable != "") {
            if ((item.type && item.type.toLowerCase().indexOf(this.searchType.toLowerCase()) == -1 && this.searchType != 'ALL')
              || (item.name && item.name.toLowerCase().indexOf(this.selectVariable.toLowerCase()) == -1)) {
              item.hidden = true;
            } else {
              item.hidden = undefined;
            }
          } else if (this.selectVariable && this.selectVariable != "") {
            if (item.name && item.name.toLowerCase().indexOf(this.selectVariable.toLowerCase()) == -1) {
              item.hidden = true;
            } else {
              item.hidden = undefined;
            }
          } else if (this.searchType && this.searchType != "") {
            if (item.type && item.type.toLowerCase().indexOf(this.searchType.toLowerCase()) == -1 && this.searchType != 'ALL') {
              item.hidden = true;
            } else {
              item.hidden = undefined;
            }
          } else {
            item.hidden = undefined;
          }
          if(this.searchType === 'ALL' && !((this.selectVariable && this.selectVariable != ""))){
            item.hidden = undefined;
          }
          datas.push(item);
        });
        this.variables = datas;
      },
      createFilter(queryString) {
        return item => {
          return (item.type && item.type.toLowerCase().indexOf(queryString.toLowerCase()) !== -1);
        };
      },
      handleRowClick(row) {
        // 做深拷贝
        this.editData = JSON.parse(JSON.stringify(row));
        this.updateFiles();
        this.showDelete = true;
      },
      updateFiles(){
        this.variables.forEach(item => {
          if(item.id === this.editData.id){
            this.editData.files = item.files
          }
        });
      }
    }
  };
</script>

<style>
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

  .ms-select {
    width: 100px;
    margin-right: 10px;
  }
</style>
