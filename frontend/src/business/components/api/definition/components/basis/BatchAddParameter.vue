<template>
  <div>
    <el-dialog
      :title="$t('commons.batch_add')"
      :visible.sync="dialogVisible"
      width="60%"
      class="batch-edit-dialog"
      :destroy-on-close="true"
      @close="handleClose">
      <div>
        <div>格式：参数名,必填,参数值,备注 如：Accept-Encoding,必填,utf-8,编码</div>
        <div style="height: 200px">
          <ms-code-edit :enable-format="false" mode="text" :data.sync="parameters" theme="eclipse" :modes="['text']"
                        ref="codeEdit"/>
        </div>
      </div>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="dialogVisible = false"
          @confirm="confirm()"/>
      </template>
    </el-dialog>
  </div>
</template>

<script>
  import MsDialogFooter from "../../../../common/components/MsDialogFooter";
  import {listenGoBack, removeGoBackListener} from "@/common/js/utils";
  import MsCodeEdit from "../../../../common/components/MsCodeEdit";

  export default {
    name: "BatchAddParameter",
    components: {
      MsDialogFooter,
      MsCodeEdit
    },
    props: {},
    data() {
      return {
        dialogVisible: false,
        parameters: "",
      }
    },
    methods: {
      open() {
        this.dialogVisible = true;
        listenGoBack(this.handleClose);
      },
      handleClose() {
        this.parameters = "";
        removeGoBackListener(this.handleClose);
      },
      confirm() {
        this.dialogVisible = false;
        this.$emit("batchSave", this.parameters);
      }
    }
  }
</script>

<style scoped>

</style>
