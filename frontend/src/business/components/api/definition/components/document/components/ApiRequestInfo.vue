<template>
  <div>
    <el-row class="apiInfoRow">
      <div>
        <el-table border v-if="formParamTypes.includes(apiInfo.requestBodyParamType)" :show-header="true" row-key="id"
                  :row-class-name="getRowClassName"
                  :data="tableData" :class="getTableClass()" ref="expandTable">
          <el-table-column prop="name"
                           :label="$t('api_test.definition.document.table_coloum.name')"
                           min-width="120px"
                           show-overflow-tooltip/>
          <el-table-column prop="contentType"
                           :label="$t('api_test.definition.document.table_coloum.type')"
                           min-width="120px"
                           show-overflow-tooltip/>
          <el-table-column prop="description"
                           :label="$t('api_test.definition.document.table_coloum.desc')"
                           min-width="280px"
                           show-overflow-tooltip/>
          <el-table-column prop="required"
                           :label="$t('api_test.definition.document.table_coloum.is_required')"
                           :formatter="formatBoolean"
                           min-width="80px"
                           show-overflow-tooltip/>
          <el-table-column prop="value"
                           :label="$t('api_test.definition.document.table_coloum.default_value')"
                           min-width="120px"
                           show-overflow-tooltip/>
          <el-table-column type="expand" :label="getCollapseOption()" width="80px">
            <template slot="header">
              <el-button type="text" size="mini" @click="expandAllRows">
                  <span :id="tableExpandButtonId">
                  {{ expandTitle }}
                  </span>
              </el-button>
            </template>
            <template v-slot:default="scope">
              <table-advanced-setting :table-data="scope.row"></table-advanced-setting>
            </template>
          </el-table-column>
        </el-table>
        <div v-else-if="apiInfo.requestBodyParamType === 'JSON-SCHEMA' || apiInfo.requestBodyParamType === 'JSON'"
             style="margin-left: 10px">
          <json-schema-show :show-preview="false" :json-schema-disable="true" :body="apiInfo.jsonSchemaBody"
                            ref="jsonCodeEdit"/>
        </div>
        <div v-else class="showDataDiv">
          <br/>
          <p style="margin: 0px 20px;"
             v-html="formatRowData(apiInfo.requestBodyParamType,apiInfo.requestBodyStrutureData)">
          </p>
          <br/>
        </div>
      </div>
    </el-row>

    <el-row class="apiInfoRow">
      <div class="blackFontClass">
        {{ $t('api_test.definition.document.example_presentation') }}
      </div>
      <div class="showDataDiv">
        <br/>
        <p style="margin: 0px 20px;"
           v-html="genPreviewData(apiInfo.requestPreviewData)">
        </p>
        <br/>
      </div>
    </el-row>
  </div>
</template>

<script>

import JsonSchemaShow
  from "@/business/components/api/definition/components/document/components/JsonSchema/JsonSchemaShow";
import tableAdvancedSetting
  from "@/business/components/api/definition/components/document/components/plugin/TableAdvancedSetting";
import {getCurrentUser, getUUID} from "@/common/js/utils";

export default {
  name: "ApiRequestInfo",
  components: {JsonSchemaShow, tableAdvancedSetting},
  data() {
    return {
      tableData: [],
      language: "zh_CN",
      tableExpandButtonId: "docTableExpandBtn" + getUUID(),
      active: true,
      expandAllRow: false,
      expandTitle: this.$t("commons.expand_all"),
      formParamTypes: ['form-data', 'x-www-from-urlencoded', 'BINARY'],
    };
  },
  props: {
    apiInfo: Object,
  },
  activated() {
    if (this.apiInfo && this.apiInfo.requestBodyFormData) {
      this.tableData = this.getJsonArr(this.apiInfo.requestBodyFormData);
    }
    //获取language，用于改变表格的展开、收起文字  zh_CN/zh_TW/en_US
    let user = getCurrentUser();
    if (user) {
      this.language = user.language;
    }
  },
  created: function () {
    if (this.apiInfo && this.apiInfo.requestBodyFormData) {
      this.tableData = this.getJsonArr(this.apiInfo.requestBodyFormData);
    }
    //获取language，用于改变表格的展开、收起文字  zh_CN/zh_TW/en_US
    let user = getCurrentUser();
    if (user) {
      this.language = user.language;
    }
  },
  mounted() {
    if (this.apiInfo && this.apiInfo.requestBodyFormData) {
      this.tableData = this.getJsonArr(this.apiInfo.requestBodyFormData);
    }
    //获取language，用于改变表格的展开、收起文字  zh_CN/zh_TW/en_US
    let user = getCurrentUser();
    if (user) {
      this.language = user.language;
    }
  },
  computed: {},
  watch: {
    'apiInfo.requestBodyFormData': {
      handler(v) {
        this.tableData = this.getJsonArr(this.apiInfo.requestBodyFormData);
      },
      deep: true
    },
    expandAllRow() {
      if (this.$refs.expandTable) {
        let expand = this.expandAllRow;
        if (this.tableData) {
          this.$nextTick(() => {
            this.tableData.forEach(i => {
              if (i.hasAdvancedSetting) {
                this.$refs.expandTable.toggleRowExpansion(i, expand)
              }
            });
            this.$refs.expandTable.doLayout();
          })
        }
      }
      this.expandTitle = this.expandAllRow ? this.$t("commons.close_all") : this.$t("commons.expand_all");
      let tableHeaderDom = document.getElementById(this.tableExpandButtonId);
      tableHeaderDom.innerText = this.expandTitle;
    }
  },
  methods: {
    getRowClassName({row, rowIndex}) {
      let classname = 'autofix-table-row ';
      // 通过判断给不需要展开行功能的数据设置样式，通过样式去隐藏展开行图标
      if (!row.hasAdvancedSetting) {
        classname += ' hide-expand';
      }
      return classname;
    },
    isNotEmptyValue(value) {
      return value && value !== '';
    },
    rowHasAdvancedSetting(tableData) {
      let hasAdvancedSetting = false;
      if (this.isNotEmptyValue(tableData['min'])) {
        hasAdvancedSetting = true;
      } else if (this.isNotEmptyValue(tableData['max'])) {
        hasAdvancedSetting = true;
      } else if (tableData['urlEncode']) {
        hasAdvancedSetting = true;
      } else if (this.isNotEmptyValue(tableData['description'])) {
        hasAdvancedSetting = true;
      }
      return hasAdvancedSetting;
    },
    getTableClass() {
      if (this.language === "zh_TW") {
        return "test-content document-table tw-table";
      } else if (this.language === "en_US") {
        return "test-content document-table us-table";
      } else {
        return "test-content document-table cn-table";
      }
    },
    expandAllRows() {
      this.expandAllRow = !this.expandAllRow;
    },
    getCollapseOption() {
      if (this.expandAllRow) {
        return this.$t('api_test.definition.document.close');
      } else {
        return this.$t('api_test.definition.document.open');
      }
    },
    formatBoolean(row, column, cellValue) {
      var ret = '';  //你想在页面展示的值
      if (cellValue) {
        ret = "是";  //根据自己的需求设定
      } else {
        ret = "否";
      }
      return ret;
    },
    formatRowData(dataType, data) {
      var returnData = data;
      if (data) {
        returnData = "<xmp>" + returnData + "</xmp>";
      }
      return returnData;
    },
    getJsonArr(jsonString) {
      let returnJsonArr = [];
      if (jsonString === '无' || jsonString === null) {
        return returnJsonArr;
      }

      let jsonArr = JSON.parse(jsonString);
      //遍历，把必填项空的数据去掉
      for (var index = 0; index < jsonArr.length; index++) {
        var item = jsonArr[index];
        if (item.name !== "" && item.name !== null) {
          item.id = getUUID();
          if (this.rowHasAdvancedSetting(item)) {
            item.hasAdvancedSetting = true;
          } else {
            item.hasAdvancedSetting = false;
          }
          returnJsonArr.push(item);
        }
      }
      return returnJsonArr;
    },
    formatRowDataToJsonSchema(api, jsonType) {
      if (jsonType === 'request' && api.requestBodyStrutureData) {
        try {
          let bodyStructData = JSON.parse(api.requestBodyStrutureData);
          api.requestJsonSchema = {'raw': bodyStructData};
          return true;
        } catch (e) {
          return false;
        }
      } else if (jsonType === 'response' && api.responseBodyStrutureData) {
        try {
          JSON.parse(api.responseBodyStrutureData);
          api.responseJsonSchema = {'raw': api.responseBodyStrutureData};
          return true;
        } catch (e) {
          return false;
        }
      } else {
        return false;
      }
    },
    //构建预览数据
    genPreviewData(previewData) {
      return previewData;
    },
  },
};
</script>

<style scoped>
.apiInfoRow {
  margin: 10px 10px;
}

.blackFontClass {
  font-weight: bold;
  font-size: 14px;
}

.showDataDiv {
  background-color: #F5F7F9;
  margin: 10px 10px;
  max-height: 300px;
  overflow: auto;
}


.document-table {
  margin: 10px 0px 10px 10px;
  width: auto;
}

.document-table /deep/ .el-table__row {
  font-size: 12px;
  font-weight: initial;
}

.document-table /deep/ .has-gutter {
  font-size: 12px;
  color: #404040;
}

.document-table /deep/ td {
  border-right: 0px solid #EBEEF5
}

/*通过样式隐藏图标*/
.document-table /deep/ .hide-expand .el-table__expand-column .cell {
  visibility: hidden;
}

/*修改展开按钮时不旋转*/
.document-table /deep/ .el-table__expand-icon {
  -webkit-transform: rotate(0deg);
  transform: rotate(0deg);
}

.document-table /deep/ .el-table__expanded-cell {
  padding: 5px;
}

.document-table /deep/ .el-icon-arrow-right {
  position: unset;
}

.document-table /deep/ th {
  background-color: #FAFAFA;
  border-right: 0px solid #EBEEF5
}

/*展开按钮未点击的样式是加号带边框*/
.cn-table /deep/ .el-table__expand-icon .el-icon-arrow-right:before {
  position: unset;
  content: "展开";
  padding: 2px;
}

/*展开按钮点击后的样式是减号带边框*/
.cn-table /deep/ .el-table__expand-icon--expanded .el-icon-arrow-right:before {
  position: unset;
  content: "收起";
}

.tw-table /deep/ .el-table__expand-icon .el-icon-arrow-right:before {
  position: unset;
  content: "展開";
  padding: 2px;
}

.tw-table /deep/ .el-table__expand-icon--expanded .el-icon-arrow-right:before {
  position: unset;
  content: "收起";
}

.us-table /deep/ .el-table__expand-icon .el-icon-arrow-right:before {
  position: unset;
  content: "Open";
  padding: 2px;
}

.us-table /deep/ .el-table__expand-icon--expanded .el-icon-arrow-right:before {
  position: unset;
  content: "Close";
  padding: 2px;
}
</style>
