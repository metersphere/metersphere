<template>
  <el-dialog
    :title="$t('load_test.run')"
    :visible.sync="oneClickOperationVisible"
    width="30%"
    left
    :destroy-on-close="true"
    show-close
    @closed="handleClose">
    <el-form :model="ruleForm" label-position="right" label-width="120px" size="small" :rules="rule">
      <el-form-item :label="$t('test_track.case.test_name')" prop="testName">
        <el-input v-model="ruleForm.testName" autocomplete="off" clearable show-word-limit></el-input>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
   <ms-dialog-footer
     @cancel="oneClickOperationVisible = false"
     @confirm="checkedSaveAndRunTest"/>
  </span>
  </el-dialog>
</template>

<script>
  import MsDialogFooter from '../../common/components/MsDialogFooter'
  import {Test} from "./model/ScenarioModel";
  import MsApiScenarioConfig from "./components/ApiScenarioConfig";
  import MsApiReportStatus from "../report/ApiReportStatus";
  import MsApiReportDialog from "./ApiReportDialog";
  import {removeGoBackListener} from "../../../../common/js/utils";


  export default {
    name: "OneClickOperation",
    components: {
      MsApiReportDialog, MsApiReportStatus, MsApiScenarioConfig, MsDialogFooter
    },
    data() {
      return {
        oneClickOperationVisible: false,
        test: null,
        tests: [],
        ruleForm: {},
        rule: {
          testName: [
            {required: true, message: this.$t('api_test.input_name'), trigger: 'blur'},
          ],
        }
      };
    },
    props: {
      selectIds: {
        type: Set
      },
      selectNames: {
        type: Set
      },
      selectProjectNames: {
        type: Set
      }
    },
    methods: {
      openOneClickOperation() {
        this.oneClickOperationVisible = true;
      },
      checkedSaveAndRunTest() {
        if (this.selectNames.has(this.testName)) {
          this.$warning(this.$t('load_test.already_exists'));
        } else {
          if (this.selectProjectNames.size > 1) {
            this.$warning(this.$t('load_test.same_project_test'));
          } else {
            for (let x of this.selectIds) {
              this.getTest(x)
            }
          }
        }
      },
      getTest(id) {
        this.result = this.$get("/api/get/" + id, response => {
          if (response.data) {
            let item = response.data;
            this.tests.push(item);
            let test = new Test({
              projectId: item.projectId,
              name: this.ruleForm.testName,
              scenarioDefinition: JSON.parse(item.scenarioDefinition),
              schedule: {},
            });
            this.test = this.test || test;
            if (this.tests.length > 1) {
              this.test.scenarioDefinition = this.test.scenarioDefinition.concat(test.scenarioDefinition);

            }
            if (this.tests.length === this.selectIds.size) {
              this.tests = [];
              this.saveRunTest();
              this.oneClickOperationVisible = false;


            }
          }
        });
      },
      saveRunTest() {
        this.save(() => {
          this.$success(this.$t('commons.save_success'));
          this.runTest();
        })
      },
      save(callback) {
        let url = "/api/create";
        this.result = this.$request(this.getOptions(url), () => {
          this.create = false;
          if (callback) callback();
        });
      },
      runTest() {
        this.result = this.$post("/api/run", {id: this.test.id, triggerMode: 'MANUAL'}, (response) => {
          this.$success(this.$t('api_test.running'));
          this.$router.push({
            path: '/api/report/view/' + response.data
          })
        });
      },
      getOptions(url) {
        let formData = new FormData();
        let requestJson = JSON.stringify(this.test);
        formData.append('request', new Blob([requestJson], {
          type: "application/json"
        }));
        let jmx = this.test.toJMX();
        let blob = new Blob([jmx.xml], {type: "application/octet-stream"});
        formData.append("file", new File([blob], jmx.name));
        return {
          method: 'POST',
          url: url,
          data: formData,
          headers: {
            'Content-Type': undefined
          }
        };
      },
      handleClose() {
        this.ruleForm = {}
      },
    }
  }
</script>

<style scoped>

</style>
