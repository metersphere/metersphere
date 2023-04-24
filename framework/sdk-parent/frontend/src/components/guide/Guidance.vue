<template>
  <el-dropdown size="medium" @command="handleCommand" class="ms-header-menu align-right">
    <span class="dropdown-link">
      <font-awesome-icon class="icon global focusing" :icon="['fas', 'question-circle']" />
    </span>
    <template v-slot:dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="guidance">{{ $t('commons.page_guidance') }}</el-dropdown-item>
        <el-dropdown-item command="introduction">{{ $t('commons.function_introduction') }}</el-dropdown-item>
        <el-dropdown-item command="novice">{{ $t('commons.novice_journey') }}</el-dropdown-item>
      </el-dropdown-menu>
    </template>
    <ms-introduction ref="introduction" @skipOpen="skipOpen" />
    <ms-side-menus ref="sideMenu"/>
  </el-dropdown>
</template>

<script>
import MsIntroduction from "../../components/guide/components/Introduction";
import MsSideMenus from "../../components/sidemenu/SideMenus";
import {getSideTask} from "../../api/novice";


export default {
  name: "MsGuidance",
  components: {MsIntroduction,MsSideMenus},
  data() {
    return {
    };
  },
  mounted() {
    this.$refs.introduction.resVisible = localStorage.getItem("introduction") && localStorage.getItem("introduction")
    !== 'false' && (localStorage.getItem("guide") === 'true' || localStorage.getItem("step") > 1);
    this.checkStep()
  },
  methods: {
    handleCommand(command) {
      switch (command) {
        case "introduction":
          this.$refs.introduction.openNext();
          break;
        case "guidance":
          localStorage.setItem("resetGuide", 'true')
          localStorage.setItem("guide", 'false')
          localStorage.removeItem('step')
          if(this.$route.path.includes('project')){
            this.$router.push('/project/home')
            this.$router.go(0);
          }else{
            this.$router.push('/project/home')
          }
          break;
        case "novice":
          this.$refs.sideMenu.toggle(2);
          break;
        default:
          break;
      }
    },
    checkStep(){
      getSideTask().then(res=> {
        if (res.data.length > 0 && res.data[0].guideStep) {
          localStorage.setItem('step', res.data[0].guideStep)
          localStorage.setItem("noviceStatus", res.data[0].status)
        } else {
          localStorage.setItem('guide','false')
        }
        let microApps = JSON.parse(sessionStorage.getItem("micro_apps"));
        if(localStorage.getItem("guide") === 'false' && microApps && microApps['project']) {
          let step = localStorage.getItem("step") && localStorage.getItem("resetGuide") !== 'true' ?
            localStorage.getItem("step") : "1"
          localStorage.setItem("step", step)

          if(step !== '3'){
            if(this.$route.path.includes('/project/home')){
              this.initStepAll()
            }else{
              this.initStep()
            }
          }
        }
      })
    },
    initStepAll() {
      const _this = this
      _this.$nextTick(() => {
        const tour = _this.$shepherd({
          useModalOverlay: true,
          exitOnEsc: false,
          keyboardNavigation: false,
          defaultStepOptions: {
            scrollTo: {
              behavior: 'smooth',
              block: 'center'
            },
            canClickTarget: false,
            // 高亮元素四周要填充的空白像素
            modalOverlayOpeningPadding: 0,
            // 空白像素的圆角
            modalOverlayOpeningRadius: 4
          }
        })
        tour.addSteps([
          {
            attachTo: {
              element: document.querySelector('.shepherd-workspace'),
              on: 'bottom-start'
            },
            buttons: [
              {
                action: function() {
                  _this.$refs.introduction.resVisible = true
                  return _this.gotoCancel(this, true)
                },
                classes: 'close-btn',
                text: _this.$t("shepherd.exit")
              },
              {
                action: function() {
                  return _this.gotoNext(this, null, 2)
                },
                classes: 'shep-btn',
                text: _this.$t("shepherd.next")
              }
            ],
            title: _this.$t("shepherd.step1.title"),
            text: _this.$t("shepherd.step1.text")
          },
          {
            attachTo: {
              element: document.querySelector('.shepherd-menu'),
              on: 'right'
            },
            buttons: [
              {
                action: function() {
                  _this.$refs.introduction.resVisible = localStorage.getItem("step") > 1
                  return _this.gotoCancel(this, true)
                },
                classes: 'close-btn',
                text: _this.$t("shepherd.exit")
              },
              {
                action: function() {
                  return _this.gotoNext(this, '/project/home', 3)
                },
                classes: 'shep-btn',
                text: _this.$t("shepherd.next")
              }
            ],
            title: _this.$t("shepherd.step2.title"),
            text: _this.$t("shepherd.step2.text")
          },
          {
            attachTo: {
              element: document.querySelector('.shepherd-project'),
              on: 'bottom-end'
            },
            classes: "custom-width",
            buttons: [
              {
                action: function() {
                  _this.$refs.introduction.resVisible = localStorage.getItem("step") > 1
                  return _this.gotoCancel(this, true)
                },
                classes: 'close-btn',
                text: _this.$t("shepherd.exit")
              },
              {
                action: function() {
                  return _this.gotoNext(this, null, 4)
                },
                classes: 'shep-btn',
                text: _this.$t("shepherd.next")
              }
            ],
            title: _this.$t("shepherd.step3.title"),
            text: _this.$t("shepherd.step3.text")
          },
          {
            attachTo: {
              element: document.querySelector('.shepherd-project-menu'),
              on: 'bottom-start'
            },
            buttons: [
              {
                action: function() {
                  _this.$refs.introduction.resVisible = localStorage.getItem("step") > 1
                  return _this.gotoCancel(this, true)
                },
                classes: 'close-btn',
                text: _this.$t("shepherd.exit")
              },
              {
                action: function() {
                  return _this.gotoNext(this, '/project/home', 5)
                },
                classes: 'shep-btn',
                text: _this.$t("shepherd.next")
              }
            ],
            title: _this.$t("shepherd.step4.title"),
            text: _this.$t("shepherd.step4.text")
          },
          {
            arrow:true,
            modalOverlayOpeningPadding: 8,
            attachTo: {
              element: document.querySelector('.shepherd-project-name'),
              on: 'right'
            },
            buttons: [
              {
                action: function() {
                  _this.$refs.introduction.resVisible = localStorage.getItem("step") > 1
                  return _this.gotoCancel(this, false)
                },
                classes: 'close-btn',
                text: _this.$t("shepherd.know")
              }
            ],
            title: _this.$t("shepherd.step5.title"),
            text: _this.$t("shepherd.step5.text")
          }
        ])
        tour.start()
      })
    },
    initStep() {
      const _this = this
      _this.$nextTick(() => {
        const tour = _this.$shepherd({
          useModalOverlay: true,
          exitOnEsc: false,
          keyboardNavigation: false,
          defaultStepOptions: {
            scrollTo: {
              behavior: 'smooth',
              block: 'center'
            },
            canClickTarget: false,
            // 高亮元素四周要填充的空白像素
            modalOverlayOpeningPadding: 0,
            // 空白像素的圆角
            modalOverlayOpeningRadius: 4
          }
        })
        tour.addSteps([
          {
            attachTo: {
              element: document.querySelector('.shepherd-workspace'),
              on: 'bottom-start'
            },
            buttons: [
              {
                action: function() {
                  _this.$refs.introduction.resVisible = localStorage.getItem("step") > 0
                  return _this.gotoCancel(this, true)
                },
                classes: 'close-btn',
                text: _this.$t("shepherd.exit")
              },
              {
                action: function() {
                  return _this.gotoNext(this, null, 2)
                },
                classes: 'shep-btn',
                text: _this.$t("shepherd.next")
              }
            ],
            title: _this.$t("shepherd.step1.title"),
            text: _this.$t("shepherd.step1.text")
          },
          {
            attachTo: {
              element: document.querySelector('.shepherd-menu'),
              on: 'right'
            },
            buttons: [
              {
                action: function() {
                  _this.$refs.introduction.resVisible = localStorage.getItem("step") > 1
                  return _this.gotoCancel(this, true)
                },
                classes: 'close-btn',
                text: _this.$t("shepherd.exit")
              },
              {
                action: function() {
                  return _this.gotoNext(this, '/project/home', 3)
                },
                classes: 'shep-btn',
                text: _this.$t("shepherd.next")
              }
            ],
            title: _this.$t("shepherd.step2.title"),
            text: _this.$t("shepherd.step2.text")
          },
        ])
        tour.start()
      })
    },
    skipOpen(path){
      if(path){
        this.$refs.sideMenu.skipOpen(path);
      }
    }
  }
};
</script>

<style scoped>
.dropdown-link {
  cursor: pointer;
  font-size: 14px;
  color: rgb(146, 147, 150);
}

.align-right {
  float: right;
}

.icon {
  width: 24px;
}

.ms-header-menu {
  padding-top: 12px;
  width: 24px;
}

.ms-header-menu:hover {
  cursor: pointer;
  border-color: var(--color);
}

</style>


