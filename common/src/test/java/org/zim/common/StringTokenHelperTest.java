package org.zim.common;

import org.junit.Test;

import java.util.StringTokenizer;

public class StringTokenHelperTest {


    @Test
    public void test1() {
        StringTokenHelper tokenHelper = new StringTokenHelper("listu hello world");
        System.out.println(tokenHelper.next());
        System.out.println(tokenHelper.remaining());
        System.out.println("--");

        tokenHelper = new StringTokenHelper("to zx hello world");
        System.out.println(tokenHelper.next());
        System.out.println(tokenHelper.next());
        System.out.println(tokenHelper.remaining());
        System.out.println("--");

        tokenHelper = new StringTokenHelper("  to  zx   hello  world ");
        System.out.println(tokenHelper.hasNext());
        System.out.println(tokenHelper.next());
        System.out.println(tokenHelper.hasNext());
        System.out.println(tokenHelper.next());
        System.out.println(tokenHelper.hasNext());
        System.out.println(tokenHelper.remaining());
        System.out.println("--");

        StringTokenizer tokenizer = new StringTokenizer("  to  zx   hello  world ", " ");
        System.out.println(tokenizer.nextToken());
        System.out.println(tokenizer.nextToken());
        System.out.println(tokenizer.nextToken());
        System.out.println(tokenizer.nextToken());
    }

    @Test
    public void test2() {
        StringTokenHelper tokenHelper = new StringTokenHelper("  a  ");
        System.out.println(tokenHelper.hasNext());
        System.out.println(tokenHelper.next());
        System.out.println(tokenHelper.hasNext());
        System.out.println(tokenHelper.next());
        System.out.println(tokenHelper.remaining());
    }
}
