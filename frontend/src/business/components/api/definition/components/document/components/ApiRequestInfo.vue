<template>
  <div>
    <el-row class="apiInfoRow">
      <div>
        <el-table border v-if="formParamTypes.includes(apiInfo.requestBodyParamType)"
                  :data="getJsonArr(apiInfo.requestBodyFormData)"
                  class="test-content document-table">
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
        </el-table>
        <div v-else-if="apiInfo.requestBodyParamType == 'JSON-SCHEMA'" style="margin-left: 10px">
          <ms-json-code-edit :show-preview="false" :json-schema-disable="true" :body="apiInfo.jsonSchemaBody"
                             ref="jsonCodeEdit"/>
        </div>
        <div v-else-if="formatRowDataToJsonSchema(apiInfo,'request') " style="margin-left: 10px">
          <ms-json-code-edit :show-preview="false" :json-schema-disable="true" :body="apiInfo.requestJsonSchema"
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

import {formatJson} from "@/common/js/format-utils";
import MsJsonCodeEdit from "@/business/components/common/json-schema/JsonSchemaEditor";

export default {
  name: "ApiRequestInfo",
  components: {MsJsonCodeEdit},
  data() {
    return {
      active: true,
      formParamTypes: ['form-data', 'x-www-from-urlencoded', 'BINARY'],
    };
  },
  props: {
    apiInfo:Object,
  },
  activated() {
  },
  created: function () {
  },
  mounted() {
  },
  computed: {
  },
  watch: {},
  methods: {
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
      if (previewData !== null && previewData !== '') {
        let showDataObj = {};
        for (var key in previewData) {
          let value = previewData[key];
          if (typeof (value) == 'string') {
            if (value.indexOf("@") >= 0) {
              value = this.showPreview(value);
            }
          }
          showDataObj[key] = value;
        }
        showDataObj = JSON.stringify(showDataObj);
        previewData = formatJson(showDataObj);
      }
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
</style>
