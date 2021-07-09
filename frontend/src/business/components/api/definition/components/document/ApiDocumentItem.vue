<template>
  <div>
    <el-container>
      <el-main style="padding-top: 0px;padding-bottom: 0px">
        <el-row style="margin-top: 10px">
          <el-select size="small" :placeholder="$t('api_test.definition.document.order')" v-model="apiSearch.orderCondition" style="float: right;width: 180px;margin-right: 5px"
                     class="ms-api-header-select" @change="initApiDocSimpleList" clearable>
            <el-option key="createTimeDesc" :label="$t('api_test.definition.document.create_time_sort')" value="createTimeDesc" />
            <el-option key="editTimeAsc" :label="$t('api_test.definition.document.edit_time_positive_sequence')" value="editTimeAsc"/>
            <el-option key="editTimeDesc" :label="$t('api_test.definition.document.edit_time_Reverse_order')" value="editTimeDesc"/>
          </el-select>

          <el-select size="small" :placeholder="$t('api_test.definition.document.request_method')" v-model="apiSearch.type" style="float: right;width: 180px;margin-right: 5px"
                     class="ms-api-header-select" @change="initApiDocSimpleList" clearable>
            <el-option key="ALL" :label="$t('api_test.definition.document.data_set.all')" value="ALL"/>
            <el-option key="GET" :label="'GET '+$t('api_test.definition.document.request_interface')" value="GET"/>
            <el-option key="POST" :label="'POST '+$t('api_test.definition.document.request_interface')" value="POST"/>
            <el-option key="PUT" :label="'PUT '+$t('api_test.definition.document.request_interface')" value="PUT"/>
            <el-option key="DELETE" :label="'DELETE '+$t('api_test.definition.document.request_interface')" value="DELETE"/>
            <el-option key="PATCH" :label="'PATCH '+$t('api_test.definition.document.request_interface')" value="PATCH"/>
            <el-option key="OPTIONS" :label="'OPTIONS '+$t('api_test.definition.document.request_interface')" value="OPTIONS"/>
            <el-option key="HEAD" :label="'HEAD '+$t('api_test.definition.document.request_interface')" value="HEAD"/>
            <el-option key="CONNECT" :label="'CONNECT '+$t('api_test.definition.document.request_interface')" value="CONNECT"/>
          </el-select>
          <el-input :placeholder="$t('api_test.definition.document.search_by_api_name')" @blur="initApiDocSimpleList()" style="float: right;width: 180px;margin-right: 5px" size="small"
                    @keyup.enter.native="initApiDocSimpleList()" v-model="apiSearch.name"/>
          <api-document-batch-share v-xpack v-if="showXpackCompnent" @shareApiDocument="shareApiDocument" :project-id="projectId" :share-url="batchShareUrl" style="float: right;margin: 6px;font-size: 17px"/>
          <!--          <api-document-batch-share v-xpack v-if="showXpackCompnent"/>-->
        </el-row>
        <el-divider></el-divider>
        <div ref="apiDocInfoDiv" @scroll="handleScroll" >
          <div v-for="(apiInfo) in apiInfoArray" :key="apiInfo.id" ref="apiDocInfoDivItem">
            <div style="font-size: 17px">
              <el-popover
                v-if="projectId"
                placement="right"
                width="260"
                @show="shareApiDocument('false')">
                <p>{{shareUrl}}</p>
                <div style="text-align: right; margin: 0">
                  <el-button type="primary" size="mini"
                             v-clipboard:copy="shareUrl">{{ $t("commons.copy") }}</el-button>
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
                            :data="getJsonArr(apiInfo.requestHead)" row-key="name" class="test-content document-table">
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
                            :data="getJsonArr(apiInfo.urlParams)" row-key="name" class="test-content document-table">
                    <el-table-column prop="name"
                                     :label="$t('api_test.definition.document.table_coloum.name')"
                                     min-width="120px"
                                     show-overflow-tooltip/>
                    <el-table-column prop="isEnable"
                                     :label="$t('api_test.definition.document.table_coloum.is_required')"
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
                          :data="getJsonArr(apiInfo.requestBodyFormData)" row-key="name"
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
                  <ms-json-code-edit :body="apiInfo.jsonSchemaBody" ref="jsonCodeEdit"/>
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
                          :data="getJsonArr(apiInfo.responseHead)" row-key="name" class="test-content document-table">
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
                          :data="getJsonArr(apiInfo.responseBodyFormData)" row-key="id"
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
                          :data="getJsonArr(apiInfo.responseCode)" row-key="name" class="test-content document-table">
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
        </div>
      </el-main>
      <!-- 右侧列表 -->
      <el-aside width="200px" style="margin-top: 70px;">
        <div ref="apiDocList" >
          <el-steps style="height: 40%" direction="vertical" :active="apiStepIndex">
            <el-step v-for="(apiInfo) in apiInfoArray" :key="apiInfo.id" @click.native="clickStep(apiInfo.id)">
              <el-link slot="title">{{ apiInfo.name }}</el-link>
            </el-step>
          </el-steps>
        </div>
      </el-aside>
    </el-container>
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

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const apiDocumentBatchShare = (requireComponent!=null&&requireComponent.keys().length) > 0 ? requireComponent("./share/ApiDocumentBatchShare.vue") : {};

export default {
  name: "ApiDocumentItem",
  components: {
    Api,
    MsJsonCodeEdit,
    ApiStatus, MsCodeEdit,
    "ApiDocumentBatchShare": apiDocumentBatchShare.default
  },
  data() {
    return {
      shareUrl:"",
      batchShareUrl:"",
      apiStepIndex: 0,
      showXpackCompnent:false,
      apiInfoArray: [],
      modes: ['text', 'json', 'xml', 'html'],
      formParamTypes: ['form-data', 'x-www-from-urlencoded', 'BINARY'],
      mockVariableFuncs: [],
      apiSearch:{
        name:"",
        type:"ALL",
        orderCondition:"createTimeDesc",
      },
      apiInfoBaseObj: {
        selectedFlag:false,
        method: "无",
        uri: "无",
        name: "无",
        id: "",
        requestHead: "无",
        urlParams: "无",
        requestBodyParamType: "无",
        requestBodyFormData: '[]',
        requestBodyStrutureData: "",
        sharePopoverVisible:false,
        jsonSchemaBody: {},
        responseHead: "无",
        responseBody: "",
        responseBodyParamType: "无",
        responseBodyFormData: "无",
        responseBodyStrutureData: "无",
        responseCode: "无",
      },
      methodColorMap: new Map(API_METHOD_COLOUR),
      clientHeight: '',//坚挺浏览器高度
    }
  },
  props: {
    projectId: String,
    documentId: String,
    moduleIds: Array,
    pageHeaderHeight:Number,
  },
  activated() {
    this.initApiDocSimpleList();
    this.clientHeight = `${document.documentElement.clientHeight}`;//获取浏览器可视区域高度
    let that = this;
    window.onresize = function () {
      this.clientHeight = `${document.documentElement.clientHeight}`;
      this.changeFixed(this.clientHeight);
    }
  },
  created: function () {
    if(requireComponent!=null && JSON.stringify(apiDocumentBatchShare) != '{}'){
      this.showXpackCompnent = true;
    }
    this.initApiDocSimpleList();
    this.clientHeight = `${document.documentElement.clientHeight}`;//获取浏览器可视区域高度
    let that = this;
    window.onresize = function () {
      this.clientHeight = `${document.documentElement.clientHeight}`;
      this.changeFixed(this.clientHeight);
    };
    window.addEventListener('scroll',that.handleScroll);
  },
  mounted() {
    let that = this;
    window.onresize = function () {
      that.clientHeight = `${document.documentElement.clientHeight}`;
      that.changeFixed(that.clientHeight);
    };
    // 监听滚动事件，然后用handleScroll这个方法进行相应的处理
    window.addEventListener('scroll',this.handleScroll);
  },
  computed: {
  },
  watch: {
    moduleIds() {
      this.initApiDocSimpleList();
    },
    clientHeight() {     //如果clientHeight 发生改变，这个函数就会运行
      this.changeFixed(this.clientHeight);
    }
  },
  methods: {
    formatRowData(dataType, data) {
      var returnData = data;
      if (data) {
        returnData = data.replace(/\n/g, '<br>');
      }
      return returnData;
    },
    changeFixed(clientHeight) {
      if (this.$refs.apiDocInfoDiv) {
        let countPageHeight = 350;
        if(this.pageHeaderHeight!=0 && this.pageHeaderHeight != null){
          countPageHeight = this.pageHeaderHeight
        }

        this.$refs.apiDocInfoDiv.style.height = clientHeight - countPageHeight + 'px';
        this.$refs.apiDocInfoDiv.style.overflow = 'auto';
        this.$refs.apiDocList.style.height = clientHeight - countPageHeight + 'px';
      }
    },
    initApiDocSimpleList() {
      let simpleRequest = this.apiSearch;
      if (this.projectId != null && this.projectId != "") {
        simpleRequest.projectId = this.projectId;
      }
      if (this.documentId != null && this.documentId != "") {
        simpleRequest.shareId = this.documentId;
      }
      if (this.moduleIds.length > 0) {
        simpleRequest.moduleIds = this.moduleIds;
      }

      let simpleInfoUrl = "/api/document/selectApiSimpleInfo";
      this.apiInfoArray = [];
      this.$post(simpleInfoUrl, simpleRequest, response => {
        this.apiInfoArray = response.data;
        this.apiStepIndex = 0;
        if (this.apiInfoArray.length > 0) {
          this.checkApiInfoNode(this.apiStepIndex);
        }
      });
    },
    shareApiDocument(isBatchShare){
      let thisHost = window.location.host;
      this.shareUrl = "";
      this.batchShareUrl = "";
      let shareIdArr = [];
      let shareType = "Single";
      if(isBatchShare == 'true'){
        this.apiInfoArray.forEach(f => {
          if (!f.id) {
            return;
          }
          shareIdArr.push(f.id);
        });
        shareType = "Batch";
      }else{
        shareIdArr.push(this.apiInfoArray[this.apiStepIndex].id);
      }
      let genShareInfoParam = {};
      genShareInfoParam.shareApiIdList = shareIdArr;
      genShareInfoParam.shareType = shareType;

      this.$post("/api/document/generateApiDocumentShareInfo", genShareInfoParam, res => {
        if(shareType == "Batch"){
          this.batchShareUrl = thisHost+"/document"+res.data.shareUrl;
        }else{
          this.shareUrl = thisHost+"/document"+res.data.shareUrl;
        }
      }, (error) => {
      });
    },
    selectApiInfo(index,apiId) {
      let simpleInfoUrl = "/api/document/selectApiInfoById/" + apiId;
      this.$get(simpleInfoUrl, response => {
        let returnData = response.data;
        this.$set(this.apiInfoArray,index,returnData);
      });
    },
    clickStep(apiId) {
      for (let index = 0; index < this.apiInfoArray.length; index++) {
        if (apiId == this.apiInfoArray[index].id) {
          this.apiStepIndex = index;
          break;
        }
      }
      //检查数据
      this.checkApiInfoNode(this.apiStepIndex);
      //进行跳转
      this.redirectScroll(this.apiStepIndex);
    },
    stepClick(stepIndex) {
      this.apiStepIndex = stepIndex;
    },
    getColor(enable, method) {
      return this.methodColorMap.get(method);
    },
    formatBoolean(row, column, cellValue) {
      var ret = ''  //你想在页面展示的值
      if (cellValue) {
        ret = "是"  //根据自己的需求设定
      } else {
        ret = "否"
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
          if(typeof(value)=='string'){
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
    onCopySuccess: function (e) {
      if(this.apiStepIndex < this.apiInfoArray.length){
        this.apiInfoArray[this.apiStepIndex].sharePopoverVisible = false;
      }
      this.$message({
        message: this.$t('commons.copy_success'),
        type: 'success'
      });
    },
    onCopyError: function (e) {
      if(this.apiStepIndex < this.apiInfoArray.length){
        this.apiInfoArray[this.apiStepIndex].sharePopoverVisible = false;
      }
      this.$message.error(this.$t('api_report.error'));
    },
    handleScroll(){

      //apiDocInfoDiv的总高度，是(每个item的高度+20)数量
      let apiDocDivScrollTop = this.$refs.apiDocInfoDiv.scrollTop;
      let apiDocDivClientTop = this.$refs.apiDocInfoDiv.clientHeight;

      let scrolledHeigh = apiDocDivScrollTop+apiDocDivClientTop;
      let lastIndex = 0;
      for (let index = 0; index < this.apiInfoArray.length; index++) {
        //判断移动到了第几个元素. 公式: 移动过的高度+页面显示高度-第index子元素的高度(含20px)>0 的 index最大值
        if(scrolledHeigh>0){
          lastIndex = index;
          let itemHeight = this.$refs.apiDocInfoDivItem[index].offsetHeight+20;
          scrolledHeigh = scrolledHeigh - itemHeight;
        }else{
          break;
        }
      }
      this.apiStepIndex = lastIndex;
      //检查上下文 3个以内的节点有没有查询出来
      this.checkApiInfoNode(this.apiStepIndex);
    },
    redirectScroll(itemIndex){
      //滚动条跳转：将滚动条下拉到显示对应对api接口的位置
      // let apiDocDivClientTop = this.$refs.apiDocInfoDiv.clientHeight;
      let apiDocDivClientTop = 0;
      let itemHeightCount = 0;
      for (let i = 0; i <= itemIndex-1; i++) {
        let itemHeight = this.$refs.apiDocInfoDivItem[i].offsetHeight+20;
        itemHeightCount+=itemHeight;
      }
      this.$refs.apiDocInfoDiv.scrollTop = (apiDocDivClientTop+itemHeightCount);
    },
    checkApiInfoNode(itemIndex){
      //检查要展示的api信息节点，和上下个3个及以内的范围内数据有没有查询过
      let beforeNodeIndex = itemIndex<3?0:(itemIndex-3);
      let afterNodeIndex = (itemIndex+3)<this.apiInfoArray.length?(itemIndex+3):this.apiInfoArray.length;

      for(let beforeIndex = itemIndex;beforeIndex < afterNodeIndex;beforeIndex++){
        let apiInfo = this.apiInfoArray[beforeIndex];
        if(apiInfo==null){
          continue;
        }
        if(apiInfo == null || !apiInfo.selectedFlag){
          let apiId = apiInfo.id;
          if(!apiInfo.isSearching){
            apiInfo.isSearching = true;
            this.selectApiInfo(beforeIndex,apiId);
          }
        }
      }

      for(let afterIndex = beforeNodeIndex;afterIndex <itemIndex;afterIndex++){
        let apiInfo = this.apiInfoArray[afterIndex];
        if(apiInfo==null){
          continue;
        }
        if(apiInfo == null || !apiInfo.selectedFlag){
          let apiId = apiInfo.id;
          if(!apiInfo.isSearching) {
            apiInfo.isSearching = true;
            this.selectApiInfo(afterIndex,apiId);
          }
        }
      }
    }
  },
}
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
  margin: 20px 10px;
}

.apiInfoRow {
  margin: 20px 10px;
}

.apiStatusTag {
  margin: 20px 5px;
}

.showDataDiv {
  background-color: #F5F7F9;
  margin: 20px 10px;
}

/*
步骤条中，已经完成后的节点样式和里面a标签的样式
*/
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
/deep/ .el-step__head.is-process {
  color: #783887;
  border-color: #783887;
}

/deep/ .el-step__title.is-process /deep/ .el-link.el-link--default {
  color: #783887;
}

.document-table {
  margin: 20px 10px;
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
