package com.vk.maxidom;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.quad69.myparser.api.http.Response;
import ru.quad69.myparser.api.http.url.URLBuilder;
import ru.quad69.myparser.api.parser.Parser;
import ru.quad69.myparser.api.parser.query.Query;
import ru.quad69.myparser.api.proxy.Proxy;
import ru.wbooster.myparser.ProductResult;


import java.io.ByteArrayOutputStream;
import java.util.regex.Pattern;

public class MaxidomProductInfo extends Parser {
    private static final Pattern SPLITTER = Pattern.compile("#");

    // brand select - .flypage__product-essence-character a[href^="/brands/"]
    // characteristics key select - .flypage__product-essence-character-name
    // characteristics value select - .flypage__product-essence-character-data
    // images container class - .swiper-wrapper
    // flypage__product-essence-character
    @Override
    public void parse(Query query) throws Exception {
        final String[] split = SPLITTER.split(query.content(), -2);
        final String productPath = split[0];

        URLBuilder urlBuilder = new URLBuilder("https", "www.maxidom.ru");
        ProductResult row = new ProductResult();
        ByteArrayOutputStream os = new ByteArrayOutputStream();


        try (Proxy proxy = proxyProvider.acquire("maxidom.ru")) {
            Response response = proxy.request("GET", urlBuilder.toURL()).send();
            if (response.getStatus() != 200) {
                query.logger().error(response.getContentAsText());
            }

            Document productPage = response.getContentAsHTML();
            row.name = productPage.select("h1").text();

            query.logger().info(row.name);


            row.brand = productPage.selectFirst(".flypage__product-essence-character a[href^=\"/brands/\"]").text();
            query.logger().info(row.brand);

            for (Element image : productPage.select(".swiper-wrapper img")) {
                row.images.add(urlBuilder.resolve(image.attr("href")).toURL());
            }

            Elements productCharacteristics = productPage.select(".flypage__product-essence-character li");
            for (Element line : productCharacteristics) {
                String title = line.select(".flypage__product-essence-character-name").text();
                String value = line.select(".flypage__product-essence-character-data").text();
                row.properties.add(new ProductResult.Property(title, value));
            }
            query.setProgress(1);
            row.write(os);
            query.results().put(os.toByteArray());
        }
    }
}
