import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex);

const store = new Vuex.Store({
  state: {
    roles: []
  },
  mutations: {
    setRoles(state, data) {
      state.roles = data
    }
  }
})

export default store


