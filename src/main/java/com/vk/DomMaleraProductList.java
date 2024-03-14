package com.vk;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class DomMaleraProductList extends Parser {
    private static final Pattern SPLITTER = Pattern.compile("#");
    private static final Pattern NO_DIGIT = Pattern.compile("[^\\d.]");
    private static final Pattern COMMA = Pattern.compile(",");
    private static final Pattern MEASURE = Pattern.compile("Единица покупки:\\s*");

    private static final int PAGE_SIZE = 32;

    @Override
    public void parse(Query query) throws Exception {
        final String[] splitted = SPLITTER.split(query.content(), -2);
        final String productPath = splitted[0];
        final String branchCode = splitted[1];

        final ProductResult row = new ProductResult();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(512);
        final ArrayList<byte[]> results = new ArrayList<>(PAGE_SIZE);

        int currentPage = 1, totalPages = 1;
        URLBuilder builder = new URLBuilder("https", new String[]{branchCode, "dommalera.ru"}, productPath);

        builder.setPath(builder.getPath().toLowerCase());
        builder.setQuery("PAGEN_1", "Y");

        try (Proxy proxy = proxyProvider.acquire("dommalera.ru")) {
            do {
                Response response = proxy.request("GET", builder.toURL()).send();

                if (response.getStatus() != 200) {
                    throw new Exception("Invalid response: " + response.getStatus());
                }

                Document page = response.getContentAsHTML();
                Elements products = page.select(".catalog_block div.item_block");
                Element numerationElement = page.selectFirst(".nums > .cur");
                currentPage = numerationElement != null ? Integer.parseInt(numerationElement.text()) : 1;
                numerationElement = page.select(".nums > :last-child").last();
                totalPages = Integer.parseUnsignedInt(numerationElement.text());


                for (int i = 0, j = products.size(); i < j; i++) {
                    Element product = products.get(i);
                    Element codeElement = Objects.requireNonNull(product.selectFirst(".like_icons span"), "Code not found.");
                    Element pathElement = Objects.requireNonNull(product.selectFirst(".image_wrapper_block a"), "Path not found.");

                    String codeText = codeElement.attr("data-item");

                    row.code = Long.parseUnsignedLong(codeText);
                    row.path = pathElement.attr("href");

                    Element mainPriceElement = product.selectFirst(".price .price_value");
                    String mainPriceText = Objects.requireNonNull(mainPriceElement, "Main price not found.").text().trim();
                    mainPriceText = COMMA.matcher(mainPriceText).replaceFirst(".");
                    mainPriceText = NO_DIGIT.matcher(mainPriceText).replaceAll("");
                    if (mainPriceText.isEmpty()) continue;
                    row.mainPrice = Double.parseDouble(mainPriceText);


                    row.currency = Currency.RUB;

                    row.measure = Optional.ofNullable(product.selectFirst(".price_measure"))
                            .map((measure) -> MEASURE.matcher(measure.text()))
                            .map((matcher) -> matcher.replaceFirst(""))
                            .map(Measure::resolve)
                            .orElse(Measure.PCE);

                    row.available = product.selectFirst(".item-stock .value") != null;

                    row.ranking = currentPage * PAGE_SIZE - PAGE_SIZE + i + 1;
                    row.write(outputStream);

                    results.add(outputStream.toByteArray());

                    outputStream.reset();
                }

                query.setProgress(currentPage, totalPages);

                query.logger().info("Found " + products.size() + " entries on page " + currentPage + " of " + totalPages);

                results.clear();

                Element element = page.selectFirst(".flex-nav-next .flex-next");
                builder = element != null ? builder.resolve(element.attr("href")) : null;
            } while (builder != null);
        }
    }

}