package tet;

public class Score {
	private int score;
	
	Score(){
		resetScore();
	}
	public int getScore(){
		return score;
	}
	public void addScore(int point){
		score+=point;
	}
	public void resetScore(){
		score=0;
	}

}
