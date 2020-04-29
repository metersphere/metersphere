<template>

  <el-drawer
    :before-close="handleClose"
    :visible.sync="showDialog"
    :with-header="false"
    size="100%"
    ref="drawer"
    v-loading="result.loading">

    <template v-slot:default="scope">

      <el-row type="flex" class="head-bar">

        <el-col :span="12">

          <div class="name-edit">
            <el-input :placeholder="'请填写模版名称'" v-model="name"/>
            <span v-if="name != ''">{{name}}</span>
            <span v-if="name == ''">请填写模版名称</span>
          </div>

        </el-col>

        <el-col :span="12" class="head-right">
          <el-button plain size="mini" @click="handleClose">{{$t('test_track.return')}}</el-button>
          <el-button type="primary" size="mini" @click="handleClose">{{$t('test_track.save')}}</el-button>
        </el-col>

      </el-row>

      <div class="container">


            <el-aside>

                <div class="description">
                  <span class="title">组件库</span>
                  <span>从组件库把需要使用的组件拖到右侧，预览测试报告的效果。系统组件只能添加一次。自定义组件，可以设定默认的标题和内容。</span>
                </div>

                <draggable
                  class="component-group"
                  :list="components"
                  :group="{ name: 'people', pull: 'clone', put: false }"
                  :clone="cloneDog"
                  @change="log">
                  <template-component-bar  v-for="item in components" :key="item.id" :component="item"/>
                </draggable>

              </el-aside>

            <el-main>
                  <draggable
                    class="preview-group"
                    :list="previews"
                    group="people"
                    @change="log">

                    <base-info-component/>
                    <test-result-component/>
                    <test-result-chart-component/>

                    <el-card class="template-component" v-for="item in previews" :key="item.id">

                      <template v-slot:header>
                        {{item.name}}
                      </template>

                      <ckeditor :editor="editor" v-model="editorData" :config="editorConfig"></ckeditor>

                    </el-card>
                  </draggable>

            </el-main>

          </div>


    </template>

  </el-drawer>

</template>

<script>

  import draggable from 'vuedraggable';
  import ClassicEditor from '@ckeditor/ckeditor5-build-classic';
  import BaseInfoComponent from "./TemplateComponent/BaseInfoComponent";
  import TestResultComponent from "./TemplateComponent/TestResultComponent";
  import TestResultChartComponent from "./TemplateComponent/TestResultChartComponent";
  import TemplateComponentBar from "./TemplateComponentBar";

  let idGlobal = 8;
    export default {
      name: "TestCaseReportTemplateEdit",
      components: {
        TemplateComponentBar,
        TestResultChartComponent,
        TestResultComponent,
        BaseInfoComponent,
        draggable
      },
      data() {
        return {
          showDialog: false,
          template: {},
          result: {},
          name: '',
          type: 'edit',
          components: [
            { name: "基础信息", id: 1 , type: 'system'},
            { name: "测试结果", id: 2 , type: 'system'},
            { name: "测试结果分布", id: 3 ,type: 'system'},
            { name: "自定义模块", id: 4 ,type: 'custom'}
          ],
          previews: [
            { name: "cat 5", id: 5 },
            { name: "cat 6", id: 6 },
            { name: "cat 7", id: 7 }
          ],

          editor: ClassicEditor,
          editorData: '<p>Content of the editor.</p>',
          editorConfig: {
            // The configuration of the editor.
          }

        }
      },
      methods: {
        open() {
          this.showDialog = true;
        },
        handleClose() {
          this.showDialog = false;
        },
        log: function(evt) {
          window.console.log(evt);
        },
        cloneDog({ id }) {
          return {
            id: idGlobal++,
            name: `cat ${id}`
          };
        }
      }
    }
</script>

<style scoped>

  .head-right {
    text-align: right;
  }

  .head-bar {
    background: white;
    height: 45px;
    line-height: 45px;
    padding: 0 10px;
    border: 1px solid #EBEEF5;
    box-shadow: 0 0 2px 0 rgba(31,31,31,0.15), 0 1px 2px 0 rgba(31,31,31,0.15);
  }

  .name-edit:hover span {
    display: none;
  }

  .name-edit .el-input {
    display: none;
    width: 200px;
  }

  .name-edit:hover .el-input{
    display: inline-block;
  }

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

</style>
