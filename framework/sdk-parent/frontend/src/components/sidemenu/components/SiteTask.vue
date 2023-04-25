<template>
  <div>
    <div class="csat-popup over" v-if="cardVisible && taskInfo.length === completeNum && taskInfo.length !== 0">
      <el-card class="box-card">
        <div slot="header" class="clearfix over-header">
          <span style="float: right; padding: 5px 0;" class="moon"  @click="open()">
            <font-awesome-icon :icon="['fa', 'times']" class="icon"/>
          </span>
          <img src="/assets/guide/talent-requirements.png" class="image" alt="MS">
        </div>
        <div class="text" :style="language === 'en-US' ? 'text-align: center;height: 200px' :
         'text-align: center;height: 165px'">
          <span class="title" >
            <img src="../../../assets/guide/flower.png" alt="MS">
            {{ $t("side_task.over.title") }}
          </span>
          <p class="text">
            <img v-for="num in completeNum" src="../../../assets/guide/moon-dark.png" class="over-moon" alt="MS" :key="num
             + 'd'">
            {{ $t("side_task.over.subtitle") }}
          </p>
          <p class="text">{{ $t("side_task.over.desc") }}</p>
          <p class="desc">
            <a href="https://blog.fit2cloud.com/categories/metersphere" target="_blank">
              {{ $t("side_task.over.blog_url") }}
            </a>
          </p>
          <p class="desc">
            <a href="https://space.bilibili.com/510493147/channel/collectiondetail?sid=40439" target="_blank">
              {{ $t("side_task.over.live_url") }}
            </a>
          </p>
        </div>
        <div class="footer">
          <el-button style="float: right; padding: 15px 0;color:#8C8C8C" type="text" @click="notShow()">
            {{$t('side_task.not_show')}}
          </el-button>
        </div>
      </el-card>
    </div>
    <div :class="language === 'en-US' ? 'csat-popup csat-popup-en' : 'csat-popup'" v-else-if="cardVisible && taskInfo.length > 0">
      <template v-for="(item,index) in taskInfo" >
        <el-card :key="item.id" v-if="(index + 1) === taskIndex && checkPermissions(item.permission)"
                 class="box-card" >
          <div slot="header" class="clearfix">
            <el-button style="float: right; padding: 5px 0;" class="moon" type="text" @click="open()">
              <font-awesome-icon :icon="['fa', 'chevron-down']" class="icon"/>
            </el-button>
            <span class="text-header" v-html="$t(item.title)" />
            <el-button style="float: right; padding: 3px 0;margin-right: 25px" type="text" >
              <img v-for="num in completeNum" src="../../../assets/guide/moon-dark.png"
                   class="moon" alt="MS" :key="num + 'd'">
              <img v-for="num in ongoingNum" src="../../../assets/guide/moon-light.png"
                   class="moon" alt="MS" :key="num + 'l'">
              <img v-for="num in incompleteNum" src="../../../assets/guide/moon.png"
                   class="moon" alt="MS" :key="num">
            </el-button>

            <el-progress :percentage="item.percentage" color="#783787"
                         :class="language === 'en-US' ? 'progress-card-en' : 'progress-card-zh'"></el-progress>
          </div>
          <div style="height: 220px">
            <template v-for="(val,i) in item.taskData">
              <div class="text item" v-if="checkPermissions(val.permission)" :key="i">
                <p v-if="val.status === 1">
                  <font-awesome-icon :icon="['far', 'check-circle']" style="color:#783887" class="title-icon"/>
                  <label>{{$t(val.name)}}</label>
                </p>
                <p v-else @click="openGif(val)" :class="checked === val.id ? 'check-p' : ''">
                  <font-awesome-icon :icon="checked === val.id ? ['fas', 'circle-notch'] : ['far', 'circle']"
                                     class="title-icon" rotation='90' />
                  <span :class="checked === val.id ? 'checked' : ''"
                        :style="language === 'en-US' ? 'display:inline-block;width: 299px' :
                        'display:inline-block;width: 248px'">{{$t(val.name)}}</span>
                </p>
              </div>
            </template>
          </div>
          <div class="footer">
            <el-button v-if="taskIndex < totalNum" style="float: right; margin-left: 10px; padding: 15px 0" type="text" @click="next()">
              {{$t('side_task.next')}}
            </el-button>
            <el-button v-if="taskIndex > 1" style="float: right;margin-left: 10px; padding: 15px 0" type="text" @click="prev()">
              {{$t('side_task.prev')}}
            </el-button>
            <el-button style="float: left; padding: 15px 0;color:#8C8C8C" type="text" @click="notShow()">
              {{$t('side_task.not_show')}}
            </el-button>
          </div>
        </el-card>
      </template>
    </div>

    <div :class="language === 'en-US' ? 'csat-popup-gif csat-popup-gif-en' : 'csat-popup-gif'" v-if="gifVisible">
      <el-card class="box-card">
        <div slot="header" class="clearfix">
          <span style="float: right; padding: 5px 0;" class="moon"  @click="closeGif()">
            <font-awesome-icon :icon="['fa', 'times']" class="icon"/>
          </span>
        </div>
        <div class="text" :style="language === 'en-US' ? 'text-align: center;height: 230px' :
         'text-align: center;height: 216px'">
          <el-image
            style="width: 340px;border-radius: 8px;"
            :src="gifData.url"
            :preview-src-list="[gifData.url]" lazy>
            <div slot="placeholder" class="image-slot">
              loading<span class="dot">...</span>
            </div>
          </el-image>
        </div>
        <div :class="language === 'en-US' ? 'gif-footer-en' : 'gif-footer'">
          <el-button type="primary" round size="small" class="is-plain" style="margin-right:10px"
                     @click="gotoPath(gifData.path)">
            {{$t('side_task.to_try')}}
          </el-button>
          <a href="https://www.metersphere.com/video" target="_blank">
            <el-button type="primary" round size="small" class="is-plain">
              {{ $t("side_task.view_video") }}
            </el-button>
          </a>
        </div>
      </el-card>
    </div>

    <div :class="language === 'en-US' ? 'csat-popup-gif csat-popup-gif-en close close-en' : 'csat-popup-gif close'"
         v-if="noviceVisible">
      <el-card class="box-card">
        <div slot="header" class="clearfix">
          <span style="float: right; padding: 5px 0;" class="moon"  @click="closeGif()">
            <font-awesome-icon :icon="['fa', 'times']" class="icon"/>
          </span>
          <span class="text-header" v-html="$t('side_task.close.title')" />
        </div>
        <div style="height: 40px">
          <p class="close-desc">
            {{ $t("side_task.close.subtitle") }}
          </p>
          <p class="close-desc">{{ $t("side_task.close.desc") }}</p>
        </div>
        <div class="gif-footer-close">
          <el-button type="primary" round size="mini" @click="closeGif()">
            {{ $t("side_task.close.continue_btn") }}
          </el-button>
          <el-button type="primary" round size="mini" @click="goContinue()">
            {{ $t("side_task.close.close_btn") }}
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import {hasPermissions} from "../../../utils/permission";
import {updateStatus} from "../../../api/novice";

export default {
  name: "SiteTask",
  data() {
    return {
      noviceVisible: false,
      cardVisible: false,
      gifVisible: false,
      gifData:'',
      taskIndex: 1,
      taskInfo: [],
      interfaceData: '',
      performanceData: '',
      projectData: '',
      uiData: '',
      completeNum: 0,
      ongoingNum: 0,
      incompleteNum: 0,
      totalNum: 0,
      language: localStorage.getItem('language'),
      taskData: [],
      checked: ""
    }
  },
  methods: {
    taskNum() {
      let totalNum = 0
      let completeNum = 0
      let ongoingNum = 0
      this.taskInfo = []
      // status -1 服务未启动或没有访问权限 0服务启动，任务未开始  1完成 2进行中
      this.taskData.forEach(item=>{
        if(item.status === 1){
          completeNum++;
        }else if(item.status === 2){
          ongoingNum++;
        }
        if(item.status !== -1) {
          totalNum++
          this.taskInfo.push(item)
        }
      })

      let incompleteNum = totalNum - completeNum - ongoingNum
      this.completeNum = completeNum
      this.ongoingNum = ongoingNum
      this.incompleteNum = incompleteNum
      this.totalNum = totalNum
    },

    open() {
      this.checked = ""
      this.taskIndex = 1
      this.cardVisible = !this.cardVisible;
      if(this.cardVisible){
        this.taskNum()
      }
      this.$emit("closeBox", this.cardVisible)
      this.gifVisible = this.cardVisible ? this.gifVisible : this.cardVisible;
      this.noviceVisible = this.cardVisible ? false : this.cardVisible;
    },
    openGif(gif) {
      this.checked = gif.id
      this.gifVisible = true
      this.noviceVisible = false
      this.gifData = gif
    },
    closeGif(){
      this.checked = ""
      this.gifVisible = false
      this.noviceVisible = false
    },
    prev() {
      this.taskIndex = this.taskIndex - 1
      this.taskIndex = this.taskIndex < 1 ? 1 : this.taskIndex
      this.closeGif()
    },
    next() {
      this.taskIndex = this.taskIndex + 1
      this.taskIndex = this.taskIndex > this.taskData.length ? this.taskData.length : this.taskIndex
      this.closeGif()
    },
    notShow() {
      this.checked = ""
      this.noviceVisible = true
      this.gifVisible = false
    },
    goContinue () {
      updateStatus(0).then(res=>{
        this.$success(this.$t('side_task.save_success'));
        this.cardVisible = false
        localStorage.setItem("noviceStatus", "0")
        this.$emit("closeNovice", false)
      })
    },
    gotoPath(path){
      this.$router.push(path)
    },
    skipOpen(data, path){
      if(data) {
        this.taskData = data
      }
      if(path) {
        this.taskData.forEach(val => {
          val.taskData.forEach(item => {
            if (item.path === path) {
              this.taskIndex = 1
              this.taskNum()
              this.cardVisible = true
              this.$emit("closeBox", this.cardVisible)
              this.$emit("closeNovice", true)
              this.openGif(item)
            }
          })
        })
      }
    },
    checkPermissions(permission) {
      return hasPermissions(...permission);
    }
  }
}
</script>

<style scoped>
.title {
  font-size: 24px;
  font-weight: 500;
}
.text {
  font-size: 16px;
  font-weight: 300;
}
.desc {
  color:#783887;
  text-align: left;
  margin: 3px 6px;
  font-size: 12px;
  font-weight: 300;
}

.close-desc {
  text-align: left;
  margin: 3px 0;
  font-size: 14px;
  font-weight: 300;
}

.item {
  margin-bottom: 5px;
  margin-left: 35px;
}

.item p {
  font-size: 14px;
  margin-top: 0;
  margin-bottom: 5px;
}
.item p label {
  color:#783887;
  padding: 3px 6px;
}

.item p span {
  padding: 3px 6px;
}
.title-icon {
  color: #303133;
  margin-right: 5px;
}
.item p:hover {
  color:#783887;
  cursor:pointer;
}
.icon {
  color: #0a0a0a;
}
.icon:hover {
  color: #783787;
}
.item p:hover .title-icon {
  color:#783887;
}
.item p:hover span {
  border-radius: 15px;
  background-color:#f5f6f7;
}

.check-p {
  color:#783887;
  cursor:pointer;
}

.check-p .title-icon {
  color:#783887;
}

.checked {
  border-radius: 15px;
  background-color:#f5f6f7;
}

.progress-card-en {
  margin-top: 10px;
  margin-right: 20px;
}

.progress-card-zh {
  margin-top: 10px;
  margin-right: 40px;
}

.footer {
  width: 95%;
  margin-bottom: 20px;
}

.gif-footer {
  width: 100%;
  margin: 10px 0 30px;
  text-align: center;
}

.gif-footer-close {
  width: 100%;
  margin: 10px 0 15px;
  text-align: right;
}

.image-slot {
  padding-top: 100px;
}

.gif-footer-en {
  width: 100%;
  margin: 10px 0 44px ;
  text-align: center;
}

.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both
}

.box-card {
  /*width: 400px;*/
}

.csat-popup {
  position: fixed;
  right: 16px;
  bottom: 170px;
  width: 400px;
  border-radius: 8px;
  overflow: hidden;
  z-index: 1000;
  -webkit-box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border: 1px solid #EBEEF5;
  background-color: #fff;
  -webkit-transition: .3s;
  transition: .3s;
}

.csat-popup-en {
  width: 430px;
}

.csat-popup-gif {
  position: fixed;
  right: 424px;
  bottom: 170px;
  width: 400px;
  border-radius: 8px;
  overflow: hidden;
  z-index: 1000;
  -webkit-box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border: 1px solid #EBEEF5;
  background-color: #fff;
  -webkit-transition: .3s;
  transition: .3s;
}
.csat-popup-gif-en {
  right: 455px;
}
.close {
  bottom: 362px;
}

.close-en {
  bottom: 388px;
}

.text-header {
  font-weight: 700;
  font-size: 24px;
}
.moon {
  width: 20px;
  height: 20px;
}

.over-moon {
  vertical-align: bottom;
  width: 12px;
  height: 12px;
}
.image {
  width: 160px;
  height: 120px;
}

.over-header {
  text-align: center;
}

::v-deep .over .el-card {
  border-radius: 8px;
  background-image: linear-gradient(to bottom, #f4f4f4 44%, #FFF 0);
}

::v-deep .el-card__body {
  padding: 0 10px;
}

::v-deep .csat-popup .el-card__header {
  border-bottom: none;
  padding: 20px 24px 10px 24px;
}

::v-deep .over .el-card__header {
  border-bottom: none;
  padding: 20px 24px 0px 24px;
}

::v-deep .close .el-card__body {
  padding: 0 24px;
}

::v-deep .csat-popup-gif .el-card__header {
  border-bottom: none;
  padding: 20px 20px 10px 24px;
}

::v-deep .el-progress__text{
  color: #783787 !important;
  float: left;
  width: 40px;
  margin-left: 0;
  text-align: center;
  /*margin-right: 8px*/
}
::v-deep .el-progress-bar__outer {
  background-color: #E8EBED !important;
}


</style>
