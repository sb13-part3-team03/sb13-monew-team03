package com.codeit.monew.article.collector;

import com.codeit.monew.article.dto.collection.CollectedArticleDTO;
import com.codeit.monew.article.entity.ArticleSource;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Slf4j
public abstract class RssNewsCollector implements NewsCollector {

    private static final int CONNECT_TIMEOUT_MS = 3000;
    private static final int READ_TIMEOUT_MS = 5000;

    private final String feedUrl;

    protected RssNewsCollector(String feedUrl) {
        this.feedUrl = feedUrl;
    }

    protected abstract ArticleSource getSource();

    @Override
    public List<CollectedArticleDTO> collect(String keyword) {
        try {
            SyndFeedInput input = new SyndFeedInput();

            URLConnection connection = new URL(feedUrl).openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);

            try (XmlReader reader = new XmlReader(connection)) {
                SyndFeed feed = input.build(reader);

                return feed.getEntries().stream()
                        .filter(this::hasPublishedDate)
                        .filter(entry -> containsKeyword(entry, keyword))
                        .map(this::toCollectedArticle)
                        .toList();
            }

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

        String description = getDescription(entry)
                .toLowerCase(Locale.ROOT);

        return title.contains(lowerKeyword)
                || description.contains(lowerKeyword);
    }

    private CollectedArticleDTO toCollectedArticle(
            SyndEntry entry
    ) {
        String description = getDescription(entry);

        Instant publishDate = entry.getPublishedDate().toInstant();

        return new CollectedArticleDTO(
                getSource(),
                entry.getLink(),
                entry.getTitle(),
                description,
                publishDate
        );
    }

    private String getDescription(SyndEntry entry) {
        if (entry.getDescription() == null
                || entry.getDescription().getValue() == null) {
            return "";
        }

        return entry.getDescription().getValue();
    }

    private boolean hasPublishedDate(SyndEntry entry) {
        if (entry.getPublishedDate() == null) {
            log.warn(
                    "RSS 기사 발행일 누락으로 기사를 건너뜁니다. source={}, url={}",
                    getSource(),
                    entry.getLink()
            );
            return false;
        }

        return true;
    }
}