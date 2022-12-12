<template>
  <!--创建新版本选择其他信息对话框-->
  <el-dialog
    :title="$t('case.create_version')"
    :visible.sync="visible"
    :show-close="false"
    width="600px"
  >
    <div class="version-label">{{ $t("case.choose_copy_info") }}</div>
    <el-form ref="form" :model="form" label-width="10px" label-position="left">
      <div class="version-detail-wrap">
        <div class="item-row">
          <el-form-item>
            <el-checkbox v-model="form.remark">{{
              $t("commons.remark")
            }}</el-checkbox>
          </el-form-item>
        </div>
        <div class="item-row">
          <el-form-item>
            <el-checkbox v-model="form.relateTest">{{
              $t("test_track.case.relate_test")
            }}</el-checkbox>
          </el-form-item>
        </div>
        <div class="item-row">
          <el-form-item>
            <el-checkbox v-model="form.relateDemand">{{
              $t("test_track.related_requirements")
            }}</el-checkbox>
          </el-form-item>
        </div>
      </div>
      <div class="version-detail-wrap">
        <div class="item-row">
          <el-form-item>
            <el-checkbox v-model="form.relateIssue">{{
              $t("test_track.case.relate_issue")
            }}</el-checkbox>
          </el-form-item>
        </div>
        <div class="item-row">
          <el-form-item>
            <el-checkbox v-model="form.dependency">{{
              $t("commons.relationship.name")
            }}</el-checkbox>
          </el-form-item>
        </div>
        <div class="item-row">
          <el-form-item>
            <el-checkbox v-model="form.archive">{{
              $t("test_track.case.attachment")
            }}</el-checkbox>
          </el-form-item>
        </div>
      </div>
      <!-- origin -->
      <!-- <el-row>
        <el-col :span="6">
          <el-form-item>
            <el-checkbox v-model="form.remark">{{
              $t("commons.remark")
            }}</el-checkbox>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item>
            <el-checkbox v-model="form.relateTest">{{
              $t("test_track.case.relate_test")
            }}</el-checkbox>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item>
            <el-checkbox v-model="form.relateDemand">{{
              $t("test_track.related_requirements")
            }}</el-checkbox>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item>
            <el-checkbox v-model="form.relateIssue">{{
              $t("test_track.case.relate_issue")
            }}</el-checkbox>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="6">
          <el-form-item>
            <el-checkbox v-model="form.dependency">{{
              $t("commons.relationship.name")
            }}</el-checkbox>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item>
            <el-checkbox v-model="form.archive">{{
              $t("test_track.case.attachment")
            }}</el-checkbox>
          </el-form-item>
        </el-col>
      </el-row> -->
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">{{ $t("commons.cancel") }}</el-button>
      <el-button type="primary" @click="confirmOtherInfo">{{
        $t("commons.confirm")
      }}</el-button>
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
      },
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
    },
  },
};
</script>

<style scoped lang="scss">
.version-label {
  margin-bottom: 8px;
  color: #1f2329;
  margin-top: 12px;
}
.version-detail-wrap {
  :deep(.el-form-item__content) {
    margin-left: 0px !important;
    width: 126px;
  }

  display: flex;
  justify-content: flex-start;
  .item-row:not(:first-child) {
    margin-left: 24px;
  }
}
</style>
