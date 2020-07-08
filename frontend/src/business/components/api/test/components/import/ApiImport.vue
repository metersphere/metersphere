<template>
  <el-dialog :title="$t('api_test.api_import.title')" :visible.sync="visible" class="api-import" v-loading="result.loading">

    <div class="data-format">
      <div>{{$t('api_test.api_import.data_format')}}</div>
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
        <div class="el-upload__text" v-html="$t('load_test.upload_tips')"></div>
        <div class="el-upload__tip" slot="tip">{{$t('api_test.api_import.file_size_limit')}}</div>
      </el-upload>
    </div>

    <div class="format-tip">
      <div>
        <span>{{$t('api_test.api_import.tip')}}：{{selectedPlatform.tip}}</span>
      </div>
      <div>
        <span>{{$t('api_test.api_import.export_tip')}}：{{selectedPlatform.exportTip}}</span>
      </div>
    </div>

  </el-dialog>
</template>

<script>
    import MsDialogFooter from "../../../../common/components/MsDialogFooter";
    export default {
      name: "ApiImport",
      components: {MsDialogFooter},
      data() {
        return {
          visible: false,
          platforms: [
            {
              name: 'Metersphere',
              value: 'Metersphere',
              tip: this.$t('api_test.api_import.ms_tip'),
              exportTip: this.$t('api_test.api_import.ms_export_tip'),
              suffixes: new Set(['json'])
            },
            {
              name: 'Postman',
              value: 'Postman',
              tip: this.$t('api_test.api_import.postman_tip'),
              exportTip: this.$t('api_test.api_import.post_man_export_tip'),
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
        selectedPlatformValue() {
          for (let i in this.platforms) {
            if (this.platforms[i].value === this.selectedPlatformValue) {
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
          this.result = this.$fileUpload('/api/import/' + this.selectedPlatformValue, this.fileList, response => {
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
            this.$warning(this.$t('api_test.api_import.suffixFormatErr'));
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
