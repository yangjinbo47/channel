package com.tenfen.www.util;

import java.util.UUID;

public class Uuid {
	public static void main(String[] args) {
		String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
		System.out.println(uuid);
	}
}
