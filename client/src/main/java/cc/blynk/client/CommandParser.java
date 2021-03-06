package cc.blynk.client;

import static cc.blynk.server.core.protocol.enums.Command.*;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 2/1/2015.
 *
 * Convertor between user-friendly command and protocol command code
 */
public class CommandParser {

    public static Short parseCommand(String stringCommand) {
        switch (stringCommand.toLowerCase()) {
            case "hardware" :
                return HARDWARE;
            case "ping" :
                return PING;
            case "loadprofilegzipped" :
                return LOAD_PROFILE_GZIPPED;
            case "sync" :
                return SYNC;
            case "sharing" :
                return SHARING;
            case "gettoken" :
                return GET_TOKEN;
            case "refreshtoken" :
                return REFRESH_TOKEN;
            case "login" :
                return LOGIN;
            case "getgraphdata" :
                return GET_GRAPH_DATA;
            case "activate" :
                return ACTIVATE_DASHBOARD;
            case "deactivate" :
                return DEACTIVATE_DASHBOARD;
            case "register" :
                return REGISTER;
            case "tweet" :
                return TWEET;
            case "email" :
                return EMAIL;
            case "push" :
                return PUSH_NOTIFICATION;
            case "bridge" :
                return BRIDGE;
            case "createdash" :
                return CREATE_DASH;
            case "savedash" :
                return SAVE_DASH;
            case "deletedash" :
                return DELETE_DASH;
            case "createwidget" :
                return CREATE_WIDGET;
            case "updatewidget" :
                return UPDATE_WIDGET;
            case "deletewidget" :
                return DELETE_WIDGET;
            case "hardsync" :
                return HARDWARE_SYNC;
            case "info" :
                return HARDWARE_INFO;

            //sharing section
            case "sharelogin" :
                return SHARE_LOGIN;
            case "getsharetoken" :
                return GET_SHARE_TOKEN;
            case "getshareddash" :
                return GET_SHARED_DASH;
            case "refreshsharetoken" :
                return REFRESH_SHARE_TOKEN;

            default:
                throw new IllegalArgumentException("Unsupported command");
        }
    }

}
