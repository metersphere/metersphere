<template>
  <div v-loading="result.loading">
    <el-row type="flex" justify="space-between" align="middle">
      <h4>{{ $t('load_test.scenario_list') }}</h4>
    </el-row>
    <el-row type="flex" justify="start" align="middle">
      <ms-table-button icon="el-icon-circle-plus-outline"
                       :disabled="isReadOnly"
                       :content="$t('load_test.load_exist_jmx')" @click="loadJMX()"/>
      <ms-table-button icon="el-icon-share"
                       :disabled="isReadOnly"
                       @click="loadApiAutomation()"
                       :content="$t('load_test.load_api_automation_jmx')"/>
    </el-row>
    <el-table class="basic-config" :data="threadGroups.filter(tg=>tg.deleted=='false')">
      <el-table-column
        :label="$t('load_test.scenario_name')">
        <template v-slot:default="{row}">
          {{ row.attributes.testname }}
        </template>
      </el-table-column>
      <el-table-column
        label="Enable/Disable">
        <template v-slot:default="{row}">
          <el-switch v-model="row.enabled"
                     inactive-color="#DCDFE6"
                     active-value="true"
                     inactive-value="false"
                     :disabled="isReadOnly || threadGroupDisable(row)"
          />
        </template>
      </el-table-column>
      <el-table-column
        label="CSVDataSet">
        <template v-slot:default="scope">
          <ms-tag v-for="(f, index) in scope.row.csvFiles"
                  :key="index"
                  effect="light"
                  :content="f"/>
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('load_test.thread_group')">
        <template v-slot:default="{row}">
          <span v-if="row.tgType === 'PostThreadGroup' || row.tgType === 'SetupThreadGroup'">
            {{ row.tgType }}
          </span>
          <el-select v-else v-model="row.tgType"
                     :disabled="isReadOnly"
                     :placeholder="$t('commons.please_select')" size="small"
                     @change="tgTypeChange(row)">
            <el-option v-for="tg in threadGroupForSelect" :key="tg.tagName" :label="tg.name"
                       :value="tg.testclass"></el-option>
          </el-select>
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('commons.operating')">
        <template v-slot:default="{row}">
          <el-button :disabled="isReadOnly || threadGroupDisable(row)"
                     @click="handleDeleteThreadGroup(row)"
                     type="danger"
                     icon="el-icon-delete" size="mini"
                     circle/>
        </template>
      </el-table-column>
    </el-table>

    <el-row type="flex" justify="space-between" align="middle">
      <h4>{{ $t('load_test.other_resource') }}</h4>
    </el-row>
    <el-row type="flex" justify="start" align="middle">

      <ms-table-button icon="el-icon-circle-plus-outline"
                       :disabled="isReadOnly"
                       :content="$t('load_test.load_exist_file')" @click="loadFile()"/>
    </el-row>
    <el-table class="basic-config" :data="tableData">
      <el-table-column
        prop="name"
        :label="$t('load_test.file_name')">
      </el-table-column>
      <el-table-column
        prop="size"
        :label="$t('load_test.file_size')">
      </el-table-column>
      <el-table-column
        prop="type"
        :label="$t('load_test.file_type')">
      </el-table-column>
      <el-table-column
        :label="$t('load_test.last_modify_time')">
        <template v-slot:default="scope">
          <i class="el-icon-time"/>
          <span class="last-modified">{{ scope.row.updateTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('commons.operating')">
        <template v-slot:default="scope">
          <el-button @click="handleDownload(scope.row)" :disabled="!scope.row.id || isReadOnly" type="primary"
                     icon="el-icon-download"
                     size="mini" circle/>
          <el-button :disabled="isReadOnly" @click="handleDelete(scope.row, scope.$index)" type="danger"
                     icon="el-icon-delete" size="mini"
                     circle/>
        </template>
      </el-table-column>
    </el-table>

    <exist-files ref="existFiles"
                 @fileChange="fileChange"
                 :file-list="fileList"
                 :table-data="tableData"
                 :upload-list="uploadList"
                 :is-read-only="isReadOnly"
                 :scenarios="threadGroups"/>

    <exist-scenarios ref="existScenarios"
                     @fileChange="fileChange"
                     :file-list="fileList"
                     :table-data="tableData"
                     :upload-list="uploadList"
                     :scenarios="threadGroups"/>

  </div>
</template>

<script>
import {Message} from "element-ui";
import MsTableButton from "@/business/components/common/components/MsTableButton";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import MsTableOperatorButton from "@/business/components/common/components/MsTableOperatorButton";
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";
import ExistFiles from "@/business/components/performance/test/components/ExistFiles";
import ExistScenarios from "@/business/components/performance/test/components/ExistScenarios";
import {findThreadGroup} from "@/business/components/performance/test/model/ThreadGroup";
import {hasPermission} from "@/common/js/utils";
import MsTag from "@/business/components/common/components/MsTag";

export default {
  name: "PerformanceBasicConfig",
  components: {
    MsTag,
    ExistScenarios, ExistFiles, MsDialogFooter, MsTableOperatorButton, MsTablePagination, MsTableButton
  },
  props: {
    test: {
      type: Object
    },
  },
  data() {
    return {
      result: {},
      isReadOnly: false,
      projectLoadingResult: {},
      getFileMetadataPath: "/performance/file/metadata",
      getFileMetadataById: "/performance/file/getMetadataById",
      jmxDownloadPath: '/performance/file/download',
      jmxDeletePath: '/performance/file/delete',
      fileList: [],
      tableData: [],
      uploadList: [],
      metadataIdList: [],
      fileNumLimit: 10,
      threadGroups: [],
      loadFileVisible: false,
      currentPage: 1,
      pageSize: 5,
      total: 0,
      existFiles: [],
      apiScenarios: [],
      loadApiAutomationVisible: false,
      selectIds: new Set(),
      threadGroupForSelect: [
        {
          name: 'ThreadGroup',
          tagName: 'ThreadGroup',
          testclass: 'ThreadGroup',
          guiclass: 'ThreadGroupGui'
        },
        {
          name: 'ConcurrencyThreadGroup',
          tagName: 'com.blazemeter.jmeter.threads.concurrency.ConcurrencyThreadGroup',
          testclass: 'com.blazemeter.jmeter.threads.concurrency.ConcurrencyThreadGroup',
          guiclass: "com.blazemeter.jmeter.threads.concurrency.ConcurrencyThreadGroupGui"
        },
      ]
    };
  },
  created() {
    if (this.test.id) {
      this.getFileMetadata(this.test);
    }
    this.isReadOnly = !hasPermission('PROJECT_PERFORMANCE_TEST:READ+EDIT');
  },
  watch: {
    test() {
      if (this.test.id) {
        this.getFileMetadata(this.test);
      }
    }
  },
  methods: {
    getFileMetadata(test) {
      this.fileList = [];
      this.tableData = [];
      this.uploadList = [];
      this.metadataIdList = [];
      this.result = this.$get(this.getFileMetadataPath + "/" + test.id, response => {
        let files = response.data;
        if (!files) {
          Message.error({message: this.$t('load_test.related_file_not_found'), showClose: true});
          return;
        }
        // deep copy
        this.fileList = JSON.parse(JSON.stringify(files));
        this.tableData = JSON.parse(JSON.stringify(files));
        this.tableData.map(f => {
          f.size = (f.size / 1024).toFixed(2) + ' KB';
        });
      });
    },
    selectAttachFileById(metadataIdArr) {
      this.metadataIdList = metadataIdArr;
      for (let i = 0; i < metadataIdArr.length; i++) {
        let id = metadataIdArr[i];
        this.result = this.$get(this.getFileMetadataById + "/" + id, response => {
          let files = response.data;
          if (files) {
            this.fileList.push(JSON.parse(JSON.stringify(files)));
            this.tableData.push(JSON.parse(JSON.stringify(files)));
            this.tableData.map(f => {
              f.size = (f.size / 1024).toFixed(2) + ' KB';
            });
          }
        });
      }
    },
    handleDownload(file) {
      let data = {
        name: file.name,
        id: file.id,
      };
      let config = {
        url: this.jmxDownloadPath,
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
    handleDelete(file) {
      this.$alert(this.$t('load_test.delete_file_confirm') + file.name + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this._handleDelete(file);
          }
        }
      });
    },
    _handleDelete(file) {
      let index = this.fileList.findIndex(f => f.name === file.name);
      if (index > -1) {
        this.fileList.splice(index, 1);
      }
      index = this.tableData.findIndex(f => f.name === file.name);
      if (index > -1) {
        this.tableData.splice(index, 1);
      }
      //
      let i = this.uploadList.findIndex(upLoadFile => upLoadFile.name === file.name);
      if (i > -1) {
        this.uploadList.splice(i, 1);
      }

      let jmxIndex = this.threadGroups.findIndex(tg => tg.handler === file.name);
      while (jmxIndex !== -1) {
        this.threadGroups.splice(jmxIndex, 1);
        jmxIndex = this.threadGroups.findIndex(tg => tg.handler === file.name);
      }
    },
    handleDeleteThreadGroup(tg) {
      this.$alert(this.$t('load_test.delete_threadgroup_confirm') + tg.attributes.testname + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            tg.deleted = 'true';
          }
        }
      });
    },
    threadGroupDisable(row) {
      return this.threadGroups.filter(tg => tg.enabled == 'true').length === 1 && row.enabled == 'true';
    },
    tgTypeChange(row) {
      this.$emit("tgTypeChange", row);
    },
    updatedFileList() {
      return this.fileList;// 表示修改了已经上传的文件列表
    },
    conversionMetadataIdList() {
      return this.metadataIdList;// 表示修改了已经上传的文件列表
    },
    fileSorts() {
      let fileSorts = {};
      this.tableData.forEach((f, index) => {
        fileSorts[f.name] = index;
      });
      return fileSorts;
    },
    loadJMX() {
      this.$refs.existFiles.open('jmx');
    },
    loadFile() {
      this.$refs.existFiles.open('resource');
    },
    loadApiAutomation() {
      this.$refs.existScenarios.open();
    },
    fileChange(threadGroups) {
      this.$emit('fileChange', threadGroups);
    },
    validConfig() {
      if (this.uploadList.length + this.fileList.length > this.fileNumLimit) {
        this.$refs.existFiles.handleExceed();
        return false;
      }

      if (this.threadGroups.filter(tg => tg.enabled == 'true').length === 0) {
        this.$error(this.$t('load_test.threadgroup_at_least_one'));
        return false;
      }
      return true;
    },
    importScenario(scenarioId) {
      this.$refs.existScenarios.selectIds.add(scenarioId);
    },
    importCase(caseObj) {
      console.log("case: ", caseObj);
      let suffixIndex = caseObj.name.lastIndexOf(".jmx");
      let jmxName = caseObj.name.substring(0, suffixIndex) + "_" + new Date().getTime() + ".jmx";
      let threadGroups = findThreadGroup(caseObj.xml, jmxName);
      threadGroups.forEach(tg => {
        tg.options = {};
      });
      this.fileChange(threadGroups);
      let file = new File([caseObj.xml], jmxName);
      this.uploadList.push(file);
      this.tableData.push({
        name: file.name,
        size: (file.size / 1024).toFixed(2) + ' KB',
        type: 'JMX',
        updateTime: file.lastModified,
      });
    },
    handleUpload() {
      // 从api创建的测试
      this.$refs.existScenarios.handleImport();
    },
  },
};
</script>

<style scoped>
.basic-config {
  width: 100%;
}

.last-modified {
  margin-left: 5px;
}

.el-dialog >>> .el-dialog__body {
  padding: 10px 20px;
}
</style>
