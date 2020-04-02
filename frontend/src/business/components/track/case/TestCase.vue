<template>
  <div class="case_container" v-loading="loadingRequire.project && loadingRequire.testCase">
    <el-container>
      <el-aside width="250px">

        <el-menu :unique-opened="true" mode="horizontal" active-text-color="write"
          class="project_menu">
          <el-submenu index="1" popper-class="submenu">
            <template slot="title">
              {{currentProject == null ? '' : currentProject.name}}
            </template>
            <div style="height:400px;">
              <el-scrollbar style="height:100%">
                <label v-for="(item,index) in projects" :key="index">
                  <el-menu-item @click="changeProject(item)">
                    {{item.name}}
                    <i class="el-icon-check" v-if="item.id === currentProject.id"></i>
                  </el-menu-item>
                </label>
              </el-scrollbar>
          </div>
          </el-submenu>
        </el-menu>
        <node-tree class="node_tree"
                   @nodeSelectEvent="getCaseByNodeIds"
                   @refresh="getCaseByNodeIds"
                   ref="nodeTree"></node-tree>
      </el-aside>

      <el-main class="main-content">

        <test-case-list
          @openTestCaseEditDialog="openTestCaseEditDialog"
          @testCaseEdit="openTestCaseEditDialog"
          ref="testCaseList"></test-case-list>
      </el-main>


    </el-container>

    <test-case-edit
      @refresh="getCaseByNodeIds"
      ref="testCaseEditDialog"></test-case-edit>

  </div>
</template>

<script>

  import NodeTree from './components/NodeTree';
  import TestCaseEdit from './components/TestCaseEdit';
  import {WORKSPACE_ID,CURRENT_PROJECT} from '../../../../common/constants';
  import TestCaseList from "./components/TestCaseList";

  export default {
    name: "TestCase",
    components: {TestCaseList, NodeTree, TestCaseEdit},
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
        loadingRequire: {project: true, testCase: true},
        projects: [],
        initProjects: [],
        currentProject: null,
        treeNodes: []
      }
    },
    created: function () {
      this.projectId = this.$route.params.projectId;
      this.getProjects();
    },
    watch: {
      '$route'(to, from) {
        if (from.name.indexOf("Project") > 0){
          this.getProjects();
        }
      }
    },
    methods: {
      getProjects() {
          this.$get("/project/listAll", (response) => {
            if (response.success) {
              this.projects = response.data;
              this.initProjects = this.projects.slice(0, 4);
              if(localStorage.getItem(CURRENT_PROJECT)) {
                this.currentProject = JSON.parse(localStorage.getItem(CURRENT_PROJECT));
              } else {
                this.currentProject = this.projects[0];
                if(this.projects.length > 0){
                  localStorage.setItem(CURRENT_PROJECT, JSON.stringify(this.projects[0]));
                }
              }
            } else {
              this.$message()({
                type: 'warning',
                message: response.message
              });
            }
            this.loadingRequire.project = false;
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
        this.$refs.testCaseList.initTableData()
        this.$refs.nodeTree.getNodeTree();
      },
      getCaseByNodeIds(data) {
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
        if(node.children){
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
      }
    }
  }
</script>

<style scoped>
  .case_container {
    padding: 15px;
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
    margin: 10%;
  }

  .project_menu {
    margin-left: 20px;
    height: 50px;
  }

</style>
