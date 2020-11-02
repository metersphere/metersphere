<template>
  <div v-loading="result.loading">
    <el-container>
      <el-header style="width: 100% ;padding: 0px">
        <el-card>
          <el-row>
            <el-col :span="3">
              <div class="variable-combine"> {{api.api_name}}</div>
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
              <div class="variable-combine">{{api.api_path}}</div>
            </el-col>
            <el-col :span="2">
              <div>{{$t('test_track.plan_view.case_count')}}：5</div>
            </el-col>
            <el-col :span="4">
              <div>
                <el-select size="small" :placeholder="$t('api_test.delimit.request.grade_info')" v-model="grdValue"
                           class="ms-api-header-select">
                  <el-option v-for="grd in grades" :key="grd.id" :label="grd.name" :value="grd.id"/>
                </el-select>
              </div>
            </el-col>
            <el-col :span="4">
              <div>
                <el-select size="small" class="ms-api-header-select" v-model="envValue"
                           :placeholder="$t('api_test.delimit.request.run_env')">
                  <el-option v-for="env in environments" :key="env.id" :label="env.name" :value="env.id"/>
                </el-select>
              </div>
            </el-col>
            <el-col :span="3">
              <div class="ms-api-header-select">
                <el-input size="small" :placeholder="$t('api_test.delimit.request.select_case')"></el-input>
              </div>
            </el-col>
            <el-col :span="1">
              <div class="ms-api-header-select">
                <el-button size="small" @click="createCase">+{{$t('api_test.delimit.request.case')}}</el-button>
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
      <el-main>
        <div v-for="(item,index) in apiCaseList" :key="index">
          <el-card style="margin-top: 10px">
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
                <i class="icon el-icon-arrow-right" :class="{'is-active': item.isActive}"
                   @click="active(item)"/>
                <el-input v-if="item.type==='create'" size="small" v-model="item.name" :name="index"
                          :key="index" class="ms-api-header-select"/>
                {{item.type!= 'create'? item.name : ''}}

                <div v-if="item.type!='create'" style="color: #999999;font-size: 12px">
                  {{item.create_time}}
                  {{item.create_user}} 创建
                  {{item.update_time}}
                  {{item.update_user}} 更新
                </div>
              </el-col>
              <el-col :span="4">
                <ms-tip-button @click="runCase" :tip="$t('api_test.run')" icon="el-icon-video-play"
                               style="background-color: #409EFF;color: white" size="mini" circle/>
                <ms-tip-button @click="copyCase(item)" :tip="$t('commons.copy')" icon="el-icon-document-copy"
                               size="mini" circle/>
                <ms-tip-button @click="deleteCase(index)" :tip="$t('commons.delete')" icon="el-icon-delete" size="mini"
                               circle/>
              </el-col>

              <el-col :span="4">
                <div v-if="item.type!='create'">{{getResult(item.execute_result)}}</div>
                <div v-if="item.type!='create'" style="color: #999999;font-size: 12px">{{item.update_time}}
                  {{item.update_user}}
                </div>
              </el-col>
            </el-row>
            <el-collapse-transition>
              <div v-show="item.isActive">
                <ms-api-request-form :debug-report-id="debugReportId" @runDebug="runDebug"
                                     :request="selected" :scenario="currentScenario" v-if="handleChange"/>

              </div>
            </el-collapse-transition>

          </el-card>
        </div>


        <el-button type="primary" size="small" style="margin-top: 20px; float: right">{{$t('commons.save')}}</el-button>

      </el-main>

    </el-container>
  </div>
</template>

<script>
  import MsTag from "../../../common/components/MsTag";
  import MsTipButton from "../../../common/components/MsTipButton";
  import MsApiRequestForm from "./request/ApiRequestForm";
  import {Request, Scenario} from "../model/ScenarioModel";

  export default {
    name: 'ApiCaseList',
    components: {
      MsTag,
      MsTipButton,
      MsApiRequestForm
    },
    props: {
      api: {
        type: Object
      },
    },
    data() {
      return {
        result: {},
        grades: [],
        environments: [],
        envValue: "",
        grdValue: "",
        value: "",
        priority: [
          {name: 'P0', id: 'P0'},
          {name: 'P1', id: 'P1'},
          {name: 'P2', id: 'P2'},
          {name: 'P3', id: 'P3'}
        ],
        apiCaseList: [],


        reportVisible: false,
        create: false,
        projects: [],
        change: false,
        isReadOnly: false,
        debugReportId: '',
        type: "",
        activeName: 0,
        selected: [Scenario, Request],
        currentScenario: {}

      }
    },

    watch: {
      // 初始化
      api() {
        let obj = {
          name: '测试用例',
          priority: 'P0',
          execute_result: 'PASS',
          create_time: '2020-11-1: 10:05',
          update_time: '2020-11-1: 10:05',
          create_user: '赵勇',
          isActive: false,
          update_user: '赵勇'
        };
        this.apiCaseList.push(obj);
      },
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
      apiCaseClose() {
        this.apiCaseList = [];
        this.$emit('apiCaseClose');
      },
      runCase() {
      },

      deleteCase(index) {
        this.apiCaseList.splice(index, 1);
      },
      copyCase(data) {
        let obj = {
          name: data.name,
          priority: data.priority,
          type: 'create'
        };
        this.apiCaseList.push(obj);
      },
      createCase() {
        let obj = {
          id: this.apiCaseList.size + 1,
          name: '',
          priority: 'P0',
          type: 'create',
          isActive: true,
        };
        this.apiCaseList.push(obj);
      },
      runDebug() {

      },
      handleChange(v) {
        return v != "";
      },
      active(item) {
        item.isActive = !item.isActive;
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
    width: 150px;
  }

  .el-card-btn {
    float: right;
    top: 20px;
    right: 20px;
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

  .ms-ap-collapse {
    padding-top: 0px;
    border: 0px;
  }

  /deep/ .el-collapse-item__header {
    border-bottom: 0px;
    margin-top: 0px;
    width: 300px;
    line-height: 0px;
    color: #666666;
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
</style>
