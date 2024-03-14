package com.vk.maxidom;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.quad69.myparser.api.http.Request;
import ru.quad69.myparser.api.http.Response;
import ru.quad69.myparser.api.http.url.URLBuilder;
import ru.quad69.myparser.api.parser.Parser;
import ru.quad69.myparser.api.parser.query.Query;
import ru.quad69.myparser.api.proxy.Proxy;
import ru.wbooster.myparser.Currency;
import ru.wbooster.myparser.Measure;
import ru.wbooster.myparser.ProductResult;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Pattern;

public class MaxidomProductList extends Parser {
    private static final Pattern SPLITTER = Pattern.compile("#");
    private static final Pattern MEASURE = Pattern.compile("Единица покупки:\\s*");

    @Override
    public void parse(Query query) throws Exception {

        final String[] splitted = SPLITTER.split(query.content(), -2);
        final String productPath = splitted[0];
        final String locId = splitted[1];
        final ProductResult row = new ProductResult();

        URLBuilder builder = new URLBuilder("https", "www.maxidom.ru", productPath);
        ByteArrayOutputStream os = new ByteArrayOutputStream();


        try (Proxy proxy = proxyProvider.acquire("maxidom.ru")) {

            Request request = proxy.request("GET", builder.toURL());
            request.setCookie("MAXI_LOC_ID", locId);

            Response response = request.send();

            if (response.getStatus() != 200) {
                throw new Exception();
            }

            Document document = response.getContentAsHTML();
            ArrayList<byte[]> results = null;
            do {
                Elements products = document.select(".b-catalog-list");
                results = new ArrayList<>(products.size());

                for (Element product : products) {
                    Elements description = product.select(".small-top b-catalog-list-product__big");

                    Element image = product.selectFirst(".b-catalog-list-product__section1 a");
                    Element price = product.selectFirst(".price span");
                    Element instock = product.selectFirst(".instock");

                    row.mainPrice = Double.valueOf(price.text());
                    row.code = Long.valueOf(description.get(1).text());
                    row.brand = description.get(2).text();

                    row.path = image.attr("href");
                    row.currency = Currency.RUB;


                    row.measure = Optional.ofNullable(product.selectFirst(".measure"))
                            .map(measure -> MEASURE.matcher(measure.text()))
                            .map(matcher -> matcher.replaceFirst(""))
                            .map(Measure::resolve)
                            .orElse(Measure.PCE);

                    row.available = instock != null;

                    row.write(os);
                    results.add(os.toByteArray());
                }
                Element navigationNext = document.getElementById("navigation_2_next_page");
                if (navigationNext == null) {
                    builder = null;
                } else {
                    builder = builder.resolve(navigationNext.attr("href"));
                }
            } while (builder != null);
        }
    }
}
