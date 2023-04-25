<template>
  <el-dialog :title="$t('custom_field.copy_issue_template')" :visible.sync="showDialog" :close-on-click-modal="false">
    <span style="font-size:13px; margin-left: 10px">{{$t('custom_field.copy_issue_template_tips1')}}</span>
    <span style="font-size:13px; margin-left: 10px; color:red">{{$t('custom_field.copy_issue_template_tips2')}}</span>
    <el-divider></el-divider>
    <span style="font-size:13px; margin-left: 10px">{{$t('custom_field.copy_issue_template_model_tips1')}}</span>
    <i style="font-size: 12px">{{$t('custom_field.copy_issue_template_model_tips2')}}</i>
    <el-checkbox-group v-model="copyModelVal" @change="checkModelChanged" style="margin-top: 15px">
      <el-checkbox false-label="0" true-label="0" :label="$t('custom_field.copy_issue_template_model_append')" style="margin-left: 10px"></el-checkbox>
      <el-checkbox false-label="1" true-label="1" :label="$t('custom_field.copy_issue_template_model_cover')" style="margin-left: 25px"></el-checkbox>
    </el-checkbox-group>
    <div class="copyTargetProjectTree" style="margin-top: 20px; margin-left: 10px">
      <span>{{$t('custom_field.target_project')}}</span>
      <el-tree
        class="copyProjectTree"
        ref="tree"
        :data="copyProjects"
        :props="defaultProps"
        :show-checkbox="true"
        :default-expand-all="true"
        :check-on-click-node="true"
        :expand-on-click-node="false"
        node-key="id">
          <span slot-scope="{data}">
            <span>{{ data.name }}</span>
            <span v-if="data.id === getCurrentProjectID()" style="font-size: 12px">{{$t("custom_field.current_project")}}</span>
            <span v-if="data.customPermissionFlag == null || !data.customPermissionFlag" style="font-size: 12px; color: red;">{{$t("custom_field.no_custom_fields_permission")}}</span>
          </span>
      </el-tree>
    </div>

    <div slot="footer" class="dialog-footer">
      <el-button @click="showDialog = false" size="small">{{ $t('test_track.cancel') }}</el-button>
      <el-button type="primary" @click="confirm" size="small">{{ $t('test_track.confirm') }}</el-button>
    </div>
  </el-dialog>
</template>

<script>

import {getUUID} from "metersphere-frontend/src/utils";
import {getCurrentUserId, getCurrentWorkspaceId, getCurrentProjectID} from "metersphere-frontend/src/utils/token"
import {copyIssueTemplate, getIssueTemplateCopyProject} from "@/api/template";

export default {
  name: "IssueTemplateCopy",
  data() {
    return {
      root: null,
      showDialog: false,
      copyData: null,
      copyProjects: [],
      copyModelVal: "0",
      defaultProps: {
        children: 'children',
        label: 'name',
        disabled: (data, node) => {
          return data.customPermissionFlag ? !data.customPermissionFlag : true;
        }
      }
    };
  },
  computed: {
  },
  methods: {
    open(copyData) {
      this.root = null;
      this.copyProjects = [];
      this.copyModelVal = "0";
      this.showDialog = true;
      this.copyData = copyData;
      this.initCopyProjects();
    },
    checkModelChanged(val) {
      if (val !== "null") {
        this.copyModelVal = this.copyModelVal !== val ? val : this.copyModelVal;
      }
    },
    initCopyProjects() {
      getIssueTemplateCopyProject(getCurrentUserId(), getCurrentWorkspaceId())
        .then((response) => {
          this.root = {id: getUUID(), name: response.data.workspaceName, customPermissionFlag: true, children: response.data.projectDTOS};
          this.copyProjects.push(this.root);
        })
    },
    confirm() {
      let checkProjectIds = this.$refs.tree.getCheckedKeys();
      if (checkProjectIds.find(item => item === this.root.id)) {
        checkProjectIds.splice(checkProjectIds.findIndex(item => item === this.root.id), 1)
      }
      let param = {id: this.copyData.id, copyModel: this.copyModelVal, targetProjectIds: checkProjectIds}
      copyIssueTemplate(param)
        .then((response) => {
          this.showDialog = false;
          this.$emit('refresh');
          this.$message({
            type: 'success',
            message: this.$t('commons.copy_success'),
          });
        })
    },
    getCurrentProjectID() {
      return getCurrentProjectID();
    }
  }
};
</script>

<style scoped>
:deep(.el-dialog__body) {
  padding: 10px 20px!important;
}

.copyProjectTree {
  margin-top: 10px;
  height: 200px;
  overflow: auto;
  display: flex
}

:deep(.el-tree-node.is-checked > .el-tree-node__content) {
  color: #783887;
}
</style>
