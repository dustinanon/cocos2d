package org.cocos2d.utils;

import java.util.Random;

public class GameRand extends Random {
	private static final long serialVersionUID = 1837782000554537751L;

	private static int high = 0xDEADBEEF;
	private static int low = 0x49616E42;
	
	public GameRand() {
		high = high & (int) System.currentTimeMillis();
	}
	
	@Override
	protected int next(int bits) {
		high = ((high << 16) + (high >> 16)) + low;
		low += high;
		
		return high >>> (32 - bits);
	}
}
