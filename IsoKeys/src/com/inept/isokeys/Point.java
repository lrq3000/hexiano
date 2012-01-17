package com.inept.isokeys;

public class Point {

	public int x;
	public int y;
	
	public Point(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	public String toString()
	{
		return "(" + x + ", " + y + ")";
	}
}
