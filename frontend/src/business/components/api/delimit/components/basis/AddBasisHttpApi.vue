<template>
  <el-dialog :close-on-click-modal="false" :title="$t('api_test.delimit.request.title')" :visible.sync="httpVisible"
             width="45%"
             :destroy-on-close="true">
    <el-form :model="httpForm" label-position="right" label-width="80px" size="small" :rules="rule" ref="httpForm">
      <el-form-item :label="$t('commons.name')" prop="name">
        <el-input v-model="httpForm.name" autocomplete="off" :placeholder="$t('commons.name')"/>
      </el-form-item>

      <el-form-item :label="$t('api_report.request')" prop="url">
        <el-input :placeholder="$t('api_test.delimit.request.path_info')" v-model="httpForm.url"
                  class="ms-http-input" size="small">
          <el-select v-model="httpForm.path" slot="prepend" style="width: 100px" size="small">
            <el-option v-for="item in options" :key="item.id" :label="item.label" :value="item.id"/>
          </el-select>
        </el-input>
      </el-form-item>
      <el-form-item :label="$t('api_test.delimit.request.responsible')" prop="userId">
        <el-select v-model="httpForm.userId"
                   :placeholder="$t('api_test.delimit.request.responsible')" filterable size="small"
                   style="width: 100%">
          <el-option
            v-for="item in maintainerOptions"
            :key="item.id"
            :label="item.id + ' (' + item.name + ')'"
            :value="item.id">
          </el-option>
        </el-select>

      </el-form-item>
      <el-form-item :label="$t('commons.description')" prop="description" style="margin-bottom: 29px">
        <el-input class="ms-http-textarea" v-model="httpForm.description"
                  type="textarea"
                  :autosize="{ minRows: 2, maxRows: 10}"
                  :rows="2" size="small"/>
      </el-form-item>

    </el-form>

    <template v-slot:footer>
      <ms-dialog-footer
        @cancel="httpVisible = false"
        @confirm="saveApi"/>
    </template>
  </el-dialog>
</template>

<script>
  import MsDialogFooter from "../../../../common/components/MsDialogFooter";
  import {WORKSPACE_ID} from '../../../../../../common/js/constants';
  import {Test} from "../../model/ScenarioModel"
  import {REQ_METHOD} from "../../model/JsonData";
  import {getCurrentUser} from "../../../../../../common/js/utils";

  export default {
    name: "MsAddBasisHttpApi",
    components: {MsDialogFooter},
    props: {},
    data() {
      return {
        httpForm: {},
        httpVisible: false,
        currentModule: {},
        projectId: "",
        maintainerOptions: [],
        rule: {
          name: [
            {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
            {max: 50, message: this.$t('test_track.length_less_than') + '50', trigger: 'blur'}
          ],
          url: [{required: true, message: this.$t('api_test.delimit.request.path_info'), trigger: 'blur'}],
          userId: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
        },
        value: REQ_METHOD[0].id,
        options: REQ_METHOD,
      }
    },
    methods: {
      saveApi() {
        this.$refs['httpForm'].validate((valid) => {
          if (valid) {
            let bodyFiles = [];
            let url = "/api/delimit/create";
            let test = new Test();
            this.httpForm.bodyUploadIds = [];
            this.httpForm.request = test.request;
            this.httpForm.request.url = this.httpForm.url;
            this.httpForm.request.path = this.httpForm.path;
            this.httpForm.projectId = this.projectId;
            this.httpForm.id = test.id;
            if (this.currentModule != null) {
              this.httpForm.modulePath = this.currentModule.path != undefined ? this.currentModule.path : null;
              this.httpForm.moduleId = this.currentModule.id;
            }
            let jmx = test.toJMX();
            let blob = new Blob([jmx.xml], {type: "application/octet-stream"});
            let file = new File([blob], jmx.name);

            this.result = this.$fileUpload(url, file, bodyFiles, this.httpForm, () => {
              this.httpVisible = false;
              this.$parent.refresh(this.currentModule);
            });
          } else {
            return false;
          }
        })
      },
      getMaintainerOptions() {
        let workspaceId = localStorage.getItem(WORKSPACE_ID);
        this.$post('/user/ws/member/tester/list', {workspaceId: workspaceId}, response => {
          this.maintainerOptions = response.data;
        });
      }
      ,
      open(currentModule, projectId) {
        this.httpForm = {path: REQ_METHOD[0].id, userId: getCurrentUser().id};
        this.currentModule = currentModule;
        this.projectId = projectId;
        this.getMaintainerOptions();
        this.httpVisible = true;
      }
      ,
    }
  }
</script>

<style scoped>

  .ht-btn-remove {
    color: white;
    background-color: #DCDFE6;
  }

  .ht-btn-confirm {
    color: white;
    background-color: #1483F6;
  }

  .ht-btn-add {
    border: 0px;
    margin-top: 10px;
    color: #1483F6;
    background-color: white;
  }

  .ht-tb {
    background-color: white;
    border: 0px;
  }
</style>
