import HashTreeElement from "@/business/components/api/definition/components/jmeter/hashtree";

export default class MsUiCommand extends HashTreeElement {
  constructor(options = {}) {
    super(options);
    this.type = "MsUiCommand";
    this.clazzName = "io.metersphere.xpack.ui.hashtree.MsUiCommand";

    this.enable = true;
    this.resourceId = null;
    this.index = null;

    this.value = null;
    this.target = null;
    this.targets = null;
    this.valueVO = {};
    this.targetVO = {};

    this.command = null;
    this.comment = null;
    this.description = null;

    this.commandConfig = null;
    this.hashTree = [];
    this.preCommands = [];
    this.postCommands = [];
    this.vo = null;
  }
}

export const schema = {
  MsUiCommand: MsUiCommand
}

