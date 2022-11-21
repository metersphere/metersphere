import { getUUID } from "metersphere-frontend/src/utils";

/**
 * options: JXM转换的JSON对象(xml2json)
 */
export default class Element {
  constructor(options = {}) {
    this.id = getUUID();
    this.type = options.type || "element";
    this.name = options.name;

    if (options.attributes) {
      if (options.attributes.testclass && this.name === undefined) {
        this.name = options.attributes.testclass;
      }
      if (options.attributes.enabled) {
        this.enabled = options.attributes.enabled === "true";
      }
    }
  }
}
