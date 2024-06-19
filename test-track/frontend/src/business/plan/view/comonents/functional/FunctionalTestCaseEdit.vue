<template>
  <el-drawer
    :before-close="handleClose"
    :visible.sync="showDialog"
    :with-header="false"
    :modal-append-to-body="false"
    size="100%"
    ref="drawer"
    v-loading="loading"
  >
    <template>
      <el-row>
        <el-col :span="17">
          <div class="container">
            <el-card>
              <el-scrollbar>
                <el-header>
                  <el-row type="flex" class="head-bar">
                    <el-col :span="4">
                      <el-button
                        plain
                        size="mini"
                        icon="el-icon-back"
                        @click="cancel"
                        >{{ $t("test_track.return") }}
                      </el-button>
                    </el-col>

                    <el-col class="head-right" :span="20">
                      <ms-previous-next-button
                        :index="index"
                        :page-num="pageNum"
                        :page-size="pageSize"
                        :page-total="pageTotal"
                        :total="total"
                        :next-page-data="nextPageData"
                        :pre-page-data="prePageData"
                        @pre="handlePre"
                        @next="handleNext"
                        :list="testCases"
                      />
                    </el-col>
                  </el-row>

                  <el-row class="head-bar">
                    <el-col>
                      <el-divider content-position="left" class="title-divider">
                        <el-button
                          class="test-case-name"
                          type="text"
                          :disabled="!hasProjectPermission"
                          @click="openTestTestCase(testCase)"
                        >
                          <span
                            class="title-link"
                            :title="testCase.name"
                            :style="{ 'max-width': titleWith + 'px' }"
                          >
                            {{ testCase.customNum }}-{{ testCase.name }}
                          </span>
                        </el-button>
                      </el-divider>
                    </el-col>
                  </el-row>
                </el-header>
                <div class="case_container">
                  <el-form :model="testCase">
                    <el-row>
                      <el-col :span="7">
                        <el-form-item
                          :label="$t('test_track.case.module')"
                          prop="nodePath"
                          :label-width="formLabelWidth"
                        >
                          {{ testCase.nodePath }}
                        </el-form-item>
                      </el-col>
                      <el-col :span="7">
                        <el-form-item
                          :label="$t('test_track.plan.plan_project')"
                          prop="projectName"
                          :label-width="formLabelWidth"
                        >
                          {{ testCase.projectName }}
                        </el-form-item>
                      </el-col>
                      <el-col :span="10">
                        <el-form-item
                          :label="
                            $t('test_track.plan.load_case.execution_status')
                          "
                          :label-width="formLabelWidth"
                        >
                          <status-table-item :value="originalStatus" />
                        </el-form-item>
                      </el-col>
                    </el-row>

                    <el-form
                      ref="customFieldForm"
                      v-if="isCustomFiledActive"
                      class="case-form"
                    >
                      <custom-field-form-items
                        :form-label-width="formLabelWidth"
                        :fields="testCaseTemplate.customFields"
                        :loading="loading"
                        :system-name-map="systemNameMap"/>
                    </el-form>

                    <form-rich-text-item
                      :label-width="formLabelWidth"
                      :disabled="true"
                      :title="$t('test_track.case.prerequisite')"
                      :data="testCase"
                      prop="prerequisite"
                    />
                    <step-change-item
                      :disable="true"
                      :label-width="formLabelWidth"
                      :form="testCase"
                    />
                    <test-plan-case-step-results-item
                      :label-width="formLabelWidth"
                      :is-read-only="isReadOnly"
                      v-if="testCase.stepModel === 'STEP'"
                      :test-case="testCase"
                    />
                    <form-rich-text-item
                      :label-width="formLabelWidth"
                      v-if="testCase.stepModel === 'TEXT'"
                      :disabled="true"
                      :title="$t('test_track.case.step_desc')"
                      :data="testCase"
                      prop="stepDescription"
                    />
                    <form-rich-text-item
                      :label-width="formLabelWidth"
                      v-if="testCase.stepModel === 'TEXT'"
                      :disabled="true"
                      :title="$t('test_track.case.expected_results')"
                      :data="testCase"
                      prop="expectedResult"
                    />
                    <form-rich-text-item
                      :label-width="formLabelWidth"
                      v-if="testCase.stepModel === 'TEXT'"
                      :title="$t('test_track.plan_view.actual_result')"
                      :data="testCase"
                      prop="actualResult"
                    />

                    <el-form-item
                      :label="$t('test_track.case.other_info')"
                      :label-width="formLabelWidth"
                    >
                      <test-case-edit-other-info
                        :plan-id="testCase.planId"
                        v-if="otherInfoActive"
                        @openTest="openTest"
                        :is-test-plan-edit="true"
                        @syncRelationGraphOpen="syncRelationGraphOpen"
                        :read-only="true"
                        :is-test-plan="true"
                        :project-id="testCase.projectId"
                        :form="testCase"
                        :case-id="testCase.caseId"
                        ref="otherInfo"
                      />
                    </el-form-item>
                  </el-form>
                </div>
              </el-scrollbar>
            </el-card>
          </div>
        </el-col>
        <el-col :span="7">
          <div class="comment-card">
            <test-plan-functional-execute
              :test-case="testCase"
              :is-read-only="isReadOnly"
              :origin-status="originalStatus"
              @saveCase="saveCase"
            />
            <review-comment
              default-type="PLAN"
              :case-id="testCase.caseId"
              ref="comment"
            />
          </div>
        </el-col>
      </el-row>
    </template>
  </el-drawer>
</template>

<script>
import TestPlanTestCaseStatusButton from "../../../common/TestPlanTestCaseStatusButton";
import ClassicEditor from "@ckeditor/ckeditor5-build-classic";
import {getUUID, listenGoBack, removeGoBackListener,} from "metersphere-frontend/src/utils";
import TestCaseAttachment from "@/business/case/components/TestCaseAttachment";
import CaseComment from "@/business/case/components/CaseComment";
import MsPreviousNextButton from "metersphere-frontend/src/components/MsPreviousNextButton";
import ReviewComment from "@/business/review/commom/ReviewComment";
import {buildTestCaseOldFields, parseCustomField,} from "metersphere-frontend/src/utils/custom_field";
import FormRichTextItem from "metersphere-frontend/src/components/FormRichTextItem";
import MsFormDivider from "metersphere-frontend/src/components/MsFormDivider";
import TestCaseEditOtherInfo from "@/business/case/components/TestCaseEditOtherInfo";
import CustomFiledComponent from "metersphere-frontend/src/components/new-ui/MsCustomFiledComponent";
import {SYSTEM_FIELD_NAME_MAP} from "metersphere-frontend/src/utils/table-constants";
import IssueDescriptionTableItem from "@/business/issue/IssueDescriptionTableItem";
import StepChangeItem from "@/business/case/components/StepChangeItem";
import TestCaseStepItem from "@/business/case/components/TestCaseStepItem";
import TestPlanCaseStepResultsItem from "@/business/plan/view/comonents/functional/TestPlanCaseStepResultsItem";
import TestPlanFunctionalExecute from "@/business/plan/view/comonents/functional/TestPlanFunctionalExecute";
import StatusTableItem from "@/business/common/tableItems/planview/StatusTableItem";
import {testPlanTestCaseEdit, testPlanTestCaseGet,} from "@/api/remote/plan/test-plan-test-case";
import {testPlanEditStatus} from "@/api/remote/plan/test-plan";
import {getTestTemplate} from "@/api/custom-field-template";
import {checkProjectPermission} from "@/api/testCase";
import {openCaseEdit, resetPlanCaseSystemField} from "@/business/case/test-case";
import CustomFieldFormItems from "@/business/common/CustomFieldFormItems";
import {getCurrentProjectID, parseMdImage, saveMarkDownImg} from "@/business/utils/sdk-utils";

export default {
  name: "FunctionalTestCaseEdit",
  components: {
    CustomFieldFormItems,
    StatusTableItem,
    TestPlanFunctionalExecute,
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
    TestPlanTestCaseStatusButton,
    TestCaseAttachment,
  },
  data() {
    return {
      loading: false,
      showDialog: false,
      hasProjectPermission: true,
      testCase: {},
      index: 0,
      editor: ClassicEditor,
      test: {},
      activeTab: "detail",
      users: [],
      tableData: [],
      comments: [],
      testCaseTemplate: {},
      formLabelWidth: "100px",
      isCustomFiledActive: false,
      otherInfoActive: true,
      isReadOnly: false,
      testCases: [],
      originalStatus: "",
      titleWith: 0,
    };
  },
  props: {
    total: {
      type: Number,
    },
    searchParam: {
      type: Object,
    },
    pageNum: Number,
    pageSize: {
      type: Number,
      default: 1,
    },
    nextPageData: Object,
    prePageData: Object,
  },
  provide() {
    return {
      enableTempUpload: true
    }
  },
  computed: {
    systemNameMap() {
      return SYSTEM_FIELD_NAME_MAP;
    },
    pageTotal() {
      return Math.ceil(this.total / this.pageSize);
    },
  },
  methods: {
    handleClose() {
      removeGoBackListener(this.handleClose);
      this.showDialog = false;
      this.searchParam.status = null;
      this.$emit("update:search-param", this.searchParam);
    },
    cancel() {
      this.handleClose();
    },
    saveCase(command) {
      let param = {};
      param.id = this.testCase.id;
      param.caseId = this.testCase.caseId;
      param.status = this.testCase.status;
      param.results = [];
      param.remark = this.testCase.remark;
      param.projectId = this.testCase.projectId;
      param.nodeId = this.testCase.nodeId;
      param.demandId = this.testCase.demandId;
      param.name = this.testCase.name;
      param.comment = this.testCase.comment;
      for (let i = 0; i < this.testCase.steptResults.length; i++) {
        let result = {};
        result.actualResult = this.testCase.steptResults[i].actualResult;
        result.executeResult = this.testCase.steptResults[i].executeResult;
        if (result.actualResult && result.actualResult.length > 500) {
          this.$warning(
            this.$t("test_track.plan_view.actual_result") +
              this.$t("test_track.length_less_than") +
              "500"
          );
          return;
        }
        if (
          result.executeResult === "Failure" &&
          this.testCase.status === "Pass"
        ) {
          this.$warning(this.$t("test_track.plan_view.execute_tip"));
          this.testCase.status = this.originalStatus;
          return;
        }
        param.results.push(result);
      }
      param.results = JSON.stringify(param.results);
      param.actualResult = this.testCase.actualResult;
      testPlanTestCaseEdit(param).then(() => {
        this.$success(this.$t("commons.save_success"));
        this.updateTestCases(param);
        this.setPlanStatus(this.testCase.planId);
        this.handleMdImages({
          id: param.id,
          actualResult: param.actualResult,
          description: this.testCase.comment,
        });

        if (this.testCase.comment) {
          this.$refs.comment.getComments();
          this.testCase.comment = "";
        }
        this.originalStatus = this.testCase.status;
        if (command === 'save') {
          this.handleNext();
        }
      });
    },
    handleMdImages(param) {
      // 解析富文本框中的图片
      let mdImages = [];
      mdImages.push(...parseMdImage(param.description));
      mdImages.push(...parseMdImage(param.actualResult));
      let comments = this.$refs.comment.comments;
      if (comments) {
        comments.forEach((item) => {
          mdImages.push(...parseMdImage(item.description));
        });
      }

      // 将图片从临时目录移入正式目录
      saveMarkDownImg({
        projectId: getCurrentProjectID(),
        resourceId: param.id,
        fileNames: mdImages
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
      if (this.isLastData()) {
        return;
      } else if (this.index === this.testCases.length - 1) {
        this.$emit("nextPage");
        this.index = 0;
        return;
      }
      this.index++;
      this.getTestCase(this.testCases[this.index].id);
      this.reloadOtherInfo();
    },
    isLastData() {
      return (
        this.index === this.testCases.length - 1 &&
        this.pageNum === this.pageTotal
      );
    },
    reloadOtherInfo() {
      this.otherInfoActive = false;
      this.$nextTick(() => {
        this.otherInfoActive = true;
      });
    },
    handlePre() {
      if (this.index === 0 && this.pageNum === 1) {
        this.$warning("已经是第一页");
        return;
      } else if (this.index === 0) {
        this.$emit("prePage");
        this.index = this.pageSize - 1;
        return;
      }
      this.index--;
      this.getTestCase(this.testCases[this.index].id);
      this.reloadOtherInfo();
    },
    getTestCase(id) {
      this.loading = true;
      // id 为 TestPlanTestCase 的 id
      testPlanTestCaseGet(id).then((response) => {
        this.loading = false;
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
          item.stepModel = "STEP";
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
                actualResult: "",
                executeResult: "",
              });
            }
          }
        }
        this.testCase = item;
        this.originalStatus = this.testCase.status;
        parseCustomField(
          this.testCase,
          this.testCaseTemplate,
          null,
          buildTestCaseOldFields(this.testCase)
        );
        this.testCaseTemplate.customFields.forEach((item) => {
          try {
            let v = JSON.parse(item.defaultValue);
            if (!(v instanceof Object) || ['multipleSelect', 'checkbox', 'multipleMember', 'multipleInput'].indexOf(item.type) > -1) {
              item.defaultValue = v;
            }
          } catch (e) {
            // nothing
          }
        });
        this.isCustomFiledActive = true;
        if (!this.testCase.actualResult) {
          // 如果没值,使用模板的默认值
          this.testCase.actualResult = this.testCaseTemplate.actualResult;
        }
        this.resetSystemField();
      });
    },
    resetSystemField() {
      resetPlanCaseSystemField(this.testCaseTemplate.customFields, this.testCase);
    },
    openTestCaseEdit(testCase, tableData) {
      this.reloadOtherInfo();
      checkProjectPermission(testCase.projectId).then((r) => {
        this.hasProjectPermission = r.data;
      });
      this.showDialog = true;
      this.activeTab = "detail";
      this.originalStatus = testCase.status;
      this.setTitleWith();

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

      listenGoBack(this.handleClose);
      let initFuc = this.getTestCase;
      getTestTemplate(testCase.projectId).then((template) => {
        this.testCaseTemplate = template;
        initFuc(testCase.id);
      });
      if (this.$refs.otherInfo) {
        this.$refs.otherInfo.reset();
      }
    },
    testRun(reportId) {
      this.testCase.reportId = reportId;
      this.saveReport(reportId);
      this.activeTab = "result";
    },
    testTabChange(data) {
      if (this.testCase.type === "performance" && data.paneName === "result") {
        this.$refs.performanceTestResult.checkReportStatus();
        this.$refs.performanceTestResult.init();
      }
    },
    saveReport(reportId) {
      testPlanTestCaseEdit({ id: this.testCase.id, reportId: reportId });
    },
    openTest(item) {
      const type = item.testType;
      const id = item.testId;
      switch (type) {
        case "performance": {
          let performanceData = this.$router.resolve({
            path: "/performance/test/edit/" + id,
          });
          window.open(performanceData.href, "_blank");
          break;
        }
        case "testcase": {
          let caseData = this.$router.resolve({
            name: "ApiDefinition",
            params: {
              versionId: "default",
              redirectID: getUUID(),
              dataType: "apiTestCase",
              dataSelectRange: "single:" + id,
            },
          });
          window.open(caseData.href, "_blank");
          break;
        }
        case "automation": {
          let automationData = this.$router.resolve({
            name: "ApiAutomation",
            params: {
              redirectID: getUUID(),
              dataType: "scenario",
              dataSelectRange: "edit:" + id,
            },
          });
          window.open(automationData.href, "_blank");
          break;
        }
      }
    },
    setTitleWith() {
      this.$nextTick(() => {
        this.titleWith = 0;
        let titleDivider = document.getElementsByClassName("title-divider");
        if (titleDivider && titleDivider.length > 0) {
          this.titleWith = 0.9 * titleDivider[0].clientWidth;
        }
      });
    },
    syncRelationGraphOpen(val) {
      this.relationGraphOpen = val;
    },
    openTestTestCase(item) {
      openCaseEdit({caseId: item.caseId, projectId: item.projectId}, this);
    },
    addPLabel(str) {
      return "<p>" + str + "</p>";
    },
    setPlanStatus(planId) {
      testPlanEditStatus(planId);
    },
  },
};
</script>

<style scoped>
.cast_label {
  color: dimgray;
}

.head-right {
  text-align: right;
}

.el-col:not(.test-detail) {
  line-height: 50px;
}

.issues-edit :deep(p) {
  line-height: 16px;
}

.el-scrollbar {
  height: 100%;
}

.container {
  height: 100vh;
  padding-right: 10px;
}

.container :deep(.el-card__body) {
  height: calc(100vh - 50px);
}

.comment-card {
  padding-left: 0;
  padding-right: 15px;
  padding-top: 15px;
}

.comment-card :deep(.el-card__header) {
  padding: 0 20px;
}

.case_container > .el-row {
  margin-top: 1%;
}

.el-switch :deep(.el-switch__label) {
  color: dimgray;
}

.el-switch :deep(.el-switch__label.is-active) {
  color: #409eff;
}

p {
  white-space: pre-line;
  line-height: 20px;
}

.issues-popover {
  height: 550px;
  overflow: auto;
}

.el-divider__text {
  line-height: normal;
}

.test-case-name {
  padding: 0;
  line-height: 20px;
  text-decoration: underline solid #783887;
}

.title-link {
  display: inline-block;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  word-break: break-all;
  margin-right: 5px;
}

:deep(.el-drawer__body) {
  overflow: unset;
}

.comment-card :deep(.executeCard) {
  margin-bottom: 5px;
}
</style>
