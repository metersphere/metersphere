<template>
  <div>
    <div>
      <div style="padding-bottom: 10px;float: left">
        <el-input :placeholder="$t('commons.search_by_name')" size="mini" v-model="selectVariable"
                  @change="filter"
                  @keyup.enter="filter">
        </el-input>
      </div>
      <div style="padding-bottom: 10px; float: right;">
        <ms-table-button v-permission="['PROJECT_ENVIRONMENT:READ+IMPORT']" icon="el-icon-box"
                         :content="$t('commons.import')" @click="importJSON"/>
        <ms-table-button v-permission="['PROJECT_ENVIRONMENT:READ+EXPORT']" icon="el-icon-box"
                         :content="$t('commons.export')" @click="exportJSON"/>
        <el-link style="margin-left: 10px" @click="batchAdd" type="primary" :disabled="isReadOnly">
          {{ $t("commons.batch_add") }}
        </el-link>
      </div>
    </div>
    <div
      style="border:1px #DCDFE6 solid; min-height: 300px;border-radius: 4px ;width: 99% ;margin-top: 10px; clear: both">
      <ms-table
        v-loading="loading"
        row-key="id"
        :data="variables"
        :total="items.length"
        :screen-height="screenHeight"
        :batch-operators="batchButtons"
        :remember-order="true"
        :highlightCurrentRow="true"
        @refresh="onChange"
        ref="variableTable">

        <ms-table-column
          prop="num"
          sortable
          label="ID"
          min-width="60">
        </ms-table-column>
        <ms-table-column prop="name" :label="$t('api_test.variable_name')" min-width="200" sortable>
          <template slot-scope="scope">
            <el-input
              v-model="scope.row.name" size="mini"
              maxlength="200" :placeholder="$t('api_test.variable_name')" show-word-limit
              @change="change"/>
          </template>
        </ms-table-column>
        <ms-table-column prop="type" :label="$t('test_track.case.type')" min-width="140" sortable>
          <template slot-scope="scope">
            <el-select v-model="scope.row.type" :placeholder="$t('commons.please_select')" size="mini"
                       @change="changeType(scope.row)">
              <el-option v-for="item in typeSelectOptions " :key="item.value" :label="item.label" :value="item.value"/>
            </el-select>
          </template>
        </ms-table-column>

        <el-table-column prop="value" :label="$t('api_test.value')"
                         min-width="200px" sortable show-overflow-tooltip>
          <template slot-scope="scope">
            <el-input v-model="scope.row.value" size="mini" v-if="scope.row.type !=='CSV'"
                      :placeholder="valueText(scope.row)"
                      :disabled="scope.row.type === 'COUNTER' || scope.row.type === 'RANDOM'"/>
            <csv-file-upload :parameter="scope.row" v-if="scope.row.type ==='CSV'"/>
          </template>
        </el-table-column>
        <ms-table-column prop="description" :label="$t('commons.remark')"
                         min-width="160" sortable>
          <template slot-scope="scope">
            <el-input v-model="scope.row.description" size="mini"/>
          </template>
        </ms-table-column>

        <ms-table-column :label="$t('commons.operating')" width="150">
          <template v-slot:default="scope">
          <span>
              <el-switch v-model="scope.row.enable" size="mini"/>
            <el-tooltip effect="dark" :content="$t('commons.remove')" placement="top-start">
              <el-button icon="el-icon-delete" type="danger" circle size="mini" style="margin-left: 10px"
                         @click="remove(scope.row)" v-if="scope.row.name"/>
            </el-tooltip>
            <el-tooltip effect="dark" :content="$t('schema.adv_setting')" placement="top-start">
              <el-button icon="el-icon-setting" circle size="mini" style="margin-left: 10px"
                         @click="openSetting(scope.row)" v-if="scope.row.type !=='LIST'" @change="change"/>
            </el-tooltip>

          </span>
          </template>
        </ms-table-column>
      </ms-table>
    </div>
    <batch-add-parameter @batchSave="batchSave" ref="batchAdd"/>
    <api-variable-setting ref="apiVariableSetting"></api-variable-setting>
    <variable-import ref="variableImport" @mergeData="mergeData"></variable-import>
  </div>
</template>

<script>
import {KeyValue} from "@/business/components/api/test/model/ScenarioModel";
import MsApiVariableInput from "@/business/components/api/automation/scenario/ApiVariableInput";
import BatchAddParameter from "@/business/components/api/definition/components/basis/BatchAddParameter";
import MsTableButton from "@/business/components/common/components/MsTableButton";
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import ApiVariableSetting from "@/business/components/api/test/components/environment/ApiVariableSetting";
import CsvFileUpload from "@/business/components/api/test/components/environment/CsvFileUpload";
import {downloadFile, getUUID} from "@/common/js/utils";
import VariableImport from "@/business/components/api/test/components/environment/VariableImport";

export default {
  name: "MsApiScenarioVariables",
  components: {
    BatchAddParameter,
    MsApiVariableInput,
    MsTableButton,
    MsTable,
    MsTableColumn,
    ApiVariableSetting,
    CsvFileUpload,
    VariableImport
  },
  props: {
    description: String,
    items: Array,
    isReadOnly: {
      type: Boolean,
      default: false
    },
    showVariable: {
      type: Boolean,
      default: true
    },
    showCopy: {
      type: Boolean,
      default: true
    },
  },
  data() {
    return {
      loading: false,
      screenHeight: '400px',
      batchButtons: [
        {
          name: this.$t('api_test.definition.request.batch_delete'),
          handleClick: this.handleDeleteBatch,
        },
      ],
      typeSelectOptions: [
        {value: 'CONSTANT', label: this.$t('api_test.automation.constant')},
        {value: 'LIST', label: this.$t('test_track.case.list')},
        {value: 'CSV', label: 'CSV'},
        {value: 'COUNTER', label: this.$t('api_test.automation.counter')},
        {value: 'RANDOM', label: this.$t('api_test.automation.random')},
      ],
      variables: {},
      selectVariable: '',
      editData: {},
    }
  },
  watch: {
    items: {
      handler(v) {
        this.variables = v;
        this.sortParameters();
      },
      immediate: true,
      deep: true
    }
  },
  methods: {
    remove: function (index) {
      const dataIndex = this.variables.findIndex(d => d.name === index.name);
      this.variables.splice(dataIndex, 1);
      this.$emit('change', this.variables);
    },
    change: function () {
      let isNeedCreate = true;
      let removeIndex = -1;
      let repeatKey = "";
      this.variables.forEach((item, index) => {
        this.variables.forEach((row, rowIndex) => {
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
      if (repeatKey !== "") {
        this.$warning(this.$t('api_test.environment.common_config') + "【" + repeatKey + "】" + this.$t('load_test.param_is_duplicate'));
      }
      if (isNeedCreate && !repeatKey) {
        this.variables.push(new KeyValue({enable: true, id: getUUID(), type: 'CONSTANT'}));
      }
      this.$emit('change', this.variables);
      // TODO 检查key重复

    },
    changeType(data) {
      if (!data.delimiter || (!data.files && data.files.length === 0) || !data.quotedData) {
        data.delimiter = ',';
        data.files = [];
        data.quotedData = 'false';
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
      let restaurants = [{value: "UTF-8"}, {value: "UTF-16"}, {value: "GB2312"}, {value: "ISO-8859-15"}, {value: "US-ASCll"}];
      let results = queryString ? restaurants.filter(this.createFilter(queryString)) : restaurants;
      // 调用 callback 返回建议列表的数据
      cb(results);
    },
    sortParameters() {
      let index = 1;
      this.variables.forEach(item => {
        item.num = index;
        if (!item.type || item.type === 'text') {
          item.type = 'CONSTANT';
        }
        if (!item.id) {
          item.id = getUUID();
        }
        if (item.remark) {
          item.description = item.remark;
        }
        index++;
      });
    },
    handleDeleteBatch() {
      this.$alert(this.$t('api_test.environment.variables_delete_info') + ' ' + " ？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let ids = this.$refs.variableTable.selectRows;
            ids.forEach(row => {
              if (row.name) {
                const index = this.variables.findIndex(d => d.name === row.name);
                this.variables.splice(index, 1);
              }
            });
            this.sortParameters();
            this.$refs.variableTable.cancelCurrentRow();
            this.$refs.variableTable.clear();
          }
        }
      });
    },
    filter() {
      let datas = [];
      this.variables.forEach(item => {
        if (this.selectVariable && this.selectVariable != "" && item.name) {
          if (item.name.toLowerCase().indexOf(this.selectVariable.toLowerCase()) == -1) {
            item.hidden = true;
          } else {
            item.hidden = undefined;
          }
        } else {
          item.hidden = undefined;
        }
        datas.push(item);
      });
      this.variables = datas;
    },
    openSetting(data) {
      this.$refs.apiVariableSetting.open(data);
    },
    isDisable: function (index) {
      return this.items.length - 1 === index;
    },
    _handleBatchVars(data) {
      let params = data.split("\n");
      let keyValues = [];
      params.forEach(item => {
        if (item) {
          let line = item.split(/：|:/);
          let values = item.split(line[0] + ":");
          let required = false;
          keyValues.push(new KeyValue({
            name: line[0],
            required: required,
            value: values[1],
            type: 'CONSTANT',
            valid: false,
            file: false,
            encode: true,
            enable: true,
            description: undefined
          }));
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
        keyValues.forEach(keyValue => {
          let isAdd = true;
          this.variables.forEach(item => {
            if (item.name === keyValue.name) {
              item.value = keyValue.value;
              isAdd = false;
            }
          })
          if (isAdd) {
            this.variables.splice(this.variables.indexOf(i => !i.name), 0, keyValue);
          }
        })
      }
    },
    onChange() {
      this.sortParameters();
    },
    exportJSON() {
      if (this.$refs.variableTable.selectIds.length < 1) {
        this.$warning(this.$t('api_test.environment.select_variable'));
        return;
      }
      let variablesJson = [];
      let messages = '';
      let rows = this.$refs.variableTable.selectRows;
      rows.forEach(row => {
        if (row.type === 'CSV') {
          messages = this.$t('variables.csv_download')
        }
        if (row.name) {
          variablesJson.push(row);
        }
      })
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
      JSON.parse(data).map(importData => {
        importData.id = getUUID();
        importData.enable = true;
        importData.showMore = false;
        let sameNameIndex = this.variables.findIndex(d => d.name === importData.name);
        if (sameNameIndex !== -1) {
          if (modeId === 'fullCoverage') {
            this.variables.splice(sameNameIndex, 1, importData);
          }
        } else {
          this.variables.splice(this.variables.length - 1, 0, importData);
        }
      })
    }
  },
  created() {
    if (this.items.length === 0) {
      this.items.push(new KeyValue({enable: true}));
    }
  }
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

/deep/ .table-select-icon {
  display: none !important;
}
</style>
