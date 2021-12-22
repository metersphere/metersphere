<template>
  <el-dialog class="user-cascade" :title="title" :visible.sync="dialogVisible"
             @close="close" v-loading="loading">
    <el-form :model="ruleForm" :rules="rules" ref="ruleForm">
      <el-form-item prop="project" label-width="0px">
        <el-alert
          :title="$t('workspace.env_group.cascader_tip')" style="height: 33px;"
          type="info" :closable="false" show-icon></el-alert>
        <el-row type="flex" justify="space-between" :gutter="10">
          <el-col :span="10" class="search">
            <el-input v-model="searchName" prefix-icon="el-icon-search"
                      :placeholder="$t('workspace.env_group.search_tip')" size="mini" style="width: 100%;"></el-input>
          </el-col>
          <el-col :span="10" class="search">
            <el-input v-model="groupName" :placeholder="$t('workspace.env_group.fast_create_tip')"
                      prefix-icon="el-icon-plus"
                      size="mini">
            </el-input>
          </el-col>
          <el-col :span="4">
            <el-button type="primary" @click="save" size="mini" style="width: 100%;">{{ $t('workspace.env_group.quickly_add') }}</el-button>
          </el-col>
        </el-row>
        <el-cascader-panel :props="props" v-loading="result.loading"
                           :options="envGroups"
                           popper-class="ms-cascade"
                           :emitPath="true"
                           ref="cascadeSelector"
                           :key="isResourceShow"
                           clearable>
        </el-cascader-panel>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
        <ms-dialog-footer
          :btn-size="'medium'"
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
  name: "EnvGroupCascader",
  components: {ElUploadList, MsTableButton, MsDialogFooter},
  data() {
    const self = this
    return {
      result: {},
      ruleForm: {
        project: '',
      },
      rules: {
        project: [
        ],
      },
      selectedIds: [],
      isResourceShow: 0,
      props: {
        multiple: true,
        lazy: false,
        value: 'id',
        label: 'name',
        lazyLoad(node, resolve) {
          const {level} = node;
          if (level === 0) {
            self.getGroup(resolve);
          } else {
            resolve([]);
          }
        }
      },
      dialogVisible: false,
      loading: false,
      groupName: "",
      searchName: "",
      envGroups: []
    };
  },
  props: {
    title: {
      type: String,
      default: ''
    }
  },
  watch: {
    searchName() {
      this.getGroup();
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
      this.getGroup();
    },
    confirm() {
      let checkNodes = this.$refs.cascadeSelector.getCheckedNodes(true);
      let selectValueArr = [];
      for (let node of checkNodes) {
        selectValueArr.push(node.value);
      }
      if (selectValueArr.length < 1) {
        this.$warning(this.$t('workspace.env_group.select'));
        return false;
      }
      this.$emit('confirm', selectValueArr);
      this.dialogVisible = false;
    },
    getGroup(resolve) {
      this.result = this.$post("/environment/group/get/all",{name: this.searchName}, res => {
        this.envGroups = res.data;
        let data = res.data;
        if (data.length > 0) {
          data.forEach(d => d.leaf = true);
        }
        if (resolve) {
          resolve(data);
        }
      })
    },
    save() {
      if (!this.groupName) {
        this.$warning(this.$t('workspace.env_group.name_not_null'));
        return false;
      }
      this.$post("/environment/group/add", {name: this.groupName}, () => {
        this.$success(this.$t('commons.save_success'));
        this.getGroup();
      });
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
  width: 700px;
}

.search >>> .el-input__inner{
  border-left: none;
  border-right: none;
  border-top: none;
  border-radius: 0;
}
</style>
