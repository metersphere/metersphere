<template>
  <ms-module-minder
    v-loading="result.loading"
    :tree-nodes="treeNodes"
    :data-map="dataMap"
    :tags="tags"
    :tag-enable="true"
    minder-key="testReview"
    :select-node="selectNode"
    :distinct-tags="[...tags, $t('test_track.plan.plan_status_prepare')]"
    @afterMount="handleAfterMount"
    @save="save"
    ref="minder"
  />
</template>

<script>
import MsModuleMinder from "@/business/components/common/components/MsModuleMinder";
import {getTestCaseDataMap, tagBatch} from "@/business/components/track/common/minder/minderUtils";
export default {
name: "TestReviewMinder",
  components: {MsModuleMinder},
  data() {
    return{
      dataMap: new Map(),
      tags: [this.$t('test_track.plan_view.pass'), this.$t('test_track.plan_view.not_pass')],
      result: {}
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
    projectId: String
  },
  mounted() {
    if (this.selectNode && this.selectNode.data) {
      if (this.$refs.minder) {
        let importJson = this.$refs.minder.getImportJsonBySelectNode(this.selectNode.data);
        this.$refs.minder.setJsonImport(importJson);
      }
    }
    this.$nextTick(() => {
      this.getTestCases();
    })
  },
  watch: {
    selectNode() {
      if (this.$refs.minder) {
        this.$refs.minder.handleNodeSelect(this.selectNode);
      }
      // this.getTestCases();
    }
  },
  computed: {
    selectNodeIds() {
      return this.$store.state.testReviewSelectNodeIds;
    },
    selectNode() {
      return this.$store.state.testReviewSelectNode;
    }
  },
  methods: {
    handleAfterMount() {
      tagBatch([...this.tags, this.$t('test_track.plan.plan_status_prepare')]);
    },
    getTestCases() {
      if (this.projectId) {
        let param = {
          reviewId: this.reviewId,
          nodeIds: this.selectNodeIds
        };
        this.result = this.$post('/test/review/case/list/all', param, response => {
          this.dataMap = getTestCaseDataMap(response.data, true, (data, item) => {
            if (item.reviewStatus === 'Pass') {
              data.resource.push(this.$t('test_track.plan_view.pass'));
            } else if (item.reviewStatus === 'UnPass') {
              data.resource.push(this.$t('test_track.plan_view.not_pass'));
            } else {
              data.resource.push(this.$t('test_track.plan.plan_status_prepare'));
            }
            data.caseId = item.caseId;
          });
        });
      }
    },
    save(data) {
      let saveCases = [];
      this.buildSaveCase(data.root, saveCases);
      this.result = this.$post('/test/review/case/minder/edit', saveCases, () => {
        this.$success(this.$t('commons.save_success'));
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
  }
}
</script>

<style scoped>

</style>
