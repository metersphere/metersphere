<template>
  <el-dialog class="user-cascade" :title="title" :visible.sync="dialogVisible"
             @close="close" v-loading="loading">
    <div class="block">
      <el-form :model="ruleForm" :rules="rules" ref="ruleForm">
        <el-form-item prop="project" label-width="0px">
          <el-cascader-panel :props="props"
                             popper-class="ms-cascade"
                             :emitPath="true"
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
import {GROUP_SYSTEM} from "@/common/js/constants";
import {USER_GROUP_SCOPE} from "@/common/js/table-constants";

export default {
  name: "GroupCascader",
  components: {ElUploadList, MsTableButton, MsDialogFooter},
  data() {
    let validateSelect = (rule, value, callback) => {
      let checkNodes = this.$refs.cascadeSelector.getCheckedNodes(true);
      if (checkNodes.length === 0) {
        callback(new Error(this.$t('user.select_group').toString()));
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
          {validator: validateSelect, message: this.$t('user.select_group'), trigger: 'change'}
        ],
      },
      selectedIds: [],
      isResourceShow: 0,
      props: {
        multiple: true,
        lazy: true,
        value: 'id',
        label: 'showLabel',
        lazyLoad(node, resolve) {
          const {level} = node;
          if (level === 0) {
            self.getGroup(resolve);
          } else if (level === 1) {
            self.getResource(node, resolve);
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
      this.rules.project[0].message = this.$t('user.select_group');
    },
    confirm() {
      this.$refs.ruleForm.validate((valid) => {
        if (valid) {
          let checkNodes = this.$refs.cascadeSelector.getCheckedNodes(true);
          let selectValueArr = [];
          for (let node of checkNodes) {
            let idString = "";
            if (node.parent) {
              idString = node.parent.value + "+" + node.value;
            } else {
              idString = node.value + "+none";
            }
            selectValueArr.push(idString);
          }
          this.$emit('confirm', 'ADD_USER_GROUP', selectValueArr);
          this.loading = true;
        } else {
          return false;
        }
      });
    },
    getGroup(resolve) {
      this.$get("user/group/get/all", res => {
        let data = res.data ? res.data : [];
        if (data.length > 0) {
          data.forEach(d => {
            d.leaf = d.type === GROUP_SYSTEM;
            d.showLabel = "[" + USER_GROUP_SCOPE[d.type] + "] " + d.name;
          });
        }
        resolve(data);
      })
    },
    getResource(node, resolve) {
      let type = node.data.type;
      let {value} = node;
      this.$get("user/group/" + type + "/" + value, res => {
        let data = res.data ? res.data : [];
        if (data.length > 0) {
          data.forEach(d => {
            d.leaf = true;
            d.showLabel = d.name;
          });
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
  width: 700px;
}

.user-cascade >>> .el-dialog__body {
  padding: 5px 20px;
}

/deep/ .el-form-item__content {
  margin-left: 0;
}

/deep/ .el-cascader-menu__wrap {
  height: 300px;
  width: 340px;
}
</style>
