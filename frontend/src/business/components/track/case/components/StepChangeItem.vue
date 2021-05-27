<template>
  <el-form-item :label="form.stepModel === 'STEP' ? $t('test_track.case.step_describe') : $t('test_track.case.text_describe')" :label-width="labelWidth">
    <el-dropdown placement="bottom-start" @command="handleCommand">
                  <span class="el-dropdown-link">
                    {{ $t('test_track.case.change_type') }} <i class="el-icon-arrow-down el-icon--right"/>
                  </span>
      <el-dropdown-menu>
        <el-dropdown-item :disabled="disable" command="STEP">
          <div>{{ $t('test_track.case.step_describe') }}</div>
<!--          <div>{{ $t('test_track.case.step_describe_tip') }}</div>-->
        </el-dropdown-item>
        <el-dropdown-item :disabled="disable" command="TEXT">
          <div>{{  $t('test_track.case.text_describe') }}</div>
<!--          <div>{{ $t('test_track.case.text_describe_tip') }}</div>-->
        </el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
  </el-form-item>
</template>

<script>
export default {
  name: "StepChangeItem",
  props: ['form','labelWidth', 'disable'],
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
    }
  }
}
</script>

<style scoped>
.el-dropdown-link {
  cursor: pointer;
  color: var(--primary_color);
}
.el-icon-arrow-down {
  font-size: 12px;
}

.step-change-item:hover {
  color: var(--primary_color);
  background-color: #F5F7FA;
}
</style>
