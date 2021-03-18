<template>
  <ms-module-minder
    v-loading="result.loading"
    :tree-nodes="treeNodes"
    :data-map="dataMap"
    :tags="tags"
    :distinct-tags="[...tags, $t('test_track.plan.plan_status_prepare')]"
    @save="save"
  />
</template>

<script>
import MsModuleMinder from "@/business/components/common/components/MsModuleMinder";
import {getTestCaseDataMap} from "@/business/components/track/common/minder/minderUtils";
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
    selectNodeIds: {
      type: Array
    },
    reviewId: {
      type: String
    },
    projectId: String
  },
  mounted() {
    this.$nextTick(() => {
      this.getTestCases();
    })
  },
  methods: {
    getTestCases() {
      if (this.projectId) {
        this.result = this.$post('/test/review/case/list/all', {reviewId: this.reviewId}, response => {
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
