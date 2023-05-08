<template>
  <ms-module-minder
    v-loading="result.loading"
    minder-key="REVIEW_CASE"
    :tree-nodes="treeNodes"
    :data-map="dataMap"
    :tags="tags"
    :tag-enable="true"
    :disabled="disable"
    :select-node="selectNode"
    :distinct-tags="distinctTags"
    :ignore-num="true"
    @afterMount="handleAfterMount"
    @save="save"
    ref="minder"
  />
</template>

<script>
import {
  clearOtherTagAfterBatchTag,
  handleExpandToLevel,
  listenBeforeExecCommand,
  listenNodeSelected,
  loadSelectNodes, saveTagBeforeBatchTag,
  tagBatch
} from "@/business/common/minder/minderUtils";
import {getReviewCasesForMinder} from "@/api/testCase";
import {setPriorityView} from "vue-minder-editor-plus/src/script/tool/utils";
import MsModuleMinder from "@/business/common/minder/MsModuleMinder";
import {useStore} from "@/store";
import {mapState} from "pinia";
import {testReviewCaseMinderEdit} from "@/api/remote/plan/test-review-case";
import {hasPermission} from "@/business/utils/sdk-utils";
import i18n from "@/i18n";

export default {
  name: "TestReviewMinder",
  components: {MsModuleMinder},
  data() {
    return {
      dataMap: new Map(),
      tags: [this.$t('test_track.plan_view.pass'), this.$t('test_track.plan_view.not_pass')],
      result: {loading: false}
    }
  },
  props: {
    treeNodes: {
      type: Array,
      default() {
        return []
      }
    },
    reviewId: {
      type: String
    },
    projectId: String,
    condition: Object
  },
  mounted() {
    this.setIsChange(false);
    if (this.selectNode && this.selectNode.data) {
      if (this.$refs.minder) {
        let importJson = this.$refs.minder.getImportJsonBySelectNode(this.selectNode.data);
        this.$refs.minder.setJsonImport(importJson);
      }
    }

  },
  watch: {
    selectNode() {
      if (this.$refs.minder) {
        this.$refs.minder.handleNodeSelect(this.selectNode);
      }
    },
    treeNodes() {
      this.$refs.minder.initData();
    }
  },
  computed: {
    ...mapState(useStore, {
      selectNodeIds: 'testReviewSelectNodeIds',
      selectNode: 'testReviewSelectNode'
    }),
    disable() {
      return !hasPermission('PROJECT_TRACK_REVIEW:READ+EDIT');
    },
    distinctTags() {
      return [...this.tags, this.$t('test_track.plan.plan_status_prepare'), this.$t('test_track.review.again'), this.$t('test_track.review.underway')];
    }
  },
  methods: {
    handleAfterMount() {
      listenNodeSelected(() => {
        loadSelectNodes(this.getParam(), getReviewCasesForMinder, this.setParamCallback);
      });
      listenBeforeExecCommand((even) => {
        if (even.commandName === 'expandtolevel') {
          let level = Number.parseInt(even.commandArgs);
          handleExpandToLevel(level, even.minder.getRoot(), this.getParam(), getReviewCasesForMinder, this.setParamCallback);
        }

        if (even.commandName.toLocaleLowerCase() === 'resource') {
          saveTagBeforeBatchTag();
          // afterExecCommand 没有效果，这里只能 setTimeout 执行
          setTimeout(() => {
            clearOtherTagAfterBatchTag(this.tags, this.distinctTags);
            // 设置完标签后，优先级显示有问题，重新设置下
            setPriorityView(true, 'P');
          }, 100);
          this.setIsChange(true);
        }
      });

      tagBatch(this.distinctTags, {
        param: this.getParam(),
        getCaseFuc: getReviewCasesForMinder,
        setParamCallback: this.setParamCallback
      });
    },
    getParam() {
      return {
        request: {
          reviewId: this.reviewId,
          orders: this.condition.orders
        },
        result: this.result,
        isDisable: true
      }
    },
    setParamCallback(data, item) {
      let statusMap = {
        'Pass' : this.$t('test_track.review.pass'),
        'UnPass' : this.$t('test_track.review.un_pass'),
        'Again' : this.$t('test_track.review.again'),
        'Underway' : this.$t('test_track.review.underway')
      }
      let resourceName = statusMap[item.status];
      if (!resourceName) {
        resourceName = this.$t('test_track.review.underway');
      }
      data.resource.push(resourceName);
      data.caseId = item.caseId;
    },
    save(data) {
      let saveCases = [];
      this.buildSaveCase(data.root, saveCases);
      this.result.loading = true;
      testReviewCaseMinderEdit(this.reviewId, saveCases)
        .then(() => {
          this.$post('/test/case/review/edit/status/' + this.reviewId);
          this.result.loading = false;
          this.$success(this.$t('commons.save_success'));
          this.setIsChange(false);
        })
        .catch(() => {
          this.result.loading = false;
        });
    },
    buildSaveCase(root, saveCases) {
      let data = root.data;
      if (data.resource && data.resource.indexOf(this.$t('api_test.definition.request.case')) > -1) {
        this._buildSaveCase(root, saveCases);
      } else {
        if (root.children) {
          root.children.forEach((childNode) => {
            this.buildSaveCase(childNode, saveCases, root.data);
          })
        }
      }
    },
    _buildSaveCase(node, saveCases) {
      let data = node.data;
      if (!data.changed) {
        return;
      }
      let testCase = {
        caseId: data.caseId,
        id: data.id
        // name: data.text,
      };
      if (data.resource.length > 1) {
        if (data.resource.indexOf(this.$t('test_track.plan_view.not_pass')) > -1) {
          testCase.status = 'UnPass';
        } else if (data.resource.indexOf(this.$t('test_track.plan_view.pass')) > -1) {
          testCase.status = 'Pass';
        }
      }
      saveCases.push(testCase);
    },
    setIsChange(isChanged) {
      useStore().$patch({
        isTestCaseMinderChanged: isChanged
      });
    }
  }
}
</script>

<style scoped>

</style>
