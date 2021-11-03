<template>
  <div ref="baseDiv">
    <div style="font-size: 17px">
      <el-popover
        v-if="projectId"
        placement="right"
        width="260"
        @show="shareApiDocument('false')">
        <p>{{ shareUrl }}</p>
        <div style="text-align: right; margin: 0">
          <el-button type="primary" size="mini"
                     v-clipboard:copy="shareUrl">{{ $t("commons.copy") }}
          </el-button>
        </div>
        <i class="el-icon-share" slot="reference" style="margin-right: 10px;cursor: pointer"></i>
      </el-popover>
      {{ apiInfo.name }}
      <span class="apiStatusTag">
              <api-status :value="apiInfo.status"/>
            </span>
    </div>
    <!--api请求信息-->
    <el-row class="apiInfoRow">
      <div class="tip">
        {{ $t('api_test.definition.document.request_info') }}
      </div>
    </el-row>
    <el-row class="apiInfoRow">
      <div class="simpleFontClass">
        <el-tag size="medium"
                :style="{'background-color': getColor(true,apiInfo.method), border: getColor(true,apiInfo.method),borderRadius:'0px', marginRight:'20px',color:'white'}">
          {{ apiInfo.method }}
        </el-tag>
        {{ apiInfo.uri }}
      </div>
    </el-row>
    <!--api请求头-->
    <el-row class="apiInfoRow">
      <div class="blackFontClass">
        {{ $t('api_test.definition.document.request_head') }}：
        <div v-if="getJsonArr(apiInfo.requestHead).length==0">
          <div class="simpleFontClass" style="margin-top: 10px">
            {{ $t('api_test.definition.document.data_set.none') }}
          </div>
        </div>
        <div v-else>
          <el-table border :show-header="false"
                    :data="getJsonArr(apiInfo.requestHead)" class="test-content document-table">
            <el-table-column prop="name"
                             :label="$t('api_test.definition.document.table_coloum.name')"
                             show-overflow-tooltip/>
            <el-table-column prop="value"
                             :label="$t('api_test.definition.document.table_coloum.value')"
                             show-overflow-tooltip/>
          </el-table>
        </div>
      </div>
    </el-row>
    <!--URL参数-->
    <el-row class="apiInfoRow">
      <div class="blackFontClass">
        URL{{ $t('api_test.definition.document.request_param') }}：
        <div v-if="getJsonArr(apiInfo.urlParams).length==0">
          <div class="simpleFontClass" style="margin-top: 10px">
            {{ $t('api_test.definition.document.data_set.none') }}
          </div>
        </div>
        <div v-else>
          <el-table border
                    :data="getJsonArr(apiInfo.urlParams)" class="test-content document-table">
            <el-table-column prop="name"
                             :label="$t('api_test.definition.document.table_coloum.name')"
                             min-width="120px"
                             show-overflow-tooltip/>
            <el-table-column prop="required"
                             :label="$t('api_test.definition.document.table_coloum.is_required')"
                             :formatter="formatBoolean"
                             min-width="80px"
                             show-overflow-tooltip/>
            <el-table-column prop="value"
                             :label="$t('api_test.definition.document.table_coloum.value')"
                             min-width="120px"
                             show-overflow-tooltip/>
            <el-table-column prop="description"
                             :label="$t('api_test.definition.document.table_coloum.desc')"
                             min-width="280px"
                             show-overflow-tooltip/>
          </el-table>
        </div>
      </div>
    </el-row>
    <!--api请求体 以及表格-->
    <el-row class="apiInfoRow">
      <div class="blackFontClass">
        {{ $t('api_test.definition.document.request_body') }}
      </div>
      <div class="smallFontClass">
        {{ $t('api_test.definition.document.table_coloum.type') }}:{{ apiInfo.requestBodyParamType }}
      </div>
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
          <ms-json-code-edit :show-preview="false" :body="apiInfo.jsonSchemaBody" ref="jsonCodeEdit"/>
        </div>
        <div v-else-if="formatRowDataToJsonSchema(apiInfo,'request') " style="margin-left: 10px">
          <ms-json-code-edit :show-preview="false" :body="apiInfo.requestJsonSchema" ref="jsonCodeEdit"/>
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
    <!--范例展示-->
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
    <!--响应信息-->
    <el-row class="apiInfoRow">
      <div class="tip">
        {{ $t('api_test.definition.document.response_info') }}
      </div>
    </el-row>
    <el-row class="apiInfoRow">

    </el-row>
    <!--响应头-->
    <el-row class="apiInfoRow">
      <div class="blackFontClass">
        {{ $t('api_test.definition.document.response_head') }}:
        <el-table border :show-header="false"
                  :data="getJsonArr(apiInfo.responseHead)" class="test-content document-table">
          <el-table-column prop="name"
                           :label="$t('api_test.definition.document.table_coloum.name')"
                           show-overflow-tooltip/>
          <el-table-column prop="value"
                           :label="$t('api_test.definition.document.table_coloum.value')"
                           show-overflow-tooltip/>
        </el-table>
      </div>
    </el-row>
    <!--响应体-->
    <el-row class="apiInfoRow">
      <div class="blackFontClass">
        {{ $t('api_test.definition.document.response_body') }}
      </div>
      <div class="smallFontClass">
        {{ $t('api_test.definition.document.table_coloum.type') }}:{{ apiInfo.responseBodyParamType }}
      </div>
      <div>
        <el-table border v-if="formParamTypes.includes(apiInfo.responseBodyParamType)"
                  :data="getJsonArr(apiInfo.responseBodyFormData)"
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
        <div v-else-if="apiInfo.responseBodyParamType == 'JSON-SCHEMA'" style="margin-left: 10px">
          <ms-json-code-edit :show-preview="false" :body="apiInfo.jsonSchemaResponseBody" ref="jsonCodeEdit"/>
        </div>
        <div v-else-if="formatRowDataToJsonSchema(apiInfo,'response') " style="margin-left: 10px">
          <ms-json-code-edit :show-preview="false" :body="apiInfo.responseJsonSchema" ref="jsonCodeEdit"/>
        </div>
        <div v-else class="showDataDiv">
          <br/>
          <p style="margin: 0px 20px;"
             v-html="formatRowData(apiInfo.responseBodyParamType,apiInfo.responseBodyStrutureData)">
          </p>
          <br/>
        </div>
      </div>
    </el-row>
    <!--响应状态码-->
    <el-row class="apiInfoRow">
      <div class="blackFontClass">
        {{ $t('api_test.definition.document.response_code') }}:
        <el-table border :show-header="false"
                  :data="getJsonArr(apiInfo.responseCode)" class="test-content document-table">
          <el-table-column prop="name"
                           :label="$t('api_test.definition.document.table_coloum.name')"
                           show-overflow-tooltip/>
          <el-table-column prop="value"
                           :label="$t('api_test.definition.document.table_coloum.value')"
                           show-overflow-tooltip/>
        </el-table>
      </div>
    </el-row>
  </div>
</template>

<script>
import {API_METHOD_COLOUR} from "@/business/components/api/definition/model/JsonData";
import MsCodeEdit from "@/business/components/common/components/MsCodeEdit";
import {formatJson,} from "@/common/js/format-utils";
import ApiStatus from "@/business/components/api/definition/components/list/ApiStatus";
import {calculate} from "@/business/components/api/definition/model/ApiTestModel";
import MsJsonCodeEdit from "@/business/components/common/json-schema/JsonSchemaEditor";
import Api from "@/business/components/api/router";
import {generateApiDocumentShareInfo} from "@/network/share";
import Convert from "@/business/components/common/json-schema/convert/convert";

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const apiDocumentBatchShare = (requireComponent != null && requireComponent.keys().length) > 0 ? requireComponent("./share/ApiDocumentBatchShare.vue") : {};

export default {
  name: "ApiInformation",
  components: {
    Api,
    MsJsonCodeEdit,
    ApiStatus, MsCodeEdit,
    "ApiDocumentBatchShare": apiDocumentBatchShare.default
  },
  data() {
    return {
      shareUrl: "",
      batchShareUrl: "",
      apiStepIndex: 0,
      apiInfoArray: [],
      modes: ['text', 'json', 'xml', 'html'],
      formParamTypes: ['form-data', 'x-www-from-urlencoded', 'BINARY'],
      mockVariableFuncs: [],
      apiSearch: {
        name: "",
        type: "ALL",
        orderCondition: "createTimeDesc",
      },
      apiInfoBaseObj: {
        selectedFlag: false,
        method: "无",
        uri: "无",
        name: "无",
        id: "",
        requestHead: "无",
        urlParams: "无",
        requestBodyParamType: "无",
        requestBodyFormData: '[]',
        requestBodyStrutureData: "",
        sharePopoverVisible: false,
        jsonSchemaBody: {},
        JsonSchemaResponseBody: {},
        responseHead: "无",
        responseBody: "",
        responseBodyParamType: "无",
        responseBodyFormData: "无",
        responseBodyStrutureData: "无",
        responseCode: "无",
      },
      methodColorMap: new Map(API_METHOD_COLOUR),
      maxCompnentSize: 5, //浏览器最多渲染的api信息体数量
      apiShowArray: [],//浏览器要渲染的api信息集合
      needAsyncSelect: false, //是否需要异步查询api详细数据做展现。只有本次要展示的数据总量大于maxCompnentSize时为true
      currentApiIndexInApiShowArray: 0,//当前主要展示的api信息在apiShowArray的索引
      clickStepFlag: false,
    };
  },
  props: {
    projectId: String,
    apiInfo:Object
  },
  activated() {
  },
  created: function () {
    if (requireComponent != null && JSON.stringify(apiDocumentBatchShare) != '{}') {
      this.showXpackCompnent = true;
    }
  },
  mounted() {
  },
  computed: {},
  watch: {
  },
  methods: {
    getId(){
      return this.apiInfo.id;
    },
    getHeight(){
      return this.$refs.baseDiv.offsetHeight;
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
    formatRowData(dataType, data) {
      var returnData = data;
      if (data) {
        returnData = "<xmp>" + returnData + "</xmp>";
      }
      return returnData;
    },
    shareApiDocument(isBatchShare) {
      this.shareUrl = "";
      this.batchShareUrl = "";
      let shareIdArr = [];
      let shareType = "Single";
      if (isBatchShare == 'true') {
        this.apiInfoArray.forEach(f => {
          if (!f.id) {
            return;
          }
          shareIdArr.push(f.id);
        });
        shareType = "Batch";
      } else {
        // shareIdArr.push(this.apiInfoArray[this.apiStepIndex].id);
        shareIdArr.push(this.apiInfo.id);
      }
      let genShareInfoParam = {};
      genShareInfoParam.shareApiIdList = shareIdArr;
      genShareInfoParam.shareType = shareType;

      generateApiDocumentShareInfo(genShareInfoParam, (data) => {
        let thisHost = window.location.host;
        if (shareType == "Batch") {
          this.batchShareUrl = thisHost + "/document" + data.shareUrl;
        } else {
          this.shareUrl = thisHost + "/document" + data.shareUrl;
        }
      });
    },
    getColor(enable, method) {
      return this.methodColorMap.get(method);
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
    getJsonArr(jsonString) {
      let returnJsonArr = [];
      if (jsonString == '无' || jsonString == null) {
        return returnJsonArr;
      }

      let jsonArr = JSON.parse(jsonString);
      //遍历，把必填项空的数据去掉
      for (var index = 0; index < jsonArr.length; index++) {
        var item = jsonArr[index];
        if (item.name != "" && item.name != null) {
          returnJsonArr.push(item);
        }
      }
      return returnJsonArr;
    },
    //构建预览数据
    genPreviewData(previewData) {
      if (previewData != null && previewData != '') {
        let showDataObj = {};
        for (var key in previewData) {
          // showDataObj.set(key,previewData[key]);
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
    showPreview(itemValue) {
      // 找到变量本身
      if (!itemValue) {
        return;
      }
      let index = itemValue.indexOf("|");
      if (index > -1) {
        itemValue = itemValue.substring(0, index).trim();
      }

      this.mockVariableFuncs.forEach(f => {
        if (!f.name) {
          return;
        }
        itemValue += "|" + f.name;
        if (f.params) {
          itemValue += ":" + f.params.map(p => p.value).join(",");
        }
      });

      itemValue = calculate(itemValue);
      return itemValue;
    },
  },
};
</script>

<style scoped>
.simpleFontClass {
  font-weight: normal;
  font-size: 14px;
  margin-left: 10px;
}

.blackFontClass {
  font-weight: bold;
  font-size: 14px;
}

.smallFontClass {
  font-size: 13px;
  margin: 20px 5px;
}

.apiInfoRow {
  margin: 10px 10px;
}

.apiInfoRow.el-row {
  margin: 10px 10px;
}

.apiStatusTag {
  margin: 10px 10px;
}

.showDataDiv {
  background-color: #F5F7F9;
  margin: 10px 10px;
  max-height: 300px;
  overflow: auto;
}

/*
步骤条中，已经完成后的节点样式和里面a标签的样式
*/

/deep/ .el-step {
  flex-basis: 40px !important;
}

/deep/ .el-step__head.is-finish {
  color: #C0C4CC;
  border-color: #C0C4CC;
}

/deep/ .el-step__title.is-finish /deep/ .el-link.el-link--default {
  color: #C0C4CC;
}

/*
步骤条中，当前节点样式和当前a标签的样式
*/
/deep/ .el-step__head {
  width: 20px;
}

/deep/ .el-step__head.is-process {
  color: #783887;
  border-color: #783887;
  width: 20px;
}

/deep/ .el-step__title.is-process .el-link.el-link--default.is-underline {
  color: #783887;
}

/deep/ .el-link--inner {
  font-size: 12px;
}

/deep/ .el-step__icon-inner {
  font-size: 12px;
}

/deep/ .el-step.is-vertical .el-step__line {
  left: 9px;
}


/deep/ .el-step__icon {
  width: 20px;
  height: 20px;
}

.document-table {
  margin: 10px 10px;
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

.document-table /deep/ th {
  background-color: #FAFAFA;
  border-right: 0px solid #EBEEF5
}

.el-divider--horizontal {
  margin: 12px 0;
}
</style>
