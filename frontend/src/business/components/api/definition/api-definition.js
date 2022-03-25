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
