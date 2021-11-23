<template>
  <el-dialog class="user-cascade" :title="title" :visible.sync="dialogVisible"
             @close="close" v-loading="loading">
    <div class="block">
      <el-select v-model="selectedUserGroup" clearable size="medium" style="width: 260px;"
                 placeholder="请选择">
        <el-option
          v-for="item in projectUserGroups"
          :key="item.id"
          :label="item.name"
          :value="item.id">
        </el-option>
      </el-select>
      <el-alert
        :title="$t('user.add_project_batch_tip')"
        type="info"
        show-icon
        :closable="false"
        style="margin-bottom: 5px;margin-top: 5px;"
      >
      </el-alert>
      <el-form :model="ruleForm" :rules="rules" ref="ruleForm">
        <el-form-item prop="project" label-width="0px">
          <el-cascader-panel :props="props"
                             popper-class="ms-cascade"
                             :style="{'--cascaderMenuWidth': cascaderMenuWidth + 'px'}"
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
import {getCurrentProjectID, getCurrentWorkspaceId, listenGoBack, removeGoBackListener} from "@/common/js/utils";
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";
import {GROUP_PROJECT} from "@/common/js/constants";

export default {
  name: "BatchToProjectGroupCascader",
  components: {ElUploadList, MsTableButton, MsDialogFooter},
  data() {
    let validateSelect = (rule, value, callback) => {
      let checkNodes = this.$refs.cascadeSelector.getCheckedNodes(true);
      if (checkNodes.length === 0) {
        callback(new Error(this.$t('user.select_project').toString()));
      }
      callback();
    };
    const self = this
    return {
      ruleForm: {
        project: '',
      },
      rules: {
        project: [
          {validator: validateSelect, message: this.$t('user.select_project'), trigger: 'change'}
        ],
      },
      selectedIds: [],
      selectedUserGroup: "",
      projectUserGroups: [],
      isResourceShow: 0,
      props: {
        multiple: true,
        lazy: true,
        value: 'id',
        label: 'name',
        lazyLoad(node, resolve) {
          const {level, value} = node;
          if (self.cascaderLevel === 1) {
            if (level === 0) {
              self.getProject(getCurrentWorkspaceId(), resolve);
            } else {
              resolve([]);
            }
          } else if (self.cascaderLevel === 2) {
            // 先加载工作空间
            if (level === 0) {
              self.getWorkspace(resolve);
            } else if (level === 1) {
              self.getProject(value, resolve);
            } else {
              resolve([]);
            }
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
    },
    // 几层 项目/工作空间-项目
    cascaderLevel: {
      type: Number,
      default: 2
    },
  },
  created() {
    this.getProjectUserGroup();
  },
  computed: {
    cascaderMenuWidth() {
      return this.cascaderLevel === 1 ? '560' : '280';
    }
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
      this.rules.project[0].message = this.$t('user.select_project');
    },
    confirm() {
      this.$refs.ruleForm.validate((valid) => {
        if (valid) {
          let checkNodes = this.$refs.cascadeSelector.getCheckedNodes(true);
          let selectValueArr = [];
          for (let i = 0; i < checkNodes.length; i++) {
            selectValueArr.push(checkNodes[i].value);
          }
          this.$emit('confirm', 'ADD_PROJECT', selectValueArr, this.selectedUserGroup);
          this.loading = true;
        } else {
          return false;
        }
      });
    },
    getWorkspace(resolve) {
      this.$get("workspace/list/", res => {
        let data = res.data ? res.data : [];
        if (data.length > 0) {
          data.forEach(d => d.leaf = false);
        }
        resolve(data);
      })
    },
    getProject(condition, resolve) {
      this.$get("project/listAll/" + condition, res => {
        let data = res.data ? res.data : [];
        if (data.length > 0) {
          data.forEach(d => d.leaf = true);
        }
        resolve(data);
      })
    },
    getProjectUserGroup() {
      // 系统菜单显示所有项目类型用户组
      if (this.cascaderLevel === 2) {
        this.$post("/user/group/get", {type: GROUP_PROJECT}, (res) => {
          this.projectUserGroups = res.data ? res.data : [];
        });
      } else if (this.cascaderLevel === 1) {
        // 过滤工作空间下用户组
        this.result = this.$post('/user/group/list', {
          type: GROUP_PROJECT,
          resourceId: getCurrentWorkspaceId()
        }, (res) => {
          this.projectUserGroups = res.data ? res.data : [];
        });
      }
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
  width: var(--cascaderMenuWidth);
}
</style>
