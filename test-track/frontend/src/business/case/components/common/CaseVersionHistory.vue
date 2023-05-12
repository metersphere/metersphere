<template>
  <div class="version-history-box">
    <span v-if="isPublicShow">
      <slot
        name="versionLabel"
        v-if="versionEnable && currentVersion.id"
      ></slot>
    </span>

    <el-popover
      v-else
      :append-to-body="false"
      placement="bottom-start"
      width="392"
      trigger="click"
      popper-class="version-popover"
      v-loading="loading"
    >
      <div class="version-history-wrap">
        <div class="label-row">
          <div class="label">{{ $t("project.version.name") }}</div>
        </div>
        <div class="history-container">
          <div class="item-row"
               v-for="item in versionOptions"
               :key="item.id"
               :class="{'not-create-item' : !caseVersionMap.has(item.id)}"
               @click="checkout(item)">
            <div class="left-detail-row">
              <div class="version-info-row">
                <div
                  class="version-label"
                  :class="{'active': item.id == currentVersionId}">
                  {{ item.name }}
                </div>
                <div class="version-lasted" v-if="item.id == dataLatestId">
                  {{ $t("case.last_version") }}
                </div>
              </div>
              <div class="version-detail" v-if="item.createName">
                <div class="creator">
                  {{ item.createName }} {{ $t("commons.create") }}
                </div>
              </div>
            </div>
            <div class="right-opt-row">
              <div
                class="updated opt-row"
                @click.stop="setLatest(item)"
                v-if="showSetNew(item)"
              >
                {{ $t("case.set_new") }}
              </div>
              <div
                class="create opt-row"
                v-if="!caseVersionMap.has(item.id)
                  && !isRead"
                @click.stop="create(item)"
              >
                {{ $t("commons.create") }}
              </div>
              <div
                class="delete opt-row"
                @click.stop="del(item)"
                v-if="caseVersionMap.has(item.id)
                  && !(item.id === currentVersionId)
                  && !isRead"
              >
                {{ $t("commons.delete") }}
              </div>
            </div>
          </div>
        </div>
        <div class="compare-row"
             :class="{'compare-btn-disable': compareDisable}"
             @click.stop="openCompare">
          <div class="icon">
            <img src="/assets/module/figma/icon_contrast_outlined.svg" alt=""/>
          </div>
          <div class="label">{{ $t("case.version_comparison") }}</div>
        </div>
      </div>

      <!-- origin -->
      <span slot="reference">
        <slot
          name="versionLabel"
          v-if="versionEnable && currentVersion.id"
        ></slot>
      </span>
    </el-popover>

    <el-dialog
      :visible.sync="compareDialogVisible"
      :title="$t('case.version_comparison')"
      width="600px"
      @close="closeCompareVersionDialog"
    >
      <div class="compare-wrap">
        <div class="version-left-box">
          <el-select v-model="versionLeftId" size="small" clearable>
            <template #prefix>
              <span class="compare-version-lasted" v-if="versionLeftId === dataLatestId">
                {{ $t("case.last_version") }}
              </span>
            </template>
            <el-option
              v-for="item in versionLeftCompareOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            >
              <span>{{ item.name }}</span>
              <span class="compare-version-lasted" v-if="item.id == dataLatestId">
                {{ $t("case.last_version") }}
              </span>
            </el-option>
          </el-select>
        </div>
        <div class="desc">{{ $t("case.compare") }}</div>
        <div class="version-right-box">
          <el-select v-model="versionRightId" size="small" clearable>
            <template #prefix>
              <span class="compare-version-lasted" v-if="versionRightId === dataLatestId">
                {{ $t("case.last_version") }}
              </span>
            </template>
            <el-option
              v-for="item in versionRightCompareOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            >
              <span>{{ item.name }}</span>
              <span class="compare-version-lasted" v-if="item.id == dataLatestId">
                {{ $t("case.last_version") }}
              </span>
            </el-option>
          </el-select>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="closeCompareVersionDialog" size="small">{{
            $t("commons.cancel")
          }}</el-button>
        <el-button
          :type="enableCompare ? 'primary' : 'info'"
          :disabled="!enableCompare"
          @click="compareBranch"
          size="small"
        >{{ $t("commons.confirm") }}</el-button
        >
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {hasLicense} from "metersphere-frontend/src/utils/permission";
import {
  getProjectMembers,
  getProjectVersions,
  isProjectVersionEnable,
} from "metersphere-frontend/src/api/version";
import {getTestCaseVersions} from "@/api/testCase";

export default {
  name: "CaseVersionHistory",
  props: {
    currentId: String,
    currentVersionId: String,
    testUsers: Array,
    useExternalUsers: Boolean,
    isTestCaseVersion: {
      type: Boolean,
      default: false,
    },
    isRead: {
      type: Boolean,
      default: false,
    },
    currentProjectId: {
      type: String,
      default() {
        return getCurrentProjectID();
      },
    },
    hasLatest: {
      type: Boolean,
      default: false,
    },
    isPublicShow: Boolean
  },
  data() {
    return {
      loading: false,
      versionEnable: false,
      versionOptions: [],
      versionCompareOptions: [],
      versionLeftCompareOptions: [],
      versionRightCompareOptions: [],
      userData: {},
      currentVersion: {},
      dataLatestId: null,
      latestVersionId: null,
      compareDialogVisible: false,
      // 版本对比相关
      versionLeftId: '',
      versionRightId: '',
      // 当前用例的所有版本
      caseVersionMap: new Map()
    };
  },
  computed: {
    enableCompare() {
      return this.versionLeftId && this.versionRightId;
    },
    compareDisable() {
      return !this.versionCompareOptions || this.versionCompareOptions.length < 2;
    }
  },
  beforeDestroy() {
    this.clearSelectData();
  },
  methods: {
    closeCompareVersionDialog() {
      this.compareDialogVisible = false;
      this.clearSelectData();
    },
    openCompare() {
      if (this.compareDisable) {
        return;
      }
      this.setDefaultCompareOptions();
      this.versionRightId = this.currentVersionId;
      this.compareDialogVisible = true;
    },
    clearSelectData() {
      //清空表单数据
      this.versionLeftId = '';
      this.versionRightId = '';
    },
    findVersionById(id) {
      let version = this.versionCompareOptions.filter((v) => v.id === id);
      return Array.isArray(version) ? version[0] : version || {};
    },
    compareBranch() {
      this.$emit(
        'compareBranch',
        this.findVersionById(this.versionLeftId),
        this.findVersionById(this.versionRightId)
      );
      this.clearSelectData();
    },
    async getVersionOptionList() {
      if (!this.currentProjectId) {
        return;
      }
      let response = await getProjectVersions(this.currentProjectId);
      let versions = response.data.filter((v) => v.status === "open") || [];
      this.versionOptions = versions;

      response = await getTestCaseVersions(this.currentId);
      let allVersionCases = response.data || [];
      this.caseVersionMap = new Map();
      allVersionCases.forEach((c) => this.caseVersionMap.set(c.versionId, c));

      // 版本对比的的选项，排除该用例没有的版本
      this.versionCompareOptions = this.versionOptions.filter((v) => this.caseVersionMap.has(v.id));

      this.handleVersionOptions();
    },
    updateUserDataByExternal() {
      if (this.testUsers && this.testUsers.length > 0) {
        this.testUsers.forEach((u) => {
          this.userData[u.id] = u.name;
        });
      }
    },
    getUserOptions() {
      if (this.useExternalUsers) {
        this.updateUserDataByExternal();
      } else {
        getProjectMembers().then((response) => {
          this.userOptions = response.data;
          this.userOptions.forEach((u) => {
            this.userData[u.id] = u.name;
          });
        });
      }
    },
    compare(row) {
      this.$emit("compare", row);
    },
    checkout(row) {
      let versionCase = this.caseVersionMap.get(row.id);
      if (!versionCase || this.currentVersionId === row.id) {
        return;
      }
      this.loading = true;
      this.$emit("checkout", versionCase);
    },
    create(row) {
      this.loading = true;
      this.$emit("create", row);
    },
    del(row) {
      this.loading = true;
      this.$emit("del", row);
    },
    setLatest(row) {
      this.loading = true;
      this.$emit("setLatest", row);
    },
    showSetNew(item) {
      let hasVersionCase = this.caseVersionMap.has(item.id);
      let latestVersionCondition = this.caseVersionMap.has(this.latestVersionId) ? item.id === this.latestVersionId : true;
      let isNotDataLatestVersionCase = item.id === this.dataLatestId;
      return hasVersionCase // 有当前版本的用例
          && latestVersionCondition  // 有最新版本的用例，则非最新版本的其他版本不显示置新
          && !this.isRead // 不是只读
          && !isNotDataLatestVersionCase // 已经是最新版本，不显示置新
    },
    handleVersionOptions() {
      let latestData = {};
      this.versionOptions.forEach(v => {
        if (v.latest) {
          // 获取最新版本
          this.latestVersionId = v.id;
        }

        // 获取当前版本
        if (v.id === this.currentVersionId) {
          v.isCurrent = true;
          this.currentVersion = v;
          this.$emit('setCurrentVersionName', this.currentVersion.name);
        }

        let versionCase = this.caseVersionMap.get(v.id);

        if (versionCase) {
          // 设置版本的创建人
          v.createName = versionCase.createName;

          // 获取当前用例最新版本
          if (versionCase.latest) {
            latestData = v;
            this.dataLatestId = v.id;
            this.$emit('setLatestVersionId', this.dataLatestId);
          }
        }
      });

      if (!this.currentVersionId) {
        // 新建的时候没有versionId , 获取最新的版本作为默认的versionId
        this.currentVersion = latestData;
      }

      this.$emit('setIsLastedVersion', this.currentVersionId === this.dataLatestId);

      this.loading = false;
    },
    setDefaultCompareOptions() {
      this.versionLeftCompareOptions = this.versionCompareOptions;
      this.versionRightCompareOptions = this.versionCompareOptions;
    },
  },
  watch: {
    versionLeftId() {
      // 左边选中，右边过滤该版本，避免比较相同版本
      if (this.versionLeftId) {
        this.versionRightCompareOptions = this.versionCompareOptions.filter(v => v.id != this.versionLeftId);
      } else {
        this.versionRightCompareOptions = this.versionCompareOptions;
      }
    },
    versionRightId() {
      if (this.versionRightId) {
        this.versionLeftCompareOptions = this.versionCompareOptions.filter(v => v.id != this.versionRightId);
      } else {
        this.versionLeftCompareOptions = this.versionCompareOptions;
      }
    },
    currentId() {
      if (!hasLicense()) {
        return;
      }
      isProjectVersionEnable(this.currentProjectId)
        .then((response) => {
          this.versionEnable = response.data;
          if (this.versionEnable) {
            this.getUserOptions();
            this.getVersionOptionList();
          }
        });
    },
    testUsers() {
      this.updateUserDataByExternal();
    },
  },
};
</script>

<style scoped lang="scss">
@import "@/business/style/index.scss";

:deep(.el-popover) {
  width: 392px !important;
}

.compare-version-lasted {
  display: inline-block;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #783887;
  padding: 1px 6px;
  margin-left: 5px;
  gap: 4px;
  min-width: 49px;
  height: 20px;
  background: rgba(120, 56, 135, 0.2);
  border-radius: 2px;
  margin-right: 8px;
}

.compare-btn-disable {
  color: #BBBFC4 !important;
}

.version-history-wrap {
  width: 392px;

  .label-row {
    height: 32px;
    line-height: 32px;
    margin-top: 8px;

    .label {
      font-weight: 500;
      color: #1f2329;
      width: 100px;
      height: 22px;
      line-height: 22px;
      margin-left: 11px;
    }
  }

  .history-container {
    overflow: scroll;
    max-height: 300px;

    .item-row:hover {
      background: rgba(31, 35, 41, 0.1);
    }

    .item-row {
      cursor: pointer;
      margin-top: 8px;
      height: 50px;
      display: flex;
      padding: 0 11px;
      justify-content: space-between;

      .left-detail-row {
        display: flex;
        flex-direction: column;
        justify-content: center;

        .version-info-row {
          display: flex;

          .version-label {
            height: 22px;
            font-weight: 500;
            font-size: 14px;
            line-height: 22px;
            color: #1f2329;
          }

          .version-lasted {
            height: 20px;
            margin-left: 4px;
            background: rgba(120, 56, 135, 0.2);
            border-radius: 2px;
            padding: 1px 6px;
            color: #783887;
            text-align: center;
            line-height: 20px;
          }
        }

        .version-detail {
          .creator {
            height: 20px;
            font-size: 12px;
            line-height: 20px;
            color: #8f959e;
          }
        }
      }

      .right-opt-row {
        display: flex;
        align-items: center;
        justify-content: flex-end;

        .opt-row:not(:first-child) {
          margin-left: 16px;
        }

        .opt-row {
          height: 22px;
          line-height: 22px;
          font-size: 14px;
          text-align: center;
          color: #646a73;
          cursor: pointer;
        }

        .opt-row:hover {
          background: rgba(120, 56, 135, 0.1);
          border-radius: 4px;
          cursor: pointer;
          padding-left: 3px;
          padding-right: 3px;
          color: #783887;
        }

        .updated {
        }

        .create {
        }

        .delete {
        }
      }
    }

    .not-create-item {
      height: 32px;
    }
  }

  .active {
    color: #783887 !important;
    font-weight: 550 !important;
  }

  .compare-row {
    cursor: pointer;
    display: flex;
    height: 32px;
    line-height: 32px;
    border-top: 1px solid rgba(31, 35, 41, 0.15);
    align-items: center;

    .icon {
      margin-left: 11.67px;
      margin-right: 4.6px;
      margin-top: 3px;
      /* width: 14.67px;
      height: 13.33px; */
      img {
        width: 100%;
        height: 100%;
      }
    }

    color: #646a73;
    .label {
      font-size: 14px;
    }
  }
}

.compare-wrap {
  display: flex;

  :deep(.el-select--small) {
    width: 100%;
  }

  .version-left-box {
    width: 254px;

    el-select {
    }
  }

  .desc {
    color: #1f2329;
    margin: 0 8px;
    height: 32px;
    line-height: 32px;
  }

  .version-right-box {
    width: 254px;

    el-select {
    }
  }

  margin-top: 24px;
  margin-bottom: 24px;
}

:deep(.el-input--prefix .el-input__inner) {
  padding-left: 15px;
}

:deep(.el-input__prefix) {
  left: 60px;
  transition: all 0.3s;
  top: 5px;
}
</style>
<style>
.version-popover {
  position: relative;
  padding: 0px !important;
}
</style>
