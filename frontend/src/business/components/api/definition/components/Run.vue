<template>
  <div></div>
</template>
<script>
  import {Scenario} from "../model/ApiTestModel";
  import {getUUID} from "@/common/js/utils";

  export default {
    name: 'MsRun',
    components: {},
    props: {
      environment: Object,
      debug: Boolean,
      reportId: String,
      runData: Array,
    },
    data() {
      return {
        result: {},
        loading: false,
        runId: "",
      }
    },

    watch: {
      // 初始化
      reportId() {
        this.run()
      }
    },
    methods: {
      getResult() {
        if (this.runId) {
          let url = "";
          if (this.debug) {
            url = "/api/definition/report/get/" + this.runId + "/" + "debug";
          } else {
            url = "/api/definition/report/get/" + this.runId + "/" + "run";
          }
          this.$get(url, response => {
            if (response.data) {
              let data = JSON.parse(response.data.content);
              this.$emit('runRefresh', data);
            } else {
              setTimeout(this.getResult, 2000);
            }
          });
        }
      },
      getBodyUploadFiles(obj) {
        let bodyUploadFiles = [];
        obj.bodyUploadIds = [];
        this.runData.forEach(request => {
          if (request.body) {
            request.body.kvs.forEach(param => {
              if (param.files) {
                param.files.forEach(item => {
                  if (item.file) {
                    let fileId = getUUID().substring(0, 8);
                    item.name = item.file.name;
                    item.id = fileId;
                    obj.bodyUploadIds.push(fileId);
                    bodyUploadFiles.push(item.file);
                  }
                });
              }
            });
          }
        });
        return bodyUploadFiles;
      },
      run() {
        let reqObj = {};
        let scenario = new Scenario({requests: this.runData});
        let url = "";
        let reportId = getUUID().substring(0, 8);
        let bodyFiles = this.getBodyUploadFiles(reqObj);
        scenario.requests.forEach(item => {
          if (this.environment != null) {
            item.useEnvironment = true;
            scenario.environmentId = this.environment.id;
          }
          item.definition = true;
        });
        scenario.name = reportId;
        scenario.dubboConfig = null;
        scenario.tcpConfig = null;
        reqObj.name = reportId;
        reqObj.scenario = scenario;
        reqObj.id = reportId;
        if (this.debug) {
          url = "/api/definition/run/debug";
        } else {
          reqObj.reportId = "run";
          url = "/api/definition/run";
        }
        this.$fileUpload(url, null, bodyFiles, reqObj, response => {
          this.runId = response.data;
          this.getResult();
        }, erro => {
          this.$emit('runRefresh', {});
        });
      }
    }
  }
</script>
