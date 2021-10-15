<template>
  <ms-container>
    <ms-main-container>
      <el-card class="card">
        <div class="setting-div1">
          <el-card class="home-height" style="position: relative">
            <div style="position: absolute; top: 50%; left: 50%;transform: translate(-50%, -50%);">
              <span class="project-name">{{ project.name }}</span>
              <el-row class="project-item">
                <span class="project-item-title">项目描述：</span><span class="project-item-desc">{{project.description}}</span>
              </el-row>
              <el-row class="project-item">
                <span class="project-item-title">管理员：</span><span class="project-item-desc">{{ project.createUser }}</span>
              </el-row>
              <el-row class="project-item">
                <span class="project-item-title">创建人：</span><span class="project-item-desc">{{ project.createUser }}</span>
              </el-row>
              <el-row class="project-item">
                <span class="project-item-title">项目成员：</span><span class="number">{{ memberSize }}</span>
              </el-row>
              <el-row class="project-item">
                <span class="project-item-title">创建时间：</span><span class="project-item-desc">{{ project.createTime | timestampFormatDate }}</span>
              </el-row>
            </div>
          </el-card>
        </div>
        <div class="setting-div2">
          <el-card class="home-height" style="position: relative">
            <div style="position: absolute; top: 50%; left: 50%;transform: translate(-50%, -50%);">
              <div class="div-item">
                <div style="float: left">
                  <i class="el-icon-user-solid icon-color" @click="click('/project/member')"></i>
                </div>
                <div style="float: left">
                  <span class="title" @click="click('/project/member')">项目成员</span><br/>
                  <span class="desc">添加项目成员以及项目成员管理</span>
                </div>
              </div>
              <div class="div-item">
                <div style="float: left">
                  <i class="el-icon-s-platform icon-color" @click="click('/project/env')"></i>
                </div>
                <div style="float: left">
                  <span class="title" @click="click('/project/env')">项目环境</span><br/>
                  <span class="desc">项目运行环境以及全局配置</span>
                </div>
              </div>
              <div class="div-item">
                <div style="float: left">
                  <i class="el-icon-s-cooperation icon-color" @click="click('/project/file/manage')"></i>
                </div>
                <div style="float: left">
                  <span class="title" @click="click('/project/file/manage')">文件管理</span><br/>
                  <span class="desc">jar包以及资源文件管理</span>
                </div>
              </div>
              <div class="div-item">
                <div style="float: left">
                  <i class="el-icon-s-flag icon-color" @click="click('/project/log')"></i>
                </div>
                <div style="float: left">
                  <span class="title" @click="click('/project/log')">操作记录</span><br/>
                  <span class="desc">项目全部操作过程</span>
                </div>
              </div>
              <div class="div-item">
                <div style="float: left">
                  <i class="el-icon-document icon-color" @click="click('/project/code/segment')"></i>
                </div>
                <div style="float: left">
                  <span class="title" @click="click('/project/code/segment')">自定义代码片段</span><br/>
                  <span class="desc">自定义代码片段</span>
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </el-card>
    </ms-main-container>
  </ms-container>

</template>

<script>
import MsContainer from "@/business/components/common/components/MsContainer";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";
import {getCurrentProjectID} from "@/common/js/utils";
export default {
  name: "ProjectHome",
  components: {MsMainContainer, MsContainer},
  data() {
    return {
      project: {
        name: '',
        description: '',
        createTime: '',
        createUser: ''
      },
      memberSize: 0
    }
  },
  methods: {
    click(str) {
      this.$router.push(str);
    }
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    }
  },
  created() {
    this.$get('/project/get/' + this.projectId, res => {
      this.project = res.data;
    })

    this.$get('/project/member/size/' + this.projectId, res => {
      this.memberSize = res.data;
    })
  }
}
</script>

<style scoped>
.home-height {
  /*height: calc(100vh - 100px);*/
  height: 55vh;
  width: 55vh;
}

.first-left-card {
  /*height: calc(100vh - 100px);*/
  /*line-height: calc(100vh - 200px);*/
  height: 55vh;
  line-height: 350px;
  width: 40%;
  float: left;
}

.first-right-card {
  /*height: calc(100vh - 100px);*/
  height: 55vh;
  width: 60%;
  float: right;
}

.project-name {
  text-align: center;
  color: var(--primary_color);
  font-size: 20px;
  user-select: none;
}

.project-item {
  margin-top: 25px;
  font-size: 16px;
  min-width: 220px;
}
/**/
.setting-div1 {
  position: absolute;
  top: 50%;
  left: 20%;
  min-width: 50px;
  min-height: 50px;
  transform: translate(-20%, -50%);
}

.setting-div2 {
  position: absolute;
  top: 50%;
  left: 80%;
  min-width: 50px;
  min-height: 50px;
  transform: translate(-80%, -50%);
}

.icon-color {
  font-size: 35px;
  color: var(--primary_color);
  margin-right: 6px;
}

.div-item {
  width: 100%;
  padding: 10px;
  height: 50px;
  min-width: 230px;
}

.icon-color:hover, .title:hover {
  cursor: pointer;
}

.title {
  font-size: 10px;
}

.card {
  height: calc(100vh - 100px);
  position: relative;
}

.number {
  font-size: 18px;
  color: var(--primary_color);
}

.project-item-title {
  font-size: 15px;
}

.project-item-desc {
  font-size: 14px;
}
</style>
