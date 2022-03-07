<template>
  <relevance-dialog :width="width" :title="dialogTitle" ref="relevanceDialog">
    <template slot="title" slot-scope="{title}">
      <ms-dialog-header :title="title" @cancel="close" @confirm="save">
        <template #other>
          <div v-if="flag" style="margin-top: 5px;">
            <el-checkbox v-model="checked" class="el-checkbox__label">{{ $t('test_track.sync_add_api_load') }}</el-checkbox>
          </div>
        </template>
      </ms-dialog-header>
    </template>

    <template v-slot:aside>
      <el-select v-if="isAcrossSpace" filterable slot="prepend" v-model="workspaceId" @change="changeWorkspace"
                 style="width: 160px"
                 size="small">
        <el-option v-for="(item,index) in workspaceList" :key="index" :label="item.name" :value="item.id"/>
      </el-select>
      <select-menu
        :data="projects"
        v-if="multipleProject"
        width="160px"
        :current-data="currentProject"
        :title="$t('test_track.switch_project')"
        @dataChange="changeProject"/>
      <slot name="aside"></slot>
    </template>

    <slot></slot>

    <!--        <template v-slot:footer>

              <div v-if="$slots.footer">
                <slot name="footer"></slot>
              </div>
              <div v-else>
                <div style="margin-bottom: 15px" v-if="flag">
                  <el-checkbox v-model="checked">{{ $t('test_track.sync_add_api_load') }}</el-checkbox>
                </div>
                <ms-dialog-footer @cancel="close" v-loading="isSaving" @confirm="save"/>
              </div>
            </template>-->

  </relevance-dialog>
</template>

<script>

import MsDialogHeader from '../../../../../common/components/MsDialogHeader'
import SelectMenu from "../../../../common/SelectMenu";
import RelevanceDialog from "./RelevanceDialog";
import {getCurrentProjectID, getCurrentUserId, getCurrentWorkspaceId} from "@/common/js/utils";

export default {
  name: "TestCaseRelevanceBase",
  components: {
    RelevanceDialog,
    SelectMenu,
    MsDialogHeader,
  },
  data() {
    return {
      checked: true,
      result: {},
      currentProject: {},
      projectId: '',
      projectName: '',
      projects: [],
      workspaceId: '',
      workspaceList: [],
      currentWorkSpaceId: ''
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
    flag: {
      type: Boolean,
    },
    width: String,
    isSaving: {
      type: Boolean,
      default() {
        return false;
      }
    },
    isAcrossSpace: {
      type: Boolean,
      default() {
        return false;
      }
    },
    multipleProject: {
      type: Boolean,
      default: true
    }
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
      this.getProject();
      this.$refs.relevanceDialog.open();
    },

    getProject() {
      let realWorkSpaceId = this.isAcrossSpace ? this.workspaceId : this.currentWorkSpaceId;
      this.result = this.$post("/project/list/related", {
        userId: getCurrentUserId(),
        workspaceId: realWorkSpaceId
      }, res => {
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
    },

    getWorkSpaceList() {
      this.$get("/workspace/list/userworkspace/" + encodeURIComponent(getCurrentUserId()), response => {
        this.workspaceList = response.data;
      });
    },

    changeWorkspace() {
      this.getProject();
    }
  },
  created() {
    this.currentWorkSpaceId = getCurrentWorkspaceId();
    this.workspaceId = this.currentWorkSpaceId;
    if (this.isAcrossSpace) {
      this.getWorkSpaceList();
    }
  }
}
</script>

<style scoped>

/*.el-checkbox__label {*/
/*  float: right;*/
/*}*/
</style>
