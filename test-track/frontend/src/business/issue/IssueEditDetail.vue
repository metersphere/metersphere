<template>
  <el-main v-loading="result.loading" class="container" :style="isCaseEdit ? '' : 'height: calc(100vh - 42px)'">
    <el-scrollbar>
      <el-form :model="form" :rules="rules" label-position="right" label-width="80px" ref="form">

        <ms-form-divider :title="$t('test_track.plan_view.base_info')"/>
        <el-form-item v-if="!enableThirdPartTemplate" :label="$t('commons.title')" prop="title">
          <el-row>
            <el-col :span="22">
              <el-input v-model="form.title" autocomplete="off" class="top-input-class"></el-input>
            </el-col>
            <el-col :span="2">
              <el-tooltip :content="$t('commons.follow')" placement="bottom" effect="dark" v-if="!showFollow">
                <i class="el-icon-star-off"
                   style="color: #783987; font-size: 25px; margin-left: 15px;cursor: pointer;position: relative;top: 5px"
                   @click="saveFollow"/>
              </el-tooltip>
              <el-tooltip :content="$t('commons.cancel')" placement="bottom" effect="dark" v-if="showFollow">
                <i class="el-icon-star-on"
                   style="color: #783987; font-size: 28px; margin-left: 15px; cursor: pointer;position: relative;top: 5px"
                   @click="saveFollow"/>
              </el-tooltip>
            </el-col>
          </el-row>
        </el-form-item>
        <div v-else style="text-align: right; margin-bottom: 5px">
          <el-tooltip :content="$t('commons.follow')" placement="bottom" effect="dark" v-if="!showFollow">
            <i class="el-icon-star-off"
               style="color: #783987; font-size: 25px; margin-left: 15px;cursor: pointer;position: relative;top: 5px"
               @click="saveFollow"/>
          </el-tooltip>
          <el-tooltip :content="$t('commons.cancel')" placement="bottom" effect="dark" v-if="showFollow">
            <i class="el-icon-star-on"
               style="color: #783987; font-size: 28px; margin-left: 15px; cursor: pointer;position: relative;top: 5px"
               @click="saveFollow"/>
          </el-tooltip>
        </div>

        <!-- 自定义字段 -->
        <el-form :model="customFieldForm" :rules="customFieldRules" ref="customFieldForm" class="case-form">
          <custom-filed-form-item
            :form="customFieldForm"
            :default-open="richTextDefaultOpen"
            :form-label-width="formLabelWidth"
            :issue-template="issueTemplate"
            @inputSearch="handleInputSearch"
            ref="customFieldItem"
          />
        </el-form>

        <el-row v-if="platformTransitions">
          <el-col :span="8">
            <el-form-item :label-width="formLabelWidth" :label="$t('test_track.issue.platform_status')"
                          prop="platformStatus">
              <el-select v-model="form.platformStatus" filterable
                         :placeholder="$t('test_track.issue.please_choose_platform_status')">
                <el-option v-for="(transition, index) in platformTransitions" :key="index" :label="transition.label"
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
        </el-row>

        <ms-form-divider :title="$t('test_track.case.other_info')"/>

        <el-tabs class="other-info-tabs" v-loading="result.loading" v-model="tabActiveName">
          <el-tab-pane :label="$t('test_track.review_view.relevance_case')" name="relateTestCase">
            <el-form-item v-if="!isCaseEdit" style="margin-left: -80px">
              <test-case-issue-list :issues-id="form.id"
                                    ref="testCaseIssueList"/>
            </el-form-item>
          </el-tab-pane>

          <el-tab-pane :label="$t('test_track.case.attachment')" name="attachment">
            <el-row>
              <el-col :span="22" style="margin-bottom: 10px;">
                <div class="upload-default" @click.stop>
                  <el-popover placement="right" trigger="hover">
                    <div>
                      <el-upload
                        multiple
                        action=""
                        :auto-upload="true"
                        :file-list="fileList"
                        :show-file-list="false"
                        :before-upload="beforeUpload"
                        :http-request="handleUpload"
                        :on-exceed="handleExceed"
                        :on-success="handleSuccess"
                        :on-error="handleError"
                        :disabled="readOnly">
                        <el-button :disabled="readOnly" type="text">
                          {{ $t('permission.project_file.local_upload') }}
                        </el-button>
                      </el-upload>
                    </div>
                    <el-button type="text" :disabled="readOnly" @click="associationFile">
                      {{ $t('permission.project_file.associated_files') }}
                    </el-button>
                    <i class="el-icon-plus" slot="reference"/>
                  </el-popover>
                </div>
                <div class="local-upload-tips">
                  <span slot="tip" class="el-upload__tip"> {{ $t('test_track.case.upload_tip') }} </span>
                </div>
              </el-col>
            </el-row>
            <el-row style="margin-top: 10px">
              <el-col :span="22">
                <test-case-attachment :table-data="tableData"
                                      :read-only="readOnly"
                                      :is-delete="isDelete"
                                      :is-copy="type === 'copy'"
                                      @handleDelete="handleDelete"
                                      @handleCancel="handleCancel"
                                      @handleUnRelate="handleUnRelate"
                                      @handleDump="handleDump"/>
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

        <issue-comment :issues-id="form.id"
                       @getComments="getComments"
                       ref="issueComment"/>
        <ms-file-metadata-list ref="metadataList" @checkRows="checkRows"/>
        <ms-file-batch-move ref="module" @setModuleId="setModuleId"/>
      </el-form>
    </el-scrollbar>
  </el-main>
</template>

<script>

import TemplateComponentEditHeader from "@/business/plan/view/comonents/report/TemplateComponentEditHeader";
import MsFormDivider from "metersphere-frontend/src/components/MsFormDivider";
import FormRichTextItem from "metersphere-frontend/src/components/FormRichTextItem";
import {buildCustomFields, parseCustomField} from "metersphere-frontend/src/utils/custom_field";
import CustomFiledComponent from "metersphere-frontend/src/components/template/CustomFiledComponent";
import TestCaseIssueList from "@/business/issue/TestCaseIssueList";
import IssueEditDetail from "@/business/issue/IssueEditDetail";
import {byteToSize, getTypeByFileName, getUUID} from "metersphere-frontend/src/utils";
import {
  getCurrentProjectID,
  getCurrentUser,
  getCurrentWorkspaceId,
  getCurrentUserId
} from "metersphere-frontend/src/utils/token"
import {hasLicense} from "metersphere-frontend/src/utils/permission";
import {
  enableThirdPartTemplate,
  getIssuePartTemplateWithProject,
  saveOrUpdateIssue,
  saveFollow,
  getFollow,
  getComments,
  getTapdUser, getPlatformTransitions, getPlatformFormOption, getTapdCurrentOwner
} from "@/api/issue";
import {
  uploadIssueAttachment,
  attachmentList,
  deleteIssueAttachment,
  unrelatedIssueAttachment,
  relatedIssueAttachment, dumpAttachment
} from "@/api/attachment";
import CustomFiledFormItem from "metersphere-frontend/src/components/form/CustomFiledFormItem";
import MsMarkDownText from "metersphere-frontend/src/components/MsMarkDownText";
import IssueComment from "@/business/issue/IssueComment";
import ReviewCommentItem from "@/business/review/commom/ReviewCommentItem";
import {TokenKey} from "metersphere-frontend/src/utils/constants";
import TestCaseAttachment from "@/business/case/components/TestCaseAttachment";
import axios from "axios";
import MsFileMetadataList from "metersphere-frontend/src/components/environment/commons/variable/QuoteFileList";
import MsFileBatchMove from "metersphere-frontend/src/components/environment/commons/variable/FileBatchMove";

export default {
  name: "IssueEditDetail",
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
    MsFileMetadataList,
    MsFileBatchMove,
  },
  data() {
    return {
      type: null,
      issueId: '',
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
        tapdUsers: [],
        platformStatus: null,
        copyIssueId: ''
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
      richTextDefaultOpen: 'preview',
      tabActiveName: 'relateTestCase',
      fileList: [],
      tableData: [],
      readOnly: false,
      isDelete: true,
      cancelFileToken: [],
      uploadFiles: [],
      relateFiles: [],
      unRelateFiles: [],
      filterCopyFiles: [],
      dumpFile: {},
      enableThirdPartTemplate: false
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
        tapdUsers: [],
        platformStatus: null
      };
      this.customFieldForm = null;
      this.issueTemplate = {};
      if (this.$refs.testCaseIssueList) {
        this.$refs.testCaseIssueList.tableData = [];
      }
      this.$refs.form.clearValidate();
    },
    open(data, type) {
      this.uploadFiles = [];
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
          this.getDataInfoAsync(data);

          enableThirdPartTemplate(this.currentProject.id)
            .then(r => {
              this.enableThirdPartTemplate = r.data;
            });
        }, () => {
          this.result.loading = false;
        });
      });
    },
    getTapdCurrentOwner() {
      getTapdCurrentOwner(this.form.id).then(res => {
        if (res && res.data && res.data[0]) {
          this.form.tapdUsers = res.data[0].split(';');
        }
      })
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
      // 编辑的时候才展示
      if (this.form.platformId && this.form.id) {
        let data = {
          platformKey: this.form.platformId,
          projectId: getCurrentProjectID(),
          workspaceId: getCurrentWorkspaceId()
        }
        getPlatformTransitions(data).then(response => {
          if (response.data.length > 0) {
            this.platformTransitions = response.data;
          }
        });
      }

      let data = {
        projectId: this.projectId,
        workspaceId: getCurrentWorkspaceId()
      }
      if (platform === 'Tapd') {
        this.hasTapdId = true;
        this.getTapdCurrentOwner();
        getTapdUser(data)
          .then((response) => {
            this.tapdUsers = response.data;
          })
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
    buildParam() {
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
      if (this.relateFiles.length > 0) {
        param.relateFileMetaIds = this.relateFiles;
      }
      if (this.filterCopyFiles.length > 0) {
        param.filterCopyFileMetaIds = this.filterCopyFiles;
      }
      return param;
    },
    _save() {
      let param = this.buildParam();
      this.parseOldFields(param);
      let option = this.getOption(param);
      this.result.loading = true;
      saveOrUpdateIssue(option.url, option.data)
        .then((response) => {
          this.$emit('close');
          this.$success(this.$t('commons.save_success'));
          this.$emit('refresh', response.data);
          this.result.loading = false;
        }).catch(() => {
          this.result.loading = false;
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

      if (this.uploadFiles.length > 0) {
        this.uploadFiles.forEach(f => {
          formData.append("file", f);
        });
      }
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
    saveFollow() {
      if (!this.form.follows) {
        this.form.follows = [];
      }
      if (this.showFollow) {
        this.showFollow = false;
        for (let i = 0; i < this.form.follows.length; i++) {
          if (this.form.follows[i] === this.currentUser().id) {
            this.form.follows.splice(i, 1)
            break;
          }
        }
        if (this.url === "issues/update") {
          saveFollow(this.issueId, this.form.follows).then(() => {
            this.$success(this.$t('commons.cancel_follow_success'));
          })
        }
      } else {
        this.showFollow = true;
        if (!this.form.follows) {
          this.form.follows = [];
        }
        this.form.follows.push(this.currentUser().id)
        if (this.url === "issues/update") {
          saveFollow(this.issueId, this.form.follows).then(() => {
            this.$success(this.$t('commons.follow_success'));
          })
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
        progress: this.type === 'add' || this.isCaseEdit || this.type === 'copy' ? 100 : 0,
        status: this.type === 'add' || this.isCaseEdit || this.type === 'copy'? 'toUpload' : 0,
        creator: user.name,
        type: getTypeByFileName(file.name),
        isLocal: true
      });

      if (this.type === 'add' || this.isCaseEdit || this.type === 'copy') {
        // 新增上传
        this.uploadFiles.push(file);
        return false;
      }
      this.uploadFile(e, (param) => {
        this.showProgress(e.file, param)
      })
    },
    async uploadFile(param, progressCallback) {
      let progress = 0;
      let file = param.file;
      let data = {"belongId": this.issueId, "belongType": "issue"};
      let CancelToken = axios.CancelToken
      let self = this;
      uploadIssueAttachment(file, data, CancelToken, self.cancelFileToken, progressCallback)
        .then(response => { // 成功回调
          progress = 100;
          param.onSuccess(response);
          progressCallback({progress, status: 'success'});
          self.cancelFileToken.forEach((token, index, array) => {
            if (token.name == file.name) {
              array.splice(token, 1)
            }
          })
        }).catch(({error}) => { // 失败回调
        progress = 100;
        progressCallback({progress, status: 'error'});
        self.cancelFileToken.forEach((token, index, array) => {
          if (token.name == file.name) {
            array.splice(token, 1)
          }
        })
      });
    },
    showProgress(file, params) {
      const {progress, status} = params
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
      if (readyFiles.length === fileList.length) {
        this.getFileMetaData(this.issueId);
      }
    },
    handleError(err, file, fileList) {
      let readyFiles = fileList.filter(item => item.status === 'success')
      if (readyFiles.length === fileList.length) {
        this.getFileMetaData(this.issueId);
      }
    },
    handleInputSearch(data, query) {
      getPlatformFormOption({
        optionMethod: data.optionMethod,
        workspaceId: getCurrentWorkspaceId(),
        platform: this.issueTemplate.platform,
        query
      }).then((r) => {
        data.options = r.data;
        this.$refs.customFieldItem.stopLoading();
      });
    },
    handleDelete(file, index) {
      this.$alert((this.cancelFileToken.length > 0 ? this.$t('load_test.delete_file_when_uploading') + '<br/>' : "") + this.$t('load_test.delete_file_confirm') + file.name + "?", '', {
        confirmButtonText: this.$t('commons.confirm'),
        dangerouslyUseHTMLString: true,
        callback: (action) => {
          if (action === 'confirm') {
            this._handleDelete(file, index);
          }
        }
      });
    },
    _handleDelete(file, index) {
      // 中断所有正在上传的文件
      if (this.cancelFileToken && this.cancelFileToken.length >= 1) {
        this.cancelFileToken.forEach(cacelToken => {
          cacelToken.cancelFunc();
        })
      }
      this.fileList.splice(index, 1);
      this.tableData.splice(index, 1);
      if (file.status === 'toUpload') {
        let delIndex = this.uploadFiles.findIndex(uploadFile => uploadFile.name === file.name)
        this.uploadFiles.splice(delIndex, 1);
      } else {
        if (this.type === 'copy') {
          this.filterCopyFiles.push(file.id);
        } else {
          deleteIssueAttachment(file.id)
            .then(() => {
              this.$success(this.$t('commons.delete_success'));
              this.getFileMetaData(this.issueId);
            });
        }
      }
    },
    handleUnRelate(file, index) {
      // 取消关联
      this.$alert(this.$t('load_test.unrelated_file_confirm') + file.name + "?", '', {
        confirmButtonText: this.$t('commons.confirm'),
        dangerouslyUseHTMLString: true,
        callback: (action) => {
          if (action === 'confirm') {
            let unRelateFileIndex = this.tableData.findIndex(f => f.name === file.name);
            this.tableData.splice(unRelateFileIndex, 1);
            if (file.status === 'toRelate') {
              // 待关联的记录, 直接移除
              let unRelateId = this.relateFiles.findIndex(f => f === file.id);
              this.relateFiles.splice(unRelateId, 1);
            } else {
              if (this.type === 'copy') {
                this.filterCopyFiles.push(file.id);
              } else {
                // 已经关联的记录
                this.unRelateFiles.push(file.id);
                let data = {'belongType': 'issue', 'belongId': this.issueId, 'metadataRefIds': this.unRelateFiles};
                this.result.loading = true;
                unrelatedIssueAttachment(data)
                  .then(() => {
                    this.$success(this.$t('commons.unrelated_success'));
                    this.result.loading = false;
                    this.getFileMetaData(this.issueId);
                  })
              }
            }
          }
        }
      });
    },
    handleDump(file) {
      this.$refs.module.init();
      this.dumpFile = file;
    },
    handleCancel(file, index) {
      this.fileList.splice(index, 1);
      let cancelToken = this.cancelFileToken.filter(f => f.name === file.name)[0];
      cancelToken.cancelFunc();
      let cancelFile = this.tableData.filter(f => f.name === file.name)[0];
      cancelFile.progress = 100;
      cancelFile.status = 'error';
    },
    associationFile() {
      this.$refs.metadataList.open();
    },
    checkRows(rows) {
      let repeatRecord = false;
      for (let row of rows) {
        let rowIndex = this.tableData.findIndex(item => item.name === row.name);
        if (rowIndex >= 0) {
          this.$error(this.$t('load_test.exist_related_file') + ": " + row.name);
          repeatRecord = true;
          break;
        }
      }
      if (!repeatRecord) {
        if (this.type === 'add' || this.isCaseEdit || this.type === 'copy') {
          // 新增
          rows.forEach(row => {
            this.relateFiles.push(row.id);
            this.tableData.push({
              id: row.id,
              name: row.name,
              size: byteToSize(row.size),
              updateTime: row.createTime,
              progress: 100,
              status: 'toRelate',
              creator: row.createUser,
              type: row.type,
              isLocal: false,
            });
          })
        } else {
          // 编辑
          let metadataRefIds = [];
          rows.forEach(row => metadataRefIds.push(row.id));
          let data = {'belongType': 'issue', 'belongId': this.issueId, 'metadataRefIds': metadataRefIds};
          this.result.loading = true;
          relatedIssueAttachment(data)
            .then(() => {
              this.$success(this.$t('commons.relate_success'));
              this.result.loading = false;
              this.getFileMetaData(this.issueId);
            });
        }
      }
    },
    setModuleId(moduleId) {
      let data = {
        id: getUUID(), resourceId: getCurrentProjectID(), moduleId: moduleId,
        projectId: getCurrentProjectID(), fileName: this.dumpFile.name, attachmentId: this.dumpFile.id
      };
      dumpAttachment(data)
        .then(() => {
          this.$success(this.$t("organization.integration.successful_operation"));
        });
    },
    getFileMetaData(id) {
      if (this.type === 'edit') {
        this.uploadFiles = [];
        this.relateFiles = [];
        this.unRelateFiles = [];
      }
      // 保存用例后传入用例id，刷新文件列表，可以预览和下载
      this.fileList = [];
      this.tableData = [];
      if (id) {
        let data = {'belongType': 'issue', 'belongId': id};
        attachmentList(data).then((response) => {
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
        getComments(this.issueId).then(response => {
          this.comments = response.data;
        })
      }
    }
  }
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

.local-upload-tips {
  display: inline-block;
  position: relative;
  left: 25px;
  top: 8px;
}
</style>
