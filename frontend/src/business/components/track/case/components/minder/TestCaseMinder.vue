<template>
  <ms-module-minder
    v-loading="result.loading"
    :tree-nodes="treeNodes"
    :data-map="dataMap"
    @save="save"
  />
</template>

<script>
import MsModuleMinder from "@/business/components/common/components/MsModuleMinder";
export default {
name: "TestCaseMinder",
  components: {MsModuleMinder},
  data() {
    return{
      testCase: [],
      dataMap: new Map(),
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
    condition: Object,
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
        this.result = this.$get('/test/case/list/detail/' + this.projectId,response => {
          this.testCase = response.data;
          this.parse();
        });
      }
    },
    save(data) {
      let saveCases = [];
      this.buildSaveCase(data.root, saveCases, undefined);
      console.log(saveCases);
      let param = {
        projectId: this.projectId,
        data: saveCases
      }
      this.result = this.$post('/test/case/minder/edit', param, () => {
        this.$success(this.$t('commons.save_success'));
      });
    },
    buildSaveCase(root, saveCases, parent) {
      let data = root.data;
      if (data.resource && data.resource.indexOf("用例") > -1) {
        this._buildSaveCase(root, saveCases, parent);
      } else {
        if (root.children) {
          root.children.forEach((childNode) => {
            this.buildSaveCase(childNode, saveCases, root.data);
          })
        }
      }
    },
    _buildSaveCase(node, saveCases, parent) {
      let data = node.data;
      let isChange = false;
      let testCase = {
        id: data.id,
        name: data.text,
        nodeId: parent ? parent.id : "",
        nodePath: parent ? parent.path : "",
        type: data.type ? data.type : 'functional',
        method: data.method ? data.method: 'manual',
        maintainer: data.maintainer,
        priority: 'P' + data.priority,
      };
      if (data.changed) isChange = true;
      let steps = [];
      let stepNum = 1;
      if (node.children) {
        node.children.forEach((childNode) => {
          let childData = childNode.data;
          if (childData.resource && childData.resource.indexOf('前置条件') > -1) {
            testCase.prerequisite = childData.text;
          } else if (childData.resource && childData.resource.indexOf('备注') > -1) {
            testCase.remark = childData.text;
          } else {
            // 测试步骤
            let step = {};
            step.num = stepNum++;
            step.desc = childData.text;
            if (childNode.children) {
              let result = "";
              childNode.children.forEach((child) => {
                result += child.data.text;
                if (child.data.changed) isChange = true;
              })
              step.result = result;
            }
            steps.push(step);
          }
          if (childData.changed) isChange = true;
        })
      }
      testCase.steps = JSON.stringify(steps);
      if (isChange) {
        saveCases.push(testCase);
      }
    },
    parse() {
      let dataMap = new Map();
      this.testCase.forEach(item => {
        item.steps = JSON.parse(item.steps);
        // if (item.tags && item.tags.length > 0) {
        //   item.tags = JSON.parse(item.tags);
        // }
        let mapItem = dataMap.get(item.nodeId);
        let nodeItem = {
          data: {
            id: item.id,
            text: item.name,
            priority: Number.parseInt(item.priority.substring(item.priority.length - 1 )),
            resource: ["用例"],
            type: item.type,
            method: item.method,
            maintainer: item.maintainer
          }
        }
        this.parseChildren(nodeItem, item);
        if (mapItem) {
          mapItem.push(nodeItem);
        } else {
          mapItem = [];
          mapItem.push(nodeItem);
          dataMap.set(item.nodeId, mapItem);
        }
      })
      this.dataMap = dataMap;
    },
    parseChildren(nodeItem, item) {
      nodeItem.children = [];
      let children = [];
      this._parseChildren(children, item.prerequisite, "前置条件");
      item.steps.forEach((step) => {
        let descNode = this._parseChildren(children, step.desc, "测试步骤");
        if (descNode) {
          descNode.data.num = step.num;
          descNode.children = [];
          this._parseChildren(descNode.children, step.result, "预期结果");
        }
      });
      this._parseChildren(children, item.remark, "备注");
      nodeItem.children = children;
    },
    _parseChildren(children, k, v) {
      if (k) {
        let node = {
          data: {
            text: k,
            resource: [v]
          }
        }
        children.push(node);
        return node;
      }
    }
  }
}
</script>

<style scoped>

</style>
