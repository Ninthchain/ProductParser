package com.vk;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.quad69.myparser.api.http.Response;
import ru.quad69.myparser.api.http.url.URLBuilder;
import ru.quad69.myparser.api.parser.Parser;
import ru.quad69.myparser.api.parser.query.Query;
import ru.quad69.myparser.api.proxy.Proxy;
import ru.quad69.myparser.api.proxy.ProxyProvider;
import ru.wbooster.myparser.ProductResult;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.regex.Pattern;

class DomMaleraProductList extends Parser {
    private static final Pattern SPLITTER = Pattern.compile("#");
    private static final Pattern NO_DIGIT = Pattern.compile("[^\\d.]");
    private static final Pattern COMMA = Pattern.compile(",");
    private static final Pattern MEASURE = Pattern.compile("Единица покупки:\\s*");
    private static final Pattern BX_TOTAL_ITEMS = Pattern.compile("BX\\('catalog_total'\\)\\.innerHTML = '(\\d+)'");
    private static final int PAGE_SIZE = 40;
    @Override
    public void parse(Query query) throws Exception {
        final String[] splitted = SPLITTER.split(query.content(), -2);
        final String productPath = splitted[0];
        final String branchCode = splitted[1];

        final ProductResult row = new ProductResult();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(512);
        final ArrayList<byte[]> results = new ArrayList<>(PAGE_SIZE);

        URLBuilder builder = new URLBuilder("https", "www.dommalera.ru", productPath);
        builder.setPath(builder.getPath().toLowerCase());
        builder.setQuery("is_catalog_ajax", "Y");

        try (Proxy proxy = proxyProvider.acquire("dommalera.ru")){

                Response response = proxy.request("GET", builder.toURL()).send();
                Document document = response.getContentAsHTML();
                Elements elements = document.select(".item_block");

                // Берём номера страничек
                Elements numerationList = elements.select(".nums span");
                int current = 1;

                // В элементах пагинации (они не содержат полный список страниц, но имеется последний) возьмём последний
                int total = Integer.parseInt(numerationList.get(numerationList.size() - 1).text());
            System.out.println(numerationList.get(0).text());
        }
    }
}

