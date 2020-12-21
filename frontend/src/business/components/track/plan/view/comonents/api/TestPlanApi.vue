<template>

  <ms-test-plan-common-component>
    <template v-slot:aside>
      <ms-api-module
        @nodeSelectEvent="nodeChange"
        @protocolChange="handleProtocolChange"
        @refreshTable="refresh"
        @setModuleOptions="setModuleOptions"
        :is-read-only="true"
        :type="'edit'"
        ref="nodeTree"/>
    </template>

    <template v-slot:main>
      <!--测试用例列表-->
      <test-plan-api-case-list
        :current-protocol="currentProtocol"
        :currentRow="currentRow"
        :select-node-ids="selectNodeIds"
        :trash-enable="trashEnable"
        :is-case-relevance="true"
        :model="'plan'"
        :plan-id="planId"
        @relevanceCase="openTestCaseRelevanceDialog"
        ref="apiCaseList"/>
    </template>

    <test-case-api-relevance
      @refresh="refresh"
      :plan-id="planId"
      ref="apiCaseRelevance"/>

  </ms-test-plan-common-component>

</template>

<script>
    import NodeTree from "../../../../common/NodeTree";
    import TestCaseRelevance from "../functional/TestCaseFunctionalRelevance";
    import MsTestPlanCommonComponent from "../base/TestPlanCommonComponent";
    import TestPlanApiCaseList from "./TestPlanApiCaseList";
    import TestCaseApiRelevance from "./TestCaseApiRelevance";
    import ApiCaseSimpleList from "../../../../../api/definition/components/list/ApiCaseSimpleList";
    import MsApiModule from "../../../../../api/definition/components/module/ApiModule";

    export default {
      name: "TestPlanApi",
      components: {
        MsApiModule,
        ApiCaseSimpleList,
        TestCaseApiRelevance,
        TestPlanApiCaseList,
        MsTestPlanCommonComponent,
        TestCaseRelevance,
        NodeTree,
      },
      data() {
        return {
          result: {},
          selectParentNodes: [],
          treeNodes: [],
          currentRow: "",
          trashEnable: false,
          currentProtocol: null,
          currentModule: null,
          selectNodeIds: [],
          moduleOptions: {},

        }
      },
      props: [
        'planId'
      ],
      mounted() {
        // this.initData();
        // this.openTestCaseEdit(this.$route.path);
      },
      watch: {
        '$route'(to, from) {
          // this.openTestCaseEdit(to.path);
        },
        planId() {
          // this.initData();
        }
      },
      methods: {
        setProject(projectId) {
          this.projectId = projectId;
        },
        // isApiListEnableChange(data) {
        //   this.isApiListEnable = data;
        // },

        refresh(data) {
          this.$refs.nodeTree.list();
          this.$refs.apiCaseList.initTable();
        },

        nodeChange(node, nodeIds, pNodes) {
          this.selectNodeIds = nodeIds;
        },
        handleProtocolChange(protocol) {
          this.currentProtocol = protocol;
        },
        setModuleOptions(data) {
          this.moduleOptions = data;
        },

        saveCaseRelevance() {
          let url = '';
          let selectIds = [];
          // if (this.isApiListEnable) {
          //   url = '/api/definition/relevance';
          //   selectIds = Array.from(this.$refs.apiList.selectRows).map(row => row.id);
          // } else {
          //   url = '/api/testcase/relevance';
          //   selectIds = Array.from(this.$refs.apiCaseList.selectRows).map(row => row.id);
          // }

          let param = {};
          param.planId = this.planId;
          param.selectIds = selectIds;
          // param.request = this.condition;
          // 选择全选则全部加入到评审，无论是否加载完全部
          // if (this.testCases.length === param.testCaseIds.length) {
          //   param.testCaseIds = ['all'];
          // }

          this.result = this.$post(url, param, () => {
            this.$success(this.$t('commons.save_success'));
            this.refresh();
            this.$refs.baseRelevance.close();
          });
        },
      //   refresh() {
      //     this.selectNodeIds = [];
      //     this.selectParentNodes = [];
      //     this.$refs.testCaseRelevance.search();
      //     this.getNodeTreeByPlanId();
      //   },
      //   initData() {
      //     this.getNodeTreeByPlanId();
      //   },
        openTestCaseRelevanceDialog() {
          this.$refs.apiCaseRelevance.open();
        },
      //   nodeChange(nodeIds, pNodes) {
      //     this.selectNodeIds = nodeIds;
      //     this.selectParentNodes = pNodes;
      //     // 切换node后，重置分页数
      //     this.$refs.testPlanTestCaseList.currentPage = 1;
      //     this.$refs.testPlanTestCaseList.pageSize = 10;
      //   },
      //   getNodeTreeByPlanId() {
      //     if (this.planId) {
      //       this.result = this.$get("/case/node/list/plan/" + this.planId, response => {
      //         this.treeNodes = response.data;
      //       });
      //     }
      //   },
      //   openTestCaseEdit(path) {
      //     if (path.indexOf("/plan/view/edit") >= 0) {
      //       let caseId = this.$route.params.caseId;
      //       this.$get('/test/plan/case/get/' + caseId, response => {
      //         let testCase = response.data;
      //         if (testCase) {
      //           this.$refs.testPlanTestCaseList.handleEdit(testCase);
      //           this.$router.push('/track/plan/view/' + testCase.planId);
      //         }
      //       });
      //     }
      //   },
      }
    }

</script>

<style scoped>

</style>
