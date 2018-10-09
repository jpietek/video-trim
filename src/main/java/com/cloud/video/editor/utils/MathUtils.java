package com.cloud.video.editor.utils;

public class MathUtils {

	public static int nearestDivisibleByTwo(double num) {
		
		if(Math.ceil(num) % 2 == 0) {
			return (int) Math.ceil(num); 
		} else {
			if(Math.floor(num) % 2 == 0) {
				return (int) Math.floor(num);
			}
			
			return (int) num + 1;
		}
	}
	
}
