<template>
  <el-tabs class="other-info-tabs" v-loading="result.loading" v-model="tabActiveName">
    <el-tab-pane :label="$t('commons.remark')" name="remark">
      <el-row>
        <form-rich-text-item class="remark-item" :disabled="readOnly" :data="form" prop="remark"/>
      </el-row>
    </el-tab-pane>
    <el-tab-pane :label="$t('test_track.case.relate_test')" name="relateTest">
      <el-col v-if="form.list" :span="7" :offset="1">
        <span class="cast_label">{{ $t('test_track.case.relate_test') }}：</span>
        <span v-for="(item,index) in form.list" :key="index">
                        <el-button @click="openTest(item)" type="text" style="margin-left: 7px;">{{
                            item.testName
                          }}</el-button>
                      </span>
      </el-col>
      <el-col v-else :span="7" style="margin-top: 10px;">
        <el-form-item :label="$t('test_track.case.relate_test')">
          <el-cascader :options="sysList" filterable :placeholder="$t('test_track.case.please_select_relate_test')"
                       show-all-levels
                       v-model="form.selected" :props="props"
                       :disabled="readOnly"
                       ref="cascade"></el-cascader>
        </el-form-item>
      </el-col>
    </el-tab-pane>

    <el-tab-pane :label="$t('test_track.related_requirements')" name="demand">
      <el-col :span="7">
        <el-form-item :label="$t('test_track.related_requirements')" :label-width="labelWidth"
                      prop="demandId">
          <el-select filterable :disabled="readOnly" v-model="form.demandId" @visible-change="visibleChange"
                     :placeholder="$t('test_track.please_related_requirements')" class="ms-case-input">
            <el-option
              v-for="item in demandOptions"
              :key="item.id"
              :label="item.platform + ': '+item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="7">
        <el-form-item :label="$t('test_track.case.demand_name_id')" :label-width="labelWidth" prop="demandName"
                      v-if="form.demandId=='other'">
          <el-input :disabled="readOnly" v-model="form.demandName"></el-input>
        </el-form-item>
      </el-col>
    </el-tab-pane>

    <el-tab-pane :label="$t('test_track.case.relate_issue')" name="bug">
      <test-case-issue-relate
        :plan-id="planId"
        :read-only="readOnly && !(isTestPlan)"
        :case-id="caseId" ref="issue"/>
    </el-tab-pane>

    <el-tab-pane :label="$t('test_track.case.attachment')" name="attachment">
      <el-row>
        <el-col :span="20" :offset="1">
          <el-upload
            accept=".jpg,.jpeg,.png,.xlsx,.doc,.pdf,.docx"
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
  </el-tabs>
</template>

<script>
import {Message} from "element-ui";
import TestCaseRichText from "@/business/components/track/case/components/MsRichText";
import MsRichText from "@/business/components/track/case/components/MsRichText";
import {TEST} from "@/business/components/api/definition/model/JsonData";
import TestCaseAttachment from "@/business/components/track/case/components/TestCaseAttachment";
import TestCaseIssueRelate from "@/business/components/track/case/components/TestCaseIssueRelate";
import {enableModules} from "@/common/js/utils";
import FormRichTextItem from "@/business/components/track/case/components/FormRichTextItem";

export default {
  name: "TestCaseEditOtherInfo",
  components: {FormRichTextItem, TestCaseIssueRelate, TestCaseAttachment, MsRichText, TestCaseRichText},
  props: ['form', 'labelWidth', 'caseId', 'readOnly', 'projectId', 'isTestPlan', 'planId', 'sysList'],
  data() {
    return {
      result: {},
      tabActiveName: "remark",
      uploadList: [],
      fileList: [],
      tableData: [],
      demandOptions: [],
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
        this.$refs.issue.getIssues();
      } else if (this.tabActiveName === 'attachment') {
        this.getFileMetaData();
      }
    }
  },
  methods: {
    updateRemark(text) {
      this.form.remark = text;
    },
    reset() {
      this.tabActiveName = "remark";
    },
    fileValidator(file) {
      /// todo: 是否需要对文件内容和大小做限制
      return file.size > 0;
    },
    openTest(data) {
      this.$emit('openTest', data);
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
    visibleChange(flag) {
      if (flag) {
        this.getDemandOptions();
      }
    },
    getDemandOptions() {
      if (this.demandOptions.length === 0) {
        this.result = {loading: true};
        this.$get("demand/list/" + this.projectId).then(response => {
          this.demandOptions = response.data.data;
          this.demandOptions.unshift({id: 'other', name: this.$t('test_track.case.other'), platform: 'Other'});
          this.result = {loading: false};
        }).catch(() => {
          this.demandOptions.unshift({id: 'other', name: this.$t('test_track.case.other'), platform: 'Other'});
          this.result = {loading: false};
        });
      }
    },

  }
};
</script>

<style scoped>

.other-info-tabs >>> .el-tabs__content {
  padding: 20px 0px;
}

.other-info-tabs {
  padding: 10px 60px;
}

.remark-item {
  padding: 0px 15px;
}

.el-cascader >>> .el-input {
  cursor: pointer;
  width: 500px;
}
</style>
