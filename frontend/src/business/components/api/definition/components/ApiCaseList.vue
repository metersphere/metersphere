<template>
  <div v-if="visible" v-loading="result.loading">
    <ms-drawer :size="40" direction="bottom">

      <template v-slot:header>
        <el-header style="width: 100% ;padding: 0px">
          <el-card>
            <el-row>
              <el-col :span="api.protocol==='HTTP'? 3:5">
                <div class="variable-combine"> {{api.name}}</div>
              </el-col>
              <el-col :span="api.protocol==='HTTP'? 1:3">
                <ms-tag v-if="api.status == 'Prepare'" type="info" effect="plain" :content="$t('test_track.plan.plan_status_prepare')"/>
                <ms-tag v-if="api.status == 'Underway'" type="warning" effect="plain" :content="$t('test_track.plan.plan_status_running')"/>
                <ms-tag v-if="api.status == 'Completed'" type="success" effect="plain" :content="$t('test_track.plan.plan_status_completed')"/>
              </el-col>
              <el-col :span="api.protocol==='HTTP'? 4:0">
                <div class="variable-combine" style="margin-left: 10px">{{api.path ===null ? " " : api.path}}</div>
              </el-col>
              <el-col :span="2">
                <div>{{$t('test_track.plan_view.case_count')}}：{{apiCaseList.length}}</div>
              </el-col>
              <el-col :span="3">
                <div>
                  <el-select size="small" :placeholder="$t('api_test.definition.request.grade_info')" v-model="priorityValue"
                             class="ms-api-header-select" @change="getApiTest">
                    <el-option v-for="grd in priority" :key="grd.id" :label="grd.name" :value="grd.id"/>
                  </el-select>
                </div>
              </el-col>
              <el-col :span="4">
                <div>
                  <el-select :disabled="isReadOnly" v-model="environment" size="small" class="ms-api-header-select"
                             :placeholder="$t('api_test.definition.request.run_env')"
                             @change="environmentChange" clearable>
                    <el-option v-for="(environment, index) in environments" :key="index"
                               :label="environment.name + (environment.config.httpConfig.socket ? (': ' + environment.config.httpConfig.protocol + '://' + environment.config.httpConfig.socket) : '')"
                               :value="environment.id"/>
                    <el-button class="environment-button" size="mini" type="primary" @click="openEnvironmentConfig">
                      {{ $t('api_test.environment.environment_config') }}
                    </el-button>
                    <template v-slot:empty>
                      <div class="empty-environment">
                        <el-button class="environment-button" size="mini" type="primary" @click="openEnvironmentConfig">
                          {{ $t('api_test.environment.environment_config') }}
                        </el-button>
                      </div>
                    </template>
                  </el-select>
                </div>
              </el-col>
              <el-col :span="3">
                <div class="ms-api-header-select">
                  <el-input size="small" :placeholder="$t('api_test.definition.request.select_case')"
                            v-model="name" @blur="getApiTest"/>
                </div>
              </el-col>
              <el-col :span="2">
                <el-dropdown size="small" split-button type="primary" class="ms-api-header-select" @click="addCase"
                             @command="handleCommand">
                  +{{$t('api_test.definition.request.case')}}
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item command="run">{{$t('commons.test')}}</el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>


              </el-col>
              <el-col :span="2">
                <button type="button" aria-label="Close" class="el-card-btn" @click="apiCaseClose()"><i
                  class="el-dialog__close el-icon el-icon-close"></i></button>
              </el-col>

            </el-row>
          </el-card>

          <!-- 环境 -->
          <api-environment-config ref="environmentConfig" @close="environmentConfigClose"/>

        </el-header>
      </template>

      <el-container style="padding-bottom: 200px">

        <el-main v-loading="loading" style="overflow: auto">
          <div v-for="(item,index) in apiCaseList" :key="index">
            <el-card style="margin-top: 5px" @click.native="selectTestCase(item,$event)">
              <el-row>
                <el-col :span="1">
                  <el-checkbox v-if="visible" @change="caseChecked(item)"/>
                </el-col>

                <el-col :span="5">
                  <div class="el-step__icon is-text ms-api-col">
                    <div class="el-step__icon-inner">{{index+1}}</div>
                  </div>

                  <label class="ms-api-label">{{$t('test_track.case.priority')}}</label>
                  <el-select size="small" v-model="item.priority" class="ms-api-select">
                    <el-option v-for="grd in priority" :key="grd.id" :label="grd.name" :value="grd.id"/>
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
                                 style="background-color: #409EFF;color: white" size="mini" circle/>
                  <ms-tip-button @click="copyCase(item)" :tip="$t('commons.copy')" icon="el-icon-document-copy"
                                 size="mini" circle/>
                  <ms-tip-button @click="deleteCase(index,item)" :tip="$t('commons.delete')" icon="el-icon-delete"
                                 size="mini" circle/>
                  <ms-api-extend-btns :row="item"/>
                </el-col>

                <el-col :span="3">
                  <div v-if="item.type!='create'">{{getResult(item.execResult)}}</div>
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
  import {parseEnvironment} from "../model/EnvironmentModel";
  import ApiEnvironmentConfig from "./environment/ApiEnvironmentConfig";
  import {PRIORITY, RESULT_MAP} from "../model/JsonData";
  import MsApiAssertions from "./assertion/ApiAssertions";
  import MsRun from "./Run";
  import MsSqlBasisParameters from "./request/database/BasisParameters";
  import MsTcpBasisParameters from "./request/tcp/BasisParameters";
  import MsDubboBasisParameters from "./request/dubbo/BasisParameters";
  import MsDrawer from "../../../common/components/MsDrawer";
  import MsApiExtendBtns from "./reference/ApiExtendBtns";

  export default {
    name: 'ApiCaseList',
    components: {
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
      api: {
        type: Object
      },
      createCase: String,
      loaded: Boolean,
      currentProject: {},
      refreshSign: String,
      currentRow: Object,
    },
    data() {
      return {
        result: {},
        grades: [],
        environments: [],
        environment: {},
        name: "",
        priorityValue: "",
        isReadOnly: false,
        selectedEvent: Object,
        priority: PRIORITY,
        apiCaseList: [],
        loading: false,
        runData: [],
        reportId: "",
        projectId: "",
        checkedCases: new Set(),
        visible: false

      }
    },
    watch: {
      // 初始化
      api() {
        if (this.currentRow) {
          this.currentRow.cases = [];
        }
        this.getApiTest();
      },
      currentProject() {
        if (this.currentRow) {
          this.currentRow.cases = [];
        }
        this.getEnvironments();
      },
      refreshSign() {
        if (this.currentRow) {
          this.currentRow.cases = [];
        }
        this.getApiTest();
      },
      createCase() {
        this.sysAddition();
      }
    },
    created() {
      this.projectId = getCurrentProjectID();
      this.getEnvironments();
      if (this.createCase) {
        this.sysAddition();
      } else {
        this.getApiTest();
      }
    },
    methods: {
      open() {
        // this.apiCaseList = [];
        this.visible = true;
      },
      sysAddition() {
        let condition = {};
        condition.projectId = this.api.projectId;
        condition.apiDefinitionId = this.api.id;
        condition.priority = this.priorityValue;
        condition.name = this.name;
        this.$post("/api/testcase/list", condition, response => {
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
      handleCommand(e) {
        if (e === "run") {
          this.batchRun();
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
        this.loading = true;
        if (this.apiCaseList.length > 0) {
          this.apiCaseList.forEach(item => {
            if (item.type != "create") {
              item.request.name = item.id;
              item.request.useEnvironment = this.environment.id;
              this.runData.push(item.request);
            }
          })
          this.loading = true;
          /*触发执行操作*/
          this.reportId = getUUID().substring(0, 8);
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
        let condition = {};
        condition.projectId = this.api.projectId;
        condition.apiDefinitionId = this.api.id;
        condition.priority = this.priorityValue;
        condition.name = this.name;
        this.result = this.$post("/api/testcase/list", condition, response => {
          for (let index in response.data) {
            let test = response.data[index];
            test.request = JSON.parse(test.request);
          }
          this.apiCaseList = response.data;
          if (this.apiCaseList.length == 0) {
            this.addCase();
          }
        });
      },
      validate(row) {
        if (!row.name) {
          this.$warning(this.$t('api_test.input_name'));
          return true;
        }
      },
      saveTestCase(row) {
        if (this.validate(row)) {
          return;
        }
        let bodyFiles = this.getBodyUploadFiles(row);
        row.projectId = this.api.projectId;
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
      getEnvironments() {
        if (this.projectId) {
          this.$get('/api/environment/list/' + this.projectId, response => {
            this.environments = response.data;
            this.environments.forEach(environment => {
              parseEnvironment(environment);
            });
            let hasEnvironment = false;
            for (let i in this.environments) {
              if (this.environments[i].id === this.api.environmentId) {
                hasEnvironment = true;
                break;
              }
            }
            if (!hasEnvironment) {
              this.environment = undefined;
            }
          });
        } else {
          this.environment = undefined;
        }
      },
      openEnvironmentConfig() {
        if (!this.projectId) {
          this.$error(this.$t('api_test.select_project'));
          return;
        }
        this.$refs.environmentConfig.open(this.projectId);
      },
      environmentChange(value) {
        for (let i in this.environments) {
          if (this.environments[i].id === value) {
            this.environment = this.environments[i];
            break;
          }
        }
      },
      environmentConfigClose() {
        this.getEnvironments();
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
      caseChecked(row) {
        row.protocol = this.api.protocol;
        row.hashTree = [];
        if (this.checkedCases.has(row)) {
          this.checkedCases.delete(row);
        } else {
          this.checkedCases.add(row)
        }
        let arr = Array.from(this.checkedCases);
        this.currentRow.cases = arr;
      },
      handleClose() {
        this.visible = false;
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

  .el-card-btn {
    float: right;
    top: 20px;
    right: 0px;
    padding: 0;
    background: 0 0;
    border: none;
    outline: 0;
    cursor: pointer;
    font-size: 18px;
    margin-left: 30px;
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

  .variable-combine {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 10px;
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

  .environment-button {
    margin-left: 20px;
    padding: 7px;
  }

  .is-selected {
    background: #EFF7FF;
  }
</style>
