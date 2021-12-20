<template>
  <ms-container>
    <ms-main-container>
      <el-card class="card" v-loading="result.loading">
        <el-row type="flex" justify="space-around" :gutter="10">
          <el-col :xs="12" :sm="12" :md="11" :lg="11" :xl="10" class="card-col">
            <el-card class="project-info-card">
              <div class="project-info-card-div">
                <span class="project-name">{{ project.name }}</span>
                <i class="el-icon-edit project-edit" @click="edit" v-permission="['PROJECT_MANAGER:READ+EDIT']"></i>
                <el-row class="project-item">
                  <span class="project-item-title">{{ $t('project.desc') }}：</span>
                  <span class="project-item-desc">{{ project.description }}</span>
                </el-row>
                <el-row class="project-item">
                  <span class="project-item-title">{{ $t('project.manage_people') }}：</span>
                  <span class="project-item-desc">{{ project.createUser }}</span>
                </el-row>
                <el-row class="project-item">
                  <span class="project-item-title">{{ $t('project.creator') }}：</span>
                  <span class="project-item-desc">{{ project.createUser }}</span>
                </el-row>
                <el-row class="project-item">
                  <span class="project-item-title">{{ $t('project.member') }}：</span>
                  <span class="number" @click="click('/project/member', ['PROJECT_USER:READ'])">{{ memberSize }}</span>
                </el-row>
                <el-row class="project-item">
                  <span class="project-item-title">{{ $t('project.create_time') }}：</span>
                  <span class="project-item-desc">{{ project.createTime | timestampFormatDate }}</span>
                </el-row>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="12" :sm="12" :md="11" :lg="11" :xl="10" class="card-col">
            <el-card class="project-menu-card">
              <div class="project-menu-card-div">
                <div class="div-item">
                  <div style="float: left">
                    <i class="el-icon-user-solid icon-color"
                       @click="click('/project/member', ['PROJECT_USER:READ'])">
                    </i>
                  </div>
                  <div style="float: left">
                    <span class="title" @click="click('/project/member', ['PROJECT_USER:READ'])">
                      {{ $t('project.member') }}
                    </span><br/>
                    <span class="desc">{{ $t('project.member_desc') }}</span>
                  </div>
                </div>
                <div class="div-item">
                  <div style="float: left">
                    <i class="el-icon-user icon-color"
                       @click="click('/project/usergroup', ['PROJECT_GROUP:READ'])">
                    </i>
                  </div>
                  <div style="float: left">
                    <span class="title" @click="click('/project/usergroup', ['PROJECT_GROUP:READ'])">
                      {{ $t('group.group_permission') }}
                    </span><br/>
                    <span class="desc">{{ $t('project.group_desc') }}</span>
                  </div>
                </div>
                <div class="div-item">
                  <div style="float: left">
                    <i class="el-icon-s-platform icon-color"
                       @click="click('/project/env', ['PROJECT_ENVIRONMENT:READ'])">
                    </i>
                  </div>
                  <div style="float: left">
                    <span class="title" @click="click('/project/env', ['PROJECT_ENVIRONMENT:READ'])">
                      {{ $t('project.env') }}
                    </span><br/>
                    <span class="desc">{{ $t('project.env_desc') }}</span>
                  </div>
                </div>
                <div class="div-item">
                  <div style="float: left">
                    <i class="el-icon-s-cooperation icon-color"
                       @click="click('/project/file/manage', ['PROJECT_FILE:READ+JAR', 'PROJECT_FILE:READ+FILE'])">
                    </i>
                  </div>
                  <div style="float: left">
                    <span class="title"
                          @click="click('/project/file/manage', ['PROJECT_FILE:READ+JAR', 'PROJECT_FILE:READ+FILE'])">
                      {{ $t('project.file_manage') }}
                    </span><br/>
                    <span class="desc">{{ $t('project.file_desc') }}</span>
                  </div>
                </div>
                <div class="div-item">
                  <div style="float: left">
                    <i class="el-icon-document icon-color"
                       @click="click('/project/code/segment', ['PROJECT_CUSTOM_CODE:READ'])">
                    </i>
                  </div>
                  <div style="float: left">
                    <span class="title"
                          @click="click('/project/code/segment', ['PROJECT_CUSTOM_CODE:READ'])">
                      {{ $t('project.code_segment.code_segment') }}
                    </span><br/>
                    <span class="desc">{{ $t('project.code_segment_desc') }}</span>
                  </div>
                </div>
                <div class="div-item">
                  <div style="float: left">
                    <i class="el-icon-s-flag icon-color"
                       @click="click('/project/log', ['PROJECT_OPERATING_LOG:READ'])">
                    </i>
                  </div>
                  <div style="float: left">
                    <span class="title" @click="click('/project/log', ['PROJECT_OPERATING_LOG:READ'])">
                      {{ $t('project.log') }}
                    </span><br/>
                    <span class="desc">{{ $t('project.log_desc') }}</span>
                  </div>
                </div>
                <div class="div-item">
                  <div style="float: left">
                    <i class="el-icon-mobile-phone icon-color"
                       @click="click('/project/app', ['PROJECT_APP_MANAGER:READ+EDIT'])">
                    </i>
                  </div>
                  <div style="float: left">
                    <span class="title" @click="click('/project/app', ['PROJECT_APP_MANAGER:READ+EDIT'])">
                      {{ $t('project.app_manage') }}
                    </span><br/>
                    <span class="desc">{{ $t('project.app_manage_desc') }}</span>
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-card>

      <edit-project ref="editProject" :is-show-app="isShowApp"/>
    </ms-main-container>
  </ms-container>

</template>

<script>
import MsContainer from "@/business/components/common/components/MsContainer";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";
import {getCurrentProjectID, hasPermission} from "@/common/js/utils";
import EditProject from "@/business/components/project/menu/EditProject";

export default {
  name: "ProjectHome",
  components: {MsMainContainer, MsContainer, EditProject},
  data() {
    return {
      project: {
        name: '',
        description: '',
        createTime: '',
        createUser: ''
      },
      memberSize: 0,
      result: {},
      isShowApp: false
    }
  },
  methods: {
    click(str, permissions) {
      for (let permission of permissions) {
        if (hasPermission(permission)) {
          this.$router.push(str);
          return;
        }
      }
      this.$warning("无操作权限！");
    },
    edit() {
      this.$refs.editProject.edit(this.project);
    }
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    }
  },
  activated() {
    this.result = this.$get('/project/get/' + this.projectId, res => {
      this.project = res.data;
    })
    this.result = this.$get('/project/member/size/' + this.projectId, res => {
      this.memberSize = res.data;
    })
  }
}
</script>

<style scoped>
.project-info-card, .project-menu-card {
  height: 500px;
  position: relative;
}

.project-info-card-div, .project-menu-card-div {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.project-menu-card {
  height: 500px;
}

.project-name {
  font-weight: bold;
  text-align: center;
  color: var(--primary_color);
  font-size: 18px;
  user-select: none;
}

.project-item {
  margin-top: 25px;
  font-size: 16px;
  min-width: 220px;
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
  font-size: 15px;
  color: var(--primary_color);
}

.number:hover {
  cursor: pointer;
}

.project-item-title {
  font-size: 15px;
  user-select: none;
}

.project-item-desc {
  font-size: 14px;
  user-select: none;
}

.card-col {
  margin-top: 80px;
}

.project-edit {
  color: var(--primary_color);
  font-size: 14px;
  margin-left: 8px;
}

.project-edit:hover {
  cursor: pointer;
}

.desc {
  color: #989292;
}
</style>
