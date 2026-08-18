package com.codeit.monew.article.collector;

import com.codeit.monew.article.dto.collection.CollectedArticleDTO;
import com.codeit.monew.article.entity.ArticleSource;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

public abstract class RssNewsCollector implements NewsCollector {

    private final String feedUrl;

    protected RssNewsCollector(String feedUrl) {
        this.feedUrl = feedUrl;
    }

    protected abstract ArticleSource getSource();

    @Override
    public List<CollectedArticleDTO> collect(String keyword) {
        try {
            SyndFeedInput input = new SyndFeedInput();

            SyndFeed feed = input.build(
                    new XmlReader(new URL(feedUrl))
            );

            return feed.getEntries().stream()
                    .filter(entry -> containsKeyword(entry, keyword))
                    .map(this::toCollectedArticle)
                    .toList();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "RSS 뉴스 수집에 실패했습니다. source=" + getSource(),
                    e
            );
        }
    }

    private boolean containsKeyword(
            SyndEntry entry,
            String keyword
    ) {
        String lowerKeyword = keyword.toLowerCase(Locale.ROOT);

        String title = entry.getTitle() == null
                ? ""
                : entry.getTitle().toLowerCase(Locale.ROOT);

        String description = entry.getDescription() == null
                ? ""
                : entry.getDescription().getValue()
                  .toLowerCase(Locale.ROOT);

        return title.contains(lowerKeyword)
                || description.contains(lowerKeyword);
    }

    private CollectedArticleDTO toCollectedArticle(
            SyndEntry entry
    ) {
        String description = entry.getDescription() == null
                ? ""
                : entry.getDescription().getValue();

        Instant publishDate = entry.getPublishedDate() == null
                ? Instant.now()
                : entry.getPublishedDate().toInstant();

        return new CollectedArticleDTO(
                getSource(),
                entry.getLink(),
                entry.getTitle(),
                description,
                publishDate
        );
    }
}