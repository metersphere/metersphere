<template>
  <el-dialog class="user-casecader" :title="title" :visible.sync="dialogVisible"
             @close="close">
    <div class="block" >
      <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
        <el-form-item prop="workspace" label-width="0px">
<!--          <el-cascader-->
<!--            :options="options"-->
<!--            :props="props"-->
<!--            v-model="selectedIds"-->
<!--            ref="cascaderSelector"-->
<!--            style="width:100%;"-->
<!--            :key="isResouceShow"-->
<!--            clearable></el-cascader>-->

          <el-cascader-panel :options="options"
                             :props="props"
                             v-model="selectedIds"
                             ref="cascaderSelector"
                             style="width:100%;"
                             :key="isResouceShow"
                             clearable></el-cascader-panel>
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
import {listenGoBack, removeGoBackListener} from "../../../../../common/js/utils";
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";

export default {
  name: "UserImport",
  components: {ElUploadList, MsTableButton,MsDialogFooter},
  data() {
    var validateSelect = (rule, value, callback) => {
      let checkNodes = this.$refs.cascaderSelector.getCheckedNodes(true);
      if(checkNodes.length==0){
        callback(new Error(this.$t('workspace.select')));
      }
      callback();
    };
    return {
      ruleForm: {
        workspace: '',
      },
      rules: {
        workspace: [
          { validator: validateSelect, message: this.$t('workspace.select'), trigger: 'change' }
        ],
      },
      selectedIds:[],
      isResouceShow:0,
      props: { multiple: true },
      dialogVisible: false,
      isLoading: false,
      batchProcessType:'',
      options:[],
    };
  },
  props: {
    title: {
      type: String,
      default: ''
    },
    lable: {
      type: String,
      default: ''
    },
  },
  methods: {
    close() {
      removeGoBackListener(this.close);
      this.dialogVisible = false;
      this.selectedIds=[];
      ++ this.isResouceShow;
      this.options = [];
      this.$refs['ruleForm'].resetFields();

    },
    open(batchProcessType,optionsParam) {
      listenGoBack(this.close);
      this.dialogVisible = true;
      this.batchProcessType = batchProcessType;
      this.options = optionsParam;
      if(this.batchProcessType == 'ADD_WORKSPACE'){
        this.rules.workspace[0].message = this.$t('workspace.select');
      }else{
        this.rules.workspace[0].message = this.$t('role.please_choose_role');
      }
    },
    confirm(){
      this.$refs.ruleForm.validate((valid) => {
        if (valid) {
          let checkNodes = this.$refs.cascaderSelector.getCheckedNodes(true);
          let selectValueArr = [];
          for (let i = 0; i < checkNodes.length; i++) {
            selectValueArr.push(checkNodes[i].value);
          }
          this.$emit('confirm',this.batchProcessType,selectValueArr)
        } else {
          return false;
        }
      });
    }
  }
}
</script>

<style>
</style>

<style scoped>

.user-casecader >>> .el-dialog {
  width: 600px;
}
/deep/ .el-form-item__content{
  margin-left: 0px;
}
/*.el-cascader-menu {*/
/*  height: 300px;*/
/*}*/
/*.el-cascader >>> .el-input--suffix {*/
/*  max-height: 200px;*/
/*}*/
/*.el-cascader >>> .el-cascader__tags {*/
/*  max-height: 190px;*/
/*  overflow: auto;*/
/*}*/
</style>
