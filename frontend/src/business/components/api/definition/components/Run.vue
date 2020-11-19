<template>
  <div></div>
</template>
<script>
  import {getUUID} from "@/common/js/utils";
  import HeaderManager from "./jmeter/components/configurations/header-manager";
  import ThreadGroup from "./jmeter/components/thread-group";
  import TestPlan from "./jmeter/components/test-plan";

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
            request.body.binary.forEach(param => {
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
        let testPlan = new TestPlan();
        let threadGroup = new ThreadGroup();
        threadGroup.hashTree = [];
        testPlan.hashTree = [threadGroup];

        this.runData.forEach(item => {
          threadGroup.hashTree.push(item);
        })
        let reqObj = {id: this.reportId, testElement: testPlan};
        let bodyFiles = this.getBodyUploadFiles(reqObj);
        let url = "";
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
