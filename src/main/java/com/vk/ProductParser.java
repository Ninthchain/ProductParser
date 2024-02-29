package com.vk;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class ProductParser {
    private Document document;

    public ProductParser(String url) throws IOException {
        this.document = Jsoup.connect(url).get();

    }

    public String getProductName() {
        return this.document.getElementsByAttributeValue("id", "pagetitle").text();
    }
    public String getProductBrandName() {
        String brandName = this.document.getElementsByClass("brand_picture").get(0).attribute("href").getValue();
        brandName = brandName.replace("/", "").replace("brands", "");

        return brandName;
    }
}
