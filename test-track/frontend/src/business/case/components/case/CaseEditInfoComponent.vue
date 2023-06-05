<template>
  <div class="content-body-wrap">
    <!-- 非创建状态下 展示 -->
    <div class="tab-pane-wrap" v-if="!editable">
      <el-tabs v-model="caseActiveName" @tab-click="tabClick">
        <!-- 用例详情 -->
        <el-tab-pane :label="$t('case.use_case_detail')" name="detail">
          <div
            class="tab-container"
            :class="{ 'comment-edit-tab-container': isCommentEdit }"
          >
            <el-scrollbar>
              <div class="content-container">
                <case-detail-component
                  :type="type"
                  :case-id="caseId"
                  :editable-state="editableState"
                  :read-only="readOnly || !editable"
                  :project-id="projectId"
                  :is-copy="isCopy"
                  :is-public-show="isPublicShow"
                  :is-readonly-user="isReadonlyUser"
                  :copy-case-id="copyCaseId"
                  :isClickAttachmentTab="isClickAttachmentTab"
                  :isTestPlan="isTestPlan"
                  :editable="editable"
                  :form="form"
                  :richTextDefaultOpen="richTextDefaultOpen"
                  :formLabelWidth="formLabelWidth"
                ></case-detail-component>
              </div>
            </el-scrollbar>
          </div>
        </el-tab-pane>
        <!-- 关联用例 -->
        <el-tab-pane
          :label="$t('case.associate_test_cases')"
          name="associateTestCases"
        >
          <span slot="label">
            {{ $t('case.associate_test_cases') }}
            <div class="el-step__icon is-text ms-api-col ms-header" v-if="relateCaseCount && relateCaseCount > 0">
              <div class="el-step__icon-inner">{{ relateCaseCount }}</div>
            </div>
          </span>
          <div
            class="tab-container"
            :class="{ 'comment-edit-tab-container': isCommentEdit }"
          >
            <el-scrollbar>
              <div class="content-container">
                <case-test-relate
                  @setCount="setRelateCaseCount"
                  ref="relateTest"
                  :case-id="caseId"
                  :read-only="readOnly"
                  :version-enable="versionEnable"
                ></case-test-relate>
              </div>
            </el-scrollbar>
          </div>
        </el-tab-pane>
        <!-- 关联缺陷 -->
        <el-tab-pane
          :label="$t('test_track.case.relate_issue')"
          name="associatedDefects"
        >
          <span slot="label">
            {{ $t('test_track.case.relate_issue') }}
            <div class="el-step__icon is-text ms-api-col ms-header" v-if="relateIssueCount && relateIssueCount > 0">
              <div class="el-step__icon-inner">{{ relateIssueCount }}</div>
            </div>
          </span>
          <div
            class="tab-container"
            :class="{ 'comment-edit-tab-container': isCommentEdit }"
          >
            <el-scrollbar>
              <div class="content-container">
                <case-issue-relate
                  @setCount="setRelateIssueCount"
                  :plan-id="planId"
                  :is-copy="isCopy"
                  :copy-case-id="copyCaseId"
                  :read-only="readOnly && !isTestPlan"
                  :plan-case-id="planId ? this.form.id : null"
                  :case-id="caseId"
                  ref="issue"
                />
              </div>
            </el-scrollbar>
          </div>
        </el-tab-pane>
        <!-- 依赖关系 -->
        <el-tab-pane :label="$t('case.dependencies')" name="dependencies">
          <span slot="label">
            {{ $t('commons.relationship.name') }}
            <div class="el-step__icon is-text ms-api-col ms-header" v-if="relationshipCount && relationshipCount > 0">
              <div class="el-step__icon-inner">{{ relationshipCount }}</div>
            </div>
          </span>
          <div
            class="tab-container"
            :class="{ 'comment-edit-tab-container': isCommentEdit }"
          >
            <el-scrollbar>
              <div class="content-container">
                <case-relationship-viewer
                  @setCount="setRelationshipCount"
                  :read-only="readOnly"
                  :resource-id="caseId"
                  @openDependGraphDrawer="setRelationshipGraph"
                  :version-enable="versionEnable"
                  resource-type="TEST_CASE"
                  ref="relationship"
                ></case-relationship-viewer>
              </div>
            </el-scrollbar>
          </div>
        </el-tab-pane>
        <!-- 评论 -->
        <el-tab-pane :label="$t('case.comment')" name="comment">
          <span slot="label">
            {{ $t('case.comment') }}
            <div class="el-step__icon is-text ms-api-col ms-header" v-if="comments && comments.length > 0">
              <div class="el-step__icon-inner">{{ comments.length }}</div>
            </div>
          </span>
          <div
            class="tab-container"
            :class="{ 'comment-edit-tab-container': isCommentEdit }"
          >
            <el-scrollbar>
              <div class="content-container">
                <case-comment-viewer
                  :is-public-show="isPublicShow || isReadonlyUser"
                  @getComments="getComments"
                  :comments="comments"
                  ref="commentRef"
                ></case-comment-viewer>
              </div>
            </el-scrollbar>
          </div>
        </el-tab-pane>
        <!-- 变更记录 -->
        <el-tab-pane :label="$t('case.change_record')" name="changeRecord">
          <div
            class="tab-container"
            :class="{ 'comment-edit-tab-container': isCommentEdit }"
          >
            <el-scrollbar>
              <div class="content-container">
                <case-change-history
                  :case-id="caseId"
                  ref="caseChangeHistoryRef"
                ></case-change-history>
              </div>
            </el-scrollbar>
          </div>
        </el-tab-pane>
      </el-tabs>
      <div class="comment-common" v-if="!isPublicShow">
        <case-comment-component
          :case-id="caseId"
          :read-only="readOnly"
          @stateChange="handleCommentStateChange"
          @toggleCommentTab="toggleCommentTab"
          @getComments="getComments"
        />
      </div>
    </div>
    <el-scrollbar>
      <div class="content-container editable-container" v-if="editable">
        <case-detail-component
          :class="{ 'edit-component' : (editableState || editable) }"
          :type="type"
          :case-id="caseId"
          :read-only="readOnly"
          :project-id="projectId"
          :is-copy="isCopy"
          :copy-case-id="copyCaseId"
          :isClickAttachmentTab="isClickAttachmentTab"
          :isTestPlan="isTestPlan"
          :is-public-show="isPublicShow"
          :is-readonly-user="isReadonlyUser"
          :editable="editable"
          :editable-state="editableState"
          :form="form"
          :richTextDefaultOpen="richTextDefaultOpen"
          :formLabelWidth="formLabelWidth"
          ref="testCaseBaseInfo"
        ></case-detail-component>
      </div>
    </el-scrollbar>
  </div>
</template>

<script>
import CaseTestRelate from "./CaseTestRelate";
import CaseRelationshipViewer from "./CaseRelationshipViewer";
import BaseEditItemComponent from "../BaseEditItemComponent";
import CaseDetailComponent from "./CaseDetailComponent";
import CaseBaseInfo from "./CaseBaseInfo";
import CaseChangeHistory from "./CaseChangeHistory";
import CaseIssueRelate from "./CaseIssueRelate";
import CaseCommentComponent from "./CaseCommentComponent";
import CaseCommentViewer from "./CaseCommentViewer";
import { getRelationshipCountCase } from "@/api/testCase";
import TabPaneCount from "@/business/plan/view/comonents/report/detail/component/TabPaneCount";
export default {
  name: "CaseEditInfoComponent",
  components: {
    CaseIssueRelate,
    CaseChangeHistory,
    CaseBaseInfo,
    CaseDetailComponent,
    BaseEditItemComponent,
    CaseCommentComponent,
    CaseCommentViewer,
    CaseRelationshipViewer,
    CaseTestRelate,
    TabPaneCount,
  },
  props: [
    "richTextDefaultOpen",
    "formLabelWidth",
    "editable",
    // other
    "form",
    "labelWidth",
    "caseId",
    "readOnly",
    "projectId",
    "isTestPlan",
    "planId",
    "versionEnable",
    "isCopy",
    "copyCaseId",
    "type",
    "comments",
    "isClickAttachmentTab",
    "defaultOpen",
    "edit",
    "editableState",
    "isPublicShow",
    "isReadonlyUser"
  ],
  data() {
    return {
      // since v2.6
      caseActiveName: "detail",

      result: {},
      fileList: [],
      tableData: [],
      demandOptions: [],
      relateCaseCount: 0,
      relateIssueCount: 0,
      relationshipCount: 0,
      demandValue: [],
      demandLabel: "",
      //sysList:this.sysList,//一级选择框的数据
      props: {
        multiple: true,
        //lazy: true,
        //lazyLoad:this.lazyLoad
      },
      intervalMap: new Map(),
      cancelFileToken: [],
      uploadFiles: [],
      relateFiles: [],
      unRelateFiles: [],
      dumpFile: {},
      commentState: "READY",
    };
  },
  computed: {
    isTesterPermission() {
      return true;
    },
    isCommentEdit() {
      return this.commentState == "EDIT";
    },
  },
  watch: {
    caseId() {
      getRelationshipCountCase(this.caseId).then((r) => {
        this.relationshipCount = r.data;
      });
    },
    demandValue() {
      if (this.demandValue.length > 0) {
        this.form.demandId = this.demandValue[this.demandValue.length - 1];
      } else {
        this.form.demandId = null;
      }
    },
  },
  methods: {
    getUploadFiles() {
      if (this.$refs.testCaseBaseInfo) {
        return this.$refs.testCaseBaseInfo.getUploadFiles();
      }
    },
    getRelateFiles() {
      if (this.$refs.testCaseBaseInfo) {
        return this.$refs.testCaseBaseInfo.getRelateFiles();
      }
    },
    getUnRelateFiles() {
      if (this.$refs.testCaseBaseInfo) {
        return this.$refs.testCaseBaseInfo.getUnRelateFiles();
      }
    },
    getFilterCopyFiles() {
      if (this.$refs.testCaseBaseInfo) {
        return this.$refs.testCaseBaseInfo.getFilterCopyFiles();
      }
    },
    getFileMetaData(id) {
      if (!this.$refs.testCaseBaseInfo) {
        return;
      }
      return this.$refs.testCaseBaseInfo.getFileMetaData(id);
    },
    validateForm() {
      let isValidate = true;
      if (this.$refs.testCaseBaseInfo) {
        isValidate = this.$refs["testCaseBaseInfo"].valideForm();
      }
      return isValidate;
    },
    tabClick(tab) {
      //初始化数据
      if (tab.name === "dependencies" && this.$refs.relationship) {
        this.$refs.relationship.open();
      } else if (tab.name === "associatedDefects") {
        this.$nextTick(() => {
          this.$refs.issue.getIssues();
        });
      } else if (tab.name === "associateTestCases") {
        this.$nextTick(() => {
          this.getRelatedTest();
        });
      } else if (tab.name === "changeRecord") {
        this.$refs.caseChangeHistoryRef.openHit();
      }
    },
    updateRemark(text) {
      this.form.remark = text;
    },
    openComment() {
      this.$emit("openComment");
    },
    getComments(testCase) {
      this.$emit("getComments", testCase);
    },
    handleCommentStateChange(state) {
      this.commentState = state;
    },
    toggleCommentTab() {
      this.caseActiveName = 'comment';
    },
    setRelationshipCount(count) {
      this.relationshipCount = count;
    },
    setRelateCaseCount(count) {
      this.relateCaseCount = count;
    },
    setRelateIssueCount(count) {
      this.relateIssueCount = count;
    },
    setRelationshipGraph(val) {
      this.$emit("syncRelationGraphOpen", val);
    },
    getRelatedTest() {
      if (this.$refs.relateTest) {
        this.$refs.relateTest.initTable();
      }
    },
    visibleChange(flag) {
      if (flag) {
        this.getDemandOptions();
      }
    },
    getDemandOptions() {
      this.result = { loading: true };
      this.demandLabel = "";
      issueDemandList(this.projectId)
        .then((r) => {
          this.demandOptions = [];
          if (r.data && r.data.length > 0) {
            this.buildDemandCascaderOptions(r.data, this.demandOptions, []);
          }
          this.addOtherOption();
        })
        .catch(() => {
          this.addOtherOption();
        });
    },
    addOtherOption() {
      this.demandOptions.unshift({
        value: "other",
        label: "Other: " + this.$t("test_track.case.other"),
        platform: "Other",
      });
      if (this.form.demandId === "other") {
        this.demandValue = ["other"];
        this.demandLabel = "Other: " + this.$t("test_track.case.other");
      }
      this.result = { loading: false };
    },
    buildDemandCascaderOptions(data, options, pathArray) {
      this.demandValue = [];
      data.forEach((item) => {
        let option = {
          label: item.platform + ": " + item.name,
          value: item.id,
        };
        if (this.form.demandId === item.id) {
          this.demandLabel = option.label;
        }
        options.push(option);
        pathArray.push(item.id);
        if (item.id === this.form.demandId) {
          this.demandValue = [...pathArray]; // 回显级联选项
        }
        if (item.children && item.children.length > 0) {
          option.children = [];
          this.buildDemandCascaderOptions(
            item.children,
            option.children,
            pathArray
          );
        }
        pathArray.pop();
      });
    },
    filterDemand(node, keyword) {
      if (
        keyword &&
        node.text.toLowerCase().indexOf(keyword.toLowerCase()) !== -1
      ) {
        return true;
      }
      return false;
    },
  },
};
</script>

<style scoped>
.other-info-tabs :deep(.el-tabs__content) {
  padding: 20px 0px;
}

.remark-item {
  padding: 0px 3px;
}

.el-cascader :deep(.el-input) {
  cursor: pointer;
}

.ms-header {
  background: #783887;
  color: white;
  height: 24px;
  width: 24px;
  font-size: xx-small;
  border-radius: 50%;
}

.demand-span {
  display: inline-block;
  max-width: 400px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  word-break: break-all;
  margin-right: 5px;
}

.demandInput {
  width: 200px;
}

.el-icon-plus {
  font-size: 16px;
}

.upload-default {
  background-color: #fbfdff;
  border: 1px dashed #c0ccda;
  border-radius: 6px;
  -webkit-box-sizing: border-box;
  box-sizing: border-box;
  width: 40px;
  height: 30px;
  line-height: 32px;
  vertical-align: top;
  text-align: center;
  cursor: pointer;
  display: inline-block;
}

.upload-default i {
  color: #8c939d;
}

.upload-default:hover {
  border: 1px dashed #783887;
}

.testplan-local-upload-tip {
  display: inline-block;
  position: relative;
  left: 25px;
  top: -5px;
}

.not-testplan-local-upload-tip {
  display: inline-block;
  position: relative;
  left: 25px;
  top: 8px;
}

.tab-container {
  height: calc(100vh - 240px);
}

.comment-edit-tab-container {
  height: calc(100vh - 240px - 160px);
}

.editable-container {
  height: calc(100vh - 200px);
}

.el-scrollbar {
  height: 100%;
}
</style>

<style scoped lang="scss">
@import "@/business/style/index.scss";

.edit-component :deep(.v-note-wrapper) {
  border: 1px solid #bbbfc4 !important;
}

.case-edit-wrap {
  :deep(.el-form-item__content) {
    line-height: px2rem(32);
  }
  .case-edit-box {
    width: px2rem(1328);
    min-height: px2rem(1001);
    /* margin-left: px2rem(34); */
    background-color: #fff;
    .edit-header-container {
      height: px2rem(56);
      width: 100%;
      border-bottom: 1px solid rgba(31, 35, 41, 0.15);
      display: flex;
      align-items: center;
      .header-content-row {
        display: flex;
        .back {
          margin-left: px2rem(24);
          width: px2rem(20);
          height: px2rem(20);
          img {
            width: 100%;
            height: 100%;
          }
        }

        .case-name {
          min-width: px2rem(64);
          height: px2rem(24);
          font-size: 16px;
          font-family: "PingFang SC";
          font-style: normal;
          font-weight: 500;
          line-height: px2rem(24);
          color: #1f2329;
          order: 1;
          flex-grow: 0;
          margin-left: px2rem(8);
        }

        .case-edit {
          .case-level {
          }

          .case-version {
            .version-icon {
            }

            .version-title {
            }
          }
        }
      }
    }

    .edit-content-container {
      width: 100%;
      height: 100%;
      display: flex;
      background-color: #fff;
      .required-item:after {
        content: "*";
        color: #f54a45;
        margin-left: px2rem(4);
        width: px2rem(8);
        height: 22px;
        font-weight: 400;
        font-size: 14px;
        line-height: 22px;
      }
      .content-body-wrap {
        // 1024 减去左右padding 各24 和 1px右边框
        width: 100%;
        height: 100%;
        .case-title-wrap {
          display: flex;
          margin-top: px2rem(24);
          margin-bottom: px2rem(8);
          .title-wrap {
            font-family: "PingFang SC";
            font-style: normal;
            font-weight: 500;
            font-size: 14px;
            color: #1f2329;
          }
        }
        .content-container {
          padding-left: px2rem(24);
          padding-right: px2rem(24);
        }
        .comment-common {
          bottom: 0px;
          width: 100%;
        }
        //公共样式
        .content-wrap {
          :deep(.v-note-op) {
            background-color: #f8f9fa !important;
            border-bottom: 1px solid #bbbfc4;
          }
          :deep(.v-note-wrapper) {
            box-sizing: border-box;
            border-radius: 4px;
            box-shadow: none !important;
          }
          :deep(.v-note-show) {
            min-height: 65px;
          }
          :deep(.v-left-item) {
            flex: none !important;
          }
        }

        .case-name-row {
          .content-wrap {
            display: flex;
            justify-content: center;
            width: 100%;
            .opt-row {
              width: 100%;
              height: 32px;
            }
          }
        }
        .pre-condition-row {
          .content-wrap {
            display: flex;
            justify-content: center;
            width: 100%;
            min-height: 100px;
            .opt-row {
              :deep(.el-form-item) {
                margin: 0;
              }
              width: 100%;
            }
          }
        }
        .remark-row {
          .content-wrap {
            display: flex;
            justify-content: center;
            width: 100%;
            min-height: 100px;
            .opt-row {
              width: 100%;
              :deep(.el-form-item) {
                margin: 0;
              }
            }
          }
        }
        .attachment-row {
          .attachment-name.case-title-wrap {
            .name.title-wrap {
            }
          }

          .content-wrap {
            .opt-btn {
            }

            .opt-tip {
              font-family: "PingFang SC";
              font-style: normal;
              font-weight: 400;
              font-size: 14px;
              line-height: 22px;
              /* identical to box height, or 157% */

              color: #8f959e;
            }
          }
        }
      }

      .content-base-info-wrap {
        width: px2rem(304);
        min-height: px2rem(864);
        border-left: 1px solid rgba(31, 35, 41, 0.15);
        .case-wrap {
          margin-left: px2rem(24);
          margin-top: px2rem(24);
        }
        .case-title-wrap {
          display: flex;
          .title-wrap {
            font-weight: 500;
            height: 22px;
            font-size: 14px;
            line-height: 22px;
            color: #1f2329;
          }
          margin-bottom: px2rem(8);
        }
        .side-content {
          width: px2rem(256);
          height: 32px;
          :deep(.el-select) {
            width: 100%;
          }
        }
      }
    }

    .edit-footer-container {
      display: flex;
      width: 100%;
      height: px2rem(80);
      background: #ffffff;
      box-shadow: 0px -1px 4px rgba(31, 35, 41, 0.1);
      align-items: center;
      font-family: "PingFang SC";
      font-style: normal;
      font-weight: 400;
      font-size: 14px;
      line-height: 22px;
      text-align: center;
      // 底部按钮激活样式
      .opt-active-primary {
        background: #783887;
        color: #ffffff;
      }
      .opt-disable-primary {
        background: #bbbfc4;
        color: #ffffff;
      }
      .opt-active {
        background: #ffffff;
        color: #1f2329;
      }
      .opt-disable {
        background: #ffffff;
        color: #bbbfc4;
      }

      .save-btn-row {
        margin-left: px2rem(24);
        el-button {
        }
      }

      .save-add-pub-row {
        margin-left: px2rem(12);
        el-button {
        }
      }

      .save-create-row {
        margin-left: px2rem(12);
        el-button {
        }
      }
    }
  }
}
</style>
<style>
.attachment-popover {
  padding: 0 !important;
  height: 80px;
  min-width: 120px !important;
}
</style>
