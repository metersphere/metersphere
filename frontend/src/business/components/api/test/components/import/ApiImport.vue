<template>
  <el-dialog :title="'接口测试导入'" :visible.sync="visible" class="api-import" v-loading="result.loading">

    <div class="data-format">
      <div>数据格式</div>
      <el-radio-group v-model="selectedPlatformValue">
        <el-radio v-for="(item, index) in platforms" :key="index" :label="item.value">{{item.name}}</el-radio>
      </el-radio-group>
    </div>

    <div  class="api-upload">
      <el-upload
        drag
        action=""
        :http-request="upload"
        :limit="1"
        :beforeUpload="uploadValidate"
        :show-file-list="false"
        :file-list="fileList"
        multiple>
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <div class="el-upload__tip" slot="tip">文件大小不超过 20 M</div>
      </el-upload>
    </div>

    <div class="format-tip">
      <div>
        <span>说明：{{selectedPlatform.tip}}</span>
      </div>
      <div>
        <span>导出方法：{{selectedPlatform.exportTip}}</span>
      </div>
    </div>

  </el-dialog>
</template>

<script>
    import MsDialogFooter from "../../../../common/components/MsDialogFooter";
    export default {
      name: "ApiImport",
      components: {MsDialogFooter},
      props: ['projectId'],
      data() {
        return {
          visible: false,
          platforms: [
            {
              name: 'Metersphere',
              value: 'Metersphere',
              tip: '支持 Metersphere json 格式',
              exportTip: '通过 Metersphere Api 测试页面或者浏览器插件导出 json 格式文件',
              suffixes: new Set(['json'])
            }
          ],
          selectedPlatform: {},
          selectedPlatformValue: 'Metersphere',
          fileList: [],
          result: {},
        }
      },
      created() {
        this.selectedPlatform = this.platforms[0];
      },
      watch: {
        selectedPlatformId() {
          for (let i in this.platforms) {
            if (this.platforms[i].id === this.selectedPlatformValue) {
              this.selectedPlatform = this.platforms[i];
              break;
            }
          }
        }
      },
      methods: {
        open() {
          this.visible = true;
        },
        upload(file) {
          this.fileList.push(file.file);
          this.result = this.$fileUpload('/api/import/' + this.selectedPlatformValue + '/' + this.projectId, this.fileList, response => {
            let res = response.data;
            this.$success(this.$t('test_track.case.import.success'));
            this.visible = false;
            this.$router.push({path: '/api/test/edit', query: {id: res.id}});
          });
          this.fileList = [];
        },
        uploadValidate(file, fileList) {
          let suffix = file.name.substring(file.name.lastIndexOf('.') + 1);
          if (!this.selectedPlatform.suffixes.has(suffix)) {
            this.$warning("格式错误");
            return false;
          }

          if (file.size / 1024 / 1024 > 20) {
            this.$warning(this.$t('test_track.case.import.upload_limit_size'));
            return false;
          }
          return true;
        }
      }
    }
</script>

<style scoped>

  .format-tip {
    background: #EDEDED;
  }

  .api-upload {
    text-align: center;
  }

  .el-radio-group {
    margin: 10px 0;
  }

  .data-format,.format-tip,.api-upload {
    border: solid #E1E1E1 1px;
    margin: 10px 0;
    padding: 10px;
    border-radius: 3px;
  }

  .api-import >>> .el-dialog__body {
    padding: 15px 25px;
  }

</style>
