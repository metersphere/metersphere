<template>
  <div>
    <el-drawer
      :close-on-click-modal="false"
      :visible.sync="visible"
      :size="widthCacl"
      @close="close"
      destroy-on-close
      :full-screen="isFullScreen"
      ref="relevanceDialog"
      custom-class="file-drawer"
      append-to-body
    >
      <template slot="title">
        <div style="color: #1f2329; font-size: 16px; font-weight: 500">
          {{ dialogTitle }}
        </div>
      </template>
      <!-- todo -->
      <template slot="headerBtn" v-if="$slots.headerBtn">
        <div>
          <slot name="headerBtn"></slot>
        </div>
      </template>
      <!-- <template slot="title" slot-scope="{ title }" v-if="!$slots.headerBtn">
        <ms-dialog-header
          :title="title"
          :enable-cancel="false"
          @confirm="save"
          btn-size="mini"
          @fullScreen="isFullScreen = !isFullScreen"
        >
          <template #other>
            <table-select-count-bar
              :count="selectCounts"
              style="float: left; margin: 5px"
            />

            <div v-if="flag" style="margin: 5px; float: left">
              <el-checkbox v-model="checked" class="el-checkbox__label">{{
                $t("test_track.sync_add_api_load")
              }}</el-checkbox>
            </div>
          </template>
        </ms-dialog-header>
      </template> -->

      <div class="content-box">
        <div class="body-wrap">
          <ms-aside-container
            :min-width="'350'"
            :max-width="'800'"
            :enable-aside-hidden="false"
            :default-hidden-bottom-top="200"
            :enable-auto-height="true"
          >
            <div class="aside-wrap">
              <span v-if="isAcrossSpace" class="menu-title">{{ $t("commons.space") }}:</span>
              <el-select
                v-if="isAcrossSpace"
                filterable
                slot="prepend"
                v-model="workspaceId"
                @change="changeWorkspace"
                class="ms-header-workspace"
                size="small"
              >
                <el-option
                  v-for="(item, index) in workspaceList"
                  :key="index"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
              <select-menu
                :data="projects"
                v-if="multipleProject"
                :current-data="currentProject"
                :title="$t('case.project') + ':'"
                @dataChange="changeProject"
              />
              <slot name="aside"> </slot>
            </div>
          </ms-aside-container>

          <div class="content-wrap">
            <slot></slot>
          </div>
        </div>
        <div class="footer-wrap">
          <slot name="options">
            <div class="options">
              <div class="options-btn">
                <div class="check-row" v-if="selectCounts > 0">
                  <div class="label">{{$t('case.selected')}} {{ selectCounts }} {{$t('case.strip')}}</div>
                  <div class="clear" @click="clearSelect">{{$t('case.clear')}}</div>
                </div>
                <div class="cancel">
                  <el-button size="small" @click="visible = false">{{
                    $t("commons.cancel")
                  }}</el-button>
                </div>
                <div class="submit">
                  <el-button
                    size="small"
                    v-prevent-re-click
                    :type="selectCounts > 0 ? 'primary' : 'info'"
                    @click="save"
                    @keydown.enter.native.prevent
                  >
                    {{ $t("commons.confirm") }}
                  </el-button>
                </div>
              </div>
            </div>
          </slot>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import MsAsideContainer from "metersphere-frontend/src/components/MsAsideContainer";
import MsDialogHeader from "metersphere-frontend/src/components/MsDialogHeader";
import SelectMenu from "./SelectMenu";
import {
  getCurrentProjectID,
  getCurrentUserId,
  getCurrentWorkspaceId,
} from "metersphere-frontend/src/utils/token";
import { getUserWorkspaceList } from "metersphere-frontend/src/api/workspace";
import { getUserProjectList } from "metersphere-frontend/src/api/project";
import TableSelectCountBar from "metersphere-frontend/src/components/table/MsTableSelectCountBar";

export default {
  name: "CaseRelevanceSideDialog",
  components: {
    SelectMenu,
    MsDialogHeader,
    TableSelectCountBar,
    MsAsideContainer
  },
  data() {
    return {
      checked: true,
      currentProject: {},
      projectId: "",
      projectName: "",
      projects: [],
      workspaceId: "",
      workspaceList: [],
      currentWorkSpaceId: "",
      selectCounts: null,
      isFullScreen: false,
      visible: false,
    };
  },
  props: {
    planId: {
      type: String,
    },
    dialogTitle: {
      type: String,
      default() {
        return this.$t("test_track.plan_view.relevance_test_case");
      },
    },
    flag: {
      type: Boolean,
    },
    width: {
      type: Number,
      default: 1200,
    },
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
  computed: {
    widthCacl() {
      if (!isNaN(this.width)) {
        //计算rem
        let remW = (this.width / 1440) * 100;
        let standW = (1200 / 1440) * 100;
        return remW > standW ? remW : standW + "%";
      }
      return this.width;
    },
  },
  methods: {
    clearSelect() {
      this.$emit("clearSelect");
    },

    refreshNode() {
      this.$emit("refresh");
    },

    save() {
      if (!this.selectCounts) {
        return;
      }
      this.$emit("save", this.checked);
    },

    close() {
      this.visible = false;
    },

    open() {
      this.workspaceId = getCurrentWorkspaceId();
      this.getProject();
      this.selectCounts = null;
      this.visible = true;
    },

    getProject() {
      let realWorkSpaceId = this.isAcrossSpace
        ? this.workspaceId
        : this.currentWorkSpaceId;
      getUserProjectList({
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
            this.$t("commons.current_workspace") +
              this.$t("commons.not_exist") +
              this.$t("commons.project") +
              "!"
          );
        }
      });
    },
    changeProject(project) {
      if (project) {
        this.currentProject = project;
        this.$emit("setProject", project.id);
        // 获取项目时刷新该项目模块
        this.$emit("refreshNode");
      }
    },
    getWorkSpaceList() {
      getUserWorkspaceList().then((r) => {
        this.workspaceList = r.data;
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
  margin-right: 10px;
  margin-bottom: 17px;
}

.ms-header-workspace {
  width: 155px;
  margin-bottom: 10px;
}
</style>
<style scoped lang="scss">
@import "@/business/style/index.scss";
.content-box {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.body-wrap {
  display: flex;
  /* height: px2rem(763); */
  /* min-height: px2rem(763); */
  flex: 9;
  .aside-wrap {
    padding: px2rem(24) px2rem(24) 0 px2rem(24);
  }
  .content-wrap {
    width: px2rem(930);
    overflow-y: scroll;
  }
}
.footer-wrap {
  flex: 1;
  width: 100%;
  height: px2rem(80);
  background: #ffffff;
  box-shadow: 0px -1px 4px rgba(31, 35, 41, 0.1);
}

.footer-wrap .options {
  height: 80px;
  background: #ffffff;
  box-shadow: 0px -1px 4px rgba(31, 35, 41, 0.1);
  overflow: hidden;
}
.footer-wrap .options-btn {
  display: flex;
  margin-top: 24px;
  height: 32px;
  margin-right: 24px;
  float: right;
}
.footer-wrap .options-btn .submit {
  margin-left: 12px;
}
.footer-wrap .check-row {
  display: flex;
  line-height: 32px;
}
.check-row .label {
  font-family: "PingFang SC";
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  text-align: center;
  color: #646a73;
}
.check-row .clear {
  font-family: "PingFang SC";
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  text-align: center;
  color: #783887;
  cursor: pointer;
  margin-left: 16px;
  margin-right: 16px;
}
</style>
