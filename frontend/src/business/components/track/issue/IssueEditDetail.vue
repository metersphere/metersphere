<template>
  <el-main v-loading="result.loading" class="container" :style="isCaseEdit ? '' : 'height: calc(100vh - 42px)'">
    <el-scrollbar>
      <el-form :model="form" :rules="rules" label-position="right" label-width="80px" ref="form">

        <ms-form-divider :title="$t('test_track.plan_view.base_info')"/>
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
          <custom-filed-form-item
            :form="customFieldForm"
            :default-open="richTextDefaultOpen"
            :form-label-width="formLabelWidth"
            :issue-template="issueTemplate"/>
        </el-form>

        <el-row v-if="platformTransitions">
          <el-col :span="8">
            <el-form-item :label-width="formLabelWidth" :label="$t('test_track.issue.platform_status')"
                          prop="platformStatus">
              <el-select v-model="form.platformStatus" filterable
                         :placeholder="$t('test_track.issue.please_choose_platform_status')">
                <el-option v-for="(transition, index) in platformTransitions" :key="index" :label="transition.lable"
                           :value="transition.value"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <form-rich-text-item
          v-if="!enableThirdPartTemplate"
          :title="$t('custom_field.issue_content')"
          :data="form"
          :default-open="richTextDefaultOpen"
          prop="description"/>

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

        <el-row class="other-info-rows" style="margin-top: 20px">
          <el-col :span="2">
            <ms-form-divider :title="$t('test_track.case.other_info')"/>
          </el-col>
          <el-col :span="22">
            <el-tabs class="other-info-tabs" v-loading="result.loading" v-model="tabActiveName">
              <el-tab-pane :label="$t('test_track.review_view.relevance_case')" name="relateTestCase">
                <el-form-item v-if="!isCaseEdit" style="margin-left: -80px">
                  <test-case-issue-list :issues-id="form.id"
                                        ref="testCaseIssueList"/>
                </el-form-item>
              </el-tab-pane>

              <el-tab-pane :label="$t('test_track.case.attachment')" name="attachment">
                <el-row>
                  <el-col :span="22">
                    <el-upload
                      multiple
                      :limit="8"
                      action=""
                      :auto-upload="true"
                      :file-list="fileList"
                      :show-file-list="false"
                      :before-upload="beforeUpload"
                      :http-request="handleUpload"
                      :on-exceed="handleExceed"
                      :on-success="handleSuccess"
                      :on-error="handleError"
                      :disabled="type === 'add' || type === 'copy'">
                      <el-button type="primary" :disabled="type === 'add' || type === 'copy'" size="mini">{{$t('test_track.case.add_attachment')}}</el-button>
                      <span slot="tip" class="el-upload__tip"> {{ $t('test_track.case.upload_tip') }} </span>
                    </el-upload>
                  </el-col>
                </el-row>
                <el-row style="margin-top: 10px">
                  <el-col :span="22">
                    <test-case-attachment :table-data="tableData"
                                          :read-only="readOnly"
                                          :is-delete="isDelete"
                                          :is-copy="type === 'copy'"
                                          @handleDelete="handleDelete"
                                          @handleCancel="handleCancel"/>
                  </el-col>
                </el-row>
              </el-tab-pane>

              <el-tab-pane :label="$t('test_track.review.comment')" name="comment">
                <el-tooltip class="item-tabs" effect="dark" :content="$t('test_track.review.comment')" placement="top-start"
                            slot="label">
                  <span>
                    {{ $t('test_track.review.comment') }}
                    <div class="el-step__icon is-text ms-api-col ms-header" v-if="comments && comments.length>0">
                      <div class="el-step__icon-inner">{{ comments.length }}</div>
                    </div>
                  </span>
                </el-tooltip>
                <el-row style="margin-top: 10px" v-if="type!=='add'">
                  <el-col :span="20" :offset="1">{{ $t('test_track.review.comment') }}:
                    <el-button icon="el-icon-plus" type="mini" @click="openComment"></el-button>
                  </el-col>
                </el-row>
                <el-row style="margin-top: 10px">
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
              </el-tab-pane>
            </el-tabs>
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
import {
  byteToSize,
  getCurrentProjectID,
  getCurrentUser,
  getCurrentUserId,
  getCurrentWorkspaceId, getTypeByFileName, hasLicense,
} from "@/common/js/utils";
import {enableThirdPartTemplate, getIssuePartTemplateWithProject, getPlatformTransitions} from "@/network/Issue";
import CustomFiledFormItem from "@/business/components/common/components/form/CustomFiledFormItem";
import MsMarkDownText from "@/business/components/track/case/components/MsMarkDownText";
import IssueComment from "@/business/components/track/issue/IssueComment";
import ReviewCommentItem from "@/business/components/track/review/commom/ReviewCommentItem";
import {TokenKey} from "@/common/js/constants";
import {Message} from "element-ui";
import TestCaseAttachment from "@/business/components/track/case/components/TestCaseAttachment";
import axios from "axios";

const {getIssuesById} = require("@/network/Issue");

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
    ReviewCommentItem,
    TestCaseAttachment
  },
  data() {
    return {
      type: null,
      issueId:'',
      result: {
        loading: false
      },
      relateFields: [],
      showFollow: false,
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
        zentaoAssigned: '',
        platformStatus: null,
        copyIssueId: ''
      },
      tapdUsers: [],
      zentaoUsers: [],
      Builds: [],
      hasTapdId: false,
      hasZentaoId: false,
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
      richTextDefaultOpen: 'preview',
      tabActiveName: 'relateTestCase',
      fileList: [],
      tableData: [],
      readOnly: false,
      isDelete: true,
      cancelFileToken: [],
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
  watch: {
    tabActiveName() {
      if (this.tabActiveName === 'attachment') {
        if (this.type === 'edit' && this.issueId) {
          this.getFileMetaData(this.issueId);
        }
      }
    },
  },
  methods: {
    resetForm() {
      this.form = {
        title: '',
        description: '',
        creator: null,
        remark: null,
        tapdUsers:[],
        zentaoBuilds:[],
        zentaoAssigned: '',
        platformStatus: null
      };
      this.customFieldForm = null;
      this.issueTemplate = {};
      if (this.$refs.testCaseIssueList) {
        this.$refs.testCaseIssueList.tableData = [];
      }
    },
    open(data, type) {
      this.tabActiveName = 'relateTestCase'
      this.showFollow = false;
      this.result.loading = true;
      this.type = type;
      this.richTextDefaultOpen = this.type === 'edit' ? 'preview' : 'edit';
      if (this.$refs.testCaseIssueList) {
        this.$refs.testCaseIssueList.clear();
        this.$refs.testCaseIssueList.isXpack = hasLicense();
      }
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
        });
        getIssuesById(data.id, (data) => {
          this.form.tapdUsers = data.tapdUsers;
          this.form.zentaoBuilds = data.zentaoBuilds;
          this.form.zentaoAssigned = data.zentaoAssigned;
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
        getPlatformTransitions(this.form.platformId, (data) => {
          if (data.length > 0) {
            this.platformTransitions = data;
          }
        });
      }

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
      } else if (platform === 'Tapd') {
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
      this.tableData = [];
      this.fileList = [];
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
          this.form.copyIssueId = data.copyIssueId;
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
        if (this.type === 'copy' && data.copyIssueId != null) {
          this.getFileMetaData(data.copyIssueId);
        } else if (this.type === 'edit' && data.id != null) {
          this.getFileMetaData(data.id);
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
      if (this.platformTransitions) {
        this.platformTransitions.forEach(item => {
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
      return param;
    },
    _save() {
      let param = this.buildPram();
      this.parseOldFields(param);
      let option = this.getOption(param);
      this.result = this.$request(option, (response) => {
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
    getOption(param) {
      let formData = new FormData();
      let requestJson = JSON.stringify(param, function (key, value) {
        return key === "file" ? undefined : value
      });

      formData.append('request', new Blob([requestJson], {
        type: "application/json"
      }));
      return {
        method: 'POST',
        url: this.url,
        data: formData,
        headers: {
          'Content-Type': undefined
        }
      };
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
    fileValidator(file) {
      return file.size < 500 * 1024 * 1024;
    },
    beforeUpload(file) {
      if (!this.fileValidator(file)) {
        this.$error(this.$t('load_test.file_size_out_of_bounds') + file.name);
        return false;
      }

      if (this.tableData.filter(f => f.name === file.name).length > 0) {
        this.$error(this.$t('load_test.delete_file') + ', name: ' + file.name);
        return false;
      }
    },
    handleUpload(e) {
      // 表格生成上传文件数据
      let file = e.file;
      let user = JSON.parse(localStorage.getItem(TokenKey));
      this.tableData.push({
        name: file.name,
        size: byteToSize(file.size),
        updateTime: new Date().getTime(),
        progress: 0,
        status: 0,
        creator: user.name,
        type: getTypeByFileName(file.name)
      });

      // 上传文件
      this.uploadFile(e, (param) => {
        this.showProgress(e.file, param)
      })
    },
    async uploadFile(param, progressCallback) {
      let file = param.file;
      let progress = 0;
      let formData = new FormData();
      let requestJson = JSON.stringify({"id": this.issueId});
      formData.append("file", file);
      formData.append('request', new Blob([requestJson], {
        type: "application/json"
      }));

      let CancelToken = axios.CancelToken
      let self = this;
      axios({
        headers: { 'Content-Type': 'application/json;charset=UTF-8' },
        method: 'post',
        url: '/issues/attachment/upload',
        data: formData,
        cancelToken: new CancelToken(function executor(c) {
          self.cancelFileToken.push({"name": file.name, "cancelFunc": c});
        }),
        onUploadProgress: progressEvent => { // 获取文件上传进度
          progress = (progressEvent.loaded / progressEvent.total * 100) | 0
          progressCallback({ progress, status: progress })
        }
      }).then(response => { // 成功回调
        progress = 100;
        param.onSuccess(response);
        progressCallback({progress, status: 'success'});
      }).catch(({error}) => { // 失败回调
        progress = 100;
        progressCallback({progress, status: 'error'});
      })
    },
    showProgress(file, params) {
      const { progress, status } = params
      const arr = [...this.tableData].map(item => {
        if (item.name === file.name) {
          item.progress = progress
          item.status = status
        }
        return item
      })
      this.tableData = [...arr]
    },
    handleExceed(files, fileList) {
      this.$error(this.$t('load_test.file_size_limit'));
    },
    handleSuccess(response, file, fileList) {
      let readyFiles = fileList.filter(item => item.status === 'success')
      if (readyFiles.length === fileList.length ) {
        this.getFileMetaData(this.issueId);
      }
    },
    handleError(err, file, fileList) {
      let readyFiles = fileList.filter(item => item.status === 'success')
      if (readyFiles.length === fileList.length ) {
        this.getFileMetaData(this.issueId);
      }
    },
    handleDownload(file) {
      let data = {
        name: file.name,
        id: file.id,
      };
      let config = {
        url: '/test/case/file/download',
        method: 'post',
        data: data,
        responseType: 'blob'
      };
      this.result = this.$request(config).then(response => {
        const content = response.data;
        const blob = new Blob([content]);
        if ("download" in document.createElement("a")) {
          // 非IE下载
          //  chrome/firefox
          let aTag = document.createElement('a');
          aTag.download = file.name;
          aTag.href = URL.createObjectURL(blob);
          aTag.click();
          URL.revokeObjectURL(aTag.href);
        } else {
          // IE10+下载
          navigator.msSaveBlob(blob, this.filename);
        }
      }).catch(e => {
        Message.error({message: e.message, showClose: true});
      });
    },
    handleDelete(file, index) {
      this.$alert(this.$t('load_test.delete_file_confirm') + file.name + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this._handleDelete(file, index);
          }
        }
      });
    },
    _handleDelete(file, index) {
      this.fileList.splice(index, 1);
      this.tableData.splice(index, 1);
      this.$get('/issues/attachment/delete/' + file.id, () => {
        this.$success(this.$t('commons.delete_success'));
        this.getFileMetaData(this.issueId);
      });
    },
    handleCancel(file, index) {
      this.fileList.splice(index, 1);
      let cancelToken = this.cancelFileToken.filter(f => f.name === file.name)[0];
      cancelToken.cancelFunc();
      let cancelFile = this.tableData.filter(f => f.name === file.name)[0];
      cancelFile.progress = 100;
      cancelFile.status = 'error';
    },
    getFileMetaData(id) {
      // 保存用例后传入用例id，刷新文件列表，可以预览和下载
      this.fileList = [];
      this.tableData = [];
      if (id) {
        this.result = this.$get("issues/file/attachmentMetadata/" + id, response => {
          let files = response.data;
          if (!files) {
            return;
          }
          // deep copy
          this.fileList = JSON.parse(JSON.stringify(files));
          this.tableData = JSON.parse(JSON.stringify(files));
          this.tableData.map(f => {
            f.size = byteToSize(f.size);
            f.status = 'success';
            f.progress = 100
          });
        });
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
.other-info-tabs >>> .el-tabs__content {
  padding: 20px 0px;
}

.top-input-class{
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
</style>
