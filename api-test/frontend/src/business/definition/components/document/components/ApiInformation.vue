<template>
  <div style="border-bottom-width: 2px" ref="baseDiv">
    <div style="font-size: 17px">
      <el-popover v-if="projectId" placement="right" width="260" @show="shareApiDocument('false')">
        <p>{{ shareUrl }}</p>
        <div style="text-align: right; margin: 0">
          <el-button type="primary" size="mini" v-clipboard:copy="shareUrl">{{ $t('commons.copy') }}</el-button>
        </div>
        <i class="el-icon-share" slot="reference" style="margin-right: 10px; cursor: pointer"></i>
      </el-popover>
      <el-tooltip :content="$t('commons.export')" placement="top" v-if="!isTemplate">
        <i
          class="el-icon-download"
          @click="handleExportHtml()"
          style="margin-right: 5px; cursor: pointer; font-size: 20px" />
      </el-tooltip>

      {{ apiInfo.name }}
      <span class="apiStatusTag">
        <api-status :value="apiInfo.status" />
      </span>
    </div>
    <!--api请求信息-->
    <el-row class="apiInfoRow">
      <div class="simpleFontClass">
        <el-tag
          size="medium"
          :style="{
            'background-color': getColor(true, apiInfo.method),
            border: getColor(true, apiInfo.method),
            borderRadius: '0px',
            marginRight: '20px',
            color: 'white',
          }">
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
        <el-row style="margin-top: 10px"> {{ $t('commons.description') }} : {{ apiInfo.desc }}</el-row>
      </div>
    </el-row>
    <!--api请求头-->
    <api-info-collapse
      :table-can-expand="false"
      v-if="isArrayHasData(apiInfo.requestHead)"
      table-column-type="nameAndValue"
      :title="$t('api_test.definition.document.request_head')"
      :string-data="apiInfo.requestHead" />
    <!--QUERY参数-->
    <api-info-collapse
      v-if="isArrayHasData(apiInfo.urlParams)"
      table-column-type="simple"
      table-type="query"
      :title="'QUERY' + $t('api_test.definition.document.request_param')"
      :string-data="apiInfo.urlParams" />
    <!--REST参数-->
    <api-info-collapse
      v-if="isArrayHasData(apiInfo.restParams)"
      table-column-type="simple"
      table-type="rest"
      :title="'REST' + $t('api_test.definition.document.request_param')"
      :string-data="apiInfo.restParams" />
    <!--api请求体 以及表格-->
    <api-info-collapse
      v-if="hasRequestParams(apiInfo)"
      :is-request="true"
      :remarks="apiInfo.requestBodyParamType"
      :title="$t('api_test.definition.document.request_body')">
      <api-request-info slot="request" :api-info="apiInfo"></api-request-info>
    </api-info-collapse>

    <!--响应头-->
    <api-info-collapse
      :table-can-expand="false"
      v-if="isArrayHasData(apiInfo.responseHead)"
      table-column-type="nameAndValue"
      :title="$t('api_test.definition.document.response_head')"
      :string-data="apiInfo.responseHead" />
    <!--响应体-->
    <api-info-collapse
      v-if="hasResponseBody(apiInfo)"
      :is-response="true"
      :remarks="apiInfo.responseBodyParamType"
      :title="$t('api_test.definition.document.response_body')">
      <api-response-info slot="response" :api-info="apiInfo"></api-response-info>
    </api-info-collapse>

    <!--响应状态码-->
    <api-info-collapse
      :table-can-expand="false"
      v-if="hasResponseCode(apiInfo.responseCode)"
      :is-text="true"
      :string-data="getName(apiInfo.responseCode)"
      :title="$t('api_test.definition.document.response_code')" />
    <!--  备注  -->
    <api-remark-show :data="apiInfo.remark"></api-remark-show>
    <el-divider></el-divider>
  </div>
</template>

<script>
import { API_METHOD_COLOUR } from '@/business/definition/model/JsonData';
import MsCodeEdit from 'metersphere-frontend/src/components/MsCodeEdit';
import ApiStatus from '@/business/definition/components/list/ApiStatus';
import MsJsonCodeEdit from '@/business/commons/json-schema/JsonSchemaEditor';
import ApiRemarkShow from '@/business/definition/components/document/components/ApiRemarkShow';
import { documentShareUrl, generateApiDocumentShareInfo } from '@/api/share';
import ApiInfoCollapse from '@/business/definition/components/document/components/ApiInfoCollapse';
import ApiRequestInfo from '@/business/definition/components/document/components/ApiRequestInfo';
import ApiResponseInfo from '@/business/definition/components/document/components/ApiResponseInfo';

export default {
  name: 'ApiInformation',
  components: {
    MsJsonCodeEdit,
    ApiStatus,
    MsCodeEdit,
    ApiInfoCollapse,
    ApiRequestInfo,
    ApiResponseInfo,
    ApiRemarkShow,
  },
  data() {
    return {
      shareUrl: '',
      apiActiveInfoNames: ['info'],
      batchShareUrl: '',
      apiStepIndex: 0,
      apiInfoArray: [],
      modes: ['text', 'json', 'xml', 'html'],
      formParamTypes: ['form-data', 'x-www-from-urlencoded', 'BINARY'],
      mockVariableFuncs: [],
      apiSearch: {
        name: '',
        type: 'ALL',
        orderCondition: 'createTimeDesc',
      },
      apiInfoBaseObj: {
        selectedFlag: false,
        method: '无',
        uri: '无',
        name: '无',
        id: '',
        requestHead: '无',
        urlParams: '无',
        restParams: '无',
        requestBodyParamType: '无',
        requestBodyFormData: '[]',
        sharePopoverVisible: false,
        jsonSchemaBody: {},
        JsonSchemaResponseBody: {},
        responseHead: '无',
        responseBody: '',
        responseBodyParamType: '无',
        responseBodyFormData: '无',
        requestBodyStructureData: '无',
        responseCode: '无',
      },
      methodColorMap: new Map(API_METHOD_COLOUR),
      maxComponentSize: 5, //浏览器最多渲染的api信息体数量
      apiShowArray: [], //浏览器要渲染的api信息集合
      needAsyncSelect: false, //是否需要异步查询api详细数据做展现。只有本次要展示的数据总量大于maxComponentSize时为true
      currentApiIndexInApiShowArray: 0, //当前主要展示的api信息在apiShowArray的索引
      clickStepFlag: false,
    };
  },
  props: {
    projectId: String,
    isTemplate: Boolean,
    apiInfo: Object,
  },
  computed: {},
  watch: {},
  methods: {
    handleExportHtml() {
      this.$emit('handleExportHtml', this.apiInfo.id);
    },
    isArrayHasData(arrayData) {
      if (!arrayData) {
        return false;
      }
      let jsonArr = JSON.parse(arrayData);
      let hasData = false;
      for (let index = 0; index < jsonArr.length; index++) {
        let item = jsonArr[index];
        if (item.name) {
          hasData = true;
        }
      }
      return hasData;
    },
    hasRequestParams(apiInfo) {
      let hasParams = false;
      if (apiInfo) {
        if (this.formParamTypes.includes(apiInfo.requestBodyParamType)) {
          if (apiInfo.requestBodyFormData && apiInfo.requestBodyFormData !== '无') {
            let jsonArr = JSON.parse(apiInfo.requestBodyFormData);
            //遍历，把必填项空的数据去掉
            for (let index = 0; index < jsonArr.length; index++) {
              let item = jsonArr[index];
              if (item.name) {
                hasParams = true;
              }
            }
          }
        } else if (apiInfo.requestBodyParamType === 'JSON-SCHEMA' || apiInfo.requestBodyParamType === 'JSON') {
          if (apiInfo.jsonSchemaBody && apiInfo.jsonSchemaBody !== '' && apiInfo.jsonSchemaBody !== '[]') {
            hasParams = true;
          }
        } else if (apiInfo.requestBodyStructureData && apiInfo.requestBodyStructureData !== '') {
          hasParams = true;
        }
      }
      return hasParams;
    },
    hasResponseBody(apiInfo) {
      let hasParams = false;
      if (apiInfo) {
        if (apiInfo.responseBodyParamType === 'Raw') {
          hasParams = true;
        } else if (this.formParamTypes.includes(apiInfo.responseBodyParamType)) {
          if (apiInfo.responseBodyFormData && apiInfo.responseBodyFormData !== '无') {
            let jsonArr = JSON.parse(apiInfo.responseBodyFormData);
            //遍历，把必填项空的数据去掉
            for (let index = 0; index < jsonArr.length; index++) {
              let item = jsonArr[index];
              if (item.name) {
                hasParams = true;
              }
            }
          }
        } else if (apiInfo.responseBodyParamType == 'JSON-SCHEMA') {
          if (
            apiInfo.jsonSchemaResponseBody &&
            apiInfo.jsonSchemaResponseBody !== '' &&
            apiInfo.jsonSchemaResponseBody !== '[]'
          ) {
            hasParams = true;
          }
        } else if (apiInfo.requestBodyStructureData && apiInfo.requestBodyStructureData !== '') {
          try {
            JSON.parse(apiInfo.requestBodyStructureData);
            hasParams = true;
          } catch (e) {
            hasParams = true;
          }
        }
      }
      return hasParams;
    },
    hasResponseCode(codeString) {
      if (codeString === '无' || codeString === null) {
        return false;
      } else {
        let hasCode = false;
        try {
          let jsonArr = JSON.parse(codeString);
          //遍历，把必填项空的数据去掉
          for (let index = 0; index < jsonArr.length; index++) {
            let item = jsonArr[index];
            if (item.name) {
              hasCode = true;
              break;
            }
          }
        } catch (e) {
          hasCode = false;
        }
        return hasCode;
      }
    },
    getId() {
      return this.apiInfo.id;
    },
    getHeight() {
      return this.$refs.baseDiv.offsetHeight;
    },
    shareApiDocument(isBatchShare) {
      this.shareUrl = '';
      this.batchShareUrl = '';
      let shareType = 'Single';
      let genShareInfoParam = {};
      genShareInfoParam.shareType = shareType;
      genShareInfoParam.shareId = this.apiInfo.id;

      generateApiDocumentShareInfo(genShareInfoParam).then((res) => {
        let data = res.data;
        if (shareType == 'Batch') {
          this.batchShareUrl = documentShareUrl(data);
        } else {
          this.shareUrl = documentShareUrl(data);
        }
      });
    },
    getColor(enable, method) {
      return this.methodColorMap.get(method);
    },
    getName(jsonString) {
      let returnString = '无';
      if (jsonString === '无' || jsonString === null) {
        return returnString;
      }

      try {
        let jsonArr = JSON.parse(jsonString);
        //遍历，把必填项空的数据去掉
        for (var index = 0; index < jsonArr.length; index++) {
          var item = jsonArr[index];
          if (item.name !== '') {
            returnString = item.name;
            break;
          }
        }
      } catch (e) {
        returnString = jsonString;
      }

      return returnString;
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

.apiInfoRow {
  margin: 10px 10px;
}

.apiInfoRow.el-row {
  margin: 10px 10px;
}

.apiStatusTag {
  margin: 10px 10px;
}

/*
步骤条中，已经完成后的节点样式和里面a标签的样式
*/
:deep(.el-step) {
  flex-basis: 40px !important;
}

:deep(.el-step__head.is-finish) {
  color: #c0c4cc;
  border-color: #c0c4cc;
}

:deep(.el-step__title.is-finish) :deep(.el-link.el-link--default) {
  color: #c0c4cc;
}

/*
步骤条中，当前节点样式和当前a标签的样式
*/
:deep(.el-step__head) {
  width: 20px;
}

:deep(.el-step__head.is-process) {
  color: #783887;
  border-color: #783887;
  width: 20px;
}

:deep(.el-step__title.is-process .el-link.el-link--default.is-underline) {
  color: #783887;
}

:deep(.el-link--inner) {
  font-size: 12px;
}

:deep(.el-step__icon-inner) {
  font-size: 12px;
  border-top-color: var(--primary_color);
}

:deep(.el-step.is-vertical .el-step__line) {
  left: 9px;
}

:deep(.el-step__icon) {
  width: 20px;
  height: 20px;
}

.attacInfo {
  font-size: 12px;
  color: #a0a0a0;
  margin: 10px;
}
</style>
