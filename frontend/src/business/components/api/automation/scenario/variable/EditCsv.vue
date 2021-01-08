<template>
  <el-form :model="editData" label-position="right" label-width="80px" size="small" ref="form3">
    <el-form-item :label="$t('api_test.variable_name')" prop="name">
      <el-input v-model="editData.name" :placeholder="$t('api_test.variable_name')"></el-input>
    </el-form-item>

    <el-form-item :label="$t('commons.description')" prop="description">
      <el-input class="ms-http-textarea"
                v-model="editData.description"
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
            <ms-csv-file-upload :parameter="editData"/>
          </el-col>
        </el-row>
        <el-row style="margin-top: 10px">
          <el-col :span="4" style="margin-top: 5px">
            <span>Encoding</span>
          </el-col>
          <el-col :span="20">
            <el-input v-model="editData.encoding" size="small"/>
          </el-col>
        </el-row>
        <el-row style="margin-top: 10px">
          <el-col :span="4" style="margin-top: 5px">
            <span>分隔符</span>
          </el-col>
          <el-col :span="20">
            <el-input v-model="editData.splits" size="small"/>
          </el-col>
        </el-row>

      </el-tab-pane>
      <el-tab-pane :label="$t('schema.preview')" name="preview">
        <el-table
          :data="previewData"
          style="width: 100%"
          height="400px"
          v-loading="loading">
          <!-- 自定义列的遍历-->
          <el-table-column v-for="(item, index) in columns" :key="index" :label="columns[index]" align="left" width="180">
            <!-- 数据的遍历  scope.row就代表数据的每一个对象-->
            <template slot-scope="scope">
              <span>{{scope.row[index]}}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </el-form>
</template>

<script>
  import MsCsvFileUpload from "./CsvFileUpload";

  export default {
    name: "MsEditCsv",
    components: {
      MsCsvFileUpload
    },
    props: {
      editData: {},
    },
    data() {
      return {
        activeName: "config",
        visible: false,
        loading: false,
        editFlag: false,
        previewData: [],
        columns: [],
        rule: {
          name: [
            {required: true, message: this.$t('api_test.variable_name'), trigger: 'blur'},
          ],
        },
      }
    },
    methods: {
      complete(results) {
        if (results.errors && results.errors.length > 0) {
          this.$error(results.errors);
          return;
        }
        if (results.data) {
          this.columns = results.data[0];
          this.previewData = results.data;
        }
        this.loading = false;
      },
      handleClick() {
        let config = {complete: this.complete};
        // 本地文件
        if (this.editData.files && this.editData.files.length > 0 && this.editData.files[0].file) {
          this.loading = true;
          this.$papa.parse(this.editData.files[0].file, config);
        }
        // 远程下载文件
        if (this.editData.files && this.editData.files.length > 0 && !this.editData.files[0].file) {
          let file = this.editData.files[0];
          let conf = {
            url: "/api/automation/file/download",
            method: 'post',
            data: file,
            responseType: 'blob',
          };
          this.result = this.$request(conf).then(response => {
            const content = response.data;
            const blob = new Blob([content]);
            this.loading = true;
            this.$papa.parse(blob, config);
          });
        }
      },
    }
  }
</script>

<style scoped>

</style>
