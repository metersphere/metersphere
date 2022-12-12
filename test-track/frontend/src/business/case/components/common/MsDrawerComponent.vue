<template>
  <el-drawer
    :close-on-click-modal="false"
    :visible.sync="visible"
    :size="1100"
    @close="close"
    destroy-on-close
    ref="editFile"
    custom-class="file-drawer"
    append-to-body
  >
    <template slot="title">
      <div style="color: #1f2329; font-size: 16px; font-weight: 500">
        {{ title }}
      </div>
    </template>
    <div class="file-box" v-if="!emptyObj.isEmpty">
      <div class="header">
        <slot name="header"></slot>
      </div>
      <div class="content">
        <slot name="content"></slot>
      </div>
      <div class="footer">
        <div class="pagination" v-if="enablePagination">
          <slot name="pagination"></slot>
        </div>
        <slot name="footer"></slot>
        <div class="options">
          <div class="options-btn">
            <div class="check-row" v-if="selectCounts > 0">
              <div class="label">{{$t('case.selected')}} {{ selectCounts }} {{$t('case.strip')}}</div>
              <div class="clear" @click="clearSelect">{{$t('case.clear')}}</div>
            </div>
            <div class="cancel">
              <el-button size="small" @click="visible = false">{{
                $t("commons.cancel")
              }}</el-button>
            </div>
            <div
              class="cancel"
              v-if="enableSaveAndReset"
              style="margin-left: 12px"
            >
              <el-button size="small" @click="saveAndReset">{{
                $t("case.saveAndCreate")
              }}</el-button>
            </div>
            <div class="submit">
              <el-button
                size="small"
                v-prevent-re-click
                :type="selectCounts > 0 || !enableSelect ? 'primary' : 'info'"
                @click="submit"
                @keydown.enter.native.prevent
              >
                {{ $t("commons.confirm") }}
              </el-button>
            </div>
          </div>
        </div>
        <!-- <div class="options">
          <div class="options-btn">
            <div class="cancel">
              <el-button size="small" @click="visible = false">{{
                $t("commons.cancel")
              }}</el-button>
            </div>
            <div class="submit">
              <el-button
                size="small"
                v-prevent-re-click
                :type="confirmBtnType"
                @click="submit"
                @keydown.enter.native.prevent
              >
                {{ $t("commons.confirm") }}
              </el-button>
            </div>
          </div>
        </div> -->
      </div>
    </div>
    <div class="empty-file" v-if="emptyObj.isEmpty">
      <div class="info-wrap" style="text-align: center; margin-top: 226px">
        <div class="image">
          <img
            style="height: 100px; width: 100px; margin-bottom: 8px"
            :src="emptyObj.imgUrl"
          />
          <div class="label" style="color: #646a73">{{ emptyObj.label }}</div>
        </div>
        <div class="upload-file" style="margin-top: 16px">
          <slot name="emptyBtn"></slot>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script>
import {
  getCurrentProjectID,
  getCurrentUserId,
} from "metersphere-frontend/src/utils/token";
export default {
  name: "MsDrawerComponent",
  components: {},
  props: {
    title: String,
    enablePagination: {
      type: Boolean,
      default: true,
    },
    confirmBtnType: {
      type: String,
      default: "primary",
    },
    enableSelect: {
      type: Boolean,
      default: true,
    },
    emptyObj: {
      type: Object,
      default() {
        return {
          isEmpty: false,
          imgUrl: "/assets/module/figma/icon_none.svg",
          label: "暂无数据",
        };
      },
    },
    enableSaveAndReset: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      loadFileVisible: false,
      result: false,
      data: {},
      condition: {},
      projectId: getCurrentProjectID(),
      height: "500px",
      typeFilters: [],
      showView: "list",
      visible: false,
      selectCounts: null,
    };
  },
  methods: {
    close() {
      this.visible = false;
    },
    open() {
      this.selectCounts = null;
      this.visible = true;
    },
    submit() {
      if (!this.selectCounts && this.enableSelect) {
        return;
      }
      this.$emit("confirm");
    },
    clearSelect() {
      this.$emit("clearSelect");
    },
    saveAndReset() {
      this.$emit("saveAndReset");
    },
  },
};
</script>

<style scoped>
.oneLine {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.ms-tags {
  margin-left: 0px;
  margin-right: 2px;
}
</style>

<style lang="scss">
@import "@/business/style/index.scss";
.file-drawer .el-drawer__header {
  height: 40px !important;
  margin: 0 !important;
  padding: 16px 24px 0 24px !important;
  border-bottom: 1px solid rgba(31, 35, 41, 0.15);
}
.file-drawer .header {
  margin-top: 24px;
  margin-bottom: 16px;
}
.file-drawer .input-row {
  width: 100%;
}
.file-drawer .file-box .both-padding {
  padding: 0 24px;
}
.file-drawer .file-box .footer {
  position: absolute;
  bottom: 0;
  width: 100%;
}
.file-drawer .file-box .footer .pagination {
  height: 68px;
  //border-top: 1px solid rgba(31, 35, 41, 0.15);
  margin: 0 24px;
  padding-top: 6px;
}
.file-drawer .file-box .footer .options {
  height: 80px;
  background: #ffffff;
  box-shadow: 0px -1px 4px rgba(31, 35, 41, 0.1);
  overflow: hidden;
}
.file-drawer .file-box .footer .options-btn {
  display: flex;
  margin-top: 24px;
  height: 32px;
  margin-right: 24px;
  float: right;
}
.file-drawer .file-box .footer .options-btn .submit {
  margin-left: 12px;
}
.file-drawer .file-box .check-row {
  display: flex;
  line-height: 32px;
}
.file-drawer .file-box .check-row .label {
  font-family: "PingFang SC";
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  text-align: center;
  color: #646a73;
}
.file-drawer .file-box .check-row .clear {
  font-family: "PingFang SC";
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  text-align: center;
  color: #783887;
  cursor: pointer;
  margin-left: 16px;
  margin-right: 16px;
}

.file-drawer .table-data .ms-table {
  height: px2rem(519) !important;
  max-height: px2rem(519) !important;
}
.empty-file {
  height: 100%;
  width: 100%;
}
</style>
<style scoped lang="scss">
.options {
  height: 80px;
  background: #ffffff;
  box-shadow: 0px -1px 4px rgba(31, 35, 41, 0.1);
  overflow: hidden;
}
.options-btn {
  display: flex;
  margin-top: 24px;
  height: 32px;
  margin-right: 24px;
  float: right;
}
.options-btn .submit {
  margin-left: 12px;
}
.check-row {
  display: flex;
  line-height: 32px;
}
.check-row .label {
  font-family: "PingFang SC";
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  text-align: center;
  color: #646a73;
}
.check-row .clear {
  font-family: "PingFang SC";
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  text-align: center;
  color: #783887;
  cursor: pointer;
  margin-left: 16px;
  margin-right: 16px;
}
</style>
