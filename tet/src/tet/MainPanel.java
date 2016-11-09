package tet;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JPanel;


public class MainPanel extends JPanel implements Runnable, KeyListener{

	enum State{
		TITLESCREEN,
		STARTSCREEN,
		CREDITSSCREEN,
		HOWTOSCREEN,
		INITSCREEN,
		PAUSESCREEN,
		QYNSCREEN,
		PLAYSCREEN,
		RESULTSCREEN,
	}
	final static int WIDTH = 416;
	final static int HEIGHT = 578;
	final static int NANO=1000000000;

	// ダブルバッファリング（db）用
	private Graphics dbg;
	private Image dbImage = null;
	//
	private Thread thread;

	private Block[][] field;
	private int[][] IFIELD;

	private Block[][] nfield;
	private int [][] NXTFIELD;

	private Block[][] blockfield;
	private int [][] NXTBLOCK;

	private int [] nxtblock;

	private SoundEffect moveSound;
	private SoundEffect pushSound;
	private SoundEffect hitSound;
	private SoundEffect expSound;
	private SoundEffect BGM;

	private Score score;

	private int[] rank;

	private String readme[];

	long g_start,g_end,w_start,w_end,t_start,s_start,s_end;
	int BY=2,BX=4;
	int B_type;
	int CNT=0;
	boolean BC_flg=true,B_flg=true,	check_flg,BC_check=false,fst_flg=true,sort_flg=true;
	boolean trans=true,game_flg=false;
	double b_s;
	int RSM=0;
	int SLT=1;
	int ST=0;
	int tmpscore;

	private State gstate;

	MainPanel(){
			setSize(WIDTH,HEIGHT);

			field = new Block[22][10];
			IFIELD = new int [22][10];

			nfield = new Block [32][23];
			NXTFIELD = new int [32][23];

			blockfield = new Block [14][6];
			NXTBLOCK = new int [14][6];

			nxtblock = new int [3];

			rank=new int[6];
			readme=new String[38];

			gstate=State.TITLESCREEN;

			score=new Score();

			moveSound = new SoundEffect("data/Move.wav");
			pushSound = new SoundEffect("data/pause.wav");
			hitSound = new SoundEffect("data/hit.wav");
			expSound = new SoundEffect("data/Explosion.wav");
			BGM = new SoundEffect("data/hometown.wav");

			t_start=System.nanoTime();

			thread = new Thread(this);
			thread.start();

			setFocusable(true);
			addKeyListener(this);
	}

	private void paintScreen(){
		try {
				// グラフィックオブジェクトを取得
				Graphics g = getGraphics();
				if ((g != null) && (dbImage != null)) {
					// バッファイメージを画面に描画
					g.drawImage(dbImage, 0, 0, null);
				}
				Toolkit.getDefaultToolkit().sync();
				if (g != null) {
					// グラフィックオブジェクトを破棄
					g.dispose();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	private void gameRender(){
		// 初回の呼び出し時にダブルバッファリング用オブジェクトを作成
		if (dbImage == null) {
			// バッファイメージ
			dbImage = createImage(WIDTH, HEIGHT);

			if (dbImage == null) {
				return;
			} else {
				// バッファイメージの描画オブジェクト
				dbg = dbImage.getGraphics();
			}
		}
		dbg.setColor(Color.BLACK);
		dbg.fillRect(0, 0, WIDTH, HEIGHT);

		if(gstate==State.PAUSESCREEN||gstate==State.QYNSCREEN||gstate==State.PLAYSCREEN){
			for(int y=0;y<field.length;y++)
				for(int x=0;x<field[y].length;x++)
					if(field[y][x].getBlockState()!=Block.BLANK)
						field[y][x].drawBlock(dbg);

			for(int y=0;y<nfield.length;y++)
				for(int x=0;x<nfield[y].length;x++)
					if(nfield[y][x].getBlockState()!=Block.BLANK)
						nfield[y][x].drawBlock(dbg);

			for(int y=0;y<blockfield.length;y++)
				for(int x=0;x<blockfield[y].length;x++)
					if(blockfield[y][x].getBlockState()!=Block.BLANK)
						blockfield[y][x].drawBlock(dbg);

			if(score.getScore()>=rank[0]){
				if(trans)
					rank[5]=rank[0];
				rank[0]=score.getScore();
				trans=false;
			}
			else
				rank[5]=score.getScore();

			dbg.setFont(new Font(null, Font.ITALIC,25));
			dbg.setColor(Color.BLACK);
			dbg.drawString("NEXT:", 309, 60);
			dbg.drawString("Score:", 299, 421);
			dbg.drawString(Integer.toString(score.getScore()), 342,451);
			dbg.drawString("Hi-Score:", 299, 361);
			dbg.drawString(Integer.toString(rank[0]), 342,391);
			dbg.setColor(Color.WHITE);
			dbg.drawString("NEXT:", 308, 59);
			dbg.drawString("Score:", 298, 420);
			dbg.drawString(Integer.toString(score.getScore()), 341,450);
			if(score.getScore()>=rank[0])
				dbg.setColor(Color.YELLOW);
			else
				dbg.setColor(Color.WHITE);
			dbg.drawString("Hi-Score:", 298, 360);
			dbg.drawString(Integer.toString(rank[0]), 341,390);

			if(gstate==State.PAUSESCREEN||gstate==State.QYNSCREEN){
				int lry=0,lrx=0,lqy=0,lqx=0,frsize=0,fqsize=0;

				dbg.setFont(new Font(null, Font.ITALIC,25));
				dbg.setColor(Color.BLACK);
				dbg.drawString("==PAUSE==", WIDTH/2-73+1,HEIGHT/2-100);
				dbg.setColor(Color.WHITE);
				dbg.drawString("==PAUSE==", WIDTH/2-73,HEIGHT/2-100);

				switch(RSM){
				case 0:
					lry=HEIGHT/2-45;lrx=WIDTH/2-41;
					lqy=HEIGHT/2;lqx=WIDTH/2-24;
					frsize=25;fqsize=20;
					dbg.setColor(Color.BLACK);
					dbg.drawString("-             -", lrx-13+1,lry+1);
					dbg.setColor(Color.WHITE);
					dbg.drawString("-             -", lrx-13,lry);
					break;
				case 1:
					lry=HEIGHT/2-45;lrx=WIDTH/2-37;
					lqy=HEIGHT/2;lqx=WIDTH/2-28;
					frsize=20;fqsize=25;
					dbg.setColor(Color.BLACK);
					dbg.drawString("-       -", lqx-13+1,lqy+1);
					dbg.setColor(Color.WHITE);
					dbg.drawString("-       -", lqx-13,lqy);
					break;
					default:
						break;
				}
				dbg.setFont(new Font(null, Font.ITALIC,frsize));
				dbg.setColor(Color.BLACK);
				dbg.drawString("resume", lrx+1,lry+1);
				dbg.setColor(Color.WHITE);
				dbg.drawString("resume", lrx,lry);
				dbg.setFont(new Font(null, Font.ITALIC,fqsize));
				dbg.setColor(Color.BLACK);
				dbg.drawString("quit", lqx+1,lqy+1);
				dbg.setColor(Color.WHITE);
				dbg.drawString("quit", lqx,lqy);

				if(gstate==State.QYNSCREEN){
					dbg.setFont(new Font(null, Font.ITALIC,25));
					dbg.setColor(Color.BLACK);
					dbg.drawString("Really?", 250+1,328+1);
					dbg.setColor(Color.WHITE);
					dbg.drawString("Really?", 250,328);

					int fysize=0,fnsize=0;
					int lyy=0,lyx=0,lny=0,lnx=0;
					switch(SLT){
						case 0:
							lyy=368;lyx=248;
							lny=398;lnx=250;
							fysize=25;fnsize=20;
							dbg.setColor(Color.BLACK);
							dbg.drawString("-        -", lyx-11+1,lyy+1);
							dbg.setColor(Color.WHITE);
							dbg.drawString("-        -", lyx-11,lyy);
							break;
						case 1:
							lyy=368;lyx=250;
							lny=398;lnx=248;
							fysize=20;fnsize=25;
							dbg.setColor(Color.BLACK);
							dbg.drawString("-      -", lnx-11+1,lny+1);
							dbg.setColor(Color.WHITE);
							dbg.drawString("-      -", lnx-11,lny);
							break;
						default:
							break;
					}
					dbg.setFont(new Font(null, Font.ITALIC,fysize));
					dbg.setColor(Color.BLACK);
					dbg.drawString("Yes", lyx+1,lyy+1);
					dbg.setColor(Color.WHITE);
					dbg.drawString("Yes", lyx,lyy);
					dbg.setFont(new Font(null, Font.ITALIC,fnsize));
					dbg.setColor(Color.BLACK);
					dbg.drawString("No", lnx+1,lny+1);
					dbg.setColor(Color.WHITE);
					dbg.drawString("No", lnx,lny);
				}
			}

		}
		else if(gstate==State.TITLESCREEN||gstate==State.STARTSCREEN){
			BGM.stop();
			dbg.setFont(new Font(null, Font.BOLD,25));
			dbg.setColor(Color.BLACK);
			dbg.fillRect(0,0,WIDTH,HEIGHT);
			dbg.setColor(Color.WHITE);
			dbg.drawString("TETRI5.", 45, HEIGHT/2-50);

			long t_end;

			if(gstate==State.TITLESCREEN){
				t_end=System.nanoTime();
				if(t_end-t_start>0.8*NANO&&t_end-t_start<=1.0*NANO){}
				else if(t_end-t_start>1.0*NANO)
					t_start=System.nanoTime();
				else{
					dbg.setFont(new Font(null, Font.ITALIC,15));
					dbg.setColor(Color.WHITE);
					dbg.drawString("Press Z key", 170, 450);
				}
			}
			else if(gstate==State.STARTSCREEN){
				int fspsize,fshsize,fsrsize,fscsize,fsqsize;
				int lspy,lspx,lshy,lshx,lsry,lsrx,lscy,lscx,lsqy,lsqx;

				lspy=358;lspx=299;
				lshy=lspy+40;lsrx=282;
				lsry=lshy+40;lshx=266;
				lscy=lsry+40;lscx=287;
				lsqy=lscy+40;lsqx=295;
				fspsize=fshsize=fsrsize=fscsize=fsqsize=20;

				switch(ST){
					case 0:
						lspx=290;
						fspsize=25;
						dbg.setColor(Color.WHITE);
						dbg.drawString("-        -", lspx-11,lspy);
						break;
					case 1:
						lshx=257;
						fshsize=25;
						dbg.setColor(Color.WHITE);
						dbg.drawString("-                    -", lshx-11,lshy);
						break;
					case 2:
						lsrx=277;
						fsrsize=25;
						dbg.setColor(Color.WHITE);
						dbg.drawString("-              -", lsrx-11,lsry);
						break;
					case 3:
						lscx=284;
						fscsize=25;
						dbg.setColor(Color.WHITE);
						dbg.drawString("-            -", lscx-11,lscy);
						break;
					case 4:
						lsqx=294;
						fsqsize=25;
						dbg.setColor(Color.WHITE);
						dbg.drawString("-        -", lsqx-12,lsqy);
						break;
					default:
						break;
				}
				dbg.setColor(Color.WHITE);
				dbg.setFont(new Font(null, Font.ITALIC,fspsize));
				dbg.drawString("Play", lspx,lspy);
				dbg.setFont(new Font(null, Font.ITALIC,fshsize));
				dbg.drawString("How to Play", lshx,lshy);
				dbg.setFont(new Font(null, Font.ITALIC,fsrsize));
				dbg.drawString("Ranking", lsrx,lsry);
				dbg.setFont(new Font(null, Font.ITALIC,fscsize));
				dbg.drawString("Credits", lscx,lscy);
				dbg.setFont(new Font(null, Font.ITALIC,fsqsize));
				dbg.drawString("Quit", lsqx,lsqy);
			}
		}
		else if(gstate==State.CREDITSSCREEN){
			dbg.setColor(Color.WHITE);
			dbg.setFont(new Font(null,Font.BOLD,25));
			dbg.drawString("TETRI5.", 50, 100);
			dbg.setFont(new Font(null,Font.BOLD,20));
			dbg.drawString("Program: ", 50, 200);
			dbg.drawString("Sound: ", 50, 300);
			dbg.drawString("BGM: ", 50, 400);
			dbg.setFont(new Font(null,Font.ITALIC,20));
			dbg.drawString("newbie29", 150, 200);
			dbg.drawString("newbie29", 150, 300);
			dbg.drawString("SHW Free music", 150, 400);
			dbg.drawString("http://shw.in", 150, 425);
		}
		else if(gstate==State.HOWTOSCREEN){
			dbg.setFont(new Font(null, Font.ITALIC,25));
			dbg.setColor(Color.WHITE);
			dbg.drawString("How to Play:", 45, 80);
			dbg.setFont(new Font(null, Font.BOLD,15));
			for(int i=0;i<38;i++){
				dbg.drawString(readme[i],20,120+i*10);
			}
		}
		else if(gstate==State.RESULTSCREEN){
			dbg.setColor(Color.BLACK);
			dbg.fillRect(0,0,WIDTH,HEIGHT);

			dbg.setFont(new Font(null, Font.ITALIC,25));
			dbg.setColor(Color.WHITE);
			dbg.drawString("Ranking:", 45, 80);
			for(int i=0;i<5;i++){
				if(game_flg){
					if(tmpscore==rank[i]){
						dbg.setColor(Color.WHITE);
						dbg.drawString("Your score: "+tmpscore, 45,490);
						dbg.setColor(Color.YELLOW);
					}
					else
						dbg.setColor(Color.WHITE);
				}
				dbg.drawString((i+1)+": "+Integer.toString(rank[i]), 155,150+i*50);
			}
		}
	}

	private void gameUpdate(){
		switch(gstate){
		case TITLESCREEN:
			lordHiScore();
			break;
		case STARTSCREEN:
			game_flg=false;
			break;
		case CREDITSSCREEN:
			break;
		case HOWTOSCREEN:
			lordReadme();
			break;
		case INITSCREEN:
			initGame();
			break;
		case PAUSESCREEN:
			pauseGame();
			break;
		case QYNSCREEN:
			break;
		case PLAYSCREEN:
			playGame();
			break;
		case RESULTSCREEN:
			resultGame();
			break;
		default:
			break;
		}
	}

	private void initGame(){
		for(int i=0;i<22;i++)
			for(int j=0;j<10;j++)
				IFIELD[i][j]=0;

		for(int i=0;i<32;i++)
			for(int j=0;j<23;j++){
				if((i>0&&j>0&&j<16&&i<31)||(i>3&&j>16&&j<22&&i<14))	NXTFIELD[i][j]=0;
				else	NXTFIELD[i][j]=10;
			}

		for(int i=0;i<14;i++)
			for(int j=0;j<6;j++)
				NXTBLOCK[i][j]=0;

		for(int i=0;i<3;i++)
			nxtblock[i]=0;

		setField(field,IFIELD,0);
		setField(nfield,NXTFIELD,1);
		setField(blockfield,NXTBLOCK,2);
		s_start=g_start=System.nanoTime();
		BY=2;
		BX=4;
		B_type=0;
		BC_flg=true;B_flg=true;check_flg=true;BC_check=false;fst_flg=true;
		CNT=0;
		RSM=0;
		SLT=1;
		tmpscore=0;
		score.resetScore();
		sort_flg=true;
		trans=true;
		game_flg=true;
		BGM.loop();
		gstate=State.PLAYSCREEN;
	}
	private void pauseGame(){
		int tmpby,tmpbx;
		tmpby=BY;tmpbx=BX;
		BY=tmpby;BX=tmpbx;
	}
	private void playGame(){

		long gover_end=0,gover_start;
		if(CNT>19){
			tmpscore=score.getScore();
			gover_start=System.nanoTime();
			while(gover_end-gover_start<0.7*NANO){
				gover_end=System.nanoTime();
			}
				gstate=State.RESULTSCREEN;
		}

		CNT=0;

		setField(field,IFIELD,0);
		setField(nfield,NXTFIELD,1);
		setField(blockfield,NXTBLOCK,2);

		//ここでゲーム処理の記述

		s_end=g_end=System.nanoTime();
		if(s_end-s_start>=0.5*NANO){
			score.addScore(100);
			s_start=System.nanoTime();
		}
		if((g_end-g_start>=0.105*NANO)&&(BY<21)){

			//一列消去処理
			if(BC_check){
				int scr_cnt=0;
				for(int i=2;i<22;i++){
					check_flg=true;
					for(int j=0;j<10;j++)
						if(IFIELD[i][j]==0){
							check_flg=false;
							break;
						}
					if(check_flg){
						for(int j=0;j<10;j++)
							IFIELD[i][j]=0;
						for(int k=i-1;k>0;k--){
							for(int j=0;j<10;j++)
								IFIELD[k+1][j]=IFIELD[k][j];
						}
						expSound.play();
						scr_cnt++;
					}
				}
				BC_check=false;
				score.addScore(scr_cnt*1000);
			}

			//残りどれくらい余裕が有るか?
			boolean cnt_flg=true;
			for(int i=0;i<IFIELD.length;i++){
				for(int j=0;j<IFIELD[i].length;j++)
					if(cnt_flg&&IFIELD[i][j]>10){
						CNT++;
						cnt_flg=false;
					}
				cnt_flg=true;
			}

			//前回の位置を一度消去
			for(int i=0;i<IFIELD.length;i++)
				for(int j=0;j<IFIELD[i].length;j++)
					if(IFIELD[i][j]!=0&&IFIELD[i][j]<10)IFIELD[i][j]=0;

			BY++;

			if(B_flg&&BC_check==false){
				int dx=1,dy=2;
				if(fst_flg){
					for(int i=0;i<2;i++)
						nxtblock[i]=(int)(Math.random()*8+1);
					fst_flg=false;
					b_s=(int)(Math.random()*8+1);
				}
				else if(fst_flg==false){
					for(int i=0;i<NXTBLOCK.length;i++){
						for(int j=0;j<NXTBLOCK[i].length;j++)
							NXTBLOCK[i][j]=0;
						}
					b_s=nxtblock[0];
					for(int i=0;i<2;i++)
						nxtblock[i]=nxtblock[i+1];
				}

				nxtblock[2]=(int)(Math.random()*8+1);

				if((int)b_s==1)
					BX=3;
				B_flg=false;

				//nextblockの描画
				for(int i=0;i<3;i++){
					if(nxtblock[i]==2)
						dx=2;
					else
						dx=1;
					drawfield(dx,dy,nxtblock[i],NXTBLOCK,0,nxtblock[i]);
					dy+=4;
				}

			}

			drawfield(BX,BY,(int)b_s,IFIELD,B_type,(int)b_s);

			//予測箇所処理
			boolean tflg=false;
			for(int j=0;j<20;j++){
				for(int i=BX;i<BX+4;i++){
					if((BY+j<21&&i>-1&&i<10&&(((IFIELD[BY][i]!=0&&IFIELD[BY][i]<10)&&IFIELD[BY+1+j][i]>10)||
							((IFIELD[BY-1][i]!=0&&IFIELD[BY-1][i]<10)&&IFIELD[BY+j][i]>10)||
							((IFIELD[BY-2][i]!=0&&IFIELD[BY-2][i]<10)&&IFIELD[BY-1+j][i]>10)))||

							(BY+j==21&&i>-1&&i<10&&(((IFIELD[BY][i]!=0&&IFIELD[BY][i]<10)&&IFIELD[21][i]==0)||
							((IFIELD[BY-1][i]!=0&&IFIELD[BY-1][i]<10)&&IFIELD[21][i]==0)||
							((IFIELD[BY-2][i]!=0&&IFIELD[BY-2][i]<10)&&IFIELD[21][i]==0)))
							){
						drawfield(BX,BY+j,(int)b_s,IFIELD,B_type,9);
						tflg=true;
						break;
					}
				}
				if(tflg)
					break;
			}

			drawfield(BX,BY,(int)b_s,IFIELD,B_type,(int)b_s);

			g_start=System.nanoTime();

			if(BY<21){
				for(int i=BX;i<BX+4;i++)
					if(i>-1&&i<10&&(((IFIELD[BY][i]!=0&&IFIELD[BY][i]<10&&IFIELD[BY][i]!=9)&&IFIELD[BY+1][i]>10)||
							((IFIELD[BY-1][i]!=0&&IFIELD[BY-1][i]<10&&IFIELD[BY-1][i]!=9)&&IFIELD[BY][i]>10)||
							((IFIELD[BY-2][i]!=0&&IFIELD[BY-2][i]<10&&IFIELD[BY-2][i]!=9)&&IFIELD[BY-1][i]>10))
							){
						hitSound.play();
							//到達処理
							for(int k=BY;k>BY-4;k--)
								for(int j=BX;j<BX+4;j++)
										if(j<10&&j>-1&&IFIELD[k][j]!=0&&IFIELD[k][j]<10&&IFIELD[i][j]!=9)IFIELD[k][j]+=10;
							B_flg=true;
							B_type=0;
							BC_check=true;
							BY=2;
							BX=4;

					}
			}
			else if(BY==21){
				hitSound.play();
				//到達処理
				for(int i=BY;i>BY-4;i--)
					for(int j=BX;j<BX+4;j++)
							if(j<10&&j>-1&&IFIELD[i][j]!=0&&IFIELD[i][j]<10&&IFIELD[i][j]!=9)IFIELD[i][j]+=10;
				B_flg=true;
				B_type=0;
				BC_check=true;
				BY=2;
				BX=4;
			}
		}


	}

	@Override
	public void run(){
		double elapsedTime = 0;

		while (true) {
			long start = System.nanoTime();
			if(elapsedTime>16.6667 * 1000000){
				// ゲーム状態を更新
				gameUpdate();
				// バッファにレンダリング（ダブルバッファリング）
				gameRender();
				// バッファを画面に描画（repaint()を自分でする！）
				paintScreen();
				elapsedTime=0;
			}
			long end = System.nanoTime();
			elapsedTime += (end-start);
		}
	}

	public void keyTyped(KeyEvent e) {}
	public void keyPressed(KeyEvent e) {
		int key=e.getKeyCode();
		if(key==KeyEvent.VK_DOWN||key==KeyEvent.VK_S){
			if(ST<4&&gstate==State.STARTSCREEN){
				moveSound.play();
				ST++;
			}
			else if(RSM<1&&gstate==State.PAUSESCREEN){
				moveSound.play();
				RSM++;
			}

			else if(SLT<1&&gstate==State.QYNSCREEN){
				moveSound.play();
				SLT++;
			}
		}
		if(key==KeyEvent.VK_UP||key==KeyEvent.VK_W){
			if(ST>0&&gstate==State.STARTSCREEN){
				moveSound.play();
				ST--;
			}
			else if(RSM>0&&gstate==State.PAUSESCREEN){
				moveSound.play();
				RSM--;
			}
			else if(SLT>0&&gstate==State.QYNSCREEN){
				moveSound.play();
				SLT--;
			}
		}
		if(key==KeyEvent.VK_Z&&gstate!=State.PLAYSCREEN){

			if(gstate==State.TITLESCREEN){
				pushSound.play();
				gstate=State.STARTSCREEN;
			}
			else if(gstate==State.STARTSCREEN){
				pushSound.play();
				switch(ST){
				case 0:
					gstate=State.INITSCREEN;
					break;
				case 1:
					gstate=State.HOWTOSCREEN;
					break;
				case 2:
					gstate=State.RESULTSCREEN;
					break;
				case 3:
					gstate=State.CREDITSSCREEN;
					break;
				case 4:
					System.exit(0);
					break;
				default:
					break;
				}
			}
			else if(gstate==State.PAUSESCREEN){
				pushSound.play();
				if(RSM==1)
					gstate=State.QYNSCREEN;
				else
					gstate=State.PLAYSCREEN;
			}
			else if(gstate==State.QYNSCREEN){
				pushSound.play();
				if(SLT==0){
					gstate=State.STARTSCREEN;
				}
				else
					gstate=State.PAUSESCREEN;
			}
		}
		if(key==KeyEvent.VK_X){
			if(gstate==State.CREDITSSCREEN){
				pushSound.play();
				gstate=State.STARTSCREEN;
			}
			else if(gstate==State.HOWTOSCREEN){
				pushSound.play();
				gstate=State.STARTSCREEN;
			}
			else if(gstate==State.PLAYSCREEN){
				pushSound.play();
				gstate=State.PAUSESCREEN;
			}
			else if(gstate==State.RESULTSCREEN){
				pushSound.play();
				gstate=State.STARTSCREEN;
			}
			else if(ST!=4&&gstate==State.STARTSCREEN){
				pushSound.play();
				ST=4;
			}
			else if(RSM!=0&&gstate==State.PAUSESCREEN){
				pushSound.play();
				RSM=0;
			}
			else if(SLT!=1&&gstate==State.QYNSCREEN){
				pushSound.play();
				SLT=1;
			}
		}
		if(gstate==State.PLAYSCREEN){
			if(BC_check==false){

				//以降改善点必要
				for(int i=BY;i>BY-4;i--)
					for(int j=BX;j<BX+4;j++)
						if(j<10&&j>-1)
							if(IFIELD[i][j]!=0&&IFIELD[i][j]<10)IFIELD[i][j]=0;

				//右操作
				if((key==KeyEvent.VK_D||key==KeyEvent.VK_RIGHT)&&BY<=21){
					moveSound.play();
					int cB_type=0;
					if((int)b_s==1||(int)b_s==3||(int)b_s==4||(int)b_s==8)	cB_type=B_type%2;
					else if((int)b_s==2)	cB_type=B_type%1;
					else if((int)b_s==5||(int)b_s==6||(int)b_s==7)	cB_type=B_type%4;

					switch(cB_type){//現在テスト中
						case 0:
							if(((int)b_s==1&&BX<6&&IFIELD[BY][BX+4]<10)||//―

								((int)b_s==2&&BX<8&&IFIELD[BY][BX+2]<10&&IFIELD[BY-1][BX+2]<10)||//■

								((int)b_s==3&&BX<7&&IFIELD[BY][BX+2]<10&&IFIELD[BY-1][BX+3]<10)||

								((int)b_s==4&&BX<7&&IFIELD[BY][BX+3]<10&&IFIELD[BY+1][BX+2]<10)||

								((int)b_s==5&&BX<7&&IFIELD[BY-1][BX+1]<10&&IFIELD[BY][BX+3]<10)||

								((int)b_s==6&&BX<7&&IFIELD[BY][BX+3]<10&&IFIELD[BY-1][BX+3]<10)||

								((int)b_s==7&&BX<7&&IFIELD[BY][BX+3]<10&&IFIELD[BY-1][BX+2]<10)||

								((int)b_s==8&&BX<7&&IFIELD[BY][BX+3]<10&&IFIELD[BY-1][BX+2]<10)
								)
									BX++;
							break;
						case 1:
							if(((int)b_s==1&&BX<9&&IFIELD[BY][BX+1]<10&&IFIELD[BY-1][BX+1]<10&&IFIELD[BY-2][BX+1]<10&&IFIELD[BY-3][BX+1]<10)||//|

								((int)b_s==3&&BX<8&&IFIELD[BY][BX+2]<10&&IFIELD[BY-1][BX+2]<10&&IFIELD[BY-2][BX+1]<10)||

								((int)b_s==4&&BX<8&&IFIELD[BY][BX+1]<10&&IFIELD[BY-1][BX+2]<10&&IFIELD[BY-2][BX+2]<10)||

								(((int)b_s==5||(int)b_s==6||(int)b_s==7)&&BX<8&&IFIELD[BY][BX+2]<10&&IFIELD[BY-1][BX+2]<10&&IFIELD[BY-2][BX+2]<10)||

								((int)b_s==8&&BX<8&&IFIELD[BY][BX+2]<10&&IFIELD[BY-1][BX+1]<10&&IFIELD[BY-2][BX+2]<10)
								)
									BX++;
							break;
						case 2:
							if(((int)b_s==5&&BX<7&&IFIELD[BY][BX+3]<10&&IFIELD[BY-1][BX+3]<10)||

								((int)b_s==6&&BX<7&&IFIELD[BY][BX+1]<10&&IFIELD[BY-1][BX+3]<10)||

								((int)b_s==7&&BX<7&&IFIELD[BY][BX+2]<10&&IFIELD[BY-1][BX+3]<10)||

								((int)b_s==8&&BX<7&&IFIELD[BY][BX+2]<10&&IFIELD[BY-1][BX+3]<10)
									)
								BX++;
							break;
						case 3:
							if(((int)b_s==5&&BX<8&&IFIELD[BY][BX+1]<10&&IFIELD[BY-1][BX+1]<10&&IFIELD[BY-2][BX+2]<10)||

								((int)b_s==6&&BX<8&&IFIELD[BY][BX+2]<10&&IFIELD[BY-1][BX+1]<10&&IFIELD[BY-2][BX+1]<10)||

								((int)b_s==7&&BX<8&&IFIELD[BY][BX+1]<10&&IFIELD[BY-1][BX+2]<10&&IFIELD[BY-2][BX+1]<10)||

								((int)b_s==8&&BX<8&&IFIELD[BY][BX+1]<10&&IFIELD[BY-1][BX+2]<10&&IFIELD[BY-2][BX+1]<10)
									)
								BX++;
							break;
						default:
							break;
					}
				}
								//現在編集中

				//左操作
				if((key==KeyEvent.VK_A||key==KeyEvent.VK_LEFT)&&BY<=21){
					moveSound.play();
					int cB_type=0;
					if((int)b_s==1||(int)b_s==3||(int)b_s==4)	cB_type=B_type%2;
					else if((int)b_s==2)	cB_type=B_type%1;
					else if((int)b_s==5||(int)b_s==6||(int)b_s==7||(int)b_s==8)	cB_type=B_type%4;

					switch(cB_type){
						case 0:
							if(((int)b_s==1&&BX>0&&IFIELD[BY][BX-1]<10)||//―

								((int)b_s==2&&BX>0&&IFIELD[BY][BX-1]<10&&IFIELD[BY-1][BX-1]<10)||

								((int)b_s==3&&BX>0&&IFIELD[BY][BX-1]<10&&IFIELD[BY-1][BX]<10)||

								((int)b_s==4&&BX>0&&IFIELD[BY][BX]<10&&IFIELD[BY-1][BX-1]<10)||

								((int)b_s==5&&BX>0&&IFIELD[BY][BX-1]<10&&IFIELD[BY-1][BX-1]<10)||

								((int)b_s==6&&BX>0&&IFIELD[BY][BX-1]<10&&IFIELD[BY-1][BX+1]<10)||

								((int)b_s==7&&BX>0&&IFIELD[BY][BX-1]<10&&IFIELD[BY-1][BX]<10)||

								((int)b_s==8&&BX>0&&IFIELD[BY][BX-1]<10&&IFIELD[BY-1][BX]<10)
									)
									BX--;
							break;
						case 1:
							if(((int)b_s==1&&BX>0&&IFIELD[BY][BX-1]<10&&IFIELD[BY-1][BX-1]<10&&IFIELD[BY-2][BX-1]<10&&IFIELD[BY-3][BX-1]<10)||

								((int)b_s==3&&BX>0&&IFIELD[BY][BX]<10&&IFIELD[BY-1][BX-1]<10&&IFIELD[BY-2][BX-1]<10)||

								((int)b_s==4&&BX>0&&IFIELD[BY][BX-1]<10&&IFIELD[BY-1][BX-1]<10&&IFIELD[BY-2][BX]<10)||

								((int)b_s==5&&BX>0&&IFIELD[BY][BX-1]<10&&IFIELD[BY-1][BX]<10&&IFIELD[BY-2][BX]<10)||

								((int)b_s==6&&BX>0&&IFIELD[BY][BX]<10&&IFIELD[BY-1][BX]<10&&IFIELD[BY-2][BX-1]<10)||

								((int)b_s==7&&BX>0&&IFIELD[BY][BX]<10&&IFIELD[BY-1][BX-1]<10&&IFIELD[BY-2][BX]<10)||

								((int)b_s==8&&BX>0&&IFIELD[BY][BX]<10&&IFIELD[BY-1][BX-1]<10&&IFIELD[BY-2][BX]<10)
									)
								BX--;
							break;
						case 2:
							if(((int)b_s==5&&BX>0&&IFIELD[BY][BX+1]<10&&IFIELD[BY-1][BX-1]<10)||

								((int)b_s==6&&BX>0&&IFIELD[BY][BX-1]<10&&IFIELD[BY-1][BX-1]<10)||

								((int)b_s==7&&BX>0&&IFIELD[BY][BX]<10&&IFIELD[BY-1][BX-1]<10)||

								((int)b_s==8&&BX>0&&IFIELD[BY][BX]<10&&IFIELD[BY-1][BX-1]<10)
									)
								BX--;
							break;
						case 3:
							if((((int)b_s==5||(int)b_s==6||(int)b_s==7)&&BX>0&&IFIELD[BY][BX-1]<10&&IFIELD[BY-1][BX-1]<10&&IFIELD[BY-2][BX-1]<10)||

								((int)b_s==8&&BX>0&&IFIELD[BY][BX-1]<10&&IFIELD[BY-1][BX]<10&&IFIELD[BY-2][BX-1]<10)
									)
								BX--;
							break;
						default:
							break;
					}
				}
				if(key==KeyEvent.VK_R||key==KeyEvent.VK_T){
					boolean flg=false;
					if(BY!=21){
						pushSound.play();
						if((int)b_s==1&&BX>-1){
							if(B_type%2==0){
								flg=true;
								for(int i=BY;i>BY-4;i--)
									if(IFIELD[i][BX]>10){
										flg=false;
										break;
									}
							}
							else if(BX>-1&&B_type%2==1){
								flg=true;
								if(BX>6)
									BX=6;
								for(int i=BX;i<BX+4;i++)
									if(IFIELD[BY][i]>10){
										flg=false;
										break;
									}
							}
						}
						if(((int)b_s==3||(int)b_s==4)&&BX>-1){
							if(BX<8&&B_type%2==0){
								flg=true;
								switch((int)b_s){
									case 3:
										for(int i=BY;i>BY-2;i--)
											if(IFIELD[i-1][BX]>10&&IFIELD[i][BX+1]>10){
												flg=false;
												break;
											}
										break;
									case 4:
										for(int i=BY;i>BY-2;i--)
											if(IFIELD[i][BX]>10&&IFIELD[i-1][BX+1]>10){
												flg=false;
												break;
											}
										break;
									default:
										break;
								}
							}
							else if(BX<9&&B_type%2==1){
								flg=true;
								if(BX==8)
									BX=7;
								switch((int)b_s){
								case 3:
									for(int i=BX;i<BX+2;i++)
										if(IFIELD[BY][i]>10&&IFIELD[BY-1][i+1]>10){
											flg=false;
											break;
										}
									break;
								case 4:
									for(int i=BX;i<BX+2;i++)
										if(IFIELD[BY-1][i]>10&&IFIELD[BY][BX+1]>10){
											flg=false;
											break;
										}
									break;
								default:
									break;
								}
							}
						}
						if(((int)b_s==5||(int)b_s==6||(int)b_s==7)&&BX>-1){
							if(BX<8&&(B_type%4==0||B_type%4==2)){
								flg=true;
								switch((int)b_s){
									case 5:
										for(int i=BY;i>BY-3;i--)
											if((B_type%4==0&&IFIELD[i][BX+1]>10&&IFIELD[BY][BX]>10)||
												(B_type%4==2&&IFIELD[i][BX]>10&&IFIELD[BY-2][BX+1]>10)){
												flg=false;
												break;
											}
										break;
									case 6:
										for(int i=BY;i>BY-3;i--)
											if((B_type%4==0&&IFIELD[BY-2][BX]>10&&IFIELD[i][BX+1]>10)||
												(B_type%4==2&&IFIELD[i][BX]>10&&IFIELD[BY][BX+1]>10)){
												flg=false;
												break;
											}
										break;
									case 7:
										for(int i=BY;i>BY-3;i--)
											if((B_type%4==0&&IFIELD[BY-1][BX]>10&&IFIELD[i][BX+1]>10)||
												(B_type%4==2&&IFIELD[i][BX]>10&&IFIELD[BY-1][BX+1]>10)){
												flg=false;
												break;
											}
										break;
									default:
										break;
								}
							}
							else if(BX<9&&(B_type%4==1||B_type%4==3)){
								flg=true;
								if(BX==8)
									BX=7;
								switch((int)b_s){
									case 5:
										for(int i=BX;i<BX+3;i++)
											if((B_type%4==1&&IFIELD[BY-1][i]>10&&IFIELD[BY][BX+2]>10)||
												(B_type%4==3&&IFIELD[BY][i]>10&&IFIELD[BY-1][BX]>10)){
												flg=false;
												break;
											}
										break;
									case 6:
										for(int i=BX;i<BX+3;i++)
											if((B_type%4==1&&IFIELD[BY-1][i]>10&&IFIELD[BY][BX]>10)||
												(B_type%4==3&&IFIELD[BY][i]>10&&IFIELD[BY-1][BX+2]>10)){
												flg=false;
												break;
											}
										break;
									case 7:
										for(int i=BX;i<BX+3;i++)
											if((B_type%4==1&&IFIELD[BY-1][i]>10&&IFIELD[BY][BX+1]>10)||
												(B_type%4==3&&IFIELD[BY][i]>10&&IFIELD[BY-1][BX+1]>10)){
												flg=false;
												break;
											}
										break;
									default:
										break;
								}
							}
						}
						else if((int)b_s==8&&BX>-1){
							flg=true;
							if((B_type%4==0&&IFIELD[BY][BX+1]>10&&IFIELD[BY-1][BX]>10&&IFIELD[BY-2][BX+1]>10)||
								(B_type%4==2&&IFIELD[BY][BX]>10&&IFIELD[BY-1][BX+1]>10&&IFIELD[BY-2][BX]>10)
									){
								flg=false;
							}
							if(B_type%4==1||B_type%4==3){
								if(BX>7)
									BX=7;
								if((B_type%4==1&&IFIELD[BY-1][BX]>10&&IFIELD[BY][BX+1]>10&&IFIELD[BY-1][BX+2]>10)||
									(B_type%4==3&&IFIELD[BY][BX]>10&&IFIELD[BY-1][BX+1]>10&&IFIELD[BY][BX+2]>10)
										){
									flg=false;
								}
							}
						}
						if(flg){
							if(key==KeyEvent.VK_T)
								B_type+=3;
							else if(key==KeyEvent.VK_R)
								B_type++;
						}
					}
				}
			}
		}
	}
	public void keyReleased(KeyEvent e) {}

	public void lordHiScore(){
		String input="data/rank.txt";
		//ファイルの読み込み
		try{
			BufferedReader reader=new BufferedReader(new FileReader(input));
			String line;

			for(int i=0;i<5;i++){
				line = reader.readLine();
				rank[i]=Integer.parseInt(line);
				if(rank[i]%97!=0)
					rank[i]=0;
				else{
					rank[i]/=97;
					rank[i]*=100;
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
		// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (NumberFormatException e) {
		// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
		// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	public void lordReadme(){
		String input="data/readme.txt";
		//ファイルの読み込み
		try{
			BufferedReader reader=new BufferedReader(new FileReader(input));
			for(int i=0;i<readme.length;i++)
				readme[i]=reader.readLine();
			reader.close();
		} catch (FileNotFoundException e) {
		// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (NumberFormatException e) {
		// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
		// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	public void resultGame(){
		String input="data/rank.txt";

		//sort
		if(sort_flg){
			for(int i=0;i<rank.length;i++)
				for(int j=i;j<rank.length;j++){
					if(rank[i]<rank[j]){
						int tmp;
						tmp=rank[i];
						rank[i]=rank[j];
						rank[j]=tmp;
					}
					else if(i!=j&&rank[i]==rank[j])
						rank[j]=0;
				}
			sort_flg=false;
		}

		//ファイルの書き出し
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(input));
			for (int i = 0; i < 5; i++) {
				writer.write(Integer.toString(rank[i]/100*97));
				writer.newLine();
			}
			writer.close();
		} catch (FileNotFoundException e) {
		// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (NumberFormatException e) {
		// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
		// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	private void setField(Block ar_bfield[][],int ar_ifield[][],int ar_check){
		int state,tmp=0;
		for(int y=0;y<ar_bfield.length;y++)
			for(int x=0;x<ar_bfield[y].length;x++){
					state = ar_ifield[y][x];
					if(ar_check==0){
						ar_bfield[y][x]=new Block(20+27*x,-34+27*y,25,25,state,Color.BLACK);
						tmp=ar_bfield[y][x].getBlockState()%10;
					}
					else if(ar_check==1){
						ar_bfield[y][x]=new Block(2+18*x,2+18*y,16,16,state,Color.BLACK);
						tmp=ar_bfield[y][x].getBlockState();
					}
					else if(ar_check==2){
						ar_bfield[y][x]=new Block(314+13*x,85+13*y,11,11,state,Color.BLACK);
						tmp=ar_bfield[y][x].getBlockState()%10;
					}
						switch(tmp){
							case Block.typeI:
								ar_bfield[y][x].setColor(Color.CYAN);
								break;
							case Block.typeO:
								ar_bfield[y][x].setColor(Color.YELLOW);
								break;
							case Block.typeS:
								ar_bfield[y][x].setColor(Color.GREEN);
								break;
							case Block.typeZ:
								ar_bfield[y][x].setColor(Color.RED);
								break;
							case Block.typeJ:
								ar_bfield[y][x].setColor(Color.BLUE);
								break;
							case Block.typeL:
								ar_bfield[y][x].setColor(Color.ORANGE);
								break;
							case Block.typeT:
								ar_bfield[y][x].setColor(Color.MAGENTA);
								break;
							case 8:
								ar_bfield[y][x].setColor(Color.PINK);
								break;
							case 9:
								ar_bfield[y][x].setColor(Color.DARK_GRAY);
								break;
							case 10:
								ar_bfield[y][x].setColor(Color.GRAY);
							break;
							default:
								break;
					}
			}
	}
	private void drawfield(int ar_bx,int ar_by,int ar_b_s,int ar_field[][],int ar_B_type,int ar_color){
		switch(ar_b_s){
			case 1:
				if(ar_B_type%2==0){
					ar_field[ar_by][ar_bx]=
					ar_field[ar_by][ar_bx+1]=
					ar_field[ar_by][ar_bx+2]=
					ar_field[ar_by][ar_bx+3]=ar_color;
				}
				else{
					ar_field[ar_by][ar_bx]=
					ar_field[ar_by-1][ar_bx]=
					ar_field[ar_by-2][ar_bx]=
					ar_field[ar_by-3][ar_bx]=ar_color;
				}
				break;
			case 2:
				ar_field[ar_by][ar_bx]=
				ar_field[ar_by][ar_bx+1]=
				ar_field[ar_by-1][ar_bx]=
				ar_field[ar_by-1][ar_bx+1]=ar_color;
				break;
			case 3:
				if(ar_B_type%2==0){
					ar_field[ar_by][ar_bx]=
					ar_field[ar_by][ar_bx+1]=
					ar_field[ar_by-1][ar_bx+1]=
					ar_field[ar_by-1][ar_bx+2]=ar_color;
				}
				else{
					ar_field[ar_by-2][ar_bx]=
					ar_field[ar_by-1][ar_bx]=
					ar_field[ar_by-1][ar_bx+1]=
					ar_field[ar_by][ar_bx+1]=ar_color;
				}
				break;
			case 4:
				if(ar_B_type%2==0){
					ar_field[ar_by-1][ar_bx]=
					ar_field[ar_by-1][ar_bx+1]=
					ar_field[ar_by][ar_bx+1]=
					ar_field[ar_by][ar_bx+2]=ar_color;
				}
				else{
					ar_field[ar_by-2][ar_bx+1]=
					ar_field[ar_by-1][ar_bx+1]=
					ar_field[ar_by-1][ar_bx]=
					ar_field[ar_by][ar_bx]=ar_color;
				}
				break;
			case 5:
				switch(ar_B_type%4){
					case 0:
						ar_field[ar_by-1][ar_bx]=
						ar_field[ar_by][ar_bx]=
						ar_field[ar_by][ar_bx+1]=
						ar_field[ar_by][ar_bx+2]=ar_color;
						break;
					case 1:
						ar_field[ar_by][ar_bx]=
						ar_field[ar_by][ar_bx+1]=
						ar_field[ar_by-1][ar_bx+1]=
						ar_field[ar_by-2][ar_bx+1]=ar_color;
						break;
					case 2:
						ar_field[ar_by-1][ar_bx]=
						ar_field[ar_by-1][ar_bx+1]=
						ar_field[ar_by-1][ar_bx+2]=
						ar_field[ar_by][ar_bx+2]=ar_color;
						break;

					case 3:
						ar_field[ar_by][ar_bx]=
						ar_field[ar_by-1][ar_bx]=
						ar_field[ar_by-2][ar_bx]=
						ar_field[ar_by-2][ar_bx+1]=ar_color;
						break;
					default:
						break;
				}
				break;
			case 6:
				switch(ar_B_type%4){
					case 0:
						ar_field[ar_by][ar_bx]=
						ar_field[ar_by][ar_bx+1]=
						ar_field[ar_by][ar_bx+2]=
						ar_field[ar_by-1][ar_bx+2]=ar_color;
						break;
					case 1:
						ar_field[ar_by-2][ar_bx]=
						ar_field[ar_by-2][ar_bx+1]=
						ar_field[ar_by-1][ar_bx+1]=
						ar_field[ar_by][ar_bx+1]=ar_color;
						break;
					case 2:
						ar_field[ar_by][ar_bx]=
						ar_field[ar_by-1][ar_bx]=
						ar_field[ar_by-1][ar_bx+1]=
						ar_field[ar_by-1][ar_bx+2]=ar_color;
						break;
					case 3:
						ar_field[ar_by][ar_bx]=
						ar_field[ar_by-1][ar_bx]=
						ar_field[ar_by-2][ar_bx]=
						ar_field[ar_by][ar_bx+1]=ar_color;
						break;
					default:
						break;
				}
				break;
			case 7:
				switch(ar_B_type%4){
					case 0:
						ar_field[ar_by][ar_bx]=
						ar_field[ar_by][ar_bx+1]=
						ar_field[ar_by-1][ar_bx+1]=
						ar_field[ar_by][ar_bx+2]=ar_color;
					break;
					case 1:
						ar_field[ar_by-1][ar_bx]=
						ar_field[ar_by][ar_bx+1]=
						ar_field[ar_by-1][ar_bx+1]=
						ar_field[ar_by-2][ar_bx+1]=ar_color;
					break;
					case 2:
						ar_field[ar_by-1][ar_bx]=
						ar_field[ar_by-1][ar_bx+1]=
						ar_field[ar_by][ar_bx+1]=
						ar_field[ar_by-1][ar_bx+2]=ar_color;
					break;
					case 3:
						ar_field[ar_by][ar_bx]=
						ar_field[ar_by-1][ar_bx]=
						ar_field[ar_by-2][ar_bx]=
						ar_field[ar_by-1][ar_bx+1]=ar_color;
					break;
					default:
						break;
				}
				break;
			case 8:
				switch(ar_B_type%4){
				case 0:
					ar_field[ar_by][ar_bx]=
					ar_field[ar_by-1][ar_bx+1]=
					ar_field[ar_by][ar_bx+2]=ar_color;
					break;
				case 1:
					ar_field[ar_by][ar_bx+1]=
					ar_field[ar_by-1][ar_bx]=
					ar_field[ar_by-2][ar_bx+1]=ar_color;
					break;
				case 2:
					ar_field[ar_by-1][ar_bx]=
					ar_field[ar_by][ar_bx+1]=
					ar_field[ar_by-1][ar_bx+2]=ar_color;
					break;
				case 3:
					ar_field[ar_by][ar_bx]=
					ar_field[ar_by-1][ar_bx+1]=
					ar_field[ar_by-2][ar_bx]=ar_color;
					break;
					default:
						break;
				}
				break;
			default:
				break;
		}
	}
}
