package com.vk;

import java.io.IOException;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.sql.Time;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;
import java.util.random.RandomGeneratorFactory;

import ru.quad69.myparser.api.parser.Parser;
import ru.quad69.myparser.api.parser.query.Logger;
import ru.quad69.myparser.api.parser.query.Query;
import ru.quad69.myparser.api.parser.query.ResultSet;

/**
 * Hello world!
 */
public class App {
    public static int squareDigits(int n) {
        String numberStr = String.valueOf(n);
        String result = "";
        for (char c : numberStr.toCharArray()) {
            result += Math.pow(Character.getNumericValue(c), 2);
        }
        return Integer.parseInt(result);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println(squareDigits(9119));
    }
}
