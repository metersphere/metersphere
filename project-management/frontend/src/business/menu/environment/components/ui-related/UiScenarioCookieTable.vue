<template>
  <div>
    <div
      style="
        border: 1px #dcdfe6 solid;
        min-height: 50px;
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
        :screen-height="'100px'"
        :remember-order="true"
        :highlightCurrentRow="true"
        @refresh="onChange"
        :enable-selection="false"
        ref="variableTable"
      >
        <ms-table-column prop="cookie" label="cookie" min-width="160">
          <template slot-scope="scope">
            <el-input
              v-model="scope.row.cookie"
              size="mini"
              :placeholder="$t('cookie')"
              @change="change(scope.row)"
            />
          </template>
        </ms-table-column>

        <!--        <ms-table-column-->
        <!--          prop="userName"-->
        <!--          :label="$t('api_test.request.sql.username')"-->
        <!--          min-width="200"-->
        <!--        >-->
        <!--          <template slot-scope="scope">-->
        <!--            <el-input-->
        <!--              v-model="scope.row.userName"-->
        <!--              size="mini"-->
        <!--              maxlength="200"-->
        <!--              :placeholder="$t('api_test.request.sql.username')"-->
        <!--              show-word-limit-->
        <!--              @change="change"-->
        <!--            />-->
        <!--          </template>-->
        <!--        </ms-table-column>-->

        <!--        <ms-table-column-->
        <!--          prop="password"-->
        <!--          :label="$t('api_test.request.tcp.password')"-->
        <!--          min-width="140"-->
        <!--        >-->
        <!--          <template slot-scope="scope">-->
        <!--            <el-input-->
        <!--              v-model="scope.row.password"-->
        <!--              size="mini"-->
        <!--              maxlength="200"-->
        <!--              show-password-->
        <!--              :placeholder="$t('api_test.request.tcp.password')"-->
        <!--              @change="change"-->
        <!--            />-->
        <!--          </template>-->
        <!--        </ms-table-column>-->

        <ms-table-column
          prop="description"
          :label="$t('commons.validity_period')"
          min-width="200"
          :editContent="'aaa'"
        >
          <template slot-scope="scope">
            <mini-timing-item :expr.sync="scope.row.expireTime" @chooseChange="change(scope.row)"></mini-timing-item>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="updateTime"
          :label="$t('commons.update_time')"
          min-width="160"
        >
          <template slot-scope="scope">
            {{
              scope.row.updateTime | datetimeFormat
            }}
          </template>
        </ms-table-column>

        <ms-table-column :label="$t('commons.operating')" width="150">
          <template v-slot:default="scope">
            <el-switch v-model="scope.row.enable" size="mini"></el-switch>
            <el-tooltip
              effect="dark"
              :content="$t('environment.relevance_ui')"
              v-if="!existCookieConfig"
              placement="top-start"
            >
              <el-button
                icon="el-icon-setting"
                circle
                size="mini"
                @click="openRelevance"
                style="margin-left: 10px"
              />
            </el-tooltip>

            <el-dropdown @command="handleCommand" v-if="existCookieConfig">
              <el-button
                icon="el-icon-paperclip"
                circle
                size="mini"
                style="margin-left: 10px"
              />

              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="view">{{ $t("environment.view_ui_relevane") }}</el-dropdown-item>
                <el-dropdown-item command="cancelRelevance">{{ $t("environment.cancel_ui_relevane") }}
                </el-dropdown-item>
                <el-dropdown-item command="relevance">{{ $t("environment.re_ui_relevane") }}</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>

          </template>
        </ms-table-column>
      </ms-table>
    </div>
    <batch-add-parameter @batchSave="batchSave" ref="batchAdd"/>
    <api-variable-setting ref="apiVariableSetting"></api-variable-setting>

    <!-- 关联登录获取cookie的场景 -->
    <ui-scenario-edit-relevance
      ref="relevanceUiDialog"
      @reference="reference"
      :scenarioType="currentRelevanceType"
    />

    <variable-import
      ref="variableImport"
      @mergeData="mergeData"
    ></variable-import>
  </div>
</template>

<script>
import {KeyValue} from "metersphere-frontend/src/model/EnvTestModel";
import MsApiVariableInput from "metersphere-frontend/src/components/environment/commons/ApiVariableInput";
import BatchAddParameter from "metersphere-frontend/src/components/environment/commons/BatchAddParameter";
import MsTableButton from "metersphere-frontend/src/components/MsTableButton";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import ApiVariableSetting from "metersphere-frontend/src/components/environment/commons/ApiVariableSetting";
import CsvFileUpload from "metersphere-frontend/src/components/environment/commons/variable/CsvFileUpload";
import {downloadFile, getUUID, operationConfirm} from "metersphere-frontend/src/utils";
import VariableImport from "metersphere-frontend/src/components/environment/VariableImport";
import {cloneDeep} from "lodash-es";
import MiniTimingItem from "metersphere-frontend/src/components/environment/commons/MiniTimingItem";
import UiScenarioEditRelevance from "@/business/menu/environment/components/ui-related/UiScenarioEditRelevance";
import {getCurrentProjectID, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";

export default {
  name: "MsUiScenarioCookieTable",
  components: {
    BatchAddParameter,
    MsApiVariableInput,
    MsTableButton,
    MsTable,
    MsTableColumn,
    ApiVariableSetting,
    CsvFileUpload,
    VariableImport,
    MiniTimingItem,
    UiScenarioEditRelevance,
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
      variables: {},
      selectVariable: "",
      editData: {},
      scopeTypeFilters: [
        {text: this.$t("commons.api"), value: "api"},
        {text: this.$t("commons.ui_test"), value: "ui"},
      ],
      currentRelevanceType: 'scenario',
      existCookieConfig: false
    };
  },
  watch: {
    items: {
      handler(val) {
        this.variables = val;
        this.sortParameters();
      },
      immediate: true,
      deep: true,
    },
    variables: {
      handler(v) {
        if (this.variables && this.variables.length && this.variables[0].relevanceId) {
          this.existCookieConfig = true;
        } else {
          this.existCookieConfig = false;
        }
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
      this.variables[0].updateTime = new Date().getTime();
      this.$emit("change", this.variables);
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
      if (this.variables) {
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
          index++;
        });
      }
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
    filterScope(scope) {
      let datas = [];
      let variables = cloneDeep(this.variables);
      variables.forEach((item) => {
        if (scope == "api") {
          if (
            item.scope && item.scope != "api"
          ) {
            item.hidden = true;
          } else {
            item.hidden = undefined;
          }
        } else {
          if (item.scope == scope) {
            item.hidden = undefined;
          } else {
            item.hidden = true;
          }
        }
        datas.push(item);
      });
      this.variables = datas;
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
    handleCommand(c) {
      switch (c) {
        case "view":
          this.redirectPage(this.variables[0].relevanceId);
          break;
        case "cancelRelevance":
          this.variables[0].relevanceId = null;
          this.$success(this.$t("organization.integration.successful_operation"));
          break;
        case "relevance":
          this.openRelevance("scenario", "scenario",);
          break;
        default:
          break;
      }
    },
    redirectPage(resourceId) {
      let uuid = getUUID().substring(1, 5);
      let projectId = getCurrentProjectID();
      let workspaceId = getCurrentWorkspaceId();
      let prefix = '/#';
      if (
        this.$route &&
        this.$route.path.startsWith('/#')
      ) {
        prefix = '';
      }
      let path = `/ui/automation/?redirectID=${uuid}&dataType=scenario&projectId=${projectId}&workspaceId=${workspaceId}&resourceId=${resourceId}`;
      let data = this.$router.resolve({
        path: path,
      });
      window.open(data.href, '_blank');
    },
    openRelevance() {
      this.$refs.relevanceUiDialog.open();
    },
    reference(id) {
      this.variables[0].relevanceId = id.keys().next().value.id;
      this.$refs.relevanceUiDialog.close();
      this.$success(this.$t('commons.save_success'));
    },
    validate() {
      if (!this.variables[0].enable) {
        return true;
      }
      let cookieConfig = this.variables[0];
      if (!cookieConfig) {
        this.$warning(this.$t("配置错误"));
        return false;
      }
      if (!cookieConfig.expireTime || cookieConfig.expireTime === "") {
        this.$warning(this.$t("environment.need_expire_time"));
        return false;
      }
      if (!cookieConfig.relevanceId) {
        this.$warning(this.$t("environment.need_relevance_ui_scenario"));
        return false;
      }
      return true;
    }
  },
  created() {
    if (!this.items || this.items.length === 0) {
      this.items = [];
      this.items.push(new KeyValue({id: getUUID(), enable: true, expireTime: '1M'}));
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
