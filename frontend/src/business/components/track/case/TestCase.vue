<template>
    <div class="main-content">
        <el-container class="case-container">
          <el-aside class="tree-aside">
            <select-menu
              :data="projects"
              :current-data="currentProject"
              :title="$t('test_track.project')"
              @dataChange="changeProject">
            </select-menu>
            <node-tree class="node-tree"
                       v-loading="result.loading"
                       @nodeSelectEvent="nodeChange"
                       @refresh="refresh"
                       :tree-nodes="treeNodes"
                       :type="'edit'"
                       ref="nodeTree"/>
          </el-aside>

          <el-main class="test-case-list">
            <test-case-list
              :current-project="currentProject"
              :selectNodeIds="selectNodeIds"
              :selectNodeNames="selectNodeNames"
              @openTestCaseEditDialog="openTestCaseEditDialog"
              @testCaseEdit="openTestCaseEditDialog"
              @refresh="refresh"
              ref="testCaseList">
            </test-case-list>
          </el-main>
        </el-container>

        <test-case-edit
          @refresh="refresh"
          :tree-nodes="treeNodes"
          ref="testCaseEditDialog">
        </test-case-edit>
    </div>
</template>

<script>

  import NodeTree from '../common/NodeTree';
  import TestCaseEdit from './components/TestCaseEdit';
  import {CURRENT_PROJECT} from '../../../../common/js/constants';
  import TestCaseList from "./components/TestCaseList";
  import SelectMenu from "../common/SelectMenu";

  export default {
    name: "TestCase",
    components: {TestCaseList, NodeTree, TestCaseEdit, SelectMenu},
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
        selectNodeNames: []
      }
    },
    mounted() {
      this.getProjects();
      this.refresh();
      if (this.$route.params.projectId){
        this.getProjectById(this.$route.params.projectId)
      }
      if (this.$route.path.indexOf("/track/case/edit") >= 0){
        this.openRecentTestCaseEditDialog();
        this.$router.push('/track/case/all');
      }
    },
    watch: {
      '$route'(to, from) {
        let path = to.path;
        if (to.params.projectId){
          this.getProjectById(to.params.projectId)
          this.getProjects();
        }
        if (path.indexOf("/track/case/edit") >= 0){
          this.openRecentTestCaseEditDialog();
          this.$router.push('/track/case/all');
          this.getProjects();
        }
      },
      currentProject() {
        this.refresh();
      }
    },
    methods: {
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
                this.currentProject = null;
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
      nodeChange(nodeIds, nodeNames) {
        this.selectNodeIds = nodeIds;
        this.selectNodeNames = nodeNames;
      },
      refreshTable() {
        this.$refs.testCaseList.initTableData();
      },
      openTestCaseEditDialog(testCase) {
        this.$refs.testCaseEditDialog.open(testCase);
      },
      getProjectByCaseId(caseId) {
        return this.$get('/test/case/project/' + caseId, async response => {
          this.setCurrentProject(response.data);
        });
      },
      refresh() {
        this.selectNodeIds = [];
        this.selectNodeNames = [];
        this.$refs.testCaseList.initTableData();
        this.getNodeTree();
      },
      openRecentTestCaseEditDialog() {
        let caseId = this.$route.params.caseId;
        this.getProjectByCaseId(caseId);
        this.$get('/test/case/get/' + caseId, response => {
          if (response.data) {
            this.openTestCaseEditDialog(response.data);
          }
        });
      },
      getProjectById(id) {
        if (id && id != 'all') {
          this.$get('/project/get/' + id, response => {
            let project = response.data;
            this.setCurrentProject(project);
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
        if (this.currentProject) {
          this.result = this.$get("/case/node/list/" + this.currentProject.id, response => {
            this.treeNodes = response.data;
          });
        }
      }
    }
  }
</script>

<style scoped>

  .node-tree {
    margin: 3%;
  }

  .tree-aside {
    position: relative;
    border: 1px solid #EBEEF5;
    box-sizing: border-box;
    background: white;
  }

  .case-container {
    height: calc(100vh - 150px);
    min-height: 600px;
    margin-top: 0;
    margin-left: 0;
  }

  .test-case-list {
    padding: 15px;
  }

</style>
