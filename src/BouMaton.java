/* ###################################################################
 * 
 *  FILE: "BouMaton.java"
 *                                    created: 2002-07-31 15:36:33 
 *                                last update: 2010-04-23 19:57:10 
 *  Author: Frédéric Boulanger
 *  E-mail: frederic.boulanger@centralesupelec.fr
 *    mail: CentraleSupélec -- Département Informatique
 *          Plateau de Moulon, 3 rue Joliot-Curie, F-91912 Gif-sur-Yvette cedex
 *     www: http://wdi.supelec.fr/boulanger/
 *  
 *  Description: 
 * 
 *  History
 * 
 *  modified   by  rev reason
 *  ---------- --- --- -----------
 *  2002-07-31 FBO 1.0 original
 *  2002-10-20 FBO 1.1 fix window update bug
 *  2002-11-24 FBO 1.2 handle low mem conditions + JPEG quality setting
 *  2003-01-10 FBO 1.3 optimize boulanger period computation
 *  2006-02-08 FBO 1.4 use javax.imageio, allow save as PNG
 *  2006-02-10 FBO 1.5 fix TYPE_CUSTOM in PNG BufferedImage.
 *                     Better handling of out of memory conditions
 *  2019-04-23 FBO 2.0 Updated to use java.awt.Desktop for OS integration
 *  2019-04-24 FBO 2.0 Removed dependencies to java.awt.Desktop (did not work on MacOS)
 * ###################################################################
 */

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.io.BufferedReader;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
//import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.ItemSelectable;
import java.awt.Label;
import java.awt.LayoutManager;
import java.awt.image.BufferedImage;
import java.awt.Scrollbar;
import java.awt.SystemColor;
import java.awt.TextArea;
import java.awt.TextField;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.Locale;

import java.math.BigInteger;
import java.text.DecimalFormat;

@SuppressWarnings("serial")
public class BouMaton extends Frame implements ActionListener {
	static protected AboutBox aboutBox = null;
	static final protected int macOSXMenuBarHeight = 22;
	protected MenuBar mainMenuBar = new MenuBar();
	protected static ResourceBundle sStrings_ = null;

	private boolean aboutIsHandled = false;
	
	protected Menu fileMenu;
	protected MenuItem miAbout;
	protected MenuItem miOpen;
	protected MenuItem miSave;
	protected MenuItem miRevert;
	protected MenuItem miClose;
	protected MenuItem miQuit;

	protected Button bouButton_;
	protected Button phoButton_;
	protected TextField iterations_;
	protected Label dimLabel_;

	protected Menu transMenu;
	protected MenuItem miBoulange_;
	protected MenuItem miMaton_;
	protected MenuItem miStop_;
	protected MenuItem miInfo_;
	protected MenuItem miHist_;

	protected Toolkit toolkit_;

	protected File file_ = null;
	protected DisplayPict pict_;

	protected long bouPeriod_;
	protected BigInteger bouBigPeriod_;
	protected long[][] bouCycles_;
	protected long phoPeriod_;
	protected BigInteger phoBigPeriod_;
	protected long[][] phoCycles_;
	protected HashMap<Long, Long> bouFreq_;
	protected HashMap<Long, Long> phoFreq_;

	protected ThreadGroup subTasks_;
	protected ThreadGroup periodTasks_;
	protected ThreadGroup transTasks_;

	protected DisplayStat statWin_;
	protected History histWin_;

	int width_;
	int height_;

	protected Panel ctrl_;

	protected Rectangle screenBounds_;

	protected boolean stopThreads_;
	protected boolean interrupt_;

	protected ProgressBar bouProgress_;
	protected ProgressBar phoProgress_;

	protected ProgressLabel bouPeriodProgress_;
	protected ProgressLabel phoPeriodProgress_;

	protected float jpegQuality_ = 0.9F;
	protected String imageFormat_ = "jpg";

	public static String version() {
		return "2.0";
	}

	public static String release() {
		return "2019-04-23";
	}
	
	private boolean checkDesktopSupport() {
		// Try to get a DesktopManager
		Class<?> desk = null;
		Object deskman = null;
		try {
			// Firstly, try the DesktopManager that relies on JDK 9 Desktop class
			desk = Class.forName("DesktopManager");
			deskman = desk.getDeclaredConstructor(BouMaton.class).newInstance(this);
		} catch (Throwable e) {
			try {
				// Next, try the DesktopManager that relies on Apple eawt stuff
				desk = Class.forName("DesktopManagerApple");
				deskman = desk.getDeclaredConstructor(BouMaton.class).newInstance(this);
			} catch (Throwable e1) {
				// Nothing else to try
				return false;
			}
		}
		// Creating the DesktopManager registered it to handle: About, OpenFiles and Quit
		// We try to know if it could register for handling About
		Object handleAbout = null;
		try {
			handleAbout = desk.getMethod("getHandleAbout").invoke(deskman);
		} catch (IllegalAccessException
			   | IllegalArgumentException
			   | InvocationTargetException
			   | NoSuchMethodException
			   | SecurityException e) {
			return false;
		}
		// If we got a result and it is true, About is handled.
		return (handleAbout != null && handleAbout.equals(true));
	}
		
	public void addFileMenuItems() {
		fileMenu = new Menu(getString("FileMenu"));
		miAbout = new MenuItem (getString("AboutMenu"));
		if (! aboutIsHandled) {
			// Add our own About menu item if it is not handled by java.awt.Desktop
			fileMenu.add(miAbout);
			miAbout.addActionListener(this);
		}
		miOpen = new MenuItem (getString("OpenItem"));
		miOpen.setShortcut(new MenuShortcut(KeyEvent.VK_O, false));
		fileMenu.add(miOpen).setEnabled(true);
		miOpen.addActionListener(this);

		miSave = new MenuItem (getString("SaveItem"));
		miSave.setShortcut(new MenuShortcut(KeyEvent.VK_S, false));
		fileMenu.add(miSave).setEnabled(false);
		miSave.addActionListener(this);

		miRevert = new MenuItem (getString("RevertItem"));
		miRevert.setShortcut(new MenuShortcut(KeyEvent.VK_R, false));
		fileMenu.add(miRevert).setEnabled(false);
		miRevert.addActionListener(this);

		miClose = new MenuItem (getString("CloseItem"));
		miClose.setShortcut(new MenuShortcut(KeyEvent.VK_W, false));
		fileMenu.add(miClose).setEnabled(false);
		miClose.addActionListener(this);

		miQuit = new MenuItem (getString("QuitItem"));

		mainMenuBar.add(fileMenu);
	}

	public void addTransMenuItems() {
		transMenu = new Menu(getString("TransformMenu"));

		miBoulange_ = new MenuItem (getString("BoulangerItem"));
		transMenu.add(miBoulange_).setEnabled(false);
		miBoulange_.addActionListener(this);

		miMaton_ = new MenuItem (getString("PhotoMatonItem"));
		transMenu.add(miMaton_).setEnabled(false);
		miMaton_.addActionListener(this);

		miStop_ = new MenuItem (getString("InterruptItem"));
		transMenu.add(miStop_).setEnabled(false);
		miStop_.addActionListener(this);

		miInfo_ = new MenuItem(getString("ShowInfoItem"));
		transMenu.add(miInfo_).setEnabled(false);
		miInfo_.addActionListener(this);

		miHist_ = new MenuItem(getString("ShowHistoryItem"));
		transMenu.add(miHist_).setEnabled(false);
		miHist_.addActionListener(this);

		mainMenuBar.add(transMenu);
	}

	public void addMenus() {
		addFileMenuItems();
		addTransMenuItems();
		setMenuBar (mainMenuBar);
	}

	protected static String getString(String key) {
		if (sStrings_ != null) {
			return sStrings_.getString(key);
		} else {
			return "*<NoString>*"; 
		}
	}

	public BouMaton() {
		super("BouMaton");

		sStrings_ = ResourceBundle.getBundle("BouMatonStrings",
				Locale.getDefault());
		if (sStrings_ == null) {
			new ErrorDialog(this, "Could not find my string resources. Aborting.");
			System.exit(1);
		}
		
		aboutIsHandled = checkDesktopSupport();
		
		aboutBox = new AboutBox();

		bouPeriod_ = -1;
		bouBigPeriod_ = null;
		phoPeriod_ = -1;
		phoBigPeriod_ = null;
		phoFreq_ = null;
		bouFreq_ = null;
		width_ = -1;
		height_ = -1;

		subTasks_ = new ThreadGroup("subtasks");
		periodTasks_ = new ThreadGroup(subTasks_, "periodtasks");
		transTasks_ = new ThreadGroup(subTasks_, "transtasks");

		stopThreads_ = true;

		screenBounds_ = getGraphicsConfiguration().getBounds();
		// Adjust for menu bar on MacOS X
		screenBounds_.y += macOSXMenuBarHeight;
		screenBounds_.height -= macOSXMenuBarHeight;

		addWindowListener(new BouMatonCloser(this));
		addComponentListener(new BouMatonResizer(this));

		setLayout(null);
		
		addMenus();
		
		pict_ = new DisplayPict();
		add(pict_);

		ctrl_ = new Panel(new BorderLayout());
		add(ctrl_);

		Panel bouBox = new Panel(new GridLayout(0,1));
		bouPeriodProgress_ = new ProgressLabel(getString("NoPictureStr"), Label.CENTER);

		bouBox.add(bouPeriodProgress_);
		bouButton_ = new Button(getString("BoulangerStr"));
		bouBox.add(bouButton_);
		bouProgress_ = new ProgressBar();
		bouBox.add(bouProgress_);
		ctrl_.add(bouBox, BorderLayout.WEST);
		bouProgress_.stop();

		Panel infoBox = new Panel(new GridLayout(0, 1));
		dimLabel_ = new Label("", Label.CENTER);
		infoBox.add(dimLabel_);
		iterations_ = new TextField("1", 5);
		Panel elastoc = new Panel();
		elastoc.add(iterations_);
		infoBox.add(elastoc);
		infoBox.add(new Label("", Label.CENTER)); // filler
		ctrl_.add(infoBox, BorderLayout.CENTER);

		Panel phoBox = new Panel(new GridLayout(0,1));
		phoPeriodProgress_ = new ProgressLabel(getString("NoPictureStr"), Label.CENTER);
		phoBox.add(phoPeriodProgress_);
		phoButton_ = new Button(getString("PhotoMatonStr"));
		phoBox.add(phoButton_);
		phoProgress_ = new ProgressBar();
		phoBox.add(phoProgress_);
		ctrl_.add(phoBox, BorderLayout.EAST);
		phoProgress_.stop();

		bouButton_.addActionListener(this);
		phoButton_.addActionListener(this);
		bouButton_.setEnabled(false);
		phoButton_.setEnabled(false);   

		statWin_ = new DisplayStat();
		final BouMaton owner = this;
		statWin_.addComponentListener(new ComponentAdapter() {
			public void componentHidden(ComponentEvent e) {
				owner.updateInfoMenu();
			}
			public void componentShown(ComponentEvent e) {
				owner.updateInfoMenu();
			}
		});

		histWin_ = new History();
		histWin_.addComponentListener(new ComponentAdapter() {
			public void componentHidden(ComponentEvent e) {
				owner.updateHistMenu();
			}
			public void componentShown(ComponentEvent e) {
				owner.updateHistMenu();
			}
		});


		toolkit_ = Toolkit.getDefaultToolkit();

		//    new HintShower(bouPeriodProgress_, "Period for the Boulanger transform");
		//    new HintShower(bouButton_, "Clic here for the Boulanger transform");
		//    new HintShower(phoPeriodProgress_, "Period for the Photomaton transform");
		//    new HintShower(phoButton_, "Clic here for the Photomaton transform");
		//    new HintShower(iterations_, "Enter here the number of tranform iterations");

		pack();

		adjustSize();

		setVisible(true);

		/* 
		 * if (!COMJPEGAvailable()) {
		 *   new ErrorDialog(this, getString("JPEGAvailError"));
		 * }
		 */
	}

	protected void adjustSize() {
		Dimension pictDim = pict_.getPreferredSize();
		Dimension ctrlDim = ctrl_.getPreferredSize();
		Insets ins = getInsets();
		Dimension size = new Dimension();
		if (pictDim.width > ctrlDim.width) {
			size.width = pictDim.width;
		} else {
			size.width = ctrlDim.width;
		}
		size.width += ins.left + ins.right;

		size.height = ctrlDim.height + pictDim.height + ins.top + ins.bottom;
		setSize(size);

		Rectangle bounds = getBounds();

		if (bounds.x < screenBounds_.x) {
			bounds.x = screenBounds_.x;
		}
		if (bounds.y < screenBounds_.y) {
			bounds.y = screenBounds_.y;
		}
		int over = (bounds.x + bounds.width)
				- (screenBounds_.x + screenBounds_.width);
		if (over > 0) {
			over -= bounds.x - screenBounds_.x;
			bounds.x = screenBounds_.x;
			if (over > 0) {
				bounds.width -= over;
			}
		}
		over = (bounds.y + bounds.height)
				- (screenBounds_.y + screenBounds_.height);
		if (over > 0) {
			over -= bounds.y - screenBounds_.y;
			bounds.y = screenBounds_.y;
			if (over > 0) {
				bounds.height -= over;
			}
		}

		setBounds(bounds);
		doLayout();
	}

	public void doLayout() {
		Rectangle bounds = getBounds();
		Insets ins = getInsets();
		int ctrlHeight = ctrl_.getPreferredSize().height;

		bounds.x = ins.left;
		bounds.y = ins.top;
		bounds.width -= ins.left + ins.right;
		bounds.height -= ins.top + ins.bottom;
		bounds.height -= ctrlHeight;
		pict_.setBounds(bounds);

		bounds.y += bounds.height;
		bounds.height = ctrlHeight;
		ctrl_.setBounds(bounds);

		ctrl_.validate();

		repaint();
	}

	public void actionPerformed(ActionEvent newEvent) {
		if (newEvent.getActionCommand().equals(miAbout.getActionCommand())) {
			handleAbout();
		} else if (newEvent.getActionCommand().equals(miOpen.getActionCommand())) {
			doOpen();
		} else if (newEvent.getActionCommand().equals(miSave.getActionCommand())) {
			doSave();
		} else if (newEvent.getActionCommand().equals(miRevert.getActionCommand())) {
			readFile(file_);
		} else if (newEvent.getActionCommand().equals(miClose.getActionCommand())) {
			doClose();
		} else if (newEvent.getActionCommand().equals(miQuit.getActionCommand())) {
			doQuit();
		} else if (newEvent.getActionCommand().equals(miBoulange_.getActionCommand())) {
			doBoulange();
		} else if (newEvent.getActionCommand().equals(bouButton_.getActionCommand())) {
			doBoulange();
		} else if (newEvent.getActionCommand().equals(miMaton_.getActionCommand())) {
			doMaton();
		} else if (newEvent.getActionCommand().equals(phoButton_.getActionCommand())) {
			doMaton();
		} else if (newEvent.getActionCommand().equals(miStop_.getActionCommand())) {
			doStop();
		} else if (newEvent.getActionCommand().equals(miInfo_.getActionCommand())) {
			doInfo();
		} else if (newEvent.getActionCommand().equals(miHist_.getActionCommand())) {
			doHistory();
		}

	}

	public void handleAbout() {
		aboutBox.setResizable(false);
		//     Point myPos = getLocation();
		//     Insets ins = getInsets();
		//     aboutBox.setLocation(myPos.x + ins.left, myPos.y + ins.top);
		aboutBox.setVisible(true);
	}

	public void doInfo() {
		if (miInfo_.getLabel().equals(getString("ShowInfoItem"))) {
			statWin_.setVisible(true);
		} else {
			statWin_.setVisible(false);
		}
	}

	public void doHistory() {
		if (miHist_.getLabel().equals(getString("ShowHistoryItem"))) {
			histWin_.setVisible(true);
		} else {
			histWin_.setVisible(false);
		}
	}

	public void updateInfoMenu() {
		if (statWin_.isVisible()) {
			miInfo_.setLabel(getString("HideInfoItem"));
		} else {
			miInfo_.setLabel(getString("ShowInfoItem"));
		}
	}

	public void updateHistMenu() {
		if (histWin_.isVisible()) {
			miHist_.setLabel(getString("HideHistoryItem"));
		} else {
			miHist_.setLabel(getString("ShowHistoryItem"));
		}
	}


	public void doOpen() {
		FileDialog getfile = new FileDialog(this, 
				getString("SelectFileStr"),
				FileDialog.LOAD);
		getfile.setVisible(true);
		String dname = getfile.getDirectory();
		String fname = getfile.getFile();
		if ((dname != null) && (fname != null)) {
			readFile(new File(dname, fname));
		}
	}

	protected void readFile(File f) {
		if (f == null) {
			return;
		}
		if (f.isDirectory()) {
			return;
		}
		file_ = f;
		miSave.setEnabled(true);
		miRevert.setEnabled(false);

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		BufferedImage img;
		String errmsg = "";

		try {
			img = ImageUtils.read(file_);
		} catch (Throwable t) {
			errmsg = t.toString() + " in BouMaton.readFile";
			img = null;
		}

		if (img == null) {
			setCursor(Cursor.getDefaultCursor());
			new ErrorDialog(this, getString("CouldNotReadStr")
					+" \""+file_.getName()+"\""
					+ System.getProperty("line.separator")
					+ errmsg);
			return;
		}

		int width = img.getWidth();
		int height = img.getHeight();
		dimLabel_.setText(Integer.toString(width)
				+ " x "
				+ Integer.toString(height));
		pict_.setImage(img);

		miInfo_.setEnabled(true);
		miHist_.setEnabled(true);

		miOpen.setEnabled(false);
		miClose.setEnabled(true);
		miBoulange_.setEnabled(true);
		miMaton_.setEnabled(true);
		bouButton_.setEnabled(true);
		phoButton_.setEnabled(true);
		histWin_.clear();

		adjustSize();

		if ((width != width_) || (height != height_)) {
			statWin_.clear();
			width_ = width;
			height_ = height;

			try {
				bouCycles_ = new long[width][height];
				phoCycles_ = new long[width][height];
			} catch (OutOfMemoryError oome) {
				bouCycles_ = null;
				phoCycles_ = null;
				new ErrorDialog(this, getString("NoMemPeriodsError"));
				bouPeriodProgress_.setText(getString("NoEnoughMemPeriodStr"));
				statWin_.append(getString("NoMemPeriodsError"), "bou");
				phoPeriodProgress_.setText(getString("NoEnoughMemPeriodStr"));
				statWin_.append(getString("NoMemPeriodsError"), "pho");

			}
			if ((bouCycles_ != null) && (phoCycles_ != null)) {
				for (int i = 0; i < width; i++) {
					for (int j = 0; j < height; j++) {
						bouCycles_[i][j] = -1;
						phoCycles_[i][j] = -1;
					}
				}

				phoFreq_ = new HashMap<Long, Long>();
				bouFreq_ = new HashMap<Long, Long>();

				Thread phoThread = new Thread(periodTasks_, "phoperiod") {
					public void run() {
						phoPeriod();
					}
				};
				phoThread.start();

				Thread bouThread = new Thread(periodTasks_, "bouperiod") {
					public void run() {
						bouPeriod();
					}
				};
				bouThread.start();
			}
		}

		setCursor(Cursor.getDefaultCursor());
		statWin_.setTitle(file_.getName() + " (" + width + " x " + height + ")");
	}

	public void doSave() {
		QualityDialog qdlg = new QualityDialog(this, pict_.getImage());
		if (!qdlg.ok()) {
			return;
		}

		FileDialog putfile = new FileDialog(this,
				getString("SaveAsStr"),
				FileDialog.SAVE);
		String snam = file_.getName();
		int doti = snam.lastIndexOf('.');
		if (doti < 0) {
			snam = snam + "." + imageFormat_;
		} else {
			snam = snam.substring(0, doti) + "." + imageFormat_;
		}
		putfile.setFile(snam);
		putfile.setVisible(true);
		String dname = putfile.getDirectory();
		String fname = putfile.getFile();
		File outfile = null;
		String errmsg;
		if ((dname != null) && (fname != null)) {
			outfile = new File(dname, fname);
		}
		if (outfile == null) {
			return;
		}
		if (outfile.isDirectory()) {
			return;
		}
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		errmsg = saveJPEGFile(pict_.getImage(), outfile);

		setCursor(Cursor.getDefaultCursor());
		if (errmsg != null) {
			new ErrorDialog(this, errmsg);
		}
	}

	protected static String sizeToString(float size) {
		String unit = BouMaton.getString("ByteStr");
		if (size >= 1024.0) {
			size /= 1024.0;
			unit = BouMaton.getString("KByteStr");
		}
		if (size > 1024.0) {
			size /= 1024.0;
			unit = BouMaton.getString("MByteStr");
		}
		return Integer.toString(Math.round(size)) + " " + unit;
	}

	/**
	 * Safe version, invoked only when com.sun.image.codec.jpeg.JPEGCodec is 
	 * available.
	 */
	protected String saveJPEGFile(BufferedImage img, File outfile) {
		String errmsg = null;
		img = removeAlphaChannel(img);

		try {
			ImageUtils.saveImage(img, outfile, imageFormat_, jpegQuality_, false, null);
		} catch (IOException ioe) {
			errmsg = getString("CouldNotWriteStr")+" \"" + outfile.getName() + "\"";
		}

		return errmsg;
	}

	protected static BufferedImage removeAlphaChannel(BufferedImage img) {
		BufferedImage noAlpha = img;

		if (img.getType() != BufferedImage.TYPE_INT_RGB) {
			// Remove Alpha channel before saving to get a valid JFIF image
			noAlpha = new BufferedImage(img.getWidth(null),
					img.getHeight(null),
					BufferedImage.TYPE_INT_RGB);
			Graphics2D grf = noAlpha.createGraphics();
			grf.drawImage(img, 0, 0, null);
		}
		return noAlpha;
	}


	class QualityDialog extends Dialog
	implements ActionListener,
	AdjustmentListener,
	ItemListener {
		public QualityDialog(Frame owner, BufferedImage img) {
			super(owner, BouMaton.getString("JPEGQualityChoiceStr"), true);
			img_ = BouMaton.removeAlphaChannel(img);
			ok_ = true;
			qualScroll_ = new Scrollbar(Scrollbar.HORIZONTAL, (int)(jpegQuality_ * 100), 1, 0, 100+1);
			qualScroll_.addAdjustmentListener(this);

			CheckboxGroup group = new CheckboxGroup();
			boolean jpeg = imageFormat_.equals("jpg");
			jpegChkbox_ = new Checkbox("JPEG", group, jpeg);
			pngChkbox_ = new Checkbox("PNG", group, !jpeg);
			jpegChkbox_.addItemListener(this);
			pngChkbox_.addItemListener(this);

			setLayout(new VerticalLayout(10,10));

			Panel fmtChoice = new Panel(new FlowLayout());
			fmtChoice.add(jpegChkbox_);
			fmtChoice.add(pngChkbox_);
			add(fmtChoice, VerticalLayout.HFILL);

			qualPanel_ = new Panel(new GridLayout(1,2,5,0));
			qualPanel_.add(new Label(BouMaton.getString("JPEGQualityStr"), Label.RIGHT));
			val_ = new Label(Integer.toString(qualScroll_.getValue()));
			qualPanel_.add(val_);
			add(qualPanel_, VerticalLayout.HFILL);
			add(qualScroll_, VerticalLayout.HFILL);

			hlpTxt_ = new TextArea(BouMaton.getString("JPEGQualityHelp"),
					0, 0, TextArea.SCROLLBARS_NONE);
			hlpTxt_.setEditable(false);
			hlpTxt_.setBackground(SystemColor.text);
			add(hlpTxt_, VerticalLayout.HFILL);

			if (!jpeg) {
				qualScroll_.setVisible(false);
				qualPanel_.setVisible(false);
				hlpTxt_.setText(BouMaton.getString("PNGHelp"));
			}

			Button calc = new Button(BouMaton.getString("CalcJPEGSizeStr"));
			sizeLabel_ = new Label("", Label.CENTER);
			Panel ctrlPanel = new Panel(new GridLayout(0, 2));
			ctrlPanel.add(calc);
			calc.setActionCommand("CALC_SIZE");
			calc.addActionListener(this);
			ctrlPanel.add(sizeLabel_);

			ctrlPanel.add(new Panel());
			ctrlPanel.add(new Panel());

			Button ok = new Button(BouMaton.getString("OKStr"));
			ok.addActionListener(this);
			Button canc = new Button(BouMaton.getString("CancelStr"));
			canc.setActionCommand("CANCEL");
			canc.addActionListener(this);
			ctrlPanel.add(canc);
			ctrlPanel.add(ok);
			add(ctrlPanel, VerticalLayout.CENTER);

			pack();
			setSize(getMinimumSize());

			setVisible(true);
		}

		public boolean ok() {
			return ok_;
		}

		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				ItemSelectable checkbox = e.getItemSelectable();
				if (checkbox == jpegChkbox_) {
					qualScroll_.setVisible(true);
					qualPanel_.setVisible(true);
					hlpTxt_.setText(BouMaton.getString("JPEGQualityHelp"));
					sizeLabel_.setText("");
					invalidate();
				} else if (checkbox == pngChkbox_) {
					qualScroll_.setVisible(false);
					qualPanel_.setVisible(false);
					hlpTxt_.setText(BouMaton.getString("PNGHelp"));
					sizeLabel_.setText("");
					invalidate();
				} else {
					throw new Error("Unknown image format '" + checkbox + "'");
				}
			}
		}

		public void adjustmentValueChanged(AdjustmentEvent e) {
			val_.setText(Integer.toString(e.getValue()));
			sizeLabel_.setText("");
			validate();
		}

		public void actionPerformed(ActionEvent newEvent) {
			String cmd = ((Button)newEvent.getSource()).getActionCommand();

			if (cmd.equals("CANCEL")) {
				ok_ = false;
				dispose();
			} else if (cmd.equals("CALC_SIZE")) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				/* 
				 * CounterOutputStream estim = new CounterOutputStream();
				 * try {
				 *   encodeAsJPEG(img_, estim, (float)(qualScroll_.getValue())/100);
				 * } catch (IOException ioe) {
				 * }
				 * 
				 * float size = (float)estim.size();
				 */
				String imgfmt = null;
				if (jpegChkbox_.getState()) {
					imgfmt = "jpg";
				} else if (pngChkbox_.getState()) {
					imgfmt = "png";
				} else {
					throw new Error("Error: image format is neither jpg nor png");
				}
				float size = (float)ImageUtils.estimateSize(img_, imgfmt, (float)(qualScroll_.getValue())/100, false);
				sizeLabel_.setText(BouMaton.sizeToString(size));
				/* 
				 * String unit = BouMaton.getString("ByteStr");
				 * if (size >= 1024.0) {
				 *   size /= 1024.0;
				 *   unit = BouMaton.getString("KByteStr");
				 * }
				 * if (size > 1024.0) {
				 *   size /= 1024.0;
				 *   unit = BouMaton.getString("MByteStr");
				 * }
				 * sizeLabel_.setText(Integer.toString(Math.round(size)) + " " + unit);
				 */
				setCursor(Cursor.getDefaultCursor());
				validate();
			} else {
				jpegQuality_ = (float)(qualScroll_.getValue())/100;
				if (jpegChkbox_.getState()) {
					imageFormat_ = "jpg";
				} else if (pngChkbox_.getState()) {
					imageFormat_ = "png";
				} else {
					throw new Error("Error: image format is neither jpg nor png");
				}
				dispose();
			}
		}

		private TextArea hlpTxt_;
		private Panel qualPanel_;
		private Scrollbar qualScroll_;
		private Checkbox jpegChkbox_;
		private Checkbox pngChkbox_;
		private Label val_;
		private Label sizeLabel_;
		private boolean ok_;
		private BufferedImage img_;
	}

	class ErrorDialog extends Dialog implements ActionListener {
		public ErrorDialog(Frame owner, String message) {
			super(owner, BouMaton.getString("ErrorStr"), true);
			message_ = new TextArea(BouMaton.getString("ErrorStr")+": " + message + ".");
			message_.setBackground(SystemColor.text);
			message_.setEditable(false);
			add(message_, BorderLayout.NORTH);
			Button close = new Button(BouMaton.getString("DismissStr"));
			close.addActionListener(this);
			buttonPanel_ = new Panel(new FlowLayout(FlowLayout.CENTER, 5, 5));
			buttonPanel_.add(close);
			add(buttonPanel_, BorderLayout.SOUTH);
			addComponentListener(new BouMatonResizer(this));
			pack();
			setVisible(true);
		}

		public void doLayout() {
			Dimension size = getSize();
			Insets ins = getInsets();
			Rectangle bounds = new Rectangle(ins.left, ins.top,
					size.width - ins.left - ins.right,
					size.height - ins.top - ins.bottom);
			buttonPanel_.doLayout();
			Dimension buttonDim = buttonPanel_.getPreferredSize();
			bounds.height -= buttonDim.height;
			message_.setBounds(bounds);
			buttonPanel_.setBounds(bounds.x, bounds.y + bounds.height, 
					bounds.width, buttonDim.height);
		}

		public void actionPerformed(ActionEvent newEvent) {
			dispose();
		}

		private Panel buttonPanel_;
		private TextArea message_;
	}

	protected void setBouPeriod(BigInteger p) {
		bouBigPeriod_ = p;
		bouPeriodProgress_.stopProgress();
		if (p == null) {
			bouPeriodProgress_.setText(getString("NoPictureStr"));
			bouPeriod_ = -1;
			bouCycles_ = null;
			return;
		}

		if (p.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
			bouPeriod_ = 0;
		} else {
			bouPeriod_ = p.longValue();
		}
		bouPeriodProgress_.setText(p.toString());
		statWin_.append(getString("PicturePeriodStr")+" = " + bouBigPeriod_.toString(), "bou");

		FreqStat[] stats = buildStats(bouFreq_);
		displayStats(stats, "bou");
	}

	protected void displayStats(FreqStat[] stats, String where) {
		statWin_.append("", where);
		statWin_.append(getString("PixelPeriodStr") + ": ", where);
		DecimalFormat fmt = new DecimalFormat("##0.00%");
		int maxLen = 0;
		for (int i = 0; i < stats.length; i++) {
			int len = Long.toString(stats[i].period).length();
			if (len > maxLen) {
				maxLen = len;
			}
		}
		for (int i = 0; i < stats.length; i++) {
			StringBuffer pper = new StringBuffer(Long.toString(stats[i].period));
			while (pper.length() < maxLen) {
				pper.insert(0, ' ');
			}

			StringBuffer ppix = new StringBuffer((stats[i].percentPix < 0.00005) ? 
					Long.toString(stats[i].occurrences) : fmt.format(stats[i].percentPix));
			while (ppix.length() < 7) {
				ppix.insert(0, ' ');
			}

			StringBuffer pplac = new StringBuffer((stats[i].percentPixInPlace < 0.00005) ? 
					Long.toString(stats[i].pixelsInPlace) : fmt.format(stats[i].percentPixInPlace));
			while (pplac.length() < 7) {
				pplac.insert(0, ' ');
			}

			statWin_.append(pper
					+ " : " + ppix + " " + getString("PixelsStr")
					+ ", " + pplac + " " + getString("PixelsPlaceStr"), where);
		}
	}

	class FreqStat {
		public FreqStat(long per, long occ) {
			period = per;
			occurrences = occ;
			pixelsInPlace = occurrences;
		}

		public FreqStat(Long per, Long occ) {
			period = per.longValue();
			occurrences = occ.longValue();
			pixelsInPlace = occurrences;
		}

		public long period;
		public long occurrences;
		public double percentPix;
		public long pixelsInPlace;
		public double percentPixInPlace;
	}

	protected FreqStat[] buildStats(final HashMap<Long, Long> frequencies) {
		long nbPixels = width_ * height_;

		FreqStat[] freqs = new FreqStat[frequencies.size()];
		Iterator<Long> iter = frequencies.keySet().iterator();
		int n = 0;
		while (iter.hasNext()) {
			Long p = (Long)iter.next();
			freqs[n] = new FreqStat(p, (Long)frequencies.get(p));
			n++;
		}

		Arrays.sort(freqs,
				new Comparator<FreqStat>() {
			public int compare(FreqStat o1, FreqStat o2) {
				long l1 = o1.occurrences;
				long l2 = o2.occurrences;
				if (l1 == l2) {
					return 0;
				}
				if (l1 < l2) {
					return 1;
				}
				return -1;
			}
		}
				);

		for (int i = 0; i < freqs.length; i++) {
			for (int j = i+1; j < freqs.length; j++) {
				if (freqs[i].period % freqs[j].period == 0) {
					freqs[i].pixelsInPlace += freqs[j].pixelsInPlace;
				}
			}
			freqs[i].percentPix = (double)freqs[i].occurrences / nbPixels;
			freqs[i].percentPixInPlace = (double)freqs[i].pixelsInPlace / nbPixels;
		}

		return freqs;
	}

	protected long pgcd(long a, long b) {
		if (a == b) {
			return a;
		}

		long t;

		if (b > a) {
			t = a;
			a = b;
			b = t;
		}
		do {
			t = a % b;
			a = b;
			b = t;
		} while (b != 0);
		return a;
	}

	protected long ppcm(long a, long b) {
		if (a == b) {
			return a;
		}

		long pgcd = pgcd(a, b);
		if (a > b) {
			a /= pgcd;
		} else {
			b /= pgcd;
		}
		long ppcm = a * b;
		if (ppcm < 0) {
			return 0;
		}
		return ppcm;
	}

	protected void setPhoPeriod(BigInteger p) {
		phoBigPeriod_ = p;
		phoPeriodProgress_.stopProgress();
		if (p == null) {
			phoPeriodProgress_.setText(getString("NoPictureStr"));
			phoPeriod_ = -1;
			phoCycles_ = null;
			return;
		}

		if (p.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
			phoPeriod_ = 0;
		} else {
			phoPeriod_ = p.longValue();
		}
		phoPeriodProgress_.setText(p.toString());
		statWin_.append(getString("PicturePeriodStr") + " = " + phoBigPeriod_.toString(), "pho");

		FreqStat[] stats = buildStats(phoFreq_);
		displayStats(stats, "pho");
	}

	public void doClose() {
		if (pict_.getImage() == null) {
			return;
		}
		interrupt_ = true;
		stopThreads_ = true;
		pict_.setImage(null);
		dimLabel_.setText("");
		miClose.setEnabled(false);
		bouButton_.setEnabled(false);
		phoButton_.setEnabled(false);   
		miBoulange_.setEnabled(false);
		miMaton_.setEnabled(false);
		miOpen.setEnabled(true);
		miSave.setEnabled(false);

		Thread[] jobs = new Thread[subTasks_.activeCount()];
		subTasks_.enumerate(jobs);
		for (int i = 0; i < jobs.length; i++) {
			try {
				jobs[i].join();
			} catch (InterruptedException ie) {
				i--;
			}
		}

		setBouPeriod(null);
		setPhoPeriod(null);
		width_ = -1;
		height_ = -1;
		bouCycles_ = null;
		phoCycles_ = null;
		phoFreq_ = null;
		bouFreq_ = null;
		statWin_.setVisible(false);
		statWin_.clear();
		miInfo_.setLabel(getString("ShowInfoItem"));
		miInfo_.setEnabled(false);
		histWin_.setVisible(false);
		histWin_.clear();
		miHist_.setLabel(getString("ShowHistoryItem"));
		miHist_.setEnabled(false);
		miRevert.setEnabled(false);
		repaint();
	}

	public void doStop() {
		interrupt_ = true;
		bouButton_.setEnabled(true);
		phoButton_.setEnabled(true);    
		miBoulange_.setEnabled(true);
		miMaton_.setEnabled(true);
	}

	public void doQuit() {
		stopThreads_ = true;
		interrupt_ = true;
		System.exit(0);
	}

	public void doBoulange() {
		final BufferedImage img = pict_.getImage();
		if (img != null) {
			final int iter;
			try {
				iter = Integer.parseInt(iterations_.getText());
			} catch (NumberFormatException e) {
				iterations_.select(0,iterations_.getText().length());
				toolkit_.beep();
				return;
			}
			bouButton_.setEnabled(false);
			phoButton_.setEnabled(false);   
			miBoulange_.setEnabled(false);
			miMaton_.setEnabled(false);
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			interrupt_ = false;
			miStop_.setLabel(getString("InterruptItem") + " boulanger");
			miStop_.setEnabled(true);
			Thread bouCalc = new Thread(transTasks_, "boulanger") {
				public void run() {
					BufferedImage res = boulanger(img, iter);
					if (res != null) {
						pict_.setImage(res);
						miRevert.setEnabled(true);
					}
					bouButton_.setEnabled(true);
					phoButton_.setEnabled(true);    
					miBoulange_.setEnabled(true);
					miMaton_.setEnabled(true);
					miStop_.setLabel(getString("InterruptItem"));
					miStop_.setEnabled(false);
					bouProgress_.stop();
					setCursor(Cursor.getDefaultCursor());
					repaint();
				}
			};
			bouCalc.start();
		}
	}

	public void doMaton() {
		final BufferedImage img = pict_.getImage();
		if (img != null) {
			final int iter;
			try {
				iter = Integer.parseInt(iterations_.getText());
			} catch (NumberFormatException e) {
				iterations_.select(0,iterations_.getText().length());
				toolkit_.beep();
				return;
			}
			bouButton_.setEnabled(false);
			phoButton_.setEnabled(false);   
			miBoulange_.setEnabled(false);
			miMaton_.setEnabled(false);
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			interrupt_ = false;
			miStop_.setLabel(getString("InterruptItem") + " photomaton");
			miStop_.setEnabled(true);
			Thread phoCalc = new Thread(transTasks_, "photomaton") {
				public void run() {
					BufferedImage res = photoMaton(img, iter);
					if (res != null) {
						pict_.setImage(res);
						miRevert.setEnabled(true);
					}
					bouButton_.setEnabled(true);
					phoButton_.setEnabled(true);    
					miBoulange_.setEnabled(true);
					miMaton_.setEnabled(true);
					miStop_.setLabel(getString("InterruptItem"));
					miStop_.setEnabled(false);
					phoProgress_.stop();
					setCursor(Cursor.getDefaultCursor());
					repaint();
				}
			};
			phoCalc.start();
		}
	}

	public static void main(String args[]) throws Throwable {
		new BouMaton();
	}

	protected void bouPeriod() {
		Image img = pict_.getImage();
		if (img == null) {
			bouPeriod_ = -1;
			bouBigPeriod_ = null;
			bouCycles_ = null;
			return;
		}
		stopThreads_ = false;

		int height = img.getHeight(null);
		int width = img.getWidth(null);
		BigInteger period = BigInteger.ONE;
		bouPeriodProgress_.setMax(height);
		bouPeriodProgress_.startProgress();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (bouCycles_[x][y] > 0) {
					continue;
				}

				int nx = x;
				int ny = y;
				long len = 0;

				while (true) {
					if (stopThreads_) {
						return;
					}

					nx = 2 * nx + ny % 2;
					ny = (ny - ny % 2) / 2;
					if (nx >= width) {
						nx = 2 * width - nx - 1;
						ny =  height - 1 - ny;
					}
					len++;
					if ((nx != x) || (ny != y)) {
						// (nx, ny) is on our orbit, so it has the same period
						// as (x, y). Remember this.
						bouCycles_[nx][ny] = -2;
					} else {
						break;
					}
				}

				bouCycles_[x][y] = len;
				int nbPixels = 1; // One pixel has this period
				// Update the period of all the pixels on our orbit
				for (int oy = 0; oy < height; oy++) {
					for (int ox = 0; ox < width; ox++) {
						if (bouCycles_[ox][oy] == -2) {
							bouCycles_[ox][oy] = len;
							nbPixels++;
						}
					}
				}

				Long l = Long.valueOf(len);
				// Get the number of pixels that have this period
				Long f = bouFreq_.get(l);
				if (f == null) {
					// This is the first pixel with this period
					//bouFreq_.put(l, new Long(1));
					bouFreq_.put(l, Long.valueOf(nbPixels));
					BigInteger bigLen = BigInteger.valueOf(len);
					BigInteger gcd = period.gcd(bigLen);
					period = period.multiply(bigLen).divide(gcd);
				} else {
					// Some more pixels with this period.
					//bouFreq_.put(l, new Long(f.longValue() + 1));
					bouFreq_.put(l, Long.valueOf(f.longValue() + nbPixels));
				}
			}
			bouPeriodProgress_.progress(y + 1);
		}
		setBouPeriod(period);
	}

	private BufferedImage boulanger(BufferedImage img, long niters) {   
		int width = img.getWidth();
		int height = img.getHeight();
		long iters = niters;
		BufferedImage res = null;
		try {
			int imgType = img.getType();
			if (imgType == BufferedImage.TYPE_CUSTOM) {
				imgType = BufferedImage.TYPE_4BYTE_ABGR;
			}
			res = new BufferedImage(width, height, imgType);
		} catch (OutOfMemoryError ome) {
			new ErrorDialog(this, getString("NoEnoughMemImageStr")
					+ System.getProperty("line.separator")
					+ getString("FreeMemStr") + " "
					+ BouMaton.sizeToString((float)Runtime.getRuntime().freeMemory())
					+ System.getProperty("line.separator")
					+ getString("MaxMemStr") + " "
					+ BouMaton.sizeToString((float)Runtime.getRuntime().maxMemory())
					);
			return null;
		}

		bouProgress_.setMax(height);
		bouProgress_.start();
		long stime = System.currentTimeMillis();
		int sign = (iters < 0) ? -1 : 1;
		iters *= sign;

		if (bouPeriod_ > 0) {
			if (bouPeriod_ < iters) {
				iters %= bouPeriod_;
			}

			if (iters > (bouPeriod_ / 2)) {
				iters = bouPeriod_ - iters;
				sign = -sign;
			}
		}

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (interrupt_) {
					return null;
				}
				int nx = x;
				int ny = y;
				long n = iters;
				int s = sign;
				long p;
				if ((bouCycles_ != null) && ((p = bouCycles_[x][y]) > 0)) {
					if (p < n) {
						n %= p;
					}

					if (n > (p / 2)) {
						n = p - n;
						s = -s;
					}
				}

				if (s > 0) {
					for (int i = 0; i < n; i++) {
						nx = 2 * nx + ny % 2;
						ny = (ny - ny % 2) / 2;
						if (nx >= width) {
							ny =  height - 1 - ny;
							nx = 2 * width - nx - 1;
						}
					}
				} else {
					for (int i = 0; i < n; i++) {
						ny = 2 * ny + nx % 2;
						nx = (nx - nx % 2) / 2;
						if (ny >= height) {
							nx =  width - 1 - nx;
							ny = 2 * height - ny - 1;
						}
					}
				}
				res.setRGB(nx, ny, img.getRGB(x, y));
			}
			bouProgress_.progress(y + 1);
		}
		stime = System.currentTimeMillis() - stime;
		histWin_.add(getString("BoulangerCapStr"), niters, iters * sign, stime);
		return res;
	}

	private Dimension photodims(int width, int height) {
		return new Dimension((width%2 == 0) ? (width/2):((width+1)/2),
				(height%2 == 0) ? (height/2):((height+1)/2));
	}

	// Version with Px and Py optimization
	protected void phoPeriod() {
		Image img = pict_.getImage();
		if (img == null) {
			phoPeriod_ = -1;
			phoBigPeriod_ = null;
			phoCycles_ = null;
			return;
		}
		int height = img.getHeight(null);
		int width = img.getWidth(null);
		Dimension hdims = photodims(width, height);
		int hWidth = hdims.width;
		int hHeight = hdims.height;

		//phoCycles_ = new long[width][height];
		long[] xcycles = new long[width];
		long[] ycycles = new long[height];

		stopThreads_ = false;
		phoPeriodProgress_.setMax(width + 2 * height);
		phoPeriodProgress_.startProgress();

		for (int x = 0; x < width; x++) {
			int nx = x;
			long len = 0;
			if (stopThreads_) {
				phoCycles_ = null;
				return;
			}

			do {
				nx = (nx % 2 == 0) ? nx / 2 : nx / 2 + hWidth;
				len++;
			} while (nx != x);
			xcycles[x] = len;
			phoPeriodProgress_.progress(x + 1);
		}

		for (int y = 0; y < height; y++) {
			int ny = y;
			long len = 0;
			if (stopThreads_) {
				phoCycles_ = null;
				return;
			}

			do {
				ny = (ny % 2 == 0) ? ny / 2 : ny / 2 + hHeight;
				len++;
			} while (ny != y);
			ycycles[y] = len;
			phoPeriodProgress_.progress(width + y + 1);
		}

		BigInteger period = BigInteger.ONE;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (stopThreads_) {
					phoCycles_ = null;
					return;
				}

				long len = ppcm(xcycles[x], ycycles[y]);
				phoCycles_[x][y] = len;

				Long l = Long.valueOf(len);
				Long f = (Long)phoFreq_.get(l);
				if (f == null) {
					phoFreq_.put(l, Long.valueOf(1));
					BigInteger bigLen = BigInteger.valueOf(len);
					BigInteger gcd = period.gcd(bigLen);
					period = period.multiply(bigLen).divide(gcd);
				} else {
					phoFreq_.put(l, Long.valueOf(f.longValue() + 1));
				}
			}
			phoPeriodProgress_.progress(width + height + y + 1);
		}
		setPhoPeriod(period);
	}


	private BufferedImage photoMaton(BufferedImage img, long niters) {
		int width = img.getWidth();
		int height = img.getHeight();
		long iters = niters;
		Dimension hdims = photodims(width, height);
		int hWidth = hdims.width;
		int hHeight = hdims.height;

		BufferedImage res = null;
		try {
			int imgType = img.getType();
			if (imgType == BufferedImage.TYPE_CUSTOM) {
				imgType = BufferedImage.TYPE_4BYTE_ABGR;
			}
			res = new BufferedImage(width, height, imgType);
		} catch (OutOfMemoryError ome) {
			new ErrorDialog(this, getString("NoEnoughMemImageStr")
					+ System.getProperty("line.separator")
					+ getString("FreeMemStr") + " "
					+ BouMaton.sizeToString((float)Runtime.getRuntime().freeMemory())
					+ System.getProperty("line.separator")
					+ getString("MaxMemStr") + " "
					+ BouMaton.sizeToString((float)Runtime.getRuntime().maxMemory())
					);
			return null;
		}
		phoProgress_.setMax(height);
		phoProgress_.start();
		long stime = System.currentTimeMillis();
		int sign = (iters < 0) ? -1 : 1;
		iters = sign * iters;

		if (phoPeriod_ > 0) {
			if (phoPeriod_ < iters) {
				iters %= phoPeriod_;
			}

			if (iters > (phoPeriod_ / 2)) {
				iters = phoPeriod_ - iters;
				sign = -sign;
			}
		}

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (interrupt_) {
					return null;
				}

				int nx = x;
				int ny = y;
				long n = iters;
				int s = sign;
				long p;
				if ((phoCycles_ != null) && ((p = phoCycles_[x][y]) > 0)) {
					if (p < n) {
						n %= p;
					}

					if (n > (p / 2)) {
						n = p - n;
						s = -s;
					}
				}

				if (s > 0) {
					for (long i = 0; i < n; i++) {
						nx = (nx % 2 == 0) ? nx / 2 : nx / 2 + hWidth;
						ny = (ny % 2 == 0) ? ny / 2 : ny / 2 + hHeight;
					}
				} else {
					for (long i = 0; i < n; i++) {
						nx = (nx < hWidth) ? nx * 2 : 2 * (nx - hWidth) + 1;
						ny = (ny < hHeight) ? ny * 2 : 2 * (ny - hHeight) + 1;
					}
				}

				res.setRGB(nx, ny, img.getRGB(x, y));
			}
			phoProgress_.progress(y + 1);
		}
		stime = System.currentTimeMillis() - stime;
		histWin_.add(getString("PhotoMatonCapStr"), niters, iters * sign, stime);
		return res;
	}

} // class BouMaton

@SuppressWarnings("serial")
class DisplayPict extends Component {
	public DisplayPict() {
		img_ = null;
	}

	public BufferedImage getImage() {
		return img_;
	}

	public void setImage(BufferedImage img) {
		img_ = img;
		invalidate();
		repaint();
	}

	public Dimension getMinimumSize() {
		return new Dimension(0, 0);
	}

	public Dimension getPreferredSize() {
		if (img_ == null) {
			return new Dimension(320, 240);
		}
		return new Dimension(img_.getWidth(), img_.getHeight());
	}

	public Dimension getMaximumSize() {
		return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	public void paint(Graphics g) {
		if (img_ == null) {
			return;
		} else {
			/* 
			 * Rectangle bounds = g.getClipBounds();
			 * int width = img_.getWidth();
			 * int height = img_.getHeight();
			 * float xscale = (float)(bounds.width)/width;
			 * float yscale = (float)(bounds.height)/height;
			 * float scale = (xscale < yscale) ? xscale : yscale;
			 * width = (int)(scale * width);
			 * height = (int)(scale * height);
			 * 
			 * g.drawImage(img_, bounds.x + (bounds.width - width)/2,
			 *             bounds.y + (bounds.height - height)/2,
			 *             width, height, null);
			 */
			int myWidth = getWidth();
			int myHeight = getHeight();
			int width = img_.getWidth();
			int height = img_.getHeight();
			float xscale = (float)(myWidth)/width;
			float yscale = (float)(myHeight)/height;
			float scale = (xscale < yscale) ? xscale : yscale;
			width = (int)(scale * width);
			height = (int)(scale * height);

			g.drawImage(img_, (myWidth - width)/2,
					(myHeight - height)/2,
					width, height, null);
		}
	}

	private BufferedImage img_;
}

@SuppressWarnings("serial")
class ProgressBar extends Component {
	final protected int height_ = 10;
	final protected int hpad_ = 5;

	public ProgressBar() {
		max_ = 100;
		partDone_ = 0.0;
	}

	public void setMax(int maxValue) {
		partDone_ *= (double)maxValue / max_;
		max_ = maxValue;
		if (partDone_ > 1.0) {
			partDone_ = 1.0;
		}
		repaint();
	}

	public void progress(int value) {
		partDone_ = (double)value / max_;
		if (partDone_ > 1.0) {
			partDone_ = 1.0;
		}
		repaint();
	}

	public void start() {
		setVisible(true);
	}

	public void stop() {
		setVisible(false);
	}

	public void paint(Graphics g) {
		int width = getWidth();
		int y = (getHeight() - height_) / 2;
		int x = hpad_;
		width -= (1 + 2 * hpad_);
		g.drawRect(x, y, width, height_);
		width = (int)(partDone_ * (width - 3));
		g.fillRect(x+2, y+2, width, height_-3);
	}

	public Dimension getMinimumSize() {
		return new Dimension(50, height_);
	}

	public Dimension getPreferredSize() {
		return new Dimension(getParent().getWidth(), height_);
	}

	public Dimension getMaximumSize() {
		return getPreferredSize();
	}

	private int max_;
	private double partDone_;
}

@SuppressWarnings("serial")
class ProgressLabel extends Container {
	public ProgressLabel(String text, int alignment) {
		label_ = new Label(text, alignment);
		progress_ = new ProgressBar();
		progress_.stop();
		setLayout(null);
		add(label_);
		add(progress_);

		stopped_ = true;
	}

	public void setMax(int maxValue) {
		progress_.setMax(maxValue);
	}

	public void progress(int value) {
		progress_.progress(value);
	}

	public void startProgress() {
		label_.setVisible(false);
		progress_.progress(0);
		progress_.start();
		stopped_ = false;
		repaint();
	}

	public void stopProgress() {
		progress_.stop();
		label_.setVisible(true);
		stopped_ = true;
		repaint();
	}

	public void setText(String text) {
		label_.setText(text);
		repaint();
	}

	public Dimension getMinimumSize() {
		if (stopped_) {
			return label_.getMinimumSize();
		} else {
			return progress_.getMinimumSize();
		}
	}

	public Dimension getPreferredSize() {
		if (stopped_) {
			return label_.getPreferredSize();
		} else {
			return progress_.getPreferredSize();
		}
	}

	public Dimension getMaximumSize() {
		if (stopped_) {
			return label_.getMaximumSize();
		} else {
			return progress_.getMaximumSize();
		}
	}

	public void paint(Graphics g) {
		Rectangle bounds = getBounds();
		if (stopped_) {
			label_.setBounds(0, 0, bounds.width, bounds.height);
			label_.paint(g);
		} else {
			progress_.setBounds(0, 0, bounds.width, bounds.height);
			progress_.paint(g);
		}
	}

	protected Label label_;
	protected ProgressBar progress_;
	protected boolean stopped_;
}

@SuppressWarnings("serial")
class Picture extends Component {
	//  public Picture(URL url) {
	//    img_ = BouMaton.getImageWithToolkit(url, this);
	public Picture(String name) throws IOException {
		img_ = ImageUtils.read(new File(name));
	}

	public Picture(InputStream in) throws IOException {
		img_ = ImageUtils.read(in);
		in.close();
	}

	public Dimension getPreferredSize() {
		if (img_ == null) {
			return new Dimension(0,0);
		}
		return new Dimension(img_.getWidth(), img_.getHeight());
	}

	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	public void paint(Graphics g) {
		if (img_ == null) {
			return;
		} else {      
			int myWidth = getWidth();
			int myHeight = getHeight();
			int width = img_.getWidth();
			int height = img_.getHeight();
			float xscale = (float)(myWidth)/width;
			float yscale = (float)(myHeight)/height;
			float scale = (xscale < yscale) ? xscale : yscale;
			width = (int)(scale * width);
			height = (int)(scale * height);

			g.drawImage(img_, (myWidth - width)/2,
					(myHeight - height)/2,
					width, height, null);
		}
	}

	private BufferedImage img_;
}

class VerticalLayout implements LayoutManager {
	public static final String WEST = "WEST";
	public static final String CENTER = "CENTER";
	public static final String HFILL = "HFILL";
	public static final String EAST = "EAST";
	public static final String GLUE = "GLUE";

	public VerticalLayout(int hmargin, int vpad) {
		hpad_ = hmargin;
		vpad_ = vpad;
		placement_ = new HashMap<Component, String>();
	}

	public void addLayoutComponent(String name, Component comp) {
		placement_.put(comp, name);
	}

	public void layoutContainer(Container parent) {
		Insets ins = parent.getInsets();
		Dimension size = parent.getSize();
		int width = size.width - ins.left - ins.right - 2 * hpad_;
		int targetheight = size.height - ins.top - ins.bottom - vpad_;
		int height = 0;
		int glueheight = 0;
		int gluecomps = 0;
		int x = ins.left + hpad_;
		int y = ins.top + vpad_;
		Component[] c = parent.getComponents();

		for (int i = 0; i < c.length; i++) {
			Dimension pref = c[i].getPreferredSize();      
			String place = (String)placement_.get(c[i]);
			if ((place != null) && (place.equals(GLUE))) {
				gluecomps++;
			} else {
				height += pref.height + vpad_;
			}
		}
		if ((height < targetheight) && (gluecomps > 0)) {
			glueheight = (targetheight - height) / gluecomps;
		}

		for (int i = 0; i < c.length; i++) {
			Dimension pref = c[i].getPreferredSize();
			String place = (String)placement_.get(c[i]);
			int offset = 0;
			int yoffset = vpad_;
			if ((place == null) || (place.equals(CENTER))) {
				offset = (width - pref.width) / 2;
			} else if (place.equals(HFILL)) {
				offset = 0;
				pref.width = width;
			} else if (place.equals(WEST)) {
				offset = 0;
			} else if (place.equals(EAST)) {
				offset = width - pref.width;
			} else if (place.equals(GLUE)) {
				pref.height = glueheight;
				yoffset = 0;
			} else {
				throw new IllegalArgumentException("Invalid vertical layout constraint \""+place+"\"");
			}

			c[i].setBounds(x + offset, y, pref.width, pref.height);
			y += pref.height + yoffset;
		}
	}

	public Dimension minimumLayoutSize(Container parent) {
		Insets ins = parent.getInsets();
		int width = ins.left + ins.right + 2 * hpad_;
		int height = ins.top + ins.bottom + vpad_;
		int cwidth = 0;
		Component[] c = parent.getComponents();
		if (c.length == 0) {
			height += vpad_;
		}

		for (int i = 0; i < c.length; i++) {
			Dimension min = c[i].getMinimumSize();
			// Some components (eg TextArea) have a preferred size
			// which is smaller than their minimum size... bug?
			Dimension pref = c[i].getPreferredSize();
			if (min.width > pref.width) {
				min.width = pref.width;
			}
			if (min.height > pref.height) {
				min.height = pref.height;
			}

			String place = (String)placement_.get(c[i]);
			if (place != null) {
				if (place.equals(HFILL)) {
					min.width = 0;
				} else if (place.equals(GLUE)) {
					min.width = 0;
					min.height = -vpad_;
				}
			}

			if (min.width > cwidth) {
				cwidth = min.width;
			}
			height += min.height + vpad_;
		}
		width += cwidth;
		return new Dimension(width, height);
	}

	public Dimension preferredLayoutSize(Container parent) {
		Insets ins = parent.getInsets();
		int width = ins.left + ins.right + 2 * hpad_;
		int height = ins.top + ins.bottom + vpad_;
		int cwidth = 0;
		Component[] c = parent.getComponents();
		if (c.length == 0) {
			height += vpad_;
		}
		for (int i = 0; i < c.length; i++) {
			Dimension pref = c[i].getPreferredSize();
			String place = (String)placement_.get(c[i]);
			if (place != null) {
				if (place.equals(HFILL)) {
					pref.width = 0;
				} else if (place.equals(GLUE)) {
					pref.width = 0;
					pref.height = -vpad_;
				}
			}

			if (pref.width > cwidth) {
				cwidth = pref.width;
			}
			height += pref.height + vpad_;
		}
		width += cwidth;
		return new Dimension(width, height);
	}

	public void removeLayoutComponent(Component comp) {
		placement_.remove(comp);
	}

	private int hpad_;
	private int vpad_;
	private HashMap<Component, String> placement_;
}

@SuppressWarnings("serial")
class AboutBox extends Frame implements ActionListener {
	public AboutBox() {
		super();
		setLayout(new VerticalLayout(25, 15));
		add(new Panel(), VerticalLayout.GLUE);

		Font ssbf14 = new Font("SansSerif", Font.BOLD, 14);
		Font ssr12 = new Font("SansSerif", Font.PLAIN, 12);
		Font ssr10 = new Font("SansSerif", Font.PLAIN, 10);

		Label aboutText = new Label ("BouMaton " + BouMaton.version(), Label.CENTER);
		aboutText.setFont(ssbf14);
		add(aboutText);

		try {
			Picture logo = new Picture(getClass().getClassLoader().getResourceAsStream("icone.gif"));
			add(logo);
		} catch (IOException ioe) {
			System.err.println("Error while loading \"icone.gif\":");
			ioe.printStackTrace();
			add(new Label("Could not find logo"));
		}


		aboutText = new Label ("Frédéric Boulanger, " + BouMaton.release(), Label.CENTER);
		aboutText.setFont(ssr12);
		add(aboutText);

		InputStream built_is = getClass().getClassLoader().getResourceAsStream("buildtime");
		String builtOn = null;
		if (built_is == null) {
			builtOn = BouMaton.getString("UnknownBuildStr");
		} else {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(built_is));
				builtOn = in.readLine();
				in.close();
			} catch (Exception e) {
				builtOn = BouMaton.getString("UnknownBuildStr");
			}
		}

		Label buildTime = new Label(BouMaton.getString("BuiltOnStr") + ": " + builtOn, Label.CENTER);
		buildTime.setFont(ssr10);
		add(buildTime);

		add(new Panel(), VerticalLayout.GLUE);

		Button okButton = new Button(BouMaton.getString("OKStr"));
		okButton.addActionListener(this);
		add(okButton);

		add(new Panel(), VerticalLayout.GLUE);

		pack();
		setSize(getMinimumSize());

		//setSize(320, 350);
	}

	public void actionPerformed(ActionEvent newEvent) {
		setVisible(false);
	}
}

class BouMatonCloser extends WindowAdapter {
	private BouMaton owner_;

	public BouMatonCloser(BouMaton owner) {
		owner_ = owner;
	}

	public void WindowClosing(WindowEvent e) {
		owner_.doClose();
	}
}

class BouMatonResizer extends ComponentAdapter {
	private Component owner_;

	public BouMatonResizer(Component owner) {
		owner_ = owner;
	}

	public void componentResized(ComponentEvent e) {
		owner_.doLayout();
	}
}

@SuppressWarnings("serial")
class DisplayStat extends Frame implements ActionListener {
	public DisplayStat() {
		super();
		setTitle(BouMaton.getString("InformationStr"));
		setLayout(null);
		phoLabel_ = new Label(BouMaton.getString("PhotoTransInfoStr"), Label.CENTER);
		Font labelFont = new Font("SansSerif", Font.BOLD, 10);
		phoLabel_.setFont(labelFont);
		add(phoLabel_);
		phoInfo_ = new TextArea("",1, 1, TextArea.SCROLLBARS_BOTH);
		phoInfo_.setEditable(false);
		phoInfo_.setBackground(SystemColor.text);
		Font infoFont = new Font("Monospaced", Font.PLAIN, 10);
		phoInfo_.setFont(infoFont);
		add(phoInfo_);
		bouLabel_ = new Label(BouMaton.getString("BoulangerTransInfoStr"), Label.CENTER);
		bouLabel_.setFont(labelFont);
		add(bouLabel_);
		bouInfo_ = new TextArea("",1, 1, TextArea.SCROLLBARS_BOTH);
		bouInfo_.setEditable(false);
		bouInfo_.setBackground(SystemColor.text);
		bouInfo_.setFont(infoFont);
		add(bouInfo_);
		Button closeButton = new Button(BouMaton.getString("CloseStr"));
		buttonPanel_ = new Panel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		buttonPanel_.add(closeButton);
		add(buttonPanel_);
		addComponentListener(new BouMatonResizer(this));
		closeButton.addActionListener(this);

		setSize(400, 300);
	}

	public void doLayout() {
		Dimension size = getSize();
		Insets ins = getInsets();
		bounds_ = new Rectangle(ins.left, ins.top,
				size.width - ins.left - ins.right,
				size.height - ins.top - ins.bottom);
		buttonPanel_.doLayout();
		Dimension buttonDim = buttonPanel_.getPreferredSize();
		bounds_.height -= buttonDim.height;
		Dimension labelDim = bouLabel_.getPreferredSize();
		int txtHeight = (bounds_.height - 2 * labelDim.height) / 2 - 3;
		phoLabel_.setBounds(bounds_.x, bounds_.y,
				bounds_.width, labelDim.height);
		phoInfo_.setBounds(bounds_.x, bounds_.y + labelDim.height,
				bounds_.width, txtHeight);
		bouLabel_.setBounds(bounds_.x, bounds_.y + txtHeight + labelDim.height + 6,
				bounds_.width, labelDim.height);
		bouInfo_.setBounds(bounds_.x, bounds_.y + 2*labelDim.height + txtHeight + 6,
				bounds_.width, txtHeight);
		buttonPanel_.setBounds(bounds_.x, bounds_.y + bounds_.height, 
				bounds_.width, buttonDim.height);
	}

	public void paint(Graphics g) {
		super.paint(g);
		g.drawLine(bounds_.x, bounds_.height / 2,
				bounds_.x + bounds_.width, bounds_.height / 2);
	}

	public void append(String str, String where) {
		TextArea txt;
		if (where.compareTo("pho") == 0) {
			txt = phoInfo_;
		} else if (where.compareTo("bou") == 0) {
			txt = bouInfo_;
		} else {
			return;
		}
		txt.append(str + System.getProperty("line.separator"));
		txt.setRows(txt.getRows() + 1);
		int strlen = str.length();
		int cols = txt.getColumns();
		txt.setColumns((strlen > cols) ? strlen : cols);
	}

	public void clear() {
		phoInfo_.setText(null);
		bouInfo_.setText(null);
		setTitle(BouMaton.getString("InformationStr"));
	}

	public void actionPerformed(ActionEvent newEvent) {
		setVisible(false);
	}

	protected Label phoLabel_;
	protected TextArea phoInfo_;
	protected Label bouLabel_;
	protected TextArea bouInfo_;
	protected Rectangle bounds_;
	protected Panel buttonPanel_;
}

@SuppressWarnings("serial")
class History extends Frame implements ActionListener {
	public History() {
		super();
		setTitle(BouMaton.getString("HistoryStr"));
		setLayout(null);
		histDisp_ = new TextArea("",1, 1, TextArea.SCROLLBARS_BOTH);
		histDisp_.setEditable(false);
		histDisp_.setBackground(SystemColor.text);
		Font infoFont = new Font("Monospaced", Font.PLAIN, 10);
		histDisp_.setFont(infoFont);
		add(histDisp_);
		Button closeButton = new Button(BouMaton.getString("CloseStr"));
		buttonPanel_ = new Panel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		buttonPanel_.add(closeButton);
		add(buttonPanel_);
		addComponentListener(new BouMatonResizer(this));
		closeButton.addActionListener(this);
		history_ = new LinkedList<>();

		setSize(400, 300);
	}

	public void doLayout() {
		Dimension size = getSize();
		Insets ins = getInsets();
		bounds_ = new Rectangle(ins.left, ins.top,
				size.width - ins.left - ins.right,
				size.height - ins.top - ins.bottom);
		buttonPanel_.doLayout();
		Dimension buttonDim = buttonPanel_.getPreferredSize();
		bounds_.height -= buttonDim.height;
		histDisp_.setBounds(bounds_.x, bounds_.y,
				bounds_.width, bounds_.height);
		buttonPanel_.setBounds(bounds_.x, bounds_.y + bounds_.height, 
				bounds_.width, buttonDim.height);
	}

	protected void append(String str) {
		histDisp_.append(str + System.getProperty("line.separator"));
		histDisp_.setRows(histDisp_.getRows() + 1);
		int strlen = str.length();
		int cols = histDisp_.getColumns();
		histDisp_.setColumns((strlen > cols) ? strlen : cols);
	}

	public void add(String  transf, long niter, long optiter, long time) {
		HistAction act = new HistAction(transf, niter, optiter, time);
		if (history_.size() == 0) {
			history_.addLast(act);
			append(act.toString());
		} else {
			act = ((HistAction)(history_.getLast())).merge(act);
			if (act != null) {
				history_.addLast(act);
				append(act.toString());
			} else {
				Iterator<HistAction> iter = history_.iterator();
				clearText();
				while (iter.hasNext()) {
					act = (HistAction)(iter.next());
					if (act.optIters == 0) {
						iter.remove();
					} else {
						append(act.toString());
					}
				}
			}
		}
	}

	protected void clearText() {
		histDisp_.setText(null);
		histDisp_.setRows(1);
		histDisp_.setColumns(1);
	}

	public void clear() {
		clearText();
		history_.clear();
	}

	public void actionPerformed(ActionEvent newEvent) {
		setVisible(false);
	}

	protected class HistAction {
		public HistAction(String name, long n, long optn, long time) {
			transf = name;
			iters = n;
			optIters = optn;
			millisec = time;
		}

		public HistAction merge(HistAction act) {
			if (act.transf.equals(transf)) {
				iters += act.iters;
				optIters += act.optIters;
				millisec += act.millisec;
				return null;
			} else {
				return act;
			}
		}

		public String fmtTime(long millis) {
			millis /= 10;
			long sec = millis / 100;
			millis %= 100;
			long min = sec / 60;
			sec %= 60;
			StringBuffer buf = new StringBuffer();
			buf.append(Long.toString(min));
			buf.append("'");
			if (sec < 10) {
				buf.append('0');
			}
			buf.append(Long.toString(sec));
			buf.append('.');
			if (millis < 10) {
				buf.append('0');
			}
			buf.append(Long.toString(millis));
			buf.append('"');
			return buf.toString();
		}

		public String toString() {
			return transf + " x " + iters + " (" + optIters + ") "
					+ BouMaton.getString("InTimeStr") + " " + fmtTime(millisec);
		}

		public String transf;
		public long iters;
		public long optIters;
		public long millisec;
	}

	protected TextArea histDisp_;
	protected Rectangle bounds_;
	protected Panel buttonPanel_;
	protected LinkedList<HistAction> history_;
}
