<template>
  <el-dialog append-to-body :close-on-click-modal="false" title="CSV编辑"
             :visible.sync="visible" class="environment-dialog" width="600px"
             @close="close">
    <el-form :model="form" label-position="right" label-width="80px" size="small" ref="form" :rules="rule">
      <el-form-item :label="$t('api_test.variable_name')" prop="name">
        <el-input v-model="form.name" :placeholder="$t('api_test.variable_name')"></el-input>
      </el-form-item>

      <el-form-item :label="$t('commons.description')" prop="description">
        <el-input class="ms-http-textarea"
                  v-model="form.description"
                  type="textarea"
                  :autosize="{ minRows: 2, maxRows: 10}"
                  :rows="2" size="small"/>
      </el-form-item>
      <el-tabs v-model="activeName" @tab-click="handleClick" style="margin-left: 40px">
        <el-tab-pane label="配置" name="config">
          <el-row>
            <el-col :span="4" style="margin-top: 5px">
              <span>添加文件</span>
            </el-col>
            <el-col :span="20">
              <ms-csv-file-upload :parameter="form.files"/>
            </el-col>
          </el-row>
          <el-row style="margin-top: 10px">
            <el-col :span="4" style="margin-top: 5px">
              <span>Encoding</span>
            </el-col>
            <el-col :span="20">
              <el-input v-model="form.encoding" size="small"/>
            </el-col>
          </el-row>
          <el-row style="margin-top: 10px">
            <el-col :span="4" style="margin-top: 5px">
              <span>分隔符</span>
            </el-col>
            <el-col :span="20">
              <el-input v-model="form.splits" size="small"/>
            </el-col>
          </el-row>

        </el-tab-pane>
        <el-tab-pane :label="$t('schema.preview')" name="preview">配置管理</el-tab-pane>
      </el-tabs>
    </el-form>
    <template v-slot:footer>
      <ms-dialog-footer
        @cancel="close"
        @confirm="saveParameters"/>
    </template>

  </el-dialog>
</template>

<script>
  import MsDialogFooter from "../../../../common/components/MsDialogFooter";
  import MsCsvFileUpload from "./CsvFileUpload";

  export default {
    name: "MsEditCsv",
    components: {
      MsDialogFooter,
      MsCsvFileUpload
    },
    data() {
      return {
        activeName: "config",
        visible: false,
        form: {type: "CSV", files: {}},
        editFlag: false,
        rule: {
          name: [
            {required: true, message: this.$t('api_test.variable_name'), trigger: 'blur'},
          ],
        },
      }
    },
    methods: {
      open: function (v) {
        this.visible = true;
        if (v) {
          this.form = v;
          this.editFlag = true;
        } else {
          this.form = {files: {}};
          this.editFlag = false;
        }
        this.form.type = "CSV";
      },
      close() {
        this.visible = false;
        this.form = {files: {}};
      },
      handleClick() {

      },
      saveParameters() {
        this.$refs['form'].validate((valid) => {
          if (valid) {
            this.visible = false;
            if (!this.editFlag) {
              this.$emit('addParameters', this.form);
            }
          }
        });
      }
    }
  }
</script>

<style scoped>

</style>
