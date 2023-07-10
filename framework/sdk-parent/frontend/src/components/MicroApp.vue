<template>
  <div class="micro-app">
    <div ref="child1"></div>
  </div>
</template>

<script>
import { initGlobalState, loadMicroApp } from "qiankun";

export default {
  name: "MicroApp",
  props: {
    to: String,
    service: String,
    routeParams: null,
    routeName: null,
  },
  data() {
    return {
      messageEvent: initGlobalState({ event: null }),
      microApp: null,
      updateTask: null,
    };
  },
  watch: {
    routeParams: {
      handler() {
        this.microAppUpdate();
      },
      deep: true,
    },
    to(val) {
      if (val) {
        this.microAppUpdate();
      }
    },
    service(val) {
      if (val) {
        this.microAppInit();
        this.messageInit();
      }
    },
  },
  mounted() {
    this.microAppInit();
    this.messageInit();
  },
  methods: {
    microAppUpdate() {
      if (this.updateTask) {
        // 如果当前还有正在执行的更新任务，则清除，重新设置更新任务
        clearTimeout(this.updateTask);
      }
      // 防抖缓冲，避免多个被watch的属性变化后触发多次更新，Vue的响应式更新会在微任务队列清空前完成，所以这里设置0ms即可
      this.updateTask = setTimeout(() => {
        this.microApp?.update({
          defaultPath: this.to,
          routeParams: this.routeParams,
          routeName: this.routeName,
        });
        this.$nextTick(() => {
          // 视图更新完毕后再清除更新任务，才可触发下次的更新
          this.updateTask = null;
        });
      }, 0);
    },
    microAppInit() {
      const microPorts = JSON.parse(sessionStorage.getItem("micro_ports"));
      let app = {
        name: "micro-app-" + this.service,
        container: this.$refs.child1,
        entry: "//127.0.0.1:" + (microPorts[this.service] - 4000),
        props: {
          defaultPath: this.to,
          routeParams: this.routeParams,
          routeName: this.routeName,
          eventBus: this.$EventBus,
        },
      };
      if (process.env.NODE_ENV !== "development") {
        // 替换成后端的端口
        app.entry = app.entry.replace(
          /127\.0\.0\.1:\d+/g,
          window.location.host + "/" + this.service
        );
      }
      this.microApp = loadMicroApp(app);
    },
    messageInit() {
      this.messageEvent.onGlobalStateChange((state, prev) => {});
    },
    changeState() {
      this.messageEvent.setGlobalState({ b: 1 });
    },
  },
  beforeDestroy() {
    this.messageEvent.offGlobalStateChange();
    this.microApp.unmount();
  },
};
</script>

<style lang="scss" scoped></style>
