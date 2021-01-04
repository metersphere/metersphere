<template>
      <el-card style="margin-top: 5px" @click.native="selectTestCase(apiCase,$event)">
        <el-row>
          <el-col :span="6">
            <div class="el-step__icon is-text ms-api-col">
              <div class="el-step__icon-inner">{{index+1}}</div>
            </div>

            <label class="ms-api-label">{{$t('test_track.case.priority')}}</label>
            <el-select size="small" v-model="apiCase.priority" class="ms-api-select" @change="changePriority(apiCase)">
              <el-option v-for="grd in priorities" :key="grd.id" :label="grd.name" :value="grd.id"/>
            </el-select>
          </el-col>
          <el-col :span="10">
            <i class="icon el-icon-arrow-right" :class="{'is-active': apiCase.active}"
               @click="active(apiCase)"/>
            <el-input v-if="!apiCase.id || isShowInput" size="small" v-model="apiCase.name" :name="index" :key="index"
                      class="ms-api-header-select" style="width: 180px"
                      @blur="saveTestCase(apiCase)" placeholder="请输入用例名称"/>
            <span v-else>
                  {{apiCase.id ? apiCase.name:''}}
                  <i class="el-icon-edit" style="cursor:pointer" @click="showInput(apiCase)" v-tester/>
                </span>
            <div v-if="apiCase.id" style="color: #999999;font-size: 12px">
                  <span>
                    {{apiCase.createTime | timestampFormatDate }}
                    {{apiCase.createUser}} {{$t('api_test.definition.request.create_info')}}
                  </span>
              <span>
                    {{apiCase.updateTime | timestampFormatDate }}
                    {{apiCase.updateUser}} {{$t('api_test.definition.request.update_info')}}
                  </span>
            </div>
          </el-col>

          <el-col :span="4">
            <ms-tip-button @click="singleRun(apiCase)" :tip="$t('api_test.run')" icon="el-icon-video-play"
                           style="background-color: #409EFF;color: white" size="mini" :disabled="!apiCase.id" circle v-tester/>
            <ms-tip-button @click="copyCase(apiCase)" :tip="$t('commons.copy')" icon="el-icon-document-copy"
                           size="mini" :disabled="!apiCase.id || isCaseEdit" circle v-tester/>
            <ms-tip-button @click="deleteCase(index,apiCase)" :tip="$t('commons.delete')" icon="el-icon-delete"
                           size="mini" :disabled="!apiCase.id || isCaseEdit" circle v-tester/>
            <ms-api-extend-btns :is-case-edit="isCaseEdit" :row="apiCase" v-tester/>
          </el-col>

          <el-col :span="3">
            <el-link type="danger" v-if="apiCase.execResult && apiCase.execResult==='error'" @click="showExecResult(apiCase)">{{getResult(apiCase.execResult)}}</el-link>
            <el-link v-else-if="apiCase.execResult && apiCase.execResult==='success'" @click="showExecResult(apiCase)">{{getResult(apiCase.execResult)}}</el-link>
            <div v-else> {{getResult(apiCase.execResult)}}</div>

            <div v-if="apiCase.id" style="color: #999999;font-size: 12px">
              <span> {{apiCase.execTime | timestampFormatDate }}</span>
              {{apiCase.updateUser}}
            </div>
          </el-col>
        </el-row>
        <!-- 请求参数-->
        <el-collapse-transition>
          <div v-if="apiCase.active">
            <p class="tip">{{$t('api_test.definition.request.req_param')}} </p>

            <ms-api-request-form :is-read-only="isReadOnly" :headers="apiCase.request.headers " :request="apiCase.request" v-if="api.protocol==='HTTP'"/>
            <ms-tcp-basis-parameters :request="apiCase.request" v-if="api.protocol==='TCP'"/>
            <ms-sql-basis-parameters :request="apiCase.request" v-if="api.protocol==='SQL'"/>
            <ms-dubbo-basis-parameters :request="apiCase.request" v-if="api.protocol==='DUBBO'"/>
            <!-- 保存操作 -->
            <el-button type="primary" size="small" style="margin: 20px; float: right" @click="saveTestCase(apiCase)" v-tester>
              {{$t('commons.save')}}
            </el-button>
          </div>
        </el-collapse-transition>
      </el-card>
</template>

<script>
  import {getCurrentProjectID, getUUID} from "../../../../../../common/js/utils";
  import {PRIORITY, RESULT_MAP} from "../../model/JsonData";
  import MsTag from "../../../../common/components/MsTag";
  import MsTipButton from "../../../../common/components/MsTipButton";
  import MsApiRequestForm from "../request/http/ApiRequestForm";
  import ApiEnvironmentConfig from "../environment/ApiEnvironmentConfig";
  import MsApiAssertions from "../assertion/ApiAssertions";
  import MsSqlBasisParameters from "../request/database/BasisParameters";
  import MsTcpBasisParameters from "../request/tcp/BasisParameters";
  import MsDubboBasisParameters from "../request/dubbo/BasisParameters";
  import MsApiExtendBtns from "../reference/ApiExtendBtns";

    export default {
      name: "ApiCaseItem",
      components: {
        MsTag,
        MsTipButton,
        MsApiRequestForm,
        ApiEnvironmentConfig,
        MsApiAssertions,
        MsSqlBasisParameters,
        MsTcpBasisParameters,
        MsDubboBasisParameters,
        MsApiExtendBtns
    },
      data() {
        return {
          result: {},
          grades: [],
          environment: {},
          isReadOnly: false,
          selectedEvent: Object,
          priorities: PRIORITY,
          runData: [],
          reportId: "",
          checkedCases: new Set(),
          visible: false,
          condition: {},
          isShowInput: false
        }
      },
      props: {
        apiCase: {
          type: Object,
          default() {
            return {}
          }
        },
        index: {
          type: Number,
          default() {
            return 0
          }
        },
        api: {
          type: Object,
          default() {
            return {}
          }
        },
        isCaseEdit: Boolean,
      },
      watch: {
      },
      methods: {

        deleteCase(index, row) {
          this.$alert(this.$t('api_test.definition.request.delete_confirm') + ' ' + row.name + " ？", '', {
            confirmButtonText: this.$t('commons.confirm'),
            callback: (action) => {
              if (action === 'confirm') {
                this.$get('/api/testcase/delete/' + row.id, () => {
                  this.$success(this.$t('commons.delete_success'));
                  this.$emit('refresh');
                });
              }
            }
          });

        },
        singleRun(data) {
          this.$emit('singleRun', data);
        },
        copyCase(data) {
          let obj = {name: "copy_" + data.name, priority: data.priority, active: true, request: data.request};
          this.$emit('copyCase', obj);
        },

        selectTestCase(item, $event) {
          if (!item.id || !this.loaded) {
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
        changePriority(row) {
          if (row.id) {
            this.saveTestCase(row);
          }
        },
        saveTestCase(row) {
          this.isShowInput = false;
          if (this.validate(row)) {
            return;
          }
          let bodyFiles = this.getBodyUploadFiles(row);
          row.projectId = getCurrentProjectID();
          row.active = true;
          row.request.path = this.api.path;
          row.request.method = this.api.method;
          row.apiDefinitionId = row.apiDefinitionId || this.api.id;
          let url = "/api/testcase/create";
          if (row.id) {
            url = "/api/testcase/update";
          }
          this.$fileUpload(url, null, bodyFiles, row, () => {
            this.$success(this.$t('commons.save_success'));
            this.$emit('refresh');
          });
        },
        showInput(row) {
          // row.type = "create";
          this.isShowInput = true;
          row.active = true;
          this.active(row);
        },
        active(item) {
          item.active = !item.active;
        },
        getResult(data) {
          if (RESULT_MAP.get(data)) {
            return RESULT_MAP.get(data);
          } else {
            return RESULT_MAP.get("default");
          }
        },
        validate(row) {
          if (!row.name) {
            this.$warning(this.$t('api_test.input_name'));
            return true;
          }
        },
        showExecResult(data) {
          this.$emit('showExecResult', data);
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
