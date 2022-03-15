<template>
  <div>
    <!--api请求头-->
    <api-info-collapse table-coloum-type="nameAndValue" :title="$t('api_test.definition.document.request_head')"
                       :string-data="apiInfo.requestHead"/>
    <!--QUERY参数-->
    <api-info-collapse table-coloum-type="simple" :title="'QUERY'+$t('api_test.definition.document.request_param')"
                       :string-data="apiInfo.urlParams"/>
    <!--REST参数-->
    <api-info-collapse table-coloum-type="simple" :title="'REST'+$t('api_test.definition.document.request_param')"
                       :string-data="apiInfo.restParams"/>
    <!--api请求体 以及表格-->
    <api-info-collapse :is-request="true" :remarks="apiInfo.requestBodyParamType"
                       :title="$t('api_test.definition.document.request_body')">
      <api-request-info slot="request" :api-info="apiInfo"></api-request-info>
    </api-info-collapse>

    <!--响应头-->
    <api-info-collapse table-coloum-type="nameAndValue" :title="$t('api_test.definition.document.response_head')"
                       :string-data="apiInfo.responseHead"/>
    <!--响应体-->
    <api-info-collapse :is-response="true" :remarks="apiInfo.responseBodyParamType"
                       :title="$t('api_test.definition.document.response_body')">
      <api-response-info slot="response" :api-info="apiInfo"></api-response-info>
    </api-info-collapse>

    <!--响应状态码-->
    <api-info-collapse :is-text="true" :string-data="getName(apiInfo.responseCode)"
                       :title="$t('api_test.definition.document.response_code')"/>
  </div>
</template>

<script>
import {API_METHOD_COLOUR} from "@/business/components/api/definition/model/JsonData";
import ApiInfoCollapse from "@/business/components/api/definition/components/document/components/ApiInfoCollapse";
import ApiRequestInfo from "@/business/components/api/definition/components/document/components/ApiRequestInfo";
import ApiResponseInfo from "@/business/components/api/definition/components/document/components/ApiResponseInfo";
export default {
  name: "HttpInformation",
  props: {
    apiInfo: {},
  },
  components:{ApiInfoCollapse,ApiResponseInfo,ApiRequestInfo},
  data() {
    return {
    }
  },
  methods: {
    getName(jsonString) {
      let returnString = "无";
      if (jsonString === '无' || jsonString === null) {
        return returnString;
      }
      try {
        let jsonArr = JSON.parse(jsonString);
        //遍历，把必填项空的数据去掉
        for (var index = 0; index < jsonArr.length; index++) {
          var item = jsonArr[index];
          if (item.name !== "") {
            returnString = item.name;
            break;
          }
        }
      } catch (e) {
        returnString = jsonString;
      }
      return returnString;
    },

  }
}
</script>

<style scoped>
</style>
