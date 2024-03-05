package com.vk;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.quad69.myparser.api.http.Request;
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

public class DomMaleraProductList extends Parser {
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

        int pageCount = 1;
        int productsPerPage = 40;
        try (Proxy proxy = proxyProvider.acquire("dommalera.ru")) {
            // Первый запрос нужен, чтобы распарсить нужные данные. Кол-во продуктов в странице. Кол-во страниц
            Response response = proxy.request("GET", builder.toURL()).send();
            Document document = response.getContentAsHTML();
            // берём ссылочные тэги содержащие НЕ ПОЛНУЮ нумерацию
            Elements nums = document.select(".module-pagination .nums a");
            Elements products = document.select(".catalog_block.items .catalog_item");
            // берём кол-во страниц исходя из последнего a.
            pageCount = Integer.parseInt(nums.get(nums.size() - 1).text());
            productsPerPage = products.size();

            // Итератор продуктов
            int i = 0;

            // Итератор страниц
            int j = 0;

            // Не стал делать сложность алгоритма n^2. Сделаю одним циклом. Через два итератора
            while (true) {
                i++;
                // Форматируем запрос, чтобы получить следующую страницу. Так как итерация идёт с нуля,
                // то прибавляем к итератору + 1
                String reqUrl = String.format("%s?PAGEN_1=%d", builder.toURL(), j + 1);
                Document page = this.getPage(proxy.request("GET", reqUrl));
                Elements productList = document.select(".catalog_block.items .catalog_item");

                // Сбрасываем при последнем продукте
                if (i == productsPerPage - 1) {
                    i = 0;
                    j++;
                    continue;
                }

                // Выполняется после парсинга всех страниц
                if (j == pageCount - 1) {
                    break;
                }
            }
        }
    }

    Document getPage(Request req) throws Exception {
        Response resp = req.send();
        Document page = resp.getContentAsHTML();
        return page;
    }
}

