<template>
  <div>
    <el-row class="apiInfoRow">
      <div>
        <el-row v-if="formParamTypes.includes(apiInfo.responseBodyParamType)">
          <div style="float: right">
            <api-params-config
              v-if="apiParamsConfigFields"
              @refresh="refreshApiParamsField"
              :storage-key="apiParamStorageKey"
              :api-params-config-fields="apiParamsConfigFields" />
          </div>
        </el-row>
        <el-table
          border
          v-if="formParamTypes.includes(apiInfo.responseBodyParamType)"
          :data="getJsonArr(apiInfo.responseBodyFormData)"
          class="test-content document-table">
          <el-table-column
            v-for="item in tableColumnArr"
            :key="item.id"
            :prop="item.prop"
            :label="item.label"
            show-overflow-tooltip />
        </el-table>
        <div v-else-if="apiInfo.responseBodyParamType == 'JSON-SCHEMA'" style="margin-left: 10px">
          <ms-json-code-edit
            :json-schema-disable="true"
            :show-preview="false"
            :body="apiInfo.jsonSchemaResponseBody"
            ref="jsonCodeEdit" />
        </div>
        <div v-else-if="formatRowDataToJsonSchema(apiInfo, 'response')" style="margin-left: 10px">
          <ms-json-code-edit
            :json-schema-disable="true"
            :show-preview="false"
            :body="apiInfo.responseJsonSchema"
            ref="jsonCodeEdit" />
        </div>
        <div v-else class="showDataDiv">
          <br />
          <p
            style="margin: 0px 20px"
            v-html="formatRowData(apiInfo.responseBodyParamType, apiInfo.responseBodyStructureData)"></p>
          <br />
        </div>
      </div>
    </el-row>
  </div>
</template>

<script>
import MsJsonCodeEdit from '@/business/commons/json-schema/JsonSchemaEditor';
import { getApiParamsConfigFields, getShowFields } from 'metersphere-frontend/src/utils/custom_field';
import ApiParamsConfig from '@/business/definition/components/request/components/ApiParamsConfig';

export default {
  name: 'ApiResponseInfo',
  components: { MsJsonCodeEdit, ApiParamsConfig },
  data() {
    return {
      active: true,
      formParamTypes: ['form-data', 'x-www-from-urlencoded', 'BINARY'],
      apiParamsConfigFields: getApiParamsConfigFields(this),
      apiParamStorageKey: 'API_RESPONSE_PARAMS_SHOW_FIELD',
      tableColumnArr: [],
    };
  },
  props: {
    apiInfo: Object,
  },
  activated() {
    this.initTableColumn();
  },
  created: function () {
    this.initTableColumn();
  },
  mounted() {
    this.initTableColumn();
  },
  computed: {},
  watch: {},
  methods: {
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
          returnJsonArr.push(item);
        }
      }

      returnJsonArr.forEach((item) => {
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

      return returnJsonArr;
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
    formatRowDataToJsonSchema(api, jsonType) {
      if (jsonType === 'request' && api.requestBodyStructureData) {
        try {
          let bodyStructData = JSON.parse(api.requestBodyStructureData);
          api.requestJsonSchema = { raw: bodyStructData };
          return true;
        } catch (e) {
          return false;
        }
      } else if (jsonType === 'response' && api.responseBodyStructureData) {
        try {
          JSON.parse(api.responseBodyStructureData);
          api.responseJsonSchema = { raw: api.responseBodyStructureData };
          return true;
        } catch (e) {
          return false;
        }
      } else {
        return false;
      }
    },
  },
};
</script>

<style scoped>
.apiInfoRow {
  margin: 10px 10px;
}

.showDataDiv {
  background-color: #f5f7f9;
  margin: 10px 10px;
  max-height: 300px;
  overflow: auto;
}
</style>

<style scoped>
.apiInfoRow {
  margin: 10px 10px;
}
</style>
