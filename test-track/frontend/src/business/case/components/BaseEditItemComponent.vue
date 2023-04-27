<template>
  <div class="edit-container">
    <div
      class="content"
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
          v-if="contentObject.content && contentObject.contentType === 'TEXT'"
          @click="handleReadTextClick"
        >
          <el-tooltip :content="contentObject.content" placement="top" effect="dark">
            <span>{{ contentObject.content }}</span>
          </el-tooltip>
        </div>
        <div
          class="select"
          v-else-if="
            contentObject.content && contentObject.contentType === 'INPUT'
          "
          @click="handleReadTextClick"
          @mouseenter="mouseEnterEvent"
        >
          <el-tooltip :content="contentObject.content" placement="top" effect="dark">
            <span>{{ contentObject.content }}</span>
          </el-tooltip>
        </div>
        <div
          class="tag-wrap"
          v-else-if="
            contentObject.content &&
            contentObject.contentType === 'TAG' &&
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
              <el-tooltip :content="item" placement="top" effect="dark">
                <span>{{ item }}</span>
              </el-tooltip>
            </div>
          </div>
        </div>
        <div
          class="story-wrap"
          v-else-if="
            contentObject.content &&
            contentObject.content.demandId &&
            contentObject.contentType === 'STORY'
          "
          @click="handleReadTextClick"
        >
          <div class="story-box">
            <div class="platform">{{ getStoryPlatform() }}</div>
            <el-tooltip :content="getStoryLabel()" placement="top" effect="dark">
              <div class="story-label text-ellipsis">{{ getStoryLabel() }}</div>
            </el-tooltip>
          </div>
        </div>
        <div
          class="select"
          v-else-if="
            contentObject.content && contentObject.contentType === 'SELECT'
          "
          @mouseenter="mouseEnterEvent"
        >
          {{ contentObject.content }}
        </div>
        <div
          class="select"
          v-else-if="
            contentObject.content && contentObject.contentType === 'RICHTEXT'
          "
          @click="handleReadTextClick"
        >
          <ms-mark-down-text
            v-if="contentObject.contentType === 'RICHTEXT'"
            class="rich-text"
            prop="content"
            :disabled="true"
            :data="contentObject"/>
        </div>
        <div
          id="custom-div"
          v-else-if="contentObject.content && contentObject.contentType === 'CUSTOM'"
          :class="getCustomComponentClass"
          @click="handleReadTextClick"
          @mouseenter="mouseEnterEvent"
        >
          <span v-if="contentObject.content.type !== 'richText'">
            {{ getCustomText() }}
          </span>
          <span v-else>
             <ms-mark-down-text
               class="rich-text"
               prop="defaultValue"
               :disabled="true"
               :data="contentObject.content"/>
          </span>
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
import MsMarkDownText from "@/business/case/components/richtext/MsMarkDownText";
import {getProjectMemberById} from "@/api/user";
export default {
  name: "BaseEditItemComponent",
  components: {MsMarkDownText},
  data() {
    return {
      selfEditable: false,
      hoverEditable: false,
      memberOptions: [],
      isCustomNone: false,
      optionPlatform: '',
      optionPlatformValue: ''
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
    projectId: String
  },
  computed: {
    edit() {
      return this.editable;
    },
    getCustomComponentClass() {
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
      if (this.isCustomNone) {
        type = type + ' custom-empty';
      }
      return type;
    }
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
    getMemberOptions() {
      if (this.projectId) {
        getProjectMemberById(this.projectId)
          .then(r => this.handleMemberOptions(r.data));
      } else {
        getProjectMemberOption()
          .then(r => this.handleMemberOptions(r.data));
      }
    },
    handleMemberOptions(data) {
      let tempMemberOptions = data || [];
      let tempArr = [];
      tempMemberOptions.forEach((e) => {
        tempArr.push({
          value: e.id,
          text: e.name,
        });
      });
      this.memberOptions = tempArr;
    },
    getTranslateOption(item) {
      if (!item) {
        return '';
      }
      if (Array.isArray(item)) {
        let arr = [];
        item.forEach((v) => {
          arr.push(v.system ? this.$t(v.text) : v.text);
        });
        return arr.join(" ");
      }
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
      if (options && options.length > 0) {
        let tempValue = this.contentObject.content.value && this.contentObject.content.value.length > 0
          ? this.contentObject.content.value
          : this.contentObject.content.defaultValue;
        if(!tempValue || Array.isArray(tempValue) && tempValue.length <= 0){
          let customVal;
          if (this.model) {
            customVal = this.model[this.contentObject.content.name];
          }
          if (customVal) {
            this.isCustomNone = false;
            tempValue = customVal;
          } else {
            this.isCustomNone = true;
            return this.$t("case.none");
          }
        } else {
          this.isCustomNone = false;
        }
        if (Array.isArray(tempValue) && tempValue.length > 0) {
          let arr = [];
          tempValue.forEach((v) => {
            let temp = options.find((o) => {
                return o.value == v;
              });
            if(temp){
              if(Array.isArray(temp)){
                arr.push(...temp);
              }
              else{
                arr.push(temp);
              }
            }
          });
          return this.getTranslateOption(arr);
        } else {
          let temp = options.find((o) => {
                return o.value == tempValue;
              });
          return this.getTranslateOption(temp);
        }
      }

      if (['input', 'richText', 'textarea'].indexOf(this.contentObject.content.type) > -1) {
        if (!this.contentObject.content.defaultValue) {
          this.isCustomNone = true;
          return this.$t("case.none");
        } else {
          this.isCustomNone = false;
          return this.contentObject.content.defaultValue;
        }
      } else if (['multipleInput'].indexOf(this.contentObject.content.type) > -1) {
        if (this.contentObject.content.defaultValue && this.contentObject.content.defaultValue instanceof Array) {
          return this.contentObject.content.defaultValue.join(' ');
        }
      }

      if (!this.contentObject.content.defaultValue) {
        this.isCustomNone = true;
        return this.$t("case.none");
      } else {
        this.isCustomNone = false;
        return this.contentObject.content.defaultValue;
      }
    },
    getStoryPlatform(demandOptions) {
      if (!demandOptions) {
        demandOptions = this.contentObject.content.demandOptions || [];
      }
      if (demandOptions.length > 0) {
        for (let i = 0; i < demandOptions.length; i++) {
          if (demandOptions[i].value === this.contentObject.content.demandId) {
            this.optionPlatform = this.handleDemandOptionPlatform(demandOptions[i]);
            break;
          }
          if (demandOptions[i].children && demandOptions[i].children.length > 0) {
            this.optionPlatform = this.getStoryPlatform(
              demandOptions[i].children,
              this.contentObject.content.demandId,
            );
          }
        }
      }
      return this.optionPlatform;
    },
    getStoryLabel(demandOptions) {
      if (!demandOptions) {
        demandOptions = this.contentObject.content.demandOptions || [];
      }
      if (demandOptions.length > 0) {
        if (this.contentObject.content.demandId === 'other') {
          this.optionPlatformValue = this.$t("test_track.case.other");
          return this.optionPlatformValue;
        }
        for (let i = 0; i < demandOptions.length; i++) {
          if (demandOptions[i].value === this.contentObject.content.demandId) {
            this.optionPlatformValue = this.handleDemandOptionLabel(demandOptions[i]);
            break;
          }
          if (demandOptions[i].children && demandOptions[i].children.length > 0) {
            this.optionPlatformValue = this.getStoryLabel(
              demandOptions[i].children,
              this.contentObject.content.demandId,
            );
          }
        }
      }
      return this.optionPlatformValue;
    },
    handleDemandOptionPlatform(data) {
      if (data.platform) {
        return data.platform;
      }
      if (data.label) {
        let arr = data.label.split(": ");
        if (arr && arr.length > 1) {
          return arr[0];
        }
      }
      return "";
    },
    handleDemandOptionLabel(data) {
      if (data.label) {
        let arr = data.label.split(": ");
        if (arr && arr.length > 1) {
          return arr[1];
        }
        return data.label;
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

.rich-text {
  border: 0px !important;
  box-sizing: border-box;
  border-radius: 4px;
  box-shadow: none !important;
  padding: 0 !important;
}

.rich-text :deep(.v-show-content) {
  background-color: #fff !important;
  padding: 0 !important;
}

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
.custom-empty {
  font-family: "PingFang SC" !important;
  font-style: normal !important;
  font-weight: 400 !important;
  font-size: 14px !important;
  line-height: 22px !important;
  color: #8f959e !important;
}
.select {
  width: 100%;
  max-height: px2rem(128);
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
      min-width: 45px;
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
    .text-ellipsis {
      text-overflow: ellipsis;
      overflow-x: hidden;
      white-space: nowrap;
      max-width: 190px;
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
