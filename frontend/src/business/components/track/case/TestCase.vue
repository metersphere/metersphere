<template>
  <div class="case_container">
    <el-container>
      <el-aside width="250px">

        <select-menu
          :data="projects"
          :current-data="currentProject"
          :title="$t('test_track.project')"
          @dataChange="changeProject">
        </select-menu>

        <node-tree class="node_tree"
                   :current-project="currentProject"
                   @nodeSelectEvent="refreshTable"
                   @refresh="refreshTable"
                   ref="nodeTree">
        </node-tree>
      </el-aside>

      <el-main class="main-content">

        <test-case-list
          :current-project="currentProject"
          @openTestCaseEditDialog="openTestCaseEditDialog"
          @testCaseEdit="openTestCaseEditDialog"
          ref="testCaseList">
        </test-case-list>
      </el-main>

    </el-container>

    <test-case-edit
      @refresh="refreshTable"
      ref="testCaseEditDialog"></test-case-edit>

  </div>
</template>

<script>

  import NodeTree from './components/NodeTree';
  import TestCaseEdit from './components/TestCaseEdit';
  import {CURRENT_PROJECT, WORKSPACE_ID} from '../../../../common/js/constants';
  import TestCaseList from "./components/TestCaseList";
  import SelectMenu from "../common/SelectMenu";

  export default {
    name: "TestCase",
    components: {TestCaseList, NodeTree, TestCaseEdit, SelectMenu},
    comments: {},
    data() {
      return {
        result: {},
        projectId: null,
        tableData: [],
        multipleSelection: [],
        currentPage: 1,
        pageSize: 5,
        total: 0,
        projects: [],
        currentProject: null,
        treeNodes: []
      }
    },
    created() {
      this.getProjects();
    },
    mounted() {
      if (this.$route.path.indexOf("/track/case/edit") >= 0){
        this.openRecentTestCaseEditDialog();
      }
    },
    watch: {
      '$route'(to, from) {
        let path = to.path;
        if (path.indexOf("/track/case/all") >= 0){
          this.refresh();
        }
        if (path.indexOf("/track/case/edit") >= 0){
          this.openRecentTestCaseEditDialog();
          this.$router.push('/track/case/all');
        }
      }
    },
    methods: {
      getProjects() {
          this.$get("/project/listAll", (response) => {
            this.projects = response.data;
            if (localStorage.getItem(CURRENT_PROJECT)) {
              let lastProject = JSON.parse(localStorage.getItem(CURRENT_PROJECT));
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
              if(this.projects.length > 0){
                this.currentProject = this.projects[0];
                localStorage.setItem(CURRENT_PROJECT, JSON.stringify(this.projects[0]));
              }
            } else {
              if(this.projects.length > 0){
                this.currentProject = this.projects[0];
                localStorage.setItem(CURRENT_PROJECT, JSON.stringify(this.projects[0]));
              }
            }
            // this.checkProject();
          });
      },
      checkProject() {
        if(this.currentProject === null) {
          this.$alert(this.$t('test_track.no_project'), {
            confirmButtonText: this.$t('project.create'),
            callback: action => {
              this.$router.push("/track/project/create");
            }
          });
        }
      },
      changeProject(project) {
        this.currentProject = project;
        localStorage.setItem(CURRENT_PROJECT, JSON.stringify(project));
      },
      refreshTable(data) {
        this.$refs.testCaseList.initTableData(data);
      },
      openTestCaseEditDialog(data) {
        this.setNodePathOption(this.$refs.nodeTree.treeNodes);
        this.setMaintainerOptions();
        this.$refs.testCaseEditDialog.openTestCaseEditDialog(data);
      },
      setNodePathOption(nodes) {
        let moduleOptions = [];
        nodes.forEach(node => {
          this.buildNodePath(node, {path: ''}, moduleOptions);
        });
        this.$refs.testCaseEditDialog.moduleOptions = moduleOptions;
      },
      buildNodePath(node, option, moduleOptions) {
        //递归构建节点路径
        option.id = node.id;
        option.path = option.path + '/' + node.name;
        moduleOptions.push(option);
        if (node.children) {
          for (let i = 0; i < node.children.length; i++){
            this.buildNodePath(node.children[i], { path: option.path }, moduleOptions);
          }
        }
      },
      setMaintainerOptions() {
        let workspaceId = localStorage.getItem(WORKSPACE_ID);
        this.$post('/user/ws/member/list/all', {workspaceId:workspaceId}, response => {
          this.$refs.testCaseEditDialog.maintainerOptions = response.data;
        });
      },
      getProjectByCaseId(caseId) {
        return this.$get('/test/case/project/' + caseId, async response => {
          localStorage.setItem(CURRENT_PROJECT, JSON.stringify(response.data));
          this.refresh();
        });
      },
      refresh() {
        this.$refs.testCaseList.initTableData();
        this.$refs.nodeTree.getNodeTree();
        this.getProjects();
      },
      openRecentTestCaseEditDialog() {
        let caseId = this.$route.params.caseId;
        this.getProjectByCaseId(caseId);
        this.refresh();
        this.$get('/test/case/get/' + caseId, response => {
          this.openTestCaseEditDialog(response.data[0]);
        });
      }
    }
  }
</script>

<style scoped>
  .case_container {
    width: 100%;
    height: 100%;
    background: white;
    height: 1000px;
    box-sizing: border-box;
  }

  .main-content {
    margin: 0 auto;
    width: 100%;
    max-width: 1200px;
  }

  .node_tree {
    margin: 5%;
  }

  .project_menu {
    margin-left: 20px;
    height: 50px;
  }

</style>
