package io.metersphere.system.service;

import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.UserInvite;
import io.metersphere.system.domain.UserInviteExample;
import io.metersphere.system.mapper.UserInviteMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserInviteService {

    @Resource
    private UserInviteMapper userInviteMapper;

    public void deleteByOneDayAgo() {
        long time = System.currentTimeMillis() - 24 * 60 * 60 * 1000;
        UserInviteExample example = new UserInviteExample();
        example.createCriteria().andInviteTimeLessThan(time);
        userInviteMapper.deleteByExample(example);
    }

    public List<UserInvite> batchInsert(List<String> inviteEmails, String inviteUser, List<String> userRoleIds, String orgId, String projectId) {
        long inviteTime = System.currentTimeMillis();
        List<UserInvite> inviteList = new ArrayList<>();
        for (String email : inviteEmails) {
            UserInvite userInvite = new UserInvite();
            userInvite.setEmail(email);
            userInvite.setRoles(JSON.toJSONString(userRoleIds));
            userInvite.setInviteUser(inviteUser);
            userInvite.setInviteTime(inviteTime);
            userInvite.setId(IDGenerator.nextStr());
            userInvite.setOrganizationId(orgId);
            userInvite.setProjectId(projectId);
            inviteList.add(userInvite);
        }
        userInviteMapper.batchInsert(inviteList);
        return inviteList;
    }

    public UserInvite selectEfficientInviteById(String id) {
        long time = System.currentTimeMillis() - 24 * 60 * 60 * 1000;
        UserInviteExample example = new UserInviteExample();
        example.createCriteria().andIdEqualTo(id).andInviteTimeGreaterThanOrEqualTo(time);
        List<UserInvite> userInvites = userInviteMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isNotEmpty(userInvites)) {
            return userInvites.getFirst();
        } else {
            return null;
        }
    }

    public void deleteInviteById(String id) {
        userInviteMapper.deleteByPrimaryKey(id);
    }

    public String genInviteMessage(String inviteUser, String inviteId, String baseUrl) {
        String inviteUrl = baseUrl + "/#/invite?inviteId=" + inviteId;
        return "<html>\n" +
                "    <head>\n" +
                "        <base target=\"_blank\">\n" +
                "        <style type=\"text/css\">\n" +
                "            ::-webkit-scrollbar{ display: none; }\n" +
                "        </style>\n" +
                "        <style id=\"cloudAttachStyle\" type=\"text/css\">\n" +
                "            #divNeteaseBigAttach, #divNeteaseBigAttach_bak{display:none;}\n" +
                "        </style>\n" +
                "    </head>\n" +
                "    <body tabindex=\"0\" role=\"listitem\">\n" +
                "        <div id=\"content\" class=\"netease_mail_readhtml netease_mail_readhtml_webmail\">\n" +
                "\t\t\t<div lang=\"en\">\n" +
                "                <div style=\"font-family: Nunito, sans-serif; font-size: 14px; font-weight: 400;\">\n" +
                "                    <section style=\"align-items: center; padding: 50px 50px;\">\n" +
                "                    <div class=\"row align-items-center\">\n" +
                "                        <div class=\"col-lg-3 col-md-3 \"></div>\n" +
                "                    </div>\n" +
                "                    <div class=\"container\">\n" +
                "                        <div class=\"row\" style=\"justify-content: center;\">\n" +
                "                            <div class=\"col-lg-6 col-md-8\">\n" +
                "                                <table style=\"box-sizing: border-box; width: 100%; border-radius: 6px; overflow: hidden; background-color: #fff; box-shadow: 0 0 3px rgba(60, 72, 88, 0.15);\">\n" +
                "                                    <thead>\n" +
                "                                    <tr style=\"background-color: #783787; padding: 3px 0; line-height: 68px; text-align: center; color: #fff; font-size: 24px; font-weight: 700; letter-spacing: 1px;\">\n" +
                "                                        <th scope=\"col\"></th>\n" +
                "                                    </tr>\n" +
                "                                    </thead>\n" +
                "\n" +
                "                                    <tbody>\n" +
                "                                    <tr>\n" +
                "                                        <td style=\"padding: 48px 24px 0; color: #161c2d; font-size: 18px; font-weight: 600;\">\n" +
                "                                            " + Translator.get("user.email.hi") + "\n" +
                "                                        </td>\n" +
                "                                    </tr>\n" +
                "                                    <tr>\n" +
                "                                        <td style=\"padding: 14px 24px 7px; color: #8492a6;\">\n" +
                "                                            <span style=\"color:#7952B3\">" + inviteUser + "</span>\n" +
                "                                            " + Translator.get("user.email.invite_ms") + "\n" +
                "                                        </td>\n" +
                "                                    </tr>\n" +
                "                                    <tr>\n" +
                "                                        <td style=\"padding: 7px 24px;\">\n" +
                "                                            <a style=\"background-color: #783787;color:#FFFFFF;padding: 4px 10px; outline:\n" +
                "                                            none;\n" +
                "                                            text-decoration: none; font-size: 16px; letter-spacing: 0.5px; transition: all 0.3s; font-weight: 600; border-radius: 6px;\" \n" +
                "                                            href=\"" + inviteUrl + "\">" + Translator.get("user.email.invite_click") + "</a>\n" +
                "                                        </td>\n" +
                "                                    </tr>\n" +
                "                                    <tr>\n" +
                "                                        <td style=\"padding: 7px 24px 0; color: #8492a6;\">\n" + Translator.get("user.email.invite_tips") +
                "                                            <a style=\"color:#7952B3\" href=\"" + inviteUrl + "\">" + inviteUrl + "</a>（" + Translator.get("user.email.invite_limited_time") + "）\n" +
                "                                        </td>\n" +
                "                                    </tr>\n" +
                "                                    <tr>\n" +
                "                                        <td style=\"padding: 14px 24px 15px; color: #8492a6;\">\n" +
                "                                        </td>\n" +
                "                                    </tr>" +
                "                                    </tbody>\n" +
                "                                </table>\n" +
                "                            </div>\n" +
                "                        </div>\n" +
                "                    </div>\n" +
                "                </section>\n" +
                "                <style type=\"text/css\">\n" +
                "                    table a {\n" +
                "                        text-decoration: none;\n" +
                "                    }\n" +
                "                </style>\n" +
                "\n" +
                "                <img src=\"https://hk-callback.qcloudmail.com/api/webhook?upn=7f5fc2d81bf99757e3e56e8e756f73cb10378796923237aa92183f70851501ea89c2f4e673237df7591e377ba7c5e1ee515348617ff808a29e7164de5a1f22ddef21237de571dee4e021ccfc41101d403ab9baadb6e9155dda36f9a7b0bca966ccc18c62cdf045ba227d2fad7f4648d874a66dbadd049742bc4fda12a475073a376f24d463d3b24da6c7c8c46767f749fb0aa0e0fe4dae659597f4a4b30aeaaa26f011b4f32085716bd9f53dd245bd12af13c091726d7ebbb211d48e3a9c0137984fd4bd80d6c2ee0fe317ba75ecfe04759f252e75a998097f962ec0ea41945b8463126d092eef4a2e9132e3882cfd4e08b6e370ff07b9109e1859b43d3e8471\" alt=\"\" height=\"1\" width=\"1\" style=\"opacity:0\"></div></div>\n" +
                "                <div style=\"clear:both;height:1px;\"></div>\n" +
                "            </div>\n" +
                "        <script>\n" +
                "        var _n = document.querySelectorAll('[href], [formAction], [onclick]');\n" +
                "        for(var i=0;i<_n.length;i++){ \n" +
                "            var _nc = _n[i];\n" +
                "            if (_nc.getAttribute('href')) {\n" +
                "                _nc.setAttribute('href', _nc.getAttribute('href')), _nc.removeAttribute('href');\n" +
                "            }\n" +
                "            if (_nc.getAttribute('formAction')) {\n" +
                "                _nc.setAttribute('__formAction', _nc.getAttribute('formAction')), _nc.removeAttribute('formAction');\n" +
                "            }\n" +
                "            if (_nc.getAttribute('onclick')) {\n" +
                "                _nc.setAttribute('__onclick', _nc.getAttribute('onclick')), _nc.removeAttribute('onclick');\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        </script>\n" +
                "        <style type=\"text/css\">\n" +
                "        * {\n" +
                "        white-space: normal !important;\n" +
                "        word-break: break-word !important;\n" +
                "        }\n" +
                "        body{font-size:14px;font-family:arial,verdana,sans-serif;line-height:1.666;padding:0;margin:0;overflow:auto;white-space:normal;word-wrap:break-word;min-height:100px}\n" +
                "        td, input, button, select, body{font-family:Helvetica, 'Microsoft Yahei', verdana}\n" +
                "        pre {white-space:pre-wrap !important;white-space:-moz-pre-wrap;white-space:-pre-wrap;white-space:-o-pre-wrap;word-wrap:break-word;width:95%}\n" +
                "        pre * { white-space: unset !important; }\n" +
                "        th,td{font-family:arial,verdana,sans-serif;line-height:1.666}\n" +
                "        img{ border:0}\n" +
                "        header,footer,section,aside,article,nav,hgroup,figure,figcaption{display:block}\n" +
                "        blockquote{margin-right:0px}\n" +
                "        </style>\n" +
                "        <style id=\"ntes_link_color\" type=\"text/css\">a,td a{color:#3370FF}</style>\n" +
                "    </body>\n" +
                "</html>";

        //        user.email.not_blank

    }
}
