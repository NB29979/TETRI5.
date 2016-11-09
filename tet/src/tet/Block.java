package tet;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public class Block extends Rectangle {

	//ブロック種類
	public static final int BLANK = 0;
	public static final int typeI = 1;
	public static final int typeO = 2;
	public static final int typeS = 3;
	public static final int typeZ = 4;
	public static final int typeJ = 5;
	public static final int typeL = 6;
	public static final int typeT = 7;

	private int blockState;
	private Color color;


	Block(int x,int y,int width,int height,int state,Color color){
		setRect(x,y,width,height);
		setBlockState(state);
		this.color=color;
	}

	public int getBlockState(){
		return blockState;
	}

	public void setColor(Color color){
		this.color=color;
	}

	public void setBlockState(int state){
		blockState=state;
	}

	public void drawBlock(Graphics g){
		g.setColor(color);
		g.fillRect(x,y,width,height);
	}
}