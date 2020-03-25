package io.metersphere;

import com.alibaba.fastjson.JSONObject;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import io.metersphere.report.base.RequestStatistics;
import org.junit.Test;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;


public class JtlTest {

    public static List<Metric> beanBuilderExample(String content) {
        HeaderColumnNameMappingStrategy<io.metersphere.Metric> ms = new HeaderColumnNameMappingStrategy<>();
        ms.setType(io.metersphere.Metric.class);
        try (Reader reader = new StringReader(content)) {

            CsvToBean<io.metersphere.Metric> cb = new CsvToBeanBuilder<Metric>(reader)
                    .withType(Metric.class)
                    .withSkipLines(0)
                    .withMappingStrategy(ms)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            return cb.parse();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Test
    public void getRequestStatistics() {
        String jtlString = "timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect\n" +
                "1584602493891,1107,https://rddev2.fit2cloud.com/,200,OK,Thread Group 1-3,text,true,,1473653,6950,3,3,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2F&state=c81989f3-27d5-4b1a-a2db-03ddb06475d5&login=true&scope=openid,232,0,26\n" +
                "1584602493891,235,https://rddev2.fit2cloud.com/-0,302,Found,Thread Group 1-3,,true,,214,567,3,3,https://rddev2.fit2cloud.com/,232,0,26\n" +
                "1584602494128,11,https://rddev2.fit2cloud.com/-1,302,Found,Thread Group 1-3,,true,,615,577,3,3,https://rddev2.fit2cloud.com/dashboard/,11,0,0\n" +
                "1584602494142,33,https://rddev2.fit2cloud.com/-2,200,OK,Thread Group 1-3,text,true,,8068,851,3,3,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2F&state=c81989f3-27d5-4b1a-a2db-03ddb06475d5&login=true&scope=openid,32,0,0\n" +
                "1584602494242,756,https://rddev2.fit2cloud.com/-3,200,OK,Thread Group 1-3,text,true,,1464756,4955,3,3,https://rddev2.fit2cloud.com/login,46,0,0\n" +
                "1584602493891,1154,https://rddev2.fit2cloud.com/,200,OK,Thread Group 1-2,text,true,,1473685,6950,3,3,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2F&state=374d02c0-6e1f-4937-b457-27d6e6ccf264&login=true&scope=openid,232,0,25\n" +
                "1584602493891,235,https://rddev2.fit2cloud.com/-0,302,Found,Thread Group 1-2,,true,,214,567,3,3,https://rddev2.fit2cloud.com/,232,0,25\n" +
                "1584602494128,11,https://rddev2.fit2cloud.com/-1,302,Found,Thread Group 1-2,,true,,615,577,3,3,https://rddev2.fit2cloud.com/dashboard/,11,0,0\n" +
                "1584602494142,35,https://rddev2.fit2cloud.com/-2,200,OK,Thread Group 1-2,text,true,,8068,851,3,3,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2F&state=374d02c0-6e1f-4937-b457-27d6e6ccf264&login=true&scope=openid,35,0,0\n" +
                "1584602494242,803,https://rddev2.fit2cloud.com/-3,200,OK,Thread Group 1-2,text,true,,1464788,4955,3,3,https://rddev2.fit2cloud.com/login,45,0,0\n" +
                "1584602493891,1316,https://rddev2.fit2cloud.com/,200,OK,Thread Group 1-1,text,true,,1473686,6942,3,3,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2F&state=515530c4-0f36-454a-b536-9a11c7d2c47a&login=true&scope=openid,232,0,25\n" +
                "1584602493891,235,https://rddev2.fit2cloud.com/-0,302,Found,Thread Group 1-1,,true,,214,567,3,3,https://rddev2.fit2cloud.com/,232,0,25\n" +
                "1584602494128,12,https://rddev2.fit2cloud.com/-1,302,Found,Thread Group 1-1,,true,,614,577,3,3,https://rddev2.fit2cloud.com/dashboard/,12,0,0\n" +
                "1584602494142,36,https://rddev2.fit2cloud.com/-2,200,OK,Thread Group 1-1,text,true,,8068,850,3,3,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2F&state=515530c4-0f36-454a-b536-9a11c7d2c47a&login=true&scope=openid,35,0,0\n" +
                "1584602494242,965,https://rddev2.fit2cloud.com/-3,200,OK,Thread Group 1-1,text,true,,1464790,4948,3,3,https://rddev2.fit2cloud.com/login,48,0,0\n" +
                "1584602496824,550,https://rddev2.fit2cloud.com/,200,OK,Thread Group 1-5,text,true,,1473644,6950,5,5,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2F&state=211a68fc-eb1e-482d-b5d2-636b411a133e&login=true&scope=openid,54,0,0\n" +
                "1584602496824,54,https://rddev2.fit2cloud.com/-0,302,Found,Thread Group 1-5,,true,,214,567,5,5,https://rddev2.fit2cloud.com/,54,0,0\n" +
                "1584602496878,12,https://rddev2.fit2cloud.com/-1,302,Found,Thread Group 1-5,,true,,615,577,5,5,https://rddev2.fit2cloud.com/dashboard/,12,0,0\n" +
                "1584602496890,29,https://rddev2.fit2cloud.com/-2,200,OK,Thread Group 1-5,text,true,,8074,851,5,5,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2F&state=211a68fc-eb1e-482d-b5d2-636b411a133e&login=true&scope=openid,29,0,0\n" +
                "1584602496922,452,https://rddev2.fit2cloud.com/-3,200,OK,Thread Group 1-5,text,true,,1464741,4955,5,5,https://rddev2.fit2cloud.com/login,20,0,0\n" +
                "1584602496821,559,https://rddev2.fit2cloud.com/,200,OK,Thread Group 1-4,text,true,,1473633,6958,5,5,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2F&state=e6ebc175-e3dc-4c99-933b-f6610688dbfe&login=true&scope=openid,57,0,2\n" +
                "1584602496821,57,https://rddev2.fit2cloud.com/-0,302,Found,Thread Group 1-4,,true,,214,567,5,5,https://rddev2.fit2cloud.com/,57,0,2\n" +
                "1584602496878,11,https://rddev2.fit2cloud.com/-1,302,Found,Thread Group 1-4,,true,,616,577,5,5,https://rddev2.fit2cloud.com/dashboard/,11,0,0\n" +
                "1584602496889,27,https://rddev2.fit2cloud.com/-2,200,OK,Thread Group 1-4,text,true,,8068,852,5,5,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2F&state=e6ebc175-e3dc-4c99-933b-f6610688dbfe&login=true&scope=openid,27,0,0\n" +
                "1584602496919,461,https://rddev2.fit2cloud.com/-3,200,OK,Thread Group 1-4,text,true,,1464735,4962,5,5,https://rddev2.fit2cloud.com/login,20,0,0\n" +
                "1584602499028,73,https://rddev2.fit2cloud.com/auth/realms/cmp/login-actions/authenticate?session_code=YWUneSay4qlQuHRZsD4kPaZDIIR50KJLaNpW7uhsD-Q&execution=c7620733-54ab-46b2-802b-66764e42682b&client_id=cmp-client&tab_id=S8xOQPgCmhQ,400,Bad Request,Thread Group 1-1,text,false,,4469,1745,8,8,https://rddev2.fit2cloud.com/auth/realms/cmp/login-actions/authenticate,73,0,6\n" +
                "1584602499125,0,https://rddev2.fit2cloud.com/dashboard/?state=3d31fe47-47bd-47f2-950d-9d135e0600ef&session_state=191d0330-dd9f-4bb0-8c24-0e3df46e2ff1&code=efe49afa-7afb-4c38-8e0c-38323291938c.191d0330-dd9f-4bb0-8c24-0e3df46e2ff1.fd56cca3-6d54-44aa-b879-a35e79fc1bfc,Non HTTP response code: java.net.URISyntaxException,\"Non HTTP response message: Illegal character in query at index 45: https://rddev2.fit2cloud.com/dashboard/?code=\n" +
                "                                        efe49afa-7afb-4c38-8e0c-38323291938c.191d0330-dd9f-4bb0-8c24-0e3df46e2ff1.fd56cca3-6d54-44aa-b879-a35e79fc1bfc\n" +
                "                                    &state=3d31fe47-47bd-47f2-950d-9d135e0600ef&session_state=191d0330-dd9f-4bb0-8c24-0e3df46e2ff1\",Thread Group 1-1,text,false,,1392,0,8,8,\"https://rddev2.fit2cloud.com/dashboard/?code=\n" +
                "                                        efe49afa-7afb-4c38-8e0c-38323291938c.191d0330-dd9f-4bb0-8c24-0e3df46e2ff1.fd56cca3-6d54-44aa-b879-a35e79fc1bfc\n" +
                "                                    &state=3d31fe47-47bd-47f2-950d-9d135e0600ef&session_state=191d0330-dd9f-4bb0-8c24-0e3df46e2ff1\",0,0,0\n" +
                "1584602499126,21,https://rddev2.fit2cloud.com/dashboard/anonymous/i18n/en_US.json?_t=1577351137654,200,OK,Thread Group 1-1,text,true,,12438,559,8,8,https://rddev2.fit2cloud.com/dashboard/anonymous/i18n/en_US.json?_t=1577351137654,21,0,0\n" +
                "1584602499251,18,https://rddev2.fit2cloud.com/dashboard/web-public/project/html/task-menus.html?_t=1577351137654,200,OK,Thread Group 1-1,text,true,,1916,573,8,8,https://rddev2.fit2cloud.com/dashboard/web-public/project/html/task-menus.html?_t=1577351137654,18,0,0\n" +
                "1584602498833,509,https://rddev2.fit2cloud.com/,200,OK,Thread Group 1-7,text,true,,1473651,6942,8,8,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2F&state=1f8130d4-f1c4-44f5-8633-03cc4892f31c&login=true&scope=openid,39,0,1\n" +
                "1584602498833,39,https://rddev2.fit2cloud.com/-0,302,Found,Thread Group 1-7,,true,,214,567,8,8,https://rddev2.fit2cloud.com/,39,0,1\n" +
                "1584602498872,9,https://rddev2.fit2cloud.com/-1,302,Found,Thread Group 1-7,,true,,614,577,8,8,https://rddev2.fit2cloud.com/dashboard/,9,0,0\n" +
                "1584602498881,18,https://rddev2.fit2cloud.com/-2,200,OK,Thread Group 1-7,text,true,,8074,850,8,8,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2F&state=1f8130d4-f1c4-44f5-8633-03cc4892f31c&login=true&scope=openid,18,0,0\n" +
                "1584602498901,441,https://rddev2.fit2cloud.com/-3,200,OK,Thread Group 1-7,text,true,,1464749,4948,8,8,https://rddev2.fit2cloud.com/login,25,0,0\n" +
                "1584602499325,71,https://rddev2.fit2cloud.com/auth/realms/cmp/login-actions/authenticate?session_code=YWUneSay4qlQuHRZsD4kPaZDIIR50KJLaNpW7uhsD-Q&execution=c7620733-54ab-46b2-802b-66764e42682b&client_id=cmp-client&tab_id=S8xOQPgCmhQ,400,Bad Request,Thread Group 1-2,text,false,,4469,1746,8,8,https://rddev2.fit2cloud.com/auth/realms/cmp/login-actions/authenticate,70,0,4\n" +
                "1584602499445,16,https://rddev2.fit2cloud.com/dashboard/web-public/project/html/notification-menus.html?_t=1577351137654,200,OK,Thread Group 1-1,text,true,,1570,581,8,8,https://rddev2.fit2cloud.com/dashboard/web-public/project/html/notification-menus.html?_t=1577351137654,16,0,0\n" +
                "1584602498832,637,https://rddev2.fit2cloud.com/,200,OK,Thread Group 1-6,text,true,,1473640,6958,8,8,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2F&state=ceda817a-6ac6-4516-9cd8-c1b25429bf94&login=true&scope=openid,50,0,1\n" +
                "1584602498832,50,https://rddev2.fit2cloud.com/-0,302,Found,Thread Group 1-6,,true,,214,567,8,8,https://rddev2.fit2cloud.com/,50,0,1\n" +
                "1584602498882,9,https://rddev2.fit2cloud.com/-1,302,Found,Thread Group 1-6,,true,,616,577,8,8,https://rddev2.fit2cloud.com/dashboard/,9,0,0\n" +
                "1584602498891,35,https://rddev2.fit2cloud.com/-2,200,OK,Thread Group 1-6,text,true,,8068,852,8,8,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2F&state=ceda817a-6ac6-4516-9cd8-c1b25429bf94&login=true&scope=openid,35,0,0\n" +
                "1584602498927,542,https://rddev2.fit2cloud.com/-3,200,OK,Thread Group 1-6,text,true,,1464742,4962,8,8,https://rddev2.fit2cloud.com/login,23,0,0\n" +
                "1584602498836,635,https://rddev2.fit2cloud.com/,200,OK,Thread Group 1-8,text,true,,1473639,6950,8,8,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2F&state=1b42574e-756d-4157-9987-a8e1df496718&login=true&scope=openid,46,0,0\n" +
                "1584602498836,46,https://rddev2.fit2cloud.com/-0,302,Found,Thread Group 1-8,,true,,214,567,8,8,https://rddev2.fit2cloud.com/,46,0,0\n" +
                "1584602498883,12,https://rddev2.fit2cloud.com/-1,302,Found,Thread Group 1-8,,true,,615,577,8,8,https://rddev2.fit2cloud.com/dashboard/,12,0,0\n" +
                "1584602498896,36,https://rddev2.fit2cloud.com/-2,200,OK,Thread Group 1-8,text,true,,8074,851,8,8,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2F&state=1b42574e-756d-4157-9987-a8e1df496718&login=true&scope=openid,36,0,0\n" +
                "1584602498933,538,https://rddev2.fit2cloud.com/-3,200,OK,Thread Group 1-8,text,true,,1464736,4955,8,8,https://rddev2.fit2cloud.com/login,26,0,0\n" +
                "1584602499605,0,https://rddev2.fit2cloud.com/dashboard/?state=3d31fe47-47bd-47f2-950d-9d135e0600ef&session_state=191d0330-dd9f-4bb0-8c24-0e3df46e2ff1&code=efe49afa-7afb-4c38-8e0c-38323291938c.191d0330-dd9f-4bb0-8c24-0e3df46e2ff1.fd56cca3-6d54-44aa-b879-a35e79fc1bfc,Non HTTP response code: java.net.URISyntaxException,\"Non HTTP response message: Illegal character in query at index 45: https://rddev2.fit2cloud.com/dashboard/?code=\n" +
                "                                        efe49afa-7afb-4c38-8e0c-38323291938c.191d0330-dd9f-4bb0-8c24-0e3df46e2ff1.fd56cca3-6d54-44aa-b879-a35e79fc1bfc\n" +
                "                                    &state=3d31fe47-47bd-47f2-950d-9d135e0600ef&session_state=191d0330-dd9f-4bb0-8c24-0e3df46e2ff1\",Thread Group 1-2,text,false,,1392,0,8,8,\"https://rddev2.fit2cloud.com/dashboard/?code=\n" +
                "                                        efe49afa-7afb-4c38-8e0c-38323291938c.191d0330-dd9f-4bb0-8c24-0e3df46e2ff1.fd56cca3-6d54-44aa-b879-a35e79fc1bfc\n" +
                "                                    &state=3d31fe47-47bd-47f2-950d-9d135e0600ef&session_state=191d0330-dd9f-4bb0-8c24-0e3df46e2ff1\",0,0,0\n" +
                "1584602499607,19,https://rddev2.fit2cloud.com/dashboard/anonymous/i18n/en_US.json?_t=1577351137654,200,OK,Thread Group 1-2,text,true,,12424,560,8,8,https://rddev2.fit2cloud.com/dashboard/anonymous/i18n/en_US.json?_t=1577351137654,19,0,0\n" +
                "1584602499856,21,https://rddev2.fit2cloud.com/dashboard/web-public/project/html/task-list.html?_t=1577351137654,200,OK,Thread Group 1-1,text,true,,2516,572,8,8,https://rddev2.fit2cloud.com/dashboard/web-public/project/html/task-list.html?_t=1577351137654,21,0,0\n" +
                "1584602500034,27,https://rddev2.fit2cloud.com/dashboard/web-public/project/html/task-menus.html?_t=1577351137654,200,OK,Thread Group 1-2,text,true,,1916,574,8,8,https://rddev2.fit2cloud.com/dashboard/web-public/project/html/task-menus.html?_t=1577351137654,27,0,0\n" +
                "1584602500182,23,https://rddev2.fit2cloud.com/dashboard/anonymous/license/validate?_nocache=1578039505321,200,OK,Thread Group 1-1,text,true,,288,566,8,8,https://rddev2.fit2cloud.com/dashboard/anonymous/license/validate?_nocache=1578039505321,23,0,0\n" +
                "1584602500484,18,https://rddev2.fit2cloud.com/dashboard/web-public/project/html/notification-menus.html?_t=1577351137654,200,OK,Thread Group 1-2,text,true,,1570,582,8,8,https://rddev2.fit2cloud.com/dashboard/web-public/project/html/notification-menus.html?_t=1577351137654,18,0,0\n" +
                "1584602500504,16,https://rddev2.fit2cloud.com/dashboard/web-public/project/html/task-list.html?_t=1577351137654,200,OK,Thread Group 1-2,text,true,,2516,573,8,8,https://rddev2.fit2cloud.com/dashboard/web-public/project/html/task-list.html?_t=1577351137654,16,0,0\n" +
                "1584602500206,420,https://rddev2.fit2cloud.com/dashboard/module/all?_nocache=1578039505321,200,OK,Thread Group 1-1,text,true,,1473342,5748,8,8,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2Fmodule%2Fall?_nocache%3D1578039505321&state=573519c4-a91b-4e46-b28d-f68231a8faf8&login=true&scope=openid,10,0,0\n" +
                "1584602500206,10,https://rddev2.fit2cloud.com/dashboard/module/all?_nocache=1578039505321-0,302,Found,Thread Group 1-1,,true,,555,550,8,8,https://rddev2.fit2cloud.com/dashboard/module/all?_nocache=1578039505321,10,0,0\n" +
                "1584602500216,23,https://rddev2.fit2cloud.com/dashboard/module/all?_nocache=1578039505321-1,200,OK,Thread Group 1-1,text,true,,8038,1439,8,8,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2Fmodule%2Fall?_nocache%3D1578039505321&state=573519c4-a91b-4e46-b28d-f68231a8faf8&login=true&scope=openid,23,0,0\n" +
                "1584602500243,383,https://rddev2.fit2cloud.com/dashboard/module/all?_nocache=1578039505321-2,200,OK,Thread Group 1-1,text,true,,1464749,3759,8,8,https://rddev2.fit2cloud.com/login,24,0,0\n" +
                "1584602500622,18,https://rddev2.fit2cloud.com/dashboard/anonymous/license/validate?_nocache=1578039505321,200,OK,Thread Group 1-2,text,true,,288,567,8,8,https://rddev2.fit2cloud.com/dashboard/anonymous/license/validate?_nocache=1578039505321,18,0,0\n" +
                "1584602500735,15,https://rddev2.fit2cloud.com/web-public/fit2cloud/html/loading/loading.html?_t=1577351137654,200,OK,Thread Group 1-1,text,true,,503,506,8,8,https://rddev2.fit2cloud.com/web-public/fit2cloud/html/loading/loading.html?_t=1577351137654,15,0,0\n" +
                "1584602501143,58,https://rddev2.fit2cloud.com/auth/realms/cmp/login-actions/authenticate?session_code=YWUneSay4qlQuHRZsD4kPaZDIIR50KJLaNpW7uhsD-Q&execution=c7620733-54ab-46b2-802b-66764e42682b&client_id=cmp-client&tab_id=S8xOQPgCmhQ,400,Bad Request,Thread Group 1-5,text,false,,4469,1746,8,8,https://rddev2.fit2cloud.com/auth/realms/cmp/login-actions/authenticate,58,0,4\n" +
                "1584602501233,0,https://rddev2.fit2cloud.com/dashboard/?state=3d31fe47-47bd-47f2-950d-9d135e0600ef&session_state=191d0330-dd9f-4bb0-8c24-0e3df46e2ff1&code=efe49afa-7afb-4c38-8e0c-38323291938c.191d0330-dd9f-4bb0-8c24-0e3df46e2ff1.fd56cca3-6d54-44aa-b879-a35e79fc1bfc,Non HTTP response code: java.net.URISyntaxException,\"Non HTTP response message: Illegal character in query at index 45: https://rddev2.fit2cloud.com/dashboard/?code=\n" +
                "                                        efe49afa-7afb-4c38-8e0c-38323291938c.191d0330-dd9f-4bb0-8c24-0e3df46e2ff1.fd56cca3-6d54-44aa-b879-a35e79fc1bfc\n" +
                "                                    &state=3d31fe47-47bd-47f2-950d-9d135e0600ef&session_state=191d0330-dd9f-4bb0-8c24-0e3df46e2ff1\",Thread Group 1-5,text,false,,1392,0,8,8,\"https://rddev2.fit2cloud.com/dashboard/?code=\n" +
                "                                        efe49afa-7afb-4c38-8e0c-38323291938c.191d0330-dd9f-4bb0-8c24-0e3df46e2ff1.fd56cca3-6d54-44aa-b879-a35e79fc1bfc\n" +
                "                                    &state=3d31fe47-47bd-47f2-950d-9d135e0600ef&session_state=191d0330-dd9f-4bb0-8c24-0e3df46e2ff1\",0,0,0\n" +
                "1584602501234,19,https://rddev2.fit2cloud.com/dashboard/anonymous/i18n/en_US.json?_t=1577351137654,200,OK,Thread Group 1-5,text,true,,12438,560,8,8,https://rddev2.fit2cloud.com/dashboard/anonymous/i18n/en_US.json?_t=1577351137654,19,0,0\n" +
                "1584602501253,17,https://rddev2.fit2cloud.com/dashboard/web-public/project/html/task-menus.html?_t=1577351137654,200,OK,Thread Group 1-5,text,true,,1916,574,8,8,https://rddev2.fit2cloud.com/dashboard/web-public/project/html/task-menus.html?_t=1577351137654,17,0,0\n" +
                "1584602500841,509,https://rddev2.fit2cloud.com/dashboard/module/all?_nocache=1578039505321,200,OK,Thread Group 1-2,text,true,,1473319,5757,8,8,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2Fmodule%2Fall?_nocache%3D1578039505321&state=c27f5334-b14f-43e8-ba3d-a31d6f385c32&login=true&scope=openid,13,0,0\n" +
                "1584602500841,13,https://rddev2.fit2cloud.com/dashboard/module/all?_nocache=1578039505321-0,302,Found,Thread Group 1-2,,true,,555,551,8,8,https://rddev2.fit2cloud.com/dashboard/module/all?_nocache=1578039505321,13,0,0\n" +
                "1584602500855,29,https://rddev2.fit2cloud.com/dashboard/module/all?_nocache=1578039505321-1,200,OK,Thread Group 1-2,text,true,,8038,1440,8,8,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2Fmodule%2Fall?_nocache%3D1578039505321&state=c27f5334-b14f-43e8-ba3d-a31d6f385c32&login=true&scope=openid,29,0,0\n" +
                "1584602500887,463,https://rddev2.fit2cloud.com/dashboard/module/all?_nocache=1578039505321-2,200,OK,Thread Group 1-2,text,true,,1464726,3766,8,8,https://rddev2.fit2cloud.com/login,26,0,0\n" +
                "1584602501352,16,https://rddev2.fit2cloud.com/web-public/fit2cloud/html/loading/loading.html?_t=1577351137654,200,OK,Thread Group 1-2,text,true,,503,507,8,8,https://rddev2.fit2cloud.com/web-public/fit2cloud/html/loading/loading.html?_t=1577351137654,16,0,0\n" +
                "1584602501458,13,https://rddev2.fit2cloud.com/dashboard/web-public/project/html/notification-menus.html?_t=1577351137654,200,OK,Thread Group 1-5,text,true,,1570,582,8,8,https://rddev2.fit2cloud.com/dashboard/web-public/project/html/notification-menus.html?_t=1577351137654,13,0,0\n" +
                "1584602501663,17,https://rddev2.fit2cloud.com/dashboard/web-public/project/html/task-list.html?_t=1577351137654,200,OK,Thread Group 1-5,text,true,,2516,573,8,8,https://rddev2.fit2cloud.com/dashboard/web-public/project/html/task-list.html?_t=1577351137654,17,0,0\n" +
                "1584602501435,359,https://rddev2.fit2cloud.com/dashboard/flow/runtime/task/pending/1/50,200,OK,Thread Group 1-1,text,true,,1473387,6761,8,8,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2Fflow%2Fruntime%2Ftask%2Fpending%2F1%2F50&state=832af3d4-2ca4-4e23-bf2a-e14a01d27fc0&login=true&scope=openid,11,0,0\n" +
                "1584602501435,11,https://rddev2.fit2cloud.com/dashboard/flow/runtime/task/pending/1/50-0,302,Found,Thread Group 1-1,,true,,558,653,8,8,https://rddev2.fit2cloud.com/dashboard/flow/runtime/task/pending/1/50,11,0,0\n" +
                "1584602501446,22,https://rddev2.fit2cloud.com/dashboard/flow/runtime/task/pending/1/50-1,200,OK,Thread Group 1-1,text,true,,8030,1614,8,8,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2Fflow%2Fruntime%2Ftask%2Fpending%2F1%2F50&state=832af3d4-2ca4-4e23-bf2a-e14a01d27fc0&login=true&scope=openid,22,0,0\n" +
                "1584602501471,323,https://rddev2.fit2cloud.com/dashboard/flow/runtime/task/pending/1/50-2,200,OK,Thread Group 1-1,text,true,,1464799,4494,8,8,https://rddev2.fit2cloud.com/login,23,0,0\n" +
                "1584602501784,17,https://rddev2.fit2cloud.com/dashboard/anonymous/license/validate?_nocache=1578039505321,200,OK,Thread Group 1-5,text,true,,288,567,8,8,https://rddev2.fit2cloud.com/dashboard/anonymous/license/validate?_nocache=1578039505321,17,0,0\n" +
                "1584602501907,304,https://rddev2.fit2cloud.com/dashboard/notification/list/unread/1/50,200,OK,Thread Group 1-1,text,true,,1473471,6749,10,10,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2Fnotification%2Flist%2Funread%2F1%2F50&state=1904f64b-a8f9-44d8-867e-359cfe46297f&login=true&scope=openid,10,0,0\n" +
                "1584602501907,10,https://rddev2.fit2cloud.com/dashboard/notification/list/unread/1/50-0,302,Found,Thread Group 1-1,,true,,555,652,10,10,https://rddev2.fit2cloud.com/dashboard/notification/list/unread/1/50,10,0,0\n" +
                "1584602501917,24,https://rddev2.fit2cloud.com/dashboard/notification/list/unread/1/50-1,200,OK,Thread Group 1-1,text,true,,8021,1603,10,10,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2Fnotification%2Flist%2Funread%2F1%2F50&state=1904f64b-a8f9-44d8-867e-359cfe46297f&login=true&scope=openid,24,0,0\n" +
                "1584602501943,268,https://rddev2.fit2cloud.com/dashboard/notification/list/unread/1/50-2,200,OK,Thread Group 1-1,text,true,,1464895,4494,10,10,https://rddev2.fit2cloud.com/login,23,0,0\n" +
                "1584602502213,16,https://rddev2.fit2cloud.com/web-public/project/html/pagination.html?_t=1577351137654,200,OK,Thread Group 1-1,text,true,,1162,499,10,10,https://rddev2.fit2cloud.com/web-public/project/html/pagination.html?_t=1577351137654,16,0,0\n" +
                "1584602501802,513,https://rddev2.fit2cloud.com/dashboard/module/all?_nocache=1578039505321,200,OK,Thread Group 1-5,text,true,,1473342,5757,10,10,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2Fmodule%2Fall?_nocache%3D1578039505321&state=7d3aea03-3217-44da-81be-35c41c1db2d7&login=true&scope=openid,15,0,0\n" +
                "1584602501802,15,https://rddev2.fit2cloud.com/dashboard/module/all?_nocache=1578039505321-0,302,Found,Thread Group 1-5,,true,,555,551,10,10,https://rddev2.fit2cloud.com/dashboard/module/all?_nocache=1578039505321,15,0,0\n" +
                "1584602501817,23,https://rddev2.fit2cloud.com/dashboard/module/all?_nocache=1578039505321-1,200,OK,Thread Group 1-5,text,true,,8038,1440,10,10,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2Fmodule%2Fall?_nocache%3D1578039505321&state=7d3aea03-3217-44da-81be-35c41c1db2d7&login=true&scope=openid,23,0,0\n" +
                "1584602501842,473,https://rddev2.fit2cloud.com/dashboard/module/all?_nocache=1578039505321-2,200,OK,Thread Group 1-5,text,true,,1464749,3766,10,10,https://rddev2.fit2cloud.com/login,18,0,0\n" +
                "1584602502316,28,https://rddev2.fit2cloud.com/web-public/fit2cloud/html/loading/loading.html?_t=1577351137654,200,OK,Thread Group 1-5,text,true,,503,507,10,10,https://rddev2.fit2cloud.com/web-public/fit2cloud/html/loading/loading.html?_t=1577351137654,28,0,0\n" +
                "1584602502110,631,https://rddev2.fit2cloud.com/,200,OK,Thread Group 1-10,text,true,,1473668,6950,10,10,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2F&state=26792af8-d8cd-4ed6-b0e0-68ad7220004f&login=true&scope=openid,63,0,1\n" +
                "1584602502110,63,https://rddev2.fit2cloud.com/-0,302,Found,Thread Group 1-10,,true,,214,567,10,10,https://rddev2.fit2cloud.com/,63,0,1\n" +
                "1584602502173,15,https://rddev2.fit2cloud.com/-1,302,Found,Thread Group 1-10,,true,,615,577,10,10,https://rddev2.fit2cloud.com/dashboard/,15,0,0\n" +
                "1584602502189,39,https://rddev2.fit2cloud.com/-2,200,OK,Thread Group 1-10,text,true,,8074,851,10,10,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2F&state=26792af8-d8cd-4ed6-b0e0-68ad7220004f&login=true&scope=openid,39,0,0\n" +
                "1584602502229,512,https://rddev2.fit2cloud.com/-3,200,OK,Thread Group 1-10,text,true,,1464765,4955,10,10,https://rddev2.fit2cloud.com/login,18,0,0\n" +
                "1584602502169,625,https://rddev2.fit2cloud.com/dashboard/flow/runtime/task/pending/1/50,200,OK,Thread Group 1-2,text,true,,1473329,6770,10,10,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2Fflow%2Fruntime%2Ftask%2Fpending%2F1%2F50&state=7bfdeacf-3f22-449d-88f9-f0d792bfe1bb&login=true&scope=openid,19,0,0\n" +
                "1584602502169,19,https://rddev2.fit2cloud.com/dashboard/flow/runtime/task/pending/1/50-0,302,Found,Thread Group 1-2,,true,,558,654,10,10,https://rddev2.fit2cloud.com/dashboard/flow/runtime/task/pending/1/50,19,0,0\n" +
                "1584602502189,32,https://rddev2.fit2cloud.com/dashboard/flow/runtime/task/pending/1/50-1,200,OK,Thread Group 1-2,text,true,,8030,1615,10,10,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2Fflow%2Fruntime%2Ftask%2Fpending%2F1%2F50&state=7bfdeacf-3f22-449d-88f9-f0d792bfe1bb&login=true&scope=openid,32,0,0\n" +
                "1584602502222,572,https://rddev2.fit2cloud.com/dashboard/flow/runtime/task/pending/1/50-2,200,OK,Thread Group 1-2,text,true,,1464741,4501,10,10,https://rddev2.fit2cloud.com/login,21,0,0\n" +
                "1584602502110,713,https://rddev2.fit2cloud.com/,200,OK,Thread Group 1-9,text,true,,1473667,6942,10,10,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2F&state=cb0aadfc-16eb-486b-b7f1-2c2df1c3231c&login=true&scope=openid,63,0,1\n" +
                "1584602502110,63,https://rddev2.fit2cloud.com/-0,302,Found,Thread Group 1-9,,true,,214,567,10,10,https://rddev2.fit2cloud.com/,63,0,1\n" +
                "1584602502174,21,https://rddev2.fit2cloud.com/-1,302,Found,Thread Group 1-9,,true,,614,577,10,10,https://rddev2.fit2cloud.com/dashboard/,21,0,0\n" +
                "1584602502195,34,https://rddev2.fit2cloud.com/-2,200,OK,Thread Group 1-9,text,true,,8074,850,10,10,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2F&state=cb0aadfc-16eb-486b-b7f1-2c2df1c3231c&login=true&scope=openid,34,0,0\n" +
                "1584602502231,592,https://rddev2.fit2cloud.com/-3,200,OK,Thread Group 1-9,text,true,,1464765,4948,10,10,https://rddev2.fit2cloud.com/login,21,0,0\n" +
                "1584602502434,434,https://rddev2.fit2cloud.com/dashboard/flow/runtime/task/pending/1/50,200,OK,Thread Group 1-1,text,true,,1473315,6926,10,10,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2Fflow%2Fruntime%2Ftask%2Fpending%2F1%2F50&state=b2a99afa-66a7-411d-bba9-239c9b20de82&login=true&scope=openid,18,0,0\n" +
                "1584602502434,18,https://rddev2.fit2cloud.com/dashboard/flow/runtime/task/pending/1/50-0,302,Found,Thread Group 1-1,,true,,558,731,10,10,https://rddev2.fit2cloud.com/dashboard/flow/runtime/task/pending/1/50,18,0,0\n" +
                "1584602502452,27,https://rddev2.fit2cloud.com/dashboard/flow/runtime/task/pending/1/50-1,200,OK,Thread Group 1-1,text,true,,8024,1603,10,10,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2Fflow%2Fruntime%2Ftask%2Fpending%2F1%2F50&state=b2a99afa-66a7-411d-bba9-239c9b20de82&login=true&scope=openid,27,0,0\n" +
                "1584602502481,387,https://rddev2.fit2cloud.com/dashboard/flow/runtime/task/pending/1/50-2,200,OK,Thread Group 1-1,text,true,,1464733,4592,10,10,https://rddev2.fit2cloud.com/login,25,0,0\n" +
                "1584602502839,65,https://rddev2.fit2cloud.com/auth/realms/cmp/login-actions/authenticate?session_code=YWUneSay4qlQuHRZsD4kPaZDIIR50KJLaNpW7uhsD-Q&execution=c7620733-54ab-46b2-802b-66764e42682b&client_id=cmp-client&tab_id=S8xOQPgCmhQ,400,Bad Request,Thread Group 1-3,text,false,,4462,1746,10,10,https://rddev2.fit2cloud.com/auth/realms/cmp/login-actions/authenticate,65,0,1\n" +
                "1584602502961,0,https://rddev2.fit2cloud.com/dashboard/?state=3d31fe47-47bd-47f2-950d-9d135e0600ef&session_state=191d0330-dd9f-4bb0-8c24-0e3df46e2ff1&code=efe49afa-7afb-4c38-8e0c-38323291938c.191d0330-dd9f-4bb0-8c24-0e3df46e2ff1.fd56cca3-6d54-44aa-b879-a35e79fc1bfc,Non HTTP response code: java.net.URISyntaxException,\"Non HTTP response message: Illegal character in query at index 45: https://rddev2.fit2cloud.com/dashboard/?code=\n" +
                "                                        efe49afa-7afb-4c38-8e0c-38323291938c.191d0330-dd9f-4bb0-8c24-0e3df46e2ff1.fd56cca3-6d54-44aa-b879-a35e79fc1bfc\n" +
                "                                    &state=3d31fe47-47bd-47f2-950d-9d135e0600ef&session_state=191d0330-dd9f-4bb0-8c24-0e3df46e2ff1\",Thread Group 1-3,text,false,,1392,0,10,10,\"https://rddev2.fit2cloud.com/dashboard/?code=\n" +
                "                                        efe49afa-7afb-4c38-8e0c-38323291938c.191d0330-dd9f-4bb0-8c24-0e3df46e2ff1.fd56cca3-6d54-44aa-b879-a35e79fc1bfc\n" +
                "                                    &state=3d31fe47-47bd-47f2-950d-9d135e0600ef&session_state=191d0330-dd9f-4bb0-8c24-0e3df46e2ff1\",0,0,0\n" +
                "1584602503108,27,https://rddev2.fit2cloud.com/dashboard/anonymous/i18n/en_US.json?_t=1577351137654,200,OK,Thread Group 1-3,text,true,,12438,560,10,10,https://rddev2.fit2cloud.com/dashboard/anonymous/i18n/en_US.json?_t=1577351137654,27,0,0\n" +
                "1584602503239,23,https://rddev2.fit2cloud.com/dashboard/web-public/project/html/task-menus.html?_t=1577351137654,200,OK,Thread Group 1-3,text,true,,1916,574,10,10,https://rddev2.fit2cloud.com/dashboard/web-public/project/html/task-menus.html?_t=1577351137654,23,0,0\n" +
                "1584602503006,262,https://rddev2.fit2cloud.com/dashboard/user/current/info?_nocache=1578039505484,200,OK,Thread Group 1-1,text,true,,1473599,5844,10,10,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2Fuser%2Fcurrent%2Finfo?_nocache%3D1578039505484&state=10ff89f8-c521-42a3-99bd-d2929d4260cc&login=true&scope=openid,14,0,0\n" +
                "1584602503006,14,https://rddev2.fit2cloud.com/dashboard/user/current/info?_nocache=1578039505484-0,302,Found,Thread Group 1-1,,true,,564,557,10,10,https://rddev2.fit2cloud.com/dashboard/user/current/info?_nocache=1578039505484,14,0,0\n" +
                "1584602503021,22,https://rddev2.fit2cloud.com/dashboard/user/current/info?_nocache=1578039505484-1,200,OK,Thread Group 1-1,text,true,,8056,1528,10,10,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2Fuser%2Fcurrent%2Finfo?_nocache%3D1578039505484&state=10ff89f8-c521-42a3-99bd-d2929d4260cc&login=true&scope=openid,22,0,0\n" +
                "1584602503047,221,https://rddev2.fit2cloud.com/dashboard/user/current/info?_nocache=1578039505484-2,200,OK,Thread Group 1-1,text,true,,1464979,3759,10,10,https://rddev2.fit2cloud.com/login,20,0,0\n" +
                "1584602502806,506,https://rddev2.fit2cloud.com/dashboard/notification/list/unread/1/50,200,OK,Thread Group 1-2,text,true,,1473450,6758,10,10,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2Fnotification%2Flist%2Funread%2F1%2F50&state=f4ac4eb9-f92a-4bcc-8214-3eac6df6d1c9&login=true&scope=openid,12,0,0\n" +
                "1584602502806,12,https://rddev2.fit2cloud.com/dashboard/notification/list/unread/1/50-0,302,Found,Thread Group 1-2,,true,,555,653,10,10,https://rddev2.fit2cloud.com/dashboard/notification/list/unread/1/50,12,0,0\n" +
                "1584602502819,26,https://rddev2.fit2cloud.com/dashboard/notification/list/unread/1/50-1,200,OK,Thread Group 1-2,text,true,,8027,1604,10,10,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2Fnotification%2Flist%2Funread%2F1%2F50&state=f4ac4eb9-f92a-4bcc-8214-3eac6df6d1c9&login=true&scope=openid,26,0,0\n" +
                "1584602502846,466,https://rddev2.fit2cloud.com/dashboard/notification/list/unread/1/50-2,200,OK,Thread Group 1-2,text,true,,1464868,4501,10,10,https://rddev2.fit2cloud.com/login,15,0,0\n" +
                "1584602503314,13,https://rddev2.fit2cloud.com/web-public/project/html/pagination.html?_t=1577351137654,200,OK,Thread Group 1-2,text,true,,1162,500,10,10,https://rddev2.fit2cloud.com/web-public/project/html/pagination.html?_t=1577351137654,13,0,0\n" +
                "1584602503471,15,https://rddev2.fit2cloud.com/dashboard/web-public/project/html/header-menu.html?_t=1577351137654,200,OK,Thread Group 1-1,text,true,,3117,574,10,10,https://rddev2.fit2cloud.com/dashboard/web-public/project/html/header-menu.html?_t=1577351137654,15,0,0\n" +
                "1584602503471,54,https://rddev2.fit2cloud.com/auth/realms/cmp/login-actions/authenticate?session_code=YWUneSay4qlQuHRZsD4kPaZDIIR50KJLaNpW7uhsD-Q&execution=c7620733-54ab-46b2-802b-66764e42682b&client_id=cmp-client&tab_id=S8xOQPgCmhQ,400,Bad Request,Thread Group 1-4,text,false,,4462,1747,10,10,https://rddev2.fit2cloud.com/auth/realms/cmp/login-actions/authenticate,54,0,4\n" +
                "1584602503569,0,https://rddev2.fit2cloud.com/dashboard/?state=3d31fe47-47bd-47f2-950d-9d135e0600ef&session_state=191d0330-dd9f-4bb0-8c24-0e3df46e2ff1&code=efe49afa-7afb-4c38-8e0c-38323291938c.191d0330-dd9f-4bb0-8c24-0e3df46e2ff1.fd56cca3-6d54-44aa-b879-a35e79fc1bfc,Non HTTP response code: java.net.URISyntaxException,\"Non HTTP response message: Illegal character in query at index 45: https://rddev2.fit2cloud.com/dashboard/?code=\n" +
                "                                        efe49afa-7afb-4c38-8e0c-38323291938c.191d0330-dd9f-4bb0-8c24-0e3df46e2ff1.fd56cca3-6d54-44aa-b879-a35e79fc1bfc\n" +
                "                                    &state=3d31fe47-47bd-47f2-950d-9d135e0600ef&session_state=191d0330-dd9f-4bb0-8c24-0e3df46e2ff1\",Thread Group 1-4,text,false,,1392,0,10,10,\"https://rddev2.fit2cloud.com/dashboard/?code=\n" +
                "                                        efe49afa-7afb-4c38-8e0c-38323291938c.191d0330-dd9f-4bb0-8c24-0e3df46e2ff1.fd56cca3-6d54-44aa-b879-a35e79fc1bfc\n" +
                "                                    &state=3d31fe47-47bd-47f2-950d-9d135e0600ef&session_state=191d0330-dd9f-4bb0-8c24-0e3df46e2ff1\",0,0,0\n" +
                "1584602503108,494,https://rddev2.fit2cloud.com/dashboard/flow/runtime/task/pending/1/50,200,OK,Thread Group 1-5,text,true,,1473344,6770,10,10,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2Fflow%2Fruntime%2Ftask%2Fpending%2F1%2F50&state=bbc579c2-fce7-4f4c-a9e0-9e27c818248c&login=true&scope=openid,21,0,0\n" +
                "1584602503108,21,https://rddev2.fit2cloud.com/dashboard/flow/runtime/task/pending/1/50-0,302,Found,Thread Group 1-5,,true,,558,654,10,10,https://rddev2.fit2cloud.com/dashboard/flow/runtime/task/pending/1/50,21,0,0\n" +
                "1584602503129,39,https://rddev2.fit2cloud.com/dashboard/flow/runtime/task/pending/1/50-1,200,OK,Thread Group 1-5,text,true,,8030,1615,10,10,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2Fflow%2Fruntime%2Ftask%2Fpending%2F1%2F50&state=bbc579c2-fce7-4f4c-a9e0-9e27c818248c&login=true&scope=openid,39,0,0\n" +
                "1584602503170,432,https://rddev2.fit2cloud.com/dashboard/flow/runtime/task/pending/1/50-2,200,OK,Thread Group 1-5,text,true,,1464756,4501,10,10,https://rddev2.fit2cloud.com/login,25,0,0\n" +
                "1584602503607,363,https://rddev2.fit2cloud.com/dashboard/notification/list/unread/1/50,200,OK,Thread Group 1-5,text,true,,1473560,6758,10,10,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2Fnotification%2Flist%2Funread%2F1%2F50&state=db071ff5-a114-4a0d-b693-fd1d8e477e75&login=true&scope=openid,12,0,0\n" +
                "1584602503607,12,https://rddev2.fit2cloud.com/dashboard/notification/list/unread/1/50-0,302,Found,Thread Group 1-5,,true,,555,653,10,10,https://rddev2.fit2cloud.com/dashboard/notification/list/unread/1/50,12,0,0\n" +
                "1584602503619,23,https://rddev2.fit2cloud.com/dashboard/notification/list/unread/1/50-1,200,OK,Thread Group 1-5,text,true,,8027,1604,10,10,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2Fnotification%2Flist%2Funread%2F1%2F50&state=db071ff5-a114-4a0d-b693-fd1d8e477e75&login=true&scope=openid,23,0,0\n" +
                "1584602503644,326,https://rddev2.fit2cloud.com/dashboard/notification/list/unread/1/50-2,200,OK,Thread Group 1-5,text,true,,1464978,4501,10,10,https://rddev2.fit2cloud.com/login,18,0,0\n" +
                "1584602503971,15,https://rddev2.fit2cloud.com/web-public/project/html/pagination.html?_t=1577351137654,200,OK,Thread Group 1-5,text,true,,1162,500,10,10,https://rddev2.fit2cloud.com/web-public/project/html/pagination.html?_t=1577351137654,15,0,0\n" +
                "1584602503971,19,https://rddev2.fit2cloud.com/dashboard/anonymous/i18n/en_US.json?_t=1577351137654,200,OK,Thread Group 1-4,text,true,,12438,561,10,10,https://rddev2.fit2cloud.com/dashboard/anonymous/i18n/en_US.json?_t=1577351137654,19,0,0\n" +
                "1584602503792,32,https://rddev2.fit2cloud.com/dashboard/user/source/list?_nocache=1578039505551,200,OK,Thread Group 1-1,text,true,,8617,2109,10,10,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2Fuser%2Fsource%2Flist?_nocache%3D1578039505551&state=2bc90b74-8d32-4217-a573-bfbd969b6b16&login=true&scope=openid,10,0,0\n" +
                "1584602503792,10,https://rddev2.fit2cloud.com/dashboard/user/source/list?_nocache=1578039505551-0,302,Found,Thread Group 1-1,,true,,563,556,10,10,https://rddev2.fit2cloud.com/dashboard/user/source/list?_nocache=1578039505551,10,0,0\n" +
                "1584602503803,21,https://rddev2.fit2cloud.com/dashboard/user/source/list?_nocache=1578039505551-1,200,OK,Thread Group 1-1,text,true,,8054,1553,10,10,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2Fuser%2Fsource%2Flist?_nocache%3D1578039505551&state=2bc90b74-8d32-4217-a573-bfbd969b6b16&login=true&scope=openid,21,0,0\n" +
                "1584602504095,21,https://rddev2.fit2cloud.com/auth/realms/cmp/login-actions/authenticate?session_code=YWUneSay4qlQuHRZsD4kPaZDIIR50KJLaNpW7uhsD-Q&execution=c7620733-54ab-46b2-802b-66764e42682b&client_id=cmp-client&tab_id=S8xOQPgCmhQ,400,Bad Request,Thread Group 1-9,text,false,,4469,1745,9,9,https://rddev2.fit2cloud.com/auth/realms/cmp/login-actions/authenticate,21,0,0\n" +
                "1584602504100,20,https://rddev2.fit2cloud.com/dashboard/web-public/project/html/task-menus.html?_t=1577351137654,200,OK,Thread Group 1-4,text,true,,1916,575,8,8,https://rddev2.fit2cloud.com/dashboard/web-public/project/html/task-menus.html?_t=1577351137654,20,0,0\n" +
                "1584602504095,27,https://rddev2.fit2cloud.com/auth/realms/cmp/login-actions/authenticate?session_code=YWUneSay4qlQuHRZsD4kPaZDIIR50KJLaNpW7uhsD-Q&execution=c7620733-54ab-46b2-802b-66764e42682b&client_id=cmp-client&tab_id=S8xOQPgCmhQ,400,Bad Request,Thread Group 1-10,text,false,,4462,1746,7,7,https://rddev2.fit2cloud.com/auth/realms/cmp/login-actions/authenticate,27,0,0\n" +
                "1584602504095,39,https://rddev2.fit2cloud.com/dashboard/flow/runtime/task/pending/1/50,200,OK,Thread Group 1-2,text,true,,8588,2336,6,6,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2Fflow%2Fruntime%2Ftask%2Fpending%2F1%2F50&state=24c9d65b-0421-4732-8193-0bf5f70b3fa4&login=true&scope=openid,13,0,0\n" +
                "1584602504095,13,https://rddev2.fit2cloud.com/dashboard/flow/runtime/task/pending/1/50-0,302,Found,Thread Group 1-2,,true,,558,732,6,6,https://rddev2.fit2cloud.com/dashboard/flow/runtime/task/pending/1/50,13,0,0\n" +
                "1584602504108,26,https://rddev2.fit2cloud.com/dashboard/flow/runtime/task/pending/1/50-1,200,OK,Thread Group 1-2,text,true,,8030,1604,6,6,https://rddev2.fit2cloud.com/auth/realms/cmp/protocol/openid-connect/auth?response_type=code&client_id=cmp-client&redirect_uri=https%3A%2F%2Frddev2.fit2cloud.com%2Fdashboard%2Fflow%2Fruntime%2Ftask%2Fpending%2F1%2F50&state=24c9d65b-0421-4732-8193-0bf5f70b3fa4&login=true&scope=openid,26,0,0\n" +
                "1584602504095,55,https://rddev2.fit2cloud.com/auth/realms/cmp/login-actions/authenticate?session_code=YWUneSay4qlQuHRZsD4kPaZDIIR50KJLaNpW7uhsD-Q&execution=c7620733-54ab-46b2-802b-66764e42682b&client_id=cmp-client&tab_id=S8xOQPgCmhQ,400,Bad Request,Thread Group 1-8,text,false,,4469,1746,5,5,https://rddev2.fit2cloud.com/auth/realms/cmp/login-actions/authenticate,55,0,3\n" +
                "1584602504095,59,https://rddev2.fit2cloud.com/auth/realms/cmp/login-actions/authenticate?session_code=YWUneSay4qlQuHRZsD4kPaZDIIR50KJLaNpW7uhsD-Q&execution=c7620733-54ab-46b2-802b-66764e42682b&client_id=cmp-client&tab_id=S8xOQPgCmhQ,400,Bad Request,Thread Group 1-6,text,false,,4462,1747,4,4,https://rddev2.fit2cloud.com/auth/realms/cmp/login-actions/authenticate,59,0,4\n" +
                "1584602504095,65,https://rddev2.fit2cloud.com/auth/realms/cmp/login-actions/authenticate?session_code=YWUneSay4qlQuHRZsD4kPaZDIIR50KJLaNpW7uhsD-Q&execution=c7620733-54ab-46b2-802b-66764e42682b&client_id=cmp-client&tab_id=S8xOQPgCmhQ,400,Bad Request,Thread Group 1-7,text,false,,4469,1745,3,3,https://rddev2.fit2cloud.com/auth/realms/cmp/login-actions/authenticate,65,0,4\n" +
                "1584602504200,12,https://rddev2.fit2cloud.com/dashboard/web-public/project/html/notification-menus.html?_t=1577351137654,200,OK,Thread Group 1-3,text,true,,1570,582,2,2,https://rddev2.fit2cloud.com/dashboard/web-public/project/html/notification-menus.html?_t=1577351137654,12,0,0\n";
        List<Metric> metrics = beanBuilderExample(jtlString);
        // 根据label分组，label作为map的key
        Map<String, List<Metric>> map = metrics.stream().collect(Collectors.groupingBy(Metric::getLabel));
        getOneRpsResult(map);
    }

    private void getOneRpsResult(Map<String, List<Metric>> map){
        Iterator<Map.Entry<String, List<Metric>>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<Metric>> entry = iterator.next();
            String label = entry.getKey();
            List<Metric> list = entry.getValue();
            List<String> timestampList = list.stream().map(Metric::getTimestamp).collect(Collectors.toList());
            int index=0;
            //总的响应时间
            int sumElapsed=0;
            Integer failSize = 0;
            Integer totalBytes = 0;
            // 响应时间的列表排序之后 用于计算90%line、95%line、99line
            List<Integer> elapsedList = new ArrayList<>();

            for (int i = 0; i < list.size(); i++) {
                try {
                    Metric row = list.get(i);
                    //响应时间
                    String elapsed = row.getElapsed();
                    sumElapsed += Integer.valueOf(elapsed);
                    elapsedList.add(Integer.valueOf(elapsed));
                    //成功与否
                    String success = row.getSuccess();
                    if (!"true".equals(success)){
                        failSize++;
                    }
                    //字节
                    String bytes = row.getBytes();
                    totalBytes += Integer.valueOf(bytes);

                    index++;
                }catch (Exception e){
                    System.out.println("exception i:"+i);
                }
            }

            Collections.sort(elapsedList, new Comparator<Integer>() {
                public int compare(Integer o1, Integer o2) {
                    return o1-o2;
                }
            });

            Integer tp90 = elapsedList.size()*9/10;
            Integer tp95 = elapsedList.size()*95/100;
            Integer tp99 = elapsedList.size()*99/100;

            Long l = Long.valueOf(timestampList.get(index-1)) - Long.valueOf(timestampList.get(0));

            RequestStatistics sceneResult = new RequestStatistics();
            sceneResult.setRequestLabel(label);
            sceneResult.setSamples(index);
//            sceneResult.setAverage(sumElapsed/index);
            sceneResult.setTp90(elapsedList.get(tp90)+"");
            sceneResult.setTp95(elapsedList.get(tp95)+"");
            sceneResult.setTp99(elapsedList.get(tp99)+"");
            sceneResult.setMin(elapsedList.get(0)+"");
            sceneResult.setMax(elapsedList.get(index-1)+"");
            sceneResult.setErrors(String.format("%.2f",failSize*100.0/index)+"%");
            sceneResult.setKbPerSec(String.format("%.2f",totalBytes*1.0/1024/(l*1.0/1000)));
            System.out.println(JSONObject.toJSONString(sceneResult));

            System.out.println();
        }

    }
}
