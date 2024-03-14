package com.vk;

import ru.quad69.myparser.api.parser.Parser;
import ru.wbooster.myparser.ProductResult;

import javax.management.Query;
import java.util.Queue;
import java.util.regex.Pattern;

public class ProductListParser {
    private Pattern splitter;

    public ProductListParser(Pattern splitter) {
        this.splitter = splitter;
    }

    public ProductResult parse(Query query) {
        
    }
}
