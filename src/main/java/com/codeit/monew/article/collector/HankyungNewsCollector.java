package com.codeit.monew.article.collector;

import com.codeit.monew.article.entity.ArticleSource;
import org.springframework.stereotype.Component;

@Component
public class HankyungNewsCollector extends RssNewsCollector {

    public HankyungNewsCollector() {
        super("https://www.hankyung.com/feed/all-news");
    }

    HankyungNewsCollector(String feedUrl) {
        super(feedUrl);
    }

    @Override
    protected ArticleSource getSource() {
        return ArticleSource.HANKYUNG;
    }
}