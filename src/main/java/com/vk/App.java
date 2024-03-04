package com.vk;

import java.io.IOException;
import ru.quad69.myparser.api.parser.Parser;
import ru.quad69.myparser.api.parser.query.Logger;
import ru.quad69.myparser.api.parser.query.Query;
import ru.quad69.myparser.api.parser.query.ResultSet;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException {
        new DomMaleraProductInfo().parse();
    }
}
