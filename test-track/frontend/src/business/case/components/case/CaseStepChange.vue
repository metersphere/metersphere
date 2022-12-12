<template>
  <el-dropdown
    placement="bottom-start"
    @command="handleCommand"
    v-if="!disable"
  >
    <span class="el-dropdown-link">
      {{ $t("test_track.case.change_type") }}
      <i class="el-icon-arrow-down el-icon--right" />
    </span>
    <el-dropdown-menu>
      <el-dropdown-item :disabled="disable" command="STEP">
        <div>{{ $t("test_track.case.step_describe") }}</div>
        <!--          <div>{{ $t('test_track.case.step_describe_tip') }}</div>-->
      </el-dropdown-item>
      <el-dropdown-item :disabled="disable" command="TEXT">
        <div>{{ $t("test_track.case.text_describe") }}</div>
        <!--          <div>{{ $t('test_track.case.text_describe_tip') }}</div>-->
      </el-dropdown-item>
    </el-dropdown-menu>
  </el-dropdown>
</template>

<script>
export default {
  name: "CaseStepChange",
  props: ["form", "labelWidth", "disable"],
  created() {
    if (!this.form.stepModel) {
      this.form.stepModel = "STEP";
    }
  },
  methods: {
    handleCommand(value) {
      if (this.disable) {
        return;
      }
      this.form.stepModel = value;
    },
  },
};
</script>

<style scoped>
.el-dropdown-link {
  cursor: pointer;
  color: #646a73;
}
.el-dropdown-link:hover {
  cursor: pointer;
  color: #783887;
  background: rgba(120, 56, 135, 0.1);
  border-radius: 4px;
}
.el-icon-arrow-down {
  font-size: 12px;
}

.step-change-item:hover {
  color: #1f2329;
  background: rgba(31, 35, 41, 0.1);
}
</style>
