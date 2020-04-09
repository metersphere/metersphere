import {library} from '@fortawesome/fontawesome-svg-core'
import {fas} from '@fortawesome/free-solid-svg-icons'
import {far} from '@fortawesome/free-regular-svg-icons'
import {FontAwesomeIcon} from '@fortawesome/vue-fontawesome'

export default {
  install(Vue) {
    library.add(fas, far);
    Vue.component('font-awesome-icon', FontAwesomeIcon);
  }
}
