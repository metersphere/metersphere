<template>
  <el-main v-loading="result.loading" class="container" :style="isCaseEdit ? '' : 'height: calc(100vh - 62px)'">
    <el-scrollbar>
      <el-form :model="form" :rules="rules" label-position="right" label-width="80px" ref="form">

        <el-form-item v-if="!enableThirdPartTemplate" :label="$t('commons.title')" prop="title">
          <el-row>
            <el-col  :span="22">
              <el-input v-model="form.title" autocomplete="off" class="top-input-class"></el-input>
            </el-col>
            <el-col  :span="2">
              <el-tooltip :content="$t('commons.follow')" placement="bottom"  effect="dark" v-if="!showFollow">
                <i class="el-icon-star-off" style="color: #783987; font-size: 25px; margin-left: 15px;cursor: pointer;position: relative;top: 5px" @click="saveFollow" />
              </el-tooltip>
              <el-tooltip :content="$t('commons.cancel')" placement="bottom"  effect="dark" v-if="showFollow" >
                <i class="el-icon-star-on" style="color: #783987; font-size: 28px; margin-left: 15px; cursor: pointer;position: relative;top: 5px" @click="saveFollow" />
              </el-tooltip>
            </el-col>
          </el-row>
        </el-form-item>
        <div v-else style="text-align: right; margin-bottom: 5px">
          <el-tooltip :content="$t('commons.follow')" placement="bottom"  effect="dark" v-if="!showFollow">
            <i class="el-icon-star-off" style="color: #783987; font-size: 25px; margin-left: 15px;cursor: pointer;position: relative;top: 5px" @click="saveFollow" />
          </el-tooltip>
          <el-tooltip :content="$t('commons.cancel')" placement="bottom"  effect="dark" v-if="showFollow" >
            <i class="el-icon-star-on" style="color: #783987; font-size: 28px; margin-left: 15px; cursor: pointer;position: relative;top: 5px" @click="saveFollow" />
          </el-tooltip>
        </div>

        <!-- 自定义字段 -->
        <el-form :model="customFieldForm" :rules="customFieldRules" ref="customFieldForm" class="case-form">
          <custom-filed-form-item :form="customFieldForm" :form-label-width="formLabelWidth" :issue-template="issueTemplate"/>
        </el-form>

        <form-rich-text-item v-if="!enableThirdPartTemplate" :title="$t('custom_field.issue_content')" :data="form" prop="description"/>

        <el-row v-if="!enableThirdPartTemplate" class="custom-field-row">
          <el-col :span="8" v-if="hasTapdId">
            <el-form-item :label-width="formLabelWidth" :label="$t('test_track.issue.tapd_current_owner')"
                          prop="tapdUsers">
              <el-select v-model="form.tapdUsers" multiple filterable
                         :placeholder="$t('test_track.issue.please_choose_current_owner')">
                <el-option v-for="(userInfo, index) in tapdUsers" :key="index" :label="userInfo.user"
                           :value="userInfo.user"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8" v-if="hasZentaoId">
            <el-form-item :label-width="formLabelWidth" :label="$t('test_track.issue.zentao_bug_build')"
                          prop="zentaoBuilds">
              <el-select v-model="form.zentaoBuilds" multiple filterable
                         :placeholder="$t('test_track.issue.zentao_bug_build')">
                <el-option v-for="(build, index) in Builds" :key="index" :label="build.name"
                           :value="build.id"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8" v-if="hasZentaoId">
            <el-form-item :label-width="formLabelWidth" :label="$t('test_track.issue.zentao_bug_assigned')"
                          prop="zentaoAssigned">
              <el-select v-model="form.zentaoAssigned" filterable
                         :placeholder="$t('test_track.issue.please_choose_current_owner')">
                <el-option v-for="(userInfo, index) in zentaoUsers" :key="index" :label="userInfo.name"
                           :value="userInfo.user"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item v-if="!isCaseEdit">
          <test-case-issue-list :issues-id="form.id"
                                ref="testCaseIssueList"/>
        </el-form-item>

<!--        <form-rich-text-item :title="$t('commons.remark')" :data="form" prop="remark"/>-->

        <el-row style="margin-top: 10px" v-if="type!=='add'">
          <el-col :span="20" :offset="1">{{ $t('test_track.review.comment') }}:
            <el-button icon="el-icon-plus" type="mini" @click="openComment"></el-button>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="20" :offset="1">

            <review-comment-item v-for="(comment,index) in comments"
                                 :key="index"
                                 :comment="comment"
                                 @refresh="getComments" api-url="/issues"/>
            <div v-if="comments.length === 0" style="text-align: center">
              <i class="el-icon-chat-line-square" style="font-size: 15px;color: #8a8b8d;">
                      <span style="font-size: 15px; color: #8a8b8d;">
                        {{ $t('test_track.comment.no_comment') }}
                      </span>
              </i>
            </div>
          </el-col>
        </el-row>

        <issue-comment :issues-id="form.id"
                       @getComments="getComments"
                       ref="issueComment"/>
      </el-form>
    </el-scrollbar>

  </el-main>
</template>

<script>

import TemplateComponentEditHeader
  from "@/business/components/track/plan/view/comonents/report/TemplateComponentEditHeader";
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import CustomFieldFormList from "@/business/components/project/template/CustomFieldFormList";
import CustomFieldRelateList from "@/business/components/project/template/CustomFieldRelateList";
import FormRichTextItem from "@/business/components/track/case/components/FormRichTextItem";
import {buildCustomFields, parseCustomField} from "@/common/js/custom_field";
import CustomFiledComponent from "@/business/components/project/template/CustomFiledComponent";
import TestCaseIssueList from "@/business/components/track/issue/TestCaseIssueList";
import IssueEditDetail from "@/business/components/track/issue/IssueEditDetail";
import {getCurrentProjectID, getCurrentUser, getCurrentUserId, getCurrentWorkspaceId,} from "@/common/js/utils";
import {enableThirdPartTemplate, getIssuePartTemplateWithProject} from "@/network/Issue";
import CustomFiledFormItem from "@/business/components/common/components/form/CustomFiledFormItem";
import MsMarkDownText from "@/business/components/track/case/components/MsMarkDownText";
import IssueComment from "@/business/components/track/issue/IssueComment";
import ReviewCommentItem from "@/business/components/track/review/commom/ReviewCommentItem";

export default {
  name: "IssueEditDetail",
  components: {
    CustomFiledFormItem,
    IssueEditDetail,
    TestCaseIssueList,
    CustomFiledComponent,
    FormRichTextItem,
    CustomFieldRelateList,
    CustomFieldFormList,
    MsFormDivider,
    TemplateComponentEditHeader,
    MsMarkDownText,
    IssueComment,
    ReviewCommentItem
  },
  data() {
    return {
      type: null,
      issueId:'',
      result: {
        loading: false
      },
      relateFields: [],
      showFollow:false,
      formLabelWidth: "150px",
      issueTemplate: {},
      customFieldForm: null,
      customFieldRules: {},
      rules: {
        title: [
          {required: true, message: this.$t('commons.please_fill_content'), trigger: 'blur'},
          {max: 300, message: this.$t('test_track.length_less_than') + '300', trigger: 'blur'}
        ],
        description: [
          {required: true, message: this.$t('commons.please_fill_content'), trigger: 'blur'},
        ]
      },
      url: '',
      form: {
        title: '',
        description: '',
        creator: null,
        remark: null,
        tapdUsers:[],
        zentaoBuilds:[],
        zentaoAssigned: ''
      },
      tapdUsers: [],
      zentaoUsers: [],
      Builds: [],
      hasTapdId: false,
      hasZentaoId: false,
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
      comments: []
    };
  },
  props: {
    isCaseEdit: {
      type: Boolean,
      default() {
        return false;
      }
    },
    caseId: String,
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
    enableThirdPartTemplate() {
      return enableThirdPartTemplate(this.currentProject);
    },
  },
  methods: {
    open(data, type) {
      this.result.loading = true;
      this.type = type;
      this.$nextTick(() => {
        getIssuePartTemplateWithProject((template, project) => {
          this.currentProject = project;
          this.init(template, data);
        });
      });

      if (data && data.id) {
        this.$get('/issues/follow/' + data.id, response => {
          this.form.follows = response.data;
          for (let i = 0; i < response.data.length; i++) {
            if (response.data[i] === this.currentUser().id) {
              this.showFollow = true;
              break;
            }
          }
        })
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
      this.getThirdPartyInfo();
      this.initEdit(data);
      this.result.loading = false;
    },
    getThirdPartyInfo() {
      let platform = this.issueTemplate.platform;
      if (platform === 'Zentao') {
        this.hasZentaoId = true;
        this.result = this.$post("/issues/zentao/builds", {
          projectId: this.projectId,
          workspaceId: getCurrentWorkspaceId()
        }, response => {
          if (response.data) {
            this.Builds = response.data;
          }
          this.result = this.$post("/issues/zentao/user", {
            projectId: this.projectId,
            workspaceId: getCurrentWorkspaceId()
          }, response => {
            this.zentaoUsers = response.data;
          });
        });
      }
      if (platform === 'Tapd') {
        this.hasTapdId = true;
        this.result = this.$post("/issues/tapd/user", {
          projectId: this.projectId,
          workspaceId: getCurrentWorkspaceId()
        }, (response) => {
          this.tapdUsers = response.data;
        });
      }
    },
    initEdit(data) {
      if (data) {
        Object.assign(this.form, data);
        if (!(data.options instanceof Array)) {
          this.form.options = data.options ? JSON.parse(data.options) : [];
        }
        if (data.id) {
          this.issueId = data.id
          this.url = 'issues/update';
        } else {
          //copy
          this.url = 'issues/add';
          if (!this.form.creator) {
            this.form.creator = getCurrentUserId();
          }
          this.form.title = data.title + '_copy';
        }
      } else {
        this.form = {
          title: this.issueTemplate.title,
          description: this.issueTemplate.content
        };
        this.url = 'issues/add';
        if (!this.form.creator) {
          this.form.creator = getCurrentUserId();
        }
      }
      this.customFieldForm = parseCustomField(this.form, this.issueTemplate, this.customFieldRules);
      this.comments = [];
      this.$nextTick(() => {
        if (this.$refs.testCaseIssueList) {
          this.$refs.testCaseIssueList.initTableData();
        }
        this.getComments();
      });
    },
    save() {
      let isValidate = true;
      this.$refs['form'].validate((valid) => {
        if (!valid) {
          isValidate = false;
          return false;
        }
      });
      this.$refs['customFieldForm'].validate((valid) => {
        if (!valid) {
          isValidate = false;
          return false;
        }
      });
      if (isValidate) {
        this._save();
      }
    },
    buildPram() {
      let param = {};
      Object.assign(param, this.form);
      param.projectId = this.projectId;
      param.workspaceId = getCurrentWorkspaceId();
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
      return param;
    },
    _save() {
      let param = this.buildPram();
      this.parseOldFields(param);
      this.result = this.$post(this.url, param, (response) => {
        this.$emit('close');
        this.$success(this.$t('commons.save_success'));
        this.$emit('refresh', response.data);
      });
    },
    parseOldFields(param) {
      let customFieldsStr = param.customFields;
      if (customFieldsStr) {
        let customFields = JSON.parse(customFieldsStr);
        customFields.forEach(item => {
          if (item.name === '状态') {
            param.status = item.value;
          }
        });
      }
    },
    saveFollow(){
      if(!this.form.follows){
        this.form.follows = [];
      }
      if(this.showFollow){
        this.showFollow = false;
        for (let i = 0; i < this.form.follows.length; i++) {
          if(this.form.follows[i]===this.currentUser().id){
            this.form.follows.splice(i,1)
            break;
          }
        }
        if(this.url === "issues/update"){
          this.$post("issues/up/follows/"+this.issueId, this.form.follows,() => {
            this.$success(this.$t('commons.cancel_follow_success'));
          });
        }

      }else {
        this.showFollow = true;
        if(!this.form.follows){
          this.form.follows = [];
        }
        this.form.follows.push(this.currentUser().id)
        if(this.url === "issues/update"){
          this.$post("issues/up/follows/"+this.issueId, this.form.follows,() => {
            this.$success(this.$t('commons.follow_success'));
          });
        }
      }
    },
    openComment() {
      if (!this.issueId) {
        this.$warning(this.$t('test_track.issue.save_before_open_comment'));
        return;
      }
      this.$refs.issueComment.open();
    },
    getComments() {
      if (this.issueId) {
        this.result = this.$get('/issues/comment/list/' + this.issueId, res => {
          this.comments = res.data;
        })
      }
    }
  }
};
</script>

<style scoped>
.top-input-class{
  width: 100%;
}

.filed-list {
  margin-top: 30px;
}

.custom-field-row {
  padding-left: 18px;
}
</style>
