<template>
  <div>
    <!-- 基于form组件进行表单验证 -->
    <el-form ref="caseDetailForm" :rules="rules" :model="form">
      <!-- case name -->
      <div class="case-name-row" v-on:keydown.enter.prevent v-if="editable">
        <div class="case-name case-title-wrap">
          <div class="name title-wrap">{{ $t("case.case_name") }}</div>
          <div class="required required-item"></div>
        </div>
        <div class="content-wrap">
          <div class="opt-row">
            <base-edit-item-component
              :editable="editable"
              :auto-save="!readOnly"
              trigger="click"
              :contentObject="{ content: form.name, contentType: 'TEXT' }"
              :model="form"
              :rules="rules"
              ref="nameRef"
            >
              <template slot="content" slot-scope="val">
                <el-form-item prop="name">
                  <el-input
                    size="small"
                    :placeholder="$t('case.please_enter_the_case_name')"
                    v-model="form.name"
                    :disabled="readOnly"
                    @blur="textBlur(val, 'nameRef')"
                    :maxlength="255"
                    show-word-limit
                  ></el-input>
                </el-form-item>
              </template>
            </base-edit-item-component>
          </div>
        </div>
      </div>

      <!-- pre condition -->
      <div class="pre-condition-row">
        <div class="condition-name case-title-wrap">
          <div class="name title-wrap">{{ $t("case.preconditions") }}</div>
        </div>
        <div class="content-wrap">
          <div class="opt-row">
            <base-edit-item-component
              :editable="editable"
              :auto-save="!readOnly"
              trigger="click"
              :contentObject="{
                content: form.prerequisite,
                contentType: 'RICHTEXT',
              }"
            >
              <template v-slot:content="{ onClick }">
                <div @click="onClick">
                  <form-rich-text-item
                    :disabled="readOnly"
                    :data="form"
                    :default-open="richTextDefaultOpen"
                    :placeholder="$t('case.please_enter_preconditions')"
                    prop="prerequisite"
                  />
                </div>
              </template>
            </base-edit-item-component>
          </div>
        </div>
      </div>

      <!-- step description -->
      <div class="step-desc-row">
        <!-- 类型切换 -->
        <div class="step-desc-name case-title-wrap">
          <div class="name title-wrap">
            {{
              form.stepModel === "TEXT"
                ? $t("test_track.case.text_describe")
                : $t("test_track.case.step_describe")
            }}
          </div>
          <div class="split title-wrap">|</div>
          <div class="update-type-row title-wrap">
            <!-- <div class="label">更改类型</div>
            <div class="icon">
              <i class="el-icon-arrow-down"></i>
            </div> -->
            <step-change-item :label-width="formLabelWidth" :form="form" />
          </div>
        </div>
        <!-- 文本描述 -->
        <div class="content-wrap">
          <div class="opt-row">
            <base-edit-item-component
              v-if="form.stepModel === 'TEXT'"
              :editable="editable"
              :auto-save="!readOnly"
              trigger="click"
              :contentObject="{
                content: form.stepDescription,
                contentType: 'RICHTEXT',
              }"
            >
              <template v-slot:content="{ onClick }">
                <div @click="onClick">
                  <form-rich-text-item
                    prop="stepDescription"
                    :disabled="readOnly"
                    :data="form"
                    :default-open="richTextDefaultOpen"
                    :placeholder="$t('case.please_enter_a_text_description')"
                  />
                </div>
              </template>
            </base-edit-item-component>

            <!-- 步骤描述 -->
            <test-case-step-item
              v-if="form.stepModel === 'STEP' || !form.stepModel"
              :form="form"
              :editable="editable"
              :read-only="readOnly"
            />
          </div>
        </div>
      </div>

      <!-- expect -->
      <div class="expect-row" v-if="form.stepModel === 'TEXT'">
        <div class="expect-name case-title-wrap">
          <div class="name title-wrap">
            {{ $t("test_track.case.expected_results") }}
          </div>
        </div>
        <div class="content-wrap">
          <div class="opt-row">
            <base-edit-item-component
              :editable="editable"
              trigger="click"
              :auto-save="!readOnly"
              :contentObject="{
                content: form.expectedResult,
                contentType: 'RICHTEXT',
              }"
            >
              <template v-slot:content="{ onClick }">
                <div @click="onClick">
                  <form-rich-text-item
                    v-if="form.stepModel === 'TEXT'"
                    prop="expectedResult"
                    :placeholder="$t('case.please_enter_expected_results')"
                    :disabled="readOnly"
                    :data="form"
                    :default-open="richTextDefaultOpen"
                  />
                </div>
              </template>
            </base-edit-item-component>
          </div>
        </div>
      </div>

      <!-- remark -->
      <div class="remark-row">
        <div class="remark-name case-title-wrap">
          <div class="name title-wrap">{{ $t("commons.remark") }}</div>
        </div>
        <div class="content-wrap">
          <div class="opt-row">
            <base-edit-item-component
              :editable="editable"
              :auto-save="!readOnly"
              trigger="click"
              :contentObject="{
                content: form.remark,
                contentType: 'RICHTEXT',
              }"
            >
              <template v-slot:content="{ onClick }">
                <div @click="onClick">
                  <form-rich-text-item
                    class="remark-item"
                    :disabled="readOnly"
                    :placeholder="$t('case.please_enter_comments')"
                    :data="form"
                    :default-open="richTextDefaultOpen"
                    prop="remark"
                  />
                </div>
              </template>
            </base-edit-item-component>
          </div>
        </div>
      </div>

      <!-- 附件 -->
      <div class="attachment-row" v-if="!editableState">
        <div class="attachment-name case-title-wrap">
          <div class="name title-wrap">{{ $t("case.attachment") }}</div>
        </div>
        <div class="content-wrap">
          <!-- 添加附件 -->
          <!-- tip -->
          <div class="opt-btn">
            <case-attachment-component
              :caseId="caseId"
              :editable="editable"
              :type="type"
              :isCopy="isCopy"
              :copyCaseId="copyCaseId"
              :readOnly="(readOnly && editable) || isPublicShow || isReadonlyUser"
              :projectId="projectId"
              :isClickAttachmentTab="isClickAttachmentTab"
              :isDelete="!isTestPlan"
              ref="attachmentComp"
            ></case-attachment-component>
          </div>
        </div>
      </div>
    </el-form>
  </div>
</template>
<script>
import FormRichTextItem from "@/business/case/components/richtext/FormRichTextItem";
import BaseEditItemComponent from "../BaseEditItemComponent";
import TestCaseStepItem from "@/business/case/components/case/CaseStepItem";
import StepChangeItem from "@/business/case/components/case/CaseStepChange";
import CaseAttachmentComponent from "@/business/case/components/case/CaseAttachmentComponent";
import MsFileBatchMove from "metersphere-frontend/src/components/environment/commons/variable/FileBatchMove";

export default {
  name: "CaseDetailComponent",
  components: {
    FormRichTextItem,
    BaseEditItemComponent,
    TestCaseStepItem,
    StepChangeItem,
    CaseAttachmentComponent,
  },
  data() {
    return {
      rules: {
        name: [
          {
            required: true,
            message: this.$t("test_track.case.input_name"),
            trigger: "blur",
          },
          {
            max: 255,
            message: this.$t("test_track.length_less_than") + "255",
            trigger: "blur",
          },
        ],
      },
    };
  },
  props: {
    editable: Boolean,
    form: Object,
    readOnly: Boolean,
    richTextDefaultOpen: String,
    formLabelWidth: String,
    isClickAttachmentTab: Boolean,
    isTestPlan: Boolean,
    type: String,
    caseId: String,
    projectId: String,
    copyCaseId: String,
    isCopy: Boolean,
    editableState: Boolean,
    isPublicShow: Boolean,
    isReadonlyUser: Boolean
  },
  methods: {
    getUploadFiles() {
      if (this.$refs.attachmentComp) {
        return this.$refs.attachmentComp.uploadFiles;
      } else {
        return [];
      }
    },
    getRelateFiles() {
      if (this.$refs.attachmentComp) {
        return this.$refs.attachmentComp.relateFiles;
      } else {
        return [];
      }
    },
    getUnRelateFiles() {
      if (this.$refs.attachmentComp) {
        return this.$refs.attachmentComp.unRelateFiles;
      } else {
        return [];
      }
    },
    getFilterCopyFiles() {
      if (this.$refs.attachmentComp) {
        return this.$refs.attachmentComp.filterCopyFiles;
      } else {
        return [];
      }
    },
    textBlur(options, refName) {
      if (!this.editable && options.autoSave) {
        options.editFactor = false;
        this.$refs[refName].changeSelfEditable(false);
        this.handleSaveEvent();
      }
    },
    handleSaveEvent() {
      //触发保存 TODO
    },
    valideForm() {
      let isValidate = true;
      this.$refs["caseDetailForm"].validate((valid) => {
        if (!valid) {
          isValidate = false;
        }
      });
      return isValidate;
    },
    getFileMetaData(id) {
      if (this.$refs.attachmentComp) {
        this.$refs.attachmentComp.getFileMetaData(id);
      }
    },
  },
};
</script>

<style scoped lang="scss">
@import "@/business/style/index.scss";
.selectHover {
  // background: rgba(31, 35, 41, 0.1);
  border-radius: 4px;
  cursor: pointer;
  :deep(.el-select) {
    // background-color: rgba(31, 35, 41, 0.1) !important;
    border: none !important;
  }
  :deep(.el-input__inner) {
    border: none !important;
    background-color: rgba(31, 35, 41, 0.1) !important;
    color: #1f2329;
  }
}
.case-edit-wrap {
  :deep(.el-form-item__content) {
    line-height: px2rem(32);
  }
  .case-edit-box {
    width: px2rem(1328);
    min-height: px2rem(1001);
    margin-left: px2rem(34);
    background-color: #fff;
    .edit-header-container {
      height: px2rem(56);
      width: 100%;
      border-bottom: 1px solid rgba(31, 35, 41, 0.15);
      display: flex;
      align-items: center;
      .header-content-row {
        display: flex;
        .back {
          margin-left: px2rem(24);
          width: px2rem(20);
          height: px2rem(20);
          img {
            width: 100%;
            height: 100%;
          }
        }

        .case-name {
          width: px2rem(64);
          height: px2rem(24);
          font-size: 16px;
          font-family: "PingFang SC";
          font-style: normal;
          font-weight: 500;
          line-height: px2rem(24);
          color: #1f2329;
          order: 1;
          flex-grow: 0;
          margin-left: px2rem(8);
        }

        .case-edit {
          .case-level {
          }

          .case-version {
            .version-icon {
            }

            .version-title {
            }
          }
        }
      }
    }

    .edit-content-container {
      width: 100%;
      height: 100%;
      display: flex;
      background-color: #fff;
      .required-item:after {
        content: "*";
        color: #f54a45;
        margin-left: px2rem(4);
        width: px2rem(8);
        height: 22px;
        font-weight: 400;
        font-size: 14px;
        line-height: 22px;
      }
      .content-body-wrap {
        // 1024 减去左右padding 各24 和 1px右边框
        width: px2rem(1024);
        height: 100%;
        .case-title-wrap {
          display: flex;
          margin-top: px2rem(24);
          margin-bottom: px2rem(8);
          .title-wrap {
            height: 22px;
            font-family: "PingFang SC";
            font-style: normal;
            font-weight: 600;
            font-size: 14px;
            line-height: 22px;
            color: #1f2329;
          }
        }

        //公共样式
        .content-wrap {
          :deep(.v-note-op) {
            background-color: #f8f9fa !important;
            border-bottom: 1px solid #bbbfc4;
          }
          :deep(.v-note-wrapper) {
            box-sizing: border-box;
            border-radius: 4px;
            box-shadow: none !important;
          }
          :deep(.content-input-wrapper){
            width: 100%;
            padding: 8px 25px 15px 15px;
          }
          :deep(.v-show-content){
            width: 100%;
            padding: 8px 25px 15px 15px;
          }
          :deep(.v-show-content-html){
            width: 100%;
            padding: 8px 25px 15px 15px;
          }
          :deep(.v-note-show) {
            min-height: 65px;
          }
          :deep(.v-left-item) {
            flex: none !important;
          }
        }

        .case-name-row {
          .content-wrap {
            display: flex;
            justify-content: center;
            width: 100%;
            .opt-row {
              width: 100%;
              height: 32px;
            }
          }
        }
        .pre-condition-row {
          .content-wrap {
            display: flex;
            justify-content: center;
            width: 100%;
            /* min-height: 100px; */
            .opt-row {
              :deep(.el-form-item) {
                margin: 0;
              }
              width: 100%;
            }
          }
        }
        .remark-row {
          .content-wrap {
            display: flex;
            justify-content: center;
            width: 100%;
            /* min-height: 100px; */
            .opt-row {
              width: 100%;
              :deep(.el-form-item) {
                margin: 0;
              }
            }
          }
        }
        .step-desc-row {
          .step-desc-name.case-title-wrap {
            .name.title-wrap {
            }

            .split.title-wrap {
              width: 1px;
              height: 18px;
              color: #bbbfc4;
              margin: 0 8px;
            }

            .update-type-row.title-wrap {
              font-family: "PingFang SC";
              font-style: normal;
              font-weight: 400;
              font-size: 14px;
              line-height: 22px;
              color: #646a73;
              display: flex;
              .label {
                margin-right: 5.54px;
              }
              .icon {
                width: 12.92px;
                height: 7.13px;
              }
            }
          }
        }
        .attachment-row {
          .attachment-name.case-title-wrap {
            .name.title-wrap {
            }
          }

          .content-wrap {
            .opt-btn {
            }
          }
        }
      }

      .content-base-info-wrap {
        width: px2rem(304);
        min-height: px2rem(864);
        border-left: 1px solid rgba(31, 35, 41, 0.15);
        .case-wrap {
          margin-left: px2rem(24);
          margin-top: px2rem(24);
        }
        .case-title-wrap {
          display: flex;
          .title-wrap {
            font-weight: 500;
            height: 22px;
            font-size: 14px;
            line-height: 22px;
            color: #1f2329;
          }
          margin-bottom: px2rem(8);
        }
        .side-content {
          width: px2rem(256);
          height: 32px;
        }
      }
    }

    .edit-footer-container {
      display: flex;
      width: 100%;
      height: 80px;
      background: #ffffff;
      box-shadow: 0px -1px 4px rgba(31, 35, 41, 0.1);
      align-items: center;
      font-family: "PingFang SC";
      font-style: normal;
      font-weight: 400;
      font-size: 14px;
      line-height: 22px;
      text-align: center;
      // 底部按钮激活样式
      .opt-active-primary {
        background: #783887;
        color: #ffffff;
      }
      .opt-disable-primary {
        background: #bbbfc4;
        color: #ffffff;
      }
      .opt-active {
        background: #ffffff;
        color: #1f2329;
      }
      .opt-disable {
        background: #ffffff;
        color: #bbbfc4;
      }

      .save-btn-row {
        margin-left: px2rem(24);
        el-button {
        }
      }

      .save-add-pub-row {
        margin-left: px2rem(12);
        el-button {
        }
      }

      .save-create-row {
        margin-left: px2rem(12);
        el-button {
        }
      }
    }
  }
}
</style>
<style>
.attachment-popover {
  padding: 0 !important;
  height: 80px;
  min-width: 120px !important;
}
</style>
