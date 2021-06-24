<template>
  <ms-edit-dialog
    :visible.sync="visible"
    width="30%"
    :title="$t('run_mode.other_config')"
    :with-footer="false"
    :close-on-click-modal="true">
    <el-form>
      <el-row>
        <el-col :span="12">
            <el-form-item :label="$t('schema.minLength')">
            <el-input-number :min="0" v-model="data.min" :placeholder="$t('schema.minLength')" size="small"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="$t('schema.maxLength')">
            <el-input-number :min="0" v-model="data.max" :placeholder="$t('schema.maxLength')" size="small"/>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
  </ms-edit-dialog>
</template>

<script>

  import MsEditDialog from "@/business/components/common/components/MsEditDialog";
  export default {
    name: "ApiVariableSetting",
    components: {MsEditDialog},
    data() {
      return {
        visible: false,
        data: {}
      }
    },
    methods: {
      open(item) {
        this.visible = true;
        this.data = item;
      }
    }

  }
</script>

<style scoped>

</style>
