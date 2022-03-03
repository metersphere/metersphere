<template>
  <div v-loading="result.loading">
    <el-dialog :close-on-click-modal="false" :title="title" :visible.sync="createVisible" destroy-on-close
               @close="handleClose" width="60%">
      <el-form :model="form" :rules="rules" ref="form" label-position="right" label-width="90px" size="small">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="form.name" autocomplete="off" show-word-limit maxlength="50"></el-input>
        </el-form-item>
        <el-form-item :label="$t('commons.description')" prop="description">
          <el-input v-model="form.description" autocomplete="off" type="textarea" show-word-limit maxlength="200"></el-input>
        </el-form-item>
        <el-form-item>
          <environment-group-row ref="environmentGroupRow" :env-group-id="environmentId" :read-only="false" :show-save-btn="true"/>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <div class="dialog-footer">
          <ms-dialog-footer
            btn-size="medium"
            @cancel="createVisible = false"
            @confirm="submit('form')"/>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";
import EnvironmentGroupRow from "@/business/components/settings/workspace/environment/EnvironmentGroupRow";

export default {
  name: "EditEnvironmentGroup",
  components: {
    EnvironmentGroupRow,
    MsDialogFooter
  },
  data() {
    return {
      title: this.$t('workspace.env_group.create'),
      form: {},
      createVisible: false,
      rules: {
        name: [
          {required: true, message: this.$t('api_test.environment.please_input_env_group_name'), trigger: 'blur'},
          {min: 1, max: 50, message: this.$t('commons.input_limit', [1, 50]), trigger: 'blur'}
        ],
        description: [
          {max: 200, message: this.$t('commons.input_limit', [0, 200]), trigger: 'blur'}
        ],
      },
      environmentGroup: {},
      environmentId: "",
      result: {}
    }
  },
  methods: {
    handleClose() {},
    submit() {
      this.$refs['form'].validate((valid) => {
        if (valid) {
          let sign = this.$refs.environmentGroupRow.valid();
          if (!sign) {
            this.$warning(this.$t('workspace.env_group.not_intact'));
            return false;
          }
          let envGroupProject = this.$refs.environmentGroupRow.envGroupProject;
          let param = {
            envGroupProject,
            name: this.form.name,
            description: this.form.description
          }
          this.result = this.$post("/environment/group/add", param, () => {
            this.$success(this.$t('commons.save_success'));
            this.createVisible = false;
            this.$emit("refresh");
          });
        } else {
          return false;
        }
      });
    },
    open() {
      this.createVisible = true;
      this.form = {
        name: "",
        description: ""
      };
    }
  }
}
</script>

<style scoped>

</style>
