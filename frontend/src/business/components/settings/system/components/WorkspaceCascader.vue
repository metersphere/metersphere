<template>
  <el-dialog class="user-cascade" :title="title" :visible.sync="dialogVisible"
             @close="close" v-loading="loading">
    <div class="block">
      <el-select v-model="selectedUserGroup" clearable size="medium" style="width: 260px;"
                 placeholder="请选择">
        <el-option
          v-for="item in workspaceUserGroups"
          :key="item.id"
          :label="item.name"
          :value="item.id">
        </el-option>
      </el-select>
      <el-form :model="ruleForm" :rules="rules" ref="ruleForm" style="margin-top: 5px;">
        <el-form-item prop="workspace" label-width="0px">
          <el-cascader-panel :props="props"
                             popper-class="ms-cascade"
                             v-model="selectedIds"
                             ref="cascadeSelector"
                             :key="isResourceShow"
                             clearable>
          </el-cascader-panel>
        </el-form-item>
      </el-form>
    </div>
    <span slot="footer" class="dialog-footer">
        <ms-dialog-footer
          @cancel="close()"
          @confirm="confirm()"/>
      </span>
  </el-dialog>
</template>

<script>
import ElUploadList from "element-ui/packages/upload/src/upload-list";
import MsTableButton from '../../../../components/common/components/MsTableButton';
import {listenGoBack, removeGoBackListener} from "@/common/js/utils";
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";
import {GROUP_WORKSPACE} from "@/common/js/constants";

export default {
  name: "WorkspaceCascader",
  components: {ElUploadList, MsTableButton, MsDialogFooter},
  data() {
    let validateSelect = (rule, value, callback) => {
      let checkNodes = this.$refs.cascadeSelector.getCheckedNodes(true);
      if (checkNodes.length === 0) {
        callback(new Error(this.$t('user.select_workspace').toString()));
      }
      callback();
    };
    const self = this
    return {
      ruleForm: {
        workspace: '',
      },
      rules: {
        workspace: [
          {validator: validateSelect, message: this.$t('user.select_workspace'), trigger: 'change'}
        ],
      },
      selectedIds: [],
      selectedUserGroup: "",
      workspaceUserGroups: [],
      isResourceShow: 0,
      props: {
        multiple: true,
        lazy: true,
        value: 'id',
        label: 'name',
        lazyLoad(node, resolve) {
          const {level} = node;
          if (level === 0) {
            self.getWorkspace(resolve);
          } else {
            resolve([]);
          }
        }
      },
      dialogVisible: false,
      loading: false,
    };
  },
  props: {
    title: {
      type: String,
      default: ''
    }
  },
  created() {
    this.getWorkspaceUserGroup();
  },
  methods: {
    close() {
      removeGoBackListener(this.close);
      this.loading = false;
      this.dialogVisible = false;
      this.selectedIds = [];
      ++this.isResourceShow;
      this.$refs['ruleForm'].resetFields();
    },
    open() {
      listenGoBack(this.close);
      this.dialogVisible = true;
      this.selectedUserGroup = "";
      this.rules.workspace[0].message = this.$t('user.select_workspace');
    },
    confirm() {
      if (!this.selectedUserGroup) {
        this.$warning(this.$t('user.select_group'));
        return;
      }
      this.$refs.ruleForm.validate((valid) => {
        if (valid) {
          let checkNodes = this.$refs.cascadeSelector.getCheckedNodes(true);
          let selectValueArr = [];
          for (let i = 0; i < checkNodes.length; i++) {
            selectValueArr.push(checkNodes[i].value);
          }
          this.$emit('confirm', 'ADD_WORKSPACE', selectValueArr, this.selectedUserGroup);
          this.loading = true;
        } else {
          return false;
        }
      });
    },
    getWorkspace(resolve) {
      this.$get("workspace/list/", res => {
        let data = res.data ? res.data : [];
        data.forEach(d => d.leaf = true);
        resolve(data);
      })
    },
    getWorkspaceUserGroup() {
      this.$post("/user/group/get", {type: GROUP_WORKSPACE}, (res) => {
        this.workspaceUserGroups = res.data ? res.data : [];
      })
    }
  }
}
</script>

<style>
</style>

<style scoped>
.user-cascade >>> .el-dialog {
  width: 600px;
}

.user-cascade >>> .el-dialog__body {
  padding: 5px 20px;
}

/deep/ .el-form-item__content {
  margin-left: 0;
}

/deep/ .el-cascader-menu__wrap {
  height: 300px;
  width: 560px;
}
</style>
