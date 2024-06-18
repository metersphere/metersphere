<template>
  <el-drawer
    :before-close="handleClose"
    :visible.sync="showDialog"
    :with-header="false"
    :modal-append-to-body="false"
    size="90%"
    ref="drawer">

    <template>
      <div class="edit-container" v-loading="loading">

        <test-review-test-case-edit-header-bar
          :test-case="testCase"
          @close="handleClose"
          ref="headerBar"/>

        <div class="case-container" :class="{'comment-empty-container': isCommentEmpty}">
          <el-scrollbar>
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
                <custom-field-form-items
                  :form-label-width="formLabelWidth"
                  :fields="testCaseTemplate.customFields"
                  :loading="loading"
                  :system-name-map="systemNameMap"/>
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
                <test-case-edit-other-info
                  v-if="otherInfoActive"
                  @openTest="openTest" :read-only="true"
                  @syncRelationGraphOpen="syncRelationGraphOpen"
                  :project-id="projectId" :form="testCase" :case-id="testCase.caseId"
                  ref="otherInfo"/>
              </el-form-item>

            </el-form>
          </el-scrollbar>
        </div>

        <ms-vertical-drag-bar top-min-height="40" bottom-min-height="40"/>

        <comment-history
          default-type="REVIEW"
          :case-id="caseId"
          :review-id="reviewId"
          @emptyChange="handleCommentEmptyChange"
          ref="comment"/>

        <test-review-test-case-edit-operation-bar
          :index="index"
          :page-num="pageNum"
          :page-size="pageSize"
          :page-total="pageTotal"
          :total="total"
          :next-page-data="nextPageData"
          :pre-page-data="prePageData"
          :list="testCases"
          :test-case="testCase"
          @refreshComment="refreshComment"
          @refreshTestCaseStatus="refreshTestCaseStatus"
          @pre="handlePre"
          @next="handleNext"/>

      </div>

    </template>
  </el-drawer>
</template>

<script>
import MsVerticalDragBar from "metersphere-frontend/src/components/dragbar/MsVerticalDragBar";

import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {getUUID, listenGoBack, removeGoBackListener} from "metersphere-frontend/src/utils"
import ReviewComment from "@/business/review/commom/ReviewComment";
import TestCaseAttachment from "@/business/case/components/TestCaseAttachment";
import {buildTestCaseOldFields, parseCustomField} from "metersphere-frontend/src/utils/custom_field";
import TestCaseEditOtherInfo from "@/business/case/components/TestCaseEditOtherInfo";
import {SYSTEM_FIELD_NAME_MAP} from "metersphere-frontend/src/utils/table-constants";
import FormRichTextItem from "metersphere-frontend/src/components/FormRichTextItem";
import CustomFiledComponent from "metersphere-frontend/src/components/new-ui/MsCustomFiledComponent";
import StepChangeItem from "@/business/case/components/StepChangeItem";
import TestCaseStepItem from "@/business/case/components/TestCaseStepItem";
import MsPreviousNextButton from "metersphere-frontend/src/components/MsPreviousNextButton";
import StatusTableItem from "@/business/common/tableItems/planview/StatusTableItem";
import {getTestTemplate} from "@/api/custom-field-template";
import {getRelateTest, getTestReviewTestCase,} from "@/api/test-review";
import TestReviewTestCaseEditOperationBar from "@/business/review/view/components/TestReviewTestCaseEditOperationBar";
import TestReviewTestCaseEditHeaderBar from "@/business/review/view/components/TestReviewTestCaseEditHeaderBar";
import CommentHistory from "@/business/review/view/components/commnet/CommentHistory";
import {resetCaseSystemField} from "@/business/case/test-case";
import CustomFieldFormItems from "@/business/common/CustomFieldFormItems";

export default {
  name: "TestReviewTestCaseEdit",
  components: {
    CustomFieldFormItems,
    CommentHistory,
    TestReviewTestCaseEditHeaderBar,
    TestReviewTestCaseEditOperationBar,
    StatusTableItem,
    MsPreviousNextButton,
    TestCaseStepItem,
    StepChangeItem,
    CustomFiledComponent,
    FormRichTextItem,
    TestCaseEditOtherInfo,
    TestCaseAttachment,
    MsVerticalDragBar,
    ReviewComment,
  },
  data() {
    return {
      loading: false,
      showDialog: false,
      testCase: {},
      index: 0,
      testCases: [],
      readConfig: { toolbar: [] },
      test: {},
      activeTab: "detail",
      isFailure: true,
      users: [],
      activeName: "comment",
      comments: [],
      tableData: [],
      currentScenario: {},
      mark: "detail",
      api: {},
      apiCase: {},
      testCaseTemplate: {},
      formLabelWidth: "100px",
      isCustomFiledActive: false,
      titleWith: 0,
      relationGraphOpen: false,
      isCommentEmpty: true,
      caseId: null,
      reviewId: null,
      otherInfoActive: true,
    };
  },
  props: {
    total: {
      type: Number,
    },
    searchParam: {
      type: Object,
    },
    isReadOnly: {
      type: Boolean,
      default: false,
    },
    pageNum: Number,
    pageSize: {
      type: Number,
      default: 1,
    },
    nextPageData: Object,
    prePageData: Object,
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
    },
  },
  methods: {
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
              versionId: "default",
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
    syncRelationGraphOpen(val) {
      this.relationGraphOpen = val;
    },
    handleClose() {
      removeGoBackListener(this.handleClose);

      this.showDialog = false;
      this.$refs.comment.clearComments();
    },
    cancel() {
      this.handleClose();
      this.$emit("refreshTable");
    },
    refreshComment() {
      if (this.$refs.comment) {
        this.$refs.comment.getComments();
      }
    },
    resetSystemField() {
      resetCaseSystemField(this.testCaseTemplate.customFields, this.testCase);
    },
    refreshTestCaseStatus(status) {
      this.testCase.reviewStatus = status;
      this.updateTestCases(this.testCase);
      this.refreshComment();
      this.$refs.headerBar.getReviewerStatus();
    },
    updateTestCases(param) {
      for (let i = 0; i < this.testCases.length; i++) {
        let testCase = this.testCases[i];
        if (testCase.id === param.id) {
          testCase.reviewStatus = param.reviewStatus;
          return;
        }
      }
    },
    handleCommentEmptyChange(isCommentEmpty) {
      this.isCommentEmpty = isCommentEmpty;
      this.$forceUpdate();
    },
    handleNext() {
      if (
        this.index === this.testCases.length - 1 &&
        this.pageNum === this.pageTotal
      ) {
        // 最后一条不处理
        return;
      } else if (this.index === this.testCases.length - 1) {
        // 到本页最后一条数据，则翻页
        this.$emit("nextPage");
        this.index = 0;
        return;
      }
      this.index++;
      this.caseId = this.testCases[this.index].caseId;
      this.getTestCase(this.testCases[this.index].id);
      this.$refs.otherInfo.getFileMetaData(this.caseId);
      this.reloadOtherInfo();
    },
    isLastData() {
      return (
        this.index === this.testCases.length - 1 &&
        this.pageNum === this.pageTotal
      );
    },
    handlePre() {
      if (this.index === 0 && this.pageNum === 1) {
        return;
      } else if (this.index === 0) {
        this.$emit("prePage");
        this.index = this.pageSize - 1;
        return;
      }
      this.index--;
      this.caseId = this.testCases[this.index].caseId;
      this.getTestCase(this.testCases[this.index].id);
      this.$refs.otherInfo.getFileMetaData(this.caseId);
      this.reloadOtherInfo();
    },
    reloadOtherInfo() {
      this.otherInfoActive = false;
      this.$nextTick(() => {
        this.otherInfoActive = true;
      });
    },
    getTestCase(id) {
      this.loading = true;
      getTestReviewTestCase(id).then((response) => {
        this.loading = false;
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
          item.stepModel = "STEP";
        }
        parseCustomField(
          item,
          this.testCaseTemplate,
          null,
          buildTestCaseOldFields(item)
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
        this.testCase = item;
        if (!this.testCase.actualResult) {
          // 如果没值,使用模板的默认值
          this.testCase.actualResult = this.testCaseTemplate.actualResult;
        }
        this.resetSystemField();
      });
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
    openTestCaseEdit(testCase, tableData) {
      this.reloadOtherInfo();
      if (this.$refs.comment) {
        this.$refs.comment.clearComments();
      }
      this.loading = true;

      this.showDialog = true;
      this.activeTab = "detail";
      listenGoBack(this.handleClose);
      let initFuc = this.getTestCase;
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

      getTestTemplate(testCase.projectId).then((response) => {
        this.testCaseTemplate = response;
        initFuc(testCase.id);
      });

      if (this.$refs.otherInfo) {
        this.$refs.otherInfo.reset();
      }

      this.caseId = testCase.caseId;
      this.reviewId = testCase.reviewId;
      this.$nextTick(() => {
        this.refreshComment();
      });
    },
    getRelatedTest() {
      if (
        this.testCase.method === "auto" &&
        this.testCase.testId &&
        this.testCase.testId !== "other"
      ) {
        getRelateTest(this.testCase.type, this.testCase.testId).then(
          (response) => {
            let data = response.data;
            if (data) {
              this.test = data;
            } else {
              this.test = {};
              this.$warning(this.$t("test_track.case.relate_test_not_find"));
            }
          }
        );
      } else if (this.testCase.testId === "other") {
        this.$warning(this.$t("test_track.case.other_relate_test_not_find"));
      }
    },
    setReviewStatus(reviewId) {
      editTestCaseReviewStatus(reviewId);
    },
    stepResultChange() {
      if (this.testCase.method === "manual") {
        this.isFailure =
          this.testCase.steptResults.filter((s) => {
            return (
              s.executeResult === "Failure" || s.executeResult === "Blocking"
            );
          }).length > 0;
      }
    },
  },
};
</script>

<style scoped>
.border-hidden :deep(.el-textarea__inner) {
  border-style: hidden;
  background-color: white;
  color: #606266;
}

.cast_label {
  color: dimgray;
}

.head-right {
  text-align: right;
  margin-top: 30px;
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

.edit-container {
  height: 100vh;
}

.case-container > .el-row {
  margin-top: 1%;
}

.case-container {
  height: calc(100vh - 355px);
}

.comment-empty-container {
  height: calc(100vh - 355px + 198px);
}

.el-switch :deep(.el-switch__label) {
  color: dimgray;
}

.el-switch :deep(.el-switch__label.is-active) {
  color: #409eff;
}

.comment-card {
  padding-left: 0;
  padding-right: 15px;
  padding-top: 15px;
}

.comment-card :deep(.el-card__header) {
  padding: 0 20px;
}

.tb-edit :deep(.el-textarea__inner) {
  border-style: hidden;
  background-color: white;
  color: #060505;
}

.test-case-name {
  padding: 0;
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

:deep(.el-form-item__content) {
  z-index: 2;
}

:deep(.el-scrollbar__bar.is-vertical) {
  z-index: 0;
  width: 0;
}
</style>
