package com.codeit.monew.article.collector;

import com.codeit.monew.article.entity.ArticleSource;
import org.springframework.stereotype.Component;

@Component
public class YonhapNewsCollector extends RssNewsCollector {

    public YonhapNewsCollector() {
        super("https://www.yna.co.kr/rss/news.xml");
    }

    YonhapNewsCollector(String feedUrl) {
        super(feedUrl);
    }

    @Override
    protected ArticleSource getSource() {
        return ArticleSource.YONHAP;
    }
}