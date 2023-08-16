<template>

  <ms-test-plan-common-component>
    <template v-slot:aside>
      <node-tree class="node-tree"
                 v-loading="loading"
                 @nodeSelectEvent="nodeChange"
                 local-suffix="test_case"
                 default-label="未规划用例"
                 :tree-nodes="treeNodes"
                 ref="nodeTree"/>
    </template>
    <template v-slot:main>
      <ms-tab-button
        :active-dom="activeDom"
        @update:activeDom="updateActiveDom"
        :left-tip="$t('test_track.case.list')"
        :left-content="$t('test_track.case.list')"
        :right-tip="$t('test_track.case.minder')"
        :right-content="$t('test_track.case.minder')"
        :middle-button-enable="false">

        <functional-test-case-list
          class="table-list"
          v-if="activeDom === 'left'"
          :plan-id="planId"
          :plan-status="planStatus "
          :clickType="clickType"
          :select-node-ids="selectNodeIds"
          :search-select-node-ids="searchSelectNodeIds"
          :search-select.sync="searchSelect"
          :version-enable="versionEnable"
          @refresh="refresh"
          @refreshTree="refreshTree"
          @setCondition="setCondition"
          @search="refreshTreeByCaseFilter"
          @openTestCaseRelevanceDialog="openTestCaseRelevanceDialog"
          ref="testPlanTestCaseList"/>
        <test-plan-minder
          :tree-nodes="treeNodes"
          :project-id="projectId"
          :condition="condition"
          :plan-id="planId"
          :plan-status="planStatus "
          v-if="activeDom === 'right'"
          ref="minder"
        />
      </ms-tab-button>
    </template>

    <test-plan-functional-relevance
      @refresh="refresh"
      :plan-id="planId"
      :version-enable="versionEnable"
      ref="testCaseRelevance"/>

    <is-change-confirm
      @confirm="changeConfirm"
      ref="isChangeConfirm"/>
  </ms-test-plan-common-component>

</template>

<script>
import NodeTree from "metersphere-frontend/src/components/module/MsNodeTree";
import MsTestPlanCommonComponent from "../base/TestPlanCommonComponent";
import FunctionalTestCaseList from "./FunctionalTestCaseList";
import MsTabButton from "metersphere-frontend/src/components/MsTabButton";
import TestPlanMinder from "@/business/common/minder/TestPlanMinder";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import TestPlanFunctionalRelevance from "@/business/plan/view/comonents/functional/TestPlanFunctionalRelevance";
import IsChangeConfirm from "metersphere-frontend/src/components/IsChangeConfirm";
import {openMinderConfirm, saveMinderConfirm} from "@/business/common/minder/minderUtils";
import {getTestPlanCaseNodesByCaseFilter} from "@/api/testCase";
import {useStore} from "@/store";
import {testPlanTestCaseGet} from "@/api/remote/plan/test-plan-test-case";

export default {
  name: "TestPlanFunctional",
  components: {
    IsChangeConfirm,
    TestPlanFunctionalRelevance,
    TestPlanMinder,
    MsTabButton,
    FunctionalTestCaseList,
    MsTestPlanCommonComponent,
    NodeTree,
  },
  data() {
    return {
      loading: false,
      selectNodeIds: [],
      treeNodes: [],
      activeDom: 'left',
      selectNode: {},
      condition: {},
      tmpActiveDom: null,
      tmpPath: null,
      currentNode: null,
      searchSelectNodeIds: [],
      searchSelect: false,
    };
  },
  props: [
    'planId',
    'redirectCharType',
    'clickType',
    'versionEnable',
    'planStatus'
  ],
  mounted() {
    this.initData();
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
  },
  activated() {
    this.clearSelectNode();
    this.initData();
    this.openTestCaseEdit(this.$route.path);
    if (this.condition.name) {
      this.condition.name = undefined;
    }
  },
  watch: {
    '$route'(to, from) {
      this.openTestCaseEdit(to.path);
    },
    planId() {
      if (this.condition.name) {
        this.condition.name = undefined;
      }
      this.clearSelectNode();
      this.getNodeTreeByPlanId(this.condition);
    }
  },
  methods: {
    refresh() {
      this.clearSelectNode();
      this.$refs.testCaseRelevance.search();
      this.refreshTreeByCaseFilter();
    },
    refreshTree() {
      this.getNodeTreeByPlanId();
    },
    clearSelectNode() {
      this.selectNodeIds = [];
      this.searchSelectNodeIds = [];
      useStore().testPlanViewSelectNode = {};
    },
    initData() {
      this.getNodeTreeByPlanId();
    },
    openTestCaseRelevanceDialog() {
      this.$refs.testCaseRelevance.open();
    },
    refreshTreeByCaseFilter() {
      this.getNodeTreeByPlanId(this.condition);
    },
    nodeChange(node, nodeIds, pNodes) {
      this.selectNodeIds = nodeIds;
      useStore().testPlanViewSelectNode = node;
      this.currentNode = node;
      this.searchSelect = false;
      // 切换node后，重置分页数
      if (this.$refs.testPlanTestCaseList) {
        this.$refs.testPlanTestCaseList.currentPage = 1;
        this.$refs.testPlanTestCaseList.pageSize = 10;
      }
    },
    getNodeTreeByPlanId(condition) {
      if (this.planId) {
        if (this.clickType) {
          this.loading = true;
          this.$get('/' + this.clickType)
            .then(response => {
              this.loading = false;
              this.treeNodes = response.data;
              this.setCurrentKey();
            });
        } else {
          this.loading = true;
          getTestPlanCaseNodesByCaseFilter(this.planId, condition)
            .then((r) => {
              this.loading = false;
              this.treeNodes = r.data;
              this.setCurrentKey();
              this.$refs.testPlanTestCaseList.$nextTick(() => {
                this.setSearchSelectNodeIds(r)
              });
            });
        }
      }
    },
    setSearchSelectNodeIds(treeNodes) {
      this.searchSelectNodeIds = []
      this.getChildNodeId(treeNodes.data[0], this.searchSelectNodeIds);
      this.searchSelect = true;
    },
    getChildNodeId(rootNode, nodeIds) {
      if (rootNode && this.currentNode) {
        if (rootNode.id === this.currentNode.data.id) {
          this.pushNode(rootNode, nodeIds);
        } else {
          if (rootNode.children) {
            for (let i = 0; i < rootNode.children.length; i++) {
              if (rootNode.children[i].id === this.currentNode.data.id) {
                this.pushNode(rootNode.children[i], nodeIds)
              }
            }
          }
        }
      }
    },
    pushNode(rootNode, nodeIds) {
      //递归获取所有子节点ID
      nodeIds.push(rootNode.id);
      if (rootNode.children) {
        for (let i = 0; i < rootNode.children.length; i++) {
          this.pushNode(rootNode.children[i], nodeIds);
        }
      }
    },
    setCurrentKey() {
      if (this.$refs.nodeTree) {
        this.$refs.nodeTree.setCurrentKey(this.currentNode);
      }
    },
    setCondition(data) {
      this.condition = data;
    },
    openTestCaseEdit(path) {
      if (path.indexOf("/plan/view/edit") >= 0) {
        let caseId = this.$route.params.caseId;
        testPlanTestCaseGet(caseId)
          .then(response => {
            let testCase = response.data;
            if (testCase) {
              this.$refs.testPlanTestCaseList.handleEdit(testCase);
              this.$router.push('/track/plan/view/' + testCase.planId);
            }
          });
      }
    },
    updateActiveDom(activeDom) {
      openMinderConfirm(this, activeDom, 'PROJECT_TRACK_PLAN:READ+RUN');
    },
    changeConfirm(isSave) {
      saveMinderConfirm(this, isSave);
    },
    handleBeforeRouteLeave(to) {
      if (useStore().isTestCaseMinderChanged) {
        if (this.planStatus !== 'Archived') {
          this.$refs.isChangeConfirm.open();
          this.tmpPath = to.path;
          return false;
        } else {
          return true;
        }
      } else {
        return true;
      }
    }
  }
};

</script>

<style scoped>
</style>
