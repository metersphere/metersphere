import { library } from "@fortawesome/fontawesome-svg-core";
import { faBars } from "@fortawesome/free-solid-svg-icons/faBars";
import { faTimes } from "@fortawesome/free-solid-svg-icons/faTimes";
import { faExpandAlt } from "@fortawesome/free-solid-svg-icons/faExpandAlt";
import { faCompressAlt } from "@fortawesome/free-solid-svg-icons/faCompressAlt";
import { faSitemap } from "@fortawesome/free-solid-svg-icons/faSitemap";
import { faListUl } from "@fortawesome/free-solid-svg-icons/faListUl";
import { faQuestionCircle } from "@fortawesome/free-solid-svg-icons/faQuestionCircle";
import { faLanguage } from "@fortawesome/free-solid-svg-icons/faLanguage";
import { faPlus } from "@fortawesome/free-solid-svg-icons/faPlus";
import { faBell } from "@fortawesome/free-solid-svg-icons/faBell";
import { faCompass } from "@fortawesome/free-solid-svg-icons/faCompass";
import { faChevronDown } from "@fortawesome/free-solid-svg-icons/faChevronDown";
import { faCircleNotch } from "@fortawesome/free-solid-svg-icons/faCircleNotch";
import { faTasks } from "@fortawesome/free-solid-svg-icons/faTasks";
import { faTag } from "@fortawesome/free-solid-svg-icons/faTag";
import { faAlignJustify } from "@fortawesome/free-solid-svg-icons/faAlignJustify";

import { faCheckCircle } from "@fortawesome/free-regular-svg-icons/faCheckCircle";
import { faCircle } from "@fortawesome/free-regular-svg-icons/faCircle";
import { faAddressCard } from "@fortawesome/free-regular-svg-icons/faAddressCard";
import { faListAlt } from "@fortawesome/free-regular-svg-icons/faListAlt";
import { faArrowRight } from "@fortawesome/free-solid-svg-icons/faArrowRight";

import { faGithubSquare } from "@fortawesome/free-brands-svg-icons/faGithubSquare";
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";

export default {
  install(Vue) {
    library.add(
      faBars,
      faTimes,
      faExpandAlt,
      faCompressAlt,
      faSitemap,
      faListUl,
      faQuestionCircle,
      faLanguage,
      faPlus,
      faBell,
      faCompass,
      faChevronDown,
      faCheckCircle,
      faCircleNotch,
      faTasks,
      faTag,
      faAlignJustify,
      faAddressCard,
      faListAlt,
      faArrowRight,
      faGithubSquare,
      faCircle
    );
    Vue.component("font-awesome-icon", FontAwesomeIcon);
  },
};
