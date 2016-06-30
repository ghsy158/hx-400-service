package fgh.test;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Hx400Provider {

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		new ClassPathXmlApplicationContext(
				new String[] { "spring-context.xml" }).start();
		System.in.read();
	}
}
