package com.pelucky.spider.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Spider {
    private final LinkedHashSet<String> subscriptionName;
    private LinkedHashMap<String, String> articleInfo;
    private HashMap<String, LinkedHashMap<String, String>> sortOfArticles;
    private final String URL;
    private final int lastDays;
    private boolean continueFlag;
    private int pageCount;
    private Logger logger = LoggerFactory.getLogger(Spider.class);

    public Spider(String URL, int lastDays, String[] wxSubscription) {
        articleInfo = new LinkedHashMap<String, String>();
        subscriptionName = new LinkedHashSet<String>();
        sortOfArticles = new HashMap<>();
        continueFlag = true;
        pageCount = 0;
        this.URL = URL;
        this.lastDays = lastDays;
        for (String string : wxSubscription) {
            subscriptionName.add(string);
        }
    }

    public HashMap<String, LinkedHashMap<String, String>> getSortOfArticles() {
        return sortOfArticles;
    }

    @SuppressWarnings("unchecked")
    public void startSpirder() {
        logger.info("Strat Spider...");
        for (String name : subscriptionName) {
            System.out.println("Begin Crawl " + name + " For latest " + lastDays + " Days Articles!");
            pageCount = 0;
            continueFlag = true;
            while (continueFlag) {
                try {
                    String totalURL = getURL(name);
                    String content = getContent(totalURL);
                    storeData(content);
                    // Sleep 2s for forbid IP;
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    logger.info("{} Interrupted Exception...", name);
                    e.printStackTrace();
                }
            }

            for (Iterator<String> iterator = articleInfo.keySet().iterator(); iterator.hasNext();) {
                String string = (String) iterator.next();
                System.out.println(articleInfo.get(string) + string);
            }
            System.out.println("Crawl " + name + " Finished!");
            System.out.println("---------------------------");
            System.out.println();
            logger.info("Crawl {} Finished!", name);

            sortOfArticles.put(name, (LinkedHashMap<String, String>) articleInfo.clone());
            articleInfo.clear();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.info("Interrupted Exception...");
                e.printStackTrace();
            }
        }
    }

    private String getURL(String name) {
        String totalURL = URL + "/account/" + name + "?start=" + pageCount;
        return totalURL;
    }

    private String getContent(String totalURL) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(30000).build();
        HttpGet httpGet = new HttpGet(totalURL);
        httpGet.addHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
        httpGet.setConfig(requestConfig);
        CloseableHttpResponse httpResponse = null;

        String content = "";
        try {
            httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            content = EntityUtils.toString(entity);

        } catch (IOException e) {
            logger.info("{} IOException in get", totalURL);
            e.printStackTrace();
        } finally {
            try {
                httpResponse.close();
            } catch (IOException e) {
                logger.info("{} IOException in close", totalURL);
                e.printStackTrace();
            }
        }
        return content;
    }

    private void storeData(String content) {
        try {
            org.jsoup.nodes.Document doc = Jsoup.parse(content);
            Elements h2 = doc.select("h2");
            for (Element element : h2) {
                String linkText = element.child(0).child(0).text();
                String linkHref = element.child(0).child(0).attr("href");
                String linkDate = element.child(0).child(1).text();
                if (isInDate(linkDate, lastDays)) {
                    articleInfo.put("(" + URL + linkHref + ")", "![" + linkDate + " " + linkText + "]");
                } else {
                    continueFlag = false;
                    break;
                }
            }
            pageCount += 12;
        } catch (Exception e) {
            continueFlag = false;
            e.printStackTrace();
        }
    }

    private boolean isInDate(String linkDate, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -days);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String lastDates = format.format(calendar.getTime());
        if (lastDates.compareTo(linkDate) <= 0) {
            return true;
        } else {
            return false;
        }
    }
}
