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
  import {Test} from "./model/ScenarioModel"
  import MsApiScenarioConfig from "./components/ApiScenarioConfig";
  import MsApiReportStatus from "../report/ApiReportStatus";
  import MsApiReportDialog from "./ApiReportDialog";
  import {getUUID} from "@/common/js/utils";


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
        change: false,
        projectId: "",
        rule: {
          testName: [
            {required: true, message: this.$t('api_test.input_name'), trigger: 'blur'},
          ],
        }
      };
    },
    watch: {

      test: {
        handler: function () {
          this.change = true;
        },
        deep: true
      }
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
      checkedSaveAndRunTest() {
        if (this.ruleForm.testName) {
          if (this.selectProjectNames.size > 1) {
            this.$warning(this.$t('load_test.same_project_test'));
            this.oneClickOperationVisible = false;
            this.$emit('refresh')
          } else {
            this.checkNameResult(this.ruleForm.testName)
          }
        } else {
          this.$warning(this.$t('api_test.input_name'))
        }
      },
      checkNameResult() {
        this.checkName(() => {
          for (let x of this.selectIds) {
            this.getTest(x)
          }
        })
      },
      checkName(callback) {
        for (let i of this.selectProjectId) {
          this.result = this.$post('/api/checkName', {name: this.ruleForm.testName, projectId: i}, () => {
            if (callback) callback();
          })
        }
      },
      _getEnvironmentAndRunTest: function (item) {
        this.result = this.$get('/api/environment/list/' + item.projectId, response => {
          let environments = response.data;
          let environmentMap = new Map();
          environments.forEach(environment => {
            environmentMap.set(environment.id, environment);
          });
          this.test.scenarioDefinition.forEach(scenario => {
              if (scenario.environmentId) {
                scenario.environment = environmentMap.get(scenario.environmentId);
              }
            }
          )
          this.tests = [];
          this.saveRunTest();
          this.oneClickOperationVisible = false;
          this.$emit('refresh')
        });
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
              this._getEnvironmentAndRunTest(item);
            }
          }
        });
      },
      saveRunTest() {
        this.change = false;
        this.save(() => {
          this.$success(this.$t('commons.save_success'));
          this.runTest();
        })
      },
      save(callback) {
        this.change = false;
        let url = "/api/create";
        let bodyFiles = this.getBodyUploadFiles();
        this.result = this.$request(this.getOptions(url, bodyFiles), () => {
          if (callback) callback();
        });
      },
      getBodyUploadFiles() {
        let bodyUploadFiles = [];
        this.test.bodyUploadIds = [];
        this.test.scenarioDefinition.forEach(scenario => {
          scenario.requests.forEach(request => {
            if (request.body) {
              request.body.kvs.forEach(param => {
                if (param.files) {
                  param.files.forEach(item => {
                    if (item.file) {
                      let fileId = getUUID().substring(0, 8);
                      item.name = item.file.name;
                      item.id = fileId;
                      this.test.bodyUploadIds.push(fileId);
                      bodyUploadFiles.push(item.file);
                      // item.file = undefined;
                    }
                  });
                }
              });
            }
          });
        });
        return bodyUploadFiles;
      },
      runTest() {
        this.result = this.$post("/api/run", {id: this.test.id, triggerMode: 'MANUAL'}, (response) => {
          this.$success(this.$t('api_test.running'));
          this.$router.push({
            path: '/api/report/view/' + response.data
          })
          this.test = ""
        });
      },
      getOptions(url, bodyFiles) {

        let formData = new FormData();
        if (bodyFiles) {
          bodyFiles.forEach(f => {
            formData.append("files", f);
          })
        }
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
