package com.pelucky.spider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import com.pelucky.spider.util.Mail;
import com.pelucky.spider.util.ReadConfig;
import com.pelucky.spider.util.Spider;

public class SpiderApp {

    private ReadConfig readConfig = new ReadConfig();
    private Spider spider;

    private String crawlContent() {
        spider = new Spider(readConfig.getUrl(), readConfig.getLastDays(), readConfig.getWxSubscription());
        spider.startSpirder();
        HashMap<String, LinkedHashMap<String, String>> sortOfArticle = spider.getSortOfArticles();
        String content = changeToContent(sortOfArticle);
        System.out.println(content);
        return content;
    }

    private String changeToContent(HashMap<String, LinkedHashMap<String, String>> sortOfArticle) {
        String content = new String();
        int totalCount = 0;
        int subScriptionCount = 0;
        for (Iterator<String> iterator = sortOfArticle.keySet().iterator(); iterator.hasNext();) {
            String name = (String) iterator.next();
            LinkedHashMap<String, String> herfTitle = sortOfArticle.get(name);
            subScriptionCount++;
            content += "#0x0" + subScriptionCount + " <strong>" + name + "</strong>: " + readConfig.getUrl() + "/article/" + spider.getWeChatId().get(name)
                    + "<br>";
            int count = 0;
            for (Iterator<String> iterator2 = herfTitle.keySet().iterator(); iterator2.hasNext();) {
                String herf = (String) iterator2.next();
                count++;
                content += count + ". " + sortOfArticle.get(name).get(herf) + " " + herf + "<br>";
            }
            content += "共" + count + "篇。<br>";
            content += "<br>";
            totalCount += count;
        }
        content += "共" + subScriptionCount + "个订阅号,共计" + totalCount + "篇。<br>";
        content += "本邮件由吊大的pel编写并自动发送...<br>";
        return content;
    }

    private void sendMail(String content) {
        Mail mail = new Mail(readConfig.getHostMail(), readConfig.getFromMail(), readConfig.getPassword());
        String[] recipients = readConfig.getToMail();
        String subject = "爱微帮--微信订阅号最近" + Integer.toString(readConfig.getLastDays()) + "日内容";
        List<String> attachmentNames = new ArrayList<String>();
        if (mail.sendMail(recipients, subject, content, attachmentNames)) {
            System.out.println("发送邮件成功");
        } else {
            System.out.println("发送邮件失败");
        }

    }

    public static void main(String[] args) {
        SpiderApp spiderApp = new SpiderApp();
        String Content = spiderApp.crawlContent();
        spiderApp.sendMail(Content);
    }

}
