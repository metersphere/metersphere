<template>
  <div v-loading="result.loading">
    <el-row type="flex" justify="space-between" align="middle">
      <h4>{{ $t('load_test.scenario_list') }}</h4>
    </el-row>
    <el-row type="flex" justify="start" align="middle">
      <el-upload
        style="padding-right: 10px;"
        accept=".jmx"
        action=""
        :limit="fileNumLimit"
        :show-file-list="false"
        :before-upload="beforeUploadJmx"
        :http-request="handleUpload"
        :on-exceed="handleExceed"
        :disabled="isReadOnly"
        :file-list="fileList">
        <ms-table-button :is-tester-permission="true" icon="el-icon-upload2"
                         :content="$t('load_test.upload_jmx')"/>
      </el-upload>
      <ms-table-button :is-tester-permission="true" icon="el-icon-circle-plus-outline"
                       :content="$t('load_test.load_exist_jmx')" @click="loadJMX()"/>
      <ms-table-button :is-tester-permission="true" icon="el-icon-share"
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
                     :disabled="threadGroupDisable(row)"
          />
        </template>
      </el-table-column>
      <el-table-column
        label="ThreadGroup">
        <template v-slot:default="{row}">
          {{ row.name.substring(row.name.lastIndexOf(".") + 1) }}
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
      <el-upload
        style="padding-right: 10px;"
        accept=".jar,.csv"
        action=""
        :limit="fileNumLimit"
        multiple
        :show-file-list="false"
        :before-upload="beforeUploadFile"
        :http-request="handleUpload"
        :on-exceed="handleExceed"
        :disabled="isReadOnly"
        :file-list="fileList">
        <ms-table-button :is-tester-permission="true" icon="el-icon-upload2"
                         :content="$t('load_test.upload_file')"/>
      </el-upload>

      <ms-table-button :is-tester-permission="true" icon="el-icon-circle-plus-outline"
                       :content="$t('load_test.load_exist_file')" @click="loadFile()"/>
    </el-row>
    <el-table class="basic-config" :data="tableData.filter(f => !f.name.toUpperCase().endsWith('.JMX'))">
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

    <el-dialog :title="$t('load_test.exist_jmx')" width="70%" :visible.sync="loadFileVisible">

      <el-table class="basic-config" :data="existFiles" v-loading="projectLoadingResult.loading">
        <el-table-column
          prop="testName"
          :label="$t('load_test.test')">
        </el-table-column>
        <el-table-column
          prop="name"
          :label="$t('load_test.file_name')">
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
            <ms-table-operator-button :is-tester-permission="true"
                                      :tip="$t('api_test.api_import.label')"
                                      icon="el-icon-upload"
                                      @exec="handleImport(scope.row)"/>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="getProjectFiles" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-dialog>
    <el-dialog :title="$t('load_test.scenario_list')" width="60%" :visible.sync="loadApiAutomationVisible">

      <el-table class="basic-config" :data="apiScenarios" v-loading="projectLoadingResult.loading">
        <el-table-column
          prop="num"
          label="ID">
        </el-table-column>
        <el-table-column
          prop="name"
          :label="$t('load_test.scenario_name')">
        </el-table-column>
        <el-table-column
          :label="$t('commons.operating')">
          <template v-slot:default="scope">
            <ms-table-operator-button :is-tester-permission="true"
                                      :tip="$t('api_test.api_import.label')"
                                      icon="el-icon-upload"
                                      @exec="handleImportApi(scope.row)"/>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="getProjectFiles" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-dialog>
  </div>
</template>

<script>
import {Message} from "element-ui";
import {findTestPlan, findThreadGroup} from "@/business/components/performance/test/model/ThreadGroup";
import MsTableButton from "@/business/components/common/components/MsTableButton";
import {getCurrentProjectID} from "@/common/js/utils";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import MsTableOperatorButton from "@/business/components/common/components/MsTableOperatorButton";

export default {
  name: "PerformanceBasicConfig",
  components: {MsTableOperatorButton, MsTablePagination, MsTableButton},
  props: {
    test: {
      type: Object
    },
    isReadOnly: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      result: {},
      projectLoadingResult: {},
      getFileMetadataPath: "/performance/file/metadata",
      jmxDownloadPath: '/performance/file/download',
      jmxDeletePath: '/performance/file/delete',
      fileList: [],
      tableData: [],
      uploadList: [],
      fileNumLimit: 10,
      threadGroups: [],
      loadFileVisible: false,
      currentPage: 1,
      pageSize: 5,
      total: 0,
      existFiles: [],
      loadType: 'jmx',
      apiScenarios: [],
      loadApiAutomationVisible: false,
    };
  },
  created() {
    if (this.test.id) {
      this.getFileMetadata(this.test)
    }
  },
  watch: {
    test() {
      if (this.test.id) {
        this.getFileMetadata(this.test)
      }
    },
    uploadList() {
      let self = this;
      let fileList = self.uploadList.filter(f => f.name.endsWith(".jmx"));
      if (fileList.length > 0) {
        let file = fileList[0];
        let jmxReader = new FileReader();
        jmxReader.onload = (event) => {
          self.threadGroups = findThreadGroup(event.target.result);
          self.$emit('fileChange', self.threadGroups);
        };
        jmxReader.readAsText(file);
      }
    }
  },
  methods: {
    getFileMetadata(test) {
      this.fileList = [];
      this.tableData = [];
      this.uploadList = [];
      this.result = this.$get(this.getFileMetadataPath + "/" + test.id, response => {
        let files = response.data;
        if (!files) {
          Message.error({message: this.$t('load_test.related_file_not_found'), showClose: true});
          return;
        }
        console.log(files);
        // deep copy
        this.fileList = JSON.parse(JSON.stringify(files));
        this.tableData = JSON.parse(JSON.stringify(files));
        this.tableData.map(f => {
          f.size = (f.size / 1024).toFixed(2) + ' KB';
        });
      })
    },
    deleteExistJmx: function () {
      // 只能上传一个jmx
      let jmxs = this.tableData.filter(f => {
        let type = f.name.substring(f.name.lastIndexOf(".") + 1);
        return type.toUpperCase() === 'JMX';
      });
      for (let i = 0; i < jmxs.length; i++) {
        let index = this.tableData.indexOf(jmxs[i]);
        if (index > -1) {
          this.tableData.splice(index, 1);
        }
        let index2 = this.uploadList.indexOf(jmxs[i]);
        if (index2 > -1) {
          this.uploadList.splice(index2, 1);
        }
      }

      jmxs = this.fileList.filter(f => {
        let type = f.name.substring(f.name.lastIndexOf(".") + 1);
        return type.toUpperCase() === 'JMX';
      });
      for (let i = 0; i < jmxs.length; i++) {
        let index3 = this.fileList.indexOf(jmxs[i]);
        if (index3 > -1) {
          this.fileList.splice(index3, 1);
        }
      }
    },
    beforeUploadJmx(file) {
      if (!this.fileValidator(file)) {
        /// todo: 显示错误信息
        return false;
      }
      this.deleteExistJmx();
      if (this.tableData.filter(f => f.name === file.name).length > 0) {
        this.$error(this.$t('load_test.delete_file'));
        return false;
      }

      let type = file.name.substring(file.name.lastIndexOf(".") + 1);

      this.tableData.push({
        name: file.name,
        size: (file.size / 1024).toFixed(2) + ' KB',
        type: type.toUpperCase(),
        updateTime: file.lastModified,
      });

      return true;
    },
    beforeUploadFile(file) {
      if (!this.fileValidator(file)) {
        /// todo: 显示错误信息
        return false;
      }
      if (this.tableData.filter(f => f.name === file.name).length > 0) {
        this.$error(this.$t('load_test.delete_file'));
        return false;
      }

      let type = file.name.substring(file.name.lastIndexOf(".") + 1);

      this.tableData.push({
        name: file.name,
        size: (file.size / 1024).toFixed(2) + ' KB',
        type: type.toUpperCase(),
        updateTime: file.lastModified,
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
          URL.revokeObjectURL(aTag.href)
        } else {
          // IE10+下载
          navigator.msSaveBlob(blob, this.filename)
        }
      }).catch(e => {
        Message.error({message: e.message, showClose: true});
      });
    },
    handleImport(row) {
      if (this.tableData.filter(f => f.name === row.name).length > 0) {
        this.$error(this.$t('load_test.delete_file'));
        return;
      }
      if (this.loadType === 'resource') {
        this.fileList.push(row);
        this.tableData.push(row);
        this.$success(this.$t('test_track.case.import.success'));
        this.loadFileVisible = false;
        return;
      }
      this.result = this.$get('/performance/get-jmx-content/' + row.testId, (response) => {
        if (response.data) {
          let testPlan = findTestPlan(response.data);
          testPlan.elements.forEach(e => {
            if (e.attributes.name === 'TestPlan.serialize_threadgroups') {
              this.serializeThreadgroups = Boolean(e.elements[0].text);
            }
          });
          this.threadGroups = findThreadGroup(response.data);
          this.threadGroups.forEach(tg => {
            tg.options = {};
          });
          this.$emit('fileChange', this.threadGroups);
        }
        this.deleteExistJmx();
        this.fileList.push(row);
        this.tableData.push(row);
        this.$success(this.$t('test_track.case.import.success'));
        this.loadFileVisible = false;
      });
    },
    countStrToBit(str) {
      let count = 0
      const arr = str.split('')
      arr.forEach(item => {
        count += Math.ceil(item.charCodeAt().toString(2).length / 8)
      })
      return count
    },
    handleImportApi(row) {
      let condition = {
        projectId: getCurrentProjectID(),
        ids: [row.id]
      };
      this.projectLoadingResult = this.$post('api/automation/export/jmx', condition, response => {
        let data = response.data[0];
        this.threadGroups = findThreadGroup(data.jmx);
        this.threadGroups.forEach(tg => {
          tg.options = {};
        });
        this.$emit('fileChange', this.threadGroups);
        this.deleteExistJmx();
        let bytes = this.countStrToBit(data.jmx);
        this.fileList.push({
          name: this.test.name + ".jmx",
          size: bytes,
          type: "JMX",
          updateTime: new Date().getTime(),
        });
        this.tableData.push({
          name: this.test.name + ".jmx",
          size: (bytes / 1024).toFixed(2) + ' KB',
          type: "JMX",
          updateTime: new Date().getTime(),
        });
        this.test.jmx = data.jmx;
        this.$success(this.$t('test_track.case.import.success'));
        this.loadApiAutomationVisible = false;
      })
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
    handleExceed() {
      this.$error(this.$t('load_test.file_size_limit'));
    },
    fileValidator(file) {
      /// todo: 是否需要对文件内容和大小做限制
      return file.size > 0;
    },
    updatedFileList() {
      return this.fileList;// 表示修改了已经上传的文件列表
    },
    loadJMX() {
      this.loadFileVisible = true;
      this.loadType = "jmx";
      this.getProjectFiles();
    },
    loadFile() {
      this.loadFileVisible = true;
      this.loadType = "resource";
      this.getProjectFiles();
    },
    loadApiAutomation() {
      this.loadApiAutomationVisible = true;
      this.getProjectScenarios();
    },
    getProjectFiles() {
      this.projectLoadingResult = this.$get('/performance/project/' + this.loadType + '/' + getCurrentProjectID() + "/" + this.currentPage + "/" + this.pageSize, res => {
        let data = res.data;
        this.total = data.itemCount;
        this.existFiles = data.listObject;
      })
    },
    getProjectScenarios() {
      let condition = {
        projectId: getCurrentProjectID(),
        filters: {status: ["Prepare", "Underway", "Completed"]}
      }
      this.projectLoadingResult = this.$post('/api/automation/list/' + this.currentPage + "/" + this.pageSize, condition, res => {
        let data = res.data;
        this.total = data.itemCount;
        this.apiScenarios = data.listObject;
      })
    },

    validConfig() {
      let newJmxNum = 0, oldJmxNum = 0, newCsvNum = 0, oldCsvNum = 0, newJarNum = 0, oldJarNum = 0;
      if (this.uploadList.length > 0) {
        this.uploadList.forEach(f => {
          if (f.name.toLowerCase().endsWith(".jmx")) {
            newJmxNum++;
          }
          if (f.name.toLowerCase().endsWith(".csv")) {
            newCsvNum++;
          }
          if (f.name.toLowerCase().endsWith(".jar")) {
            newJarNum++;
          }
        });
      }
      if (this.fileList.length > 0) {
        this.fileList.forEach(f => {
          if (f.name.toLowerCase().endsWith(".jmx")) {
            oldJmxNum++;
          }
          if (f.name.toLowerCase().endsWith(".csv")) {
            oldCsvNum++;
          }
          if (f.name.toLowerCase().endsWith(".jar")) {
            oldJarNum++;
          }
        });
      }
      if (newCsvNum + oldCsvNum + newJarNum + oldJarNum > this.fileNumLimit - 1) {
        this.handleExceed();
        return false;
      }
      if (newJmxNum + oldJmxNum !== 1) {
        this.$error(this.$t('load_test.jmx_is_null'));
        return false;
      }
      if (this.threadGroups.filter(tg => tg.attributes.enabled == 'true').length === 0) {
        this.$error(this.$t('load_test.threadgroup_at_least_one'));
        return false;
      }
      return true;
    }
  },
}
</script>

<style scoped>
.basic-config {
  width: 100%
}

.last-modified {
  margin-left: 5px;
}

.el-dialog >>> .el-dialog__body {
  padding: 10px 20px;
}
</style>
