package com.codeit.monew.article.collector;

import com.codeit.monew.article.entity.ArticleSource;
import org.springframework.stereotype.Component;

@Component
public class ChosunNewsCollector extends RssNewsCollector {

    public ChosunNewsCollector() {
        super("https://www.chosun.com/arc/outboundfeeds/rss/?outputType=xml");
    }

    @Override
    protected ArticleSource getSource() {
        return ArticleSource.CHOSUN;
    }
}