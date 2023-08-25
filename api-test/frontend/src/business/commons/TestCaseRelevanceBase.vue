<template>
  <relevance-dialog :width="width" :title="dialogTitle" ref="relevanceDialog" :full-screen="isFullScreen">
    <!-- todo -->
    <template slot="headerBtn" v-if="$slots.headerBtn">
      <div>
        <slot name="headerBtn"></slot>
      </div>
    </template>
    <template slot="title" slot-scope="{ title }" v-if="!$slots.headerBtn">
      <ms-dialog-header
        :title="title"
        :enable-cancel="false"
        @confirm="save"
        btn-size="mini"
        :enable-full-screen="false"
        @fullScreen="isFullScreen = !isFullScreen">
        <template #other>
          <table-select-count-bar :count="selectCounts" style="float: left; margin: 5px" />

          <div v-if="flag" style="margin: 5px; float: left">
            <el-checkbox v-model="checked" class="el-checkbox__label"
              >{{ $t('test_track.sync_add_api_load') }}
            </el-checkbox>
          </div>
        </template>
      </ms-dialog-header>
    </template>

    <template v-slot:aside>
      <span v-if="isAcrossSpace" class="menu-title">{{
        '[' + $t('project.version.checkout') + $t('commons.space') + ']'
      }}</span>
      <el-select
        v-if="isAcrossSpace"
        filterable
        slot="prepend"
        v-model="workspaceId"
        @change="changeWorkspace"
        class="ms-header-workspace"
        size="small">
        <el-option v-for="(item, index) in workspaceList" :key="index" :label="item.name" :value="item.id" />
      </el-select>
      <select-menu
        :data="projects"
        v-if="multipleProject"
        width="155px"
        :current-data="currentProject"
        :title="$t('test_track.switch_project')"
        @dataChange="changeProject" />
      <slot name="aside"></slot>
    </template>

    <slot></slot>
  </relevance-dialog>
</template>

<script>
import { getUserWorkspace, projectRelated } from '@/api/project';
import MsDialogHeader from 'metersphere-frontend/src/components/MsDialogHeader';
import SelectMenu from '@/business/commons/SelectMenu';
import RelevanceDialog from './RelevanceDialog';
import { getCurrentProjectID, getCurrentUserId, getCurrentWorkspaceId } from 'metersphere-frontend/src/utils/token';
import TableSelectCountBar from '@/business/automation/scenario/api/TableSelectCountBar';

export default {
  name: 'TestCaseRelevanceBase',
  components: {
    TableSelectCountBar,
    RelevanceDialog,
    SelectMenu,
    MsDialogHeader,
  },
  data() {
    return {
      checked: true,
      result: false,
      currentProject: {},
      projectId: '',
      projectName: '',
      projects: [],
      workspaceId: '',
      workspaceList: [],
      currentWorkSpaceId: '',
      selectCounts: null,
      isFullScreen: false,
    };
  },
  props: {
    planId: {
      type: String,
    },
    dialogTitle: {
      type: String,
      default() {
        return this.$t('test_track.plan_view.relevance_test_case');
      },
    },
    flag: {
      type: Boolean,
    },
    width: String,
    isSaving: {
      type: Boolean,
      default() {
        return false;
      },
    },
    isAcrossSpace: {
      type: Boolean,
      default() {
        return false;
      },
    },
    multipleProject: {
      type: Boolean,
      default: true,
    },
  },
  methods: {
    refreshNode() {
      this.$emit('refresh');
    },

    save() {
      this.$emit('save', this.checked);
    },

    close() {
      this.$refs.relevanceDialog.close();
    },

    open() {
      this.getWorkSpaceList();
      this.workspaceId = getCurrentWorkspaceId();
      this.getProject();
      this.selectCounts = null;
      this.$refs.relevanceDialog.open();
    },

    getProject() {
      let realWorkSpaceId = this.isAcrossSpace ? this.workspaceId : this.currentWorkSpaceId;
      this.result = projectRelated({
        userId: getCurrentUserId(),
        workspaceId: realWorkSpaceId,
      }).then((res) => {
        let data = res.data;
        if (data && data.length > 0) {
          const index = data.findIndex((d) => d.id === getCurrentProjectID());
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
        } else {
          this.$message.warning(
            this.$t('commons.current_workspace') + this.$t('commons.not_exist') + this.$t('commons.project') + '!'
          );
        }
      });
    },

    changeProject(project) {
      if (project) {
        this.currentProject = project;
        this.$emit('setProject', project.id);
        // 获取项目时刷新该项目模块
        this.$emit('refreshNode');
      }
    },

    getWorkSpaceList() {
      getUserWorkspace().then((response) => {
        this.workspaceList = response.data;
      });
    },

    changeWorkspace() {
      this.getProject();
    },
  },
  created() {
    this.currentWorkSpaceId = getCurrentWorkspaceId();
    this.workspaceId = this.currentWorkSpaceId;
    if (this.isAcrossSpace) {
      this.getWorkSpaceList();
    }
  },
};
</script>

<style scoped>
.menu-title {
  color: darkgrey;
  margin-left: 10px;
  margin-right: 10px;
}

.ms-header-workspace {
  width: 155px;
  margin-bottom: 10px;
}
</style>
<style>
.el-select-dropdown__wrap {
  max-width: 500px;
}
</style>
