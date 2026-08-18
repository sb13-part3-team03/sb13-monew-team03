package com.codeit.monew.article.controller;

import com.codeit.monew.article.collector.NaverNewsCollector;
import com.codeit.monew.article.collector.NewsCollector;
import com.codeit.monew.article.dto.collection.CollectedArticleDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class NaverNewsController {

    private final NaverNewsCollector naverNewsCollector;

}