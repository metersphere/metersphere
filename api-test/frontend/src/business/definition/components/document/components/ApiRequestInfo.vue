<template>
  <div>
    <el-row class="apiInfoRow">
      <div>
        <div v-if="formParamTypes.includes(apiInfo.requestBodyParamType)">
          <el-row>
            <div style="float: right">
              <api-params-config
                v-if="apiParamsConfigFields"
                @refresh="refreshApiParamsField"
                :api-params-config-fields="apiParamsConfigFields" />
            </div>
          </el-row>
          <el-table
            border
            :show-header="true"
            row-key="id"
            :row-class-name="getRowClassName"
            :data="tableData"
            :class="getTableClass()"
            ref="expandTable">
            <el-table-column
              v-for="item in tableColumnArr"
              :key="item.id"
              :prop="item.prop"
              :label="item.label"
              show-overflow-tooltip />
          </el-table>
        </div>

        <div
          v-else-if="apiInfo.requestBodyParamType === 'JSON-SCHEMA' || apiInfo.requestBodyParamType === 'JSON'"
          style="margin-left: 10px">
          <json-schema-show
            :show-preview="false"
            :json-schema-disable="true"
            :body="apiInfo.jsonSchemaBody"
            ref="jsonCodeEdit" />
        </div>
        <div v-else class="showDataDiv">
          <br />
          <p
            style="margin: 0px 20px"
            v-html="formatRowData(apiInfo.requestBodyParamType, apiInfo.requestBodyStructureData)"></p>
          <br />
        </div>
      </div>
    </el-row>

    <el-row class="apiInfoRow">
      <div class="blackFontClass">
        {{ $t('api_test.definition.document.example_presentation') }}
      </div>
      <div class="showDataDiv">
        <br />
        <p style="margin: 0px 20px" v-html="genPreviewData(apiInfo.requestPreviewData)"></p>
        <br />
      </div>
    </el-row>
  </div>
</template>

<script>
import JsonSchemaShow from '@/business/definition/components/document/components/JsonSchema/JsonSchemaShow';
import tableAdvancedSetting from '@/business/definition/components/document/components/plugin/TableAdvancedSetting';
import { getCurrentUser } from 'metersphere-frontend/src/utils/token';
import { getUUID } from 'metersphere-frontend/src/utils';
import { getApiParamsConfigFields, getShowFields } from 'metersphere-frontend/src/utils/custom_field';
import ApiParamsConfig from '@/business/definition/components/request/components/ApiParamsConfig';

export default {
  name: 'ApiRequestInfo',
  components: { JsonSchemaShow, tableAdvancedSetting, ApiParamsConfig },
  data() {
    return {
      tableData: [],
      language: 'zh_CN',
      tableExpandButtonId: 'docTableExpandBtn' + getUUID(),
      active: true,
      expandAllRow: false,
      apiParamStorageKey: 'API_PARAMS_SHOW_FIELD',
      expandTitle: this.$t('commons.expand_all'),
      apiParamsConfigFields: getApiParamsConfigFields(this),
      formParamTypes: ['form-data', 'x-www-from-urlencoded', 'BINARY'],
      tableColumnArr: [],
    };
  },
  props: {
    apiInfo: Object,
  },
  activated() {
    if (this.apiInfo && this.apiInfo.requestBodyFormData) {
      this.tableData = this.getJsonArr(this.apiInfo.requestBodyFormData);
      this.formatTableData();
    }
    //获取language，用于改变表格的展开、收起文字  zh_CN/zh_TW/en_US
    let user = getCurrentUser();
    if (user) {
      this.language = user.language;
    }
    this.initTableColumn();
  },
  created: function () {
    if (this.apiInfo && this.apiInfo.requestBodyFormData) {
      this.tableData = this.getJsonArr(this.apiInfo.requestBodyFormData);
      this.formatTableData();
    }
    //获取language，用于改变表格的展开、收起文字  zh_CN/zh_TW/en_US
    let user = getCurrentUser();
    if (user) {
      this.language = user.language;
    }
    this.initTableColumn();
  },
  mounted() {
    if (this.apiInfo && this.apiInfo.requestBodyFormData) {
      this.tableData = this.getJsonArr(this.apiInfo.requestBodyFormData);
      this.formatTableData();
    }
    //获取language，用于改变表格的展开、收起文字  zh_CN/zh_TW/en_US
    let user = getCurrentUser();
    if (user) {
      this.language = user.language;
    }
    this.initTableColumn();
  },
  computed: {},
  watch: {
    'apiInfo.requestBodyFormData': {
      handler(v) {
        this.tableData = this.getJsonArr(this.apiInfo.requestBodyFormData);
        this.formatTableData();
      },
      deep: true,
    },
    expandAllRow() {
      if (this.$refs.expandTable) {
        let expand = this.expandAllRow;
        if (this.tableData) {
          this.$nextTick(() => {
            this.tableData.forEach((i) => {
              if (i.hasAdvancedSetting) {
                this.$refs.expandTable.toggleRowExpansion(i, expand);
              }
            });
            this.$refs.expandTable.doLayout();
          });
        }
      }
      this.expandTitle = this.expandAllRow ? this.$t('commons.close_all') : this.$t('commons.expand_all');
      let tableHeaderDom = document.getElementById(this.tableExpandButtonId);
      tableHeaderDom.innerText = this.expandTitle;
    },
  },
  methods: {
    formatTableData() {
      if (this.tableData) {
        this.tableData.forEach((item) => {
          if (item.urlEncode !== null && item.urlEncode !== undefined) {
            if (item.urlEncode === true) {
              item.urlEncode = this.$t('commons.yes');
            } else {
              item.urlEncode = this.$t('commons.no');
            }
          }
          if (item.enable !== null && item.enable !== undefined) {
            if (item.enable === true) {
              item.enable = this.$t('commons.yes');
            } else {
              item.enable = this.$t('commons.no');
            }
          }
        });
      }
    },
    refreshApiParamsField() {
      this.initTableColumn();
      this.reloadedApiVariable = false;
      this.$nextTick(() => {
        this.reloadedApiVariable = true;
      });
    },
    initTableColumn() {
      this.tableColumnArr = [
        { id: 1, prop: 'name', label: this.$t('api_definition.document.name') },
        { id: 2, prop: 'contentType', label: this.$t('api_definition.document.type') },
        {
          id: 3,
          prop: 'enable',
          label: this.$t('api_definition.document.is_required'),
        },
        {
          id: 4,
          prop: 'value',
          label: this.$t('api_definition.document.value'),
        },
      ];
      if (this.formParamTypes.includes(this.apiInfo.requestBodyParamType)) {
        let apiParamConfigArr = getShowFields(this.apiParamStorageKey);
        if (apiParamConfigArr) {
          apiParamConfigArr.forEach((item) => {
            let tableColumn = {};
            if (item === 'MIX_LENGTH') {
              tableColumn.id = 5;
              tableColumn.prop = 'min';
              tableColumn.label = this.$t('schema.minLength');
            } else if (item === 'MAX_LENGTH') {
              tableColumn.id = 6;
              tableColumn.prop = 'max';
              tableColumn.label = this.$t('schema.maxLength');
            } else if (item === 'ENCODE') {
              tableColumn.id = 7;
              tableColumn.prop = 'urlEncode';
              tableColumn.label = this.$t('commons.encode');
            } else if (item === 'DESCRIPTION') {
              tableColumn.id = 8;
              tableColumn.prop = 'description';
              tableColumn.label = this.$t('commons.description');
            } else {
              tableColumn = null;
            }
            if (tableColumn) {
              this.tableColumnArr.push(tableColumn);
            }
          });
        }
      }
    },
    getRowClassName({ row, rowIndex }) {
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
      if (this.language === 'zh_TW') {
        return 'test-content document-table tw-table';
      } else if (this.language === 'en_US') {
        return 'test-content document-table us-table';
      } else {
        return 'test-content document-table cn-table';
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
      var ret = ''; //你想在页面展示的值
      if (cellValue) {
        ret = '是'; //根据自己的需求设定
      } else {
        ret = '否';
      }
      return ret;
    },
    formatRowData(dataType, data) {
      var returnData = data;
      if (data) {
        returnData = '<xmp>' + returnData + '</xmp>';
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
        if (item.name !== '' && item.name !== null) {
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
      if (jsonType === 'request' && api.requestBodyStructureData) {
        try {
          let bodyStructData = JSON.parse(api.requestBodyStructureData);
          api.requestJsonSchema = { raw: bodyStructData };
          return true;
        } catch (e) {
          return false;
        }
      } else if (jsonType === 'response' && api.requestBodyStructureData) {
        try {
          JSON.parse(api.requestBodyStructureData);
          api.responseJsonSchema = { raw: api.requestBodyStructureData };
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
  background-color: #f5f7f9;
  margin: 10px 10px;
  max-height: 300px;
  overflow: auto;
}

.document-table {
  margin: 10px 0px 10px 10px;
  width: auto;
}

.document-table :deep(.el-table__row) {
  font-size: 12px;
  font-weight: initial;
}

.document-table :deep(.has-gutter) {
  font-size: 12px;
  color: #404040;
}

.document-table :deep(td) {
  border-right: 0px solid #ebeef5;
}

/*通过样式隐藏图标*/
.document-table :deep(.hide-expand .el-table__expand-column .cell) {
  visibility: hidden;
}

/*修改展开按钮时不旋转*/
.document-table :deep(.el-table__expand-icon) {
  -webkit-transform: rotate(0deg);
  transform: rotate(0deg);
}

.document-table :deep(.el-table__expanded-cell) {
  padding: 5px;
}

.document-table :deep(.el-icon-arrow-right) {
  position: unset;
}

.document-table :deep(th) {
  background-color: #fafafa;
  border-right: 0px solid #ebeef5;
}

/*展开按钮未点击的样式是加号带边框*/
.cn-table :deep(.el-table__expand-icon .el-icon-arrow-right:before) {
  position: unset;
  content: '展开';
  padding: 2px;
}

/*展开按钮点击后的样式是减号带边框*/
.cn-table :deep(.el-table__expand-icon--expanded .el-icon-arrow-right:before) {
  position: unset;
  content: '收起';
}

.tw-table :deep(.el-table__expand-icon .el-icon-arrow-right:before) {
  position: unset;
  content: '展開';
  padding: 2px;
}

.tw-table :deep(.el-table__expand-icon--expanded .el-icon-arrow-right:before) {
  position: unset;
  content: '收起';
}

.us-table :deep(.el-table__expand-icon .el-icon-arrow-right:before) {
  position: unset;
  content: 'Open';
  padding: 2px;
}

.us-table :deep(.el-table__expand-icon--expanded .el-icon-arrow-right:before) {
  position: unset;
  content: 'Close';
  padding: 2px;
}
</style>
