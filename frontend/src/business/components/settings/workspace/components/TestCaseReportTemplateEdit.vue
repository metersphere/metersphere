<template>

  <el-drawer
    :before-close="handleClose"
    :visible.sync="showDialog"
    :with-header="false"
    size="100%"
    ref="drawer"
    v-loading="result.loading">
    <template v-slot:default="scope">

      <template-component-edit-header :template="template" @cancel="handleClose" @save="handleSave"/>

      <div class="container">
        <el-aside>
          <div class="description">
            <span class="title">组件库</span>
            <span>从组件库把需要使用的组件拖到右侧，预览测试报告的效果。系统组件只能添加一次。自定义组件，可以设定默认的标题和内容。</span>
          </div>
          <draggable
            class="dragArea list-group"
            :list="components"
            :group="{ name: 'component', pull: 'clone', put: false }"
            :clone="clonePreview">
            <transition-group>
              <template-component-bar  v-for="item in components" :key="item" :component="componentMap.get(item)"/>
            </transition-group>
          </draggable>
        </el-aside>

        <el-main>
          <draggable
                class="dragArea list-group"
                :list="previews"
                @change="change"
                group="component">
            <transition-group>
              <div class="preview" v-for="item in previews" :key="item.id">
                <base-info-component v-if="item.id == 1"/>
                <test-result-component v-if="item.id == 2"/>
                <test-result-chart-component v-if="item.id == 3"/>
                <rich-text-component :preview="item" v-if="item.type != 'system'"/>
                <i class="el-icon-error" @click="handleDelete(item)"/>
              </div>
            </transition-group>
          </draggable>
        </el-main>
      </div>
    </template>
  </el-drawer>
</template>

<script>

  import draggable from 'vuedraggable';
  import BaseInfoComponent from "./TemplateComponent/BaseInfoComponent";
  import TestResultComponent from "./TemplateComponent/TestResultComponent";
  import TestResultChartComponent from "./TemplateComponent/TestResultChartComponent";
  import TemplateComponentBar from "./TemplateComponentBar";
  import RichTextComponent from "./TemplateComponent/RichTextComponent";
  import TemplateComponentEditHeader from "./TemplateComponentEditHeader";
  import {WORKSPACE_ID} from '../../../../../common/js/constants';
  import {jsonToMap, mapToJson} from "../../../../../common/js/utils";

    export default {
      name: "TestCaseReportTemplateEdit",
      components: {
        TemplateComponentEditHeader,
        RichTextComponent,
        TemplateComponentBar,
        TestResultChartComponent,
        TestResultComponent,
        BaseInfoComponent,
        draggable
      },
      data() {
        return {
          showDialog: false,
          result: {},
          name: '',
          type: 'edit',
          componentMap: new Map(
            [
              [1, { name: "基础信息", id: 1 , type: 'system'}],
              [2, { name: "测试结果", id: 2 , type: 'system'}],
              [3, { name: "测试结果分布", id: 3 ,type: 'system'}],
              [4, { name: "自定义模块", id: 4 ,type: 'custom'}]
            ]
          ),
          components: [4],
          previews: [],
          template: {}
        }
      },
      props: {

      },
      watch: {
      },
      methods: {
        open(id) {
          this.template = {
            name: '',
              content: {
              components: [1,2,3,4],
                customComponent: new Map()
            }
          };
          this.previews = [];
          this.components = [4];
          if (id) {
            this.type = 'edit';
            this.getTemplateById(id);
          } else {
            this.type = 'add';
            this.initComponents();
          }
          this.showDialog = true;
        },
        initComponents() {
          this.componentMap.forEach((value, key) =>{
            if (this.template.content.components.indexOf(key) < 0 && this.components.indexOf(key) < 0) {
              this.components.push(key);
            }
          });
          this.template.content.components.forEach(item => {
            let preview = this.componentMap.get(item);
            if (preview && preview.type != 'custom') {
              this.previews.push(preview);
            } else {
              if (this.template.content.customComponent) {
                let customComponent = this.template.content.customComponent.get(item.toString());
                if (customComponent) {
                  this.previews.push({id: item, title: customComponent.title, content: customComponent.content});
                }
              }
            }
          });
        },
        handleClose() {
          this.showDialog = false;
        },
        change(evt) {
          if (evt.added) {
            let preview = evt.added.element;
            if ( preview.type == 'system') {
              for (let i = 0; i < this.components.length; i++) {
                this.deleteComponentById(preview.id);
              }
            }
          }
        },
        clonePreview(componentId) {
          let component = this.componentMap.get(componentId);
          let id = componentId;
          if (component.type != 'system') {
            id = this.generateComponentId();
          }
          return {
            id: id,
            name: component.name,
            type: component.type,
          };
        },
        handleDelete(preview) {
          if (this.previews.length == 1) {
            this.$warning('请至少保留一个组件');
            return;
          }
          for (let i = 0; i < this.previews.length; i++) {
            if (this.previews[i].id == preview.id) {
              this.previews.splice(i,1);

              if (preview.type == 'system') {
                this.components.push(preview.id);
              }
              break;
            }
          }
        },
        generateComponentId() {
          return Date.parse(new Date()) + Math.ceil(Math.random()*100000);
        },
        deleteComponentById(id) {
          for (let i = 0; i < this.components.length; i++) {
            if (this.components[i] == id) {
              this.components.splice(i,1);
              break;
            }
          }
        },
        getTemplateById(id) {
          this.$get('/case/report/template/get/' + id, (response) =>{
            this.template = response.data;
            this.template.content = JSON.parse(response.data.content);
            if (this.template.content.customComponent) {
              this.template.content.customComponent = jsonToMap(this.template.content.customComponent);
            }
            this.initComponents();
          });
        },
        handleSave() {
          if (this.template.name == '') {
            this.$warning('请填写模版名称');
            return;
          }
          let param = {};
          this.buildParam(param);
          this.$post('/case/report/template/' + this.type, param, () =>{
            this.$success('保存成功');
            this.showDialog = false;
            this.$emit('refresh');
          });
        },
        buildParam(param) {
          let content = {};
          content.components = [];
          this.previews.forEach(item => {
            content.components.push(item.id);
            if (!this.componentMap.get(item.id)) {
              content.customComponent = new Map();
              content.customComponent.set(item.id, {title: item.title, content: item.content})
            }
          });
          param.name = this.template.name;
          if (content.customComponent) {
            content.customComponent = mapToJson(content.customComponent);
          }
          param.content = JSON.stringify(content);
          if (this.type == 'edit') {
            param.id = this.template.id;
          }
          if (this.template.workspaceId) {
            param.workspaceId = localStorage.getItem(WORKSPACE_ID);
          }
        }
      }
    }
</script>

<style scoped>

  .el-aside {
    border: 1px solid #EBEEF5;
    box-sizing: border-box;
    min-height: calc(100vh - 70px);
    padding: 20px 20px;
    border-radius: 3px;
    background: white;
    position: absolute;
    width: 250px;
    box-shadow: 0 0 2px 0 rgba(31,31,31,0.15), 0 1px 2px 0 rgba(31,31,31,0.15);
  }

  .el-main {
    height: calc(100vh - 70px);
    width: calc(100vw - 320px);
    margin-left: 300px;
    margin-top: 0;
    margin-bottom: 0;
    position: absolute;
  }

  .el-card {
    margin: 5px auto;
    min-height: 300px;
    width: 80%;
  }

  .el-card:hover {
    box-shadow: 0 0 2px 2px #409EFF;
  }

  .description > span {
    display: block;
    padding-bottom: 5px;
  }

  .description {
    margin-bottom: 10px;
  }

  .container {
    height: 100vh;
    background: #F5F5F5;
  }

  .preview {
    position: relative;
  }

  .el-icon-error {
    position: absolute;
    right: 11%;
    top: 13px;
    color: gray;
    display:none;
    font-size: 20px;
  }

  .el-icon-error:hover {
    display: inline;
    color: red;
  }

  .template-component:hover+i {
    display: inline;
  }

</style>
