import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/**
 * Ant agent that interacts with an ant colony simulation evironment.
 */
public class Ant {
	//その蟻が見つけた食べ物のセルの集合
	Set<Cell> foodFound = new HashSet<Cell>();

	//単位時間進むごとに蟻が持っている食べ物フェロモンが減少する割合
	public static double dropoffRate = .9;
	//ある確率で移動先のセルの選択の基準が変わってるっぽい
	public static double bestCellNext = 0.5;
	//その蟻がいるセルの行番号列番号
	private int x;
	private int y;
	//その蟻がいる世界
	Cell[][] world;
	//巣もしくは食べ物を出発してからの経過ステップ数
	int steps = 0;
	//その蟻がいるシミュレーション領域
	private Ants ants;

	//自転車整理にかかる残り時間
	private int waitTime = 0;

	//コンストラクタ
	public Ant(Cell startCell/*初期位置*/, Cell[][] world/*その蟻がいる世界*/, Ants ants/*その蟻がいるシミュレーション領域*/){
		this.x = startCell.c;
		this.y = startCell.r;
		this.world = world;
		this.ants = ants;
	}

	//状態遷移関数
	public void step(){
		//自転車整理中は動かない
		if(waitTime > 0)
		{
			waitTime--;
			return;
		}
		//0以上1より小さい乱数
		double chanceToTakeBest = Math.random();
		
		//巣を出発してからの経過時間をインクリメント
		steps++;

		//食べ物が消去されていたらその食べ物の記憶を消去する.
		foodFound.retainAll(ants.getFood());

		//そこに食べ物がある場合
		if(world[x][y].isGoal()){
			//自分が見つけた食べ物セルに現在地のセルを追加する.
			foodFound.add(world[x][y]);
			//全ての食べ物を見つけていたら食べ物の記憶を消去する.
			if(foodFound.size() >= ants.getFood().size()){
				foodFound.clear();
				//ただし最後に見つけた食べ物だけは記憶に残しておく.二重に食べ物を得ることを防ぐ.
				foodFound.add(world[x][y]);
			}
		}
		//今までに調べた隣接セルの中で最も強い食べ物フェロモン
		double maxFoodSoFar = 0;
		//食べ物フェロモンが最も大きい隣接セルの集合
		List<Cell> maxFoodCells = new ArrayList<Cell>();
		//全ての隣接セル
		List<Cell> allNeighborCells = new ArrayList<Cell>();
		//全ての隣接セルに届いている全ての食べ物フェロモンの和
		double totalNeighborPheromones = 0;
		//その食べ物セルから隣接セルに届いている食べ物フェロモンの最大値
		Map<Cell, Double> maxFoodSoFarMap = new HashMap<Cell, Double>();
		//隣接セルをひとつずつ見ていく
		for(int c = -1; c <=1; c++){

			if(x+c < 0 || x+c >= world.length){
				continue;
			}

			for(int r = -1; r <= 1; r++){
				//don't count yourself
				if(c == 0 && r == 0){
					continue;
				}
				else if(y+r < 0 || y+r >= world[0].length){
					continue;
				}

				if(!world[x+c][y+r].isBlocked()){
					//全ての隣接セルにその隣接セルを追加する.
					allNeighborCells.add(world[x+c][y+r]);

					if(maxFoodSoFar == 0){
						maxFoodCells.add(world[x+c][y+r]);
					}
					//その蟻が見つけた全ての食べ物セルについて
					for(Cell food : foodFound){
						//その食べ物セルから隣接セルに届いている食べ物フェロモンの最大値を更新する.
						if(!maxFoodSoFarMap.containsKey(food) || world[x+c][y+r].getFoodPheromoneLevel(food) > maxFoodSoFarMap.get(food)){
							maxFoodSoFarMap.put(food, world[x+c][y+r].getFoodPheromoneLevel(food));
						}

					}
					//そこに食べ物がある場合
					if(world[x][y].isGoal()){
						maxFoodSoFarMap.put(world[x][y], Cell.maxFoodPheromoneLevel);
					}

					for(Cell food : foodFound){
						//蟻が食べ物フェロモンをそのセルに運んできた時に呼び出される関数.その蟻が運んできた食べ物フェロモンを自身の食べ物フェロモンマップに追加または更新する.
						world[x][y].setFoodPheromone(food, maxFoodSoFarMap.get(food) * Ant.dropoffRate);
					}
					//環境中に食べ物セルがない場合
					if(ants.getFood().isEmpty()){
						totalNeighborPheromones += 1;
					}
					//環境中に食べ物セルがある場合
					else{
						//全ての食べ物セルについて
						for(Cell food : ants.getFood()){
							//その食べ物セルを自分がすでに見つけているならなにもしない.
							if(foodFound.contains(food)){
								continue;
							}
							//その食べ物セルからその隣接セルに届いている食べ物フェロモンを全ての隣接セルに届いている全ての食べ物フェロモンの和に足す.
							totalNeighborPheromones += world[x+c][y+r].getFoodPheromoneLevel(food);
							//最大の食べ物フェロモンを更新する.
							if(world[x+c][y+r].getFoodPheromoneLevel(food) > maxFoodSoFar){
								maxFoodSoFar = world[x+c][y+r].getFoodPheromoneLevel(food);
								maxFoodCells.clear();
								maxFoodCells.add(world[x+c][y+r]);
							}
							else if(world[x+c][y+r].getFoodPheromoneLevel(food) == maxFoodSoFar){
								maxFoodCells.add(world[x+c][y+r]);
							}
						}	
					}
				}
			}
		}
		//蟻の移動先を決定する.
		//確率Ant.bestCellNextでこっちを実行する.
		//食べ物フェロモンが最も大きい隣接セルに移動する.
		if(Ant.bestCellNext > chanceToTakeBest){
			if(!maxFoodCells.isEmpty()){
				int cellIndex = (int) (maxFoodCells.size()*Math.random());
				Cell bestCellSoFar = maxFoodCells.get(cellIndex);

				x = bestCellSoFar.c;
				y = bestCellSoFar.r;
				//移動先のセルで自転車が乱れている場合自転車整理を開始する.
				if(world[x][y].isGoal() && world[x][y].hasFood())waitTime = world[x][y].getWaitTime();
			}
		}	
		//確率1-Ant.bestCellNextでこっちを実行する.
		//食べ物フェロモンの強さに応じた確率で移動先の隣接セルを決定してそこに移動する.
		else{ //give cells chance based on pheremone
			double pheremonesSoFar = 0;
			double goalPheromoneLevel = totalNeighborPheromones * Math.random();
			//全ての隣接セルについて
			for(Cell neighbor : allNeighborCells){
				//環境中に食べ物セルがない場合
				if(ants.getFood().isEmpty()){
					pheremonesSoFar += 1;
					if(pheremonesSoFar > goalPheromoneLevel){

						x = neighbor.c;
						y = neighbor.r;
						//移動先のセルで自転車が乱れている場合自転車整理を開始する.
						if(world[x][y].isGoal() && world[x][y].hasFood())waitTime = world[x][y].getWaitTime();
						break;
					}
				}
				//環境中に食べ物セルがある場合
				else{

					for(Cell food : ants.getFood()){
						//発見済みの食べ物のフェロモンは無視する.
						if(foodFound.contains(food)){
							continue;
						}
						pheremonesSoFar+=neighbor.getFoodPheromoneLevel(food);
						if(pheremonesSoFar > goalPheromoneLevel){

							x = neighbor.c;
							y = neighbor.r;
							//移動先のセルで自転車が乱れている場合自転車整理を開始する.
							if(world[x][y].isGoal() && world[x][y].hasFood())waitTime = world[x][y].getWaitTime();
							return;
						}
					}
				}
			}
		}
	}

	//その蟻が何列目にいるか
	public int getCol() {
		return x;
	}

	//その蟻が何行目にいるか
	public int getRow(){
		return y;
	}
}
