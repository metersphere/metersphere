<template>
  <relevance-dialog :title="dialogTitle"  ref="relevanceDialog">

    <template v-slot:aside>
      <select-menu
        :data="projects"
        width="160px"
        :current-data="currentProject"
        :title="$t('test_track.switch_project')"
        @dataChange="changeProject"/>
      <slot name="aside"></slot>
    </template>

    <slot></slot>

    <template v-slot:footer>

    <div v-if="$slots.footer">
        <slot name="footer"></slot>
      </div>
      <div v-else>
        <div style="margin-bottom: 15px" v-if="flag">
          <el-checkbox v-model="checked">{{ $t('test_track.sync_add_api_load') }}</el-checkbox>
        </div>
        <ms-dialog-footer @cancel="close" @confirm="save"/>
      </div>
    </template>

  </relevance-dialog>
</template>

<script>

  import MsDialogFooter from '../../../../../common/components/MsDialogFooter'
  import SelectMenu from "../../../../common/SelectMenu";
  import RelevanceDialog from "./RelevanceDialog";
  import {getCurrentProjectID, getCurrentUserId, getCurrentWorkspaceId} from "@/common/js/utils";

  export default {
    name: "TestCaseRelevanceBase",
    components: {
      RelevanceDialog,
      SelectMenu,
      MsDialogFooter,
    },
    data() {
      return {
        checked:true,
        result: {},
        currentProject: {},
        projectId: '',
        projectName: '',
        projects: [],

      };
    },
    props: {
      planId: {
        type: String
      },
      dialogTitle: {
        type: String,
        default() {
          return this.$t('test_track.plan_view.relevance_test_case');
        }
      },
      flag:{
        type:Boolean,
      }
    },
    watch: {

    },
    methods: {

      refreshNode() {
        this.$emit('refresh');
      },

      save() {
        this.$emit('save',this.checked);
      },

      close() {
        this.$refs.relevanceDialog.close();
      },

      open() {
        this.getProject();
        this.$refs.relevanceDialog.open();
      },

      getProject() {
        this.result = this.$post("/project/list/related", {userId: getCurrentUserId(), workspaceId: getCurrentWorkspaceId()}, res => {
          let data = res.data;
          if (data) {
            const index = data.findIndex(d => d.id === getCurrentProjectID());
            this.projects = data;
            if (index !== -1) {
              this.projectId = data[index].id;
              this.projectName = data[index].name;
              this.changeProject(data[index]);
            } else {
              this.projectId = data[0].id;
              this.projectName = data[0].name;
              this.changeProject(data[0]);
            }
          }
        })
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
</style>
