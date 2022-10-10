<template>
  <div v-if="isShow">
    <el-row :gutter="12">
      <el-col :span="8">
        <el-card shadow="hover">
          {{ $t('commons.my_workstation') }}
          <el-switch v-model="workstation" @change="update('workstation')" active-value="ENABLE"
                     inactive-value="DISABLE" :disabled="disabled"/>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          {{ $t('test_track.test_track') }}
          <el-switch v-model="track" @change="update('track')" active-value="ENABLE" inactive-value="DISABLE"
                     :disabled="disabled"/>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          {{ $t('commons.api') }}
          <el-switch v-model="api" @change="update('api')" active-value="ENABLE" inactive-value="DISABLE"
                     :disabled="disabled"/>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="12">
      <el-col :span="8">
        <el-card shadow="hover">
          {{ $t('commons.ui') }}
          <el-switch v-model="ui" @change="update('ui')" active-value="ENABLE"
                     inactive-value="DISABLE" :disabled="disabled"/>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          {{ $t('commons.performance') }}
          <el-switch v-model="performance" @change="update('performance')" active-value="ENABLE"
                     inactive-value="DISABLE" :disabled="disabled"/>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          {{ $t('commons.report_statistics.title') }}
          <el-switch v-model="report" @change="update('report')" active-value="ENABLE"
                     inactive-value="DISABLE" :disabled="disabled"/>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import {MODULE_CHANGE, ModuleEvent} from "metersphere-frontend/src/components/head/ListEvent";
import {hasPermission} from "metersphere-frontend/src/utils/permission";
import {getModuleList, updateStatus} from "@/api/module";
import {hasLicense} from "metersphere-frontend/src/utils/permission";

export default {
  name: "MxModule",
  props: {
    isShow: {
      type: Boolean,
      default: true,
    }
  },
  data() {
    return {
      api: "ENABLE",
      performance: "ENABLE",
      project: "ENABLE",
      report: "ENABLE",
      reportStat: "ENABLE",
      setting: "ENABLE",
      testTrack: "ENABLE",
      track: "ENABLE",
      ui: "ENABLE",
      workstation: "ENABLE",
    };
  },
  mounted() {
    if (hasLicense()) {
      this.list();
    }
  },
  computed: {
    disabled() {
      if (this.isShow) {
        return !hasPermission('SYSTEM_SETTING:READ+EDIT');
      }
      return true;
    }
  },
  methods: {
    list() {
      return getModuleList()
        .then(response => {
          let modules = {}
          response.data.forEach(m => {
            this[m.key] = m.status;
            modules[m.key] = m.status;
          });
          localStorage.setItem('modules', JSON.stringify(modules));
        });
    },
    update(key) {
      updateStatus(key, this[key])
        .then(() => {
          this.$success(this.$t('commons.save_success'));
          ModuleEvent.$emit(MODULE_CHANGE, key, this[key]);
          let modules = JSON.parse(localStorage.getItem('modules'));
          modules[key] = this[key];
          localStorage.setItem('modules', JSON.stringify(modules));
        })
    }
  },
};
</script>

<style scoped>
.el-card .el-switch {
  float: right;
}

.el-row {
  padding-bottom: 5px;
}
</style>
