package com.vk;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import ru.quad69.myparser.api.http.Response;
import ru.quad69.myparser.api.http.url.URLBuilder;
import ru.quad69.myparser.api.parser.Parser;
import ru.quad69.myparser.api.parser.query.Query;
import ru.quad69.myparser.api.proxy.Proxy;
import ru.wbooster.myparser.Currency;
import ru.wbooster.myparser.Measure;
import ru.wbooster.myparser.ProductResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

public class DomMaleraProductInfo extends Parser {
    private static final Pattern SPLITTER = Pattern.compile("#");
    private static final Pattern BRAND_SLASH = Pattern.compile("\\s*/\\s*");

    @Override
    public void parse(Query query) throws Exception {
        final String[] splitted = SPLITTER.split(query.content(), -2);
        final String productPath = splitted[0];
        final String productCode = splitted[1];

        URLBuilder urlBuilder = new URLBuilder("https", "www.dommalera.ru", productPath);
        ProductResult row = new ProductResult();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (Proxy proxy = proxyProvider.acquire("dommalera.ru")) {
            Response response = proxy.request("GET", urlBuilder.toURL()).send();

            if (response.getStatus() != 200) {
                throw new Exception("Bad request" + response.getStatus());
            }
            Document document = response.getContentAsHTML();

            row.name = Optional
                    .ofNullable(document.selectFirst("h1"))
                    .map(Element::text)
                    .orElseThrow(() -> new Exception("not found"));

            query.logger().info(row.name);
            row.brand = Optional
                    .of(document.select(".brand img"))
                    .map(elem -> elem.attr("title"))
                    .map(text -> BRAND_SLASH.split(text)[0])// На сайте бренд указан так: KNAUF / КНАУФ, желательно брать только одно название.
                    .map(String::trim)
                    .orElseThrow(() -> new Exception("Brand not found."));
            query.logger().info(row.brand);

            for (Element element : document.select(".item_slider a")) {
                query.logger().info(element.attr("href"));
                row.images.add(urlBuilder.resolve(element.attr("href")).toURL());// В объект помещаем полный URL картинки, тут снова обращаемся к URLBuilder.
            }

            // Также собираем все характеристики товара (название и значение)
            for (Element element : document.select(".props_list:first-child .char-list__item")) {

                String title = Optional
                        .ofNullable(element.selectFirst(".char_name span"))
                        .map(Element::text)
                        .map(String::trim)
                        .orElseThrow(() -> new Exception("Property title not found."));

                String value = Optional
                        .ofNullable(element.selectFirst(".char_value span"))
                        .map(Element::text)
                        .map(String::trim)
                        .orElseThrow(() -> new Exception("Property value not found."));
                query.logger().info("title: " + title + "; value: " + value);

                if (title.isEmpty()) continue;

                row.properties.add(new ProductResult.Property(title, value));
            }

            // Когда все данные собрали, записываем наш объект в байтовый буфер
            row.write(outputStream);

            // И после наш байтовый буфер сохраняем как результат парсинга.

        }
    }
}
