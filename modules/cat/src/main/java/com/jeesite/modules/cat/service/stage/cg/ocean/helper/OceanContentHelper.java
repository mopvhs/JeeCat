package com.jeesite.modules.cat.service.stage.cg.ocean.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.DateTimeUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import com.jeesite.modules.cat.service.cg.third.dto.ShortUrlDetail;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageSyncIndex;
import com.jeesite.modules.cat.service.stage.cg.ocean.v2.AbstraUpOceanStage;
import com.jeesite.modules.cat.service.toolbox.CommandService;
import com.mchange.lang.ByteUtils;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OceanContentHelper {

    public static List<String> deletions = new ArrayList<>();
    public static List<AbstraUpOceanStage.TextBO> deletionTexts = new ArrayList<>();

    static {
        deletions.add("豪车");
        deletions.add("❗");
        deletions.add("速度❗❗✋慢无~");
        deletions.add("✋慢无~");
        deletions.add("✋慢无");
        deletions.add("漏栋价");
        deletions.add("漏栋");
        deletions.add("随时无~");
        deletions.add("超超豪车‼手慢无无无");
        deletions.add("☞复制去桃宝弹出：");
        deletions.add("☞复制去桃宝弹出:");
        deletions.add("🦆");
        deletions.add("\uD83E\uDD86"); // 🦆
        deletions.add("🐱");
        deletions.add("\uD83D\uDC31"); // 🐱
        deletions.add("🐔");
        deletions.add("\uD83D\uDC14");
        deletions.add("⚠");
        deletions.add("🐦");
        deletions.add("\uD83D\uDC26");
        deletions.add("👇");
        deletions.add("\uD83D\uDC47");
        deletions.add("✅");
        deletions.add("👉复制去🍑宝");
        deletions.add("\uD83D\uDC49复制去\uD83C\uDF51宝");
        deletions.add("--");
        deletions.add("復zhi打开𝙏𝙖𝙤𝘽𝙖𝙤 𝘼𝙋𝙋");
        deletions.add("復zhi打开\uD835\uDE4F\uD835\uDE56\uD835\uDE64\uD835\uDE3D\uD835\uDE56\uD835\uDE64 \uD835\uDE3C\uD835\uDE4B\uD835\uDE4B");
        deletions.add("可可独家");
        deletions.add("可可首发");
        deletions.add("可可");

        deletions.add("好价");
        deletions.add("简单车");
        deletions.add("速度");
        deletions.add("速度手慢无");
        deletions.add("活动稀少");
        deletions.add("手慢无~");
        deletions.add("手慢无");
        deletions.add("\uD83D\uDC36"); // 🐶
        deletions.add("☞复制去淘宝弹出：");
        deletions.add("快冲‼");
        deletions.add("‼");
        deletions.add("！");
        deletions.add("!");
        deletions.add("进猫车群#COCO猫舍");
        deletions.add("_________________");
        deletions.add("@所有人");
        deletions.add("\uD83D\uDC31车:https://s.q5url.cn/yA7U");
        deletions.add("车:http://t.q5url.cn");
        deletions.add("车:http://*.q5url.cn");
        deletions.add("超级豪车");
        deletions.add("超级豪车冲");
        deletions.add("历史猫车");
        deletions.add("历史猫车http://t.q5url.cn");
        deletions.add("BGW77rwneED50ap");
        deletions.add("☞复制去淘宝弹出:");
        deletions.add("猫车http://t.q5url.cn/o111p0");
        deletions.add("http://t.q5url.cn/dh.html");
        deletions.add("猫车 http://t.q5url.cn/1kVxzo");
        deletions.add("直接戳👉：");
        deletions.add("直接戳👉:");
        deletions.add("对标绿十字");
        deletions.add("对标许翠花");
        deletions.add("史低随时无");
        deletions.add("平台自营史低️随时结束");
        deletions.add("随时🈚");
        deletions.add("速冲");
        deletions.add("近期史低随时无");
        deletions.add("近期史低");
        deletions.add("近期最低");
        deletions.add("近期最低价");
        deletions.add("手漫无");
        deletions.add("史低");
        deletions.add("近期史低");
        deletions.add("自营史低");
        deletions.add("试吃～");
        deletions.add("稀少活动");
        deletions.add("旗舰店随时无");
        deletions.add("高品质好粮");
        deletions.add("史低，");
        deletions.add("❶");
        deletions.add("❷");
        deletions.add("❸");
        deletions.add("❹");
        deletions.add("❺");
        deletions.add("冲");
        deletions.add("近期史.抵.");
        deletions.add("史.抵.");
        deletions.add("猫车:mp://WoWnjYcWSdj01es");
        deletions.add("mp://WoWnjYcWSdj01es");
        deletions.add("猫车:mp://WoWnjYcWSdj01es");
        deletions.add("mp://WoWnjYcWSdj01es");
        deletions.add("全网最低价");
        deletions.add("WoWnjYcWSdj01es");
        deletions.add("小小独家");
        deletions.add("直接戳\uD83D\uDC49:");
        deletions.add("猫车入口#COCO猫舍");
        deletions.add("https://y-03.cn/4zbilN");
//        deletions.add("猫车:mp://");

        for (String item : deletions) {
            AbstraUpOceanStage.TextBO textBO = new AbstraUpOceanStage.TextBO(item, item.length());
            deletionTexts.add(textBO);
        }

        // 排序，按大到小
        // 使用Collections.sort方法和自定义Comparator进行排序
        deletionTexts.sort(new Comparator<AbstraUpOceanStage.TextBO>() {
            @Override
            public int compare(AbstraUpOceanStage.TextBO o1, AbstraUpOceanStage.TextBO o2) {
                // 按照size字段从大到小排序
                return Integer.compare(o2.getSize(), o1.getSize());
            }
        });
    }

    public static List<String> deletionUrls = new ArrayList<>();

    static {
        deletionUrls.add("s.q5url.cn");
        deletionUrls.add("http://t.q5url.cn/1kVxzo");
        deletionUrls.add("http://t.q5url.cn");
        deletionUrls.add("车群");
    }

    public static List<String> regularDeletion = new ArrayList<>();

    static {
        regularDeletion.add("^(?=.*猫车)(?=.*mp:\\/\\/).+$");
//        regularDeletion.add("^(?=.*mp:\\/\\/).+$");
        regularDeletion.add("^(?=.*猫)(?=.*车).+$");
        regularDeletion.add("^(?=.*车)(?=.*群).+$");
        regularDeletion.add( "\\p{InTransportandMapSymbols}+群.*");
    }

    public static Map<String, List<Pattern>> groupWhiteUrlMap = new HashMap<>();
    static {
        List<Pattern> whiteUrls = new ArrayList<>();
        whiteUrls.add(Pattern.compile("y.q5url.cn/[a-z A-Z 0-9]+"));
        whiteUrls.add(Pattern.compile("jd.q5url.cn/[a-z A-Z 0-9]+"));
        whiteUrls.add(Pattern.compile("t.q5url.cn/[a-z A-Z 0-9]+"));

        groupWhiteUrlMap.put("q5url.cn", whiteUrls);
    }

    /**
     * 命中关键词，直接不进公海
     */
    public static List<String> failTexts = new ArrayList<>();

    static {
        failTexts.add("冠军标");
        failTexts.add("元佑双标");
    }

    public static Map<String, String> replacements = new LinkedHashMap<>();

    static {
        replacements.put("：", ":");
        replacements.put("卷", "券");
        replacements.put("锩", "券");
        replacements.put("蕞低", "最低");
        replacements.put("加车1件", "加购一件");
        replacements.put("plus\\+首单", "Plus叠首单");
        replacements.put("plus", "Plus");
        replacements.put("亓", "元");
        replacements.put("旗见店", "旗舰店");
        replacements.put("好萍仮", "好返");
        replacements.put("到✋", "到手价");
        replacements.put("拼\\.团\\.", "拼团");
        replacements.put("帼际", "国际");
        replacements.put("桃宝", "淘宝");
        replacements.put("✖", "*");
        replacements.put("坤", "鸡");
        replacements.put("➕", "&");
        replacements.put("普素羊肉", "经典鲜羊肉");
        replacements.put("好萍", "好评");
        replacements.put("原本", "日常");
        replacements.put("不吃包tui", "不吃包退");
        replacements.put("好反", "好返");
        replacements.put("荭包", "红包");
        replacements.put("帼", "国");
        replacements.put("忦值", "价值");
        replacements.put("忦", "价");

        replacements.put("拍1:", "加购一件\n");
        replacements.put("拍2:", "加购两件\n");
        replacements.put("拍3:", "加购三件\n");
        replacements.put("拍4:", "加购四件\n");
        replacements.put("拍5:", "加购五件\n");
        replacements.put("拍6:", "加购六件\n");
        replacements.put("拍7:", "加购七件\n");
        replacements.put("拍8:", "加购八件\n");
        replacements.put("拍9:", "加购九件\n");
        replacements.put("拍10:", "加购十件\n");

        replacements.put("凑1:", "凑单一件\n");
        replacements.put("凑2:", "凑单两件\n");
        replacements.put("凑3:", "凑单三件\n");
        replacements.put("凑4:", "凑单四件\n");
        replacements.put("凑5:", "凑单五件\n");
        replacements.put("凑6:", "凑单六件\n");
        replacements.put("凑7:", "凑单七件\n");
        replacements.put("凑8:", "凑单八件\n");
        replacements.put("凑9:", "凑单九件\n");
        replacements.put("凑10:", "凑单十件\n");

        replacements.put("加1:", "加购一件\n");
        replacements.put("加2:", "加购两件\n");
        replacements.put("加3:", "加购三件\n");
        replacements.put("加4:", "加购四件\n");
        replacements.put("加5:", "加购五件\n");
        replacements.put("加6:", "加购六件\n");
        replacements.put("加7:", "加购七件\n");
        replacements.put("加8:", "加购八件\n");
        replacements.put("加9:", "加购九件\n");
        replacements.put("加10:", "加购十件\n");

        replacements.put("包由", "包邮");
        replacements.put("棋舰惦", "旗舰店");
        replacements.put("湊箪", "凑单");
        replacements.put("巻", "券");
        replacements.put("惊東", "京东");
        replacements.put("領", "领");
        replacements.put("构物車", "购物车");
        replacements.put("付窾", "付款");

        replacements.put("奍", "券");
        replacements.put("αρρ", "app");
        replacements.put("百.亿.卜.贴.", "百亿补贴");
        replacements.put("送貨上扪", "送货上门");
        replacements.put("拼団", "拼团");
        replacements.put("保價", "保价");
        replacements.put("附歀", "付款");
        replacements.put("补萜", "补贴");
        replacements.put("浮力", "福利");
        replacements.put("钩物袋", "购物袋");

        replacements.put("仮", "返");
        replacements.put("钭音", "抖音");
        replacements.put("削量", "销量");
        replacements.put("钩物车", "购物车");
        replacements.put("爆窾", "爆款");
        replacements.put("自萤", "自营");
        replacements.put("陋洞", "漏洞");
        replacements.put("天緢", "天猫");
        replacements.put("变忦", "变价");

        replacements.put("构物莗", "购物车");
        replacements.put("凑箪", "凑单");
        replacements.put("构物董", "购物车");
        replacements.put("劵", "券");
        replacements.put("超级卜贴", "超级补贴");
        replacements.put("下箪", "下单");
        replacements.put("國伋", "国际");
        replacements.put("凑単", "凑单");
        replacements.put("単", "单");
        replacements.put("淘.金.币补.贴", "淘金币补贴");
        replacements.put("国bu", "国补");
        replacements.put("加车去小程序付款↓", "加车去小程序付");
        replacements.put("如糖↓", "乳糖");
        replacements.put("眇殺", "秒杀");
        replacements.put("支fu", "支付");
        replacements.put("更D", "更低");
        replacements.put("凑掸", "凑单");
        replacements.put("淘.金币", "淘金币");
        replacements.put("页.面", "页面");
        replacements.put("蕞.抵", "最低");
        replacements.put("补.贴", "补贴");
        replacements.put("⭕", "券");
        replacements.put("漏\uD83D\uDD73\uFE0F", "漏洞");
        replacements.put("掏宝", "淘宝");
        replacements.put("秒殺", "秒杀");
        replacements.put("兔单", "免单");
        replacements.put("荭笣", "红包");
        replacements.put("加杩", "加码");
        replacements.put("更D\uFE0F", "更低");
        replacements.put("更D", "更低");

        replacements.put("(KkkHVd6OmlS)/ AC00", "(KFUNVeHtLNC)/ CA21,)/ AC01");
        replacements.put("(ANKaVeHdXnO)/ AC00", "(KFUNVeHtLNC)/ CA21,)/ AC01");
        replacements.put("(ARp9Ves5iCL)/ AC00", "(KFUNVeHtLNC)/ CA21,)/ AC01");
        replacements.put("(8TDRVetNj8P)/ AC00", "(KFUNVeHtLNC)/ CA21,)/ AC01");
        replacements.put("/8TDRVetNj8P// AC00", "(KFUNVeHtLNC)/ CA21,)/ AC01");
        replacements.put("(cjPVVetB2f4)/ AC00", "(KFUNVeHtLNC)/ CA21,)/ AC01");
        replacements.put("(lEf6VetJXBH)/ AC00", "(KFUNVeHtLNC)/ CA21,)/ AC01");
        replacements.put("/ZnEuVeG7FEj// AC01", "(KFUNVeHtLNC)/ CA21,)/ AC01");

    }

    public static String interposeMsg(String msg) {
        if (StringUtils.isBlank(msg)) {
            return msg;
        }

        // 规则
        // 先做replace
        // 遍历替换规则并进行替换
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            msg = msg.replaceAll(entry.getKey(), entry.getValue());
        }

        msg = msg.replaceAll("\u2028", "\n");
        // 构建排序，长的文本需要先匹配做删除
        String[] split = msg.split("\n");
        StringBuilder builder = new StringBuilder();

        // 特殊空格
        byte[] bytes = new byte[]{-17, -72, -113};
        String hexAscii = ByteUtils.toHexAscii(bytes);

        for (String failText : failTexts) {
            if (msg.contains(failText)) {
                throw new IllegalArgumentException("messageSync contains fail . msg: " + msg + ", text " + failText);
            }
        }

        for (String line : split) {
            String replace = line;
            // 处理@某某人
            if (replace.contains("@") && replace.contains(" ")) {
                int i = replace.indexOf("@");
                if (i >= 0) {
                    String a = replace.substring(i);
                    if (a.contains(" ")) {
                        int b = a.indexOf(" ");
                        if (b > 0) {
                            String substring = a.substring(0, b);
                            replace = replace.replace(substring, "");
                        }
                    }
                }
            }

            if (replace.equals("-")) {
                continue;
            }

            // 是否包含 url
            for (String url : deletionUrls) {
                if (replace.contains(url)) {
                    replace = null;
                    break;
                }
            }
            if (StringUtils.isBlank(replace)) {
                continue;
            }

            for (String regular : regularDeletion) {
                Pattern p = Pattern.compile(regular);
                Matcher matcher = p.matcher(replace);
                if (matcher.find()) {
                    replace = null;
                    break;
                }
            }
            if (StringUtils.isBlank(replace)) {
                continue;
            }

            String replaceAscii = ByteUtils.toHexAscii(replace.getBytes(StandardCharsets.UTF_8));

            if (replace.equals("\n") || replace.equals("\uFE0F\uFE0F") || hexAscii.equals(replace) || hexAscii.equals(replaceAscii)) {
                continue;
            }

            for (AbstraUpOceanStage.TextBO textBO : deletionTexts) {
                replace = replace.replaceAll(textBO.getText(), "");
            }

            if (StringUtils.isBlank(replace)) {
                continue;
            }

            // 判断是否有特殊的url，是的话，只允许白名单的域名
            for (Map.Entry<String, List<Pattern>> entry : groupWhiteUrlMap.entrySet()) {
                String urlDomain = entry.getKey();
                if (replace.contains(urlDomain)) {
                    // 存在，提取出url
                    Matcher matcher = CommandService.url.matcher(replace);
                    boolean pass = false;
                    if (matcher.find()) {
                        String group = StringUtils.trim2(matcher.group());
                        // 判断是否是白名单的 url
                        for (Pattern whiteUrl : entry.getValue()) {
                            Matcher m = whiteUrl.matcher(group);
                            if (m.find()) {
                                // 把一下其他的内容吃掉，例如url：https://y.q5url.cn/2Gd8j3 /  -> https://y.q5url.cn/2Gd8j3
                                replace = group;
                                pass = true;
                                break;
                            }
                        }
                    } else {
                        // 匹配失败，先忽略
                    }
                    if (!pass) {
                        replace = "";
                        break;
                    }
                }
            }

            replaceAscii = ByteUtils.toHexAscii(replace.getBytes(StandardCharsets.UTF_8));
            if (replace.equals("\n") || replace.equals("\uFE0F\uFE0F") || hexAscii.equals(replaceAscii)) {
                continue;
            }

            Matcher matcher = CommandService.tb.matcher(replace);
            if (matcher.find()) {
                replace = StringUtils.trim2(replace);
            }

            builder.append(replace).append("\n");
        }

        return builder.toString();
    }

    public static String interposeMsgWithRule(String msg,
                                              Map<String, String> keywordReplacement,
                                              List<AbstraUpOceanStage.TextBO> deletes,
                                              List<String> failTexts) {
        if (StringUtils.isBlank(msg)) {
            return msg;
        }

        // 规则
        // 先做replace
        // 遍历替换规则并进行替换
        for (Map.Entry<String, String> entry : keywordReplacement.entrySet()) {
            msg = msg.replaceAll(entry.getKey(), entry.getValue());
        }

        msg = msg.replaceAll("\u2028", "\n");
        // 构建排序，长的文本需要先匹配做删除
        String[] split = msg.split("\n");
        StringBuilder builder = new StringBuilder();

        // 特殊空格
        byte[] bytes = new byte[]{-17, -72, -113};
        String hexAscii = ByteUtils.toHexAscii(bytes);

        for (String failText : failTexts) {
            if (msg.contains(failText)) {
                throw new IllegalArgumentException("messageSync contains fail . msg: " + msg + ", text " + failText);
            }
        }

        for (String line : split) {
            String replace = line;
            // 处理@某某人
            if (replace.contains("@") && replace.contains(" ")) {
                int i = replace.indexOf("@");
                if (i >= 0) {
                    String a = replace.substring(i);
                    if (a.contains(" ")) {
                        int b = a.indexOf(" ");
                        if (b > 0) {
                            String substring = a.substring(0, b);
                            replace = replace.replace(substring, "");
                        }
                    }
                }
            }

            if (replace.equals("-")) {
                continue;
            }

            // 是否包含 url
            for (String url : deletionUrls) {
                if (replace.contains(url)) {
                    replace = null;
                    break;
                }
            }
            if (StringUtils.isBlank(replace)) {
                continue;
            }

            for (String regular : regularDeletion) {
                Pattern p = Pattern.compile(regular);
                Matcher matcher = p.matcher(replace);
                if (matcher.find()) {
                    replace = null;
                    break;
                }
            }
            if (StringUtils.isBlank(replace)) {
                continue;
            }

            String replaceAscii = ByteUtils.toHexAscii(replace.getBytes(StandardCharsets.UTF_8));

            if (replace.equals("\n") || replace.equals("\uFE0F\uFE0F") || hexAscii.equals(replace) || hexAscii.equals(replaceAscii)) {
                continue;
            }

            for (AbstraUpOceanStage.TextBO textBO : deletes) {
                replace = replace.replaceAll(textBO.getText(), "");
            }

            if (StringUtils.isBlank(replace)) {
                continue;
            }

            // 判断是否有特殊的url，是的话，只允许白名单的域名
            for (Map.Entry<String, List<Pattern>> entry : groupWhiteUrlMap.entrySet()) {
                String urlDomain = entry.getKey();
                if (replace.contains(urlDomain)) {
                    // 存在，提取出url
                    Matcher matcher = CommandService.url.matcher(replace);
                    boolean pass = false;
                    if (matcher.find()) {
                        String group = StringUtils.trim2(matcher.group());
                        // 判断是否是白名单的 url
                        for (Pattern whiteUrl : entry.getValue()) {
                            Matcher m = whiteUrl.matcher(group);
                            if (m.find()) {
                                // 把一下其他的内容吃掉，例如url：https://y.q5url.cn/2Gd8j3 /  -> https://y.q5url.cn/2Gd8j3
                                replace = group;
                                pass = true;
                                break;
                            }
                        }
                    } else {
                        // 匹配失败，先忽略
                    }
                    if (!pass) {
                        replace = "";
                        break;
                    }
                }
            }

            replaceAscii = ByteUtils.toHexAscii(replace.getBytes(StandardCharsets.UTF_8));
            if (replace.equals("\n") || replace.equals("\uFE0F\uFE0F") || hexAscii.equals(replaceAscii)) {
                continue;
            }

            Matcher matcher = CommandService.tb.matcher(replace);
            if (matcher.find()) {
                replace = StringUtils.trim2(replace);
            }

            builder.append(replace).append("\n");
        }

        return builder.toString();
    }


    public static void main(String[] args) {
//        String msg = "\uD83D\uDE972ddd2群y-02.com/2s3zjS";
        String msg = "\uD83D\uDE97群y-02.com/2s3zjS";

//        String c = OceanContentHelper.interposeMsg(msg);
        for (String regular : regularDeletion) {
            Pattern p = Pattern.compile(regular);
            Matcher matcher = p.matcher(msg);
            if (matcher.find()) {
                msg = null;
                break;
            }
        }

        System.out.println(msg);
    }

    public static String match(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group();
        }

        return null;
    }


    public static List<Map<String, Object>> getMessageSyncIndex(List<MaocheRobotCrawlerMessageSyncDO> crawlerMessages) {
        if (CollectionUtils.isEmpty(crawlerMessages)) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (MaocheRobotCrawlerMessageSyncDO item : crawlerMessages) {
            MaocheMessageSyncIndex index = MaocheMessageSyncIndex.toIndex(item);
            if (index == null) {
                continue;
            }
            Map<String, Object> map = JsonUtils.toReferenceType(JsonUtils.toJSONString(index), new TypeReference<Map<String, Object>>() {
            });

            list.add(map);
        }

        return list;
    }

    public static List<Map<String, Object>> getMessageSyncIndex(List<MaocheRobotCrawlerMessageSyncDO> crawlerMessages,
                                                                List<MaocheRobotCrawlerMessageDO> robotMessages) {
        if (CollectionUtils.isEmpty(crawlerMessages) || CollectionUtils.isEmpty(robotMessages)) {
            return new ArrayList<>();
        }

        Map<Long, MaocheRobotCrawlerMessageDO> messageDOMap = robotMessages.stream().collect(Collectors.toMap(MaocheRobotCrawlerMessageDO::getIid, Function.identity(), (o1, o2) -> o1));

        List<Map<String, Object>> list = new ArrayList<>();
        for (MaocheRobotCrawlerMessageSyncDO item : crawlerMessages) {
            MaocheMessageSyncIndex index = MaocheMessageSyncIndex.toIndex(item);
            if (index == null) {
                continue;
            }

            MaocheRobotCrawlerMessageDO messageDO = messageDOMap.get(index.getRobotMsgId());
            if (messageDO != null) {
                index.setRobotChatId(messageDO.getFromgid());
                index.setRobotSendId(messageDO.getFromid());
                index.setOceanStatus(messageDO.getStatus());
                index.setRelationId(messageDO.getRelationId());
//                Long aiOcean = 0L;
//                if ("SPECIAL".equals(messageDO.getStatus()) || ) {
//                    aiOcean = 1L;
//                } else if ("OCEAN".equals(messageDO.getStatus())) {
//                    aiOcean = 2L;
//                }
//                index.setAiOcean(0L);
            }

            Map<String, Object> map = JsonUtils.toReferenceType(JsonUtils.toJSONString(index), new TypeReference<Map<String, Object>>() {
            });

            list.add(map);
        }

        return list;
    }
}
