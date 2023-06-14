<template>
  <div class="add-issue-box" v-loading="this.result.loading">
    <el-form
      :model="form"
      :rules="rules"
      label-position="top"
      label-width="80px"
      :hide-required-asterisk="true"
      ref="form"
    >
      <!-- 标题 -->
      <div v-if="!enableThirdPartTemplate">
        <div class="title-row" v-if="!(this.issueTemplate.platform === 'Local')">
          <el-form-item :label="$t('commons.title')" prop="title">
            <div slot="label" class="required-item">
              {{ $t("commons.title") }}
            </div>
            <el-input
              v-model="form.title"
              autocomplete="off"
              class="top-input-class"
              maxlength="255"
              show-word-limit
            >
            </el-input>
          </el-form-item>
        </div>

        <div class="title-row" v-else>
          <el-form-item :label="$t('commons.title')" prop="title">
            <div slot="label" class="required-item">
              {{ $t("commons.title") }}
            </div>
            <el-input
              v-model="form.title"
              autocomplete="off"
              class="top-input-class"
              maxlength="300"
              show-word-limit
            >
            </el-input>
          </el-form-item>
        </div>
      </div>

      <!-- 自定义字段 -->
      <div class="custom-field-wrap">
        <el-form
          :model="customFieldForm"
          :rules="customFieldRules"
          ref="customFieldForm"
          class="case-form"
        >
          <custom-filed-form-item
            :form="customFieldForm"
            :default-open="richTextDefaultOpen"
            :form-label-width="formLabelWidth"
            :issue-template="issueTemplate"
            class="custom-case-form"
            @inputSearch="handleInputSearch"
            ref="customFieldItem"
          />
        </el-form>
        <!-- 未开启第三方 -->
        <div class="plat-form-trans-wrap split-wrap" v-if="platformTransitions">
          <el-form-item
            :label-width="formLabelWidth"
            :label="$t('test_track.issue.platform_status')"
            prop="platformStatus"
          >
            <el-select
              v-model="form.platformStatus"
              filterable
              :placeholder="
                $t('test_track.issue.please_choose_platform_status')
              "
            >
              <el-option
                v-for="(transition, index) in platformTransitions"
                :key="index"
                :label="transition.label"
                :value="transition.value"
              />
            </el-select>
          </el-form-item>
        </div>
        <div
          class="not-enable-third-wrap split-wrap"
          v-if="!enableThirdPartTemplate && hasTapdId"
        >
          <el-form-item
            :label-width="formLabelWidth"
            :label="$t('test_track.issue.tapd_current_owner')"
            prop="tapdUsers"
          >
            <el-select
              v-model="form.tapdUsers"
              multiple
              filterable
              :placeholder="$t('test_track.issue.please_choose_current_owner')"
            >
              <el-option
                v-for="(userInfo, index) in tapdUsers"
                :key="index"
                :label="userInfo.user"
                :value="userInfo.user"
              />
            </el-select>
          </el-form-item>
        </div>
      </div>
      <div class="content-wrap">
        <form-rich-text-item
          v-if="!enableThirdPartTemplate"
          :slotTitleRequired="$t('custom_field.issue_content')"
          :data="form"
          :default-open="richTextDefaultOpen"
          :placeholder="$t('case.enter_issues_content')"
          prop="description"
        />
      </div>
      <el-form-item :label="$t('test_track.case.attachment')"></el-form-item>
    </el-form>

    <!-- 附件 -->
    <div class="atta-wrap">
      <case-attachment-component
        :editable="true"
        :caseId="caseId"
        type="add"
        :isCopy="type === 'copy'"
        :copyCaseId="copyCaseId"
        :readOnly="readOnly"
        :projectId="projectId"
        :isDelete="isDelete"
        belongType="issue"
        :issueId="issueId"
        ref="attachmentComp"
      ></case-attachment-component>
    </div>
  </div>
</template>

<script>
import CaseAttachmentComponent from "@/business/case/components/case/CaseAttachmentComponent";
import TemplateComponentEditHeader from "@/business/plan/view/comonents/report/TemplateComponentEditHeader";
import MsFormDivider from "metersphere-frontend/src/components/MsFormDivider";
import FormRichTextItem from "../richtext/FormRichTextItem";
import {buildCustomFields, parseCustomField,} from "metersphere-frontend/src/utils/custom_field";
import CustomFiledComponent from "metersphere-frontend/src/components/template/CustomFiledComponent";
import TestCaseIssueList from "@/business/issue/TestCaseIssueList";
import IssueEditDetail from "@/business/issue/IssueEditDetail";
import {
  getCurrentProjectID,
  getCurrentUser,
  getCurrentUserId,
  getCurrentWorkspaceId,
} from "metersphere-frontend/src/utils/token";
import {hasLicense} from "metersphere-frontend/src/utils/permission";
import {
  enableThirdPartTemplate,
  getFollow,
  getIssuePartTemplateWithProject,
  getPlatformFormOption,
  getPlatformTransitions,
  getTapdUser,
  saveOrUpdateIssue,
} from "@/api/issue";
import CustomFiledFormItem from "../common/CaseCustomFiledFormItem";
import MsMarkDownText from "metersphere-frontend/src/components/MsMarkDownText";
import IssueComment from "@/business/issue/IssueComment";
import ReviewCommentItem from "@/business/review/commom/ReviewCommentItem";
import TestCaseAttachment from "@/business/case/components/TestCaseAttachment";
import MsFileBatchMove from "metersphere-frontend/src/components/environment/commons/variable/FileBatchMove";

export default {
  name: "CaseIssueEditDetail",
  components: {
    CustomFiledFormItem,
    IssueEditDetail,
    TestCaseIssueList,
    CustomFiledComponent,
    FormRichTextItem,
    MsFormDivider,
    TemplateComponentEditHeader,
    MsMarkDownText,
    IssueComment,
    ReviewCommentItem,
    TestCaseAttachment,
    MsFileBatchMove,
    CaseAttachmentComponent,
  },
  data() {
    return {
      type: null,
      issueId: "",
      result: {
        loading: false,
      },
      relateFields: [],
      showFollow: false,
      formLabelWidth: "150px",
      issueTemplate: {},
      customFieldForm: null,
      customFieldRules: {},
      rules: {
        title: [
          {
            required: true,
            message: this.$t("commons.please_fill_content"),
            trigger: "blur",
          },
          {
            max: 300,
            message: this.$t("test_track.length_less_than") + "300",
            trigger: "blur",
          },
        ],
        description: [
          {
            required: true,
            message: this.$t("commons.please_fill_content"),
            trigger: "blur",
          },
        ],
      },
      url: "",
      form: {
        title: "",
        description: "",
        creator: null,
        remark: null,
        tapdUsers: [],
        platformStatus: null,
        copyIssueId: "",
      },
      tapdUsers: [],
      hasTapdId: false,
      platformTransitions: null,
      currentProject: null,
      toolbars: {
        bold: false, // 粗体
        italic: false, // 斜体
        header: false, // 标题
        underline: false, // 下划线
        strikethrough: false, // 中划线
        mark: false, // 标记
        superscript: false, // 上角标
        subscript: false, // 下角标
        quote: false, // 引用
        ol: false, // 有序列表
        ul: false, // 无序列表
        link: false, // 链接
        imagelink: true, // 图片链接
        code: false, // code
        table: false, // 表格
        fullscreen: false, // 全屏编辑
        readmodel: false, // 沉浸式阅读
        htmlcode: false, // 展示html源码
        help: false, // 帮助
        /* 1.3.5 */
        undo: false, // 上一步
        redo: false, // 下一步
        trash: false, // 清空
        save: false, // 保存（触发events中的save事件）
        /* 1.4.2 */
        navigation: false, // 导航目录
        /* 2.1.8 */
        alignleft: false, // 左对齐
        aligncenter: false, // 居中
        alignright: false, // 右对齐
        /* 2.2.1 */
        subfield: false, // 单双栏模式
        preview: false, // 预览
      },
      comments: [],
      richTextDefaultOpen: "preview",
      tabActiveName: "relateTestCase",
      fileList: [],
      tableData: [],
      readOnly: false,
      isDelete: true,
      enableThirdPartTemplate: false,
    };
  },
  props: {
    isCaseEdit: {
      type: Boolean,
      default() {
        return false;
      },
    },
    caseId: String,
    copyCaseId: String,
    planId: String,
    planCaseId: String,
    isMinder: Boolean,
  },
  computed: {
    isSystem() {
      return this.form.system;
    },
    projectId() {
      return getCurrentProjectID();
    },
  },
  watch: {
    tabActiveName() {
      if (this.tabActiveName === "attachment") {
        if (this.type === "edit" && this.issueId) {
          this.getFileMetaData(this.issueId);
        }
      }
    },
  },
  methods: {
    resetForm() {
      this.form = {
        title: "",
        description: "",
        creator: getCurrentUserId(),
        remark: null,
        tapdUsers: [],
        platformStatus: null,
      };
      if (this.$refs.testCaseIssueList) {
        this.$refs.testCaseIssueList.tableData = [];
      }
      this.$refs.form.clearValidate();
    },
    open(data, type) {
      this.tabActiveName = "relateTestCase";
      this.showFollow = false;
      this.result.loading = true;
      this.type = type;
      this.richTextDefaultOpen = this.type === "edit" ? "preview" : "edit";
      if (this.$refs.testCaseIssueList) {
        this.$refs.testCaseIssueList.clear();
        this.$refs.testCaseIssueList.isXpack = hasLicense();
      }

      enableThirdPartTemplate(this.projectId).then((r) => {
        this.enableThirdPartTemplate = r.data;
      });

      this.$nextTick(() => {
        getIssuePartTemplateWithProject((template, project) => {
          this.currentProject = project;
          this.init(template, data);
          this.getDataInfoAsync(data);
        });
      });
    },
    getDataInfoAsync(data) {
      if (data && data.id) {
        getFollow(data.id).then((response) => {
          this.form.follows = response.data;
          for (let i = 0; i < response.data.length; i++) {
            if (response.data[i] === this.currentUser().id) {
              this.showFollow = true;
              break;
            }
          }
        });
      } else {
        this.issueId = null;
        this.form.follows = [];
      }
    },
    currentUser: () => {
      return getCurrentUser();
    },
    init(template, data) {
      this.issueTemplate = template;
      this.initEdit(data);
      this.getThirdPartyInfo();
      this.result.loading = false;
    },
    getThirdPartyInfo() {
      let platform = this.issueTemplate.platform;

      this.platformTransitions = null;
      if (this.form.platformId) {
        let data = {
          platformKey: this.form.platformId,
          projectId: getCurrentProjectID(),
          workspaceId: getCurrentWorkspaceId(),
        };
        getPlatformTransitions(data).then((response) => {
          if (response.data.length > 0) {
            this.platformTransitions = response.data;
          }
        });
      }

      let data = {
        projectId: this.projectId,
        workspaceId: getCurrentWorkspaceId(),
      };
      if (platform === "Tapd") {
        this.hasTapdId = true;
        getTapdUser(data).then((response) => {
          this.tapdUsers = response.data;
        });
      }
    },
    initEdit(data) {
      this.tableData = [];
      this.fileList = [];
      if (data) {
        Object.assign(this.form, data);
        if (!(data.options instanceof Array)) {
          this.form.options = data.options ? JSON.parse(data.options) : [];
        }
        if (data.id) {
          this.issueId = data.id;
          this.url = "issues/update";
        } else {
          //copy
          this.url = "issues/add";
          if (!this.form.creator) {
            this.form.creator = getCurrentUserId();
          }
          this.form.title = data.title + "_copy";
          this.form.copyIssueId = data.copyIssueId;
        }
      } else {
        this.form = {
          title: this.issueTemplate.title,
          description: this.issueTemplate.content,
        };
        this.url = "issues/add";
        if (!this.form.creator) {
          this.form.creator = getCurrentUserId();
        }
      }
      this.customFieldForm = parseCustomField(
        this.form,
        this.issueTemplate,
        this.customFieldRules
      );
      this.comments = [];
      this.$nextTick(() => {
        if (this.$refs.testCaseIssueList) {
          this.$refs.testCaseIssueList.initTableData();
        }
        if (this.type === "copy" && data.copyIssueId != null) {
          this.getFileMetaData(data.copyIssueId);
        } else if (this.type === "edit" && data.id != null) {
          this.getFileMetaData(data.id);
        }
        // this.getComments();
      });
    },
    handleInputSearch(data, query) {
      getPlatformFormOption({
        optionMethod: data.optionMethod,
        workspaceId: getCurrentWorkspaceId(),
        platform: this.issueTemplate.platform,
        projectId: this.form.projectId ? this.form.projectId : this.projectId,
        query
      }).then((r) => {
        data.options = r.data;
        this.$refs.customFieldItem.stopLoading();
      });
    },
    save(reset) {
      let isValidate = true;
      this.$refs["form"].validate((valid) => {
        if (!valid) {
          isValidate = false;
          return false;
        }
      });
      this.$refs["customFieldForm"].validate((valid) => {
        if (!valid) {
          isValidate = false;
          return false;
        }
      });
      if (isValidate) {
        this._save(reset);
      }
    },
    buildParam() {
      let param = {};
      Object.assign(param, this.form);
      param.projectId = this.projectId;
      param.workspaceId = getCurrentWorkspaceId();
      if (this.platformTransitions) {
        this.platformTransitions.forEach((item) => {
          if (item.value === this.form.platformStatus) {
            param.transitions = item;
          }
        });
      }
      buildCustomFields(this.form, param, this.issueTemplate);
      if (this.planId) {
        // 测试计划用例创建缺陷
        if (!this.form.id) {
          param.addResourceIds = [this.planCaseId];
          param.refId = this.caseId;
          param.isPlanEdit = true;
        }
      } else {
        if (this.isCaseEdit) {
          // 功能用例创建缺陷
          if (!this.form.id) {
            param.addResourceIds = [this.caseId];
          }
        } else {
          // 缺陷管理创建缺陷
          param.addResourceIds = [...this.$refs.testCaseIssueList.addIds];
          param.deleteResourceIds = [...this.$refs.testCaseIssueList.deleteIds];
        }
      }
      if (this.planId) {
        param.resourceId = this.planId;
      }

      param.withoutTestCaseIssue = this.isMinder;
      param.thirdPartPlatform = this.enableThirdPartTemplate;
      if (this.$refs.attachmentComp.relateFiles.length > 0) {
        param.relateFileMetaIds = this.$refs.attachmentComp.relateFiles;
      }
      return param;
    },
    _save(reset) {
      let param = this.buildParam();
      this.parseOldFields(param);
      let option = this.getOption(param);
      saveOrUpdateIssue(option.url, option.data).then((response) => {
        if (reset) {
          this.resetForm();
        } else {
          this.$emit("close");
        }
        this.$success(this.$t("commons.save_success"), false);
        this.$emit("refresh", response.data);
      });
    },
    parseOldFields(param) {
      let customFieldsStr = param.customFields;
      if (customFieldsStr) {
        let customFields = JSON.parse(customFieldsStr);
        customFields.forEach((item) => {
          if (item.name === "状态") {
            param.status = item.value;
          }
        });
      }
    },
    getOption(param) {
      let formData = new FormData();
      let requestJson = JSON.stringify(param, function (key, value) {
        return key === "file" ? undefined : value;
      });
      if (this.$refs.attachmentComp.uploadFiles.length > 0) {
        this.$refs.attachmentComp.uploadFiles.forEach((f) => {
          formData.append("file", f);
        });
      }
      formData.append(
        "request",
        new Blob([requestJson], {
          type: "application/json",
        })
      );

      return {
        method: "POST",
        url: this.url,
        data: formData,
        headers: {
          "Content-Type": undefined,
        },
      };
    }
  },
};
</script>

<style scoped>
.other-info-tabs :deep(.el-tabs__content) {
  padding: 20px 0px;
}

.other-info-tabs {
  margin-left: 20px;
}

.top-input-class {
  width: 100%;
}

.filed-list {
  margin-top: 30px;
}

.custom-field-row {
  padding-left: 18px;
}

.ms-header {
  background: #783887;
  color: white;
  height: 18px;
  width: 18px;
  font-size: xx-small;
  border-radius: 50%;
}

.el-icon-plus {
  font-size: 16px;
}

/* .upload-default {
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
} */

.local-upload-tips {
  display: inline-block;
  position: relative;
  left: 25px;
  top: 8px;
}
</style>
<style scoped lang="scss">
@import "@/business/style/index.scss";
:deep(.el-form-item__label) {
  /* padding: 0 0 0 8px !important; */
}

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
  :deep(.el-cascader) {
    width: 100%;
  }
}

.required-item:after {
  content: "*";
  color: #f54a45;
  margin-left: px2rem(4);
  width: px2rem(8);
  height: 32px;
  font-weight: 400;
  font-size: 14px;
  line-height: 32px;
}
.required-item {
  font-weight: 500;
  font-size: 14px;
  color: #1f2329;
}

.add-issue-box {
  padding: 0 24px 24px 24px;
  overflow-y: scroll;
  max-height: calc(100vh - 200px)
}
.atta-wrap {
  width: 100%;
}
.custom-field-wrap {
  display: flex;
  flex-wrap: wrap;

  :deep(.el-form-item:not(:first-child)) {
    width: 301px;
    margin-right: 12px;
  }
  :deep(.el-form-item__content > div) {
    width: 301px;
  }
  // .el-form-item:not(:first-child):not(:last-child) {
  //   margin: 0 24px; /* 设置左右间距为 24px */
  // }

  // .el-form-item:nth-child(3n + 1) {
  //   margin-left: 0; /* 第一个元素不需要左边距 */
  // }

  // .el-form-item:nth-child(3n) {
  //   margin-right: 0; /* 最后一个元素不需要右边距 */
  // }
}

:deep(.opt-tip) {
  width: 300px;
}
:deep(.viewer-box) {
  width: px2rem(900);
}

</style>
