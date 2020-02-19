<template>
  <div>
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
      <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
      <div class="el-upload__tip" slot="tip">只能上传jmx文件</div>
    </el-upload>

    <el-table
      :data="tableData"
      style="width: 100%">
      <el-table-column
        prop="name"
        label="文件名">
      </el-table-column>
      <el-table-column
        prop="size"
        label="文件大小">
      </el-table-column>
      <el-table-column
        prop="type"
        label="文件类型">
      </el-table-column>
      <el-table-column
        label="修改时间">
        <template slot-scope="scope">
          <i class="el-icon-time"/>
          <span style="margin-left: 10px">{{ scope.row.lastModified | timestampFormatDate }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="status"
        label="文件状态">
      </el-table-column>
      <el-table-column
        label="操作">
        <template slot-scope="scope">
          <el-button @click="handleDownload(scope.row)" type="text" size="small">下载</el-button>
          <el-button @click="handleDelete(scope.row, scope.$index)" type="text" size="small">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
  import {Message} from "element-ui";

  export default {
    name: "TestPlanBasicConfig",
    props: ["testPlan"],
    data() {
      return {
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
    methods: {
      getFileMetadata(testPlan) {
        this.$get(this.getFileMetadataPath + "/" + testPlan.id, response => {
          let file = response.data;

          if (!file) {
            Message.error({message: "未找到关联的测试文件！", showClose: true});
            return;
          }

          this.testPlan.file = file;

          this.fileList.push({
            id: file.id,
            name: file.name
          });

          this.tableData.push({
            name: file.name,
            size: file.size + 'Byte', /// todo: 按照大小显示Byte、KB、MB等
            type: 'JMX',
            lastModified: file.lastModified,
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
          name: file.name
        };

        this.$post(this.jmxDownloadPath, data, response => {
          const content = response.data;
          const blob = new Blob([content]);
          if ("download" in document.createElement("a")) {
            // 非IE下载
            //  chrome/firefox
            let aTag = document.createElement('a');
            aTag.download = file.name;
            aTag.href = URL.createObjectURL(blob)
            aTag.click();
            URL.revokeObjectURL(aTag.href)
          } else {
            // IE10+下载
            navigator.msSaveBlob(blob, this.filename)
          }
        });
      },
      handleDelete(file, index) {
        this.$alert('确认删除文件: ' + file.name + "？", '', {
          confirmButtonText: '确定',
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
        this.$message.error("请先删除已存在的文件！");
      },
      fileValidator(file) {
        /// todo: 是否需要对文件内容和大小做限制
        return file.size > 0;
      },
    },
  }
</script>
