package com.pelcuky.spider.util;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.pelucky.spider.util.Spider;


public class SpiderTest {

    private Spider spider;

    @Before
    public void setUp() {
        String[] wxSubscription = {"wow36kr"};
        this.spider = new Spider("http://top.aiweibang.com", 7, wxSubscription);
    }

    @Ignore
    @Test
    public void testGetWeChatId() {
        String[] wxSubscription = {"wow36kr"};
        spider.setWeChatId(wxSubscription);
    }

}
