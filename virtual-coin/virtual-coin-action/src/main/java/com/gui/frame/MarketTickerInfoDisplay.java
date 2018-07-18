package com.gui.frame;

import java.awt.Dimension;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;

import com.gui.tableModel.EinsteinTableModel;
import com.market.task.PlatformTickerTask;

public class MarketTickerInfoDisplay extends JFrame {
	private PlatformTickerTask platformTickerTask;
	private SwingWorker dataRefreshTask;
	private JPanel mPanel;
	private JScrollPane mScrollPane;
	private JTable mTable;
	private EinsteinTableModel mTableModel;

	public MarketTickerInfoDisplay() {

	}

	public MarketTickerInfoDisplay(PlatformTickerTask platformTickerTask) {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.platformTickerTask = platformTickerTask;
	}

	public void createAndShow() {
		mTableModel = new EinsteinTableModel(platformTickerTask);
		mTable = new JTable();

		mTable.setModel(mTableModel);
		mTable.setPreferredScrollableViewportSize(new Dimension(800, 600));
		mTable.setFillsViewportHeight(true);

		mScrollPane = new JScrollPane();
		mScrollPane.setViewportView(mTable);

		mPanel = new JPanel();
		mPanel.add(mScrollPane);

		mPanel.setOpaque(true);

		dataRefreshTask = new SwingWorker<Vector<Vector<Object>>, Vector<Vector<Object>>>() {

			@Override
			protected Vector<Vector<Object>> doInBackground() throws Exception {
				Vector<Vector<Object>> data = null;				
				do {	
					Thread.sleep(10*1000);// 10秒
					data = mTableModel.fetchEinsteinModelData();
					System.out.println("doInBackground, get data.size = "+data.size());
					publish(data);					
				} while (!isDone());
				return data;
			}

			@Override
			protected void process(List<Vector<Vector<Object>>> list) {
				Vector<Vector<Object>> mData = list.get(list.size() - 1);
				/*if(null==mData) {
					mData = new Vector<Vector<Object>>();
				}*/
				mTableModel.setData(mData);
				mTable.setModel(mTableModel);
				mTable.validate();
				mTable.updateUI();
			}

			@Override
			protected void done() {
				System.out.println("dataRefreshTask is done");
			}

		};

		this.getContentPane().add(mPanel);
		this.pack();
		this.setVisible(true);

		this.dataRefreshTask.execute();
	}

}
