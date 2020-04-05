/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.gui.MainWindow;
import com.asofterspace.toolbox.Utils;

import java.awt.BorderLayout;
import java.awt.CardLayout;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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

	private PlayerCtrl playerCtrl;
	private SongCtrl songCtrl;

	private Song currentlyPlayedSong;

	private JPanel mainPanelRight;

	private JPanel searchPanel;
	private JTextField searchField;

	private JMenuItem close;

	private ConfigFile configuration;
	private JList<String> songListComponent;
	private JPopupMenu songListPopup;
	private String[] strSongs;
	private JScrollPane songListScroller;


	public GUI(PlayerCtrl playerCtrl, SongCtrl songCtrl, ConfigFile config) {

		this.playerCtrl = playerCtrl;

		this.songCtrl = songCtrl;

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

		regenerateSongList();
	}

	private JMenuBar createMenu(JFrame parent) {

		JMenuBar menu = new JMenuBar();

		JMenu file = new JMenu("File");
		menu.add(file);

		/*
		JMenuItem newFile = new JMenuItem("New File");
		newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		newFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				newFile();
			}
		});
		file.add(newFile);
		*/

		// file.addSeparator();

		close = new JMenuItem("Exit");
		close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		file.add(close);

		JMenu huh = new JMenu("?");

		JMenuItem openConfigPath = new JMenuItem("Open Config Path");
		openConfigPath.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().open(configuration.getParentDirectory().getJavaFile());
				} catch (IOException ex) {
					// do nothing
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
		mainPanelRight.setLayout(new CardLayout());
		mainPanelRight.setPreferredSize(new Dimension(8, 8));

		JPanel gapPanel = new JPanel();
		gapPanel.setPreferredSize(new Dimension(8, 8));

		String[] songList = new String[0];
		songListComponent = new JList<String>(songList);

		/*
		songListComponent.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				showSelectedTab();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				showPopupAndSelectedTab(e);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				showPopupAndSelectedTab(e);
			}

			private void showPopupAndSelectedTab(MouseEvent e) {
				if (e.isPopupTrigger()) {
					songListComponent.setSelectedIndex(songListComponent.locationToIndex(e.getPoint()));
					songListPopup.show(songListComponent, e.getX(), e.getY());
				}

				showSelectedTab();
			}
		});

		songListComponent.addKeyListener(new KeyListener() {

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
		*/

		songListScroller = new JScrollPane(songListComponent);
		songListScroller.setPreferredSize(new Dimension(8, 8));
		songListScroller.setBorder(BorderFactory.createEmptyBorder());

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

				// TODO :: actually search for the song ;)
			}
		});

		// listen to the enter key being pressed (which does not create text updates)
		searchField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String searchFor = searchField.getText();

				// TODO :: actually search for the song ;)
			}
		});

		searchPanel.add(searchField, new Arrangement(0, 0, 1.0, 1.0));

		mainPanelRightOuter.add(mainPanelRight, new Arrangement(0, 0, 1.0, 1.0));

		mainPanelRightOuter.add(searchPanel, new Arrangement(0, 1, 1.0, 0.0));

		mainPanel.add(songListScroller, new Arrangement(0, 0, 0.2, 1.0));

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
	 * Regenerate the file list on the left hand side based on the songCtrl.getSongs() list,
	 * and (if at least one file exists), select and open the current tab or, if it
	 * is null, the lastly added one
	 */
	public void regenerateSongList() {

		// if there is no last shown tab...
		if (currentlyPlayedSong == null) {
			// ... show some random tab explicitly - this is fun, and the tabbed layout otherwise shows it anyway, so may as well...
			if (songCtrl.getSongs().size() > 0) {
				setCurrentlyPlayedSong(songCtrl.getSongs().get(0));
			}
		}

		/*
		Collections.sort(songCtrl.getSongs(), new Comparator<Song>() {
			public int compare(Song a, Song b) {
				return a.getFilePath().toLowerCase().compareTo(b.getFilePath().toLowerCase());
			}
		});
		*/

		strSongs = new String[songCtrl.getSongs().size()];

		int i = 0;

		for (Song song : songCtrl.getSongs()) {
			strSongs[i] = song.toString();
			if (song.equals(currentlyPlayedSong)) {
				strSongs[i] = ">> " + strSongs[i] + " <<";
			}
			i++;
		}

		songListComponent.setListData(strSongs);

		// if there still is no last shown tab (e.g. we just deleted the very last one)...
		if (currentlyPlayedSong == null) {
			// ... then we do not need to show or highlight any ;)
			return;
		}

		/*
		// show the last shown tab
		showTab(currentlyPlayedSong);
		*/

		highlightTabInLeftListOrTree(currentlyPlayedSong);
	}

	public void highlightTabInLeftListOrTree(final Song song) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// highlight tab the list
				int i = 0;
				for (Song cur : songCtrl.getSongs()) {
					if (song.equals(cur)) {
						songListComponent.setSelectedIndex(i);
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

	private void setCurrentlyPlayedSong(Song song) {
		currentlyPlayedSong = song;
	}

}
