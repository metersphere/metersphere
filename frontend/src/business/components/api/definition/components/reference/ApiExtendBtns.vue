<template>
  <el-dropdown @command="handleCommand" class="scenario-ext-btn">
    <el-link type="primary" :underline="false">
      <el-icon class="el-icon-more"></el-icon>
    </el-link>
    <el-dropdown-menu slot="dropdown">
      <el-dropdown-item command="ref">{{ $t('api_test.automation.view_ref') }}</el-dropdown-item>
      <el-dropdown-item command="create_performance" v-permission="['PROJECT_API_DEFINITION:READ+CREATE_PERFORMANCE']">{{ $t('api_test.create_performance_test') }}</el-dropdown-item>
    </el-dropdown-menu>
    <ms-reference-view ref="viewRef"/>
  </el-dropdown>
</template>

<script>
  import MsReferenceView from "./ReferenceView";
  import MsTestPlanList from "../../../automation/scenario/testplan/TestPlanList";
  import {getBodyUploadFiles, getCurrentProjectID, getUUID, strMapToObj} from "@/common/js/utils";
  import TestPlan from "@/business/components/api/definition/components/jmeter/components/test-plan";
  import ThreadGroup from "@/business/components/api/definition/components/jmeter/components/thread-group";
  import {TYPE_TO_C} from "@/business/components/api/automation/scenario/Setting";

  export default {
    name: "MsApiExtendBtns",
    components: {MsReferenceView, MsTestPlanList},
    props: {
      row: Object,
      isCaseEdit: Boolean,
      environment: {},
    },
    data() {
      return {
        planVisible: false,
      }
    },
    methods: {
      handleCommand(cmd) {
        if (this.row.id) {
          switch (cmd) {
            case  "ref":
              this.$refs.viewRef.open(this.row);
              break;
            case "create_performance":
              this.createPerformance(this.row);
              break;
          }
        } else {
          this.$warning(this.$t('api_test.automation.save_case_info'))
        }
      },
      sortHashTree(stepArray) {
        if (stepArray) {
          for (let i in stepArray) {
            if (!stepArray[i].clazzName) {
              stepArray[i].clazzName = TYPE_TO_C.get(stepArray[i].type);
            }
            if (stepArray[i].type === "Assertions" && !stepArray[i].document) {
              stepArray[i].document = {type: "JSON", data: {xmlFollowAPI: false, jsonFollowAPI: false, json: [], xml: []}};
            }
            if (stepArray[i] && stepArray[i].authManager && !stepArray[i].authManager.clazzName) {
              stepArray[i].authManager.clazzName = TYPE_TO_C.get(stepArray[i].authManager.type);
            }
            if (stepArray[i].hashTree && stepArray[i].hashTree.length > 0) {
              this.sortHashTree(stepArray[i].hashTree);
            }
          }
        }
      },
      createPerformance(row) {
        /**
         * 思路：调用后台创建性能测试的方法，把当前案例的hashTree在后台转化为jmx并文件创建性能测试。
         * 然后跳转到修改性能测试的页面
         *
         * 性能测试保存地址： performance/save
         *
         */
        if (!this.environment || !this.environment) {
          this.$warning(this.$t('api_test.environment.select_environment'));
          return;
        }
        let projectId = getCurrentProjectID();
        this.runData = [];
        this.singleLoading = true;
        this.row.request.name = this.row.id;
        this.row.request.useEnvironment = this.environment;
        this.runData.push(this.row.request);
        /*触发执行操作*/
        let testPlan = new TestPlan();
        testPlan.clazzName = TYPE_TO_C.get(testPlan.type);
        let threadGroup = new ThreadGroup();
        threadGroup.clazzName = TYPE_TO_C.get(threadGroup.type);
        threadGroup.hashTree = [];
        testPlan.hashTree = [threadGroup];
        this.runData.forEach(item => {
          item.projectId = projectId;
          if (!item.clazzName) {
            item.clazzName = TYPE_TO_C.get(item.type);
          }
          threadGroup.hashTree.push(item);
        });
        this.sortHashTree(testPlan.hashTree);
        let reqObj = {
          id: row.id,
          testElement: testPlan,
          type: this.type,
          name: row.name,
          clazzName: this.clazzName ? this.clazzName : TYPE_TO_C.get(this.type),
          projectId: getCurrentProjectID(),
          environmentMap: new Map([
            [projectId, this.environment.id]
          ]),
        };

        let bodyFiles = getBodyUploadFiles(reqObj, this.runData);
        reqObj.reportId = "run";
        let url = "/api/genPerformanceTestXml";
        this.$fileUpload(url, null, bodyFiles, reqObj, response => {
          let jmxObj = {};
          jmxObj.name = response.data.name;
          jmxObj.xml = response.data.xml;
          jmxObj.attachFiles = response.data.attachFiles;
          jmxObj.attachByteFiles = response.data.attachByteFiles;
          jmxObj.caseId = reqObj.id;
          jmxObj.version = row.version;
          jmxObj.envId = this.environment;
          this.$store.commit('setTest', {
            name: row.name,
            jmx: jmxObj
          })
          this.$router.push({
            path: "/performance/test/create"
          })
        }, erro => {
          this.$emit('runRefresh', {});
        });
      }
    }
  }
</script>

<style scoped>
  .scenario-ext-btn {
    margin-left: 10px;
  }
</style>
