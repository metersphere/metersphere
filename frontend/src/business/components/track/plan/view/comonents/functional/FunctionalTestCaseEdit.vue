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
      <el-row :gutter="10">
        <div class="container">
          <el-col :span="17">
            <el-card>
              <el-scrollbar>

                <el-header>
                  <el-row type="flex" class="head-bar">

                    <el-col :span="4">
                      <el-button plain size="mini"
                                 icon="el-icon-back"
                                 @click="cancel">{{ $t('test_track.return') }}
                      </el-button>
                    </el-col>

                    <el-col class="head-right" :span="20">
                      <ms-previous-next-button :index="index" @pre="handlePre" @next="saveCase(true, true)" :list="testCases"/>
                      <el-button class="save-btn" type="primary" size="mini" :disabled="isReadOnly" @click="saveCase(true)">
                        {{$t('test_track.save')}} & {{$t('test_track.next')}}
                      </el-button>
                    </el-col>

                  </el-row>

                  <el-row class="head-bar">
                    <el-col>
                      <el-divider content-position="left">
                        <el-button class="test-case-name" type="text" @click="openTestTestCase(testCase)">{{ testCase.name }}</el-button>
                      </el-divider>
                    </el-col>
                  </el-row>
                </el-header>

                <div class="case_container">

                  <el-form :model="testCase">

                    <el-row>
                      <el-col :span="7">
                        <el-form-item :label="$t('test_track.case.module')" prop="nodePath" :label-width="formLabelWidth">
                          {{testCase.nodePath}}
                        </el-form-item >
                      </el-col>
                      <el-col :span="7">
                        <el-form-item :label="$t('test_track.plan.plan_project')" prop="projectName" :label-width="formLabelWidth">
                          {{testCase.projectName}}
                        </el-form-item >
                      </el-col>
                      <el-col :span="10">
                        <test-plan-test-case-status-button class="status-button"
                                                           @statusChange="statusChange"
                                                           :is-read-only="isReadOnly"
                                                           :status="testCase.status"/>
                      </el-col>
                    </el-row>

                    <el-form ref="customFieldForm"
                             v-if="isCustomFiledActive"
                             class="case-form">
                      <el-row>
                        <el-col :span="7" v-for="(item, index) in testCaseTemplate.customFields" :key="index">
                          <el-form-item :label-width="formLabelWidth" :label="item.system ? $t(systemNameMap[item.name]) : item.name" :prop="item.name">
                            <custom-filed-component :disabled="true" :data="item" :form="{}" prop="defaultValue"/>
                          </el-form-item>
                        </el-col>
                      </el-row>
                    </el-form>

                    <form-rich-text-item :label-width="formLabelWidth" :disabled="true" :title="$t('test_track.case.prerequisite')" :data="testCase" prop="prerequisite"/>
                    <step-change-item :disable="true" :label-width="formLabelWidth" :form="testCase"/>
                    <test-plan-case-step-results-item :label-width="formLabelWidth" :is-read-only="isReadOnly" v-if="testCase.stepModel === 'STEP'" :test-case="testCase"/>
                    <form-rich-text-item :label-width="formLabelWidth" v-if="testCase.stepModel === 'TEXT'" :disabled="true" :title="$t('test_track.case.step_desc')" :data="testCase" prop="stepDescription"/>
                    <form-rich-text-item :label-width="formLabelWidth" v-if="testCase.stepModel === 'TEXT'" :disabled="true" :title="$t('test_track.case.expected_results')" :data="testCase" prop="expectedResult"/>
                    <form-rich-text-item :label-width="formLabelWidth" v-if="testCase.stepModel === 'TEXT'" :title="$t('test_track.plan_view.actual_result')" :data="testCase" prop="actualResult"/>

                    <test-case-edit-other-info :plan-id="testCase.planId" v-if="otherInfoActive" @openTest="openTest" :read-only="true" :is-test-plan="true" :project-id="testCase.projectId" :form="testCase" :case-id="testCase.caseId" ref="otherInfo"/>
                  </el-form>
                </div>

              </el-scrollbar>
            </el-card>
          </el-col>
          <el-col :span="7">
            <el-card class="comment-card">
              <template slot="header">
                <span style="font-size: 15px; color: #1E90FF">{{ $t('test_track.review.comment') }}</span>
                <i class="el-icon-refresh" @click="getComments(testCase)"
                   style="margin-left:10px;font-size: 14px; cursor: pointer"/>
              </template>
              <review-comment :comments="comments" :case-id="testCase.caseId" :review-id="testCase.reviewId"
                              @getComments="getComments"/>
            </el-card>
            <!--            <case-comment :case-id="testCase ? testCase.caseId : ''" class="comment-card"/>-->
          </el-col>


        </div>
      </el-row>


    </template>

  </el-drawer>


</template>

<script>
import TestPlanTestCaseStatusButton from '../../../common/TestPlanTestCaseStatusButton';
import ClassicEditor from '@ckeditor/ckeditor5-build-classic';
import ApiTestDetail from "../test/ApiTestDetail";
import ApiTestResult from "../test/ApiTestResult";
import PerformanceTestDetail from "../test/PerformanceTestDetail";
import PerformanceTestResult from "../test/PerformanceTestResult";
import {getCurrentProjectID, getUUID, hasPermission, listenGoBack, removeGoBackListener} from "@/common/js/utils";
import TestCaseAttachment from "@/business/components/track/case/components/TestCaseAttachment";
import CaseComment from "@/business/components/track/case/components/CaseComment";
import MsPreviousNextButton from "../../../../../common/components/MsPreviousNextButton";
import ReviewComment from "@/business/components/track/review/commom/ReviewComment";
import {buildTestCaseOldFields, getTemplate, parseCustomField} from "@/common/js/custom_field";
import FormRichTextItem from "@/business/components/track/case/components/FormRichTextItem";
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import TestCaseEditOtherInfo from "@/business/components/track/case/components/TestCaseEditOtherInfo";
import CustomFiledComponent from "@/business/components/settings/workspace/template/CustomFiledComponent";
import {SYSTEM_FIELD_NAME_MAP} from "@/common/js/table-constants";
import IssueDescriptionTableItem from "@/business/components/track/issue/IssueDescriptionTableItem";
import StepChangeItem from "@/business/components/track/case/components/StepChangeItem";
import TestCaseStepItem from "@/business/components/track/case/components/TestCaseStepItem";
import TestPlanCaseStepResultsItem from "@/business/components/track/plan/view/comonents/functional/TestPlanCaseStepResultsItem";

export default {
  name: "FunctionalTestCaseEdit",
  components: {
    TestPlanCaseStepResultsItem,
    TestCaseStepItem,
    StepChangeItem,
    IssueDescriptionTableItem,
    CustomFiledComponent,
    TestCaseEditOtherInfo,
    MsFormDivider,
    FormRichTextItem,
    ReviewComment,
    MsPreviousNextButton,
    CaseComment,
    PerformanceTestResult,
    PerformanceTestDetail,
    ApiTestResult,
    ApiTestDetail,
    TestPlanTestCaseStatusButton,
    TestCaseAttachment
  },
  data() {
    return {
      result: {},
      showDialog: false,
      testCase: {},
      index: 0,
      testCases: [],
      editor: ClassicEditor,
      editorConfig: {
        toolbar: ['heading', '|', 'bold', 'italic', 'link', 'bulletedList', 'numberedList', 'blockQuote', 'insertTable', '|', 'undo', 'redo'],
      },
      readConfig: {toolbar: []},
      test: {},
      activeTab: 'detail',
      users: [],
      Builds: [],
      zentaoBuilds: [],
      zentaoUsers: [],
      zentaoAssigned: "",
      hasTapdId: false,
      hasZentaoId: false,
      tableData: [],
      comments: [],
      testCaseTemplate: {},
      formLabelWidth: "100px",
      isCustomFiledActive: false,
      otherInfoActive: true,
      isReadOnly: false,
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
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
    systemNameMap() {
      return SYSTEM_FIELD_NAME_MAP;
    }
  },
  methods: {
    getComments(testCase) {
      let id = '';
      if (testCase) {
        id = testCase.caseId;
      } else {
        id = this.testCase.caseId;
      }
      this.result = this.$get('/test/case/comment/list/' + id, res => {
        this.comments = res.data;
      })
    },
    handleClose() {
      removeGoBackListener(this.handleClose);
      this.showDialog = false;
      this.searchParam.status = null;
      this.$emit('update:search-param', this.searchParam);
    },
    cancel() {
      this.handleClose();
      this.$emit('refreshTable');
    },
    statusChange(status) {
      this.testCase.status = status;
      this.saveCase(true);
    },
    getOption(param) {
      let formData = new FormData();
      let url = '/test/case/edit/testPlan';

      if (this.$refs.otherInfo && this.$refs.otherInfo.uploadList) {
        this.$refs.otherInfo.uploadList.forEach(f => {
          formData.append("file", f);
        });
      }

      if (this.$refs.otherInfo && this.$refs.otherInfo.fileList) {
        param.updatedFileList = this.$refs.otherInfo.fileList;
      } else {
        param.fileIds = [];
        param.updatedFileList = [];
      }

      // param.updatedFileList = this.fileList;
      let requestJson = JSON.stringify(param, function (key, value) {
        return key === "file" ? undefined : value;
      });

      formData.append('request', new Blob([requestJson], {
        type: "application/json "
      }));

      return {
        method: 'POST',
        url: url,
        data: formData,
        headers: {
          'Content-Type': undefined
        }
      };
    },
    saveCase(next, noTip) {
      let param = {};
      param.id = this.testCase.id;
      param.status = this.testCase.status;
      param.results = [];
      param.remark = this.testCase.remark;
      param.projectId = this.testCase.projectId;
      param.nodeId = this.testCase.nodeId;
      let option = this.getOption(param);
      for (let i = 0; i < this.testCase.steptResults.length; i++) {
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
      param.actualResult = this.testCase.actualResult;
      this.$post('/test/plan/case/edit', param, () => {
        this.$request(option, (response) => {

        });
        if (!noTip) {
          this.$success(this.$t('commons.save_success') + ' -> ' + this.$t('test_track.plan_view.next_case'));
        }
        this.updateTestCases(param);
        this.setPlanStatus(this.testCase.planId);
        if (next && this.index < this.testCases.length - 1) {
          this.handleNext();
        }
      });
    },
    updateTestCases(param) {
      for (let i = 0; i < this.testCases.length; i++) {
        let testCase = this.testCases[i];
        if (testCase.id === param.id) {
          testCase.results = param.results;
          testCase.issues = param.issues;
          testCase.status = param.status;
          return;
        }
      }
    },
    handleNext() {
      this.index++;
      this.getTestCase(this.index);
      this.reloadOtherInfo();
    },
    reloadOtherInfo() {
      this.otherInfoActive = false;
      this.$nextTick(() => {
        this.otherInfoActive = true;
      })
    },
    handlePre() {
      this.index--;
      this.getTestCase(this.index);
      this.reloadOtherInfo();
    },
    getTestCase(index) {
      this.testCase = {};
      let testCase = this.testCases[index];
      // id 为 TestPlanTestCase 的 id
      this.result = this.$get('/test/plan/case/get/' + testCase.id, response => {
        let item = {};
        Object.assign(item, response.data);
        if (item.results) {
          item.results = JSON.parse(item.results);
        } else if (item.steps) {
          item.results = [item.steps.length];
        }
        if (item.issues) {
          item.issues = JSON.parse(item.issues);
        } else {
          item.issues = {};
        }
        item.steps = JSON.parse(item.steps);
        if (!item.stepModel) {
          item.stepModel = 'STEP';
        }
        item.steptResults = [];
        if (item.steps) {
          for (let i = 0; i < item.steps.length; i++) {
            if (item.results) {
              if (item.results[i]) {
                item.steps[i].actualResult = item.results[i].actualResult;
                item.steps[i].executeResult = item.results[i].executeResult;
              }
              item.steptResults.push(item.steps[i]);
            } else {
              item.steptResults.push({
                actualResult: '',
                executeResult: ''
              });
            }
          }
        }
        this.testCase = item;
        parseCustomField(this.testCase, this.testCaseTemplate, null, null, buildTestCaseOldFields(this.testCase));
        this.isCustomFiledActive = true;
        if (!this.testCase.actualResult) {
          // 如果没值,使用模板的默认值
          this.testCase.actualResult = this.testCaseTemplate.actualResult;
        }
        this.getComments(item);
      });
    },
    openTestCaseEdit(testCase) {
      this.showDialog = true;
      this.activeTab = 'detail';
      this.hasTapdId = false;
      this.hasZentaoId = false;
      this.isReadOnly = !hasPermission('PROJECT_TRACK_PLAN:READ+RELEVANCE_OR_CANCEL');

      listenGoBack(this.handleClose);
      let initFuc = this.initData;
      getTemplate('field/template/case/get/relate/', this)
        .then((template) => {
          this.testCaseTemplate = template;
          initFuc(testCase);
        });
      if (this.$refs.otherInfo) {
        this.$refs.otherInfo.reset();
      }
    },
    testRun(reportId) {
      this.testCase.reportId = reportId;
      this.saveReport(reportId);
      this.activeTab = 'result';
    },
    testTabChange(data) {
      if (this.testCase.type === 'performance' && data.paneName === 'result') {
        this.$refs.performanceTestResult.checkReportStatus();
        this.$refs.performanceTestResult.init();
      }
    },
    saveReport(reportId) {
      this.$post('/test/plan/case/edit', {id: this.testCase.id, reportId: reportId});
    },
    initData(testCase) {
      this.result = this.$post('/test/plan/case/list/ids', this.searchParam, response => {
        this.testCases = response.data;
        for (let i = 0; i < this.testCases.length; i++) {
          if (this.testCases[i].id === testCase.id) {
            this.index = i;
            this.getTestCase(i);
          }
        }
      });
    },
    openTest(item) {
      const type = item.testType;
      const id = item.testId;
      switch (type) {
        case "performance": {
          let performanceData = this.$router.resolve({
            path: '/performance/test/edit/' + id,
          })
          window.open(performanceData.href, '_blank');
          break;
        }
        case "testcase": {
          let caseData = this.$router.resolve({name:'ApiDefinition',params:{redirectID:getUUID(),dataType:"apiTestCase",dataSelectRange:'single:'+id}});
          window.open(caseData.href, '_blank');
          break;
        }
        case "automation": {
          let automationData = this.$router.resolve({
            name: 'ApiAutomation',
            params: {redirectID: getUUID(), dataType: "scenario", dataSelectRange: 'edit:' + id}
          });
          window.open(automationData.href, '_blank');
          break;
        }
      }
    },
    openTestTestCase(item) {
      let TestCaseData = this.$router.resolve(
        {path: '/track/case/all', query: {redirectID: getUUID(), dataType: "testCase", dataSelectRange: item.caseId}}
      );
      window.open(TestCaseData.href, '_blank');
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

.cast_label {
  color: dimgray;
}

.status-button {
  padding-left: 4%;
  padding-right: 4%;
}

.head-right {
  text-align: right;
}

.el-col:not(.test-detail) {
  line-height: 50px;
}

.issues-edit >>> p {
  line-height: 16px;
}

.status-button {
  float: right;
}

.el-scrollbar {
  height: 100%;
}

.container {
  height: 100vh;
}

.container >>> .el-card__body {
  height: calc(100vh - 50px);
}

.comment-card >>> .el-card__header {
  padding: 0 20px;
}

.comment-card >>> .el-card__body {
  height: calc(100vh - 100px);
}

.case_container > .el-row {
  margin-top: 1%;
}

.el-switch >>> .el-switch__label {
  color: dimgray;
}

.el-switch >>> .el-switch__label.is-active {
  color: #409EFF;
}

p {
  white-space: pre-line;
  line-height: 20px;
}

.head-bar {
  z-index: 999;
}
</style>

<style>
.issues-popover {
  height: 550px;
  overflow: auto;
}

.save-btn {
  margin-left: 10px;
}


.el-divider__text {
  line-height: normal;
}

.test-case-name {
  padding: 0;
}
</style>
