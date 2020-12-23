<template>
  <div>
    <el-container style="padding-bottom: 300px">
      <el-header style="width: 100% ;padding: 0px">
        <el-card>
          <el-row>
            <el-col :span="2" class="ms-api-col">
              <el-checkbox :indeterminate="isIndeterminate" v-model="checkAll" @change="handleCheckAllChange">全选</el-checkbox>
            </el-col>
            <el-col :span="api.protocol==='HTTP'? 3:6" class="ms-api-col">
              <div class="variable-combine"> {{api.name}}</div>
            </el-col>
            <el-col :span="api.protocol==='HTTP'? 1:3" class="ms-api-col">
              <el-tag size="mini" :style="{'background-color': getColor(true, api.method), border: getColor(true, api.method)}" class="api-el-tag">
                {{ api.method}}
              </el-tag>
            </el-col>
            <el-col :span="api.protocol==='HTTP'? 5:0" class="ms-api-col">
              <div class="variable-combine" style="margin-left: 10px">{{api.path ===null ? " " : api.path}}</div>
            </el-col>
            <el-col :span="2" class="ms-api-col">
              <div>{{$t('test_track.plan_view.case_count')}}：{{apiCaseList.length}}</div>
            </el-col>
            <el-col :span="4">
              <div>
                <el-select size="small" :placeholder="$t('api_test.definition.request.grade_info')" v-model="priorityValue"
                           class="ms-api-header-select" @change="getApiTest">
                  <el-option v-for="grd in priority" :key="grd.id" :label="grd.name" :value="grd.id"/>
                </el-select>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="ms-api-header-select">
                <el-input size="small" :placeholder="$t('api_test.definition.request.select_case')"
                          v-model="name" @blur="getApiTest"/>
              </div>
            </el-col>

            <el-col :span="2">
              <button type="button" aria-label="Close" class="el-card-btn" @click="apiCaseClose()"><i
                class="el-dialog__close el-icon el-icon-close"></i></button>
            </el-col>

          </el-row>
        </el-card>
      </el-header>

      <!-- 用例部分 -->
      <el-main v-loading="loading" style="overflow: auto;padding: 5px 10px 10px">
        <el-checkbox-group v-model="checkedIndex" @change="handleCheckedChange">
          <div v-for="(item,index) in apiCaseList" :key="index">
            <el-card style="margin-top: 5px">
              <el-row>
                <el-col :span="1" class="ms-api-col">
                  <el-checkbox :key="item.id" :label="index+1"/>
                </el-col>
                <el-col :span="3">
                  <el-select size="small" v-model="item.priority" class="ms-api-select">
                    <el-option v-for="grd in priority" :key="grd.id" :label="grd.name" :value="grd.id"/>
                  </el-select>
                </el-col>
                <el-col :span="14">
                  <span v-if="item.type!='create'" style="color: #303132;font-size: 13px">
                    <i class="icon el-icon-arrow-right" :class="{'is-active': item.active}"
                       @click="active(item)"/>
                    {{item.name}}
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

                <el-col :span="6">
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
        </el-checkbox-group>

      </el-main>

    </el-container>

  </div>

</template>
<script>
  import MsTag from "../../../../common/components/MsTag";
  import MsTipButton from "../../../../common/components/MsTipButton";
  import MsApiRequestForm from "../../../definition/components/request/http/ApiRequestForm";
  import {downloadFile, getUUID, getCurrentProjectID} from "@/common/js/utils";
  import {PRIORITY, RESULT_MAP} from "../../../definition/model/JsonData";
  import MsApiAssertions from "../../../definition/components/assertion/ApiAssertions";
  import MsSqlBasisParameters from "../../../definition/components/request/database/BasisParameters";
  import MsTcpBasisParameters from "../../../definition/components/request/tcp/BasisParameters";
  import MsDubboBasisParameters from "../../../definition/components/request/dubbo/BasisParameters";
  import MsApiExtendBtns from "../../../definition/components/reference/ApiExtendBtns";
  import {API_METHOD_COLOUR} from "../../../definition/model/JsonData";

  export default {
    name: 'ApiCaseList',
    components: {
      MsTag,
      MsTipButton,
      MsApiRequestForm,
      MsApiAssertions,
      MsSqlBasisParameters,
      MsTcpBasisParameters,
      MsDubboBasisParameters,
      MsApiExtendBtns
    },
    props: {
      api: {
        type: Object
      },
      visible: {
        type: String,
      },
      loaded: Boolean,
      refreshSign: String,
      currentRow: Object,
    },
    data() {
      return {
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
        methodColorMap: new Map(API_METHOD_COLOUR),

        checkAll: false,
        checkedIndex: [],
        isIndeterminate: true

      }
    },
    watch: {
      // 初始化
      api() {
        this.getApiTest();
      },
      visible() {
        this.getApiTest();
      }
    },
    created() {
      this.projectId = getCurrentProjectID();
      this.getApiTest();
    },
    methods: {
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
        this.$emit('apiCaseClose');
      },

      active(item) {
        item.active = !item.active;
      },
      getApiTest() {
        if (this.api) {
          this.loading = true;
          if (this.currentRow) {
            this.currentRow.cases = [];
          }
          let condition = {};
          condition.projectId = this.projectId;
          condition.apiDefinitionId = this.api.id;
          condition.priority = this.priorityValue;
          condition.name = this.name;
          this.$post("/api/testcase/list", condition, response => {
            for (let index in response.data) {
              let test = response.data[index];
              test.checked = false;
              test.request = JSON.parse(test.request);
            }
            this.loading = false;
            this.apiCaseList = response.data;
          });
        }
      },
      validate(row) {
        if (!row.name) {
          this.$warning(this.$t('api_test.input_name'));
          return true;
        }
      },

      getColor(enable, method) {
        if (enable) {
          return this.methodColorMap.get(method);
        }
      },
      handleCheckAllChange(val) {
        this.currentRow.cases = [];
        if (val) {
          let index = 1;
          this.apiCaseList.forEach(item => {
            this.checkedIndex.push(index);
            item.protocol = this.api.protocol;
            item.hashTree = [];
            this.currentRow.cases.push(item)
            index++;
          })
        } else {
          this.checkedIndex = [];
        }
        this.isIndeterminate = false;
      },
      handleCheckedChange(value) {
        let checkedCount = value.length;
        this.checkAll = checkedCount === this.apiCaseList.length;
        this.isIndeterminate = checkedCount > 0 && checkedCount < this.apiCaseList.length;
        this.currentRow.cases = [];
        value.forEach(i => {
          let index = i - 1;
          let item = this.apiCaseList[index];
          item.protocol = this.api.protocol;
          item.hashTree = [];
          this.currentRow.cases.push(item);
        })
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
    /*color: #CCCCCC;*/
  }

  .ms-api-col {
    margin-top: 5px;
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

  .api-el-tag {
    color: white;
  }

  .is-selected {
    background: #EFF7FF;
  }
</style>
