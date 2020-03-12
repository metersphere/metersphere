<template>
  <div v-loading="result.loading">
    <el-upload
      accept=".jmx"
      drag
      action=""
      :limit="1"
      :show-file-list="false"
      :before-upload="beforeUpload"
      :http-request="handleUpload"
      :on-exceed="handleExceed"
      :file-list="fileList">
      <i class="el-icon-upload"/>
      <div class="el-upload__text" v-html="$t('load_test.upload_tips')"></div>
      <div class="el-upload__tip" slot="tip">{{$t('load_test.upload_type')}}</div>
    </el-upload>

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
        <template slot-scope="scope">
          <i class="el-icon-time"/>
          <span class="last-modified">{{ scope.row.lastModified | timestampFormatDate }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="status"
        :label="$t('load_test.file_status')">
      </el-table-column>
      <el-table-column
        :label="$t('commons.operating')">
        <template slot-scope="scope">
          <el-button @click="handleDownload(scope.row)" :disabled="!scope.row.id" type="primary" icon="el-icon-download"
                     size="mini" circle/>
          <el-button @click="handleDelete(scope.row, scope.$index)" type="danger" icon="el-icon-delete" size="mini"
                     circle/>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
  import {Message} from "element-ui";

  export default {
    name: "PerformanceBasicConfig",
    props: ["testPlan"],
    data() {
      return {
        result: {},
        getFileMetadataPath: "/testplan/file/metadata",
        jmxDownloadPath: '/testplan/file/download',
        jmxDeletePath: '/testplan/file/delete',
        fileList: [],
        tableData: [],
      };
    },
    created() {
      if (this.testPlan.id) {
        this.getFileMetadata(this.testPlan)
      }
    },
    watch: {
      testPlan() {
        if (this.testPlan.id) {
          this.getFileMetadata(this.testPlan)
        }
      }
    },
    methods: {
      getFileMetadata(testPlan) {
        this.fileList = [];// 一个测试只有一个文件
        this.tableData = [];// 一个测试只有一个文件
        this.result = this.$get(this.getFileMetadataPath + "/" + testPlan.id, response => {
          let file = response.data;

          if (!file) {
            Message.error({message: this.$t('load_test.related_file_not_found'), showClose: true});
            return;
          }

          this.testPlan.file = file;
          this.fileList.push({
            id: file.id,
            name: file.name
          });

          this.tableData.push({
            id: file.id,
            name: file.name,
            size: file.size + 'Byte', /// todo: 按照大小显示Byte、KB、MB等
            type: 'JMX',
            lastModified: file.updateTime,
            status: 'todo',
          });
        })
      },
      beforeUpload(file) {
        if (!this.fileValidator(file)) {
          /// todo: 显示错误信息
          return false;
        }

        this.tableData.push({
          name: file.name,
          size: file.size + 'Byte', /// todo: 按照大小显示Byte、KB、MB等
          type: 'JMX',
          lastModified: file.lastModified,
          status: 'todo',
        });

        return true;
      },
      handleUpload(uploadResources) {
        this.testPlan.file = uploadResources.file;
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
      handleDelete(file, index) {
        this.$alert(this.$t('commons.delete_file_confirm') + file.name + "？", '', {
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
        this.testPlan.file = null;
      },
      handleExceed() {
        this.$message.error(this.$t('load_test.delete_file'));
      },
      fileValidator(file) {
        /// todo: 是否需要对文件内容和大小做限制
        return file.size > 0;
      },
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
</style>
