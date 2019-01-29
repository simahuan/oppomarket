package com.kong.oppmarket.util;

import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Arrays;

public class AccessibilityUtil {
    private static int tabcount = -1;
    private static StringBuilder sb;

    public static void printPacketInfo(AccessibilityNodeInfo root) {
        sb = new StringBuilder();
        tabcount = 0;
        int[] is = {};
        analysisPacketInfo(root, is);
        Log.d("NODE", sb.toString());
    }

    //打印此时的界面状况,便于分析
    private static void analysisPacketInfo(AccessibilityNodeInfo info, int... ints) {
        if (info == null) {
            return;
        }
        if (tabcount > 0) {
            for (int i = 0; i < tabcount; i++) {
                sb.append("\t\t");
            }
        }
        if (ints != null && ints.length > 0) {
            StringBuilder s = new StringBuilder();
            for (int j = 0; j < ints.length; j++) {
                s.append(ints[j]).append(".");
            }
            sb.append(s).append(" ");
        }
        String name = info.getClassName().toString();
        String[] split = name.split("\\.");
        name = split[split.length - 1];
        if ("TextView".equals(name)) {
            CharSequence text = info.getText();
            sb.append("text:").append(text);
        } else if ("Button".equals(name)) {
            CharSequence text = info.getText();
            sb.append("Button:").append(text);
        } else {
            sb.append(name);
        }
        sb.append("\n");

        int count = info.getChildCount();
        if (count > 0) {
            tabcount++;
            int len = ints.length + 1;
            int[] newInts = Arrays.copyOf(ints, len);

            for (int i = 0; i < count; i++) {
                newInts[len - 1] = i;
                analysisPacketInfo(info.getChild(i), newInts);
            }
            tabcount--;
        }
    }

    //查找节点
    public static AccessibilityNodeInfo findNodeByViewName(AccessibilityNodeInfo info, String viewName) {
        String name = info.getClassName().toString();
        String[] split = name.split("\\.");
        name = split[split.length - 1];
        if (name.equals(viewName)) {
            return info;
        } else {
            int count = info.getChildCount();
            if (count > 0) {
                for (int i = 0; i < count; i++) {
                    AccessibilityNodeInfo inf = findNodeByViewName(info.getChild(i), viewName);
                    if (inf != null) {
                        return inf;
                    }
                }
            } else {
                return null;
            }
        }
        return null;
    }

    private AccessibilityNodeInfo findNodeInfoByResourceName(AccessibilityNodeInfo nodeInfo, int level, int maxLevel, String resourceName) {
        if (null != nodeInfo) {
            if (TextUtils.equals(resourceName, nodeInfo.getViewIdResourceName())) {
                return nodeInfo;
            }
            if (level < maxLevel) {
                for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                    AccessibilityNodeInfo tmp = findNodeInfoByResourceName(nodeInfo.getChild(i), level + 1, maxLevel, resourceName);
                    if (null != tmp) {
                        return tmp;
                    }
                }
            }
        }
        return null;
    }
}
