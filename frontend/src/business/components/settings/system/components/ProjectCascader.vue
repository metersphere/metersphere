<template>
  <el-dialog class="user-cascade" :title="title" :visible.sync="dialogVisible"
             @close="close">
    <div class="block">
      <el-alert
        title="默认为成员添加只读用户组(系统)"
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
        callback(new Error("请选择项目"));
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
          {validator: validateSelect, message: "请选择项目", trigger: 'change'}
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
      isLoading: false,
      batchProcessType: '',
      options: [],
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
      this.dialogVisible = false;
      this.selectedIds = [];
      ++this.isResourceShow;
      this.options = [];
      this.$refs['ruleForm'].resetFields();
    },
    open(batchProcessType, optionsParam) {
      listenGoBack(this.close);
      this.dialogVisible = true;
      this.batchProcessType = batchProcessType;
      this.options = optionsParam;
      if (this.batchProcessType === 'ADD_PROJECT') {
        this.rules.project[0].message = "请选择项目";
      } else {
        this.rules.project[0].message = "请选择用户组";
      }
    },
    confirm() {
      this.$refs.ruleForm.validate((valid) => {
        if (valid) {
          let checkNodes = this.$refs.cascadeSelector.getCheckedNodes(true);
          let selectValueArr = [];
          for (let i = 0; i < checkNodes.length; i++) {
            selectValueArr.push(checkNodes[i].value);
          }
          this.$emit('confirm', this.batchProcessType, selectValueArr)
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
