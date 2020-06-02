import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex);

const Common = {
  state: {
    projectId: ""
  },
  mutations: {
    setProjectId(state, projectId) {
      state.projectId = projectId;
    }
  }
}

const API = {
  state: {
    test: {}
  },
  mutations: {
    setTest(state, test) {
      state.test = test;
    },
    clearTest(state) {
      state.test = {};
    }
  }
}

export default new Vuex.Store({
  modules: {
    api: API,
    common: Common
  }
})
