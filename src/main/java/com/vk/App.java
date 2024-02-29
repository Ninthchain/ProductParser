package com.vk;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException {
        ProductParser parser = new ProductParser("https://www.dommalera.ru/catalog/germetiki/dlya_shvov_1/germetik_akrilovyy_krass_belyy_300ml/");

        System.out.println(parser.getProductName());
        System.out.println(parser.getProductBrandName());
    }
}
