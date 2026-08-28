package com.codeit.monew.article.collector;

import com.codeit.monew.article.entity.ArticleSource;
import org.springframework.stereotype.Component;

@Component
public class YeonhapNewsCollector extends RssNewsCollector {

    public YeonhapNewsCollector() {
        super("https://www.yna.co.kr/rss/news.xml");
    }

    YeonhapNewsCollector(String feedUrl) {
        super(feedUrl);
    }

    @Override
    protected ArticleSource getSource() {
        return ArticleSource.YEONHAP;
    }
}