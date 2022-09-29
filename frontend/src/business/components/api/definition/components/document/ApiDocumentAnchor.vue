<template>
  <div v-loading="isLoading">
    <div>
      <el-container>
        <el-main style="padding-top: 0px;padding-bottom: 0px">
          <!--   筛选条件     -->
          <el-row v-if="sharePage" style="margin-top: 10px">
            <el-select size="small" :placeholder="$t('api_test.definition.document.order')"
                       v-model="apiSearch.orderCondition" style="float: right;width: 180px;margin-right: 5px"
                       class="ms-api-header-select" @change="initApiDocSimpleList" clearable>
              <el-option key="createTimeDesc" :label="$t('api_test.definition.document.create_time_sort')"
                         value="createTimeDesc"/>
              <el-option key="editTimeAsc" :label="$t('api_test.definition.document.edit_time_positive_sequence')"
                         value="editTimeAsc"/>
              <el-option key="editTimeDesc" :label="$t('api_test.definition.document.edit_time_Reverse_order')"
                         value="editTimeDesc"/>
            </el-select>

            <el-select size="small" :placeholder="$t('api_test.definition.document.request_method')"
                       v-model="apiSearch.type" style="float: right;width: 180px;margin-right: 5px"
                       class="ms-api-header-select" @change="initApiDocSimpleList" clearable>
              <el-option key="ALL" :label="$t('api_test.definition.document.data_set.all')" value="ALL"/>
              <el-option key="GET" :label="'GET '+$t('api_test.definition.document.request_interface')" value="GET"/>
              <el-option key="POST" :label="'POST '+$t('api_test.definition.document.request_interface')" value="POST"/>
              <el-option key="PUT" :label="'PUT '+$t('api_test.definition.document.request_interface')" value="PUT"/>
              <el-option key="DELETE" :label="'DELETE '+$t('api_test.definition.document.request_interface')"
                         value="DELETE"/>
              <el-option key="PATCH" :label="'PATCH '+$t('api_test.definition.document.request_interface')"
                         value="PATCH"/>
              <el-option key="OPTIONS" :label="'OPTIONS '+$t('api_test.definition.document.request_interface')"
                         value="OPTIONS"/>
              <el-option key="HEAD" :label="'HEAD '+$t('api_test.definition.document.request_interface')" value="HEAD"/>
              <el-option key="CONNECT" :label="'CONNECT '+$t('api_test.definition.document.request_interface')"
                         value="CONNECT"/>
            </el-select>
            <el-input :placeholder="$t('api_test.definition.document.search_by_api_name')"
                      @blur="initApiDocSimpleList()"
                      style="float: right;width: 180px;margin-right: 5px" size="small"
                      @keyup.enter.native="initApiDocSimpleList()" v-model="apiSearch.name"/>
            <api-document-batch-share v-xpack v-if="showXpackCompnent" @shareApiDocument="shareApiDocument"
                                      :project-id="projectId" :share-url="batchShareUrl"
                                      style="float: right;margin: 6px;font-size: 17px"/>
          </el-row>
          <el-row v-else
                  style="margin-top: 0px;position: fixed;float: right;margin-right: 0px;margin-left: 400px;top: 90px; right: 40px;">
            <el-select size="small" :placeholder="$t('api_test.definition.document.order')"
                       v-model="apiSearch.orderCondition" style="float: right;width: 180px;margin-right: 5px"
                       class="ms-api-header-select" @change="initApiDocSimpleList" clearable>
              <el-option key="createTimeDesc" :label="$t('api_test.definition.document.create_time_sort')"
                         value="createTimeDesc"/>
              <el-option key="editTimeAsc" :label="$t('api_test.definition.document.edit_time_positive_sequence')"
                         value="editTimeAsc"/>
              <el-option key="editTimeDesc" :label="$t('api_test.definition.document.edit_time_Reverse_order')"
                         value="editTimeDesc"/>
            </el-select>

            <el-select size="small" :placeholder="$t('api_test.definition.document.request_method')"
                       v-model="apiSearch.type" style="float: right;width: 180px;margin-right: 5px"
                       class="ms-api-header-select" @change="initApiDocSimpleList" clearable>
              <el-option key="ALL" :label="$t('api_test.definition.document.data_set.all')" value="ALL"/>
              <el-option key="GET" :label="'GET '+$t('api_test.definition.document.request_interface')" value="GET"/>
              <el-option key="POST" :label="'POST '+$t('api_test.definition.document.request_interface')" value="POST"/>
              <el-option key="PUT" :label="'PUT '+$t('api_test.definition.document.request_interface')" value="PUT"/>
              <el-option key="DELETE" :label="'DELETE '+$t('api_test.definition.document.request_interface')"
                         value="DELETE"/>
              <el-option key="PATCH" :label="'PATCH '+$t('api_test.definition.document.request_interface')"
                         value="PATCH"/>
              <el-option key="OPTIONS" :label="'OPTIONS '+$t('api_test.definition.document.request_interface')"
                         value="OPTIONS"/>
              <el-option key="HEAD" :label="'HEAD '+$t('api_test.definition.document.request_interface')" value="HEAD"/>
              <el-option key="CONNECT" :label="'CONNECT '+$t('api_test.definition.document.request_interface')"
                         value="CONNECT"/>
            </el-select>
            <el-input :placeholder="$t('api_test.definition.document.search_by_api_name')"
                      @blur="initApiDocSimpleList()"
                      style="float: right;width: 180px;margin-right: 5px" size="small"
                      @keyup.enter.native="initApiDocSimpleList()" v-model="apiSearch.name"/>
            <api-document-batch-share v-xpack v-if="showXpackCompnent" @shareApiDocument="shareApiDocument"
                                      :project-id="projectId" :share-url="batchShareUrl"
                                      style="float: right;margin: 6px;font-size: 17px"/>
          </el-row>
          <el-divider></el-divider>
          <!--   展示区域     -->
          <div ref="apiDocInfoDiv" @scroll="handleScroll" style="overflow: auto">
            <api-information v-for="(apiInfo) in apiInfoArray" :key="apiInfo.id" :api-info="apiInfo"
                             :project-id="projectId" ref="apiDocInfoDivItem"/>
          </div>
        </el-main>
        <!-- 右侧列表 -->
        <el-aside width="210px" style="margin-top: 30px;">
          <div ref="apiDocList">
            <el-steps style="height: 40%" direction="vertical">
              <el-step v-for="(apiInfo) in apiInfoArray" :key="apiInfo.id" @click.native="clickStep(apiInfo.id)"
                       name="apiInfoStepNode">
                <el-link slot="title">
                  <span style="display: inline-block; word-break: break-all; white-space: normal;">
                    {{ apiInfo.name }}
                  </span>
                </el-link>
              </el-step>
            </el-steps>
          </div>
        </el-aside>
      </el-container>
    </div>
    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="currentPage"
      :page-sizes="[10, 20, 50]"
      :page-size="pageSize"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total">
    </el-pagination>
  </div>
</template>

<script>
import {API_METHOD_COLOUR} from "@/business/components/api/definition/model/JsonData";
import MsCodeEdit from "@/business/components/common/components/MsCodeEdit";
import ApiStatus from "@/business/components/api/definition/components/list/ApiStatus";
import MsJsonCodeEdit from "@/business/components/common/json-schema/JsonSchemaEditor";
import Api from "@/business/components/api/router";
import {generateApiDocumentShareInfo} from "@/network/share";
import ApiInformation from "@/business/components/api/definition/components/document/components/ApiInformation";

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const apiDocumentBatchShare = (requireComponent != null && requireComponent.keys().length) > 0 ? requireComponent("./share/ApiDocumentBatchShare.vue") : {};

export default {
  name: "ApiDocumentAnchor",
  components: {
    Api,
    MsJsonCodeEdit,
    ApiStatus, MsCodeEdit, ApiInformation,
    "ApiDocumentBatchShare": apiDocumentBatchShare.default
  },
  data() {
    return {
      isLoading: false,
      shareUrl: "",
      batchShareUrl: "",
      showXpackCompnent: false,
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
      clientHeight: '',//浏览器高度,
      maxCompnentSize: 10, //浏览器最多渲染的api信息体数量
      needAsyncSelect: false, //是否需要异步查询api详细数据做展现。只有本次要展示的数据总量大于maxCompnentSize时为true
      currentApiIndexInApiShowArray: 0,//当前主要展示的api信息在apiShowArray的索引
      clickStepFlag: false,
      pageSize: 10,
      currentPage: 1,
      total: 0,
    };
  },
  props: {
    projectId: String,
    documentId: String,
    moduleIds: Array,
    sharePage: Boolean,
    pageHeaderHeight: Number,
    trashEnable: {
      type: Boolean,
      default: false,
    },
    versionId: String
  },
  activated() {
    this.initApiDocSimpleList();
    this.clientHeight = `${document.documentElement.clientHeight}`;//获取浏览器可视区域高度
    let that = this;
    window.onresize = function () {
      this.clientHeight = `${document.documentElement.clientHeight}`;
      this.changeFixed(this.clientHeight);
    };
  },
  created: function () {
    if (requireComponent != null && JSON.stringify(apiDocumentBatchShare) != '{}') {
      this.showXpackCompnent = true;
    }
    this.initApiDocSimpleList();
    this.clientHeight = `${document.documentElement.clientHeight}`;//获取浏览器可视区域高度
    // let that = this;
    window.onresize = function () {
      this.clientHeight = `${document.documentElement.clientHeight}`;
      this.changeFixed(this.clientHeight);
    };
    window.addEventListener('scroll', this.handleScroll);
  },
  mounted() {
    let that = this;
    window.onresize = function () {
      that.clientHeight = `${document.documentElement.clientHeight}`;
      that.changeFixed(that.clientHeight);
    };
    // 监听滚动事件，然后用handleScroll这个方法进行相应的处理
    window.addEventListener('scroll', this.handleScroll);
  },
  computed: {},
  watch: {
    moduleIds() {
      this.initApiDocSimpleList();
    },
    clientHeight() {     //如果clientHeight 发生改变，这个函数就会运行
      this.changeFixed(this.clientHeight);
    },
    trashEnable() {
      this.initApiDocSimpleList();
    },
    versionId() {
      this.initApiDocSimpleList();
    }
  },
  methods: {
    handleSizeChange(val) {
      this.pageSize = val;
      this.initApiDocSimpleList();
    },
    handleCurrentChange(val) {
      this.currentPage = val;
      this.initApiDocSimpleList();
    },
    changeFixed(clientHeight) {
      if (this.$refs.apiDocInfoDiv) {
        let countPageHeight = 210;
        if (this.pageHeaderHeight != 0 && this.pageHeaderHeight != null) {
          countPageHeight = this.pageHeaderHeight;
        }
        this.$refs.apiDocInfoDiv.style.height = clientHeight - countPageHeight + 'px';
        this.$refs.apiDocInfoDiv.style.overflow = 'auto';
        this.$refs.apiDocList.style.height = clientHeight - countPageHeight + 'px';
      }
    },
    initApiDocSimpleList() {
      this.apiInfoArray = [];
      let simpleRequest = this.apiSearch;
      if (this.projectId != null && this.projectId != "") {
        simpleRequest.projectId = this.projectId;
      }
      if (this.documentId != null && this.documentId != "") {
        simpleRequest.shareId = this.documentId;
      }
      if (this.moduleIds.length > 0) {
        simpleRequest.moduleIds = this.moduleIds;
      } else {
        simpleRequest.moduleIds = [];
      }
      simpleRequest.versionId = this.versionId;
      simpleRequest.trashEnable = this.trashEnable;
      let simpleInfoUrl = "/share/info/selectApiInfoByParam/" + this.currentPage + "/" + this.pageSize;
      this.$post(simpleInfoUrl, simpleRequest, response => {
        this.apiInfoArray = response.data.listObject;
        this.total = response.data.itemCount;
        if (response.data.length > this.maxCompnentSize) {
          this.needAsyncSelect = true;
        } else {
          this.needAsyncSelect = false;
        }
        //每次查询完成之后定位右侧的步骤
        this.$nextTick(() => {
          this.handleScroll();
        });
      });
    },
    shareApiDocument() {
      this.shareUrl = "";
      this.batchShareUrl = "";
      let shareIdArr = [];
      let genShareInfoParam = {};
      genShareInfoParam.shareApiIdList = shareIdArr;
      genShareInfoParam.shareType = "Batch";

      let simpleRequest = this.apiSearch;
      if (this.projectId != null && this.projectId != "") {
        simpleRequest.projectId = this.projectId;
      }
      if (this.documentId != null && this.documentId != "") {
        simpleRequest.shareId = this.documentId;
      }
      if (this.moduleIds.length > 0) {
        simpleRequest.moduleIds = this.moduleIds;
      } else {
        simpleRequest.moduleIds = [];
      }
      simpleRequest.trashEnable = this.trashEnable;

      genShareInfoParam.selectRequest = simpleRequest;

      generateApiDocumentShareInfo(genShareInfoParam, (data) => {
        let thisHost = window.location.host;
        this.batchShareUrl = thisHost + "/document" + data.shareUrl;
      });
    },

    clickStep(apiId) {
      this.clickStepFlag = true;
      let apiStepIndex = 0;
      for (let index = 0; index < this.apiInfoArray.length; index++) {
        if (apiId == this.apiInfoArray[index].id) {
          apiStepIndex = index;
          break;
        }
      }
      //节点跳转
      this.checkApiInfoNode(apiStepIndex);
    },
    changeApiStepNodeClass(apiStep, isApiShow) {
      if (isApiShow) {
        if (apiStep.className.indexOf("apiShowStep") < 0) {
          apiStep.className = apiStep.className + " apiShowStep";
        }
      } else {
        if (apiStep.className.indexOf("apiShowStep") > 0) {
          apiStep.className = apiStep.className.substring(0, apiStep.className.length - 12);
        }
      }
    },
    handleScroll() {
      if (!this.clickStepFlag && this.$refs.apiDocInfoDiv) {
        //apiNodeDom:设置右侧节点的样式
        let apiNodeDoms = document.getElementsByName("apiInfoStepNode");
        if (!apiNodeDoms) {
          return;
        }

        let scrollTopCount = 0;
        if (this.$refs.apiDocInfoDiv && this.$refs.apiDocInfoDiv.scrollTop) {
          scrollTopCount = this.$refs.apiDocInfoDiv.scrollTop;
        }
        let mainDivHeight = this.$refs.apiDocInfoDiv.clientHeight;

        let apiDocDivScrollTop = scrollTopCount;
        let screenHight = mainDivHeight;
        for (let index = 0; index < this.apiInfoArray.length; index++) {
          let itemHeight = this.$refs.apiDocInfoDivItem[index].getHeight() + 3;
          if (apiDocDivScrollTop > itemHeight) {
            this.changeApiStepNodeClass(apiNodeDoms[index], false);
          } else {
            if (screenHight + apiDocDivScrollTop > 0) {
              this.changeApiStepNodeClass(apiNodeDoms[index], true);
            } else {
              this.changeApiStepNodeClass(apiNodeDoms[index], false);
            }
          }
          apiDocDivScrollTop = apiDocDivScrollTop - itemHeight;
        }
      }
      this.clickStepFlag = false;
    },

    checkApiInfoNode(itemIndex) {
      let scrollTopCount = 0;
      for (let index = 0; index < this.apiInfoArray.length; index++) {
        if (index < itemIndex) {
          scrollTopCount += this.$refs.apiDocInfoDivItem[index].getHeight() + 5;
        }
      }
      this.$refs.apiDocInfoDiv.scrollTop = scrollTopCount;
      this.$nextTick(() => {
        this.handleScroll();
      });
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
/deep/ .el-step__head {
  width: 20px;
}

.apiShowStep /deep/ .el-step__head.is-wait {
  color: #783887;
  border-color: #783887;
  width: 20px;
}

.apiShowStep /deep/ .el-step__title.is-wait .el-link.el-link--default.is-underline {
  color: #783887;
}

/deep/ .el-step__icon-inner {
  font-size: 12px;
  border-top-color: #783887;
}

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

/deep/ .el-link--inner {
  font-size: 12px;
}

.el-divider--horizontal {
  margin: 12px 0;
}
</style>
