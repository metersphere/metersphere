import {Assertions} from "@/business/components/api/definition/model/ApiTestModel";
import {getUUID} from "@/common/js/utils";

export function getProtocolFilter(protocolType) {
  if (protocolType === "HTTP") {
    return [
      {text: 'GET', value: 'GET'},
      {text: 'POST', value: 'POST'},
      {text: 'PUT', value: 'PUT'},
      {text: 'PATCH', value: 'PATCH'},
      {text: 'DELETE', value: 'DELETE'},
      {text: 'OPTIONS', value: 'OPTIONS'},
      {text: 'HEAD', value: 'HEAD'},
      {text: 'CONNECT', value: 'CONNECT'},
    ];
  } else if (protocolType === "TCP") {
    return [
      {text: 'TCP', value: 'TCP'},
    ];
  } else if (protocolType === "SQL") {
    return [
      {text: 'SQL', value: 'SQL'},
    ];
  } else if (protocolType === "DUBBO") {
    return [
      {text: 'DUBBO', value: 'DUBBO'},
      {text: 'dubbo://', value: 'dubbo://'},
    ];
  } else {
    return [
      {text: 'GET', value: 'GET'},
      {text: 'POST', value: 'POST'},
      {text: 'PUT', value: 'PUT'},
      {text: 'PATCH', value: 'PATCH'},
      {text: 'DELETE', value: 'DELETE'},
      {text: 'OPTIONS', value: 'OPTIONS'},
      {text: 'HEAD', value: 'HEAD'},
      {text: 'CONNECT', value: 'CONNECT'},
      {text: 'DUBBO', value: 'DUBBO'},
      {text: 'dubbo://', value: 'dubbo://'},
      {text: 'SQL', value: 'SQL'},
      {text: 'TCP', value: 'TCP'},
    ];
  }
}

export function parse(item) {
  if (item.jsonPath) {
    item.jsonPath.forEach(node => {
      node.enable = item.enable;
    })
  }
  if (item.jsr223) {
    item.jsr223.forEach(node => {
      node.enable = item.enable;
    })
  }
  if (item.regex) {
    item.regex.forEach(node => {
      node.enable = item.enable;
    });
  }
  if (item.xpath2) {
    item.xpath2.forEach(node => {
      node.enable = item.enable;
    })
  }
  if (item.duration && item.duration.value > 0) {
    item.duration.enable = item.enable;
  }
  if (item.document && item.document.data && (item.document.data.json.length > 0 || item.document.data.xml.length > 0)) {
    item.document.enable = item.enable;
  }
}

export function hisDataProcessing(array, request) {
  let assertions = new Assertions({id: getUUID()});
  if (!request.hashTree) {
    request.hashTree = [];
  }
  let isOne = true;
  let assertionsIndex = [];
  if (array) {
    for (let index in array) {
      let item = array[index];
      if (item.type === "Assertions" && isOne) {
        assertions = JSON.parse(JSON.stringify(item));
        parse(assertions);
        isOne = false;
        assertionsIndex.push(item);
      } else if (item.type === "Assertions") {
        parse(item);
        if (item.jsonPath) {
          assertions.jsonPath.push(...item.jsonPath);
        }
        if (item.jsr223) {
          assertions.jsr223.push(...item.jsr223);
        }
        if (item.regex) {
          assertions.regex.push(...item.regex);
        }
        if (item.xpath2) {
          assertions.xpath2.push(...item.xpath2);
        }
        assertionsIndex.push(item);
        if (item.duration && item.duration.value > 0) {
          assertions.duration = item.duration;
        }
        if (item.document && item.document.data && (item.document.data.json.length > 0 || item.document.data.xml.length > 0)) {
          assertions.document = item.document;
        }
      }
    }
  }
  assertionsIndex.forEach(item => {
    const rmIndex = request.hashTree.findIndex((d) => d.id === item.id&&d.resourceId===item.resourceId);
    request.hashTree.splice(rmIndex, 1);
  })

  request.hashTree.push(assertions);
}

export function stepCompute(array, request) {
  let preSize = 0;
  let postSize = 0;
  let ruleSize = 0;
  array.forEach(item => {
    if (["JSR223PreProcessor", "JDBCPreProcessor", "ConstantTimer"].indexOf(item.type) !== -1) {
      preSize++;
    } else if (["JSR223PostProcessor", "JDBCPostProcessor", "Extract"].indexOf(item.type) !== -1) {
      postSize++;
    } else if (item.type === "Assertions") {
      ruleSize = (item.jsonPath.length + item.jsr223.length + item.regex.length + item.xpath2.length);
      if (item.document && item.document.data && (item.document.data.json.length > 0 || item.document.data.xml.length > 0)) {
        ruleSize++;
      }
      if (item.duration && item.duration.value > 0) {
        ruleSize++;
      }
      ruleSize += item.text ? item.text.length : 0;
    }
  })
  request.preSize = preSize;
  request.postSize = postSize;
  request.ruleSize = ruleSize;

}
