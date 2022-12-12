<template>
  <div class="version-history-box">
    <el-popover
      placement="bottom-end"
      width="392"
      height="271"
      trigger="click"
      popper-class="version-popover"
      v-loading="loading"
    >
      <div class="version-history-wrap">
        <div class="label-row">
          <div class="label">{{ $t("project.version.name") }}</div>
        </div>
        <div class="history-container">
          <div class="item-row" v-for="item in versionOptions" :key="item.id">
            <div class="left-detail-row">
              <div class="version-info-row">
                <div
                  :class="
                    item.id == dataLatestId
                      ? ['version-label', 'active']
                      : ['version-label']
                  "
                >
                  {{ item.name }}
                </div>
                <div class="version-lasted" v-if="item.id == dataLatestId">
                  {{ $t("case.last_version") }}
                </div>
              </div>
              <div class="version-detail">
                <div class="creator">
                  {{ item.createUserName }} {{ $t("commons.create") }}
                </div>
              </div>
            </div>
            <div class="right-opt-row">
              <div
                class="updated opt-row"
                @click="setLatest(item)"
                v-if="
                  hasLatest &&
                  item.isCheckout &&
                  !(isRead || item.id === dataLatestId)
                "
              >
                {{ $t("case.set_new") }}
              </div>
              <div
                class="checkout opt-row"
                @click="checkout(item)"
                v-if="item.isCheckout && !item.isCurrent"
              >
                {{ $t("project.version.checkout") }}
              </div>
              <div
                class="create opt-row"
                v-if="!item.isCheckout && item.status === 'open' && !isRead"
                @click="create(item)"
              >
                {{ $t("commons.create") }}
              </div>
              <div
                class="delete opt-row"
                @click="del(item)"
                v-if="item.isCheckout && !(item.isCurrent || isRead)"
              >
                {{ $t("commons.delete") }}
              </div>
              <!-- <div
                @click="compare(item)"
                v-if="item.isCheckout && !item.isCurrent"
              >
                {{ $t("project.version.compare") }}
              </div> -->
            </div>
          </div>
        </div>
        <div class="compare-row" @click.stop="compareDialogVisible = true">
          <div class="icon">
            <img src="/assets/module/figma/icon_contrast_outlined.svg" alt="" />
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
          <el-select v-model="versionLeftId" size="small">
            <el-option
              v-for="item in versionLeftOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            ></el-option>
          </el-select>
        </div>
        <div class="desc">{{ $t("case.compare") }}</div>
        <div class="version-right-box">
          <el-select v-model="versionRightId" size="small">
            <el-option
              v-for="item in versionRightOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            ></el-option
          ></el-select>
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
import { getCurrentProjectID } from "metersphere-frontend/src/utils/token";
import { hasLicense } from "metersphere-frontend/src/utils/permission";
import {
  getProjectMembers,
  getProjectVersions,
  isProjectVersionEnable,
} from "metersphere-frontend/src/api/version";

export default {
  name: "CaseVersionHistory",
  props: {
    versionData: Array,
    currentId: String,
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
  },
  data() {
    return {
      loading: false,
      versionEnable: false,
      versionOptions: [],
      userData: {},
      currentVersion: {},
      dataLatestId: "",
      compareDialogVisible: false,
      // 版本对比相关
      versionLeftId: "",
      versionRightId: "",
    };
  },
  computed: {
    enableCompare() {
      return this.versionLeftId && this.versionRightId;
    },
    versionLeftOptions() {
      return this.versionOptions;
    },
    versionRightOptions() {
      return this.versionOptions;
    },
  },
  beforeDestroy() {
    this.clearSelectData();
  },
  methods: {
    closeCompareVersionDialog() {
      this.compareDialogVisible = false;
      this.clearSelectData();
    },
    clearSelectData() {
      //清空表单数据
      this.versionLeftId = "";
      this.versionRightId = "";
    },
    findVersionById(id) {
      let version = this.versionOptions.filter((v) => v.id === id);
      return Array.isArray(version) ? version[0] : version || {};
    },
    compareBranch() {
      this.$emit(
        "compareBranch",
        this.findVersionById(this.versionLeftId),
        this.findVersionById(this.versionRightId)
      );
      this.clearSelectData();
    },
    getVersionOptionList(callback) {
      getProjectVersions(this.currentProjectId).then((response) => {
        this.versionOptions = response.data.filter((v) => v.status === "open");
        if (callback) {
          callback(this.versionOptions);
        }
      });
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
      this.loading = true;
      this.$emit("checkout", row);
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
    handleVersionOptions() {
      let versionData = this.versionData;
      if (versionData.length === 0) {
        this.currentVersion =
          this.versionOptions.filter(
            (v) => v.status === "open" && v.latest
          )[0] || {};
        this.loading = false;
        return;
      }
      let latestData = versionData.filter((v) => v.latest === true);
      if (latestData) {
        this.dataLatestId = latestData[0].versionId;
      }
      this.versionOptions.forEach((version) => {
        let vs = versionData.filter((v) => v.versionId === version.id);
        version.isCheckout = vs.length > 0; // 已存在可以切换，不存在则创建
        if (version.isCheckout) {
          version.createUser =
            vs[0].createUser || vs[0].userId || vs[0].creator;
        } else {
          version.createUser = null;
        }
        let lastItem = versionData.filter((v) => v.id === this.currentId)[0];
        if (lastItem) {
          version.isCurrent = lastItem.versionId === version.id;
          if (version.isCurrent) {
            this.currentVersion = version;
          }
        }
      });
      this.loading = false;
    },
  },
  watch: {
    versionData() {
      if (!hasLicense()) {
        return;
      }
      isProjectVersionEnable(this.currentProjectId).then((response) => {
        this.versionEnable = response.data;
      });
      this.getUserOptions();
      this.getVersionOptionList(this.handleVersionOptions);
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
.version-history-wrap {
  width: 392px;
  height: 271px;
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
    height: 182px;
    overflow: scroll;
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
        justify-content: flex-end;
        align-items: flex-start;
        .opt-row:not(:first-child) {
          margin-left: 16px;
        }
        .opt-row {
          margin-top: 4px;
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
        }
        .updated {
        }

        .create {
        }

        .delete {
        }
      }
    }
  }
  .active {
    color: #783887;
  }
  .compare-row {
    cursor: pointer;
    display: flex;
    height: 32px;
    line-height: 32px;
    margin-bottom: 8px;
    margin-top: 3px;
    border-top: 1px solid rgba(31, 35, 41, 0.15);
    align-items: center;
    .icon {
      margin-left: 11.67px;
      margin-right: 4.6px;
      width: 14.67px;
      height: 13.33px;
      img {
        width: 100%;
        height: 100%;
      }
    }

    .label {
      font-size: 14px;
      color: #646a73;
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
    margin: 0 5px;
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
</style>
<style>
.version-popover {
  left: 215px !important;
  padding: 0px !important;
  height: 271px !important;
}
</style>
