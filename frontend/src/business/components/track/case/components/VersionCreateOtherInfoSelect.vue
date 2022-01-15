<template>
  <div>
    <el-form ref="form" :model="form" label-width="10px" label-position="left">
      <el-row>
        <el-col :span="24">
          <el-form-item>
            <span>{{ $t('test_track.case.sync_to_new_version') }}</span>
          </el-form-item>
        </el-col>
      </el-row>
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
      <el-row>
        <el-form-item style="float:right">
          <el-button size="small" @click="$emit('cancelOtherInfo')">{{ $t('commons.cancel') }}</el-button>
          <el-button type="primary" size="small" @click="confirmOtherInfo">{{ $t('commons.confirm') }}</el-button>
        </el-form-item>
      </el-row>
    </el-form>
  </div>
</template>

<script>

export default {
  name: "VersionCreateOtherInfoSelect",
  data() {
    return {
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
    confirmOtherInfo() {
      this.$emit("confirmOtherInfo", this.form);
    }
  }
};
</script>

<style scoped>

</style>
