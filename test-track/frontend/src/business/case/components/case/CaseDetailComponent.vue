<template>
  <div>
    <!-- 基于form组件进行表单验证 -->
    <el-form>
      <!-- case name -->
      <div class="case-name-row">
        <div class="case-name case-title-wrap">
          <div class="name title-wrap">用例名称</div>
          <div class="required required-item"></div>
        </div>
        <div class="content-wrap">
          <div class="opt-row">
            <base-edit-item-component
              :editable="editable"
              trigger="click"
              :contentObject="{ content: form.name, contentType: 'TEXT' }"
              ref="nameRef"
            >
              <template slot="content" slot-scope="val">
                <el-form-item prop="name">
                  <el-input
                    size="small"
                    placeholder="请输入用例名称"
                    v-model="form.name"
                    @blur="textBlur(val, 'nameRef')"
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
          <div class="name title-wrap">前置条件</div>
        </div>
        <div class="content-wrap">
          <div class="opt-row">
            <form-rich-text-item
              :disabled="readOnly"
              :data="form"
              :default-open="richTextDefaultOpen"
              placeholder="请输入前置条件"
              prop="prerequisite"
            />
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
            <form-rich-text-item
              v-if="form.stepModel === 'TEXT'"
              prop="stepDescription"
              :disabled="readOnly"
              :data="form"
              :default-open="richTextDefaultOpen"
            />
            <!-- 步骤描述 -->
            <test-case-step-item
              v-if="form.stepModel === 'STEP' || !form.stepModel"
              :form="form"
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
            <form-rich-text-item
              v-if="form.stepModel === 'TEXT'"
              prop="expectedResult"
              :disabled="readOnly"
              :data="form"
              :default-open="richTextDefaultOpen"
            />
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
            <form-rich-text-item
              class="remark-item"
              :disabled="readOnly"
              :data="form"
              :default-open="richTextDefaultOpen"
              prop="remark"
            />
          </div>
        </div>
      </div>

      <!-- 附件 -->
      <div class="attachment-row">
        <div class="attachment-name case-title-wrap">
          <div class="name title-wrap">附件</div>
        </div>
        <div class="content-wrap">
          <!-- 添加附件 -->
          <!-- tip -->
          <div class="opt-btn">
            <el-popover
              ref="popover"
              placement="right"
              trigger="click"
              popper-class="attachment-popover"
              :visible-arrow="false"
            >
              <div
                class="upload-wrap"
                style="
                  display: flex;
                  flex-direction: column;
                  width: 120px;
                  align-items: center;
                "
              >
                <!-- 本地上传 -->
                <div
                  class="local-row"
                  style="
                    display: flex;
                    height: 32px;
                    margin-top: 8px;
                    line-height: 32px;
                    text-align: center;
                    magin-left: 1px;
                    magin-right: 1px;
                  "
                >
                  <div class="icon">
                    <i class="el-icon-upload2" style="color: #646a73"></i>
                  </div>
                  <div
                    class="title"
                    style="
                      letter-spacing: -0.1px;
                      color: #1f2329;
                      margin-left: 10px;
                    "
                  >
                    上传文件
                  </div>
                </div>
                <!-- 关联文件 -->
                <div
                  class="ref-row"
                  style="
                    display: flex;
                    height: 32px;
                    margin-bottom: 8px;
                    line-height: 32px;
                    text-align: center;
                    magin-left: 1px;
                    magin-right: 1px;
                  "
                >
                  <div class="icon">
                    <i class="el-icon-connection" style="color: #646a73"></i>
                  </div>
                  <div
                    class="title"
                    style="
                      letter-spacing: -0.1px;
                      color: #1f2329;
                      margin-left: 10px;
                    "
                  >
                    关联文件
                  </div>
                </div>
              </div>
            </el-popover>
            <el-button v-popover:popover icon="el-icon-plus" size="small"
              >添加附件</el-button
            >
          </div>
          <div class="opt-tip">支持任意类型文件，文件大小不超过 500MB</div>
          <div class="attachment-preview">
            <case-attachment-viewer></case-attachment-viewer>
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
import CaseAttachmentViewer from "@/business/case/components/case/CaseAttachmentViewer";
export default {
  name: "CaseDetailComponent",
  components: {
    FormRichTextItem,
    BaseEditItemComponent,
    TestCaseStepItem,
    StepChangeItem,
    CaseAttachmentViewer,
  },
  data() {
    return {};
  },
  props: {
    editable: Boolean,
    form: Object,
    readOnly: Boolean,
    richTextDefaultOpen: String,
    formLabelWidth: String,
  },
  methods: {
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
  },
};
</script>

<style scoped lang="scss">
@import "@/business/style/index.scss";
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
        width: px2rem(975);
        height: 100%;
        padding-left: px2rem(24);
        padding-right: px2rem(24);
        .case-title-wrap {
          display: flex;
          margin-top: px2rem(24);
          margin-bottom: px2rem(8);
          .title-wrap {
            height: 22px;
            font-family: "PingFang SC";
            font-style: normal;
            font-weight: 500;
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
            border: 1px solid #bbbfc4 !important;
            border-radius: 4px;
            box-shadow: none !important;
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
            min-height: 100px;
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
            min-height: 100px;
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

            .opt-tip {
              font-family: "PingFang SC";
              font-style: normal;
              font-weight: 400;
              font-size: 14px;
              line-height: 22px;
              /* identical to box height, or 157% */

              color: #8f959e;
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
      height: px2rem(80);
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
