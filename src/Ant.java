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

	//単位時間進むごとに蟻が持っている食べ物フェロモンや巣のフェロモンが減少する割合
	public static double dropoffRate = .9;
	//ある確率で移動先のセルの選択の基準が変わってるっぽい
	public static double bestCellNext = 0.5;
	//Food NeedでAllが選択されているかどうか.
	public static boolean allFoodRequired = false;
	//その蟻がいるセルの行番号列番号
	private int x;
	private int y;
	//ひとつの食べ物または全ても食べ物を得て帰還モードに入っているかどうか.
	private boolean returnToNest;
	//その蟻がいる世界
	Cell[][] world;
	//使用されていない謎の変数
	double maxPheromone = 10.0;
	//巣もしくは食べ物を出発してからの経過ステップ数
	int steps = 0;
	//その蟻がいるシミュレーション領域
	private Ants ants;

	//コンストラクタ
	public Ant(Cell startCell/*初期位置*/, Cell[][] world/*その蟻がいる世界*/, Ants ants/*その蟻がいるシミュレーション領域*/){
		this.x = startCell.c;
		this.y = startCell.r;
		this.world = world;
		this.ants = ants;
	}

	//その蟻が必要な食べ物を得て巣に帰還した時に呼び出される.蟻は死に,ランダムな巣にまた生まれる.
	public void die(){
		returnToNest = false;
		steps = 0;
		foodFound.clear();
		Set<Cell> nests = ants.getNests();
		if(!nests.isEmpty()){
			int nestIndex = (int) (nests.size() * Math.random());
			Cell nest = (Cell) nests.toArray()[nestIndex];
			x = nest.c;
			y = nest.r;
		}
	}

	//状態遷移関数
	public void step(){
		//0以上1より小さい乱数
		double chanceToTakeBest = Math.random();
		
		//巣を出発してからの経過時間をインクリメント
		steps++;

		//食べ物が消去されていたらその食べ物の記憶を消去する.
		foodFound.retainAll(ants.getFood());

		//巣に帰ろうとしている時
		if(returnToNest){
			//巣に帰ったら死ぬ.
			if(world[x][y].hasNest()){
				die();
			}
			//巣に帰ろうとしているがまだ巣にたどり着けていない時
			else{
				//今までに調べた隣接セルの中で最も強い巣のフェロモン
				double maxNestSoFar = 0;
				//その食べ物セルから隣接セルに届いている食べ物フェロモンの最大値
				Map<Cell/*食べ物セル*/, Double/*隣接セルのうちその食べ物セルから届いている食べ物フェロモンが最も高いセルのその食べ物セルから届いている食べ物フェロモンの強さ*/> maxFoodSoFarMap = new HashMap<Cell, Double>();
				//隣接セルのうち最大の巣のフェロモンを持っている隣接セルの集合
				List<Cell> maxNestCells = new ArrayList<Cell>();
				//全ての隣接セル
				List<Cell> allNeighborCells = new ArrayList<Cell>();
				//全ての隣接セルの巣のフェロモンの和
				double totalNeighborPheromones = 0;
				//8つの隣接セルをひとつずつ見ていく.
				for(int c = -1; c <=1; c++){

					if(x+c < 0 || x+c >= world.length){
						continue;
					}

					for(int r = -1; r <= 1; r++){
						if(c == 0 && r == 0){
							continue;
						}
						else if(y+r < 0 || y+r >= world[0].length){
							continue;
						}
						//その隣接セルが障害物でなければ
						if(!world[x+c][y+r].isBlocked()){
							//隣接セルを追加する.
							allNeighborCells.add(world[x+c][y+r]);
							//全ての隣接セルの巣のフェロモンの和にその隣接セルの巣のフェロモンを足す.
							totalNeighborPheromones += world[x+c][y+r].nestPheromoneLevel;
							//隣接セルのうち最大の巣のフェロモンを持っている隣接セルの集合の更新
							if(world[x+c][y+r].getNestPheromoneLevel() > maxNestSoFar){
								maxNestSoFar = world[x+c][y+r].getNestPheromoneLevel();

								maxNestCells.clear();
								maxNestCells.add(world[x+c][y+r]);
							}
							else if(world[x+c][y+r].getNestPheromoneLevel() == maxNestSoFar){
								maxNestCells.add(world[x+c][y+r]);
							}
							//その蟻が見つけた全ての食べ物セルについて
							for(Cell food : foodFound){
								//その食べ物セルから隣接セルに届いている食べ物フェロモンの最大値を更新する.
								if(!maxFoodSoFarMap.containsKey(food) || world[x+c][y+r].getFoodPheromoneLevel(food) > maxFoodSoFarMap.get(food)){
									maxFoodSoFarMap.put(food, world[x+c][y+r].getFoodPheromoneLevel(food));
								}
							}	
						}
					}
				}
				//今自分がいるところに食べ物がある場合食べ物セルからそのセルに届いている食べ物フェロモンの最大値にその食べ物セルからそのセル自身に届いている食べ物フェロモンを追加する.
				if(world[x][y].isGoal()){
					maxFoodSoFarMap.put(world[x][y], Cell.maxFoodPheromoneLevel);
				}
				//その蟻が見つけた全ての食べ物セルについて今自分がいるセルのその食べ物セルから届いている食べ物フェロモンを更新する.
				for(Cell food : foodFound){
					world[x][y].setFoodPheromone(food, maxFoodSoFarMap.get(food) * Ant.dropoffRate);
				}
				//蟻の移動先を決定する.
				//確率Ant.bestCellNextでこっちを実行する.
				//巣のフェロモンが最も大きい隣接セルに移動する.
				if(Ant.bestCellNext > chanceToTakeBest){
					//巣のフェロモンが最も大きい隣接セルがある場合その中からひとつの隣接セルをランダムに選んで底に移動する.
					if(!maxNestCells.isEmpty()){
						int cellIndex = (int) (maxNestCells.size()*Math.random());
						Cell bestNestCellSoFar = maxNestCells.get(cellIndex);

						x = bestNestCellSoFar.c;
						y = bestNestCellSoFar.r;
					}
				}
				//確率1-Ant.bestCellNextでこっちを実行する.
				//巣のフェロモンの強さに応じた確率で移動先の隣接セルを決定してそこに移動する.
				else{ //give cells chance based on pheremone
					double pheremonesSoFar = 0;
					double goalPheromoneLevel = totalNeighborPheromones * Math.random();
					for(Cell neighbor : allNeighborCells){
						pheremonesSoFar+=neighbor.getNestPheromoneLevel();
						if(pheremonesSoFar > goalPheromoneLevel){
							x = neighbor.c;
							y = neighbor.r;
							break;
						}
					}
				}
			}
		}
		//巣に帰ろうとしていない時
		else{ //look for food
			//そこに食べ物がある場合
			if(world[x][y].isGoal()){
				//自分が見つけた食べ物セルに現在地のセルを追加する.
				foodFound.add(world[x][y]);
				//Food NeedでAllが選択されている場合
				if(Ant.allFoodRequired){
					//全ての食べ物を見つけていたら帰還モードに入る.
					if(foodFound.size() >= ants.getFood().size()){
						steps = 0;
						returnToNest = true;
						return;
					}
				}
				//Food NeedでOneが選択されている場合
				else{
					//帰還モードに入る.
					steps = 0;
					returnToNest = true;
					return;
				}
			}
			//そこに食べ物がなく巣があり出発してから時間が2以上経過している場合場合
			else if(world[x][y].hasNest()){
				if(steps > 1){
					die();
					return;
				}
			}
			//今までに調べた隣接セルの中で最も強い食べ物フェロモン
			double maxFoodSoFar = 0;
			//今までに調べた隣接セルの中で最も強い巣のフェロモン
			double maxNestSoFar = 0;
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
						//今までに調べた隣接セルの中で最も強い巣のフェロモンを更新する.
						if(world[x+c][y+r].getNestPheromoneLevel() > maxNestSoFar){
							maxNestSoFar = world[x+c][y+r].getNestPheromoneLevel();
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
			//そこに巣がある場合最大の巣のフェロモンを更新する.
			if(world[x][y].hasNest()){
				maxNestSoFar = Cell.maxNestPheromoneLevel;
			}
			//そこの巣のフェロモンを更新する.
			world[x][y].setNestPheromone(maxNestSoFar * Ant.dropoffRate);

			//蟻の移動先を決定する.
			//確率Ant.bestCellNextでこっちを実行する.
			//食べ物フェロモンが最も大きい隣接セルに移動する.
			if(Ant.bestCellNext > chanceToTakeBest){
				if(!maxFoodCells.isEmpty()){
					int cellIndex = (int) (maxFoodCells.size()*Math.random());
					Cell bestCellSoFar = maxFoodCells.get(cellIndex);

					x = bestCellSoFar.c;
					y = bestCellSoFar.r;
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
								return;
							}
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

	//その蟻が巣に帰ろうとしているかどうか
	public boolean isReturningHome() {
		return returnToNest;
	}
}
