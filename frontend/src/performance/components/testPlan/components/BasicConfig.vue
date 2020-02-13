<template>
  <div>
    <el-upload
      accept=".jmx"
      drag
      :limit="1"
      :show-file-list="false"
      :action="jmxUploadPath"
      :before-upload="beforeUpload"
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
  export default {
    name: "TestPlanBasicConfig",
    data() {
      return {
        jmxUploadPath: '/testplan/file/upload',
        jmxDownloadPath: '/testplan/file/download',
        jmxDeletePath: '/testplan/file/delete',
        fileList: [],
        tableData: [],
      };
    },
    methods: {
      beforeUpload(file) {
        window.console.log(file);

        /// todo: 上传的文件需要绑定到当前testPlan，这里暂时使用文件名
        this._changeTestPlan(function (testPlan) {
          testPlan.fileId = file.name;
        });

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
      handleDownload(file) {
        let data = {
          name: file.name
        };

        this.$post(this.jmxDownloadPath, data).then(response => {
          if (response) {
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
          }
        }).catch((response) => {
          this.$message.error(response.message);
        });
      },
      handleDelete(file, index) {
        this.$alert('确认删除文件: ' + file.name + "？", '', {
          confirmButtonText: '确定',
          callback: () => {
            this._handleDelete(file, index);
          }
        });
      },
      _handleDelete(file, index) {
        let data = {
          name: file.name
        };

        this.$post(this.jmxDeletePath, data).then(response => {
          if (response.data.success) {
            this.fileList.splice(index, 1);
            this.tableData.splice(index, 1);

            this.$message({
              message: '删除成功！',
              type: 'success'
            });
          } else {
            this.$message.error(response.message);
          }
        });
      },
      _changeTestPlan(updateTestPlanFunc) {
        this.$emit('change-test-plan', updateTestPlanFunc);
      },
      fileValidator(file) {
        /// todo: 是否需要对文件内容和大小做限制
        return file.size > 0;
      },
    },
  }
</script>
