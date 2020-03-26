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
                   @nodeSelectEvent="getCaseByNodeIds"></node-tree>
      </el-aside>


      <el-main>
        <el-card>
          <div slot="header">
            <el-row type="flex" justify="space-between" align="middle">
              <span class="title">{{$t('commons.test')}}</span>
              <span class="search">
            <el-input type="text" size="small" :placeholder="$t('load_test.search_by_name')"
                      prefix-icon="el-icon-search"
                      maxlength="60"
                      v-model="condition" @change="search" clearable/>
          </span>
            </el-row>
          </div>
          <el-table :data="tableData" class="test-content">
            <el-table-column
              prop="name"
              :label="$t('commons.name')"
              width="150"
              show-overflow-tooltip>
            </el-table-column>
            <el-table-column
              prop="description"
              :label="$t('commons.description')"
              show-overflow-tooltip>
            </el-table-column>
            <el-table-column
              prop="projectName"
              :label="$t('load_test.project_name')"
              width="150"
              show-overflow-tooltip>
            </el-table-column>
            <el-table-column
              width="250"
              :label="$t('commons.create_time')">
              <template slot-scope="scope">
                <span>{{ scope.row.createTime | timestampFormatDate }}</span>
              </template>
            </el-table-column>
            <el-table-column
              width="250"
              :label="$t('commons.update_time')">
              <template slot-scope="scope">
                <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
              </template>
            </el-table-column>
            <el-table-column
              width="150"
              :label="$t('commons.operating')">
              <template slot-scope="scope">
                <el-button @click="handleEdit(scope.row)" type="primary" icon="el-icon-edit" size="mini" circle/>
                <el-button @click="handleDelete(scope.row)" type="danger" icon="el-icon-delete" size="mini" circle/>
              </template>
            </el-table-column>
          </el-table>
          <div>
            <el-row>
              <el-col :span="22" :offset="1">
                <div class="table-page">
                  <el-pagination
                    @size-change="handleSizeChange"
                    @current-change="handleCurrentChange"
                    :current-page.sync="currentPage"
                    :page-sizes="[5, 10, 20, 50, 100]"
                    :page-size="pageSize"
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="total">
                  </el-pagination>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-main>
    </el-container>

  </div>
</template>

<script>

  import NodeTree from './components/NodeTree';

  export default {
    name: "TestCase",
    components: {NodeTree},
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
      }
    },
    watch: {
      '$route'(to) {
        this.projectId = to.params.projectId;
        this.initTableData();
      }
    },
    created: function () {
      this.projectId = this.$route.params.projectId;
      this.initTableData();
      this.getProjects();
    },
    methods: {
      initTableData() {
        let param = {
          name: this.condition,
        };

        if (this.projectId !== 'all') {
          param.projectId = this.projectId;
        }

        this.result = this.$post(this.buildPagePath(this.queryPath), param, response => {
          this.loadingRequire.testCase = false;
          let data = response.data;
          this.total = data.itemCount;
          this.tableData = data.listObject;
        });
      },
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
      search() {
        this.initTableData();
      },
      buildPagePath(path) {
        return path + "/" + this.currentPage + "/" + this.pageSize;
      },
      handleSizeChange(size) {
        this.pageSize = size;
        this.initTableData();
      },
      handleCurrentChange(current) {
        this.currentPage = current;
        this.initTableData();
      },
      handleSelectionChange(val) {
        this.multipleSelection = val;
      },
      handleEdit(testPlan) {
        this.$router.push({
          path: '/performance/plan/edit/' + testPlan.id,
        })
      },
      handleDelete(testPlan) {
        this.$alert(this.$t('load_test.delete_confirm') + testPlan.name + "？", '', {
          confirmButtonText: this.$t('commons.confirm'),
          callback: (action) => {
            if (action === 'confirm') {
              this._handleDelete(testPlan);
            }
          }
        });
      },
      _handleDelete(testPlan) {
        let data = {
          id: testPlan.id
        };

        this.result = this.$post(this.deletePath, data, () => {
          this.$message({
            message: this.$t('commons.delete_success'),
            type: 'success'
          });
          this.initTableData();
        });
      },
      checkProject() {
        if(this.currentProject === null) {
          this.$alert('该工作空间下无项目，请先创建项目', '创建项目', {
            confirmButtonText: '去创建项目',
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

        console.log(data);

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
    /*box-shadow: 0 2px 4px rgba(0, 0, 0, .12), 0 0 6px rgba(0, 0, 0, .04);*/
    margin: 10%;
  }

  .project_menu {
    /*border-style:none;*/
    margin-left: 20px;
    height: 50px;
  }
</style>
