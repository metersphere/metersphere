<template>
  <div class="container">
    <div class="main-content">
      <el-container>
        <el-aside width="250px">

          <select-menu
            :data="projects"
            :current-data="currentProject"
            :title="$t('test_track.project')"
            @dataChange="changeProject">
          </select-menu>

          <node-tree class="node-tree"
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
        ref="testCaseEditDialog">
      </test-case-edit>

    </div>
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
            let lastProject = JSON.parse(localStorage.getItem(CURRENT_PROJECT));
            if (lastProject) {
              let hasCurrentProject   = false;
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
          this.$alert(this.$t('test_track.no_project'), {
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
          this.setCurrentProject(response.data);
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
        // this.refresh();
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
      }

    }
  }
</script>

<style scoped>

  .main-content {
    width: 100%;
    height: 100%;
    background: white;
    height: 1000px;
    box-sizing: border-box;
  }

  .node-tree {
    margin: 5%;
  }

  .container {
    padding: 0px;
  }

</style>
