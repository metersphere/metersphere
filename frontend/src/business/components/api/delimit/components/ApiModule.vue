<template>
  <div v-loading="result.loading">
    <select-menu
      :data="projects"
      :current-data="currentProject"
      :title="$t('test_track.project')"
      @dataChange="changeProject" style="margin-bottom: 20px"/>

    <el-select style="width: 100px ;height: 30px" size="small" v-model="value">
      <el-option
        v-for="item in options"
        :key="item.value"
        :label="item.label"
        :value="item.value"
        :disabled="item.disabled">
      </el-option>
    </el-select>
    <el-input style="width: 175px; padding-left: 3px" :placeholder="$t('test_track.module.search')" v-model="filterText"
              size="small">
      <template v-slot:append>
        <el-button icon="el-icon-folder-add" @click="addApi"></el-button>
      </template>
    </el-input>

    <el-tree :data="data"
             class="filter-tree node-tree"
             node-key="id"
             default-expand-all
             :expand-on-click-node="false"
             @node-click="nodeClick"
             @node-drag-start="handleDragStart"
             @node-drag-enter="handleDragEnter"
             @node-drag-leave="handleDragLeave"
             @node-drag-over="handleDragOver"
             @node-drag-end="handleDragEnd"
             @node-drop="handleDrop"
             draggable
             :allow-drop="allowDrop"
             :allow-drag="allowDrag">
        <span class="custom-tree-node father"
              slot-scope="{ node, data }">
          <!-- 如果是编辑状态 -->
          <template v-if="data.isEdit==1">
            <el-input ref="input"
                      @blur="() => submitEdit(node,data)"
                      v-model="newLabel"
                      class="ms-el-input" size="mini"></el-input>
          </template>
          <!-- 如果不是编辑状态 -->
          <span class="node-title" v-else v-text="data.label"></span>

          <span class="node-operate child">
            <el-tooltip
              v-if="data.id!=1"
              class="item"
              effect="dark"
              :open-delay="200"
              :content="$t('test_track.module.rename')"
              placement="top">
              <i @click.stop="() => edit(node,data)" class="el-icon-edit"></i>
            </el-tooltip>

            <el-tooltip
              class="item"
              effect="dark"
              :open-delay="200"
              :content="$t('test_track.module.add_submodule')"
              placement="top">
              <i @click.stop="() => append(node,data)" class="el-icon-circle-plus-outline"></i>
            </el-tooltip>

             <el-tooltip
               v-if="data.id!=1"
               class="item"
               effect="dark"
               :open-delay="200"
               :content="$t('commons.delete')"
               placement="top">
              <i @click.stop="() => remove(node, data)" class="el-icon-delete"></i>
            </el-tooltip>
          </span>
        </span>
    </el-tree>

    <ms-add-basis-http-api ref="httpApi"></ms-add-basis-http-api>

  </div>

</template>

<script>
  import MsAddBasisHttpApi from "./basis/AddBasisHttpApi";
  import SelectMenu from "../../../track/common/SelectMenu";

  export default {
    name: 'MsApiModule',
    components: {
      MsAddBasisHttpApi,
      SelectMenu
    },
    data() {
      return {
        options: [{
          value: 'HTTP',
          label: 'HTTP'
        }, {
          value: 'DUBBO',
          label: 'DUBBO'
        }, {
          value: 'TCP',
          label: 'TCP'
        }, {
          value: 'SQL',
          label: 'SQL'
        }],
        value: 'HTTP',
        httpVisible: false,
        result: {},
        filterText: "",
        nextFlag: true,
        currentProject: {},
        projects: [],
        data: [
          {
            "id": 1,
            "label": "技术部",
            "level": 1,
            "children": [
              {
                "id": 2,
                "label": "运维组",
                "level": 2,
                "children": [
                  {
                    "id": 3,
                    "label": "godo",
                    "level": 3,
                    "children": []
                  }
                ]
              },
              {
                "id": 4,
                "label": "测试组",
                "level": 2,
                "children": []
              }
            ]
          }
        ],
        newLabel: '',
        defaultProps: {
          children: 'children',
          label: 'label'
        }
      }
    },
    created() {
      this.getApiGroupData()
    },
    methods: {
      getApiModuleTree() {
        if (this.currentProject) {
          this.result = this.$get("/api/module/list/" + this.currentProject.id + "/" + this.value, response => {
            this.data = response.data;
          });
        }
      },
      // 调api获取接口分组数据
      getApiGroupData() {
        this.getProjects();
        this.getApiModuleTree();

      },
      handleDragStart(node, ev) {
        console.log('drag start', node.data.label)
      },
      handleDragEnter(draggingNode, dropNode, ev) {
        console.log('tree drag enter: ', dropNode.data.label)
      },
      handleDragLeave(draggingNode, dropNode, ev) {
        console.log('tree drag leave: ', dropNode.data.label)
      },
      handleDragOver(draggingNode, dropNode, ev) {
        console.log('tree drag over: ', dropNode.data.label)
      },
      handleDragEnd(draggingNode, dropNode, dropType, ev) {
        console.log(
          'tree drag end: ',
          dropNode && dropNode.data.label,
          dropType
        )
        // 调后端更新
        this.updateApiGroup(this.data)
      },
      handleDrop(draggingNode, dropNode, dropType, ev) {
        console.log('tree drop: ', dropNode.data.label, dropType)
      },
      allowDrop(draggingNode, dropNode, type) {
        if (dropNode.data.id === 1) {
          return false
        } else {
          return true
        }
      },
      allowDrag(draggingNode) {
        // 顶层默认分组不允许拖拽
        if (draggingNode.data.id === 1) {
          return false
        } else {
          return true
        }
      },

      append(node, data) {
        // var pid = data.parentApiGroupId + ':' + data.id
        if (this.nextFlag) {
          var timestamp = new Date().getTime()
          const newChild = {
            id: timestamp,
            isEdit: 0,
            label: '',
            children: []
          }
          if (!data.children) {
            this.$set(data, 'children', [])
          }
          data.children.push(newChild)
          this.updateApiGroup(this.data)
          this.edit(node, newChild);
        }
      },

      remove(node, data) {
        if (data.label === "") {
          this.nextFlag = true;
        }
        const parent = node.parent
        const children = parent.data.children || parent.data
        const index = children.findIndex(d => d.id === data.id)
        children.splice(index, 1)
        this.updateApiGroup(this.data)
      },

      edit(node, data) {
        this.$set(data, 'isEdit', 1)
        this.newLabel = data.label
        this.$nextTick(() => {
          this.$refs.input.focus()
        })

        console.log('after:', data.id, data.label, data.isEdit)
      },

      submitEdit(node, data) {

        // 触发了保存按钮
        if (this.newLabel === "") {
          this.nextFlag = false;
          this.$message.warning(this.$t('commons.input_name'));
          return;
        }
        if (data.label == this.newLabel) {
          this.newLabel = ''
          this.$set(data, 'isEdit', 0)
        } else {
          this.$set(data, 'label', this.newLabel)
          this.newLabel = ''
          this.$set(data, 'isEdit', 0)
          this.updateApiGroup(node, data);
        }
        this.nextFlag = true;
      },

      cancelEdit(node, data) {
        this.newLabel = ''
        this.$set(data, 'isEdit', 0)
      },


      // 保存或修改
      updateApiGroup(node, data) {
        let url = '';
        if (!data.id) {
          url = '/api/module/add';
          data.level = 1;
          if (node.parent) {
            //非根节点
            data.parentId = node.parent.id;
            data.level = node.parent.level + 1;
          }
        } else if (this.type === 'edit') {
          url = '/api/module/edit';
          data.id = this.node.id;
          data.level = this.node.level;
          data.nodeIds = this.nodeIds;
        }
        data.name = data.label;
        data.projectId = this.currentProject.id;

        this.$post(url, data, () => {
          this.$success(this.$t('commons.save_success'));
          this.getApiGroupData();
        });
      },

      nodeClick(node, data, obj) {
        console.log('点击了：', node.id, node.label)

      },
      addApi() {
        this.$refs.httpApi.open();
      },
      // 项目相关方法
      changeProject(project) {
        this.setCurrentProject(project);
      },
      setCurrentProject(project) {
        if (project) {
          this.currentProject = project;
          localStorage.setItem("current_project", JSON.stringify(project));
        }
        // 触发其他方法
      },
      getProjects() {
        this.$get("/project/listAll", (response) => {
          this.projects = response.data;
          let lastProject = JSON.parse(localStorage.getItem("current_project"));
          if (lastProject) {
            let hasCurrentProject = false;
            for (let i = 0; i < this.projects.length; i++) {
              if (this.projects[i].id == lastProject.id) {
                this.currentProject = lastProject;
                hasCurrentProject = true;
                break;
              }
            }
            if (!hasCurrentProject) {
              this.setCurrentProject(this.projects[0]);
            }
          } else {
            if (this.projects.length > 0) {
              this.setCurrentProject(this.projects[0]);
            }
          }
        });
      },

    }
  }
</script>

<style scoped>
  .node-tree {
    margin-top: 15px;
    margin-bottom: 15px;
  }

  .ms-el-input {
    height: 25px;
    line-height: 25px;
  }

  .custom-tree-node {
    flex: 1 1 auto;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding-right: 8px;
    width: 100%;
  }

  .father .child {
    display: none;
  }

  .father:hover .child {
    display: block;
  }

  .node-title {
    width: 0px;
    text-overflow: ellipsis;
    white-space: nowrap;
    flex: 1 1 auto;
    padding: 0px 5px;
    overflow: hidden;
  }

  .node-operate > i {
    color: #409eff;
    margin: 0px 5px;
  }

  /deep/ .el-tree-node__content {
    height: 33px;
  }
</style>
