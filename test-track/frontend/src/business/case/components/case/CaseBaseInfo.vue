<template>
  <div v-loading="isloading">
    <el-form ref="caseFrom">
      <div class="module-row case-wrap" v-if="!publicEnable">
        <div class="case-title-wrap">
          <div class="name title-wrap">
            {{ $t("test_track.case.module") }}
          </div>
          <div class="required required-item"></div>
        </div>

        <div class="side-content">
          <base-edit-item-component
            :editable="editable"
            trigger="hover"
            :contentObject="{
              content: getModuleLabel(),
              contentType: 'SELECT',
            }"
            :readonlyHoverEvent="true"
            ref="moduleRef"
          >
            <template v-slot:content="{ onClick, hoverEditable }">
              <div :class="hoverEditable ? 'selectHover' : ''">
                <el-form-item prop="module">
                  <ms-select-tree
                    :disabled="readOnly"
                    :data="treeNodes"
                    :defaultKey="form.module"
                    :obj="moduleObj"
                    @getValue="(id, data) => setModule(id, data)"
                    clearable
                    checkStrictly
                    size="small"
                    @selectClick="onClick"
                    @clean="onClick"
                  />
                </el-form-item>
              </div>
            </template>
          </base-edit-item-component>
        </div>
      </div>

      <div class="module-row case-wrap" v-if="publicEnable">
        <div class="case-title-wrap">
          <div class="name title-wrap">
            {{ $t("test_track.case.project") }}
          </div>
          <div class="required required-item"></div>
        </div>
        <div class="side-content">
          <el-form-item prop="projectId" v-if="publicEnable">
            <el-select v-model="form.projectId" filterable clearable>
              <el-option
                v-for="item in projectList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              ></el-option>
            </el-select>
          </el-form-item>
        </div>
      </div>
      <!-- 
      <div class="case-level-row case-wrap">
        <div class="case-title-wrap">
          <div class="name title-wrap">用例等级</div>
          <div class="required required-item"></div>
        </div>
        <div class="side-content">
          <el-select v-model="form.text" size="small" width="100%"
            ><el-option label="未规划用例" value="shanghai"></el-option>
          </el-select>
        </div>
      </div>
      <div class="case-status-row case-wrap">
        <div class="case-title-wrap">
          <div class="name title-wrap">用例状态</div>
          <div class="required required-item"></div>
        </div>
        <div class="side-content">
          <el-select v-model="form.text" size="small" width="100%"
            ><el-option label="未规划用例" value="shanghai"></el-option>
          </el-select>
        </div>
      </div>
      <div class="maintainer-row case-wrap">
        <div class="case-title-wrap">
          <div class="name title-wrap">责任人</div>
          <div class="required required-item"></div>
        </div>
        <div class="side-content">
          <el-select v-model="form.text" size="small" width="100%"
            ><el-option label="未规划用例" value="shanghai"></el-option>
          </el-select>
        </div>
      </div> -->
    </el-form>
    <!-- 自定义字段 -->
    <el-form
      v-if="isFormAlive"
      :model="customFieldForm"
      :rules="customFieldRules"
      ref="customFieldForm"
      label-position="right"
      label-width="100px"
      size="small"
      class="case-form"
    >
      <custom-filed-form-row
        :form="customFieldForm"
        :disabled="readOnly"
        :default-open="defaultOpen"
        :issue-template="testCaseTemplate"
        :editable="editable"
      />
    </el-form>
    <el-form>
      <div class="version-row case-wrap">
        <div class="case-title-wrap">
          <div class="name title-wrap">版本</div>
          <div class="required required-item"></div>
        </div>
        <div class="side-content">
          <base-edit-item-component
            :editable="editable"
            trigger="hover"
            :contentObject="{
              content: getVersionLabel(),
              contentType: 'SELECT',
            }"
            :readonlyHoverEvent="true"
          >
            <template v-slot:content="{ onClick, hoverEditable }">
              <div :class="hoverEditable ? 'selectHover' : ''">
                <el-form-item prop="versionId">
                  <el-select
                    v-model="form.versionId"
                    size="small"
                    width="100%"
                    @click.native="onClick"
                    ><el-option
                      label="V3.0.0"
                      value="4eb81bac-3678-11ed-bd4f-44e517fe65ae"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </div>
            </template>
          </base-edit-item-component>
        </div>
      </div>
      <div class="id-row case-wrap" v-if="isCustomNum">
        <div class="case-title-wrap">
          <div class="name title-wrap">ID</div>
          <div class="required required-item"></div>
        </div>
        <div class="side-content">
          <base-edit-item-component
            :editable="editable"
            trigger="hover"
            :contentObject="{
              content: form.customNum,
              contentType: 'INPUT',
            }"
            :readonlyHoverEvent="true"
          >
            <template v-slot:content="{ onClick, hoverEditable }">
              <div :class="hoverEditable ? 'selectHover' : ''">
                <el-form-item prop="customNum">
                  <el-input
                    :disabled="readOnly"
                    v-model.trim="form.customNum"
                    size="small"
                    class="ms-case-input"
                    @click.native="onClick"
                  ></el-input>
                </el-form-item>
              </div>
            </template>
          </base-edit-item-component>
        </div>
      </div>
      <div class="story-row case-wrap">
        <div class="case-title-wrap">
          <div class="name title-wrap">
            {{ $t("test_track.related_requirements") }}
          </div>
        </div>
        <div class="side-content">
          <base-edit-item-component
            :editable="editable"
            trigger="hover"
            :contentObject="{
              content: {demandId: form.demandId, demandOptions},
              contentType: 'STORY',
            }"
            :readonlyHoverEvent="true"
          >
            <template v-slot:content="{ onClick, hoverEditable }">
              <div :class="hoverEditable ? 'selectHover' : ''">
                <el-form-item prop="demandId">
                  <el-cascader
                    v-if="!readOnly"
                    v-model="demandValue"
                    :show-all-levels="false"
                    :options="demandOptions"
                    clearable
                    filterable
                    :filter-method="filterDemand"
                    @click.native="onClick"
                    size="small"
                  >
                    <template slot-scope="{ data }">
                      <span class="demand-span" :title="data.label">{{
                        data.label
                      }}</span>
                    </template>
                  </el-cascader>

                  <el-input
                    class="demandInput"
                    v-else
                    :disabled="readOnly"
                    :value="demandLabel"
                    @click.native="onClick"
                  >
                  </el-input>
                </el-form-item>
              </div>
            </template>
          </base-edit-item-component>
        </div>
      </div>
      <!-- 选择了关联需求后展示，并且必填 -->
      <div class="story-name-row case-wrap" v-if="form.demandId">
        <div class="case-title-wrap">
          <div class="name title-wrap">
            {{ $t("test_track.case.demand_name_id") }}
          </div>
          <div class="required required-item"></div>
        </div>
        <div class="side-content">
          <base-edit-item-component
            :editable="editable"
            trigger="hover"
            :contentObject="{
              content: form.demandName,
              contentType: 'INPUT',
            }"
            :readonlyHoverEvent="true"
          >
            <template v-slot:content="{ onClick, hoverEditable }">
              <div :class="hoverEditable ? 'selectHover' : ''">
                <el-form-item prop="demandName">
                  <el-input
                    @click.native="onClick"
                    :disabled="readOnly"
                    v-model="form.demandName"
                    size="small"
                    width="100%"
                  ></el-input>
                </el-form-item>
              </div>
            </template>
          </base-edit-item-component>
        </div>
      </div>
      <div class="tag-row case-wrap">
        <div class="case-title-wrap">
          <div class="name title-wrap">{{ $t("commons.tag") }}</div>
        </div>
        <div class="side-content">
          <base-edit-item-component
            :editable="editable"
            trigger="hover"
            :contentObject="{
              content: form.tags,
              contentType: 'TAG',
            }"
            :readonlyHoverEvent="true"
          >
            <template v-slot:content="{ onClick, hoverEditable }">
              <div :class="hoverEditable ? 'selectHover' : ''">
                <el-form-item prop="tags">
                  <ms-input-tag
                    :read-only="readOnly"
                    :currentScenario="form"
                    v-if="showInputTag"
                    ref="tag"
                    class="ms-case-input"
                    @click.native="onClick"
                  ></ms-input-tag>
                </el-form-item>
              </div>
            </template>
          </base-edit-item-component>
        </div>
      </div>
    </el-form>
  </div>
</template>

<script>
import MsFormDivider from "metersphere-frontend/src/components/MsFormDivider";
import MsSelectTree from "metersphere-frontend/src/components/select-tree/SelectTree";
import MsInputTag from "metersphere-frontend/src/components/MsInputTag";
import CustomFiledFormRow from "./CaseCustomFiledFormRow";
import { useStore } from "@/store";
import BaseEditItemComponent from "../BaseEditItemComponent";
import { issueDemandList } from "@/api/issue";

export default {
  name: "CaseBaseInfo",
  components: {
    MsFormDivider,
    MsSelectTree,
    MsInputTag,
    CustomFiledFormRow,
    BaseEditItemComponent,
  },
  watch: {
    demandValue() {
      if (this.demandValue.length > 0) {
        this.form.demandId = this.demandValue[this.demandValue.length - 1];
      } else {
        this.form.demandId = null;
      }
    },
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
        module: [
          {
            required: true,
            message: this.$t("test_track.case.input_module"),
            trigger: "change",
          },
        ],
        customNum: [
          { required: true, message: "ID必填", trigger: "blur" },
          {
            max: 50,
            message: this.$t("test_track.length_less_than") + "50",
            trigger: "blur",
          },
        ],
        demandName: [
          {
            required: true,
            message: this.$t("test_track.case.input_demand_name"),
            trigger: "change",
          },
        ],
        maintainer: [
          {
            required: true,
            message: this.$t("test_track.case.input_maintainer"),
            trigger: "change",
          },
        ],
        priority: [
          {
            required: true,
            message: this.$t("test_track.case.input_priority"),
            trigger: "change",
          },
        ],
        method: [
          {
            required: true,
            message: this.$t("test_track.case.input_method"),
            trigger: "change",
          },
        ],
      },
      moduleObj: {
        id: "id",
        label: "name",
      },

      // 关联需求相关
      demandValue: [],
      demandLabel: "",
      demandOptions: [],
    };
  },
  props: {
    editable: Boolean,
    form: Object,
    isFormAlive: Boolean,
    isloading: Boolean,
    readOnly: Boolean,
    publicEnable: Boolean,
    showInputTag: Boolean,
    treeNodes: Array,
    projectList: Array,
    customFieldForm: Object,
    customFieldRules: Object,
    testCaseTemplate: Object,
    defaultOpen: String,
  },
  computed: {
    isCustomNum() {
      return useStore().currentProjectIsCustomNum;
    },
    //   this.customFieldForm["用例等级"] = this.form.priority;
    //   this.customFieldForm["责任人"] = this.form.maintainer;
    //   this.customFieldForm["用例状态"] = this.form.status;
    // priority() {
    //   return this.customFieldForm["用例等级"] || this.form.priority;
    // },
    // maintainer() {
    //   return this.customFieldForm["责任人"] || this.form.maintainer;
    // },
    // status() {
    //   return this.customFieldForm["用例状态"] || this.form.status;
    // },
  },
  mounted() {
    this.getDemandOptions();
  },
  methods: {
    getVersionLabel() {
      return "V3.0.0";
    },
    getModuleLabel() {
      let module = this.treeNodes.find((v) => {
        return v.id === this.form.module;
      });
      if (module) {
        return module.name;
      }
      return "";
    },
    setModule(id, data) {
      this.form.module = id;
      this.form.nodePath = data.path;
    },
    textBlur(options, refName) {
      if (!this.editable && this.$refs[refName]) {
        // this.$refs[refName].changeSelfEditable(false);
        this.handleSaveEvent();
      }
    },
    mouseLeaveEvent(refName) {
      if (!this.editable && this.$refs[refName]) {
        this.$refs[refName].changeHoverEditable(false);
        this.handleSaveEvent();
      }
    },
    handleSaveEvent() {
      //触发保存 TODO
    },
    validateForm() {
      let isValidate = true;
      this.$refs["caseFrom"].validate((valid) => {
        if (!valid) {
          isValidate = false;
        }
      });
      return isValidate;
    },
    validateCustomForm() {
      let isValidate = true;
      this.$refs["customFieldForm"].validate((valid) => {
        if (!valid) {
          isValidate = false;
        }
      });
      return isValidate;
    },
    getCustomFields() {
      let caseFromFields = this.$refs["caseFrom"].fields;
      let customFields = this.$refs["customFieldForm"].fields;
      Array.prototype.push.apply(caseFromFields, customFields);
      return caseFromFields;
    },

    // 关联需求相关
    visibleChange(flag) {
      if (flag) {
        this.getDemandOptions();
      }
    },
    getDemandOptions() {
      this.result = { loading: true };
      this.demandLabel = "";
      issueDemandList(this.projectId)
        .then((r) => {
          this.demandOptions = [];
          if (r.data && r.data.length > 0) {
            this.buildDemandCascaderOptions(r.data, this.demandOptions, []);
          }
          this.addOtherOption();
        })
        .catch(() => {
          this.addOtherOption();
        });
    },
    addOtherOption() {
      this.demandOptions.unshift({
        value: "other",
        label: "Other: " + this.$t("test_track.case.other"),
        platform: "Other",
      });
      if (this.form.demandId === "other") {
        this.demandValue = ["other"];
        this.demandLabel = "Other: " + this.$t("test_track.case.other");
      }
      this.result = { loading: false };
    },
    buildDemandCascaderOptions(data, options, pathArray) {
      this.demandValue = [];
      data.forEach((item) => {
        let option = {
          label: item.platform + ": " + item.name,
          value: item.id,
        };
        if (this.form.demandId === item.id) {
          this.demandLabel = option.label;
        }
        options.push(option);
        pathArray.push(item.id);
        if (item.id === this.form.demandId) {
          this.demandValue = [...pathArray]; // 回显级联选项
        }
        if (item.children && item.children.length > 0) {
          option.children = [];
          this.buildDemandCascaderOptions(
            item.children,
            option.children,
            pathArray
          );
        }
        pathArray.pop();
      });
    },
    filterDemand(node, keyword) {
      if (
        keyword &&
        node.text.toLowerCase().indexOf(keyword.toLowerCase()) !== -1
      ) {
        return true;
      }
      return false;
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
          :deep(.el-select) {
            width: 100%;
          }
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
