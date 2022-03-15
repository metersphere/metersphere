<template>
  <div style="margin-bottom: 50px;border-bottom-width: 2px" ref="baseDiv">
    <div style="font-size: 18px; margin-left: 15px">
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
      <slot name="headerRight"></slot>
    </div>

    <!--api基础信息-->
    <el-row class="apiInfoRow">
      <div class="simpleFontClass">
        <el-tag size="medium"
                :style="{'background-color': getColor(true,apiInfo.method), border: getColor(true,apiInfo.method),borderRadius:'0px', marginRight:'20px',color:'white'}">
          {{ apiInfo.method }}
        </el-tag>
        {{ apiInfo.uri }}
      </div>
      <div class="attacInfo">
        <el-row :gutter="10">
          <el-col :span="6"> {{ $t('test_track.module.module') }} : {{ apiInfo.modules }}</el-col>
          <el-col :span="6">{{ $t('commons.tag') }} : {{ apiInfo.tags }}</el-col>
          <el-col :span="6">{{ $t('api_test.definition.request.responsible') }} : {{ apiInfo.responsibler }}</el-col>
          <el-col :span="6">{{ $t('commons.create_user') }} : {{ apiInfo.createUser }}</el-col>
        </el-row>
        <el-row style="margin-top: 10px">
          {{ $t('commons.description') }} : {{ apiInfo.desc }}
        </el-row>
      </div>
    </el-row>

    <http-information v-if="apiInfo.protocol==='HTTP'" :api-info="apiInfo"/>
    <tcp-information v-else-if="apiInfo.protocol==='TCP'" :api="apiObject"/>
    <sql-api-information v-else-if="apiInfo.protocol==='SQL'" :api="apiObject"/>
    <dubbo-information v-else-if="apiInfo.protocol==='DUBBO'" :api="apiObject"/>
    <el-divider></el-divider>
  </div>
</template>

<script>
import MsCodeEdit from "@/business/components/common/components/MsCodeEdit";
import ApiStatus from "@/business/components/api/definition/components/list/ApiStatus";
import MsJsonCodeEdit from "@/business/components/common/json-schema/JsonSchemaEditor";
import Api from "@/business/components/api/router";
import {generateApiDocumentShareInfo} from "@/network/share";
import ApiInfoCollapse from "@/business/components/api/definition/components/document/components/ApiInfoCollapse";
import ApiRequestInfo from "@/business/components/api/definition/components/document/components/ApiRequestInfo";
import ApiResponseInfo from "@/business/components/api/definition/components/document/components/ApiResponseInfo";
import HttpInformation
  from "@/business/components/api/definition/components/document/components/protocal/HttpInformation";
import TcpInformation
  from "@/business/components/api/definition/components/document/components/protocal/TcpInformation";
import DubboInformation
  from "@/business/components/api/definition/components/document/components/protocal/DubboInformation";
import SqlApiInformation
  from "@/business/components/api/definition/components/document/components/protocal/SqlApiInformation";
import {API_METHOD_COLOUR} from "@/business/components/api/definition/model/JsonData";
const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const apiDocumentBatchShare = (requireComponent != null && requireComponent.keys().length) > 0 ? requireComponent("./share/ApiDocumentBatchShare.vue") : {};

export default {
  name: "ApiInformation",
  components: {
    Api,
    MsJsonCodeEdit,
    ApiStatus, MsCodeEdit, ApiInfoCollapse, ApiRequestInfo, ApiResponseInfo,
    HttpInformation,TcpInformation,DubboInformation,SqlApiInformation,
    "ApiDocumentBatchShare": apiDocumentBatchShare.default
  },
  data() {
    return {
      apiObject:{},
      shareUrl: "",
      apiActiveInfoNames: ["info"],
      batchShareUrl: "",
      apiStepIndex: 0,
      apiInfoArray: [],
      modes: ['text', 'json', 'xml', 'html'],
      formParamTypes: ['form-data', 'x-www-from-urlencoded', 'BINARY'],
      methodColorMap: new Map(API_METHOD_COLOUR),
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
        restParams: "无",
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

      maxCompnentSize: 5, //浏览器最多渲染的api信息体数量
      apiShowArray: [],//浏览器要渲染的api信息集合
      needAsyncSelect: false, //是否需要异步查询api详细数据做展现。只有本次要展示的数据总量大于maxCompnentSize时为true
      currentApiIndexInApiShowArray: 0,//当前主要展示的api信息在apiShowArray的索引
      clickStepFlag: false,
    };
  },
  props: {
    projectId: String,
    apiInfo: Object,
    api:Object,
  },
  activated() {
  },
  created: function () {
    this.apiObject = JSON.parse(JSON.stringify(this.api));
    if (requireComponent != null && JSON.stringify(apiDocumentBatchShare) != '{}') {
      this.showXpackCompnent = true;
    }
  },
  mounted() {
  },
  computed: {},
  watch: {},
  methods: {
    getId() {
      return this.apiInfo.id;
    },
    getHeight() {
      return this.$refs.baseDiv.offsetHeight;
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
  },
};
</script>

<style scoped>

.apiStatusTag {
  margin: 10px 10px;
}
.simpleFontClass {
  font-weight: normal;
  font-size: 14px;
  margin-left: 10px;
}

.apiInfoRow {
  margin: 10px 10px;
}

.apiInfoRow.el-row {
  margin: 10px 10px;
}
.attacInfo {
  font-size: 12px;
  color: #A0A0A0;
  margin: 10px;
}
/*!**/
/*步骤条中，已经完成后的节点样式和里面a标签的样式*/
/**!*/
/*/deep/ .el-step {*/
/*  flex-basis: 40px !important;*/
/*}*/

/*/deep/ .el-step__head.is-finish {*/
/*  color: #C0C4CC;*/
/*  border-color: #C0C4CC;*/
/*}*/

/*/deep/ .el-step__title.is-finish /deep/ .el-link.el-link--default {*/
/*  color: #C0C4CC;*/
/*}*/

/*!**/
/*步骤条中，当前节点样式和当前a标签的样式*/
/**!*/
/*/deep/ .el-step__head {*/
/*  width: 20px;*/
/*}*/

/*/deep/ .el-step__head.is-process {*/
/*  color: #783887;*/
/*  border-color: #783887;*/
/*  width: 20px;*/
/*}*/

/*/deep/ .el-step__title.is-process .el-link.el-link--default.is-underline {*/
/*  color: #783887;*/
/*}*/

/*/deep/ .el-link--inner {*/
/*  font-size: 12px;*/
/*}*/

/*/deep/ .el-step__icon-inner {*/
/*  font-size: 12px;*/
/*}*/

/*/deep/ .el-step.is-vertical .el-step__line {*/
/*  left: 9px;*/
/*}*/

/*/deep/ .el-step__icon {*/
/*  width: 20px;*/
/*  height: 20px;*/
/*}*/


</style>
