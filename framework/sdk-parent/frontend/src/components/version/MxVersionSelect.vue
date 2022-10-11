<template>
  <span class="version-select" v-if="projectVersionEnable"
        :style="{marginLeft: marginLeft + 'px',marginRight: marginRight + 'px'}">
    <el-select size="small" v-model="currentVersion" @change="changeVersion"
               :filterable="true"
               :placeholder="$t('api_test.api_import.latest_version')"
               clearable>
      <el-option
        v-for="item in versionOptions"
        :key="item.id"
        :label="item.name + ' (' + item.status + ')'"
        :value="item.id">
      </el-option>
    </el-select>
  </span>
</template>

<script>
import {hasLicense} from "../../utils/permission";
import {getProjectVersions, isProjectVersionEnable} from "../../api/version";

export default {
  name: "MxVersionSelect",
  props: {
    projectId: String,
    versionId: String,
    defaultVersion: String,
    marginLeft: {
      type: String,
      default: '0'
    },
    marginRight: {
      type: String,
      default: '0'
    }
  },
  data() {
    return {
      versionOptions: [],
      currentVersion: '',
      projectVersionEnable: false,
    };
  },
  created() {
    if (this.defaultVersion) {
      this.currentVersion = this.defaultVersion;
    }
    if (this.versionId) {
      this.currentVersion = this.versionId;
    }
    if (hasLicense()) {
      this.isVersionEnable();
      this.getVersionOptionList(this.projectId);
    }
  },
  methods: {
    changeVersion() {
      this.$emit('changeVersion', this.currentVersion);
    },
    getVersionOptionList(projectId) {
      if (!projectId) {
        return;
      }
      if (hasLicense()) {
        getProjectVersions(projectId)
          .then(response => {
            this.versionOptions = response.data;
          });
      }
    },
    isVersionEnable() {
      if (!this.projectId) {
        return;
      }
      isProjectVersionEnable(this.projectId)
        .then(response => {
          this.projectVersionEnable = response.data;
        });
    }
  },
  watch: {
    projectId() {
      if (!hasLicense()) {
        return;
      }
      this.currentVersion = null;
      this.getVersionOptionList(this.projectId);
      this.isVersionEnable();
    },
  }
};
</script>

<style scoped>

</style>
