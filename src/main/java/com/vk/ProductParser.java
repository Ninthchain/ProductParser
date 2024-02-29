package com.vk;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class ProductParser {
    private Document document;
    private String url;

    public ProductParser(String url) throws IOException {
        this.document = Jsoup.connect(url).get();
    }

    public String getProductName() {
        return null;
    }
    public String getProductBrand() {
        return null;
    }
}
