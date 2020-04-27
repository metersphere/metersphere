export const get = [{
  "headers": [{"name": "Upgrade-Insecure-Requests", "value": "1"}, {
    "name": "User-Agent",
    "value": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.122 Safari/537.36"
  }, {
    "name": "Accept",
    "value": "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"
  }],
  "label": "https://www.baidu.com/",
  "method": "GET",
  "requestId": "21478",
  "request_subtype": "",
  "request_type": "top_level",
  "tabId": 394,
  "timestamp": 1587958164590,
  "url": "https://www.baidu.com/"
}, {
  "headers": [{"name": "Accept", "value": "application/json, text/javascript, */*; q=0.01"}, {
    "name": "User-Agent",
    "value": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.122 Safari/537.36"
  }],
  "label": "https://www.baidu.com/sugrec?prod=pc_his&from=pc_web&json=1&sid=1447_31124_21122_31424_31341_30903_31229_30824_26350_31164_31195&hisdata=&csor=0",
  "method": "GET",
  "requestId": "21522",
  "request_subtype": "",
  "request_type": "ajax",
  "tabId": 394,
  "timestamp": 1587958165726,
  "url": "https://www.baidu.com/sugrec?prod=pc_his&from=pc_web&json=1&sid=1447_31124_21122_31424_31341_30903_31229_30824_26350_31164_31195&hisdata=&csor=0"
}, {
  "headers": [{"name": "Accept", "value": "text/plain, */*; q=0.01"}, {
    "name": "User-Agent",
    "value": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.122 Safari/537.36"
  }, {"name": "X-Requested-With", "value": "XMLHttpRequest"}],
  "label": "https://www.baidu.com/home/xman/data/tipspluslist?indextype=manht&_req_seqid=0xacf4e73f00017936&asyn=1&t=1587958166015&sid=1447_31124_21122_31424_31341_30903_31229_30824_26350_31164_31195",
  "method": "GET",
  "requestId": "21540",
  "request_subtype": "",
  "request_type": "ajax",
  "tabId": 394,
  "timestamp": 1587958166018,
  "url": "https://www.baidu.com/home/xman/data/tipspluslist?indextype=manht&_req_seqid=0xacf4e73f00017936&asyn=1&t=1587958166015&sid=1447_31124_21122_31424_31341_30903_31229_30824_26350_31164_31195"
}, {
  "headers": [{"name": "Accept", "value": "text/plain, */*; q=0.01"}, {
    "name": "User-Agent",
    "value": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.122 Safari/537.36"
  }, {"name": "X-Requested-With", "value": "XMLHttpRequest"}],
  "label": "https://www.baidu.com/home/msg/data/personalcontent?num=8&indextype=manht&_req_seqid=0xacf4e73f00017936&asyn=1&t=1587958166028&sid=1447_31124_21122_31424_31341_30903_31229_30824_26350_31164_31195",
  "method": "GET",
  "requestId": "21541",
  "request_subtype": "",
  "request_type": "ajax",
  "tabId": 394,
  "timestamp": 1587958166031,
  "url": "https://www.baidu.com/home/msg/data/personalcontent?num=8&indextype=manht&_req_seqid=0xacf4e73f00017936&asyn=1&t=1587958166028&sid=1447_31124_21122_31424_31341_30903_31229_30824_26350_31164_31195"
}];

export const post = [{
  "body": ["{\"id\":null,\"projectId\":\"e15d2d02-7404-485f-944b-2f2dbd456e3e\",\"name\":\"test second\",\"scenarioDefinition\":\"[{\\\"name\\\":null,\\\"url\\\":null,\\\"variables\\\":[],\\\"headers\\\":[],\\\"requests\\\":[{\\\"randomId\\\":2311,\\\"name\\\":null,\\\"url\\\":null,\\\"method\\\":\\\"GET\\\",\\\"parameters\\\":[],\\\"headers\\\":[],\\\"body\\\":{\\\"type\\\":null,\\\"text\\\":null,\\\"kvs\\\":[]},\\\"assertions\\\":{\\\"text\\\":[],\\\"regex\\\":[],\\\"responseTime\\\":{\\\"type\\\":\\\"RESPONSE_TIME\\\",\\\"responseInTime\\\":null}},\\\"extract\\\":[]}]}]\"}"],
  "headers": [{"name": "Accept", "value": "application/json, text/plain, */*"}, {
    "name": "User-Agent",
    "value": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.122 Safari/537.36"
  }, {"name": "Content-Type", "value": "application/json;charset=UTF-8"}],
  "label": "http://localhost:8080/api/save",
  "method": "POST",
  "requestId": "26129",
  "request_subtype": "",
  "request_type": "ajax",
  "tabId": 466,
  "timestamp": 1587968228917,
  "url": "http://localhost:8080/api/save"
}]


export const scenario = [{
  "name": "Scenario 1",
  "url": null,
  "variables": [],
  "headers": [],
  "requests": [{
    "randomId": 6271,
    "name": "Request 1",
    "url": "https://www.baidu.com",
    "method": "GET",
    "parameters": [{"key": "flag", "value": "test"}],
    "headers": [{"key": "test_header", "value": "test_heade_value"}],
    "body": {"type": null, "text": null, "kvs": []},
    "assertions": {
      "text": [],
      "regex": [{"type": "REGEX", "subject": "HTTP_CODE", "expression": "^200$", "description": "equals: 200"}],
      "responseTime": {"type": "RESPONSE_TIME", "responseInTime": null}
    },
    "extract": []
  }]
}]
