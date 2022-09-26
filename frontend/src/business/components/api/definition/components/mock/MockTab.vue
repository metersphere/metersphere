<template>
  <div>
    <div>
      <div class="ms-opt-btn" v-if="versionEnable">
        {{ $t('project.version.name') }}: {{ versionName }}
      </div>
      Mock地址：
      <el-link v-if="this.getUrlPrefix !== '' " :href="getUrlPrefix" style="color: black" target="_blank"
               type="primary">
        <span>{{ this.getUrlPrefix }}</span>
      </el-link>
      <el-link v-else style="color: darkred" target="_blank"
               type="primary">当前项目未开启Mock服务
      </el-link>
      <el-input v-model="tableSearch" :clearable="true" :placeholder="$t('commons.search_by_name')"
                class="search-input"
                size="small"/>
      <el-button v-permission="['PROJECT_API_DEFINITION:READ+EDIT_API']" icon="el-icon-plus" size="small"
                 style="float: right;margin-right: 10px"
                 type="primary"
                 @click="addApiMock">{{ $t('commons.add') }}
      </el-button>

      <ms-table
        v-loading="result.loading"
        :data="mockConfigData.mockExpectConfigList.filter(data=>!tableSearch || data.name.toLowerCase().includes(tableSearch.toLowerCase()))"
        :operators="operators"
        :page-size="pageSize"
        :showSelectAll="false"
        :screen-height="screenHeight"
        @row-click="clickRow"
        row-key="id"
        operator-width="170px"
        ref="table"
      >

        <ms-table-column
          prop="expectNum"
          :label="$t('commons.id')"
          min-width="120px">
        </ms-table-column>

        <ms-table-column
          prop="name"
          :label="$t('api_test.mock.table.name')"
          min-width="160px">
        </ms-table-column>
        <ms-table-column prop="tags" width="200px" :label="$t('api_test.mock.table.tag')">
          <template v-slot:default="scope">
            <ms-tag v-for="(itemName,index)  in scope.row.tags" :key="index" type="success" effect="plain"
                    :show-tooltip="true" :content="itemName" style="margin-left: 0px; margin-right: 2px"/>
            <span/>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="createUserId"
          :label="$t('api_test.mock.table.creator')"
          min-width="160px"/>
        <ms-table-column
          sortable="updateTime"
          min-width="160px"
          :label="$t('api_test.definition.api_last_time')"
          prop="updateTime">
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </ms-table-column>
        <ms-table-column
          prop="status"
          min-width="50px"
          :label="$t('api_test.mock.table.status')">
          <template v-slot:default="scope">
            <div>
              <el-switch
                v-model="scope.row.status"
                class="captcha-img"
                @change="changeStatus(scope.row)"
              ></el-switch>
            </div>
          </template>
        </ms-table-column>
      </ms-table>
    </div>
    <mock-edit-drawer :is-tcp="isTcp" :api-id="this.baseMockConfigData.mockConfig.apiId"
                      :api-params="apiParams"
                      @refreshMockInfo="refreshMockInfo"
                      :mock-config-id="mockConfigData.mockConfig.id" ref="mockEditDrawer"/>
  </div>
</template>

<script>

import {getCurrentProjectID, hasLicense, operationConfirm} from "@/common/js/utils";
import MockEditDrawer from "@/business/components/api/definition/components/mock/MockEditDrawer";
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import MsTag from "@/business/components/common/components/MsTag";

export default {
  name: 'MockTab',
  components: {
    MockEditDrawer,
    MsTable, MsTableColumn, MsTag
  },
  props: {
    baseMockConfigData: {},
    versionName: String,
    isTcp: {
      type: Boolean,
      default: false,
    },
    form: Object
  },
  data() {
    return {
      result: {},
      visible: false,
      mockConfigData: {},
      tableSearch: "",
      apiParams: {},
      pageSize: 10,
      screenHeight: document.documentElement.clientHeight - 250,
      operators: [
        {
          tip: this.$t('api_test.automation.execute'),
          icon: "el-icon-video-play",
          exec: this.redirectToTest,
          class: "run-button",
          permissions: ['PROJECT_API_DEFINITION:READ+RUN']
        },
        {
          tip: this.$t('commons.edit'), icon: "el-icon-edit",
          exec: this.clickRow,
        },
        {
          tip: this.$t('commons.copy'), icon: "el-icon-copy-document",
          exec: this.copyExpect,
        },
        {
          tip: this.$t('commons.delete'), icon: "el-icon-delete", type: "danger",
          exec: this.removeExpect,
          permissions: ['PROJECT_TRACK_REVIEW:READ+RELEVANCE_OR_CANCEL']
        }
      ],
      versionEnable: false,
      mockBaseUrl: "",
    };
  },

  watch: {
    baseMockConfigData() {
      this.mockConfigData = this.baseMockConfigData;
    },
  },
  created() {
    this.mockConfigData = this.baseMockConfigData;
    this.checkVersionEnable();
    this.initMockEnvironment();
  },
  computed: {
    getUrlPrefix() {
      if (this.form.path == null) {
        return this.mockBaseUrl;
      } else {
        let path = this.form.path;
        let protocol = this.form.method;
        if (protocol === 'GET' || protocol === 'DELETE') {
          if (this.form.request != null && this.form.request.rest != null) {
            let pathUrlArr = path.split("/");
            let newPath = "";
            pathUrlArr.forEach(item => {
              if (item !== "") {
                let pathItem = item;
                if (item.indexOf("{") === 0 && item.indexOf("}") === (item.length - 1)) {
                  let paramItem = item.substr(1, item.length - 2);
                  for (let i = 0; i < this.form.request.rest.length; i++) {
                    let param = this.form.request.rest[i];
                    if (param.name === paramItem) {
                      pathItem = param.value;
                    }
                  }
                }
                newPath += "/" + pathItem;
              }
            });
            if (newPath !== "") {
              path = newPath;
            }
          }
        }
        return this.mockBaseUrl + path;
      }
    },
    projectId() {
      return getCurrentProjectID();
    },
    urlWidth() {
      return document.documentElement.clientWidth - 900 + 'px';
    }
  },
  methods: {
    redirectToTest(row) {
      let requestParam = null;
      if (row && row.request) {
        requestParam = JSON.parse(JSON.stringify(row.request));
      }
      if (requestParam.xmlDataStruct) {
        this.getTcpMockTestData(requestParam);
      } else {
        this.getHttpMockTestData(requestParam);
      }

    },
    getTcpMockTestData(requestParam) {
      if (requestParam && requestParam.xmlDataStruct) {
        let selectParma = requestParam.xmlDataStruct;
        //调用后台生成符合mock需求的测试数据
        this.$post("/mockConfig/getTcpMockTestData", selectParma, response => {
          let returnData = response.data;
          if (returnData) {
            requestParam.xmlDataStruct = returnData;
          }
          this.$emit("redirectToTest", requestParam);
        }, error => {
          this.$emit("redirectToTest", requestParam);
        });
      }
    },
    getHttpMockTestData(requestParam) {
      if (requestParam && requestParam.params) {
        let selectParma = [];
        if (requestParam.params.arguments && requestParam.params.arguments.length > 0) {
          requestParam.params.arguments.forEach(item => {
            if (item.rangeType && item.value && item.uuid) {
              let paramObj = {id: item.uuid, value: item.value, condition: item.rangeType};
              selectParma.push(paramObj);
            }
          });
        }
        if (requestParam.params.rest && requestParam.params.rest.length > 0) {
          requestParam.params.rest.forEach(item => {
            if (item.rangeType && item.value && item.uuid) {
              let paramObj = {id: item.uuid, value: item.value, condition: item.rangeType};
              selectParma.push(paramObj);
            }
          });
        }
        if (requestParam.params.body.kvs && requestParam.params.body.kvs.length > 0) {
          requestParam.params.body.kvs.forEach(item => {
            if (item.rangeType && item.value && item.uuid) {
              let paramObj = {id: item.uuid, value: item.value, condition: item.rangeType};
              selectParma.push(paramObj);
            }
          });
        }
        //调用后台生成符合mock需求的测试数据
        this.$post("/mockConfig/getMockTestData", selectParma, response => {
          let returnData = response.data;
          if (returnData && returnData.length > 0) {
            returnData.forEach(data => {
              if (requestParam.params.arguments && requestParam.params.arguments.length > 0) {
                for (let i = 0; i < requestParam.params.arguments.length; i++) {
                  if (requestParam.params.arguments[i].uuid === data.id) {
                    requestParam.params.arguments[i].value = data.value;
                  }
                }
              }
              if (requestParam.params.rest && requestParam.params.rest.length > 0) {
                for (let i = 0; i < requestParam.params.rest.length; i++) {
                  if (requestParam.params.rest[i].uuid === data.id) {
                    requestParam.params.rest[i].value = data.value;
                  }
                }
              }
              if (requestParam.params.body.kvs && requestParam.params.body.kvs.length > 0) {
                for (let i = 0; i < requestParam.params.body.kvs.length; i++) {
                  if (requestParam.params.body.kvs[i].uuid === data.id) {
                    requestParam.params.body.kvs[i].value = data.value;
                  }
                }
              }
            });
          }
          this.$emit("redirectToTest", requestParam);
        }, error => {
          this.$emit("redirectToTest", requestParam);
        });
      }
    },
    searchApiParams(apiId) {
      let selectUrl = "/mockConfig/getApiParams/" + apiId;
      this.$get(selectUrl, response => {

        this.apiParams = response.data;
        if (!this.apiParams.query) {
          this.apiParams.query = [];
        }
        if (!this.apiParams.rest) {
          this.apiParams.rest = [];
        }
        if (!this.apiParams.form) {
          this.apiParams.form = [];
        }
      });
    },
    changeStatus(row) {
      let mockParam = {};
      mockParam.id = row.id;
      mockParam.status = row.status;
      this.$post('/mockConfig/updateMockExpectConfigStatus', mockParam, response => {
      });
    },
    copyExpect(row) {
      let selectUrl = "/mockConfig/mockExpectConfig/" + row.id;
      this.$get(selectUrl, response => {
        let data = response.data;
        let mockExpectConfig = data;
        mockExpectConfig.id = "";
        mockExpectConfig.copyId = row.id;
        mockExpectConfig.name = mockExpectConfig.name + "_copy";
        if (mockExpectConfig.request == null) {
          mockExpectConfig.request = {
            jsonParam: false,
            variables: [],
            jsonData: "{}",
          };
        }
        if (mockExpectConfig.response == null) {
          mockExpectConfig.response = {
            httpCode: "",
            delayed: "",
            httpHeads: [],
            body: "",
          };
        }
        this.searchApiParams(this.mockConfigData.mockConfig.apiId);
        this.$refs.mockEditDrawer.close();
        this.$nextTick(() => {
          this.$refs.mockEditDrawer.open(mockExpectConfig);
        });
      });
    },
    clickRow(row, column, event) {
      let selectUrl = "/mockConfig/mockExpectConfig/" + row.id;
      this.$get(selectUrl, response => {
        let data = response.data;
        let mockExpectConfig = data;
        if (mockExpectConfig.request == null) {
          mockExpectConfig.request = {
            jsonParam: false,
            variables: [],
            jsonData: "{}",
          };
        }
        if (mockExpectConfig.response == null) {
          mockExpectConfig.response = {
            httpCode: "",
            delayed: "",
            httpHeads: [],
            body: "",
          };
        }
        this.searchApiParams(this.mockConfigData.mockConfig.apiId);
        this.$refs.mockEditDrawer.close();
        this.$nextTick(() => {
          this.$refs.mockEditDrawer.open(mockExpectConfig);
        });
      });
    },
    addApiMock() {
      this.searchApiParams(this.mockConfigData.mockConfig.apiId);
      this.$refs.mockEditDrawer.close();
      this.$nextTick(() => {
        this.$refs.mockEditDrawer.open();
      });
    },
    removeExpect(row) {
      operationConfirm(this.$t('api_test.mock.delete_mock_expect'), () => {
        let mockInfoId = row.mockConfigId;
        let selectUrl = "/mockConfig/deleteMockExpectConfig/" + row.id;
        this.$get(selectUrl, response => {
          this.refreshMockInfo(mockInfoId);
          this.$message({
            type: 'success',
            message: this.$t('commons.delete_success'),
          });
        });
      });
    },
    refreshMockInfo(mockConfigId) {
      let mockParam = {};
      mockParam.id = mockConfigId;
      this.$post('/mockConfig/genMockConfig', mockParam, response => {
        this.mockConfigData = response.data;
      });
    },
    checkVersionEnable() {
      if (!this.projectId) {
        return;
      }
      if (hasLicense()) {
        this.$get('/project/version/enable/' + this.projectId, response => {
          this.versionEnable = response.data;
        });
      }
    },
    initMockEnvironment() {
      let protocol = document.location.protocol;
      protocol = protocol.substring(0, protocol.indexOf(":"));
      let url = "/api/definition/getMockEnvironment/";
      this.$get(url + this.projectId, response => {
        let mockEnvironment = response.data;
        let httpConfig = JSON.parse(mockEnvironment.config);
        if (httpConfig != null) {
          httpConfig = httpConfig.httpConfig;
          let httpType = httpConfig.defaultCondition;
          let conditions = httpConfig.conditions;
          conditions.forEach(condition => {
            if (condition.type === httpType) {
              this.mockBaseUrl = condition.protocol + "://" + condition.socket;
            }
          });
        }
      });
    },
  }
};
</script>

<style scoped>

.ms-drawer >>> .ms-drawer-body {
  margin-top: 40px;
}

.search-input {
  float: right;
  width: 300px;
  margin-right: 10px;
  margin-bottom: 10px;
}

</style>
