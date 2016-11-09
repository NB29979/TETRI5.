package tet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class tet extends JFrame {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new tet();
			}
		});
	}

	private JPanel subPanelNorth, subPanelEast, subPanelSouth, subPanelWest;

	tet() {
		setTitle("TETRI5.");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(466, 658);
		setLocationRelativeTo(null);
		setPanel();
		setVisible(true);
		setResizable(false
				);
	}
	void setPanel() {
		setLayout(new BorderLayout());
		subPanelNorth = new JPanel();
		subPanelNorth.setBackground(Color.GRAY);
		subPanelNorth.setPreferredSize(new Dimension(0, 30));
		subPanelNorth.setLayout(new GridLayout(1, 2));
		
		subPanelEast = new JPanel();
		subPanelEast.setBackground(Color.GRAY);
		subPanelEast.setPreferredSize(new Dimension(22,0));//238で程よい大きさ
		subPanelSouth = new JPanel();
		subPanelSouth.setBackground(Color.GRAY);
		subPanelSouth.setPreferredSize(new Dimension(0, 22));
		subPanelWest = new JPanel();
		subPanelWest.setBackground(Color.GRAY);
		subPanelWest.setPreferredSize(new Dimension(22, 0));
		add(subPanelNorth, BorderLayout.NORTH);
		add(subPanelEast, BorderLayout.EAST);
		add(subPanelSouth, BorderLayout.SOUTH);
		add(subPanelWest, BorderLayout.WEST);
		add(new MainPanel(), BorderLayout.CENTER);
		}
}
