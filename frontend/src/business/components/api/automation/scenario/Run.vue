<template>
  <div></div>
</template>
<script>
  import {getUUID} from "@/common/js/utils";
  import ThreadGroup from "../../definition/components/jmeter/components/thread-group";
  import TestPlan from "../../definition/components/jmeter/components/test-plan";

  export default {
    name: 'MsRun',
    components: {},
    props: {
      environment: String,
      debug: Boolean,
      reportId: String,
      runData: Array,
    },
    data() {
      return {
        result: {},
        loading: false,
        runId: "",
        reqNumber: 0,
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
              if (this.reqNumber < 60) {
                this.reqNumber++;
                setTimeout(this.getResult, 2000);
              } else {
                this.$error("获取报告超时");
                this.$emit('runRefresh', {});
              }
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
                    if (!item.id) {
                      let fileId = getUUID().substring(0, 12);
                      item.name = item.file.name;
                      item.id = fileId;
                    }
                    obj.bodyUploadIds.push(item.id);
                    bodyUploadFiles.push(item.file);
                  }
                });
              }
            });
            request.body.binary.forEach(param => {
              if (param.files) {
                param.files.forEach(item => {
                  if (item.file) {
                    if (!item.id) {
                      let fileId = getUUID().substring(0, 12);
                      item.name = item.file.name;
                      item.id = fileId;
                    }
                    obj.bodyUploadIds.push(item.id);
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
        this.runData.forEach(item => {
          let threadGroup = new ThreadGroup();
          threadGroup.hashTree = [];
          threadGroup.name = item.name;
          threadGroup.hashTree.push(item);
          testPlan.hashTree.push(threadGroup);
        })
        console.log("====",testPlan)
        let reqObj = {id: this.reportId, reportId: this.reportId, environmentId: this.environment, testElement: testPlan};
        let bodyFiles = this.getBodyUploadFiles(reqObj);
        let url = "/api/automation/run";
        this.$fileUpload(url, null, bodyFiles, reqObj, response => {
          this.runId = response.data;
          this.$emit('runRefresh', {});
        }, erro => {
        });
      }
    }
  }
</script>
