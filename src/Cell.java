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
	//このセルが食べ物セルであった場合の,食べ物が発生する確率
	private double foodProbability;
	//このセルが食べ物セルであった場合,食べ物が発生しているかどうか
	private boolean hasFood = false;
	//このセルが食べ物セルであった場合,警備員がこのセルの自転車を整理中であるかどうか
	private boolean isSet = false;
	//このセルが食べ物セルであった場合,測定期間中に食べ物が発生していたステップ数
	private int hasFoodSteps = 0;
	//測定中であるかどうか
	private boolean isObserved = false;
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
		//食べ物セルは一定の確率で食べ物が発生する.
		if(isGoal && !hasFood && !isSet && Math.random() < foodProbability)
		{
			System.out.println("セル" + c + "," + r + "乱れました.");
			hasFood = true;
		}
		//測定中で自転車が乱れていたら乱れているステップ数をインクリメントする.
		if(isObserved && hasFood)hasFoodSteps++;
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

	//その食べ物セルの自転車が乱れた時に警備員が整理に要する時間
	public int getWaitTime() {
		return waitTime;
	}

	//その食べ物セルの自転車が乱れているかどうか
	public boolean hasFood() {
		return hasFood;
	}

	//その食べ物セルが自転車整理されている最中であるかどうか
	public boolean isSet() {
		return isSet;
	}

	//警備員がその食べ物セルの自転車整理を開始する.
	public void beginSet() {
		System.out.println("セル" + c + "," + r + "整理します.");
		isSet = true;
	}

	//警備員がその食べ物セルの自転車整理を完了する.
	public void endSet() {
		System.out.println("セル" + c + "," + r + "整理終わりました.");
		isSet = false;
		hasFood = false;
	}

	//測定を開始する
	public void observe() {
		//測定期間に入る.
		isObserved = true;
		//自転車置き場が乱れる.
		hasFood = true;
		System.out.println("セル" + c + "," + r + "測定開始!");
	}

	//測定中に乱れていたステップ数
	public int getHasFoodSteps() {
		return hasFoodSteps;
	}

	//自転車置き場が1ステップ値に乱れる確率を設定する.
	public void setFoodProbability(double probability) {
		foodProbability = probability;
	}
}
