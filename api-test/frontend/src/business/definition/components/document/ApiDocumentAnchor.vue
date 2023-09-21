<template>
  <div v-loading="isLoading">
    <div>
      <el-container>
        <el-main style="padding-top: 0px; padding-bottom: 0px">
          <!--   筛选条件     -->
          <el-row v-if="sharePage" style="margin-top: 10px">
            <el-select
              size="small"
              :placeholder="$t('api_test.definition.document.order')"
              v-model="apiSearch.orderCondition"
              style="float: right; width: 180px; margin-right: 5px"
              class="ms-api-header-select"
              @change="initApiDocSimpleList"
              clearable>
              <el-option
                key="createTimeDesc"
                :label="$t('api_test.definition.document.create_time_sort')"
                value="createTimeDesc" />
              <el-option
                key="editTimeAsc"
                :label="$t('api_test.definition.document.edit_time_positive_sequence')"
                value="editTimeAsc" />
              <el-option
                key="editTimeDesc"
                :label="$t('api_test.definition.document.edit_time_Reverse_order')"
                value="editTimeDesc" />
            </el-select>

            <el-select
              size="small"
              :placeholder="$t('api_test.definition.document.request_method')"
              v-model="apiSearch.type"
              style="float: right; width: 180px; margin-right: 5px"
              class="ms-api-header-select"
              @change="initApiDocSimpleList"
              clearable>
              <el-option key="ALL" :label="$t('api_test.definition.document.data_set.all')" value="ALL" />
              <el-option key="GET" :label="'GET ' + $t('api_test.definition.document.request_interface')" value="GET" />
              <el-option
                key="POST"
                :label="'POST ' + $t('api_test.definition.document.request_interface')"
                value="POST" />
              <el-option key="PUT" :label="'PUT ' + $t('api_test.definition.document.request_interface')" value="PUT" />
              <el-option
                key="DELETE"
                :label="'DELETE ' + $t('api_test.definition.document.request_interface')"
                value="DELETE" />
              <el-option
                key="PATCH"
                :label="'PATCH ' + $t('api_test.definition.document.request_interface')"
                value="PATCH" />
              <el-option
                key="OPTIONS"
                :label="'OPTIONS ' + $t('api_test.definition.document.request_interface')"
                value="OPTIONS" />
              <el-option
                key="HEAD"
                :label="'HEAD ' + $t('api_test.definition.document.request_interface')"
                value="HEAD" />
              <el-option
                key="CONNECT"
                :label="'CONNECT ' + $t('api_test.definition.document.request_interface')"
                value="CONNECT" />
            </el-select>
            <el-input
              :placeholder="$t('api_test.definition.document.search_by_api_name')"
              @blur="initApiDocSimpleList()"
              style="float: right; width: 180px; margin-right: 5px"
              size="small"
              @keyup.enter.native="initApiDocSimpleList()"
              v-model="apiSearch.name" />
            <mx-api-document-batch-share
              v-xpack
              @shareApiDocument="shareApiDocument"
              :project-id="projectId"
              :share-url="batchShareUrl"
              style="float: right; margin: 6px; font-size: 17px" />
          </el-row>
          <el-row
            v-else
            style="
              margin-top: 5px;
              position: fixed;
              float: right;
              margin-right: 0px;
              margin-left: 400px;
              top: 90px;
              right: 40px;
            "
            v-show="!isTemplate">
            <el-select
              size="small"
              :placeholder="$t('api_test.definition.document.order')"
              v-model="apiSearch.orderCondition"
              style="float: right; width: 180px; margin-right: 5px"
              class="ms-api-header-select"
              @change="initApiDocSimpleList"
              clearable>
              <el-option
                key="createTimeDesc"
                :label="$t('api_test.definition.document.create_time_sort')"
                value="createTimeDesc" />
              <el-option
                key="editTimeAsc"
                :label="$t('api_test.definition.document.edit_time_positive_sequence')"
                value="editTimeAsc" />
              <el-option
                key="editTimeDesc"
                :label="$t('api_test.definition.document.edit_time_Reverse_order')"
                value="editTimeDesc" />
            </el-select>

            <el-select
              size="small"
              :placeholder="$t('api_test.definition.document.request_method')"
              v-model="apiSearch.type"
              style="float: right; width: 180px; margin-right: 5px"
              class="ms-api-header-select"
              @change="initApiDocSimpleList"
              clearable>
              <el-option key="ALL" :label="$t('api_test.definition.document.data_set.all')" value="ALL" />
              <el-option key="GET" :label="'GET ' + $t('api_test.definition.document.request_interface')" value="GET" />
              <el-option
                key="POST"
                :label="'POST ' + $t('api_test.definition.document.request_interface')"
                value="POST" />
              <el-option key="PUT" :label="'PUT ' + $t('api_test.definition.document.request_interface')" value="PUT" />
              <el-option
                key="DELETE"
                :label="'DELETE ' + $t('api_test.definition.document.request_interface')"
                value="DELETE" />
              <el-option
                key="PATCH"
                :label="'PATCH ' + $t('api_test.definition.document.request_interface')"
                value="PATCH" />
              <el-option
                key="OPTIONS"
                :label="'OPTIONS ' + $t('api_test.definition.document.request_interface')"
                value="OPTIONS" />
              <el-option
                key="HEAD"
                :label="'HEAD ' + $t('api_test.definition.document.request_interface')"
                value="HEAD" />
              <el-option
                key="CONNECT"
                :label="'CONNECT ' + $t('api_test.definition.document.request_interface')"
                value="CONNECT" />
            </el-select>
            <el-input
              :placeholder="$t('api_test.definition.document.search_by_api_name')"
              @blur="initApiDocSimpleList()"
              style="float: right; width: 180px; margin-right: 5px"
              size="small"
              @keyup.enter.native="initApiDocSimpleList()"
              v-model="apiSearch.name" />
            <mx-api-document-batch-share
              v-xpack
              @shareApiDocument="shareApiDocument"
              :project-id="projectId"
              :share-url="batchShareUrl"
              style="float: right; margin: 6px; font-size: 17px" />

            <el-tooltip :content="$t('commons.export')" placement="top" v-if="!isTemplate" v-xpack>
              <i
                class="el-icon-download"
                @click="handleExportHtml()"
                style="margin-top: 5px; font-size: 20px; cursor: pointer" />
            </el-tooltip>
          </el-row>
          <el-divider></el-divider>
          <!--   展示区域     -->
          <div ref="apiDocInfoDiv" @scroll="handleScroll" style="overflow: auto">
            <api-information
              v-for="apiInfo in apiInfoArray"
              :key="apiInfo.id"
              :api-info="apiInfo"
              :project-id="projectId"
              :isTemplate="isTemplate"
              @handleExportHtml="handleExportHtml"
              ref="apiDocInfoDivItem" />
          </div>
        </el-main>
        <!-- 右侧列表 -->
        <el-aside width="210px" style="margin-top: 30px">
          <div ref="apiDocList">
            <el-steps style="height: 40%" direction="vertical">
              <el-step
                v-for="apiInfo in apiInfoArray"
                :key="apiInfo.id"
                @click.native="clickStep(apiInfo.id)"
                name="apiInfoStepNode">
                <el-link slot="title">
                  <span style="display: inline-block; word-break: break-all; white-space: normal">
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
      :total="total"
      v-show="!isTemplate">
    </el-pagination>
  </div>
</template>

<script>
import { API_METHOD_COLOUR } from '@/business/definition/model/JsonData';
import MsCodeEdit from 'metersphere-frontend/src/components/MsCodeEdit';
import ApiStatus from '@/business/definition/components/list/ApiStatus';
import MsJsonCodeEdit from '@/business/commons/json-schema/JsonSchemaEditor';
import { documentShareUrl, generateApiDocumentShareInfo, selectApiInfoByParam } from '@/api/share';
import ApiInformation from '@/business/definition/components/document/components/ApiInformation';
import { getCurrentUser } from 'metersphere-frontend/src/utils/token';
import { request } from 'metersphere-frontend/src/plugins/request';

export default {
  name: 'ApiDocumentAnchor',
  components: {
    MsJsonCodeEdit,
    ApiStatus,
    MsCodeEdit,
    ApiInformation,
    MxApiDocumentBatchShare: () => import('@/business/definition/components/share/MxApiDocumentBatchShare'),
  },
  data() {
    return {
      isLoading: false,
      shareUrl: '',
      batchShareUrl: '',
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
        requestBodyParamType: '无',
        requestBodyFormData: '[]',
        requestBodyStructureData: '',
        sharePopoverVisible: false,
        jsonSchemaBody: {},
        JsonSchemaResponseBody: {},
        responseHead: '无',
        responseBody: '',
        responseBodyParamType: '无',
        responseBodyFormData: '无',
        responseCode: '无',
      },
      methodColorMap: new Map(API_METHOD_COLOUR),
      clientHeight: '', //浏览器高度,
      maxComponentSize: 10, //浏览器最多渲染的api信息体数量
      needAsyncSelect: false, //是否需要异步查询api详细数据做展现。只有本次要展示的数据总量大于maxComponentSize时为true
      currentApiIndexInApiShowArray: 0, //当前主要展示的api信息在apiShowArray的索引
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
    isTemplate: {
      type: Boolean,
      default: false,
    },
    trashEnable: {
      type: Boolean,
      default: false,
    },
    versionId: String,
  },
  activated() {
    this.initApiDocSimpleList();
    this.clientHeight = `${document.documentElement.clientHeight}`; //获取浏览器可视区域高度
    let that = this;
    window.onresize = function () {
      this.clientHeight = `${document.documentElement.clientHeight}`;
      this.changeFixed(this.clientHeight);
    };
  },
  created: function () {
    this.initApiDocSimpleList();
    this.clientHeight = `${document.documentElement.clientHeight}`; //获取浏览器可视区域高度
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
    clientHeight() {
      //如果clientHeight 发生改变，这个函数就会运行
      this.changeFixed(this.clientHeight);
    },
    trashEnable() {
      this.initApiDocSimpleList();
    },
    versionId() {
      this.initApiDocSimpleList();
    },
  },
  methods: {
    fileDownload(url, param) {
      let config = {
        url: url,
        method: 'post',
        data: param,
        responseType: 'blob',
        headers: { 'Content-Type': 'application/json; charset=utf-8' },
      };
      request(config).then(
        (response) => {
          let link = document.createElement('a');
          link.href = window.URL.createObjectURL(
            new Blob([response.data], {
              type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8',
            })
          );
          link.download = 'api-doc.html';
          this.result = false;
          link.click();
        },
        (error) => {
          this.result = false;
          if (error.response && error.response.status === 509) {
            let reader = new FileReader();
            reader.onload = function (event) {
              let content = reader.result;
              $error(content);
            };
            reader.readAsText(error.response.data);
          } else {
            $error('导出doc文件失败');
          }
        }
      );
    },
    handleExportHtml(id) {
      let url = '/share/doc/export/' + this.currentPage + '/' + this.pageSize;
      let lang = 'zh_CN';
      let user = getCurrentUser();
      if (user && user.language) {
        lang = user.language;
      }
      url = url + '/' + lang;
      this.loading = true;
      let simpleRequest = this.apiSearch;
      if (this.projectId != null && this.projectId !== '') {
        simpleRequest.projectId = this.projectId;
      }
      if (this.documentId != null && this.documentId !== '') {
        simpleRequest.shareId = this.documentId;
      }
      if (this.moduleIds.length > 0) {
        simpleRequest.moduleIds = this.moduleIds;
      } else {
        simpleRequest.moduleIds = [];
      }
      simpleRequest.apiIdList = [];
      if (id) {
        simpleRequest.apiIdList = [id];
      }
      simpleRequest.versionId = this.versionId;
      simpleRequest.trashEnable = this.trashEnable;
      this.fileDownload(url, simpleRequest);
    },
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
        if (this.pageHeaderHeight && this.pageHeaderHeight !== 0) {
          countPageHeight = this.pageHeaderHeight;
        }
        if (this.isTemplate) {
          this.$refs.apiDocInfoDiv.style.height = clientHeight - 50 + 'px';
          this.$refs.apiDocList.style.height = clientHeight - 50 + 'px';
        } else {
          this.$refs.apiDocInfoDiv.style.height = clientHeight - countPageHeight + 'px';
          this.$refs.apiDocList.style.height = clientHeight - countPageHeight + 'px';
        }
        this.$refs.apiDocInfoDiv.style.overflow = 'auto';
      }
    },
    initApiDocSimpleList() {
      this.apiInfoArray = [];
      let simpleRequest = this.apiSearch;
      if (this.projectId !== null && this.projectId !== '') {
        simpleRequest.projectId = this.projectId;
      }
      if (this.documentId !== null && this.documentId !== '') {
        simpleRequest.shareId = this.documentId;
      }
      if (this.moduleIds && this.moduleIds.length > 0) {
        simpleRequest.moduleIds = this.moduleIds;
      } else {
        simpleRequest.moduleIds = [];
      }
      simpleRequest.versionId = this.versionId;
      simpleRequest.trashEnable = this.trashEnable;
      if (this.isTemplate) {
        let response = '#export-doc';
        this.apiInfoArray = response.listObject;
        this.total = response.itemCount;
        this.needAsyncSelect = response.length > this.maxComponentSize;
        //每次查询完成之后定位右侧的步骤
        this.$nextTick(() => {
          let apiNodeDom = document.getElementsByName('apiInfoStepNode');
          if (apiNodeDom) {
            this.changeApiStepNodeClass(apiNodeDom[0], true);
          }
        });
      } else {
        selectApiInfoByParam(simpleRequest, this.currentPage, this.pageSize).then((response) => {
          this.apiInfoArray = response.data.listObject;
          this.total = response.data.itemCount;
          if (response.data.length > this.maxComponentSize) {
            this.needAsyncSelect = true;
          } else {
            this.needAsyncSelect = false;
          }
          //每次查询完成之后定位右侧的步骤
          this.$nextTick(() => {
            this.handleScroll();
          });
        });
      }
    },
    shareApiDocument() {
      this.shareUrl = '';
      this.batchShareUrl = '';
      let shareIdArr = [];
      let genShareInfoParam = {};
      genShareInfoParam.shareApiIdList = shareIdArr;
      genShareInfoParam.shareType = 'Batch';

      let simpleRequest = this.apiSearch;
      if (this.projectId != null && this.projectId != '') {
        simpleRequest.projectId = this.projectId;
      }
      if (this.documentId != null && this.documentId != '') {
        simpleRequest.shareId = this.documentId;
      }
      if (this.moduleIds.length > 0) {
        simpleRequest.moduleIds = this.moduleIds;
      } else {
        simpleRequest.moduleIds = [];
      }
      simpleRequest.trashEnable = this.trashEnable;

      genShareInfoParam.selectRequest = simpleRequest;

      generateApiDocumentShareInfo(genShareInfoParam).then((res) => {
        let data = res.data;
        this.batchShareUrl = documentShareUrl(data);
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
        if (apiStep.className.indexOf('apiShowStep') < 0) {
          apiStep.className = apiStep.className + ' apiShowStep';
        }
      } else {
        if (apiStep.className.indexOf('apiShowStep') > 0) {
          apiStep.className = apiStep.className.substring(0, apiStep.className.length - 12);
        }
      }
    },
    handleScroll() {
      if (!this.clickStepFlag && this.$refs.apiDocInfoDiv) {
        //apiNodeDom:设置右侧节点的样式
        let apiNodeDom = document.getElementsByName('apiInfoStepNode');
        if (!apiNodeDom) {
          return;
        }

        let scrollTopCount = 0;
        if (this.$refs.apiDocInfoDiv && this.$refs.apiDocInfoDiv.scrollTop) {
          scrollTopCount = this.$refs.apiDocInfoDiv.scrollTop;
        }
        let mainDivHeight = this.$refs.apiDocInfoDiv.clientHeight;

        let apiDocDivScrollTop = scrollTopCount;
        for (let index = 0; index < this.apiInfoArray.length; index++) {
          let itemHeight = this.$refs.apiDocInfoDivItem[index].getHeight() + 3;
          if (apiDocDivScrollTop > itemHeight) {
            this.changeApiStepNodeClass(apiNodeDom[index], false);
          } else {
            if (mainDivHeight + apiDocDivScrollTop > 0) {
              this.changeApiStepNodeClass(apiNodeDom[index], true);
            } else {
              this.changeApiStepNodeClass(apiNodeDom[index], false);
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
  background-color: #f5f7f9;
  margin: 10px 10px;
  max-height: 300px;
  overflow: auto;
}

/*
步骤条中，已经完成后的节点样式和里面a标签的样式
*/
:deep(.el-step__head) {
  width: 20px;
}

.apiShowStep :deep(.el-step__head.is-wait) {
  color: #783887;
  border-color: #783887;
  width: 20px;
}

.apiShowStep :deep(.el-step__title.is-wait .el-link.el-link--default.is-underline) {
  color: #783887;
}

:deep(.el-step__icon-inner) {
  font-size: 12px;
  border-top-color: #783887;
}

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

:deep(.el-link--inner) {
  font-size: 12px;
}

.el-divider--horizontal {
  margin: 12px 0;
}
</style>
