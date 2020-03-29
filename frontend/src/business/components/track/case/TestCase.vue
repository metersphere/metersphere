<template>
  <div class="case_container" v-loading="loadingRequire.project && loadingRequire.testCase">
    <el-container>
      <el-aside width="250px">

        <el-menu :unique-opened="true" mode="horizontal" active-text-color="write"
          class="project_menu">
          <el-submenu index="1" popper-class="submenu" v-permission="['test_user', 'test_viewer']">
            <template slot="title">
              {{currentProject.name}}
            </template>
            <el-scrollbar style="height:500px">
                <label v-for="(item,index) in projects" :key="index">
                  <el-menu-item @click="changeProject(item)">
                    {{item.name}}
                    <i class="el-icon-check" v-if="item.id === currentProject.id"></i>
                  </el-menu-item>
                </label>
            </el-scrollbar>
          </el-submenu>
        </el-menu>
        <node-tree class="node_tree" :project-id="currentProject.id"
                   @nodeSelectEvent="getCaseByNodeIds"
                   ref="nodeTree"></node-tree>
      </el-aside>

      <test-case-list
        @opentestCaseEditDialog="opentestCaseEditDialog"
        @testCaseEdit="opentestCaseEditDialog"
        ref="testCaseList"></test-case-list>

    </el-container>

    <test-case-edit
      :project-id="currentProject.id"
      @refresh="getCaseByNodeIds"
      ref="testCaseEditDialog"></test-case-edit>

  </div>
</template>

<script>

  import NodeTree from './components/NodeTree';
  import TestCaseEdit from './components/TestCaseEdit';
  import {WORKSPACE_ID} from '../../../../common/constants';
  import TestCaseList from "./components/TestCaseList";

  export default {
    name: "TestCase",
    components: {TestCaseList, NodeTree, TestCaseEdit},
    comments: {},
    data() {
      return {
        result: {},
        queryPath: "/testplan/list",
        deletePath: "/testplan/delete",
        condition: "",
        projectId: null,
        tableData: [],
        multipleSelection: [],
        currentPage: 1,
        pageSize: 5,
        total: 0,
        loadingRequire: {project: true, testCase: true},
        testId: null,
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
    methods: {

      getProjects() {
          this.$get("/project/listAll", (response) => {
            if (response.success) {
              this.projects = response.data;
              this.initProjects = this.projects.slice(0, 4);
              this.currentProject = response.data[0];
            } else {
              this.$message()({
                type: 'warning',
                message: response.message
              });
            }
            this.loadingRequire.project = false;

            this.checkProject();

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
      },
      getCaseByNodeIds(data) {
        this.$refs.testCaseList.initTableData(data);
      },
      opentestCaseEditDialog(data) {
        this.setNodePathOption(this.$refs.nodeTree.treeNodes);
        this.setMaintainerOptions();
        this.$refs.testCaseEditDialog.opentestCaseEditDialog(data);
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
            this.buildNodePath(node.children[i], { path: '/' + node.children[i].name }, moduleOptions);
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
  .testplan-container {
    padding: 15px;
    width: 100%;
    height: 100%;
    box-sizing: border-box;
  }

  .main-content {
    margin: 0 auto;
    width: 100%;
    max-width: 1200px;
  }

  .test-content {
    width: 100%;
  }

  .table-page {
    padding-top: 20px;
    margin-right: -9px;
    float: right;
  }

  .case_container {
    background: white;
  }

  .node_tree {
    margin: 10%;
  }

  .project_menu {
    /*border-style:none;*/
    margin-left: 20px;
    height: 50px;
  }

  .case_container {
    height: 600px;
  }

</style>
