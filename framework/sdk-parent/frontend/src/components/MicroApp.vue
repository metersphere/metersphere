<template>
  <div class="micro-app">
    <div ref="child1"></div>
  </div>
</template>

<script>
import {initGlobalState, loadMicroApp} from 'qiankun';

export default {
  name: 'MicroApp',
  props: {
    to: String,
    service: String,
    routeParams: null,
    routeName: null
  },
  data() {
    return {
      messageEvent: initGlobalState({event: null}),
      microApp: null,
    }
  },

  mounted() {
    this.microAppInit()
    this.messageInit()
  },
  methods: {
    microAppInit() {
      const microPorts = JSON.parse(sessionStorage.getItem("micro_ports"));
      let app = {
        name: 'micro-app-' + this.service,
        container: this.$refs.child1,
        entry: '//127.0.0.1:' + (microPorts[this.service] - 4000),
        props: {
          defaultPath: this.to,
          routeParams: this.routeParams,
          routeName: this.routeName,
          eventBus: this.$EventBus
        }
      };
      if (process.env.NODE_ENV !== 'development') {
        // 替换成后端的端口
        app.entry = app.entry.replace(/127\.0\.0\.1:\d+/g, window.location.host + "/" + this.service);
      }
      this.microApp = loadMicroApp(app);
    },
    messageInit() {
      this.messageEvent.onGlobalStateChange((state, prev) => {});
    },
    changeState() {
      this.messageEvent.setGlobalState({b: 1});
    }
  },
  beforeDestroy() {
    this.messageEvent.offGlobalStateChange()
    this.microApp.unmount();
  }
}
</script>

<style lang="scss" scoped>

</style>
