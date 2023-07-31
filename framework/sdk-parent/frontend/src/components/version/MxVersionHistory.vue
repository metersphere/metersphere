<template>
  <el-popover
    placement="bottom"
    width="550"
    trigger="click">
    <el-table :data="versionOptions" v-loading="loading" height="200px">
      <el-table-column
        prop="name"
        :label="$t('project.version.name')"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <font-awesome-icon v-if="scope.row.isCurrent"
                             class="icon global focusing" :icon="['fas', 'tag']"/>
          {{ scope.row.name }}
          <el-tag v-if="scope.row.id === dataLatestId" size="mini" type="primary">
            {{ $t('api_test.api_import.latest_version') }}&nbsp;
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" column-key="status"
                       min-width="65"
                       :label="$t('commons.status')">
        <template v-slot:default="{row}">
          <el-tag size="mini" type="primary" v-if="row.status === 'open'">
            {{ $t('project.version.version_open') }}
          </el-tag>
          <el-tag size="mini" type="info" effect="plain" v-else-if="row.status === 'closed'">
            {{ $t('project.version.version_closed') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        prop="createUser"
        :label="$t('operating_log.user')">
        <template v-slot:default="scope">
          <span>{{ userData[scope.row.createUser] ? userData[scope.row.createUser] : scope.row.createUser }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('commons.operating')" min-width="70px">
        <template v-slot:default="scope">
          <el-link @click="compare(scope.row)" v-if="scope.row.isCheckout" :disabled="scope.row.isCurrent">
            {{ $t('project.version.compare') }}&nbsp;
          </el-link>

          <el-link @click="checkout(scope.row)" v-if="scope.row.isCheckout" :disabled="scope.row.isCurrent">
            {{ $t('project.version.checkout') }}&nbsp;
          </el-link>

          <el-link v-if="!scope.row.isCheckout && scope.row.status === 'open'" @click="create(scope.row)"
                   :disabled="isRead" v-permission="['PROJECT_VERSION:READ+CREATE']">
            {{ $t('commons.create') }}&nbsp;
          </el-link>

          <el-link @click="del(scope.row)" v-if="scope.row.isCheckout" :disabled="scope.row.isCurrent || isRead"
                   v-permission="['PROJECT_VERSION:READ+DELETE']">
            {{ $t('commons.delete') }}&nbsp;
          </el-link>

          <el-popover
            placement="bottom"
            width="100"
            trigger="hover"
            v-if="scope.row.isCheckout || scope.row.status !== 'open'"
          >
            <div style="text-align: left;">
              <el-link @click="setLatest(scope.row)" v-if="hasLatest && scope.row.isCheckout"
                       :disabled="isRead || scope.row.id === dataLatestId">
                {{ $t('project.version.set_new') }}&nbsp;
              </el-link>

            </div>
            <span slot="reference">...</span>
          </el-popover>
        </template>
      </el-table-column>
    </el-table>
    <span slot="reference">
      <el-link type="primary" style="margin-right: 5px" v-if="versionEnable && currentVersion.id">
        {{ $t('project.version.name') }}: {{ currentVersion.name }}
      </el-link>
    </span>
  </el-popover>
</template>

<script>

import {getCurrentProjectID} from "../../utils/token";
import {hasLicense} from "../../utils/permission";
import {getDefaultVersion, getProjectMembers, getProjectVersions, isProjectVersionEnable} from "../../api/version";

export default {
  name: "MxVersionHistory",
  props: {
    versionData: Array,
    currentId: String,
    testUsers: Array,
    useExternalUsers: Boolean,
    isTestCaseVersion: {
      type: Boolean,
      default: false
    },
    isRead: {
      type: Boolean,
      default: false
    },
    currentProjectId: {
      type: String,
      default() {
        return getCurrentProjectID();
      }
    },
    hasLatest: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      loading: false,
      versionEnable: false,
      versionOptions: [],
      userData: {},
      currentVersion: {},
      dataLatestId: ''
    };
  },
  methods: {
    getVersionOptionList(callback) {
      getProjectVersions(this.currentProjectId)
        .then(response => {
          this.versionOptions = response.data.filter(v => v.status === 'open');
          if (callback) {
            callback(this.versionOptions);
          }
        });
    },
    updateUserDataByExternal() {
      if (this.testUsers && this.testUsers.length > 0) {
        this.testUsers.forEach(u => {
          this.userData[u.id] = u.name;
        });
      }
    },
    getUserOptions() {
      if (this.useExternalUsers) {
        this.updateUserDataByExternal();
      } else {
        getProjectMembers()
          .then(response => {
            this.userOptions = response.data;
            this.userOptions.forEach(u => {
              this.userData[u.id] = u.name;
            });
          });
      }
    },
    compare(row) {
      this.$emit('compare', row);
    },
    checkout(row) {
      this.loading = true;
      this.$emit('checkout', row);
      this.loading = false;
    },
    create(row) {
      this.loading = true;
      this.$emit('create', row);
    },
    del(row) {
      this.loading = true;
      this.$emit('del', row);
      this.loading = false;
    },
    setLatest(row) {
      this.loading = true;
      this.$emit('setLatest', row);
      this.loading = false;
    },
    handleVersionOptions() {
      let versionData = this.versionData;
      if (versionData.length === 0) {
        this.currentVersion = this.versionOptions.filter(v => v.status === 'open' && v.latest)[0] || {};
        this.loading = false;
        return;
      }
      let latestData = versionData.filter((v) => v.latest === true);
      if (latestData) {
        this.dataLatestId = latestData[0].versionId;
      }
      this.versionOptions.forEach(version => {
        let vs = versionData.filter(v => v.versionId === version.id);
        version.isCheckout = vs.length > 0; // 已存在可以切换，不存在则创建
        if (version.isCheckout) {
          version.createUser = vs[0].createUser || vs[0].userId || vs[0].creator;
        } else {
          version.createUser = null;
        }
        let lastItem = versionData.filter(v => v.id === this.currentId)[0];
        if (lastItem) {
          version.isCurrent = lastItem.versionId === version.id;
          if (version.isCurrent) {
            this.currentVersion = version;
          }
        }
      });
      this.loading = false;
    }
  },
  watch: {
    versionData() {
      if (!hasLicense()) {
        return;
      }
      isProjectVersionEnable(this.currentProjectId)
        .then(response => {
          this.versionEnable = response.data;
        });
      this.getUserOptions();
      this.getVersionOptionList(this.handleVersionOptions);
    },
    testUsers() {
      this.updateUserDataByExternal();
    }
  },
};
</script>

<style scoped>

</style>
