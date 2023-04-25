<template>
  <div class="diff-box">
    <div class="switch-version-container">
      <div class="prev-row" @click.stop="prev" v-show="enablePrev">
        <i class="el-icon-arrow-left"></i>
      </div>
      <div class="version-viewer-row">
        <div
          class="tab-list"
          :style="{
            transform: 'translateX( ' + translateX + 'px)',
            transition: '0.5s',
          }"
        >
          <div
            class="version-item-row"
            v-for="item in versionList"
            :key="item.id"
          >
            <div class="version-label">
              <div class="label">{{ item.name }}</div>
              <div class="is-new" v-if="item.latest">
                {{ $t("case.last_version") }}
              </div>
            </div>
            <div class="version-creator">
              <div class="username">{{ item.createName }}</div>
              <div class="static-label">{{$t('permission.project_custom_code.create')}}</div>
            </div>
          </div>
        </div>
      </div>
      <div class="next-row" @click.stop="next" v-show="enableNext">
        <i class="el-icon-arrow-right"></i>
      </div>
    </div>
    <div class="version-detail-diff-container content-body-wrap">
      <div class="tab-pane-wrap">
        <el-tabs v-model="caseActiveName" @tab-click="tabClick">
          <el-tab-pane :label="$t('case.use_case_detail')" name="detail">
            <el-scrollbar>
              <div class="content-container">
                <div class="case-name-row">
                  <div class="case-name case-title-wrap case-content-wrap">
                    <div class="name title-wrap">{{ $t("case.case_name") }}</div>
                    <div class="required required-item"></div>
                  </div>
                  <div class="content-wrap">
                    <div class="opt-row">
                      <case-diff-text
                        :diffInfo="contentDiffData.caseName"
                      ></case-diff-text>
                    </div>
                  </div>
                </div>
                <!-- pre condition -->
                <div class="pre-condition-row">
                  <div class="condition-name case-title-wrap case-content-wrap">
                    <div class="name title-wrap">
                      {{ $t("case.preconditions") }}
                    </div>
                  </div>
                  <div class="content-wrap">
                    <div class="opt-row">
                      <case-diff-text
                        :diffInfo="contentDiffData.prerequisite"
                      ></case-diff-text>
                    </div>
                  </div>
                </div>

                <!-- step description -->
                <div class="step-desc-row">
                  <!-- 类型切换 -->
                  <div class="step-desc-name case-title-wrap case-content-wrap">
                    <div class="name title-wrap">
                      {{
                        contentDiffData.stepModel === "TEXT"
                          ? $t("test_track.case.text_describe")
                          : $t("test_track.case.step_describe")
                      }}
                    </div>
                    <div class="update-type-row title-wrap"></div>
                  </div>
                  <!-- 文本描述 -->
                  <div class="content-wrap">
                    <div class="opt-row">
                      <case-diff-text
                        :diffInfo="contentDiffData.stepDescription"
                      ></case-diff-text>
                    </div>
                  </div>
                </div>

                <!-- expect -->
                <div
                  class="expect-row"
                  v-if="contentDiffData.stepModel === 'TEXT'"
                >
                  <div class="expect-name case-title-wrap case-content-wrap">
                    <div class="name title-wrap">
                      {{ $t("test_track.case.expected_results") }}
                    </div>
                  </div>
                  <div class="content-wrap">
                    <div class="opt-row">
                      <case-diff-text
                        :diffInfo="contentDiffData.expectedResult"
                      ></case-diff-text>
                    </div>
                  </div>
                </div>

                <!-- remark -->
                <div class="remark-row">
                  <div class="remark-name case-title-wrap case-content-wrap">
                    <div class="name title-wrap">{{ $t("commons.remark") }}</div>
                  </div>
                  <div class="content-wrap">
                    <div class="opt-row">
                      <case-diff-text
                        :diffInfo="contentDiffData.remark"
                      ></case-diff-text>
                    </div>
                  </div>
                </div>

                <!-- 附件 -->
                <div class="attachment-row">
                  <div class="attachment-name case-title-wrap case-content-wrap">
                    <div class="name title-wrap">{{ $t("case.attachment") }}</div>
                  </div>
                  <div class="content-wrap">
                    <!-- 添加附件 -->
                    <!-- tip -->
                    <div class="opt-btn">
                      <div class="attachment-preview" v-if="showAttachment">
                        <case-attachment-viewer
                          :tableData="attachmentDiffData"
                          :isCopy="false"
                          :readOnly="true"
                          :is-delete="false"
                        ></case-attachment-viewer>
                        <div v-if="!showAttachment">{{ $t("case.none") }}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-scrollbar>
          </el-tab-pane>
          <!-- 关联测试用例 -->
          <el-tab-pane
            :label="$t('case.associate_test_cases')"
            name="associateTestCases"
          >
            <el-scrollbar>
              <div class="content-container">
                <case-diff-test-relate
                  :tableData="testCaseRelateData"
                ></case-diff-test-relate>
              </div>
            </el-scrollbar>
          </el-tab-pane>
          <!-- 关联缺陷 -->
          <el-tab-pane
            :label="$t('test_track.case.relate_issue')"
            name="associatedDefects"
          >
            <el-scrollbar>
              <div class="content-container">
                <case-diff-issue-relate
                  :tableData="issuesData"
                ></case-diff-issue-relate>
              </div>
            </el-scrollbar>
          </el-tab-pane>
          <!-- 依赖关系 -->
          <el-tab-pane :label="$t('case.dependencies')" name="dependencies">
            <el-scrollbar>
              <div class="content-container">
                <case-diff-relationship
                  :resourceId="caseId"
                  :preTableData="preTableData"
                  :postTableData="postTableData"
                ></case-diff-relationship>
              </div>
            </el-scrollbar>
          </el-tab-pane>
          <!-- 评论 -->
          <el-tab-pane :label="$t('case.comment')" name="comment">
            <el-scrollbar>
              <div class="content-container">
                <case-comment-viewer
                  :readOnly="true"
                  :comments="diffCommentsData"
                ></case-comment-viewer>
              </div>
            </el-scrollbar>
          </el-tab-pane>
          <!-- 变更记录 -->
          <!-- <el-tab-pane :label="$t('case.change_record')" name="changeRecord">
            <div class="content-container"></div>
          </el-tab-pane> -->
        </el-tabs>
      </div>
      <div class="content-base-info-wrap">
        <el-scrollbar>
          <div class="base-info-wrap">
            <!-- 所属模块 -->
            <div class="module-row">
              <div class="case-title-wrap">
                <div class="name title-wrap">
                  {{ $t("test_track.case.module") }}
                </div>
                <div class="required required-item"></div>
              </div>
              <case-diff-text :diffInfo="baseInfoDiffData.modules"></case-diff-text>
            </div>
            <!-- 自定义字段 -->
            <div
              class="module-row item-row"
              v-for="(item, index) in customDiffData"
              :key="index"
            >
              <div class="case-title-wrap">
                <div class="name title-wrap">
                  {{ item.key }}
                </div>
                <div class="required required-item" v-if="item.required"></div>
              </div>
              <case-diff-text
                :diffInfo="[{ diffArr: item.value }]"
              ></case-diff-text>
            </div>
            <!-- 关联需求 -->
            <div class="module-row item-row">
              <div class="case-title-wrap">
                <div class="name title-wrap">
                  {{ $t("test_track.related_requirements") }}
                </div>
                <div class="required required-item"></div>
              </div>
              <case-diff-text :diffInfo="baseInfoDiffData.story"></case-diff-text>
            </div>
            <!-- 需求ID/名称 -->
            <div class="module-row item-row">
              <div class="case-title-wrap">
                <div class="name title-wrap">
                  {{ $t("test_track.case.demand_name_id") }}
                </div>
                <div class="required required-item"></div>
              </div>
              <case-diff-text :diffInfo="baseInfoDiffData.storyId"></case-diff-text>
            </div>
            <!-- 标签 -->
            <div class="module-row item-row">
              <div class="case-title-wrap">
                <div class="name title-wrap">
                  {{ $t("commons.tag") }}
                </div>
              </div>
              <case-diff-text :diffInfo="tagDiffData.tags"></case-diff-text>
            </div>
          </div>
        </el-scrollbar>
      </div>
    </div>
  </div>
</template>
<script>
import CaseDiffText from "./CaseDiffText";
import DefaultDiffExecutor from "./version_diff";
import CaseAttachmentViewer from "@/business/case/components/case/CaseAttachmentViewer";
import { getTestCaseVersions } from "@/api/testCase";
import { getCurrentProjectID } from "metersphere-frontend/src/utils/token";
import { getProjectVersions } from "metersphere-frontend/src/api/version";
import { attachmentList } from "@/api/attachment";
import { byteToSize } from "@/business/utils/sdk-utils";
import CaseDiffTestRelate from "./CaseDiffTestRelate";
import { getRelateTest } from "@/api/testCase";
import { testCaseCommentList } from "@/api/test-case-comment";
import CaseCommentViewer from "../CaseCommentViewer";
import CaseDiffRelationship from "./CaseDiffRelationship";
import CaseDiffIssueRelate from "./CaseDiffIssueRelate";
import { getRelationshipCase } from "@/api/testCase";
import { getIssuesByCaseIdWithSearch } from "@/api/issue";
import { getProjectMemberOption } from "metersphere-frontend/src/api/user";
import {
  buildCustomFields,
  buildTestCaseOldFields,
  parseCustomField,
} from "metersphere-frontend/src/utils/custom_field";
import { getTestTemplate } from "@/api/custom-field-template";

export default {
  name: "CaseDiffViewer",
  components: {
    CaseDiffText,
    CaseAttachmentViewer,
    CaseDiffTestRelate,
    CaseCommentViewer,
    CaseDiffRelationship,
    CaseDiffIssueRelate,
  },
  props: {
    leftVersion: {
      type: Object,
      default() {
        return {}
      },
    },
    rightVersion: {
      type: Object,
      default() {
        return {}
      },
    },
    caseId: {
      type: String,
      default: "",
    },
  },
  data() {
    return {
      translateX: 0,
      //根据数据 每两个分为一组 由最后一组展示
      currentGroupIndex: -1,
      standardWith: 184,
      prevBtn: true,
      nextBtn: true,
      /**
       * 正文
       */
      caseActiveName: "detail",
      defaultExecutor: {},
      contentDiffData: {},
      customDiffData: {},
      attachmentDiffData: [],
      showAttachment: false,
      baseInfoDiffData: {},
      tagDiffData: {},
      originCase: {},
      targetCase: {},
      // 版本id 与 具体的 case详情的关系
      cacheVersionsMap: new Map(),
      // 版本名称与版本id的对应关系
      cacheVersionsNameMap: new Map(),
      // 版本列表
      versionOptions: [],
      versionList: [],
      // 关联用例diff数据
      testCaseRelateData: [],
      // 评论diff数据
      diffCommentsData: [],
      // 前置
      preTableData: [],
      // 后置
      postTableData: [],
      //缺陷对比结果
      issuesData: [],
      //自定义字段
      customFields: [],
      testCaseTemplate: {},
      memberOptions: [],
      //自定义模板字段 缓存
      cacheCustomFields: new Map(),
      // 自定义字段 options - id 关系缓存
      catchCustomOptions: new Map(),
    };
  },
  async mounted() {
    let testTemplateRes = await getTestTemplate();
    this.testCaseTemplate = testTemplateRes;
    this.customFields = testTemplateRes.customFields || [];
    this.refresh();
  },
  computed: {
    enablePrev() {
      if (!this.versionList || this.versionList.length <= 2) {
        return false;
      }
      return this.prevBtn;
    },
    enableNext() {
      if (!this.versionList || this.versionList.length <= 2) {
        return false;
      }
      return this.nextBtn;
    },
  },
  methods: {
    /**
     * 版本切换相关
     */
    calculate() {
      if (!this.versionList) {
        return;
      }
      let length = this.versionList.length;
      // length <= 2
      if (length <= 2) {
        this.prevBtn = false;
        this.nextBtn = false;
        return;
      }
      //lenght > 2
      this.currentGroupIndex = length;
      //当前初始 从右往左
      this.translateX = this.standardWith * (this.currentGroupIndex - 2) * -1;
      this.nextBtn = false;
    },
    generateTranslateX(symbol = 1) {
      this.translateX = this.standardWith * symbol + this.translateX;
    },
    prev() {
      this.currentGroupIndex =
        this.currentGroupIndex - 1 <= 0 ? 0 : this.currentGroupIndex - 1;
      this.listenBtnStatus();
      this.generateTranslateX();
    },
    next() {
      this.currentGroupIndex =
        this.currentGroupIndex + 1 >= this.versionList.length
          ? this.versionList.length
          : this.currentGroupIndex + 1;
      this.listenBtnStatus();
      this.generateTranslateX(-1);
    },
    listenBtnStatus() {
      this.prevBtn = this.currentGroupIndex > 2;
      this.nextBtn = this.currentGroupIndex < this.versionList.length;
    },

    /**
     * 内容对比
     */
    tabClick() {},
    async refresh() {
      await this.fetchCaseVersions();
      await this.fetchAllCaseVersion();
      this.formatCompareVersion();
      this.checkoutVersionCase();
      this.calculate();
      this.defaultExecutor = new DefaultDiffExecutor(
        this.originCase,
        this.targetCase,
        null
      );
      this.defaultExecutor.diff();

      // 自定义字段 填充value
      if (this.customFields.length > 0) {
        //获取成员列表 自定义字段 填充到类型为member、multipleMember的options中
        let memberRes = await getProjectMemberOption();
        this.memberOptions = memberRes.data || [];
      }
      this.fillCustomValue();
      this.diffCustomData();

      await this.diffAttachment();
      await this.diffTestRelate();
      await this.diffComments();
      await this.diffRelationship();
      await this.diffIssues();
      // 初始化
      this.initContent();
    },
    initContent() {
      this.contentDiffData = this.defaultExecutor.contentDiffData || {};
      this.tagDiffData = this.defaultExecutor.tagDiffData || {};
      this.baseInfoDiffData = this.defaultExecutor.baseInfoDiffData || {};
    },
    async fetchAllCaseVersion() {
      //首先获取所有版本，再去构造版本展示的数组
      let res = await getProjectVersions(getCurrentProjectID());
      this.versionOptions = res.data ?? [];
    },
    async fetchCaseVersions() {
      let res = await getTestCaseVersions(this.caseId);
      let data = res.data || [];
      data.forEach((e) => {
        this.cacheVersionsMap.set(e.versionId, e);
        this.cacheVersionsNameMap.set(e.versionName, e.versionId);
      });
    },

    async fetchAttachmentList(id) {
      let data = { belongType: "testcase", belongId: id };
      let tableData = [];
      let response = await attachmentList(data);
      let files = response.data;
      if (!files) {
        return;
      }
      tableData = JSON.parse(JSON.stringify(files));
      tableData.map((f) => {
        f.size = byteToSize(f.size);
        f.status = "success";
        f.progress = 100;
      });
      return tableData;
    },
    formatCompareVersion() {
      // 只添加比对的版本
      this.versionList.push(this.leftVersion);
      this.versionList.push(this.rightVersion);
    },
    checkoutVersionCase() {
      if (this.leftVersion) {
        this.originCase = this.cacheVersionsMap.get(this.leftVersion.id);
      }
      if (this.rightVersion) {
        this.targetCase = this.cacheVersionsMap.get(this.rightVersion.id);
      }
    },
    async diffAttachment() {
      // 首先获取 两个版本的附件信息
      let origin = await this.fetchAttachmentList(this.originCase.id);
      let target = await this.fetchAttachmentList(this.targetCase.id);
      this.attachmentDiffData = this.defaultExecutor.diffAttachment(
        origin,
        target
      );
      if (this.attachmentDiffData && this.attachmentDiffData.length > 0) {
        this.showAttachment = true
      }
    },
    async fetchTestRelate(id) {
      let res = await getRelateTest(id);
      let data = res.data || [];
      return data;
    },
    async diffTestRelate() {
      let origin = await this.fetchTestRelate(this.originCase.id);
      let target = await this.fetchTestRelate(this.targetCase.id);
      this.testCaseRelateData = this.defaultExecutor.diffTableData(
        origin,
        target,
        "num",
        ["projectName", "name", "testType"]
      );
    },

    /**
     * 获取评论信息
     */
    async fetchComments(id) {
      let res = await testCaseCommentList(id);
      return res.data || [];
    },
    async diffComments() {
      let origin = await this.fetchComments(this.originCase.id);
      let target = await this.fetchComments(this.targetCase.id);
      this.diffCommentsData = this.defaultExecutor.diffTableData(
        origin,
        target,
        "id",
        ["description", "authorName"]
      );
    },

    /**
     * 变更记录相关
     */

    /**
     * 依赖关系相关
     */
    async fetchRelationshipData(id, type) {
      let res = await getRelationshipCase(id, type);
      return res.data || [];
    },

    async diffRelationship() {
      // 前置 table数据
      let origin = await this.fetchRelationshipData(this.originCase.id, "PRE");
      let target = await this.fetchRelationshipData(this.targetCase.id, "PRE");
      this.preTableData = this.defaultExecutor.diffTableData(
        origin,
        target,
        "sourceId",
        ["targetName", "status"]
      );
      // 后置 table数据
      let postOrigin = await this.fetchRelationshipData(
        this.originCase.id,
        "POST"
      );
      let postTarget = await this.fetchRelationshipData(
        this.targetCase.id,
        "POST"
      );
      this.postTableData = this.defaultExecutor.diffTableData(
        postOrigin,
        postTarget,
        "sourceId",
        ["targetName", "status"]
      );
    },

    /**
     * 关联缺陷
     */
    async fetchIssues(id) {
      let page = {
        data: [],
        result: {},
      };
      let condition = {};
      let res = await getIssuesByCaseIdWithSearch(
        "FUNCTIONAL",
        id,
        page,
        condition
      );

      return page.data || [];
    },
    async diffIssues() {
      let origin = await this.fetchIssues(this.originCase.id);
      let target = await this.fetchIssues(this.targetCase.id);
      this.issuesData = this.defaultExecutor.diffTableData(
        origin,
        target,
        "id",
        [
          "title",
          "description",
          "platform",
          "platformId",
          "platformStatus",
          "projectId",
        ]
      );
    },
    // fillCustomStruct(form) {
    //   return parseCustomField(
    //     form,
    //     this.testCaseTemplate,
    //     this.customFieldRules
    //   );
    // },
    fillCustomStruct(fields) {
      if (!fields || fields.length <= 0) {
        return {};
      }
      let temp = [];
      fields.forEach((f) => {
        let tempFiled = this.cacheCustomFields.get(f.id);
        if (!tempFiled) {
          return;
        }

        // 填充值
        if (f.textValue) {
          // 无需处理 options
          tempFiled.lastValue = f.textValue;
          temp.push(tempFiled);
          return;
        }
        if (!f.value) {
          return;
        }
        // 处理options
        let res = f.value;
        // 校验 转json 是否通过
        try {
          res = JSON.parse(f.value);
        } catch {
          //
        }

        // 判断 options 是否存在
        let options = this.catchCustomOptions.get(f.id);
        if (!options || options.length <= 0) {
          tempFiled.lastValue = res;
          temp.push(tempFiled);
          return;
        }

        let tempMap = new Map();
        options.forEach((o) => {
          if (o.value) {
            tempMap.set(o.value, o);
          }
        });

        //判断是否为数组
        if (Array.isArray(res)) {
          // 是数组 则
          let translates = [];
          res.forEach((r) => {
            let option = tempMap.get(r);
            if (option) {
              translates.push(
                option.system ? this.$t(option.text) : option.text
              );
            }
          });
          tempFiled.lastValue = translates.join(" ");
          temp.push(tempFiled);
          return;
        }

        let option = tempMap.get(res);
        if (option) {
          tempFiled.lastValue = option.system
            ? this.$t(option.text)
            : option.text;
          temp.push(tempFiled);
        }
      });

      //将temp 转为 json
      let resObj = {};
      temp.forEach((t) => {
        resObj[t.name] = t.lastValue || "";
      });
      return resObj;
    },
    diffCustomData() {
      let filed1 = this.fillCustomStruct(this.originCase.fields);
      let filed2 = this.fillCustomStruct(this.targetCase.fields);
      this.customDiffData =
        this.defaultExecutor.diffCustomData(filed1, filed2) || {};
    },
    fillCustomValue() {
      // 缓存自定义字段信息
      // 成员相关的 填充options
      if (!this.customFields || this.customFields.length <= 0) {
        return;
      }

      this.customFields.forEach((c) => {
        if (["member", "multipleMember"].indexOf(c.type) != -1) {
          let standOptions = [];
          this.memberOptions.forEach((mo) => {
            let obj = {};
            obj.system = false;
            obj.text = mo.name;
            obj.value = mo.id;
            standOptions.push(obj);
          });
          c.options = standOptions;
        }
        if (c.options && c.options.length > 0) {
          this.catchCustomOptions.set(c.id, c.options);
        }

        this.cacheCustomFields.set(c.id, c);
      });
    },
  },
};
</script>
<style scoped lang="scss">
.diff-box {
  background-color: #fff;
  .switch-version-container {
    display: flex;
    margin-top: 17px;
    margin-left: 22px;
    .prev-row {
      width: 20px;
      height: 64px;
      background: #f5f6f7;
      border-radius: 4px;
      line-height: 64px;
      text-align: center;
      cursor: pointer;
      i {
        color: #1f2329;
      }
    }
    .prev-row:hover {
      background: rgba(31, 35, 41, 0.1);
    }

    .version-viewer-row {
      width: 368px;
      overflow: hidden;
      .tab-list {
        display: flex;
        width: 100%;
        .version-item-row {
          width: 180px;
          min-width: 180px;
          height: 64px;
          box-sizing: border-box;
          background: #f5f6f7;
          border-radius: 4px;
          margin-left: 4px;
          display: flex;
          flex-direction: column;

          .version-label {
            margin-left: 20px;
            margin-top: 8px;
            display: flex;
            height: 22px;
            line-height: 22px;
            .label {
              color: #1f2329;
              font-weight: 500;
              margin-right: 2px;
            }

            .is-new {
              padding: 1px 6px;
              height: 20px;
              line-height: 20px;
              background: rgba(120, 56, 135, 0.2);
              border-radius: 2px;
              color: #783887;
            }
          }

          .version-creator {
            margin-left: 20px;
            display: flex;
            color: #646a73;
            height: 22px;
            line-height: 22px;
            .username {
            }

            .static-label {
              margin-left: 8px;
            }
          }
        }
      }
    }

    .next-row {
      width: 20px;
      height: 64px;
      background: #f5f6f7;
      border-radius: 4px;
      line-height: 64px;
      margin-left: 4px;
      text-align: center;
      cursor: pointer;
      i {
        color: #1f2329;
      }
    }
    .next-row:hover {
      background: rgba(31, 35, 41, 0.1);
    }
  }
  .version-detail-diff-container {
    width: 100%;
    border-top: 1px solid rgba(31, 35, 41, 0.15);
    margin-top: 20px;
  }
}
</style>
<style lang="scss">
.file-drawer .el-drawer__header {
  border-bottom: none !important;
}

// 公共样式
.line-through {
  text-decoration-line: line-through;
  color: #8f959e;
  font-size: 14px;
}

.create-row {
  width: 32px;
  height: 16px;
  border-radius: 2px;
  background: rgba(52, 199, 36, 0.2);
  padding: 0px 4px;
  line-height: 16px;
  font-weight: 500;
  font-size: 12px;
  color: #2ea121;
  text-align: center;
  line-height: 16px;
}

.delete-row {
  padding: 0 4px;
  width: 32px;
  height: 16px;
  background: rgba(245, 74, 69, 0.2);
  border-radius: 2px;
  color: #d83931;
  font-weight: 500;
  font-size: 12px;
  text-align: center;
  line-height: 16px;
}

.format-change-row {
  padding: 0px 4px;
  width: 56px;
  height: 16px;
  background: rgba(255, 136, 0, 0.2);
  border-radius: 2px;
  color: #de7802;
  font-weight: 500;
  font-size: 12px;
  text-align: center;
  line-height: 16px;
}

.content-base-info-wrap {
  height: calc(100vh - 110px);
}
</style>
<style lang="scss" scoped>
// 正文部分
@import "@/business/style/index.scss";
.content-body-wrap {
  // 1024 减去左右padding 各24 和 1px右边框
  width: px2rem(1024);
  height: 100%;
  display: flex;

  .tab-pane-wrap {
    width: px2rem(896);
    height: 100%;
    border-right: 1px solid rgba(31, 35, 41, 0.15);

    :deep(.el-tabs__item) {
      padding-left: px2rem(24);
    }

    :deep(.el-tabs__content) {
      overflow: hidden;
    }
  }
  .base-info-wrap {
    width: px2rem(304);
    max-height: calc(70vh);
    padding: 24px;
    .item-row {
      margin-top: 21px;
    }
  }
  .content-container {
    padding-left: px2rem(24);
    padding-right: px2rem(24);
    max-height: calc(70vh);
  }
}
.case-content-wrap {
  margin-top: 24px;
}
.case-title-wrap {
  display: flex;
  .title-wrap {
    font-weight: 500;
    font-size: 14px;
    color: #1f2329;
  }
  margin-bottom: px2rem(8);
}
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

.attachment-preview :deep(.atta-box) {
  width: 25rem !important;
}
:deep(.atta-box .atta-container .icon) {
  // width: 100% !important;
}
:deep(.atta-box .atta-container .detail .filename) {
  width: 60% !important;
}
</style>
