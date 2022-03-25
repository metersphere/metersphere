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

                <el-header style="height: 100%;">

                  <el-row type="flex" class="head-bar">

                    <el-col :span="8">
                      <el-button plain size="mini"
                                 icon="el-icon-back"
                                 @click="cancel">{{ $t('test_track.return') }}
                      </el-button>
                    </el-col>

                    <el-col :span="14" class="head-right">

                      <ms-previous-next-button
                        :index="index"
                        :page-num="pageNum"
                        :page-size="pageSize"
                        :page-total="pageTotal"
                        :total="total"
                        :next-page-data="nextPageData"
                        :pre-page-data="prePageData"
                        :list="testCases"
                        @pre="handlePre"
                        @next="handleNext"/>

                      <el-divider direction="vertical"></el-divider>

                      <el-button type="success" size="mini"
                                 :disabled="isReadOnly" :icon="testCase.reviewStatus === 'Pass' ? 'el-icon-check' : ''"
                                 @click="saveCase('Pass')">
                        {{ $t('test_track.review.pass') }}
                      </el-button>
                      <el-button type="danger" size="mini"
                                 :disabled="isReadOnly"
                                 :icon="testCase.reviewStatus === 'UnPass' ? 'el-icon-check' : ''"
                                 @click="saveCase('UnPass')">
                        {{ $t('test_track.review.un_pass') }}
                      </el-button>
                    </el-col>

                  </el-row>

                  <el-row style="margin-top: 0;">
                    <el-col>
                      <el-divider content-position="left">
                        <el-button class="test-case-name" type="text" @click="openTestTestCase(testCase)">
                          {{ testCase.num }}-{{ testCase.name }}
                        </el-button>
                      </el-divider>
                    </el-col>
                  </el-row>

                </el-header>

                <div class="case_container">
                  <el-form>

                    <el-row>
                      <el-col :span="7">
                        <el-form-item :label="$t('test_track.case.module')" prop="nodePath"
                                      :label-width="formLabelWidth">
                          {{ testCase.nodePath }}
                        </el-form-item>
                      </el-col>
                      <el-col :span="7">
                        <el-form-item :label="$t('test_track.plan.plan_project')" prop="projectName"
                                      :label-width="formLabelWidth">
                          {{ testCase.projectName }}
                        </el-form-item>
                      </el-col>
                    </el-row>

                    <el-form ref="customFieldForm"
                             v-if="isCustomFiledActive"
                             class="case-form">
                      <el-row>
                        <el-col :span="7" v-for="(item, index) in testCaseTemplate.customFields" :key="index">
                          <el-form-item :label="item.system ? $t(systemNameMap[item.name]) : item.name"
                                        :prop="item.name"
                                        :label-width="formLabelWidth">
                            <custom-filed-component :disabled="true" :data="item" :form="{}" prop="defaultValue"/>
                          </el-form-item>
                        </el-col>
                      </el-row>
                    </el-form>

                    <form-rich-text-item :label-width="formLabelWidth" :disabled="true"
                                         :title="$t('test_track.case.prerequisite')"
                                         :data="testCase" prop="prerequisite"/>
                    <step-change-item :disable="true" :label-width="formLabelWidth" :form="testCase"/>
                    <form-rich-text-item :label-width="formLabelWidth" :disabled="true"
                                         v-if="testCase.stepModel === 'TEXT'" :title="$t('test_track.case.step_desc')"
                                         :data="testCase" prop="stepDescription"/>
                    <form-rich-text-item :label-width="formLabelWidth" :disabled="true"
                                         v-if="testCase.stepModel === 'TEXT'"
                                         :title="$t('test_track.case.expected_results')" :data="testCase"
                                         prop="expectedResult"/>

                    <test-case-step-item :label-width="formLabelWidth" :read-only="true"
                                         v-if="testCase.stepModel === 'STEP'" :form="testCase"/>

                    <el-form-item :label="$t('test_track.case.other_info')" :label-width="formLabelWidth">
                      <test-case-edit-other-info @openTest="openTest" :read-only="true" :is-test-plan="true"
                                                 :project-id="projectId" :form="testCase" :case-id="testCase.caseId"
                                                 ref="otherInfo"/>
                    </el-form-item>

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
                              :oldReviewStatus="oldReviewStatus"
                              @getComments="getComments" :review-status="testCase.reviewStatus" ref="reviewComment"
                              @saveCaseReview="saveCaseReview"/>
            </el-card>
          </el-col>
        </div>
      </el-row>

    </template>

  </el-drawer>
</template>

<script>


import PerformanceTestResult from "../../../plan/view/comonents/test/PerformanceTestResult";
import PerformanceTestDetail from "../../../plan/view/comonents/test/PerformanceTestDetail";
import ApiTestResult from "../../../plan/view/comonents/test/ApiTestResult";
import ApiTestDetail from "../../../plan/view/comonents/test/ApiTestDetail";
import TestPlanTestCaseStatusButton from "../../../plan/common/TestPlanTestCaseStatusButton";
import {getCurrentProjectID, getUUID, listenGoBack, removeGoBackListener} from "@/common/js/utils";
import ReviewComment from "../../commom/ReviewComment";
import TestCaseAttachment from "@/business/components/track/case/components/TestCaseAttachment";
import ApiCaseItem from "@/business/components/api/definition/components/case/ApiCaseItem";
import MsEditApiScenario from "@/business/components/api/automation/scenario/EditApiScenario";
import {buildTestCaseOldFields, getTemplate, parseCustomField} from "@/common/js/custom_field";
import TestCaseEditOtherInfo from "@/business/components/track/case/components/TestCaseEditOtherInfo";
import {SYSTEM_FIELD_NAME_MAP} from "@/common/js/table-constants";
import FormRichTextItem from "@/business/components/track/case/components/FormRichTextItem";
import CustomFiledComponent from "@/business/components/project/template/CustomFiledComponent";
import StepChangeItem from "@/business/components/track/case/components/StepChangeItem";
import TestCaseStepItem from "@/business/components/track/case/components/TestCaseStepItem";
import MsPreviousNextButton from "@/business/components/common/components/MsPreviousNextButton";

export default {
  name: "TestReviewTestCaseEdit",
  components: {
    MsPreviousNextButton,
    TestCaseStepItem,
    StepChangeItem,
    CustomFiledComponent,
    FormRichTextItem,
    TestCaseEditOtherInfo,
    PerformanceTestResult,
    PerformanceTestDetail,
    ApiTestResult,
    ApiTestDetail,
    TestPlanTestCaseStatusButton,
    ReviewComment,
    TestCaseAttachment,
    ApiCaseItem,
    MsEditApiScenario

  },
  data() {
    return {
      result: {},
      showDialog: false,
      testCase: {},
      index: 0,
      testCases: [],
      readConfig: {toolbar: []},
      test: {},
      activeTab: 'detail',
      isFailure: true,
      users: [],
      activeName: 'comment',
      comments: [],
      tableData: [],
      currentScenario: {},
      mark: 'detail',
      api: {},
      apiCase: {},
      testCaseTemplate: {},
      hasTapdId: false,
      hasZentaoId: false,
      formLabelWidth: '100px',
      isCustomFiledActive: false,
      oldReviewStatus: 'Prepare'
    };
  },
  props: {
    total: {
      type: Number
    },
    searchParam: {
      type: Object
    },
    isReadOnly: {
      type: Boolean,
      default: false
    },
    pageNum: Number,
    pageSize: {
      type: Number,
      default: 1
    },
    nextPageData: Object,
    prePageData: Object
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
    systemNameMap() {
      return SYSTEM_FIELD_NAME_MAP;
    },
    pageTotal() {
      return Math.ceil(this.total / this.pageSize);
    }
  },
  methods: {
    openTest(item) {
      const type = item.testType;
      const id = item.testId;
      switch (type) {
        case "performance": {
          let performanceData = this.$router.resolve({
            path: '/performance/test/edit/' + id,
          });
          window.open(performanceData.href, '_blank');
          break;
        }
        case "testcase": {
          let caseData = this.$router.resolve({
            name: 'ApiDefinition',
            params: {redirectID: getUUID(), dataType: "apiTestCase", dataSelectRange: 'single:' + id}
          });
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
    handleClose() {
      removeGoBackListener(this.handleClose);
      this.showDialog = false;
    },
    cancel() {
      this.handleClose();
      this.$emit('refreshTable');
    },
    saveCase(status) {
      let param = {};
      param.id = this.testCase.id;
      param.caseId = this.testCase.caseId;
      param.reviewId = this.testCase.reviewId;
      param.status = status;
      if (status === 'UnPass') {
        this.testCase.reviewStatus = 'UnPass';
        // 第一种情况，第一次评审，用户直接点击未通过，需要提醒未评论
        if (this.oldReviewStatus === 'Prepare' && this.comments.length < 1) {
          this.$refs.reviewComment.inputLight();
          this.$warning(this.$t('test_track.comment.description_is_null'));
        } else if (this.$refs.reviewComment.form.description.length > 0) {
          // 第二种情况，当前状态为未通过，但是评论区内还有内容未提交
          this.$refs.reviewComment.inputLight();
          this.$warning(this.$t('test_track.comment.submit_description'));
        } else if (this.oldReviewStatus === 'Pass') {
          // 第三种情况，从通过状态切换未通过状态，需要重新提交新的评论，才能切换
          this.$refs.reviewComment.inputLight();
          this.$warning(this.$t('test_track.comment.description_is_null'));
        } else {
          // 第四种情况，未通过状态直接点击未通过
          this.$post('/test/review/case/edit', param, () => {
            this.$success(this.$t('commons.save_success'));
            this.updateTestCases(param);
            this.setReviewStatus(this.testCase.reviewId);
            this.testCase.reviewStatus = status;
            // 修改当前用例在整个用例列表的状态
            this.testCases[this.index].reviewStatus = status;
            // 修改旧的状态
            this.oldReviewStatus = status;
            if (this.index < this.testCases.length - 1) {
              this.handleNext();
            }
          });
        }
      } else {
        this.$post('/test/review/case/edit', param, () => {
          this.$success(this.$t('commons.save_success'));
          this.updateTestCases(param);
          this.setReviewStatus(this.testCase.reviewId);
          this.testCase.reviewStatus = status;
          // 修改当前用例在整个用例列表的状态
          this.testCases[this.index].reviewStatus = status;
          if (this.index < this.testCases.length - 1) {
            this.handleNext();
          }
          // 切换状态后需要修改旧的状态
          this.oldReviewStatus = status;
        });
      }
    },
    saveCaseReview() {
      let param = {};
      let status = this.testCase.reviewStatus;
      param.id = this.testCase.id;
      param.caseId = this.testCase.caseId;
      param.reviewId = this.testCase.reviewId;
      param.status = status;
      this.$post('/test/review/case/edit', param, () => {
        this.updateTestCases(param);
        this.setReviewStatus(this.testCase.reviewId);
        this.oldReviewStatus = status;
        // 修改当前用例在整个用例列表的状态
        this.testCases[this.index].reviewStatus = status;
        if (this.index < this.testCases.length - 1) {
          this.handleNext();
        }
      });
    },
    updateTestCases(param) {
      for (let i = 0; i < this.testCases.length; i++) {
        let testCase = this.testCases[i];
        if (testCase.id === param.id) {
          testCase.status = param.status;
          return;
        }
      }
    },
    handleNext() {
      if (this.index === this.testCases.length - 1 && this.pageNum === this.pageTotal) {
        return;
      } else if (this.index === this.testCases.length - 1) {
        this.$emit('nextPage');
        return;
      }
      this.index++;
      this.getTestCase(this.testCases[this.index].id);
    },
    handlePre() {
      if (this.index === 0 && this.pageNum === 1) {
        return;
      } else if (this.index === 0) {
        this.$emit('prePage');
        return;
      }
      this.index--;
      this.getTestCase(this.testCases[this.index].id);
    },
    getTestCase(id) {
      this.result = this.$get("/test/review/case/get/" + id, response => {
        let item = {};
        let data = response.data;
        Object.assign(item, data);
        item.results = JSON.parse(item.results);
        if (item.issues) {
          item.issues = JSON.parse(item.issues);
        } else {
          item.issues = {};
        }
        item.steps = JSON.parse(item.steps);
        if (!item.stepModel) {
          item.stepModel = 'STEP';
        }
        parseCustomField(item, this.testCaseTemplate, null, buildTestCaseOldFields(item));
        this.isCustomFiledActive = true;
        this.testCase = item;
        this.oldReviewStatus = this.testCase.reviewStatus;
        if (!this.testCase.actualResult) {
          // 如果没值,使用模板的默认值
          this.testCase.actualResult = this.testCaseTemplate.actualResult;
        }
        this.getComments(item);
        this.$refs.reviewComment.resetInputLight();
      })

    },
    getFileMetaData(testCase) {
      this.tableData = [];
      this.result = this.$get("test/case/file/metadata/" + testCase.caseId, response => {
        let files = response.data;
        if (!files) {
          return;
        }
        this.tableData = JSON.parse(JSON.stringify(files));
        this.tableData.map(f => {
          f.size = f.size + ' Bytes';
        });
      })
    },
    openTestCaseEdit(testCase, tableData) {
      this.showDialog = true;
      // 一开始加载时候需要保存用例评审旧的状态
      this.oldReviewStatus = testCase.reviewStatus;
      this.activeTab = 'detail';
      this.getComments(testCase);
      this.hasTapdId = false;
      this.hasZentaoId = false;
      listenGoBack(this.handleClose);
      let initFuc = this.getTestCase;

      if (tableData) {
        this.testCases = tableData;
        for (let i = 0; i < this.testCases.length; i++) {
          let item = this.testCases[i];
          if (item.id === testCase.id) {
            this.index = i;
            break;
          }
        }
      }

      getTemplate('field/template/case/get/relate/', this)
        .then((template) => {
          this.testCaseTemplate = template;
          initFuc(testCase.id);
        });
    },

    getComments(testCase) {
      let id = '';
      if (testCase) {
        id = testCase.caseId;
      } else {
        id = this.testCase.caseId;
      }
      this.result = this.$get('/test/case/comment/list/' + id, res => {
        if (res.data) {
          this.comments = null;
          this.comments = res.data;
        }

      })
    },
    openTestTestCase(item) {
      let testCaseData = this.$router.resolve(
        {path: '/track/case/all', query: {redirectID: getUUID(), dataType: "testCase", dataSelectRange: item.caseId}}
      );
      window.open(testCaseData.href, '_blank');
    },
    getRelatedTest() {
      if (this.testCase.method === 'auto' && this.testCase.testId && this.testCase.testId !== 'other') {
        this.$get('/' + this.testCase.type + '/get/' + this.testCase.testId, response => {
          let data = response.data;
          if (data) {
            this.test = data;
          } else {
            this.test = {};
            this.$warning(this.$t("test_track.case.relate_test_not_find"));
          }
        });
      } else if (this.testCase.testId === 'other') {
        this.$warning(this.$t("test_track.case.other_relate_test_not_find"));
      }
    },
    setReviewStatus(reviewId) {
      this.$post('/test/case/review/edit/status/' + reviewId);
    },
    stepResultChange() {
      if (this.testCase.method === 'manual') {
        this.isFailure = this.testCase.steptResults.filter(s => {
          return s.executeResult === 'Failure' || s.executeResult === 'Blocking';
        }).length > 0;
      }
    },
  }
}
</script>

<style scoped>

.border-hidden >>> .el-textarea__inner {
  border-style: hidden;
  background-color: white;
  color: #606266;
}

.cast_label {
  color: dimgray;
}

.status-button {
  padding-left: 4%;
}

.head-right {
  text-align: right;
  margin-top: 30px;
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

.head-right-tip {
  color: darkgrey;
}

.el-scrollbar {
  height: 100%;
}

.container {
  height: 100vh;
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

.container >>> .el-card__body {
  height: calc(100vh - 50px);
}

.comment-card >>> .el-card__header {
  padding: 0 20px;
}

.comment-card >>> .el-card__body {
  height: calc(100vh - 100px);
}

.tb-edit >>> .el-textarea__inner {
  border-style: hidden;
  background-color: white;
  color: #060505;
}

.step-info {
  padding-left: 40px;
  padding-right: 15px;
}

.el-divider__text {
  line-height: normal;
}
</style>
