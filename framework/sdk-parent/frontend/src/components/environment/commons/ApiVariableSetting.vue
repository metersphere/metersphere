<template>
  <ms-edit-dialog
    :visible.sync="visible"
    width="70%"
    :title="$t('schema.adv_setting')"
    :with-footer="false"
    append-to-body
    :close-on-click-modal="true">
    <ms-env-variable-advance v-if="editData.type=='CONSTANT'" ref="variableAdvance" :current-item="editData"
                             @advancedRefresh="reload"/>
    <ms-edit-counter v-if="editData.type=='COUNTER'" ref="counter" :editData.sync="editData"/>
    <ms-edit-random v-if="editData.type=='RANDOM'" ref="random" :editData.sync="editData"/>
    <ms-edit-csv v-if="editData.type=='CSV'" ref="csv" :editData.sync="editData"/>

    <template v-slot:footer>
      <ms-dialog-footer
        @cancel="handleCancel"
        @confirm="handleConfirm"
      />
    </template>
  </ms-edit-dialog>

</template>

<script>

import MsEditDialog from "../../MsEditDialog";
import MsEditConstant from "./variable/EditConstant";
import MsEditCounter from "./variable/EditCounter";
import MsEditRandom from "./variable/EditRandom";
import MsEditListValue from "./variable/EditListValue";
import MsEditCsv from "./variable/EditCsv";
import MsEnvVariableAdvance from "./EnvVariableAdvance";
import MsDialogFooter from "../../MsDialogFooter";

export default {
  name: "ApiVariableSetting",
  components: {
    MsEditDialog,
    MsEditConstant,
    MsEditCounter,
    MsEditRandom,
    MsEditListValue,
    MsEditCsv,
    MsEnvVariableAdvance,
    MsDialogFooter
  },
  data() {
    return {
      visible: false,
      data: {},
      editData: {}
    }
  },
  methods: {
    open(item) {
      this.visible = true;
      this.editData = item;
    },
    handleConfirm() {
      if (this.editData.type === 'CONSTANT') {
        this.$refs.variableAdvance.saveAdvanced();
      }
      if (this.editData.type === 'CSV' && this.$refs.csv) {
        if (this.editData.files.length === 0) {
          this.$warning(this.$t('api_test.automation.csv_warning'));
          return;
        }
      }
      this.$emit('changeData');
      this.visible = false;
    },
    handleCancel() {
      this.visible = false;
    },
    reload() {
      this.isActive = false;
      this.$nextTick(() => {
        this.isActive = true;
      });
    },
  }

}
</script>

<style scoped>

</style>
