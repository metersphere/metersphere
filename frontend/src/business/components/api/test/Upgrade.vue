<template>
  <el-dialog
    title="选则模块"
    :visible.sync="oneClickOperationVisible"
    width="600px"
    left
    :destroy-on-close="true"
    show-close
    @closed="handleClose" v-loading="loading">
    <el-form :model="ruleForm" label-position="right" label-width="80px" size="small" :rules="rule">
      <el-form-item :label="$t('test_track.module.module')" prop="apiScenarioModuleId">
        <el-select size="small" style="width: 80%" v-model="apiScenarioModuleId">
          <el-option v-for="item in moduleOptions" :key="item.id" :label="item.path" :value="item.id"/>
        </el-select>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
   <ms-dialog-footer
     @cancel="oneClickOperationVisible = false"
     @confirm="confirm"/>
  </span>
  </el-dialog>
</template>

<script>
  import MsDialogFooter from '../../common/components/MsDialogFooter'
  import MsApiScenarioConfig from "./components/ApiScenarioConfig";
  import MsApiReportStatus from "../report/ApiReportStatus";
  import MsApiReportDialog from "./ApiReportDialog";
  import {getUUID, getCurrentProjectID} from "@/common/js/utils";
  import {buildNodePath} from "../definition/model/NodeTree";


  export default {
    name: "MsUpgrade",
    components: {
      MsApiReportDialog, MsApiReportStatus, MsApiScenarioConfig, MsDialogFooter
    },
    data() {
      return {
        oneClickOperationVisible: false,
        apiScenarioModuleId: "",
        moduleOptions: [],
        ruleForm: {},
        loading: false,
        rule: {
          apiScenarioModuleId: [
            {required: true, message: this.$t('test_track.module.module'), trigger: 'blur'},
          ],
        }
      };
    },
    created() {
      this.initModule();
    },
    props: {
      selectIds: {
        type: Set
      },
      selectProjectNames: {
        type: Set
      },
      selectProjectId: {
        type: Set
      }
    },
    methods: {
      openOneClickOperation() {
        this.oneClickOperationVisible = true;
      },
      getPath(id) {
        let path = this.moduleOptions.filter(function (item) {
          return item.id === id ? item.path : "";
        });
        return path[0].path;
      },
      confirm() {
        this.loading = true;
        let arr = Array.from(this.selectIds);
        let obj = {testIds: arr, projectId: getCurrentProjectID(), modulePath: this.getPath(this.apiScenarioModuleId), moduleId: this.apiScenarioModuleId};
        this.$post("/api/historicalDataUpgrade", obj, response => {
          this.loading = false;
          this.$success(this.$t('organization.integration.successful_operation'));
          this.oneClickOperationVisible = false;
        })
      },
      initModule() {
        let url = "/api/automation/module/list/" + getCurrentProjectID();
        this.$get(url, response => {
          if (response.data != undefined && response.data != null) {
            this.data = response.data;
            let modules = [];
            this.data.forEach(node => {
              buildNodePath(node, {path: ''}, modules);
            });
            this.moduleOptions = modules;
          }
        });
      },
      handleClose() {
        this.ruleForm = {}
      },
    }
  }
</script>

<style scoped>

</style>
