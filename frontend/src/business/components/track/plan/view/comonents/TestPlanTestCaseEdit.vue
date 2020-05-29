<template>

  <el-drawer
    :before-close="handleClose"
    :visible.sync="showDialog"
    :with-header="false"
    :modal-append-to-body="false"
    size="100%"
    ref="drawer"
    v-loading="result.loading">

      <template v-slot:default="scope">
        <div class="container">

          <el-scrollbar>

            <el-header>

              <el-row type="flex" class="head-bar">

                <el-col :span="12">
                  <el-button plain size="mini"
                             icon="el-icon-back"
                             @click="cancel">{{$t('test_track.return')}}</el-button>
                </el-col>

                <el-col :span="12" class="head-right">
                <span class="head-right-tip" v-if="index + 1 == testCases.length">
                  {{$t('test_track.plan_view.pre_case')}} : {{testCases[index - 1] ? testCases[index - 1].name : ''}}
                </span>
                  <span class="head-right-tip" v-if="index + 1 != testCases.length">
                  {{$t('test_track.plan_view.next_case')}} : {{testCases[index + 1] ? testCases[index + 1].name : ''}}
                </span>

                  <el-button plain size="mini" icon="el-icon-arrow-up"
                             :disabled="index + 1 <= 1"
                             @click="handlePre()"/>
                  <span>  {{index + 1}}/{{testCases.length}} </span>
                  <el-button plain size="mini" icon="el-icon-arrow-down"
                             :disabled="index + 1 >= testCases.length"
                             @click="handleNext()"/>
                  <el-divider direction="vertical"></el-divider>

                  <el-button type="primary" size="mini" @click="saveCase(false)">{{$t('test_track.save')}}</el-button>
                </el-col>

              </el-row>

              <el-row style="margin-top: 0px;">
                <el-col>
                  <el-divider content-position="left">{{testCase.name}}</el-divider>
                </el-col>
              </el-row>

            </el-header>

            <div class="case_container">
              <el-row >
                <el-col :span="4" :offset="1">
                  <span class="cast_label">{{$t('test_track.case.priority')}}：</span>
                  <span class="cast_item">{{testCase.priority}}</span>
                </el-col>
                <el-col :span="5">
                  <span class="cast_label">{{$t('test_track.case.case_type')}}：</span>
                  <span class="cast_item" v-if="testCase.type == 'functional'">{{$t('commons.functional')}}</span>
                  <span class="cast_item" v-if="testCase.type == 'performance'">{{$t('commons.performance')}}</span>
                  <span class="cast_item" v-if="testCase.type == 'api'">{{$t('commons.api')}}</span>
                </el-col>
                <el-col :span="13">
                  <test-plan-test-case-status-button class="status-button"
                                                     @statusChange="statusChange"
                                                     :status="testCase.status"/>
                </el-col>
              </el-row>

              <el-row>
                <el-col :span="4" :offset="1">
                  <span class="cast_label">{{$t('test_track.case.method')}}：</span>
                  <span v-if="testCase.method == 'manual'">{{$t('test_track.case.manual')}}</span>
                  <span v-if="testCase.method == 'auto'">{{$t('test_track.case.auto')}}</span>
                </el-col>
                <el-col :span="5">
                  <span class="cast_label">{{$t('test_track.case.module')}}：</span>
                  <span class="cast_item">{{testCase.nodePath}}</span>
                </el-col>
              </el-row>

              <el-row v-if="testCase.method == 'auto' && testCase.testId">
                <el-col class="test-detail" :span="20" :offset="1">
                    <el-tabs type="border-card">
                      <el-tab-pane :label="$t('test_track.plan_view.test_detail')">
                        <api-test-detail v-if="testCase.type == 'api'" @runTest="apiTestRun" :id="testCase.testId" ref="apiTestDetail"/>
                        <performance-test-detail v-if="testCase.type == 'performance'" :id="testCase.testId" ref="performanceTestDetail"/>
                      </el-tab-pane>
                      <el-tab-pane :label="$t('test_track.plan_view.test_result')">
                        <api-test-result :report-id="testCase.reportId" v-if=" testCase.type == 'api'" ref="apiTestResult"/>
                        <performance-test-result :report-id="testCase.reportId" v-if="testCase.type == 'performance'"/>
                      </el-tab-pane>
                    </el-tabs>
                </el-col>
              </el-row>

              <el-row v-if="testCase.method && testCase.method != 'auto'">
                <el-col :span="20" :offset="1">
                  <div>
                    <span class="cast_label">{{$t('test_track.case.steps')}}：</span>
                  </div>
                  <el-table
                    :data="testCase.steptResults"
                    class="tb-edit"
                    size="mini"
                    :border="true"
                    :default-sort = "{prop: 'num', order: 'ascending'}"
                    highlight-current-row>
                    <el-table-column :label="$t('test_track.case.number')" prop="num" min-width="5%"></el-table-column>
                    <el-table-column :label="$t('test_track.case.step_desc')" prop="desc" min-width="29%">
                      <template v-slot:default="scope">
                        <span>{{scope.row.desc}}</span>
                      </template>
                    </el-table-column>
                    <el-table-column :label="$t('test_track.case.expected_results')" prop="result" min-width="28%">
                      <template v-slot:default="scope">
                        <span>{{scope.row.result}}</span>
                      </template>
                    </el-table-column>
                    <el-table-column :label="$t('test_track.plan_view.actual_result')" min-width="29%">
                      <template v-slot:default="scope">
                        <el-input
                          size="mini"
                          type="textarea"
                          :autosize="{ minRows: 2, maxRows: 4}"
                          :rows="2"
                          v-model="scope.row.actualResult"
                          :placeholder="$t('commons.input_content')"
                          clearable></el-input>
                        <span>{{scope.row.actualResult}}</span>
                      </template>
                    </el-table-column>
                    <el-table-column :label="$t('test_track.plan_view.step_result')" min-width="9%">
                      <template v-slot:default="scope">
                        <el-select
                          v-model="scope.row.executeResult"
                          size="mini">
                          <el-option :label="$t('test_track.plan_view.pass')" value="Pass" style="color: #7ebf50;"></el-option>
                          <el-option :label="$t('test_track.plan_view.failure')" value="Failure" style="color: #e57471;"></el-option>
                          <el-option :label="$t('test_track.plan_view.blocking')" value="Blocking" style="color: #dda451;"></el-option>
                          <el-option :label="$t('test_track.plan_view.skip')" value="Skip" style="color: #919399;"></el-option>
                        </el-select>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-col>
              </el-row>

              <el-row v-if="testCase.issues">
                <el-col :span="5" :offset="1">
                  <el-switch
                    v-model="testCase.issues.hasIssues"
                    @change="issuesChange"
                    :active-text="$t('test_track.plan_view.submit_issues')">
                  </el-switch>
                </el-col>
              </el-row>

              <el-row v-if="testCase.issues && testCase.issues.hasIssues">
                <el-col :span="20" :offset="1" class="step-edit">
                  <ckeditor :editor="editor" v-model="testCase.issues.content"/>
                </el-col>
              </el-row>

              <el-row>
                <el-col :span="15" :offset="1">
                  <div>
                    <span class="cast_label">{{$t('commons.remark')}}：</span>
                    <span v-if="testCase.remark == null || testCase.remark == ''" style="color: darkgrey">{{$t('commons.not_filled')}}</span>
                  </div>
                  <div>
                    <el-input :rows="3"
                              type="textarea"
                              v-if="testCase.remark"
                              disabled
                              v-model="testCase.remark"></el-input>
                  </div>
                </el-col>
              </el-row>
            </div>

          </el-scrollbar>

        </div>

      </template>

  </el-drawer>


</template>

<script>
  import TestPlanTestCaseStatusButton from '../../common/TestPlanTestCaseStatusButton';
  import ClassicEditor from '@ckeditor/ckeditor5-build-classic';
  import ApiTestDetail from "./test/ApiTestDetail";
  import ApiTestResult from "./test/ApiTestResult";
  import PerformanceTestDetail from "./test/PerformanceTestDetail";
  import PerformanceTestResult from "./test/PerformanceTestResult";

  export default {
    name: "TestPlanTestCaseEdit",
    components: {
      PerformanceTestResult,
      PerformanceTestDetail,
      ApiTestResult,
      ApiTestDetail,
      TestPlanTestCaseStatusButton},
    data() {
      return {
        result: {},
        showDialog: false,
        testCase: {},
        index: 0,
        testCases: [],
        editor: ClassicEditor,
        test: {}
      };
    },
    props: {
      total: {
        type: Number
      },
      searchParam: {
        type: Object
      }
    },
    methods: {
      listenGoBack() {
        //监听浏览器返回操作，关闭该对话框
        if (window.history && window.history.pushState) {
          history.pushState(null, null, document.URL);
          window.addEventListener('popstate', this.goBack, false);
        }
      },
      goBack() {
        this.handleClose();
      },
      handleClose() {
        //移除监听，防止监听其他页面
        window.removeEventListener('popstate', this.goBack, false);
        this.showDialog = false;
      },
      cancel() {
        this.handleClose();
        this.$emit('refreshTable');
      },
      statusChange(status) {
        this.testCase.status = status;
      },
      saveCase() {
        let param = {};
        param.id = this.testCase.id;
        param.status = this.testCase.status;
        param.results = [];

        for (let i = 0; i < this.testCase.steptResults.length; i++){
          let result = {};
          result.actualResult = this.testCase.steptResults[i].actualResult;
          result.executeResult = this.testCase.steptResults[i].executeResult;
          if (result.actualResult && result.actualResult.length > 300) {
            this.$warning(this.$t('test_track.plan_view.actual_result')
              + this.$t('test_track.length_less_than') + '300');
            return;
          }
          param.results.push(result);
        }

        param.results = JSON.stringify(param.results);
        param.issues = JSON.stringify(this.testCase.issues);
        this.$post('/test/plan/case/edit', param, () => {
          this.$success(this.$t('commons.save_success'));
          this.setPlanStatus(this.testCase.planId);
        });
      },
      handleNext() {
        this.index++;
        this.getTestCase(this.index);
      },
      handlePre() {
        this.index--;
        this.getTestCase(this.index);
      },
      getTestCase(index) {
        let testCase = this.testCases[index];
        let item = {};
        Object.assign(item, testCase);
        item.results = JSON.parse(item.results);
        item.steps = JSON.parse(item.steps);
        if (item.issues) {
          item.issues = JSON.parse(item.issues);
        } else {
          item.issues = {};
          item.issues.hasIssues = false;
        }
        item.steptResults = [];
        for (let i = 0; i < item.steps.length; i++){
          if(item.results[i]){
            item.steps[i].actualResult = item.results[i].actualResult;
            item.steps[i].executeResult = item.results[i].executeResult;
          }
          item.steptResults.push(item.steps[i]);
        }
        this.testCase = item;
      },
      openTestCaseEdit(testCase) {
        this.showDialog = true;
        this.listenGoBack();
        this.initData(testCase);
      },
      initTest() {
        this.$nextTick(() => {
          if (this.testCase.method == 'auto') {
            if (this.$refs.apiTestDetail && this.testCase.type == 'api') {
              this.$refs.apiTestDetail.init();
            } else if(this.testCase.type == 'performance') {
              this.$refs.performanceTestDetail.init();
            }
          }
        });
      },
      apiTestRun(reportId) {
        this.testCase.reportId = reportId;
        this.saveReport(reportId);
      },
      saveReport(reportId) {
        this.$post('/test/plan/case/edit', {id: this.testCase.id, reportId: reportId});
      },
      initData(testCase) {
        this.result = this.$post('/test/plan/case/list/all', this.searchParam, response => {
          this.testCases = response.data;
          for (let i = 0; i < this.testCases.length; i++) {
            if (this.testCases[i].id === testCase.id) {
              this.index = i;
              this.getTestCase(i);
              this.getRelatedTest();
              this.initTest();
            }
          }
        });
      },
      getRelatedTest() {
        if (this.testCase.method == 'auto' && this.testCase.testId) {
          this.$get('/' + this.testCase.type + '/get/' + this.testCase.testId, response => {
            this.test = response.data;
          });
        }
      },
      issuesChange() {
       if (this.testCase.issues.hasIssues) {
          let desc = this.addPLabel('[' + this.$t('test_track.plan_view.operate_step') + ']');
          let result = this.addPLabel('[' + this.$t('test_track.case.expected_results') + ']');
          let executeResult = this.addPLabel('[' + this.$t('test_track.plan_view.actual_result') + ']');
          this.testCase.steps.forEach(step => {
            let stepPrefix = this.$t('test_track.plan_view.step') + step.num + ':';
            desc += this.addPLabel(stepPrefix + (step.desc == undefined ? '' : step.desc));
            result += this.addPLabel(stepPrefix + (step.result == undefined ? '' : step.result));
            executeResult += this.addPLabel(stepPrefix + (step.executeResult == undefined ? '' : step.executeResult));
          });
          this.testCase.issues.content = desc + this.addPLabel('') + result + this.addPLabel('') + executeResult + this.addPLabel('');
        }
      },
      addPLabel(str) {
        return "<p>" + str + "</p>";
      },
      setPlanStatus(planId) {
        this.$post('/test/plan/edit/status/' + planId);
      }
    }
  }
</script>

<style scoped>


  .tb-edit .el-textarea {
    display: none;
  }
  .tb-edit .current-row .el-textarea {
    display: block;
  }
  .tb-edit .current-row .el-textarea+span {
    display: none;
  }

  .cast_label {
    color: dimgray;
  }

  .status-button {
    padding-left: 4%;
  }

  .head-right {
    text-align: right;
  }

  .el-col:not(.test-detail){
    line-height: 50px;
  }

  .step-edit >>> p {
    line-height: 16px;
  }

  .status-button {
    float: right;
  }

  .head-right-tip {
    color: darkgrey;
  }

  .el-scrollbar {
    height: 100%;
  }

  .container {
    height: 100vh;
  }

  .case_container > .el-row{
    margin-top: 1%;
  }

  .el-switch >>> .el-switch__label {
    color: dimgray;
  }

  .el-switch >>> .el-switch__label.is-active {
    color: #409EFF;
  }

</style>
