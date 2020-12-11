<template>
  <div v-if="visible" v-loading="result.loading">
    <ms-drawer :size="40" direction="bottom">
      <template v-slot:header>
        <api-case-header
          :api="api"
          @getApiTest="getApiTest"
          @setEnvironment="setEnvironment"
          @close="apiCaseClose"
          @addCase="addCase"
          @batchRun="batchRun"
          :condition="condition"
          :priorities="priorities"
          :apiCaseList="apiCaseList"
          :is-read-only="isReadOnly"
          :project-id="projectId"
        />
      </template>

      <el-container style="padding-bottom: 200px">

        <el-main v-loading="loading" style="overflow: auto">
          <div v-for="(item,index) in apiCaseList" :key="index">
            <el-card style="margin-top: 5px" @click.native="selectTestCase(item,$event)">
              <el-row>
                <el-col :span="6">
                  <div class="el-step__icon is-text ms-api-col">
                    <div class="el-step__icon-inner">{{index+1}}</div>
                  </div>

                  <label class="ms-api-label">{{$t('test_track.case.priority')}}</label>
                  <el-select size="small" v-model="item.priority" class="ms-api-select" @change="changePriority(item)">
                    <el-option v-for="grd in priorities" :key="grd.id" :label="grd.name" :value="grd.id"/>
                  </el-select>
                </el-col>
                <el-col :span="10">
                  <i class="icon el-icon-arrow-right" :class="{'is-active': item.active}"
                     @click="active(item)"/>
                  <el-input v-if="item.type==='create'" size="small" v-model="item.name" :name="index" :key="index"
                            class="ms-api-header-select" style="width: 180px"
                            @blur="saveTestCase(item)"/>
                  <span v-else>
                  {{item.type!= 'create' ? item.name:''}}
                  <i class="el-icon-edit" style="cursor:pointer" @click="showInput(item)"/>
                </span>
                  <div v-if="item.type!='create'" style="color: #999999;font-size: 12px">
                  <span>
                    {{item.createTime | timestampFormatDate }}
                    {{item.createUser}} {{$t('api_test.definition.request.create_info')}}
                  </span>
                    <span>
                    {{item.updateTime | timestampFormatDate }}
                    {{item.updateUser}} {{$t('api_test.definition.request.update_info')}}
                  </span>
                  </div>
                </el-col>

                <el-col :span="4">
                  <ms-tip-button @click="singleRun(item)" :tip="$t('api_test.run')" icon="el-icon-video-play"
                                 style="background-color: #409EFF;color: white" size="mini" :disabled="item.type=='create'" circle/>
                  <ms-tip-button @click="copyCase(item)" :tip="$t('commons.copy')" icon="el-icon-document-copy"
                                 size="mini" :disabled="item.type=='create'" circle/>
                  <ms-tip-button @click="deleteCase(index,item)" :tip="$t('commons.delete')" icon="el-icon-delete"
                                 size="mini" :disabled="item.type=='create'" circle/>
                  <ms-api-extend-btns :row="item"/>
                </el-col>

                <el-col :span="3">
                  <el-link type="danger" @click="showExecResult(item)" v-if="item.execResult && item.execResult==='error'">{{getResult(item.execResult)}}</el-link>
                  <div v-else>
                    <div v-if="item.type!='create'">{{getResult(item.execResult)}}</div>
                  </div>
                  <div v-if="item.type!='create'" style="color: #999999;font-size: 12px">
                    <span> {{item.updateTime | timestampFormatDate }}</span>
                    {{item.updateUser}}
                  </div>
                </el-col>
              </el-row>
              <!-- 请求参数-->
              <el-collapse-transition>
                <div v-if="item.active">
                  <p class="tip">{{$t('api_test.definition.request.req_param')}} </p>

                  <ms-api-request-form :is-read-only="isReadOnly" :headers="item.request.headers " :request="item.request" v-if="api.protocol==='HTTP'"/>
                  <ms-tcp-basis-parameters :request="item.request" v-if="api.protocol==='TCP'"/>
                  <ms-sql-basis-parameters :request="item.request" v-if="api.protocol==='SQL'"/>
                  <ms-dubbo-basis-parameters :request="item.request" v-if="api.protocol==='DUBBO'"/>
                  <!-- 保存操作 -->
                  <el-button type="primary" size="small" style="margin: 20px; float: right" @click="saveTestCase(item)">
                    {{$t('commons.save')}}
                  </el-button>
                </div>
              </el-collapse-transition>
            </el-card>
          </div>
        </el-main>

      </el-container>
    </ms-drawer>

    <!-- 执行组件 -->
    <ms-run :debug="false" :environment="environment" :reportId="reportId" :run-data="runData"
            @runRefresh="runRefresh" ref="runTest"/>

  </div>

</template>
<script>
  import MsTag from "../../../common/components/MsTag";
  import MsTipButton from "../../../common/components/MsTipButton";
  import MsApiRequestForm from "./request/http/ApiRequestForm";
  import {downloadFile, getUUID, getCurrentProjectID} from "@/common/js/utils";
  import ApiEnvironmentConfig from "./environment/ApiEnvironmentConfig";
  import {PRIORITY, RESULT_MAP} from "../model/JsonData";
  import MsApiAssertions from "./assertion/ApiAssertions";
  import MsRun from "./Run";
  import MsSqlBasisParameters from "./request/database/BasisParameters";
  import MsTcpBasisParameters from "./request/tcp/BasisParameters";
  import MsDubboBasisParameters from "./request/dubbo/BasisParameters";
  import MsDrawer from "../../../common/components/MsDrawer";
  import MsApiExtendBtns from "./reference/ApiExtendBtns";
  import ApiCaseHeader from "./case/ApiCaseHeader";

  export default {
    name: 'ApiCaseList',
    components: {
      ApiCaseHeader,
      MsDrawer,
      MsTag,
      MsTipButton,
      MsApiRequestForm,
      ApiEnvironmentConfig,
      MsApiAssertions,
      MsSqlBasisParameters,
      MsTcpBasisParameters,
      MsDubboBasisParameters,
      MsRun,
      MsApiExtendBtns
    },
    props: {
      createCase: String,
      loaded: Boolean,
      refreshSign: String,
      currentApi: {
        type: Object
      },
    },
    data() {
      return {
        result: {},
        grades: [],
        environment: {},
        isReadOnly: false,
        selectedEvent: Object,
        priorities: PRIORITY,
        apiCaseList: [],
        loading: false,
        runData: [],
        reportId: "",
        projectId: "",
        checkedCases: new Set(),
        visible: false,
        condition: {},
        api: {}
      }
    },
    watch: {
      refreshSign() {
        this.api = this.currentApi;
        this.getApiTest();
      },
      createCase() {
        this.api = this.currentApi;
        this.sysAddition();
      }
    },
    created() {
      this.api = this.currentApi;
      this.projectId = getCurrentProjectID();
      if (this.createCase) {
        this.sysAddition();
      } else {
        this.getApiTest();
      }
    },
    methods: {
      open(api) {
        this.api = api;
        this.getApiTest();
        this.visible = true;
      },
      setEnvironment(environment) {
        this.environment = environment;
      },
      sysAddition() {
        this.condition.projectId = this.projectId;
        this.condition.apiDefinitionId = this.api.id;
        this.$post("/api/testcase/list", this.condition, response => {
          for (let index in response.data) {
            let test = response.data[index];
            test.request = JSON.parse(test.request);
          }
          this.apiCaseList = response.data;
          this.addCase();
        });
      },
      getResult(data) {
        if (RESULT_MAP.get(data)) {
          return RESULT_MAP.get(data);
        } else {
          return RESULT_MAP.get("default");
        }
      },
      showInput(row) {
        row.type = "create";
        row.active = true;
        this.active(row);
      },
      apiCaseClose() {
        this.apiCaseList = [];
        this.visible = false;
      },
      batchRun() {
        if (!this.environment) {
          this.$warning(this.$t('api_test.environment.select_environment'));
          return;
        }
        if (this.apiCaseList.length > 0) {
          this.apiCaseList.forEach(item => {
            if (item.type != "create") {
              item.request.name = item.id;
              item.request.useEnvironment = this.environment.id;
              this.runData.push(item.request);
            }
          })
          if (this.runData.length > 0) {
            this.loading = true;
            /*触发执行操作*/
            this.reportId = getUUID().substring(0, 8);
          } else {
            this.$warning("没有可执行的用例！");
          }
        } else {
          this.$warning("没有可执行的用例！");
        }
      },
      singleRun(row) {
        if (!this.environment) {
          this.$warning(this.$t('api_test.environment.select_environment'));
          return;
        }
        this.runData = [];
        this.loading = true;
        row.request.name = row.id;
        row.request.useEnvironment = this.environment.id;
        this.runData.push(row.request);
        /*触发执行操作*/
        this.reportId = getUUID().substring(0, 8);
      },
      runRefresh(data) {
        this.loading = false;
        this.$success(this.$t('schedule.event_success'));
        this.getApiTest();
        this.$emit('refresh');
      },
      deleteCase(index, row) {
        this.$get('/api/testcase/delete/' + row.id, () => {
          this.$success(this.$t('commons.delete_success'));
          this.apiCaseList.splice(index, 1);
          this.$emit('refresh');
        });
      },
      copyCase(data) {
        let obj = {name: data.name, priority: data.priority, type: 'create', active: false, request: data.request};
        this.apiCaseList.unshift(obj);
      },
      addCase() {
        if (this.api.request) {
          // 初始化对象
          let request = {};
          if (this.api.request instanceof Object) {
            request = this.api.request;
          } else {
            request = JSON.parse(this.api.request);
          }
          let obj = {apiDefinitionId: this.api.id, name: '', priority: 'P0', type: 'create', active: false};
          obj.request = request;
          this.apiCaseList.unshift(obj);
        }
      },
      active(item) {
        item.active = !item.active;
      },
      getBodyUploadFiles(row) {
        let bodyUploadFiles = [];
        row.bodyUploadIds = [];
        let request = row.request;
        if (request.body && request.body.kvs) {
          request.body.kvs.forEach(param => {
            if (param.files) {
              param.files.forEach(item => {
                if (item.file) {
                  let fileId = getUUID().substring(0, 8);
                  item.name = item.file.name;
                  item.id = fileId;
                  row.bodyUploadIds.push(fileId);
                  bodyUploadFiles.push(item.file);
                }
              });
            }
          });
          if (request.body.binary) {
            request.body.binary.forEach(param => {
              if (param.files) {
                param.files.forEach(item => {
                  if (item.file) {
                    let fileId = getUUID().substring(0, 8);
                    item.name = item.file.name;
                    item.id = fileId;
                    row.bodyUploadIds.push(fileId);
                    bodyUploadFiles.push(item.file);
                  }
                });
              }
            });
          }
        }
        return bodyUploadFiles;
      },
      getApiTest() {
        if (this.api) {
          this.condition.projectId = this.projectId;
          this.condition.apiDefinitionId = this.api.id;
          this.result = this.$post("/api/testcase/list", this.condition, response => {
            for (let index in response.data) {
              let test = response.data[index];
              test.request = JSON.parse(test.request);
            }
            this.apiCaseList = response.data;
            if (this.apiCaseList.length == 0) {
              this.addCase();
            }
          });
        }
      },
      validate(row) {
        if (!row.name) {
          this.$warning(this.$t('api_test.input_name'));
          return true;
        }
      },
      changePriority(row) {
        if (row.type != 'create') {
          this.saveTestCase(row);
        }
      },
      saveTestCase(row) {
        if (this.validate(row)) {
          return;
        }
        let bodyFiles = this.getBodyUploadFiles(row);
        row.projectId = this.projectId;
        row.apiDefinitionId = row.apiDefinitionId || this.api.id;
        let url = "/api/testcase/create";
        if (row.id) {
          url = "/api/testcase/update";
        }
        this.$fileUpload(url, null, bodyFiles, row, () => {
          this.$success(this.$t('commons.save_success'));
          this.getApiTest();
          this.$emit('refresh');
        });
      },

      selectTestCase(item, $event) {
        if (item.type === "create" || !this.loaded) {
          return;
        }
        if ($event.currentTarget.className.indexOf('is-selected') > 0) {
          $event.currentTarget.className = "el-card is-always-shadow";
          this.$emit('selectTestCase', null);
        } else {
          if (this.selectedEvent.currentTarget != undefined) {
            this.selectedEvent.currentTarget.className = "el-card is-always-shadow";
          }
          this.selectedEvent.currentTarget = $event.currentTarget;
          $event.currentTarget.className = "el-card is-always-shadow is-selected";
          this.$emit('selectTestCase', item);
        }

      },
      handleClose() {
        this.visible = false;
      },
      showExecResult(row) {
        this.visible = false;
        this.$emit('showExecResult', row);
      }
    }
  }
</script>

<style scoped>
  .ms-api-select {
    margin-left: 20px;
    width: 80px;
  }

  .ms-api-header-select {
    margin-left: 20px;
    min-width: 100px;
  }

  .ms-api-label {
    color: #CCCCCC;
  }

  .ms-api-col {
    background-color: #7C3985;
    border-color: #7C3985;
    margin-right: 10px;
    color: white;
  }

  .icon.is-active {
    transform: rotate(90deg);
  }

  .tip {
    padding: 3px 5px;
    font-size: 16px;
    border-radius: 4px;
    border-left: 4px solid #783887;
    margin: 20px 0;
  }

  .is-selected {
    background: #EFF7FF;
  }
</style>
