<template>
  <div>
    <el-row class="apiInfoRow">
      <div>
        <el-row>
          <div class="tip" style="float: left">
            <span>{{ title }}</span>
            <span v-if="remarks" style="font-weight: 400;font-size: smaller;background: #F1F0F0;margin-left: 5px;">
              {{ remarks }}
            </span>
          </div>
          <div class="right-button">
            <el-link v-if="active" @click="changeActive">
              {{ $t('api_test.definition.document.close') }} <i class="el-icon-arrow-up"/>
            </el-link>
            <el-link v-if="!active" @click="changeActive">
              {{ $t('api_test.definition.document.open') }} <i class="el-icon-arrow-down"/>
            </el-link>
          </div>
        </el-row>
        <el-collapse-transition>
          <div v-show="active">
            <div v-if="!showSlotCompnent">
              <div v-if="isText">
                <div class="showDataDiv">
                  <br/>
                  <p style="margin: 0px 20px;"
                     v-html="stringData">
                  </p>
                  <br/>
                </div>
              </div>
              <div v-else-if="tableData.length===0">
                <div class="simpleFontClass" style="margin-top: 10px">
                  {{ $t('api_test.definition.document.data_set.none') }}
                </div>
              </div>
              <div v-else>
                <el-table border :show-header="true" row-key="id" :row-class-name="getRowClassName"
                          :data="tableData" :class="getTableClass()" ref="expandTable">
                  <el-table-column v-for="item in tableColoumArr" :key="item.id"
                                   :prop="item.prop"
                                   :label="item.label"
                                   show-overflow-tooltip>
                  </el-table-column>
                  <el-table-column type="expand" v-if="tableCanExpand" :label="getCollapseOption()"
                                   width="80px">
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
              </div>
            </div>
            <slot name="request"></slot>
            <slot name="response"></slot>
          </div>
        </el-collapse-transition>
      </div>
    </el-row>
  </div>
</template>

<script>

import {getCurrentUser, getUUID} from "@/common/js/utils";
import tableAdvancedSetting
  from "@/business/components/api/definition/components/document/components/plugin/TableAdvancedSetting";

export default {
  name: "ApiInfoCollapse",
  components: {tableAdvancedSetting},
  data() {
    return {
      active: true,
      expandAllRow: false,
      language: "zh_CN",
      tableData: [],
      tableExpandButtonId: "docTableExpandBtn" + getUUID(),
      expandTitle: this.$t("commons.expand_all"),
      tableColoumArr: [
        {id: 1, prop: "name", label: this.$t('api_test.definition.document.table_coloum.name')},
        {id: 2, prop: "isRequired", label: this.$t('api_test.definition.document.table_coloum.is_required')},
        {id: 3, prop: "value", label: this.$t('api_test.definition.document.table_coloum.value')},
        {id: 4, prop: "description", label: this.$t('api_test.definition.document.table_coloum.desc')},
      ],
    };
  },
  props: {
    title: String,
    tableColoumType: String,
    remarks: String,
    isRequest: Boolean,
    isResponse: Boolean,
    tableCanExpand: {
      type: Boolean,
      default() {
        return true;
      }
    },
    isText: Boolean,
    stringData: {
      type: String,
      default() {
        return "{}";
      }
    },
  },
  activated() {
    //获取language，用于改变表格的展开、收起文字  zh_CN/zh_TW/en_US
    let user = getCurrentUser();
    if (user) {
      this.language = user.language;
    }
    this.tableData = this.getJsonArr(this.stringData);
  },
  created: function () {
    //获取language，用于改变表格的展开、收起文字  zh_CN/zh_TW/en_US
    let user = getCurrentUser();
    if (user) {
      this.language = user.language;
    }
    this.tableData = this.getJsonArr(this.stringData);
  },
  mounted() {
    //获取language，用于改变表格的展开、收起文字  zh_CN/zh_TW/en_US
    let user = getCurrentUser();
    if (user) {
      this.language = user.language;
    }
    this.tableData = this.getJsonArr(this.stringData);
  },
  computed: {
    showSlotCompnent() {
      return this.isRequest || this.isResponse;
    }
  },
  watch: {
    stringData() {
      this.tableData = this.getJsonArr(this.stringData);
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
        classname += ' hide-expand'
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
    getCollapseOption() {
      if (this.expandAllRow) {
        return this.$t('api_test.definition.document.close');
      } else {
        return this.$t('api_test.definition.document.open');
      }
    },
    expandAllRows() {
      this.expandAllRow = !this.expandAllRow;
    },
    getJsonArr(jsonString) {
      let returnJsonArr = [];
      if (jsonString === '无' || jsonString === null) {
        return returnJsonArr;
      }
      try {
        let jsonArr = JSON.parse(jsonString);
        //遍历，把必填项空的数据去掉
        for (var index = 0; index < jsonArr.length; index++) {
          var item = jsonArr[index];
          if (item.name !== "" && item.name !== null) {
            if (item.required) {
              item.isRequired = "true";
            } else {
              item.isRequired = "false";
            }
            item.id = getUUID();
            if (this.rowHasAdvancedSetting(item)) {
              item.hasAdvancedSetting = true;
            } else {
              item.hasAdvancedSetting = false;
            }
            returnJsonArr.push(item);
          }
        }
      } catch (e) {
        returnJsonArr = [];
      }
      return returnJsonArr;
    },
    changeActive() {
      this.active = !this.active;
    },
  },
};
</script>

<style scoped>

.apiInfoRow {
  margin: 10px 10px;
}

.apiInfoRow.el-row {
  margin: 10px 10px;
}

.simpleFontClass {
  font-weight: normal;
  font-size: 14px;
  margin-left: 10px;
}

.blackFontClass {
  font-weight: bold;
  font-size: 14px;
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

/*通过样式隐藏图标*/
.document-table /deep/ .hide-expand .el-table__expand-column .cell {
  visibility: hidden;
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

.right-button {
  float: right;
}

.showDataDiv {
  background-color: #F5F7F9;
  margin: 10px 10px;
  max-height: 300px;
  overflow: auto;
}

</style>
