<template>
  <el-tabs class="other-info-tabs" v-loading="result.loading" v-model="tabActiveName">
    <el-tab-pane :label="$t('commons.remark')" name="remark">
      <el-row>
        <form-rich-text-item class="remark-item" :disabled="readOnly" :data="form" prop="remark"/>
      </el-row>
    </el-tab-pane>
    <el-tab-pane :label="$t('test_track.case.relate_test')" name="relateTest">
      <test-case-test-relate :read-only="readOnly" :case-id="caseId" :version-enable="versionEnable" ref="relateTest"/>
    </el-tab-pane>

    <el-tab-pane :label="$t('test_track.related_requirements')" name="demand">
      <el-col :span="8">
        <el-form-item :label="$t('test_track.related_requirements')" :label-width="labelWidth"
                      prop="demandId">

          <el-cascader v-model="demandValue" :show-all-levels="false" :options="demandOptions" style="width: 100%"
                       clearable filterable :filter-method="filterDemand"/>
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
        <el-col :span="20" :offset="1">
          <el-upload
            accept=".jpg,.jpeg,.png,.xlsx,.doc,.pdf,.docx,.txt"
            action=""
            :show-file-list="false"
            :before-upload="beforeUpload"
            :http-request="handleUpload"
            :on-exceed="handleExceed"
            multiple
            :limit="8"
            :disabled="readOnly"
            :file-list="fileList">
            <el-button icon="el-icon-plus" :disabled="readOnly" size="mini"></el-button>
            <span slot="tip" class="el-upload__tip"> {{ $t('test_track.case.upload_tip') }} </span>
          </el-upload>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="19" :offset="2">
          <test-case-attachment :table-data="tableData"
                                :read-only="readOnly"
                                :is-delete="!isTestPlan"
                                @handleDelete="handleDelete"/>
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
  props: ['form', 'labelWidth', 'caseId', 'readOnly', 'projectId', 'isTestPlan', 'planId', 'versionEnable', 'isCopy', 'isTestPlanEdit', 'type', 'comments', 'isClickAttachmentTab'],
  data() {
    return {
      result: {},
      tabActiveName: "remark",
      uploadList: [],
      fileList: [],
      tableData: [],
      demandOptions: [],
      relationshipCount: 0,
      demandValue: [],
      //sysList:this.sysList,//一级选择框的数据
      props: {
        multiple: true,
        //lazy: true,
        //lazyLoad:this.lazyLoad
      },
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
        this.comments = res.data;
      })
    },
    setRelationshipCount(count) {
      this.relationshipCount = count;
    },
    reset() {
      this.tabActiveName = "remark";
    },
    fileValidator(file) {
      /// todo: 是否需要对文件内容和大小做限制
      return file.size > 0;
    },
    beforeUpload(file) {
      if (!this.fileValidator(file)) {
        /// todo: 显示错误信息
        return false;
      }

      if (this.tableData.filter(f => f.name === file.name).length > 0) {
        this.$error(this.$t('load_test.delete_file') + ', name: ' + file.name);
        return false;
      }

      let type = file.name.substring(file.name.lastIndexOf(".") + 1);

      this.tableData.push({
        name: file.name,
        size: file.size + ' Bytes', /// todo: 按照大小显示Byte、KB、MB等
        type: type.toUpperCase(),
        updateTime: new Date().getTime(),
      });

      return true;
    },
    handleUpload(uploadResources) {
      this.uploadList.push(uploadResources.file);
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
      let i = this.uploadList.findIndex(upLoadFile => upLoadFile.name === file.name);
      if (i > -1) {
        this.uploadList.splice(i, 1);
      }
    },
    handleExceed() {
      this.$error(this.$t('load_test.file_size_limit'));
    },
    getFileMetaData(id) {
      this.$emit("update:isClickAttachmentTab", true);
      // 保存用例后传入用例id，刷新文件列表，可以预览和下载
      if (this.uploadList && this.uploadList.length > 0 && !id) {
        return;
      }
      this.fileList = [];
      this.tableData = [];
      this.uploadList = [];
      let testCaseId = id ? id : this.caseId;
      if (testCaseId) {
        this.result = this.$get("test/case/file/metadata/" + testCaseId, response => {
          let files = response.data;

          if (!files) {
            return;
          }
          // deep copy
          this.fileList = JSON.parse(JSON.stringify(files));
          this.tableData = JSON.parse(JSON.stringify(files));
          this.tableData.map(f => {
            f.size = f.size + ' Bytes';
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
      if (this.demandOptions.length === 0) {
        this.result = {loading: true};
        this.$get("/issues/demand/list/" + this.projectId).then(response => {
          this.demandOptions = [];
          if (response.data.data && response.data.data.length > 0) {
            this.buildDemandCascaderOptions(response.data.data, this.demandOptions, []);
          }
          this.demandOptions.unshift({
            value: 'other',
            label: 'Other: ' + this.$t('test_track.case.other'),
            platform: 'Other'
          });
          if (this.form.demandId === 'other') {
            this.demandValue = ['other'];
          }
          this.result = {loading: false};
        }).catch(() => {
          this.demandOptions.unshift({
            value: 'other',
            label: 'Other: ' + this.$t('test_track.case.other'),
            platform: 'Other'
          });
          if (this.form.demandId === 'other') {
            this.demandValue = ['other'];
          }
          this.result = {loading: false};
        });
      }
    },
    buildDemandCascaderOptions(data, options, pathArray) {
      data.forEach(item => {
        let option = {
          label: item.platform + ': ' + item.name,
          value: item.id
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
</style>
