/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.accountant.entries.Outgoing;
import com.asofterspace.accountant.tabs.MonthTab;
import com.asofterspace.accountant.tabs.Tab;
import com.asofterspace.accountant.tabs.TimeSpanTab;
import com.asofterspace.accountant.tabs.YearTab;
import com.asofterspace.accountant.timespans.Month;
import com.asofterspace.accountant.timespans.Year;
import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.gui.MainWindow;
import com.asofterspace.toolbox.gui.MenuItemForMainMenu;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.SimpleFile;
import com.asofterspace.toolbox.utils.StrUtils;
import com.asofterspace.toolbox.Utils;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;


public class GUI extends MainWindow {

	private final static String CONFIG_KEY_WIDTH = "mainFrameWidth";
	private final static String CONFIG_KEY_HEIGHT = "mainFrameHeight";
	private final static String CONFIG_KEY_LEFT = "mainFrameLeft";
	private final static String CONFIG_KEY_TOP = "mainFrameTop";
	private final static String CONFIG_KEY_INVOICE_LOCATION_ON_DISK = "invoiceLocationOnDisk";

	private Database database;
	private TabCtrl tabCtrl;

	private Tab currentlyOpenedTab;

	private JPanel mainPanelRight;
	private JScrollPane mainPanelRightScroller;
	private JPanel emptyTab;

	private JPanel searchPanel;
	private JTextField searchField;

	private ConfigFile configuration;
	private JList<String> tabListComponent;
	private JPopupMenu tabListPopup;
	private List<Tab> tabs;
	private String[] strTabs;
	private JScrollPane tabListScroller;

	private NewYearGUI newYearGUI;
	private AddEntryGUI addEntryGUI;


	public GUI(Database database, TabCtrl tabCtrl, ConfigFile config) {

		this.database = database;
		database.setGUI(this);

		this.tabCtrl = tabCtrl;

		this.configuration = config;
	}

	@Override
	public void run() {

		super.create();

		refreshTitleBar();

		createMenu(mainFrame);

		createMainPanel(mainFrame);

		// do not call super.show, as we are doing things a little bit
		// differently around here (including restoring from previous
		// position...)
		// super.show();

		final Integer lastWidth = configuration.getInteger(CONFIG_KEY_WIDTH, -1);
		final Integer lastHeight = configuration.getInteger(CONFIG_KEY_HEIGHT, -1);
		final Integer lastLeft = configuration.getInteger(CONFIG_KEY_LEFT, -1);
		final Integer lastTop = configuration.getInteger(CONFIG_KEY_TOP, -1);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// Stage everything to be shown
				mainFrame.pack();

				// Actually display the whole jazz
				mainFrame.setVisible(true);

				if ((lastWidth < 1) || (lastHeight < 1)) {
					GuiUtils.maximizeWindow(mainFrame);
				} else {
					mainFrame.setSize(lastWidth, lastHeight);

					mainFrame.setPreferredSize(new Dimension(lastWidth, lastHeight));

					mainFrame.setLocation(new Point(lastLeft, lastTop));
				}

				mainFrame.addComponentListener(new ComponentAdapter() {
					public void componentResized(ComponentEvent componentEvent) {
						configuration.set(CONFIG_KEY_WIDTH, mainFrame.getWidth());
						configuration.set(CONFIG_KEY_HEIGHT, mainFrame.getHeight());
					}

					public void componentMoved(ComponentEvent componentEvent) {
						configuration.set(CONFIG_KEY_LEFT, mainFrame.getLocation().x);
						configuration.set(CONFIG_KEY_TOP, mainFrame.getLocation().y);
					}
				});
			}
		});

		regenerateTabList();
		highlightTabInLeftListOrTree(tabCtrl.getOverviewTab());
		showTab(tabCtrl.getOverviewTab());
	}

	private JMenuBar createMenu(JFrame parent) {

		JMenuBar menu = new JMenuBar();

		JMenu file = new JMenu("File");
		menu.add(file);

		JMenuItem addYear = new JMenuItem("Add Year");
		addYear.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		addYear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// show the new year GUI
				if (newYearGUI == null) {
					newYearGUI = new NewYearGUI(GUI.this, database);
				}
				newYearGUI.show();
			}
		});
		file.add(addYear);

		file.addSeparator();

		JMenuItem bulkImportIn = new JMenuItem("Bulk Import Incoming TSV");
		bulkImportIn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				bulkImportIncomings();
			}
		});
		file.add(bulkImportIn);

		JMenuItem bulkImportOut = new JMenuItem("Bulk Import Outgoing TSV");
		bulkImportOut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				bulkImportOutgoings();
			}
		});
		file.add(bulkImportOut);

		file.addSeparator();

		JMenuItem dropEntireDatabase = new JMenuItem("Drop Entire Database");
		dropEntireDatabase.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO :: guard this with an extra popup!
				database.drop();
			}
		});
		file.add(dropEntireDatabase);

		file.addSeparator();

		JMenuItem save = new JMenuItem("Save");
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				database.save();
			}
		});
		file.add(save);

		file.addSeparator();

		JMenuItem close = new JMenuItem("Exit");
		close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Accountant out. Have a fun day! :)");
				System.exit(0);
			}
		});
		file.add(close);

		JMenu edit = new JMenu("Edit");
		menu.add(edit);

		JMenuItem undo = new JMenuItem("Undo");
		undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
		undo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				database.undo();
			}
		});
		edit.add(undo);

		JMenuItem redo = new JMenuItem("Redo");
		redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
		redo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				database.redo();
			}
		});
		edit.add(redo);

		AbstractButton addEntry = new MenuItemForMainMenu("Add Entry");
		addEntry.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// show the add entry GUI
				if (addEntryGUI == null) {
					addEntryGUI = new AddEntryGUI(GUI.this, database);
				}
				addEntryGUI.show();
			}
		});
		menu.add(addEntry);

		// open the invoice file location on disk
		AbstractButton openOnDisk = new MenuItemForMainMenu("Open on Disk");
		openOnDisk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String diskLocation = configuration.getValue(CONFIG_KEY_INVOICE_LOCATION_ON_DISK);
				if (diskLocation == null) {
					AccountingUtils.complain("Sorry, the key " +
						CONFIG_KEY_INVOICE_LOCATION_ON_DISK + " in the configuration file " +
						configuration.getAbsoluteFilename() + " has not been set!");
					return;
				}
				if (currentlyOpenedTab != null) {
					// go directly to the current year
					if (currentlyOpenedTab instanceof TimeSpanTab) {
						diskLocation += "/" + ((TimeSpanTab) currentlyOpenedTab).getYear().getNum();
					}
					// go directly to the current month
					if (currentlyOpenedTab instanceof MonthTab) {
						Month curMonth = ((MonthTab) currentlyOpenedTab).getMonth();
						diskLocation += "/" + StrUtils.leftPad0(curMonth.getNum() + 1, 2) + " " +
							curMonth.getMonthName().toLowerCase();
					}
				}
				Directory diskLocationFile = new Directory(diskLocation);
				try {
					Desktop.getDesktop().open(diskLocationFile.getJavaFile());
				} catch (IOException ex) {
					AccountingUtils.complain("Sorry, the folder " +
						diskLocationFile.getAbsoluteDirname() + " could not be opened!");
				}
			}
		});
		menu.add(openOnDisk);

		JMenu huh = new JMenu("?");

		JMenuItem openConfigPath = new JMenuItem("Open Config Path");
		openConfigPath.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().open(configuration.getParentDirectory().getJavaFile());
				} catch (IOException ex) {
					AccountingUtils.complain("Sorry, the folder " +
						configuration.getParentDirectory().getAbsoluteDirname() + " could not be opened!");
				}
			}
		});
		huh.add(openConfigPath);

		JMenuItem about = new JMenuItem("About");
		about.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String aboutMessage = "This is the " + Main.PROGRAM_TITLE + ".\n" +
					"Version: " + Main.VERSION_NUMBER + " (" + Main.VERSION_DATE + ")\n" +
					"Brought to you by: A Softer Space";
				JOptionPane.showMessageDialog(mainFrame, aboutMessage, "About", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		huh.add(about);
		menu.add(huh);

		parent.setJMenuBar(menu);

		return menu;
	}

	private JPanel createMainPanel(JFrame parent) {

		JPanel mainPanel = new JPanel();
		mainPanel.setPreferredSize(new Dimension(800, 500));
		GridBagLayout mainPanelLayout = new GridBagLayout();
		mainPanel.setLayout(mainPanelLayout);

		JPanel mainPanelRightOuter = new JPanel();
		GridBagLayout mainPanelRightOuterLayout = new GridBagLayout();
		mainPanelRightOuter.setLayout(mainPanelRightOuterLayout);

		mainPanelRight = new JPanel();
		mainPanelRight.setPreferredSize(new Dimension(100, 100));

		mainPanelRightScroller = new JScrollPane(mainPanelRight,
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		mainPanelRightScroller.setPreferredSize(new Dimension(8, 8));
		mainPanelRightScroller.setBorder(BorderFactory.createEmptyBorder());

		JPanel gapPanel = new JPanel();
		gapPanel.setPreferredSize(new Dimension(8, 8));

		String[] tabList = new String[0];
		tabListComponent = new JList<String>(tabList);

		tabListComponent.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				showSelectedTab();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				showSelectedTab();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				showSelectedTab();
			}
		});

		tabListComponent.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				switch (e.getKeyCode()) {
					case KeyEvent.VK_UP:
					case KeyEvent.VK_DOWN:
						showSelectedTab();
						break;
				}
			}
		});

		tabListScroller = new JScrollPane(tabListComponent);
		tabListScroller.setPreferredSize(new Dimension(8, 8));
		tabListScroller.setBorder(BorderFactory.createEmptyBorder());

		searchPanel = new JPanel();
		searchPanel.setLayout(new GridBagLayout());
		searchPanel.setVisible(false);

		searchField = new JTextField();

		// listen to text updates
		searchField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				search();
			}
			public void removeUpdate(DocumentEvent e) {
				search();
			}
			public void insertUpdate(DocumentEvent e) {
				search();
			}
			private void search() {
				String searchFor = searchField.getText();

				// TODO :: actually search for the year ;)
			}
		});

		// listen to the enter key being pressed (which does not create text updates)
		searchField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String searchFor = searchField.getText();

				// TODO :: actually search for the year ;)
			}
		});

		searchPanel.add(searchField, new Arrangement(0, 0, 1.0, 1.0));

		mainPanelRightOuter.add(mainPanelRightScroller, new Arrangement(0, 0, 1.0, 1.0));

		mainPanelRightOuter.add(searchPanel, new Arrangement(0, 1, 1.0, 0.0));

		mainPanel.add(tabListScroller, new Arrangement(0, 0, 0.2, 1.0));

		mainPanel.add(gapPanel, new Arrangement(2, 0, 0.0, 0.0));

		mainPanel.add(mainPanelRightOuter, new Arrangement(3, 0, 1.0, 1.0));

		parent.add(mainPanel, BorderLayout.CENTER);

		return mainPanel;
	}

	private void showSearchBar() {

		searchPanel.setVisible(true);

		searchField.requestFocus();
	}

	/**
	 * Regenerate the list on the left hand side based on the years and their months coming
	 * from the database
	 */
	public void regenerateTabList() {

		tabs = tabCtrl.getTabs();

		ArrayList<String> strTabList = new ArrayList<>();

		for (Tab tab : tabs) {
			if ((tab instanceof YearTab) && (strTabList.size() > 0)) {
				strTabList.add(" ");
			}
			if (tab.equals(currentlyOpenedTab)) {
				strTabList.add(">> " + tab.toString() + " <<");
			} else {
				strTabList.add(tab.toString());
			}
		}

		strTabs = StrUtils.strListToArray(strTabList);

		tabListComponent.setListData(strTabs);

		// if there still is no last shown tab (e.g. we just deleted the very last one)...
		if (currentlyOpenedTab == null) {
			// ... then we do not need to show or highlight any ;)
			return;
		}

		highlightTabInLeftListOrTree(currentlyOpenedTab);
	}

	public void highlightTabInLeftListOrTree(final Tab tab) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// highlight tab the list
				int i = 0;
				for (String strTab : strTabs) {
					if (strTab.equals(tab.toString()) || strTab.equals(">> " + tab.toString() + " <<")) {
						tabListComponent.setSelectedIndex(i);
						break;
					}
					i++;
				}
			}
		});
	}

	private void refreshTitleBar() {
		mainFrame.setTitle(Main.PROGRAM_TITLE);
	}

	/**
	 * To show a tab, or to set the currentlyOpenedTab, ALWAYS call this function,
	 * NEVER set currentlyOpenedTab directly - as it is handled in here, and this
	 * here also destroys the previous tabs!
	 */
	public void showTab(Tab tab) {

		// if we switch from one tab to itself, ignore the switch :)
		if (currentlyOpenedTab != null) {
			if (tab != null) {
				if (tab.equals(currentlyOpenedTab)) {
					return;
				}
			}
		}

		// remove previous tab
		if (currentlyOpenedTab != null) {
			currentlyOpenedTab.destroyTabOnGUI(mainPanelRight);
		} else {
			if (emptyTab != null) {
				mainPanelRight.remove(emptyTab);
			}
		}

		currentlyOpenedTab = tab;

		// open new tab
		if (currentlyOpenedTab != null) {
			currentlyOpenedTab.createTabOnGUI(mainPanelRight, database);
		} else {
			emptyTab = new JPanel();
			mainPanelRight.add(emptyTab);
		}

		mainPanelRight.revalidate();
		mainPanelRight.repaint();
		mainPanelRightScroller.revalidate();
		mainPanelRightScroller.repaint();
	}

	private void showSelectedTab() {
		String strTab = strTabs[tabListComponent.getSelectedIndex()];
		for (Tab tab : tabs) {
			if (strTab.equals(tab.toString()) || strTab.equals(">> " + tab.toString() + " <<")) {
				showTab(tab);
				return;
			}
		}
		showTab(null);
	}

	/**
	 * Refreshes the content of the currently open tab
	 */
	public void refreshOpenTab() {
		if (currentlyOpenedTab != null) {
			currentlyOpenedTab.destroyTabOnGUI(mainPanelRight);
			currentlyOpenedTab.createTabOnGUI(mainPanelRight, database);
			mainPanelRight.revalidate();
			mainPanelRight.repaint();
			mainPanelRightScroller.revalidate();
			mainPanelRightScroller.repaint();
		}
	}

	private void bulkImportIncomings() {

		// TODO :: actually, write our own file chooser
		JFileChooser importFilePicker = new JFileChooser(System.getProperty("java.class.path") + "/..");

		importFilePicker.setDialogTitle("Open a File to Bulk Import Incoming Invoices");
		importFilePicker.setMultiSelectionEnabled(true);

		int result = importFilePicker.showOpenDialog(mainFrame);

		switch (result) {

			case JFileChooser.APPROVE_OPTION:

				// load the files
				for (java.io.File curFile : importFilePicker.getSelectedFiles()) {
					database.bulkImportIncomings(new SimpleFile(curFile));
				}

				break;

			case JFileChooser.CANCEL_OPTION:
				// cancel was pressed... do nothing!
				break;
		}
	}

	private void bulkImportOutgoings() {

		// TODO :: actually, write our own file chooser
		JFileChooser importFilePicker = new JFileChooser(System.getProperty("java.class.path") + "/..");

		importFilePicker.setDialogTitle("Open a File to Bulk Import Outgoing Invoices");
		importFilePicker.setMultiSelectionEnabled(true);

		int result = importFilePicker.showOpenDialog(mainFrame);

		switch (result) {

			case JFileChooser.APPROVE_OPTION:

				// load the files
				for (java.io.File curFile : importFilePicker.getSelectedFiles()) {
					database.bulkImportOutgoings(new SimpleFile(curFile));
				}

				break;

			case JFileChooser.CANCEL_OPTION:
				// cancel was pressed... do nothing!
				break;
		}
	}

	public static Dimension getDefaultDimensionForInvoiceLine() {
		int defaultSize = 20;
		return new Dimension(defaultSize, defaultSize);
	}

}
