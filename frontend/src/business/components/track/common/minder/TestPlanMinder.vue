<template>
  <ms-module-minder
    v-loading="result.loading"
    :tree-nodes="treeNodes"
    :data-map="dataMap"
    :tags="tags"
    :tag-enable="true"
    minder-key="testPlan"
    :select-node="selectNode"
    :distinct-tags="[...tags, this.$t('test_track.plan.plan_status_prepare')]"
    :ignore-num="true"
    @afterMount="handleAfterMount"
    @save="save"
    ref="minder"
  />
</template>

<script>
import MsModuleMinder from "@/business/components/common/components/MsModuleMinder";
import {
  handleExpandToLevel, listenBeforeExecCommand, listenNodeSelected, loadSelectNodes,
  tagBatch,
} from "@/business/components/track/common/minder/minderUtils";
import {getPlanCasesForMinder} from "@/network/testCase";
export default {
name: "TestPlanMinder",
  components: {MsModuleMinder},
  data() {
    return{
      dataMap: new Map(),
      result: {loading: false},
      tags: [this.$t('test_track.plan_view.pass'), this.$t('test_track.plan_view.failure'), this.$t('test_track.plan_view.blocking'), this.$t('test_track.plan_view.skip')],
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
    planId: {
      type: String
    },
    projectId: String
  },
  computed: {
    selectNode() {
      return this.$store.state.testPlanViewSelectNode;
    }
  },
  mounted() {
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
    }
  },
  methods: {
    handleAfterMount() {
      listenNodeSelected(() => {
        let param = {
          request: {planId: this.planId},
          result: this.result,
          isDisable: true
        }
        loadSelectNodes(param,  getPlanCasesForMinder, this.setParamCallback);
      });
      listenBeforeExecCommand((even) => {
        if (even.commandName === 'expandtolevel') {
          let level = Number.parseInt(even.commandArgs);
          let param = {
            request: {planId: this.planId},
            result: this.result,
            isDisable: true
          }
          handleExpandToLevel(level, even.minder.getRoot(), param, getPlanCasesForMinder, this.setParamCallback);
        }
      });

      tagBatch([...this.tags, this.$t('test_track.plan.plan_status_prepare')]);
    },
    setParamCallback(data, item) {
      if (item.status === 'Pass') {
        data.resource.push(this.$t('test_track.plan_view.pass'));
      } else if (item.status === 'Failure') {
        data.resource.push(this.$t('test_track.plan_view.failure'));
      } else if (item.status === 'Blocking') {
        data.resource.push(this.$t('test_track.plan_view.blocking'));
      } else if (item.status === 'Skip') {
        data.resource.push(this.$t('test_track.plan_view.skip'));
      } else {
        data.resource.push(this.$t('test_track.plan.plan_status_prepare'));
      }
    },
    save(data) {
      let saveCases = [];
      this.buildSaveCase(data.root, saveCases);
      this.result = this.$post('/test/plan/case/minder/edit', saveCases, () => {
        this.$success(this.$t('commons.save_success'));
      });
    },
    buildSaveCase(root, saveCases) {
      let data = root.data;
      if (data.resource && data.resource.indexOf(this.$t('api_test.definition.request.case')) > -1) {
        this._buildSaveCase(root, saveCases, parent);
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
        id: data.id,
      };
      if (data.resource.length > 1) {
        if (data.resource.indexOf(this.$t('test_track.plan_view.failure')) > -1) {
          testCase.status = 'Failure';
        } else if (data.resource.indexOf(this.$t('test_track.plan_view.pass')) > -1) {
          testCase.status = 'Pass';
        } else if (data.resource.indexOf(this.$t('test_track.plan_view.blocking')) > -1) {
          testCase.status = 'Blocking';
        } else if (data.resource.indexOf(this.$t('test_track.plan_view.skip')) > -1) {
          testCase.status = 'Skip';
        }
      }
      saveCases.push(testCase);
    }
  }
}
</script>

<style scoped>

</style>
