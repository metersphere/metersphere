<template>
  <span></span>
</template>
<script>
import {getBodyUploadFiles, getCurrentProjectID, strMapToObj} from "@/common/js/utils";
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
      type: String,
      envMap: Map
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
            url = "/api/definition/report/get/" + this.runId;
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
      run() {
        let projectId = getCurrentProjectID();
        // 如果envMap不存在，是单接口调用
        if (!this.envMap || this.envMap.size === 0) {
          projectId = getCurrentProjectID();
        } else {
          // 场景步骤下接口调用
          if (this.runData.projectId) {
            projectId = this.runData.projectId;
          }
        }

        let testPlan = new TestPlan();
        let threadGroup = new ThreadGroup();
        threadGroup.hashTree = [];
        testPlan.hashTree = [threadGroup];
        this.runData.forEach(item => {
          item.projectId = projectId;
          threadGroup.hashTree.push(item);
        })
        let reqObj = {id: this.reportId, testElement: testPlan, type: this.type, projectId: projectId, environmentMap: strMapToObj(this.envMap)};
        let bodyFiles = getBodyUploadFiles(reqObj, this.runData);
        if (this.runData[0].url) {
          reqObj.name = this.runData[0].url;
        } else {
          reqObj.name = this.runData[0].path;
        }
        let url = "";
        if (this.debug) {
          reqObj.reportId = this.reportId;
          url = "/api/definition/run/debug";
        } else {
          url = "/api/definition/run";
        }
        this.$fileUpload(url, null, bodyFiles, reqObj, response => {
          this.runId = response.data;
          this.getResult();
          this.$emit('autoCheckStatus');  //   执行结束后，自动更新计划状态
        }, error => {
          this.$emit('errorRefresh', {});
        });
      }
    }
  }
</script>
