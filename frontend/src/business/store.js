import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex);

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
  },
  actions: {},
  getters: {}
}

export default new Vuex.Store({
  modules: {
    api: API
  }
})
