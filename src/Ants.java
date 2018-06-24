import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;


import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

//シミュレーション領域を描画するパネル
/**
 * Class for displaying and updating an ant colony simulation.
 * Another class should be used to control the simulation
 * by calling the methods of this class.
 */
@SuppressWarnings("serial")
public class Ants extends JPanel{
	//Pattern(障害物の配置方法)の選択肢
	public static enum Pattern{
		Clear/*障害物なし*/, Random/*0.3の確率でランダムに障害物を配置し,中央のセルに巣を配置する*/, Filled/*全てのセルを障害物にする*/;
	}
	//セルがとり得る状態
	public static enum Tile{
		OBSTACLE/*障害物*/, NEST/*巣*/, GOAL/*食べ物*/, CLEAR/*何もない*/; 
	}
	//Place tileで選択されているやつ
	private Tile tile = Tile.GOAL;

	//シミュレーション領域内にセルが100*100個ある.
	int rows = 100;
	int columns = 100;
	//セルの2次元配列
	Cell [][] cellArray = new Cell[columns][rows];

	private int maxAnts = 1;
	//蟻の配列
	private List<Ant/*Ant.javaで定義されているありのクラス*/> ants = new ArrayList<Ant>();

	//巣があるセルの集合
	private Set<Cell> nests = new HashSet<Cell>();
	//食べ物があるセルの集合
	private Set<Cell> food = new HashSet<Cell>();

	//シミュレーションの詳細設定
	AdvancedControlPanel advancedControlPanel;
	
	//詳細設定のaboutを押した時に表示される各パラメータに関する説明書き
	final JInternalFrame aboutFrame = new JInternalFrame("About", false, true);
	//最初にシミュレーション領域の上部に表示される簡単な説明書き
	final JInternalFrame messageFrame = new JInternalFrame("Getting Started", false, true);

	//コンストラクタ
	public Ants(){

		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		//最初にシミュレーション領域の上部に表示される簡単な説明書きを生成して表示する.
		JLabel messageLabel1 = new JLabel("Place some food and obstacles in the environment and press play,");
		JLabel messageLabel2 = new JLabel("then experiment by changing the environment!");
		JLabel messageLabel3 = new JLabel("For more info, see About from the Advanced panel.");
		messageFrame.add(messageLabel1, BorderLayout.NORTH);
		messageFrame.add(messageLabel2, BorderLayout.CENTER);
		messageFrame.add(messageLabel3, BorderLayout.SOUTH);
		add(messageFrame);
		messageFrame.pack();
		messageFrame.setLocation(300, 300);
		messageFrame.setVisible(true);
		
		//詳細設定のaboutを押した時に表示される各パラメータに関する説明書きを生成する.表示はしない.
		JPanel aboutPanel = new JPanel();
		//上からコンポーネントを追加していく.
		aboutPanel.setLayout(new BoxLayout(aboutPanel, BoxLayout.Y_AXIS));
		//10ピクセルの余白
		aboutPanel.add(Box.createVerticalStrut(10));
		//パラメータに関する説明書き
		aboutPanel.add(new JLabel("Auto-adjust: Automatically adjust parameters over time."));
		aboutPanel.add(Box.createVerticalStrut(10));
		aboutPanel.add(new JLabel("Deltas: How fast each parameter should adjust."));
		aboutPanel.add(Box.createVerticalStrut(10));
		aboutPanel.add(new JLabel("Max Pheromone: The maximum pheromone allowed in the environment."));
		aboutPanel.add(Box.createVerticalStrut(10));
		aboutPanel.add(new JLabel("Evaporation: How fast pheromones dissipate."));
		aboutPanel.add(Box.createVerticalStrut(10));
		aboutPanel.add(new JLabel("Dropoff: Pheromones get weaker further from nests or food."));
		aboutPanel.add(Box.createVerticalStrut(10));
		aboutPanel.add(new JLabel("Trail Strength: How strictly ants follow strongest pheromones."));
		aboutPanel.add(Box.createVerticalStrut(10));
		aboutPanel.add(new JLabel("Adapt Time: How fast all parameters should adjust."));
		aboutPanel.add(Box.createVerticalStrut(10));
		aboutPanel.add(new JLabel("Food Needed: One: Ants must find one food before returning."));
		aboutPanel.add(Box.createVerticalStrut(10));
		aboutPanel.add(new JLabel("Food Needed: All: Ants must find all food before returning. (TSP)"));
		aboutPanel.add(Box.createVerticalStrut(10));
		aboutFrame.setContentPane(aboutPanel);
		aboutFrame.pack();
		add(aboutFrame);
		aboutFrame.setLocation(300, 300);
		
		
		//シミュレーション領域をクリックするとクリックしたセルにPlace Tileで選択されているやつが配置されるやつ
		addMouseListener(new MouseAdapter(){
			//シミュレーション領域がクリックされた時の処理
			@Override
			public void mouseClicked(MouseEvent e){
				//最初にシミュレーション領域の上部に表示される簡単な説明書きを破棄する.
				messageFrame.dispose();
				//詳細設定のaboutを押した時に表示される各パラメータに関する説明書きの不可視化
				aboutFrame.setVisible(false);
				//クリックされたセルの行番号と列番号
				int clickedCellColumn = (int) (((double)e.getX())/getWidth() * columns);
				int clickedCellRow = (int) (((double)e.getY())/getHeight() * rows);
				//どのセルもクリックされていなかったら何もしない.
				if(clickedCellColumn < 0 || clickedCellColumn >= columns || clickedCellRow < 0 || clickedCellRow >= rows){
					return;
				}
				//クリックされたセルの初期化(そのセルに設定されていた食べ物,巣,障害物を削除する.)
				cellArray[clickedCellColumn][clickedCellRow].setIsGoal(false, 0);
				food.remove(cellArray[clickedCellColumn][clickedCellRow]);
				cellArray[clickedCellColumn][clickedCellRow].setIsObstacle(false);
				cellArray[clickedCellColumn][clickedCellRow].setHasNest(false);
				nests.remove(cellArray[clickedCellColumn][clickedCellRow]);
				//左クリックされていたならPlace Tileで選択されているやつをそのセルに設定する.
				if(e.getButton() == MouseEvent.BUTTON1){
					if(Tile.OBSTACLE.equals(tile)){
						cellArray[clickedCellColumn][clickedCellRow].setIsObstacle(true);
					}
					else if(Tile.GOAL.equals(tile)){
						cellArray[clickedCellColumn][clickedCellRow].setIsGoal(true, 100);
						food.add(cellArray[clickedCellColumn][clickedCellRow]);
					}
					else if(Tile.NEST.equals(tile)){
						cellArray[clickedCellColumn][clickedCellRow].setHasNest(true);
						nests.add(cellArray[clickedCellColumn][clickedCellRow]);
					}
				}
				//advancedControlPanelにシミュレーション領域の環境が変更されたことを伝える.
				advancedControlPanel.environmentChanged();
				//セルの設定を変更した後のシミュレーション領域の再描画
				repaint();
			}
		});

		//シミュレーション領域上をマウスでドラッグしたときにそのセルにPlace Tileで選択されていたやつが配置されるやつ
		addMouseMotionListener(new MouseAdapter(){
			//以前マウスが置かれていたセルの座標.
			private int previousColumn = -1;
			private int previousRow = -1;
			//マウスが動いた時の処理
			@Override
			public void mouseDragged(MouseEvent e){
				//マウスの移動先のセルの座標
				int clickedCellColumn = (int) (((double)e.getX())/getWidth() * columns);
				int clickedCellRow = (int) (((double)e.getY())/getHeight() * rows);
				//マウスの移動先がシミュレーション領域の範囲外の時は何もしない.
				if(clickedCellColumn < 0 || clickedCellColumn >= columns || clickedCellRow < 0 || clickedCellRow >= rows){
					return;
				}
				//ドラッグしながらあるセルから別のセルにマウスが移った時.
				if(clickedCellColumn != previousColumn || clickedCellRow != previousRow){
					//マウスの移動先のセルの初期化(そのセルに設定されていた食べ物,巣,障害物を削除する.)
					cellArray[clickedCellColumn][clickedCellRow].setIsGoal(false, 0);
					food.remove(cellArray[clickedCellColumn][clickedCellRow]);
					cellArray[clickedCellColumn][clickedCellRow].setIsObstacle(false);
					cellArray[clickedCellColumn][clickedCellRow].setHasNest(false);
					nests.remove(cellArray[clickedCellColumn][clickedCellRow]);
					//Place Tileで選択されているやつを移動先のセルに設定する.
					if(Tile.OBSTACLE.equals(tile)){
						cellArray[clickedCellColumn][clickedCellRow].setIsObstacle(true);
					}
					else if(Tile.GOAL.equals(tile)){
						cellArray[clickedCellColumn][clickedCellRow].setIsGoal(true, 100);
						food.add(cellArray[clickedCellColumn][clickedCellRow]);
					}
					else if(Tile.NEST.equals(tile)){
						cellArray[clickedCellColumn][clickedCellRow].setHasNest(true);
						nests.add(cellArray[clickedCellColumn][clickedCellRow]);
					}
					//advancedControlPanelにシミュレーション領域の環境が変更されたことを伝える.
					advancedControlPanel.environmentChanged();
					//セルの設定を変更した後のシミュレーション領域の再描画
					repaint();
					//以前マウスが置かれていたセルの座標を更新する.
					previousColumn = clickedCellColumn;
					previousRow = clickedCellRow;
				}
			}
		});
		//背景色を白にする.
		setBackground(Color.WHITE);
		//シミュレーション領域を巣も食べ物もない状態にする.
		
		killAllCells();
		//ここにマップの初期状態を記述する.
		//シミュレーション領域の中央に巣を配置する.
		int[][] feald;
		feald = new int[100][100];
		Imagefileload feald_data = new Imagefileload();
		feald = feald_data.get_feald();
		for(int i=0;i<100;i++){
			for(int j=0;j<100;j++){
				if (feald[i][j]==1000){
					cellArray[i][j].setIsObstacle(true);//壁

				}else if (feald[i][j]==2000){
					cellArray[i][j].setHasNest(true);	
					nests.add(cellArray[i][j]);//巣

				}else if (feald[i][j]>0){
					System.out.println("Goal"); 
					cellArray[i][j].setIsGoal(true,feald[i][j]);	
					food.add(cellArray[i][j]);
				
				}
			}
		}
		
		//nests.add(cellArray[columns/2][rows/2]);
		//再描画
		repaint();
	}

	//プログラム開始時に呼び出される.シミュレーション領域を巣も食べ物もない状態にする.
	public void killAllCells(){
		//巣を全て消去する.
		nests.clear();
		//食べ物を全て消去する.
		food.clear();
		//セルの動的確保
		for(int column = 0; column < columns; column++){
			for(int row = 0; row < rows; row++){
				cellArray[column][row] = new Cell(column, row);
			}
		}
		//advancedControlPanelに環境が変わったことを伝える.
		if(advancedControlPanel != null){
			advancedControlPanel.environmentChanged();
		}
		//再描画
		repaint();
	}

	//描画処理
	public void paintComponent(Graphics g){
		//とりあえず親クラスの描画処理を呼び出す.
		super.paintComponent(g);
		//色を黒に設定する.
		g.setColor(Color.BLACK);
		//セルの横幅
		double cellWidth = (double)getWidth()/columns;
		//セルの高さ
		double cellHeight = (double)getHeight()/rows;
		//セルの行数と列数がともに50以下の場合セルの境界線を引く.
		if(columns <= 50 && rows <= 50){

			for(int column = 0; column < columns; column++){
				int cellX = (int) (cellWidth * column);
				g.drawLine(cellX, 0, cellX, getHeight());
			}

			for(int row = 0; row < rows; row++){
				int cellY = (int) (cellHeight * row);
				g.drawLine(0, cellY, getWidth(), cellY);
			}
		}
		//各セルの描画
		for(int column = 0; column < columns; column++){
			for(int row = 0; row < rows; row++){
				//そのセルの座標
				int cellX = (int) (cellWidth * column);
				int cellY = (int) (cellHeight * row);
				//そのセルの横幅と高さ
				int thisCellWidth = (int) (cellWidth*(column+1) - cellX);
				int thisCellHeight = (int) (cellHeight*(row+1) - cellY);
				//巣はオレンジ
				if(cellArray[column][row].hasNest()){
					g.setColor(Color.ORANGE);
				}
				//食べ物はランダムな色
				else if(cellArray[column][row].isGoal()){
					Random random = new Random(cellArray[column][row].hashCode());
					g.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256), 255));
				}
				//障害物は灰色
				else if(cellArray[column][row].isBlocked()){
					g.setColor(Color.GRAY);
				}
				//そのセルに何もないときは巣のフェロモンと食べ物のフェロモンの強さに応じた色を設定する.
				else{
					double foodPheromone = 0;
					double maxFood = 0;
					Cell maxFoodCell = null;

					for(Cell food : getFood()){
						if(cellArray[column][row].getFoodPheromoneLevel(food) > maxFood){
							maxFood = cellArray[column][row].getFoodPheromoneLevel(food);
							maxFoodCell = food;
						}
						foodPheromone = Math.max(foodPheromone, Math.min(1, (cellArray[column][row].getFoodPheromoneLevel(food)-1)/Cell.maxFoodPheromoneLevel));
					}

					if(maxFoodCell != null ){

						Random random = new Random(maxFoodCell.hashCode());
						g.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256), (int) (255*foodPheromone)));

					}
					else{
						g.setColor(Color.white);
					}
				}
				//指定した色でセルを塗りつぶす.
				g.fillRect(cellX+1, cellY+1, Math.max(thisCellWidth-1, 1), Math.max(thisCellHeight-1, 1));
			}
		}
		//蟻の描画
		for(Ant ant : ants){
			//その蟻のいるセルの行番号と列番号
			int column = ant.getCol();
			int row = ant.getRow();
			//その蟻の画面上の座標
			int cellX = (int) (cellWidth * column);
			int cellY = (int) (cellHeight * row);
			//その蟻のいるセルの幅と高さ
			int thisCellWidth = (int) (cellWidth*(column+1) - cellX);
			int thisCellHeight = (int) (cellHeight*(row+1) - cellY);
			//巣に戻ろうとしている蟻は青
			/*
			if(ant.isReturningHome()){
				g.setColor(Color.BLUE);
			}
			*/
			//まだ食べ物を探している蟻は黒
			//else{
				g.setColor(Color.BLACK);
			//}
			//指定された色で蟻を描画する.
			g.fillRect(cellX+2, cellY+2, thisCellWidth-3, thisCellHeight-3);
		}
	}

	//食べ物があるセルの集合を返す.
	public Set<Cell> getFood(){
		return food;
	}

	//巣があるセルの集合を返す.
	public Set<Cell> getNests(){
		return nests;
	}

	//状態遷移関数
	public void step(){
		//蟻の数が最大数を超えていない場合ランダムに選択した巣に新しい蟻を作る.
		if(ants.size() < maxAnts){
			if(!nests.isEmpty()){
				int nestIndex = (int) (nests.size() * Math.random());
				ants.add(new Ant((Cell) nests.toArray()[nestIndex], cellArray, this));
			}
		}
		//蟻の数が最大値を超えている場合蟻の配列の先頭要素を削除する.
		else if(ants.size() > maxAnts){
			ants.remove(0);
		}
		//各蟻の状態遷移関数を呼び出す.
		for(Ant ant : ants){
			ant.step();
		}
		//各セルの状態遷移関数を呼び出す.
		for(int column = 0; column < columns; column++){
			for(int row = 0; row < rows; row++){
				cellArray[column][row].step();
			}
		}
		//再描画
		repaint();
	}

	//AdvancedのAboutを呼び出した時に呼び出される関数.各パラメータの説明書きを表示する.
	public void showAboutFrame(){
		messageFrame.dispose();
		aboutFrame.setVisible(true);
	}

	//Place Tileのボタンを押した時に呼び出される関数.セルをクリックした時にそのセルをどの状態に変えるかを変更する.
	public void setTileToAdd(Tile tile) {
		this.tile = tile;
	}

	//Ant Countのスライダーを動かした時に呼び出される関数.蟻の数の最大値を変更する.
	public void setMaxAnts(int maxAnts) {
		this.maxAnts = maxAnts;
		while(ants.size() > maxAnts){
			//削除する警備員が自転車整理中である場合,自転車整理を終える.
			if(cellArray[ants.get(0).getCol()][ants.get(0).getRow()].isSet())cellArray[ants.get(0).getCol()][ants.get(0).getRow()].endSet();
			ants.remove(0);
		}
	}
}
