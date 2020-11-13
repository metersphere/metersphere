<template>
  <div>
    <el-container>
      <el-header style="width: 100% ;padding: 0px">
        <el-card>
          <el-row>
            <el-col :span="3">
              <div class="variable-combine"> {{api.name}}</div>
            </el-col>
            <el-col :span="1">
              <template>
                <div>
                  <ms-tag v-if="api.api_status == 'Prepare'" type="info"
                          :content="$t('test_track.plan.plan_status_prepare')"/>
                  <ms-tag v-if="api.api_status == 'Underway'" type="primary"
                          :content="$t('test_track.plan.plan_status_running')"/>
                  <ms-tag v-if="api.api_status == 'Completed'" type="success"
                          :content="$t('test_track.plan.plan_status_completed')"/>
                </div>
              </template>
            </el-col>
            <el-col :span="4">
              <div class="variable-combine">{{api.url}}</div>
            </el-col>
            <el-col :span="2">
              <div>{{$t('test_track.plan_view.case_count')}}：5</div>
            </el-col>
            <el-col :span="4">
              <div>
                <el-select size="small" :placeholder="$t('api_test.delimit.request.grade_info')" v-model="priorityValue"
                           class="ms-api-header-select" @change="getApiTest">
                  <el-option v-for="grd in priority" :key="grd.id" :label="grd.name" :value="grd.id"/>
                </el-select>
              </div>
            </el-col>
            <el-col :span="4">
              <div>
                <el-select :disabled="isReadOnly" v-model="envValue" size="small" class="ms-api-header-select"
                           :placeholder="$t('api_test.delimit.request.run_env')"
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
                <el-input size="small" :placeholder="$t('api_test.delimit.request.select_case')"
                          v-model="name" @blur="getApiTest"/>
              </div>
            </el-col>
            <el-col :span="2">
              <div class="ms-api-header-select">
                <el-button size="small" style="background-color: #783887;color: white" @click="createCase">
                  +{{$t('api_test.delimit.request.case')}}
                </el-button>
              </div>
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

      <!-- 用例部分 -->
      <el-main v-loading="loading">
        <div v-for="(item,index) in apiCaseList" :key="index">
          <el-card style="margin-top: 10px" @click.native="selectTestCase(item,$event)">
            <el-row>
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
                          @blur="saveTestCase(item)"
                />
                <span v-else>
                  {{item.type!= 'create' ? item.name:''}}
                  <i class="el-icon-edit" style="cursor:pointer" @click="showInput(item)"/>
                </span>
                <div v-if="item.type!='create'" style="color: #999999;font-size: 12px">
                  <span> {{item.createTime | timestampFormatDate }}</span> {{item.createUser}} 创建
                  <span> {{item.updateTime | timestampFormatDate }}</span> {{item.updateUser}} 更新
                </div>
              </el-col>
              <el-col :span="4">
                <ms-tip-button @click="runCase(item)" :tip="$t('api_test.run')" icon="el-icon-video-play"
                               style="background-color: #409EFF;color: white" size="mini" circle/>
                <ms-tip-button @click="copyCase(item)" :tip="$t('commons.copy')" icon="el-icon-document-copy"
                               size="mini" circle/>
                <ms-tip-button @click="deleteCase(index,item)" :tip="$t('commons.delete')" icon="el-icon-delete"
                               size="mini"
                               circle/>
              </el-col>

              <el-col :span="4">
                <div v-if="item.type!='create'">{{getResult(item.execute_result)}}</div>
                <div v-if="item.type!='create'" style="color: #999999;font-size: 12px">
                  <span> {{item.updateTime | timestampFormatDate }}</span>
                  {{item.updateUser}}
                </div>
              </el-col>
            </el-row>
            <!-- 请求参数-->
            <el-collapse-transition>
              <div v-if="item.active">
                <p class="tip">{{$t('api_test.delimit.request.req_param')}} </p>

                <ms-api-request-form :is-read-only="isReadOnly" :request="item.test.request"/>

                <p class="tip">{{$t('api_test.delimit.request.assertions_rule')}} </p>

                <ms-api-assertions :request="item.test.request" :is-read-only="isReadOnly"
                                   :assertions="item.test.request.assertions"/>

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
  </div>

</template>

<script>
  import MsTag from "../../../common/components/MsTag";
  import MsTipButton from "../../../common/components/MsTipButton";
  import MsApiRequestForm from "./request/ApiRequestForm";
  import {Test, RequestFactory} from "../model/ApiTestModel";
  import {downloadFile, getUUID} from "@/common/js/utils";
  import {parseEnvironment} from "../model/EnvironmentModel";
  import ApiEnvironmentConfig from "../../test/components/ApiEnvironmentConfig";
  import {PRIORITY} from "../model/JsonData";
  import MsApiAssertions from "./assertion/ApiAssertions";

  export default {
    name: 'ApiCaseList',
    components: {
      MsTag,
      MsTipButton,
      MsApiRequestForm,
      ApiEnvironmentConfig,
      MsApiAssertions
    },
    props: {
      api: {
        type: Object
      },
      loaded: Boolean,
      currentProject: {},
    },
    data() {
      return {
        grades: [],
        environments: [],
        environment: {},
        envValue: {},
        name: "",
        priorityValue: "",
        isReadOnly: false,
        selectedEvent: Object,
        priority: PRIORITY,
        apiCaseList: [],
        loading: false,
      }
    },

    watch: {
      // 初始化
      api() {
        this.getApiTest();
      },
      currentProject() {
        this.getEnvironments();
      }
    },
    created() {
      this.getApiTest();
      this.getEnvironments();
    },
    methods: {
      getResult(data) {
        if (data === 'PASS') {
          return '执行结果：通过';
        } else if (data === 'UN_PASS') {
          return '执行结果：未通过';
        } else {
          return '执行结果：未执行';
        }
      },
      showInput(row) {
        row.type = "create";
        row.active = true;
        this.active(row);
      },
      apiCaseClose() {
        this.apiCaseList = [];
        this.$emit('apiCaseClose');
      },
      getReport(id) {
        let url = "/api/delimit/report/get/" + id + "/run";
        this.$get(url, response => {
          if (response.data) {
            this.loading = false;
            this.$success(this.$t('schedule.event_success'));
            this.$emit('refresh');
          } else {
            setTimeout(this.getReport, 2000)
          }
        });
      },
      runCase(row) {
        if (!this.environment) {
          this.$warning(this.$t('api_test.environment.select_environment'));
          return;
        }
        row.request = row.test.request;
        let url = "/api/delimit/run";
        let bodyFiles = this.getBodyUploadFiles(row);
        let env = this.environment.config.httpConfig.socket ? (this.environment.config.httpConfig.protocol + '://' + this.environment.config.httpConfig.socket) : '';
        if (env.endsWith("/")) {
          env = env.substr(0, env.length - 1);
        }
        let sendUrl = this.api.url;
        if (!sendUrl.startsWith("/")) {
          sendUrl = "/" + sendUrl;
        }
        row.test.request.url = env + sendUrl;
        row.test.request.path = this.api.path;
        row.test.request.name = row.id;
        row.test.request.connectTimeout = "6000";
        row.test.request.responseTimeout = "0";
        row.reportId = "run";
        this.loading = true;
        let jmx = row.test.toJMX();
        let blob = new Blob([jmx.xml], {type: "application/octet-stream"});
        let file = new File([blob], jmx.name);
        this.$fileUpload(url, file, bodyFiles, row, response => {
          this.getReport(row.id);
        }, erro => {
          this.loading = false;
        });
      },
      deleteCase(index, row) {
        this.$get('/api/testcase/delete/' + row.id, () => {
          this.$success(this.$t('commons.delete_success'));
          this.apiCaseList.splice(index, 1);
        });
      },
      copyCase(data) {
        let obj = {
          name: data.name,
          priority: data.priority,
          type: 'create',
          active: false,
          test: data.test,
        };
        this.apiCaseList.unshift(obj);
      },
      createCase(row) {
        let obj = {
          name: '',
          priority: 'P0',
          type: 'create',
          active: false,
        };
        let request = {};
        if (row) {
          request = row.request;
          obj.apiDelimitId = row.apiDelimitId;
        } else {
          request: new RequestFactory(JSON.parse(this.api.request))
        }
        obj.test = new Test({request: request});
        this.apiCaseList.unshift(obj);
      },
      active(item) {
        item.active = !item.active;
      },
      getBodyUploadFiles(row) {
        let bodyUploadFiles = [];
        row.bodyUploadIds = [];
        let request = row.test.request;
        if (request.body) {
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
        }
        return bodyUploadFiles;
      },
      getApiTest() {
        let condition = {};
        condition.projectId = this.api.projectId;
        condition.apiDelimitId = this.api.id;
        condition.priority = this.priorityValue;
        condition.name = this.name;
        this.$post("/api/testcase/list", condition, response => {
          for (let index in response.data) {
            let test = response.data[index];
            test.test = new Test({request: new RequestFactory(JSON.parse(test.request))});
          }
          this.apiCaseList = response.data;

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
        row.test.request.url = this.api.url;
        row.test.request.path = this.api.path;
        row.projectId = this.api.projectId;
        row.apiDelimitId = row.apiDelimitId || this.api.id;
        row.request = row.test.request;
        let jmx = row.test.toJMX();
        let blob = new Blob([jmx.xml], {type: "application/octet-stream"});
        let file = new File([blob], jmx.name);
        let url = "/api/testcase/create";
        if (row.id) {
          url = "/api/testcase/update";
        }
        this.$fileUpload(url, file, bodyFiles, row, () => {
          this.$success(this.$t('commons.save_success'));
          this.getApiTest();
        });
      },
      getEnvironments() {
        if (this.currentProject) {
          this.$get('/api/environment/list/' + this.currentProject.id, response => {
            this.environments = response.data;
            this.environments.forEach(environment => {
              parseEnvironment(environment);
            });
            let hasEnvironment = false;
            for (let i in this.environments) {
              if (this.environments[i].id === this.api.environmentId) {
                this.environment = this.environments[i];
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
        if (!this.currentProject) {
          this.$error(this.$t('api_test.select_project'));
          return;
        }
        this.$refs.environmentConfig.open(this.currentProject.id);
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
    font-size: 14px;
    width: 100%;
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
