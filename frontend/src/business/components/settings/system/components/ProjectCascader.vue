<template>
  <el-dialog class="user-cascade" :title="title" :visible.sync="dialogVisible"
             @close="close" v-loading="loading">
    <div class="block">
      <el-alert
        :title="$t('user.add_project_batch_tip')"
        type="info"
        show-icon
        :closable="false"
        style="margin-bottom: 10px;"
      >
      </el-alert>
      <el-form :model="ruleForm" :rules="rules" ref="ruleForm">
        <el-form-item prop="project" label-width="0px">
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

export default {
  name: "User2ProjectCascader",
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
      isResourceShow: 0,
      props: {
        multiple: true,
        lazy: true,
        value: 'id',
        label: 'name',
        lazyLoad(node, resolve) {
          const {level, value} = node;
          if (level === 0) {
            self.getOrganization(resolve);
          } else if (level === 1) {
            self.getWorkspace(value, resolve);
          } else if (level === 2) {
            self.getProject(value, resolve);
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
          this.$emit('confirm', 'ADD_PROJECT', selectValueArr);
          this.loading = true;
        } else {
          return false;
        }
      });
    },
    getOrganization(resolve) {
      this.$get("organization/list", res => {
        let data = res.data ? res.data : [];
        if (data.length > 0) {
          data.forEach(d => d.leaf = false);
        }
        resolve(data);
      })
    },
    getWorkspace(condition, resolve) {
      this.$get("workspace/list/" + condition, res => {
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
    }
  }
}
</script>

<style>
</style>

<style scoped>
.user-cascade >>> .el-dialog {
  width: 750px;
}

.user-cascade >>> .el-dialog__body {
  padding: 5px 20px;
}

/deep/ .el-form-item__content {
  margin-left: 0;
}

/deep/ .el-cascader-menu__wrap {
  height: 300px;
  width: 250px;
}
</style>
