<template>
  <div>
    <div>
      <div style="padding-bottom: 10px; float: left">
        <el-input
          :placeholder="$t('api_test.search_by_variables')"
          size="mini"
          v-model="selectVariable"
          @change="filter"
          @keyup.enter="filter"
        >
        </el-input>
      </div>
      <div style="padding-bottom: 10px; float: right">
        <ms-table-button
          v-permission="['PROJECT_ENVIRONMENT:READ+IMPORT']"
          icon="el-icon-box"
          :content="$t('commons.import')"
          @click="importJSON"
        />
        <el-dropdown @command="handleExportCommand" class="scenario-ext-btn" trigger="hover"
                     v-permission="['PROJECT_ENVIRONMENT:READ+EXPORT']">
          <ms-table-button
            style="margin-left: 10px"
            icon="el-icon-box"
            :content="$t('commons.export')"
          />
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="exportApi">{{ $t('envrionment.export_variable_tip') }}</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>

        <el-link
          style="margin-left: 10px"
          @click="batchAdd"
          type="primary"
          :disabled="isReadOnly"
        >
          {{ $t("commons.batch_add") }}
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
      "
    >
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
        ref="variableTable"
      >
        <ms-table-column prop="num" sortable label="ID" min-width="60">
        </ms-table-column>

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
              @change="changeType(scope.row)"
            >
              <el-option
                v-for="item in scopeTypeFilters"
                :key="item.value"
                :label="item.text"
                :value="item.value"
              />
            </el-select>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="name"
          :label="$t('api_test.variable_name')"
          min-width="200"
          sortable
        >
          <template slot-scope="scope">
            <el-input
              v-model="scope.row.name"
              size="mini"
              maxlength="200"
              :placeholder="$t('api_test.variable_name')"
              show-word-limit
              @change="change"
            />
          </template>
        </ms-table-column>
        <ms-table-column
          prop="type"
          :label="$t('test_track.case.type')"
          min-width="140"
          sortable
        >
          <template slot-scope="scope">
            <el-select
              v-model="scope.row.type"
              v-if="!scope.row.scope || scope.row.scope == 'api'"
              :placeholder="$t('commons.please_select')"
              size="mini"
            >
              <el-option
                v-for="item in typeSelectOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>

            <el-select
              v-else
              v-model="scope.row.type"
              :placeholder="$t('commons.please_select')"
              size="mini"
            >
              <el-option
                v-for="item in uiTypeSelectOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </template>
        </ms-table-column>

        <el-table-column
          prop="value"
          :label="$t('api_test.value')"
          min-width="200px"
          sortable
          show-overflow-tooltip
        >
          <template slot-scope="scope">
            <el-input
              v-model="scope.row.value"
              size="mini"
              v-if="scope.row.type !== 'CSV'"
              :placeholder="valueText(scope.row)"
              :disabled="
                scope.row.type === 'COUNTER' || scope.row.type === 'RANDOM'
              "
            />
            <csv-file-upload
              :parameter="scope.row"
              v-if="scope.row.type === 'CSV'"
            />
          </template>
        </el-table-column>
        <ms-table-column
          prop="description"
          :label="$t('commons.remark')"
          min-width="160"
          sortable
        >
          <template slot-scope="scope">
            <el-input v-model="scope.row.description" size="mini"/>
          </template>
        </ms-table-column>

        <ms-table-column :label="$t('commons.operating')" width="150">
          <template v-slot:default="scope">
            <span>
              <el-switch v-model="scope.row.enable" size="mini"/>
              <el-tooltip
                effect="dark"
                :content="$t('commons.remove')"
                placement="top-start"
              >
                <el-button
                  icon="el-icon-delete"
                  type="danger"
                  circle
                  size="mini"
                  style="margin-left: 10px"
                  @click="remove(scope.row)"
                  v-if="isDisable(scope.row)"
                />
              </el-tooltip>
              <el-tooltip
                v-if="!scope.row.scope || scope.row.scope == 'api'"
                effect="dark"
                :content="$t('schema.adv_setting')"
                placement="top-start"
              >
                <el-button
                  icon="el-icon-setting"
                  circle
                  size="mini"
                  style="margin-left: 10px"
                  @click="openSetting(scope.row)"
                  v-if="scope.row.type !== 'LIST'"
                  @change="change"
                />
              </el-tooltip>
            </span>
          </template>
        </ms-table-column>
      </ms-table>
    </div>
    <batch-add-parameter @batchSave="batchSave" ref="batchAdd"/>
    <api-variable-setting ref="apiVariableSetting"></api-variable-setting>
    <variable-import
      ref="variableImport"
      @mergeData="mergeData"
    ></variable-import>
  </div>
</template>

<script>
import {KeyValue} from "../../../model/EnvTestModel";
import MsApiVariableInput from "./ApiVariableInput";
import BatchAddParameter from "./BatchAddParameter";
import MsTableButton from "../../MsTableButton";
import MsTable from "../../table/MsTable";
import MsTableColumn from "../../table/MsTableColumn";
import ApiVariableSetting from "./ApiVariableSetting";
import CsvFileUpload from "./variable/CsvFileUpload";
import {downloadFile, getUUID, operationConfirm} from "../../../utils";
import VariableImport from "./variable/VariableImport";
import _ from "lodash";

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
    VariableImport,
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
      loading: false,
      screenHeight: "400px",
      batchButtons: [
        {
          name: this.$t("api_test.definition.request.batch_delete"),
          handleClick: this.handleDeleteBatch,
        },
      ],
      typeSelectOptions: [
        {value: "CONSTANT", label: this.$t("api_test.automation.constant")},
        {value: "LIST", label: this.$t("test_track.case.list")},
        {value: "CSV", label: "CSV"},
        {value: "COUNTER", label: this.$t("api_test.automation.counter")},
        {value: "RANDOM", label: this.$t("api_test.automation.random")},
      ],
      uiTypeSelectOptions: [
        {value: "STRING", label: this.$t("api_test.automation.string")},
        {value: "ARRAY", label: this.$t("api_test.automation.array")},
        {value: "JSON", label: this.$t("api_test.automation.json")},
        {value: "NUMBER", label: this.$t("api_test.automation.number")},
      ],
      variables: {},
      selectVariable: "",
      editData: {},
      scopeTypeFilters: [
        {text: this.$t("commons.api"), value: "api"},
        {text: this.$t("commons.ui_test"), value: "ui"},
      ]
    };
  },
  watch: {
    items: {
      handler(v) {
        this.variables = v;
        this.sortParameters();
      },
      immediate: true,
      deep: true,
    },
  },
  methods: {
    remove: function (index) {
      const dataIndex = this.variables.findIndex((d) => d.name === index.name);
      this.variables.splice(dataIndex, 1);
      this.$emit("change", this.variables);
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
        this.$warning(
          this.$t("api_test.environment.common_config") +
          "【" +
          repeatKey +
          "】" +
          this.$t("load_test.param_is_duplicate")
        );
      }
      if (isNeedCreate) {
        this.variables.push(
          new KeyValue({enable: true, id: getUUID(), type: "CONSTANT", scope: "api"})
        );
      }
      this.$emit("change", this.variables);
      // TODO 检查key重复
    },
    changeType(data) {
      data.value = "";
      if (
        !data.delimiter ||
        (!data.files && data.files.length === 0) ||
        !data.quotedData
      ) {
        data.delimiter = ",";
        data.files = [];
        data.quotedData = "false";
      }

      if (!data.scope || data.scope == "ui") {
        data.type = 'STRING';
      }
    },
    valueText(data) {
      switch (data.type) {
        case "LIST":
          return this.$t("api_test.environment.list_info");
        case "CONSTANT":
          return this.$t("api_test.value");
        case "COUNTER":
        case "RANDOM":
          return this.$t("api_test.environment.advanced_setting");
        default:
          return this.$t("api_test.value");
      }
    },
    querySearch(queryString, cb) {
      let restaurants = [
        {value: "UTF-8"},
        {value: "UTF-16"},
        {value: "GB2312"},
        {value: "ISO-8859-15"},
        {value: "US-ASCll"},
      ];
      let results = queryString
        ? restaurants.filter(this.createFilter(queryString))
        : restaurants;
      // 调用 callback 返回建议列表的数据
      cb(results);
    },
    sortParameters() {
      let index = 1;
      this.variables.forEach((item) => {
        item.num = index;
        if (!item.type || item.type === "text") {
          item.type = "CONSTANT";
        }
        if (!item.id) {
          item.id = getUUID();
        }
        if (item.remark) {
          this.$set(item, "description", item.remark);
          item.remark = undefined;
        }
        if (!item.scope) {
          this.$set(item, "scope", "api");
        }
        index++;
      });
    },
    handleDeleteBatch() {
      operationConfirm(
        this,
        this.$t("api_test.environment.variables_delete_info") + " ？",
        () => {
          let ids = this.$refs.variableTable.selectRows;
          ids.forEach((row) => {
            if (row.id) {
              const index = this.variables.findIndex((d) => d.id === row.id);
              if (index !== this.variables.length - 1) {
                this.variables.splice(index, 1);
              }
            }
          });
          this.sortParameters();
          this.$refs.variableTable.cancelCurrentRow();
          this.$refs.variableTable.clear();
        }
      );
    },
    filter(scope) {
      let datas = [];
      this.variables.forEach((item) => {
        if (this.selectVariable && this.selectVariable != "" && item.name) {
          if (
            item.name
              .toLowerCase()
              .indexOf(this.selectVariable.toLowerCase()) == -1
          ) {
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
    filterScope(value, row) {
      if (value == "ui") {
        return row.scope == "ui";
      }
      return !row.scope || row.scope == "api";
    },
    openSetting(data) {
      this.$refs.apiVariableSetting.open(data);
    },
    isDisable: function (row) {
      const index = this.variables.findIndex((d) => d.name === row.name);
      return this.variables.length - 1 !== index;
    },
    _handleBatchVars(data) {
      let params = data.split("\n");
      let keyValues = [];
      params.forEach((item) => {
        if (item) {
          let line = item.split(/：|:/);
          let values = item.split(line[0] + ":");
          let required = false;
          keyValues.push(
            new KeyValue({
              name: line[0],
              required: required,
              value: values[1],
              type: "CONSTANT",
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
          this.variables.forEach((item) => {
            if (item.name === keyValue.name) {
              item.value = keyValue.value;
              isAdd = false;
            }
          });
          if (isAdd) {
            this.variables.splice(
              this.variables.indexOf((i) => !i.name),
              0,
              keyValue
            );
          }
        });
      }
    },
    onChange() {
      this.sortParameters();
    },
    exportJSON() {
      let apiVariable = [];
      this.$refs.variableTable.selectRows.forEach((r) => {
        if (!r.scope || r.scope != "ui") {
          apiVariable.push(r);
        }
      });

      if (apiVariable.length < 1) {
        this.$warning(this.$t("api_test.environment.select_api_variable"));
        return;
      }
      let variablesJson = [];
      let messages = "";
      let rows = this.$refs.variableTable.selectRows;
      rows.forEach((row) => {
        if (row.type === "CSV") {
          messages = this.$t("variables.csv_download");
        }
        if (row.name && (!row.scope || row.scope == "api")) {
          variablesJson.push(row);
        }
      });
      if (messages !== "") {
        this.$warning(messages);
        return;
      }
      downloadFile(
        "MS_" + variablesJson.length + "_Environments_variables.json",
        JSON.stringify(variablesJson)
      );
    },
    importJSON() {
      this.$refs.variableImport.open();
    },
    mergeData(data, modeId) {
      JSON.parse(data).map((importData) => {
        importData.id = getUUID();
        importData.enable = true;
        importData.showMore = false;
        let sameNameIndex = this.variables.findIndex(
          (d) => d.name === importData.name
        );
        if (sameNameIndex !== -1) {
          if (modeId === "fullCoverage") {
            this.variables.splice(sameNameIndex, 1, importData);
          }
        } else {
          this.variables.splice(this.variables.length - 1, 0, importData);
        }
      });
    },
    handleExportCommand(command){
      this.exportJSON();
    }
  },
  created() {
    if (this.items.length === 0) {
      this.items.push(new KeyValue({enable: true, scope: "api"}));
    } else {
      //历史数据默认是 api 应用场景
      _.forEach(this.items, item => {
        if (!item.scope) {
          this.$set(item, "scope", "api");
        }
      })
      this.variables = this.items;
    }
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

:deep(.table-select-icon) {
  display: none !important;
}
</style>
