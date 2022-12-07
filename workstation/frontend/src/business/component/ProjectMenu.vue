<template>
  <div>
    <el-row v-if="currentTodo==='api_definition' || currentTodo==='api_case'">
      <el-col class="protocol-col" :span="9">
        <el-select class="protocol-select" size="small" v-model="protocol" @change="changeProtocol">
          <el-option
            v-for="item in options"
            :key="item.value"
            :name="item.name"
            :value="item.value"
            :disabled="item.disabled">
          </el-option>
        </el-select>
      </el-col>
      <el-col :span="15">
        <el-input
          size="small"
          :placeholder="$t('api_test.request.parameters_mock_filter_tips')"
          v-model="filterText">
        </el-input>
      </el-col>
    </el-row>
    <el-row v-else>
      <el-input
        size="small"
        :placeholder="$t('api_test.request.parameters_mock_filter_tips')"
        v-model="filterText">
      </el-input>
    </el-row>
    <el-tree
      class="filter-tree"
      :data="treeDate"
      node-key="id"
      :props="defaultProps"
      default-expand-all
      @node-click="nodeClick"
      :highlight-current=true
      :filter-node-method="filterNode"
      ref="tree">
    </el-tree>
  </div>

</template>

<script>
import {getCurrentProjectID, getCurrentUserId, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {PROJECT_NAME} from "metersphere-frontend/src/utils/constants";
import {getUserProjectList} from "metersphere-frontend/src/api/project";

export default {
  name: 'ProjectMenu',
  components: {
  },
  props: {
    currentTodo: String
  },
  data() {
    return {
      checked: true,
      result: {},
      currentProject: {},
      currentNode: {},
      childNodes: [],
      projectId: '',
      projectName: PROJECT_NAME,
      projects: [],
      filterText: '',
      protocol: 'HTTP',
      treeDate: [{
        id: 1,
        name: this.$t('commons.all_project'),
        disabled: true,
        children: [],
      }],
      defaultProps: {
        children: 'children',
        label: 'name'
      },
      operators: [{
        label: '创建项目',
        //callback: this.addTestCase,
        permissions: ['PROJECT_TRACK_CASE:READ+CREATE']
      }],
      options: [
        {name: 'DUBBO', value: 'DUBBO'},
        {name: 'HTTP', value: 'HTTP'},
        {name: 'SQL', value: 'SQL'},
        {name: 'TCP', value: 'TCP'},]
    }
  },
  watch: {
    filterText(val) {
      this.$refs.tree.filter(val);
    },
    childNodes() {
      const nodeList = this.$refs.tree.root.childNodes[0].childNodes;
      this.childNodes = nodeList;
      for (let i = 0; i < nodeList.length; i++) {
        if (nodeList[i].data.id === getCurrentProjectID()) {
          this.$refs.tree.setCurrentKey(getCurrentProjectID())
        }
      }
    }

  },
  methods: {
    filterNode(value, data) {
      if (!value) return true;
      return data.name.indexOf(value) !== -1;
    },
    handleClose(key, keyPath) {
    },
    nodeClick(data, node, refNode) {
      if (data.id === 1) {
        return
      }
      this.changeProject(null, null, data);
    },
    getProject() {
      let paramData = {
        userId: getCurrentUserId(),
        workspaceId: getCurrentWorkspaceId()
      }
      getUserProjectList(paramData).then(res => {
        let data = res.data;
        if (data && data.length > 0) {
          const index = data.findIndex(d => d.id === getCurrentProjectID());
          this.projects = data;
          this.treeDate[0].children = data
          if (index !== -1) {
            this.projectId = data[index].id;
            this.projectName = data[index].name;
            this.changeProject(null, null, data[index]);

          } else {
            this.projectId = data[0].id;
            this.projectName = data[0].name;
            this.changeProject(data[0]);
          }
        }
      })
    },
    changeProject(key, keyPath, project) {
      this.currentProject = project;
      if (key) {
        this.$emit('setProject', key);
      } else {
        this.$emit('setProject', project.id);
      }
      // 获取项目时刷新该项目模块
      this.$emit('refreshNode');

    },
    changeProtocol(protocol) {
      this.$emit('setCurrentProtocol', protocol);
    }

  },
  created() {
    this.getProject();
  },
  mounted() {
    this.$nextTick(function () {
      // 仅在整个视图都被渲染之后才会运行的代码
      this.childNodes = this.$refs.tree.root.childNodes[0].childNodes;
    })
  }
}

</script>

<style scoped>
.workstation-card {
  height: 100%;
  color: #0a0a0a;
}
</style>
