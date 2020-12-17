<template>
  <div>
    <el-dialog :title="$t('test_track.plan_view.relevance_test_case')"
               :visible.sync="dialogVisible"
               @close="close"
               width="60%" v-loading="result.loading"
               :close-on-click-modal="false"
               top="50px">

      <el-container class="main-content">
        <el-aside class="tree-aside" width="250px">
          <select-menu
            :data="projects"
            width="160px"
            :current-data="currentProject"
            :title="$t('test_track.switch_project')"
            @dataChange="changeProject"/>
          <slot name="aside"></slot>
        </el-aside>

        <el-container>
          <el-main class="case-content">
            <slot></slot>
          </el-main>
        </el-container>
      </el-container>

      <template v-slot:footer>
        <ms-dialog-footer @cancel="dialogVisible = false" @confirm="save"/>
      </template>

    </el-dialog>

  </div>

</template>

<script>

  import MsDialogFooter from '../../../../../common/components/MsDialogFooter'
  import SelectMenu from "../../../../common/SelectMenu";

  export default {
    name: "TestCaseRelevanceBase",
    components: {
      SelectMenu,
      MsDialogFooter,
    },
    data() {
      return {
        result: {},
        dialogVisible: false,
        currentProject: {},
        projectId: '',
        projectName: '',
        projects: [],

      };
    },
    props: {
      planId: {
        type: String
      }
    },
    watch: {

    },
    methods: {
      open() {
        this.getProject();
        this.dialogVisible = true;
      },

      refreshNode() {
        this.$emit('refresh');
      },

      save() {
        this.$emit('save');
      },

      close() {
        this.dialogVisible = false;
      },

      getProject() {
        if (this.planId) {
          this.result = this.$post("/test/plan/project/", {planId: this.planId}, res => {
            let data = res.data;
            if (data) {
              this.projects = data;
              this.projectId = data[0].id;
              this.projectName = data[0].name;
              this.changeProject(data[0]);
            }
          })
        }
      },

      changeProject(project) {
        this.currentProject = project;
        this.$emit('setProject', project.id);
        // 获取项目时刷新该项目模块
        this.$emit('refreshNode');
      }
    }
  }
</script>

<style scoped>

  .tb-edit .el-input {
    display: none;
    color: black;
  }

  .tb-edit .current-row .el-input {
    display: block;

  }

  .tb-edit .current-row .el-input + span {
    display: none;

  }

  .node-tree {
    margin-right: 10px;
  }

  .el-header {
    background-color: darkgrey;
    color: #333;
    line-height: 60px;
  }

  .case-content {
    padding: 0px 20px;
    height: 100%;
    /*border: 1px solid #EBEEF5;*/
  }

  .tree-aside {
    min-height: 300px;
    max-height: 100%;
  }

  .main-content {
    min-height: 300px;
    height: 100%;
    /*border: 1px solid #EBEEF5;*/
  }

  .project-link {
    float: right;
    margin-right: 12px;
    margin-bottom: 10px;
  }

</style>
