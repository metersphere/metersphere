<template>
  <el-tabs class="other-info-tabs" v-loading="result.loading" v-model="tabActiveName">
    <el-tab-pane :label="$t('commons.remark')" name="remark">
      <el-row>
        <form-rich-text-item
          class="remark-item"
          :disabled="readOnly"
          :data="form"
          :default-open="defaultOpen"
          prop="remark"/>
      </el-row>
    </el-tab-pane>
    <el-tab-pane :label="$t('test_track.case.relate_test')" name="relateTest">
      <test-case-test-relate :read-only="readOnly" :case-id="caseId" :version-enable="versionEnable" ref="relateTest"/>
    </el-tab-pane>

    <el-tab-pane :label="$t('test_track.related_requirements')" name="demand">
      <el-col :span="8">
        <el-form-item :label="$t('test_track.related_requirements')" :label-width="labelWidth"
                      prop="demandId">

          <el-cascader  v-if="!readOnly" v-model="demandValue" :show-all-levels="false" :options="demandOptions"
                       clearable filterable :filter-method="filterDemand">
            <template slot-scope="{ node, data }">
              <span class="demand-span" :title="data.label">{{ data.label }}</span>
            </template>
          </el-cascader>

          <el-input class="demandInput" v-else :disabled="readOnly" :value="demandLabel">

          </el-input>
        </el-form-item>
      </el-col>
      <el-col :span="8" :offset="2">
        <el-form-item :label="$t('test_track.case.demand_name_id')" :label-width="labelWidth" prop="demandName"
                      v-if="form.demandId=='other'">
          <el-input :disabled="readOnly" v-model="form.demandName"></el-input>
        </el-form-item>
      </el-col>
    </el-tab-pane>

    <el-tab-pane :label="$t('test_track.case.relate_issue')" name="bug">
      <test-case-issue-relate
        v-if="tabActiveName === 'bug'"
        :plan-id="planId"
        :is-copy="isCopy"
        :read-only="readOnly && !(isTestPlan)"
        :plan-case-id="planId ? this.form.id : null"
        :case-id="caseId"
        ref="issue"/>
    </el-tab-pane>

    <el-tab-pane :label="$t('commons.relationship.name')" name="relationship">
      <template v-slot:label>
        <tab-pane-count :title="$t('commons.relationship.name')" :count="relationshipCount"/>
      </template>
      <dependencies-list @setCount="setRelationshipCount" :read-only="readOnly" :resource-id="caseId"
                         :version-enable="versionEnable" resource-type="TEST_CASE" ref="relationship"/>
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
            :disabled="readOnly || isCopy">
            <el-button :disabled="readOnly || isCopy" type="primary" size="mini">{{$t('test_track.case.add_attachment')}}</el-button>
            <span slot="tip" class="el-upload__tip"> {{ $t('test_track.case.upload_tip') }} </span>
          </el-upload>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="22">
          <test-case-attachment :table-data="tableData"
                                :read-only="readOnly"
                                :is-copy="isCopy"
                                :is-delete="!isTestPlan"
                                @handleDelete="handleDelete"
                                @handleCancel="handleCancel"/>
        </el-col>
      </el-row>
    </el-tab-pane>
    <el-tab-pane v-if="type!=='add'" :label="$t('test_track.review.comment')" name="comment">
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
          <el-button :disabled="readOnly" icon="el-icon-plus" type="mini" @click="openComment"></el-button>
        </el-col>
      </el-row>
      <el-row v-if="type!=='add'">
        <el-col :span="20" :offset="1">
          <review-comment-item v-for="(comment,index) in comments"
                               :key="index"
                               :comment="comment"
                               @refresh="getComments" api-url="/test/case"/>
          <div v-if="!comments || comments.length === 0" style="text-align: center">
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
</template>

<script>
import {Message} from "element-ui";
import TestCaseRichText from "@/business/components/track/case/components/MsRichText";
import MsRichText from "@/business/components/track/case/components/MsRichText";
import TestCaseAttachment from "@/business/components/track/case/components/TestCaseAttachment";
import TestCaseIssueRelate from "@/business/components/track/case/components/TestCaseIssueRelate";
import FormRichTextItem from "@/business/components/track/case/components/FormRichTextItem";
import TestCaseTestRelate from "@/business/components/track/case/components/TestCaseTestRelate";
import DependenciesList from "@/business/components/common/components/graph/DependenciesList";
import TabPaneCount from "@/business/components/track/plan/view/comonents/report/detail/component/TabPaneCount";
import {getRelationshipCountCase} from "@/network/testCase";
import TestCaseComment from "@/business/components/track/case/components/TestCaseComment";
import ReviewCommentItem from "@/business/components/track/review/commom/ReviewCommentItem";
import {byteToSize, getTypeByFileName, hasLicense} from "@/common/js/utils";
import {TokenKey} from "@/common/js/constants";
import axios from "axios";
import {validateAndSetLicense} from "@/business/permission";

export default {
  name: "TestCaseEditOtherInfo",
  components: {
    TabPaneCount,
    DependenciesList,
    TestCaseTestRelate,
    TestCaseComment,
    ReviewCommentItem,
    FormRichTextItem, TestCaseIssueRelate, TestCaseAttachment, MsRichText, TestCaseRichText
  },
  props: ['form', 'labelWidth', 'caseId', 'readOnly', 'projectId', 'isTestPlan', 'planId', 'versionEnable', 'isCopy', 'copyCaseId',
    'type', 'comments', 'isClickAttachmentTab',
    'defaultOpen'
  ],
  data() {
    return {
      result: {},
      tabActiveName: "remark",
      fileList: [],
      tableData: [],
      demandOptions: [],
      relationshipCount: 0,
      demandValue: [],
      demandLabel: '',
      //sysList:this.sysList,//一级选择框的数据
      props: {
        multiple: true,
        //lazy: true,
        //lazyLoad:this.lazyLoad
      },
      intervalMap: new Map(),
      cancelFileToken: [],
      uploadFiles: []
    };
  },
  computed: {
    isTesterPermission() {
      return true;
    }
  },
  watch: {
    tabActiveName() {
      if (this.tabActiveName === 'demand') {
        this.getDemandOptions();
      } else if (this.tabActiveName === 'bug') {
        this.$nextTick(() => {
          this.$refs.issue.getIssues();
        });
      } else if (this.tabActiveName === 'relationship') {
        this.$refs.relationship.open();
      } else if (this.tabActiveName === 'attachment') {
        this.getFileMetaData();
      } else if (this.tabActiveName === 'relateTest') {
        this.$nextTick(() => {
          this.getRelatedTest();
        });
      }
    },
    caseId() {
      getRelationshipCountCase(this.caseId, (data) => {
        this.relationshipCount = data;
      });
    },
    demandValue() {
      if (this.demandValue.length > 0) {
        this.form.demandId = this.demandValue[this.demandValue.length - 1];
      } else {
        this.form.demandId = null;
      }
    }
  },
  methods: {
    updateRemark(text) {
      this.form.remark = text;
    },
    openComment() {
      this.$emit('openComment');
    },
    getComments(testCase) {
      let id = '';
      if (testCase) {
        id = testCase.id;
      } else {
        id = this.form.id;
      }
      this.result = this.$get('/test/case/comment/list/' + id, res => {
        this.$emit('update:comments', res.data);
      })
    },
    setRelationshipCount(count) {
      this.relationshipCount = count;
    },
    reset() {
      this.tabActiveName = "remark";
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
        progress: this.type === 'add' ? 100 : 0,
        status: this.type === 'add' ? 'toUpload' : 0,
        creator: user.name,
        type: getTypeByFileName(file.name)
      });

      if (this.type === 'add') {
        // 新增上传
        this.uploadFiles.push(file);
        return false;
      }
      // 上传文件
      this.uploadFile(e, (param) => {
        this.showProgress(e.file, param)
      })
    },
    async uploadFile(param, progressCallback) {
      let file = param.file;
      let progress = 0;
      let formData = new FormData();
      let requestJson = JSON.stringify({"belongId": this.caseId, "belongType": "testcase"});
      formData.append("file", file);
      formData.append('request', new Blob([requestJson], {
        type: "application/json"
      }));

      let CancelToken = axios.CancelToken
      let self = this;
      axios({
        headers: { 'Content-Type': 'application/json;charset=UTF-8' },
        method: 'post',
        url: '/attachment/upload',
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
        self.cancelFileToken.forEach((token, index, array)=>{
          if(token.name == file.name){
            array.splice(token,1)
          }
        })
      }).catch(error => { // 失败回调
        progress = 100;
        progressCallback({progress, status: 'error'});
        self.cancelFileToken.forEach((token, index, array)=>{
          if(token.name == file.name){
            array.splice(token,1)
          }
        })
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
      let readyFiles = fileList.filter(item => item.status === 'ready')
      if (readyFiles.length === 0 ) {
        this.getFileMetaData();
      }
    },
    handleError(err, file, fileList) {
      let readyFiles = fileList.filter(item => item.status === 'ready')
      if (readyFiles.length === 0 ) {
        this.getFileMetaData();
      }
    },
    handleDelete(file, index) {
      this.$alert((this.cancelFileToken.length > 0 ? this.$t('load_test.delete_file_when_uploading') + '<br/>': "") +  this.$t('load_test.delete_file_confirm') + file.name + "?", '', {
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
      if (this.type === 'add') {
        this.uploadFiles.splice(index, 1);
      } else {
        this.$get('/attachment/delete/testcase/' + file.id , response => {
          this.$success(this.$t('commons.delete_success'));
          this.getFileMetaData();
        });
      }
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
      this.$emit("update:isClickAttachmentTab", true);
      // 保存用例后传入用例id，刷新文件列表，可以预览和下载
      this.fileList = [];
      this.tableData = [];
      let testCaseId;
      if (this.isCopy) {
        testCaseId = this.copyCaseId
      } else {
        testCaseId = id ? id : this.caseId;
      }
      if (testCaseId) {
        let data = {'belongType': 'testcase', 'belongId': testCaseId};
        this.result = this.$post("/attachment/metadata/list", data, response => {
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
    getRelatedTest() {
      this.$refs.relateTest.initTable();
    },
    visibleChange(flag) {
      if (flag) {
        this.getDemandOptions();
      }
    },
    getDemandOptions() {
      this.result = {loading: true};
      this.demandLabel = '';
      this.$get("/issues/demand/list/" + this.projectId).then(response => {
        this.demandOptions = [];
        if (response.data.data && response.data.data.length > 0) {
          this.buildDemandCascaderOptions(response.data.data, this.demandOptions, []);
        }
        this.addOtherOption();
      }).catch(() => {
        this.addOtherOption();
      });
    },
    addOtherOption() {
      this.demandOptions.unshift({
        value: 'other',
        label: 'Other: ' + this.$t('test_track.case.other'),
        platform: 'Other'
      });
      if (this.form.demandId === 'other') {
        this.demandValue = ['other'];
        this.demandLabel = 'Other: ' + this.$t('test_track.case.other');
      }
      this.result = {loading: false};
    },
    buildDemandCascaderOptions(data, options, pathArray) {
      data.forEach(item => {
        let option = {
          label: item.platform + ': ' + item.name,
          value: item.id
        }
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
          this.buildDemandCascaderOptions(item.children, option.children, pathArray);
        }
        pathArray.pop();
      });
    },
    filterDemand(node, keyword) {
      if (keyword && node.text.toLowerCase().indexOf(keyword.toLowerCase()) !== -1) {
        return true;
      }
      return false;
    }
  }
};
</script>

<style scoped>

.other-info-tabs >>> .el-tabs__content {
  padding: 20px 0px;
}

.remark-item {
  padding: 0px 3px;
}

.el-cascader >>> .el-input {
  cursor: pointer;
}

.ms-header {
  background: #783887;
  color: white;
  height: 18px;
  width: 18px;
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
</style>
