<template>
  <!--  功能用例编辑页面右侧基本信息-->
  <div v-loading="isLoading" class="case-base-info-form">
    <el-form ref="caseFrom" :rules="headerRules" :model="form" class="case-padding">
      <!-- ID及自定义ID  -->
      <div class="id-row case-wrap" v-if="!editable || isCustomNum">
        <div class="case-title-wrap">
          <div class="name title-wrap">ID</div>
          <div class="required required-item"></div>
        </div>
      <div class="side-content">
          <base-edit-item-component
            :editable="editable"
            :auto-save="!readOnly"
            trigger="hover"
            :contentObject="{
              content: isCustomNum ? form.customNum.toString() : form.num.toString(),
              contentType: 'INPUT',
            }"
            :readonlyHoverEvent="!readOnly"
            :content-click-event="!readOnly"
            :model="form"
            :rules="rules"
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
      <!-- 公共用例展示所属项目 -->
      <div class="module-row case-wrap" v-if="publicEnable">
        <div class="case-title-wrap">
          <div class="name title-wrap">
            {{ $t("test_track.case.project") }}
          </div>
          <div class="required required-item"></div>
        </div>
        <div class="side-content">
          <base-edit-item-component
            :editable="editable"
            :auto-save="!readOnly"
            trigger="hover"
            :contentObject="{
              content: getProjectLabel(),
              contentType: 'SELECT',
            }"
            :readonlyHoverEvent="!readOnly"
            :content-click-event="!readOnly"
            :model="form"
            :rules="headerRules"
          >
            <template v-slot:content="{ onClick, hoverEditable }">
              <div :class="hoverEditable ? 'selectHover' : ''">
                <el-form-item prop="projectId">
                  <el-select
                    size="small"
                    v-model="form.projectId"
                    @click.native="onClick"
                    filterable
                    clearable
                  >
                    <el-option
                      v-for="item in projectList"
                      :key="item.id"
                      :label="item.name"
                      :value="item.id"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </div>
            </template>
          </base-edit-item-component>
        </div>
      </div>
      <!--  用例展示所属模块    -->
      <div class="module-row case-wrap" v-else>
        <div class="case-title-wrap">
          <div class="name title-wrap">
            {{ $t("test_track.case.module") }}
          </div>
          <div class="required required-item"></div>
        </div>

        <div class="side-content" v-if="treeNodes">
          <base-edit-item-component
            :editable="editable"
            :auto-save="!readOnly"
            trigger="hover"
            :contentObject="{
              content: getModuleLabel(),
              contentType: 'SELECT',
            }"
            :readonlyHoverEvent="!readOnly"
            :content-click-event="!readOnly"
            :model="form"
            :rules="headerRules"
            ref="moduleRef"
          >
            <template v-slot:content="{ onClick, hoverEditable }">
              <div :class="hoverEditable ? 'selectHover' : ''">
                <el-form-item prop="nodeId">
                  <ms-select-tree
                    :disabled="readOnly"
                    :data="treeNodes"
                    :obj="moduleObj"
                    :default-key="form.nodeId"
                    @getValue="setModule"
                    clearable
                    checkStrictly
                    size="small"
                    @selectClick="onClick"
                    @clean="onClick"
                    ref="moduleTree"
                  />
                </el-form-item>
              </div>
            </template>
          </base-edit-item-component>
        </div>
      </div>
    </el-form>
    <!-- 自定义字段Form -->
    <el-form
      v-if="isFormAlive"
      :model="customFieldForm"
      :rules="customFieldRules"
      ref="customFieldForm"
      label-position="right"
      label-width="100px"
      size="small"
      class="case-form case-padding"
    >
      <custom-filed-form-row
        :form="customFieldForm"
        :rules="customFieldRules"
        :disabled="readOnly"
        :default-open="defaultOpen"
        :issue-template="testCaseTemplate"
        :editable="editable"
        :project-id="form.projectId"
      />
    </el-form>
    <el-form ref="baseCaseFrom" :rules="rules" :model="form" class="case-padding">
      <!-- 版本字段  -->
      <div class="version-row case-wrap" v-if="versionEnable && !this.form.id">
        <div class="case-title-wrap">
          <div class="name title-wrap">{{ $t("commons.version") }}</div>
          <div class="required required-item"></div>
        </div>
        <div class="side-content">
          <!-- v-xpack -->
          <base-edit-item-component
            :editable="editable"
            :readonlyHoverEvent="!readOnly"
            :content-click-event="!readOnly"
            :auto-save="!readOnly"
            trigger="hover"
            :contentObject="{
              content: getVersionLabel(),
              contentType: 'SELECT',
            }"
            :model="form"
            :rules="rules"
          >
            <template v-slot:content="{ onClick, hoverEditable }">
              <div :class="hoverEditable ? 'selectHover' : ''">
                <el-form-item prop="versionId">
                  <el-select
                    :disabled="readOnly"
                    v-model="form.versionId"
                    size="small"
                    width="100%"
                    @click.native="onClick"
                    @change="changeVersion"
                  >
                    <el-option
                      v-for="(version, index) in versionFilters"
                      :key="index"
                      :value="version.id"
                      :label="version.name"
                    />
                  </el-select>
                </el-form-item>
              </div>
            </template>
          </base-edit-item-component>
        </div>
      </div>
      <!-- 关联需求  -->
      <div class="story-row case-wrap">
        <div class="case-title-wrap">
          <div class="name title-wrap">
            {{ $t("test_track.related_requirements") }}
          </div>
        </div>
        <div class="side-content">
          <base-edit-item-component
            :editable="editable"
            :auto-save="!readOnly"
            trigger="hover"
            :contentObject="{
              content: { demandId: form.demandId, demandOptions },
              contentType: 'STORY',
            }"
            :readonlyHoverEvent="!readOnly"
            :content-click-event="!readOnly"
            :model="form"
            :rules="rules"
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
                      <div class="story-box">
                        <div class="story-platform">{{ handleDemandOptionPlatform(data) }}</div>
                        <div class="story-label" v-if="data.value === 'other'">
                          {{ $t("test_track.case.other") }}
                        </div>
                        <div class="story-label text-ellipsis" v-else>{{ handleDemandOptionLabel(data) }}</div>
                      </div>
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
      <!-- 需求名称: 选择了关联需求后展示，并且必填 -->
      <div class="story-name-row case-wrap" v-if="form.demandId === 'other'">
        <div class="case-title-wrap">
          <div class="name title-wrap">
            {{ $t("test_track.case.demand_name_id") }}
          </div>
          <div class="required required-item"></div>
        </div>
        <div class="side-content">
          <base-edit-item-component
            :editable="editable"
            :auto-save="!readOnly"
            trigger="hover"
            :contentObject="{
              content: form.demandName,
              contentType: 'INPUT',
            }"
            :readonlyHoverEvent="!readOnly"
            :content-click-event="!readOnly"
            :model="form"
            :rules="rules"
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
      <!-- 标签字段  -->
      <div class="tag-row case-wrap">
        <div class="case-title-wrap">
          <div class="name title-wrap">{{ $t("commons.tag") }}</div>
        </div>
        <div class="side-content">
          <base-edit-item-component
            :editable="editable"
            :auto-save="!readOnly"
            trigger="hover"
            :contentObject="{
              content: form.tags,
              contentType: 'TAG',
            }"
            :readonlyHoverEvent="!readOnly"
            :content-click-event="!readOnly"
            :model="form"
            :rules="rules"
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
import {getProjectVersions} from "metersphere-frontend/src/api/version";
import { hasLicense } from "metersphere-frontend/src/utils/permission";
import MsFormDivider from "metersphere-frontend/src/components/MsFormDivider";
import MsSelectTree from "metersphere-frontend/src/components/select-tree/SelectTree";
import MsInputTag from "metersphere-frontend/src/components/new-ui/MsInputTag";
import CustomFiledFormRow from "./CaseCustomFiledFormRow";
import { useStore } from "@/store";
import BaseEditItemComponent from "../BaseEditItemComponent";
import { issueDemandList } from "@/api/issue";
import {getTestCaseNodesByCaseFilter} from "@/api/testCase";
import {buildTree} from "@/business/utils/sdk-utils";
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
    'form.demandId'() {
      this.buildDemandOptions();
    },
    projectId() {
      this.init();
    }
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
        versionId:[
        {
            required: true,
            message: this.$t("case.version_id_cannot_be_empty"),
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
      headerRules: {
        customNum: [
          { required: true, message: 'ID' + this.$t('commons.cannot_be_null'), trigger: "blur" },
          {
            max: 50,
            message: this.$t("test_track.length_less_than") + "50",
            trigger: "blur",
          },
        ],
        nodeId: [
          {
            required: true,
            message: this.$t("api_test.environment.module_warning"),
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
      versionFilters: [],
      demandList: [],
      defaultModuleKey: '',
      treeNodes: null,
      validateTip: ''
    };
  },
  props: {
    editable: Boolean,
    editableState: Boolean,
    form: Object,
    isFormAlive: Boolean,
    isLoading: Boolean,
    readOnly: Boolean,
    publicEnable: Boolean,
    showInputTag: Boolean,
    projectList: Array,
    customFieldForm: Object,
    customFieldRules: Object,
    testCaseTemplate: Object,
    defaultOpen: String,
    versionEnable: Boolean,
    projectId: String,
    enableDefaultModule: Boolean
  },
  computed: {
    isCustomNum() {
      return useStore().currentProjectIsCustomNum;
    },
    createNodeId() {
      return this.$route.query.createNodeId;
    }
  },
  mounted() {
    this.init();
  },
  methods: {
    init() {
      this.getDemandOptions();
      this.getVersionOptions();
      this.getNodeTrees();
    },
    setDefaultModule(treeNodes) {
      if (this.enableDefaultModule) {
        // 创建时设置模块ID
        if (treeNodes && treeNodes.length > 0) {
          if (this.createNodeId) {
            // 创建时设置选中的模块
            this.form.nodeId = this.createNodeId;
            let node = this.findTreeNode(treeNodes);
            if (node) {
              this.form.nodePath = node.path;
            } else {
              // 如果模块已删除，设置为未规划模块
              this.setUnplannedModule(treeNodes);
            }
          }  else {
            // 创建不带模块ID，设置成为规划模块
            this.setUnplannedModule(treeNodes);
          }
        }
      } else {
        if (this.form.nodeId) {
          // 编辑重新设置下 nodePath
          let node = this.findTreeNode(treeNodes);
          if (node) {
            this.form.nodePath = node.path;
          } else {
            // 如果模块已删除，或者跨项目复制公共用例，设置为未规划模块
            this.setUnplannedModule(treeNodes);
          }
        } else {
          this.setUnplannedModule(treeNodes);
        }
      }
    },
    setUnplannedModule(treeNodes) {
      // 创建不带模块ID，设置成为规划模块
      this.form.nodeId = treeNodes[0].id;
      this.form.nodePath = treeNodes[0].path;
    },
    getNodeTrees() {
      if (this.publicEnable || !this.projectId) {
        return;
      }
      getTestCaseNodesByCaseFilter(this.projectId, {})
        .then(r => {
          let treeNodes = r.data;
          treeNodes.forEach(node => {
            node.name = node.name === '未规划用例' ? this.$t('api_test.unplanned_case') : node.name
            buildTree(node, {path: ''});
          });

          this.setDefaultModule(treeNodes);

          this.treeNodes = treeNodes;
        });
    },
    handleDemandOptionPlatform(data){
      if(data.platform){
        return data.platform
      }
      if(data.label){
        let arr = data.label.split(": ")
        if(arr && arr.length > 1){
          return arr[0];
        }
      }
      return "";
    },
    handleDemandOptionLabel(data){
      if(data.label){
        let arr = data.label.split(": ")
        if(arr && arr.length > 1){
          return arr[1];
        }
        return data.label;
      }
      return "";
    },
    getVersionOptions() {
      if (hasLicense()) {
        if (this.projectId) {
          getProjectVersions(this.projectId).then(
            (r) => (this.versionFilters = r.data)
          );
        }
      }
    },
    changeVersion(data) {
      //从versionFilters 中找到 版本名称
      let version = this.versionFilters.filter((v) => {
        return v.id === data;
      });
      if (version || (Array.isArray(version) && version.length > 0)) {
        if (Array.isArray(version)) {
          this.form.versionName = version[0].name;
        } else {
          this.form.versionName = version.name;
        }
      }
    },
    getVersionLabel() {
      let version = this.versionFilters.find((v) => {
        return v.id === this.form.versionId;
      });
      if (version) {
        return version.name;
      }
      return "";
    },
    getProjectLabel() {
      let project = this.projectList.find((v) => {
        return v.id === this.form.projectId;
      });
      if (project) {
        return project.name;
      }
      return "";
    },
    getModuleLabel() {
      let module = this.findTreeNode(this.treeNodes);
      if (module) {
        return module.name;
      }
      return "";
    },
    findTreeNode(nodeArray) {
      for (let i = 0; i < nodeArray.length; i++) {
        let node = nodeArray[i];
        if (node.id === this.form.nodeId) {
          return node;
        } else {
          if (node.children && node.children.length > 0) {
            let findNode = this.findTreeNode(node.children);
            if (findNode != null) {
              return findNode;
            }
          }
        }
      }
    },
    setModule(id, data) {
      if (data) {
        this.form.nodeId = id;
        this.form.nodePath = data.path;
      }
    },
    mouseLeaveEvent(refName) {
      if (!this.editable && this.$refs[refName]) {
        this.$refs[refName].changeHoverEditable(false);
        this.handleSaveEvent();
      }
    },
    validateWithTip() {
      this.validateTip = '';
      // 这里非短路或，收集所有的错误提示
      if (!this.validateForm() | !this.validateCustomForm() | !this.validateCaseFrom()) {
        if (this.validateTip) {
          this.$error(this.validateTip.substring(0, this.validateTip.length - 1));
          this.validateTip = '';
        }
        return false;
      }
      return true;
    },
    validateForm() {
      let isValidate = true;
      this.$refs["baseCaseFrom"].validate((valid, msg) => {
        if (!valid) {
          isValidate = false;
          this.recordTip(msg);
        }
      });
      return isValidate;
    },
    validateCustomForm() {
      let isValidate = true;
      this.$refs["customFieldForm"].validate((valid, msg) => {
        if (!valid) {
          isValidate = false;
          this.recordTip(msg);
        }
      });
      return isValidate;
    },
    validateCaseFrom() {
      let isValidate = true;
      this.$refs["caseFrom"].validate((valid, msg) => {
        if (!valid) {
          isValidate = false;
          this.recordTip(msg);
        }
      });
      return isValidate;
    },
    recordTip(msg) {
      for (const field in msg) {
        if (msg[field]) {
          msg[field].forEach(item => this.validateTip += item.message + ',');
        }
      }
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
      if (this.projectId) {
        issueDemandList(this.projectId)
          .then((r) => {
            this.demandOptions = [];
            if (r.data) {
              this.demandList = r.data;
            }
            this.buildDemandOptions();
          })
          .catch((r) => {
            this.demandOptions = [];
            this.addOtherOption();
          });
      }
    },
    buildDemandOptions() {
      this.demandOptions = [];
      this.addOtherOption();
      if (this.demandList.length > 0) {
        this.buildDemandCascaderOptions(this.demandList, this.demandOptions, []);
      }
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
.case-padding{
  margin-left: px2rem(24);
  margin-right: px2rem(24);
}
/* 关联需求下拉框 */
.story-box {
  display: flex;
  .story-platform {
    font-weight: 400;
    font-size: 14px;
    line-height: 22px;
    color: #783887;
    padding: 1px 6px;
    gap: 4px;
    min-width: 49px;
    height: 24px;
    background: rgba(120, 56, 135, 0.2);
    border-radius: 2px;
    margin-right: 8px;
  }
  .story-label {
    line-height: 22px;
    color: #1f2329;
    max-width: 300px;
  }
}
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
    /* line-height: px2rem(32); */
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
            font-family: "PingFang SC";
            font-style: normal;
            font-weight: 500;
            font-size: 14px;
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
          /* margin-left: px2rem(24); */
          margin-top: px2rem(24);
          /* margin-right: px2rem(24); */
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
          :deep(.el-select) {
            width: 100%;
          }
          :deep(.el-cascader) {
            width: 100%;
          }
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
