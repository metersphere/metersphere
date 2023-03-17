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
    <el-dropdown-menu style="margin: 5px 0; width: 120px; padding: 5px 0;">
      <el-dropdown-item :disabled="disable" command="STEP">
        <div>{{ $t("test_track.case.step_describe") }}</div>
      </el-dropdown-item>
      <el-dropdown-item :disabled="disable" command="TEXT" divided>
        <div>{{ $t("test_track.case.text_describe") }}</div>
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
  padding: 0.3rem 0.6rem;
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

.el-dropdown-menu__item:hover {
  background-color: rgba(31, 35, 41, 0.1)!important;
}

.el-dropdown-menu__item {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  color: #1F2329!important;
}
</style>

<style>
.el-popper .popper__arrow{
  display: none!important;
}
</style>
