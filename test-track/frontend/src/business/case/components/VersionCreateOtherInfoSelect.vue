<template>
  <!--创建新版本选择其他信息对话框-->
  <el-dialog
    :title="$t('case.create_version')"
    :visible.sync="visible"
    :show-close="false"
    width="600px"
    heigth="234px"
  >
    <div class="version-label">{{ $t("case.choose_copy_info") }}</div>
    <div class="version-detail-wrap">
      <div class="item-row">
        <el-checkbox v-model="form.remark">{{
            $t("commons.remark")
          }}
        </el-checkbox>
      </div>
      <div class="item-row">
        <el-checkbox v-model="form.relateTest">{{
            $t("test_track.case.relate_test")
          }}
        </el-checkbox>
      </div>
      <div class="item-row">
        <el-checkbox v-model="form.relateDemand">{{
            $t("test_track.related_requirements")
          }}
        </el-checkbox>
      </div>
    </div>
    <div class="version-detail-wrap">
      <div class="item-row">
        <el-checkbox v-model="form.relateIssue">{{
            $t("test_track.case.relate_issue")
          }}
        </el-checkbox>
      </div>
      <div class="item-row">
        <el-checkbox v-model="form.dependency">{{
            $t("commons.relationship.name")
          }}
        </el-checkbox>
      </div>
      <div class="item-row">
        <el-checkbox v-model="form.archive">{{
            $t("test_track.case.attachment")
          }}
        </el-checkbox>
      </div>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false" size="small">{{ $t("commons.cancel") }}</el-button>
      <el-button type="primary" size="small"
                 @click="confirmOtherInfo">{{
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
        versionId: null
      },
    };
  },
  methods: {
    open(versionId) {
      this.visible = true;
      this.form.versionId = versionId;
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

.version-detail-wrap:not(:last-child) {
  margin-bottom: 8px;
}

.version-detail-wrap {
  :deep(.el-form-item__content) {
    margin-left: 0px !important;
    width: 126px;
  }

  display: flex;
  justify-content: flex-start;

  .item-row {
    height: 22px;
    line-height: 22px;
    width: 168px;
  }

  .item-row:not(:first-child) {
    margin-left: 24px;
  }
}

.dialog-footer {
  .el-button {
    width: 80px;
    margin-top: 27px;
  }
}
</style>
