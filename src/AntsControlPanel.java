import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * Controller of the Ants class.
 */
public class AntsControlPanel {

	private Ants ants;
	private JPanel panel = new JPanel();
	private JButton timerButton = new JButton("\u25BA");
	private final AdvancedControlPanel advancedPanel;
	
	private Timer stepTimer = new Timer(0, new ActionListener(){
		public void actionPerformed(ActionEvent e){
			step();
		}
	});

	public AntsControlPanel(final Ants ants, final AdvancedControlPanel advancedPanel){
		this.ants = ants;
		this.advancedPanel = advancedPanel;

		Dimension controlDimension = new Dimension(75, 25);

		timerButton.setMinimumSize(controlDimension);
		timerButton.setMaximumSize(controlDimension);
		timerButton.setPreferredSize(controlDimension);
		timerButton.setFocusable(false);
		timerButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		timerButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(stepTimer.isRunning()){
					pause();
				}
				else{
					start();
				}
			}
		});

		final JButton stepButton = new JButton("Step");
		stepButton.setMinimumSize(controlDimension);
		stepButton.setMaximumSize(controlDimension);
		stepButton.setPreferredSize(controlDimension);
		stepButton.setFocusable(false);
		stepButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		stepButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				pause();
				step();
			}
		});

		
		JLabel antsLabel = new JLabel("Ant Count:");
		antsLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);

		//警備員の人数を入力するテキストボックス
		final JTextField antsTextField = new JTextField("1");
		//テキストボックスでenterが押された時の動作
		antsTextField.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				//警備員の人数を変更する.
				ants.setMaxAnts(Integer.parseInt(antsTextField.getText()));
			}
		});
		//初期値は1
		ants.setMaxAnts(1);
		
		
		JPanel blockPanel = new JPanel();
		blockPanel.setLayout(new BoxLayout(blockPanel, BoxLayout.Y_AXIS));
		blockPanel.setBorder(BorderFactory.createTitledBorder("Place Tile"));
		
		
		JRadioButton obstacle = new JRadioButton("Obstacle");
		obstacle.setFocusable(false);
		obstacle.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				ants.setTileToAdd(Ants.Tile.OBSTACLE);
			}
		});
		JRadioButton nest = new JRadioButton("Nest");
		nest.setFocusable(false);
		nest.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				ants.setTileToAdd(Ants.Tile.NEST);
			}
		});
		JRadioButton goal = new JRadioButton("Food");
		goal.setFocusable(false);
		goal.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				ants.setTileToAdd(Ants.Tile.GOAL);
			}
		});
		JRadioButton clear = new JRadioButton("Clear");
		clear.setFocusable(false);
		clear.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				ants.setTileToAdd(Ants.Tile.CLEAR);
			}
		});
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(obstacle);
		bg.add(nest);
		bg.add(goal);
		bg.add(clear);
		goal.setSelected(true);
		
		blockPanel.add(obstacle);
		blockPanel.add(nest);
		blockPanel.add(goal);
		blockPanel.add(clear);
		
		blockPanel.setAlignmentX(JComponent.CENTER_ALIGNMENT);

		JPanel foodRequiredPanel = new JPanel();
		foodRequiredPanel.setLayout(new BoxLayout(foodRequiredPanel, BoxLayout.Y_AXIS));
		foodRequiredPanel.setBorder(BorderFactory.createTitledBorder("Food Needed"));
		foodRequiredPanel.setAlignmentX(JComponent.CENTER_ALIGNMENT);

		final JCheckBox showAdvanced = new JCheckBox("Advanced");
		showAdvanced.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		showAdvanced.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				advancedPanel.getPanel().setVisible(showAdvanced.isSelected());
			}
			
		});
		advancedPanel.getPanel().setVisible(showAdvanced.isSelected());
		
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(timerButton);
		panel.add(stepButton);
		panel.add(Box.createGlue());
		panel.add(antsLabel);
		panel.add(antsTextField);
		panel.add(Box.createGlue());
		panel.add(blockPanel);
		panel.add(Box.createGlue());
		panel.add(Box.createGlue());
		panel.add(foodRequiredPanel);
		panel.add(Box.createGlue());
		panel.add(Box.createGlue());
		panel.add(showAdvanced);
	}

	public void start(){
		stepTimer.restart();
		timerButton.setText("Pause");
	}

	public void pause(){
		stepTimer.stop();
		timerButton.setText("\u25BA");
	}

	public void step(){
		ants.step();
		advancedPanel.step();
	}

	public JPanel getPanel(){
		return panel;
	}
}
