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
    public static boolean check(String sentence){
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        sentence = sentence.toLowerCase();
        int matches = 0;
        for(char ch : sentence.toCharArray()) {
            String pattern = String.format("%c", ch);
            if (alphabet.contains(pattern)) {
               alphabet = alphabet.replace(pattern, "");
               ++matches;
            }
        }
        return matches == 26;
    }
    public static void main( String[] args ) throws IOException {
        System.out.println(check("The quick brown fox jumps over the lazy dog."));
    }
}
