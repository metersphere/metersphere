<template>
  <div class="comment-edit-box">
    <!-- 预览状态 -->
    <div class="state-preview-row" v-if="state === 'PREVIEW'">
      <ms-mark-down-text
        prop="richText"
        :data="tempFormData"
        :disabled="true"
      />
    </div>
    <!-- 编辑状态 -->
    <div class="state-edit-row" v-else-if="state === 'EDIT'">
      <el-form prop="richText">
        <form-rich-text-item
          class="remark-item"
          :data="tempFormData"
          :default-open="richTextDefaultOpen"
          prop="richText"
        />
        <div class="options">
          <div class="cancel" @click="cancel">
            <div class="text">{{ $t("commons.cancel") }}</div>
          </div>
          <div class="submit" @click="submit">
            <div class="text">{{ $t("case.make_comment") }}</div>
          </div>
        </div>
      </el-form>
    </div>
    <!-- 准备状态 -->
    <div class="state-ready-row" v-else>
      <div class="input-click-row" @click="toEdit">
        <div class="label">{{ $t("case.please_enter_comment") }}</div>
      </div>
    </div>
  </div>
</template>
<script>
import FormRichTextItem from "@/business/case/components/richtext/FormRichTextItem";
import MsMarkDownText from "@/business/case/components/richtext/MsMarkDownText";
export default {
  name: "CaseCommentEdit",
  components: {
    FormRichTextItem,
    MsMarkDownText,
  },
  props: {
    // READY - 准备
    // PREVIEW - 预览
    // EDIT - 编辑
    state: {
      type: String,
      default: "READY",
    },
    formData: {
      type: Object,
      default() {
        return {
          richText: "",
          prop: "richText",
        };
      },
    },
    readOnly: Boolean,
    richTextDefaultOpen: {
      type: String,
      default: "edit",
    },
  },
  data() {
    return {
      originText: "",
      tempFormData: {
        richText: "",
        prop: "richText",
      },
    };
  },
  watch: {
    formData: {
      deep: true,
      immediate: true,
      handler(v) {
        this.originText = v.richText || "";
        this.tempFormData.richText = v.richText || "";
      },
    },
  },
  methods: {
    cancel() {
      this.$emit("cancel", this.originText);
    },
    submit() {
      if (this.readOnly) {
        return;
      }
      this.$emit("submit", this.tempFormData.richText);
    },
    toEdit() {
      this.$emit("toEdit");
    },
  },
};
</script>
<style scoped lang="scss">
@import "@/business/style/index.scss";
.comment-edit-box {
  width: 100%;
  .state-preview-row {
    :deep(.v-show-content-html) {
      padding: 8px 12px !important;
    }
    :deep(.v-show-content) {
      padding: 8px 12px !important;
    }
    :deep(.v-note-wrapper) {
      box-shadow: none !important;
      background: #f5f6f7 !important;
      border-radius: 4px !important;
    }
  }

  .state-edit-row {
    .remark-item {
      :deep(.v-right-item) {
        display: none;
      }
      :deep(.v-note-wrapper:hover) {
        border: 1px solid #783887 !important;
      }
      :deep(.v-note-wrapper .v-note-op) {
        height: 34px !important;
        min-height: 34px !important;
        padding-top: 0px !important;
      }
      :deep(.v-note-edit) {
        height: 165px;
        overflow-y: scroll;
      }
      :deep(.v-note-show) {
        height: 165px;
        overflow-y: scroll;
      }
    }

    .options {
      display: flex;
      position: relative;
      top: -55px;
      width: auto;
      float: right;
      margin-right: 12px;
      .cancel {
        box-sizing: border-box;
        width: auto;
        height: 28px;
        line-height: 28px;
        background: #ffffff;
        border: 1px solid #bbbfc4;
        border-radius: 4px;
        cursor: pointer;
        color: #1f2329;
      }

      .submit {
        margin-left: 8px;
        width: auto;
        height: 28px;
        line-height: 28px;
        background: #783887;
        border-radius: 4px;
        cursor: pointer;
        color: #fff;
      }

      .text {
        height: 100%;
        width: auto;
        text-align: center;
        margin-left: 12px;
        margin-right: 12px;
      }
    }
  }

  .state-ready-row:hover {
    border: 1px solid #783887;
  }
  .state-ready-row {
    height: 40px;
    background: #ffffff;
    border: 1px solid #bbbfc4;
    border-radius: 4px;
    .input-click-row {
      width: 100%;
      height: 100%;
      .label {
        font-family: "PingFang SC";
        font-style: normal;
        font-weight: 400;
        font-size: 14px;
        line-height: 40px;
        color: #8f959e;
        margin-left: 12px;
      }
    }
  }
}
</style>
