<template>
  <ms-container>

    <ms-aside-container>
      <select-menu
        :data="projects"
        :current-data="currentProject"
        :title="$t('test_track.project')"
        @dataChange="changeProject"/>
      <node-tree
        class="node-tree"
        v-loading="result.loading"
        @nodeSelectEvent="nodeChange"
        @refresh="refresh"
        :tree-nodes="treeNodes"
        :type="'edit'"
        :draggable="nodeTreeDraggable"
        :select-node.sync="selectNode"
        @refreshTable="refreshTable"
        :current-project="currentProject"
        ref="nodeTree"/>
    </ms-aside-container>

    <ms-main-container>
      <test-case-list
        :current-project="currentProject"
        :select-node-ids="selectNodeIds"
        :select-parent-nodes="selectParentNodes"
        @testCaseEdit="editTestCase"
        @testCaseCopy="copyTestCase"
        @testCaseDetail="showTestCaseDetail"
        @batchMove="batchMove"
        @refresh="refresh"
        @moveToNode="moveToNode"
        ref="testCaseList">
      </test-case-list>
    </ms-main-container>

    <test-case-edit
      @refresh="refreshTable"
      :read-only="testCaseReadOnly"
      :tree-nodes="treeNodes"
      :select-node="selectNode"
      :current-project="currentProject"
      ref="testCaseEditDialog">
    </test-case-edit>

    <test-case-move @refresh="refresh" ref="testCaseMove"/>

    <batch-move @refresh="refresh" ref="testBatchMove"/>

  </ms-container>

</template>

<script>

  import NodeTree from '../common/NodeTree';
  import TestCaseEdit from './components/TestCaseEdit';
  import {CURRENT_PROJECT, ROLE_TEST_MANAGER, ROLE_TEST_USER} from '../../../../common/js/constants';
  import TestCaseList from "./components/TestCaseList";
  import SelectMenu from "../common/SelectMenu";
  import TestCaseMove from "./components/TestCaseMove";
  import MsContainer from "../../common/components/MsContainer";
  import MsAsideContainer from "../../common/components/MsAsideContainer";
  import MsMainContainer from "../../common/components/MsMainContainer";
  import {checkoutTestManagerOrTestUser, hasRoles} from "../../../../common/js/utils";
  import BatchMove from "./components/BatchMove";

  export default {
    name: "TestCase",
    components: {
      MsMainContainer,
      MsAsideContainer, MsContainer, TestCaseMove, TestCaseList, NodeTree, TestCaseEdit, SelectMenu, BatchMove},
    comments: {},
    data() {
      return {
        result: {},
        currentPage: 1,
        pageSize: 5,
        total: 0,
        projects: [],
        currentProject: null,
        treeNodes: [],
        selectNodeIds: [],
        selectParentNodes: [],
        testCaseReadOnly: true,
        selectNode: {},
        nodeTreeDraggable: true,
      }
    },
    mounted() {
      this.init(this.$route);
    },
    watch: {
      '$route'(to, from) {
        this.init(to);
      },
      currentProject() {
        this.refresh();
      }
    },
    methods: {
      init(route) {
        let path = route.path;
        if (path.indexOf("/track/case/edit") >= 0 || path.indexOf("/track/case/create") >= 0){
          this.getProjects();
          this.testCaseReadOnly = false;
          if (!checkoutTestManagerOrTestUser()) {
            this.testCaseReadOnly = true;
          }
          let caseId = this.$route.params.caseId;
          this.openRecentTestCaseEditDialog(caseId);
          this.$router.push('/track/case/all');
        } else if (route.params.projectId){
          this.getProjects();
          this.getProjectById(route.params.projectId);
        }
      },
      getProjects() {
          this.$get("/project/listAll", (response) => {
            this.projects = response.data;
            let lastProject = JSON.parse(localStorage.getItem(CURRENT_PROJECT));
            if (lastProject) {
              let hasCurrentProject = false;
              for (let i = 0; i < this.projects.length; i++) {
                if (this.projects[i].id == lastProject.id) {
                  this.currentProject = lastProject;
                  hasCurrentProject = true;
                  break;
                }
              }
              if (!hasCurrentProject) {
                this.setCurrentProject(this.projects[0]);
              }
            } else {
              if(this.projects.length > 0){
                this.setCurrentProject(this.projects[0]);
              }
            }
            // this.checkProject();
          });
      },
      checkProject() {
        if(this.currentProject === null) {
          this.$alert(this.$t('test_track.case.no_project'), {
            confirmButtonText: this.$t('project.create'),
            callback: action => {
              this.$router.push("/track/project/create");
            }
          });
        }
      },
      changeProject(project) {
        this.setCurrentProject(project);
      },
      nodeChange(nodeIds, pNodes) {
        this.selectNodeIds = nodeIds;
        this.selectParentNodes = pNodes;
      },
      refreshTable() {
        this.$refs.testCaseList.initTableData();
      },
      editTestCase(testCase) {
        this.testCaseReadOnly = false;
        if (this.treeNodes.length < 1) {
          this.$warning(this.$t('test_track.case.create_module_first'));
          return;
        }
        this.$refs.testCaseEditDialog.open(testCase);
      },
      copyTestCase(testCase) {
        this.testCaseReadOnly = false;
        let item = {};
        Object.assign(item, testCase);
        item.name = '';
        item.isCopy = true;
        this.$refs.testCaseEditDialog.open(item);
      },
      showTestCaseDetail(testCase) {
        this.testCaseReadOnly = true;
        this.$refs.testCaseEditDialog.open(testCase);
      },
      getProjectByCaseId(caseId) {
        return this.$get('/test/case/project/' + caseId, async response => {
          this.setCurrentProject(response.data);
        });
      },
      refresh() {
        this.selectNodeIds = [];
        this.selectParentNodes = [];
        this.selectNode = {};
        this.$refs.testCaseList.initTableData();
        this.getNodeTree();
      },
      openRecentTestCaseEditDialog(caseId) {
        if (caseId) {
          this.getProjectByCaseId(caseId);
          this.$get('/test/case/get/' + caseId, response => {
            if (response.data) {
              this.$refs.testCaseEditDialog.open(response.data);
            }
          });
        } else {
          this.$refs.testCaseEditDialog.open();
        }
      },
      getProjectById(id) {
        if (id && id != 'all') {
          this.$get('/project/get/' + id, response => {
            let project = response.data;
            this.setCurrentProject(project);
            // this.$router.push('/track/case/all');
          });
        }
        if (id === 'all') {
          this.refresh();
        }
      },
      setCurrentProject(project) {
        if (project) {
          this.currentProject = project;
          localStorage.setItem(CURRENT_PROJECT, JSON.stringify(project));
        }
        this.refresh();
      },
      getNodeTree() {
        if (!hasRoles(ROLE_TEST_USER, ROLE_TEST_MANAGER)) {
          this.nodeTreeDraggable = false;
        }
        if (this.currentProject) {
          this.result = this.$get("/case/node/list/" + this.currentProject.id, response => {
            this.treeNodes = response.data;
          });
        }
      },
      moveToNode(selectIds) {
        if (selectIds.size < 1) {
          this.$warning(this.$t('test_track.plan_view.select_manipulate'));
          return;
        }
        this.$refs.testCaseEditDialog.getModuleOptions();
        this.$refs.testCaseMove.open(this.$refs.testCaseEditDialog.moduleOptions, selectIds);
      },
      batchMove(selectIds) {
        this.$refs.testBatchMove.open(this.treeNodes, selectIds,this.$refs.testCaseEditDialog.moduleOptions);
      }
    }
  }
</script>

<style scoped>

  .el-main {
    padding: 15px;
  }

</style>
