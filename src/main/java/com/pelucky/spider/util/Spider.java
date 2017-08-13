package com.pelucky.spider.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Spider {
    private Map<String, String> weChatId;
    public Map<String, String> getWeChatId() {
        return weChatId;
    }

    private LinkedHashMap<String, String> articleInfo;
    private HashMap<String, LinkedHashMap<String, String>> sortOfArticles;
    private final int lastDays;
    private boolean continueFlag;
    private int pageIndex;
    private Logger logger = LoggerFactory.getLogger(Spider.class);

    public Spider(String URL, int lastDays, String[] wxSubscription) {
        articleInfo = new LinkedHashMap<String, String>();
        weChatId = new LinkedHashMap<String, String>();
        sortOfArticles = new HashMap<>();
        continueFlag = true;
        pageIndex = 1;
        this.lastDays = lastDays;
        setWeChatId(wxSubscription);
    }

    public HashMap<String, LinkedHashMap<String, String>> getSortOfArticles() {
        return sortOfArticles;
    }

    @SuppressWarnings("unchecked")
    public void startSpirder() {
        logger.info("Start Spider...");
        for (Iterator<String> iterator = weChatId.keySet().iterator(); iterator.hasNext();) {
            String wxId = (String) iterator.next();
            String wxUrl = weChatId.get(wxId);
            System.out.println("Begin Crawl " + wxId + " For latest " + lastDays + " Days Articles! Please wait...");
            pageIndex = 1;
            continueFlag = true;
            String totalURL = "http://top.aiweibang.com/article/getarticles?Wechat=" + wxUrl;
            // At most crawl 10 pages
            while (continueFlag & pageIndex <= 10) {
                try {
                    int retryTimes = 0;
                    String content = "";
                    content = getContent(totalURL, wxUrl, pageIndex);
                    while (content == null && retryTimes < 3) {
                        // Sleep 5s for retry
                        Thread.sleep(5000);
                        content = getContent(totalURL, wxUrl, pageIndex);
                        retryTimes++;
                        logger.info("{} get {}'s content failed, retries {} time(s).", wxId, totalURL, retryTimes);
                    }

                    if (retryTimes == 3) {
                        logger.info("Can't get {}'s content in {} after 3 times tries or page is missing!", wxId,
                                totalURL);
                        pageIndex += 1;
                        continue;
                    }
                    storeData(content);
                    // Sleep 2s for forbid IP;
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    logger.info("{} Interrupted Exception...", wxId);
                    e.printStackTrace();
                }
            }

            for (Iterator<String> iterator1 = articleInfo.keySet().iterator(); iterator1.hasNext();) {
                String string = (String) iterator1.next();
                System.out.println(articleInfo.get(string) + string);
            }
            System.out.println("Crawl " + wxId + " Finished!");
            System.out.println("---------------------------");
            System.out.println();
            logger.info("Crawl {} Finished!", wxId);

            sortOfArticles.put(wxId, (LinkedHashMap<String, String>) articleInfo.clone());
            articleInfo.clear();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.info("Interrupted Exception...");
                e.printStackTrace();
            }
        }
    }

    private String getContent(String url, String wxUrl, int pageIndex) {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        ArrayList<NameValuePair> postParameters = new ArrayList<>();
        postParameters.add(new BasicNameValuePair("Wechat", wxUrl));
        postParameters.add(new BasicNameValuePair("PageIndex", String.valueOf(pageIndex)));
        postParameters.add(new BasicNameValuePair("PageSize", "20"));
        postParameters.add(new BasicNameValuePair("Type", "1"));
        String content = getResponse(postContent(httpClient, url, postParameters), "UTF-8");

        // Close httpClient
        try {
            httpClient.close();
        } catch (IOException e1) {
            logger.info(e1.getMessage());
        }
        return content;
    }

    private void storeData(String content) {

        final String URL = "http://top.aiweibang.com/article/url?aid=";

        JSONObject jsonObject = new JSONObject(content);
        JSONArray arr = jsonObject.getJSONObject("data").getJSONArray("data");

        for (int i = 0; i < arr.length(); i++) {
            JSONObject data = arr.getJSONObject(i);
            String urlId = data.getString("Id");
            String title = data.getString("Title");
            String postTime = data.getString("PostTime").substring(0, 10);
            String readNum = String.valueOf(data.get("ReadNum"));
            String likeNum = String.valueOf(data.get("LikeNum"));

            if (isInDate(postTime, lastDays)) {
                articleInfo.put("(" + URL + urlId + " &nbsp;)",
                        "Page" + pageIndex + "| [" + postTime + "| <strong>" + title + "</strong> (" + readNum + "|" + likeNum + ")]");
            } else {
                continueFlag = false;
                break;
            }
        }
        pageIndex += 1;
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

    public void setWeChatId(String[] wxSubscription) {
        for (String wxId : wxSubscription) {
            String url = "http://top.aiweibang.com/user/getsearch?kw=" + wxId;
            CloseableHttpClient httpClient = HttpClients.createDefault();

            ArrayList<NameValuePair> postParameters = new ArrayList<>();
            postParameters.add(new BasicNameValuePair("Kw", wxId));
            postParameters.add(new BasicNameValuePair("PageIndex", "1"));
            postParameters.add(new BasicNameValuePair("PageSize", "10"));
            String content = getResponse(postContent(httpClient, url, postParameters), "UTF-8");

            // Close httpClient
            try {
                httpClient.close();
            } catch (IOException e1) {
                logger.info(e1.getMessage());
            }

            JSONObject jsonObject = new JSONObject(content);
            String wxUrl = jsonObject.getJSONObject("data").getJSONArray("data").getJSONObject(0).getString("Id");
            weChatId.put(wxId, wxUrl);
        }
    }

    /**
     * Post Method to post content
     *
     * @param url
     * @param postParameters
     * @return
     */
    public CloseableHttpResponse postContent(CloseableHttpClient httpClient, String url,
            ArrayList<NameValuePair> postParameters) {

        // Set Form parameters
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36");
        httpPost.addHeader("Content-Type", "text/html; charset=UTF-8");

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParameters, Consts.UTF_8);
        httpPost.setEntity(entity);

        // Get response
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpPost);
        } catch (IOException e) {
            logger.info(e.getMessage());
        }
        return httpResponse;
    }

    /**
     * Get Response from get or post method and close httpclient
     *
     * @param httpResponse
     * @param coding
     * @return
     */
    public String getResponse(CloseableHttpResponse httpResponse, String coding) {
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        String content = "";
        if (statusCode < 400) {
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(
                        new InputStreamReader(httpResponse.getEntity().getContent(), coding));
            } catch (UnsupportedEncodingException e) {
                logger.info(e.getMessage());
            } catch (UnsupportedOperationException e) {
                logger.info(e.getMessage());
            } catch (IOException e) {
                logger.info(e.getMessage());
            }

            // Read content
            try {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    content += line;
                }
            } catch (IOException e) {
                logger.info(e.getMessage());
            }

        } else {
            logger.info("Can't post url, Please check network!");
            System.exit(1);
        }
        return content;
    }
}
