<template>
  <div class="edit-container">
    <div
      class="content"
      v-click-outside="preSave"
      @mouseleave="mouseLeaveEvent"
    >
      <div class="edit" v-show="edit">
        <el-form
          :rules="rules"
          :model="model"
          ref="customForm"
          v-if="!editable"
        >
          <slot
            name="content"
            :onClick="clickContent"
            :hoverEditable="hoverEditable"
          ></slot>
        </el-form>
        <slot
          name="content"
          :onClick="clickContent"
          :hoverEditable="hoverEditable"
          v-if="editable"
        ></slot>
      </div>
      <div class="readonly" v-show="!edit">
        <div
          class="text"
          v-if="contentObject.content && contentObject.contentType == 'TEXT'"
          @click="handleReadTextClick"
        >
          {{ contentObject.content }}
        </div>
        <div
          class="select"
          v-else-if="
            contentObject.content && contentObject.contentType == 'INPUT'
          "
          @click="handleReadTextClick"
          @mouseenter="mouseEnterEvent"
        >
          {{ contentObject.content }}
        </div>
        <div
          class="tag-wrap"
          v-else-if="
            contentObject.content &&
            contentObject.contentType == 'TAG' &&
            Array.isArray(contentObject.content) &&
            contentObject.content.length > 0
          "
          @click="handleReadTextClick"
        >
          <div class="tag-box">
            <div
              v-for="(item, index) in contentObject.content"
              :key="index"
              class="tag-row"
            >
              {{ item }}
            </div>
          </div>
        </div>
        <div
          class="story-wrap"
          v-else-if="
            contentObject.content &&
            contentObject.content.demandId &&
            contentObject.contentType == 'STORY'
          "
          @click="handleReadTextClick"
        >
          <div class="story-box">
            <div class="platform">{{ getStoryPlatform() }}</div>
            <div class="story-label">{{ getStoryLabel() }}</div>
          </div>
        </div>
        <div
          class="select"
          v-else-if="
            contentObject.content && contentObject.contentType == 'SELECT'
          "
          @mouseenter="mouseEnterEvent"
        >
          {{ contentObject.content }}
        </div>
        <div
          class="select"
          v-else-if="
            contentObject.content && contentObject.contentType == 'RICHTEXT'
          "
          @click="handleReadTextClick"
        >
          {{ contentObject.content }}
        </div>
        <div
          v-else-if="
            contentObject.content && contentObject.contentType == 'CUSTOM'
          "
          :class="getCustomComponentType()"
          @click="handleReadTextClick"
          @mouseenter="mouseEnterEvent"
        >
          {{ getCustomText() }}
        </div>
        <div class="empty" v-else @click="handleReadTextClick">
          {{ $t("case.none") }}
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import { getProjectMemberOption } from "metersphere-frontend/src/api/user";
export default {
  name: "BaseEditItemComponent",
  data() {
    return {
      selfEditable: false,
      hoverEditable: false,
      memberOptions: [],
    };
  },
  props: {
    rules: Object,
    prop: String,
    model: Object,
    editable: {
      type: Boolean,
      default: true,
    },
    autoSave: {
      type: Boolean,
      default: true,
    },
    contentObject: {
      type: Object,
      default() {
        return { prefix: "", content: "", suffix: "", contentType: "TEXT" };
      },
    },
    readonlyHoverEvent: {
      type: Boolean,
      default: false,
    },
    contentClickEvent: {
      type: Boolean,
      default: true,
    },
  },
  computed: {
    edit() {
      return this.editable || this.selfEditable || this.hoverEditable;
    },
  },
  mounted() {
    if (
      this.contentObject.content &&
      this.contentObject.content.type &&
      (this.contentObject.content.type === "member" ||
        this.contentObject.content.type === "multipleMember")
    ) {
      this.getMemberOptions();
    }
  },
  methods: {
    getCustomComponentType() {
      let type = "select";
      let curType = this.contentObject.content.type || "";
      //select
      switch (curType) {
        case "select":
        case "multipleSelect":
        case "cascadingSelect":
        case "member":
        case "multipleMember":
        case "input":
          type = "select";
          break;
        case "text":
          type = "text";
          break;
        case "richText":
          type = "text";
          break;
        default:
          type = "select";
          break;
      }
      return type;
    },
    getMemberOptions() {
      getProjectMemberOption().then((r) => {
        let tempMemberOptions = r.data || [];
        let tempArr = [];
        tempMemberOptions.forEach((e) => {
          tempArr.push({
            value: e.id,
            text: e.name,
          });
        });
        this.memberOptions = tempArr;
      });
    },
    getTranslateOption(item) {
      return item.system ? this.$t(item.text) : item.text;
    },
    getCustomText() {
      let options = this.contentObject.content.options || [];
      if (
        this.contentObject.content.type &&
        (this.contentObject.content.type === "member" ||
          this.contentObject.content.type === "multipleMember")
      ) {
        options = this.memberOptions;
      }
      if (options.length > 0) {
        let option = options.find((item) => {
          if (this.contentObject.content.value) {
            return item.value == this.contentObject.content.value;
          }
          return item.value == this.contentObject.content.defaultValue;
        });
        if (option) {
          return this.getTranslateOption(option);
        }
      }

      if (
        this.contentObject.content.type === "input" ||
        this.contentObject.content.type === "richText"
      ) {
        return this.contentObject.content.defaultValue === "" ||
          this.contentObject.content.defaultValue == null
          ? this.$t("case.none")
          : this.contentObject.content.defaultValue;
      }
      return "";
    },
    getStoryPlatform() {
      let demandOptions = this.contentObject.content.demandOptions || [];
      if (demandOptions.length > 0) {
        let demand = demandOptions.find((item) => {
          return item.value === this.contentObject.content.demandId;
        });
        if (demand) {
          return demand.platform;
        }
      }
      return "";
    },
    getStoryLabel() {
      let demandOptions = this.contentObject.content.demandOptions || [];
      if (demandOptions.length > 0) {
        let demand = demandOptions.find((item) => {
          return item.value === this.contentObject.content.demandId;
        });
        if (demand) {
          if (demand.value === "other") {
            return this.$t("test_track.case.other");
          }
          return demand.label;
        }
      }
      return "";
    },
    clickContent() {
      if (this.contentClickEvent) {
        this.selfEditable = true;
        this.hoverEditable = false;
      }
    },
    preSave() {
      if (!this.autoSave || this.editable) {
        return;
      }
      this.preProcessor();
      // this.$emit("validateForm", (val) => {
      //   if (val) {
      //     this.doSave();
      //   }
      // });
    },
    preProcessor(ignoreAutoSaveProp) {
      if (this.editable) {
        return;
      }
      //先校验
      let isValidate = false;
      if (this.$refs.customForm) {
        this.$refs.customForm.validate((val) => {
          isValidate = true;
          if (val) {
            this.doSave(ignoreAutoSaveProp);
            return;
          }
        });
      }

      if (!this.model || !this.rules || !isValidate) {
        this.doSave(ignoreAutoSaveProp);
      }
    },
    postProcessor() {
      if (this.selfEditable) {
        this.selfEditable = false;
        this.hoverEditable = false;
      }
    },
    doSave(ignoreAutoSaveProp) {
      //准备保存
      if ((ignoreAutoSaveProp || this.autoSave) && this.selfEditable) {
        this.postProcessor();
        this.$EventBus.$emit("handleSaveCaseWithEvent", this.form);
      }
    },
    mouseLeaveEvent() {
      if (this.readonlyHoverEvent) {
        this.hoverEditable = false;
      }
    },
    mouseEnterEvent() {
      if (this.readonlyHoverEvent) {
        this.hoverEditable = true;
      }
    },
    changeHoverEditable(edit) {
      this.hoverEditable = edit;
    },
    changeSelfEditable(edit) {
      this.selfEditable = edit;
    },
    handleReadTextClick() {
      if (!this.autoSave) {
        this.selfEditable = false;
      } else {
        this.selfEditable = true;
      }
    },
    handleReadTextHover() {
      this.selfEditable = true;
    },
  },
};
</script>
<style scoped lang="scss">
@import "@/business/style/index.scss";
.text {
  font-family: "PingFang SC";
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #1f2329;
}
.empty {
  font-family: "PingFang SC";
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #8f959e;
}
.select {
  width: 100%;
  height: auto;
  line-height: 32px;
  font-weight: 400;
  font-size: 14px;
  color: #1f2329;
}
// .select:hover {
//   background: rgba(31, 35, 41, 0.1);
//   border-radius: 4px;
//   cursor: pointer;
// }
.story-wrap {
  .story-box {
    display: flex;
    flex-flow: row wrap;
    width: px2rem(256);
    height: auto;
    .platform {
      display: flex;
      flex-direction: row;
      align-items: center;
      padding: 1px 6px;
      gap: 4px;
      width: 45px;
      height: 24px;
      line-height: 24px;
      text-align: center;
      background: rgba(120, 56, 135, 0.2);
      border-radius: 2px;
      color: #783887;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .story-label {
      margin-left: 8px;
      font-weight: 400;
      font-size: 14px;
      line-height: 22px;
      color: #1f2329;
    }
  }
}
.tag-wrap {
  .tag-box {
    display: flex;
    flex-flow: row wrap;
    width: px2rem(256);
    height: auto;
    .tag-row {
      margin-right: 4px;
      margin-bottom: 4px;
      padding: 1px 6px;
      gap: 4px;
      width: 82px;
      height: 24px;
      line-height: 24px;
      text-align: center;
      background: rgba(120, 56, 135, 0.2);
      border-radius: 2px;
      color: #783887;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}
.edit-container {
  width: 100%;
  .content {
    width: 100%;
    .edit {
      width: 100%;
    }
    .readonly {
      width: 100%;
    }
  }
  .footer {
    width: 100%;
    .footer-row {
      margin-top: 8px;
      display: flex;
      justify-content: flex-end;
      .save {
        margin-left: 12px;
        color: #fff !important;
        el-button {
        }
      }
      .save:hover {
        color: #783887 !important;
      }

      .cancel {
        el-button {
        }
      }
    }
  }
}
</style>
