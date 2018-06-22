import java.util.HashMap;
import java.util.Map;


public class Cell {
	//食べ物フェロモンの最大値
	public static double maxFoodPheromoneLevel = 100.0;
	//フェロモンの蒸発率
	public static double evaporationRate = .9;
	//そのセルに障害物があるかどうか
	private boolean hasObstacle;
	//そのセルに巣があるかどうか
	private boolean hasNest;
	//食べ物フェロモン
	public Map<Cell/*食べ物セル*/, Double/*その食べ物セルのフェロモンが自分のセルにどれくらい届いているか*/> foodPheromoneLevelMap = new HashMap<Cell, Double>();
	//そのセルに食べ物があるかどうか
	private boolean isGoal = false;
	//このセルが食べ物セルであった場合の,警備員が自転車整理にかかる時間
	private int waitTime;
	//そのセルの行番号列番号
	int c;
	int r;

	//コンストラクタ.行番号と列番号を指定する.
	public Cell(int c, int r){
		this.c = c;
		this.r = r;
	}

	//そのセルに食べ物があるかどうかを設定する.
	public void setIsGoal(boolean isGoal, int waitTime/*この自転車置き場の取り締まりにかかる時間*/){
		this.isGoal = isGoal;
		this.waitTime = waitTime;
	}
	
	//状態遷移関数
	public void step(){
		//そのセルにフェロモンが届いている各食べ物セルについて
		for(Cell food : foodPheromoneLevelMap.keySet()){
			//その食べ物セルから届いている食べ物フェロモンの強さ
			double foodPheromoneLevel = foodPheromoneLevelMap.get(food);
			//食べ物フェロモンを蒸発させる.
			foodPheromoneLevel *= Cell.evaporationRate;
			if(foodPheromoneLevel < 1){
				foodPheromoneLevel = 1;
			}
			if(foodPheromoneLevel > Cell.maxFoodPheromoneLevel){
				foodPheromoneLevel = Cell.maxFoodPheromoneLevel;
			}
			//食べ物フェロモンの強さを更新する.
			foodPheromoneLevelMap.put(food, foodPheromoneLevel);
		}
	}

	//蟻が食べ物フェロモンをそのセルに運んできた時に呼び出される関数.その蟻が運んできた食べ物フェロモンを自身の食べ物フェロモンマップに追加または更新する.
	public void setFoodPheromone(Cell food, double pheromone){
		if(pheromone > Cell.maxFoodPheromoneLevel){
			pheromone = Cell.maxFoodPheromoneLevel;
		}
		foodPheromoneLevelMap.put(food, pheromone);
	}
	
	//指定された食べ物セルからこのセルに行き届いている食べ物フェロモンの強さを返す.
	public double getFoodPheromoneLevel(Cell food){
		if(!foodPheromoneLevelMap.containsKey(food)){
			return 1;
		}
		return foodPheromoneLevelMap.get(food);
	}
	
	//そのセルに障害物があるかどうか.
	public boolean isBlocked(){
		return hasObstacle;
	}

	//そのセルに食べ物があるかどうか.
	public boolean isGoal() {
		return isGoal;
	}

	//そのセルに障害物があるかどうかを設定する.
	public void setIsObstacle(boolean hasObstacle) {
		this.hasObstacle = hasObstacle;
	}

	//そのセルに巣があるかどうか.
	public boolean hasNest() {
		return hasNest;
	}

	//そのセルに巣があるかどうかを設定する.
	public void setHasNest(boolean hasNest) {
		this.hasNest = hasNest;
	}	

	//警備員が取り締まりに要する時間
	public int getWaitTime() {
		return waitTime;
	}
}
