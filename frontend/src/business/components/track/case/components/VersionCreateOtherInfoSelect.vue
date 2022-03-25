<template>
  <!--创建新版本选择其他信息对话框-->
  <el-dialog
    :title="$t('commons.sync_other_info')"
    :visible.sync="visible"
    :show-close="false"
    width="30%"
  >
    <el-form ref="form" :model="form" label-width="10px" label-position="left">
      <el-row>
        <el-col :span="6">
          <el-form-item>
            <el-checkbox v-model="form.remark">{{ $t('commons.remark') }}</el-checkbox>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item>
            <el-checkbox v-model="form.relateTest">{{ $t('test_track.case.relate_test') }}</el-checkbox>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item>
            <el-checkbox v-model="form.relateDemand">{{ $t('test_track.related_requirements') }}</el-checkbox>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item>
            <el-checkbox v-model="form.relateIssue">{{ $t('test_track.case.relate_issue') }}</el-checkbox>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="6">
          <el-form-item>
            <el-checkbox v-model="form.dependency">{{ $t('commons.relationship.name') }}</el-checkbox>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item>
            <el-checkbox v-model="form.archive">{{ $t('test_track.case.attachment') }}</el-checkbox>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <span slot="footer" class="dialog-footer">
        <el-button @click="visible = false;">{{ $t('commons.cancel') }}</el-button>
        <el-button type="primary" @click="confirmOtherInfo">{{ $t('commons.confirm') }}</el-button>
    </span>
  </el-dialog>
</template>

<script>

export default {
  name: "VersionCreateOtherInfoSelect",
  data() {
    return {
      visible: false,
      form: {
        remark: false,
        relateTest: false,
        relateDemand: false,
        relateIssue: false,
        archive: false,
        dependency: false,
      }
    };
  },
  methods: {
    open() {
      this.visible = true;
    },
    close() {
      this.visible = false;
    },
    confirmOtherInfo() {
      this.visible = false;
      this.$emit("confirmOtherInfo", this.form);
    }
  }
};
</script>

<style scoped>

</style>
